package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.services.PersonalContactService;
import com.dbkj.meet.services.PublicContactService;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.ehcache.CacheKit;

/**
 * 清除联系人信息的缓存的拦截器
 * Created by DELL on 2017/06/04.
 */
public class ContactCacheInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        invocation.invoke();
        Class<?> clazz = invocation.getTarget().getClass();
        if(clazz.equals(PublicContactService.class)){
            CacheKit.remove(Constant.CACHE_NAME_ADMDIV,Constant.PUBLIC_CONTACT_CACHE_KEY);
        }else if(clazz.equals(PersonalContactService.class)){
            CacheKit.remove(Constant.CACHE_NAME_ADMDIV,Constant.PRIVATE_CONTACT_CACHE_KEY);
        }
    }
}
