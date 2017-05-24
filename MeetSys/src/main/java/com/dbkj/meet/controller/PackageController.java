package com.dbkj.meet.controller;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.AdminInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.interceptors.SAdminInterceptor;
import com.dbkj.meet.services.PackageServiceImpl;
import com.dbkj.meet.services.inter.PackageService;
import com.dbkj.meet.validator.PackageValidator;
import com.dbkj.meet.vo.PackageListItemVo;
import com.dbkj.meet.vo.PackageVo;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.GET;
import com.jfinal.ext.interceptor.POST;

import java.util.List;
import java.util.Map;

/**
 * 套餐管理
 * Created by DELL on 2017/04/01.
 */
@Before({AdminInterceptor.class, SAdminInterceptor.class})
@Clear({InfoInterceptor.class})
public class PackageController extends Controller {

    private final PackageService packageService=new PackageServiceImpl();

    public void index(){
        List<PackageListItemVo> packageVoList=packageService.list();
        setAttr("plist",packageVoList);
        render("list.html");
    }

    @ActionKey("/admin/package/addnew")
    @Before({GET.class})
    public void addNew(){
        Map<String,Object> attrs=packageService.showAdd();
        setAttrs(attrs);
        render("add.html");
    }

    public void exist(){
        String name=getPara("name");
        Result result=packageService.exist(name);
        renderJson(result);
    }

    @Before({POST.class, PackageValidator.class})
    public void add(){
        PackageVo packageVo=getBean(PackageVo.class,"pv");
        packageService.add(packageVo);
        redirect("/admin/package");
    }

    @Clear({SAdminInterceptor.class})
    public void list(){
        List<PackageListItemVo> plist=packageService.list();
        renderJson(new Result<List<PackageListItemVo>>(true,plist));
    }
}
