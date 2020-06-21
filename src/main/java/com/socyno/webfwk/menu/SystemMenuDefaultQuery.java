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
@Attributes(title = "系统菜单查询")
public class SystemMenuDefaultQuery extends AbstractStateFormQuery {
    
    /**
     * SELECT
     *     m.*,
     *     d.name AS dir_name,
     *     p.name AS pane_name
     * FROM
     *     system_menu m
     * LEFT JOIN system_menu_dir d
     *     ON m.dir_id = d.id
     * INNER JOIN system_menu_pane p
     *     ON d.pane_id = p.id
     */
    @Multiline
    private static final String SQL_QUERY_ALL = "X";
    
    /**
     * SELECT
     *     COUNT(1) 
     * FROM
     *     system_menu m
     * LEFT JOIN system_menu_dir d
     *     ON m.dir_id = d.id
     * INNER JOIN system_menu_pane p
     *     ON d.pane_id = p.id
     */
    @Multiline
    private static final String SQL_QUERY_COUNT = "X";
    
    /**
     * (
     *         m.name LIKE CONCAT('%', ?, '%')
     *     OR
     *         d.name LIKE CONCAT('%', ?, '%')
     *     OR
     *         p.name LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "菜单编号", description = "多个可以使用逗号、分号或空格分隔")
    private String menuIdsIn;
    
    public SystemMenuDefaultQuery() {
        super();
    }
    
    public SystemMenuDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        
        if (StringUtils.isNotBlank(getMenuIdsIn())) {
            String[] splittedMenuIds = StringUtils.split(getMenuIdsIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedMenuIds.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedMenuIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedMenuIds.length, ",", "m.id IN (", ") AND m.id > 0"));
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
                .setSql(String.format("%s %s ORDER BY p.`order`, d.`order`, m.`order` LIMIT %s, %s",
                        SQL_QUERY_ALL, whereQuery.getSql(), getOffset(), getLimit()));
    }
}
