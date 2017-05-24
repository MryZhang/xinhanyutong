package com.dbkj.meet.vo;

/**
 * Created by DELL on 2017/04/14.
 */
public class AdminUserVo {

    private Long id;
    private String username;
    private Integer cid;
    private String password;
    private String encryptPassword;
    private String confirmPwd;
    private String encryptConfirmPwd;
    private Integer eid;
    private Integer aid;

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

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }

    public String getEncryptConfirmPwd() {
        return encryptConfirmPwd;
    }

    public void setEncryptConfirmPwd(String encryptConfirmPwd) {
        this.encryptConfirmPwd = encryptConfirmPwd;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    @Override
    public String toString() {
        return "AdminUserVo{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", cid=" + cid +
                ", password='" + password + '\'' +
                ", encryptPassword='" + encryptPassword + '\'' +
                ", confirmPwd='" + confirmPwd + '\'' +
                ", encryptConfirmPwd='" + encryptConfirmPwd + '\'' +
                ", eid=" + eid +
                ", aid=" + aid +
                '}';
    }

    public Integer getAid() {
        return aid;
    }

    public void setAid(Integer aid) {
        this.aid = aid;
    }
}
