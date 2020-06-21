package com.socyno.webfwk.notify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.base.bscsqlutil.AbstractSqlStatement;
import com.socyno.base.bscsqlutil.BasicSqlStatement;
import com.socyno.base.bscfield.FieldDateOnly;
import com.socyno.stateform.abs.AbstractStateFormQuery;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@Attributes(title = "通知模板查询")
public class SystemNotifyRecordDefaultQuery extends AbstractStateFormQuery {
    
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
     * (
     *    f.message_to LIKE CONCAT('%', ?, '%')
     *  OR
     *    f.message_cc LIKE CONCAT('%', ?, '%')
     *  OR
     *    f.content LIKE CONCAT('%', ?, '%')
     * )
    */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_TMPL = "X";
    
    
    @Attributes(title = "结果", position = 1010, type = SystemNotifyRecordSimple.FieldOptionsResult.class)
    private String result;
    
    @Attributes(title = "起始创建日期", position = 1020, type = FieldDateOnly.class)
    private Date createdAtBegin;
    
    @Attributes(title = "结束创建日期", position = 1030, type = FieldDateOnly.class)
    private Date createdAtEnd;
    
    @Attributes(title = "类型", position = 1110, type = SystemNotifyRecordSimple.FieldOptionsResult.class)
    private String type;
    
    @Attributes(title = "状态", position = 1120, type = SystemNotifyRecordSimple.FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "关键字", position = 1130)
    private String keyword;
    
    public SystemNotifyRecordDefaultQuery() {
        super();
    }
    
    public SystemNotifyRecordDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        List<Object> sqlargs = new ArrayList<>();
        StringBuilder sqlstmt = new StringBuilder();
        if (StringUtils.isNotBlank(type)) {
            sqlargs.add(type);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(" f.type = ?");
        }
        if (StringUtils.isNotBlank(state)) {
            sqlargs.add(state);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ")
                    .append(String.format(" f.%s = ?", SystemNotifyRecordService.getInstance().getFormStateField()));
        }
        if (StringUtils.isNotBlank(keyword)) {
            sqlargs.add(keyword);
            sqlargs.add(keyword);
            sqlargs.add(keyword);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(SQL_QUERY_NAMELIKE_TMPL);
        }
        if (StringUtils.isNotBlank(result)) {
            sqlargs.add(result);
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(" f.result = ?");
        }
        if (createdAtEnd != null) {
            sqlargs.add(DateFormatUtils.format(createdAtEnd, "yyyy-MM-dd 24:00:00"));
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(" f.created_at < ?");
        }
        if (createdAtBegin != null) {
            sqlargs.add(DateFormatUtils.format(createdAtBegin, "yyyy-MM-dd 00:00:00"));
            StringUtils.appendIfNotEmpty(sqlstmt, " AND ").append(" f.created_at >= ?");
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlstmt, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s",
                        String.format(SQL_QUERY_COUNT, SystemNotifyRecordService.getInstance().getFormTable()),
                        whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                        String.format(SQL_QUERY_ALL, SystemNotifyRecordService.getInstance().getFormTable()),
                        whereQuery.getSql(), getOffset(), getLimit()));
    }
}
