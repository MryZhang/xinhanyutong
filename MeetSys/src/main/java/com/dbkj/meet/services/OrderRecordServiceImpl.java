package com.dbkj.meet.services;

import com.dbkj.meet.dic.MeetState;
import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.IOrderRecordService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by DELL on 2017/06/03.
 */
public class OrderRecordServiceImpl implements IOrderRecordService {

    @Override
    public Record findOrderRecord(Long orderId, String startTime) {
        return Record.dao.findByOrderRecordIdAndStartTime(orderId,startTime);
    }



    @Override
    public boolean createRecord(Long orderId, String startTime, Record record) {
        OrderMeet orderMeet=OrderMeet.dao.findById(orderId);
        return createRecord(orderMeet,startTime,record);
    }

    @Override
    public boolean createRecord(OrderMeet orderMeet, String startTime, Record record) {
        Record rd=findOrderRecord(orderMeet.getId(),startTime);
        if(rd!=null){
            rd.setStatus(MeetState.GOINGON.getStateCode());
            rd.setMid(record.getMid());
            record.setId(rd.getId());
            return createRecord(orderMeet,rd);
        }else{
            return createRecord(orderMeet,record);
        }

    }

    @Override
    public boolean createRecord(Long orderId, Record record) {
        OrderMeet orderMeet=OrderMeet.dao.findById(orderId);
        return createRecord(orderMeet,record);
    }

    @Override
    public boolean createRecord(final OrderMeet orderMeet, final Record record) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean flag=false;
                Integer rid=orderMeet.getRid();
                /**
                 * 由于创建预约会议和定时任务是在不同的线程，为防止定时任务执行时，
                 * 预约会议创建的事务还未提交，导致查不到预约会议的数据产生NPE，
                 * 所以当获取不到预约会议的数据，休眠500毫秒后重新查询，一共重试5次
                 */
                int attempt=1;
                while(rid==null||attempt<=5){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    rid=orderMeet.getRid();
                    attempt++;
                }
                if(record.getId()!=null){
//                    Record rd=Record.dao.findById(rid);
//                    rd.setStatus(MeetState.GOINGON.getStateCode());
//                    rd.setMid(record.getMid());
//                    rd.setGmtModified(new Date());
//                    record.setId(rd.getId());
                    return record.update();
                }else{
                    flag=record.save();
                    if(flag){
                        orderMeet.setRid(Integer.parseInt(record.getId().toString()));
                        orderMeet.setGmtModified(new Date());
                        flag=orderMeet.update();
                    }
                    if(flag){
                        List<OrderAttendee> orderAttendees=OrderAttendee.dao.findByOrderMeetId(orderMeet.getId());
                        //记录添加成功后添加参会人记录
                        List<Attendee> attendeeList=new ArrayList<Attendee>();
                        for(int i=0,len=orderAttendees.size();i<len;i++) {
                            OrderAttendee orderAttendee = orderAttendees.get(i);

                            Attendee attendee = new Attendee();
                            attendee.setName(orderAttendee.getName());
                            attendee.setPhone(orderAttendee.getPhone());
                            attendee.setRid(Integer.parseInt(record.getId().toString()));
                            attendee.setStatus(0);
                            attendee.setType(orderAttendee.getType());
                            attendeeList.add(attendee);
                        }
                        int[] count = Db.batchSave(attendeeList, 100);
                        int num=0;
                        for(int i=0,len=count.length;i<len;i++){
                            num+=count[i];
                        }
                        return num==attendeeList.size();
                    }
                }

                return false;
            }
        });
    }

    @Override
    public String getStartTime(Long orderId, Date date) {
        OrderMeet orderMeet=OrderMeet.dao.findById(orderId);
        return getStartTime(orderMeet,date);
    }

    @Override
    public String getStartTime(OrderMeet orderMeet, Date date) {
        Map<String,Object> paraMap=new HashMap<>();
        paraMap.put("oid",orderMeet.getId());
        Schedule schedule=Schedule.dao.getSchedule(paraMap);
        return getStartTime(orderMeet,schedule,date);
    }

    @Override
    public String getStartTime(OrderMeet orderMeet, Schedule schedule, Date date) {
        String startTime=null;
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
        if(schedule.getOrderType()== RepeatType.NONE.getCode()){
            startTime=orderMeet.getStartTime()+":00";
        }else{
            startTime=dateFormat.format(date)+" "+orderMeet.getStartTime()+":00";
        }
        return startTime;
    }


}
