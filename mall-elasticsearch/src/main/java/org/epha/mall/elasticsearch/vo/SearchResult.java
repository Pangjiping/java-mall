package org.epha.mall.elasticsearch.vo;

import lombok.Data;
import org.epha.mall.elasticsearch.to.SkuElasticsearchModel;

import java.util.List;

/**
 * @author pangjiping
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuElasticsearchModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 当前查到的结果，所有涉及到的品牌
     */
    private List<BrandVo> brandVos;

    /**
     * 当前查到的结果，所有涉及到的属性
     */
    private List<AttrVo> attrVos;

    /**
     * 当前查到的结果，所有涉及到的分类
     */
    private List<CatalogVo> catalog;

    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs;

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;

    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

}
