package org.epha.mall.order.mq.callback;

import lombok.extern.slf4j.Slf4j;
import org.epha.common.enumm.MessageStatusEnum;
import org.epha.mall.order.service.MqMessageService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@Slf4j
@Service
public class ConfirmCallback implements RabbitTemplate.ConfirmCallback {

    @Resource
    MqMessageService mqMessageService;

    /**
     * mq的broker确认回调函数，只能获取到correlationData的数据
     * 在发送数据时要把messageId放在这里面
     * 如果没有ack，不用修改发送记录（此时记录应该是已发送）
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.debug("消息抵达交换机");

        String messageId = correlationData.getId();

        if (!ack) {
            log.error("消息{}未能到达Broker, 原因: {}", messageId, cause);

            // 更细数据库状态为未到达broker，并记录失败原因
            mqMessageService.updateMessageRecord(messageId, MessageStatusEnum.ERROR_BROKER.getCode(), cause);
            return;
        }

        // 更新数据库状态为已经到达（如果没到达队列，returnsCallback会再次修改数据库状态的）
        mqMessageService.updateMessageRecord(messageId, MessageStatusEnum.ARRIVE.getCode());

    }
}
