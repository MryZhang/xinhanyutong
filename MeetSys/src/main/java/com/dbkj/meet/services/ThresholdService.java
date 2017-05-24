package com.dbkj.meet.services;

import com.dbkj.meet.dic.ConfigTypeEnum;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Config;
import com.dbkj.meet.services.inter.IThresoldService;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.kit.StrKit;

import java.util.Date;

/**
 * Created by DELL on 2017/03/01.
 */
public class ThresholdService implements IThresoldService{
    @Override
    public Config getThresold() {
        return Config.dao.findByType(ConfigTypeEnum.BALANCE_THRESHOLD.getCode());
    }

    @Override
    public Result updateThresold(String value) {
        if(StrKit.isBlank(value)|| !ValidateUtil.validateNumber(value)){
            return new Result(false,"格式不正确");
        }
        Config config=getThresold();
        config.setValue(value);
        config.setGmtModified(new Date());
        return new Result(config.update());
    }
}
