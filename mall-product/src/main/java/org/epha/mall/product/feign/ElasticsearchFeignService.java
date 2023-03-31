package org.epha.mall.product.feign;

import org.epha.common.utils.R;
import org.epha.mall.product.to.SkuElasticsearchModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("java-mall-elasticsearch")
public interface ElasticsearchFeignService {
    @PostMapping("/search/product")
    R productStatusUp(@RequestBody List<SkuElasticsearchModel> models);
}
