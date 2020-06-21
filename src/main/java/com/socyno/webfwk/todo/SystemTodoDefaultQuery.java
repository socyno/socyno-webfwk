package com.socyno.webfwk.todo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.socyno.base.bscfield.*;
import com.socyno.base.bscmixutil.StringUtils;
import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscsqlutil.AbstractSqlStatement;
import com.socyno.base.bscsqlutil.BasicSqlStatement;
import com.socyno.stateform.abs.AbstractStateFormQuery;
import com.socyno.webfwk.user.FieldSystemUser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "待办实现查询")
public class SystemTodoDefaultQuery extends AbstractStateFormQuery {
    
    @Attributes(title = "状态", position = 10, type = SystemTodoSimple.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "起始创建日期(包括)", position = 20, type = FieldDateOnly.class)
    private Date createdAtBegin;
    
    @Attributes(title = "结束创建日期(不包括)", position = 30, type = FieldDateOnly.class)
    private Date createdAtEnd;
    
    @Attributes(title = "类型", position = 40)
    private String category;
    
    @Attributes(title = "审批人包含", position = 50, type = FieldSystemUser.class)
    private Long assignee;
    
    @Attributes(title = "流程单编号")
    private String targetId;
    
    @Attributes(title = "最终审批人", position = 60, type = FieldSystemUser.class)
    private Long closedUserId;
    
    @Attributes(title = "待办项标识")
    private String targetKey;

    @Attributes(title = "待办流程发起人", position = 70, type = FieldSystemUser.class)
    private Long applyUserId ;
    
    public SystemTodoDefaultQuery() {
        super();
    }
    
    public SystemTodoDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    /**
     * SELECT f.* FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_ALL_TODOS = "X";
    
    /**
     * SELECT COUNT(1) FROM %s f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_TODOS = "X";
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlwhere = new StringBuilder();
        if (StringUtils.isNotBlank(getCategory())) {
            sqlargs.add(getCategory());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.category = ?");
        }
        if (StringUtils.isNotBlank(getTargetId())) {
            sqlargs.add(getTargetId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.target_id = ?");
        }
        if (StringUtils.isNotBlank(getTargetKey())) {
            sqlargs.add(getTargetKey());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.target_key = ?");
        }
        if (StringUtils.isNotBlank(getState())) {
            sqlargs.add(getState());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                    .append(String.format("f.%s = ?", SystemTodoService.getInstance().getFormStateField()));
        }
        if (getClosedUserId() != null) {
            sqlargs.add(getClosedUserId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.closed_user_id = ?");
        }
        if (getCreatedAtEnd() != null) {
            sqlargs.add(DateFormatUtils.format(getCreatedAtEnd(), "yyyy-MM-dd 24:00:00"));
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.created_at < ?");
        }
        if (getCreatedAtBegin() != null) {
            sqlargs.add(DateFormatUtils.format(getCreatedAtBegin(), "yyyy-MM-dd 00:00:00"));
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.created_at >= ?");
        }
        if (getApplyUserId() != null){
            sqlargs.add(getApplyUserId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append("f.apply_user_id = ?");
        }
        if (getAssignee() != null) {
            sqlargs.add(getAssignee());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(
                    "EXISTS (SELECT a.todo_id FROM system_common_todo_assignee a WHERE a.todo_id = f.id AND a.todo_user = ?)");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray()).setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s",
                        String.format(SQL_QUERY_COUNT_TODOS, SystemTodoService.getInstance().getFormTable()),
                        whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL_TODOS, SystemTodoService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
