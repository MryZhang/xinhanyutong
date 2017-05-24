package com.dbkj.meet.services;

import com.dbkj.meet.dic.CallTypeEnum;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.dto.DatetimeRange;
import com.dbkj.meet.model.*;
import com.dbkj.meet.model.Package;
import com.dbkj.meet.model.Record;
import com.dbkj.meet.services.inter.ChargingService;
import com.dbkj.meet.services.inter.IChargeService;
import com.dbkj.meet.utils.DateUtil;
import com.dbkj.meet.utils.JsonUtil;
import com.dbkj.meet.utils.ThreadPoolUtil;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by DELL on 2017/03/09.
 */
public class ChargingServiceImpl implements ChargingService {

    private final Logger log= LoggerFactory.getLogger(this.getClass());

    private final IChargeService chargeService=new ChargeService();

    @Override
    public void charge(HttpServletRequest request) {
        String result= HttpKit.readData(request);
        charge(result);
    }

    @Override
    public void charge(String result) {
        final Map<String,Object> resultMap= JsonUtil.parseToMap(result);
        String meetId=resultMap.get(Constant.MEETID).toString();
        String type=resultMap.get(Constant.CALLMODE).toString();
        if(!StrKit.isBlank(meetId)&&Constant.CALL_MODE_MEET.equals(type)){
            final Record record= Record.dao.findByMeetId(meetId);

            if(record!=null){
                log.info(record.toString());
                Db.tx(new IAtom() {
                    @Override
                    public boolean run() throws SQLException {
                        return chargeback(resultMap,record);
                    }
                });
            }else{
                log.error("meet record is null.");
            }
        }
    }

