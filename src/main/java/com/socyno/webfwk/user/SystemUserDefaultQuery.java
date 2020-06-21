package com.socyno.webfwk.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscsqlutil.AbstractSqlStatement;
import com.socyno.base.bscsqlutil.BasicSqlStatement;
import com.socyno.stateform.abs.AbstractStateFormQuery;
import com.socyno.webfwk.role.FieldSystemRole;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统用户查询")
public class SystemUserDefaultQuery extends AbstractStateFormQuery {
    
    /**
     * SELECT COUNT(1) FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_USERS = "X";
    
    /**
     * SELECT f.* FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL_USERS = "X";
    
    /**
     (
         f.username LIKE CONCAT('%', ?, '%')
       OR
         f.display LIKE CONCAT('%', ?, '%')
       OR
         f.mail_address LIKE CONCAT('%', ?, '%')
     )
    */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_USERS_TMPL = "X";
    
    /**
        EXISTS (
            SELECT
                x.user_id
            FROM
                system_user_scope_role x
            WHERE
                f.id = x.user_id
            AND
                x.role_id = ?
            %s
        )
    */
    @Multiline
    private static final String SQL_QUERY_ROLE_USERS_TMPL = "X";
    
    /**
        EXISTS (
           SELECT
               x.user_id
           FROM
               system_user_scope_role x
           WHERE
               f.id = x.user_id
           AND
               x.scope_type = ?
        )
    */
    @Multiline
    private static final String SQL_QUERY_SCOPE_USERS_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1100)
    private String nameLike;
    
    @Attributes(title = "授权范围", position = 1200, type = FilterSystemUserAuth.FieldOptionsScopeType.class)
    private String permScope;
    
    @Attributes(title = "授权角色", position = 1300, type = FieldSystemRole.class)
    private Long permRoleId;
    
    @Attributes(title = "是否包括已禁用", position = 1400)
    private boolean disableIncluded = false;
    
    @Attributes(title = "直属领导", position = 1500, type = FieldSystemUser.class)
    private Long manager;
    
    @Attributes(title = "用户编号列表", description = "多个可使用逗号、分号或空格分隔")
    private String userIdsIn;
    
    @Attributes(title = "用户账户列表", description = "多个可使用逗号、分号或空格分隔")
    private String usernamesIn;
    
    public SystemUserDefaultQuery() {
        super();
    }
    
    public SystemUserDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_NAMELIKE_USERS_TMPL);
        }
        if (!isDisableIncluded()) {
            sqlargs.add(SystemUserService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SystemUserService.getInstance().getFormStateField())
                    .append(" != ?");
        }
        if (getPermRoleId() != null) {
            String scopePlaced = "";
            sqlargs.add(getPermRoleId());
            if (StringUtils.isNotBlank(getPermScope())) {
                sqlargs.add(getPermScope());
                scopePlaced = " AND x.scope_type = ?";
            }
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format(SQL_QUERY_ROLE_USERS_TMPL, scopePlaced));
        } else if (StringUtils.isNotBlank(getPermScope())) {
            sqlargs.add(getPermScope());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_SCOPE_USERS_TMPL);
        }
        if (StringUtils.isNotBlank(getUserIdsIn())) {
            String[] splittedUserIds = StringUtils.split(getUserIdsIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedUserIds.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedUserIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedUserIds.length, ",", "f.id IN (", ") AND f.id > 0"));
            }
            /* 编号列表为空时，确保返回空的结果集 */
            else {
                StringUtils.prependIfNotEmpty(sqlwhere, " AND ").insert(0, "1 = 0");
            }
        }
        if (StringUtils.isNotBlank(getUsernamesIn())) {
            String[] splittedUsernames = StringUtils.split(getUsernamesIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedUsernames.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedUsernames));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedUsernames.length, ",", "f.username IN (", ")"));
            }
            /* 编号列表为空时，确保返回空的结果集 */
            else {
                StringUtils.prependIfNotEmpty(sqlwhere, " AND ").insert(0, "1 = 0");
            }
        }
        if (manager != null) {
            sqlargs.add(manager);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.manager = ?");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                String.format(SQL_QUERY_COUNT_USERS, SystemUserService.getInstance().getFormTable()), whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL_USERS, SystemUserService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
