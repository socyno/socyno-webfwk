package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscfield.FieldPassword;
import com.socyno.stateform.abs.BasicStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "修改用户密码")
public class SystemUserForNewPassword extends BasicStateForm {

    @Attributes(title = "密码", position = 1010, required = true, type = FieldPassword.class)
    private String password;

    @Attributes(title = "新的密码", position = 1020, required = true, type = FieldPassword.class)
    private String newPassword;
    
    @Attributes(title = "密码确认", position = 1030, required = true, type = FieldPassword.class)
    private String confirmPassword;
}
