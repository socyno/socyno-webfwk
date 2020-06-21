package com.socyno.webfwk.feature;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemFeatureTenant extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemFeatureTenant instance = new FieldSystemFeatureTenant();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<SystemFeatureOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemFeatureService.getInstance().queryWithTenant(SystemFeatureOption.class,
                SessionContext.getTenantOrNull(), filter.getKeyword(), 1L, 50).getList();
    }
    
    @Override
    public List<SystemFeatureOption> queryDynamicValues(Object[] values) throws Exception {
        long[] featureIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(values);
        if (featureIds == null || featureIds.length <= 0) {
            return Collections.emptyList();
        }
        return SystemFeatureService.getInstance().queryByIds(SystemFeatureOption.class, featureIds);
    }
}
