package org.epha.mall.order.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.constant.OrderConstant;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.mall.order.entity.OrderEntity;
import org.epha.mall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author pangjiping
 */
@RabbitListener(queues = OrderConstant.MQ_QUEUE_ORDER_RELEASE_ORDER)
@Component
@Slf4j
public class OrderCloseListener {

    @Resource
    OrderService orderService;

    /**
     * 关闭订单
     */
    @RabbitHandler
    public void orderCloseHandler(Channel channel,
                                  Message message) throws IOException {

        String content = new String(message.getBody());
        OrderEntity orderEntity = JSON.parseObject(content, new TypeReference<>() {
        });

        try {
            handle(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单关闭消息处理错误: {}", e.getMessage());
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    private void handle(OrderEntity orderEntity) throws BizException {
        if (orderEntity == null) {
            throw new BizException(BizCodeEnum.UNHANDLE_ORDER_CLOSE_MESSAGE);
        }
        orderService.closeOrder(orderEntity);
    }
}
