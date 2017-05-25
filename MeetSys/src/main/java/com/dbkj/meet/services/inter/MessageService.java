package com.dbkj.meet.services.inter;

import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.model.Record;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by DELL on 2017/03/23.
 */
public interface MessageService {

    /**
     * 发送短信
     * @param
     */
    void sendNotice(HttpServletRequest request);

    /**
     * 向参会人发送通知短信(即时会议使用)
     * @param rid 会议记录id
     * @param phone
     */
    void sendMsg(Long rid,String phone);

    /**
     * 向参会人发送通知短信（预约会议使用）
     * @param orid 预约会议记录id
     * @param phone
     */
    void sendSms(Long orid,String phone);

    /**
     * 向参会人发送通知短信（预约会议使用）
     * @param orderMeet 预约会议记录
     * @param phone
     */
    void sendSms(OrderMeet orderMeet,String phone);
}
