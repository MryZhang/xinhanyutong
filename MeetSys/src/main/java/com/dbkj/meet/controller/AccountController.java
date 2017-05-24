package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.AccountService;
import com.dbkj.meet.services.inter.IAccountService;
import com.dbkj.meet.vo.BillDetailVo;
import com.dbkj.meet.vo.BillVo;
import com.jfinal.core.Controller;

import java.io.File;

/**
 * Created by DELL on 2017/03/01.
 */
public class AccountController extends Controller {

    private IAccountService accountService=new AccountService();

    public void bill(){
        double total = accountService.getTotalConsume(this);
        setAttr("total",total);
        render("bill.html");
    }

    public void billList(){
        BillVo billVo=accountService.getBillPage(getRequest().getParameterMap(), (User) getSessionAttr(Constant.USER_KEY));
        renderJson(billVo);
    }

    public void billDetail(){
        Long id=getParaToLong();
        BillVo<BillDetailVo> detailList=accountService.getBillDetail(id,getRequest().getParameterMap());
        renderJson(detailList);
    }

    /**
     * 导出账单记录
     */
    public void exportBill(){
        File file=accountService.exportBillExcel(getParaMap());
        renderFile(file);
    }
}
