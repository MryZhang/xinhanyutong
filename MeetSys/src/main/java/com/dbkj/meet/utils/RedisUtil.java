package com.dbkj.meet.utils;

import com.jfinal.plugin.redis.Redis;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by DELL on 2017/03/29.
 */
public class RedisUtil {

    private Jedis jedis;

    private RedisUtil(){
        jedis=Redis.use().getJedis();
    }

    private static final RedisUtil instance=new RedisUtil();

    public static RedisUtil getInstance(){
        return instance;
    }

    /**
     * 存储Set数据
     * @param key
     * @param data
     * @param <T>
     */
    public <T extends Serializable> void set(String key, Set<T> data){
        jedis.set(key.getBytes(),SerializeUtil.serializeSet(data));
    }

    /**
     * 获取Set数据
     * @param key
     * @param <T>
     * @return
     */
    public <T extends Serializable> Set<T> get(String key){
        byte[] bytes=jedis.get(key.getBytes());
        if(bytes!=null){
            return SerializeUtil.deserializeSet(jedis.get(key.getBytes()));
        }
        return null;
    }

    /**
     * 删除数据
     * @param key
     */
    public void del(String key){
        jedis.del(key.getBytes());
    }
}
