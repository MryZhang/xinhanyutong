package com.dbkj.meet.services.inter;

import com.dbkj.meet.model.User;
import com.dbkj.meet.vo.UserLoginVo;
import com.jfinal.core.Controller;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MrQin on 2016/11/7.
 */
public interface ILoginService {

    String KEY_MAP="keyMap";
    /**
     * 验证次数的key
     */
    String TIMES="times";
    /**
     * 用来存储已登陆用户sessionId
     */
    Map<String,String> userMap=new ConcurrentHashMap<String, String>();

    /**
     * 登陆操作
     * @param user
     */
    boolean login(UserLoginVo user, Controller controller);

    Map<String,Key> getKeyMap();

    /**
     * 获取记住密码的登陆用户信息
     * @param request
     * @return
     */
    UserLoginVo getRememberUser(HttpServletRequest request);
}
