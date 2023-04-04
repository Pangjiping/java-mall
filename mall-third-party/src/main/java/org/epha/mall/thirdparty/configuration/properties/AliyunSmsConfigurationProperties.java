package org.epha.mall.thirdparty.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pangjiping
 */
@ConfigurationProperties(prefix = "jmall.auth.sms")
@Component
@Data
public class AliyunSmsConfigurationProperties {
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String signName;
    private String templateCode;
}
