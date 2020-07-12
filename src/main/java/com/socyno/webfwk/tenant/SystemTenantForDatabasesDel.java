package com.socyno.webfwk.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.webbsc.model.SystemTenantDbInfo.FieldOptionsName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "移除租户数据库")
public class SystemTenantForDatabasesDel implements AbstractStateForm {
    @Attributes(title = "编号", position = -1)
    private Long id;
    
    @Attributes(title = "版本", position = -1)
    private Long revision;
    
    @Attributes(title = "状态", position = -1)
    private String state;
    
    @Attributes(title = "数据库", position = 1000, required = true, type = FieldOptionsName.class)
    private List<String> databasesRemoved;
}
