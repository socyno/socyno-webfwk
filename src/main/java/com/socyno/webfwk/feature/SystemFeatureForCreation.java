package com.socyno.webfwk.feature;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscfield.FieldText;
import com.socyno.stateform.abs.AbstractStateForm;
import com.socyno.stateform.field.FieldSystemAuths;
import com.socyno.stateform.field.OptionSystemAuth;

import lombok.Data;

@Data
@Attributes(title = "添加系统功能")
public class SystemFeatureForCreation implements AbstractStateForm {
    @Attributes(title = "编号")
    private Long id;
    
    @Attributes(title = "状态")
    private String state;
    
    @Attributes(title = "版本")
    private Long revision;
    
    @Attributes(title = "名称", position = 1100, required = true)
    private String name;
    
    @Attributes(title = "描述", position = 1200, type = FieldText.class)
    private String description;
    
    @Attributes(title = "接口/操作", position = 1300, type = FieldSystemAuths.class)
    private List<OptionSystemAuth> auths;
}
