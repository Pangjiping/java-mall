package org.epha.mall.member.exception;

/**
 * @author pangjiping
 */
public class PasswordMismatchException extends RuntimeException{
    public PasswordMismatchException() {
        super("账户或密码错误");
    }
}
