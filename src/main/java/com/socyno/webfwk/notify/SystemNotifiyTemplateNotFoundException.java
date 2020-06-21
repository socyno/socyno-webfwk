package com.socyno.webfwk.notify;

import com.socyno.base.bscexec.MessageException;

public class SystemNotifiyTemplateNotFoundException extends MessageException {
    
    private static final long serialVersionUID = 1L;
    
    public SystemNotifiyTemplateNotFoundException(String template) {
        super(String.format("No such notify template found: %s", template));
    }
    
}
