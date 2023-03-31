package org.epha.mall.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.cat.NodesResponse;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.epha.mall.elasticsearch.usage.Product;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
class MallElasticsearchApplicationTests {

    @Resource
    ElasticsearchClient elasticsearchClient;

    @Test
    void contextLoads() {
        System.out.println(elasticsearchClient);
    }

    @Test
    public void indexUsingFluentDSLTest() throws IOException {
        Product product = new Product("bk-1", "City bike", 123.0);

        IndexRequest<Object> request = IndexRequest.of(i -> i
                .index("products")
                .id(product.getSku())
                .document(product));

        IndexResponse indexResponse = elasticsearchClient.index(request);
        log.info("indexResponse " + indexResponse);
    }

    private List<Product> fetchProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("bk-1", "City Bike", 123.0));
        list.add(new Product("bk-2", "Mountain Bike", 134.0));
        return list;
    }

    @Test
    public void bulkIndexUsingFluentDSLTest() throws IOException {
        List<Product> products = fetchProducts();

        BulkRequest.Builder builder = new BulkRequest.Builder();

        products.forEach(product ->
                builder.operations(op -> op
                        .index(idx -> idx
                                .index("products")
                                .id(product.getSku())
                                .document(product)
                        )
                )
        );

        BulkResponse bulkResponse = elasticsearchClient.bulk(builder.build());

        if (bulkResponse.errors()) {
            log.error("Bulk had errors");
            bulkResponse.items().forEach(bulkResponseItem -> {
                assert bulkResponseItem.error() != null;
                log.error(bulkResponseItem.error().reason());
            });
        }
    }

    @Test
    public void getUsingFluentDSLTest() throws IOException {
        // 构建请求
        GetRequest getRequest = GetRequest.of(g -> g
                .index("products")
                .id("bk-1")
        );

        // 发送GET请求，表明映射到的类
        GetResponse<Product> getResponse = elasticsearchClient.get(getRequest, Product.class);

        if (getResponse.found()) {
            Product product = getResponse.source();
            log.info("Product name " + product.getName());
        } else {
            log.info("Product not found");
        }
    }

    @Test
    public void simpleSearchQueryUsingFluentDSLTest() throws IOException {
        String searchText = "bike";

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("products")
                .query(q -> q
                        .match(t -> t
                                .field("name")
                                .query(searchText)
                        )
                )
        );
        log.info("searchRequest: " + searchRequest.toString());

        SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);

        TotalHits totalHits = searchResponse.hits().total();
        boolean isExactResult = totalHits.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            log.info("There are " + totalHits.value() + " results");
        } else {
            log.info("There are more than " + totalHits.value() + " results");
        }

        searchResponse.hits().hits().forEach(hit -> {
            Product product = hit.source();
            log.info("Found product " + product.getSku() + ", score " + hit.score());
        });
    }

    @Test
    public void nestedSearchQueryUsingFluentDSLTest() throws IOException {
        String searchText = "bike";
        double maxPrice = 200.0;

        // Search by product name
        Query byName = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        )._toQuery();

        // Search by max price
        Query byMaxPrice = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of(maxPrice))
        )._toQuery();

        // Combine name and price queries to search the product index
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("products")
                .query(q -> q
                        .bool(b -> b
                                .must(byName)
                                .must(byMaxPrice)
                        )
                )
        );
        log.info("searchRequest: " + searchRequest.toString());

        SearchResponse<Product> searchResponse = elasticsearchClient.search(searchRequest, Product.class);

        searchResponse.hits().hits().forEach(productHit -> {
            Product product = productHit.source();
            log.info("Found product " + product.getSku() + ", score " + productHit.score());
        });
    }

    @Test
    public void simpleAggregationUsingFluentDSLTest() throws IOException {
        String searchText = "bike";

        Query byName = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        )._toQuery();

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("products")
                .size(0)
                .query(byName)
                .aggregations("price-histogram", a -> a
                        .histogram(h -> h
                                .field("price")
                                .interval(50.0)
                        )
                )
        );
        log.info("searchRequest: " + searchRequest.toString());

        SearchResponse<Void> searchResponse = elasticsearchClient.search(searchRequest, Void.class);

        searchResponse.aggregations()
                .get("price-histogram")
                .histogram()
                .buckets()
                .array().forEach(histogramBucket -> {
            log.info("There are " + histogramBucket.docCount() +
                    " bikes under " + histogramBucket.key());
        });
    }

    @Test
    public void connectionTest() throws IOException {
        NodesResponse nodes = elasticsearchClient.cat().nodes();
        System.out.println(nodes);
    }

}
