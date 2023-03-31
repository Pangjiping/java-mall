package org.epha.mall.elasticsearch.service;

import org.epha.mall.elasticsearch.to.SkuElasticsearchModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuElasticsearchModel> models) throws IOException;
}
