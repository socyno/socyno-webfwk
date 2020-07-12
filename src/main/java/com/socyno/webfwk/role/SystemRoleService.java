package com.socyno.webfwk.role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.adrianwalker.multilinestring.Multiline;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscexec.NamingConflictedException;
import com.socyno.base.bscexec.NamingFormatInvalidException;
import com.socyno.base.bscmixutil.CommonUtil;
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
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.stateform.util.StateFormEventClassEnum;
import com.socyno.stateform.util.StateFormNamedQuery;
import com.socyno.stateform.util.StateFormQueryBaseEnum;
import com.socyno.stateform.util.StateFormStateBaseEnum;
import com.socyno.webbsc.service.jdbc.TenantSpecialDataSource;
import com.socyno.webfwk.feature.SystemFeatureOption;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

public class SystemRoleService extends AbstractStateFormServiceWithBaseDao<SystemRoleSimple> {
    
    @Getter
    private final static SystemRoleService instance = new SystemRoleService();
    
    public SystemRoleService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Getter
    public static enum InternalRoles {
        Admin("admin"),
        Basic("basic");
        
        private final String code;
        InternalRoles(String code) {
            this.code = code;
        }
        
        public static boolean contains(String code) {
            if (code != null && (code = code.trim()).isEmpty()) {
                return false;
            }
            for (InternalRoles v : InternalRoles.values()) {
                if (v.getCode().equalsIgnoreCase(code)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum  {
        ENABLED("enabled", "有效")
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
        DEFAULT(new StateFormNamedQuery<SystemRoleDefaultRow>("通用查询", 
                SystemRoleDefaultRow.class, SystemRoleDefaultQuery.class))
        ;
        
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemRoleSimple, SystemRoleForCreation, SystemRoleSimple> {
        
        public EventCreate() {
            super("添加", STATES.ENABLED.getCode());
        }

        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemRoleSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemRoleSimple handle(String event, SystemRoleSimple originForm, SystemRoleForCreation role, String message)
                throws Exception {
            ensureRoleCodeValid(null, role.getCode());
            /* 添加角色及其授权信息 */
            final AtomicLong id = new AtomicLong();
            getFormBaseDao().executeUpdate(
                SqlQueryUtil.prepareInsertQuery(getFormTable(),
                        new ObjectMap().put("code", role.getCode()).put("name", role.getName())
                                .put("description", CommonUtil.ifNull(role.getDescription(), ""))),
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
    
    public class EventUpdate extends AbstractStateAction<SystemRoleSimple, SystemRoleForEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemRoleSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemRoleSimple originForm, final SystemRoleForEdition role, final String message)
                            throws Exception {
            ensureRoleCodeValid(role.getId(), role.getCode());
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id", role.getId())
                        .put("code", role.getCode())
                        .put("name", role.getName())
                        .put("description", role.getDescription())
            ));
            return null;
        }
    }
    
    public class EventDelete extends AbstractStateDeleteAction<SystemRoleSimple> {
        
        public EventDelete() {
            super("编辑", getStateCodesEx());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemRoleSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemRoleSimple originForm, BasicStateForm form, String message)
                        throws Exception {
            if (InternalRoles.contains(originForm.getCode())) {
                throw new MessageException("系统内建角色，禁止删除");
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                    getFormTable(), new ObjectMap()
                            .put("=id", originForm.getId())
            ));
            setFeatures(originForm.getId(), Collections.emptyList());
            return null;
        }
    }
    
    public static enum EVENTS implements StateFormEventClassEnum {
        
        Create(EventCreate.class)
        , Update(EventUpdate.class)
        , Delete(EventDelete.class)
        ;
        
        private final Class<? extends AbstractStateAction<SystemRoleSimple, ?, ?>> actionClass;
        EVENTS(Class<? extends AbstractStateAction<SystemRoleSimple, ?, ?>> actionClass) {
            this.actionClass = actionClass;
        }
        
        @Override
        public Class<? extends AbstractStateAction<SystemRoleSimple, ?, ?>> getEventClass() {
            return actionClass;
        }
    }
    
    @Override
    public String getFormName() {
        return "system_role";
    }
    
    @Override
    public String getFormTable() {
        return "system_role";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return TenantSpecialDataSource.getMain();
    }
    
    /**
     * 通过名称关键字检索角色
     */
    public <T extends SystemRoleSimple> PagedList<T> queryByNameLike(@NonNull Class<T> clazz, String nameLike, long page, int limit) throws Exception {
        return listForm(clazz, new SystemRoleDefaultQuery(limit, page).setNameLike(nameLike));
    }
    
    /**
     * 根据角色编号或者代码检索角色详情。
     */
    public SystemRoleDetail get(Object idOrCode) throws Exception {
        if (idOrCode == null || StringUtils.isBlank(idOrCode.toString())) {
            return null;
        }
        List<SystemRoleDetail> result;
        if (idOrCode.toString().matches("^\\d+$")) {
            result = queryByIds(SystemRoleDetail.class, CommonUtil.parseLong(idOrCode));
        } else {
            result = queryByCodes(SystemRoleDetail.class, idOrCode.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        return result.get(0);
    }
    
    /**
     * 通过给定的编号列表，检索角色清单。
     */
    public <T extends SystemRoleSimple> List<T> queryByIds(@NonNull Class<T> clazz, final long... ids)
            throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemRoleDefaultQuery(ids.length, 1L).setRoleIdsIn(StringUtils.join(ids, ',')))
                .getList();
    }

    /**
     * 通过给定的代码列表，检索角色清单。
     */
    public <T extends SystemRoleSimple> List<T> queryByCodes(@NonNull Class<T> clazz, final String... roleCodes) throws Exception {
        if (roleCodes == null || roleCodes.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemRoleDefaultQuery(roleCodes.length, 1L).setRoleCodesIn(StringUtils.join(roleCodes, ',')))
                .getList();
    }
    
    protected void ensureRoleCodeValid(Long id, String code) throws Exception {
        /**
         * 检查命名规范
         */
        if (StringUtils.isBlank(code) || !code.matches("^[a-zA-Z\\_\\-\\d]+$")
                || code.matches("^\\d+$")) {
            throw new NamingFormatInvalidException("角色代码的命名规范");
        }
        
        /**
         * 检测角色代码是否已被使用
         */
        if (id == null && InternalRoles.contains(code)) {
            throw new NamingConflictedException("角色代码已被占用");
        }
        String checkSql = String.format("SELECT COUNT(1) FROM %s WHERE code = ?", getFormTable());
        if (id != null) {
            checkSql += String.format(" AND id != %s", id);
        }
        if (getFormBaseDao().queryAsObject(Long.class, String.format(checkSql, getFormTable()),
                new Object[] { code }) != 0) {
            throw new NamingConflictedException("角色代码已被占用");
        }
    }
    
    /**
     * 获取角色详情（包括关联功能数据）。
     */
    @Override
    public SystemRoleDetail getForm(long id) throws Exception {
        return get(id);
    }
    
    private void setFeatures(long formId, List<SystemFeatureOption> features) throws Exception {
        if (features == null) {
            return;
        }
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                "system_role_feature", new ObjectMap().put("=role_id", formId)));
        for (SystemFeatureOption feature : features) {
            if (feature == null) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                    "system_role_feature", new ObjectMap()
                            .put("=feature_id", feature.getId())
                            .put("role_id", formId)));
        }
    }
    
    @Data
    public static class RoleFeatureOnlyId {
        
        private Long roleId;
        
        private Long featureId;
    }

    
    /**
     * SELECT DISTINCT
     *    r.role_id,
     *    r.feature_id
     * FROM
     *    system_role_feature r
     * WHERE
     *    r.role_id in (%s)
     */
    @Multiline
    private static final String SQL_QUERY_ROLE_FEATURES = "X";
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemRoleSimple> forms) throws Exception {
        
    }
}
