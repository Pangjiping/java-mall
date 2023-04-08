package org.epha.mall.order.configuration;

import org.epha.common.constant.OrderConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author pangjiping
 */
@Configuration
public class RabbitQueueConfiguration {
    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", OrderConstant.MQ_EXCHANGE_ORDER_EVENT);
        args.put("x-dead-letter-routing-key", OrderConstant.MQ_ROUTING_KEY_ORDER_RELEASE);
        args.put("x-message-ttl", 60000);

        return new Queue(
                OrderConstant.MQ_QUEUE_ORDER_DELAY,
                true,
                false,
                false,
                args
        );
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue(
                OrderConstant.MQ_QUEUE_ORDER_RELEASE_ORDER,
                true,
                false,
                false
        );
    }
}
