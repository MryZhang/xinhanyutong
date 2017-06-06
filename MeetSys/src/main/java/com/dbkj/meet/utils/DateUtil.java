package com.dbkj.meet.utils;

import com.dbkj.meet.dto.DatetimeRange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtil {
	
	public static final String MONDAY="mon";
	public static final String TUESDAY="tues";
	public static final String WEDNESDAY="wed";
	public static final String THURSDAY="thur";
	public static final String FRIDAY="fri";
	public static final String SATURDAY="sat";
	public static final String SUNDAY="sun";
	
	public static final String WORKDAY="workday";
	
	public static String getWeekdayByNum(int n){
		String[] arr={"日","一","二","三","四","五","六"};
		return arr[n-1];
	}
	
	/**
	 * 根据英文星期几的简称获取中文名称
	 * @param str
	 * @return
	 */
	public static String getWeekday(String str){
		if(MONDAY.equals(str)){
			return "一";
		}
		if(TUESDAY.equals(str)){
			return "二";
		}
		if(WEDNESDAY.equals(str)){
			return "三";
		}
		if(THURSDAY.equals(str)){
			return "四";
		}
		if(FRIDAY.equals(str)){
			return "五";
		}
		if(SATURDAY.equals(str)){
			return "六";
		}
		if(SUNDAY.equals(str)){
			return "日";
		}
		return null;
	}
	/**
	 * 获取指定日期所在月的第几个星期几
	 * @param date 指定日期
	 * @param weekNum 
	 * @param weekday 星期几（1:星期天，2：星期一，3：星期二，4：星期三，5：星期四，6：星期五，7：星期六）
	 * @return
	 */
//	public static Date getDate(Date date,String weekNum,int weekday){
//		Calendar cal=Calendar.getInstance();
//		cal.setTime(date);
//		int max=cal.getActualMaximum(Calendar.DAY_OF_MONTH);//获取当前月有多少天
//		int count=0;
//		Date d=null;
//		
//		for(int i=1;i<=max;i++){
//			cal.set(Calendar.DAY_OF_MONTH, i);
//			if(cal.get(Calendar.DAY_OF_WEEK)==weekday){
//				count++;
//			}
//		}
//	}
	
	/**
	 * 获取当前周星期几的日期
	 * @param date
	 * @param n 获取星期一的日期那么n为1，以此类推
	 * @return
	 */
	public static Date getDateByWeekdayNum(Date date,int n){
		Calendar cal=Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()+(n-1));
		return cal.getTime();
	}
	
	/**
	 * 获取当前日期的日
	 * @param date
	 * @return
	 */
	public static int getDayofDate(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 判断当前日期是否属于工作日（工作日：星期一~星期五）
	 * @param date
	 * @return
	 */
	public static boolean isWorkday(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		int w=cal.get(Calendar.DAY_OF_WEEK);
		return w>1&&w<7;
	}
	
	/**
	 * 获取当前日期是是星期几
	 * @param date
	 * @return
	 */
	public static String getWeekday(Date date){
		String[] weekdays=new String[]{DateUtil.SUNDAY,DateUtil.MONDAY,DateUtil.TUESDAY,DateUtil.WEDNESDAY,DateUtil.THURSDAY,DateUtil.FRIDAY,DateUtil.SATURDAY};
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		int w=cal.get(Calendar.DAY_OF_WEEK)-1;
		if(w<0){
			w=0;
		}
		return weekdays[w];
	}
	
	/**
	 * 获取星期几
	 * @param n 
	 * @return
	 */
	public static String getWeekday(int n){
		String[] weekdays=new String[]{SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY};
		return weekdays[n];
	}
	
	/**
	 * 判断当前日期是当前的第几周
	 * @param date
	 * @return
	 */
	public static String getWeeksByDate(Date date){
		if(isLastWeekOfMonth(date)){
			return "L";
		}else{
			Calendar cal=Calendar.getInstance();
			cal.setTime(date);
			int n=cal.get(Calendar.WEEK_OF_MONTH);
			return n+"";
		}
		
	}
	
	/**
	 * 判断当前日期是否这个月的最后一周
	 * @param date
	 * @return
	 */
	public static boolean isLastWeekOfMonth(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM");
		String value=sdf1.format(date);
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		int val=cal.get(Calendar.WEEK_OF_MONTH);
		try {
			cal.setTime(sdf.parse(value+"-00"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cal.add(Calendar.MONTH, 1);
		int val1=cal.get(Calendar.WEEK_OF_MONTH);
		if(val==val1){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取本周星期几的日期
	 * @param weekday 指定星期几（为英语中星期几的缩写）
	 * @return
	 */
	public static Date getDate(String weekday){
		Calendar cal=Calendar.getInstance();
		cal.setTime(new Date());
		if(MONDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}else if(TUESDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		}else if(WEDNESDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		}else if(THURSDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		}else if(FRIDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		}else if(SATURDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		}else if(SUNDAY.equals(weekday)){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		}
		return cal.getTime();
	}
	
	/**
	 * 获取指定日期的所在周时间范围
	 * @param date
	 * @return
	 */
	public static String getWeekDate(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(getFirstDayOfWeek(date))+" → "+sdf.format(getLastDayOfWeek(date));
	}
	
	/**
	 * 获取当前日期所在周的范围
	 * @return
	 */
	public static String getNowWeekDate(){
		return getWeekDate(new Date());
	}
	
	/**
	 * 获取指定日期所在周的第一天
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfWeek(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		return cal.getTime();
	}
	
	/**
	 * 获取指定日期所在周的的最后一天
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfWeek(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek()+6);
		return cal.getTime();
	}
	
	/**
	 * 获取指定日期的所在月份的第一天
	 * @param date
	 * @return
	 */
	public static String getFirstDayOfMonth(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return sdf.format(cal.getTime());
	}
	
	/**
	 * 获得指定日期的所在月份的最后一天
	 * @param date
	 * @return
	 */
	public static String getLastDayOfMonth(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return sdf.format(cal.getTime());
	}
	
	
	/**
	 * 比较两个日期的大小，精确到日
	 * @param first 
	 * @param second
	 * @return 如果第一个日期等于第二个日期，返回值等于0；如果第一个日期在第二个日期之前，返回小于0；否则返回值大于0
	 */
	public static int compareDate(Date first,Date second){
		Calendar cal1=Calendar.getInstance();
		Calendar cal2=Calendar.getInstance();
		cal1.setTime(first);
		cal2.setTime(second);
		cal1.set(Calendar.HOUR, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(Calendar.HOUR, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		return cal1.compareTo(cal2);
	}
	
	/**
	 * 比较两个日期时间的大小
	 * @param first
	 * @param second
	 * @return 如果第一个日期等于第二个日期，返回值等于0；如果第一个日期在第二个日期之前，返回小于0；否则返回值大于0
	 */
	public static int compareDateTime(Date first,Date second){
		Calendar cal1=Calendar.getInstance();
		Calendar cal2=Calendar.getInstance();
		cal1.setTime(first);
		cal2.setTime(second);
		return cal1.compareTo(cal2);
	}
	
	/**
	 * 获取两个日期之间的间隔的天数
	 * @param first
	 * @param second
	 * @return
	 */
	public static long daysBetween(Date first,Date second){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		try {
			second=sdf.parse(sdf.format(second));
			first=sdf.parse(sdf.format(first));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar cal=Calendar.getInstance();
		cal.setTime(first);
		long time1=cal.getTimeInMillis();
		cal.setTime(second);
		long time2=cal.getTimeInMillis();
		return Math.abs((time1-time2)/1000*60*60*24);
	}

	/**
	 * 判断是否是闰年
	 * @param date
	 * @return
     */
	public static boolean isLeapYear(Date date){
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		return (year%4==0&&year%100!=0||year%400==0);
	}

	/**
	 * 在当前日期的基础加几个月获取
	 * @param date
	 * @param months
	 * @return
	 */
	public static Date addByMonths(Date date,int months){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH,months);
		return calendar.getTime();
	}

	public static Date addByMinutes(Date date,int minutes){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE,minutes);
		return calendar.getTime();
	}

	public static Date addByDays(Date date,int days){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH,days);
		return calendar.getTime();
	}



	/**
	 * 获取一组时间段的时间交集
	 * @param arr
	 * @return
	 */
	private static List<DatetimeRange> getIntersection(DatetimeRange[] arr){
		if(arr==null){
			return null;
		}
		List<DatetimeRange> list=new ArrayList<DatetimeRange>();
		for(int i=0;i<arr.length-1;i++){
			for(int n=i+1;n<arr.length;n++){
				if(arr[i].getEnd().getTime()<arr[n].getBegin().getTime()||arr[i].getBegin().getTime()>arr[n].getEnd().getTime()){
					continue;
				}
				DatetimeRange intersection=new DatetimeRange();
				if(arr[i].getBegin().getTime()>=arr[n].getBegin().getTime()){
					intersection.setBegin(arr[i].getBegin());
				}else{
					intersection.setBegin(arr[n].getBegin());
				}
				if(arr[i].getEnd().getTime()<=arr[n].getEnd().getTime()){
					intersection.setEnd(arr[i].getEnd());
				}else{
					intersection.setEnd(arr[n].getEnd());
				}
				list.add(intersection);
			}
		}
		return list;
	}

	/**
	 * 从一组时间段中获取同时在线的最多次数
	 * @param arr
	 * @return
	 */
	public static int getMaxOnline(DatetimeRange[] arr){
		List<DatetimeRange> intersectionList=getIntersection(arr);
		if(arr!=null){
			List<Integer> times=new ArrayList<Integer>();
			for(DatetimeRange range:intersectionList){
				Integer n=0;
				for(DatetimeRange item:arr){
					if((range.getBegin().getTime()>=item.getBegin().getTime()&&range.getBegin().getTime()<item.getEnd().getTime())||
							(range.getBegin().getTime()<item.getBegin().getTime()&&item.getBegin().getTime()<range.getEnd().getTime())||
							(range.getEnd().getTime()<=item.getEnd().getTime()&&range.getEnd().getTime()>item.getBegin().getTime())||
							(range.getEnd().getTime()>item.getEnd().getTime()&&item.getEnd().getTime()>range.getBegin().getTime())){
						n++;
					}
				}
				times.add(n);
			}
			if(times.size()>0){
				Collections.sort(times);
				return times.get(times.size()-1);
			}
		}
		return 0;
	}

	/**
	 * 获取两个时间之间时间差，单位为秒
	 * @param before
	 * @param after
	 * @return
	 */
	public static long getSecondsBetween(Date before,Date after){
		return (after.getTime()-before.getTime())/1000L;
	}

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");
		Date time =timeFormat.parse("10:55");
		System.out.println(simpleDateFormat.format(time));
		Date now=new Date();
//		System.out.println(simpleDateFormat.format(now));
//		Date date=DateUtil.addByMinutes(time,-50);
		Date date=DateUtil.addByDays(now,1);
		System.out.println(simpleDateFormat.format(date));
	}
}
