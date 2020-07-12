package com.socyno.webfwk.tenant;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscexec.NamingFormatInvalidException;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.base.bscsqlutil.AbstractDao.ResultSetProcessor;
import com.socyno.base.bscsqlutil.SqlQueryUtil;
import com.socyno.stateform.abs.AbstractStateAction;
import com.socyno.stateform.abs.AbstractStateCreateAction;
import com.socyno.stateform.abs.AbstractStateFormServiceWithBaseDao;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.webbsc.model.SystemTenantDbInfoWithId;
import com.socyno.stateform.util.StateFormEventClassEnum;
import com.socyno.stateform.util.StateFormNamedQuery;
import com.socyno.stateform.util.StateFormQueryBaseEnum;
import com.socyno.stateform.util.StateFormStateBaseEnum;
import com.socyno.webbsc.service.jdbc.TenantBasicService;
import com.socyno.webfwk.feature.FieldSystemFeatureAll;
import com.socyno.webfwk.feature.SystemFeatureOption;

import lombok.Data;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;

public class SystemTenantService extends AbstractStateFormServiceWithBaseDao<SystemTenantSimple> {
    
    @Getter
    private static final SystemTenantService instance = new SystemTenantService();
    
    private SystemTenantService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_tenant";
    }
    
    @Override
    public String getFormTable() {
        return "system_tenant";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return TenantBasicService.getInstance().getDao();
    }
    
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        DISABLED ("disabled", "禁用"),
        ENABLED  ("enabled",  "有效")
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
        DEFAULT(new StateFormNamedQuery<SystemTenantSimple>("通用查询", 
                SystemTenantSimple.class, SystemTenantDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
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
         * 添加功能
         */
        FeaturesAdd(EventFeaturesAdd.class),
        
        /**
         * 移除功能
         */
        FeaturesDel(EventFeaturesDel.class),
        
        /**
         * 添加功能
         */
        DatabaseAdd(EventDatabasesAdd.class),
        
        /**
         * 移除功能
         */
        DatabaseDel(EventDatabasesDel.class),
        
        /**
         * 禁用
         */
        Disable(EventDisable.class),
        
        /**
         * 恢复
         */
        Enable(EventEnable.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemTenantSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemTenantSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemTenantSimple, SystemTenantForCreation, SystemTenantSimple> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode()); 
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemTenantSimple handle(String event, SystemTenantSimple originForm, SystemTenantForCreation form, String message) throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            checkSystemTenantChange(form);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    getFormTable(), new ObjectMap()
                        .put("code",            form.getCode())
                        .put("name",            form.getName())
            ), new ResultSetProcessor () {
                @Override
                public void process(ResultSet r, Connection c) throws Exception {
                    r.next();
                    id.set(r.getLong(1));
                }
            });
            TenantBasicService.getInstance().addTenantDatabases(id.get(), form.getDatabasesAdded());
            return getForm(id.get());
        }
    }
    
    public class EventEdit extends AbstractStateAction<SystemTenantSimple, SystemTenantForEdition, Void> {

        public EventEdit() {
            super("编辑", getStateCodesEx(), ""); 
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTenantSimple originForm, final SystemTenantForEdition form, final String message) throws Exception {
            checkSystemTenantChange(form);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id",    form.getId())
                        .put("name",        form.getName())
            ));
            return null;
        }
    }
    
    public class EventEnable extends AbstractStateAction<SystemTenantSimple, BasicStateForm, Void> {
        
        public EventEnable() {
            super("启用", STATES.DISABLED.getCode(), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
    }
    
    public class EventFeaturesAdd extends AbstractStateAction<SystemTenantSimple, SystemTenantForFeaturesAdd, Void> {
        
        public EventFeaturesAdd() {
            super("授予功能", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTenantSimple originForm, final SystemTenantForFeaturesAdd form, final String message) throws Exception {
            List<SystemFeatureOption> featuresAdded;
            if ((featuresAdded = form.getFeaturesAdded()) == null || featuresAdded.isEmpty()) {
                return null;
            }
            for (SystemFeatureOption feature : featuresAdded) {
                if (feature == null) {
                    continue;
                }
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_tenant_feature",
                    new ObjectMap().put("tenant_id", originForm.getId())
                            .put("=feature_id", feature.getOptionValue())
                ));
            }
            return null;
        }
    }
    
    public class EventDatabasesAdd extends AbstractStateAction<SystemTenantSimple, SystemTenantForDatabasesAdd, Void> {
        
        public EventDatabasesAdd() {
            super("添加数据库", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTenantSimple originForm, final SystemTenantForDatabasesAdd form, final String message) throws Exception {
            TenantBasicService.getInstance().addTenantDatabases(originForm.getId(), form.getDatabasesAdded());
            return null;
        }
    }
    
    public class EventDatabasesDel extends AbstractStateAction<SystemTenantSimple, SystemTenantForDatabasesDel, Void> {
        
        public EventDatabasesDel() {
            super("移除数据库", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTenantSimple originForm, final SystemTenantForDatabasesDel form, final String message) throws Exception {
            TenantBasicService.getInstance().delTenantDatabases(originForm.getId(), form.getDatabasesRemoved());
            return null;
        }
    }
    
    public class EventFeaturesDel extends AbstractStateAction<SystemTenantSimple, SystemTenantForFeaturesDel, Void> {
        
        public EventFeaturesDel() {
            super("移除功能", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemTenantSimple originForm, final SystemTenantForFeaturesDel form, final String message) throws Exception {
            List<SystemFeatureOption> featuresRemoved;
            if ((featuresRemoved = form.getFeaturesRemoved()) == null || featuresRemoved.isEmpty()) {
                return null;
            }
            for (SystemFeatureOption feature : featuresRemoved) {
                if (feature == null) {
                    continue;
                }
                getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                        "system_tenant_feature", new ObjectMap()
                            .put("=tenant_id", originForm.getId())
                            .put("=feature_id", feature.getOptionValue())
                ));
            }
            return null;
        }
    }
    
    public class EventDisable extends AbstractStateAction<SystemTenantSimple, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemTenantSimple form, String sourceState) {
            
        }
    }
    
    private void checkSystemTenantChange(AbstractSystemTenant changed) throws Exception {
        if (StringUtils.isBlank(changed.getCode()) || changed.getCode().matches("^\\d+$")
                            || !changed.getCode().matches("^[a-z0-9][a-z0-9\\-\\.]+[a-z0-9]$")) {
            throw new NamingFormatInvalidException("租户代码命名不规范 ：只能包含数字、小写字母或短横线(-), 且不能为纯数字，不能以短横线开头或结尾");
        }
        StringBuffer sql = new StringBuffer(
                String.format("SELECT COUNT(1) FROM %s WHERE code = ?", getFormTable()));
        if (changed.getId() != null) {
            sql.append("AND id != ").append(changed.getId());
        }
        if (getFormBaseDao().queryAsObject(Long.class, sql.toString(),
                new Object[] { changed.getCode() }) > 0) {
            throw new MessageException(String.format("租户代码(%s)已被占用，请重新命名！", changed.getCode()));
        }
    }
    
    /**
     * 检索租户详情
     */
    public SystemTenantDetail getForm(long id) throws Exception {
        return getForm(SystemTenantDetail.class, id);
    }

    /**
     *     SELECT
     *         f.tenant_id,
     *         f.feature_id
     *     FROM
     *         system_tenant_feature f
     *     WHERE
     *         f.tenant_id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES = "X";
    
    @Data
    public static class TenantFeatureLink {
        
        private long tenantId;
        
        private long featureId;
        
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemTenantSimple> forms) throws Exception {
        if (forms == null || forms.isEmpty()) {
            return;
        }
        List<SystemTenantWithDbInfos> sameDbTenants;
        List<SystemTenantWithFeatures> sameFeTenants;
        Map<Long, List<SystemTenantWithDbInfos>> withDbInfos = new HashMap<>();
        Map<Long, List<SystemTenantWithFeatures>> withFeatures = new HashMap<>();
        for (SystemTenantSimple form: forms) {
            if (form == null || form.getId() == null) {
                continue;
            }
            if (form instanceof SystemTenantWithFeatures) {
                if ((sameFeTenants = withFeatures.get(form.getId())) == null) {
                    withFeatures.put(form.getId(), sameFeTenants = new ArrayList<>());
                }
                sameFeTenants.add((SystemTenantWithFeatures)form);
            }
            if (form instanceof SystemTenantWithDbInfos) {
                if ((sameDbTenants = withDbInfos.get(form.getId())) == null) {
                    withDbInfos.put(form.getId(), sameDbTenants = new ArrayList<>());
                }
                sameDbTenants.add((SystemTenantWithDbInfos)form);
            }
        }
        if (withDbInfos.size() > 0) {
            List<SystemTenantDbInfoWithId> allTenantDbInfos = TenantBasicService.getInstance()
                    .getTenantDatabases(withDbInfos.keySet().toArray(new Long[0]));
            if (allTenantDbInfos != null && !allTenantDbInfos.isEmpty()) {
                List<SystemTenantDbInfoWithId> tenantDatabases;
                for (SystemTenantDbInfoWithId option : allTenantDbInfos) {
                    if ((sameDbTenants = withDbInfos.get(option.getTenantId())) == null) {
                        continue;
                    }
                    for (SystemTenantWithDbInfos tenant : sameDbTenants) {
                        if ((tenantDatabases = tenant.getDatabases()) == null) {
                            tenant.setDatabases(tenantDatabases = new ArrayList<>());
                        }
                        tenantDatabases.add(option);
                    }
                }
            }
        }
        if (withFeatures.size() > 0) {
            List<TenantFeatureLink> featureLinks = getFormBaseDao().queryAsList(TenantFeatureLink.class,
                    String.format(SQL_QUERY_TENANT_FEATURES, StringUtils.join("?", withFeatures.size(), ",")),
                    withFeatures.keySet().toArray());
            Set<Long> oneTenantIds;
            Set<Long> allFeaturesIds = new HashSet<>();
            Map<Long, Set<Long>> mappedTenantIds = new HashMap<>();
            for (TenantFeatureLink link : featureLinks) {
                if ((oneTenantIds = mappedTenantIds.get(link.getFeatureId())) == null) {
                    mappedTenantIds.put(link.getFeatureId(), oneTenantIds = new HashSet<>());
                }
                oneTenantIds.add(link.getTenantId());
                allFeaturesIds.add(link.getFeatureId());
            }
            if (allFeaturesIds.size() > 0) {
                List<SystemFeatureOption> tenantFeatures;
                List<SystemFeatureOption> allFeatureOptions;
                if ((allFeatureOptions = FieldSystemFeatureAll.getInstance().queryDynamicValues(allFeaturesIds.toArray())) != null) {
                    for (SystemFeatureOption option : allFeatureOptions) {
                        for (long tenantId : mappedTenantIds.get(option.getId())) {
                            if ((sameFeTenants = withFeatures.get(tenantId)) == null) {
                               continue; 
                            }
                            for (SystemTenantWithFeatures tenant : sameFeTenants) {
                                if ((tenantFeatures = tenant.getFeatures()) == null) {
                                    tenant.setFeatures(tenantFeatures = new ArrayList<>());
                                }
                                tenantFeatures.add(option);
                            }
                        }
                    }
                }
            }
        }
    }
}
