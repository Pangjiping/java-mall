package org.epha.mall.elasticsearch.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.utils.R;
import org.epha.mall.elasticsearch.constant.ProductSearchOptions;
import org.epha.mall.elasticsearch.feign.ProductFeignService;
import org.epha.mall.elasticsearch.service.MallSearchService;
import org.epha.mall.elasticsearch.to.AttrResponseTo;
import org.epha.mall.elasticsearch.to.SkuElasticsearchModel;
import org.epha.mall.elasticsearch.vo.SearchParam;
import org.epha.mall.elasticsearch.vo.SearchResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author pangjiping
 */
@Service
@Slf4j
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    ElasticsearchClient elasticsearchClient;

    @Resource
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) throws IOException {
        // 根据SearchParam构建查询需要的DSL语句
        SearchRequest searchRequest = buildSearchRequest(param);

        // 执行查询操作
        SearchResponse<SkuElasticsearchModel> searchResponse = elasticsearchClient.search(searchRequest, SkuElasticsearchModel.class);
        log.debug("searchResponse: {}", searchResponse);


        // 分析响应数据，封装成我们需要的格式
        return buildSearchResult(searchResponse, param);
    }

    /**
     * 构建检索请求
     * 模糊匹配、过滤、排序、分页、高亮、聚合分析
     * TODO: stream流构建有个bug，当不传条件时会无法构建请求
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        // 构造DSL语句
        return SearchRequest.of(request -> request
                .index(ProductSearchOptions.INDEX)
                .query(queryBuilder -> queryBuilder
                        .bool(boolBuilder -> {
                            fetchMustBySkuTitle(param).ifPresent(boolBuilder::must);
                            fetchFilterTermByCatalogId(param).ifPresent(boolBuilder::filter);
                            fetchFilterTermsByBrandId(param).ifPresent(boolBuilder::filter);
                            fetchByFilterNestedAttrs(param).ifPresent(boolBuilder::filter);
                            fetchFilterTermByHasStock(param).ifPresent(boolBuilder::filter);
                            fetchFilterRangeBySkuPrice(param).ifPresent(boolBuilder::filter);
                            return boolBuilder;
                        })
                )
                .from(fetchPageFrom(param))
                .size(ProductSearchOptions.PAGE_SIZE)
                .sort(sortBuilder -> {
                    if (!StringUtils.hasText(param.getSort())) {
                        return sortBuilder;
                    }
                    String[] s = param.getSort().split(ProductSearchOptions.SORT_DELIMITER);
                    if (s.length == 0) {
                        return sortBuilder;
                    }
                    sortBuilder.field(FieldSort.of(fieldSortBuilder -> {
                                fieldSortBuilder.field(s[0]);
                                if (s.length > 1 && s[1].equalsIgnoreCase(ProductSearchOptions.SORT_ASC)) {
                                    fieldSortBuilder.order(SortOrder.Asc);
                                } else {
                                    fieldSortBuilder.order(SortOrder.Desc);
                                }
                                return fieldSortBuilder;
                            })
                    );

                    return sortBuilder;
                })
                .highlight(highlightBuilder -> {
                    if (!StringUtils.hasText(param.getKeyword())) {
                        return highlightBuilder;
                    }
                    highlightBuilder.fields(ProductSearchOptions.SKU_TITLE, fieldBuilder ->
                            fieldBuilder.preTags("<b style='color:red'>")
                                    .postTags("</b>")
                    );
                    return highlightBuilder;
                })
                .aggregations(ProductSearchOptions.AGGREGATIONS_BRAND, brandAggBuilder -> brandAggBuilder
                        .terms(brandTermsBuilder -> brandTermsBuilder
                                .field(ProductSearchOptions.BRAND_ID)
                                .size(10)
                        )
                        .aggregations(ProductSearchOptions.AGGREGATIONS_BRAND_NAME, brandNameAggBuilder -> brandNameAggBuilder
                                .terms(brandNameTermsBuilder -> brandNameTermsBuilder
                                        .field(ProductSearchOptions.BRAND_NAME)
                                        .size(10)
                                )
                        )
                        .aggregations(ProductSearchOptions.AGGREGATIONS_BRAND_IMG, brandImgAggBuilder -> brandImgAggBuilder
                                .terms(brandImgTermsBuilder -> brandImgTermsBuilder
                                        .field(ProductSearchOptions.BRAND_IMG)
                                        .size(10)
                                )
                        )
                )
                .aggregations(ProductSearchOptions.AGGREGATIONS_CATALOG, catalogAggBuilder -> catalogAggBuilder
                        .terms(catalogTermsBuilder -> catalogTermsBuilder
                                .field(ProductSearchOptions.CATALOG_ID)
                                .size(10)
                        )
                )
                .aggregations(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR, aggBuilder -> aggBuilder
                        .nested(attrNestedBuilder -> attrNestedBuilder
                                .path(ProductSearchOptions.Attrs.ATTR)
                        )
                        .aggregations(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR_ID, attrIdAggBuilder -> attrIdAggBuilder
                                .terms(attrIdTermsBuilder -> attrIdTermsBuilder
                                        .field(ProductSearchOptions.Attrs.ATTR_ID)
                                        .size(10)
                                )
                                .aggregations(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR_NAME, attrNameAggBuilder -> attrNameAggBuilder
                                        .terms(attrNameTermsBuilder -> attrNameTermsBuilder
                                                .field(ProductSearchOptions.Attrs.ATTR_NAME)
                                                .size(10)
                                        )
                                )
                                .aggregations(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR_VALUE, attrValueAggBuilder -> attrValueAggBuilder
                                        .terms(attrValueTermsBuilder -> attrValueTermsBuilder
                                                .field(ProductSearchOptions.Attrs.ATTR_VALUE)
                                                .size(10)
                                        )
                                )
                        )
                )
        );
    }

    /**
     * 封装返回结果
     */
    private SearchResult buildSearchResult(SearchResponse<SkuElasticsearchModel> searchResponse, SearchParam param) {
        SearchResult searchResult = new SearchResult();

        // 查询到的所有商品
        List<SkuElasticsearchModel> productList = searchResponse.hits().hits()
                .stream()
                .map(skuElasticsearchModelHit -> {
                    SkuElasticsearchModel skuElasticsearchModel = new SkuElasticsearchModel();
                    if (skuElasticsearchModelHit.source() != null) {
                        BeanUtils.copyProperties(skuElasticsearchModelHit.source(), skuElasticsearchModel);
                    }
                    return skuElasticsearchModel;
                })
                .collect(Collectors.toList());
        searchResult.setProducts(productList);

        // 当前所有商品涉及到的属性信息
        NestedAggregate attrAgg = searchResponse.aggregations().get(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR).nested();
        LongTermsAggregate attrIdAgg = attrAgg.aggregations().get(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR_ID).lterms();

        List<SearchResult.AttrVo> attrVos = attrIdAgg.buckets().array().stream()
                .map(longTermsBucket -> {

                    SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
                    attrVo.setAttrId(longTermsBucket.key());

                    // 设置attrName
                    StringTermsAggregate attrNameAgg = longTermsBucket.aggregations().get(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR_NAME).sterms();
                    attrVo.setAttrName(attrNameAgg.buckets().array().stream()
                            .map(StringTermsBucket::key)
                            .findFirst().orElseGet(() -> "")
                    );

                    // 设置attrValue
                    StringTermsAggregate attrValueAgg = longTermsBucket.aggregations().get(ProductSearchOptions.Attrs.AGGREGATIONS_ATTR_VALUE).sterms();
                    attrVo.setAttrValue(attrValueAgg.buckets().array().stream()
                            .map(StringTermsBucket::key)
                            .collect(Collectors.toList())
                    );

                    return attrVo;
                }).collect(Collectors.toList());
        searchResult.setAttrVos(attrVos);

        // 当前所有商品涉及到的品牌信息
        LongTermsAggregate brandAgg = searchResponse.aggregations().get(ProductSearchOptions.AGGREGATIONS_BRAND).lterms();
        List<SearchResult.BrandVo> brandVos = brandAgg.buckets().array().stream()
                .map(longTermsBucket -> {

                    SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                    brandVo.setBrandId(longTermsBucket.key());

                    // 设置brandImg
                    StringTermsAggregate brandImgAgg = longTermsBucket.aggregations().get(ProductSearchOptions.AGGREGATIONS_BRAND_IMG).sterms();
                    brandVo.setBrandImg(brandImgAgg.buckets().array().stream()
                            .map(StringTermsBucket::key)
                            .findFirst().orElseGet(() -> "")
                    );

                    // 设置brandName
                    StringTermsAggregate brandNameAgg = longTermsBucket.aggregations().get(ProductSearchOptions.AGGREGATIONS_BRAND_NAME).sterms();
                    brandVo.setBrandName(brandNameAgg.buckets().array().stream()
                            .map(StringTermsBucket::key)
                            .findFirst().orElseGet(() -> "")
                    );

                    return brandVo;
                }).collect(Collectors.toList());
        searchResult.setBrandVos(brandVos);


        // 当前所有商品涉及到的分类信息
        LongTermsAggregate catalogAgg = searchResponse.aggregations().get(ProductSearchOptions.AGGREGATIONS_CATALOG).lterms();
        List<SearchResult.CatalogVo> catalogVos = catalogAgg.buckets().array()
                .stream()
                .map(longTermsBucket -> {
                    SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                    catalogVo.setCatalogId(longTermsBucket.key());
                    return catalogVo;
                })
                .collect(Collectors.toList());
        searchResult.setCatalog(catalogVos);

        // 分页信息-页码
        searchResult.setPageNum(param.getPageNum());

        // 分页信息-total
        searchResult.setTotal(searchResponse.hits().total().value());

        // 分页信息-总页码
        if (searchResult.getTotal() == 0) {
            searchResult.setTotalPages(0);
        } else {
            searchResult.setTotalPages((int) (searchResult.getTotal() % ProductSearchOptions.PAGE_SIZE == 0 ?
                    searchResult.getTotal() / ProductSearchOptions.PAGE_SIZE :
                    (searchResult.getTotal() / ProductSearchOptions.PAGE_SIZE + 1)));
        }

        // 构建面包屑导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = buildSearchResultNav(param);
            searchResult.setNavs(navVos);
        }

        return searchResult;
    }

    /**
     * 构建模糊查询条件 SkuTitle
     */
    private Optional<Query> fetchMustBySkuTitle(SearchParam param) {

        Query bySkuTitle = null;

        if (StringUtils.hasText(param.getKeyword())) {
            bySkuTitle = MatchQuery.of(builder -> builder
                    .field(ProductSearchOptions.SKU_TITLE)
                    .query(param.getKeyword())
            )._toQuery();
        }

        return Optional.ofNullable(bySkuTitle);
    }

    /**
     * 构建过滤条件CatalogId terms查询
     */
    private Optional<Query> fetchFilterTermByCatalogId(SearchParam param) {
        Query byCatalogId = null;

        if (param.getCatalog3Id() != null) {
            byCatalogId = TermQuery.of(builder -> builder
                    .field(ProductSearchOptions.CATALOG_ID)
                    .value(param.getCatalog3Id())
            )._toQuery();
        }

        return Optional.ofNullable(byCatalogId);
    }

    /**
     * 构建过滤条件BrandId terms查询
     */
    private Optional<Query> fetchFilterTermsByBrandId(SearchParam param) {

        Query byBrandId = null;

        if (param.getBrandId() != null && param.getBrandId().size() > 0) {

            // 构造查询值
            TermsQueryField termsQueryField = TermsQueryField.of(builder -> builder
                    .value(param.getBrandId()
                            .stream()
                            .map(FieldValue::of)
                            .collect(Collectors.toList()))
            );

            byBrandId = TermsQuery.of(builder -> builder
                    .field(ProductSearchOptions.BRAND_ID)
                    .terms(termsQueryField)
            )._toQuery();
        }

        return Optional.ofNullable(byBrandId);
    }

    /**
     * 构建嵌套过滤条件 包含terms查询
     */
    private Optional<List<Query>> fetchByFilterNestedAttrs(SearchParam param) {
        List<Query> byAttrs = new ArrayList<>();

        if (param.getAttrs() != null && param.getAttrs().size() > 0) {

            // 分割attrs数组
            param.getAttrs().forEach(attrStr -> {
                String[] s = attrStr.split(ProductSearchOptions.Attrs.ATTR_ID_VALUES_DELIMITER);
                if (s.length <= 0) {
                    return;
                }

                Query byAttrId;
                Query byAttrValue = null;

                if (s.length == 2) {
                    byAttrId = TermQuery.of(builder -> builder
                            .field(ProductSearchOptions.Attrs.ATTR_ID)
                            .value(s[0])
                    )._toQuery();

                    String[] attrValues = s[1].split(ProductSearchOptions.Attrs.ATTR_VALUES_DELIMITER);
                    TermsQueryField termsQueryField = TermsQueryField.of(builder -> builder
                            .value(Arrays.stream(attrValues)
                                    .map(FieldValue::of)
                                    .collect(Collectors.toList()))
                    );
                    byAttrValue = TermsQuery.of(builder -> builder
                            .field(ProductSearchOptions.Attrs.ATTR_VALUE)
                            .terms(termsQueryField)
                    )._toQuery();
                } else if (s.length == 1) {
                    byAttrId = TermQuery.of(builder -> builder
                            .field(ProductSearchOptions.Attrs.ATTR_ID)
                            .value(s[0])
                    )._toQuery();
                } else {
                    return;
                }

                // 把查询条件放到嵌套条件里面
                Query finalByAttrId = byAttrId;
                Query finalByAttrValue = byAttrValue;
                Query attrs = NestedQuery.of(builder -> builder
                        .path(ProductSearchOptions.Attrs.ATTR)
                        .query(queryBuilder -> queryBuilder
                                .bool(boolBuilder -> {
                                    if (finalByAttrId != null) {
                                        boolBuilder.must(finalByAttrId);
                                    }
                                    if (finalByAttrValue != null) {
                                        boolBuilder.must(finalByAttrValue);
                                    }
                                    return boolBuilder;
                                })
                        )
                )._toQuery();

                byAttrs.add(attrs);
            });
        }

        return byAttrs.size() > 0 ? Optional.of(byAttrs) : Optional.empty();
    }

    /**
     * 构建过滤条件HasStock
     */
    private Optional<Query> fetchFilterTermByHasStock(SearchParam param) {

        Query byHasStock = null;

        byHasStock = TermQuery.of(builder -> builder
                .field(ProductSearchOptions.HAS_STOCK)
                .value(param.getHasStock() != 0)
        )._toQuery();

        return Optional.ofNullable(byHasStock);
    }

    /**
     * 构建过滤条件SkuPrice range
     */
    private Optional<Query> fetchFilterRangeBySkuPrice(SearchParam param) {
        Query bySkuPrice = null;

        if (StringUtils.hasText(param.getSkuPrice())) {
            String[] s = param.getSkuPrice().split(ProductSearchOptions.SKU_PRICE_DELIMITER);

            if (s.length == 2) {
                bySkuPrice = RangeQuery.of(builder -> builder
                        .field(ProductSearchOptions.SKU_PRICE)
                        .gte(JsonData.of(s[0]))
                        .lte(JsonData.of(s[1]))
                )._toQuery();
            } else if (s.length == 1 && param.getSkuPrice().startsWith(ProductSearchOptions.SKU_PRICE_DELIMITER)) {
                bySkuPrice = RangeQuery.of(builder -> builder
                        .field(ProductSearchOptions.SKU_PRICE)
                        .lte(JsonData.of(s[0]))
                )._toQuery();
            } else if (s.length == 1 && param.getSkuPrice().endsWith(ProductSearchOptions.SKU_PRICE_DELIMITER)) {
                bySkuPrice = RangeQuery.of(builder -> builder
                        .field(ProductSearchOptions.SKU_PRICE)
                        .gte(JsonData.of(s[0]))
                )._toQuery();
            }
        }

        return Optional.ofNullable(bySkuPrice);
    }

    /**
     * 构建起始页
     */
    private Integer fetchPageFrom(SearchParam param) {
        return (param.getPageNum() - 1) * ProductSearchOptions.PAGE_SIZE;
    }

    /**
     * 构建面包屑导航
     */
    private List<SearchResult.NavVo> buildSearchResultNav(SearchParam param) {

        List<SearchResult.NavVo> navVos = param.getAttrs().stream()
                .map(attr -> {
                    SearchResult.NavVo navVo = new SearchResult.NavVo();

                    // 分析每个attr传过来的查询参数值
                    String[] s = attr.split(ProductSearchOptions.Attrs.ATTR_ID_VALUES_DELIMITER);
                    navVo.setNavValue(s[1]);

                    // 远程调用商品模块进行查询
                    R r = productFeignService.getAttrInfo(Long.parseLong(s[0]));
                    if (r.getCode() == 0) {
                        AttrResponseTo rData = r.getData("attr", new TypeReference<AttrResponseTo>() {
                        });

                        navVo.setNavName(rData.getAttrName());
                    } else {
                        navVo.setNavName(s[0]);
                    }

                    // 取消了面包屑之后，要跳转到哪里
                    // 去掉当前查询条件即可
                    try {
                        String encodedAttr = URLEncoder.encode(attr, "UTF-8");
                        String newQueryString = param.get_queryString().replace("&attrs=" + encodedAttr, "");
                        navVo.setLink("http://localhost:8888/api/search/list.html?" + newQueryString);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    return navVo;
                })
                .collect(Collectors.toList());

        return navVos;
    }
}
