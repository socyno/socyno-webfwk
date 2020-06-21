package com.socyno.webfwk.tenant;

import java.util.List;

import com.socyno.webfwk.feature.SystemFeatureOption;

public interface SystemTenantWithFeatures {
    
    public List<SystemFeatureOption> getFeatures();

    public void setFeatures(List<SystemFeatureOption> features);
    
}
