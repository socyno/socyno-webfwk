package com.socyno.webfwk.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.webfwk.feature.FieldSystemFeatureAll;
import com.socyno.webfwk.feature.SystemFeatureOption;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "授予租户功能")
public class SystemTenantForFeaturesAdd implements AbstractStateForm {
    
    @Attributes(title = "编号", position = -1)
    private Long id;
    
    @Attributes(title = "版本", position = -1)
    private Long revision;
    
    @Attributes(title = "状态", position = -1)
    private String state;
    
    @Attributes(title = "功能", position = 1000, required = true, type = FieldSystemFeatureAll.class)
    private List<SystemFeatureOption> featuresAdded;
}
