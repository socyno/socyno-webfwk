package com.socyno.webfwk.user;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.SchemaIgnore;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemUsername extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemUsername instance = new FieldSystemUsername();
    
    @Override
    @SchemaIgnore
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    @Override
    public List<OptionSystemUsername> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        return SystemUserService.getInstance()
                .queryByNameLike(OptionSystemUsername.class, filter.getKeyword(), false, 1L, 50).getList();
    }
    
    @Override
    public List<OptionSystemUsername> queryDynamicValues(Object[] opvalues) throws Exception {
        String[] usernames = ConvertUtil.asNonBlankUniqueTrimedStringArray(opvalues);
        if (usernames == null || usernames.length <= 0) {
            return Collections.emptyList();
        }
        return SystemUserService.getInstance().queryByUsernames(OptionSystemUsername.class, usernames);
    }
}
