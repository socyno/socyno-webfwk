package com.socyno.webfwk.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "租户基本信息")
public class SystemTenantSimple implements AbstractSystemTenant {
    
    public static class FieldOptionsState extends FieldType {
        
        @Getter
        private static final FieldOptionsState instance = new FieldOptionsState();
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemTenantService.getInstance().getStates();
        }
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "租户代码", required = true)
    private String code;
    
    @Attributes(title = "租户名称", required = true)
    private String name;
}
