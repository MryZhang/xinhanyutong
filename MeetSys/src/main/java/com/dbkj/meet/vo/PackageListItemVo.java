package com.dbkj.meet.vo;

/**
 * Created by DELL on 2017/04/03.
 */
public class PackageListItemVo {

    private Long id;
    private String name;
    private String callInMode;
    private String callInRate;
    private String callOutMode;
    private String callOutRate;
    private String smsRate;

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

    public String getCallInMode() {
        return callInMode;
    }

    public void setCallInMode(String callInMode) {
        this.callInMode = callInMode;
    }

    public String getCallInRate() {
        return callInRate;
    }

    public void setCallInRate(String callInRate) {
        this.callInRate = callInRate;
    }

    public String getCallOutMode() {
        return callOutMode;
    }

    public void setCallOutMode(String callOutMode) {
        this.callOutMode = callOutMode;
    }

    public String getCallOutRate() {
        return callOutRate;
    }

    public void setCallOutRate(String callOutRate) {
        this.callOutRate = callOutRate;
    }

    public String getSmsRate() {
        return smsRate;
    }

    public void setSmsRate(String smsRate) {
        this.smsRate = smsRate;
    }

    @Override
    public String toString() {
        return super.toString()+"[id="+id+";name="+name+";callInMode="+callInMode+";callInRate="
                +callInRate+";callOutMode="+callOutMode+";callOutRate="+callOutRate+";smsRate="+smsRate+"]";
    }

}
