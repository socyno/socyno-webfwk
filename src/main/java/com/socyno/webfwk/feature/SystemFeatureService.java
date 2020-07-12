package com.socyno.webfwk.feature;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.v1.FieldOption;
import com.socyno.base.bscmixutil.CommonUtil;
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

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

public class SystemFeatureService extends AbstractStateFormServiceWithBaseDao<SystemFeatureSimple> {
    
    @Getter
    private static final SystemFeatureService instance = new SystemFeatureService();
    
    private SystemFeatureService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_feature";
    }
    
    @Override
    protected String getFormTable() {
        return "system_feature";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return ContextUtil.getBaseDataSource();
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        ENABLED("enabled", "有效")
        , DISABLE("disabled", "禁用")
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
        DEFAULT(new StateFormNamedQuery<SystemFeatureDefaultRow>("通用查询", 
                SystemFeatureDefaultRow.class, SystemFeatureDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        Create(EventCreate.class)
        , Update(EventUpdate.class)
        , Delete(EventDelete.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemFeatureSimple, ?, ?>> eventClass;
        
        EVENTS(Class<? extends AbstractStateAction<SystemFeatureSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    /**
     * 系统功能的添加事件
     *
     */
    public class EventCreate extends AbstractStateCreateAction<SystemFeatureSimple, SystemFeatureForCreation, SystemFeatureSimple> {
        
        public EventCreate() {
            super("创建", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemFeatureSimple handle(String event, SystemFeatureSimple originForm, SystemFeatureForCreation form, String message)
                throws Exception {
            final AtomicLong id = new AtomicLong(-1);
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(),
                    new ObjectMap().put("name", form.getName())
                            .put("description", CommonUtil.ifNull(form.getDescription(), ""))
                            .put("created_by", SessionContext.getDisplay())),
                    new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet r, Connection c) throws Exception {
                            r.next();
                            id.set(r.getLong(1));
                        }
                    });
            List<AuthorityEntity> auths;
            if ((auths = form.getAuths()) != null) {
                setFeatureAuths(id.get(), auths);
            }
            return getForm(id.get());
        }
    }
    
    /**
     * 系统功能的编辑事件
     *
     */
    public class EventUpdate extends AbstractStateAction<SystemFeatureSimple, SystemFeatureForEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemFeatureSimple originForm, final SystemFeatureForEdition form,
                final String message) throws Exception {
            List<AuthorityEntity> auths;
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(),
                    new ObjectMap().put("=id", form.getId()).put("name", form.getName()).put("description",
                            CommonUtil.ifNull(form.getDescription(), ""))));
            
            if ((auths = form.getAuths()) != null) {
                setFeatureAuths(form.getId(), auths);
            }
            return null;
        }
    }
    
    /**
     * 系统功能的删除事件
     *
     */
    public class EventDelete extends AbstractStateDeleteAction<SystemFeatureSimple> {
        
        public EventDelete() {
            super("删除", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemFeatureSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, final SystemFeatureSimple originForm, final BasicStateForm form,
                final String message) throws Exception {
            getFormBaseDao().executeUpdate(
                    SqlQueryUtil.prepareDeleteQuery(getFormTable(), new ObjectMap().put("=id", originForm.getId())));
            setFeatureAuths(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    /**
     * 检索已分配给指定租户的功能清单。
     * 
     * @param tenantCode 租户代码
     * 
     * @param nameLike   检索的关键字
     */
    public <T extends SystemFeatureSimple> PagedList<T> queryWithTenant(Class<T> clazz, String tenantCode,
            String nameLike, long page, int limit) throws Exception {
        if (StringUtils.isBlank(tenantCode)) {
            return null;
        }
        return listForm(clazz,
                new SystemFeatureDefaultQuery(limit, page).setNameLike(nameLike).setTenantCode(tenantCode));
    }
    
    /**
     * 通过名称或描述关键字检索功能清单。
     * 
     * @param nameLike   检索的关键字
     */
    public <T extends SystemFeatureSimple> PagedList<T> queryByNameLike(Class<T> clazz, String nameLike, long page,
            int limit) throws Exception {
        return listForm(clazz, new SystemFeatureDefaultQuery(limit, page).setNameLike(nameLike));
    }
    
    /**
     * 重写获取单个表单明细的方法
     * 
     */
    @Override
    public SystemFeatureDetail getForm(long formId) throws Exception {
        return getForm(SystemFeatureDetail.class, formId);
    }
    
    /**
     * 通过给定的编号列表，检索系统功能清单
     */
    public <T extends SystemFeatureSimple> List<T> queryByIds(@NonNull Class<T> clazz, final long... ids)
            throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemFeatureDefaultQuery(ids.length, 1L)
                .setFeatureIdsIn(StringUtils.join(ids, ','))).getList();
    }
    
    /**
     * 存储功能的授权数据
     * @param formId
     * @param auths
     * @throws Exception
     */
    private void setFeatureAuths(long formId, List<AuthorityEntity> auths) throws Exception {
        if (auths == null) {
            return;
        }
        getFormBaseDao().executeUpdate(
                SqlQueryUtil.prepareDeleteQuery("system_feature_auth", new ObjectMap().put("=feature_id", formId)));
        for (AuthorityEntity auth : auths) {
            if (auth == null || StringUtils.isBlank(auth.getOptionValue())) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery("system_feature_auth",
                    new ObjectMap().put("feature_id", formId).put("=auth_key", auth.getOptionValue())));
        }
    }
    
    /**
     * SELECT DISTINCT
     *     f.id feature_id,
     *     a.auth_key
     * FROM
     *     system_feature f,
     *     system_feature_auth a
     * WHERE
     *     f.id = a.feature_id
     * AND
     *     f.id IN (%s)
     */
    @Multiline
    private static final String SQL_QUERY_FEATURE_AUTHS = "X";
    
    @Data
    public static class FeatureAuthKey {
        
        private long featureId;
        
        private String authKey;
        
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemFeatureSimple> forms) throws Exception {
        if (forms == null || forms.size() <= 0) {
            return;
        }
        Map<Long, SystemFeatureWithAuths> withAuths = new HashMap<>();
        for (SystemFeatureSimple form : forms) {
            if (form == null || form.getId() == null) {
                continue;
            }
            if (SystemFeatureWithAuths.class.isAssignableFrom(form.getClass())) {
                withAuths.put(form.getId(), (SystemFeatureWithAuths)form);
            }
        }
        if (withAuths.size() > 0) {
            List<FeatureAuthKey> authKeys = getFormBaseDao().queryAsList(FeatureAuthKey.class,
                    String.format(SQL_QUERY_FEATURE_AUTHS, StringUtils.join("?", withAuths.size(), ",")),
                    withAuths.keySet().toArray());
            Set<String> singleFeatureAuths;
            Set<String> flattenFeatureAuths = new HashSet<>();
            Map<Long, Set<String>> authKeysByFeatureId = new HashMap<>();
            if (authKeys != null && !authKeys.isEmpty()) {
                for (FeatureAuthKey auth : authKeys) {
                    if ((singleFeatureAuths = authKeysByFeatureId.get(auth.getFeatureId())) == null) {
                        authKeysByFeatureId.put(auth.getFeatureId(), singleFeatureAuths = new HashSet<>());
                    }
                    singleFeatureAuths.add(auth.getAuthKey());
                    flattenFeatureAuths.add(auth.getAuthKey());
                }
                if (flattenFeatureAuths.size() > 0) {
                    List<? extends FieldOption> flattenAuthOptions;
                    if ((flattenAuthOptions = FieldSystemAuths.getInstance()
                            .queryDynamicValues(flattenFeatureAuths.toArray())) != null
                            && flattenAuthOptions.size() > 0) {
                        List<AuthorityEntity> singleOptionAuths;
                        Map<String, AuthorityEntity> mappedOptionAuths = new HashMap<>();
                        for (FieldOption option : flattenAuthOptions) {
                            mappedOptionAuths.put(option.getOptionValue(), (AuthorityEntity)option);
                        }
                        for (Map.Entry<Long, Set<String>> entry: authKeysByFeatureId.entrySet()) {
                            SystemFeatureWithAuths withAuth;
                            if ((withAuth = withAuths.get(entry.getKey())) == null) {
                                continue;
                            }
                            withAuth.setAuths(singleOptionAuths = new ArrayList<>());
                            AuthorityEntity option;
                            for (String authKey : entry.getValue()) {
                                if ((option = mappedOptionAuths.get(authKey)) == null) {
                                    continue;
                                }
                                singleOptionAuths.add(option);
                            }
                        }
                    }
                }
            }
        }
    }
}
