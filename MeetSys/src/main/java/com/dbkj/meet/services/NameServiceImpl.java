package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.model.Employee;
import com.dbkj.meet.model.PrivateContacts;
import com.dbkj.meet.model.PublicContacts;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.INameService;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.IDataLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2017/06/02.
 */
public class NameServiceImpl implements INameService {

    @Override
    public Map<String, String> getAllName(final Long uid) {
        return CacheKit.get(Constant.CACHE_NAME_PERMANENT, NameServiceImpl.NAME_OF_CACHE, new IDataLoader() {
            @Override
            public Object load() {
                User user=User.dao.findById(uid);
                Map<String,String> nameMap=new HashMap<>();
                //先从私有联系人中获取
                List<Record> privateContacts= PrivateContacts.dao.getContactsByUserId(uid);
                String phone="phone";
                String name="name";
                if(privateContacts!=null){
                    for(Record record:privateContacts){
                        nameMap.put(record.getStr(phone),record.getStr(name));
                    }
                }

                //再从公司联系人中获取
                Map<String,Object> paraMap=new HashMap<>(1);
                paraMap.put("a.cid",user.getCid());
                List<Record> publicContacts= PublicContacts.dao.getContacts(paraMap);
                if(publicContacts!=null){
                    for(Record record:publicContacts){
                        nameMap.put(record.getStr(phone),record.getStr(name));
                    }
                }

                //再从公司员工信息中获取
                List<Employee> employeeList=Employee.dao.findByCompanyId(user.getCid());
                if(employeeList!=null){
                    for(Employee employee:employeeList){
                        nameMap.put(employee.getPhone(),employee.getName());
                    }
                }
                return nameMap;
            }
        });
    }

    @Override
    public String getNameByPhone(Long uid,String phone) {
        String name = getAllName(uid).get(phone);
        return name!=null?name:phone;
    }
}
