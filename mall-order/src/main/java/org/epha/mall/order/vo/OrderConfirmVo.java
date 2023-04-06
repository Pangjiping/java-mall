package org.epha.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author pangjiping
 */
public class OrderConfirmVo {

    /**
     * 收货地址列表
     */
    @Getter
    @Setter
    private List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    @Getter
    @Setter
    private List<OrderItemVo> items;

    // 发票记录...

    /**
     * 优惠券信息
     */
    @Getter
    @Setter
    private Integer integration;

    /**
     * 订单的令牌，防止重复下单
     */
    @Getter
    @Setter
    private String orderToken;

    /**
     * 是否有库存，key-skuId value-是否有库存
     */
    @Getter
    @Setter
    private Map<Long, Boolean> hasStockMap;

    /**
     * 运费信息
     */
    @Getter
    @Setter
    private BigDecimal fare;

    /**
     * 订单总额
     */
    private BigDecimal totalAccount;

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    public BigDecimal getTotalAccount() {

        final BigDecimal[] total = {BigDecimal.ZERO};

        if (this.items != null && this.items.size() > 0) {
            items.forEach(orderItemVo -> {
                BigDecimal price = orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount().toString()));
                total[0] = total[0].add(price);
            });
        }
        return total[0];
    }

    public BigDecimal getPayPrice() {
        return getTotalAccount();
    }
}
