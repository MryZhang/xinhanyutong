package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.DateUtil;
import com.dbkj.meet.utils.ScheduleHelper;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 无重复会议类型
 * Created by DELL on 2017/03/27.
 */
public class NoRepeateOrderMeet extends BaseOrderMeetType {

    private final Logger log= LoggerFactory.getLogger(this.getClass());

    private final int orderType=RepeatType.NONE.getCode();

    @Override
    public boolean add() {
        Long pid=getOrderMeet().getId();
        Schedule schedule=new Schedule();
        schedule.setJobName(jname);
        schedule.setJobGroup(jgroup);
        schedule.setOid(Integer.parseInt(pid.toString()));
        schedule.setOrderType(orderType);
        boolean result=schedule.save();

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startTime=null;
        try {
            startTime=sdf.parse(orderMeet.getStartTime());
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
            result=false;
        }

        if(!result){
            return false;
        }

        Trigger trigger=newTrigger()
                .withIdentity(jname,jgroup)
                .startAt(startTime)
                .build();

        JobDetail detail=newJob(ScheduleJob.class)
                .withIdentity(jname, jgroup)
                .usingJobData(ScheduleJob.HOST_NUM, orderMeet.getHostNum())
                .usingJobData(ScheduleJob.ORDER_MEET_ID,pid)
                .usingJobData(ScheduleJob.IS_RECORD, orderMeet.getIsRecord())
                .usingJobData(ScheduleJob.CALL_NUM,callNum)
                .usingJobData(ScheduleJob.SHOW_NUM,showNum)
                .build();

        Trigger remindTrigger=null;
        JobDetail remindDetail=null;
        //是否会前提醒
        if(orderMeet.getSmsRemind()==Integer.parseInt(Constant.YES)){
            remindTrigger=newTrigger()
                    .withIdentity(jname+"_remind",jgroup)
                    .startAt(DateUtil.addByMinutes(startTime,smsRemindTime*(-1)))
                    .build();
            remindDetail=newJob(RemindScheduleJob.class)
                    .withIdentity(jname+"_remind",jgroup)
                    .usingJobData(RemindScheduleJob.ORDER_MEET_ID,pid)
                    .usingJobData(RemindScheduleJob.CONTAIN_HOST,containHost)
                    .build();
        }

        try{
            ScheduleHelper.addJob(detail,trigger);
            if(remindTrigger!=null){
                ScheduleHelper.addJob(remindDetail,remindTrigger);
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            result=false;
        }

        return result;
    }

}
