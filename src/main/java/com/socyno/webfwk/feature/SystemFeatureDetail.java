package com.socyno.webfwk.feature;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.stateform.field.FieldSystemAuths;
import com.socyno.webbsc.authority.AuthorityEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统功能详情")
public class SystemFeatureDetail extends SystemFeatureSimple implements SystemFeatureWithAuths {
    
    @Attributes(title = "接口/操作", type = FieldSystemAuths.class)
    private List<AuthorityEntity> auths;
        
}
