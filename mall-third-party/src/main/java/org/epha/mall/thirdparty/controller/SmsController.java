package org.epha.mall.thirdparty.controller;

import org.epha.common.utils.R;
import org.epha.mall.thirdparty.service.AliyunSmsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Resource
    AliyunSmsService aliyunSmsService;

    // TODO: 远程调用的时候为什么失败？但无报错信息
    @GetMapping("/code")
    public R sendCaptcha(@RequestParam("phoneNumber") String phoneNumber,
                         @RequestParam("code") String code) {

        try {
            aliyunSmsService.sendCaptcha(phoneNumber,code);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return R.ok();
    }
}
