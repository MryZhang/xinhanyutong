package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Department;

import java.util.List;

/**
 * Created by MrQin on 2016/11/14.
 */
public interface IDepartmentService {

    Result<Department> addDeparment(Department department,Integer cid);


    /**
     * 判断部门是否已存在
     * @param name
     * @param cid
     * @return
     */
    boolean isExistDepart(String name,long cid);

    /**
     * 修改时判断修改的部门名称是否已存在
     * @param name
     * @param cid
     * @param did
     * @return
     */
    boolean isExistDepart(String name,long cid,long did);

    /**
     * 修改部门信息
     * @param department
     * @param cid
     * @return
     */
    Result<Department> update(Department department,Integer cid);

    /**
     * 删除部门及相关信息
     * @param did
     * @return
     */
    boolean deleteDepart(long did,Integer cid);
}
