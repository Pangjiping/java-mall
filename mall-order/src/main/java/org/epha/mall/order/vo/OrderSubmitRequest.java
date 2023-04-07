package org.epha.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author pangjiping
 */
@Data
public class OrderSubmitRequest {

    /**
     * 收货地址id
     */
    private Long addressId;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 令牌
     */
    private String orderToken;

    /**
     * 验价
     */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;
}
