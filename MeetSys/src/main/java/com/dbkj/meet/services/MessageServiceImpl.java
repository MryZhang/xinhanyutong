package com.dbkj.meet.services;

import com.dbkj.meet.dic.CallTypeEnum;
import com.dbkj.meet.dic.MessageConstant;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.MessageService;
import com.dbkj.meet.utils.DateUtil;
import com.dbkj.meet.utils.MessageUtil;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by DELL on 2017/03/23.
 */
public class MessageServiceImpl implements MessageService {

    private final Logger log= LoggerFactory.getLogger(this.getClass());

    @Override
    public void sendMsg(HttpServletRequest request) {
        Long rid=null;
        try {
            rid = Long.parseLong(request.getParameter("rid"));
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        String phone=request.getParameter("phone");
        sendMsg(rid,phone);
    }

    @Override
    public void sendMsg(Long rid, String phone) {
        if(rid!=null&&ValidateUtil.validateMobilePhone(phone)){
            Record record = Record.dao.findById(rid);
            //短息内容
            StringBuilder smsContent=new StringBuilder(250);
            smsContent.append(record.getHostName());
            smsContent.append("邀请您于");
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            smsContent.append(simpleDateFormat.format(new Date()));
            smsContent.append("参加");
            smsContent.append(record.getSubject());
            smsContent.append(",请拨打");
            Company company=Company.dao.findById(User.dao.findById(record.getBelong()).getCid());
            smsContent.append(AccessNum.dao.findById(company.getCallNum()).getNum());
            smsContent.append("并输入");
            smsContent.append(record.getHostPwd());
            smsContent.append("参加会议。");

            Map<String,Object> paraMap=new HashMap<String,Object>();
            paraMap.put(MessageConstant.MOBILE,phone);
            paraMap.put(MessageConstant.SMS_CONTENT,smsContent.toString());

            Map<String,Object> resultMap= MessageUtil.sendMessage(paraMap);
            //短信发送成功，扣费
            if(resultMap.get(MessageConstant.STATUS).equals(MessageConstant.SUCCESS)){
//                charging(record,company.getId());
                charging(rid,company.getId(),smsContent.toString());
            }else{
                //发送失败，重发
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(),e);
                }
                sendMsg(rid,phone);
            }
        }
    }

    private void charging(final Long rid, final Long cid, final String msg){
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Fee fee=getMessageFee();
                //添加短信发送记录
                Sms sms=new Sms();
                sms.setRid(rid!=null?Integer.parseInt(rid.toString()):null);
                sms.setMsg(msg);
                sms.setFee(fee.getRate());
                sms.setGmtCreate(new Date());
                if(sms.save()){
                    //扣除账户中短信费用
                    AccountBalance accountBalance = AccountBalance.dao.findByCompanyId(cid);
                    accountBalance.setBalance(accountBalance.getBalance().subtract(fee.getRate()));
                    if(accountBalance.update()){
                        //添加扣费记录
                        Chargeback chargeback=new Chargeback();
                        chargeback.setFee(fee.getRate().multiply(new BigDecimal(-1)));
                        chargeback.setCid(cid);
                        chargeback.setGmtCreated(new Date());
                        chargeback.setRemark("短信费用");
                        return chargeback.save();
                    }
                }
                return false;
            }
        });
    }


    @Override
    public void sendSms(Long orid, String phone) {
        OrderMeet orderMeet=OrderMeet.dao.findById(orid);
        sendSms(orderMeet,phone);
    }

    @Override
    public void sendSms(OrderMeet orderMeet, String phone) {
        if(orderMeet!=null&&ValidateUtil.validateMobilePhone(phone)){
            //短信内容
            StringBuilder smsContent=new StringBuilder(250);
            smsContent.append(orderMeet.getHostName());
            smsContent.append("邀请您于");
            smsContent.append(getOrderMeetStartTime(orderMeet));
            smsContent.append("参加");
            smsContent.append(orderMeet.getSubject());
            smsContent.append("，");
            Company company=Company.dao.findById(User.dao.findById(orderMeet.getBelong()).getCid());
            smsContent.append("请注意接听");
            smsContent.append(AccessNum.dao.findById(company.getCallNum()).getNum());
            smsContent.append("的来电");

            Map<String,Object> paraMap=new HashMap<String,Object>();
            paraMap.put(MessageConstant.MOBILE,phone);
            paraMap.put(MessageConstant.SMS_CONTENT,smsContent.toString());

            Map<String,Object> resultMap= MessageUtil.sendMessage(paraMap);
            //短信发送成功，扣费
            if(resultMap.get(MessageConstant.STATUS).equals(MessageConstant.SUCCESS)){
                orderMeetCharging(company.getId(),smsContent.toString());
            }else{
                //发送失败，重发
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(),e);
                }
                sendSms(orderMeet,phone);
            }
        }
    }

    private void orderMeetCharging(final Long cid, final String msg){
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Fee fee=getMessageFee();
                //添加短信发送记录
                Sms sms=new Sms();
                sms.setMsg(msg);
                sms.setFee(fee.getRate());
                sms.setGmtCreate(new Date());
                if(sms.save()){
                    //扣除账户中短信费用
                    AccountBalance accountBalance = AccountBalance.dao.findByCompanyId(cid);
                    accountBalance.setBalance(accountBalance.getBalance().subtract(fee.getRate()));
                    if(accountBalance.update()){
                        //添加扣费记录
                        Chargeback chargeback=new Chargeback();
                        chargeback.setFee(fee.getRate().multiply(new BigDecimal(-1)));
                        chargeback.setCid(cid);
                        chargeback.setGmtCreated(new Date());
                        chargeback.setRemark("短信费用");
                        return chargeback.save();
                    }
                }
                return false;
            }
        });
    }


    /**
     * 获取预约会议开始时间
     * @param orderMeet
     * @return
     */
    private String getOrderMeetStartTime(OrderMeet orderMeet){
        Map<String,Object> params=new HashMap<>();
        params.put("oid",orderMeet.getId());
        List<Schedule> scheduleList=Schedule.dao.getScheduleList(params);
        Iterator<Schedule> itr=scheduleList.iterator();
        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
        int count=scheduleList.size();
        int n=0;
        StringBuilder temp=new StringBuilder();
        while (itr.hasNext()){
            n++;
            Schedule sch=itr.next();
            int type=sch.getOrderType();
            String interval=sch.getInterval();
            switch (type){
                case 0://无重复周期
                    return orderMeet.getStartTime();
                case 1://重复周期为天
                    if("workday".equals(interval)){
                        return "每个工作日"+orderMeet.getStartTime();
                    }else{
                        return "每隔"+interval+"天"+orderMeet.getStartTime();
                    }
                case 2://重复周期为星期
                    if(temp.length()==0){
                        temp.append("每隔"+interval+"周周"+ DateUtil.getWeekday(sch.getOrderNum()));
                    }else{
                        temp.append("、周"+DateUtil.getWeekday(sch.getOrderNum()));
                    }
                    if(n==count){
                        temp.append(" "+orderMeet.getStartTime());
                        return temp.toString();
                    }
                case 3://重复周期为月
                    String orderNum=sch.getOrderNum();
                    if(orderNum==null){
                        return "每个月第"+interval+"天"+orderMeet.getStartTime();
                    }else{
                        if("L".equals(interval)){
                            return "每个月最后一周"+DateUtil.getWeekdayByNum(Integer.parseInt(orderNum))+" "+orderMeet.getStr("startTime");
                        }else{
                            return "每个月第"+interval+"周周"+DateUtil.getWeekdayByNum(Integer.parseInt(orderNum))+" "+orderMeet.getStr("startTime");
                        }
                    }
            }
        }
        return temp.toString();
    }

    /**
     * 获取短息费率
     * @return
     */
    private Fee getMessageFee(){
        Map<String,Object> map=new HashMap<>();
        map.put(Fee.dao.TYPE_KEY, CallTypeEnum.CALL_TYPE_MESSAGE.getCode());
        List<Fee> feeList=Fee.dao.getFee(map);
        return feeList.isEmpty()?null:feeList.get(0);
    }
}
