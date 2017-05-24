package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.DateUtil;
import com.dbkj.meet.utils.ScheduleHelper;
import com.jfinal.plugin.activerecord.Db;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by DELL on 2017/03/27.
 */
public class RepeatOfWeekOrderMeet extends BaseOrderMeetType {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int orderType= RepeatType.WEEK.getCode();
    /**
     * 重复周期
     */
    private String interval;
    /**
     *
     */
    private String orderNum;

    @Override
    public boolean add() {
        Long pid=orderMeet.getId();
        int n=Integer.parseInt(interval);
        String[] wks=orderNum.split(",");
        Map<JobDetail,Trigger> map=new HashMap<JobDetail, Trigger>();
        Map<JobDetail,Trigger> remindMap=null;
        if(orderMeet.getSmsRemind()==Integer.parseInt(Constant.YES)){
            remindMap=new HashMap<>();
        }
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Schedule> scheduleList=new ArrayList<Schedule>();
        boolean result=true;
        for(int i=0,len=wks.length;i<len;i++){
            Schedule schedule=new Schedule();
            schedule.setJobName(jname+"_"+wks[i]);
            schedule.setJobGroup(jgroup);
            schedule.setOrderType(orderType);
            schedule.setOid(Integer.parseInt(pid.toString()));
            schedule.setInterval(interval);
            schedule.setOrderNum(wks[i]);
            scheduleList.add(schedule);

            JobDetail jobDetail=newJob(ScheduleJob.class)
                    .withIdentity(jname+"_"+wks[i],jgroup)
                    .usingJobData(ScheduleJob.HOST_NUM,orderMeet.getHostNum())
                    .usingJobData(ScheduleJob.ORDER_MEET_ID,pid)
                    .usingJobData(ScheduleJob.IS_RECORD,orderMeet.getIsRecord())
                    .usingJobData(ScheduleJob.CALL_NUM,callNum)
                    .usingJobData(ScheduleJob.SHOW_NUM,showNum)
                    .build();
            try {
                String dateStr=jname.split(" ")[0];
                Date date=sdf.parse(dateStr+" "+orderMeet.getStartTime());
                Trigger tri= ScheduleHelper.getWeekTrigger(jname+"_"+wks[i],jgroup,date,wks[i],n);
                map.put(jobDetail,tri);
                //提醒定时任务
                if(orderMeet.getSmsRemind()==Integer.parseInt(Constant.YES)){
                    JobDetail remindJob=newJob(RemindScheduleJob.class)
                            .withIdentity(jname+"_"+wks[i]+"_remind",jgroup)
                            .usingJobData(RemindScheduleJob.ORDER_MEET_ID,pid)
                            .usingJobData(RemindScheduleJob.SMS_REMIND,orderMeet.getSmsRemind())
                            .usingJobData(RemindScheduleJob.SMS_REMIND_TIME,smsRemindTime)
                            .build();

                    Date remindDate= DateUtil.addByMinutes(date,smsRemindTime*(-1));
                    Trigger remindTri=ScheduleHelper.getRemindWeekTrigger(jname+"_"+wks[i]+"_remind",
                            jgroup,remindDate,wks[i],n);
                    remindMap.put(jobDetail,remindTri);
                }

            } catch (ParseException e) {
                logger.error(e.getMessage(),e);
                result=false;
            }
        }
        if(result){
            try{
                ScheduleHelper.addJobs(map);
                if(remindMap!=null){
                    ScheduleHelper.addJobs(remindMap);
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                result=false;
            }
        }
        return result;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }
}
