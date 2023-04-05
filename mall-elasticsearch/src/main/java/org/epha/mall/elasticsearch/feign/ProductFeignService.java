package org.epha.mall.elasticsearch.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author pangjiping
 */
@FeignClient("java-mall-product")
public interface ProductFeignService {
    @GetMapping("/product/attr/info/{attrId}")
    R getAttrInfo(@PathVariable("attrId") Long attrId);
}
