package com.dbkj.meet.validator;

import com.dbkj.meet.model.Company;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.AdminService;
import com.dbkj.meet.services.LoginService;
import com.dbkj.meet.services.RSAKeyServiceImpl;
import com.dbkj.meet.services.inter.IAdminService;
import com.dbkj.meet.services.inter.ILoginService;
import com.dbkj.meet.services.inter.IUserService;
import com.dbkj.meet.services.UserService;
import com.dbkj.meet.services.inter.RSAKeyService;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.RedisUtil;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.AdminUserVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.validate.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/9.
 */
public class UserValidator extends Validator {

    private static final Logger logger= LoggerFactory.getLogger(UserValidator.class);

    private IUserService userService=new UserService();
    private AdminUserVo user=null;
    private RSAKeyService rsaKeyService=new RSAKeyServiceImpl();

    protected void validate(Controller controller) {
        Res resCn= I18n.use("zh_CN");
        user=controller.getBean(AdminUserVo.class,"user");
        String url=controller.getRequest().getRequestURI();

        String username=user.getUsername();
        if(StrKit.isBlank(username)){//如果不是更新，则需要验证用户名
            addError("usernameMsg",resCn.get("username.not.empty"));
        }else if(!ValidateUtil.validateMobilePhone(username)){
            addError("usernameMsg",resCn.get("username.format.not.correct"));
        }
        else if(!url.contains("update")&&userService.isExistUser(username)){
            addError("usernameMsg",resCn.get("username.exist"));
        }

        String key=controller.getPara("key");
        String privateKey=rsaKeyService.getPrivateKey(key);
        String password=user.getEncryptPassword();
        if(StrKit.isBlank(password)){
            addError("passwordMsg",resCn.get("password.not.empty"));
        }else {
            password=RSAUtil2.decryptBase64(password, privateKey);
            user.setPassword(password);
            if(password.length()<6||password.length()>16){
                addError("passwordMsg",resCn.get("passsword.length"));
            }
        }

        String confirmPwd=user.getEncryptConfirmPwd();//确认密码
        if(StrKit.isBlank(confirmPwd)){
            addError("confirmPwdMsg",resCn.get("confirmPassword.not.empty"));
        }else {
            confirmPwd=RSAUtil2.decryptBase64(confirmPwd,privateKey);
            user.setConfirmPwd(confirmPwd);
            if(password!=null&&!password.equals(confirmPwd)){
                addError("confirmPwdMsg",resCn.get("confirmPassword.not.equal"));
            }
        }

        if(0==user.getCid()||null==user.getCid()){
            addError("cidMsg",resCn.get("company.not.select"));
        }

    }

    protected void handleError(Controller controller) {
        controller.setAttr("user",user);
        controller.keepPara("queryStr","publicKey","key");
        IAdminService adminService=new AdminService();
        List<Company> companyList = adminService.getCompanys();
        controller.setAttr("clist",companyList);
        controller.render("add.html");
    }
}
