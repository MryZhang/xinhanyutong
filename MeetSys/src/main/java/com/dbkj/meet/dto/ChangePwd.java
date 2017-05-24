package com.dbkj.meet.dto;

/**
 * Created by MrQin on 2016/11/10.
 */
public class ChangePwd {
    private String oldPassword;
    private String encryptOldPwd;
    private String newPassword;
    private String encryptNewPwd;
    private String confirmPassword;
    private String encryptConfirmPwd;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEncryptOldPwd() {
        return encryptOldPwd;
    }

    public void setEncryptOldPwd(String encryptOldPwd) {
        this.encryptOldPwd = encryptOldPwd;
    }

    public String getEncryptNewPwd() {
        return encryptNewPwd;
    }

    public void setEncryptNewPwd(String encryptNewPwd) {
        this.encryptNewPwd = encryptNewPwd;
    }

    public String getEncryptConfirmPwd() {
        return encryptConfirmPwd;
    }

    public void setEncryptConfirmPwd(String encryptConfirmPwd) {
        this.encryptConfirmPwd = encryptConfirmPwd;
    }
}
