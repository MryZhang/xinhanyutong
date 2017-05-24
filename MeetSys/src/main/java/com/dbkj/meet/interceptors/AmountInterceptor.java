package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.AmountService;
import com.dbkj.meet.vo.AmountVo;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * 显示余额信息的拦截器
 * Created by DELL on 2017/03/01.
 */
public class AmountInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation invocation) {
        Controller controller=invocation.getController();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        //如果不是超级管理员
        if(user!=null&&user.getAid()!= UserType.SUPER_ADMIN.getTypeCode()
                &&user.getAid()!=UserType.SUPER_SUPER_ADMIN.getTypeCode()){
            AmountVo amountVo=new AmountService().getAmount(user.getCid());
            controller.setAttr("amount",amountVo);
        }
        invocation.invoke();;
    }
}
