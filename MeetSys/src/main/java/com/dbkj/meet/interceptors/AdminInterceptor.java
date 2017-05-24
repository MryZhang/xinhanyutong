package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Created by MrQin on 2016/11/8.
 */
public class AdminInterceptor implements Interceptor {

    public void intercept(Invocation invocation) {
        Controller controller=invocation.getController();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        if(user.getAid()== UserType.SUPER_ADMIN.getTypeCode()||user.getAid()==UserType.SUPER_SUPER_ADMIN.getTypeCode()){
            invocation.invoke();
        }else{
            controller.render("/pages/other/noauth.html");
        }
    }
}
