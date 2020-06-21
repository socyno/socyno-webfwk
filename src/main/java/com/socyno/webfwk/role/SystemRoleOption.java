package com.socyno.webfwk.role;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Attributes(title = "系统角色列表信息")
public class SystemRoleOption extends SystemRoleSimple implements FieldOption {
    
    @Override
    public String getOptionValue() {
        return "" + getId();
    }
    
    @Override
    public void setOptionValue(String value) {
        setId(new Long(value));
    }
    
    @Override
    public String getOptionDisplay() {
        return String.format("%s : %s", getCode(), getName());
    }
}
