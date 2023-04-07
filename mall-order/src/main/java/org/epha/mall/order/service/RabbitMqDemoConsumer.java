package org.epha.mall.order.service;

import com.rabbitmq.client.Channel;
import org.epha.mall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author pangjiping
 */

// @Service
// @RabbitListener(queues = {"hello-java-queue"})
public class RabbitMqDemoConsumer {

//    /**
//     * 消费端接收消息示例
//     *
//     * @param message 消息体
//     * @param content 消息直接映射到某个对象上，省去自己反序列化的步骤
//     * @param channel 一个消息channel
//     */
//    @RabbitHandler
//    public void receiveMessage(Message message,
//                               OrderReturnReasonEntity content,
//                               Channel channel) {
//        System.out.println("接收到消息..." + content);
//
//        byte[] body = message.getBody();
//
//        // 消息头属性
//        MessageProperties headers = message.getMessageProperties();
//
//        System.out.println("消息处理完成..." + content.getName());
//
//        // 拿到tag，是一个channel内自增的
//        long deliveryTag = headers.getDeliveryTag();
//        System.out.println("deliveryTag: " + deliveryTag);
//
//
//        try {
//            if (deliveryTag % 2 == 0) {
//                // 手动ACK，不要批量确认
//                channel.basicAck(deliveryTag, false);
//                System.out.println("手动ACK: " + deliveryTag);
//            } else {
//                // 手动reject（消息处理不了，重新入队）
//                channel.basicReject(deliveryTag, true);
//                System.out.println("手动拒接，重新入队: " + deliveryTag);
//
//                // 手动reject，直接丢弃
//                // channel.basicReject(deliveryTag,false);
//                // System.out.println("手动拒接，丢弃消息: "+deliveryTag);
//
//                // 手动NoAck，和reject一样的，不过会加上一个批量处理
//                // channel.basicNack(deliveryTag,false,true);
//            }
//
//        } catch (IOException e) {
//            // 可能网络中断，手动确认失败了
//            e.printStackTrace();
//        }
//    }
}
