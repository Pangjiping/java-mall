package org.epha.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author pangjiping
 */
@Data
public class SkuItemSaleAttr {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuId> attrValues;
}
