package com.dbkj.meet.services.inter;

import com.dbkj.meet.model.User;
import com.dbkj.meet.vo.BillVo;
import com.jfinal.core.Controller;

import java.io.File;
import java.util.Map;

/**
 * Created by DELL on 2017/03/02.
 */
public interface IAccountService {

    /**
     * 获取消费总额
     * @param controller
     * @return
     */
    double getTotalConsume(Controller controller);

    /**
     * 获取话单的分页数据
     * @param paraMap
     * @return
     */
    BillVo getBillPage(Map<String,String[]> paraMap,User uid);

    /**
     * 根据
     * @param paraMap
     * @return
     */
    BillVo getBillDetail(long id,Map<String,String[]> paraMap);

    /**
     * 导出账单记录excel
     * @param paraMap
     * @return
     */
    File exportBillExcel(Map<String,String[]> paraMap);
}
