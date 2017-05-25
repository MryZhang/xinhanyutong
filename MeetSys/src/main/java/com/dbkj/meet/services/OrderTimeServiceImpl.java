package com.dbkj.meet.services;

import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.services.inter.IOrderTimeService;
import com.dbkj.meet.utils.DateUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2017/05/25.
 */
public class OrderTimeServiceImpl implements IOrderTimeService {
    @Override
    public String getOrderMeetStartTime(OrderMeet orderMeet) {
        Map<String,Object> params=new HashMap<>();
        params.put("oid",orderMeet.getId());
        List<Schedule> scheduleList=Schedule.dao.getScheduleList(params);
        Iterator<Schedule> itr=scheduleList.iterator();
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        int count=scheduleList.size();
        int n=0;
        StringBuilder temp=new StringBuilder();
        while (itr.hasNext()){
            n++;
            Schedule sch=itr.next();
            int type=sch.getOrderType();
            String interval=sch.getInterval();
            switch (type){
                case 0://无重复周期
                    return orderMeet.getStartTime();
                case 1://重复周期为天
                    if("workday".equals(interval)){
                        return "每个工作日"+orderMeet.getStartTime();
                    }else{
                        return "每隔"+interval+"天"+orderMeet.getStartTime();
                    }
                case 2://重复周期为星期
                    if(temp.length()==0){
                        temp.append("每隔"+interval+"周周"+ DateUtil.getWeekday(sch.getOrderNum()));
                    }else{
                        temp.append("、周"+DateUtil.getWeekday(sch.getOrderNum()));
                    }
                    if(n==count){
                        temp.append(" "+orderMeet.getStartTime());
                        return temp.toString();
                    }
                case 3://重复周期为月
                    String orderNum=sch.getOrderNum();
                    if(orderNum==null){
                        return "每个月第"+interval+"天"+orderMeet.getStartTime();
                    }else{
                        if("L".equals(interval)){
                            return "每个月最后一周"+DateUtil.getWeekdayByNum(Integer.parseInt(orderNum))+" "+orderMeet.getStr("startTime");
                        }else{
                            return "每个月第"+interval+"周周"+DateUtil.getWeekdayByNum(Integer.parseInt(orderNum))+" "+orderMeet.getStr("startTime");
                        }
                    }
            }
        }
        return temp.toString();
    }
}
