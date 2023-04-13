package org.epha.mall.seckill.feign.fallback;

import lombok.extern.slf4j.Slf4j;
import org.epha.common.utils.R;
import org.epha.mall.seckill.feign.CouponFeignService;
import org.springframework.stereotype.Component;

/**
 * @author pangjiping
 */
@Component
@Slf4j
public class CouponFeignServiceFallback implements CouponFeignService {
    @Override
    public R listThreeDaysSession() {
        log.info("CouponFeignService接口熔断");
        return R.error();
    }
}
