package com.socyno.webfwk.role;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemRole extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemRole instance = new FieldSystemRole();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<SystemRoleOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemRoleService.getInstance().queryByNameLike(SystemRoleOption.class, filter.getKeyword(), 1L, 50)
                .getList();
    }
    
    @Override
    public List<SystemRoleOption> queryDynamicValues(Object[] values) throws Exception {
        long[] roleIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(values);
        if (roleIds == null || roleIds.length <= 0) {
            return Collections.emptyList();
        }
        return SystemRoleService.getInstance().queryByIds(SystemRoleOption.class, roleIds);
    }
}
