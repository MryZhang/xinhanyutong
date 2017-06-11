package com.dbkj.meet.services.ordermeet;

import com.dbkj.meet.dic.AttendeeType;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.MeetState;
import com.dbkj.meet.dic.MeetType;
import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.MessageServiceImpl;
import com.dbkj.meet.services.SMTPServiceImpl;
import com.dbkj.meet.services.common.MeetManager;
import com.dbkj.meet.services.inter.ISMTPService;
import com.dbkj.meet.services.inter.MessageService;
import com.dbkj.meet.utils.DateUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by DELL on 2017/03/27.
 */
public abstract class BaseOrderMeetType {

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    /**
     * 从前台接受预约的会议数据
     */
    private OrderModel orderModel;

    /**
     * 呼叫号码
     */
    private String callNum;

    /**
     * 显示号码
     */
    private String showNum;

    /**
     * 当前用户
     */
    private User user;

    private Date now;

    private MessageService messageService;

    private ISMTPService smtpService;

    protected List<OrderAttendee> orderAttendeeList;

    protected OrderMeet orderMeet;

    public final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 添加预约会议的操作
     * @return
     */
    public abstract boolean add();


    /**
     * 插入会议记录，和参会人记录，以及设置预约会议记录中rid字段
     * @return
     */
    public boolean addRecord() {
        if(orderMeet==null){
            throw new RuntimeException("方法调用顺序有误，请先添加预约会议记录");
        }
        com.dbkj.meet.model.Record record = new com.dbkj.meet.model.Record();
        record.setSubject(orderMeet.getSubject());
        record.setHostName(orderMeet.getHostName());
        record.setHost(orderMeet.getHostNum());
        record.setIsRecord(orderMeet.getIsRecord());
        record.setBelong(orderMeet.getBelong());
        record.setStatus(MeetState.OREDER_RECORD.getStateCode());
        record.setHostPwd(orderMeet.getHostPwd());
        record.setMeetNums(MeetManager.getInstance().getMeetNums());
        record.setType(MeetType.ORDER_MEET.getCode());
        record.setGmtCreate(new Date());
        record.setOid(Integer.parseInt(orderMeet.getId().toString()));
        if (record.save()) {
            List<Attendee> attendeeList = new ArrayList<>();
            if(orderAttendeeList==null){
                throw new RuntimeException("方法调用顺序有误，请获取预约会议参会人");
            }
            for (OrderAttendee orderAttendee : orderAttendeeList) {
                Attendee attendee = new Attendee();
                attendee.setName(orderAttendee.getName());
                attendee.setRid(Integer.parseInt(record.getId().toString()));
                attendee.setPhone(orderAttendee.getPhone());
                attendee.setType(orderAttendee.getType());
                attendee.setStatus(Integer.parseInt(Constant.WATING_CALL));
                attendeeList.add(attendee);
            }
            Db.batchSave(attendeeList, 100);

            orderMeet.setRid(Integer.parseInt(record.getId().toString()));
            orderMeet.setGmtModified(new Date());
            System.out.println(System.currentTimeMillis() + ":" + record);
            return orderMeet.update();
        }
        return false;
    }

