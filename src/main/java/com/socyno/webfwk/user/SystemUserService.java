package com.socyno.webfwk.user;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscexec.NamingFormatInvalidException;
import com.socyno.base.bscexec.TenantMissingException;
import com.socyno.base.bscmixutil.ClassUtil;
import com.socyno.base.bscmixutil.CommonUtil;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.AbstractUser;
import com.socyno.base.bscmodel.ObjectMap;
import com.socyno.base.bscmodel.PagedList;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.base.bscmodel.UserContext;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.base.bscsqlutil.AbstractDao.ResultSetProcessor;
import com.socyno.base.bscsqlutil.SqlQueryUtil;
import com.socyno.stateform.abs.AbstractStateAction;
import com.socyno.stateform.abs.AbstractStateCreateAction;
import com.socyno.stateform.abs.AbstractStateFormServiceWithBaseDao;
import com.socyno.stateform.abs.BasicStateForm;
import com.socyno.stateform.sugger.DefaultStateFormSugger;
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.webbsc.authority.AuthoritySpecialChecker;
import com.socyno.webbsc.authority.AuthoritySpecialRejecter;
import com.socyno.stateform.util.*;
import com.socyno.webbsc.ctxutil.LoginTokenUtil;
import com.socyno.webbsc.service.jdbc.TenantBasicService;
import com.socyno.webbsc.service.jdbc.TenantSpecialDataSource;
import com.socyno.webfwk.role.SystemRoleService;
import com.socyno.webfwk.token.UserTokenService;
import com.socyno.webfwk.user.WindowsAdService.SystemWindowsAdUser;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SystemUserService extends AbstractStateFormServiceWithBaseDao<SystemUserSimple> {
    
    @Getter
    private static final SystemUserService instance = new SystemUserService();
    
    static {
        DefaultStateFormSugger.addFieldDefinitions(
                new SuggerSystemUser(),
                new SuggerSystemUsername(),
                new SuggerSystemUserGrantedAuth());
    }
    
    private SystemUserService() {
        setStates(STATES.values());
        setActions(EVENTS.values());
        setQueries(QUERIES.values());
    }
    
    @Override
    public String getFormName() {
        return "system_user";
    }
    
    @Override
    public String getFormTable() {
        return "system_user";
    }
    
    @Override
    protected AbstractDao getFormBaseDao() {
        return TenantSpecialDataSource.getMain();
    }
    
    @Getter
    public static enum STATES implements StateFormStateBaseEnum {
        DISABLED ("disabled", "禁用"),
        ENABLED  (AbstractUser.ENABLED,  "有效")
        ;
        
        private final String code;
        private final String name;
        
        STATES(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    
    @Getter
    public static enum EVENTS implements StateFormEventClassEnum {
        /**
         * 创建用户
         */
        Create(EventCreate.class),
        
        /**
         * 创建用户
         */
        CreateFromDomain(EventCreateFromDomain.class),
        
        /**
         * 更新用户信息
         */
        Update(EventUpdate.class),
        
        /**
         * 修改用户密码
         */
        Password(EventPassword.class),
        
        /**
         * 禁用指定用户
         */
        Disable(EventDisable.class),
        
        /**
         * 恢复对用户的禁用
         */
        Enable(EventEnable.class),
        
        /**
         * 给用户授权
         */
        AuthsAdd(EventAuthsAdd.class),
        
        /**
         * 移除用户授权
         */
        AuthsDel(EventAuthsDel.class)
        
        ;
        
        private final Class<? extends AbstractStateAction<SystemUserSimple, ?, ?>> eventClass;
        EVENTS(Class<? extends AbstractStateAction<SystemUserSimple, ?, ?>> eventClass) {
            this.eventClass = eventClass;
        }
    }
    
    @Getter
    public static enum QUERIES implements StateFormQueryBaseEnum {
        DEFAULT(new StateFormNamedQuery<SystemUserDefaultRow>("通用查询", 
                SystemUserDefaultRow.class, SystemUserDefaultQuery.class));
        private StateFormNamedQuery<?> namedQuery;
        
        QUERIES(StateFormNamedQuery<?> namedQuery) {
            this.namedQuery = namedQuery;
        }
    }

    public List<SystemUserSecurityOnly> getUsersSecurity(Object[] userIds) throws Exception {
        if (userIds == null || userIds.length <= 0) {
            return Collections.emptyList();
        }
        return getUsersSecurity(ConvertUtil.asNonNullUniqueLongArray(userIds));
    }
    
    public List<SystemUserSecurityOnly> getUsersSecurity(Collection<?> userIds) throws Exception {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getUsersSecurity(ConvertUtil.asNonNullUniqueLongArray(userIds.toArray()));
    }
    
    public List<SystemUserSecurityOnly> getUsersSecurity(Long... userIds) throws Exception {
        if (userIds == null || userIds.length <= 0
                || (userIds = ConvertUtil.asNonNullUniqueLongArray(userIds)).length <= 0) {
            return Collections.emptyList();
        }
        return listForm(SystemUserSecurityOnly.class,
                    new SystemUserDefaultQuery(userIds.length, 1L)
                    .setUserIdsIn(StringUtils.join(userIds, ','))).getList();
    }
    
    protected AbstractUser ensureAdUserExisted(SystemWindowsAdUser windowsAdUser, String tenant) throws Exception {
        String username = String.format(String.format("%s@%s", windowsAdUser.getLogin(), tenant));
        ObjectMap queryData = new ObjectMap().put("username", username).put("=display", windowsAdUser.getName())
                                                .put("=password", "");
        AbstractUser manager = null;
        if (windowsAdUser.getManager() != null) {
            manager = ensureAdUserExisted(windowsAdUser.getManager(), tenant);
            queryData.put("=manager", manager.getId());
        }
        String title = StringUtils.trimToEmpty(windowsAdUser.getTitle());
        String mobile = StringUtils.trimToEmpty(windowsAdUser.getMobile());
        String mailAddress = StringUtils.trimToEmpty(windowsAdUser.getMail());
        String telphone = StringUtils.trimToEmpty(windowsAdUser.getTelphone());
        String department = StringUtils.trimToEmpty(windowsAdUser.getDepartment());
        queryData.put(title.isEmpty() ? "title" : "=title", title);
        queryData.put(mobile.isEmpty() ? "mobile" : "=mobile", mobile);
        queryData.put(department.isEmpty() ? "department" : "=department", department);
        queryData.put(telphone.isEmpty() ? "telphone" : "=telphone", telphone);
        queryData.put(mailAddress.isEmpty() ? "mail_address" : "=mail_address", mailAddress);
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(getFormTable(), queryData));
        return getSimple(username);
    }
    
    /**
     * 从字段映射关系层，移除敏感信息
     */
    @Override
    protected Map<String, String> getExtraFieldMapper(Class<?> resultClazz, Map<String, String> queryMapper) {
        Map<String, String> finalMapper = new HashMap<>();
        if (queryMapper != null) {
            finalMapper.putAll(queryMapper);
        }
        finalMapper.put("-password", "****");
        if (!SystemUserWithSecurities.class.isAssignableFrom(resultClazz)) {
            finalMapper.put("-mobile", "****");
            finalMapper.put("-telphone", "****");
        }
        return finalMapper;
    }
    
    @Override
    protected void fillExtraFormFields(Collection<? extends SystemUserSimple> forms) throws Exception {
        DefaultStateFormSugger.getInstance().apply(forms);
    }
    
    public class CurrentUserIsMeChecker implements AuthoritySpecialChecker {

        @Override
        public boolean check(Object originForm) {
            return originForm != null && SessionContext.getUserId() != null 
                    && SessionContext.getUserId().equals(((AbstractUser)originForm).getId());
        }
    }

    public class CreateDomainUserRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return !TenantBasicService.getInstance().inSuperTenantContext()
                    && !WindowsAdService.equalsDefaultDomain(SessionContext.getTenant());
        }
    }
    
    public class ChangePasswordRejecter implements AuthoritySpecialRejecter {
        
        @Override
        public boolean check(Object originForm) throws Exception {
            return originForm == null ||
                    !localPasswordEnabled(((AbstractUser)originForm).getId());
        }
    }
    
    public class EventCreate extends AbstractStateCreateAction<SystemUserSimple, SystemUserForCreation, SystemUserSimple> {
        
        public EventCreate() {
            super("添加常规账户", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemUserSimple handle(String event, SystemUserSimple originForm, SystemUserForCreation form, String message) throws Exception {
            return getForm(createUserWithoutCheck(form, false));
        }
    }
    
    public class EventCreateFromDomain extends AbstractStateCreateAction<SystemUserSimple, SystemUserForCreationDomain, SystemUserSimple> {
        
        public EventCreateFromDomain() {
            super("添加域(Windows)用户", STATES.ENABLED.getCode());
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, rejecter = CreateDomainUserRejecter.class)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
        
        @Override
        public SystemUserSimple handle(String event, SystemUserSimple originForm, SystemUserForCreationDomain form, String message) throws Exception {
            SystemWindowsAdUser windowsAdUser;
            if ((windowsAdUser = WindowsAdService.getAdUser(form.getUsername())) == null) {
                throw new MessageException(String.format("用户（%s）的未在域中注册", form.getUsername()));
            }
            if (getSimple(String.format("%s@%s", windowsAdUser.getLogin(), SessionContext.getTenant())) != null) {
                throw new MessageException(String.format("用户(%s)已存在", windowsAdUser.getLogin()));
            }
            return getForm(ensureAdUserExisted(windowsAdUser, SessionContext.getTenant()).getId());
        }
    }
    
    public class EventUpdate extends AbstractStateAction<SystemUserSimple, SystemUserForEdition, Void> {
        
        public EventUpdate() {
            super("编辑", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = CurrentUserIsMeChecker.class)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemUserSimple originForm, final SystemUserForEdition form, final String message) throws Exception {
           
            ObjectMap changed = new ObjectMap()
                    .put("=id", form.getId())
                    .put("display", form.getDisplay())
                    .put("mail_address", form.getMailAddress())
                    .put("title", StringUtils.trimToEmpty(form.getTitle()))
                    .put("department", StringUtils.trimToEmpty(form.getDepartment()))
                    .put("mobile", StringUtils.trimToEmpty(form.getMobile()))
                    .put("telphone", StringUtils.trimToEmpty(form.getTelphone()));
            if (form.getManagerEntity() != null) {
                changed.put("manager", form.getManagerEntity().getId());
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(getFormTable(), changed));
            return null;
        }
    }

    public class EventPassword extends AbstractStateAction<SystemUserSimple, SystemUserForNewPassword, Void> {
        
        public EventPassword() {
            super("修改密码", getStateCodesEx(), "");
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System, checker = CurrentUserIsMeChecker.class, rejecter = ChangePasswordRejecter.class)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemUserSimple originForm, SystemUserForNewPassword form, String message)
                            throws Exception {
            /* 确认密码非空，且输入确认正确 */
            String newPassword = ((SystemUserForNewPassword)form).getNewPassword();
            if (StringUtils.isBlank(newPassword)
                    || !StringUtils.equals(newPassword, ((SystemUserForNewPassword)form).getConfirmPassword())) {
                throw new MessageException("两次输入的新密码不一致!");
            }
            
            /* 当修改自己的密码时，需要验证原密码 */
            if (form.getId().equals(SessionContext.getUserId())) {
                if (!checkLocalPassword(SessionContext.getUserId(), ((SystemUserForNewPassword)form).getPassword())) {
                    throw new MessageException("输入密码不正确，或者非本地用户!");
                }
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareUpdateQuery(
                    getFormTable(), new ObjectMap()
                        .put("=id", form.getId())
                        .put("password", passwordEncode(newPassword))
            ));
            return null;
        }
        
    }

    public class EventDisable extends AbstractStateAction<SystemUserSimple, BasicStateForm, Void> {
        
        public EventDisable() {
            super("禁用", getStateCodesEx(STATES.DISABLED), STATES.DISABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
    }

    public class EventEnable extends AbstractStateAction<SystemUserSimple, BasicStateForm, Void> {
        
        public EventEnable() {
            super("启用", getStateCodes(STATES.DISABLED), STATES.ENABLED.getCode());
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
    }

    public class EventAuthsAdd extends AbstractStateAction<SystemUserSimple, SystemUserForAuthsAdd, Void> {
        
        public EventAuthsAdd() {
            super("添加授权", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemUserSimple originForm, SystemUserForAuthsAdd form, String message)
                            throws Exception {
            addSystemUserAuths(form.getId(), form.getAuthsAdded());
            return null;
        }
    }
    
    private void addSystemUserAuths(long formId, List<OptionSystemUserAuth> auths) throws Exception {
        if (auths == null || auths.isEmpty()) {
            return;
        }
        for (OptionSystemUserAuth a : auths) {
            if (a == null) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                "system_user_scope_role", new ObjectMap()
                    .put("=user_id", formId)
                    .put("role_id", a.getRoleId())
                    .put("scope_id", a.getScopeId())
                    .put("scope_type", a.getScopeType())
            ));
        }
    }

    public class EventAuthsDel extends AbstractStateAction<SystemUserSimple, SystemUserForAuthsDel, Void> {
        
        public EventAuthsDel() {
            super("移除授权", getStateCodesEx(), "");
        }
        
        @Override
        public Boolean messageRequired() {
            return true;
        }
        
        @Override
        @Authority(value = AuthorityScopeType.System)
        public void check(String event, SystemUserSimple form, String sourceState) {
            
        }
        
        @Override
        public Void handle(String event, SystemUserSimple originForm, SystemUserForAuthsDel form, String message)
                            throws Exception {
            delSystemUserAuths(form.getId(), form.getAuthsRemoved());
            return null;
        }
    }
    
    private void delSystemUserAuths(long formId, List<OptionSystemUserAuth> auths) throws Exception {
        if (auths == null || auths.isEmpty()) {
            return;
        }
        for (OptionSystemUserAuth a : auths) {
            if (a == null) {
                continue;
            }
            getFormBaseDao().executeUpdate(SqlQueryUtil.prepareDeleteQuery(
                "system_user_scope_role", new ObjectMap()
                    .put("=user_id", formId)
                    .put("=role_id", a.getRoleId())
                    .put("=scope_id", a.getScopeId())
                    .put("=scope_type", a.getScopeType())
            ));
        }
    }
    
    /**
     * 获取用户本地密码
     */
    private String getLocalPassword(long userId) throws Exception {
        return getFormBaseDao().queryAsObject(String.class, String.format(
                "SELECT password FROM %s WHERE %s = ?",
                getFormTable(), getFormIdField()
            ), new Object[] {userId});
    }
    
    /**
     * 是否允许本地登录
     */
    public boolean localPasswordEnabled(long userId) throws Exception {
        return !StringUtils.isBlank(getLocalPassword(userId));
    }
    
    private static String passwordEncode(String password) {
        String encoded = DigestUtils.sha256Hex(password);
        return DigestUtils.sha256Hex(String.format("ufwe4&$%%&casdf$%s&*w", encoded));
    }
    
    /**
     * 验证登录密码
     */
    public boolean checkLocalPassword(long userId, String password) throws Exception {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        String localPassword;
        if (StringUtils.isBlank(localPassword = getLocalPassword(userId))) {
            return false;
        }
        return localPassword.equals(passwordEncode(password));
    }
    
    /**
     * 检索用户清单。
     * 
     * @param nameLike        检索的关键字
     * @param disableIncluded 是否包括已禁用的用户
     */
    public <T extends SystemUserSimple> PagedList<T> queryByNameLike(Class<T> clazz, String nameLike,
            boolean disableIncluded, long page, int limit) throws Exception {
        return listForm(clazz,
                new SystemUserDefaultQuery(limit, page).setNameLike(nameLike).setDisableIncluded(disableIncluded));
    }
    
    /**
     * 检索用户详情。
     */
    public <T extends SystemUserSimple> List<T> queryByUserIds(@NonNull Class<T> clazz, final long... ids)
            throws Exception {
        if (ids == null || ids.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemUserDefaultQuery(ids.length, 1L).setDisableIncluded(true)
                .setUserIdsIn(StringUtils.join(ids, ','))).getList();
    }
    
    /**
     * 检索用户详情。
     */
    public <T extends SystemUserSimple> List<T> queryByUsernames(@NonNull Class<T> clazz, final String... usernames)
            throws Exception {
        if (usernames == null || usernames.length <= 0) {
            return Collections.emptyList();
        }
        return listForm(clazz, new SystemUserDefaultQuery(usernames.length, 1L)
                .setUsernamesIn(StringUtils.join(usernames, ',')).setDisableIncluded(true)).getList();
    }
    
    /**
     * 根据用户的编号或者代码检索用户详情。
     */
    public SystemUserDetail get(Object idOrUsername) throws Exception {
        if (idOrUsername == null || StringUtils.isBlank(idOrUsername.toString())) {
            return null;
        }
        List<SystemUserDetail> result;
        if (idOrUsername.toString().matches("^\\d+$")) {
            result = queryByUserIds(SystemUserDetail.class, CommonUtil.parseLong(idOrUsername));
        } else {
            result = queryByUsernames(SystemUserDetail.class, idOrUsername.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        SystemUserDetail user = result.get(0);
        return user;
    }
    
    /**
     * 切换到指定用户上下文，返回当前的上下文对象。
     */
    public UserContext forceSuToUser(String username) throws Exception {
        /* 首先, 提取租户代码 */
        UserContext currentContext = SessionContext.getUserContext();
        String tenant = AbstractUser.parseTenantFromUsername(username);
        SessionContext.setUserContext(new UserContext().setTenant(tenant));
        
        /* 检索并验证用户是否有效 */
        AbstractUser sysuser;
        if ((sysuser = getSimple(username)) == null || sysuser.isDisabled()) {
            log.info("账户（{}）不存在，或者已被禁用", username);
            throw new MessageException(String.format("No such user found or disabled : %s", username));
        }
        SessionContext.setUserContext(new UserContext().setSysUser(sysuser)
                .setTokenHead(UserTokenService.getTokenHeader())
                .setToken(LoginTokenUtil.generateToken(sysuser, false)));
        return currentContext;
    }
    
    /**
     * 用户认证
     */
    public SystemUserToken login(SystemUserForLogin form) throws Exception {
        if (form == null || StringUtils.isBlank(form.getUsername()) || StringUtils.isBlank(form.getPassword())) {
            throw new MessageException("账户或密码信息不完整");
        }
        UserContext currentContext = SessionContext.getUserContext();
        try {
            String tenant;
            try {
                tenant = AbstractUser.parseTenantFromUsername(form.getUsername());
            } catch (TenantMissingException e) {
                if (StringUtils.isBlank(form.getUsername())) {
                    throw e;
                }
                tenant = WindowsAdService.getDefaultDomain();
                form.setUsername(String.format("%s%s", form.getUsername(), WindowsAdService.getDefaultDomainSuffix()));
            }
            
            /* 系统登录前必须先设置租户上下文，否则无法访问到对应的租户数据库 */
            SessionContext.setUserContext(new UserContext().setTenant(tenant));
            if (!TenantBasicService.getInstance().checkTenantEnabled(SessionContext.getTenant())) {
                log.info("租户（{}）不存在，或者已被禁用", SessionContext.getTenant());
                throw new MessageException("账户或密码信息错误");
            }
            /* 检索并验证用户的密码 */
            AbstractUser sysuser;
            if ((sysuser = getSimple(form.getUsername())) == null || sysuser.isDisabled()) {
                throw new MessageException("账户或密码信息错误");
            }
            boolean success = false;
            if (checkLocalPassword(sysuser.getId(), form.getPassword())) {
                success = true;
            }
            SystemWindowsAdUser winadUser = null;
            if (!success && WindowsAdService.equalsDefaultDomain(sysuser.getTenant())
                    && (winadUser = WindowsAdService.verifyAdUser(form.getUsername(), form.getPassword())) != null) {
                success = true;
            }
            if (!success && TenantBasicService.getInstance().equalsSuperTenant(sysuser.getTenant())
                    &&(winadUser = WindowsAdService.verifyAdUser(
                            form.getUsername().replace("@" + TenantBasicService.getInstance().getSuperTenant(), ""),
                            form.getPassword())) != null) {
                success = true;
            }
            if (success) {
                /**
                 *  代理登录模式校验
                 */
                AbstractUser proxiedUser = null;
                boolean isAdmin = isAdmin(sysuser.getId());
                boolean isSuperTeant = TenantBasicService.getInstance().inSuperTenantContext();
                if (StringUtils.isNotBlank(form.getProxied())) {
                    if (!isAdmin) {
                        throw new MessageException("未获得被代理用户的授权");
                    }
                    String proxiedTenant = AbstractUser.parseTenantFromUsername(form.getProxied());
                    if (!isSuperTeant && !StringUtils.equals(proxiedTenant, sysuser.getTenant())) {
                        throw new MessageException("未获得被代理用户的授权");
                    }
                    /* 系统登录前必须先设置为别代理用户的租户上下文，否则无法访问到对应的租户数据库 */
                    SessionContext.setUserContext(new UserContext().setTenant(proxiedTenant));
                    /* 确认被代理租户可用 */
                    if (!TenantBasicService.getInstance().checkTenantEnabled(SessionContext.getTenant())) {
                        log.info("被代理租户（{}）不存在，或者已被禁用", SessionContext.getTenant());
                        throw new MessageException("未获得被代理用户的授权");
                    }
                    /* 确认被代理用户可用 */
                    if ((proxiedUser = getSimple(form.getProxied())) == null || proxiedUser.isDisabled()) {
                        log.info("被代理用户（{}）不存在，或者已被禁用", SessionContext.getTenant());
                        throw new MessageException("未获得被代理用户的授权");
                    }
                }
                /* 更新域用户的必要信息 */
                if (winadUser != null) {
                    ensureAdUserExisted(winadUser, tenant);
                }
                /* END */
                /**
                 * 自动添加基本授权角色
                 */
                getFormBaseDao().executeUpdate(SQL_QUERY_ADD_SYSTEM_ROLE,
                        new Object[] { proxiedUser == null ? sysuser.getId() : proxiedUser.getId(),
                                SystemRoleService.InternalRoles.Basic.getCode() });
                /**
                 * 创建并返回用户登录令牌
                 */
                final SystemUserToken userToken = new SystemUserToken();
                if (proxiedUser != null) {
                    userToken.setTokenHeader(UserTokenService.getTokenHeader())
                            .setTokenContent(LoginTokenUtil.generateToken(proxiedUser,
                                    isAdmin(proxiedUser.getId()), sysuser,
                                    new ObjectMap().put("isSuperTenant",
                                            TenantBasicService.getInstance().equalsSuperTenant(proxiedUser.getTenant()))));
                } else {
                    userToken.setTokenHeader(UserTokenService.getTokenHeader()).setTokenContent(LoginTokenUtil
                            .generateToken(sysuser, isAdmin, null, new ObjectMap().put("isSuperTenant", isSuperTeant)));
                }
                return userToken;
            }
        } finally {
            SessionContext.setUserContext(currentContext);
        }
        throw new MessageException("账户或密码信息错误");
    }
    
    /**
     * 检索用户详情（包括关联的可执行操作）。
     */
    @Override
    public SystemUserDetail getForm(long id) throws Exception {
        return get(id);
    }
    
    /**
     * SELECT
     *     COUNT( 1 ) 
     * FROM
     *     system_user_scope_role s,
     *     system_role r 
     * WHERE
     *     r.id = s.role_id 
     * AND 
     *     s.user_id = ?
     * AND
     *     r.code = ?
     * AND
     *     s.scope_type = 'System'
     */
    @Multiline
    private static final String SQL_QUERY_CHECK_USER_ADMIN = "X";
    
    /**
     * 判断用户是否是管理员
     * 
     */
    public boolean isAdmin(Long userId) throws Exception {
        if (userId == null) {
            return false;
        }
        if (Long.valueOf(1).equals(userId)) {
            return true;
        }
        return getFormBaseDao().queryAsObject(Long.class, SQL_QUERY_CHECK_USER_ADMIN,
                new Object[] { userId, SystemRoleService.InternalRoles.Admin.getCode()}) > 0;
    }
    
    public AbstractUser getSimple(Object idOrUsername) throws Exception {
        if (idOrUsername == null || StringUtils.isBlank(idOrUsername.toString())) {
            return null;
        }
        List<SystemUserSimple> result;
        if (idOrUsername.toString().matches("^\\d+$")) {
            result = queryByUserIds(SystemUserSimple.class, CommonUtil.parseLong(idOrUsername));
        } else {
            result = queryByUsernames(SystemUserSimple.class, idOrUsername.toString());
        }
        if (result == null || result.size() != 1) {
            return null;
        }
        return result.get(0);
    }
    
    /**
     * INSERT IGNORE INTO system_user_scope_role (user_id, scope_type, role_id, scope_id)
     *     SELECT ?, 'System', r.id, 0 FROM system_role r WHERE r.code = ?
     */
    @Multiline
    private static final String SQL_QUERY_ADD_SYSTEM_ROLE = "X";
    
    public long createUserWithoutCheck(SystemUserForCreation form, boolean asAdmin) throws Exception {
        
        ClassUtil.checkFormRequiredAndOpValue(form, true, AbstractStateAction.getInternalFields());
        
        String nameSuffix = String.format("@%s", SessionContext.getTenant());
        String nameNoTenant = StringUtils.removeEnd(form.getUsername(), nameSuffix);
        if (nameNoTenant.equals(form.getUsername())) {
            throw new NamingFormatInvalidException(String.format("用户的账户名称不规范，必须以 %s 结尾", nameSuffix));
        }
        AbstractUser.ensuerNameFormatValid(nameNoTenant);
        String password = form.getNewPassword();
        String confirmPassword = form.getConfirmPassword();
        if (StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)
                || !StringUtils.equals(password, confirmPassword)) {
                throw new MessageException("两次输入的新密码不一致!");
        }
        if (getFormBaseDao().queryAsMap(
                String.format("SELECT * FROM %s WHERE username = ?", getFormTable()),
                new Object[] { form.getUsername() }) != null) {
            throw new MessageException("用户的账户名已被占用!");
        }
        final AtomicLong id = new AtomicLong(-1);
        getFormBaseDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                getFormTable(), new ObjectMap()
                    .put("username",        form.getUsername())
                    .put("mail_address",    form.getMailAddress())
                    .put("display",         form.getDisplay())
                    .put("password",        passwordEncode(password))
                    .put("title",           StringUtils.trimToEmpty(form.getTitle()))
                    .put("department",      StringUtils.trimToEmpty(form.getDepartment()))
                    .put("mobile",          StringUtils.trimToEmpty(form.getMobile()))
                    .put("telphone",        StringUtils.trimToEmpty(form.getTelphone()))
                    .put("created_at",      new Date())
                    .put("created_by",      SessionContext.getTokenUserId())
                    .put("created_code_by", SessionContext.getUsername())
                    .put("created_name_by", SessionContext.getDisplay())
        ), new ResultSetProcessor () {
            @Override
            public void process(ResultSet r, Connection c) throws Exception {
                r.next();
                id.set(r.getLong(1));
            }
        });
        if (asAdmin) {
            getFormBaseDao().executeUpdate(SQL_QUERY_ADD_SYSTEM_ROLE,
                    new Object[] { id.get(), SystemRoleService.InternalRoles.Admin.getCode() });
        }
        return id.get();
    }
}
