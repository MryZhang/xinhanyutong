package com.dbkj.meet.converter;

import com.dbkj.meet.dic.CallModeEnum;
import com.dbkj.meet.dic.CallTypeEnum;
import com.dbkj.meet.model.Bill;
import com.dbkj.meet.vo.BillDetailVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * Created by DELL on 2017/03/03.
 */
public class BillDetailConverter {

    private final static Logger log= LoggerFactory.getLogger(BillDetailConverter.class);

    public static BillDetailVo to(Bill bill){
        if(bill==null){
            throw new IllegalArgumentException("bill cannot be null");
        }
        BillDetailVo billDetailVo=new BillDetailVo();
        billDetailVo.setId(bill.getId());
        billDetailVo.setName(bill.getStr("name"));
        billDetailVo.setCallee(bill.getCallee());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
        try{
            if(bill.getCallBeginTime()!=null){
                billDetailVo.setStartTime(simpleDateFormat.format(bill.getCallBeginTime()));
            }
            if(bill.getCallAnswerTime()!=null){
                billDetailVo.setAnswerTime(simpleDateFormat.format(bill.getCallAnswerTime()));
            }
            if(bill.getHangupTime()!=null){
                billDetailVo.setEndTime(simpleDateFormat.format(bill.getHangupTime()));
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

        billDetailVo.setFee(bill.getFee().doubleValue());
        billDetailVo.setCallTime((int) Math.ceil(bill.getCallTime()/60.0D));
        billDetailVo.setRate(bill.getRate());
        billDetailVo.setCallType(CallTypeEnum.codeOf(bill.getCallType()).getDesc());

        return billDetailVo;
    }
}
