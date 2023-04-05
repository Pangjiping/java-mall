package org.epha.mall.member.exception;

/**
 * @author pangjiping
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已经存在");
    }
}
