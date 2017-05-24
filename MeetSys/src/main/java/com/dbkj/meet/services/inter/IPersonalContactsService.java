package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.ContactInfo;
import com.dbkj.meet.dto.Node;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Group;
import com.jfinal.plugin.activerecord.Record;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/18.
 */
public interface IPersonalContactsService {

    Map<String,Object> getRenderData(long uid);

    Result<Group> addGroup(Group group);

    Map<String,Object> getContactsPage(Map<String,String[]> map,long uid);

    Result<Group> updateGroup(Group group);

    boolean deleteGroup(Long gid);

    List<Group> getGroupsByUserId(long uid);

    boolean addContact(ContactInfo contactInfo);

    boolean isExistContactByPhone(String phone, long uid);

    boolean isExistContactByPhone(String phone,long id,long uid);

    boolean isExistContactByName(String name,long pid,long uid);

    boolean deleteContacts(String ids);

    ContactInfo getContactInfo(int id);

    boolean updateContactInfo(ContactInfo contactInfo);

    Result<Map<String,Object>> importContacts(File file, Long uid);

    File exportContacts(String search,Integer groupId,Long uid) throws ClassNotFoundException, IllegalAccessException, InstantiationException;

    List<BaseNode> getContacts(long uid);
}
