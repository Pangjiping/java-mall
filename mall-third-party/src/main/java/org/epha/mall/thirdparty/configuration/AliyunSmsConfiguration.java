package org.epha.mall.thirdparty.configuration;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.epha.mall.thirdparty.configuration.properties.AliyunSmsConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pangjiping
 */
@Configuration
public class AliyunSmsConfiguration {

    @Bean
    public Client client(AliyunSmsConfigurationProperties properties) throws Exception {
        Config config = new Config().setAccessKeyId(properties.getAccessKey())
                .setAccessKeySecret(properties.getSecretKey())
                .setEndpoint(properties.getEndpoint());
        return new Client(config);
    }
}
