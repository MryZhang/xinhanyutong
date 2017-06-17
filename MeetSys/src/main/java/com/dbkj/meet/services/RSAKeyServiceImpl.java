package com.dbkj.meet.services;

import com.dbkj.meet.services.inter.RSAKeyService;
import com.dbkj.meet.utils.RSAUtil2;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Map;
import java.util.UUID;

/**
 * Created by DELL on 2017/06/16.
 */
public class RSAKeyServiceImpl implements RSAKeyService {

    @Override
    public Map<String, Key> getKeyMap(String key) {
        Map<String,Key> keyMap = RSAUtil2.generateKeys();
        Cache redis= Redis.use();
        //将生成的密钥对存进redis中，默认过期时间为一周
        String privateKey=RSAUtil2.getBase64PrivateKey(keyMap);
        String publicKey=RSAUtil2.getBase64PublicKey(keyMap);
        redis.setex(key+"_"+RSAUtil2.PRIVATE_KEY,60*60*24*7,privateKey);
        redis.setex(key+"_"+RSAUtil2.PUBLIC_KEY,60*60*24*7,publicKey);
        return keyMap;
    }

    @Override
    public String generateKeyPairAndGetPublicKey(String key) throws UnsupportedEncodingException {
        Map<String,Key> keyMap = RSAUtil2.generateKeys();
        Cache redis= Redis.use();
        //将生成的密钥对存进redis中，默认过期时间为一周
        String privateKey=RSAUtil2.getBase64PrivateKey(keyMap);
        String publicKey=RSAUtil2.getBase64PublicKey(keyMap);
        redis.setex(key+"_"+RSAUtil2.PRIVATE_KEY,60*60*24*7,privateKey);
        redis.setex(key+"_"+RSAUtil2.PUBLIC_KEY,60*60*24*7,publicKey);
        return RSAUtil2.getBase64PublicKey(keyMap);
    }

    @Override
    public String getPublicKey(String key) {
        Cache redis= Redis.use();
        return redis.get(key+"_"+RSAUtil2.PUBLIC_KEY);
    }

    @Override
    public String getPrivateKey(String key) {
        Cache redis= Redis.use();
        return redis.get(key+"_"+RSAUtil2.PRIVATE_KEY);
    }

    @Override
    public void deleteKeyPair(String key) {
        Cache redis=Redis.use();
        redis.del(key+"_"+RSAUtil2.PUBLIC_KEY);
        redis.del(key+"_"+RSAUtil2.PRIVATE_KEY);
    }

    @Override
    public String generateCacheKey() {
        return UUID.randomUUID().toString();
    }
}
