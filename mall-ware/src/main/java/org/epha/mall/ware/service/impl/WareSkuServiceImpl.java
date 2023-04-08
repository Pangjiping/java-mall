package org.epha.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.epha.common.constant.WareConstant;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.common.mq.StockLockedMessage;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.ware.dao.WareSkuDao;
import org.epha.mall.ware.entity.WareOrderTaskDetailEntity;
import org.epha.mall.ware.entity.WareOrderTaskEntity;
import org.epha.mall.ware.entity.WareSkuEntity;
import org.epha.mall.ware.service.WareOrderTaskDetailService;
import org.epha.mall.ware.service.WareOrderTaskService;
import org.epha.mall.ware.service.WareSkuService;
import org.epha.mall.ware.vo.OrderItemVo;
import org.epha.mall.ware.vo.SkuHasStockVo;
import org.epha.mall.ware.vo.WareSkuLockRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author pangjiping
 */
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    WareOrderTaskService wareOrderTaskService;

    @Resource
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(skuId -> {
            SkuHasStockVo stockVo = new SkuHasStockVo();

            // 查询sku库存
            Long count = getBaseMapper().getSkuStock(skuId);
            if (count == null) {
                // 如果查不到库存信息，先默认有库存
                stockVo.setHasStock(true);
            } else {
                stockVo.setHasStock(count > 0);
            }

            stockVo.setSkuId(skuId);

            return stockVo;
        }).collect(Collectors.toList());

        return skuHasStockVos;
    }

    /**
     * 为某个订单锁定库存
     * 涉及多个商品的库存，封装成事务操作，快速失败，失败回滚
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void orderLockStock(WareSkuLockRequest request) throws BizException {

        // 保存库存工作单
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(request.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = request.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream()
                .map(item -> {

                    SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
                    skuWareHasStock.setSkuId(item.getSkuId());
                    skuWareHasStock.setNum(item.getCount());

                    // 查询商品在哪里有库存
                    List<Long> wareIds = getBaseMapper().listWareIdsHasStock(item.getSkuId());
                    skuWareHasStock.setWareIds(wareIds);

                    return skuWareHasStock;
                })
                .collect(Collectors.toList());

        // 锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {

            boolean skuStockLocked = false;

            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareIds();

            // 没有仓库信息，快速失败
            if (wareIds == null || wareIds.size() == 0) {
                throw new BizException(BizCodeEnum.EMPTY_STOCK_EXCEPTION);
            }

            for (Long wareId : wareIds) {
                Long count = getBaseMapper().lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if (count == 1) {
                    // 锁定成功，跳出循环
                    skuStockLocked = true;

                    // 创建锁定成功信息
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                    detailEntity.setSkuNum(skuWareHasStock.getNum());
                    detailEntity.setTaskId(wareOrderTaskEntity.getId());
                    detailEntity.setSkuId(skuWareHasStock.getSkuId());
                    detailEntity.setWareId(wareId);
                    detailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(detailEntity);

                    // 发送消息给交换机
                    sendStockLockedMessage(wareOrderTaskEntity.getId(), detailEntity);

                    break;
                }
            }

            // 如果当前商品没有锁住库存（库存不足），抛出异常，快速失败
            if (!skuStockLocked) {
                throw new BizException(BizCodeEnum.EMPTY_STOCK_EXCEPTION);
            }
        }
    }

    @Override
    public void unlockStock(Long skuId, Long wareId, Integer num) {
        this.getBaseMapper().unlockStock(skuId, wareId, num);
    }

    /**
     * 当库存锁定成功，发送消息给MQ
     * exchange: stock-event-exchange
     * routingKey: stock-locked
     */
    private void sendStockLockedMessage(Long taskId, WareOrderTaskDetailEntity taskDetail) {

        StockLockedMessage.WareOrderTaskDetail detail = new StockLockedMessage.WareOrderTaskDetail();
        BeanUtils.copyProperties(taskDetail, detail);

        StockLockedMessage message = new StockLockedMessage();
        message.setWareOrderTaskId(taskId);
        message.setTaskDetail(detail);

        rabbitTemplate.convertAndSend(WareConstant.MQ_EXCHANGE_STOCK_EVENT,
                WareConstant.MQ_ROUTING_KEY_STOCK_LOCKED,
                message
        );
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}