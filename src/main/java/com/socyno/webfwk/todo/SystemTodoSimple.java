package com.socyno.webfwk.todo;

import java.util.Date;
import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.socyno.base.bscfield.FieldDateTime;
import com.socyno.base.bscfield.FieldText;
import com.socyno.base.bscfield.FieldTextLine;
import com.socyno.stateform.abs.AbstractStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项基本数据")
public class SystemTodoSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        @Getter
        private static final FieldOptionsState instance = new FieldOptionsState();
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemTodoService.getInstance().getStates();
        }
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "标题", type = FieldTextLine.class)
    private String title;
    
    @Attributes(title = "类型")
    private String category;
    
    @Attributes(title = "待办项标识")
    private String targetKey;
    
    @Attributes(title = "流程单标识")
    private String targetId;
    
    @Attributes(title = "待办项页面", type = FieldTextLine.class)
    private String targetPage;
    
    @Attributes(title = "创建时间", type = FieldDateTime.class)
    private Date   createdAt;
    
    @Attributes(title = "创建人编号")
    private Long   createdUserId;
    
    @Attributes(title = "创建人账户")
    private String createdUserName;
    
    @Attributes(title = "创建人姓名")
    private String createdUserDisplay;
    
    @Attributes(title = "处理时间", type = FieldDateTime.class)
    private Date   closedAt;
    
    @Attributes(title = "处理意见", type = FieldText.class)
    private String result;
    
    @Attributes(title = "处理人编号")
    private Long   closedUserId;
    
    @Attributes(title = "处理人账户")
    private String closedUserName;

    @Attributes(title = "处理人姓名")
    private String closedUserDisplay;
    
    @Attributes(title = "发起人编号")
    private Long applyUserId;
    
    @Attributes(title = "发起人账户")
    private String applyUserName;
    
    @Attributes(title = "发起人姓名")
    private String applyUserDisplay;
}
