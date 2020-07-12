package com.socyno.webfwk.todo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.AbstractUser;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscmodel.PagedList;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.base.bscsqlutil.AbstractDao.ResultSetProcessor;
import com.socyno.base.bscsqlutil.SqlQueryUtil;
import com.socyno.base.bsctmplutil.EnjoyUtil;
import com.socyno.stateform.abs.AbstractStateAction;
import com.socyno.stateform.abs.AbstractStateCreateAction;
import com.socyno.stateform.abs.AbstractStateDeleteAction;
import com.socyno.stateform.abs.AbstractStateEnterAction;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.stateform.abs.AbstractStateFormServiceWithBaseDao;
import com.socyno.stateform.abs.AbstractStateLeaveAction;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityEveryoneChecker;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.stateform.util.StateFormEventClassEnum;
import com.socyno.stateform.util.StateFormNamedQuery;
import com.socyno.stateform.util.StateFormQueryBaseEnum;
import com.socyno.stateform.util.StateFormStateBaseEnum;
import com.socyno.webbsc.ctxutil.ContextUtil;
import com.socyno.webbsc.service.jdbc.TenantSpecialDataSource;
import com.socyno.webfwk.user.SystemUserOption;
import com.socyno.webfwk.user.SystemUserSecurityOnly;
import com.socyno.webfwk.user.SystemUserService;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

public class SystemTodoService extends AbstractStateFormServiceWithBaseDao<SystemTodoSimple> {
    
