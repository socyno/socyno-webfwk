package com.socyno.webfwk.feature;

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
@Attributes(title = "系统功能查询", description="通用表单默认查询界面模型")
public class SystemFeatureDefaultQuery extends AbstractStateFormQuery{
    
    /**
     * SELECT f.* FROM system_feature f
     */
    @Multiline
    private static final String SQL_QUERY_ALL_FEATURES = "X";
    
    /**
     * SELECT COUNT(1) FROM system_feature f
     */
    @Multiline
    private static final String SQL_QUERY_COUNT_FEATURES = "X";
    
    /** (
     *     f.name LIKE CONCAT('%', ?, '%')
     * OR
     *     f.description LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_NAMELIKE_FEATURES_TMPL = "X";
    
    /** EXISTS (
     *     SELECT
     *          a.feature_id
     *     FROM
     *          system_feature_auth a
     *     WHERE
     *          a.feature_id = f.id
     *     AND
     *          a.auth_key LIKE CONCAT('%', ?, '%')
     * )
     */
    @Multiline
    private static final String SQL_QUERY_AUTHKEY_FEATURES_TMPL = "X";
    
    /**
     *  EXISTS (
     *     SELECT
     *          tf.feature_id
     *     FROM
     *          system_tenant_feature tf,
     *          system_tenant t
     *     WHERE
     *          tf.feature_id = f.id
     *     AND
     *          tf.tenant_id = t.id
     *     AND
     *          t.code = ?
     * )
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES_TMPL = "X";
    
    /**
     *  EXISTS (
     *     SELECT
     *          tf.feature_id
     *     FROM
     *          system_tenant_feature tf
     *     WHERE
     *          tf.feature_id = f.id
     *     AND
     *          tf.tenant_id = ?
     * )
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_FEATURES_TMPLID = "X";
    
    @Attributes(title = "关键字", position = 1010)
    private String nameLike;
    
    @Attributes(title = "授权标识", position = 1020)
    private String authKeyLike;
    
    @Attributes(title = "租户代码", position = 1030)
    private String tenantCode;
    
    @Attributes(title = "租户编号", position = 1040)
    private Long tenantId;
    
    @Attributes(title = "功能编号", description = "多个可用逗号或分号分隔")
    private String featureIdsIn;
    
    public SystemFeatureDefaultQuery() {
        super();
    }
    
    public SystemFeatureDefaultQuery(Integer limit, Long page) {
        super(limit, page);
    }
    
    private AbstractSqlStatement buildWhereSql() {
        StringBuilder sqlwhere = new StringBuilder();
        List<Object> sqlargs = new ArrayList<>();
        if (StringUtils.isNotBlank(getNameLike())) {
            sqlargs.add(getNameLike());
            sqlargs.add(getNameLike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_NAMELIKE_FEATURES_TMPL);
        }
        if (StringUtils.isNotBlank(getAuthKeyLike())) {
            sqlargs.add(getAuthKeyLike());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_AUTHKEY_FEATURES_TMPL);
        }
        if (StringUtils.isNotBlank(getFeatureIdsIn())) {
            String[] splittedFeatureIds = StringUtils.split(getFeatureIdsIn(), "[,;\\s]+",
                    StringUtils.STR_NONBLANK | StringUtils.STR_TRIMED | StringUtils.STR_UNIQUE);
            if (splittedFeatureIds.length > 0) {
                sqlargs.addAll(Arrays.asList(splittedFeatureIds));
                StringUtils.appendIfNotEmpty(sqlwhere, " AND ")
                        .append(StringUtils.join("?", splittedFeatureIds.length, ",", "f.id IN (", ") AND f.id > 0"));
            }
            /* 编号列表为空时，确保返回空的结果集 */
            else {
                StringUtils.prependIfNotEmpty(sqlwhere, " AND ").insert(0, "1 = 0");
            }
        }
        if (getTenantId() != null) {
            sqlargs.add(getTenantId());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_TENANT_FEATURES_TMPLID);
        }
        if (StringUtils.isNotBlank(getTenantCode())) {
            sqlargs.add(getTenantCode());
            StringUtils.appendIfNotEmpty(sqlwhere, " AND ").append(SQL_QUERY_TENANT_FEATURES_TMPL);
        }
        return new BasicSqlStatement().setValues(sqlargs.toArray())
                .setSql(StringUtils.prependIfNotEmpty(sqlwhere, "WHERE ").toString());
    }
    
    @Override
    public AbstractSqlStatement prepareSqlTotal() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s", SQL_QUERY_COUNT_FEATURES, whereQuery.getSql()));
    }
    
    @Override
    public AbstractSqlStatement prepareSqlQuery() {
        AbstractSqlStatement whereQuery = buildWhereSql();
        return new BasicSqlStatement().setValues(whereQuery.getValues())
                .setSql(String.format("%s %s ORDER BY f.id DESC LIMIT %s, %s",
                            SQL_QUERY_ALL_FEATURES,
                            whereQuery.getSql(), getOffset(), getLimit()));
    }
}
