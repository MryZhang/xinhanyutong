package com.dbkj.meet.services.proxy;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.services.PersonalContactService;
import com.dbkj.meet.services.PublicContactService;
import com.jfinal.plugin.ehcache.CacheKit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 用来清除缓存代理类
 * Created by MrQin on 2016/11/25.
 */
public class ClearCacheProxy implements InvocationHandler{

    //用来存储在执行后需要执行清除缓存的方法名称
    private String[] methods=new String[]{"add","update","delete","import","add"};

    private Object target;

    /**
     * 绑定委托对象并返回一个代理类
     * @param target
     * @return
     */
    public Object bind(Object target){
        this.target=target;
        //取得代理对象
        ClassLoader classLoader=target.getClass().getClassLoader();
        return Proxy.newProxyInstance(classLoader,target.getClass().getInterfaces(),this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result=null;
        result=method.invoke(target,args);
        //判断前方法是否需要执行清空缓存工作
        String methodName = method.getName();
        if(contains(methodName)){
            boolean flag=Boolean.parseBoolean(result.toString());
            if(flag){
                String className=target.getClass().getSimpleName();
                if(className.equals(PublicContactService.class.getSimpleName())){
                    CacheKit.remove(Constant.CACHE_NAME_ADMDIV,Constant.PUBLIC_CONTACT_CACHE_KEY);
                }else if(className.equals(PersonalContactService.class.getSimpleName())){
                    CacheKit.remove(Constant.CACHE_NAME_ADMDIV,Constant.PRIVATE_CONTACT_CACHE_KEY);
                }
            }
        }
        return result;
    }

    private boolean contains(String methodName){
        boolean flag=false;
        for(int i=0;i<methods.length;i++){
            if(methodName.contains(methods[i])){
                flag=true;
                break;
            }
        }
        return flag;
    }
}
