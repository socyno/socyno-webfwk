package com.socyno.webfwk.todo;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.webfwk.user.FieldSystemUser;
import com.socyno.webfwk.user.OptionSystemUser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "待办事项详情")
public class SystemTodoDetail extends SystemTodoSimple implements SystemTodoWithAssingees {
    
    @Attributes(title = "审批人清单", type = FieldSystemUser.class)
    private List<OptionSystemUser> assignees;
}
