package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.Chargeback;
import com.dbkj.meet.services.inter.IChargingService;
import com.dbkj.meet.utils.ExcelUtil;
import com.dbkj.meet.utils.FileUtil;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2017/03/14.
 */
public class ChargingServce implements IChargingService {

    private final int PAGE_SIZE=15;

    @Override
    public Map<String, Object> getChargingRecordPage(Map<String, String[]> paraMap) {
        //获取用户分页数据
        //查询条件
        Map<String,Object> params=new HashMap<String, Object>();
        params.put(Constant.CURRENT_PAGE_KEY,1);
        params.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);

        String[] indexPara=paraMap.get("pageIndex");
        if(indexPara!=null){//当前页码
            params.put(Constant.CURRENT_PAGE_KEY,indexPara[0]);
        }

        Map<String,Object> map=getParameters(paraMap,params);

        Page<Chargeback> recordPage=Chargeback.dao.getPage(params);
        map.put("pages", recordPage);
        return map;
    }

    private Map<String,Object> getParameters(Map<String,String[]> paraMap,Map<String,Object> params){
        Map<String,Object> map=new HashMap<String,Object>();

        if(paraMap!=null){
            String[] searchStr=paraMap.get("search");
            if(searchStr!=null){
                String search=searchStr[0];
                params.put("b.name",search);
                map.put("search",search);
            }

            String[] beginPara=paraMap.get("beginTime");
            if(beginPara!=null){
                String beginTime=beginPara[0];
                if(!StrKit.isBlank(beginTime)){
                    params.put(Chargeback.BEGIN_TIME,beginTime+" 00:00:00");
                    map.put("beginTime",beginTime);
                }

            }

            String[] endPara=paraMap.get("endTime");
            if(endPara!=null){
                String endTime=endPara[0];
                if(!StrKit.isBlank(endTime)){
                    params.put(Chargeback.END_TIME,endTime+" 23:59:59");
                    map.put("endTime",endTime);
                }
            }
        }
        return map;
    }

    @Override
    public File export(Map<String, String[]> paraMap) {
        Map<String,Object> params=new HashMap<String,Object>();
        Map<String,Object> map=getParameters(paraMap,params);

        List<Chargeback> list=Chargeback.dao.getList(params);
        String[] heads=new String[]{"公司名称","扣费金额（元）","扣费说明","扣费时间"};
        String[] columns=new String[]{"name","fee","remark","gmt_created"};
        //导出前先清除垃圾
        String directory="chargeback/";
        String path= PathKit.getWebRootPath()+"/download/"+directory;
        FileUtil.deleteFiles(path);

        ExcelUtil excelUtil=new ExcelUtil();
        String fileName = excelUtil.writeExcel(list,directory,heads,columns);
        return new File(path+fileName);
    }
}
