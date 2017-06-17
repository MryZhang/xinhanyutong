package com.dbkj.meet.services.inter;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Map;

/**
 * Created by DELL on 2017/06/16.
 */
public interface RSAKeyService {

    Map<String,Key> getKeyMap(String key) throws UnsupportedEncodingException;

    /**
     * 生成密钥对并返回公钥
     * @param key
     * @return
     */
    String generateKeyPairAndGetPublicKey(String key) throws UnsupportedEncodingException;

    /**
     * 获取公钥
     * @param key
     * @return
     */
    String getPublicKey(String key);

    /**
     * 获取私钥
     * @param key
     * @return
     */
    String getPrivateKey(String key);

    /**
     * 删除redis的密钥对
     * @param key
     */
    void deleteKeyPair(String key);

    /**
     * 生成缓存key
     * @return
     */
    String generateCacheKey();
}
