package com.socyno.webfwk.user;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;

@Attributes(title = "系统用户清单")
public class OptionSystemUser extends SystemUserSimple implements FieldOption {
    
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
    
    @Override
    public int hashCode() {
        if (getOptionValue() != null) {
            return getOptionValue().hashCode();
        }
        if (getId() != null) {
            return getId().hashCode();
        }
        if (getUsername() != null) {
            return getUsername().hashCode();
        }
        return 0;
    }
    
    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof OptionSystemUser)) {
            return false;
        }
        OptionSystemUser anotherUser = (OptionSystemUser)another;
        if (getOptionValue() != null && getOptionValue().equals(anotherUser.getOptionValue())) {
            return true;
        }
        if (getId() != null && getId().equals(anotherUser.getId())) {
            return true;
        }
        if (getUsername() != null && getUsername().equals(anotherUser.getUsername())) {
            return true;
        }
        return false;
    }
}
