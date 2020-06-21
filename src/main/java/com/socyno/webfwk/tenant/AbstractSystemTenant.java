package com.socyno.webfwk.tenant;

import com.socyno.stateform.abs.AbstractStateForm;

public interface AbstractSystemTenant extends AbstractStateForm {
    
    public String getCode();
    
    public String getName();
    
    public default boolean isEnabled() {
        return SystemTenantService.STATES.ENABLED.getCode().equals(getState());
    }
    
    public default boolean isDisabled() {
        return !isEnabled();
    }
}
