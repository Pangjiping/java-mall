package org.epha.mall.thirdparty.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teautil.models.RuntimeOptions;
import org.epha.mall.thirdparty.service.AliyunSmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@Service
public class AliyunSmsServiceImpl implements AliyunSmsService {

    @Value("${jmall.auth.sms.sign_name}")
    private String smsSignName;

    @Value("${jmall.auth.sms.template_code}")
    private String smsTemplateCode;

    @Resource
    Client client;


    @Override
    public void sendCaptcha(String phoneNumber, String code) throws Exception {
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(smsSignName)
                .setPhoneNumbers(phoneNumber)
                .setTemplateCode(smsTemplateCode);

        String param = "{\"code\":\"anycode\"}";
        String replace = param.replace("anycode", code);
        sendSmsRequest.setTemplateParam(replace);

        RuntimeOptions runtimeOptions = new RuntimeOptions();

        client.sendSmsWithOptions(sendSmsRequest,runtimeOptions);
    }
}
