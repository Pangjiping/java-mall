package org.epha.mall.seckill.mq.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@Slf4j
@Service
public class ReturnCallback implements RabbitTemplate.ReturnsCallback {

    @Override
    public void returnedMessage(ReturnedMessage returned) {

        log.error("消息未能抵达队列");
        log.error("replyCode: {}", returned.getReplyCode());
        log.error("replyText: {}", returned.getReplyText());
        String messageId = returned.getMessage().getMessageProperties().getMessageId();
        String errMessage = returned.getReplyCode() + "::" + returned.getReplyText();

        // 更新数据库状态为错误抵达
    }
}
