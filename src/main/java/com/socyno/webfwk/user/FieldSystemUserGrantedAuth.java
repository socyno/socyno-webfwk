package com.socyno.webfwk.user;

import java.util.Collections;
import java.util.List;

import com.github.reinert.jjschema.v1.FieldOption;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemUserGrantedAuth extends FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private final static FieldSystemUserGrantedAuth instance = new FieldSystemUserGrantedAuth();
    
    @Override
    public List<? extends FieldOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        if (filter == null || !SystemUserService.getInstance().getFormName().contentEquals(filter.getFormName())
                || filter.getFormId() ==null) {
            return Collections.emptyList();
        }
        return FieldSystemUserAuth.getInstance().queryByUserIds(filter.getFormId());
    }

}

