package com.socyno.webfwk.user;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SystemUserToken {
    
    private String tokenHeader;
    
    private String tokenContent;
    
    final private List<Cookie> cookies = new ArrayList<>();
}
