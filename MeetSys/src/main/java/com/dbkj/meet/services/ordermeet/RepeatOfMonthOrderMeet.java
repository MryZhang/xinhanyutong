package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.model.Schedule;
import com.dbkj.meet.utils.ScheduleHelper;
import com.jfinal.kit.StrKit;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by DELL on 2017/03/27.
 */
public class RepeatOfMonthOrderMeet extends BaseOrderMeetType {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int orderType = RepeatType.MONTH.getCode();

    /**
     * 重复周期
     */
    private String interval;
    private String orderNum;
    /**
     * 周几
     */
    private String weekday;

    @Override
    public boolean add() {
        Long pid = orderMeet.getId();
        Schedule schedule = new Schedule();
        schedule.setJobName(jname);
        schedule.setJobGroup(jgroup);
        schedule.setOrderType(orderType);
        schedule.setOid(Integer.parseInt(pid.toString()));
        if (StrKit.isBlank(orderNum)) {
            schedule.setInterval(interval);
        } else {
            schedule.setInterval(orderNum);
            schedule.setOrderNum(weekday);
        }
        boolean result = schedule.save();
        if (!result) {
            return false;
        }
        int[] temps = ScheduleHelper.getParaFromTime(orderMeet.getStartTime());
        int[] arr = null;

        JobDetail detail = newJob(ScheduleJob.class)
                .withIdentity(jname, jgroup)
                .usingJobData(ScheduleJob.HOST_NUM, orderMeet.getHostNum())
                .usingJobData(ScheduleJob.ORDER_MEET_ID, pid)
                .usingJobData(ScheduleJob.IS_RECORD, orderMeet.getIsRecord())
                .usingJobData(ScheduleJob.CALL_NUM, callNum)
                .usingJobData(ScheduleJob.SHOW_NUM, showNum)
                .build();

        JobDetail remindDetail = null;

        if (orderMeet.getSmsRemind() == Integer.parseInt(Constant.YES)) {
            arr = ScheduleHelper.getParaFromTime(getRemindTime(orderMeet.getStartTime(), smsRemindTime * (-1)));
            remindDetail = newJob(RemindScheduleJob.class)
                    .withIdentity(jname + "_remind", jgroup)
                    .usingJobData(RemindScheduleJob.ORDER_MEET_ID, pid)
                    .usingJobData(RemindScheduleJob.CONTAIN_HOST, containHost)
                    .build();
        }

        String cronExpression = null;
        String remindCronExpression = null;

        if (orderNum == null) {
            cronExpression = "0 " + temps[1] + " " + temps[0] + " " + interval + " * ?";
            if (orderMeet.getSmsRemind() == Integer.parseInt(Constant.YES)) {//短信提醒定时任务
                remindCronExpression = "0 " + arr[1] + " " + arr[0] + " " + interval + " * ?";
            }
        } else {
            cronExpression = "0 " + temps[1] + " " + temps[0] + " ? * ";
            if ("L".equals(interval)) {
                cronExpression += orderNum + "L";
                if (orderMeet.getSmsRemind() == Integer.parseInt(Constant.YES)) {//短信提醒定时任务
                    remindCronExpression = "0 " + arr[1] + " " + arr[0] + " ? * " + orderNum + "L";
                }
            } else {
                cronExpression += weekday + "#" + orderNum;
                if (orderMeet.getSmsRemind() == Integer.parseInt(Constant.YES)) {//短信提醒定时任务
                    remindCronExpression = "0 " + arr[1] + " " + arr[0] + " ? * " + weekday
                            + "#" + orderNum;
                }
            }
        }
        if(result){
            try{
                ScheduleHelper.addJob(detail,jname,jgroup,cronExpression,null);
                if(remindCronExpression!=null){
                    ScheduleHelper.addJob(remindDetail,jname+"_remind",jgroup,remindCronExpression,null);
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

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }
}
