package org.epha.mall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * @author pangjiping
 */
@Data
public class UserLoginVo {
    @NotEmpty(message = "账号不能为空")
    private String loginAccount;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码必须是6-18位字符")
    private String password;
}
