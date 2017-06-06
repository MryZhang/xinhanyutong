package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.*;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.OrderRecordServiceImpl;
import com.dbkj.meet.services.common.MeetManager;
import com.dbkj.meet.services.inter.IOrderRecordService;
import com.google.gson.internal.LinkedTreeMap;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by MrQin on 2016/11/17.
 */
public class ScheduleJob implements Job {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    public static final String HOST_NUM="hostNum";
    public static final String ORDER_MEET_ID="oid";
    public static final String IS_RECORD="isRecord";
    public static final String CALL_NUM="callNum";
    public static final String SHOW_NUM="showNum";

    private IOrderRecordService orderRecordService=new OrderRecordServiceImpl();

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String hostNum=jobDataMap.getString(ScheduleJob.HOST_NUM);
        Long oid=jobDataMap.getLong(ScheduleJob.ORDER_MEET_ID);
        Integer isRecord=jobDataMap.getInt(ScheduleJob.IS_RECORD);
        String callNum=jobDataMap.getString(ScheduleJob.CALL_NUM);
        String showNum=jobDataMap.getString(ScheduleJob.SHOW_NUM);
        //获取任务名称
        String jobName=jobExecutionContext.getJobDetail().getKey().getName();

        Date now=new Date();
        OrderMeet orderMeet=OrderMeet.dao.findById(oid);
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
            orderMeet=OrderMeet.dao.findById(oid);
        }

        Record record=null;
        if(orderMeet.getRid()!=null){
            record=Record.dao.findById(orderMeet.getRid());
        }
        //如果会议已经开始或者结束，则不用创建会议
        if(record!=null&&record.getStatus()>MeetState.STARTED.getStateCode()){
            return;
        }

        Map<String,Object> map=new HashMap<String, Object>();
        map.put(Constant.CALLER,hostNum);
        map.put(Constant.SHOWNUM,showNum);
        map.put(Constant.COSTNUM,callNum);
        Map<String,Object> resultMap = createMeet(map,isRecord,oid,jobName);
        if(Constant.SUCCESS.equals(resultMap.get(Constant.STATUS).toString())){
            //获取会议id
            String meetId=resultMap.get(Constant.CONTENT).toString();
            String hostPwd=map.get(Constant.CHAIRMANPWD).toString();
            String listenerPwd=map.get(Constant.AUDIENCEPWD).toString();
            //创建或更新会议记录
            createRecord(meetId,orderMeet,record,hostPwd,listenerPwd);

            //加入会议
            joinMeet(meetId,oid,record.getId(),orderMeet.getHostName(),hostNum);
        }
    }

    //创建会议
    private Map<String,Object> createMeet(Map<String,Object> map,int isRecord,Long oid,String jobName){
        OrderMeet orderMeet=OrderMeet.dao.findById(oid);
        //防止定时任务开始是，预约会议记录还未插入
        while (orderMeet==null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            orderMeet=OrderMeet.dao.findById(oid);
        }
        logger.info("order id:{},{}",oid,orderMeet);
        //获取参会密码
        map.put(Constant.CHAIRMANPWD,orderMeet.getHostPwd());
        Date now=new Date();
        Map<String,Object> resultMap= MeetManager.getInstance().createMeet(map);
        logger.info(resultMap.toString());
        return resultMap;
    }

    //产生会议记录
    private void createRecord(String meetId,OrderMeet orderMeet,Record record,String hostPwd,String listenerPwd){
        logger.info("Enter method createRecord.");
        //如果会议记录已创建，则只需要更新会议记录
        Date date=new Date();
        if(record!=null){
            record.setStatus(MeetState.GOINGON.getStateCode());
            record.setStartTime(date);
            record.setGmtModified(date);
            record.setMid(meetId);
        }else{
            String hostName=orderMeet.getHostName();
            String hostNum=orderMeet.getHostNum();
            Long oid=orderMeet.getId();

            record=new Record();
            record.setBelong(orderMeet.getBelong());
            record.setSubject(orderMeet.getSubject());
            record.setStartTime(date);
            record.setGmtCreate(date);
            record.setHost(hostNum);
            record.setIsRecord(orderMeet.getIsRecord());
            record.setStatus(MeetState.GOINGON.getStateCode());
            record.setHostName(hostName);
            record.setHostPwd(hostPwd);
            record.setListenerPwd(listenerPwd);
            record.setMid(meetId);
            record.setMeetNums(MeetManager.getInstance().getMeetNums());
            record.setType(MeetType.ORDER_MEET.getCode());
            record.setOid(Integer.parseInt(orderMeet.getId().toString()));

            logger.info(record.toString());
        }
        orderRecordService.createRecord(orderMeet,record);
    }

    private void joinMeet(String meetId,Long oid,Long rid,String hostName,String hostNum){
        List<OrderAttendee> orderAttendees=OrderAttendee.dao.findByOrderMeetId(oid);
        if(orderAttendees.size()>1){//除了会议主持人还有其他参会者
            List<Map<String,String>> callers=new ArrayList<Map<String, String>>();

            Map<String,Object> resultMap = MeetManager.getInstance().getMeetCallStatus(meetId);
            List<LinkedTreeMap<String,String>> attendees= (List<LinkedTreeMap<String, String>>) resultMap.get(Constant.CONTENT);


            for(int i=0,len=orderAttendees.size();i<len;i++){
                OrderAttendee orderAttendee=orderAttendees.get(i);
                //如何是非主持人
                if(orderAttendee.getType()== AttendeeType.ATTENDEE.getCode()){
                    boolean flag=true;
                    //判断参会人是否已进入会议
                    for(LinkedTreeMap<String,String> map:attendees){
                        if(map.get(Constant.CALLER).equals(orderAttendee.getPhone())){
                            flag=false;
                            break;
                        }
                    }
                    if(flag){
                        Map<String,String> map=new HashMap<String, String>();
                        map.put(Constant.CALLER,orderAttendee.getPhone());
                        map.put(Constant.NAME,orderAttendee.getName());
                        callers.add(map);
                    }
                }
            }

            Map<String,Object> map=new HashMap<String, Object>();
            map.put(Constant.MEETID,meetId);
            map.put(Constant.CALLERS,callers);

            resultMap=MeetManager.getInstance().joinMeet(map);
            if(Constant.SUCCESS.equals(resultMap.get(Constant.STATUS))){//加入会议成功
                logger.info("Join meetting success.");
            }else{
                logger.error("加入会议失败:"+resultMap.get(Constant.CONTENT).toString());
            }
        }
    }
}
