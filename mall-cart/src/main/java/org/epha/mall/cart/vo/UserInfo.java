package org.epha.mall.cart.vo;

import lombok.Data;

/**
 * @author pangjiping
 */
@Data
public class UserInfo {

    private Long userId;

    private String userKey;

    private boolean newTempUser = true;
}
