package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.interceptors.AmountInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.interceptors.LoginInterceptor;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.LoginService;
import com.dbkj.meet.services.inter.ILoginService;
import com.dbkj.meet.utils.RSAUtil;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.VertifyCodeUtil;
import com.dbkj.meet.validator.LoginValidator;
import com.dbkj.meet.vo.UserLoginVo;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import com.jfinal.kit.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/4.
 */
@Clear({LoginInterceptor.class, InfoInterceptor.class, AmountInterceptor.class})
public class LoginController extends Controller {

    private ILoginService loginService=new LoginService();

    private final Logger logger= LoggerFactory.getLogger(LoginController.class);

    public void index(){
        setAttr("times",getCookie(LoginService.TIMES));
        Map<String,Key> keyMap=loginService.getKeyMap();
        setAttr("publicKey",RSAUtil2.getPublicKey(keyMap));
        setSessionAttr(LoginService.KEY_MAP,keyMap);

        UserLoginVo userLoginVo=loginService.getRememberUser(getRequest());
        setAttr("user",userLoginVo);
        render("index.html");
    }

    @ActionKey("/dologin")
    @Before({POST.class, LoginValidator.class})
    public void login(){
        UserLoginVo user=getBean(UserLoginVo.class,"user");
        boolean result=loginService.login(user,this);
        if(result){//登陆成功
            //setSessionAttr(Constant.USER_KEY,user);

            int type = ((User)getSessionAttr(Constant.USER_KEY)).getAid();
            if(type== UserType.SUPER_ADMIN.getTypeCode()||type==UserType.SUPER_SUPER_ADMIN.getTypeCode()){//超级管理员
                redirect("/admin");
            }else{//
                redirect("/meetlist");
            }
        }else{//登陆失败
            Map<String,Key> keyMap=getSessionAttr(LoginService.KEY_MAP);
            setAttr("publicKey",RSAUtil2.getPublicKey(keyMap));
            render("index.html");
        }
    }

    public void vertifyCode(){
        String path=getRequest().getSession().getServletContext().getRealPath("/")+"/vertifycode/code.jpg";
        File file=new File(path);
        if(file.exists()){
            file.delete();
        }
        String code = VertifyCodeUtil.outputVertifyImage(100, 40, file, 4);
        setSessionAttr(Constant.VERTIFY_CODE, code);
        renderFile(file);
    }

    @ActionKey("logout")
    public void logout(){//注销登陆
        HttpSession session=getSession();
        if(session!=null){
            session.invalidate();
        }
        redirect("/login");
    }

}
