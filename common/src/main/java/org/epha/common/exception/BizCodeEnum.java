package org.epha.common.exception;

/**
 * 错误码和错误信息定义
 * 1. 错误码定义规则为五位数字
 * 2. 前两位表示业务场景，最后三位表示错误码
 * 3. 维护错误码后需要维护错误描述，将其定义为枚举形式
 *
 * 10：通用
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 */
public enum BizCodeEnum {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常");

    private int code;
    private String message;
    BizCodeEnum(int code,String message){
        this.code=code;
        this.message=message;
    }

    public int getCode(){
        return this.code;
    }
    public String getMessage(){
        return this.message;
    }
}