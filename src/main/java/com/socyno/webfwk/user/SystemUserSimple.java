package com.socyno.webfwk.user;

import java.util.Date;

import com.github.reinert.jjschema.Attributes;
import com.socyno.base.bscmodel.AbstractUser;
import com.socyno.stateform.abs.AbstractStateForm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Attributes(title = "系统用户基本信息")
public class SystemUserSimple implements AbstractUser, AbstractStateForm {
    
    @Attributes(title = "编号")
    private Long  id;
    
    @Attributes(title = "状态", type = SystemUserDetail.FieldOptionsState.class)
    private String  state;
    
    @Attributes(title = "账户")
    private String  username;
    
    @Attributes(title = "姓名")
    private String  display;
    
    @Attributes(title = "职务")
    private String  title;

    @Attributes(title = "部门")
    private String  department;
    
    @Attributes(title = "直属领导")
    private Long  manager;
    
    @Attributes(title = "创建时间")
    private Date  createdAt;

    @Attributes(title = "创建人编号")
    private Long  createdBy;

    @Attributes(title = "创建人账户")
    private String  createdCodeBy;

    @Attributes(title = "创建人姓名")
    private String  createdNameBy;
    
    @Attributes(title = "版本")
    private Long  revision;
}
