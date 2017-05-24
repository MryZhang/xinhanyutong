package com.dbkj.meet.vo;

/**
 * Created by DELL on 2017/03/01.
 */
public class AmountVo {

    public AmountVo(){

    }


    /**
     * 账户余额
     */
    private double balance;
    /**
     * 计费方式
     */
    private String mode;

    /**
     * 如果计费方式为包月，则为包月方数
     */
    private Integer count;

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
