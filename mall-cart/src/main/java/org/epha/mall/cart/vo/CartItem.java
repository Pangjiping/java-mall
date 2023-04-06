package org.epha.mall.cart.vo;

import com.alibaba.fastjson.annotation.JSONField;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车里的购物项
 *
 * @author pangjiping
 */
public class CartItem {

    private Long skuId;

    private boolean check;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    /**
     * 总价不保存到购物车，不需要序列化
     */
    @JSONField(serialize = false)
    private BigDecimal totalPrice;

    public CartItem() {
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public boolean getCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.count.toString()));
    }

    public static class Builder {
        private Long skuId;

        private boolean check = true;

        private String title;

        private String image;

        private List<String> skuAttr;

        private BigDecimal price;

        private Integer count;

        public Builder() {

        }

        public Builder withSkuId(Long skuId) {
            this.skuId = skuId;
            return this;
        }

        public Builder withCheck(boolean check) {
            this.check = check;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withImage(String image) {
            this.image = image;
            return this;
        }

        public Builder withSkuAttr(List<String> skuAttr) {
            this.skuAttr = skuAttr;
            return this;
        }

        public Builder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder withCount(Integer count) {
            this.count = count;
            return this;
        }

        public CartItem build() {
            return new CartItem(this);
        }
    }

    private CartItem(Builder builder) {
        this.skuId = builder.skuId;
        this.count = builder.count;
        this.price = builder.price;
        this.skuAttr = builder.skuAttr;
        this.check = builder.check;
        this.image = builder.image;
        this.title = builder.title;
    }
}
