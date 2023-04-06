package org.epha.mall.order.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("java-mall-cart")
public interface CartFeignService {
    @GetMapping("/cart/check/items")
    R getCheckedItems();
}
