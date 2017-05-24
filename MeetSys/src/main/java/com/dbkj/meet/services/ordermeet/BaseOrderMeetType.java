package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.model.OrderMeet;
import com.dbkj.meet.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DELL on 2017/03/27.
 */
public abstract class BaseOrderMeetType {

    private final Logger logger= LoggerFactory.getLogger(BaseOrderMeetType.class);

    /**
     * 呼叫号码
     */
    protected String callNum;
    /**
     * 显示号码
     */
    protected String showNum;
    /**
     * 呼叫时是否包含主持人
     */
    protected boolean containHost;
    /**
     * 定时任务名称
     */
    protected String jname;
    /**
     * 定时任务组名称
     */
    protected String jgroup;
    /**
     * 预约会议自己录
     */
    protected OrderMeet orderMeet;
    /**
     * 会议前多久时间提醒
     */
    protected int smsRemindTime;

    public abstract boolean add();

    protected String getRemindTime(String startTime,int minutes){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");
        try {
            Date time=timeFormat.parse(startTime);
            Date remindTime= DateUtil.addByMinutes(time,minutes*(-1));
            String remindTimeStr=simpleDateFormat.format(remindTime);
            remindTimeStr=remindTimeStr.substring(0,remindTimeStr.lastIndexOf(":"));
            return remindTimeStr.split(" ")[1];
        } catch (ParseException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String getCallNum() {
        return callNum;
    }

    public void setCallNum(String callNum) {
        this.callNum = callNum;
    }

    public String getShowNum() {
        return showNum;
    }

    public void setShowNum(String showNum) {
        this.showNum = showNum;
    }

    public boolean isContainHost() {
        return containHost;
    }

    public void setContainHost(boolean containHost) {
        this.containHost = containHost;
    }

    public String getJname() {
        return jname;
    }

    public void setJname(String jname) {
        this.jname = jname;
    }

    public String getJgroup() {
        return jgroup;
    }

    public void setJgroup(String jgroup) {
        this.jgroup = jgroup;
    }

    public OrderMeet getOrderMeet() {
        return orderMeet;
    }

    public void setOrderMeet(OrderMeet orderMeet) {
        this.orderMeet = orderMeet;
    }

    public int getSmsRemindTime() {
        return smsRemindTime;
    }

    public void setSmsRemindTime(int smsRemindTime) {
        this.smsRemindTime = smsRemindTime;
    }
}
