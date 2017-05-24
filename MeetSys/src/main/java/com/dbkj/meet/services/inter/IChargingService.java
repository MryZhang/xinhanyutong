package com.dbkj.meet.services.inter;

import java.io.File;
import java.util.Map;

/**
 * Created by DELL on 2017/03/14.
 */
public interface IChargingService {

    /**
     * 获取扣费记录分页数据
     * @param paraMap
     * @return
     */
    Map<String,Object> getChargingRecordPage(Map<String,String[]> paraMap);

    /**
     * 导出excel
     * @param paraMap
     * @return
     */
    File export(Map<String,String[]> paraMap);
}
