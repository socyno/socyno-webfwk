package com.socyno.webfwk.notify;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscmodel.PagedList;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.base.bscsqlutil.AbstractDao.ResultSetProcessor;
import com.socyno.base.bscsqlutil.SqlQueryUtil;
import com.socyno.stateform.abs.AbstractStateAction;
import com.socyno.stateform.abs.AbstractStateCreateAction;
import com.socyno.stateform.abs.AbstractStateFormServiceWithBaseDao;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.stateform.authority.Authority;
import com.socyno.stateform.authority.AuthorityScopeType;
import com.socyno.stateform.service.TenantSpecialDataSource;
import com.socyno.stateform.util.*;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

public class SystemNotifyTemplateService extends AbstractStateFormServiceWithBaseDao<SystemNotifyTemplateSimple> {
    
    @Getter
    private final static SystemNotifyTemplateService instance = new SystemNotifyTemplateService();
    
    private SystemNotifyTemplateService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        DISABLED ("disabled", "禁用"),
        ENABLED  ("enabled", "有效") ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    private void checkNotifyTemplateFormChange(SystemNotifyTemplateForCreation changed) throws Exception {
        if (StringUtils.isBlank(changed.getCode()) || changed.getCode().matches("^\\s*\\d+\\s*$")) {
            throw new MessageException("通知模板的代码，不允许为纯数字！");
        }
        StringBuffer sql = new StringBuffer(
                String.format("SELECT COUNT(1) FROM %s WHERE code = ?", getFormTable()));
        if (changed.getId() != null) {
            sql.append("AND id != ").append(changed.getId());
        }
        if (getFormBaseDao().queryAsObject(Long.class, sql.toString(),
                new Object[] { changed.getCode() }) > 0) {
            throw new MessageException(String.format("通知模板的代码名称(%s)已被占用，请重新命名！", changed.getCode()));
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemNotifyTemplateSimple, SystemNotifyTemplateForCreation, SystemNotifyTemplateSimple> {
        
        public EventCreate () {
            super("添加", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemNotifyTemplateSimple handle(String event, SystemNotifyTemplateSimple originForm, SystemNotifyTemplateForCreation form, String message) throws Exception {
            checkNotifyTemplateFormChange(form);
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                        .put("code",            form.getCode())
                        .put("comment",         form.getComment())
                        .put("mail_to",         form.getMailTo())
                        .put("mail_cc",         form.getMailCc())
                        .put("mail_content",    form.getMailContent())
                        .put("message_to",      form.getMessageTo())
                        .put("message_content", form.getMessageContent())
                        .put("created_at",      new Date())
                        .put("created_by",      SessionContext.getTokenUserId())
                        .put("created_code_by", SessionContext.getTokenUsername())
                        .put("created_name_by", SessionContext.getTokenDisplay())
            ), new ResultSetProcessor () {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    id.set(r.getLong(1));
                }
            });
            return getForm(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemNotifyTemplateSimple, SystemNotifyTemplateForEdition, Void> {
        
        public EventEdit () {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemNotifyTemplateSimple originForm, final SystemNotifyTemplateForEdition form, final String message) throws Exception {
            checkNotifyTemplateFormChange(form);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                getFormTable(), new ObjectMap()
                    .put("=id",            form.getId())
                    .put("code",            form.getCode())
                    .put("comment",         form.getComment())
                    .put("mail_to",         form.getMailTo())
                    .put("mail_cc",         form.getMailCc())
                    .put("mail_content",    form.getMailContent())
                    .put("message_to",      form.getMessageTo())
                    .put("message_content", form.getMessageContent())
                    ));
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SystemNotifyTemplateSimple, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateSimple form, String sourceState) {
            
        }
    }
    
    public class EventEnable extends AbstractStateAction<SystemNotifyTemplateSimple, BasicStateForm, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemNotifyTemplateSimple form, String sourceState) {
            
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        /**
         * 创建
         */
        Create(EventCreate.class),
        
        /**
         * 更新
         */
        Edit(EventEdit.class),
        
        /**
         * 禁用
         */
        Disable(EventDisable.class),
        
        /**
         * 恢复
         */
        Enable(EventEnable.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemNotifyTemplateSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemNotifyTemplateSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemNotifyTemplateDefaultRow>("默认查询", 
                SystemNotifyTemplateDefaultRow.class, SystemNotifyTemplateDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public SystemNotifyTemplateSimple getByCode(String tmplCode) throws Exception {
        if (StringUtils.isBlank(tmplCode)) {
            return null;
        }
        List <SystemNotifyTemplateSimple> list;
        PagedList<SystemNotifyTemplateSimple> paged;
        if ((paged = listForm(SystemNotifyTemplateSimple.class,
                new SystemNotifyTemplateDefaultQuery(1, 1L).setCodeEquals(tmplCode)))
                == null || (list = paged.getList()) == null || list.size() <= 0 ) {
            return null;
        }
        return list.get(0);
    }
    
    @Override
    public String getFormName() {
        return "system_notify_template";
    }
    
    @Override
    public String getFormTable() {
        return "system_notify_template";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return TenantSpecialDataSource.getMain();
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemNotifyTemplateSimple> forms) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
