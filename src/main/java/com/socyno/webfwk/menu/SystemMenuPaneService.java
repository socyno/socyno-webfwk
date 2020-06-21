package com.socyno.webfwk.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.base.bscsqlutil.AbstractDao.ResultSetProcessor;
import com.socyno.base.bscsqlutil.SqlQueryUtil;
import com.socyno.stateform.abs.AbstractStateAction;
import com.socyno.stateform.abs.AbstractStateCreateAction;
import com.socyno.stateform.abs.AbstractStateDeleteAction;
import com.socyno.stateform.abs.AbstractStateFormServiceWithBaseDao;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.stateform.authority.Authority;
import com.socyno.stateform.authority.AuthorityScopeType;
import com.socyno.stateform.util.StateFormEventClassEnum;
import com.socyno.stateform.util.StateFormNamedQuery;
import com.socyno.stateform.util.StateFormQueryBaseEnum;
import com.socyno.stateform.util.StateFormStateBaseEnum;
import com.socyno.webbsc.ctxutil.ContextUtil;

import lombok.Getter;

public class SystemMenuPaneService extends AbstractStateFormServiceWithBaseDao<SystemMenuPaneSimple> {
    
    @Getter
    private static final SystemMenuPaneService instance = new SystemMenuPaneService();
    
    private SystemMenuPaneService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_menu_pane";
    }
    
    @Override
    protected String getFormTable() {
        return "system_menu_pane";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Getter
    public enum STATES implements StateFormStateBaseEnum {
          ENABLED  ("enabled", "有效")
        , DISABLED ("disabled", "禁用")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemMenuPaneDefaultRow>("通用查询", 
                SystemMenuPaneDefaultRow.class, SystemMenuPaneDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class)
        , Edit(EventEdit.class)
        , Delete(EventDelete.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemMenuPaneSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemMenuPaneSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemMenuPaneSimple> forms) throws Exception {
        
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemMenuPaneSimple, SystemMenuPaneForCreation, SystemMenuPaneSimple> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuPaneSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemMenuPaneSimple handle(String event, SystemMenuPaneSimple originForm, SystemMenuPaneForCreation form, String message)
                throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(),
                            new ObjectMap()
                                    .put("path", form.getPath())
                                    .put("name", form.getName())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("order", form.getOrder())
                ), new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet r, Connection c) throws Exception {
                        r.next();
                        id.set(r.getLong(1));
                    }
                });
            return getForm(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemMenuPaneSimple, SystemMenuPaneForEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuPaneSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuPaneSimple originForm, final SystemMenuPaneForEdition form,
                final String message) throws Exception {
            getFormBaseDao()
                    .executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                            new ObjectMap()
                                    .put("=id", form.getId())
                                    .put("name", form.getName())
                                    .put("path", form.getPath())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("order", form.getOrder())
                                ));
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemMenuPaneSimple> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuPaneSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuPaneSimple originForm, final BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(getFormTable(),
                    new ObjectMap().put("=id", originForm.getId())));
            return null;
        }
    }
}