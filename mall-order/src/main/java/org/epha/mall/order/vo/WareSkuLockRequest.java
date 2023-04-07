package org.epha.mall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author pangjiping
 */
@Data
public class WareSkuLockRequest {

    private String orderSn;

    /**
     * 需要锁住的所有库存
     */
    private List<OrderItemVo> locks;
}
