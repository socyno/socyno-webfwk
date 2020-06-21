package com.socyno.webfwk.todo;

import java.util.List;

import com.socyno.webfwk.user.SystemUserOption;

public interface SystemTodoWithAssingees {
    
    public List<SystemUserOption> getAssignees();
    
    public void setAssignees(List<SystemUserOption> assignees);
    
}
