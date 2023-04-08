package org.epha.mall.order.mq.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author pangjiping
 */
@Slf4j
@Service
public class ConfirmCallback implements RabbitTemplate.ConfirmCallback {
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("消息唯一标识: {}", correlationData);
        log.info("确认状态: {}", ack);
        log.info("造成原因: {}", cause);
    }
}
