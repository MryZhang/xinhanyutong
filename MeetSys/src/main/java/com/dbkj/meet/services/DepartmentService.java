package com.dbkj.meet.services;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.NameCacheInterceptor;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.IDepartmentService;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;

/**
 * Created by MrQin on 2016/11/14.
 */
public class DepartmentService implements IDepartmentService {

    public Result<Department> addDeparment(Department department,Integer cid) {
        department.setCid(cid);
        Result<Department> result=validate(department);
        if (result.getResult()) {
            boolean flag=department.save();
            if(flag){
                result.setData(department);
            }else {
                result.setResult(false);
                result.setMsg("添加失败");
            }
        }
        return result;
    }

    private Result<Department> validate(Department department){
        if(department!=null){
            if(StrKit.isBlank(department.getName())){
                return new Result<Department>(false,"部门名称不能为空");
            }
            if(department.getId()!=null){//更新部门
                if(isExistDepart(department.getName(),department.getCid(),department.getId())){
                    return new Result<Department>(false,"部门已存在");
                }
            }else{//添加部门
                if(isExistDepart(department.getName(),department.getCid())){
                    return new Result<Department>(false,"部门已存在");
                }
            }

            return new Result<Department>(true);
        }
        return new Result<Department>(false,"缺少必要信息");
    }


    /**
     *添加时判断部门是否已存在
     * @param name
     * @param cid
     * @return
     */
    public boolean isExistDepart(String name, long cid) {
        return Department.dao.findByName(name,cid)!=null;
    }

    /**
     * 修改时判断部门是否已存在
     * @param name
     * @param cid 所属公司id
     * @param did 要修改的部门id
     * @return
     */
    public boolean isExistDepart(String name, long cid, long did) {
        Department department=Department.dao.findByName(name,cid);
        if(department!=null){
            return department.getId()!=did;
        }
        return false;
    }

    public Result<Department> update(Department department,Integer cid){
        department.setCid(cid);
        Result<Department> result=validate(department);
        if (result.getResult()) {
            boolean flag=department.update();
            if(flag){
                result.setData(department);
            }else {
                result.setResult(false);
                result.setMsg("添加失败");
            }
        }
        return result;
    }

    /**
     * 删除部门及相关信息
     * @param did
     * @return
     */
    @Before(NameCacheInterceptor.class)
    public boolean deleteDepart(final long did, final Integer cid) {
        return Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                //注意删除操作的先后顺序，由于数据的关联性，
                // 有些表中的数据必须在另一张表之前删除，否则将导致漏删
                //删除部门中所有用户的个人联系人信息
                PrivatePhone.dao.deleteByDepartmentId(did);
                PrivateContacts.dao.deleteByDepartmentId(did);
                //删除部门中的账号及员工信息
                User.dao.deleteByDepartmentId(did);
                Employee.dao.deleteByDepartmentId(did);
                //删除部门下的公共联系人信息
                PublicPhone.dao.deleteByDepartmentId(did);
                //由于一个联系人信息可能关联多个电话，电话信息删除
                // 之后无法确定该联系人是否还关联有其他号码，
                // 所以统一对公司下所有的未关联的联系人信息进行删除
                PublicContacts.dao.deleteNotExistByCompanyId(cid);
                return Department.dao.deleteById(did);
            }
        });
    }
}
