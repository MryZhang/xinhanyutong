package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.vo.ChangePackageVo;
import com.dbkj.meet.vo.ChargeVo;
import com.dbkj.meet.vo.ChargesDetailVo;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by DELL on 2017/02/25.
 */
public interface IChargeService {

    /**
     * 获取账户余额分页数据
     * @param request
     * @return
     */
    Page<Record> getPageOfBalance(HttpServletRequest request);

    /**
     * 根据账户余额id获取账户余额的相关信息
     * @param id
     * @return
     */
    ChargesDetailVo getAccountBalanceInfoById(Long id);

    /**
     * 修改计费模式
     * @param mode 计费模式
     * @param count 如果计费模式为包月，则为包月方数
     * @param bid 账户余额id
     * @return
     */
    Result changeChargingMode(Integer mode,Integer count,Long bid);

    /**
     * 获取账户余额和公司名称
     * @param id
     * @return
     */
    Record getAccountBalanceAndCompanyName(Long id);

    /**
     * 充值
     * @param chargeVo
     */
    void charge(ChargeVo chargeVo,Long id);

    /**
     * 获取充值记录分页数据
     * @param paraMap
     * @return
     */
    Map<String,Object> getChargeRecordPage(Map<String,String[]> paraMap);

    /**
     * 获取修改套餐的相关数据
     * @param id 账户余额id
     * @return
     */
    Map<String,Object> getChangePackageData(Long id);

    boolean changePackage(ChangePackageVo changePackageVo);

    /**
     * 根据账户余额id获取账户账户余额信息及费率信息
     * @param id
     * @return
     */
    Record getAccountInfoById(Long id);
}
