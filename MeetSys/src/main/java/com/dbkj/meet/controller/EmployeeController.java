package com.dbkj.meet.controller;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.dbkj.meet.model.Department;
import com.dbkj.meet.model.Employee;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.EmployeeService;
import com.dbkj.meet.services.inter.IEmployeeService;
import com.dbkj.meet.validator.EmployeeValidator;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.ext.interceptor.POST;
import com.sun.corba.se.impl.oa.poa.POACurrent;

import java.util.List;

/**
 * Created by MrQin on 2016/11/14.
 */
public class EmployeeController extends Controller {

    private IEmployeeService employeeService= Enhancer.enhance(EmployeeService.class);

    @Clear({InfoInterceptor.class})
    public void showPrefect(){
        User user=getSessionAttr(Constant.USER_KEY);
        int cid=user.getCid();
        List<Department> departmentList=employeeService.getDepartments(cid);
        setAttr("dlist",departmentList);
        render("info.html");
    }

    @Clear({InfoInterceptor.class})
    @Before({POST.class, EmployeeValidator.class})
    public void prefect(){
        Employee employee=getModel(Employee.class,"e");
        User user=getSessionAttr(Constant.USER_KEY);
        int cid=user.getCid();
        employee.setPhone(user.getUsername());
        boolean result=employeeService.addEmployee(employee,user);
        if(result){
            redirect("/meetlist");
        }else{
            redirect("/employee/showAdd");
        }
    }
}
