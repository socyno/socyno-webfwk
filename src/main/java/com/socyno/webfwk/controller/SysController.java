package com.socyno.webfwk.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.socyno.base.bscmodel.R;
import com.socyno.webbsc.authority.Authority;
import com.socyno.webbsc.authority.AuthorityScopeType;
import com.socyno.webfwk.initail.SystemInitialConfig;
import com.socyno.webfwk.initail.SystemInitialService;

public class SysController {
    
    @ResponseBody
    @Authority(AuthorityScopeType.Guest)
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public R info() throws Exception {
        return R.ok().setData(SystemInitialService.getInstance().getSysInfo());
    }
    
    @ResponseBody
    @Authority(AuthorityScopeType.Guest)
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public R init(@RequestBody SystemInitialConfig config) throws Exception {
        SystemInitialService.getInstance().initialize(config);
        return R.ok();
    }
}
