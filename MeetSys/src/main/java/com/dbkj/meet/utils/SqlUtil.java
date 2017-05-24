package com.dbkj.meet.utils;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Model;


/**
 * 获取sql配置文件中sql的工具类
 * Created by MrQin on 2016/11/10.
 */
public class SqlUtil {


    private static Prop[] props;

    static {
        String location=PropKit.use("config.properties").get("sqlLocation");
        if(location==null){
            Prop prop=new Prop("sql.properties");
            props=new Prop[]{prop};
        }else {
            String[] arr=location.split(",");
            props=new Prop[arr.length];
            for(int i=0;i<arr.length;i++){
                Prop prop=new Prop(arr[i]);
                props[i]=prop;
            }
        }
    }

    public static String getSql(String name){
        for(int i=0;i<props.length;i++){
            Prop prop=props[i];
            String key=prop.get(name);
            if(key!=null){
                return key;
            }
        }
        return null;
    }

    public static <T extends Model> String getSql(String name,T instance){
        String className=instance.getClass().getName();
        return getSql(className+"."+name);
    }

}
