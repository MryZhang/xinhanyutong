package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.MeetType;
import com.dbkj.meet.dto.MeetListItem;
import com.dbkj.meet.model.*;
import com.dbkj.meet.model.Record;
import com.dbkj.meet.services.inter.IMeetListService;
import com.dbkj.meet.utils.DateUtil;
import com.jfinal.core.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by MrQin on 2016/11/7.
 */
public class MeetListService implements IMeetListService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private SimpleDateFormat datetimeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public List<MeetListItem> getMeetList(Long uid) {
        if(uid==null){
            throw new IllegalArgumentException("the uid can not be null!");
        }
        List<MeetListItem> list=new ArrayList<MeetListItem>();
        List<Record> recordList = Record.dao.getMeetListByUserId(uid);
        if(recordList!=null&&recordList.size()>0){
            for (Record record:recordList) {
                MeetListItem item=new MeetListItem();
                item.setId(record.getId());
                item.setSubject(record.getSubject());
                item.setHostName(record.getHostName());
                item.setCreateTime(datetimeFormat.format(record.getStartTime()));
                item.setStatus(record.getStatus());
                list.add(item);
            }
        }
        return list;
    }



    public List<MeetListItem> getNotStartMeetList(Long uid) {
        if(uid==null){
            throw new IllegalArgumentException("the uid can not be null!");
        }
        List<MeetListItem> list=new ArrayList<MeetListItem>();
        //预约会议
        Map<String,Object> map=new HashMap<>();
        map.put("a.belong",uid);
        List<OrderMeet> orderMeetList=OrderMeet.dao.getOrderMeetList(map);
        Iterator<OrderMeet> iterator=orderMeetList.iterator();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (iterator.hasNext()){
            OrderMeet orderMeet=iterator.next();
            int otype=orderMeet.getInt("orderType");
            if(otype==0){//如果是无循环预约会议判断会议是否已召开
                String start=orderMeet.getStartTime();
                Date time= null;
                try {
                    time = sdf.parse(start+":00");
                } catch (ParseException e) {
                    logger.error(e.getMessage(),e);
                }
                if(DateUtil.compareDateTime(time, new Date())<=0){
                    continue;
                }
            }
            MeetListItem meetListItem=new MeetListItem();
            meetListItem.setId(orderMeet.getId());
            meetListItem.setSubject(orderMeet.getSubject());
            meetListItem.setCreateTime(sdf.format(orderMeet.getCreated()));
            meetListItem.setHostName(orderMeet.getHostName());

            //获取预约会议类型
            Map<String,Object> params=new HashMap<>();
            params.put("oid",orderMeet.getId());
            Schedule sche=Schedule.dao.getSchedule(params);
            String type="";
            switch(sche.getOrderType()){
                case 0:
                    type="无";
                    break;
                case 1:
                    type="日";
                    break;
                case 2:
                    type="周";
                    break;
                case 3:
                    type="月";
                    break;
            }
            meetListItem.setType(type);
            list.add(meetListItem);
        }

        //固定会议
        Map<String,Object> params=new HashMap<>();
        params.put("belong",uid);
        List<FixedMeet> flist=FixedMeet.dao.getFixedMeetList(params);
        Iterator<FixedMeet> fitr=flist.iterator();
        while(fitr.hasNext()){
            FixedMeet fm=fitr.next();
            MeetListItem item=new MeetListItem();
            item.setId(fm.getId());
            item.setSubject(fm.getSubject());
            item.setCreateTime(sdf.format(fm.getCreateTime()));
            item.setHostName(fm.getHostName());
            item.setType(MeetType.FIXED_MEET.getText());
            list.add(item);
        }
        return list;
    }

    /**
     * 获取会议详情
     * @param controller
     * @return
     */
    @Override
    public Map<String, Object> getMeetDetail(Controller controller) {
        Map<String,Object> map=new HashMap<>();
        int mtype=controller.getParaToInt("mtype");
        int id=controller.getParaToInt("id");
        map.put("mtype",mtype);
        if(mtype== MeetType.NORMAL_MEET.getCode()){//及时会议
            String meetId=controller.getPara("mid");
            map.put("mid",meetId);
            Record rd=Record.dao.findById(id);

            com.jfinal.plugin.activerecord.Record record=new com.jfinal.plugin.activerecord.Record();
            record.set("id",rd.getId());
            record.set("subject",rd.getSubject());
            record.set("hostName",rd.getHost());
            record.set("hostPwd",rd.getHostPwd());
            record.set("isRecord",rd.getIsRecord());
            record.set("status",rd.getStatus());
            record.set("startTime",rd.getStartTime());
            //获取呼入呼出号码
            Company company=Company.dao.findById(User.dao.findById(rd.getBelong()).getCid());
            record.set("callNum", AccessNum.dao.findById(company.getCallNum()).getNum());
            record.set("showNum", AccessNum.dao.findById(company.getShowNum()).getNum());

            map.put("record",record);
            List<Attendee> attendeeList=Attendee.dao.findByRecordId(id);
            map.put("alist",attendeeList);
        }else if(mtype==MeetType.ORDER_MEET.getCode()){//预约会议
            OrderMeet orderMeet=OrderMeet.dao.findById(id);
            Company company=Company.dao.findById(User.dao.findById(orderMeet.getBelong()).getCid());
            com.jfinal.plugin.activerecord.Record record=new com.jfinal.plugin.activerecord.Record();
            record.set("id",orderMeet.getId());
            record.set("subject",orderMeet.getSubject());
            record.set("hostName",orderMeet.getHostName());
            record.set("isRecord",orderMeet.getIsRecord());
            record.set("callNum", AccessNum.dao.findById(company.getCallNum()).getNum());
            record.set("showNum", AccessNum.dao.findById(company.getShowNum()).getNum());
            record.set("hostPwd",orderMeet.getHostPwd());

            Map<String,Object> params=new HashMap<>();
            params.put("oid",orderMeet.getId());
            List<Schedule> scheduleList=Schedule.dao.getScheduleList(params);
            Iterator<Schedule> itr=scheduleList.iterator();
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
            int count=scheduleList.size();
            int n=0;
            while(itr.hasNext()){
                n++;
                Schedule sch=itr.next();
                int type=sch.getOrderType();
                String interval=sch.getInterval();
                switch(type){
                    case 0:
                        record.set("time", orderMeet.getStr("startTime"));
                        break;
                    case 1:
                        if("workday".equals(interval)){
                            record.set("time", "每个工作日"+orderMeet.getStr("startTime"));
                        }else{
                            record.set("time", "每隔"+interval+"天"+orderMeet.getStr("startTime"));
                        }
                        break;
                    case 2:
                        String temp=orderMeet.getStr("time");
                        if(temp==null){
                            temp="每隔"+interval+"周周"+ DateUtil.getWeekday(sch.getOrderNum());
                            record.set("time", temp);
                        }else{
                            temp+="、周"+DateUtil.getWeekday(sch.getOrderNum());
                            record.set("time", temp);
                        }
                        if(n==count){
                            temp=record.getStr("time");
                            temp+=" "+orderMeet.getStr("startTime");
                            record.set("time", temp);
                        }
                        break;
                    case 3:
                        String orderNum=sch.getOrderNum();
                        if(orderNum==null){
                            record.set("time", "每个月第"+interval+"天"+orderMeet.getStr("startTime"));
                        }else{
                            if("L".equals(interval)){
                                record.set("time", "每个月最后一周周"+DateUtil.getWeekdayByNum(Integer.parseInt(orderNum))+" "+orderMeet.getStr("startTime"));
                            }else{
                                record.set("time", "每个月第"+interval+"周周"+DateUtil.getWeekdayByNum(Integer.parseInt(orderNum))+" "+orderMeet.getStr("startTime"));
                            }

                        }
                        break;
                }
            }
            map.put("record",record);
            List<OrderAttendee> orderAttendeeList = OrderAttendee.dao.findByOrderMeetId(id);
            map.put("alist",orderAttendeeList);
        }else if(mtype==MeetType.FIXED_MEET.getCode()){
            FixedMeet fixedMeet=FixedMeet.dao.findById(id);
            com.jfinal.plugin.activerecord.Record record=new com.jfinal.plugin.activerecord.Record();
            User user= controller.getSessionAttr(Constant.USER_KEY);
            Company company=Company.dao.findById(user.getCid());
            record.set("callNum", AccessNum.dao.findById(company.getCallNum()).getNum());
            record.set("showNum", AccessNum.dao.findById(company.getShowNum()).getNum());
            record.set("id", fixedMeet.getId());
            record.set("subject", fixedMeet.getSubject());
            record.set("hostName", fixedMeet.getHostName());
            record.set("hostPwd", fixedMeet.getHostPwd());
            record.set("listenerPwd", fixedMeet.getListenerPwd());
            record.set("isRecord", fixedMeet.getIsRecord().equals(Constant.YES)?1:0);
            map.put("record",record);
        }
        return map;
    }
}
