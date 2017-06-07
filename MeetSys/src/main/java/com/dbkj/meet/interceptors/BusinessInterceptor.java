package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/06/06.
 */
public class BusinessInterceptor implements Interceptor{
    @Override
    public void intercept(Invocation invocation) {
        Controller controller=invocation.getController();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        //判断如果是系统管理员，则重定向到首页
        if(UserType.SUPER_SUPER_ADMIN.getTypeCode()==user.getAid()||
                UserType.SUPER_ADMIN.getTypeCode()==user.getAid()){
            controller.redirect("/");
        }else{
            invocation.invoke();
        }
    }
}
