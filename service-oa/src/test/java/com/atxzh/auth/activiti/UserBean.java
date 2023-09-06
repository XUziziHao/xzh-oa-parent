package com.atxzh.auth.activiti;

import org.springframework.stereotype.Component;

@Component
public class UserBean {

    public String getUsername(int id) {
        if(id == 1) {
            return "xzh";
        }
        if(id == 2) {
            return "wmy";
        }
        return "admin";
    }
}
