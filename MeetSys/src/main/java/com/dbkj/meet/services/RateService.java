package com.dbkj.meet.services;

import com.dbkj.meet.dic.CallTypeEnum;
import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.model.Fee;
import com.dbkj.meet.services.inter.IRateService;
import com.dbkj.meet.services.inter.PackageService;
import com.dbkj.meet.vo.PackageListItemVo;
import com.dbkj.meet.vo.RateVo;
import com.jfinal.kit.StrKit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 2017/02/28.
 */
public class RateService implements IRateService {

    private final PackageService packageService=new PackageServiceImpl();

    @Override
    public List<PackageListItemVo> getRateInfo() {
        List<PackageListItemVo> list = packageService.list();
        return list;
    }

    private RateVo findByMode(List<RateVo> list,Integer mode){
        if(list==null){
            throw new IllegalArgumentException("参数list不能为null");
        }
        CallTypeEnum callTypeEnumEnum = CallTypeEnum.codeOf(mode);
        for(RateVo rateVo:list){
            if(callTypeEnumEnum.getDesc().equals(rateVo.getType())){
                return rateVo;
            }
        }
        return null;
    }

    private Fee findById(List<Fee> list,Fee fee){
        for(Fee item:list){
            if((fee.getParentId()!=null&&fee.getParentId().intValue()==item.getId().longValue())||
                    (item.getParentId()!=null&&item.getParentId().intValue()==fee.getId().longValue())){
                return item;
            }
        }
        return null;
    }

    private boolean isHandled(List<RateVo> list,Fee fee){
        RateVo rv=null;
        for(RateVo rateVo:list){
            if(rateVo.getType().equals(CallTypeEnum.codeOf(fee.getType()).getDesc())){
                rv=rateVo;
                break;
            }
        }
        if(rv==null){
            return false;
        }
        for(RateVo.Rate rate:rv.getList()){
            if((fee.getParentId()!=null&&fee.getParentId().intValue()==rate.getId().longValue())||
                    (rate.getParentId()!=null&&rate.getParentId().intValue()==fee.getId().longValue())){
                return true;
            }
        }
        return false;
    }
}
