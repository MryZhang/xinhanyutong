package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Created by MrQin on 2016/11/22.
 */
public class UserInterceptor implements Interceptor {

    public void intercept(Invocation invocation) {
        Controller controller = invocation.getController();
        User user = controller.getSessionAttr(Constant.USER_KEY);
        if(user.getAid()== UserType.ADMIN.getTypeCode()){
            invocation.invoke();
        }else{//如果不是公司管理员，则显示没有权限的页面
            controller.render("/pages/other/noauth.html");
        }
    }
}
