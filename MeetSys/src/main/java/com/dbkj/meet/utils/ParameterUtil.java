package com.dbkj.meet.utils;

import com.jfinal.kit.StrKit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2017/02/25.
 */
public class ParameterUtil {

    public static Map<String,Object> getParaMap(Map<String,String[]> map){
        Map<String,Object> paramMap=new HashMap<String, Object>(10);
        if(map!=null){
            for(String key:map.keySet()){
                String[] value=map.get(key);
                if(value!=null&&map.get(key).length>0){
                    if(!StrKit.isBlank(value[0])){
                        paramMap.put(key,value[0]);
                    }
                }
            }
        }
        return paramMap;
    }

    public static void getPara(Map<String,Object> paraMap, StringBuilder where, List<Object> params){
        for(Map.Entry<String,Object> entry:paraMap.entrySet()){
            String key=entry.getKey();
            Object value=entry.getValue();
            if(value!=null){
                if(where.length()==0){
                    where.append(" WHERE ");
                }else{
                    where.append(" AND ");
                }
                where.append(key);
                where.append("=?");
                params.add(value);
            }
        }
    }
}
