package com.dbkj.meet.services.inter;

import com.dbkj.meet.model.Department;
import com.dbkj.meet.model.Employee;
import com.dbkj.meet.model.User;

import java.util.List;

/**
 * Created by MrQin on 2016/11/14.
 */
public interface IEmployeeService {

    /**
     * 根据公司id获取所有的部门信息
     * @param cid
     * @return
     */
    List<Department> getDepartments(long cid);

    boolean addEmployee(Employee employee,User user);
}
