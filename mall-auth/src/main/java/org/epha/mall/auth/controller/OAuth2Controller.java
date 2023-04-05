package org.epha.mall.auth.controller;

import org.epha.common.exception.BizCodeEnum;
import org.epha.common.utils.R;
import org.epha.mall.auth.feign.MemberFeignService;
import org.epha.mall.auth.service.WeiboOAuth2Service;
import org.epha.mall.auth.vo.WeiboUserLoginVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@RestController
@RequestMapping("/oauth2.0")
public class OAuth2Controller {

    @Resource
    WeiboOAuth2Service weiboOAuth2Service;

    @Resource
    MemberFeignService memberFeignService;

    @GetMapping("/weibo/success")
    public R weibo(@RequestParam("code") String code) throws Exception {

        // 根据code换取access-token
        WeiboUserLoginVo weiboUser = weiboOAuth2Service.getAccessToken(code);
        if (weiboUser == null) {
            return R.error(BizCodeEnum.RELOGIN_EXCEPTION.getCode(), BizCodeEnum.RELOGIN_EXCEPTION.getMessage());
        }

        // 登录或者注册
        R r = memberFeignService.OAuthWeiboLogin(weiboUser);
        if (r.getCode() != 0) {
            return r;
        }

        // 登陆成功，跳到首页
        return R.ok();
    }
}