    @Getter
    private static final SystemTodoService instance = new SystemTodoService();
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum  {
        OPENED("0", "待处理"),
        CLOSED("1", "已关闭")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemTodoDefaultRow>("通用查询", 
                SystemTodoDefaultRow.class, SystemTodoDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemTodoSimple, SystemTodoForCreation, SystemTodoSimple> {
        
        public EventCreate() {
            super("添加", STATES.OPENED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = AuthorityEveryoneChecker.class)
        public void check(String event, SystemTodoSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemTodoSimple handle(String event, SystemTodoSimple originForm, SystemTodoForCreation form, String message) throws Exception {
            final AtomicLong todoId = new AtomicLong();
            String targetPage;
            if (StringUtils.isBlank(targetPage = form.getTargetPage())) {
                targetPage = EnjoyUtil.format(ContextUtil.getConfigTrimed("system.todo.target.page.tmpl"),
                        (Object) new ObjectMap().put("targetKey", form.getTargetKey())
                                .put("targetId", form.getTargetId()).asMap());
            }
            Long applyUserId = SessionContext.getTokenUserId();
            String applyUsername = SessionContext.getTokenUsername();
            String applyDisplay = SessionContext.getTokenDisplay();
            if (form.getApplyUser().getId() != null) {
                AbstractUser applier;
                if ((applier = SystemUserService.getInstance().getSimple(form.getApplyUser().getId())) != null) {
                    applyUserId = applier.getId();
                    applyUsername = applier.getUsername();
                    applyDisplay = applier.getDisplay();
                }
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                getFormTable(), new ObjectMap()
                        .put("title", StringUtils.truncate(form.getTitle(), 128))
                        .put("category", form.getCategory())
                        .put("target_key", form.getTargetKey())
                        .put("target_id", form.getTargetId())
                        .put("target_page", targetPage)
                        .put("apply_user_id", applyUserId)
                        .put("apply_user_name", applyUsername)
                        .put("apply_user_display", applyDisplay)
                        .put("created_user_id", SessionContext.getTokenUserId())
                        .put("created_user_name", SessionContext.getTokenUsername())
                        .put("created_user_display", SessionContext.getTokenDisplay())
                        .put("created_at", new Date())
            ), new ResultSetProcessor() {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    todoId.set(r.getLong(1));
                }
            });
            setAssignee(todoId.get(), form.getAssignee());
            return getForm(todoId.get());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SystemTodoSimple, SystemTodoForEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTodoSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTodoSimple originForm, final SystemTodoForEdition form, final String message)
                            throws Exception {
            ObjectMap updated = new ObjectMap()
                .put("=id", form.getId())
                .put("title", form.getTitle())
                .put("category", form.getCategory())
                .put("target_key", form.getTargetKey())
                .put("target_id", form.getTargetId())
                .put("target_page", form.getTargetPage());
            if (form.getApplyUser() != null) {
                AbstractUser applier;
                if ((applier = SystemUserService.getInstance().getSimple(form.getApplyUser().getId())) != null) {
                    updated.put("apply_user_id", applier.getId());
                    updated.put("apply_user_name", applier.getUsername());
                    updated.put("apply_user_display", applier.getDisplay());
                }
            }
            getFormBaseDao()
                    .executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), updated));
            setAssignee(form.getId(), form.getAssignee());
            return null;
        }
    }

    public class EventClose extends AbstractStateAction<SystemTodoSimple, SystemTodoForClose, Void> {
        
        public EventClose() {
            super("关闭", STATES.OPENED.getCode(), STATES.CLOSED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = AuthorityEveryoneChecker.class)
        public void check(String event, SystemTodoSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTodoSimple originForm, final SystemTodoForClose form, final String message)
                            throws Exception {
            String result;
            if ((result = form.getResult()).length() > 128) {
                result = result.substring(0, 128);
            }
            long closedUserId = SessionContext.getUserId();
            String closedUserName = SessionContext.getUsername();
            String closedUserDisplay = SessionContext.getDisplay();
            if (form.getClosedUserId() != null) {
                AbstractUser customUser;
                if ((customUser = SystemUserService.getInstance().getSimple(form.getClosedUserId())) == null) {
                    throw new MessageException(
                            String.format("提供的用户信息(closedUserId = %s)不存在，请再次确认!", form.getClosedUserId()));
                }
                closedUserId = customUser.getId();
                closedUserName = customUser.getUsername();
                closedUserDisplay = customUser.getDisplay();
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                getFormTable(), new ObjectMap()
                        .put("=target_key", originForm.getTargetKey())
                        .put("=target_id", originForm.getTargetId())
                        .put(String.format("=%s", getFormStateField()), STATES.OPENED.getCode())
                        .put(getFormStateField(), STATES.CLOSED.getCode())
                        .put("result", result)
                        .put("closed_user_id", closedUserId)
                        .put("closed_user_name", closedUserName)
                        .put("closed_user_display", closedUserDisplay)
                        .put("closed_at", new Date())
            ));
            return null;
        }
    }
    

    
    public class EventOnCreated extends AbstractStateEnterAction<SystemTodoSimple> {

        public EventOnCreated() {
            super("待办事项创建内部事件", STATES.OPENED.getCode());
        }
        
        @Override
        public Void handle(String event, SystemTodoSimple originForm, AbstractStateForm form, String message) throws Exception {
            SystemTodoDetail closedForm = getForm(form.getId());
            getNotifyService().sendAsync(
                    "system.todo.notify.standard.created", 
                    new ObjectMap()
                        .put("originForm", originForm)
                        .put("changedForm", closedForm)
                        .put("notifyInfo", getNotifyInfo(closedForm))
                        .put("notifyService", getContextFormService()),
                        0);
            return null;
        }
    }
    
    public class EventOnClosed extends AbstractStateLeaveAction<SystemTodoSimple> {

        public EventOnClosed() {
            super ("待办事项关闭内部事件", STATES.OPENED.getCode());
        }
        
        @Override
        public Void handle(String event, SystemTodoSimple originForm, AbstractStateForm form, String message) throws Exception {
            SystemTodoDetail closedForm = getForm(form.getId());
            getNotifyService().sendAsync(
                    "system.todo.notify.standard.closed", 
                    new ObjectMap()
                        .put("originForm", originForm)
                        .put("changedForm", closedForm)
                        .put("notifyInfo", getNotifyInfo(closedForm))
                        .put("notifyService", getContextFormService()),
                        0);
            return null;
        }
    }
    
    public class EventReopen extends AbstractStateAction<SystemTodoSimple, BasicStateForm, Void> {
        
        public EventReopen() {
            super("重新打开", STATES.CLOSED.getCode(), STATES.OPENED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTodoSimple form, String sourceState) {
            
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemTodoSimple> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTodoSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTodoSimple originForm, BasicStateForm form, String message)
                throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            setAssignee(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class)
        , Edit(EventUpdate.class)
        , Close(EventClose.class)
        , Reopen(EventReopen.class)
        , Delete(EventDelete.class)
        , OnClosed(EventOnClosed.class)
        , OnCreated(EventOnCreated.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemTodoSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemTodoSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_common_todo";
    }
    
    @Override
    public String getFormTable() {
        return "system_common_todo";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return TenantSpecialDataSource.getMain();
    }
    
    /**
     * 获取待办事项的详情数据。
     */
    @Override
    public SystemTodoDetail getForm(long id) throws Exception {
        return getForm(SystemTodoDetail.class, id);
    }
    
    private void setAssignee(long formId, List<SystemUserOption> assignees) throws Exception {
        if (assignees == null) {
            return;
        }
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                "system_common_todo_assignee", new ObjectMap().put("=todo_id", formId)));
        for (SystemUserOption user : assignees) {
            if (user == null) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    "system_common_todo_assignee", new ObjectMap()
                            .put("=todo_user", user.getId())
                            .put("todo_id", formId)));
        }
    }
    
    public <T extends SystemTodoSimple> PagedList<T> queryOpenedByAssignee(Class<T> clazz, long assignee, Long page,
            Integer limit) throws Exception {
        return listForm(clazz,
                new SystemTodoDefaultQuery(limit, page).setState(STATES.OPENED.getCode()).setAssignee(assignee));
    }
    
    public long queryOpenedCountByAssignee(long assignee) throws Exception {
        return getListFormTotal(new SystemTodoDefaultQuery().setState(STATES.OPENED.getCode()).setAssignee(assignee));
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemTodoSimple> forms) throws Exception {
        // TODO: fill assignees
    }
    
    @Data
    @Accessors(chain = true)
    public static class TodoNotifyInfo {
        
        private SystemUserSecurityOnly applierNotifyInfo;
        
        private SystemUserSecurityOnly creatorNotifyInfo;
        
        private SystemUserSecurityOnly closerNotifyInfo;
        
        private Collection<SystemUserSecurityOnly> assigneeNotifyInfos;
    }
    
    private TodoNotifyInfo getNotifyInfo(SystemTodoDetail originForm) throws Exception {
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.add(originForm.getApplyUserId());
        allUserIds.add(originForm.getClosedUserId());
        allUserIds.add(originForm.getCreatedUserId());
        List<SystemUserOption> assignees;
        if ((assignees = originForm.getAssignees()) != null) {
            for (SystemUserOption o : assignees) {
                allUserIds.add(o.getId());
            }
        }
        List<SystemUserSecurityOnly> usersSecurity = SystemUserService.getInstance().getUsersSecurity(
                        allUserIds.toArray(new Long[0]));
        Map<Long, SystemUserSecurityOnly> allNotifyInfos = new HashMap<>();
        for (SystemUserSecurityOnly u : usersSecurity) {
            allNotifyInfos.put(u.getId(), u);
            
        }
        TodoNotifyInfo result =  new TodoNotifyInfo()
                .setCloserNotifyInfo(allNotifyInfos.remove(originForm.getClosedUserId()))
                .setCreatorNotifyInfo(allNotifyInfos.remove(originForm.getCreatedUserId()))
                .setApplierNotifyInfo(allNotifyInfos.remove(originForm.getApplyUserId()));

        return result.setAssigneeNotifyInfos(allNotifyInfos.values());
    }
}
