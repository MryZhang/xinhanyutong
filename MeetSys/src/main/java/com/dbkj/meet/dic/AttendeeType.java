package com.dbkj.meet.dic;

/**
 * 参会人类型
 * Created by MrQin on 2016/11/17.
 */
public enum AttendeeType {
    HOST(1,"主持人"),
    ATTENDEE(2,"参会人")
    ;

    AttendeeType(int code, String text) {
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

    public static AttendeeType valueOf(int code){
        for(AttendeeType attendeeType:values()){
            if(attendeeType.getCode()==code){
                return attendeeType;
            }
        }
        return null;
    }
}
