package com.socyno.webfwk.token;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmodel.AbstractUser;
import com.socyno.webbsc.ctxsrv.AbstractSessionInterceptor;
import com.socyno.webfwk.user.SystemUserService;

import lombok.Getter;

public class SessionContextInterceptor extends AbstractSessionInterceptor {
    
    @Getter
    private static final SessionContextInterceptor instance = new SessionContextInterceptor();
    
    public SessionContextInterceptor() {
        super();
    }
    
    public SessionContextInterceptor(String weakValidation) {
        super(weakValidation);
    }
    
    public SessionContextInterceptor(String weakValidation, int allowedExpiredMinites) {
        super(weakValidation, allowedExpiredMinites);
    }
    
    @Override
    protected AbstractUser getAbstractUser(String username) throws Exception {
        return SystemUserService.getInstance().getSimple(username);
    }
    
    @Override
    protected void checkUserAndTokenInvlid(AbstractUser user, String token) throws Exception {
        if (user == null || user.isDisabled() || UserTokenService.checkTokenDiscard(token)) {
            throw new MessageException("不存在用户、已禁用、或令牌已注销");
        }
    }
}
