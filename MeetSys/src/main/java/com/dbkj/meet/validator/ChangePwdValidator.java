package com.dbkj.meet.validator;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.ChangePwd;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.AdminService;
import com.dbkj.meet.utils.RSAUtil2;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

import java.security.Key;
import java.util.Map;


/**
 * Created by MrQin on 2016/11/10.
 */
public class ChangePwdValidator extends Validator {

    private ChangePwd changePwd;

    protected void validate(Controller controller) {
        Res resCn= I18n.use("zh_CN");
        changePwd = controller.getBean(ChangePwd.class,"p");
        String username=((User)controller.getSessionAttr(Constant.USER_KEY)).getUsername();
        Map<String,Key> keyMap=controller.getSessionAttr(AdminService.KEY_MAP);
        Key privateKey=keyMap.get(RSAUtil2.PRIVATE_KEY);
        if(StrKit.isBlank(changePwd.getEncryptOldPwd())){
            addError("oldPasswordMsg",resCn.get("oldPassword.not.empty"));
        }else{
            String oldPwd=RSAUtil2.decryptBase64(changePwd.getEncryptOldPwd(),privateKey);
            changePwd.setOldPassword(oldPwd);
            if(!new AdminService().validatePassword(username,oldPwd)){
                addError("oldPasswordMsg",resCn.get("oldPassword.not.correct"));
            }
        }

        String newPassword=null;
        if(StrKit.isBlank(changePwd.getEncryptNewPwd())){
            addError("newPasswordMsg",resCn.get("newPassword.not.empty"));
        }else{
            newPassword=RSAUtil2.decryptBase64(changePwd.getEncryptNewPwd(),privateKey);
            changePwd.setNewPassword(newPassword);
            if(newPassword.length()<6||newPassword.length()>16) {
                addError("newPasswordMsg", resCn.get("password.length"));
            }
        }

        String confirmPassword=changePwd.getEncryptConfirmPwd();
        if(StrKit.isBlank(confirmPassword)){
            addError("confirmPasswordMsg",resCn.get("confirmPassword.not.empty"));
        }else{
            confirmPassword=RSAUtil2.decryptBase64(confirmPassword,privateKey);
            changePwd.setConfirmPassword(confirmPassword);
            if(newPassword.length()<6||newPassword.length()>16) {
                addError("confirmPasswordMsg", resCn.get("password.length"));
            }else if(!confirmPassword.equals(newPassword)){
                addError("confirmPasswordMsg",resCn.get("confirmPassword.not.equal"));
            }
        }
    }

    protected void handleError(Controller controller) {
        controller.setAttr("p",changePwd);
        controller.keepPara("publicKey");
        controller.render("changepwd.html");
    }
}
