package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.model.Department;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.DepartmentService;
import com.dbkj.meet.services.inter.IDepartmentService;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;

/**
 * Created by MrQin on 2016/11/14.
 */
public class DepartmentController extends Controller {

    private IDepartmentService departmentService= Enhancer.enhance(DepartmentService.class);

    @Clear({InfoInterceptor.class})
    @Before({POST.class})
    public void add(){
        Department department=getModel(Department.class,"d");
        User user=getSessionAttr(Constant.USER_KEY);
        Integer cid=user.getCid();
        Result<Department> result=departmentService.addDeparment(department,cid);
        renderJson(result);
    }

    @Before({POST.class})
    public void update(){
        Department department=getModel(Department.class,"d");
        User user=getSessionAttr(Constant.USER_KEY);
        Integer cid=user.getCid();
        Result<Department> result=departmentService.update(department,cid);
        renderJson(result);
    }

    public void delete(){
        Long did=getParaToLong();
        User user=getSessionAttr(Constant.USER_KEY);
        Integer cid=user.getCid();
        boolean result=departmentService.deleteDepart(did,cid);
        renderJson("{\"result\":"+result+"}");
    }
}
