package com.dbkj.meet.dto;

/**
 * 用户分页数据的封装
 * Created by MrQin on 2016/11/8.
 */
public class UserPage extends BaseUser{

    private String type;//账号类型
    private String company;//所属公司名称
    private String callNum;//呼叫号码

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCallNum() {
        return callNum;
    }

    public void setCallNum(String callNum) {
        this.callNum = callNum;
    }
}
