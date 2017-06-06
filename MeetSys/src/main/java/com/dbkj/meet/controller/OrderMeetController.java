package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.BalanceInterceptor;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.OrderMeetService;
import com.dbkj.meet.services.inter.IOrderMeetService;
import com.jfinal.aop.Before;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/7.
 */
public class OrderMeetController extends Controller {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private IOrderMeetService orderMeetService= Enhancer.enhance(OrderMeetService.class);

    private User user;

    private User getUser(){
        if(user==null){
            user=getSessionAttr(Constant.USER_KEY);
        }
        return user;
    }

    @Before({BalanceInterceptor.class})
    public void index(){
        User user=getSessionAttr(Constant.USER_KEY);
        Map<String,Object> map=orderMeetService.getRenderData(this);
        setAttrs(map);
        render("index.html");
    }

    public void getContacts(){//获取联系人json数据
        String search=getPara("search");
        User user=getUser();
        orderMeetService=new OrderMeetService();
        List<BaseNode> list=orderMeetService.getContacts(search,user);
        renderJson(list);
    }

    public void producePwd(){//获取密码
        String password = orderMeetService.getPassword();
        renderJson("{\"result\":\""+password+"\"}");
    }

    @Before({POST.class,BalanceInterceptor.class})
    public void create(){//创建预约会议
        Result<?> result =null;
        try {
            result=orderMeetService.createOrderMeet(this);
        }catch (Exception ex){
            logger.error(ex.getMessage(),ex);
            result=new Result<Object>(false,"服务器内部错误，操作失败！");
        }
        renderJson(result);
    }


    public void cancel(){//取消会议
        long oid=getParaToLong("id");
        int type=getParaToInt("type");
        boolean result=orderMeetService.cancelMeet(oid,type);
        renderJson("{\"result\":"+result+"}");
    }

    public void showUpdate(){//修改预约会议
        Long id=getParaToLong("id");
        Integer type=getParaToInt("type");
        Map<String,Object> map=orderMeetService.getShowUpdatePageData(id,type);
        setAttrs(map);
        render("index.html");
    }
}
