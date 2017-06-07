package com.dbkj.meet.controller;

import com.dbkj.meet.controller.base.BaseAdminController;
import com.dbkj.meet.services.RateService;
import com.dbkj.meet.services.inter.IRateService;
import com.dbkj.meet.vo.PackageListItemVo;

import java.util.List;

/**
 * 计费相关
 * Created by DELL on 2017/02/28.
 */
public class RateController extends BaseAdminController {

    private final IRateService rateService=new RateService();

    public void index(){
        List<PackageListItemVo> rateVoList=rateService.getRateInfo();
        setAttr("rateList",rateVoList);
        render("index.html");
    }
}
