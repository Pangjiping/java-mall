package org.epha.mall.order.vo;

import lombok.Data;
import org.epha.mall.order.entity.OrderEntity;

/**
 * @author pangjiping
 */
@Data
public class OrderSubmitResponse {

    /**
     * 订单信息
     */
    private OrderEntity orderEntity;
}
