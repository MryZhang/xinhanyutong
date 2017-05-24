package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.MeetingRecordService;
import com.dbkj.meet.services.inter.IMeetingRecordService;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

import java.util.Map;

/**
 * Created by MrQin on 2016/11/25.
 */
public class MeetingRecordController extends Controller {

    private IMeetingRecordService meetingRecordService=new MeetingRecordService();

    private User user;

    private User getUser(){
        if(user==null){
            getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }

    public void index(){
        Map<String,Object> map=meetingRecordService.getRecordPageData(this);
        setAttrs(map);
        render("index.html");
    }

    public void detail(){//会议详情
        long id=getParaToLong();
        String queryString=getRequest().getQueryString();
        Map<String,Object> map=meetingRecordService.getRecordDetail(id);
        setAttrs(map);
        setAttr("queryString", StrKit.isBlank(queryString)?"":"?"+queryString);
        render("detail.html");
    }


}