    protected String getRemindTime(String startTime,int minutes){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");
        try {
            Date time=timeFormat.parse(startTime);
            Date remindTime=DateUtil.addByMinutes(time,minutes*(-1));
            String remindTimeStr=simpleDateFormat.format(remindTime);
            remindTimeStr=remindTimeStr.substring(0,remindTimeStr.lastIndexOf(":"));
            return remindTimeStr.split(" ")[1];
        } catch (ParseException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public int getCount(int[] result){
        int count=0;
        if(result!=null){
            for(int i=0;i<result.length;i++){
                count+=result[i];
            }
        }
        return count;
    }

    protected boolean addAttendees(){
        Long pid=orderMeet.getId();
        initAttendeeList();
        for(int i=0,len=orderAttendeeList.size();i<len;i++){
            OrderAttendee orderAttendee=orderAttendeeList.get(i);
            orderAttendee.setOid(Integer.parseInt(pid.toString()));
        }
        int[] results=Db.batchSave(orderAttendeeList, 100);
        return getCount(results)==orderAttendeeList.size();
    }

    /**
     * 发送通知
     * @param isSmsNotice
     * @param emailNotice
     * @param containHost
     * @param orid
     * @param list
     * @param uid
     */
    void notice(boolean isSmsNotice,boolean emailNotice, boolean containHost, Long orid, List<OrderAttendee> list,Long uid){
        List<String> plist=null;
        OrderMeet orderMeet=null;
        if(isSmsNotice){
            plist=new ArrayList<>(list.size());
            orderMeet=OrderMeet.dao.findById(orid);
            if(list!=null&&list.size()>0){
                if(messageService==null){
                    messageService=new MessageServiceImpl();
                }
                for(OrderAttendee attendee:list){
                    if(!containHost&&attendee.getType()== AttendeeType.HOST.getCode()){
                        continue;
                    }
                    messageService.sendOrderSms(orderMeet,attendee.getPhone(),attendee.getName());
                    plist.add(attendee.getPhone());
                }
            }

        }
        if(emailNotice){
            if(plist==null){
                plist=new ArrayList<>(list.size());
                orderMeet=OrderMeet.dao.findById(orid);
                for(OrderAttendee attendee:list){
                    if(!containHost&&attendee.getType()==AttendeeType.HOST.getCode()){
                        continue;
                    }
                    plist.add(attendee.getPhone());
                }
            }
            //根据号码去查询获取邮箱
            List<String> toList= Employee.dao.getEmailByUsername(plist);
            if(toList.size()>0){
                if(smtpService==null){
                    smtpService=new SMTPServiceImpl();
                }
                smtpService.sendMail(uid,toList.toArray(new String[toList.size()]),orderMeet);
            }
        }
    }

    /**
     * 根据提交的数据创建预约会议记录的数据
     * @return
     */
    OrderMeet initOrderMeet(){
        if(orderModel==null){
            throw new RuntimeException("orderModel cannot be null.");
        }
        if(orderMeet==null){
            orderMeet=new OrderMeet();
            orderMeet.setSubject(orderModel.getSubject());
            orderMeet.setHostNum(orderModel.getHostNum());
            orderMeet.setIsRecord(orderModel.getIsRecord());
            orderMeet.setBelong(Integer.parseInt(user.getId().toString()));
            orderMeet.setHostName(orderModel.getHostName());
            orderMeet.setStartTime(orderModel.getStartTime());
            orderMeet.setHostPwd(orderModel.getHostPwd());
            orderMeet.setIsCallInitiative(orderModel.isCallInitiative()?Integer.parseInt(Constant.YES):Integer.parseInt(Constant.NO));

            orderMeet.setGmtCreated(now);
            if(orderModel.isSmsRemind()){
                orderMeet.setSmsRemind(Integer.parseInt(Constant.YES));
            }

            orderMeet.setStartTime(orderModel.getStartTime());
            orderMeet.setSmsRemind(orderModel.isSmsRemind()?Integer.parseInt(Constant.YES):Integer.parseInt(Constant.NO));
        }
        return orderMeet;
    }

    /**
     * 根据提交的数据创建预约会议参会人数据
     * @return
     */
    private void initAttendeeList(){
        if(orderAttendeeList==null){
            orderAttendeeList=new ArrayList<>();
            String phoneField="phone";
            String nameField="name";
            //参会人
            orderAttendeeList=new ArrayList<OrderAttendee>();
            //先将主持人添加到参会人中
            OrderAttendee host=new OrderAttendee();
            host.setName(orderModel.getHostName());
            host.setPhone(orderModel.getHostNum());
            host.setType(AttendeeType.HOST.getCode());
            orderAttendeeList.add(host);
            //解析参会人json字符串
            String constr=orderModel.getContacts();
            if(!StrKit.isBlank(constr)){
                Gson gson=new Gson();
                List<Map<String,Object>> contacts = gson.fromJson(constr,new TypeToken<List<HashMap<String,Object>>>(){}.getType());
                for(Map<String,Object> map:contacts){
                    OrderAttendee orderAttendee=new OrderAttendee();
                    orderAttendee.setPhone(map.get(phoneField).toString());
                    orderAttendee.setName(map.get(nameField).toString());
                    orderAttendee.setType(AttendeeType.ATTENDEE.getCode());
                    orderAttendeeList.add(orderAttendee);
                }
            }
        }
    }

    /**
     * 获取定时组名
     * @return
     */
    public String getJobGroup(){
         return user.getUsername();
    }

    /**
     * 获取定时任务名称
     * @return
     */
    public String getJobName(){
        return sdf.format(getNow());
    }


    public OrderModel getOrderModel() {
        return orderModel;
    }

    public void setOrderModel(OrderModel orderModel) {
        this.orderModel = orderModel;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public List<OrderAttendee> getOrderAttendeeList(){
        return this.orderAttendeeList;
    }
}
