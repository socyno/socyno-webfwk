package com.socyno.webfwk.todo;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.webfwk.user.FieldSystemUser;
import com.socyno.webfwk.user.SystemUserOption;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项创建")
public class SystemTodoForCreation extends BasicStateForm {
    
    @Attributes(title = "标题", required = true)
    private String title;
    
    @Attributes(title = "类型", required = true)
    private String category;
    
    @Attributes(title = "待办项标识", required = true)
    private String targetKey;
    
    @Attributes(title = "流程单标识", required = true)
    private String targetId;
    
    @Attributes(title = "待办项页面")
    private String targetPage;
    
    @Attributes(title = "流程发起人", type = FieldSystemUser.class)
    private SystemUserOption applyUser;
    
    @Attributes(title = "审批人清单", required = true, type = FieldSystemUser.class)
    private List<SystemUserOption> assignee;
}
