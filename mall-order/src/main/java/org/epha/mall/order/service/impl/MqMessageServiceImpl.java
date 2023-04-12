package org.epha.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.constant.OrderConstant;
import org.epha.common.enumm.MessageStatusEnum;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.order.dao.MqMessageDao;
import org.epha.mall.order.entity.MqMessageEntity;
import org.epha.mall.order.service.MqMessageService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("mqMessageService")
public class MqMessageServiceImpl extends ServiceImpl<MqMessageDao, MqMessageEntity> implements MqMessageService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MqMessageEntity> page = this.page(
                new Query<MqMessageEntity>().getPage(params),
                new QueryWrapper<MqMessageEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void createOrderCreateMessageRecord(String uuid, String content) {
        MqMessageEntity messageEntity = initMessageEntity(uuid);
        messageEntity.setContent(content);
        messageEntity.setToExchane(OrderConstant.MQ_EXCHANGE_ORDER_EVENT);
        messageEntity.setRoutingKey(OrderConstant.MQ_ROUTING_KEY_ORDER_CREATE);

        // 插入记录
        this.save(messageEntity);
    }

    @Override
    public void updateMessageRecord(String uuid, Integer status) {
        MqMessageEntity update = new MqMessageEntity();
        update.setMessageId(uuid);
        update.setMessageStatus(status);
        update.setUpdateTime(new Date());
        this.updateById(update);
    }

    @Override
    public void updateMessageRecord(String uuid, Integer status, String errMessage) {
        MqMessageEntity update = new MqMessageEntity();
        update.setMessageId(uuid);
        update.setMessageStatus(status);
        update.setErrorMessage(errMessage);
        update.setUpdateTime(new Date());
        this.updateById(update);
    }

    @Override
    public void updateMessageRecord(String uuid, Integer status, Integer retry) {
        MqMessageEntity update = new MqMessageEntity();
        update.setMessageId(uuid);
        update.setMessageStatus(status);
        update.setRetry(retry);
        update.setUpdateTime(new Date());
        this.updateById(update);
    }

    @Override
    public void createOrderCloseMessageRecord(String uuid, String content) {
        MqMessageEntity messageEntity = initMessageEntity(uuid);
        messageEntity.setContent(content);
        messageEntity.setToExchane(OrderConstant.MQ_EXCHANGE_ORDER_EVENT);
        messageEntity.setRoutingKey("order.release.other");

        // 插入记录
        this.save(messageEntity);
    }

    private MqMessageEntity initMessageEntity(String uuid) {
        MqMessageEntity messageEntity = new MqMessageEntity();
        messageEntity.setMessageId(uuid);
        messageEntity.setMessageStatus(MessageStatusEnum.SENT.getCode());
        messageEntity.setCreateTime(new Date());
        messageEntity.setUpdateTime(new Date());

        return messageEntity;
    }

}