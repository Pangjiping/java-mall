package org.epha.mall.ware.configuration;

import org.epha.common.constant.WareConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pangjiping
 */
@Configuration
public class RabbitBindingConfiguration {

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(
                WareConstant.MQ_QUEUE_STOCK_RELEASE_STOCK,
                Binding.DestinationType.QUEUE,
                WareConstant.MQ_EXCHANGE_STOCK_EVENT,
                WareConstant.MQ_ROUTING_KEY_STOCK_RELEASE,
                null
        );
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(
                WareConstant.MQ_QUEUE_STOCK_DELAY,
                Binding.DestinationType.QUEUE,
                WareConstant.MQ_EXCHANGE_STOCK_EVENT,
                WareConstant.MQ_ROUTING_KEY_STOCK_LOCKED,
                null
        );
    }
}
