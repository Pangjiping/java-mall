package org.epha.mall.thirdparty.impl;

import org.epha.mall.thirdparty.service.AliyunSmsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AliyunSmsServiceImplTest {

    @Resource
    AliyunSmsService aliyunSmsService;

    @Test
    public void sendCaptchaTest() throws Exception {
        aliyunSmsService.sendCaptcha("13626376642","1234");
    }
}