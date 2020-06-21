package com.socyno.webfwk.menu;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Attributes(title = "系统菜单目录")
public class SystemMenuDirOption extends SystemMenuDirSimple implements FieldOption {
    @Override
    public void setOptionValue(String value) {
        setId(Long.valueOf(value));
    }
    
    @Override
    public String getOptionValue() {
        return "" + getId();
    }
    
    @Override
    public String getOptionDisplay() {
        return getName();
    }
    
    @Override
    public String getOptionGroup() {
        return getPaneName();
    }
}
