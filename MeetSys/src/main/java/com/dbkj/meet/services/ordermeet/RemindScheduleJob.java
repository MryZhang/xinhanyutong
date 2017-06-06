package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.AttendeeType;
import com.dbkj.meet.dic.MeetState;
import com.dbkj.meet.dic.MeetType;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.MessageServiceImpl;
import com.dbkj.meet.services.OrderRecordServiceImpl;
import com.dbkj.meet.services.SMTPServiceImpl;
import com.dbkj.meet.services.common.MeetManager;
import com.dbkj.meet.services.inter.IOrderRecordService;
import com.dbkj.meet.services.inter.ISMTPService;
import com.dbkj.meet.services.inter.MessageService;
import com.dbkj.meet.utils.DateUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by DELL on 2017/03/24.
 */
public class RemindScheduleJob implements Job {

    public static final String ORDER_MEET_ID="orderMeetId";
    public static final String SMS_REMIND="smsRemind";
    public static final String SMS_REMIND_TIME="smsRemindTime";
    public static final String CONTAIN_HOST="containHost";
    public static final String USER_ID="userId";
    public static final String REMIND_MINUTES="remindMinutes";

    private MessageService messageService=new MessageServiceImpl();
    private ISMTPService smtpService=new SMTPServiceImpl();
    private IOrderRecordService orderRecordService=new OrderRecordServiceImpl();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Long roid=jobDataMap.getLong(RemindScheduleJob.ORDER_MEET_ID);
        Long uid=jobDataMap.getLong(RemindScheduleJob.USER_ID);
//        int remindMinites=jobDataMap.getInt(RemindScheduleJob.REMIND_MINUTES);
        boolean containHost=jobDataMap.getBoolean(RemindScheduleJob.CONTAIN_HOST);
        //向预约会议参会人发送通知短信
        OrderMeet orderMeet=OrderMeet.dao.findById(roid);
        List<OrderAttendee> list=OrderAttendee.dao.findByOrderMeetId(roid);
        /**
         * 由于创建预约会议和定时任务是在不同的线程，为防止定时任务执行时，
         * 预约会议创建的事务还未提交，导致查不到预约会议的数据产生NPE，
         * 所以当获取不到预约会议的数据，休眠500毫秒后重新查询，一共重试5次
         */
        int attempt=1;
        while (orderMeet==null||attempt>5){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            orderMeet=OrderMeet.dao.findById(roid);
        }

        Record record=null;
        if(orderMeet.getRid()!=null){
            record=Record.dao.findById(orderMeet.getRid());
        }
        if(record==null){
            //创建会议记录
            record = createRecord(record,orderMeet,uid);
        }

        List<String> phoneList=new ArrayList<>(list.size());
        for(OrderAttendee orderAttendee:list){
            if(!containHost&&orderAttendee.getType()== AttendeeType.HOST.getCode()){
                continue;
            }
            String phone=orderAttendee.getPhone();
            String name=orderAttendee.getName();
            phoneList.add(phone);
            if(record!=null){
                messageService.sendOrderSms(record.getId(),orderMeet,phone,name);
            }
        }
        //发送邮件通知
        List<String> emailList= Employee.dao.getEmailByUsername(phoneList);
        smtpService.sendMail(uid,emailList.toArray(new String[emailList.size()]),orderMeet);
    }

    private Record createRecord(Record record,OrderMeet orderMeet,Long uid){
        record=new Record();
        record.setSubject(orderMeet.getSubject());
        record.setHostName(orderMeet.getHostName());
        record.setHost(orderMeet.getHostNum());
        record.setGmtCreate(new Date());
        record.setIsRecord(orderMeet.getIsRecord());
        record.setBelong(Integer.parseInt(uid.toString()));
        record.setStatus(MeetState.STARTED.getStateCode());
        record.setHostPwd(orderMeet.getHostPwd());
        record.setMeetNums(MeetManager.getInstance().getMeetNums());
        record.setType(MeetType.ORDER_MEET.getCode());
        record.setOid(Integer.parseInt(orderMeet.getId().toString()));

        boolean result = orderRecordService.createRecord(orderMeet,record);
        if(result){
            return record;
        }
        return null;
    }
}
