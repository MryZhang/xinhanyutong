package com.dbkj.meet.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by DELL on 2017/03/13.
 */
public class ThreadPoolUtil {

    /**
     * 获取缓存线程池
     * @return
     */
    public static ExecutorService getCacheThreadPool(){
        return Executors.newCachedThreadPool();
    }
}
