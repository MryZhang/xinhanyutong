package com.dbkj.meet.dto;

/**
 * 封装公共联系人信息
 * Created by MrQin on 2016/11/24.
 */
public class PubContact extends BaseContact {

    private Integer did;
    private String position;

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
}
