package com.dbkj.meet.vo;

/**
 * Created by DELL on 2017/05/23.
 */
public class SmtpEmailVO {

    private Long id;
    private String email;
    private String host;
    private String password;
    private String encryptPwd;
    private String confirmPassword;
    private String encryptConfirmPwd;

    public SmtpEmailVO() {
    }

    public SmtpEmailVO(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public SmtpEmailVO(Long id, String email, String password, String confirmPassword) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "SmtpEmailVO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", confirmPassword='" + confirmPassword + '\'' +
                '}';
    }
}
