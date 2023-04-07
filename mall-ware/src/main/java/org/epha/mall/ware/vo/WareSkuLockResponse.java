package org.epha.mall.ware.vo;

import lombok.Data;

/**
 * @author pangjiping
 */
@Data
public class WareSkuLockResponse {

    private Long skuId;

    private Integer num;

    private boolean locked = false;

}
