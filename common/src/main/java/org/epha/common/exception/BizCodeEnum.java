package org.epha.common.exception;

/**
 * 错误码和错误信息定义
 * 1. 错误码定义规则为五位数字
 * 2. 前两位表示业务场景，最后三位表示错误码
 * 3. 维护错误码后需要维护错误描述，将其定义为枚举形式
 * <p>
 * 10：通用
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15: 用户
 * 16: 库存
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，稍后再试"),
    SMS_CODE_MISMATCH_EXCEPTION(10003, "验证码错误"),
    RELOGIN_EXCEPTION(10004, "请重新登录"),

    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),

    USER_EXIST_EXCEPTION(15001, "用户名已经存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已经存在"),
    ACCOUNT_NOT_EXIST_EXCEPTION(15003, "账户不存在，请先注册"),
    PASSWORD_MISMATCH_EXCEPTION(15004, "账户或密码错误"),

    REPEATE_ORDER_SUNMIT_EXCEPTION(12001, "请勿重复提交订单"),
    PRICE_MISMATCH_EXCEPTION(12002, "订单金额出现变动，请重新下单"),

    EMPTY_STOCK_EXCEPTION(16001,"库存不足");

    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
