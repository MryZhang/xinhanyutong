package com.dbkj.meet.dic;

/**
 * 会议状态
 * Created by MrQin on 2016/11/7.
 */
public enum MeetState {
    OREDER_RECORD(-1,"预约会议记录"),
    STARTED(0,"会议开始"),
    GOINGON(1,"会议进行中"),
    FINSHED(2,"会议结束")
    ;

    MeetState(int stateCode, String stateText) {
        this.stateCode = stateCode;
        this.stateText = stateText;
    }

    private int stateCode;
    private String stateText;

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String stateText) {
        this.stateText = stateText;
    }

    public static MeetState valueOf(int stateCode){
        for (MeetState state:values()) {
            if(state.getStateCode()==stateCode){
                return state;
            }
        }
        return null;
    }
}
