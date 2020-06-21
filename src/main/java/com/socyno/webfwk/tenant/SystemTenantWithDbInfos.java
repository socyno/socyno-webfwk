package com.socyno.webfwk.tenant;

import java.util.List;

import com.socyno.stateform.model.SystemTenantDbInfoWithId;

public interface SystemTenantWithDbInfos {
    
    public List<SystemTenantDbInfoWithId> getDatabases();

    public void setDatabases(List<SystemTenantDbInfoWithId> dbinfos);
}
