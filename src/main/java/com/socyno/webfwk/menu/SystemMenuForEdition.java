package com.socyno.webfwk.menu;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑系统菜单")
public class SystemMenuForEdition extends BasicStateForm {
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径", required = true)
    private String path;
    
    @Attributes(title = "排序", required = true)
    private Integer order;
    
    @Attributes(title = "目录", required = true, type = FieldSystemMenuDir.class)
    private SystemMenuDirOption menuDir;
    
}
