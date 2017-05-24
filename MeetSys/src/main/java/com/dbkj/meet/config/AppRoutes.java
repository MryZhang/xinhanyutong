package com.dbkj.meet.config;

import com.dbkj.meet.controller.*;
import com.jfinal.config.Routes;

/**
 * Created by MrQin on 2016/11/4.
 */
public class AppRoutes extends Routes {
    public void config() {
        add("/",HomeController.class);
        add("/login", LoginController.class,"/pages/login/");
        add("/meetlist", MeetListController.class,"/pages/meetlist/");
        add("/ordermeet", OrderMeetController.class,"/pages/order/");
        add("/admin", AdminController.class,"/pages/admin/");
        add("/admin/charge",ChargeController.class,"/pages/admin/charge/");
        add("/admin/rate",RateController.class,"/pages/admin/rate");
        add("/admin/threshold",ThresholdController.class,"/pages/admin/threshold");
        add("/admin/charging",ChargingController.class,"/pages/admin/charging");
        add("/company", CompanyController.class);
        add("/user",UserController.class,"/pages/user/");
        add("/employee",EmployeeController.class,"/pages/employee/");
        add("/department",DepartmentController.class);
        add("/meet",MeetController.class,"/pages/mc/");
        add("/personalcontacts",PersonalContactsController.class,"/pages/personal/");
        add("/publiccontacts",PublicContactsController.class,"/pages/pub/");
        add("/record",MeetingRecordController.class,"/pages/record/");
        add("/amount",AmountController.class);
        add("/account",AccountController.class,"/pages/account/");
        add("/msg",MessageController.class);
        add("/admin/package",PackageController.class,"/pages/admin/package/");
        add("/smtp",SMTPController.class,"/pages/smtp");
    }
}
