package org.epha.mall.member.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("java-mall-coupon")
public interface CouponFeignService {
    @RequestMapping("/coupon/coupon/list")
    //@RequiresPermissions("coupon:coupon:list")
    R list(@RequestParam Map<String, Object> params);
}
