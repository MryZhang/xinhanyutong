package com.dbkj.meet.services.inter;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by DELL on 2017/03/09.
 */
public interface ChargingService {

    /**
     * 计费
     * @param request
     */
    void charge(HttpServletRequest request);

    void charge(String result);

    /**
     * 扣除包月费用
     */
    void chargeMonthly();
}
