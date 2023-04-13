package org.epha.mall.seckill.feign;

import org.epha.common.utils.R;
import org.epha.mall.seckill.feign.fallback.CouponFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author pangjiping
 */
@FeignClient(value = "java-mall-coupon",fallback = CouponFeignServiceFallback.class)
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/threedays/session")
    R listThreeDaysSession();
}
