package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.vo.AmountVo;

/**
 * Created by DELL on 2017/03/01.
 */
public interface IAmountService {

    /**
     * 根据公司id获取公司账号的余额信息
     * @param cid
     * @return
     */
    AmountVo getAmount(long cid);

    Result<AmountVo> getAmountInfo(long cid);
}
