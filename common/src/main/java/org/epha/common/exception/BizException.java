package org.epha.common.exception;

/**
 * @author pangjiping
 */
public class BizException extends Exception{

    private int code;

    private String message;

    public BizException(String message){
        super(message);
    }

    public BizException(BizCodeEnum codeEnum){
        this.code=codeEnum.getCode();
        this.message=codeEnum.getMessage();
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
