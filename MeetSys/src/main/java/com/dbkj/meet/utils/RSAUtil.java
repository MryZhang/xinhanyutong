package com.dbkj.meet.utils;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加解密工具类
 * <p>
 *     字符串格式的密钥在未在特殊说明的情况下都为BASE64编码格式<br/>
 *     由于费对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 *     非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也保证了数据的安全
 * </p>
 * Created by DELL on 2017/04/14.
 */
public class RSAUtil {

    private static final Logger logger= LoggerFactory.getLogger(RSAUtil.class);

    /**
     * 加密算法
     */
    private static final String KEY_ALGORITHM = "RSA";
    /**
     * 签名算法
     */
    private static final String SIGNATURE_ALGORITHM="MD5withRSA";
    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY="RSAPublicKey";
    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY="RSAPrivateKey";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK=117;
    /**
     * RSA最大解密明文大小
     */
    private static final int MAX_DECRYPTE_BLOCK=128;

    /**
     * 生成密钥对（公钥和私钥）
     * @return
     */
    public static Map<String,Key> getKeyPair(){
        Map<String,Key> map=new HashMap<>(2);
        try {
            KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(1024);
            KeyPair keyPair=keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey= (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey= (RSAPrivateKey) keyPair.getPrivate();
            map.put(PUBLIC_KEY,publicKey);
            map.put(PRIVATE_KEY,privateKey);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(),e);
        }
        return map;
    }

    /**
     * 用私钥对信息生成数字签名
     * @param data 已加密数据
     * @param privateKey 私钥（BASE64编码）
     * @return
     */
    public static String sign(byte[] data,String privateKey){
        byte[] keyBytes= Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec=new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey privateK=keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Signature signature=Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateK);
            signature.update(data);
            return Base64.encodeBase64String(signature.sign());
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|InvalidKeyException
                |SignatureException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 校验数字签名
     * @param data 已加密数据
     * @param publicKey 公钥（Base64编码）
     * @param sign 数字签名
     * @return
     */
    public static boolean vertify(byte[] data,String publicKey,String sign){
        byte[] keyBytes=Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509EncodedKeySpec=new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicK=keyFactory.generatePublic(x509EncodedKeySpec);
            Signature signature=Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicK);
            signature.update(data);
            return signature.verify(Base64.decodeBase64(sign));
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|InvalidKeyException
                |SignatureException e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 私钥解密
     * @param encryptedData 已加密数据
     * @param privateKey 私钥（Base64编码）
     * @return
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData,String privateKey){
        byte[] keyBytes=Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec=new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
            Key privateK=keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE,privateK);
            return decryptSegmented(encryptedData,cipher);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|NoSuchPaddingException
                |InvalidKeyException|BadPaddingException|IllegalBlockSizeException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 对数据分段解密
     * @param encryptedData 加密的数据
     * @param cipher
     * @return
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static byte[] decryptSegmented(byte[] encryptedData,Cipher cipher) throws BadPaddingException, IllegalBlockSizeException {
        int inputLen=encryptedData.length;
        byte[] decryptedData=null;
        int offset=0;
        byte[] cache;
        int i=0;
        try(ByteArrayOutputStream out=new ByteArrayOutputStream()){
            //对数据分段解密
            while(inputLen-offset>0){
                if(inputLen-offset>MAX_DECRYPTE_BLOCK){
                    cache=cipher.doFinal(encryptedData,offset,MAX_DECRYPTE_BLOCK);
                }else{
                    cache=cipher.doFinal(encryptedData,offset,inputLen-offset);
                }
                out.write(cache,0,cache.length);
                i++;
                offset=i*MAX_DECRYPTE_BLOCK;
            }
            decryptedData=out.toByteArray();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return decryptedData;
    }

    /**
     * 公钥解密
     * @param encryptedData 已加密数据
     * @param publicKey 公钥（Base64编码）
     * @return
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData,String publicKey){
        byte[] keyBytes=Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509EncodedKeySpec=new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
            Key publicK=keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE,publicK);
            return decryptSegmented(encryptedData,cipher);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|NoSuchPaddingException
                |InvalidKeyException|BadPaddingException|IllegalBlockSizeException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 对数据分段加密
     * @param data 源数据
     * @param cipher
     * @return
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static byte[] encryptSegmented(byte[] data,Cipher cipher) throws BadPaddingException, IllegalBlockSizeException {
        int inputLen=data.length;
        byte[] encryptedData=null;
        int offset=0;
        byte[] cache;
        int i=0;
        try(ByteArrayOutputStream out=new ByteArrayOutputStream()){
            //对数据分段解密
            while(inputLen-offset>0){
                if(inputLen-offset>MAX_ENCRYPT_BLOCK){
                    cache=cipher.doFinal(data,offset,MAX_ENCRYPT_BLOCK);
                }else{
                    cache=cipher.doFinal(data,offset,inputLen-offset);
                }
                out.write(cache,0,cache.length);
                i++;
                offset=i*MAX_ENCRYPT_BLOCK;
            }
            encryptedData=out.toByteArray();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return encryptedData;
    }

    /**
     * 公钥加密
     * @param data 数据源
     * @param publicKey 公钥（BASE64编码）
     * @return
     */
    public static byte[] encryptByPublicKey(byte[] data,String publicKey){
        byte[] keyBytes=Base64.decodeBase64(publicKey);
        X509EncodedKeySpec x509EncodedKeySpec=new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
            Key publicK=keyFactory.generatePublic(x509EncodedKeySpec);
            //对数据加密
            Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE,publicK);
            return encryptSegmented(data,cipher);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|NoSuchPaddingException
                |InvalidKeyException|BadPaddingException|IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 私钥加密
     * @param data 源数据
     * @param privateKey 私钥（Base64编码）
     * @return
     */
    public static byte[] encryptByPrivateKey(byte[] data,String privateKey){
        byte[] keyBytes=Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec=new PKCS8EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);
            Key privateK=keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE,privateK);
            return encryptSegmented(data,cipher);
        } catch (NoSuchAlgorithmException|InvalidKeySpecException|NoSuchPaddingException
                |InvalidKeyException|BadPaddingException|IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
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
        System.err.println("公钥加密——私钥解密");
        String source = "这是一行没有任何意义的文字，你看完了等于没看，不是吗？";
        System.out.println("\r加密前文字：\r\n" + source);
        byte[] data = source.getBytes();
        Map<String,Key> keyMap=RSAUtil.getKeyPair();
        byte[] encodedData = RSAUtil.encryptByPublicKey(data, RSAUtil.getPublicKey(keyMap));
        System.out.println("加密后文字：\r\n" + new String(encodedData,"utf8"));
        byte[] decodedData = RSAUtil.decryptByPrivateKey(encodedData, RSAUtil.getPrivateKey(keyMap));
        String target = new String(decodedData,"utf8");
        System.out.println("解密后文字: \r\n" + target);
    }
}
