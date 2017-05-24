package com.dbkj.meet.validator;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.ContactInfo;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.PersonalContactService;
import com.dbkj.meet.services.inter.IPersonalContactsService;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

/**
 * Created by MrQin on 2016/11/21.
 */
public class ContactInfoValidator extends Validator {

    private IPersonalContactsService personalContactsService=new PersonalContactService();

    protected void validate(Controller controller) {
        ContactInfo contactInfo=controller.getBean(ContactInfo.class,"contact");
        Res res = I18n.use("zh_CN");
        String name=contactInfo.getName();
        boolean nameFlag=true;
        if(StrKit.isBlank(name)){
            addError("nameMsg",res.get("name.not.empty"));
            nameFlag=false;
        }else if(name.length()>10){
            addError("nameMsg",res.get("name.length.not.correct"));
            nameFlag=false;
        }

        String phone=contactInfo.getPhone();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        long uid=user.getId();
        boolean phoneFlag=true;
        if(StrKit.isBlank(phone)){
            addError("phoneMsg",res.get("phone.not.empty"));
            phoneFlag=false;
        }else if(!ValidateUtil.validatePhone(phone)){
            addError("phoneMsg",res.get("phone.formart.wrong"));
            phoneFlag=false;
        }

        String actionKey=getActionKey();
        if(actionKey.indexOf("updateContact")!=-1){//修改
            if(contactInfo.getPid()==null||contactInfo.getId()==null){
                addError("error",res.get("error.message"));
            }
            //判断姓名是否重复
            if(nameFlag&&personalContactsService.isExistContactByName(name,contactInfo.getPid(),uid)){
                addError("nameMsg",res.get("name.repeat"));
            }
            //判断电话号码是否重复
            if(phoneFlag&&personalContactsService.isExistContactByPhone(phone,contactInfo.getId(),uid)){
                addError("phoneMsg",res.get("phone.repeat"));
            }
        }else{//添加联系人
            if(phoneFlag&&personalContactsService.isExistContactByPhone(phone,uid)){
                addError("phoneMsg",res.get("phone.repeat"));
            }
        }

        String email=contactInfo.getEmail();
        if(!StrKit.isBlank(email)&& !ValidateUtil.validateEmail(email)){
            addError("emailMsg",res.get("email.format.wrong"));
        }
    }

    protected void handleError(Controller controller) {
        controller.keepBean(ContactInfo.class,"contact");
        User user=controller.getSessionAttr(Constant.USER_KEY);
        controller.setAttr("glist",personalContactsService.getGroupsByUserId(user.getId()));
        controller.render("add.html");
    }
}
