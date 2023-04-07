package org.epha.mall.order.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author pangjiping
 */
@FeignClient("java-mall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/{skuId}/info")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId);
}
