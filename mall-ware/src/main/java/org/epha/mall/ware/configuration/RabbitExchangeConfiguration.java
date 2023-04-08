package org.epha.mall.ware.configuration;

import org.epha.common.constant.WareConstant;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pangjiping
 */
@Configuration
public class RabbitExchangeConfiguration {

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange(
                WareConstant.MQ_EXCHANGE_STOCK_EVENT,
                true,
                false
        );
    }
}
