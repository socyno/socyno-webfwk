package com.socyno.webfwk.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.v1.FieldOption;
import com.github.reinert.jjschema.v1.FieldSimpleOption;
import com.github.reinert.jjschema.v1.FieldType;
import com.socyno.base.bscservice.AbstractNotifyEntity;
import com.socyno.stateform.abs.AbstractStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "通知记录内容")
public class SystemNotifyRecordSimple implements AbstractNotifyEntity, AbstractStateForm {
    
    @Getter
    public enum MessageType {
        Email("email", "邮件"),
        Message("message", "短消息")
        ;
        private final String value;
        
        private final String display;
        
        MessageType (String value, String display) {
            this.value = value;
            this.display = display;
        }
    }
    
    @Getter
    public enum SendResult {
        Skipped("skipped", "忽略"),
        Success("success", "成功"),
        Failure("failure", "失败")
        ;
        private final String value;
        
        private final String display;
        
        SendResult (String value, String display) {
            this.value = value;
            this.display = display;
        }
    }
    
    public static class FieldOptionsState extends FieldType {
        public List<? extends FieldOption> getStaticOptions() {
            return SystemNotifyRecordService.getInstance().getStates();
        }
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
    }

    public static class FieldOptionsResult extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {{
            for (SendResult type : SendResult.values()) {
                add(FieldSimpleOption.create(type.getValue(),  type.getDisplay()));
            }
        }};
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }

    public static class FieldOptionsType extends FieldType {
        @SuppressWarnings("serial")
        private final static List<FieldSimpleOption> options = new ArrayList<FieldSimpleOption>() {{
            for (MessageType type : MessageType.values()) {
                add(FieldSimpleOption.create(type.getValue(),  type.getDisplay()));
            }
        }};
        
        public FieldType.FieldOptionsType getOptionsType() {
            return FieldOptionsType.STATIC;
        }
        
        public List<FieldSimpleOption> getStaticOptions() {
            return Collections.unmodifiableList(options);
        }
    }
    
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态", type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "类型", type = FieldOptionsType.class)
    private String type;
    
    @Attributes(title = "通知内容")
    private String content;
    
    @Attributes(title = "收件人", description = "多个可使用逗号或分号分隔")
    private String messageTo;
    
    @Attributes(title = "抄送人", description = "多个可使用逗号或分号分隔")
    private String messageCc;
    
    @Attributes(title = "发送结果", type = FieldOptionsResult.class)
    private String result;
    
    @Attributes(title = "创建时间")
    private Date createdAt;
    
    @Attributes(title = "创建人编号")
    private Long createdBy;
    
    @Attributes(title = "创建人帐户")
    private String createdCodeBy;
    
    @Attributes(title = "创建人姓名")
    private String createdNameBy;
}
