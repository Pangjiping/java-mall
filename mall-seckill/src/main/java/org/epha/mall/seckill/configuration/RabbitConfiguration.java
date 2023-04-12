package org.epha.mall.seckill.configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@Configuration
public class RabbitConfiguration {

    @Resource
    RabbitTemplate.ConfirmCallback confirmCallback;

    @Resource
    RabbitTemplate.ReturnsCallback returnsCallback;

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
     * 定义全局统一的两个回调函数
     * 自定义消息序列化为Json格式，否则是默认的java序列化
     */
    @Bean
    // @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 设置全局回调函数
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnsCallback(returnsCallback);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

}
