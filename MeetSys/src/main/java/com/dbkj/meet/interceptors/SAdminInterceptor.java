package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/04/03.
 */
public class SAdminInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        Controller controller=invocation.getController();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        if(user.getAid()== UserType.SUPER_SUPER_ADMIN.getTypeCode()){
            invocation.invoke();
        }else{
            controller.render("/pages/other/noauth.html");
        }
    }
}
