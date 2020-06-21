package com.socyno.webfwk.notify;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "创建通知模板")
public class SystemNotifyTemplateForCreation extends BasicStateForm {
    
    @Attributes(title = "代码", required = true, description = "模板的唯一标识，必须确保唯一性")
    private String code;
    
    @Attributes(title = "功能描述")
    private String comment;
    
    @Attributes(title = "邮件收件人", description = "多个可使用逗号或分号分隔")
    private String mailTo;
    
    @Attributes(title = "邮件抄送人", description = "多个可使用逗号或分号分隔")
    private String mailCc;
    
    @Attributes(title = "移动手机号", description = "当指定了移动手机号是，将以短消息的方式发送。多个可使用逗号或分号分隔。")
    private String messageTo;
    
    @Attributes(title = "邮件模板", description = "使用 jFinal Enjoy 模板格式，参见 : https://jfinal.com/doc/6-1")
    private String mailContent;
    
    @Attributes(title = "短消息模板", description = "使用 jFinal Enjoy 模板格式，参见 : https://jfinal.com/doc/6-1")
    private String messageContent;
    
}
