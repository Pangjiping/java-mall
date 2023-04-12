package org.epha.mall.ware.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.constant.WareConstant;
import org.epha.common.enumm.OrderStatusEnum;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.common.mq.StockLockedMessage;
import org.epha.common.utils.R;
import org.epha.mall.ware.entity.WareOrderTaskDetailEntity;
import org.epha.mall.ware.entity.WareOrderTaskEntity;
import org.epha.mall.ware.feign.OrderFeignService;
import org.epha.mall.ware.service.WareOrderTaskDetailService;
import org.epha.mall.ware.service.WareOrderTaskService;
import org.epha.mall.ware.service.WareSkuService;
import org.epha.mall.ware.to.OrderEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pangjiping
 */
@RabbitListener(queues = WareConstant.MQ_QUEUE_STOCK_RELEASE_STOCK)
@Component
@Slf4j
public class StockReleaseListener {

    @Resource
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Resource
    WareOrderTaskService wareOrderTaskService;

    @Resource
    OrderFeignService orderFeignService;

    @Resource
    WareSkuService wareSkuService;

    /**
     * 库存自动解锁
     */
    @RabbitHandler
    public void handleStockLockedRelease(Message message,
                                         Channel channel) throws IOException {

        String content = new String(message.getBody());
        StockLockedMessage stockLockedMessage = JSON.parseObject(content, new TypeReference<>() {
        });

        try {
            handle(stockLockedMessage);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    /**
     * 防止订单服务卡顿导致订单消息一直改不了
     * 库存到期优先到期，发现订单状态不满足删除条件，这个消息就被丢弃了
     * 导致该解锁的订单没有解锁
     */
    @RabbitHandler
    public void handleOrderCloseRelease(Message message,
                                        Channel channel) throws IOException {
        String s = new String(message.getBody());
        OrderEntity orderEntity = JSON.parseObject(s, new TypeReference<>() {
        });

        try {
            handle(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }


    private void handle(OrderEntity orderEntity) throws Exception {
        String orderSn = orderEntity.getOrderSn();

        // 查一下订单状态
        R r = orderFeignService.getOrderStatus(orderSn);
        if (r.getCode() != 0) {
            log.error("远程调用订单服务失败: {}", r.getErrorMessage());
            throw new BizException(BizCodeEnum.ORDER_RPC_EXCEPTION);
        }

        // 如果订单的状态不对，不需要解锁库存，返回
        Integer orderStatus = r.getData(new TypeReference<>() {
        });
        if (!orderStatus.equals(OrderStatusEnum.CANCLED.getCode())) {
            return;
        }

        // 查一下最新的库存解锁状态，防止重复解锁库存
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.query()
                .eq("order_sn", orderEntity.getOrderSn())
                .getEntity();

        // 按照工作单找到所有没有解锁的库存进行解锁
        List<WareOrderTaskDetailEntity> taskDetailEntities = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>()
                        .eq("task_id", orderTaskEntity.getId())
                        .eq("lock_status", 1)
        );

        // 如果没找到，已经解锁了还是怎样的，返回
        if (taskDetailEntities == null || taskDetailEntities.size() <= 0) {
            return;
        }

        // 批量解锁库存
        batchUnlockStock(taskDetailEntities);

    }

    private void handle(StockLockedMessage stockLockedMessage) throws BizException {
        log.debug("收到解锁库存的消息: {}", stockLockedMessage);
        StockLockedMessage.WareOrderTaskDetail taskDetail = stockLockedMessage.getTaskDetail();

        // 查询数据库关于这个订单的锁定库存信息
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(taskDetail.getId());

        // 如果数据库没有，那就是库存锁定失败了，数据库整体回滚，不需要解锁
        if (taskDetailEntity == null) {
            log.debug("数据库没有detail表，锁库存失败");
            return;
        }

        // 如果数据库有，证明库存锁定成功了
        // 如果没有这个订单，订单不存在，必须解锁
        // 如果有这个订单，不是解锁库存了，看订单状态，只要订单状态没取消，就不能解锁
        Long taskId = stockLockedMessage.getWareOrderTaskId();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);

        // 拿到订单号，根据订单号查询订单的状态
        String orderSn = taskEntity.getOrderSn();
        R r = orderFeignService.getOrderStatus(orderSn);
        if (r.getCode() != 0) {
            // TODO: 请求失败怎么办？直接拒绝掉消息，并让其重新入队
            log.error("远程调用失败，原因 {}", r.getErrorMessage());
            throw new BizException(BizCodeEnum.ORDER_RPC_EXCEPTION);
        }

        Integer orderStatus = r.getData(new TypeReference<>() {
        });
        // 订单被取消了或者不存在，解锁库存
        if (orderStatus.equals(OrderStatusEnum.CANCLED.getCode())) {
            log.debug("为订单{}解锁库存", orderSn);
            unlockStock(taskDetailEntity.getSkuId(), taskDetailEntity.getWareId(), taskDetailEntity.getSkuNum(), taskDetailEntity.getId());
        }

        log.debug("解锁库存成功");

    }

    /**
     * 解锁库存，保证是事务的
     *
     * @param skuId        商品id
     * @param wareId       解锁哪个仓库的库存【数据库忘记设置，统一使用1号仓库】
     * @param num          解锁的数量
     * @param taskDetailId 库存工作单详情的id
     */
    @Transactional(rollbackFor = Exception.class)
    void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {

        // 解锁库存
        wareSkuService.unlockStock(skuId, wareId, num);

        // 更新库存工作单状态
        wareOrderTaskDetailService.update()
                .eq("id", taskDetailId)
                .set("lock_status", 2);
    }

    /**
     * 批量解锁库存，保证是事务的
     */
    @Transactional(rollbackFor = Exception.class)
    void batchUnlockStock(List<WareOrderTaskDetailEntity> taskDetailEntities) throws Exception {
        // 解锁库存
        // TODO: 可以是批量的吗？
        taskDetailEntities.forEach(taskDetailEntity ->
                wareSkuService.unlockStock(
                        taskDetailEntity.getSkuId(),
                        taskDetailEntity.getWareId(),
                        taskDetailEntity.getSkuNum())
        );

        // 更新工作单状态
        List<WareOrderTaskDetailEntity> updates = taskDetailEntities.stream()
                .map(taskDetailEntity -> {
                    WareOrderTaskDetailEntity update = new WareOrderTaskDetailEntity();
                    update.setId(taskDetailEntity.getId());
                    update.setLockStatus(2);
                    return update;
                })
                .collect(Collectors.toList());
        boolean b = wareOrderTaskDetailService.updateBatchById(updates);
        if (!b) {
            throw new Exception("批量更新工作单状态失败");
        }
    }
}
