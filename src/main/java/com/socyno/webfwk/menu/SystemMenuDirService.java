package com.socyno.webfwk.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscmodel.PagedList;
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

public class SystemMenuDirService extends AbstractStateFormServiceWithBaseDao<SystemMenuDirSimple> {
    
    @Getter
    private static final SystemMenuDirService instance = new SystemMenuDirService();
    
    private SystemMenuDirService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_menu_dir";
    }
    
    @Override
    protected String getFormTable() {
        return "system_menu_dir";
    }
    
    @Override
    public AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
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
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemMenuDirDefaultRow>("通用查询", 
                SystemMenuDirDefaultRow.class, SystemMenuDirDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
        public static List<StateFormNamedQuery<?>> getQueries() {
            List<StateFormNamedQuery<?>> queries = new ArrayList<>();
            for (QUERIES item : QUERIES.values()) {
                queries.add(item.getNamedQuery());
            }
            return queries;
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class)
        , Edit(EventEdit.class)
        , Delete(EventDelete.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemMenuDirSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemMenuDirSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemMenuDirSimple, SystemMenuDirForCreation, SystemMenuDirSimple> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuDirSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemMenuDirSimple handle(String event, SystemMenuDirSimple originForm, SystemMenuDirForCreation form, String message)
                throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(),
                            new ObjectMap().put("path", form.getPath())
                                    .put("name", form.getName())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("pane_id", form.getMenuPane().getId())
                                    .put("order", form.getOrder())),
                    new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet r, Connection c) throws Exception {
                            r.next();
                            id.set(r.getLong(1));
                        }
                    });
            return getForm(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemMenuDirSimple, SystemMenuDirForEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuDirSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuDirSimple originForm, final SystemMenuDirForEdition form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                    new ObjectMap().put("=id", form.getId())
                            .put("name", form.getName())
                            .put("path", form.getPath())
                            .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                            .put("order", form.getOrder())
                            .put("pane_id", form.getMenuPane().getId())));
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemMenuDirSimple> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuDirSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuDirSimple originForm, final BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            return null;
        }
        
    }
    
    /**
     * 重写获取表单详情的方法
     * 
     */
    @Override
    public SystemMenuDirDetail getForm(long formId) throws Exception {
        return getForm(SystemMenuDirDetail.class, formId);
    }
    
    /**
     * 重写获取表单详情的方法
     * 
     */
    @Override
    public <T extends SystemMenuDirSimple> T getForm(Class<T> clazz, long formId) throws Exception {
        List<T> list;
        PagedList<T> paged;
        if ((paged = listForm(clazz, new SystemMenuDirDefaultQuery(1, 1L).setMenuDirIdsIn(formId + ""))) == null
                || (list = paged.getList()) == null || list.size() != 1) {
            return null;
        }
        return list.get(0);
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemMenuDirSimple> forms) throws Exception {
        if (forms == null || forms.isEmpty()) {
            return;
        }
        Set<Long> allMenuPaneIds = new HashSet<>();
        List<SystemMenuDirWithPaneEntity> withPaneEntities = new ArrayList<>();
        for (SystemMenuDirSimple dir : forms) {
            if (dir == null) {
                continue;
            }
            
            if (dir.getPaneId() != null && (dir instanceof SystemMenuDirWithPaneEntity)) {
                allMenuPaneIds.add(dir.getPaneId());
                withPaneEntities.add((SystemMenuDirWithPaneEntity)dir);
            }
        }
        if (withPaneEntities.size() > 0) {
            List<SystemMenuPaneOption> options;
            if ((options = FieldSystemMenuPane.getInstance().queryDynamicValues(allMenuPaneIds.toArray())) != null 
                    && !options.isEmpty()) {
                Map<Long, SystemMenuPaneOption> mappedOptions = new HashMap<>();
                for (SystemMenuPaneOption o: options) {
                    mappedOptions.put(o.getId(), o);
                }
                for (SystemMenuDirWithPaneEntity e : withPaneEntities) {
                    e.setMenuPane(mappedOptions.get(((SystemMenuDirSimple)e).getPaneId()));
                }
            }
        }
    }
}
