package com.dbkj.meet.controller;

import com.dbkj.meet.interceptors.AdminInterceptor;
import com.dbkj.meet.interceptors.InfoInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;

/**
 * 管理员控制器基类
 * Created by DELL on 2017/02/28.
 */
@Before({AdminInterceptor.class})
@Clear({InfoInterceptor.class})
public class BaseAdminController extends Controller {

}
