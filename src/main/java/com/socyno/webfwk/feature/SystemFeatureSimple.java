package com.socyno.webfwk.feature;

import java.util.Date;
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
@Attributes(title = "系统功能基础表单")
public class SystemFeatureSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        private final static FieldOptionsState INSTANCE = new FieldOptionsState();
        
        public static FieldOptionsState getInstance() {
            return INSTANCE;
        }
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemFeatureService.getInstance().getStates();
        }
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号")
    private Long   id;
    
    @Attributes(title = "版本")
    private Long   revision;
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "创建人")
    private String createdBy;
    
    @Attributes(title = "创建时间")
    private Date   createdAt;
    
    @Attributes(title = "描述", type = FieldText.class)
    private String description;
}
