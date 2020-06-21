package com.socyno.webfwk.menu;


import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单目录")
public class SystemMenuDirDetail extends SystemMenuDirSimple implements SystemMenuDirWithPaneEntity {

    @Attributes(title = "面板", required = true, type = FieldSystemMenuPane.class)
    private SystemMenuPaneOption menuPane;
}
