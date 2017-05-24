package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.AmountService;
import com.dbkj.meet.services.inter.IAmountService;
import com.dbkj.meet.vo.AmountVo;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/03/01.
 */
public class AmountController extends Controller {

    IAmountService amountService=new AmountService();

    public void index(){
        long cid=((User)getSessionAttr(Constant.USER_KEY)).getCid();
        Result<AmountVo> result=amountService.getAmountInfo(cid);
        renderJson(result);
    }
}
