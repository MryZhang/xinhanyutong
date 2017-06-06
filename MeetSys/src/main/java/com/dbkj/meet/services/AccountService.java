package com.dbkj.meet.services;

import com.dbkj.meet.converter.BillDetailConverter;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.model.Bill;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.IAccountService;
import com.dbkj.meet.utils.ExcelUtil;
import com.dbkj.meet.utils.FileUtil;
import com.dbkj.meet.vo.BillDetailVo;
import com.dbkj.meet.vo.BillVo;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.IDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2017/03/02.
 */
public class AccountService implements IAccountService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final String IS_EXPORTING_KEY="is_exporting";

    /**
     * 获取消费总额
     * @param controller
     * @return
     */
    @Override
    public double getTotalConsume(Controller controller) {
        User user=controller.getSessionAttr(Constant.USER_KEY);
        //如果用户是普通用户，则只获取用户的消费总额
        double total=0;

        Map<String,String[]> paraMap=controller.getParaMap();
        Map<String,Object> params=new HashMap<>();
        String beginTime=paraMap.get("beginTime")!=null?paraMap.get("beginTime")[0]:null;
        if(!StrKit.isBlank(beginTime)){
            params.put(com.dbkj.meet.model.Record.BEGIN_TIME,beginTime+" 00:00:00");
            controller.setAttr("beginTime",beginTime);
        }

        String endTime=paraMap.get("endTime")!=null?paraMap.get("endTime")[0]:null;
        if(!StrKit.isBlank(endTime)){
            params.put(com.dbkj.meet.model.Record.BEGIN_TIME,endTime+" 23:59:59");
            controller.setAttr("endTime",endTime);
        }
        String subject=paraMap.get("search")!=null?paraMap.get("search")[0]:null;
        if(!StrKit.isBlank(subject)&&!StrKit.isBlank(subject.trim())){
            params.put(com.dbkj.meet.model.Record.SUBJECT,subject);
            controller.setAttr("search",subject);
        }
        params.put(Bill.dao.BELONG,controller.getSessionAttr(Constant.USER_KEY));
        logger.info(params.toString());
        BigDecimal totalFee=Bill.dao.getTotalFee(params);

        return total=totalFee!=null?totalFee.doubleValue():0;
    }


    private Map<String,Object> getParams(Map<String,String[]> paraMap){
        Map<String,Object> params=new HashMap<>();
        if(paraMap!=null){
            String beginTime=paraMap.get("beginTime")!=null?paraMap.get("beginTime")[0]:null;
            if(!StrKit.isBlank(beginTime)){
                params.put(Bill.dao.BEGIN_TIME_KEY,beginTime+" 00:00:00");
            }

            String endTime=paraMap.get("endTime")!=null?paraMap.get("endTime")[0]:null;
            if(!StrKit.isBlank(endTime)){
                params.put(Bill.dao.END_TIME_KEY,endTime+" 23:59:59");
            }
            String subject=paraMap.get("search")!=null?paraMap.get("search")[0]:null;
            if(!StrKit.isBlank(subject)&&!StrKit.isBlank(subject.trim())){
                params.put(Bill.dao.SUBJECT,subject);
            }
            //获取当前页码，pq_curpage为前端grid组件传过来的参数
            Integer curpage=paraMap.get("pq_curpage")!=null?Integer.parseInt(paraMap.get("pq_curpage")[0]):null;
            if(curpage!=null){
                params.put(Constant.CURRENT_PAGE_KEY,curpage);
            }
            //获取每页显示的数量，pq_rpp为前端grid组件传过来的参数
            Integer rpp=paraMap.get("pq_rpp")!=null?Integer.parseInt(paraMap.get("pq_rpp")[0]):null;
            if(rpp!=null){
                params.put(Constant.PAGE_SIZE_KEY,rpp);
            }
        }
        return params;
    }

    @Override
    public BillVo getBillPage(Map<String, String[]> paraMap,User user) {
        final Map<String,Object> map = getParams(paraMap);
        map.put(Bill.dao.BELONG,user);
        Page<Record> page = CacheKit.get(Constant.CACHE_NAME_BILLCACHE, generateCacheKey(map), new IDataLoader() {
            @Override
            public Object load() {
//                logger.info("从数据库获取数据");
                return Bill.dao.getBillPage(map);
            }
        });
        BillVo bv=new BillVo();
        bv.setCurPage(page.getPageNumber());
        bv.setTotalRecords(page.getTotalRow());
        bv.setData(page.getList());
        return bv;
    }

    private String generateCacheKey(Map<String,Object> map){
        StringBuilder key=new StringBuilder(BillVo.class.getSimpleName()+":");
        for(Map.Entry<String,Object> entry:map.entrySet()){
            key.append(entry.getKey()+"="+entry.getValue()+"&");
        }
        return key.toString().substring(0,key.length()-1);
    }

    @Override
    public BillVo getBillDetail(long id,Map<String,String[]> paraMap) {
        Map<String,Object> params=new HashMap<String,Object>();
        params.put(Bill.dao.RECORD_ID,id);
        //获取当前页码，pq_curpage为前端grid组件传过来的参数
        Integer curpage=paraMap.get("pq_curpage")!=null?Integer.parseInt(paraMap.get("pq_curpage")[0]):null;
        if(curpage!=null){
            params.put(Constant.CURRENT_PAGE_KEY,curpage);
        }else{
            params.put(Constant.CURRENT_PAGE_KEY,1);
        }
        //获取每页显示的数量，pq_rpp为前端grid组件传过来的参数
        Integer rpp=paraMap.get("pq_rpp")!=null?Integer.parseInt(paraMap.get("pq_rpp")[0]):null;
        if(rpp!=null){
            params.put(Constant.PAGE_SIZE_KEY,rpp);
        }else{
            params.put(Constant.PAGE_SIZE_KEY,5);
        }
        com.dbkj.meet.model.Record record= com.dbkj.meet.model.Record.dao.findById(id);
        params.put(Bill.dao.UID,record.getBelong());
        params.put(Bill.dao.CID,User.dao.findById(record.getBelong()).getCid());
        Page<Bill> list=Bill.dao.getPage(params);
        List<BillDetailVo> detailList=new ArrayList<>();
        for(Bill bill:list.getList()){
            detailList.add(BillDetailConverter.to(bill));
        }

        return new BillVo<BillDetailVo>(curpage,list.getTotalRow(),detailList);
    }

    @Override
    public File exportBillExcel(Map<String, String[]> paraMap) {
        Map<String,Object> params=getParams(paraMap);
        List<Bill> billList=Bill.dao.getBillList(params);
        String[] heads={"开始时间","会议主题","消费总额（元）","通话时长（分钟）","短信费（元）","短信（条）","会议安排者","人数"};
        String[] columns={"startTime","subject","fee","call_time","msg_count","msg_fee","name","count"};
        //导出前先清除垃圾
        String directory="bill/";
        String path= PathKit.getWebRootPath()+"/download/"+directory;
        FileUtil.deleteFiles(path);

        String fileName = ExcelUtil.writeExcel(billList,directory,heads,columns);
        return new File(path+fileName);
    }
}
