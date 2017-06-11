package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.ScheduleHelper;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by DELL on 2017/03/27.
 */
public class RepeatOfMonthOrderMeet extends BaseOrderMeetType {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int orderType = RepeatType.MONTH.getCode();

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
                    schedule.setJobName(jname);
                    schedule.setJobGroup(jgroup);
                    schedule.setOrderType(orderModel.getPeriod());
                    schedule.setOid(Integer.parseInt(pid.toString()));
                    if(StrKit.isBlank(orderModel.getOrderNum())){
                        schedule.setInterval(orderModel.getInterval());
                    }else{
                        schedule.setInterval(orderModel.getOrderNum());
                        schedule.setOrderNum(orderModel.getWeekday());
                    }
                    isSuccess=schedule.save();
                    if(isSuccess){
                        //添加参会人数据
                        isSuccess = addAttendees();
                        if(isSuccess){
                            return addRecord();
                        }
                    }
                }
                return false;
            }
        });
        if(flag){
            int[] temps=ScheduleHelper.getParaFromTime(orderModel.getStartTime());
            int[] arr=null;
            String cronExpression=null;
            String remindCronExpression=null;
            if(orderModel.isSmsRemind()){
                arr=ScheduleHelper.getParaFromTime(getRemindTime(orderModel.getStartTime(),
                        orderModel.getSmsRemindTime()*(-1)));
            }

            if(orderModel.getOrderNum()==null){
                cronExpression="0 "+temps[1]+" "+temps[0]+" "+orderModel.getInterval()+" * ?";
                if(orderModel.isSmsRemind()){//短信提醒定时任务
                    remindCronExpression="0 "+arr[1]+" "+arr[0]+" "+orderModel.getInterval()+" * ?";
                }
            }else{
                cronExpression="0 "+temps[1]+" "+temps[0]+" ? * ";
                if("L".equals(orderModel.getInterval())){
                    cronExpression+=orderModel.getOrderNum()+"L";
                    if(orderModel.isSmsRemind()){//短信提醒定时任务
                        remindCronExpression="0 "+arr[1]+" "+arr[0]+" ? * "+orderModel.getOrderNum()+"L";
                    }
                }else{
                    cronExpression+=orderModel.getWeekday()+"#"+orderModel.getOrderNum();
                    if(orderModel.isSmsRemind()){//短信提醒定时任务
                        remindCronExpression="0 "+arr[1]+" "+arr[0]+" ? * "+orderModel.getWeekday()
                                +"#"+orderModel.getOrderNum();
                    }
                }
            }
            String callNum=getCallNum();
            String showNum=getShowNum();
            Long pid=orderMeet.getId();

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

            JobDataMap dataMap=new JobDataMap();
            dataMap.put(RemindScheduleJob.ORDER_MEET_ID,pid);
            dataMap.put(RemindScheduleJob.CONTAIN_HOST,orderModel.isContainHost());
            dataMap.put(RemindScheduleJob.USER_ID,getUser().getId());
            dataMap.put(RemindScheduleJob.REMIND_MINUTES,orderModel.getSmsRemindTime());

            JobDetail remindDetail=null;
            if(orderModel.isSmsRemind()){
                remindDetail=newJob(RemindScheduleJob.class)
                        .withIdentity(jname+"_remind",jgroup)
                        .usingJobData(dataMap)
                        .build();
            }

            ScheduleHelper.addJob(detail,jname,jgroup,cronExpression,null);
            if(remindCronExpression!=null){
                ScheduleHelper.addJob(remindDetail,jname+"_remind",jgroup,remindCronExpression,null);
            }
            return flag;
        }
        return false;
    }

}
