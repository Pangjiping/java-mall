package org.epha.mall.order.mq.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author pangjiping
 */
@Slf4j
@Service
public class ReturnCallback implements RabbitTemplate.ReturnsCallback {
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.info("消息主体: {}", returned.getMessage());
        log.info("回复编码: {}", returned.getReplyCode());
        log.info("回复内容: {}", returned.getReplyText());
        log.info("交换器: {}", returned.getExchange());
        log.info("路由键: {}", returned.getRoutingKey());
    }
}
