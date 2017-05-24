package com.dbkj.meet.services.inter;

import java.security.Key;
import java.util.Map;

/**
 * 生成公私钥的Service
 * Created by DELL on 2017/05/23.
 */
public interface IRSAService {

    /**
     * 生成公私钥
     * @return
     */
    Map<String,Key> generateKeys();

    /**
     * 从公私钥对中获取公钥
     * @param keyMap
     * @return
     */
    String getPublicKey(Map<String,Key> keyMap);
}
