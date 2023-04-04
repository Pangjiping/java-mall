package org.epha.mall.auth.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author pangjiping
 */
@FeignClient("mall-third-party")
public interface ThirdPartyFeignService {
    @GetMapping("/sms/code")
    R sendCaptcha(@RequestParam("phoneNumber") String phoneNumber,
                  @RequestParam("code") String code);
}
