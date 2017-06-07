package com.dbkj.meet.controller;

import com.dbkj.meet.controller.base.BaseController;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.ContactInfo;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Group;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.PersonalContactService;
import com.dbkj.meet.services.inter.IPersonalContactsService;
import com.dbkj.meet.validator.ContactInfoValidator;
import com.jfinal.aop.Before;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 个人通讯录
 * Created by MrQin on 2016/11/17.
 */
public class PersonalContactsController extends BaseController {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    //使用代理类来清除缓存
    private final IPersonalContactsService personalContactService= Enhancer.enhance(PersonalContactService.class);

    private User user;

    private User getUser(){
        if(user==null){
            user=getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }

    public void index(){
        User user=getUser();
        Map<String,Object> map = personalContactService.getRenderData(user.getId());
        setAttrs(map);
        render("index.html");
    }

    public void page(){
        User user=getUser();
        Map<String,Object> map=personalContactService.getContactsPage(getParaMap(),user.getId());
        setAttrs(map);
        render("index.html");
    }

    @Before({POST.class})
    public void addGroup(){
        Group group=getModel(Group.class,"g");
        Long uid=getUser().getId();
        group.setUid(Integer.parseInt(uid.toString()));
        Result<Group> result=personalContactService.addGroup(group);
        renderJson(result);
    }

    @Before({POST.class})
    public void updateGroup(){
        Group group=getModel(Group.class,"g");
        Result<Group> result=personalContactService.updateGroup(group);
        renderJson(result);
    }

    public void deleteGroup(){
        Long gid=getParaToLong();
        personalContactService.deleteGroup(gid);
        redirect("/personalcontacts");
    }

    public void showAddContact(){
        User user=getUser();
        Long uid=user.getId();
        List<Group> groups=personalContactService.getGroupsByUserId(uid);
        setAttr("glist",groups);
        String queryString = getRequest().getQueryString();
        setAttr("query", StrKit.isBlank(queryString)?"":"?"+queryString);
        render("add.html");
    }

    @Before({POST.class,ContactInfoValidator.class})
    public void addContact(){//添加联系人
       addCon("/personalcontacts/page");
    }

    private void addCon(String path){
        ContactInfo contactInfo=getBean(ContactInfo.class,"contact");
        User user=getUser();
        contactInfo.setUid(user.getId());
        personalContactService.addContact(contactInfo);
        String queryString=getPara("queryString");
        redirect(path.concat(queryString));
    }

    @Before({POST.class,ContactInfoValidator.class})
    public void addAndContinue(){//保存并继续添加联系人
        addCon("/personalcontacts/showAddContact");
    }

    public void isExist(){
        String phone=getPara("phone");
        String name=getPara("name");


        User user=getUser();
        long uid=user.getId();

        boolean result=true;
        if(phone!=null){
            Long id=getParaToLong("id");
            if(id==null){
                result=personalContactService.isExistContactByPhone(phone,uid);
            }else{
                result=personalContactService.isExistContactByPhone(phone,id,uid);
            }
        }else{
            Long pid=getParaToLong("pid");
            result=personalContactService.isExistContactByName(name,pid,uid);
        }

        renderJson("{\"result\":"+result+"}");
    }

    public void deleteContacts(){
        String ids=getPara("ids");
        String queryString=getRequest().getQueryString();
        personalContactService.deleteContacts(ids);
        redirect("/personalcontacts/page"+queryString);
    }

    public void editContact(){
        int id=getParaToInt();
        ContactInfo contactInfo=personalContactService.getContactInfo(id);
        setAttr("contact",contactInfo);
        String queryString = getRequest().getQueryString();
        setAttr("query", StrKit.isBlank(queryString)?"":"?"+queryString);
        //获取所有分组
        User user=getUser();
        Long uid=user.getId();
        List<Group> groups=personalContactService.getGroupsByUserId(uid);
        setAttr("glist",groups);
        render("add.html");
    }

    @Before({POST.class,ContactInfoValidator.class})
    public void updateContact(){
        ContactInfo contactInfo=getBean(ContactInfo.class,"contact");
        personalContactService.updateContactInfo(contactInfo);
        String queryString=getPara("queryString");
        redirect("/personalcontacts/page".concat(queryString));
    }

    public void showImport(){
        User user=getUser();
        List<Group> groupList=personalContactService.getGroupsByUserId(user.getId());
        setAttr("glist",groupList);
        String queryString=getRequest().getQueryString();
        setAttr("queryString", StrKit.isBlank(queryString)?"":"?"+queryString);
        render("import.html");
    }

    public void download(){
        renderFile("private.xls");
    }

    public void importContacts(){//导入联系人
        UploadFile uploadFile = getFile("contactFile");
        User user=getUser();
        long uid=user.getId();
        Result<Map<String,Object>> result = personalContactService.importContacts(uploadFile.getFile(),uid);
        setAttrs(result.getData());
        String queryString=getPara("queryString");
        setAttr("queryString",queryString);
        render("result.html");
    }

    public void exportContacts(){//导出联系人
        String search=getPara("search");
        Integer groupId=getParaToInt("groupId");
        File file=null;
        try{
            file=personalContactService.exportContacts(search,groupId,getUser().getId());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        renderFile(file);
    }

    public void getContacts(){//获取个人联系人json信息
        List<BaseNode> baseNodeList = personalContactService.getContacts(getUser().getId());
        renderJson(baseNodeList);
    }
}
