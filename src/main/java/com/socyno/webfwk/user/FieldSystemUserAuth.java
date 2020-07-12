package com.socyno.webfwk.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;

import com.github.reinert.jjschema.v1.FieldOptionsFilter;
import com.socyno.base.bscfield.FieldTableView;
import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.webbsc.service.jdbc.TenantSpecialDataSource;

import lombok.Getter;

public class FieldSystemUserAuth extends FieldTableView {
    
    @Getter
    private final static FieldSystemUserAuth instance = new FieldSystemUserAuth();
    
    @Override
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }

    @Override
    public Class<? extends FieldOptionsFilter> getDynamicFilterFormClass() {
        return FilterSystemUserAuth.class;
    }
    
    /**
        SELECT
            s.scope_type,
            '系统全局'    AS scope_type_name,
            0             AS scope_id,
            ''            AS scope_name,
            r.id          AS role_id,
            r.name        AS role_name,
            s.user_id     AS user_id
        FROM
            system_user_scope_role s
            INNER JOIN system_role r ON r.id = s.role_id 
        WHERE
            s.user_id IN (?)
        AND
            s.scope_type = 'System' 
        AND
            r.id = s.role_id
            
        UNION SELECT
            s.scope_type,
            '业务系统'     AS scope_type_name,
            s.scope_id    AS scope_id,
            l.name        AS scope_name,
            r.id          AS role_id,
            r.name        AS role_name,
            s.user_id     AS user_id
        FROM
            system_user_scope_role s
            INNER JOIN system_role r ON r.id = s.role_id
            INNER JOIN system_subsystem l ON s.scope_id = l.id 
        WHERE
            s.user_id IN (?)
        AND
            s.scope_type = 'Subsystem'
     */
    @Multiline
    private static final String SQL_QUERY_USER_AUTHS_BYUSERID = "X";
    
    /**
        SELECT DISTINCT
            'System'        AS scope_type,
            0               AS scope_id,
            '系统全局'       AS scope_type_name,
            ''              AS scope_name,
            r.id            AS role_id,
            r.name          AS role_name 
        FROM
            system_role r
        WHERE
            r.id = ?
     */
    @Multiline
    private final static String SQL_QUERY_USER_SYSTEM_AUTHS = "X";
    
    /**
        SELECT DISTINCT
            'Subsystem' AS scope_type,
            s.id        AS scope_id,
            '业务系统'  AS scope_type_name,
            s.name      AS scope_name,
            r.id        AS role_id,
            r.name      AS role_name 
        FROM
            system_subsystem s
            INNER JOIN system_role r
        WHERE
            r.id = ?
     */
    @Multiline
    private final static String SQL_QUERY_USER_SUBSYSTEM_AUTHS = "X";
    
    /**
        AND 
            s.name LIKE CONCAT( '%', ?, '%' )
            
     */
    @Multiline
    private final static String SQL_QUERY_USER_SCOPE_KEYWORD_AUTHS = "X";
    
    /**
     * 给定输入关键字，查询可选授权清单
     */
    @Override
    public List<OptionSystemUserAuth> queryDynamicOptions(FieldOptionsFilter cond) throws Exception {
        String scopeType;
        FilterSystemUserAuth filter = (FilterSystemUserAuth) cond;
        if ((scopeType = filter.getScopeType()) == null || filter.getRoleId() == null) {
            return Collections.emptyList();
        }
        String sql = "";
        List<Object> args = new ArrayList<>();
        if (AuthorityScopeType.System.equals(scopeType)) {
            sql = SQL_QUERY_USER_SYSTEM_AUTHS;
        } else if (AuthorityScopeType.Subsystem.equals(scopeType)) {
            sql = SQL_QUERY_USER_SUBSYSTEM_AUTHS;
            if (StringUtils.isNotBlank(filter.getScopeTargetKeyword())) {
                args.add(filter.getScopeTargetKeyword());
                sql = String.format("%s %s", SQL_QUERY_USER_SUBSYSTEM_AUTHS, SQL_QUERY_USER_SCOPE_KEYWORD_AUTHS);
            }
        }
        args.add(0, filter.getRoleId());
        return TenantSpecialDataSource.getMain().queryAsList(OptionSystemUserAuth.class, sql, args.toArray());
    }
    
    /**
     * 给定用户的编号，查询授权清单
     */
    public List<OptionSystemUserAuth> queryByUserIds(Long ...userIds) throws Exception {
        if ((userIds = ConvertUtil.asNonNullUniqueLongArray((Object[])userIds)) == null || userIds.length <= 0) {
            return Collections.emptyList();
        }
        String userIdsPlaceholder = StringUtils.join("?", userIds.length, ",");
        return TenantSpecialDataSource.getMain().queryAsList(OptionSystemUserAuth.class,
                String.format(SQL_QUERY_USER_AUTHS_BYUSERID, userIdsPlaceholder, userIdsPlaceholder),
                ArrayUtils.addAll(userIds, userIds));
    }
}
