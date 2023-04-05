package org.epha.mall.member.exception;

/**
 * @author pangjiping
 */
public class UserNameExistException extends RuntimeException {

    public UserNameExistException() {
        super("用户名已经存在");
    }
}
