package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.AbstractStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加域用户")
public class SystemUserForCreationDomain implements AbstractStateForm {
    
    @Attributes(title = "编号")
    private Long  id;
    
    @Attributes(title = "状态")
    private String  state;

    @Attributes(title = "版本")
    private Long  revision;

    @Attributes(title = "用户", position = 1010, required = true)
    private String  username;
}
