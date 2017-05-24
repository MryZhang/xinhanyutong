package com.dbkj.meet.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/27.
 */
public class DaoUtil {

    public static void getCondition(Map<String,Object> map, StringBuilder stringBuilder, List<Object> params){
        if(stringBuilder==null){
            stringBuilder=new StringBuilder();
        }
        if(params==null){
            params=new ArrayList<>();
        }
        if(map!=null){
            for(String key:map.keySet()){
                Object value=map.get(key);
                if(value!=null){
                    if(stringBuilder.length()==0){
                        stringBuilder.append(" WHERE ");
                    }else {
                        stringBuilder.append(" AND ");
                    }
                    stringBuilder.append(key);
                    stringBuilder.append("=?");
                    params.add(value);
                }
            }
        }
    }
}
