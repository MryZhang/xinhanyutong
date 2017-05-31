package com.dbkj.meet.utils;

import com.dbkj.meet.dic.Constant;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by MrQin on 2016/11/7.
 */
public class AESUtil1 {

    private static Charset CHARSET=Charset.forName("utf-8");

    /**
     * 生成4个字节的网络字节序
     * @param sourceNumber
     * @return
     */
    private static byte[] getNetworkBytesOrder(int sourceNumber){
        byte[] orderBytes = new byte[4];
        orderBytes[3] = (byte) (sourceNumber & 0xFF);
        orderBytes[2] = (byte) (sourceNumber >> 8 & 0xFF);
        orderBytes[1] = (byte) (sourceNumber >> 16 & 0xFF);
        orderBytes[0] = (byte) (sourceNumber >> 24 & 0xFF);
        return orderBytes;
    }

    /**
     * 还原4个字节的网络字节序
     * @param orderBytes
     * @return
     */
    private static int recoverNetworkBytesOrder(byte[] orderBytes){
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= orderBytes[i] & 0xff;
        }
        return sourceNumber;
    }

    /**
     * 随机生成16位字符串
     * @return
     */
    private static String getRandomStr(){
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 对明文进行加密
     * @param key 解密密钥
     * @param text 要解密的明文
     * @return 加密后base64编码的字符串
     */
    @SuppressWarnings("Since15")
    public static String encrypt(String key, String text) throws Exception{
        ByteGroup byteCollector=new ByteGroup();
        byte[] randomStrBytes=getRandomStr().getBytes(CHARSET);
        byte[] textBytes=text.getBytes(CHARSET);
        byte[] networkBytesOrder=getNetworkBytesOrder(textBytes.length);
        // randomStr + networkBytesOrder + text
        byteCollector.addBytes(randomStrBytes);
        byteCollector.addBytes(networkBytesOrder);
        byteCollector.addBytes(textBytes);

        //...+pad:使用自定义的填充方式对明文进行补位填充
        byte[] padBytes=PKCS7Encoder.encode(byteCollector.size());
        byteCollector.addBytes(padBytes);

        //获取最终的字节流，为加密
        byte[] unencryted=byteCollector.toBytes();

        //设置加密模式为AES的CBC模式
        Cipher cipher=Cipher.getInstance("AES/CBC/NoPadding");
        byte[] aesKey=Base64.decodeBase64(key);
        SecretKeySpec keySpec=new SecretKeySpec(aesKey,"AES");
        IvParameterSpec iv=new IvParameterSpec(aesKey,0,16);
        cipher.init(Cipher.ENCRYPT_MODE,keySpec,iv);

        //加密
        byte[] encrypted=cipher.doFinal(unencryted);

        //使用BASE64对加密后的字符串进行编码
        String base64Encrypted=new Base64().encodeAsString(encrypted);
        return base64Encrypted;
    }

    /**
     * 对密文进行解密
     * @param key 密钥
     * @param text 需要解密的密文
     * @return
     * @throws Exception
     */
    @SuppressWarnings("Since15")
    public static String decrypt(String key, String text) throws Exception{
        byte[] original;
        //设置解密模式为AES的CBC模式
        Cipher cipher=Cipher.getInstance("AES/CBC/Nopadding");
        byte[] aesKey=Base64.decodeBase64(key);
        SecretKeySpec keySpec=new SecretKeySpec(aesKey,"AES");
        IvParameterSpec iv=new IvParameterSpec(Arrays.copyOfRange(aesKey,0,16));
        cipher.init(Cipher.DECRYPT_MODE,keySpec,iv);

        //使用BASE64对密文进行解码
        byte[] encrypted=Base64.decodeBase64(text);

        //解密
        original=cipher.doFinal(encrypted);

        //去除补位字符
        byte[] bytes=PKCS7Encoder.decode(original);

        //分离16位随机字符串，网络字节序
        byte[] networkOrder=Arrays.copyOfRange(bytes,16,20);

        int contentLength=recoverNetworkBytesOrder(networkOrder);
        String content=new String(Arrays.copyOfRange(bytes,20,20+contentLength),CHARSET);
        return content;
    }

    public static void main(String[] args) throws Exception {
//        String encodingAesKey = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";
//        String text = "我是中文abcd123";
//        String encrypted=encrypt(encodingAesKey,text);
//        System.out.println("加密后："+encrypted);
//        String decrypted=decrypt(encodingAesKey,encrypted);
//        System.out.println("解密后："+decrypted);
        System.out.println(decrypt(Constant.ENCRYPT_KEY,"qnFsjic4wGbO3TXO/pGVrg=="));
    }
}
