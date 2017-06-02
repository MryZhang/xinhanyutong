package com.dbkj.meet.utils;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;

public class ScheduleHelper {

	private static final Logger log=Logger.getLogger(ScheduleHelper.class);
//	private static String JOB_GROUP_NAME="group1";
//	private static String TRIGGER_GROUP_NAME="trigger1";
	//任务类型
//	public static final int NO_REPEAT=0;
//	public static final int DAY_REPEAT=1;
//	public static final int WEEK_REPEAT=2;
//	public static final int MONTH_REPEAT=3;
//	
	public static final String MONDAY="mon";
	public static final String TUESDAY="tues";
	public static final String WESDNESDAY="wed";
	public static final String THURSDAY="thur";
	public static final String FRIDAY="fri";
	public static final String SATURDAY="sat";
	public static final String SUNDAY="sun";

	public static final String WORKDAY="workday";
	
	
	
	/**
	 * 添加定时任务
	 * @param jobName 要执行的任务名称
	 * @param groupName 任务祖组名
	 * @param triggerName 触发器名称
	 * @param triggerGroup 触发器组名称
	 * @param cronExpression 时间设置
	 * @param date 如果cronExpression为null，则date为触发器开始执行的时间，否则date为触发器结束的时间
	 */
	public static void addJob(String jobName,String groupName,String triggerName,String triggerGroup,String cronExpression,Date date){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			JobDetail job=newJob()
					.withIdentity(jobName, groupName)
					.build();
			Trigger trigger=getTrigger(triggerName, triggerGroup, cronExpression,date);
			sch.scheduleJob(job, trigger);
			if(!sch.isStarted()){
				sch.start();
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 添加多个定时任务
	 * @param map 
	 */
	public static void addJobs(Map<JobDetail,Trigger> map){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			for(Entry<JobDetail, Trigger> entry:map.entrySet()){
				sch.scheduleJob(entry.getKey(), entry.getValue());
			}
			if(!sch.isStarted()){
				sch.start();
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 添加定时任务
	 * @param job 要执行的任务
	 * @param trigger 任务的触发器
	 */
	public static void addJob(JobDetail job,Trigger trigger){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			sch.scheduleJob(job, trigger);
			if(!sch.isStarted()){
				sch.start();
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 添加定时任务
	 * @param job 要执行的定时任务
	 * @param triggerName 触发器名称
	 * @param triggerGroup 触发器组
	 * @param cronExpression 时间设置
	 * @param date  如果cronExpression为null，则date为触发器开始执行的时间，否则date为触发器结束的时间
	 */
	public static void addJob(JobDetail job,String triggerName,String triggerGroup,String cronExpression,Date date){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			Trigger trigger=getTrigger(triggerName, triggerGroup, cronExpression, date);
			sch.scheduleJob(job, trigger);
			if(!sch.isStarted()){
				sch.start();
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	/**
	 * 添加定时任务
	 * @param job 
	 * @param triggers
	 */
	public static void addJob(JobDetail job,Set<Trigger> triggers){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			Map<JobDetail,Set<? extends Trigger>> jobs=new HashMap<JobDetail, Set<? extends Trigger>>();
			jobs.put(job, triggers);
			sch.scheduleJobs(jobs,true);
			if(!sch.isStarted()){
				sch.start();
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
		
	}
	/**
	 * 移除一个任务
	 * @param jobName 任务名
	 * @param jobGroup 任务组名
	 */
	public static void removeJob(String jobName,String jobGroup){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			List<Trigger> jobTriggers=(List<Trigger>) sch.getTriggersOfJob(new JobKey(jobName, jobGroup));
			for(int i=0,len=jobTriggers.size();i<len;i++){
				Trigger trigger=jobTriggers.get(i);
				String name=trigger.getKey().getName();
				String group=trigger.getKey().getGroup();
				
				sch.pauseTrigger(new TriggerKey(name,group));//停止触发器
				sch.unscheduleJob(new TriggerKey(name,group));//移除触发器
			}
			
			sch.deleteJob(new JobKey(jobName, jobGroup));//删除任务
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
		
	}
	
	
	
	/**
	 * 获取触发器
	 * @param triggerName 触发器名称
	 * @param triggerGroup 触发器组名
	 * @param cronExpression cron表达式
	 * @param date 如果cronExpression为null，则date为触发器开始执行的时间，否则date为触发器结束的时间
	 * @return
	 */
	private static Trigger getTrigger(String triggerName,String triggerGroup,String cronExpression,Date date){
		Trigger trigger=null;
		try {
			Scheduler sche = StdSchedulerFactory.getDefaultScheduler();
			trigger=sche.getTrigger(new TriggerKey(triggerName, triggerGroup));
			if(trigger==null){
				if(cronExpression==null){
					trigger=newTrigger()
						    .withIdentity(triggerName, triggerGroup)
						    .startAt(date)
						    .build();
				}else{
					trigger=newTrigger()
							.withIdentity(triggerName, triggerGroup)
							.startNow()
							.withSchedule(cronSchedule(cronExpression))
							.build();
				}
				
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
		return trigger;
	}
	
	/**
	 * 根据星期几获取触发器
	 * @param triggerName 触发器名称
	 * @param triggerGroup 触发器组民
	 * @param weekday 星期几（英语中星期的简称，可为：mon,tues,wed,thur,fri,sat,sun）
	 * @return
	 */
	public static Trigger getWeekTrigger(String triggerName,String triggerGroup,Date startTime,String weekday,int interval){
		Scheduler sche=null;
		Trigger trigger=null;
		try {
			sche = StdSchedulerFactory.getDefaultScheduler();
			trigger = sche.getTrigger(new TriggerKey(triggerName, triggerGroup));
			
			if(trigger==null){
				//比较当前时间和定时星期几的时间,来确定定时任务的执行时间
				Calendar cal=Calendar.getInstance();
				Date stime=DateUtil.getDate(weekday);
				String date=new SimpleDateFormat("yyyy-MM-dd").format(stime);
				String time=new SimpleDateFormat("HH:mm:ss").format(startTime);
				try {
					stime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date+" "+time);
				} catch (ParseException e) {
					log.error(e.getMessage(),e);
				}
				Date now=new Date();
				int n = DateUtil.compareDateTime(now, stime);
				if(n>0){//说明当前时间已超过执行时间
					cal.setTime(stime);
					cal.add(Calendar.DAY_OF_MONTH, 7);
					stime=cal.getTime();
				}
				trigger=newTrigger()
						.withIdentity(triggerName, triggerGroup)
						.startAt(stime)
						.withSchedule(calendarIntervalSchedule().withIntervalInWeeks(interval))
						.build();
			}
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
		return trigger;
	}

	/**
	 * 根据星期几获取提醒任务触发器
	 * @param triggerName 触发器名称
	 * @param triggerGroup 触发器组民
	 * @param weekday 星期几（英语中星期的简称，可为：mon,tues,wed,thur,fri,sat,sun）
	 * @return
	 */
	public static Trigger getRemindWeekTrigger(String triggerName,String triggerGroup,Date startTime,String weekday,int interval){
		return getWeekTrigger(triggerName,triggerGroup,startTime,weekday,interval);
	}
	
	
	
	/**
	 * 将日期转换为int型的数组
	 * @param date
	 * @return
	 */
	public static int[] getParaFromDate(Date date){
		String time=new SimpleDateFormat("HH:mm:ss").format(date);
		String[] temp=time.split(":");
		int[] para=new int[3];
		for(int i=0,len=temp.length;i<len;i++){
			para[i]=Integer.parseInt(temp[i]);
		}
		return para;
	}
	
	/**
	 * 修改指定任务的触发器
	 * @param name 任务名
	 * @param group 任务组
	 * @param newTrigger
	 */
	public static void updateJobTrigger(String name,String group,Trigger newTrigger){
		try {
			Scheduler sch = StdSchedulerFactory.getDefaultScheduler();
			sch.rescheduleJob(new TriggerKey(name, group), newTrigger);
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
		}
	}
	
	
	
	/**
	 * 将时间字符串转换为int型的数组
	 * @param time
	 * @return
	 */
	public static int[] getParaFromTime(String time){
		String[] temp=time.split(":");
		int[] para=new int[temp.length];
		for(int i=0,len=temp.length;i<len;i++){
			para[i]=Integer.parseInt(temp[i]);
		}
		return para;
	}
}
