package com.socyno.webfwk.initail;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;

@Data
public class SystemInitialConfig {
    
    @Attributes(title = "超级租户代码", required = true)
    private String superTenantCode;

    @Attributes(title = "超级租户名称", required = true)
    private String superTenantName;

    @Attributes(title = "超级租户管理员账户", required = true)
    private String superAdminUsername;

    @Attributes(title = "超级租户管理员密码", required = true)
    private String superAdminPassword;

    @Attributes(title = "超级租户管理员密码", required = true)
    private String superAdminConfirmPassword;
    
    @Attributes(title = "超级租户管理员姓名", required = true)
    private String superAdminUserDisplay;

    @Attributes(title = "超级租户管理员邮箱", required = true)
    private String superAdminMailAddress;
    
}
