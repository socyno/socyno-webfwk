package com.socyno.webfwk.initail;

import com.github.reinert.jjschema.Attributes;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SystemBasicInfo {
    
    @Attributes(title = "是否完成初始化")
    private boolean initialized = false;
    
    @Attributes(title = "超级租户代码")
    private String superTenantCode;
    
}
