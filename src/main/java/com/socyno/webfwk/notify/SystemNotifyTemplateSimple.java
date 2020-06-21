package com.socyno.webfwk.notify;

import java.util.Date;
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
@Attributes(title = "通知模板详情")
public class SystemNotifyTemplateSimple implements AbstractStateForm {
    
    public static class FieldOptionsState extends FieldType {
        
        @Getter
        private static final FieldOptionsState instance = new FieldOptionsState();
        
        public List<? extends FieldOption> getStaticOptions() {
            return SystemNotifyTemplateService.getInstance().getStates();
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
    
    @Attributes(title = "代码", description = "模板的唯一标识，必须确保唯一性")
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
    
    @Attributes(title = "创建时间")
    private Date createdAt;
    
    @Attributes(title = "创建人编号")
    private Long createdBy;
    
    @Attributes(title = "创建人帐户")
    private String createdCodeBy;

    @Attributes(title = "创建人姓名")
    private String createdNameBy;
    
}