    @Override
    public void chargeMonthly() {
        log.info("calculate monthly fee.");
        //获取计费当月计费模式为包月的账号
        List<ChargingMode> list=null;
        try{
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
            String name=simpleDateFormat.format(new Date());
            list=ChargingMode.dao.getNotHandledListByName(Integer.parseInt(name));
            log.info("待处理：",list);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            list=new ArrayList<>();
        }

        for(final ChargingMode chargingMode:list){
            log.info(chargingMode.toString());
            //如果是包月模式且未处理则处理
            boolean flag =Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    //计算包月费用
                    Package pk=Package.dao.findById(chargingMode.getPid());
                    BigDecimal monthRate=pk.getBigDecimal(Package.dao.CALLIN_RATE);

                    BigDecimal charge=monthRate.multiply(new BigDecimal(chargingMode.getCount()));
                    AccountBalance balance=AccountBalance.dao.findById(chargingMode.getBid());
                    balance.setBalance(balance.getBalance().subtract(charge));
                    balance.setGmtModified(new Date());
                    if(balance.update()){
                        chargingMode.setHandled(Integer.parseInt(Constant.YES));
                        if(chargingMode.update()){
                            Chargeback chargeback=new Chargeback();
                            chargeback.setCid(balance.getCid());
                            chargeback.setFee(charge.divide(new BigDecimal(-1)));
                            chargeback.setRemark("包月费用");
                            chargeback.setGmtCreated(new Date());
                            return chargeback.save();
                        }
                    }
                    return false;
                }
            });
        }
    }

    private boolean createBill(Record record,Map<String,Object> resultMap,BigDecimal fee,String rateStr){
        Bill bill=new Bill();
        bill.setRid(record.getId());
        bill.setCaller(resultMap.get(Constant.CALLER).toString());
        bill.setCallee(resultMap.get(Constant.CALLEE).toString());
        bill.setCallType(Integer.parseInt(resultMap.get(Constant.CALL_TYPE).toString()));
        bill.setCallTime(Integer.parseInt(resultMap.get(Constant.CALL_TIME).toString()));
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            bill.setCallBeginTime(sdf.parse(resultMap.get(Constant.CALL_BEGIN_TIME).toString()));
            Object callAnswerTime=resultMap.get(Constant.CALL_ANSWER_TIME);
            if(callAnswerTime!=null&&callAnswerTime.toString().length()>0){
                bill.setCallAnswerTime(sdf.parse(resultMap.get(Constant.CALL_ANSWER_TIME).toString()));
            }
            bill.setHangupTime(sdf.parse(resultMap.get(Constant.HANGUP_TIME).toString()));
        } catch (ParseException e) {
            log.error(e.getMessage(),e);
        }
        bill.setCallMode(Integer.parseInt(resultMap.get(Constant.CALL_MODE).toString()));
        bill.setAnswer(Integer.parseInt(resultMap.get(Constant.IS_ANSWER).toString()));
        bill.setGmtCreate(new Date());
        bill.setRate(rateStr);
        //计算费用
        bill.setFee(fee);
        return bill.save();
    }

    /**
     * 扣费
     */
    private boolean chargeback(Map<String,Object> resultMap, Record record){
        String callType=resultMap.get(Constant.CALL_TYPE).toString();
        long callTime=Integer.parseInt(resultMap.get(Constant.CALL_TIME).toString());
        final int uid=record.getBelong();
        final long rid=record.getId();
        //获取账户计费模式
        User user=User.dao.findById(uid);
        AccountBalance accountBalance = AccountBalance.dao.findByCompanyId(user.getCid());
        com.jfinal.plugin.activerecord.Record rd = chargeService.getAccountInfoById(accountBalance.getId());

        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        //呼入
        if(callType.equals(Constant.CALL_TYPE_CALLIN)){
            //分钟计费
            if(rd.getInt(AccountBalance.dao.CALLIN_MODE)== RateModeEnum.MINUTE.getCode()){
                //获取费率
                BigDecimal rate=rd.getBigDecimal(AccountBalance.dao.CALLIN_RATE);
                try {
                    Date before=simpleDateFormat.parse(resultMap.get(Constant.CALL_BEGIN_TIME).toString());
                    Date after=simpleDateFormat.parse(resultMap.get(Constant.HANGUP_TIME).toString());
                    callTime=DateUtil.getSecondsBetween(before,after);
                    resultMap.put(Constant.CALL_TIME,callTime);
                } catch (ParseException e) {
                    log.error(e.getMessage(),e);
                    throw new RuntimeException(e);
                }

                double minutes=Math.ceil(callTime/60.0D);
                BigDecimal amount=rate.multiply(new BigDecimal(minutes));
                //添加话单记录
                String rateStr=rate.doubleValue()+"元/分钟";
                createBill(record,resultMap,amount,rateStr);

                if(Constant.YES.equals(resultMap.get(Constant.ISANSWER))){
                    //添加扣费记录
                    Chargeback chargeback=new Chargeback();
                    chargeback.setCid(Long.parseLong(user.getCid().toString()));
                    chargeback.setFee(amount.multiply(new BigDecimal(-1)));
//                log.info("chargeback:"+amount.multiply(new BigDecimal(-1)));
                    chargeback.setRemark("呼入话费（计费模式：分钟）");
                    chargeback.setGmtCreated(new Date());
                    chargeback.save();

                    //更新会议中消费额

                    accountBalance.setBalance(accountBalance.getBalance().subtract(amount));
                    log.info("呼入扣费(分钟)："+amount.doubleValue()+",minutes:"+minutes+",phone:"+resultMap.get(Constant.CALLEE)
                            +",uid:"+uid+",rid:"+rid);
                    return accountBalance.update();
                }

            }else if(rd.getInt(AccountBalance.dao.CALLIN_MODE)==RateModeEnum.MONTH.getCode()){//包月计费
                //获取计费费率信息
                BigDecimal monthlyRate=rd.getBigDecimal(AccountBalance.dao.CALLIN_RATE);
                BigDecimal passRate=rd.getBigDecimal(AccountBalance.dao.PASS_RATE);
                String rateStr="包月："+monthlyRate.doubleValue()+"元/方，超方："+passRate.doubleValue()+"元/方";
                Map<String,Object> paraMap=new HashMap<String,Object>();

                //如果是会议记录中的第一个话单则开启一个线程处理包月计费
                paraMap.clear();
                paraMap.put(Bill.dao.RECORD_ID,rid);
                long count=Bill.dao.getCountByRecordId(paraMap);
                if(count<=0){
                    ExecutorService executorService = ThreadPoolUtil.getCacheThreadPool();
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            int minutes= PropKit.use("config.properties").getInt(Constant.CHARGING_INTERVAL,15);
                            try {
                                Thread.sleep(1000*60*minutes);
                            } catch (InterruptedException e) {
                                log.error(e.getMessage(),e);
                            }
                            chargeMonth(rid,uid);
                        }
                    });
                }
                //创建话单记录
                createBill(record,resultMap,new BigDecimal(0),rateStr.toString());
                return true;
            }
        }else{//呼出
            double minutes=Math.ceil(callTime/60.0D);
            BigDecimal rate=rd.getBigDecimal(AccountBalance.dao.CALLOUT_RATE);
            BigDecimal amount=rate.multiply(new BigDecimal(minutes));
            //添加话单记录
            String rateStr=rate.doubleValue()+"元/分钟";
            createBill(record,resultMap,amount,rateStr);
            //判断是否接通，只有接通才会产生扣费记录
            if(Constant.YES.equals(resultMap.get(Constant.ISANSWER))){
                //添加扣费记录
                Chargeback chargeback=new Chargeback();
                chargeback.setCid(Long.parseLong(user.getCid().toString()));
                chargeback.setFee(amount.multiply(new BigDecimal(-1)));
                log.info("chargeback:"+amount.multiply(new BigDecimal(-1)));
                chargeback.setRemark("呼出话费（计费模式：分钟）");
                chargeback.setGmtCreated(new Date());
                chargeback.save();

                accountBalance.setBalance(accountBalance.getBalance().subtract(amount));
                log.info("呼出扣费(分钟)："+amount.doubleValue()+",minutes:"+minutes+",phone:"+resultMap.get(Constant.CALLEE)
                        +",uid:"+uid+",rid:"+rid);
                return accountBalance.update();
            }
        }

        return true;
    }


    /**
     * 包月超方扣费
     * @param rid
     */
    private void chargeMonth(long rid,long uid){
        Map<String,Object> paraMap=new HashMap<String,Object>(1);
        paraMap.put("rid",rid);
        //获取当前会议记录所有的话单记录，并根据呼入式话单起始时间计算同时在线的最多人数
        List<Bill> billList = Bill.dao.getList(paraMap);
        List<DatetimeRange> list=new ArrayList<DatetimeRange>();
        for(Bill bill:billList){
            if(bill.getCallType()==Integer.parseInt(Constant.CALL_TYPE_CALLIN)){
                DatetimeRange range=new DatetimeRange();
                range.setBegin(bill.getCallAnswerTime());
                range.setEnd(bill.getHangupTime());
                list.add(range);
            }
        }
        int count= DateUtil.getMaxOnline(list.toArray(new DatetimeRange[list.size()]));

        //计算超方数
        AccountBalance accountBalance=AccountBalance.dao.findByCompanyId(User.dao.findById(uid).getCid());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
        com.jfinal.plugin.activerecord.Record record=chargeService.getAccountInfoById(accountBalance.getId());
        long n=count-record.getInt(AccountBalance.dao.COUNT);
        if(n>0){
            User user=User.dao.findById(uid);
            //获取计费费率
            BigDecimal rate=record.getBigDecimal(AccountBalance.dao.PASS_RATE);
            BigDecimal chargeAmount=rate.multiply(new BigDecimal(n));
            accountBalance.setBalance(accountBalance.getBalance().subtract(chargeAmount));
            log.info("扣费（包月）："+chargeAmount.doubleValue()+",uid:"+uid+",rid:"+rid);
            //添加扣费记录
            Chargeback chargeback=new Chargeback();
            chargeback.setCid(Long.parseLong(user.getCid().toString()));
            chargeback.setFee(chargeAmount);
            chargeback.setRemark("呼入话费（计费模式：包月）");
            chargeback.setGmtCreated(new Date());
            chargeback.save();
        }
    }

    @Deprecated
    private void setFee(Bill bill,Record rd){
        if(bill!=null&&rd!=null){
            User user=User.dao.findById(rd.getBelong());
            //呼出
            if(bill.getCallType().intValue()== CallTypeEnum.CALL_TYPE_CALLOUT.getCode()){
                Fee fee=Fee.dao.getCallOutFee();
                //分钟计费
                if(fee.getMode()==RateModeEnum.MINUTE.getCode()){
                    double minutes=Math.ceil(bill.getCallTime()/60.0D);
                    BigDecimal f=fee.getRate().multiply(new BigDecimal(minutes));
                    bill.setFee(f);
                    bill.setRate(fee.getRate().doubleValue()+"元/分钟");

                    //添加扣费记录
//                    Chargeback chargeback=new Chargeback();
//                    chargeback.setCid(Long.parseLong(user.getCid().toString()));
//                    chargeback.setFee(f.multiply(new BigDecimal(-1)));
//                    chargeback.setRemark("呼出话费（计费模式：分钟）");
//                    chargeback.setGmtCreated(new Date());
//                    chargeback.save();
                }

            }else{//呼入
                //获取账户计费模式
                AccountBalance accountBalance = AccountBalance.dao.findByCompanyId(user.getCid());
                com.jfinal.plugin.activerecord.Record record = getAccountBalanceInfoById(accountBalance.getId());

                List<Fee> feeList=null;
                //分钟计费
                if(record.getInt("mode")== RateModeEnum.MINUTE.getCode()){
                    Map<String,Object> paraMap=new HashMap<String,Object>();
                    paraMap.put(Fee.dao.MODE_KEY,RateModeEnum.MINUTE.getCode());
                    paraMap.put(Fee.dao.TYPE_KEY, CallTypeEnum.CALL_TYPE_CALLIN.getCode());
                    feeList=Fee.dao.getFee(paraMap);

                    Fee fee=null;
                    for(Fee f:feeList){
                        if(f.getMode()==RateModeEnum.MINUTE.getCode()){
                            fee=f;
                            break;
                        }
                    }
                    double minutes=Math.ceil(bill.getCallTime()/60.0D);
                    BigDecimal f=fee.getRate().add(new BigDecimal(minutes));
                    bill.setFee(f);
                    bill.setRate(fee.getRate().doubleValue()+"元/分钟");
                    //添加扣费记录
//                    Chargeback chargeback=new Chargeback();
//                    chargeback.setCid(Long.parseLong(user.getCid().toString()));
//                    chargeback.setFee(f);
//                    chargeback.setRemark("呼出话费（计费模式：分钟）");
//                    chargeback.setGmtCreated(new Date());
//                    chargeback.save();
                }else{//包月计费
                    bill.setFee(new BigDecimal(0));

                    Map<String,Object> paraMap=new HashMap<String,Object>();
                    paraMap.put(Fee.dao.MODE_KEY,RateModeEnum.MONTH.getCode());
                    paraMap.put(Fee.dao.TYPE_KEY,CallTypeEnum.CALL_TYPE_CALLIN.getCode());
                    feeList=Fee.dao.getFee(paraMap);
                    StringBuilder rate=new StringBuilder(20);
                    for(Fee fee:feeList){
                        if(rate.length()>0){
                            rate.append(",");
                        }
                        rate.append(fee.getRemark()+":");
                        rate.append(fee.getRate().doubleValue()+"元/方");
                    }
                    bill.setRate(rate.toString());
                }
            }
        }
    }

    /**
     * 根据账户余额id获取账户计费相关信息
     * @param id
     * @return
     */
    @Deprecated
    private com.jfinal.plugin.activerecord.Record getAccountBalanceInfoById(Long id){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
        Date now=new Date();
        com.jfinal.plugin.activerecord.Record record = AccountBalance.dao.getAccountBalanceInfoById(id,simpleDateFormat.format(now));
        //如果没有查到当前月份的计费信息，则获取该账号最近一次计费模式信息并新增
        if(record==null){
            ChargingMode chargingMode=ChargingMode.dao.getLastByAccountBalanceId(id);
            chargingMode.setGmtCreate(now);
            chargingMode.setRemark("复制");
            ChargingMode.dao.insert(chargingMode);
            record=AccountBalance.dao.getAccountBalanceInfoById(id,simpleDateFormat.format(now));
        }
        record.set("modeDesc", RateModeEnum.codeOf((Integer) record.get("mode")).getDesc());
        return record;
    }


}
