package com.socyno.webfwk.tenant;

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
@Attributes(title = "租户信息查询")
public class SystemTenantDefaultQuery extends AbstractStateFormQuery {
    
    /**
     * SELECT COUNT(1) FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * SELECT f.* FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     (
         f.code LIKE CONCAT(?, '%')
       OR
         f.name LIKE CONCAT(?, '%')
     )
    */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "是否包括已禁用", position = 1020)
    private boolean disableIncluded = false;
    
    @Attributes(title = "租户编号", description = "多个可以使用逗号、分号或空格分隔")
    private String tenantIdsIn;
    
    public SystemTenantDefaultQuery() {
        super();
    }
    
    public SystemTenantDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        if (!isDisableIncluded()) {
            sqlargs.add(SystemTenantService.STATES.DISABLED.getCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(SystemTenantService.getInstance().getFormStateField()).append(" != ?");
        }
        if (StringUtils.isNotBlank(getTenantIdsIn())) {
            String[] splittedTenantIds = StringUtils.split(getTenantIdsIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedTenantIds.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedTenantIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedTenantIds.length, ",", "f.id IN (", ") AND f.id > 0"));
            }
            /* 编号列表为空时，确保返回空的结果集 */
            else {
                StringUtils.prependIfNotEmpty(sqlwhere, " AND ").insert(0, "1 = 0");
            }
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
   @Override
   public AbstractSqlStatement prepareSqlTotal() {
       AbstractSqlStatement whereQuery = buildWhereSql();
       return new BasicSqlStatement().setValues(whereQuery.getValues()).setSql(String.format("%s %s",
                       String.format(SQL_QUERY_COUNT, SystemTenantService.getInstance().getFormTable()),
                       whereQuery.getSql()));
   }
   
   @Override
   public AbstractSqlStatement prepareSqlQuery() {
       AbstractSqlStatement whereQuery = buildWhereSql();
       return new BasicSqlStatement().setValues(whereQuery.getValues())
               .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                       String.format(SQL_QUERY_ALL, SystemTenantService.getInstance().getFormTable()),
                       whereQuery.getSql(), getOffset(), getLimit()));
   }
}
