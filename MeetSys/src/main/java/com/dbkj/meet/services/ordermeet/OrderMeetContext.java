package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.services.common.MeetManager;

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
        if(orderMeetType==null){
            throw new RuntimeException("orderMeetType cannot be null");
        }
        OrderMeet orderMeet = orderMeetType.initOrderMeet();
        boolean result = orderMeetType.add();
        if(result){
            OrderModel orderModel=orderMeetType.getOrderModel();
            //发送会议通知
            orderMeetType.notice(orderModel.isSmsNotice(),orderModel.isEmailNotice(),orderModel.isContainHost(),
                    orderMeet.getId(),orderMeetType.getOrderAttendeeList(),orderMeetType.getUser().getId());
            //会议创建成功后，将会议密码添加为已使用的密码中
            MeetManager.getInstance().addUsedPassword(orderModel.getHostPwd());
        }
        return result;
    }
}
