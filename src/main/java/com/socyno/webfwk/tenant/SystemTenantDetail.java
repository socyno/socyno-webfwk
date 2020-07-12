package com.socyno.webfwk.tenant;

import java.util.List;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscfield.FieldTableView;
import com.socyno.webbsc.model.SystemTenantDbInfoWithId;
import com.socyno.webfwk.feature.SystemFeatureOption;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "租户详情信息")
public class SystemTenantDetail extends SystemTenantSimple implements SystemTenantWithFeatures, SystemTenantWithDbInfos {
    @Attributes(title = "授权功能", type = FieldTableView.class)
    private List<SystemFeatureOption> features;
    
    @Attributes(title = "数据连接", type = FieldTableView.class)
    private List<SystemTenantDbInfoWithId> databases;
}
