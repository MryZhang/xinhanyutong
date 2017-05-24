package com.dbkj.meet.dto;

/**
 * 封装个人联系人信息
 * Created by MrQin on 2016/11/21.
 */
public class ContactInfo extends BaseContact{

    private Long groupId;
    private String email;
    private String remark;
    private Long uid;


    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

}
