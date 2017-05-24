package com.dbkj.meet.dic;

/**
 * Created by DELL on 2017/03/02.
 */
public enum CallModeEnum {
    CALL_MODE_CALL(8000,"普通通话"),
    CALL_MODE_MEET(8001,"会议");

    CallModeEnum(int code, String desc) {
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

    public static CallModeEnum codeOf(int code){
        for(CallModeEnum mode:values()){
            return mode;
        }
        return null;
    }
}
