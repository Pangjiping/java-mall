package org.epha.mall.order.web;

import org.epha.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * @author pangjiping
 */
@RestController
public class HelloController {

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 测试创建订单
     */
    @GetMapping("/order/test")
    public String createOrderTest(){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setCreateTime(new Date());

        // 给MQ发消息
        rabbitTemplate.convertAndSend(
                "order-event-exchange",
                "order.create.order",
                orderEntity
        );

        return "ok";
    }
}
