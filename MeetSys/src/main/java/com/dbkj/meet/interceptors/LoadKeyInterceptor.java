package com.dbkj.meet.interceptors;

import com.dbkj.meet.services.RSAKeyServiceImpl;
import com.dbkj.meet.services.inter.RSAKeyService;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by DELL on 2017/06/17.
 */
public class LoadKeyInterceptor implements Interceptor {

    private RSAKeyService rsaKeyService=new RSAKeyServiceImpl();
    private static final Logger logger= LoggerFactory.getLogger(LoadKeyInterceptor.class);

    @Override
    public void intercept(Invocation invocation) {
        Controller controller=invocation.getController();
        String key= rsaKeyService.generateCacheKey();
        controller.setAttr("key",key);
        String publicKey= null;
        try {
            publicKey = rsaKeyService.generateKeyPairAndGetPublicKey(key);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
        controller.setAttr("publicKey",publicKey);

        invocation.invoke();
    }
}
