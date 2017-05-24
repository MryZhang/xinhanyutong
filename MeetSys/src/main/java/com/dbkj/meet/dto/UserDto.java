package com.dbkj.meet.dto;

/**
 * 封装添加或修改用户时的数据
 * Created by MrQin on 2016/11/8.
 */
public class UserDto extends BaseUser{

    private String confirm;//确认密码
    private Long cid;//公司Id

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }
}
