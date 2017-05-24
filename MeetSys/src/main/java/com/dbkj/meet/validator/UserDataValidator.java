package com.dbkj.meet.validator;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.UserData;
import com.dbkj.meet.dto.UserDto;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.UserService;
import com.dbkj.meet.services.inter.IUserService;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

import java.security.Key;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/23.
 */
public class UserDataValidator extends Validator {

    private IUserService userService=new UserService();
    private UserData userData;

    protected void validate(Controller controller) {
        userData = controller.getBean(UserData.class,"user");
        Res res = I18n.use("zh_CN");
        String username=userData.getUsername();

        boolean flag=true;
        if(StrKit.isBlank(username)){
            flag=false;
            addError("usernameMsg",res.get("username.not.empty"));
        }else if(!ValidateUtil.validateMobilePhone(username)){
            flag=false;
            addError("usernameMsg",res.get("username.format.not.correct"));
        }else if(userData.getId()==null&&userService.isExistUser(username)){//如果是添加则需要验证号码是否已被注册
            flag=false;
            addError("usernameMsg",res.get("username.exist"));
        }

        Map<String,Key> keyMap=controller.getSessionAttr(UserService.KEY_MAP);
        Key privateKey=keyMap.get(RSAUtil2.PRIVATE_KEY);

        String encryptPwd=userData.getEncryptPwd();
        String decryptPwd=null;
        if(StrKit.isBlank(encryptPwd)){
            flag=false;
            addError("passwordMsg",res.get("newPassword.not.empty"));
        }else {
            decryptPwd= RSAUtil2.decryptBase64(encryptPwd,privateKey);
            userData.setPassword(decryptPwd);
            if (decryptPwd.length() < 6 || decryptPwd.length() > 16) {
                flag=false;
                addError("passwordMsg", res.get("password.length"));
            }
        }
        String encryptConfimrPwd=userData.getEncryptConfirmPwd();
        if(StrKit.isBlank(encryptConfimrPwd)){
            flag=false;
            addError("confirmPwdMsg",res.get("confirmPassword.not.empty"));
        }else {
            String decryptConfimrPwd=RSAUtil2.decryptBase64(encryptConfimrPwd,privateKey);
            userData.setConfirmPwd(decryptConfimrPwd);
            if (!decryptConfimrPwd.equals(decryptPwd)) {
                flag=false;
                addError("confirmPwdMsg", res.get("confirmPassword.not.equal"));
            }
        }
        String name=userData.getName();
        if(StrKit.isBlank(name)){
            flag=false;
            addError("nameMsg",res.get("employee.name.not.empty"));
        }

        if(userData.getDid()==null){
            flag=false;
            addError("didMsg",res.get("employee.department.id.not.select"));
        }

        String email=userData.getEmail();
        if(!StrKit.isBlank(email)&& !ValidateUtil.validateEmail(email)){
            flag=false;
            addError("emailMsg",res.get("employee.email.format.wrong"));
        }

        if(flag) {
            controller.setAttr("user", userData);
        }
    }

    protected void handleError(Controller controller) {
        controller.setAttr("user",userData);
        controller.keepPara("queryString","publicKey");
        User user=controller.getSessionAttr(Constant.USER_KEY);
        controller.setAttr("dlist",userService.getDepartments(user.getCid()));
        controller.render("add.html");
    }
}
