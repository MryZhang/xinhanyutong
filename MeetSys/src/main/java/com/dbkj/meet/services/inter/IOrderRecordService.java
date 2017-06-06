package com.dbkj.meet.services.inter;

import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.model.Record;
import com.dbkj.meet.model.Schedule;

import java.util.Date;

/**
 *
 * Created by DELL on 2017/06/03.
 */
public interface IOrderRecordService {

    /**
     * 根据条件获取会议记录表中预约会议产生的会议记录
     * @param orderId 预约会议id
     * @param startTime 会议开始时间
     * @return
     */
    Record findOrderRecord(Long orderId, String startTime);

    /**
     * 创建会议记录
     * @param orderId 预约会议id
     * @param startTime 会议开始时间
     * @param record 要创建的会议记录
     * @return
     */
    boolean createRecord(Long orderId,String startTime,Record record);

    /**
     * 创建会议
     * @param orderMeet
     * @param startTime
     * @param record
     * @return
     */
    boolean createRecord(OrderMeet orderMeet,String startTime,Record record);

    /**
     * 创建会议记录
     * @param orderId
     * @param record
     * @return
     */
    boolean createRecord(Long orderId,Record record);


    /**
     * 创建预约会议
     * @param orderMeet
     * @param record
     * @return
     */
    boolean createRecord(OrderMeet orderMeet,Record record);

    /**
     * 获取最近一次预约会议的开始时间
     * @param orderId 预约会议id
     * @param date 当前时间
     * @return
     */
    String getStartTime(Long orderId,Date date);

    /**
     * 获取最近一次预约会议的开始时间
     * @param orderMeet 预约会议记录
     * @param date 当前时间
     * @return
     */
    String getStartTime(OrderMeet orderMeet,Date date);

    /**
     * 获取最近一次预约会议的开始时间
     * @param orderMeet 预约会议记录
     * @param schedule 任务计划
     * @param date 当前时间
     * @return
     */
    String getStartTime(OrderMeet orderMeet, Schedule schedule,Date date);
}
