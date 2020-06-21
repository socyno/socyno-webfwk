package com.socyno.webfwk.menu;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.socyno.stateform.abs.AbstractStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统菜单面板")
public class SystemMenuPaneSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        @Getter
        private static final FieldOptionsState instance = new FieldOptionsState();
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemMenuPaneService.getInstance().getStates();
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
    
    @Attributes(title = "名称")
    private String name;
    
    @Attributes(title = "图标")
    private String icon;
    
    @Attributes(title = "路径")
    private String path;
    
    @Attributes(title = "排序")
    private Integer order;
}
