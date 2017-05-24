package com.dbkj.meet.dto;

/**
 * Created by MrQin on 2016/11/24.
 */
public class BaseUser {

    private Long id;//用户id
    private String username;//用户名
    private String password;//用户密码

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
