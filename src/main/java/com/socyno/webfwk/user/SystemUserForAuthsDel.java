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
@Attributes(title = "移除用户授权")
public class SystemUserForAuthsDel extends BasicStateForm {
    
    @Attributes(title = "用户", position = 1100, readonly = true)
    private String username;
    
    @Attributes(title = "姓名", position = 1200, readonly = true)
    private String display;

    @Attributes(title = "授权移除", position = 1300, required = true, type = FieldSystemUserGrantedAuth.class)
    private List<OptionSystemUserAuth> authsRemoved;
    
}
