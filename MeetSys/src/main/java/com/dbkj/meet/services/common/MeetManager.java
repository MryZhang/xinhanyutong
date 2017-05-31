package com.dbkj.meet.services.common;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.FixedMeet;
import com.dbkj.meet.model.Record;
import com.dbkj.meet.utils.RandomUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.IDataLoader;

import java.util.*;

/**
 * 会议管理类
 * Created by MrQin on 2016/11/17.
 */
public class MeetManager {

    public static final String PASSWORD_CACHE_KEY ="4_digit_password";
    private static MeetManager instance;
    private static Object lock=new Object();

    private MeetManager(){}

    private static class InstanceHolder{
        private static MeetManager instance=new MeetManager();
    }

    public static MeetManager getInstance(){
        return InstanceHolder.instance;
    }

    /**
     * 获取会议接口服务的地址
     * @return
     */
    public String getMeetServiceUrl(){
        return PropKit.use("config.properties").get("meetService");
    }

    /**
     * 获取会议方数
     * @return
     */
    public int getMeetNums(){
        return PropKit.use("config.properties").getInt("meetNums",200);
    }


    /**
     * 生成不重复的4位会议密码
     * @return
     */
    public String getPassword() {
        String password=null;
        //循环生成密码直到没有
        do {
            password=RandomUtil.getPwd(4);
        }while (isUsedPassword(password));
        //生成密码成功将当前密码添加到已使用的密码中
        getUsedPassword().add(password);
        return password;
    }

    private Set<String> getUsedPassword(){
        //从缓存中获取使用的密码
        Set<String> passwordSet = CacheKit.get(Constant.CACHE_NAME_ADMDIV, PASSWORD_CACHE_KEY, new IDataLoader() {
            public Object load() {
                List<Record> recordList = Record.dao.getUsedPassword();
                List<FixedMeet> fixedMeets=FixedMeet.dao.getUsedPassword();
                Set<String> stringSet=new HashSet<String>();
                for(int i=0,length=recordList.size();i<length;i++){
                    Record record=recordList.get(i);
                    stringSet.add(record.getHostPwd());
                    stringSet.add(record.getListenerPwd());
                }
                for(int i=0,length=fixedMeets.size();i<length;i++){
                    FixedMeet fixedMeet=fixedMeets.get(i);
                    stringSet.add(fixedMeet.getListenerPwd());
                    stringSet.add(fixedMeet.getHostPwd());
                }
                return stringSet;
            }
        });
        return passwordSet;
    }

    public synchronized boolean isUsedPassword(String password) {
        Set<String> passwordSet=getUsedPassword();
        return passwordSet.contains(password);
    }

    /**
     * 移除缓存中已使用的密码
     */
    public void removeUsedPasswordInCache(){
        CacheKit.remove(Constant.CACHE_NAME_ADMDIV, PASSWORD_CACHE_KEY);
    }

    /**
     * 创建呼入式电话会议
     * @param map
     * @return
     */
    public Map<String, Object> createMeet(Map<String,Object> map){
        map.put(Constant.REQID, RandomUtil.getSeqId());
        map.put(Constant.MEETNUMS,getMeetNums());//设置会议方数
        if(map.get(Constant.CHAIRMANPWD)==null){
            map.put(Constant.CHAIRMANPWD,getPassword());
        }
        if(map.get(Constant.AUDIENCEPWD)==null){
            map.put(Constant.AUDIENCEPWD,getPassword());
        }
        map.put(Constant.ACTION,Constant.ACTION_CREATE_CALLMEET);
        String result = HttpKit.post(getMeetServiceUrl(), JsonKit.toJson(map));
        Map<String,Object> resultMap = getResult(result);
        //如果创建失败，则在删除缓存对应中的已使用的集合中的密码
        if(!Constant.SUCCESS.equals(resultMap.get(Constant.STATUS).toString())){
            Set<String> usedSet = getUsedPassword();
            usedSet.remove(map.get(Constant.CHAIRMANPWD));
            usedSet.remove(map.get(Constant.AUDIENCEPWD));
        }
        return resultMap;
    }

    public Map<String,Object> getResult(String result){
        Gson gson=new Gson();
        Map<String,Object> resultMap=gson.fromJson(result,new TypeToken<HashMap<String,Object>>(){}.getType());
        return resultMap;
    }


    /**
     * 加入会议
     * @param map
     * @return
     */
    public Map<String, Object> joinMeet(Map<String,Object> map){
        map.put(Constant.REQID,RandomUtil.getSeqId());
        map.put(Constant.ACTION,Constant.ACTION_JOIN_MEET);
        String result = HttpKit.post(getMeetServiceUrl(), JsonKit.toJson(map));
        return getResult(result);
    }

    /**
     * 获取会议中与会电话状态
     * @param meetId
     * @return
     */
    public Map<String,Object> getMeetCallStatus(String meetId){
        Map<String,Object> map=new HashMap<String, Object>();
        map.put(Constant.ACTION,Constant.ACTION_GET_MEET_CALLSTATUS);
        map.put(Constant.REQID,RandomUtil.getSeqId());
        map.put(Constant.MEETID,meetId);
        String result=HttpKit.post(getMeetServiceUrl(),JsonKit.toJson(map));
        return getResult(result);
    }

    /**
     * 开始会场录音
     * @param meetId
     * @return
     */
    public Map<String,Object> startRecord(String meetId){
        Map<String,Object> map=new HashMap<>(3);
        map.put(Constant.REQID,RandomUtil.getSeqId());
        map.put(Constant.MEETID,meetId);
        map.put(Constant.ACTION,Constant.ACTION_BEGIN_RECORD);
        String result=HttpKit.post(getMeetServiceUrl(),JsonKit.toJson(map));
        return getResult(result);
    }

    /**
     * 结束会议
     * @param meetId
     * @return
     */
    public Map<String,Object> stopMeet(String meetId){
        Map<String,Object> map=new HashMap<>(3);
        map.put(Constant.REQID,RandomUtil.getSeqId());
        map.put(Constant.MEETID,meetId);
        map.put(Constant.ACTION,Constant.ACTION_CLOSE_MEET);
        String result=HttpKit.post(getMeetServiceUrl(),JsonKit.toJson(map));
        return getResult(result);
    }

    public static void main(String[] args) {
//        Map<String,Object> resultMap = getInstance().getMeetCallStatus("tf9hq7rd1w4luxwxubbxdpn6t8f8e192");
//        for(String key:resultMap.keySet()){
//            System.out.println(key+":"+resultMap.get(key));
//        }
        Map<String,Object> paraMap=new HashMap<>();
        paraMap.put(Constant.CHAIRMANPWD,"123456");
        paraMap.put(Constant.AUDIENCEPWD,"123456");
        paraMap.put(Constant.COSTNUM,"057185783631");
        paraMap.put(Constant.SHOWNUM,"057185783631");
        paraMap.put(Constant.CALLER,"13277054876");
        paraMap.put(Constant.NAME,"123");
        Map<String,Object> resultMap = getInstance().createMeet(paraMap);
        System.out.println(resultMap.toString());
    }
}
