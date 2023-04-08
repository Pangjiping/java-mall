package org.epha.mall.order.configuration;

import org.epha.common.constant.OrderConstant;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 给容器中创建好需要的queue、exchange等
 * rabbitmq会自动创建这些组件
 *
 * @author pangjiping
 */
@Configuration
public class RabbitExchangeConfiguration {

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange(
                OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                true,
                false
        );
    }

}
