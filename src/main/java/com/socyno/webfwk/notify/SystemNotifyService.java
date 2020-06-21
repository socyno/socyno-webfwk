package com.socyno.webfwk.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscmodel.RunableWithSessionContext;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.base.bsctmplutil.EnjoyUtil;
import com.socyno.webfwk.notify.SystemNotifyRecordSimple.MessageType;
import com.socyno.webfwk.user.SystemUserService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemNotifyService {
    
    public final static int NOEXCEPTION_TMPL_NOTFOUD = 1;
    
    @Getter
    private final static SystemNotifyService instance = new SystemNotifyService();
    
    private static final ThreadPoolExecutor NotifyThreadsPool = new ThreadPoolExecutor(1, 5, 5, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(200), new ThreadPoolExecutor.DiscardOldestPolicy());
    
    public static void sendAsync(String template, Map<String, Object> context, int options) {
        try {
            NotifyThreadsPool.submit(new SystemNotifyThread(template, context, options));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    static class SystemNotifyThread extends RunableWithSessionContext {
        private final String template;
        private final Map<String, Object> context;
        private final int options;
        
        SystemNotifyThread(String template, Map<String, Object> context, int options) {
            this.context = context;
            this.template = template;
            this.options = options;
        }
        
        @Override
        public void exec() {
            try {
                sendSync(template, context, options);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 邮件发送
     * 
     * @param template
     *            邮件模板
     * @param context
     *            模板上下文数据
     * @return
     * @throws Exception
     */
    public static void sendSync(String template, Map<String, Object> context, int options) throws Exception {
        
        /* 获得模板信息 */
        SystemNotifyTemplateSimple tmplForm = null;
        if ((tmplForm = SystemNotifyTemplateService.getInstance()
                .getByCode(StringUtils.trimToEmpty(template))) == null) {
            if ((options & NOEXCEPTION_TMPL_NOTFOUD) != 0) {
                return;
            }
            throw new SystemNotifiyTemplateNotFoundException(template);
        }
        ObjectMap tmplContext = new ObjectMap()
                .put("systemUserService", SystemUserService.getInstance())
                .put("sessionContext", SessionContext.getUserContext());
        if (context != null) {
            tmplContext.putAll(context);
        }
        List<SystemNotifyRecordForCreation> notifies = new ArrayList<>(5);
        if (StringUtils.isNotBlank(tmplForm.getMailContent())) {
            SystemNotifyRecordForCreation mailNotify= new SystemNotifyRecordForCreation();
            mailNotify.setType(MessageType.Email.getValue());
            mailNotify.setMessageTo(tmplForm.getMailTo());
            mailNotify.setMessageCc(tmplForm.getMailCc());
            mailNotify.setContent(EnjoyUtil.format(tmplForm.getMailContent(), tmplContext.asMap()));
            notifies.add(mailNotify);
        }
        if (StringUtils.isNotBlank(tmplForm.getMessageContent())) {
            SystemNotifyRecordForCreation messageNotify = new SystemNotifyRecordForCreation();
            messageNotify.setType(MessageType.Message.getValue());
            messageNotify.setMessageTo(tmplForm.getMessageTo());
            messageNotify.setContent(EnjoyUtil.format(tmplForm.getMessageContent(), tmplContext.asMap()));
            notifies.add(messageNotify);
        }
        for (SystemNotifyRecordForCreation notify : notifies) {
            SystemNotifyRecordService.getInstance().triggerAction(SystemNotifyRecordService.EVENTS.Create.getName(), notify);
        }
    }
}
