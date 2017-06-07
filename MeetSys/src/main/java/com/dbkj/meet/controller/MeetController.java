package com.dbkj.meet.controller;

import com.dbkj.meet.controller.base.BaseController;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.BaseContact;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.BalanceInterceptor;
import com.dbkj.meet.interceptors.BusinessInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.interceptors.LoginInterceptor;
import com.dbkj.meet.model.Record;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.MeetControlService;
import com.dbkj.meet.services.inter.IMeetControlService;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;

import java.util.Map;

/**
 * Created by MrQin on 2016/11/17.
 */
public class MeetController extends BaseController {

    private final IMeetControlService meetControlService=enhance(MeetControlService.class);


    private User user;

    private User getUser(){
        if(user==null){
            user = getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }

    @Before({BalanceInterceptor.class})
    public void index(){
        Integer id=getParaToInt();
        Map<String,Object> map=meetControlService.getMeetData(getUser(),id);
        setAttrs(map);
        render("index.html");
    }

    public void addPersonalContact(){
        BaseContact baseContact=getBean(BaseContact.class,"c");
        long uid=((User)getSessionAttr(Constant.USER_KEY)).getId();
        Result result=meetControlService.addPersonalContact(baseContact,uid);
        renderJson(result);
    }

    @Before({BalanceInterceptor.class})
    public void restart(){//会议重新开始
        long id=getParaToLong();
        Map<String,Object> map=meetControlService.getMeetRestartData(id,getUser());
        redirect("/meet/"+((Record)map.get("record")).getId());
    }

    public void addInvite(){//添加会议邀请人
        Result result=meetControlService.addAttendee(this);
        renderJson(result);
    }

    public void delInvite(){
        boolean result=meetControlService.removeAttendee(this);
        renderJson("{\"result\":"+result+"}");
    }

    @Before({BalanceInterceptor.class})
    public void createmeet(){//开始会议
        Result<Map<String,Object>> result=meetControlService.creeteMeet(this);
        renderJson(result);
    }

    public void finishMeet(){//结束会议
        long rid=getParaToLong();
        boolean result=meetControlService.updateMeetStatus(rid);
        renderJson("{\"result\":"+result+"}");
    }

    public void cancelMeet(){//取消已创建未开始的会议
        Long rid=getParaToLong();
        boolean result=meetControlService.cancelMeet(rid);
        renderJson("{\"result\":"+result+"}");
    }

    public void getRemark(){//获取会议备注
        long rid=getParaToLong();
        String remark=meetControlService.getRecordMark(rid);
        renderText(remark);
    }

    public void updateRemark(){
        int rid=getParaToInt("rid");
        String text=getPara("txt");
        boolean result=meetControlService.updateRemark(rid,text);
        renderJson("{\"result\":"+result+"}");
    }

    public void updateRecordState(){
        Integer rid=getParaToInt();
        boolean result=meetControlService.updateRecordState(rid);
        renderJson("{\"result\":"+result+"}");
    }

    @ActionKey("/phonemeeting/stopMeet")
    @Clear({LoginInterceptor.class, InfoInterceptor.class,BusinessInterceptor.class})
    public void stopMeet(){//接收通话上报接口发送过来的消息
        meetControlService.updateAfterMeetStop(getRequest());
        renderNull();
    }

    @ActionKey("/phonemeeting/getStatus")
    @Clear({LoginInterceptor.class,InfoInterceptor.class, BusinessInterceptor.class})
    public void getStatus(){//接收会议、用户状态变更时推送过来的消息
        meetControlService.getStatus(getRequest());
        renderNull();
    }

    @Before({BalanceInterceptor.class})
    public void createFixedMeet(){//创建固定会议
        Long id=getParaToLong();
        Map<String,Object> map = meetControlService.createFixedMeet(id,getUser());
        redirect("/meet/"+((Record)map.get("record")).getId());
    }

    public void setHost(){//设置为的第二主持人
        Result result=meetControlService.setHost(this);
        renderJson(result);
    }

}
