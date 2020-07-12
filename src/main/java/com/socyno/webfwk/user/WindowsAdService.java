package com.socyno.webfwk.user;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import com.socyno.base.bscexec.MessageException;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.webbsc.ctxutil.ContextUtil;
import com.socyno.webbsc.service.SimpleEncryptService;

import java.util.Hashtable;

@Slf4j
public class WindowsAdService  {
    
    @Data
    public static class SystemWindowsAdUser {
        private String login;
        private String name;
        private String mail;
        private String title;
        private String department;
        private String mobile;
        private String telphone;
        private SystemWindowsAdUser manager;
    }
    
    private static String attr2String(Attribute attr) throws NamingException {
        Object val = null;
        if ( attr == null || (val=attr.get()) == null ) {
            return null;
        }
        return val.toString();
    }
    
    public static boolean equalsDefaultDomain(String domain) {
        return getDefaultDomain().equals(domain);
    }
    
    public static String getDefaultDomain() {
        return getDefaultDomainSuffix().substring(1);
    }
    
    public static String getDefaultDomainSuffix() {
        String suffix = StringUtils.ifBlank(StringUtils.trimToEmpty(
                ContextUtil.getConfig("system.winad.userid.suffix")
            ).toLowerCase(), "@socyno.com");
        if (!suffix.startsWith("@")) {
            suffix = String.format("@%s", suffix);
        }
        return suffix;
    }
    
    public static String[] checkAndReturnFixedUsername(String username) {
        String suffix = getDefaultDomainSuffix();
        if (username != null && !suffix.isEmpty() && username.toLowerCase().endsWith(suffix)) {
            username = username.substring(0, username.length() - suffix.length());
        }
        if (StringUtils.isBlank(username)) {
            throw new MessageException("用户名称不可为空");
        }
        return new String[] { username, username + suffix };
    }
    
    public static SystemWindowsAdUser verifyAdUser(String username, String password) throws Exception {
        SystemWindowsAdUser adUser;
        if ((adUser = getAdUser(username)) == null) {
            return null;
        }
        LdapContext ldapContext = null;
        try {
            if ((ldapContext = getLdapContext(username, password)) == null) {
                return null;
            }
        } finally {
            if (ldapContext != null) {
                ldapContext.close();
            }
        }
        return adUser;
    }
    
    protected static LdapContext getLdapContext(String username, String password) {
        String ldapServiceUrl = ContextUtil.getConfigTrimed("system.winad.service.url");
        String ldapServiceUser = ContextUtil.getConfigTrimed("system.winad.service.username");
        String ldapServicePasswd = ContextUtil.getConfigTrimed("system.winad.service.password");
        if (ldapServiceUrl.isEmpty() || ldapServiceUser.isEmpty() || ldapServicePasswd.isEmpty()) {
            throw new MessageException("域访问配置参数不完整，无法连接域服务器。");
        }
        try {
            if (username != null || password != null) {
                ldapServiceUser = username;
                ldapServicePasswd = password;
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                    throw new MessageException("提供的用户名或密码不正确，无法登陆。");
                }
                String[] tokon = checkAndReturnFixedUsername(username);
                ldapServiceUser = tokon[1];
            } else {
                ldapServicePasswd = new String(SimpleEncryptService.getDefault().decryptFromBase64(ldapServicePasswd),
                        "UTF-8");
            }
            Hashtable<String, String> envi = new Hashtable<String, String>();
            envi.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            envi.put(Context.PROVIDER_URL, ldapServiceUrl);
            envi.put(Context.SECURITY_AUTHENTICATION, "simple");
            envi.put(Context.SECURITY_PRINCIPAL, ldapServiceUser);
            envi.put(Context.SECURITY_CREDENTIALS, ldapServicePasswd);
            return new InitialLdapContext(envi, null);
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        return null;
    }
    
    public static SystemWindowsAdUser getAdUser(String username) {
        return getAdUser(username, false);
    }
    
    protected static SystemWindowsAdUser getAdUser(String username, boolean asDN) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        LdapContext ldapContext;
        if ((ldapContext = getLdapContext(null, null)) == null) {
            return null;
        }
        try {
            String[] userInfo = null;
            String searchFilter = "";
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            SystemWindowsAdUser adUser = new SystemWindowsAdUser();
            if (asDN) {
                searchFilter = "distinguishedName=" + username;
            } else {
                userInfo = checkAndReturnFixedUsername(username);
                searchFilter = "sAMAccountName=" + userInfo[0];
            }
            NamingEnumeration<SearchResult> results;

            /* 没有获取到有用的信息 */
            SearchResult result = null;
            if ((results = ldapContext.search("", searchFilter, searchCtls)) == null
                        || ! results.hasMoreElements()
                        || (result=results.next()) == null ) {
                return null;
            }
            /* 根据LDAP信息，设置用户信息 */
            Attributes attrs = result.getAttributes();
            userInfo = checkAndReturnFixedUsername(attr2String(attrs.get("sAMAccountName")));
            adUser.setName(userInfo[0]);
            adUser.setMail(userInfo[1]);
            adUser.setLogin(userInfo[0]);
            adUser.setMail(attr2String(
                    attrs.get(StringUtils.ifBlank(ContextUtil.getConfigTrimed("system.ldap.attr.mail"), "mail"))));
            adUser.setName(attr2String(attrs
                    .get(StringUtils.ifBlank(ContextUtil.getConfigTrimed("system.ldap.attr.display"), "displayName"))));
            adUser.setTitle(attr2String(
                    attrs.get(StringUtils.ifBlank(ContextUtil.getConfigTrimed("system.ldap.attr.title"), "title"))));
            adUser.setDepartment(attr2String(attrs.get(
                    StringUtils.ifBlank(ContextUtil.getConfigTrimed("system.ldap.attr.department"), "department"))));
            adUser.setTelphone(attr2String(attrs.get(
                    StringUtils.ifBlank(ContextUtil.getConfigTrimed("system.ldap.attr.telphone"), "telephoneNumber"))));
            adUser.setMobile(attr2String(
                    attrs.get(StringUtils.ifBlank(ContextUtil.getConfigTrimed("system.ldap.attr.mobile"), "mobile"))));
            String userDN = attr2String(attrs.get("distinguishedName"));
            String managerDN = attr2String(attrs.get(StringUtils.ifBlank(
                    ContextUtil.getConfigTrimed("system.ldap.attr.manager"), "manager")));
            if (StringUtils.isNotBlank(managerDN) && !StringUtils.equalsIgnoreCase(managerDN, userDN)) {
                adUser.setManager(getAdUser(managerDN, true));
            }
            return adUser;
        } catch (Exception e) {
            log.warn(e.toString(), e);
        } finally {
            try {
                ldapContext.close();
            } catch (NamingException e) {
                
            }
        }
        return null;
    }
}
