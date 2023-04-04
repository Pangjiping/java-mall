package org.epha.mall.thirdparty.service;

/**
 * @author pangjiping
 */
public interface AliyunSmsService {
    void sendCaptcha(String phoneNumber,String code) throws Exception;
}
