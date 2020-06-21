package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Attributes(title = "系统用户清单")
public class SystemUserDefaultRow extends SystemUserSimple implements SystemUserWithManagerEntity {
    
    @Attributes(title = "直属领导", type = FieldSystemUser.class)
    private SystemUserOption managerEntity;
    
}
