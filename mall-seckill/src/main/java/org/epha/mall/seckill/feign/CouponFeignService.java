package org.epha.mall.seckill.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author pangjiping
 */
@FeignClient("java-mall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/threedays/session")
    R listThreeDaysSession();
}
