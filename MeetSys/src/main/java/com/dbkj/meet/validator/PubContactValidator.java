package com.dbkj.meet.validator;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.PubContact;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.PublicContactService;
import com.dbkj.meet.services.inter.IPublicContactService;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

/**
 * 验证公共联系人的数据
 * Created by MrQin on 2016/11/24.
 */
public class PubContactValidator extends Validator {

    private IPublicContactService publicContactService=new PublicContactService();

    private User user;

    protected void validate(Controller controller) {
        Res res= I18n.use("zh_CN");
        PubContact pubContact = controller.getBean(PubContact.class,"contact");
        user=getUser(controller);

        String name=pubContact.getName();
        if(StrKit.isBlank(name)){
            addError("nameMsg",res.get("name.not.empty"));
        }else if(pubContact.getId()!=null&&publicContactService
                .isExistName(name,pubContact.getPid(),user.getCid())){
            //如果id不为空，则当前操作为修改，并判断修改的联系人姓名是否重复
            addError("nameMsg",res.get("name.repeat"));
        }

        String phone=pubContact.getPhone();
        if(StrKit.isBlank(phone)){
            addError("phoneMsg",res.get("phone.not.empty"));
        }else if(!ValidateUtil.validatePhone(phone)){
            addError("phoneMsg",res.get("phone.formart.wrong"));
        }else {
            if(pubContact.getId()!=null){
                if(publicContactService.isExistPhone(phone,pubContact.getId(),user.getCid())){
                    addError("phoneMsg",res.get("phone.repeat"));
                }
            }else{
                if(publicContactService.isExistPhone(phone,user.getCid())){
                    addError("phoneMsg",res.get("phone.repeat"));
                }
            }
        }

        Integer did=pubContact.getDid();
        if(did==null||did==0){
            addError("didMsg",res.get("employee.department.id.not.select"));
        }
    }

    protected void handleError(Controller controller) {
        controller.keepBean(PubContact.class,"contact");
        User user=getUser(controller);
        controller.setAttr("dlist",publicContactService.getDepartments(user.getCid()));
        controller.render("add.html");
    }

    private User getUser(Controller controller){
        if(user==null){
            user=controller.getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }
}
