package com.socyno.webfwk.menu;

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

@Setter
@Getter
@ToString
@Accessors(chain = true)
@Attributes(title = "菜单面板查询")
public class SystemMenuPaneDefaultQuery extends AbstractStateFormQuery {
    
    /**
     * SELECT p.* FROM system_menu_pane p
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     * SELECT COUNT(1)  FROM system_menu_pane p
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    public SystemMenuPaneDefaultQuery () {
        super();
    }
    
    public SystemMenuPaneDefaultQuery (Integer limit, Long page) {
        super(limit, page);
    }
    
    @Attributes(title = "关键字", position = 1100)
    private String nameLike;
    
    @Attributes(title = "面板编号", description = "多个使用逗号、分号或空格分隔", position = 1200)
    private String menuPaneIdsIn;
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(nameLike);
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("p.name LIKE CONCAT('%', ?, '%')");
        }
        
        if (StringUtils.isNotBlank(getMenuPaneIdsIn())) {
            String[] splittedMenuPaneIds = StringUtils.split(getMenuPaneIdsIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedMenuPaneIds.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedMenuPaneIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedMenuPaneIds.length, ",", "p.id IN (", ") AND p.id > 0"));
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
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s", SQL_QUERY_COUNT, whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY p.`order` LIMIT %s, %s",
                        SQL_QUERY_ALL, whereQuery.getSql(), getOffset(), getLimit()));
    }
}
