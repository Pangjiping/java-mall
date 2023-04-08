package org.epha.mall.ware.configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pangjiping
 */
@Configuration
public class RabbitConfiguration {
    /**
     * 保证RabbitAdmin的初始化方法正确执行
     * 可以拿到bean然后创建对应的exchange、queue、binding
     */
    @Bean
    ApplicationRunner runner(ConnectionFactory cf) {
        return args -> {
            cf.createConnection().close();
        };
    }

    /**
     * 自定义消息序列化为Json格式，否则是默认的java序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
