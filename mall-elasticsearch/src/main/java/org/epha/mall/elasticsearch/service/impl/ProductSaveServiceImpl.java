package org.epha.mall.elasticsearch.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.extern.slf4j.Slf4j;
import org.epha.mall.elasticsearch.constant.ProductSearchOptions;
import org.epha.mall.elasticsearch.service.ProductSaveService;
import org.epha.mall.elasticsearch.to.SkuElasticsearchModel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author pangjiping
 */
@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    ElasticsearchClient elasticsearchClient;

    @Override
    public boolean productStatusUp(List<SkuElasticsearchModel> models) throws IOException {
        // 数据保存到es中

        // 给es中建立映射关系，已经建立好了
        // 保存数据
        BulkRequest.Builder builder = new BulkRequest.Builder();
        models.forEach(model ->
                builder.operations(op -> op
                        .index(idx -> idx
                                .index(ProductSearchOptions.INDEX)
                                .id(model.getSkuId().toString()).document(model)
                        )
                )
        );

        BulkResponse bulkResponse = elasticsearchClient.bulk(builder.build());

        // TODO: 怎么处理错误
        if (bulkResponse.errors()) {
            log.error("商品上架错误");
            bulkResponse.items().forEach(bulkResponseItem -> {
                assert bulkResponseItem.error() != null;
                log.error(bulkResponseItem.error().reason());
            });
        }

        return !bulkResponse.errors();
    }
}
