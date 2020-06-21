package com.socyno.webfwk.initail;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmixutil.ClassUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.base.bscmodel.UserContext;
import com.socyno.base.bscsqlutil.AbstractDao;
import com.socyno.stateform.service.SimpleEncryptService;
import com.socyno.stateform.service.TenantBasicService;
import com.socyno.webbsc.ctxutil.ContextUtil;
import com.socyno.webbsc.ctxutil.LoginTokenUtil;
import com.socyno.webfwk.user.SystemUserForCreation;
import com.socyno.webfwk.user.SystemUserService;

import lombok.Getter;

public class SystemInitialService {
    
    @Getter
    private static final SystemInitialService instance = new SystemInitialService();
	
    private Boolean systemNeedToInitialized = null;
    
    private SystemInitialService() {
        
    }
    
    private AbstractDao getDao() {
       return TenantBasicService.getInstance().getDao();
    }
    
    private boolean isInitiallized() throws Exception {
        if (systemNeedToInitialized != null) {
            return systemNeedToInitialized;
        }
        synchronized (this.getClass()) {
            if (systemNeedToInitialized == null) {
                systemNeedToInitialized = checkAndInitializeSystem();
            }
        }
        return systemNeedToInitialized;
    }
    
    public SystemBasicInfo getSysInfo() throws Exception {
        return new SystemBasicInfo().setInitialized(isInitiallized())
                .setSuperTenantCode(TenantBasicService.getInstance().getSuperTenant());
    }
    
    public synchronized void initialize(SystemInitialConfig config) throws Exception {
        
        if (systemNeedToInitialized = checkAndInitializeSystem()) {
            throw new MessageException("当前系统已完成初始化，请勿重复操作");
        }
        ClassUtil.checkFormRequiredAndOpValue(config);
        /**
         * 创建超级租户并授权于当前所有功能集合
         */
        TenantBasicService.getInstance().createTenantIfMissing(config.getSuperTenantCode(),
                config.getSuperTenantName(), true);
        ContextUtil.setConfig(TenantBasicService.CONFIG_SUPER_TENANT_KEY, config.getSuperTenantCode());
        String superTeantCode = TenantBasicService.getInstance().getSuperTenant();
        if (!StringUtils.equals(superTeantCode, config.getSuperTenantCode())) {
            throw new MessageException("系统异常，创建超级租户失败");
        }
        /**
         * 创建超级租户管理员
         */
        if (!StringUtils.endsWith(config.getSuperAdminUsername(), superTeantCode)) {
            config.setSuperAdminUsername(String.format("%s@%s", config.getSuperAdminUsername(), superTeantCode));
        }
        SystemUserForCreation form = new SystemUserForCreation();
        form.setUsername(config.getSuperAdminUsername());
        form.setDisplay(config.getSuperAdminUserDisplay());
        form.setMailAddress(config.getSuperAdminMailAddress());
        form.setNewPassword(config.getSuperAdminPassword());
        form.setConfirmPassword(config.getSuperAdminConfirmPassword());
        /* 正常情况下，执行此操作时，是无用户上下文的，必须先设置当前租户 */
        SessionContext.setUserContext(new UserContext().setTenant(superTeantCode));
        SystemUserService.getInstance().createUserWithoutCheck(form, true);
        
        systemNeedToInitialized = true;
    }
    
    private boolean checkAndInitializeSystem() throws Exception {
        
        /**
         * 初始化通用加解密密钥
         */
        if (StringUtils.isBlank(ContextUtil.getConfig(SimpleEncryptService.CONFIG_SIMPLE_ENCRYPT_KEY))) {
            ContextUtil.setConfig(SimpleEncryptService.CONFIG_SIMPLE_ENCRYPT_KEY, SimpleEncryptService.generateKey());
        }
        
        /**
         * 初始化用户令牌签名密钥
         */
        if (StringUtils.isBlank(ContextUtil.getConfig(LoginTokenUtil.CONFIG_TOKEN_SECRET_KEY))) {
            ContextUtil.setConfig(LoginTokenUtil.CONFIG_TOKEN_SECRET_KEY,
                    SimpleEncryptService.getDefault().encryptAsBase64(StringUtils.randomGuid()));
        }
        
        /**
         * 检查系统是否需要初始化
         */
        return getDao().queryAsObject(Long.class, String.format("SELECT %s FROM %s LIMIT 1",
                SystemUserService.getInstance().getFormIdField(), SystemUserService.getInstance().getFormTable()),
                null) != null;
    }
}
