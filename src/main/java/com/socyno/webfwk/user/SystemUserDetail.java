package com.socyno.webfwk.user;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统用户详情")
public class SystemUserDetail extends SystemUserSimple implements SystemUserWithSecurities,
                        SystemUserWithAuths, SystemUserWithManagerEntity {
    public static class FieldOptionsState extends FieldType {
        public List<? extends FieldOption> getStaticOptions() {
            return SystemUserService.getInstance().getStates();
        }
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "邮箱", position = 3100)
    private String mailAddress;
    
    @Attributes(title = "手机", position = 3200)
    private String mobile;
    
    @Attributes(title = "座机", position = 3300)
    private String telphone;
    
    @Attributes(title = "直属领导", position = 3400, type = FieldSystemUser.class)
    private SystemUserOption managerEntity;

    @Attributes(title = "授权", position = 3500, type = FieldSystemUserAuth.class)
    private List<OptionSystemUserAuth> systemAuths;
}
