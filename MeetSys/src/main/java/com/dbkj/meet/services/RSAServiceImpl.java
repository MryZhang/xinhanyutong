package com.dbkj.meet.services;

import com.dbkj.meet.services.inter.IRSAService;
import com.dbkj.meet.utils.RSAUtil2;

import java.security.Key;
import java.util.Map;

/**
 * Created by DELL on 2017/05/23.
 */
public class RSAServiceImpl implements IRSAService {

    @Override
    public Map<String, Key> generateKeys() {
        return RSAUtil2.generateKeys();
    }

    @Override
    public String getPublicKey(Map<String, Key> keyMap) {
        return RSAUtil2.getPublicKey(keyMap);
    }
}
