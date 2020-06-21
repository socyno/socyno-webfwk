package com.socyno.webfwk.menu;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemMenuDir extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemMenuDir instance = new FieldSystemMenuDir();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<SystemMenuDirOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemMenuDirService.getInstance().listForm(SystemMenuDirOption.class,
                new SystemMenuDirDefaultQuery(50, 1L).setNameLike(filter.getKeyword())).getList();
    }
    
    @Override
    public List<SystemMenuDirOption> queryDynamicValues(Object[] values) throws Exception {
        long[] menuDirIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(values);
        if (menuDirIds == null || menuDirIds.length <= 0) {
            return Collections.emptyList();
        }
        return SystemMenuDirService.getInstance()
                .listForm(SystemMenuDirOption.class, new SystemMenuDirDefaultQuery(menuDirIds.length, 1L)
                        .setMenuDirIdsIn(StringUtils.join(menuDirIds, ',')))
                .getList();
    }
}
