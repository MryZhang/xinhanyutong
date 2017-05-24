package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.MeetListItem;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.MeetListService;
import com.dbkj.meet.services.inter.IMeetListService;
import com.jfinal.core.Controller;

import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/7.
 */
public class MeetListController extends Controller {

    private IMeetListService meetListService=new MeetListService();

    private User user;

    private User getUser(){
        if(user==null){
            user=getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }

    public void index(){
        Long uid=getUser().getId();
        List<MeetListItem> inmeet = meetListService.getMeetList(uid);
        setAttr("inmeet",inmeet);
        List<MeetListItem> notStart=meetListService.getNotStartMeetList(uid);
        setAttr("noStart",notStart);
        render("index.html");
    }

    public void detail(){
        Map<String,Object> map = meetListService.getMeetDetail(this);
        setAttrs(map);
        render("detail.html");
    }
}
