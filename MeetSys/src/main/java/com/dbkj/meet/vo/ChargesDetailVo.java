package com.dbkj.meet.vo;

import java.math.BigDecimal;

/**
 * Created by DELL on 2017/04/06.
 */
public class ChargesDetailVo {

    private Long id;
    /**
     * 公司名称
     */
    private String name;
    /**
     * 账户余额
     */
    private BigDecimal balance;
    /**
     * 套餐名称
     */
    private String pname;
    /**
     * 呼入计费模式
     */
    private Integer callInMode;
    /**
     * 套餐资费说明
     */
    private String description;
    /**
     * 包月方数
     */
    private Long count;

    public Integer getCallInMode() {
        return callInMode;
    }

    public void setCallInMode(Integer callInMode) {
        this.callInMode = callInMode;
    }

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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
