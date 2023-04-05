package org.epha.mall.auth.controller;

import org.epha.common.constant.AuthConstant;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.utils.R;
import org.epha.mall.auth.feign.MemberFeignService;
import org.epha.mall.auth.feign.ThirdPartyFeignService;
import org.epha.mall.auth.vo.UserLoginVo;
import org.epha.mall.auth.vo.UserRegisterVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author pangjiping
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    ThirdPartyFeignService thirdPartyFeignService;

    @Resource
    MemberFeignService memberFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/sms/code")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber) {

        // TODO: 接口防刷，检查上次发送验证码的时间
        String redisCode = stringRedisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phoneNumber);
        if (StringUtils.hasText(redisCode)) {
            long cachedTimestamp = Long.parseLong(redisCode.split(AuthConstant.SMS_CODE_TIMESTAMP_DELIMITER)[1]);
            if (System.currentTimeMillis() - cachedTimestamp < AuthConstant.SMS_CODE_RESEND_INTERVAL) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5);
        String cachedCode = code + "_" + System.currentTimeMillis();

        // 验证码放到redis里面
        stringRedisTemplate.opsForValue()
                .set(AuthConstant.SMS_CODE_CACHE_PREFIX + phoneNumber,
                        cachedCode,
                        10,
                        TimeUnit.MINUTES);

        // 异步发送验证码
        threadPoolExecutor.execute(() ->
                thirdPartyFeignService.sendCaptcha(phoneNumber, code)
        );

        return R.ok();
    }

    @PostMapping("/reg")
    public R register(@Valid @RequestBody UserRegisterVo userRegister) {

        // 校验验证码
        String code = stringRedisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegister.getPhone());
        if (StringUtils.hasText(code)) {
            if (userRegister.getCode().equals(code.split(AuthConstant.SMS_CODE_TIMESTAMP_DELIMITER)[0])) {
                // 删除验证码[异步]
                threadPoolExecutor.execute(() ->
                        stringRedisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegister.getPhone())
                );

                // 验证码通过，调用member远程服务进行注册
                R r = memberFeignService.register(userRegister);
                if (r.getCode() == 0) {
                    // 成功
                    return R.ok();
                } else {
                    return r;
                }

            } else {
                return R.error(BizCodeEnum.SMS_CODE_MISMATCH_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_MISMATCH_EXCEPTION.getMessage());
            }
        } else {
            return R.error(BizCodeEnum.SMS_CODE_MISMATCH_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_MISMATCH_EXCEPTION.getMessage());
        }
    }

    @PostMapping("/login")
    public R login(@Valid @RequestBody UserLoginVo loginVo) {

        // 远程登录
        R r = memberFeignService.login(loginVo);

        // 页面跳转之类的，没写

        return r;
    }

}
