package org.epha.mall.auth.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pangjiping
 */
@ConfigurationProperties(prefix = "jmall.thread")
@Component
@Data
public class ExecutorConfigurationProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
