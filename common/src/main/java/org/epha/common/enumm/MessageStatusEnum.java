package org.epha.common.enumm;

/**
 * @author pangjiping
 */

public enum MessageStatusEnum {
    SENT(0, "已发送"),
    ERROR_BROKER(1, "未到达接收机"),
    ERROR_QUEUE(2, "未到达队列"),
    ARRIVE(4, "消息已到达");
    private Integer code;
    private String msg;

    MessageStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
