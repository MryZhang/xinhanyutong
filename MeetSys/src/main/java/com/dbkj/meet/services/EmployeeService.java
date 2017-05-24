package com.dbkj.meet.services;

import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.IEmployeeService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by MrQin on 2016/11/14.
 */
public class EmployeeService implements IEmployeeService {

    public List<Department> getDepartments(long cid) {
        return Department.dao.findByCompanyId(cid);
    }

    /**
     * 添加员工信息，并将号码添加到公共联系人
     * @param employee
     * @param user
     * @return
     */
    public boolean addEmployee(final Employee employee, final User user) {
        if(employee!=null){
            //添加员工信息并将信息添加到公共联系人中
            final PublicContacts publicContacts=new PublicContacts();
            publicContacts.setName(employee.getName());
            publicContacts.setCid(user.getCid());
            publicContacts.setPosition(employee.getPosition());

            final PublicPhone publicPhone=new PublicPhone();
            publicPhone.setPhone(employee.getPhone());
            publicPhone.setDid(employee.getDid());

            return Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    boolean result=publicContacts.save();
                    if(result){
                        publicPhone.setPid(Integer.parseInt(publicContacts.getId().toString()));
                        result=publicPhone.save();
                        if(result){
                            result = employee.save();
                            if(result){
                                user.setEid(Integer.parseInt(employee.getId().toString()));
                                return user.update();
                            }
                        }
                    }
                    return false;
                }
            });
        }
        return false;
    }
}
