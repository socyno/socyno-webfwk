package com.socyno.webfwk.menu;

import java.util.List;

import com.socyno.stateform.field.OptionSystemAuth;

public interface SystemMenuWithAuths {
    
    public List<OptionSystemAuth> getAuths();
    
    public void setAuths(List<OptionSystemAuth> auths);
    
}
