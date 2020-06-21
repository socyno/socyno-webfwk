package com.socyno.webfwk.tenant;

import java.util.Collections;
import java.util.List;

import org.adrianwalker.multilinestring.Multiline;

import com.socyno.base.bscmixutil.ConvertUtil;
import com.socyno.base.bscmixutil.StringUtils;
import com.socyno.stateform.field.FieldAbstractKeyword;
import com.socyno.stateform.field.FilterBasicKeyword;

import lombok.Getter;

public class FieldSystemTenant extends  FieldAbstractKeyword<FilterBasicKeyword> {
    
    @Getter
    private static final FieldSystemTenant instance = new FieldSystemTenant();
    
    public FieldOptionsType getOptionsType() {
        return FieldOptionsType.DYNAMIC;
    }
    
    /**
     *  SELECT
     *     t.id,
     *     t.code,
     *     t.name
     *  FROM
     *      system_tenant t
     * 
     */
    @Multiline
    private static final String SQL_QUERY_TENANT_OPTIONS = "X";
    
    /**
     *  ORDER BY
     *      t.code ASC
     * 
     */
    @Multiline
    private static final String SQL_ORDER_TENANT_OPTIONS = "X";
    
    /**
     * 覆盖父类的方法，根据关键字检索系统租户
     */
    @Override
    public List<SystemTenantOption> queryDynamicOptions(FilterBasicKeyword filter) throws Exception {
        
        return SystemTenantService.getInstance().listForm(SystemTenantOption.class,
                new SystemTenantDefaultQuery(50, 1L).setNameLike(filter.getKeyword())).getList();
    }
    
    /**
     * 覆盖父类的方法，根据选项值检索系统租户
     */
    @Override
    public List<SystemTenantOption> queryDynamicValues(Object[] values) throws Exception {
        Long[] ids;
        if (values == null || values.length <= 0
                || (ids = ConvertUtil.asNonNullUniqueLongArray((Object[]) values)).length <= 0) {
            return Collections.emptyList();
        }
        
        return SystemTenantService.getInstance()
                .listForm(SystemTenantOption.class,
                        new SystemTenantDefaultQuery(ids.length, 1L).setTenantIdsIn(StringUtils.join(ids, ',')))
                .getList();
    }
}
