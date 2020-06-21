package com.socyno.webfwk.menu;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemMenuPane extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemMenuPane instance = new FieldSystemMenuPane();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<SystemMenuPaneOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemMenuPaneService.getInstance().listForm(SystemMenuPaneOption.class,
                new SystemMenuPaneDefaultQuery(50, 1L).setNameLike(filter.getKeyword())).getList();
    }
    
    @Override
    public List<SystemMenuPaneOption> queryDynamicValues(Object[] values) throws Exception {
        long[] menuPaneIds = ConvertUtil.asNonNullUniquePrimitiveLongArray(values);
        if (menuPaneIds == null || menuPaneIds.length <= 0) {
            return Collections.emptyList();
        }
        return SystemMenuPaneService.getInstance()
                .listForm(SystemMenuPaneOption.class, new SystemMenuPaneDefaultQuery(menuPaneIds.length, 1L)
                        .setMenuPaneIdsIn(StringUtils.join(menuPaneIds, ',')))
                .getList();
    }
}
