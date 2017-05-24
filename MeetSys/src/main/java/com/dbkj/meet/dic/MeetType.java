package com.dbkj.meet.dic;

/**
 * 会议类型
 * Created by MrQin on 2016/11/17.
 */
public enum MeetType {
    NORMAL_MEET(1,"即时会议"),
    ORDER_MEET(2,"预约会议"),
    FIXED_MEET(3,"固定会议")
    ;

    MeetType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    private int code;
    private String text;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
