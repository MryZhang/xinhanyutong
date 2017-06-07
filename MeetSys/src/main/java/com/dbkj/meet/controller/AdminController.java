package com.dbkj.meet.controller;

import com.dbkj.meet.controller.base.BaseAdminController;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.ChangePwd;
import com.dbkj.meet.model.Company;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.AdminService;
import com.dbkj.meet.services.inter.IAdminService;
import com.dbkj.meet.validator.ChangePwdValidator;
import com.dbkj.meet.validator.UserValidator;
import com.dbkj.meet.vo.AdminUserVo;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.ext.interceptor.GET;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/8.
 */
public class AdminController extends BaseAdminController {

    private IAdminService adminService=new AdminService();

    public void index(){
        List<Company> companyList=adminService.getCompanys();
        setAttr("clist",companyList);
        Page<Record> userPages=adminService.getUserPage();
        setAttr("pages",userPages);
        render("index.html");
    }

//    @Before({POST.class})
//    public void search(){//搜索
//        Map<String,String[]> where=getParaMap();
//
//        adminService=new AdminService();
//        List<Company> companyList=adminService.getCompanys();
//        setAttr("clist",companyList);
//        PageData<UserPage> userPages=adminService.getUserPage(where);
//        setAttr("pages",userPages);
//        setAttr("where",where);
//        render("index.html");
//    }

    public void page(){//分页
        List<Company> companyList=adminService.getCompanys();
        setAttr("clist",companyList);
        Map<String,String[]> paras=getParaMap();
        Map<String,Object> where=adminService.getParameterMap(paras);
        Page<Record> userPages=adminService.getUserPage(new HashMap<String,Object>(where));
        setAttr("pages",userPages);
        if(where.containsKey("a.cid")){
            setAttr("cid",where.get("a.cid"));
        }
        setAttrs(where);
        render("index.html");
    }

    @Before({GET.class})
    public void showAdd(){//添加用户
        adminService.setPageData(getRequest());
        List<Company> companyList=adminService.getCompanys();
        setAttr("clist",companyList);
        String query = getRequest().getQueryString();
        setAttr("query",query);
        render("add.html");
    }

    @Before({POST.class, UserValidator.class})
    public void add(){//添加用户
        AdminUserVo user=getBean(AdminUserVo.class,"user");
        Map<String,Key> keyMap=getSessionAttr(AdminService.KEY_MAP);
        adminService.addUser(user,keyMap);
        String query=getPara("queryStr");
        redirect("/admin/page?"+query);
    }

    @Before({POST.class, UserValidator.class})
    public void addAndContinue(){//保存并继续添加
        AdminUserVo user=getBean(AdminUserVo.class,"user");
        Map<String,Key> keyMap=getSessionAttr(AdminService.KEY_MAP);
        adminService.setPageData(getRequest());
        adminService.addUser(user,keyMap);
        List<Company> companyList=adminService.getCompanys();
        setAttr("clist",companyList);
        String query=getPara("queryStr");
        setAttr("query",query);
        render("add.html");
    }

    @ActionKey("/admin/edit")
    public void edit(){
        long id=getParaToLong(0);
        adminService.setPageData(getRequest());
        User user=adminService.getUserById(id);
        setAttr("user",user);
        List<Company> companyList=adminService.getCompanys();
        setAttr("clist",companyList);
        String query = getRequest().getQueryString();
        setAttr("query",query);
        render("add.html");
    }

    @Before({POST.class,UserValidator.class})
    public void  update(){
        AdminUserVo user=getBean(AdminUserVo.class,"user");
        Map<String,Key> keyMap=getSessionAttr(AdminService.KEY_MAP);
        adminService.updateUser(user,keyMap);
        String query=getPara("queryStr");
        setAttr("query",query);
        redirect("/admin/page?"+query);
    }

    @Before({POST.class})
    public void isExist(){
        String username=getPara("username");
        boolean result=adminService.isExistUser(username);
        renderJson("{\"result\":"+result+"}");
    }

    @Before({POST.class})
    public void deleteUsers(){
        String idStr=getPara("id");
        boolean result=adminService.deleteUsers(idStr);
        renderJson("{\"result\":"+result+"}");
    }

    public void showChangePwd(){
        adminService.setPageData(getRequest());
        render("changepwd.html");
    }

    @Before({POST.class, ChangePwdValidator.class})
    public void updatePwd(){//修改密码
        ChangePwd changePwd=getBean(ChangePwd.class,"p");
        long id=((User)getSessionAttr(Constant.USER_KEY)).getId();
        adminService.updatePassword(id,changePwd,getRequest());
        redirect("/admin/showChangePwd");
    }
}
