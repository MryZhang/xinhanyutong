package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.RateModeEnum;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.ICompanyService;
import com.dbkj.meet.utils.ScheduleHelper;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.CompanyVo;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by MrQin on 2016/11/8.
 */
public class CompanyService implements ICompanyService {

    @Deprecated
    public Result validateCompany(Company company)  {
        Result result=null;
        if(StrKit.isBlank(company.getName())){
            result=new Result(false,"公司名称不能为空");
            return result;
        }
        if(isExistCompany(company)){//判断公司名称是否重复
            result=new Result(false,"该公司已存在");
            return result;
        }
//        if(StrKit.isBlank(company.getCode())|| !ValidateUtil.validatePhone(company.getCode())){
//            result=new Result(false,"呼入号码格式不正确");
//            return result;
//        }
//        if(StrKit.isBlank(company.getShowNum())||!ValidateUtil.validatePhone(company.getShowNum())){
//            result=new Result(false,"呼出号码格式不正确");
//            return result;
//        }

        return new Result(true);
    }

    //判断公司名称是否存在
    @Deprecated
    @Override
    public boolean isExistCompany(Company company){
        Company company1=Company.dao.getCompanyByName(company.getName());
        if(company.getId()!=null){
            return company1!=null&&company1.getId()!=company.getId();
        }
        return company1!=null;
    }

    @Override
    public boolean isExistCompany(String name) {
        return Company.dao.getCompanyByName(name)!=null;
    }

    @Override
    public boolean isExistCompany(long id, String name) {
        Company company=Company.dao.findById(id);
        Company company1=Company.dao.getCompanyByName(name);
        if(company!=null){
            return company1!=null&&company.getId()!=company1.getId();
        }
        return company1!=null;
    }

    @Override
    public List<AccessNum> getNumList() {
        return AccessNum.dao.list();
    }

    public Result<Company> addCompany(final Company company) {
        Result<Company> result=validateCompany(company);
        if(result.getResult()){
            final SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
            final Date now=new Date();
            boolean flag=Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    if(company.save()){
                        AccountBalance accountBalance=new AccountBalance();
                        accountBalance.setCid(company.getId());
                        PropKit.use("config.properties");
                        accountBalance.setBalance(new BigDecimal(PropKit.getInt(Constant.DEFAULT_AMOUNT,10)));
                        accountBalance.setGmtCreate(new Date());
                        accountBalance.setCreditLines(new BigDecimal(0));
                        accountBalance.save();

                        ChargingMode chargingMode=new ChargingMode();
                        chargingMode.setBid(accountBalance.getId());
                        chargingMode.setGmtCreate(now);
                        chargingMode.setName(Long.parseLong(simpleDateFormat.format(now)));
//                        chargingMode.setMode(RateModeEnum.MINUTE.getCode());
                        return chargingMode.save();
                    }
                    return false;
                }
            });
            if(flag){
                result.setData(company);
            }else{
                result.setResult(false);
                result.setMsg("更新失败");
            }
        }
        return result;
    }

    @Override
    public boolean addCompany(CompanyVo companyVo) {
        final Company company=new Company();
        company.setName(companyVo.getName());
        company.setShowNum(companyVo.getShowNum());
        company.setCallNum(companyVo.getCallNum());

        final AccountBalance accountBalance=new AccountBalance();
        accountBalance.setBalance(new BigDecimal(PropKit.getInt(Constant.DEFAULT_AMOUNT,10)));
        accountBalance.setGmtCreate(new Date());
        accountBalance.setCreditLines(new BigDecimal(0));

        Date now=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMM");
        final ChargingMode chargingMode=new ChargingMode();
        chargingMode.setPid(companyVo.getPid());
        chargingMode.setGmtCreate(now);
        chargingMode.setName(Long.parseLong(simpleDateFormat.format(now)));
        chargingMode.setRemark("新增");
        Integer count=companyVo.getCount();
        if(count!=null){
            chargingMode.setCount(Long.parseLong(companyVo.getCount().toString()));
        }

        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                if(company.save()){
                    accountBalance.setCid(company.getId());
                    accountBalance.save();
                    chargingMode.setBid(accountBalance.getId());
                    return chargingMode.save();
                }
                return false;
            }
        });
    }

    public Company getCompany(Long id) {
        return Company.dao.findById(id);
    }

    public Result<Company> updateCompany(Company company) {
        Result<Company> result=validateCompany(company);
        if(result.getResult()){
            if(company.update()){
                result.setData(company);
            }else{
                result.setResult(false);
                result.setMsg("添加失败");
            }
        }
        return result;
    }

    /**
     * 删除公司以及相关数据
     * @param id
     * @return
     */
    public boolean deleteCompany(final Long id) {
        return Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                FixedMeet.dao.deleteByCompanyId(id);//删除固定会议
                //删除预约会议
                deleteSchedule(id);
                OrderAttendee.dao.deleteByCompanyId(id);
                OrderMeet.dao.deleteByCompanyId(id);
                //删除公共联系人
                PublicPhone.dao.deleteByCompanyId(id);
                PublicContacts.dao.deleteByCompanyId(id);
                //删除个人联系人
                PrivatePhone.dao.deleteByCompanyId(id);
                PrivateContacts.dao.deleteByCompanyId(id);
                Group.dao.deleteByCompanyId(id);
                //删除员工信息
                Employee.dao.deleteByCompanyId(id);
                User.dao.deleteByCompanyId(id);//删除账号
                Department.dao.deleteByCompanyId(id);//删除部门
                return Company.dao.deleteById(id);
            }
        });
    }

    /**
     * 根据公司id删除并取消该公司所有的预约会议
     * @param id
     */
    private int deleteSchedule(Long id){
        List<Schedule> list=Schedule.dao.findByCompanyId(id);
        //将定时器中的任务删除
        for (Schedule schedule:list) {
            ScheduleHelper.removeJob(schedule.getJobName(),schedule.getJobGroup());
        }
        return Schedule.dao.deleteByCompanyId(id);
    }
}
