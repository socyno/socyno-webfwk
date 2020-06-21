package com.socyno.webfwk.user;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemUser extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemUser instance = new FieldSystemUser();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<SystemUserOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemUserService.getInstance()
                .queryByNameLike(SystemUserOption.class, filter.getKeyword(), false, 1L, 50).getList();
    }
    
    @Override
    public List<SystemUserOption> queryDynamicValues(Object[] values) throws Exception {
        long[] userIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(values);
        if (userIds == null || userIds.length <= 0) {
            return Collections.emptyList();
        }
        return SystemUserService.getInstance().queryByUserIds(SystemUserOption.class, userIds);
    }
}
