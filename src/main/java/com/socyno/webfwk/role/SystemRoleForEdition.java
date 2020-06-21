package com.socyno.webfwk.role;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统角色")
public class SystemRoleForEdition extends BasicStateForm {
    
    @Attributes(title = "代码", position = 1010, required = true)
    private String code;
    
    @Attributes(title = "名称", position = 1020, required = true)
    private String name;
    
    @Attributes(title = "描述", position = 1030)
    private String description;
}
