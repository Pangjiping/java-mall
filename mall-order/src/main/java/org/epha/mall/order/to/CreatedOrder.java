package org.epha.mall.order.to;

import lombok.Data;
import org.epha.mall.order.entity.OrderEntity;
import org.epha.mall.order.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author pangjiping
 */
@Data
public class CreatedOrder {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /**
     * 订单应付金额
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;
}
