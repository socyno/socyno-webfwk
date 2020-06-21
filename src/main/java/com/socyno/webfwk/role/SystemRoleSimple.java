package com.socyno.webfwk.role;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.socyno.base.bscfield.FieldText;
import com.socyno.stateform.abs.AbstractStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统角色基本信息")
public class SystemRoleSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        @Getter
        private final static FieldOptionsState instance = new FieldOptionsState();
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemRoleService.getInstance().getStates();
        }
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号")
    private Long   id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long   revision;
    
    @Attributes(title = "代码")
    private String code;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
