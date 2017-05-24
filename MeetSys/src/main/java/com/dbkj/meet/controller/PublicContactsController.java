package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.PubContact;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Department;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.PublicContactService;
import com.dbkj.meet.services.inter.IPublicContactService;
import com.dbkj.meet.services.proxy.ClearCacheProxy;
import com.dbkj.meet.validator.PubContactValidator;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/24.
 */
public class PublicContactsController extends Controller {

    /**
     * 使用代理类来清除相关缓存
     */
    private IPublicContactService publicContactService= (IPublicContactService) new ClearCacheProxy().bind(new PublicContactService());

    private User user;

    private User getUser(){
        if(user==null){
            user = getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }

    public void index(){
        Map<String,Object> map = publicContactService.getContactPageData(this);
        setAttrs(map);
        render("index.html");
    }

    public void showAdd(){
        user=getUser();
        List<Department> departments=publicContactService.getDepartments(user.getCid());
        setAttr("dlist",departments);
        String queryString=getRequest().getQueryString();
        setAttr("queryString", StrKit.isBlank(queryString)?"":"?"+queryString);
        render("add.html");
    }

    public void isExist(){//验证联系人姓名或者电话是否已存在
        String name=getPara("name");
        String phone=getPara("phone");
        user=getUser();
        boolean result=false;
        if(!StrKit.isBlank(name)){
            long id=getParaToLong("id");
            result=publicContactService.isExistName(name,id,user.getCid());
        }else{
            Long id=getParaToLong("id");
            if(id!=null){//修改时判断要修改的电话号码是否与公司中其他联系人的号码重复
                result=publicContactService.isExistPhone(phone,id,user.getCid());
            }else{
                result=publicContactService.isExistPhone(phone,user.getCid());
            }
        }
        renderJson("{\"result\":"+result+"}");
    }

    @Before({POST.class, PubContactValidator.class})
    public void add(){
        add("/publiccontacts");
    }

    private void add(String path){
        PubContact pubContact=getBean(PubContact.class,"contact");
        user=getUser();
        publicContactService.addContact(pubContact,user.getCid());
        String queryString=getPara("queryString");
        redirect(path.concat(queryString));
    }

    @Before({POST.class, PubContactValidator.class})
    public void addAndContinue(){
        add("/publiccontacts/showAdd");
    }

    public void showUpdate(){
        Long id=getParaToLong();
        PubContact pubContact=publicContactService.getPubContact(id);
        setAttr("contact",pubContact);
        user=getUser();
        List<Department> departments=publicContactService.getDepartments(user.getCid());
        setAttr("dlist",departments);
        String queryString=getRequest().getQueryString();
        setAttr("queryString", StrKit.isBlank(queryString)?"":"?"+queryString);
        render("add.html");
    }

    @Before({POST.class,PubContactValidator.class})
    public void update(){
        PubContact pubContact=getBean(PubContact.class,"contact");
        publicContactService.updateContactData(pubContact);
        String queryString=getPara("queryString");
        redirect("/publiccontacts".concat(queryString));
    }

    public void delete(){
        String idStr=getPara();
        String queryString=getRequest().getQueryString();
        user=getUser();
        publicContactService.deleteContacts(idStr,user.getCid());
        redirect("/publiccontacts".concat(StrKit.isBlank(queryString)?"":"?"+queryString));
    }

    public void export(){//导出联系人
        String search=getPara("search");
        Integer groupId=getParaToInt("groupId");
        File file=publicContactService.exportContacts(search,groupId,getUser().getCid());
        renderFile(file);
    }

    public void download(){//下载模板文件
        renderFile("public.xls");
    }

    public void showImport(){
        List<Department> departmentList = publicContactService.getDepartments(getUser().getCid());
        setAttr("dlist",departmentList);
        String queryString=getRequest().getQueryString();
        setAttr("queryString", StrKit.isBlank(queryString)?"":"?"+queryString);
        render("import.html");
    }

    public void importContact(){
        UploadFile uploadFile = getFile("contactFile");
        Integer cid=getUser().getCid();
        Result<Map<String,Object>> result = publicContactService.importContacts(uploadFile.getFile(),cid);
        setAttrs(result.getData());
        String queryString=getPara("queryString");
        setAttr("queryString",queryString);
        render("result.html");
    }

    public void getContacts(){
        List<BaseNode> personalContacts = publicContactService.getContacts(getUser().getCid());
        renderJson(personalContacts);
    }
}
