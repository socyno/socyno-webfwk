package com.socyno.webfwk.menu;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.field.FieldSystemAuths;
import com.socyno.webbsc.authority.AuthorityEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单详情")
public class SystemMenuDetail extends SystemMenuSimple implements SystemMenuWithAuths, SystemMenuWithDirEntity {

    @Attributes(title = "菜单目录", type = FieldSystemMenuDir.class)
    private SystemMenuDirOption menuDir;
    
    @Attributes(title = "授权明细", type = FieldSystemAuths.class)
    private List<AuthorityEntity> auths;
}
