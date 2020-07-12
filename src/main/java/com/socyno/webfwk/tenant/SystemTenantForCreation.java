package com.socyno.webfwk.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.webbsc.model.SystemTenantDbInfo;
import com.socyno.webfwk.tenant.SystemTenantForDatabasesAdd.FieldDatabaseCreation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "创建租户信息")
public class SystemTenantForCreation implements AbstractSystemTenant {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "租户代码", position = 1100, required = true)
    private String code;
    
    @Attributes(title = "租户名称", position = 1200, required = true)
    private String name;
    
    @Attributes(title = "数据库", position = 1300, type = FieldDatabaseCreation.class)
    private List<SystemTenantDbInfo> databasesAdded;
}
