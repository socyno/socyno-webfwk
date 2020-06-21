package com.socyno.webfwk.tenant;

import java.util.Collections;
import java.util.List;

import com.socyno.stateform.field.FilterBasicKeyword;
import com.socyno.webfwk.feature.FieldSystemFeatureAll;
import com.socyno.webfwk.feature.SystemFeatureDefaultQuery;
import com.socyno.webfwk.feature.SystemFeatureOption;
import com.socyno.webfwk.feature.SystemFeatureService;

import lombok.Getter;

public class FieldSystemFeatureForm extends FieldSystemFeatureAll {
    
    @Getter
    private static final FieldSystemFeatureForm instance = new FieldSystemFeatureForm();
    
    @Override
    public List<SystemFeatureOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        if (!SystemTenantService.getInstance().getFormName().equals(filter.getFormName())
                || filter.getFormId() == null) {
            return Collections.emptyList();
        }
        return SystemFeatureService.getInstance().listForm(SystemFeatureOption.class,
                new SystemFeatureDefaultQuery(50, 1L).setNameLike(filter.getKeyword()).setTenantId(filter.getFormId()))
                .getList();
    }
    
}
