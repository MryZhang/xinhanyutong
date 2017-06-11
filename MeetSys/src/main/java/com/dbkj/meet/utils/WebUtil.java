package com.dbkj.meet.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by DELL on 2017/06/08.
 */
public class WebUtil {

    /**
     * 判断请求是否是ajax请求
     * @param request
     * @return
     */
    public static boolean isAjax(HttpServletRequest request){
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
}
