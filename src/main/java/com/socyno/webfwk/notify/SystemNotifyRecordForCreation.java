package com.socyno.webfwk.notify;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.webfwk.notify.SystemNotifyRecordSimple.FieldOptionsType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "创建通知记录")
public class SystemNotifyRecordForCreation extends BasicStateForm {
    
    @Attributes(title = "类型", required = true, type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "收件人", description = "多个可使用逗号或分号分隔")
    private String messageTo;
    
    @Attributes(title = "抄送人", description = "多个可使用逗号或分号分隔")
    private String messageCc;
    
    @Attributes(title = "通知内容", required = true)
    private String content;
}
