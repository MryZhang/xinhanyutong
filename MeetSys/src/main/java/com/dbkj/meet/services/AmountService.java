package com.dbkj.meet.services;

import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.AccountBalance;
import com.dbkj.meet.services.inter.IAmountService;
import com.dbkj.meet.services.inter.IChargeService;
import com.dbkj.meet.vo.AmountVo;
import com.dbkj.meet.vo.ChargesDetailVo;
import com.jfinal.plugin.activerecord.Record;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DELL on 2017/03/01.
 */
public class AmountService implements IAmountService {

    private IChargeService chargeService=new ChargeService();

    @Override
    public AmountVo getAmount(long cid) {
        AccountBalance accountBalance=AccountBalance.dao.findByCompanyId(cid);

        com.jfinal.plugin.activerecord.Record record=chargeService.getAccountInfoById(accountBalance.getId());

        AmountVo amountVo=new AmountVo();
        amountVo.setBalance(accountBalance.getBalance().doubleValue());

        int mode=record.getInt(AccountBalance.dao.CALLIN_MODE);
        amountVo.setMode(RateModeEnum.codeOf(mode).getDesc());
        //如果是包月，则还要显示包月方数
        if(mode==RateModeEnum.MONTH.getCode()){
            amountVo.setCount(Integer.parseInt(record.getLong(AccountBalance.dao.COUNT).toString()));
        }
        return amountVo;
    }

    @Override
    public Result<AmountVo> getAmountInfo(long cid) {
        AmountVo amountVo= getAmount(cid);
        return new Result<>(true,amountVo);
    }
}
