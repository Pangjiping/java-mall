package org.epha.mall.member.exception;

/**
 * @author pangjiping
 */
public class AccountNotExistException extends RuntimeException{
    public AccountNotExistException() {
        super("账户不存在");
    }
}
