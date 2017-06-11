package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.DateUtil;
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
        final String jname=getJobName();
        final String jgroup=getJobGroup();
        final OrderModel orderModel=getOrderModel();
        boolean flag = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean isSuccess=orderMeet.save();
                if(isSuccess){
                    Long pid=orderMeet.getId();
                    Schedule schedule=new Schedule();
                    schedule.setJobName(jname);
                    schedule.setJobGroup(jgroup);
                    schedule.setOid(Integer.parseInt(pid.toString()));
                    schedule.setOrderType(orderModel.getPeriod());
                    isSuccess=schedule.save();
                    if(isSuccess){
                        isSuccess=addAttendees();
                        if(isSuccess){
                            return addRecord();
                        }
                    }
                }
                return false;
            }
        });

        if(flag){
            Date startTime=null;
            try {
                startTime=sdf.parse(orderModel.getStartTime());
            } catch (ParseException e) {
                log.error(e.getMessage());
                flag=false;
            }
            Trigger trigger=newTrigger()
                    .withIdentity(jname,jgroup)
                    .startAt(startTime)
                    .build();
            //是否会前提醒
            Trigger remindTrigger=null;
            if(orderModel.isSmsRemind()){
                remindTrigger=newTrigger()
                        .withIdentity(jname+"_remind",jgroup)
                        .startAt(DateUtil.addByMinutes(startTime,orderModel.getSmsRemindTime()*(-1)))
                        .build();
            }
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

            //添加预约会议定时任务
            ScheduleHelper.addJob(detail,trigger);

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

                if(remindTrigger!=null){
                    //添加预约会议定时提醒任务
                    ScheduleHelper.addJob(remindDetail,remindTrigger);
                }
            }

        }
        return flag;
    }


}
