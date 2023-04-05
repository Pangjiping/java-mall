package org.epha.mall.auth.feign;

import org.epha.common.utils.R;
import org.epha.mall.auth.vo.UserLoginVo;
import org.epha.mall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author pangjiping
 */
@FeignClient("java-mall-member")
public interface MemberFeignService {
    @PostMapping("member/member/register")
    R register(@RequestBody UserRegisterVo registerVo);

    @PostMapping("member/member//login")
    R login(@RequestBody UserLoginVo loginVo);
}
