package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.Company;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.ILoginService;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Created by MrQin on 2016/11/7.
 */
public class LoginInterceptor implements Interceptor {

    public void intercept(Invocation invocation) {
        Controller controller = invocation.getController();
        Object obj=controller.getSessionAttr(Constant.USER_KEY);
        if(obj==null){//未登陆或登陆过期
            controller.redirect("/login");
        }else{
            User user= (User) obj;
            String sessionId=controller.getSession().getId();
            //判断用户是否重复登陆
            if(!sessionId.equals(ILoginService.userMap.get(user.getId().toString()))){
                controller.render("/pages/other/relogin.html");
            }else{
                controller.setAttr("aid",user.getAid());
                Company company=Company.dao.findById(user.getCid());
                controller.setAttr("cname", company!=null?company.getName():"");//设置公司名称
                controller.setAttr("uname",user.getUsername());//设置显示用户名称
                invocation.invoke();
            }
        }
    }
}
