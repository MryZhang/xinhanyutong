package com.dbkj.meet.dto;

/**
 * 封装User的分页查询条件
 * Created by MrQin on 2016/11/8.
 */
public class UserPageWhere extends BasePageWhere {

    private Long companyId;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
