package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.AdminInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.ChargeService;
import com.dbkj.meet.services.inter.IChargeService;
import com.dbkj.meet.validator.ChangePackageValidator;
import com.dbkj.meet.validator.ChargeValidator;
import com.dbkj.meet.vo.ChangePackageVo;
import com.dbkj.meet.vo.ChargeVo;
import com.dbkj.meet.vo.ChargesDetailVo;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.omg.PortableServer.POA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by DELL on 2017/02/25.
 */
public class ChargeController extends BaseAdminController {

    private IChargeService chargeService=new ChargeService();

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    public void index(){
        Page<Record> pageData=chargeService.getPageOfBalance(getRequest());
        setAttr("pages",pageData);
        render("list.html");
    }

    public void detail(){
        Long id=getParaToLong(0);
        ChargesDetailVo record=chargeService.getAccountBalanceInfoById(id);
        setAttr("account",record);
        String queryString=getRequest().getQueryString();
        setAttr("query",queryString);
        render("detail.html");
    }

    /**
     * 修改计费模式
     */
    public void showChange(){
        Long bid=getParaToLong();
        Map<String,Object> map=chargeService.getChangePackageData(bid);
        setAttrs(map);
        String referrer=getPara("referrer");
        setAttr("referrer",referrer);
        render("changePackage.html");
    }

    @Before({POST.class, ChangePackageValidator.class})
    public void changePackage(){
        ChangePackageVo changePackageVo=getBean(ChangePackageVo.class,"cv");
        chargeService.changePackage(changePackageVo);
        String referrer=getPara("referrer");
        redirect(referrer);
    }

    public void chargePage(){
        Long id=getParaToLong();
        Record record=chargeService.getAccountBalanceAndCompanyName(id);
        setAttr("account",record);
        String queryString=getRequest().getQueryString();
        setAttr("query",queryString);
        render("charge.html");
    }

    @Before(ChargeValidator.class)
    public void doCharge(){
        ChargeVo chargeVo=getBean(ChargeVo.class);
        Long id=((User)getSessionAttr(Constant.USER_KEY)).getId();
        chargeService.charge(chargeVo,id);
        String queryString=null;
        try {
            queryString= URLDecoder.decode(getPara("queryStr"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
            queryString="";
        }
        redirect("/admin/charge"+ (StrKit.isBlank(queryString)?"":"?"+queryString));
    }

    public void record(){
        Map<String,Object> result=chargeService.getChargeRecordPage(getRequest().getParameterMap());
        setAttrs(result);
        render("record.html");
    }
}
