package com.dbkj.meet.utils;

import java.util.Random;

/**
 * Created by MrQin on 2016/11/11.
 */
public class RandomUtil {

    /**
     * 获取一个随机的32位字符串
     * @return
     */
    public static String getSeqId() {
        return getRanCode(32);
    }

    /**
     * 获取一个指定长度的随机码
     * @param len
     * @return
     */
    public static String getRanCode(int len) {
        Random randGen = new Random();
        char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
                .toCharArray();
        char[] randBuffer = new char[len];
        for (int i = 0; i < randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(35)];
        }
        return new String(randBuffer);
    }

    /**
     * 获取指定长度的随机密码
     * @param length
     * @return
     */
    public static String getPwd(int length){
        StringBuilder sb=new StringBuilder(length);
        for(int i=0;i<length;i++){
            sb.append(Math.round(Math.random()*9));
        }
        return sb.toString();
    }



}
