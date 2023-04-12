package org.epha.mall.member.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author pangjiping
 */
@FeignClient("java-mall-order")
public interface OrderFeignService {
    @PostMapping("/order/order/listwithItem")
    R listWithItems(@RequestParam Map<String,Object> params);
}
