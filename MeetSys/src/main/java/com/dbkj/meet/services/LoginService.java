package com.dbkj.meet.services;

import com.dbkj.meet.converter.UserConverter;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.ILoginService;
import com.dbkj.meet.utils.AESUtil;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.vo.UserLoginVo;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Key;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/7.
 */
public class LoginService implements ILoginService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final static String ENCRYPT_KEY="wer345465#$%^.;~";
    private final String USERNAME_COOKIE="username";
    private final String PASSWORD_COOKIE="password";
    private final String REMEMBER_COOKIE="remember";
    /**
     * 判断是否存在该用户
     * @param user
     * @return
     */
    private User isExistUser(User user) {
        try {
            user.setPassword(AESUtil.encrypt(user.getPassword(), Constant.ENCRYPT_KEY));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return User.dao.findUser(user.getUsername(),user.getPassword());
    }

    /**
     * 登陆操作
     * @param user
     * @param controller
     * @return
     */
    public boolean login(UserLoginVo user, Controller controller) {
        User u= UserConverter.of(user);
        //解密密码
        Map<String,Key> keyMap=controller.getSessionAttr(LoginService.KEY_MAP);
        String password=RSAUtil2.decryptBase64(user.getEncryptPwd(),keyMap.get(RSAUtil2.PRIVATE_KEY));
        u.setPassword(password);

        User user1=isExistUser(u);
        boolean flag = user1!=null;
        if(flag){
            userMap.put(user1.getId().toString(),controller.getSession().getId());
            controller.setSessionAttr(Constant.USER_KEY,user1);
            //记住密码
            if(!StrKit.isBlank(user.getRemember())){
                int expire=60*60*24*7;
                Cookie usernameCookie=new Cookie(USERNAME_COOKIE,u.getUsername());
                usernameCookie.setMaxAge(expire);
                controller.setCookie(usernameCookie);

                String encryptPassword=null;
                try {
                    encryptPassword=AESUtil.encrypt(password,LoginService.ENCRYPT_KEY);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
                Cookie passwordCookie=new Cookie(PASSWORD_COOKIE,encryptPassword);
                passwordCookie.setMaxAge(expire);
                controller.setCookie(passwordCookie);

                Cookie rememberCookie=new Cookie(REMEMBER_COOKIE,user.getRemember());
                rememberCookie.setMaxAge(expire);
                controller.setCookie(rememberCookie);
            }
        }else{
            String times = controller.getCookie(LoginService.TIMES,"0");
            times=String.valueOf(Integer.parseInt(times)+1);
            controller.setCookie(LoginService.TIMES,times,60*30,true);
            controller.setAttr("times",times);

            Res resCn= I18n.use();
            user.setPassword(password);
            controller.setAttr("user",user);
            controller.setAttr("errorMsg",resCn.get("usernameOrPassword.not.correct"));
        }
        return flag;
    }

    @Override
    public Map<String, Key> getKeyMap() {
        return RSAUtil2.generateKeys();
    }

    @Override
    public UserLoginVo getRememberUser(HttpServletRequest request) {
        //从cookie中获取记录的登陆信息
        Cookie[] cookies=request.getCookies();
        String username=null;
        String password=null;
        String remember=null;
        //

        if(cookies!=null){
            int count=0;
            for(int i=0;i<cookies.length;i++){
                Cookie cookie=cookies[i];
                //如果count>=3说明登陆信息都已经取出来
                //终止遍历
                if(count>=3){
                    break;
                }
                if(USERNAME_COOKIE.equals(cookie.getName())){
                    username=cookie.getValue();
                    count++;
                    continue;
                }
                if(PASSWORD_COOKIE.equals(cookie.getName())){
                    password=cookie.getValue();
                    count++;
                    continue;
                }
                if(REMEMBER_COOKIE.equals(cookie.getName())){
                    remember=cookie.getValue();
                    count++;
                    continue;
                }
            }
            if(!StrKit.isBlank(remember)&&"on".equals(remember)){
                UserLoginVo userLoginVo=new UserLoginVo();
                userLoginVo.setUsername(username);
                userLoginVo.setRemember(remember);
                if(!StrKit.isBlank(password)){
                    try {
                        userLoginVo.setPassword(AESUtil.decrypt(password,ENCRYPT_KEY));
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                }
                return userLoginVo;
            }
        }
        return null;
    }
}
