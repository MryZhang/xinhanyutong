package com.dbkj.meet.utils;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by DELL on 2017/04/02.
 */
public class StringFilter {

    private static final Prop FILTER_PROP=PropKit.use("stringFilter.properties");


    /**
     * 过滤特殊字符
     * @param value
     */
    public static String filter(String value){
        if(value==null){
            return null;
        }
        Properties properties=FILTER_PROP.getProperties();
        String[] arr=value.split("");
        StringBuilder str=new StringBuilder(value.length());
        for(int i=0;i<arr.length;i++){
            for(Map.Entry entry:properties.entrySet()){
                String key=entry.getKey().toString();
                if(arr[i].contains(entry.getKey().toString())){
                    arr[i]=arr[i].replace(key,entry.getValue().toString());
                    break;
                }
            }
            str.append(arr[i]);
        }
        return str.toString();
    }

    public static void main(String[] args) {
        String str="%亲啊是的$";
        System.out.println(filter(str));
    }

}
