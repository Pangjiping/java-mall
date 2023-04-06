package org.epha.mall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
public class RabbitMqTests {

    @Resource
    AmqpAdmin amqpAdmin;

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 如何创建Exchange
     */
    @Test
    public void createExchange() {

        // 创建一个DirectExchange
        DirectExchange directExchange = new DirectExchange(
                "hello-java-exchange",
                true,
                false);
        amqpAdmin.declareExchange(directExchange);

        log.debug("交换机创建完成: {}", directExchange);

    }

    /**
     * 如何创建Queue
     */
    @Test
    public void createQueue() {

        // 创建一个Queue
        Queue queue = new Queue(
                "hello-java-queue",
                true,
                false,
                false
        );
        amqpAdmin.declareQueue(queue);

        log.debug("队列创建完成: {}", queue);
    }

    /**
     * 如何创建Binding
     */
    @Test
    public void createBinding() {

        // 创建一个Binding
        Binding binding = new Binding(
                "hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null
        );
        amqpAdmin.declareBinding(binding);

        log.debug("队列创建完成: {}", binding);
    }

    /**
     * 测试发送消息
     */
    @Test
    public void sendMessage() {
        rabbitTemplate.convertAndSend(
                "hello-java-exchange",
                "hello.java",
                "hello world"
        );

        log.debug("消息发送完成");
    }



}
