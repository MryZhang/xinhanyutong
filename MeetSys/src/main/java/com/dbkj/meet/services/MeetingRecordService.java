package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.Attendee;
import com.dbkj.meet.model.Record;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.IMeetingRecordService;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/25.
 */
public class MeetingRecordService implements IMeetingRecordService {

    private final int PAGE_SIZE=15;

    /**
     * 获取会议记录的分页数据
     * @param controller
     * @return
     */
    public Map<String, Object> getRecordPageData(Controller controller) {
        Map<String,Object> map=new HashMap<String, Object>();
        User user=controller.getSessionAttr(Constant.USER_KEY);

        //获取下载录音地址
        PropKit.use("config.properties");
        String download= PropKit.get("downloadUrl");
        map.put("download", download);

        //获取用户分页数据
        //查询条件
        Map<String,Object> params=new HashMap<String, Object>();
        params.put(Constant.CURRENT_PAGE_KEY,1);
        params.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);
        params.put("r.belong",user.getId());

        Map<String,String[]> paraMap = controller.getParaMap();
        String[] indexPara=paraMap.get("pageIndex");
        if(indexPara!=null){//当前页码
            params.put(Constant.CURRENT_PAGE_KEY,indexPara[0]);
        }

        String[] searchStr=paraMap.get("search");
        if(searchStr!=null){
            String search=searchStr[0];
            params.put("r.subject",search);
            map.put("search",search);
        }

        String[] beginPara=paraMap.get("beginTime");
        if(beginPara!=null){
            String beginTime=beginPara[0];
            if(!StrKit.isBlank(beginTime)){
                params.put(Record.BEGIN_TIME,beginTime+" 00:00:00");
                map.put("beginTime",beginTime);
            }

        }

        String[] endPara=paraMap.get("endTime");
        if(endPara!=null){
            String endTime=endPara[0];
            if(!StrKit.isBlank(endTime)){
                params.put(Record.END_TIME,endTime+" 23:59:59");
                map.put("endTime",endTime);
            }
        }

        Page<Record> recordPage=Record.dao.getRecordPages(params);
        map.put("totalPage", recordPage.getTotalPage());
        map.put("currentPage", recordPage.getPageNumber());
        map.put("pageSize", recordPage.getPageSize());
        map.put("totalRow", recordPage.getTotalRow());
        map.put("records", recordPage.getList());

        return map;
    }

    /**
     * 获取会议记录详情
     * @param id
     * @return
     */
    public Map<String, Object> getRecordDetail(long id) {
        Map<String,Object> map=new HashMap<String, Object>();
        Record record=Record.dao.findById(id);
        map.put("record",record);
        List<Attendee> attendeeList=Attendee.dao.findByRecordId(id);
        map.put("alist",attendeeList);
        return map;
    }
}
