package com.socyno.webfwk.menu;

import java.util.List;

import com.socyno.webbsc.authority.AuthorityEntity;

public interface SystemMenuWithAuths {
    
    public List<AuthorityEntity> getAuths();
    
    public void setAuths(List<AuthorityEntity> auths);
    
}
