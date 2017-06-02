package com.dbkj.meet.dto;

/**
 * 封装预约会议的数据
 * Created by MrQin on 2016/11/14.
 */
public class OrderModel {

    private Long id;
    private String subject;//会议主题
    private String hostNum;//主持人号码
    private int period;//预约会议重复周期类型（0：无重复，1：天，2：星期，3：月,4:固定会议）
    private int isRecord;//是否录音
    private String hostName;//主持人
    private String hostPwd;//会议密码
    private String listenerPwd;//听众密码
    private String startTime;
    private String orderNum;
    private String dayChoice;//当会议周期为天的时候，会议重复类型，interval:周期性间隔，workday：工作日
    private String interval;//当会议周期为天，重复类型为interval的时候，间隔的天数
    private int weekInterval;//当重复周期周的时候，每隔的周数
    private String[] weekdays;//当重复周期为周，重复多少周的星期几
    private String monthChoice;//当会议重复周期为月的时候，会议的重复类型，dayOfInterval:每个月第几天，weekdayOfInterval:每个月的第几个星期几
    private int dayOfInterval;//当会议重复周期为月的时候，会议重复类型为dayOfInterval的时候，每个月的第几天
    private String weekNum;//当周会议重复周期为月的时候，重复类型为weekdayOfInterval的时候，每个月的第几周
    private String weekday;//当周会议重复周期为月的时候，重复类型为weekdayOfInterval的时候，每个月的第几周的星期几

    private String contacts;//会议邀请人json字符串数据

    private boolean smsRemind;//会议开始前是否提醒
    private int smsRemindTime;//会议开始前多少分钟提醒
    private boolean smsNotice;//预约会议创建后是否向参会人发短信通知
    private boolean containHost;//是否包含主持人
    private boolean callInitiative;//是否主动呼叫


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHostNum() {
        return hostNum;
    }

    public void setHostNum(String hostNum) {
        this.hostNum = hostNum;
    }

    public int getIsRecord() {
        return isRecord;
    }

    public void setIsRecord(int isRecord) {
        this.isRecord = isRecord;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostPwd() {
        return hostPwd;
    }

    public void setHostPwd(String hostPwd) {
        this.hostPwd = hostPwd;
    }

    public String getListenerPwd() {
        return listenerPwd;
    }

    public void setListenerPwd(String listenerPwd) {
        this.listenerPwd = listenerPwd;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getDayChoice() {
        return dayChoice;
    }

    public void setDayChoice(String dayChoice) {
        this.dayChoice = dayChoice;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public int getWeekInterval() {
        return weekInterval;
    }

    public void setWeekInterval(int weekInterval) {
        this.weekInterval = weekInterval;
    }

    public String[] getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String[] weekdays) {
        this.weekdays = weekdays;
    }

    public String getMonthChoice() {
        return monthChoice;
    }

    public void setMonthChoice(String monthChoice) {
        this.monthChoice = monthChoice;
    }

    public int getDayOfInterval() {
        return dayOfInterval;
    }

    public void setDayOfInterval(int dayOfInterval) {
        this.dayOfInterval = dayOfInterval;
    }

    public String getWeekNum() {
        return weekNum;
    }

    public void setWeekNum(String weekNum) {
        this.weekNum = weekNum;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public boolean isSmsRemind() {
        return smsRemind;
    }

    public void setSmsRemind(boolean smsRemind) {
        this.smsRemind = smsRemind;
    }

    public int getSmsRemindTime() {
        return smsRemindTime;
    }

    public void setSmsRemindTime(int smsRemindTime) {
        this.smsRemindTime = smsRemindTime;
    }

    public boolean isSmsNotice() {
        return smsNotice;
    }

    public void setSmsNotice(boolean smsNotice) {
        this.smsNotice = smsNotice;
    }

    public boolean isContainHost() {
        return containHost;
    }

    public void setContainHost(boolean containHost) {
        this.containHost = containHost;
    }

    public boolean isCallInitiative() {
        return callInitiative;
    }

    public void setCallInitiative(boolean callInitiative) {
        this.callInitiative = callInitiative;
    }
}
