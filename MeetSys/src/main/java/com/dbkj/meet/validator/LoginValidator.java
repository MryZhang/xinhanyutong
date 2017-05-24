package com.dbkj.meet.validator;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.vo.UserLoginVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

/**
 * Created by MrQin on 2016/11/7.
 */
public class LoginValidator extends Validator{

    private UserLoginVo user;

    protected void validate(Controller controller) {
        Res resCn= I18n.use("zh_CN");
        user = controller.getBean(UserLoginVo.class,"user");
        String authCode=controller.getSessionAttr(Constant.VERTIFY_CODE);
        if(user==null|| StrKit.isBlank(user.getUsername())||StrKit.isBlank(user.getPassword())||StrKit.isBlank(user.getEncryptPwd())){
            addError("errorMsg",resCn.get("usernameAndPassword.not.empty"));
        }else if(authCode!=null){
            if(user.getAuthCode()==null||user.getAuthCode().trim().length()==0){
                addError("errorMsg",resCn.get("authcode.not.empty"));
            }else if(!user.getAuthCode().equalsIgnoreCase(authCode.toLowerCase())){
                addError("errorMsg",resCn.get("authcode.error"));
            }
        }
    }

    protected void handleError(Controller controller) {
        controller.keepBean(UserLoginVo.class,"user");
        controller.keepPara("publicKey");
        controller.render("index.html");
    }
}
