package com.dbkj.meet.validator;

import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.model.Fee;
import com.dbkj.meet.model.Package;
import com.dbkj.meet.vo.ChangePackageVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.validate.Validator;

/**
 * Created by DELL on 2017/04/06.
 */
public class ChangePackageValidator extends Validator {
    @Override
    protected void validate(Controller controller) {
        ChangePackageVo changePackageVo=controller.getBean(ChangePackageVo.class,"cv");
        Res res= I18n.use("zh_CN");
        Long pid=changePackageVo.getPid();
        Package pk=Package.dao.findById(pid);
        if(pid==null){
            addError("pidMsg",res.get("company.pid.not.empty"));
        }if(pk==null){
            addError("pidMsg",res.get("company.pid.error"));
        }
        //如果是包月计费还要验证包月方数
        if(pk!=null){
            Fee fee=Fee.dao.findById(pk.getCallin());
            if(fee.getMode()== RateModeEnum.MONTH.getCode()){
                Long count=changePackageVo.getCount();
                if(count==null){
                    addError("countMsg",res.get("company.count.not.empty"));
                }else if(count<=0){
                    addError("countMsg",res.get("company.count.error"));
                }
            }
        }
    }

    @Override
    protected void handleError(Controller controller) {
        controller.keepBean(ChangePackageVo.class,"cv");
        controller.keepPara("showCount","referrer");
        controller.render("changePackage.html");
    }
}
