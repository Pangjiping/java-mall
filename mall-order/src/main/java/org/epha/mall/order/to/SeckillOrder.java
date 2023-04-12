package org.epha.mall.order.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author pangjiping
 */
@Data
public class SeckillOrder {
    private String orderSn;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal number;

    private Long memberId;
}
