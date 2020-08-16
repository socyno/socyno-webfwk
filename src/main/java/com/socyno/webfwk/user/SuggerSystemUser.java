package com.socyno.webfwk.user;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.sugger.AbstractStateFormSugger.Definition;
import com.socyno.stateform.sugger.AbstractStateFormSugger.OptionClass;


public class SuggerSystemUser extends Definition {
    
    @Override
    public Class<?> getTypeClass() {
        return null;
    }
    
    private static final OptionClass<?> optionClass = new OptionClass<OptionSystemUser>() {
        @Override
        protected Class<OptionSystemUser> getType() {
            return OptionSystemUser.class;
        }
        
        @Override
        protected Collection<OptionSystemUser> queryOptions(Collection<OptionSystemUser> values) throws Exception {
            if (values == null) {
                return null;
            }
            String optionValue;
            Set<String> optionValues = new HashSet<>();
            for (OptionSystemUser v : values) {
                if ((optionValue = v.getOptionValue()) != null) {
                    optionValues.add(optionValue);
                }
            }
            return FieldSystemUser.getInstance().queryDynamicValues(optionValues.toArray());
        }
        
        @Override
        protected void fillOriginOption(Object form, OptionSystemUser origin, OptionWrapper wrapper, Field field,
                Attributes fieldAttrs) throws Exception {
            return;
        }
    };
    
    @Override
    public OptionClass<?> getOptionClass() {
        return optionClass;
    }
}
