package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.Employee;
import com.dbkj.meet.model.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * 完善个人资料的拦截器
 * Created by MrQin on 2016/11/11.
 */
public class InfoInterceptor implements Interceptor{

    public void intercept(Invocation invocation) {
        Controller controller = invocation.getController();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        if(user.getAid()== UserType.SUPER_ADMIN.getTypeCode()||user.getAid()==UserType.SUPER_SUPER_ADMIN.getTypeCode()){
            invocation.invoke();
        }else{
            Employee employee=Employee.dao.findById(user.getEid());
            if(employee==null){
                controller.redirect("/employee/showPrefect");
            }else{
                invocation.invoke();
            }
        }
    }
}
