package com.socyno.webfwk.user;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscmodel.AbstractUser;
import com.socyno.stateform.sugger.AbstractStateFormSugger.Definition;
import com.socyno.stateform.sugger.AbstractStateFormSugger.OptionClass;

public class SuggerSystemUserGrantedAuth extends Definition {
    @Override
    public Class<?> getTypeClass() {
        return null;
    }
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionSystemUserAuth>() {
        @Override
        protected Class<OptionSystemUserAuth> getType() {
            return OptionSystemUserAuth.class;
        }
        
        @Override
        protected Collection<OptionSystemUserAuth> queryOptions(Collection<OptionSystemUserAuth> values)
                throws Exception {
            if (values == null || values.size() <= 0) {
                return null;
            }
            Long userId;
            Set<Object> userIds = new HashSet<>();
            for (OptionSystemUserAuth v : values) {
                if ((userId = v.getUserId()) == null) {
                    continue;
                }
                userIds.add(userId);
            }
            return FieldSystemUserAuth.getInstance().queryByUserIds(userIds.toArray(new Long[0]));
        }
        
        @Override
        protected Object parseOriginValue(Object form, Field field, OptionWrapper wrapper, Attributes fieldAttrs)
                throws Exception {
            if (!(form instanceof AbstractUser)) {
                return null;
            }
            Long userId;
            if ((userId = ((AbstractUser)form).getId()) == null) {
                return null;
            }
            OptionSystemUserAuth option = new OptionSystemUserAuth();
            option.setUserId(userId);
            return wrapper.fromFieldFlatValues(OptionSystemUserAuth.class,
                    Arrays.asList(new OptionSystemUserAuth[] { option }));
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionSystemUserAuth origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
        
        @Override
        protected void setMatchedValues(Object form, Field field, Object[] flatValues,
                Map<Object, Object> mappedFinalValues, Definition.OptionWrapper wrapper, Attributes fieldAttrs)
                throws Exception {
            if (flatValues == null || mappedFinalValues == null) {
                return;
            }
            OptionSystemUserAuth origin;
            OptionSystemUserAuth fetched;
            List<OptionSystemUserAuth> matched = new ArrayList<>();
            for (Object v : flatValues) {
                origin = (OptionSystemUserAuth) v;
                for (Object m : mappedFinalValues.values()) {
                    fetched = (OptionSystemUserAuth) m;
                    if (origin.getUserId().equals(fetched.getUserId())) {
                        matched.add(fetched);
                    }
                }
                field.setAccessible(true);
                field.set(form, wrapper.fromFieldFlatValues(OptionSystemUserAuth.class, matched));
                break;
            }
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}