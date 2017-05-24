package com.dbkj.meet.services.ordermeet;

/**
 * 预约会议状态上下
 * Created by DELL on 2017/03/27.
 */
public class OrderMeetContext {

    private BaseOrderMeetType orderMeetType;

    public void setOrderMeetType(BaseOrderMeetType orderMeetType){
        this.orderMeetType=orderMeetType;
    }

    public boolean handle(){
        return orderMeetType.add();
    }
}
