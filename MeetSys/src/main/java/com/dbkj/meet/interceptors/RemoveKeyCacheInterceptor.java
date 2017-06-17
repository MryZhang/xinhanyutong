package com.dbkj.meet.interceptors;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.services.RSAKeyServiceImpl;
import com.dbkj.meet.services.inter.RSAKeyService;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/06/17.
 */
public class RemoveKeyCacheInterceptor implements Interceptor {

    private RSAKeyService rsaKeyService;

    @Override
    public void intercept(Invocation invocation) {
        invocation.invoke();;
        Object obj = invocation.getReturnValue();
        boolean result=false;
        if(obj instanceof Result){
            result=((Result)obj).getResult();
        }else{
            result= (boolean) obj;
        }
        if(result){
            //操作成功后删除Cache的Key Pair
            rsaKeyService=new RSAKeyServiceImpl();
            Object[] args = invocation.getArgs();
            Object arg=args[args.length-1];
            String key=null;
            if(arg instanceof Controller){
                key=((Controller)arg).getPara("key");
            }else {
                key= (String) arg;
            }
            rsaKeyService.deleteKeyPair(key);
        }
    }
}
