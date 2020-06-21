package com.socyno.webfwk.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscsqlutil.AbstractSqlStatement;
import com.socyno.base.bscsqlutil.BasicSqlStatement;
import com.socyno.stateform.abs.AbstractStateFormQuery;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "系统角色查询")
public class SystemRoleDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "角色代码")
    private String roleCode;
    
    @Attributes(title = "角色编号列表", description = "多个可使用逗号、分号或空格分隔")
    private String roleIdsIn;
    
    @Attributes(title = "角色代码列表", description = "多个可使用逗号、分号或空格分隔")
    private String roleCodesIn;
    
    public SystemRoleDefaultQuery() {
        super();
    }
    
    public SystemRoleDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    /**
     * SELECT f.* FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     * SELECT  COUNT(1) FROM  %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * (
     *     f.code LIKE CONCAT('%', ?, '%')
     * OR
     *     f.name LIKE CONCAT('%', ?, '%')
     * OR
     *     f.description LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlwhere = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        if (StringUtils.isNotBlank(getRoleIdsIn())) {
            String[] splittedRoleIds = StringUtils.split(getRoleIdsIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedRoleIds.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedRoleIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedRoleIds.length, ",", "f.id IN (", ") AND f.id > 0"));
            }
            /* 编号列表为空时，确保返回空的结果集 */
            else {
                StringUtils.prependIfNotEmpty(sqlwhere, " AND ").insert(0, "1 = 0");
            }
        }
        if (StringUtils.isNotBlank(getRoleCodesIn())) {
            String[] splittedRoleCodes = StringUtils.split(getRoleCodesIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedRoleCodes.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedRoleCodes));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedRoleCodes.length, ",", "f.code IN (", ")"));
            }
            /* 编号列表为空时，确保返回空的结果集 */
            else {
                StringUtils.prependIfNotEmpty(sqlwhere, " AND ").insert(0, "1 = 0");
            }
        }
        if (StringUtils.isNotBlank(getRoleCode())) {
            sqlargs.add(getRoleCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.code = ?");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                String.format(SQL_QUERY_COUNT, SystemRoleService.getInstance().getFormTable()), whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL, SystemRoleService.getInstance().getFormTable()), whereQuery.getSql(),
                        getOffset(), getLimit()));
    }
}
