package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.ContactInfo;
import com.dbkj.meet.dto.Node;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.ContactCacheInterceptor;
import com.dbkj.meet.interceptors.NameCacheInterceptor;
import com.dbkj.meet.model.Group;
import com.dbkj.meet.model.PrivateContacts;
import com.dbkj.meet.model.PrivatePhone;
import com.dbkj.meet.services.inter.IPersonalContactsService;
import com.dbkj.meet.utils.ExcelUtil;
import com.dbkj.meet.utils.FileUtil;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by MrQin on 2016/11/18.
 */
public class PersonalContactService implements IPersonalContactsService {

    private final int PAGE_SIZE=15;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private List<Group> getGroups(long uid){
        List<Group> groups=Group.dao.findByUserId(uid);
        //判断是否存在未分组的联系人
        if(PrivatePhone.dao.findNoGroupByUserId(uid).size()>0){
            Group group=new Group();
            group.setId(0L);
            group.setName("未分组");
            groups.add(group);
        }
        return groups;
    }


    public Map<String, Object> getRenderData(long uid) {
        Map<String,Object> map=new HashMap<String, Object>();
        List<Group> groups=getGroups(uid);
        map.put("groups",groups);
        //获取分页数据
        Map<String,Object> params=new HashMap<String, Object>();
        params.put("a.uid",uid);
        params.put(Constant.CURRENT_PAGE_KEY,1);
        params.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);
        Page<PrivateContacts> page=PrivateContacts.dao.getContactsPage(params);
        map.put("totalPage",page.getTotalPage());
        map.put("currentPage",1);
        map.put("pageSize",PAGE_SIZE);
        map.put("totalRow",page.getTotalRow());
        map.put("contacts",page.getList());
        map.put("count",page.getTotalRow());
        return map;
    }

    /**
     * 添加联系人组
     * @param group
     * @return
     */
    public Result<Group> addGroup(Group group) {
        if(group!=null){
            if(StrKit.isBlank(group.getName())){
                return new Result<Group>(false,"分组名称不能为空");
            }
            if(Group.dao.findByNameAndUserId(group.getName(),group.getUid())!=null){
                return new Result<Group>(false,"该分组已存在");
            }
            boolean flag=group.save();
            Result result=null;
            if(flag){
                result=new Result(true);
                result.setData(group);
            }else{
                result=new Result(false);
            }
            return result;
        }
        return new Result<Group>(false,"添加失败，缺少必要信息");
    }

    /**
     * 获取联系人分页数据
     * @param map
     * @param uid
     * @return
     */
    public Map<String, Object> getContactsPage(Map<String, String[]> map,long uid) {
        Map<String,Object> attrs=new HashMap<String, Object>();
        attrs.put("groups",getGroups(uid));
        //查询参数
        Map<String,Object> params=new HashMap<String, Object>(map.size());
        params.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);
        params.put(Constant.CURRENT_PAGE_KEY,1);
        String[] pageIndexPara=map.get("pageIndex");

        if(pageIndexPara!=null){
            params.put(Constant.CURRENT_PAGE_KEY,Integer.parseInt(pageIndexPara[0]));

        }

        params.put("a.uid",uid);

        String[] groupIdPara=map.get("groupId");
        if(groupIdPara!=null){
            if(!StrKit.isBlank(groupIdPara[0])){
                params.put("b.gid",groupIdPara[0]);
                attrs.put("currentGroup",Long.parseLong(groupIdPara[0]));
            }
        }
        String[] searchPara=map.get("search");
        if(searchPara!=null){
            params.put("a.name",searchPara[0].trim());
            params.put("b.phone",searchPara[0].trim());
            attrs.put("search",searchPara[0].trim());
        }

        Page<PrivateContacts> page=PrivateContacts.dao.getContactsPage(params);

        attrs.put("totalPage",page.getTotalPage());
        attrs.put("currentPage",page.getPageNumber());
        attrs.put("pageSize",PAGE_SIZE);
        attrs.put("totalRow",page.getTotalRow());
        attrs.put("contacts",page.getList());
        attrs.put("count",page.getTotalRow());

        return attrs;
    }



    /**
     * 修改分组信息
     * @param group
     * @return
     */
    public Result<Group> updateGroup(Group group) {
        if(group!=null){
            if(StrKit.isBlank(group.getName())){
                return new Result<Group>(false,"分组名称不能为空");
            }
            Group group1=Group.dao.findById(group.getId());
            if(!group1.getName().equals(group.getName())&&Group.dao.findByNameAndUserId(group.getName(),group1.getUid())!=null){
                return new Result<Group>(false,"改分组已存在");
            }
            boolean flag=group.update();
            return new Result<Group>(flag);
        }
        return new Result<Group>(false,"添加失败，缺少必要信息");
    }

    /**
     * 删除分组及相关信息
     * @param gid
     * @return
     */
    @Before({NameCacheInterceptor.class, ContactCacheInterceptor.class})
    public boolean deleteGroup(final Long gid) {
        boolean result= Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                PrivateContacts.dao.deleteByGroupId(gid);
                PrivatePhone.dao.deleteByCompanyId(gid);
                return Group.dao.deleteById(gid);
            }
        });

        return  result;
    }

    public List<Group> getGroupsByUserId(long uid) {
        return Group.dao.findByUserId(uid);
    }

    /**
     * 添加联系人
     * @param contactInfo
     * @return
     */
    @Before({NameCacheInterceptor.class, ContactCacheInterceptor.class})
    public boolean addContact(ContactInfo contactInfo) {
        final PrivateContacts privateContacts=new PrivateContacts();
        privateContacts.setName(contactInfo.getName());
        privateContacts.setUid(Integer.parseInt(contactInfo.getUid().toString()));
        privateContacts.setEmail(contactInfo.getEmail());
        privateContacts.setRemark(contactInfo.getRemark());

        final PrivatePhone privatePhone=new PrivatePhone();
        privatePhone.setPhone(contactInfo.getPhone());
        if(contactInfo.getGroupId()!=null){
            privatePhone.setGid(Integer.parseInt(contactInfo.getGroupId().toString()));
        }

        boolean result = Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                Map<String,Object> map=new HashMap<String, Object>(2);
                map.put("a.name",privateContacts.getName());
                map.put("a.uid",privateContacts.getUid());
                Record contact=PrivateContacts.dao.findContact(map);
                if(contact!=null){//如果已存在该联系人的信息
                    privatePhone.setPid(contact.getInt("pid"));
                }else{//如果不存在则先添加联系人信息，再添加电话号码
                    if(privateContacts.save()){
                        privatePhone.setPid(Integer.parseInt(privateContacts.getId().toString()));
                    }
                }
                return privatePhone.save();
            }
        });

        return result;
    }

    /**
     * 判断联系人是否重复
     * @param phone
     * @param uid
     * @return
     */
    public boolean isExistContactByPhone(String phone, long uid) {
        Map<String,Object> map=new HashMap<String, Object>(2);
        map.put("a.uid",uid);
        map.put("b.phone",phone);
        return PrivateContacts.dao.findContact(map)!=null;
    }

    /**
     * 更新信息时判断电话号码是否重复
     * @param phone 更改后的电话号码
     * @param id 联系人号码id
     * @param uid 当前用户id
     * @return
     */
    public boolean isExistContactByPhone(String phone, long id, long uid) {
        Map<String,Object> map=new HashMap<String, Object>(2);
        map.put("a.uid",uid);
        map.put("b.phone",phone);
        Record record = PrivateContacts.dao.findContact(map);

        return false;
    }

    /**
     * 更新时判断姓名是否和已有的联系人姓名重复
     * @param name 修改的联系人姓名
     * @param pid 联系人pid
     * @param uid 当前用户id
     * @return
     */
    public boolean isExistContactByName(String name,long pid, long uid) {
        Map<String,Object> map=new HashMap<String, Object>(2);
        map.put("a.uid",uid);
        map.put("a.name",name);
        Record record = PrivateContacts.dao.findContact(map);

        return false;
    }


    /**
     * 批量删除联系人
     * @param ids
     * @return
     */
    @Before({NameCacheInterceptor.class, ContactCacheInterceptor.class})
    public boolean deleteContacts(final Long uid, String ids) {
        String[] arr = ids.split("-");
        final int[] params=new int[arr.length];
        for(int i=0,len=arr.length;i<len;i++){
            params[i]=Integer.parseInt(arr[i]);
        }
        boolean result = Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                //由于一个联系人可能有多个号码，所有要判断当前号码对应的联系人
                //是否有多个号码，如果有则只删除当前号码，否则就连联系人一起删除
                List<Integer> ids=getDeleteContactIds(params);
                if(PrivatePhone.dao.deleteBatchById(params)==params.length){
                    return PrivateContacts.dao.deleteBatchById(uid,ids)==ids.size();
                }
                return false;
            }
        });

        return result;
    }

    //获取要删除的联系人id
    private List<Integer> getDeleteContactIds(int[] params){
        if(params!=null){
            List<Integer> list=new ArrayList<>(params.length);
            int n=0;
            for(int i=0;i<params.length;i++){
                PrivatePhone privatePhone=PrivatePhone.dao.findById(params[i]);
                int pid=privatePhone.getPid();
                List<PrivatePhone> privatePhoneList=PrivatePhone.dao.findByContactId(pid);
                if(privatePhoneList.size()==1){
                    list.add(pid);
                    n++;
                }
            }
            return list;
        }
        return null;
    }

    /**
     * 获取数组去除为0元素后的长度
     * @param arr
     * @return
     */
    private int getRealCount(int[] arr){
        if(arr!=null){
            int temp=0;
            for(int i=0;i<arr.length;i++){
                if(arr[i]!=0){
                    temp++;
                }
            }
            return temp;
        }
        return 0;
    }

    public ContactInfo getContactInfo(int id) {
        Map<String,Object> map=new HashMap<String, Object>(1);
        map.put("b.pid",id);
        Record record=PrivateContacts.dao.findContact(map);
        if(record!=null){
            ContactInfo contactInfo=new ContactInfo();
            contactInfo.setId(record.getLong("id"));
            contactInfo.setPid(Long.parseLong(record.getInt("pid").toString()));
            contactInfo.setName(record.getStr("name"));
            contactInfo.setPhone(record.getStr("phone"));
            contactInfo.setGroupId(Long.parseLong(record.getInt("gid").toString()));
            contactInfo.setUid(Long.parseLong(record.getInt("uid").toString()));
            contactInfo.setEmail(record.getStr("email"));
            contactInfo.setRemark(record.getStr("remark"));
            return contactInfo;
        }
        return null;
    }

    /**
     * 更新联系人信息
     * @param contactInfo
     * @return
     */
    @Before({NameCacheInterceptor.class, ContactCacheInterceptor.class})
    public boolean updateContactInfo(ContactInfo contactInfo) {
        if(contactInfo!=null){
            final PrivateContacts privateContacts=new PrivateContacts();
            privateContacts.setId(contactInfo.getPid());
            privateContacts.setName(contactInfo.getName());
            privateContacts.setUid(Integer.parseInt(contactInfo.getUid().toString()));
            privateContacts.setRemark(contactInfo.getRemark());
            privateContacts.setEmail(contactInfo.getEmail());

            final PrivatePhone privatePhone=new PrivatePhone();
            privatePhone.setId(contactInfo.getId());
            privatePhone.setPid(Integer.parseInt(contactInfo.getPid().toString()));
            privatePhone.setGid(Integer.parseInt(contactInfo.getGroupId().toString()));
            privatePhone.setPhone(contactInfo.getPhone());

            boolean result = Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    if(privatePhone.update()){
                        return privateContacts.update();
                    }
                    return false;
                }
            });

            return result;
        }
        return false;
    }

    /**
     * 从上传文件中导入联系人数据
     * @param file
     * @param uid
     * @return
     */
    @Before({NameCacheInterceptor.class, ContactCacheInterceptor.class})
    public Result<Map<String,Object>> importContacts(File file, final Long uid) {
        //读取数据
        List<Record> list=new ArrayList<Record>();
        ExcelUtil excelUtil=new ExcelUtil();
        excelUtil.readExcel(file,list,new String[]{"name","gname","phone","email"});
        //存放结果数据
        final Map<String,Object> data=new HashMap<String, Object>();
        //验证数据
        List<Record> errorList=validateContacts(list,uid);
        data.put("privateContacts",errorList);

        if(list.size()>0){
            final Set<Group> groups=new HashSet<>();
            final Set<PrivateContacts> privateContactses=new HashSet<>();
            final Set<PrivatePhone> privatePhones=new HashSet<>();

            final Map<String,String> contactMap=new HashMap<String, String>();
            final Map<String,String> groupMap=new HashMap<String, String>();
            for(Record record:list){
                String name=record.get("name");
                String phone=record.get("phone");
                String gname=record.get("gname");
                String email=record.get("email");

                PrivatePhone privatePhone=new PrivatePhone();
                privatePhone.setPhone(phone);
                Group group=Group.dao.findByNameAndUserId(gname,uid);
                PrivateContacts privateContact=PrivateContacts.dao.findByNameAndUserId(name,uid);
                if(group!=null){//如果该组别存在
                    privatePhone.setGid(Integer.parseInt(group.getId().toString()));
                }else{//如果该组别不存在
                    Group group1=new Group();
                    group1.setName(gname);
                    group1.setUid(Integer.parseInt(uid.toString()));
                    groups.add(group1);
                    groupMap.put(phone,gname);
                }
                if(privateContact==null){//如果该联系人信息不存在
                    privateContact=new PrivateContacts();
                    privateContact.setName(name);
                    privateContact.setUid(Integer.parseInt(uid.toString()));
                    privateContact.setEmail(email);
                    privateContactses.add(privateContact);
                    contactMap.put(phone,name);
                }else{//如果该联系人信息存在
                    privatePhone.setPid(Integer.parseInt(privateContact.getId().toString()));
                }
                privatePhones.add(privatePhone);
            }
            if(privatePhones.size()>0){
                //将联系人相关信息添加到数据库
                boolean flag = Db.tx(new IAtom() {
                    public boolean run() throws SQLException {
                        int[] result = Db.batchSave(new ArrayList<Model>(privateContactses),100);
                        if(getCount(result)==privateContactses.size()){
                            result=Db.batchSave(new ArrayList<Model>(groups),100);
                            if(getCount(result)==groups.size()){
                                perfectPrivatePhones(privatePhones,uid,contactMap,groupMap);
                                result=Db.batchSave(new ArrayList<Model>(privatePhones),100);
                                data.put("count",privatePhones.size());
                                return getCount(result)==privatePhones.size();
                            }
                        }
                        return false;
                    }
                });

                if(!flag){
                    data.put("count",0);
                }
                return new Result<Map<String, Object>>(flag,data);
            }

        }
        try{
            file.delete();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        data.put("count",0);
        return new Result<Map<String, Object>>(false,data);
    }

    public File exportContacts(String search, Integer groupId,Long uid) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Map<String,Object> map=new HashMap<String, Object>(3);
        map.put("a.uid",uid);
        if(!StrKit.isBlank(search)){
            map.put("a.name",search);
            map.put("b.phone",search);
        }
        if(groupId!=null){
            map.put("b.gid",groupId);
        }
        List<Record> privateContacts = PrivateContacts.dao.getContacts(map);

        for(Record record:privateContacts){
            //获取联系人的分组
            Group group=Group.dao.findById(record.get("gid"));
            if(group==null){
                record.set("gname","未分组");
            }else{
                record.set("gname",group.getName());
            }

        }
        String[] heads=new String[]{"姓名","组别","电话","email","备注"};//excel表头
        String[] columns=new String[]{"name","gname","phone","email","remark"};//要到导出的数据字段
        //导出前先清除垃圾
        String directory="privateContacts/";
        String path=PathKit.getWebRootPath()+"/download/"+directory;
        FileUtil.deleteFiles(path);

        String fileName = ExcelUtil.writeExcel(privateContacts,directory,heads,columns);
        return new File(path+fileName);
    }

    /**
     * 在PrivatePhone添加到数据库之前将其数据完善
     * @param list
     */
    private void perfectPrivatePhones(Set<PrivatePhone> list,Long uid,Map<String,String> contactMap,Map<String,String> groupMap){
        if(list!=null){
            for(PrivatePhone privatePhone:list){
                String phone=privatePhone.getPhone();
                if(privatePhone.getPid()==null){
                    String name=contactMap.get(phone);
                    PrivateContacts privateContacts = PrivateContacts.dao.findByNameAndUserId(name,uid);
                    if(privateContacts!=null){
                        privatePhone.setPid(Integer.parseInt(privateContacts.getId().toString()));
                    }
                }
                if(privatePhone.getGid()==null){
                    String gname=groupMap.get(phone);
                    Group group = Group.dao.findByNameAndUserId(gname,uid);
                    if(group!=null){
                        privatePhone.setGid(Integer.parseInt(group.getId().toString()));
                    }
                }
            }
        }
    }

    private int getCount(int[] arr){
        int count=0;
        if(arr!=null){
            for(int i=0,len=arr.length;i<len;i++){
                count+=arr[i];
            }
        }
        return count;
    }

    /**
     * 验证需要上传的联系人数据
     * @param list
     * @return
     */
    private List<Record> validateContacts(List<Record> list,long uid){
        List<Record> errorList=new ArrayList<Record>();
        if(errorList!=null){
            Iterator<Record> itr=list.iterator();
            while (itr.hasNext()){
                Record record=itr.next();
                String name=record.get("name");
                String gname=record.get("gname");
                String phone=record.get("phone");
                String email=record.get("email");
                if(StrKit.isBlank(name)){
                    errorList.add(record);
                    itr.remove();
                    continue;
                }
                if(StrKit.isBlank(gname)){
                    errorList.add(record);
                    itr.remove();
                    continue;
                }
                if(StrKit.isBlank(phone)||!ValidateUtil.validatePhone(phone)||isExistContactByPhone(phone,uid)){
                    errorList.add(record);
                    itr.remove();
                    continue;
                }
                if(!StrKit.isBlank(email)&& ValidateUtil.validateEmail(email)){
                    errorList.add(record);
                    itr.remove();
                }
            }
        }
        return errorList;
    }

    /**
     * 根据用户id获取用的个人联系人信息
     * @param uid
     * @return
     */
    @Override
    public List<BaseNode> getContacts(long uid) {
        List<BaseNode> baseNodeList=new ArrayList<>();
        List<Group> groupList=Group.dao.findByUserId(uid);
        groupList.add(new Group().set("id",0L).set("name","未分组"));
        for(int i=0,size=groupList.size();i<size;i++){
            Group group=groupList.get(i);
            BaseNode<Node> baseNode=new BaseNode<>();
            baseNode.setId(baseNode.getId());
            baseNode.setName(group.getName());

            //获取当前分组下的联系人信息
            Map<String,Object> map=new HashMap<>();
            map.put("b.gid",group.getId());
            map.put(PrivateContacts.A_UID,uid);
            List<Record> privateContactses=PrivateContacts.dao.getContacts(map);
            List<Node> childrenNodes=new ArrayList<>();
            for(int n=0,len=privateContactses.size();n<len;n++){
                Record record=privateContactses.get(n);
                Node node=new Node();
                node.setId(record.getLong("bid"));//获取的是phone的id，contact的id可能会有冲突
                node.setName(record.getStr("name"));
                node.setPhone(record.getStr("phone"));
                childrenNodes.add(node);
            }
            baseNode.setChildren(childrenNodes);
            baseNodeList.add(baseNode);
        }
        return baseNodeList;
    }
}
