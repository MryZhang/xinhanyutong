package com.dbkj.meet.interceptors;

import com.dbkj.meet.dic.Constant;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.ehcache.CacheKit;

/**
 * Created by DELL on 2017/03/03.
 */
public class RemoveBillCacheInterceptor implements Interceptor {
    @Override
    public void intercept(Invocation invocation) {
        invocation.invoke();
        CacheKit.removeAll(Constant.CACHE_NAME_BILLCACHE);
    }
}
