package com.socyno.webfwk.tenant;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "编辑租户信息")
public class SystemTenantForEdition implements AbstractSystemTenant {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "状态", readonly = true)
    private String state;
    
    @Attributes(title = "租户代码", readonly = true)
    private String code;
    
    @Attributes(title = "租户名称", required = true)
    private String name;
}
