package com.socyno.webfwk.menu;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加菜单目录")
public class SystemMenuDirForCreation extends BasicStateForm {
    
    @Attributes(title = "名称", required = true)
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径", required = true)
    private String path;
    
    @Attributes(title = "排序", required = true)
    private Integer order;
    
    @Attributes(title = "面板", required = true, type = FieldSystemMenuPane.class)
    private SystemMenuPaneOption menuPane;
}
