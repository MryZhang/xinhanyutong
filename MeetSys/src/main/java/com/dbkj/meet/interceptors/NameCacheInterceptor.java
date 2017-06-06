package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.services.NameServiceImpl;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.ehcache.CacheKit;

/**
 * 清除缓存中联系人信息的缓存
 * Created by DELL on 2017/06/04.
 */
public class NameCacheInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation invocation) {
        invocation.invoke();
        CacheKit.remove(Constant.CACHE_NAME_PERMANENT, NameServiceImpl.NAME_OF_CACHE);
    }
}
