package com.socyno.webfwk.feature;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Attributes(title = "系统功能可选项信息")
public class SystemFeatureOption extends SystemFeatureSimple implements FieldOption {
    
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
        return getName();
    }
}
