package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.ScheduleHelper;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by DELL on 2017/03/27.
 */
public class RepeatOfDayOrderMeet extends BaseOrderMeetType {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int orderType= RepeatType.DAY.getCode();
    /**
     * 重复周期
     */
    private String interval;


    @Override
    public boolean add() {
        Long pid=orderMeet.getId();
        Schedule schedule=new Schedule();
        schedule.setJobGroup(jgroup);
        schedule.setJobName(jname);
        schedule.setOrderType(orderType);
        schedule.setOid(Integer.parseInt(pid.toString()));
        schedule.setInterval(interval);
        boolean result=schedule.save();

        if(!result){
            return false;
        }
        int[] para= ScheduleHelper.getParaFromTime(orderMeet.getStartTime());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //预约会议重复周期为工作日
        String cronExpression=null;
        String remindCronExpression=null;
        JobDetail detail=newJob(ScheduleJob.class)
                .withIdentity(jname, jgroup)
                .usingJobData(ScheduleJob.HOST_NUM, orderMeet.getHostNum())
                .usingJobData(ScheduleJob.ORDER_MEET_ID,pid)
                .usingJobData(ScheduleJob.IS_RECORD, orderMeet.getIsRecord())
                .usingJobData(ScheduleJob.CALL_NUM,callNum)
                .usingJobData(ScheduleJob.SHOW_NUM,showNum)
                .build();
        Trigger trigger=null;
        JobDetail remindDetail=null;
        Trigger remindTrigger=null;

        if(ScheduleHelper.WORKDAY.equals(interval)){
            cronExpression="0 "+para[1]+" "+para[0]+" ? * MON-FRI";
            if(orderMeet.getSmsRemind().equals(Integer.parseInt(Constant.YES))){//定时提醒任务
                int[] arr=ScheduleHelper.getParaFromTime(getRemindTime(orderMeet.getStartTime(),smsRemindTime*(-1)));
                remindCronExpression="0 "+arr[1]+" "+arr[0]+" ? * MON-FRI";
            }
        }else{
            int n=Integer.parseInt(interval);
            try {
                String dateStr=jname.split(" ")[0];
                trigger=newTrigger()
                        .withIdentity(jname,jgroup)
                        .startAt(sdf.parse(dateStr+" "+orderMeet.getStartTime()))
                        .withSchedule(calendarIntervalSchedule().withIntervalInDays(n))
                        .build();

                if(orderMeet.getSmsRemind().equals(Integer.parseInt(Constant.YES))){
                    remindTrigger=newTrigger()
                            .withIdentity(jname+"_remind",jgroup)
                            .startAt(sdf.parse(dateStr+" "+getRemindTime(orderMeet.getStartTime(),smsRemindTime*(-1))))
                            .build();

                }
            } catch (ParseException e) {
                logger.error(e.getMessage(),e);
                result=false;
            }
        }
        if(result){
            try{
                if(cronExpression!=null){
                    ScheduleHelper.addJob(detail,jname,jgroup,cronExpression,null);
                    if(remindCronExpression!=null){
                        ScheduleHelper.addJob(remindDetail,jname+"_remind",jgroup,remindCronExpression,null);
                    }
                }else{
                    ScheduleHelper.addJob(detail, trigger);
                    if(remindTrigger!=null){
                        ScheduleHelper.addJob(remindDetail,remindTrigger);
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                result=false;
            }

        }
        return false;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public int getOrderType() {
        return orderType;
    }

}
