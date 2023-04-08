package org.epha.mall.ware.configuration;

import org.epha.common.constant.WareConstant;
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
    public Queue stockReleaseStockQueue() {
        return new Queue(
                WareConstant.MQ_QUEUE_STOCK_RELEASE_STOCK,
                true,
                false,
                false
        );
    }

    @Bean
    public Queue stockRelayQueue() {

        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", WareConstant.MQ_EXCHANGE_STOCK_EVENT);
        args.put("x-dead-letter-routing-key", "stock.release");
        args.put("x-message-ttl", 120000);

        return new Queue(
                WareConstant.MQ_QUEUE_STOCK_DELAY,
                true,
                false,
                false,
                args
        );
    }
}
