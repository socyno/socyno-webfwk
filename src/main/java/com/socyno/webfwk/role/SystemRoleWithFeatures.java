package com.socyno.webfwk.role;

import java.util.List;

import com.socyno.webfwk.feature.SystemFeatureOption;

public interface SystemRoleWithFeatures {
    
    public List<SystemFeatureOption> getFeatures();
    
    public void setFeatures(List<SystemFeatureOption> feature);
    
}
