package org.epha.mall.order.configuration;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@Configuration
public class RabbitConfiguration {

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 自定义消息序列化为Json格式，否则是默认的java序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制rabbitTemplate
     *
     * 消息抵达Broker消息回调：
     *      1. spring.rabbitmq.publisher-confirm-type=correlated
     *      2. 设置确认回调
     *
     * 消息正确抵达Queue进行回调：
     *      1. spring.rabbitmq.publisher-returns=true spring.rabbitmq.template.mandatory=true
     *      2. 设置消息抵达队列的确认回调
     *
     *  消费端确认，保证每个消息正确被消费，此时Broker才能删除这个消息
     *  默认是自动的，只要消息被收到，消费端会自动确认，Broker就会删除这个消息
     *  问题：消费端收到很多消息，自动回复了ack，但是消息没有完全处理成功就宕机了，此时会产生消息丢失！
     *      1. 手动确认解决，消息处理完再确认，服务器宕机消息不会丢失，消息会转为ready状态
     *      2. 如何手动ack？见org.epha.mall.order.service.RabbitMqDemoConsumer#receiveMessage()
     */
    @PostConstruct
    public void initRabbitTemplate() {

        // 设置消息抵达Broker回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 发送端回调：只要消息抵达了Broker，就会ack=true
             * @param correlationData 当前消息的唯一关联数据（这个消息的id）
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println(correlationData + "==>" + ack + "==>" + cause);
            }
        });

        // 设置消息抵达Queue回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调
             * 投递成功不会触发的
             * @param returnedMessage 投递失败消息封装
             *  private final Message message; 投递失败消息的详细信息
             *
             * 	private final int replyCode; 回复的状态码
             *
             * 	private final String replyText; 回复的文本内容
             *
             * 	private final String exchange; 当时这个消息发送给哪个交换机
             *
             * 	private final String routingKey; 当时这个消息指定的routingKey
             */
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                System.out.println(returnedMessage);
            }
        });
    }

}
