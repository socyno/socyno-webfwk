package com.socyno.webfwk.menu;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adrianwalker.multilinestring.Multiline;
import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscmixutil.ArrayUtils;
import com.socyno.base.bscmixutil.CommonUtil;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscmodel.PagedList;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.base.bscsqlutil.AbstractDao.ResultSetProcessor;
import com.socyno.base.bscsqlutil.SqlQueryUtil;
import com.socyno.stateform.abs.AbstractStateAction;
import com.socyno.stateform.abs.AbstractStateCreateAction;
import com.socyno.stateform.abs.AbstractStateDeleteAction;
import com.socyno.stateform.abs.AbstractStateFormServiceWithBaseDao;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.webbsc.authority.AuthorityEntity;
import com.socyno.stateform.field.FieldSystemAuths;
import com.socyno.stateform.util.StateFormEventClassEnum;
import com.socyno.stateform.util.StateFormNamedQuery;
import com.socyno.stateform.util.StateFormQueryBaseEnum;
import com.socyno.stateform.util.StateFormStateBaseEnum;
import com.socyno.webbsc.ctxutil.ContextUtil;
import com.socyno.webbsc.service.jdbc.FeatureBasicService;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class SystemMenuService extends AbstractStateFormServiceWithBaseDao<SystemMenuSimple> {
    
    @Getter
    private static final SystemMenuService instance = new SystemMenuService();
    
    private SystemMenuService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_menu";
    }
    
    @Override
    public String getFormTable() {
        return "system_menu";
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
        DEFAULT(new StateFormNamedQuery<SystemMenuDefaultRow>("通用查询", SystemMenuDefaultRow.class,
                SystemMenuDefaultQuery.class));
        private final StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
        
    }
    
    @Getter
    public enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class)
        , Edit(EventEdit.class)
        , Delete(EventDelete.class)
        , AuthsAdd(EventAuthsAdd.class)
        , AuthsDel(EventAuthsDel.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemMenuSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemMenuSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemMenuSimple, SystemMenuForCreation, SystemMenuSimple> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemMenuSimple handle(String event, SystemMenuSimple originForm, SystemMenuForCreation form, String message)
                throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareInsertQuery(getFormTable(),
                            new ObjectMap()
                                    .put("path", form.getPath())
                                    .put("name", form.getName())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("order",form.getOrder())
                                    .put("dir_id", form.getMenuDir().getId())
                ), new ResultSetProcessor() {
                    @Override
                    public void process(ResultSet r, Connection c) throws Exception {
                        r.next();
                        id.set(r.getLong(1));
                    }
                });
            if ((form.getAuths()) != null) {
                menuAuthsAdd(id.get(), form.getAuths());
            }
            return getForm(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemMenuSimple, SystemMenuForEdition, Void> {
        
        public EventEdit() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuSimple originForm, final SystemMenuForEdition form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                                    .put("=id", form.getId())
                                    .put("name", form.getName())
                                    .put("path", form.getPath())
                                    .put("icon", StringUtils.trimToEmpty(form.getIcon()))
                                    .put("order", form.getOrder())
                                    .put("dir_id", form.getMenuDir().getId())
                                ));
            return null;
        }
    }

    public class EventAuthsAdd extends AbstractStateAction<SystemMenuSimple, SystemMenuForAuthsAdd, Void> {
        
        public EventAuthsAdd() {
            super("添加授权", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuSimple originForm, final SystemMenuForAuthsAdd form,
                final String message) throws Exception {
            menuAuthsAdd(originForm.getId(), form.getAuthsAdded());
            return null;
        }
    }

    public class EventAuthsDel extends AbstractStateAction<SystemMenuSimple, SystemMenuForAuthsDel, Void> {
        
        public EventAuthsDel() {
            super("移除授权", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuSimple originForm, final SystemMenuForAuthsDel form,
                final String message) throws Exception {
            menuAuthsDel(originForm.getId(), form.getAuthsRemoved());
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemMenuSimple> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemMenuSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemMenuSimple originForm, final BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            menuAuthsClear(originForm.getId());
            return null;
        }
    }
    
    /**
     * 重写获取表单详情的方法
     * 
     */
    @Override
    public SystemMenuDetail getForm(long formId) throws Exception {
        return getForm(SystemMenuDetail.class, formId);
    }
    
    /**
     * 重写获取表单详情的方法
     * 
     */
    @Override
    public <T extends SystemMenuSimple> T getForm(Class<T> clazz, long formId) throws Exception {
        List<T> list;
        PagedList<T> paged;
        if ((paged = listForm(clazz, new SystemMenuDefaultQuery(1, 1L).setMenuIdsIn(formId + ""))) == null
                || (list = paged.getList()) == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
    
    /**
     * 添加菜单授权信息
     */
    private void menuAuthsAdd(long formId, List<AuthorityEntity> auths) throws Exception {
        if (auths == null || auths.isEmpty()) {
            return;
        }
        for (AuthorityEntity auth : auths) {
            if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_menu_auth",
                    new ObjectMap().put("menu_id", formId).put("=auth_key", auth.getOptionValue())));
        }
    }
    
    /**
     * 清除菜单授权信息
     */
    private void menuAuthsClear(long formId) throws Exception {
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("system_menu_auth",
                new ObjectMap().put("=menu_id", formId)));
    }
    
    /**
     * 添加菜单授权信息
     */
    private void menuAuthsDel(long formId, List<AuthorityEntity> auths) throws Exception {
        if (auths == null || auths.isEmpty()) {
            return;
        }
        for (AuthorityEntity auth : auths) {
            if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery("system_menu_auth",
                    new ObjectMap().put("=menu_id", formId).put("=auth_key", auth.getOptionValue())));
        }
    }
    
    /**
     * SELECT
     *   m.*
     * FROM 
     *   system_menu_auth m
     * WHERE
     *   m.menu_id IN (%s)
     */
    @Multiline
    private final static String SQL_QUERY_MENU_AUTH_KEYS = "X";
    
    @Data
    public static class SystemMenuAuthKey {
        
        private long menuId;
        
        private String authKey;
        
    }
    
    /**
     *  批量检索菜单的授权，返回对象以菜单编号为键，授权列表为值
     */
    private Map<Long, List<AuthorityEntity>> queryAuthsByMenuId(Long... menuIds) throws Exception {
        if ((menuIds = ConvertUtil.asNonNullUniqueLongArray((Object[]) menuIds)) == null || menuIds.length <= 0) {
            return Collections.emptyMap();
        }
        List<SystemMenuAuthKey> allAuthKeys = getFormBaseDao().queryAsList(SystemMenuAuthKey.class,
                String.format(SQL_QUERY_MENU_AUTH_KEYS, StringUtils.join("?", menuIds.length, ",")), menuIds);
        if (allAuthKeys.size() <= 0) {
            return Collections.emptyMap();
        }
        
        Set<Long> singleAuthMenuIds;
        Set<String> flattedAuthKeys = new HashSet<>();
        Map<String, Set<Long>> mappedAuthMenuIds = new HashMap<>();
        for (SystemMenuAuthKey ma : allAuthKeys) {
            flattedAuthKeys.add(ma.getAuthKey());
            if ((singleAuthMenuIds = mappedAuthMenuIds.get(ma.getAuthKey())) == null) {
                mappedAuthMenuIds.put(ma.getAuthKey(), singleAuthMenuIds = new HashSet<>());
            }
            singleAuthMenuIds.add(ma.getMenuId());
        }
        List<AuthorityEntity> allSystemAuths = FieldSystemAuths.getInstance()
                .queryDynamicValues(flattedAuthKeys.toArray());
        if (allSystemAuths == null || allSystemAuths.size() <= 0) {
            return Collections.emptyMap();
        }
        List<AuthorityEntity> singleMenuAuths;
        Map<Long, List<AuthorityEntity>> mappedMenuAuths = new HashMap<>();
        for (AuthorityEntity option : allSystemAuths) {
            if ((singleAuthMenuIds = mappedAuthMenuIds.get(option.getAuth())) == null) {
                continue;
            }
            for (Long menuId : singleAuthMenuIds) {
                if ((singleMenuAuths = mappedMenuAuths.get(menuId)) == null) {
                    mappedMenuAuths.put(menuId, singleMenuAuths = new ArrayList<>());
                }
                singleMenuAuths.add(option);
            }
        }
        return mappedMenuAuths;
    }
    
    /**
     * SELECT DISTINCT
     *     m.*,
     *     p.`id`    AS `pane_id`,
     *     p.`icon`  AS `pane_icon`,
     *     p.`path`  AS `pane_path`,
     *     p.`name`  AS `pane_name`,
     *     p.`order` AS `pane_order`,
     *     d.`icon`  AS `dir_icon`,
     *     d.`path`  AS `dir_path`,
     *     d.`name`  AS `dir_name`,
     *     d.`order` AS `dir_order`,
     *     a.`auth_key`
     * FROM
     *     system_menu m,
     *     system_menu_dir d,
     *     system_menu_pane p,
     *     system_menu_auth a
     * WHERE
     *     m.dir_id = d.id
     * AND 
     *     d.pane_id = p.id
     * AND 
     *     a.menu_id = m.id
     * AND 
     *     m.state_form_status = 'enabled'
     * AND 
     *     d.state_form_status = 'enabled'
     * AND 
     *     p.state_form_status = 'enabled'
     * ORDER BY
     *     p.`order` ASC,
     *     d.`order` ASC,
     *     m.`order` ASC
     */
    @Multiline
    private static final String SQL_QUERY_MENU_TREE = "X";
    
    @Getter
    @Setter
    public static class SystemMenuAuthForm extends SystemMenuDefaultRow {
        
        @Attributes(title = "目录编号")
        private Long dirId;
        
        @Attributes(title = "目录图标")
        private String dirIcon;
        
        @Attributes(title = "目录路径")
        private String dirPath;
        
        @Attributes(title = "面板排序")
        private Integer dirOrder;
        
        @Attributes(title = "面板编号")
        private Long paneId;
        
        @Attributes(title = "面板图标")
        private String paneIcon;
        
        @Attributes(title = "面板路径")
        private String panePath;
        
        @Attributes(title = "面板排序")
        private Integer paneOrder;
        
        @Attributes(title = "操作接口")
        private String authKey;
    }
    
    private final static Pattern MENU_OPEN_TYPE_PARSER =  Pattern.compile("(^.+)#\\((.+)\\)$");
    private static void setOpenType(SystemMenuTree tree) {
        List<SystemMenuTree> children;
        if ((children = tree.getChildren()) == null || children.size() <= 0) {
            String path;
            Matcher matched;
            if (StringUtils.isBlank(path = tree.getPath()) || (matched = MENU_OPEN_TYPE_PARSER.matcher(path)) == null
                    || !matched.find()) {
                return;
            }
            tree.setPath(matched.group(1).trim());
            tree.setOpenType(matched.group(2).trim());
            return;
        }
        for (SystemMenuTree child : children) {
            setOpenType(child);
        }
    }
    
    private static void sortChildren(SystemMenuTree tree) {
        List<SystemMenuTree> children;
        if ((children = tree.getChildren()) == null || children.size() <= 0) {
            return;
        }
        tree.getChildren().sort(new Comparator<SystemMenuTree>() {
            
            @Override
            public int compare(SystemMenuTree left, SystemMenuTree right) {
                return CommonUtil.ifNull(left.getOrder(), 0) - CommonUtil.ifNull(right.getOrder(), 0);
            }
            
        });
    }
    
    public SystemMenuTree getMyMenuTree() throws Exception {
        if (!SessionContext.hasUserSession()) {
            return null;
        }
        Map<Long, SystemMenuTree> dirMap = new HashMap<>();
        Map<Long, SystemMenuTree> menuMap = new HashMap<>();
        Map<Long, SystemMenuTree> paneMap = new HashMap<>();
        SystemMenuTree menuTree = new SystemMenuTree().setChildren(new ArrayList<>());
        List<SystemMenuAuthForm> result = getFormBaseDao().queryAsList(
                SystemMenuAuthForm.class, SQL_QUERY_MENU_TREE);
        String[] myAuths = SessionContext.isAdmin()
                ? FeatureBasicService.getInstance().getTenantAllAuths(SessionContext.getTenant())
                : getPermissionService().getMyAuths();
        /* 无授权，即无菜单 */
        if (myAuths == null || myAuths.length <= 0) {
            return null;
        }
        for (SystemMenuAuthForm item : result) {
            long menuId = item.getId();
            long dirId = item.getDirId();
            long paneId = item.getPaneId();
            SystemMenuTree paneTree;
            if ((paneTree = paneMap.get(paneId)) == null) {
                paneMap.put(paneId,
                        paneTree = new SystemMenuTree().setName(item.getPaneName()).setPath(item.getPanePath())
                                .setId(dirId).setIcon(item.getPaneIcon()).setOrder(item.getPaneOrder())
                                .setChildren(new ArrayList<>()));
                menuTree.getChildren().add(paneTree);
            }
            SystemMenuTree dirTree;
            if ((dirTree = dirMap.get(dirId)) == null) {
                dirMap.put(dirId,
                        dirTree = new SystemMenuTree().setName(item.getDirName()).setId(paneId).setParentId(paneId)
                                .setPath(item.getDirPath()).setIcon(item.getDirIcon()).setOrder(item.getDirOrder())
                                .setChildren(new ArrayList<>()));
                paneTree.getChildren().add(dirTree);
            }
            SystemMenuTree menuItem;
            if ((menuItem = menuMap.get(menuId)) == null) {
                menuMap.put(menuId,
                        menuItem = new SystemMenuTree().setName(item.getName()).setPath(item.getPath()).setId(menuId)
                                .setParentId(dirId).setIcon(item.getIcon()).setOrder(item.getOrder())
                                .setAuthKeys(new HashSet<>()));
                dirTree.getChildren().add(menuItem);
            }
            if (StringUtils.isNotBlank(item.getAuthKey())) {
                menuItem.getAuthKeys().add(item.getAuthKey());
            }
        }
        Set<String> menuAuths;
        Long[] menuIds = menuMap.keySet().toArray(new Long[0]);
        for (int m = 0; m < menuIds.length; m++) {
            if ((menuAuths = menuMap.get(menuIds[m]).getAuthKeys()) == null || menuAuths.size() <= 0
                    || !ArrayUtils.containsAll(myAuths, menuAuths.toArray())) {
                menuMap.remove(menuIds[m]);
            }
        }
        int maxMenuChildren = menuTree.getChildren().size() - 1;
        for (int i = maxMenuChildren; i >= 0; i--) {
            SystemMenuTree paneTree = menuTree.getChildren().get(i);
            int maxPaneChildren = paneTree.getChildren().size() - 1;
            for (int j = maxPaneChildren; j >= 0; j--) {
                SystemMenuTree dirTree = paneTree.getChildren().get(j);
                int maxDirChildren = dirTree.getChildren().size() - 1;
                for (int k = maxDirChildren; k >= 0; k--) {
                    SystemMenuTree menuItem = dirTree.getChildren().get(k);
                    if (!menuMap.containsKey(menuItem.getId())) {
                        dirTree.getChildren().remove(k);
                    }
                }
                if (dirTree.getChildren().size() < 1) {
                    paneTree.getChildren().remove(j);
                }
                /* 当菜单目录上配置了地址时，移出其下的所有子菜单,即意味着为两级结构，任意子菜单的授权将被视为该目录的访问授权 */
                if (StringUtils.isNotBlank(dirTree.getPath()) && dirTree.getPath().trim().length() > 3) {
                    dirTree.getChildren().clear();
                }
                setOpenType(dirTree);
                sortChildren(dirTree);
            }
            if (paneTree.getChildren().size() < 1) {
                menuTree.getChildren().remove(i);
            }
            sortChildren(paneTree);
        }
        sortChildren(menuTree);
        return menuTree;
    }

    @Override
    protected void fillExtraFormFields(Collection<? extends SystemMenuSimple> forms) throws Exception {
        if (forms == null || forms.isEmpty()) {
            return;
        }

        Set<Long> authMenuIds = new HashSet<>();
        Set<Long> allMenuDirIds = new HashSet<>();
        List<SystemMenuWithAuths> withAuths = new ArrayList<>();
        List<SystemMenuWithDirEntity> withDirEntities = new ArrayList<>();
        for (SystemMenuSimple simple : forms) {
            if (simple == null) {
                continue;
            }
            if (simple instanceof SystemMenuWithAuths) {
                authMenuIds.add(simple.getId());
                withAuths.add((SystemMenuWithAuths)simple);
                
            }
            if (simple instanceof SystemMenuWithDirEntity) {
                allMenuDirIds.add(simple.getDirId());
                withDirEntities.add((SystemMenuWithDirEntity)simple);
            }
        }
        
        if (withDirEntities.size() > 0) {
            List<SystemMenuDirOption> options;
            if ((options = FieldSystemMenuDir.getInstance().queryDynamicValues(allMenuDirIds.toArray())) != null 
                    && !options.isEmpty()) {
                Map<Long, SystemMenuDirOption> mappedOptions = new HashMap<>();
                for (SystemMenuDirOption o: options) {
                    mappedOptions.put(o.getId(), o);
                }
                for (SystemMenuWithDirEntity e : withDirEntities) {
                    e.setMenuDir(mappedOptions.get(((SystemMenuSimple)e).getDirId()));
                }
            }
        }
        
        if (authMenuIds.size() > 0) {
            Map<Long, List<AuthorityEntity>> mappedMenuAuths;
            if ((mappedMenuAuths = queryAuthsByMenuId(authMenuIds.toArray(new Long[0]))) != null) {
                for (SystemMenuWithAuths ma : withAuths) {
                    ma.setAuths(mappedMenuAuths.get(((SystemMenuSimple)ma).getId()));
                }
            }
        }
    }
}




