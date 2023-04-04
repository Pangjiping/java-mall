package org.epha.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author pangjiping
 */
@Data
public class SpuItemAttrGroup {
    private String groupName;
    private List<SpuBaseAttr> attrs;
}
