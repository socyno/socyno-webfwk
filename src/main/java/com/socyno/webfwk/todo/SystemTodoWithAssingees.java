package com.socyno.webfwk.todo;

import java.util.List;

import com.socyno.webfwk.user.OptionSystemUser;

public interface SystemTodoWithAssingees {
    
    public List<OptionSystemUser> getAssignees();
    
    public void setAssignees(List<OptionSystemUser> assignees);
    
}
