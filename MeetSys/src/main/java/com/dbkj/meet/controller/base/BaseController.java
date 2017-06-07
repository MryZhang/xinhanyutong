package com.dbkj.meet.controller.base;

import com.dbkj.meet.interceptors.BusinessInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/06/06.
 */
@Before({BusinessInterceptor.class})
public class BaseController extends Controller{

}
