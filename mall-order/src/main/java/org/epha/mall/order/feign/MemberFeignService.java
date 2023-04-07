package org.epha.mall.order.feign;

import org.epha.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author pangjiping
 */
@FeignClient("java-mall-member")
public interface MemberFeignService {
    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    R getAddress(@PathVariable("memberId") Long memberId);

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    public R info(@PathVariable("id") Long id);
}
