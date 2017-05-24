package com.dbkj.meet.validator;

import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.model.AccessNum;
import com.dbkj.meet.model.Fee;
import com.dbkj.meet.model.Package;
import com.dbkj.meet.services.CompanyService;
import com.dbkj.meet.services.inter.ICompanyService;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.CompanyVo;
import com.dbkj.meet.vo.RateVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

import java.util.List;

/**
 * Created by DELL on 2017/04/05.
 */
public class CompanyValidator extends Validator {

    private final ICompanyService companyService=new CompanyService();

    @Override
    protected void validate(Controller controller) {
        CompanyVo companyVo=controller.getBean(CompanyVo.class,"cv");
        //判断当前操作是否是添加操作
        String url=controller.getRequest().getRequestURI();
        boolean isAdd=url.contains("add");

        Res res= I18n.use("zh_CN");
        String cname=companyVo.getName();
        if(StrKit.isBlank(cname)){
            addError("cnameMsg",res.get("company.name.not.empty"));
        }else if(ValidateUtil.validateSpecialString(cname)){
            addError("cnameMsg",res.get("company.name.contain.special.word"));
        }else if(companyService.isExistCompany(cname)){
            if(isAdd){
                if(companyService.isExistCompany(cname)){
                    addError("cnameMsg",res.get("company.name.repeat"));
                }
            }else if(companyService.isExistCompany(companyVo.getId(),cname)){
                addError("cnameMsg",res.get("company.name.repeat"));
            }
        }
        Integer callNum=companyVo.getCallNum();
        if(callNum==null){
            addError("callNumMsg",res.get("company.callnum.not.empty"));
        }else if(AccessNum.dao.findById(callNum)==null){
            addError("callNumMsg",res.get("company.callnum.not.exists"));
        }
        Integer showNum=companyVo.getShowNum();
        if(showNum==null){
            addError("showNumMsg",res.get("company.shownum.not.empty"));
        }else if(AccessNum.dao.findById(showNum)==null){
            addError("showNumMsg",res.get("company.shownum.not.exists"));
        }
        if(isAdd){
            Long pid=companyVo.getPid();
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
                    Integer count=companyVo.getCount();
                    if(count==null){
                        addError("countMsg",res.get("company.count.not.empty"));
                    }else if(count<=0){
                        addError("countMsg",res.get("company.count.error"));
                    }
                }
            }
        }
    }

    @Override
    protected void handleError(Controller controller) {
        controller.keepBean(CompanyVo.class,"cv");
        controller.keepPara("referrer");
        //是否显示包月方数
        CompanyVo companyVo=controller.getBean(CompanyVo.class,"cv");
        Package pk=Package.dao.findById(companyVo.getPid());
        boolean flag=false;
        if(pk!=null){
            Fee fee=Fee.dao.findById(pk.getCallin());
            if(fee.getMode()== RateModeEnum.MONTH.getCode()){
                flag=true;
            }
        }
        controller.setAttr("showCount",flag);
        List<AccessNum> accessNums=companyService.getNumList();
        controller.setAttr("accessNums",accessNums);
        //判断当前操作是否是添加操作
        String url=controller.getRequest().getRequestURI();
        boolean isAdd=url.contains("add");
        if(isAdd){
            controller.render("/pages/admin/addCompany.html");
        }else{
            controller.render("/pages/admin/updateCompany.html");
        }

    }
}
