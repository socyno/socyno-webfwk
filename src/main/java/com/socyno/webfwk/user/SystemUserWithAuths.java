package com.socyno.webfwk.user;

import java.util.List;

public interface SystemUserWithAuths {
    
    public List<OptionSystemUserAuth> getSystemAuths();

    public void setSystemAuths(List<OptionSystemUserAuth> auths);

}
