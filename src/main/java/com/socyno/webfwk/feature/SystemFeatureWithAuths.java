package com.socyno.webfwk.feature;

import java.util.List;

import com.socyno.stateform.field.OptionSystemAuth;

public interface SystemFeatureWithAuths {
    
    public List<OptionSystemAuth> getAuths();
    
    public void setAuths(List<OptionSystemAuth> auths);
    
}
