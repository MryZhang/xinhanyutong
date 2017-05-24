package com.dbkj.meet.dto;


/**
 * 封装用户数据
 * Created by MrQin on 2016/11/23.
 */
public class UserData extends BaseUser{

    private String confirmPwd;
    private String encryptPwd;
    private String encryptConfirmPwd;

    private Long eid;
    private String name;
    private String email;
    private Integer did;
    private String position;

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }

    public Long getEid() {
        return eid;
    }

    public void setEid(Long eid) {
        this.eid = eid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getDid() {
        return did;
    }

    public void setDid(Integer did) {
        this.did = did;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getEncryptPwd() {
        return encryptPwd;
    }

    public void setEncryptPwd(String encryptPwd) {
        this.encryptPwd = encryptPwd;
    }

    public String getEncryptConfirmPwd() {
        return encryptConfirmPwd;
    }

    public void setEncryptConfirmPwd(String encryptConfirmPwd) {
        this.encryptConfirmPwd = encryptConfirmPwd;
    }
}
