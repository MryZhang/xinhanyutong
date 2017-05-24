package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Config;

/**
 * Created by DELL on 2017/03/01.
 */
public interface IThresoldService {

    /**
     * 获取余额阙值
     * @return
     */
    Config getThresold();

    /**
     * 更新余额阙值
     * @param value
     * @return
     */
    Result updateThresold(String value);
}
