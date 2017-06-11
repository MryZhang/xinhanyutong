package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.RepeatType;
import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.model.OrderMeet;
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
import java.util.*;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by DELL on 2017/03/27.
 */
public class RepeatOfWeekOrderMeet extends BaseOrderMeetType {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int orderType= RepeatType.WEEK.getCode();

    @Override
    public boolean add() {
        final OrderModel orderModel=getOrderModel();
        final String jgroup=getJobGroup();
        final String jname=getJobName();
        final String[] wks=orderModel.getOrderNum().split(",");
        int n=Integer.parseInt(orderModel.getInterval());
        boolean flag=Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean isSuccess=orderMeet.save();
                if(isSuccess){
                    //添加任务计划数据
                    Long pid=orderMeet.getId();
                    List<Schedule> scheduleList=new ArrayList<>(wks.length);
                    for(int i=0,len=wks.length;i<len;i++){
                        Schedule schedule=new Schedule();
                        schedule.setJobName(jname+"_"+wks[i]);
                        schedule.setJobGroup(jgroup);
                        schedule.setOrderType(orderModel.getPeriod());
                        schedule.setOid(Integer.parseInt(pid.toString()));
                        schedule.setInterval(orderModel.getInterval());
                        schedule.setOrderNum(wks[i]);
                        scheduleList.add(schedule);
                    }
                    int[] results = Db.batchSave(scheduleList,100);
                    isSuccess = getCount(results)==scheduleList.size();
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

        if(flag) {
            String callNum = getCallNum();
            String showNum = getShowNum();
            Long pid = orderMeet.getId();
            Map<JobDetail, Trigger> map = new HashMap<>(wks.length);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(ScheduleJob.HOST_NUM, orderModel.getHostNum());
            jobDataMap.put(ScheduleJob.ORDER_MEET_ID, pid);
            jobDataMap.put(ScheduleJob.IS_RECORD, orderModel.getIsRecord());
            jobDataMap.put(ScheduleJob.CALL_NUM, callNum);
            jobDataMap.put(ScheduleJob.SHOW_NUM, showNum);

            Map<JobDetail,Trigger> remindMap=null;
            if(orderModel.isSmsRemind()){
                remindMap=new HashMap<>(wks.length);
            }
            JobDataMap dataMap=new JobDataMap();
            dataMap.put(RemindScheduleJob.ORDER_MEET_ID,pid);
            dataMap.put(RemindScheduleJob.CONTAIN_HOST,orderModel.isContainHost());
            dataMap.put(RemindScheduleJob.USER_ID,getUser().getId());
            dataMap.put(RemindScheduleJob.REMIND_MINUTES,orderModel.getSmsRemindTime());

            try {
                for (int i = 0, len = wks.length; i < len; i++) {
                    JobDetail jobDetail = newJob(ScheduleJob.class)
                            .withIdentity(jname + "_" + wks[i], jgroup)
                            .usingJobData(jobDataMap)
                            .build();
                    String dateStr = jname.split(" ")[0];
                    Date date = sdf.parse(dateStr + " " + orderModel.getStartTime());
                    Trigger tri = ScheduleHelper.getWeekTrigger(jname + "_" + wks[i], jgroup, date, wks[i], n);
                    map.put(jobDetail, tri);

                    if(orderModel.isSmsRemind()){
                        JobDetail remindJob=newJob(RemindScheduleJob.class)
                                .withIdentity(jname+"_"+wks[i]+"_remind",jgroup)
                                .usingJobData(dataMap)
                                .build();

                        Date remindDate=DateUtil.addByMinutes(date,orderModel.getSmsRemindTime()*(-1));
                        Trigger remindTri=ScheduleHelper.getRemindWeekTrigger(jname+"_"+wks[i]+"_remind",
                                jgroup,remindDate,wks[i],n);
                        remindMap.put(remindJob,remindTri);
                    }
                }
            }catch(ParseException e){
                logger.error(e.getMessage());
                flag=false;
            }
            if(flag){
                ScheduleHelper.addJobs(map);
                if(remindMap!=null){
                    ScheduleHelper.addJobs(remindMap);
                }
            }
            return flag;
        }
        return false;
    }

}
