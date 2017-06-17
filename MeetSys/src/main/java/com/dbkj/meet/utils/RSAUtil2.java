package com.dbkj.meet.utils;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL on 2017/04/14.
 */
public class RSAUtil2 {

    private static final Logger logger= LoggerFactory.getLogger(RSAUtil2.class);

    /**
     * 获取公钥的key
     */
    public static final String PUBLIC_KEY="RSAPublicKey";
    /**
     * 获取私钥的key
     */
    public static final String PRIVATE_KEY="RSAPrivateKey";
    /**
     * 默认字符集
     */
    public static final String DEFUALT_CHARSET="utf8";


    /**
     * 生成密钥对
     * @return
     */
    public static Map<String,Key> generateKeys(){
        Security.addProvider(new BouncyCastleProvider());
        SecureRandom random=new SecureRandom();
        Map<String,Key> keyMap=new HashMap<>(2);
        try {
            KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA","BC");
            generator.initialize(1024,random);
            KeyPair keyPair=generator.generateKeyPair();
            keyMap.put(PUBLIC_KEY,keyPair.getPublic());
            keyMap.put(PRIVATE_KEY,keyPair.getPrivate());
        } catch (NoSuchAlgorithmException|NoSuchProviderException e) {
            logger.error(e.getMessage(),e);
        }
        return keyMap;
    }

    /**
     * 获取Base64的public key
     * @param keyMap
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getBase64PublicKey(Map<String,Key> keyMap) {
        Key publicKey=keyMap.get(PUBLIC_KEY);
        try {
            return new String(Base64.encodeBase64(publicKey.getEncoded()),DEFUALT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    public static String getBase64PrivateKey(Map<String,Key> keyMap) {
        Key privateKey=keyMap.get(PRIVATE_KEY);
        try {
            return new String(Base64.encodeBase64(privateKey.getEncoded()),DEFUALT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] decrypt(byte[] data,PrivateKey privateKey){
        Security.addProvider(new BouncyCastleProvider());
        try {
            Cipher cipher=Cipher.getInstance("RSA/None/PKCS1Padding","BC");
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException|NoSuchProviderException|BadPaddingException
                |InvalidKeyException|IllegalBlockSizeException|NoSuchPaddingException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 解密
     * @param data
     * @param privateKey
     * @return
     */
    public static String decryptBase64(String data,Key privateKey) throws UnsupportedEncodingException {
        return new String(decrypt(Base64.decodeBase64(data), (PrivateKey) privateKey),DEFUALT_CHARSET);
    }

    /**
     * 加密
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] encrypt(byte[] data,Key publicKey){
        Security.addProvider(new BouncyCastleProvider());
        try {
            Cipher cipher=Cipher.getInstance("RSA/None/PKCS1Padding","BC");
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException|NoSuchProviderException|BadPaddingException
                |InvalidKeyException|IllegalBlockSizeException|NoSuchPaddingException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     *
     * @param data
     * @param publicKey
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encryptBase64(String data,Key publicKey) throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(encrypt(data.getBytes(DEFUALT_CHARSET),publicKey)),DEFUALT_CHARSET);
    }

    /**
     *
     * @param data
     * @param publicKey
     * @return
     */
    public static String encryptBase64(String data,String publicKey) {
        try {
            byte[] keyBytes = Base64.decodeBase64(publicKey.getBytes(DEFUALT_CHARSET));
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            PublicKey publicK = keyFactory.generatePublic(x509EncodedKeySpec);
            return encryptBase64(data,publicK);
        } catch (NoSuchAlgorithmException|NoSuchProviderException|InvalidKeySpecException|UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

        /**
     *
     * @param data
     * @param privateKey
     * @return
     */
    public static String decryptBase64(String data,String privateKey){
        try {
            byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes(DEFUALT_CHARSET));
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            return decryptBase64(data,privateK);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|NoSuchProviderException|UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

        /**
     * 获取私钥
     * @param keyMap 密钥对
     * @return
     */
    public static String getPrivateKey(Map<String,Key> keyMap){
        Key key= keyMap.get(PRIVATE_KEY);
        return Base64.encodeBase64String(key.getEncoded());
    }

    /**
     * 获取公钥
     * @param keyMap 密钥对
     * @return
     */
    public static String getPublicKey(Map<String,Key> keyMap){
        Key key= keyMap.get(PUBLIC_KEY);
        return Base64.encodeBase64String(key.getEncoded());
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        Map<String,Key> keyMap = generateKeys();
        String source="1234567890112315465446546545646";
        System.out.println("加密长度："+source.getBytes(DEFUALT_CHARSET).length);
        String encrypted=encryptBase64(source,getBase64PublicKey(keyMap));
        System.out.println("加密后："+encrypted);
        String decrypted=decryptBase64(encrypted,getBase64PrivateKey(keyMap));
        System.out.println("解密后："+decrypted);
    }
}
