package com.dbkj.meet.vo;

import com.dbkj.meet.dto.BaseUser;

/**
 * Created by DELL on 2017/03/23.
 */
public class UserLoginVo extends BaseUser{

    private String encryptPwd;
    private String authCode;
    private String remember;

    public String getEncryptPwd() {
        return encryptPwd;
    }

    public void setEncryptPwd(String encryptPwd) {
        this.encryptPwd = encryptPwd;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getRemember() {
        return remember;
    }

    public void setRemember(String remember) {
        this.remember = remember;
    }
}
