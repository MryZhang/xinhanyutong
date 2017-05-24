package com.dbkj.meet.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.dbkj.meet.dic.Constant;
import org.apache.commons.codec.binary.Base64;

/**
 * AES加解密工具
 * Created by MrQin on 2016/11/7.
 */
public class AESUtil {

    //算法名称
    public static final String KEY_ALGORITHM="AES";
    public static final String ENCODING="utf-8";
    //算法名称/加密模式/补码方式
    public static final String CIPHER_ALGORITHM="AES/ECB/PKCS5Padding";

    /**
     * 加密
     * @param data
     * @param key
     * @return
     */
    public static String  encrypt(String data,String key) throws Exception{
        if(key==null){
            throw new IllegalArgumentException("The key can not be null");
        }
        //判断key是否为16位
        if(key.length()!=16){
            throw new IllegalArgumentException("The length of the key must be 16");
        }
        byte[] raw=key.getBytes(ENCODING);
        SecretKeySpec secretKeySpec=new SecretKeySpec(raw,KEY_ALGORITHM);
        Cipher cipher=Cipher.getInstance(CIPHER_ALGORITHM);//算法/模式/补码方式
        cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
        byte[] results=cipher.doFinal(data.getBytes(ENCODING));
        return Base64.encodeBase64String(results);//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    /**
     * 解密
     * @param data
     * @param key
     * @return
     */
    public static String decrypt(String data,String key)throws Exception{
        if(key==null){
            throw new IllegalArgumentException("The key can not be null");
        }
        //判断key是否为16位
        if(key.length()!=16){
            throw new IllegalArgumentException("The length of the key must be 16");
        }
        byte[] raw=key.getBytes(ENCODING);
        SecretKeySpec keySpec=new SecretKeySpec(raw,KEY_ALGORITHM);
        Cipher cipher=Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,keySpec);
        byte[] results=cipher.doFinal(Base64.decodeBase64(data));
        return new String(results,ENCODING);
    }

    public static void main(String[] args) throws Exception{
        /*
         * 此处使用AES-128-ECB加密模式，key需要为16位。
         */
//        String cKey = "1234567890123456";
//        // 需要加密的字串
//        String cSrc = "www.gowhere.so";
//        System.out.println(cSrc);
//        // 加密
//        String enString = encrypt(cSrc, cKey);
//        System.out.println("加密后的字串是：" + enString);
//
//        // 解密
//        String DeString = decrypt(enString, cKey);
//        System.out.println("解密后的字串是：" + DeString);
        String encryted=encrypt("dbkjadmin", Constant.ENCRYPT_KEY);
        System.out.println("加密后："+encryted);
        System.out.println("解密后："+decrypt(encryted,Constant.ENCRYPT_KEY));
    }
}
