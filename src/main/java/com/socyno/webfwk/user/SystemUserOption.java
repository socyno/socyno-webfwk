package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Attributes(title = "系统用户清单")
public class SystemUserOption extends SystemUserSimple implements FieldOption {
    
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
        return String.format("%s(%s)", getDisplay(), getUsername());
    }
}
