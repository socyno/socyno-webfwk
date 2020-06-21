package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统用户敏感数据")
public class SystemUserSecurityOnly extends SystemUserSimple implements SystemUserWithSecurities {

    @Attributes(title = "手机")
    private String mobile;
    
    @Attributes(title = "座机")
    private String telphone;
    
    @Attributes(title = "邮箱")
    private String mailAddress;
}
