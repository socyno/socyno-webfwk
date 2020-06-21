package com.socyno.webfwk.menu;

import java.util.List;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统菜单树")
public class SystemMenuTree {
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "排序")
    private Integer order;
    
    @Attributes(title = "路径")
    private String path;
    
    @Attributes(title = "父节点")
    private Long parentId;
    
    @Attributes(title = "子集")
    private List<SystemMenuTree> children;
    
    @Attributes(title = "授权")
    private Set<String> authKeys;

    @Attributes(title = "打开方式")
    private String openType;
}
