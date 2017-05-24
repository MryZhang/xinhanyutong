package com.dbkj.meet.controller;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.services.ThresholdService;
import com.dbkj.meet.services.inter.IThresoldService;
import com.jfinal.core.Controller;

/**
 * Created by DELL on 2017/03/01.
 */
public class ThresholdController extends Controller {

    private IThresoldService thresoldService=new ThresholdService();

    public void index(){
        setAttr("tval",thresoldService.getThresold().getValue());
        render("index.html");
    }

    public void update(){
        String value=getPara("value");
        Result result=thresoldService.updateThresold(value);
        renderJson(result);
    }
}
