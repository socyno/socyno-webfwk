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
@Attributes(title = "添加系统用户")
public class SystemUserForCreation extends BasicStateForm {
    
    @Attributes(title = "用户", position = 1010, required = true)
    private String username;
    
    @Attributes(title = "姓名", position = 1020, required = true)
    private String display;
    
    @Attributes(title = "邮箱", position = 1030, required = true)
    private String mailAddress;
    
    @Attributes(title = "职务", position = 1040)
    private String title;
    
    @Attributes(title = "部门", position = 1050)
    private String department;
    
    @Attributes(title = "手机", position = 1060)
    private String mobile;
    
    @Attributes(title = "座机", position = 1070)
    private String telphone;
    
    @Attributes(title = "新的密码", position = 1080, required = true, type = FieldPassword.class)
    private String newPassword;
    
    @Attributes(title = "密码确认", position = 1090, required = true, type = FieldPassword.class)
    private String confirmPassword;
}
