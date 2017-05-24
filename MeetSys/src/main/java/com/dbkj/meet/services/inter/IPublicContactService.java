package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.PubContact;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Department;
import com.jfinal.core.Controller;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/14.
 */
public interface IPublicContactService {


    Map<String,Object> getContactPageData(Controller controller);

    List<Department> getDepartments(long cid);

    /**
     * 修改联系人姓名时验证要修改的姓名在该联系人当前所有所在的公司是否已存在
     * @param name 要修改的联系人姓名
     * @param id 当前修改的联系人id
     * @param cid 改联系人所在的公司id
     * @return
     */
    boolean isExistName(String name,long id,long cid);

    /**
     * 判断号码在公司中是否存在
     * @param phone
     * @param cid
     * @return
     */
    boolean isExistPhone(String phone,long cid);

    /**
     * 修改联系人号码时，判断要修改的号码在公司是否已存在
     * @param phone
     * @param id
     * @param cid
     * @return
     */
    boolean isExistPhone(String phone,long id,long cid);

    /**
     * 添加公共联系人
     * @param pubContact
     * @return
     */
    boolean addContact(PubContact pubContact,Integer cid);

    /**
     * 获取公共联系人
     * @param id 联系人号码id
     * @return
     */
    PubContact getPubContact(Long id);

    /**
     * 修改联系人信息
     * @param pubContact
     * @return
     */
    boolean updateContactData(PubContact pubContact);

    /**
     * 导出公共联系人
     * @param search 搜索条件
     * @param did 部门id
     * @return
     */
    File exportContacts(String search,Integer did,long cid);

    boolean deleteContacts(String idStr,long cid);

    Result<Map<String,Object>> importContacts(File file, Integer cid);

    List<BaseNode> getContacts(long cid);
}
