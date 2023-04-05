package org.epha.mall.member.vo;

import lombok.Data;

/**
 * @author pangjiping
 */
@Data
public class WeiboMemberLoginVo {
    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
}
