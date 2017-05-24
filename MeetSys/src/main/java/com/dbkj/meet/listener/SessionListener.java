package com.dbkj.meet.listener;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.ILoginService;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created by MrQin on 2016/11/7.
 */
public class SessionListener implements HttpSessionListener {
    /**
     * Session创建事件
     * @param httpSessionEvent
     */
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {

    }

    /**
     * Session销毁事件
     * @param httpSessionEvent
     */
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session=httpSessionEvent.getSession();
        User user= (User) session.getAttribute(Constant.USER_KEY);
        if(user!=null){
            ILoginService.userMap.remove(user.getId().toString());//从已登录的用户中删除该用户的数据
        }

    }
}
