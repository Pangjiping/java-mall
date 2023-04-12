package org.epha.mall.order.configuration;

import org.epha.common.constant.OrderConstant;
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
    public Binding orderCreateOrderBinding() {
        return new Binding(
                OrderConstant.MQ_QUEUE_ORDER_DELAY,
                Binding.DestinationType.QUEUE,
                OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                OrderConstant.MQ_ROUTING_KEY_ORDER_CREATE,
                null
        );
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding(
                OrderConstant.MQ_QUEUE_ORDER_RELEASE_ORDER,
                Binding.DestinationType.QUEUE,
                OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                OrderConstant.MQ_ROUTING_KEY_ORDER_RELEASE,
                null
        );
    }

    /**
     * 订单释放，直接和库存释放进行绑定
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding(
                WareConstant.MQ_QUEUE_STOCK_RELEASE_STOCK,
                Binding.DestinationType.QUEUE,
                OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                OrderConstant.MQ_ROUTING_KEY_ORDER_RELEASE_OTHER,
                null
        );
    }

    @Bean
    public Binding orderSeckillBinding() {
        return new Binding(
                "order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                "order.seckill.order",
                null
        );
    }
}
