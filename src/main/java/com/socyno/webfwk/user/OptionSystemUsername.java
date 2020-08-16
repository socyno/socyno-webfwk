package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;

@Attributes(title = "系统用户清单")
public class OptionSystemUsername extends OptionSystemUser {
    
    @Override
    public String getOptionValue() {
        return getUsername();
    }
    
    @Override
    public void setOptionValue(String value) {
        setUsername(value);
    }
}
