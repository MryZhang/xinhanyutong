package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.AttendeeType;
import com.dbkj.meet.model.OrderAttendee;
import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.services.MessageServiceImpl;
import com.dbkj.meet.services.inter.MessageService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Created by DELL on 2017/03/24.
 */
public class RemindScheduleJob implements Job {

    public static final String ORDER_MEET_ID="orderMeetId";
    public static final String SMS_REMIND="smsRemind";
    public static final String SMS_REMIND_TIME="smsRemindTime";
    public static final String CONTAIN_HOST="containHost";

    private MessageService messageService=new MessageServiceImpl();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Long roid=jobDataMap.getLong(ORDER_MEET_ID);
        boolean containHost=jobDataMap.getBoolean(CONTAIN_HOST);
        //向预约会议参会人发送通知短信
        OrderMeet orderMeet=OrderMeet.dao.findById(roid);
        List<OrderAttendee> list=OrderAttendee.dao.findByOrderMeetId(roid);
        for(OrderAttendee orderAttendee:list){
            if(!containHost&&orderAttendee.getType()== AttendeeType.HOST.getCode()){
                continue;
            }
            messageService.sendSms(orderMeet,orderAttendee.getPhone());
        }
    }
}
