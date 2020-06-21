package com.socyno.webfwk.feature;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscfield.FieldText;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.stateform.field.FieldSystemAuths;
import com.socyno.stateform.field.OptionSystemAuth;
import com.socyno.webfwk.feature.SystemFeatureSimple.FieldOptionsState;

import lombok.Data;

@Data
@Attributes(title = "编辑系统功能")
public class SystemFeatureForEdition implements AbstractStateForm {
    
    @Attributes(title = "编号", readonly = true)
    private Long id;
    
    @Attributes(title = "状态", readonly = true, type = FieldOptionsState.class)
    private String state;
    
    @Attributes(title = "版本", readonly = true)
    private Long revision;
    
    @Attributes(title = "名称", position = 1020, required = true)
    private String name;
    
    @Attributes(title = "描述", position = 1030, type = FieldText.class)
    private String description;
    
    @Attributes(title = "接口/操作", position = 1040, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
