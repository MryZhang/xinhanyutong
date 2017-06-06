package com.dbkj.meet.services.inter;

import com.dbkj.meet.model.OrderMeet;

/**
 * Created by DELL on 2017/05/25.
 */
public interface IOrderTimeService {

    /**
     * 获取预约会议开始时间
     * @param orderMeet
     * @return
     */
    String getOrderMeetStartTime(OrderMeet orderMeet);

    /**
     * 获取下次预约会议开始的时间
     * @param orderMeet
     * @return
     */
    String getNextStartTime(OrderMeet orderMeet);
}
