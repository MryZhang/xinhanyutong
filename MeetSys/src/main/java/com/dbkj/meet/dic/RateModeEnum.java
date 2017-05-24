package com.dbkj.meet.dic;

/**
 * Created by DELL on 2017/02/25.
 */
public enum RateModeEnum {
    MINUTE(1,"分钟计费"),
    MONTH(2,"包月计费");

    RateModeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private int code;
    private String desc;

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

    public static RateModeEnum codeOf(int code){
        for(RateModeEnum item:values()){
            if(item.code==code){
                return item;
            }
        }
        return null;
    }
}
