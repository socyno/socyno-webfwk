package com.socyno.webfwk.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscmodel.R;
import com.socyno.base.bscmodel.SessionContext;
import com.socyno.stateform.authority.Authority;
import com.socyno.stateform.authority.AuthorityScopeType;
import com.socyno.webfwk.menu.SystemMenuService;
import com.socyno.webfwk.todo.SystemTodoService;
import com.socyno.webfwk.todo.SystemTodoSimple;
import com.socyno.webfwk.token.UserTokenService;
import com.socyno.webfwk.user.SystemUserForLogin;
import com.socyno.webfwk.user.SystemUserService;
import com.socyno.webfwk.user.SystemUserToken;

public class UserController {
    
    @ResponseBody
    @Authority(AuthorityScopeType.Guest)
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public R login(@RequestBody SystemUserForLogin loginInfo, HttpServletResponse resp) throws Exception {
        SystemUserToken userToken;
        if ((userToken = SystemUserService.getInstance().login(loginInfo)) == null) {
            throw new MessageException("账户或密码错误");
        }
        for (Cookie cookie : userToken.getCookies()) {
            resp.addCookie(cookie);
        }
        return R.ok().setData(userToken);
    }

    @ResponseBody
    @Authority(AuthorityScopeType.Guest)
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public R logout(HttpServletRequest request) throws Exception {
        String token;
        if (StringUtils.isNotBlank(token = request.getHeader(UserTokenService.getTokenHeader()))) {
            UserTokenService.markTokenDiscard(token);
        }
        return R.ok();
    }

    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/menus", method = RequestMethod.GET)
    public R getMenus(HttpServletRequest request) throws Exception {
        if (!SessionContext.hasUserSession()) {
            return R.ok();
        }
        return R.ok().setData(SystemMenuService.getInstance().getMyMenuTree());
    }

    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/todo/opened/list", method = RequestMethod.GET)
    public R getOpenedTodoList() throws Exception {
        return R.ok().setData(SystemTodoService.getInstance()
                .queryOpenedByAssignee(SystemTodoSimple.class, SessionContext.getUserId(), 1L, 100).getList());
    }

    @ResponseBody
    @Authority(AuthorityScopeType.System)
    @RequestMapping(value = "/todo/opened/total", method = RequestMethod.GET)
    public R getOpenedTodoTotal() throws Exception {
        return R.ok().setData(SystemTodoService.getInstance().queryOpenedCountByAssignee(SessionContext.getUserId()));
    }
}
