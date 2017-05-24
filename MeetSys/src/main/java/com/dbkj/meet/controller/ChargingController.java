package com.dbkj.meet.controller;

import com.dbkj.meet.services.ChargingServce;
import com.jfinal.core.Controller;

import java.io.File;
import java.util.Map;

/**
 * Created by DELL on 2017/03/14.
 */
public class ChargingController extends BaseAdminController {

    private ChargingServce chargingServce=new ChargingServce();

    public void index(){
        Map<String,Object> map=chargingServce.getChargingRecordPage(getParaMap());
        setAttrs(map);
        render("record.html");
    }

    public void export(){
        File file=chargingServce.export(getParaMap());
        renderFile(file);
    }
}
