package com.socyno.webfwk.tenant;

import java.util.List;

import com.socyno.webbsc.model.SystemTenantDbInfoWithId;

public interface SystemTenantWithDbInfos {
    
    public List<SystemTenantDbInfoWithId> getDatabases();

    public void setDatabases(List<SystemTenantDbInfoWithId> dbinfos);
}
