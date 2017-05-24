package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.PubContact;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.Department;
import com.dbkj.meet.model.PublicContacts;
import com.dbkj.meet.model.PublicPhone;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.IPublicContactService;
import com.dbkj.meet.utils.ExcelUtil;
import com.dbkj.meet.utils.FileUtil;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by MrQin on 2016/11/24.
 */
public class PublicContactService implements IPublicContactService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());
    private final int PAGE_SIZE=15;
    /**
     * 获取页面的相关数据
     * @param controller
     * @return
     */
    public Map<String, Object> getContactPageData(Controller controller) {
        Map<String,Object> map=new HashMap<String, Object>();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        //获取公司所有的部门
        List<Department> departmentList=Department.dao.findByCompanyId(user.getCid());
        map.put("dlist",departmentList);

        //获取用户分页数据
        //查询条件
        Map<String,Object> params=new HashMap<String, Object>();
        params.put(Constant.CURRENT_PAGE_KEY,1);
        params.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);

        Map<String,String[]> paraMap = controller.getParaMap();
        String[] indexPara=paraMap.get("pageIndex");
        if(indexPara!=null){//当前页码
            params.put(Constant.CURRENT_PAGE_KEY,indexPara[0]);
        }
        String[] departId=paraMap.get("departId");
        if(departId!=null){
            String did=departId[0];
            if(!StrKit.isBlank(did)){
                params.put("b.did",did);
                map.put("departId",did);
            }
        }

        String[] searchStr=paraMap.get("search");
        if(searchStr!=null){
            String search=searchStr[0];
            params.put("a.name",search);
            params.put("b.phone",search);
            map.put("search",search);
        }

        params.put(PublicContacts.COMPANY_ID,user.getCid());

        Page<Record> page= PublicContacts.dao.getContactsPage(params);

        map.put("totalPage", page.getTotalPage());
        map.put("currentPage", page.getPageNumber());
        map.put("pageSize", page.getPageSize());
        map.put("totalRow", page.getTotalRow());
        map.put("contacts", page.getList());

        return map;
    }

    public List<Department> getDepartments(long cid) {
        return Department.dao.findByCompanyId(cid);
    }

    /**
     * 修改联系人姓名时验证要修改的姓名在该联系人当前所有所在的公司是否已存在
     * @param name 要修改的联系人姓名
     * @param id 当前修改的联系人id
     * @param cid 改联系人所在的公司id
     * @return
     */
    public boolean isExistName(String name, long id, long cid) {
        PublicContacts publicContacts = PublicContacts.dao.findByNameAndCompanyId(name,cid);
        return publicContacts!=null&&publicContacts.getId().longValue()!=id;
    }

    /**
     * 判断号码在公司中是否存在
     * @param phone
     * @param cid
     * @return
     */
    public boolean isExistPhone(String phone, long cid) {
        return PublicPhone.dao.findByPhoneAndCompanyId(phone,cid)!=null;
    }

    public boolean isExistPhone(String phone, long id, long cid) {
        PublicPhone publicPhone=PublicPhone.dao.findById(id);
        return publicPhone!=null&&publicPhone.getId().longValue()!=id;
    }

    /**
     * 添加公共联系人
     * @param pubContact
     * @param cid
     * @return
     */
    public boolean addContact(final PubContact pubContact, final Integer cid) {
        if(pubContact!=null){
            final PublicPhone publicPhone=new PublicPhone();
            publicPhone.setDid(pubContact.getDid());
            publicPhone.setPhone(pubContact.getPhone());
            boolean result = Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    PublicContacts publicContacts=PublicContacts.dao.findByNameAndCompanyId(pubContact.getName(),cid);
                    if(publicContacts!=null){
                        publicPhone.setPid(Integer.parseInt(publicContacts.getId().toString()));
                        return publicPhone.save();
                    }else{
                        publicContacts=new PublicContacts();
                        publicContacts.setName(pubContact.getName());
                        publicContacts.setCid(cid);
                        publicContacts.setPosition(pubContact.getPosition());
                        if(publicContacts.save()){
                            publicPhone.setPid(Integer.parseInt(publicContacts.getId().toString()));
                            return publicPhone.save();
                        }
                        return false;
                    }
                }
            });

            return result;
        }
        return false;
    }

    public PubContact getPubContact(Long id) {
        PubContact pubContact=new PubContact();
        PublicPhone publicPhone=PublicPhone.dao.findById(id);
        pubContact.setPhone(publicPhone.getPhone());
        pubContact.setDid(publicPhone.getDid());
        pubContact.setId(publicPhone.getId());
        Integer pid=publicPhone.getPid();
        pubContact.setPid(Long.parseLong(pid.toString()));

        PublicContacts publicContacts=PublicContacts.dao.findById(pid);
        pubContact.setName(publicContacts.getName());
        pubContact.setPosition(publicContacts.getPosition());
        return pubContact;
    }


    public boolean updateContactData(PubContact pubContact) {
        if(pubContact!=null){
            final PublicContacts publicContacts=PublicContacts.dao.findById(pubContact.getPid());
            publicContacts.setName(pubContact.getName());
            publicContacts.setPosition(pubContact.getPosition());

            final PublicPhone publicPhone=PublicPhone.dao.findById(pubContact.getId());
            publicPhone.setPhone(pubContact.getPhone());
            publicPhone.setDid(pubContact.getDid());

            boolean result = Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    if(publicPhone.update()){
                        return publicContacts.update();
                    }
                    return false;
                }
            });

            return result;
        }
        return false;
    }

    public File exportContacts(String search, Integer did,long cid) {
        Map<String,Object> map=new HashMap<String, Object>(3);
        map.put("a.cid",cid);
        if(!StrKit.isBlank(search)){
            map.put("a.name",search);
            map.put("b.phone",search);
        }
        if(did!=null){
            map.put("b.did",did);
        }

        List<Record> contactList = PublicContacts.dao.getContacts(map);

        String[] heads=new String[]{"姓名","部门","职位","电话号码"};
        String[] columns=new String[]{"name","dname","position","phone"};
        //导出前先清除垃圾
        String directory="publicContacts/";
        String path= PathKit.getWebRootPath()+"/download/"+directory;
        FileUtil.deleteFiles(path);

        ExcelUtil excelUtil=new ExcelUtil();
        String fileName = excelUtil.writeExcel(contactList,directory,heads,columns);
        return new File(path+fileName);
    }

    public boolean deleteContacts(String idStr, final long cid) {
        String[] arr=idStr.split(",");
        final int[] ids=new int[arr.length];
        for(int i=0;i<arr.length;i++){
            if(ValidateUtil.isNum(arr[i])){
                ids[i]=Integer.parseInt(arr[i]);
            }
        }
        boolean result = Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                int count=PublicPhone.dao.deleteBatch(ids);
                if(count==ids.length){
                    //由于一个联系人信息可能关联多个电话，电话信息删除
                    // 之后无法确定该联系人是否还关联有其他号码，
                    // 所以统一对公司下所有的未关联的联系人信息进行删除
                    PublicContacts.dao.deleteNotExistByCompanyId(cid);
                    return true;
                }
                return false;
            }
        });

        return result;
    }

    /**
     * 从excel中导入公共联系人
     * @param file
     * @param cid
     * @return
     */
    public Result<Map<String, Object>> importContacts(File file, final Integer cid) {
        //读取数据
        List<Record> list=new ArrayList<Record>();
        ExcelUtil excelUtil=new ExcelUtil();
        excelUtil.readExcel(file,list,new String[]{"name","dname","position","phone"});
        //存放结果数据
        final Map<String,Object> data=new HashMap<String, Object>();
        //验证数据
        List<Record> errorList=validateContacts(list,cid);
        data.put("privateContacts",errorList);

        if(list.size()>0){
            final Set<Department> departmentList=new HashSet<>();
            final Set<PublicContacts> publicContactsList=new HashSet<PublicContacts>();
            final Set<PublicPhone> publicPhoneList=new HashSet<PublicPhone>();

            final Map<String,String> departMap=new HashMap<String, String>();
            final Map<String,String> contactMap=new HashMap<String, String>();

            for(Record record:list){
                String name=record.get("name");
                String dname=record.get("dname");
                String position=record.get("position");
                String phone=record.get("phone");

                PublicPhone publicPhone=new PublicPhone();
                publicPhone.setPhone(phone);
                Department department=Department.dao.findByName(dname,Long.parseLong(cid.toString()));
                PublicContacts publicContacts = PublicContacts.dao.findByNameAndCompanyId(name,cid);
                if(department!=null){//如果该部门存在
                    publicPhone.setDid(Integer.parseInt(department.getId().toString()));
                }else {//该部门不存在
                    department=new Department();
                    department.setName(dname);
                    department.setCid(cid);
                    departmentList.add(department);
                    departMap.put(phone,dname);
                }
                if(publicContacts==null){//如果该联系人信息不存在
                    publicContacts=new PublicContacts();
                    publicContacts.setName(name);
                    publicContacts.setCid(cid);
                    publicContacts.setPosition(position);
                    publicContactsList.add(publicContacts);
                    contactMap.put(phone,name);
                }else{//如果该联系人存在
                    publicPhone.setPid(Integer.parseInt(publicContacts.getId().toString()));
                }
                publicPhoneList.add(publicPhone);
            }

            if(publicContactsList.size()>0){
                boolean flag=Db.tx(new IAtom() {
                    public boolean run() throws SQLException {
                        int[] result=Db.batchSave(new ArrayList<Model>(publicContactsList),100);
                        if(getCount(result)==publicContactsList.size()){
                            result=Db.batchSave(new ArrayList<Model>(departmentList),100);
                            if(getCount(result)==departmentList.size()){
                                prefectPublicPhone(publicPhoneList,cid,contactMap,departMap);
                                result=Db.batchSave(new ArrayList<Model>(publicPhoneList),100);
                                data.put("count",publicPhoneList.size());
                                return getCount(result)==publicPhoneList.size();
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
     *在PublicPhone添加到数据库之前将其数据添加完整
     * @param publicPhoneList
     * @param cid
     * @param contactMap
     * @param departMap
     */
    private void prefectPublicPhone(Set<PublicPhone> publicPhoneList,Integer cid,Map<String,String> contactMap,Map<String,String> departMap){
        if(publicPhoneList!=null){
            for(PublicPhone publicPhone:publicPhoneList){
                String phone=publicPhone.getPhone();
                if(publicPhone.getPid()==null){
                    String name=contactMap.get(phone);
                    PublicContacts publicContacts=PublicContacts.dao.findByNameAndCompanyId(name,cid);
                    if(publicContacts!=null){
                        publicPhone.setPid(Integer.parseInt(publicContacts.getId().toString()));
                    }
                }
                if(publicPhone.getDid()==null){
                    String dname=departMap.get(phone);
                    Department department=Department.dao.findByName(dname,Long.parseLong(cid.toString()));
                    if(department!=null){
                        publicPhone.setDid(Integer.parseInt(department.getId().toString()));
                    }
                }
            }
        }
    }


    private List<Record> validateContacts(List<Record> list,long cid){
        List<Record> errorList=new ArrayList<Record>();
        if(errorList!=null){
            Iterator<Record> itr=list.iterator();
            while (itr.hasNext()){
                Record record=itr.next();
                String name=record.get("name");
                String dname=record.get("dname");
                String phone=record.get("phone");

                if(StrKit.isBlank(name)){
                    errorList.add(record);
                    itr.remove();
                    continue;
                }
                if(StrKit.isBlank(dname)){
                    errorList.add(record);
                    itr.remove();
                    continue;
                }
                if(StrKit.isBlank(phone)||!ValidateUtil.validatePhone(phone)||isExistPhone(phone,cid)){
                    errorList.add(record);
                    itr.remove();
                }
            }
        }
        return errorList;
    }

    /**
     * 根据公司id获取公司公共联系人信息
     * @param cid
     * @return
     */
    @Override
    public List<BaseNode> getContacts(long cid) {
        List<BaseNode> baseNodeList=new ArrayList<>();
        //获取所有的部门
        List<Department> departmentList=Department.dao.findByCompanyId(cid);
        for(int i=0,len=departmentList.size();i<len;i++){
            Department department=departmentList.get(i);
            BaseNode<Map<String,String>> baseNode=new BaseNode<>();
            baseNode.setId(department.getId());
            baseNode.setName(department.getName());
            //获取部门中的所有联系人
            Map<String,Object> params=new HashMap<>();
            params.put("b.did",department.getId());
            List<Record> publicContactsList = PublicContacts.dao.getContacts(params);
            List<Map<String,String>> childrenList=new ArrayList<>();
            for(int n=0,size=publicContactsList.size();n<size;n++){
                Record record=publicContactsList.get(n);
                Map<String,String> children=new HashMap<>();
                children.put("id", record.getLong("bid").toString());
                children.put("name",record.getStr("name"));
                children.put("phone",record.getStr("phone"));
                children.put("position",record.getStr("position"));
                childrenList.add(children);
            }
            baseNode.setChildren(childrenList);
            baseNodeList.add(baseNode);
        }
        return baseNodeList;
    }
}
