package com.socyno.webfwk.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.webbsc.authority.AuthorityScope;
import com.socyno.stateform.exec.StateFormFieldInvalidOptionException;
import com.socyno.stateform.service.PermissionService;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OptionSystemUserAuth implements FieldOption {
    
    private final static Pattern REGEXP_OPTION_VALUE = Pattern.compile("^([^,]+),(\\d+),(\\d+)$");
    
    @Attributes(title = "授权角色编号", required = true)
    private Long    roleId;
    
    @Attributes(title = "授权范围", required = true)
    private String  scopeType;
    
    @Attributes(title = "授权标的", required = true)
    private Long    scopeId;
    
    @Attributes(title = "授权范围", position = 1010, readonly = true)
    private String  scopeTypeName;
    
    @Attributes(title = "授权标的名称" , position = 1020, readonly = true)
    private String  scopeName;
    
    @Attributes(title = "授权角色名称", position = 1031, readonly = true)
    private String  roleName;
    
    @Attributes(title = "授予用户编号", readonly = true)
    private Long    userId;
    
    @Override
    public void setOptionValue(String value) {
        Matcher matched;
        AuthorityScope scopTypeEnum;
        if (StringUtils.isBlank(value) || (matched = REGEXP_OPTION_VALUE.matcher(value)) == null 
                || !matched.find() || (scopTypeEnum = PermissionService.getInstance().getProvidedAuthorityScope(matched.group(1))) == null) {
            throw new StateFormFieldInvalidOptionException(OptionSystemUserAuth.class, value);
        }
        scopeType = scopTypeEnum.getName();
        roleId = new Long(matched.group(3));
        scopeId = scopTypeEnum.isSubsystem() ? new Long(matched.group(2)) : 0;
    }
    
    @Override
    public String getOptionValue() {
        return String.format("%s,%s,%s", getScopeType(), getScopeId(), getRoleId());
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s,%s,%s", getScopeTypeName(), getScopeName(), getRoleName());
    }
}
