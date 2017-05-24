package com.dbkj.meet.dto;

/**
 * Created by MrQin on 2016/11/24.
 */
public class BaseContact {

    private Long id;//联系电话的id
    private String name;//联系人姓名
    private String phone;//联系人号码
    private Long pid;//联系人id

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }
}
