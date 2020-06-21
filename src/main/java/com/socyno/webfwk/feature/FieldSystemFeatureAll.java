package com.socyno.webfwk.feature;

import java.util.Collections;
import java.util.List;
import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;
import lombok.Getter;

public class FieldSystemFeatureAll extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemFeatureAll instance = new FieldSystemFeatureAll();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<SystemFeatureOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemFeatureService.getInstance().queryByNameLike(SystemFeatureOption.class, filter.getKeyword(), 1L, 50)
                .getList();
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
