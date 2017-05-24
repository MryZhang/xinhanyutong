package com.dbkj.meet.utils;

import org.bouncycastle.util.Arrays;

import java.nio.charset.Charset;

/**
 * 提供基于PKCS7算法的加解密接口.
 * Created by MrQin on 2016/11/7.
 */
public class PKCS7Encoder {

    private static Charset CHARSET=Charset.forName("utf-8");
    private static int BLOCK_SIZE=32;

    /**
     * 获得明文进行补位填充的字节
     * @param count 需要进行填充补位操作的明文字节个数
     * @return 补齐用的字节数组
     */
    @SuppressWarnings("Since15")
    public static byte[] encode(int count){
        //计算需要填充的位数
        int amountToPad=BLOCK_SIZE-(count%BLOCK_SIZE);
        if(amountToPad==0){
            amountToPad=BLOCK_SIZE;
        }
        //获得补位所用的字符
        char padChr=chr(amountToPad);
        String tmp=new String();
        for(int index=0;index<amountToPad;index++){
            tmp+=padChr;
        }
        return tmp.getBytes(CHARSET);
    }

    /**
     * 删除解密后明文的补位字符
     * @param decrypted 解密后的明文
     * @return 删除补位字符后的明文
     */
    public static byte[] decode(byte[] decrypted){
        int pad=decrypted[decrypted.length-1];
        if(pad<1||pad>32){
            pad=0;
        }
        return Arrays.copyOfRange(decrypted,0,decrypted.length-pad);
    }

    private static char chr(int a){
        byte target= (byte) (a&0xFF);
        return (char) target;
    }

}
