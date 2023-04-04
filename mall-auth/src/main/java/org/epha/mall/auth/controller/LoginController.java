package org.epha.mall.auth.controller;

import org.epha.common.constant.AuthConstant;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.utils.R;
import org.epha.mall.auth.feign.ThirdPartyFeignService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author pangjiping
 */
@Controller
public class LoginController {

    @Resource
    ThirdPartyFeignService thirdPartyFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("/sms/code")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber) {

        // TODO: 接口防刷，检查上次发送验证码的时间
        String redisCode = stringRedisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phoneNumber);
        if (StringUtils.hasText(redisCode)) {
            long cachedTimestamp = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - cachedTimestamp < 60000) {
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

        thirdPartyFeignService.sendCaptcha(phoneNumber, code);

        return R.ok();
    }


}
