package org.epha.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.exception.BizException;
import org.epha.common.utils.PageUtils;
import org.epha.mall.order.entity.OrderEntity;
import org.epha.mall.order.vo.OrderConfirmVo;
import org.epha.mall.order.vo.OrderSubmitRequest;
import org.epha.mall.order.vo.OrderSubmitResponse;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:37:46
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    OrderSubmitResponse submitOrder(OrderSubmitRequest request) throws BizException, ExecutionException, InterruptedException;

    Integer getOrderStatusByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);
}

