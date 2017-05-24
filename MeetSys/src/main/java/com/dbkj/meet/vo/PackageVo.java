package com.dbkj.meet.vo;

import java.math.BigDecimal;

/**
 * Created by DELL on 2017/04/02.
 */
public class PackageVo {

    /**
     * 套餐名称
     */
   private String name;
    /**
     * 呼入计费模式
     * @see com.dbkj.meet.dic.RateModeEnum
     */
   private Integer callInMode;
    /**
     * 呼入费率
     */
   private BigDecimal callInRate;
    /**
     * 如果计费模式为包月，值为超方费率
     * 否则为null
     */
   private BigDecimal passRate;
    /**
     * 呼出计费模式，目前默认为分钟计费
     */
   private Integer callOutMode;
    /**
     * 呼出费率
     */
   private BigDecimal callOutRate;
    /**
     * 短信费率
     */
   private BigDecimal smsRate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCallInMode() {
        return callInMode;
    }

    public void setCallInMode(Integer callInMode) {
        this.callInMode = callInMode;
    }

    public BigDecimal getCallInRate() {
        return callInRate;
    }

    public void setCallInRate(BigDecimal callInRate) {
        this.callInRate = callInRate;
    }

    public BigDecimal getPassRate() {
        return passRate;
    }

    public void setPassRate(BigDecimal passRate) {
        this.passRate = passRate;
    }

    public Integer getCallOutMode() {
        return callOutMode;
    }

    public void setCallOutMode(Integer callOutMode) {
        this.callOutMode = callOutMode;
    }

    public BigDecimal getCallOutRate() {
        return callOutRate;
    }

    public void setCallOutRate(BigDecimal callOutRate) {
        this.callOutRate = callOutRate;
    }

    public BigDecimal getSmsRate() {
        return smsRate;
    }

    public void setSmsRate(BigDecimal smsRate) {
        this.smsRate = smsRate;
    }

    @Override
    public String toString() {
        return "PackageVo{" +
                "name='" + name + '\'' +
                ", callInMode=" + callInMode +
                ", callInRate=" + callInRate +
                ", passRate=" + passRate +
                ", callOutMode=" + callOutMode +
                ", callOutRate=" + callOutRate +
                ", smsRate=" + smsRate +
                '}';
    }
}
