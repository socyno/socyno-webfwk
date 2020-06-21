package com.socyno.webfwk.tenant;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Attributes(title = "租户基本信息")
public class SystemTenantOption extends SystemTenantSimple implements FieldOption {

    @Override
    public void setOptionValue(String value) {
        setId(Long.valueOf(value));
    }

    @Override
    public String getOptionValue() {
        return getId() + "";
    }

    @Override
    public String getOptionDisplay() {
        return String.format("%s:%s", getCode(), getName());
    }
    
}
