package com.socyno.webfwk.feature;

import java.util.List;

import com.socyno.webbsc.authority.AuthorityEntity;

public interface SystemFeatureWithAuths {
    
    public List<AuthorityEntity> getAuths();
    
    public void setAuths(List<AuthorityEntity> auths);
    
}
