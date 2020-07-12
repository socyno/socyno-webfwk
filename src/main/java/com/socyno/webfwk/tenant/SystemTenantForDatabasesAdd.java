package com.socyno.webfwk.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscfield.FieldTableView;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.webbsc.model.SystemTenantDbInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "添加租户数据库")
public class SystemTenantForDatabasesAdd implements AbstractStateForm {
    
    public static class FieldDatabaseCreation extends FieldTableView {
        
        @Getter
        private static final FieldDatabaseCreation instance = new FieldDatabaseCreation();
        
        @Override
        public Class<?> getListItemCreationFormClass() {
            return SystemTenantDbInfo.class;
        }
        
    }
    
    @Attributes(title = "编号", position = -1)
    private Long id;
    
    @Attributes(title = "版本", position = -1)
    private Long revision;
    
    @Attributes(title = "状态", position = -1)
    private String state;
    
    @Attributes(title = "数据库", position = 1000, required = true, type = FieldDatabaseCreation.class)
    private List<SystemTenantDbInfo> databasesAdded;
}
