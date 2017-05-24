package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.User;
import com.jfinal.core.Controller;

/**
 * Created by MrQin on 2016/11/29.
 */
public class HomeController extends Controller {

    public void index(){
        User user=getSessionAttr(Constant.USER_KEY);
        if(UserType.SUPER_ADMIN.getTypeCode()==user.getAid()){//超级管理员
            redirect("/admin");
        }else{//
            redirect("/meetlist");
        }
    }
}
