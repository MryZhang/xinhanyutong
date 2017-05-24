package com.dbkj.meet.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by MrQin on 2016/11/8.
 */
public class ValidateUtil {

    /**
     * 验证手机号码
     * @param str
     * @return
     */
    public static boolean validateMobilePhone(String str){
        String regex="^1[3|4|5|7|8]\\d{9}$";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证座机号码
     * @param str
     * @return
     */
    public static boolean validateTelephone(String str){
        String regex="^([0-9]{3,4})?([0-9]{7,9}|[0-9]{5})((p|P|,|-)[0-9]{1,})?$";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证电话号码（包括手机和座机）
     * @param str
     * @return
     */
    public static boolean validatePhone(String str){
        return validateMobilePhone(str)||validateTelephone(str);
    }

    /**
     * 判断字符串是否是数字
     * @param str
     * @return
     */
    public static boolean isNum(String str){
        return str.matches("[0-9]+");
    }

    /**
     * 验证电子邮箱
     * @param str
     * @return
     */
    public static boolean validateEmail(String str){
        String regex="^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证是否是正整数
     * @param str
     * @return
     */
    public static boolean validatePositiveInteger(String str){
        String regex="^[1-9]\\d*$";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证四位数的密码
     * @param str
     * @return
     */
    public static boolean validate4DigitalPassword(String str){
        String regex="\\d{4}";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证大于0的整数或者小数
     * @param str
     * @return
     */
    public static boolean validateNumber(String str){
        String regex="([1-9]\\d*(\\.\\d*[1-9])?)|(0\\.\\d*[1-9])";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证是否包含特殊字符串
     * @param value
     * @return
     */
    public static boolean validateSpecialString(String value){
        String regex="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(value);
        return matcher.find();
    }

    public static void main(String[] args) {
        System.out.println(validateSpecialString("%阿斯蒂芬"));
    }
}
