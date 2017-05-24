package com.dbkj.meet.validator;

import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.services.PackageServiceImpl;
import com.dbkj.meet.services.inter.PackageService;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.PackageVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by DELL on 2017/04/03.
 */
public class PackageValidator extends Validator {

    private final PackageService packageService=new PackageServiceImpl();

    @Override
    protected void validate(Controller controller) {
        Res res = I18n.use("zh_CN");
        PackageVo packageVo=controller.getBean(PackageVo.class,"pv");
        String name=packageVo.getName();
        if(StrKit.isBlank(name)){
            addError("nameMsg",res.get("package.name.not.empty"));
        }else if(ValidateUtil.validateSpecialString(name)){
            addError("nameMsg",res.get("package.name.format.wrong"));
        }else if(packageService.exist(name).getResult()){
            addError("nameMsg",res.get("package.name.repeat"));
        }
        Integer callInMode=packageVo.getCallInMode();
        if(callInMode==null){
            addError("callInModeMsg",res.get("package.callinmode.not.empty"));
        }else if(callInMode!=RateModeEnum.MINUTE.getCode()&&callInMode!=RateModeEnum.MONTH.getCode()){
            addError("callInModeMsg",res.get("package.callinmode.wrong"));
        }
        BigDecimal callInRate=packageVo.getCallInRate();
        if(callInRate==null){
            addError("callInRateMsg",res.get("package.callinrate.not.empty"));
        }else if(callInRate.doubleValue()<0){
            addError("callInRateMsg",res.get("package.callinrate.format.wrong"));
        }
        BigDecimal callOutRate=packageVo.getCallOutRate();
        if(callOutRate==null){
            addError("callOutRateMsg",res.get("package.passrate.not.empty"));
        }else if(callOutRate.doubleValue()<0){
            addError("callOutMsg",res.get("package.passrate.format.wrong"));
        }
        BigDecimal smsRate=packageVo.getSmsRate();
        if(smsRate==null){
            addError("smsRateMsg",res.get("package.smsrate.not.empty"));
        }else if(smsRate.doubleValue()<0){
            addError("smsRateMsg",res.get("package.smsrate.format.wrong"));
        }
    }

    @Override
    protected void handleError(Controller controller) {
        controller.keepBean(PackageVo.class,"pv");
        Map<String,Object> attrs=packageService.showAdd();
        controller.setAttrs(attrs);
        controller.render("add.html");
    }
}
