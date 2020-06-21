package com.socyno.webfwk.user;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加用户授权")
public class SystemUserForAuthsAdd extends BasicStateForm {
    
    @Attributes(title = "用户", position = 1100, readonly = true)
    private String username;
    
    @Attributes(title = "姓名", position = 1200, readonly = true)
    private String display;

    @Attributes(title = "授权添加", position = 1300, required = true, type = FieldSystemUserAuth.class)
    private List<OptionSystemUserAuth> authsAdded;
    
}
