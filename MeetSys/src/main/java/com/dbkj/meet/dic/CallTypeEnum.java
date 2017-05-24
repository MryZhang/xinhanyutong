package com.dbkj.meet.dic;

/**
 * 计费模式
 * Created by DELL on 2017/02/28.
 */
public enum CallTypeEnum {
    CALL_TYPE_CALLIN(7000,"呼入"),
    CALL_TYPE_CALLOUT(7001,"呼出"),
    CALL_TYPE_MESSAGE(7002,"短信");

    CallTypeEnum(int code, String desc) {
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

    public static CallTypeEnum codeOf(int code){
        for(CallTypeEnum item:values()){
            if(code==item.code){
                return item;
            }
        }
        return null;
    }
}
