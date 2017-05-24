package com.dbkj.meet.dic;

/**
 *预约会议类型
 * Created by MrQin on 2016/11/16.
 */
public enum RepeatType {
    NONE(0,"无重复"),
    DAY(1,"天"),
    WEEK(2,"星期"),
    MONTH(3,"月"),
    FIEXD(4,"固定会议")
    ;
    RepeatType(int code, String text) {
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
