package com.dbkj.meet.controller;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.AdminInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.model.AccessNum;
import com.dbkj.meet.model.Company;
import com.dbkj.meet.services.CompanyService;
import com.dbkj.meet.services.inter.ICompanyService;
import com.dbkj.meet.validator.CompanyValidator;
import com.dbkj.meet.vo.CompanyVo;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;

import java.util.List;

/**
 * Created by MrQin on 2016/11/8.
 */
@Before({AdminInterceptor.class})
@Clear({InfoInterceptor.class})
public class CompanyController extends Controller{

    private ICompanyService companyService= Enhancer.enhance(CompanyService.class);

    public void addnew(){
        String referrer=getPara("referrer");
        setAttr("referrer",referrer);
        List<AccessNum> accessNums=companyService.getNumList();
        setAttr("accessNums",accessNums);
        render("/pages/admin/addCompany.html");
    }

    @Before({POST.class})
    public void add(){
        CompanyVo company=getBean(CompanyVo.class,"cv");
        companyService.addCompany(company);
        String referrer=getPara("referrer");
        redirect(referrer);
    }

    public void exists(){
        String name=getPara("name");
        Long id=getParaToLong("id");
        boolean flag=true;
        if(id!=null){
            flag=companyService.isExistCompany(id,name);
        }else{
            flag = companyService.isExistCompany(name);
        }
        renderJson(new Result(flag));
    }

    public void getCompany(){
        Long id=getParaToLong(0);
        Company company=companyService.getCompany(id);
        renderJson(company);
    }

    public void modify(){
        Long id=getParaToLong();
        Company company=companyService.getCompany(id);
        setAttr("cv",company);
        String referrer=getPara("referrer");
        setAttr("referrer",referrer);
        List<AccessNum> accessNums=companyService.getNumList();
        setAttr("accessNums",accessNums);
        render("/pages/admin/updateCompany.html");
    }

    @Before({POST.class, CompanyValidator.class})
    public void update(){
        Company company=getModel(Company.class);
        Result<Company> result=companyService.updateCompany(company);
        String referrer=getPara("referrer");
        redirect(referrer);
    }

    public void delete(){
        Long id=getParaToLong(0);
        boolean result=companyService.deleteCompany(id);
        renderJson("{\"result\":"+result+"}");
    }
}
