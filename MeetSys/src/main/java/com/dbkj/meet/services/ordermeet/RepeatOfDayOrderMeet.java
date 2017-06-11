package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.ScheduleHelper;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;

import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by DELL on 2017/03/27.
 */
public class RepeatOfDayOrderMeet extends BaseOrderMeetType {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int orderType= RepeatType.DAY.getCode();


    @Override
    public boolean add() {
        final OrderModel orderModel=getOrderModel();
        final String jgroup=getJobGroup();
        final String jname=getJobName();
        boolean flag= Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean isSuccess=orderMeet.save();
                if(isSuccess){
                    Long pid=orderMeet.getId();
                    Schedule schedule=new Schedule();
                    schedule.setJobGroup(jgroup);
                    schedule.setJobGroup(jgroup);
                    schedule.setJobName(jname);
                    schedule.setOrderType(orderModel.getPeriod());
                    schedule.setOid(Integer.parseInt(pid.toString()));
                    schedule.setInterval(orderModel.getInterval());
                    isSuccess=schedule.save();
                    if(isSuccess){
                        isSuccess = addAttendees();
                        if(isSuccess){
                            //添加参会人数据
                            return addRecord();
                        }
                    }
                }
                return false;
            }
        });

        if(flag){
            int[] para=ScheduleHelper.getParaFromTime(orderModel.getStartTime());
            //预约会议重复周期为工作日
            String cronExpression=null;
            String remindCronExpression=null;

            Trigger trigger=null;
            Trigger remindTrigger=null;
            if(ScheduleHelper.WORKDAY.equals(orderModel.getInterval())){
                cronExpression="0 "+para[1]+" "+para[0]+" ? * MON-FRI";
                if(orderModel.isSmsRemind()){//定时提醒任务
                    int[] arr=ScheduleHelper.getParaFromTime(getRemindTime(orderModel.getStartTime(),
                            orderModel.getSmsRemindTime()*(-1)));
                    remindCronExpression="0 "+arr[1]+" "+arr[0]+" ? * MON-FRI";
                }
            }else{
                int n=Integer.parseInt(orderModel.getInterval().trim());
                try {
                    String dateStr=jname.split(" ")[0];
                    trigger=newTrigger()
                            .withIdentity(jname,jgroup)
                            .startAt(sdf.parse(dateStr+" "+orderModel.getStartTime()))
                            .withSchedule(calendarIntervalSchedule().withIntervalInDays(n))
                            .build();

                    if(orderModel.isSmsRemind()){
                        remindTrigger=newTrigger()
                                .withIdentity(jname+"_remind",jgroup)
                                .startAt(sdf.parse(dateStr+" "+getRemindTime(orderModel.getStartTime(),
                                        orderModel.getSmsRemindTime()*(-1))))
                                .build();
                    }
                } catch (ParseException e) {
                    logger.error(e.getMessage(),e);
                    flag=false;
                }
            }
            if(flag){
                String callNum=getCallNum();
                String showNum=getShowNum();
                long pid=orderMeet.getId();
                JobDataMap jobDataMap=new JobDataMap();
                jobDataMap.put(ScheduleJob.HOST_NUM,orderModel.getHostNum());
                jobDataMap.put(ScheduleJob.ORDER_MEET_ID,pid);
                jobDataMap.put(ScheduleJob.IS_RECORD,orderModel.getIsRecord());
                jobDataMap.put(ScheduleJob.CALL_NUM,callNum);
                jobDataMap.put(ScheduleJob.SHOW_NUM,showNum);

                JobDetail detail=newJob(ScheduleJob.class)
                        .withIdentity(jname, jgroup)
                        .usingJobData(jobDataMap)
                        .build();

                JobDetail remindDetail=null;
                if(orderModel.isSmsRemind()){
                    JobDataMap dataMap=new JobDataMap();
                    dataMap.put(RemindScheduleJob.ORDER_MEET_ID,pid);
                    dataMap.put(RemindScheduleJob.CONTAIN_HOST,orderModel.isContainHost());
                    dataMap.put(RemindScheduleJob.USER_ID,getUser().getId());
                    dataMap.put(RemindScheduleJob.REMIND_MINUTES,orderModel.getSmsRemindTime());

                    remindDetail=newJob(RemindScheduleJob.class)
                            .withIdentity(jname+"_remind",jgroup)
                            .usingJobData(dataMap)
                            .build();
                }

                if(cronExpression!=null){
                    ScheduleHelper.addJob(detail,jname,jgroup,cronExpression,null);
                    if(remindCronExpression!=null){
                        ScheduleHelper.addJob(remindDetail,jname+"_remind",jgroup,remindCronExpression,null);
                    }
                }else if(trigger!=null){
                    ScheduleHelper.addJob(detail, trigger);
                    if(remindTrigger!=null){
                        ScheduleHelper.addJob(remindDetail,remindTrigger);
                    }
                }
                return flag;
            }
        }
        return false;
    }

}
