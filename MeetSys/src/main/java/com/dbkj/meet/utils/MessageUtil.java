package com.dbkj.meet.utils;

import com.dbkj.meet.dic.MessageConstant;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信工具类
 * Created by DELL on 2017/03/23.
 */
public class MessageUtil {

    private static final Logger log=Logger.getLogger(MessageUtil.class);
    /**
     * 短信服务接口地址
     */
    private static final String serviceUrl= PropKit.use("config.properties").get("messageService");

    /**
     * 用户业务ID
     */
    private static final String servId=PropKit.use("config.properties").get("servId");
    /**
     * 用户业务鉴权
     */
    private static final String servAuth=PropKit.use("config.properties").get("servAuth");

    /**
     * 发送短信
     * @param paraMap 参数Map，必须包含
     *                mobile:要发送的手机号码
     *                sysContent:短信内容，不超过250字
     *                可选：longSms:是否长短信发送，取值：2000：是，2001：否
     *                不传默认为2001：否
     * @return
     */
    public static Map<String,Object> sendMessage(Map<String,Object> paraMap){
        paraMap.put(MessageConstant.REQ_ID,RandomUtil.getSeqId());
        paraMap.put(MessageConstant.SERV_ID,servId);
        paraMap.put(MessageConstant.SERV_AUTH,servAuth);
        paraMap.put(MessageConstant.ACTION,MessageConstant.ACTION_SEND_SMS);
        String result= HttpKit.post(serviceUrl, JsonKit.toJson(paraMap));
        log.info(result);
        return JsonUtil.parseToMap(result);
    }

    public static void main(String[] args) {
        Map<String,Object> map=new HashMap<>();
        map.put(MessageConstant.MOBILE,"13277054876");
        map.put(MessageConstant.SMS_CONTENT,"hello hello短息来了!");
        sendMessage(map);
    }
}
