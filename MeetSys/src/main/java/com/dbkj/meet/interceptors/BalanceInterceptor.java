package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.ConfigTypeEnum;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.AccountBalance;
import com.dbkj.meet.model.Config;
import com.dbkj.meet.model.User;
import com.jfinal.aop.Before;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 资费拦截器
 * 如果资费不足将提示相关信息
 * Created by DELL on 2017/03/01.
 */
public class BalanceInterceptor implements Interceptor{
    @Override
    public void intercept(Invocation invocation) {
        Controller controller = invocation.getController();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        //账户余额小于余额阙值则显示提示信息
        if(isReach(user.getCid())){
            if(isAjax(controller.getRequest())){
                controller.renderJson(new Result(false,"账户余额不足，请充值"));
            }else{
                controller.render("/pages/other/notenough.html",403);
            }
        }else{
            invocation.invoke();
        }
    }

    /**
     * 判断当前Action method是否是POST
     * @param method
     * @return
     */
    private boolean isPost(Method method){
       Before before = method.getAnnotation(Before.class);
       Class<? extends Interceptor>[] clazzs = before.value();
       for(Class<? extends Interceptor> cz:clazzs){
           if(POST.class.equals(cz)){
               return true;
           }
       }
       return false;
    }

    /**
     * 判断公司余额是否少于余额阙值
     * @param cid
     * @return
     */
    private boolean isReach(int cid){
        AccountBalance accountBalance=AccountBalance.dao.findByCompanyId(cid);
        //获取设置的余额阙值
        Config config=Config.dao.findByType(ConfigTypeEnum.BALANCE_THRESHOLD.getCode());
        return Double.parseDouble(config.getValue())>(accountBalance.getBalance()
                .add(accountBalance.getCreditLines()).doubleValue());
    }

    /**
     * 判断时是否是ajax请求
     * @param request
     * @return
     */
    private boolean isAjax(HttpServletRequest request){
        String value = request.getHeader("X-Requested-With");
        if(value!=null&&"XMLHttpRequest".equals(value)){
            return true;
        }else {
            return false;
        }
    }
}
