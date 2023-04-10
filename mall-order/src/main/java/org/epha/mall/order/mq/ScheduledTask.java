package org.epha.mall.order.mq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.enumm.MessageStatusEnum;
import org.epha.mall.order.entity.MqMessageEntity;
import org.epha.mall.order.service.MqMessageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author pangjiping
 */
@Component
@Slf4j
public class ScheduledTask {

    private final static String NO_EXCHANGE_PATTERN = "reply-code=404\\S*\\s*reply-text=NOT_FOUND\\s*-\\s*no\\s*exchange";

    private final static String NO_QUEUE_PATTERN = "NO_ROUTE";

    @Resource
    MqMessageService messageService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 60000)
    public void resendMessage() {
        List<MqMessageEntity> failedMessages = scanFailedMessage();

        failedMessages.forEach(mqMessageEntity -> {

            // 检查是不是交换机不存在
            if (StringUtils.hasText(mqMessageEntity.getErrorMessage()) &&
                    Pattern.matches(NO_EXCHANGE_PATTERN, mqMessageEntity.getErrorMessage())) {
                log.error("未能处理交换机异常，message: {}", mqMessageEntity);
                return;
            }

            // 检查是不是routingKey写错
            if (StringUtils.hasText(mqMessageEntity.getErrorMessage()) &&
                    Pattern.matches(NO_QUEUE_PATTERN, mqMessageEntity.getErrorMessage())) {
                log.error("未能处理路由键异常，message: {}", mqMessageEntity);
                return;
            }

            CorrelationData correlationData = new CorrelationData(mqMessageEntity.getMessageId());
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(mqMessageEntity.getMessageId());
            properties.setContentType("text/plain");
            properties.setContentEncoding("utf-8");

            Message message = new Message(
                    mqMessageEntity.getContent().getBytes(StandardCharsets.UTF_8),
                    properties
            );
            try {
                rabbitTemplate.convertAndSend(
                        mqMessageEntity.getToExchane(),
                        mqMessageEntity.getRoutingKey(),
                        message,
                        correlationData
                );

            } catch (Exception e) {
                log.error("消息{} 发送失败: {}", mqMessageEntity.getMessageId(),
                        e.getMessage());
            } finally {
                messageService.updateMessageRecord(
                        mqMessageEntity.getMessageId(),
                        MessageStatusEnum.SENT.getCode(),
                        mqMessageEntity.getRetry() + 1
                );
            }
        });
    }

    /**
     * 从全表拿到失败的消息，状态不为ARRIVE就是失败
     * 全表拿可能不太合理，需要一些设计，比如引入失败原因之类的
     * 如果是因为路由键啥的写错了，重新发也没用
     */
    private List<MqMessageEntity> scanFailedMessage() {
        return messageService.list(new QueryWrapper<MqMessageEntity>()
                .ne("message_status", MessageStatusEnum.ARRIVE.getCode())
                .lt("retry", 3)
        );
    }
}
