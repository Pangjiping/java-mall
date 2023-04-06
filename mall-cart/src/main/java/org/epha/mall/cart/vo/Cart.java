package org.epha.mall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 *
 * @author pangjiping
 */
public class Cart {

    private List<CartItem> items;

    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品类型数量
     */
    private Integer countType;

    /**
     * 购物车总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        final Integer[] count = {0};
        if (items != null && items.size() > 0) {
            items.forEach(cartItem -> {
                if (cartItem.getCheck()) {
                    count[0] += cartItem.getCount();
                }
            });
        }
        return count[0];
    }

    public Integer getCountType() {
        final Integer[] count = {0};
        if (items != null && items.size() > 0) {
            items.forEach(cartItem -> {
                if (cartItem.getCheck()) {
                    count[0] += 1;
                }
            });
        }
        return count[0];
    }

    public BigDecimal getTotalAmount() {

        final BigDecimal[] amount = {new BigDecimal("0")};
        if (items != null && items.size() > 0) {
            items.forEach(cartItem -> {
                if (cartItem.getCheck()) {
                    amount[0] = amount[0].add(cartItem.getTotalPrice());
                }
            });
        }

        // 减去减免价格
        amount[0] = amount[0].subtract(reduce);

        return amount[0];
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
