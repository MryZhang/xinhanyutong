package com.dbkj.meet.services;

import com.dbkj.meet.dic.CallTypeEnum;
import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Fee;
import com.dbkj.meet.model.Package;
import com.dbkj.meet.services.inter.PackageService;
import com.dbkj.meet.vo.PackageListItemVo;
import com.dbkj.meet.vo.PackageVo;
import com.dbkj.meet.vo.RateModeVo;
import com.dbkj.meet.vo.RateVo;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.lang.annotation.Inherited;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by DELL on 2017/04/01.
 */
public class PackageServiceImpl implements PackageService {

    @Override
    public List<PackageListItemVo> list() {
        List<Package> list = Package.dao.list();
        List<PackageListItemVo> plist=new ArrayList<>(list.size());
        for(Package p:list){
            PackageListItemVo packageVo=new PackageListItemVo();
            packageVo.setId(p.getId());
            packageVo.setName(p.getName());
            int callInMode=p.getInt(Package.dao.CALLIN_MODE);
            packageVo.setCallInMode(RateModeEnum.codeOf(callInMode).getDesc());
            //呼入费率
            String unit=null;
            if(callInMode==RateModeEnum.MINUTE.getCode()){
                unit="元/分钟";
                packageVo.setCallInRate(p.getBigDecimal(Package.dao.CALLIN_RATE).doubleValue()+unit);
            }else{
                unit="元/方";
                StringBuilder str=new StringBuilder();
                str.append("包月：");
                str.append(p.getBigDecimal(Package.dao.CALLIN_RATE).doubleValue());
                str.append(unit);
                str.append("，超方：");
                str.append(p.getBigDecimal(Package.dao.PASS_RATE).doubleValue());
                str.append(unit);
                packageVo.setCallInRate(str.toString());
            }
            //呼出费率
            unit="元/分钟";
            packageVo.setCallOutMode(RateModeEnum.MINUTE.getDesc());
            packageVo.setCallOutRate(p.getBigDecimal(Package.dao.CALLOUT_RATE).doubleValue()+unit);
            //短信费率
            unit="元/每条";
            packageVo.setSmsRate(p.getBigDecimal(Package.dao.SMS_RATE).doubleValue()+unit);
            plist.add(packageVo);
        }
        return plist;
    }

    @Override
    public Map<String, Object> showAdd() {
        Map<String,Object> attrs=new HashMap<>();
        List<RateModeVo> list=new ArrayList<>(2);
        list.add(new RateModeVo(RateModeEnum.MINUTE.getCode(),RateModeEnum.MINUTE.getDesc()));
        list.add(new RateModeVo(RateModeEnum.MONTH.getCode(),RateModeEnum.MONTH.getDesc()));
        attrs.put("modes",list);
        return attrs;
    }

    @Override
    public Result exist(String name) {
        boolean result=Package.dao.findByName(name)!=null;
        return new Result(result);
    }

    @Override
    public boolean add(final PackageVo packageVo) {
        final Date now=new Date();
        //呼入计费
        final Fee callIn=new Fee();
        callIn.setMode(packageVo.getCallInMode());
        callIn.setRate(packageVo.getCallInRate());
        callIn.setType(CallTypeEnum.CALL_TYPE_CALLIN.getCode());
        callIn.setGmtCreate(now);
        final Fee passFee=new Fee();
        if(packageVo.getCallInMode()==RateModeEnum.MONTH.getCode()){
            callIn.setRemark("包月");
            passFee.setMode(packageVo.getCallInMode());
            passFee.setRate(packageVo.getPassRate());
            passFee.setType(CallTypeEnum.CALL_TYPE_CALLIN.getCode());
            passFee.setGmtCreate(now);
            passFee.setRemark("超方");
        }
        //呼出计费
        final Fee callOut=new Fee();
        callOut.setMode(RateModeEnum.MINUTE.getCode());
        callOut.setType(CallTypeEnum.CALL_TYPE_CALLOUT.getCode());
        callOut.setRate(packageVo.getCallOutRate());
        callOut.setGmtCreate(now);
        //短信费率
        final Fee smsFee=new Fee();
        smsFee.setType(CallTypeEnum.CALL_TYPE_MESSAGE.getCode());
        smsFee.setRate(packageVo.getSmsRate());
        smsFee.setGmtCreate(now);
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean flag=callIn.save();
                if(flag){
                    if(callIn.getMode()==RateModeEnum.MONTH.getCode()){
                        passFee.setParentId(Integer.parseInt(callIn.getId().toString()));
                        flag=passFee.save();
                    }
                    callOut.save();
                    smsFee.save();
                    Package pk=new Package();
                    pk.setName(packageVo.getName());
                    pk.setCallin(callIn.getId());
                    pk.setCallout(callOut.getId());
                    pk.setSms(smsFee.getId());
                    pk.setGmtCreate(now);
                    return pk.save();
                }
                return false;
            }
        });
    }
}
