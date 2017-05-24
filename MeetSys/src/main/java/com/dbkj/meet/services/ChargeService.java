package com.dbkj.meet.services;

import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.AccountBalance;
import com.dbkj.meet.model.ChargeRecord;
import com.dbkj.meet.model.ChargingMode;
import com.dbkj.meet.model.Package;
import com.dbkj.meet.services.inter.IChargeService;
import com.dbkj.meet.utils.DateUtil;
import com.dbkj.meet.utils.ParameterUtil;
import com.dbkj.meet.vo.ChangePackageVo;
import com.dbkj.meet.vo.ChargeVo;
import com.dbkj.meet.vo.ChargesDetailVo;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.IDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DELL on 2017/02/25.
 */
public class ChargeService implements IChargeService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int PAGE_SIZE=15;

    /**
     * 获取账户余额分页数据
     * @param request
     * @return
     */
    @Override
    public Page<Record> getPageOfBalance(HttpServletRequest request) {
        Map<String,String[]> map=request.getParameterMap();
        Map<String,Object> paramMap= ParameterUtil.getParaMap(map);
        paramMap.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);
        Page<Record> pages= AccountBalance.dao.getPage(paramMap);
        try {
            for(Map.Entry<String,Object> entry:paramMap.entrySet()){
                    request.setAttribute(entry.getKey(), URLDecoder.decode(entry.getValue().toString(),"UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
        }
        return pages;
    }

    /**
     * 根据账户余额id获取账户计费相关信息
     * @param id
     * @return
     */
    @Override
    public ChargesDetailVo getAccountBalanceInfoById(final Long id) {
        final ChargesDetailVo chargesDetailVo=new ChargesDetailVo();
        if(id!=null){
            Record record=getAccountInfoById(id);
            chargesDetailVo.setId(record.getLong(AccountBalance.dao.BID));
            chargesDetailVo.setName(record.getStr(AccountBalance.dao.NAME));
            chargesDetailVo.setBalance(record.getBigDecimal(AccountBalance.dao.BALANCE));
            chargesDetailVo.setPname(record.getStr(AccountBalance.dao.PNAME));
            chargesDetailVo.setCount(record.getLong(AccountBalance.dao.COUNT));

            //获取套餐资费说明
            StringBuilder desc=new StringBuilder();
            desc.append("呼入：计费模式：");
            int mode=record.getInt(AccountBalance.dao.CALLIN_MODE);
            chargesDetailVo.setCallInMode(mode);
            String unit="元/分钟";
            if(mode==RateModeEnum.MONTH.getCode()){
                unit="元/方";
            }
            desc.append(RateModeEnum.codeOf(mode).getDesc());
            desc.append(",");
            desc.append("费率：");
            desc.append(record.getBigDecimal(AccountBalance.dao.CALLIN_RATE).doubleValue());
            desc.append(unit);
            if(mode==RateModeEnum.MONTH.getCode()){
                desc.append("超方：");
                desc.append(record.getBigDecimal(AccountBalance.dao.PASS_RATE).doubleValue());
                desc.append(unit);
            }
            desc.append("；");
            unit="元/分钟";
            desc.append("呼出费率：");
            desc.append(record.getBigDecimal(AccountBalance.dao.CALLOUT_RATE).doubleValue());
            desc.append(unit+"；");
            unit="元/条";
            desc.append("短信费率：");
            desc.append(record.getBigDecimal(AccountBalance.dao.SMS_RATE).doubleValue());
            desc.append(unit+"。");
            chargesDetailVo.setDescription(desc.toString());
            return chargesDetailVo;

        }
        return chargesDetailVo;
    }

    @Override
    public Record getAccountInfoById(final Long id){
        return CacheKit.get(Constant.CACHE_NAME_ADMDIV, AccountBalance.class.getSimpleName() + "_" + id, new IDataLoader() {
            @Override
            public Object load() {
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
                Date now=new Date();
                String modeName=simpleDateFormat.format(now);
                Record record = AccountBalance.dao.getAccountBalanceInfoById(id,modeName);
                //如果没有查到当前月份的计费信息，则获取该账号最近一次计费模式信息并新增
                if(record==null){
                    ChargingMode chargingMode=ChargingMode.dao.getLastByAccountBalanceId(id);
                    if(chargingMode!=null){
                        chargingMode.setName(Long.parseLong(simpleDateFormat.format(now)));
                        chargingMode.setGmtCreate(now);
                        chargingMode.setRemark("复制");
                        ChargingMode.dao.insert(chargingMode);
                    }
                    record=AccountBalance.dao.getAccountBalanceInfoById(id,simpleDateFormat.format(now));
                }
                logger.info("查询参数：id:{},name:{},结果:{}",id,modeName,record);
                return record;
            }
        });

    }


    @Override
    public Result changeChargingMode(Integer mode, Integer count,Long bid) {
        if(mode==null||(mode!= RateModeEnum.MINUTE.getCode()&&mode!= RateModeEnum.MONTH.getCode())){
            return new Result(false,"参数格式有误");
        }
        if(mode== RateModeEnum.MONTH.getCode()&&count==null||count<=0){
            return new Result(false,"参数格式有误");
        }
        if(bid==null){
            return new Result(false,"参数格式有误");
        }
        /*
        因为当前修改需要需要在下个月生效，所以先获取下个月的日期
        然后计费模式表中是否有下个月的的数据，有则修改该数据，没有则添加
         */
        int name=0;
        Date now=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
        Date nextMonth=DateUtil.addByMonths(now,1);
        try {
            name = Integer.parseInt(simpleDateFormat.format(nextMonth));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        ChargingMode chargingMode = ChargingMode.dao.findByNameAndAccountBalanceId(bid,name);
        if(chargingMode==null){
            chargingMode=new ChargingMode();
            chargingMode.setBid(bid);
            chargingMode.setName(Long.parseLong(name+""));
            chargingMode.setRemark("新增");
//            chargingMode.setMode(mode);
            chargingMode.setCount(count!=null?Long.parseLong(count+""):null);
            chargingMode.setGmtCreate(now);
            chargingMode.setHandled(Integer.parseInt(Constant.NO));
            ChargingMode.dao.insert(chargingMode);
        }else{
//            chargingMode.setMode(mode);
            chargingMode.setCount(count!=null?Long.parseLong(count+""):null);
            chargingMode.setGmtModified(now);
            chargingMode.update();
        }
        return new Result(true);
    }

    @Override
    public Record getAccountBalanceAndCompanyName(Long id) {
        if(id!=null){
            Record record=AccountBalance.dao.getAccountBalanceWithCompanyNameById(id);
            return record;
        }
        return new Record();
    }

    @Override
    public void charge(final ChargeVo chargeVo, final Long id) {
        final Date date=new Date();

        boolean result = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                AccountBalance accountBalance=AccountBalance.dao.findById(chargeVo.getId());
                accountBalance.setBalance(accountBalance.getBalance().add(chargeVo.getActualCharge()));
                accountBalance.setGmtModified(date);

                ChargeRecord chargeRecord=new ChargeRecord();
                chargeRecord.setCid(accountBalance.getCid());
                chargeRecord.setAmount(chargeVo.getChargeAmount());
                chargeRecord.setCharge(chargeVo.getActualCharge());
                chargeRecord.setCUid(id);
                chargeRecord.setGmtCreate(date);

                if(accountBalance.update()){
                    return chargeRecord.save();
                }
                return false;
            }
        });
        //如果充值成功从缓存中移除该公司的账户余额信息
        if(result){
            CacheKit.remove(Constant.CACHE_NAME_ADMDIV, AccountBalance.class.getSimpleName() + "_" + chargeVo.getId());
        }
    }

    /**
     * 获取充值分页记录
     * @param paraMap
     * @return
     */
    @Override
    public Map<String, Object> getChargeRecordPage(Map<String, String[]> paraMap) {
        Map<String,Object> map=new HashMap<String,Object>();
        //获取用户分页数据
        //查询条件
        Map<String,Object> params=new HashMap<String, Object>();
        params.put(Constant.CURRENT_PAGE_KEY,1);
        params.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);

        String[] indexPara=paraMap.get("pageIndex");
        if(indexPara!=null){//当前页码
            params.put(Constant.CURRENT_PAGE_KEY,indexPara[0]);
        }

        String[] searchStr=paraMap.get("search");
        if(searchStr!=null){
            String search=searchStr[0];
            params.put("b.name",search);
            map.put("search",search);
        }

        String[] beginPara=paraMap.get("beginTime");
        if(beginPara!=null){
            String beginTime=beginPara[0];
            if(!StrKit.isBlank(beginTime)){
                params.put(ChargeRecord.BEGIN_TIME,beginTime+" 00:00:00");
                map.put("beginTime",beginTime);
            }

        }

        String[] endPara=paraMap.get("endTime");
        if(endPara!=null){
            String endTime=endPara[0];
            if(!StrKit.isBlank(endTime)){
                params.put(ChargeRecord.END_TIME,endTime+" 23:59:59");
                map.put("endTime",endTime);
            }
        }

        Page<Record> recordPage=ChargeRecord.dao.getChargeRecordInfoPage(params);
        map.put("pages", recordPage);
        return map;
    }

    @Override
    public Map<String,Object> getChangePackageData(Long id) {
        Map<String,Object> map=new HashMap<>();
        Record record=getAccountInfoById(id);
        ChangePackageVo changePackageVo=new ChangePackageVo();
        changePackageVo.setId(id);
        changePackageVo.setName(record.getStr(AccountBalance.dao.NAME));
        changePackageVo.setPname(record.getStr(AccountBalance.dao.PNAME));
        changePackageVo.setPid(record.getLong(AccountBalance.dao.PID));
        changePackageVo.setCount(record.getLong(AccountBalance.dao.COUNT));
        map.put("cv",changePackageVo);
        int mode=record.getInt(AccountBalance.dao.CALLIN_MODE);
        map.put("showCount",mode==RateModeEnum.MONTH.getCode());
        return map;
    }


    @Override
    public boolean changePackage(final ChangePackageVo changePackageVo) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
        final Date date=new Date();
        final int name=Integer.parseInt(simpleDateFormat.format(DateUtil.addByMonths(date,1)));

        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                ChargingMode chargingMode = ChargingMode.dao.findByNameAndAccountBalanceId(changePackageVo.getId(),name);
                if(chargingMode==null){
                    Package pk=Package.dao.findById(changePackageVo.getPid());
                    chargingMode = new ChargingMode();
                    chargingMode.setBid(changePackageVo.getId());
                    chargingMode.setPid(changePackageVo.getPid());
                    chargingMode.setName(Long.parseLong(name+""));
                    chargingMode.setPid(changePackageVo.getPid());
                    chargingMode.setCount(changePackageVo.getCount());
                    chargingMode.setRemark("新增");
                    chargingMode.setGmtCreate(date);
                    return ChargingMode.dao.insert(chargingMode)>0;
                }else{
                    chargingMode.setPid(changePackageVo.getPid());
                    chargingMode.setCount(changePackageVo.getCount());
                    chargingMode.setGmtModified(date);
                    return chargingMode.update();
                }
            }
        });
    }
}
