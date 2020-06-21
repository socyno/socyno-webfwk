package com.socyno.webfwk.menu;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.stateform.field.FieldSystemAuths;
import com.socyno.stateform.field.OptionSystemAuth;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加菜单授权")
public class SystemMenuForAuthsAdd extends BasicStateForm {

    @Attributes(title = "面板", readonly = true)
    private String paneName;

    @Attributes(title = "目录", readonly = true)
    private String dirName;
    
    @Attributes(title = "名称", readonly = true)
    private String name;
    
    @Attributes(title = "路径", readonly = true)
    private String path;
    
    @Attributes(title = "授权添加", required = true, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> authsAdded;
}
