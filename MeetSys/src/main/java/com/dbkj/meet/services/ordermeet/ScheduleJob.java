package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.AttendeeType;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.MeetState;
import com.dbkj.meet.dic.MeetType;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.common.MeetManager;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
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

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String hostNum=jobDataMap.getString(HOST_NUM);
        Long oid=jobDataMap.getLong(ORDER_MEET_ID);
        Integer isRecord=jobDataMap.getInt(IS_RECORD);
        String callNum=jobDataMap.getString(CALL_NUM);
        String showNum=jobDataMap.getString(SHOW_NUM);
        //获取任务名称
        String jobName=jobExecutionContext.getJobDetail().getKey().getName();

        Map<String,Object> map=new HashMap<String, Object>();
        map.put(Constant.CALLER,hostNum);
        map.put(Constant.SHOWNUM,showNum);
        map.put(Constant.COSTNUM,callNum);

        createMeet(map,isRecord,oid,jobName);
    }

    //创建会议
    private void createMeet(Map<String,Object> map,int isRecord,Long oid,String jobName){
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
        Map<String,Object> resultMap= MeetManager.getInstance().createMeet(map);
        logger.info(resultMap.toString());
        //创建会议成功
        if(Constant.SUCCESS.equals(resultMap.get(Constant.STATUS).toString())){
            logger.info("Create meetting success.");
            //获取会议id
            String meetId=resultMap.get(Constant.CONTENT).toString();
            String hostPwd=map.get(Constant.CHAIRMANPWD).toString();
            String listenerPwd=map.get(Constant.AUDIENCEPWD).toString();
            createRecord(meetId,orderMeet,jobName,hostPwd,listenerPwd);
        }
    }

    //产生会议记录并邀请参会人
    private void createRecord(String meetId,OrderMeet orderMeet,String jobName,String hostPwd,String listenerPwd){
        logger.info("Enter method createRecord.");
        logger.info(orderMeet.toString());
        String hostName=orderMeet.getHostName();
        String hostNum=orderMeet.getHostNum();

        Long oid=orderMeet.getId();
        logger.info("oid:"+oid+",jobName:"+jobName);
        Schedule schedule=Schedule.dao.findByOrderMeetIdAndJobName(oid,jobName);
        logger.info(schedule.toString());

        logger.info("Execute method createRecord.");

        Record record=new Record();
        record.setBelong(orderMeet.getBelong());
        record.setSubject(orderMeet.getSubject());
        record.setStartTime(new Date());
        record.setHost(hostNum);
        record.setIsRecord(orderMeet.getIsRecord());
        record.setStatus(MeetState.GOINGON.getStateCode());
        record.setHostName(hostName);
        record.setHostPwd(hostPwd);
        record.setListenerPwd(listenerPwd);
        record.setMid(meetId);
        record.setMeetNums(MeetManager.getInstance().getMeetNums());
        record.setType(MeetType.ORDER_MEET.getCode());
        record.setSid(Integer.parseInt(schedule.getId().toString()));

        logger.info(record.toString());

        if(record.save()){
            logger.info("Add record success.");
            joinMeet(meetId,oid,record.getId(),hostName,hostNum);
        }else{
            logger.error("添加会议记录失败");
        }
    }

    private void joinMeet(String meetId,Long oid,Long rid,String hostName,String hostNum){
        List<OrderAttendee> orderAttendees=OrderAttendee.dao.findByOrderMeetId(oid);
        if(orderAttendees.size()>1){//除了会议主持人还有其他参会者
            List<Map<String,String>> callers=new ArrayList<Map<String, String>>();
            final List<Attendee> attendeeList=new ArrayList<Attendee>();
            for(int i=0,len=orderAttendees.size();i<len;i++){
                OrderAttendee orderAttendee=orderAttendees.get(i);

                Attendee attendee=new Attendee();
                attendee.setName(orderAttendee.getName());
                attendee.setPhone(orderAttendee.getPhone());
                attendee.setRid(Integer.parseInt(rid.toString()));
                attendee.setStatus(0);
                attendee.setType(orderAttendee.getType());
                attendeeList.add(attendee);
                //如何是非主持人
                if(orderAttendee.getType()== AttendeeType.ATTENDEE.getCode()){
                    Map<String,String> map=new HashMap<String, String>();
                    map.put(Constant.CALLER,orderAttendee.getPhone());
                    map.put(Constant.NAME,orderAttendee.getName());
                    callers.add(map);
                }
            }

            Map<String,Object> map=new HashMap<String, Object>();
            map.put(Constant.MEETID,meetId);
            map.put(Constant.CALLERS,callers);

            Map<String,Object> resultMap=MeetManager.getInstance().joinMeet(map);
            if(Constant.SUCCESS.equals(resultMap.get(Constant.STATUS))){//加入会议成功
                logger.info("Join meetting success.");
                Db.tx(new IAtom() {
                    public boolean run() throws SQLException {
                        int[] count = Db.batchSave(attendeeList, 100);
                        int num=0;
                        for(int i=0,len=count.length;i<len;i++){
                            num+=count[i];
                        }
                        return num==attendeeList.size();
                    }
                });
            }else{
                logger.error("加入会议失败:"+resultMap.get(Constant.CONTENT).toString());
            }
        }else{
            Attendee attendee=new Attendee();
            attendee.setName(hostName);
            attendee.setPhone(hostNum);
            attendee.setRid(Integer.parseInt(rid.toString()));
            attendee.setType(AttendeeType.HOST.getCode());
            attendee.setStatus(0);
            attendee.save();
        }
    }
}
