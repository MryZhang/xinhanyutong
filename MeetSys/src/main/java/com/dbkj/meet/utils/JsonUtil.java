package com.dbkj.meet.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL on 2017/03/09.
 */
public class JsonUtil {

    /**
     * 将json字符串转换为HashMap
     * @param str
     * @return
     */
    public static Map<String,Object> parseToMap(String str){
        Gson gson=new Gson();
        Map<String,Object> resultMap=gson.fromJson(str,new TypeToken<HashMap<String,Object>>(){}.getType());
        return resultMap;
    }
}
