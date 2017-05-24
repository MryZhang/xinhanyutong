package com.dbkj.meet.dic;

/**
 * Created by DELL on 2017/03/01.
 */
public enum ConfigTypeEnum {
    BALANCE_THRESHOLD(1,"余额阙值");

    ConfigTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

    public static ConfigTypeEnum of(int code){
        for(ConfigTypeEnum item:values()){
            if(code==item.code){
                return item;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
