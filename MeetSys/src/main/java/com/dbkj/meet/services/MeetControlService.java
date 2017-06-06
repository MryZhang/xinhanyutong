package com.dbkj.meet.services;

import com.dbkj.meet.dic.*;
import com.dbkj.meet.dto.BaseContact;
import com.dbkj.meet.dto.Node;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.interceptors.RemoveBillCacheInterceptor;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.common.MeetManager;
import com.dbkj.meet.services.inter.*;
import com.dbkj.meet.utils.RedisUtil;
import com.dbkj.meet.utils.ValidateUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.IDataLoader;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by MrQin on 2016/11/25.
 */
public class MeetControlService implements IMeetControlService {

    private final static Object lock=new Object();

    /**
     * redis存储参会人信息的key前缀
     */
    private final String ATTENDEE_REDIS_KEY_PREFIX="meet_attendee";

    /**
     * 参会者缓存键
     */
    private final String ATTENDEE_CACHE_KEY="attendee_cache";

    private final Logger logger=Logger.getLogger(this.getClass());

    private IChargeService chargeService=new ChargeService();

    private MessageService messageService=new MessageServiceImpl();

    private ChargingService chargingService=new ChargingServiceImpl();

    private INameService nameService=new NameServiceImpl();

    private IPersonalContactsService personalContactsService;
    /**
     * 获取会议的相关数据
     * @param user 当前用户
     * @param id 会议记录id
     * @return
     */
    public Map<String, Object> getMeetData(User user, Integer id) {
        Map<String,Object> map=new HashMap<String, Object>();

        //获取下载地址前缀
        String recPrefix = PropKit.use("config.properties").get(Constant.DOWNLOAD_URL_PREFIX);
        map.put("recPrefix",recPrefix);

        Long uid=user.getId();
        long cid=user.getCid();
        //获取呼入号码
        Company company=Company.dao.findById(user.getCid());
        String callNum=AccessNum.dao.findById(company.getCallNum()).getNum();
        map.put("callNum",callNum);

        //获取当前会议创建者姓名
        Employee employee=Employee.dao.findById(user.getEid());
        final String name=employee.getName();
        final String phone=employee.getPhone();
        map.put("uname",name);
        map.put("hostNum",phone);
        //获取当前会议记录
        Record record=null;
        if(id!=null){
            record=Record.dao.findById(id);
        }else{
            record=Record.dao.findByUserId(uid);
        }
        //会议邀请人数据
        List<Node> inviteData=new ArrayList<Node>();
        if(record!=null){//会议已开始
            //正在会议中的参会人
            List<Node> inMeetData=new ArrayList<Node>();
            String meetId=record.getMid();
            List<Node> attendeeList=null;
            if(!StrKit.isBlank(meetId)){//会议已开始
                attendeeList=getAttendeeList(meetId,record.getHost(),cid,uid,record.getId());
            }else{//会议已创建未开始
                //会议未开始时，会议所有的邀请人数据存在缓存中，以提高效率
                attendeeList=getAttendeeData(record.getId());
            }
            getMeetAttendee(attendeeList,inviteData,inMeetData);
            map.put("inMeetData",JsonKit.toJson(inMeetData));
        }else{//会议还未创建，要创建会议相关记录
            MeetManager meetManager=MeetManager.getInstance();
            String hostPwd=meetManager.getPassword();
            String listenerPwd=meetManager.getPassword();
            //新增会议记录
            int meetNums=meetManager.getMeetNums();
            record=new Record().set("subject",name+"的电话会议").set("hostName", name).set("host", phone).set("allow_begin",Integer.parseInt(Constant.YES))
                    .set("startTime",new Date()).set("hostPwd", hostPwd).set("listenerPwd", listenerPwd).set("meetNums", meetNums)
                    .set("belong", uid).set("status", MeetState.STARTED.getStateCode()).set("isRecord", 0).set("notice",Integer.parseInt(Constant.NO));

            boolean result= record.save();

            if(result){//会议记录添加成功
                //添加主持人的参会人数据
                Attendee attendee=new Attendee();
                attendee.setName(name);
                attendee.setPhone(phone);
                attendee.setRid(Integer.parseInt(record.getId().toString()));
                attendee.setType(AttendeeType.HOST.getCode());
                attendee.setStatus(Integer.parseInt(Constant.WATING_CALL));
                attendee.save();

                Node node=new Node();
                node.setName(name);
                node.setPhone(phone);
                node.setStatus(Constant.WATING_CALL);
                node.setRole(AttendeeType.HOST.getCode());
                node.setHost(true);

                addAttendeeData(node,record.getId());
                inviteData.add(node);

            }else{//
                throw new RuntimeException("会议记录添加失败！");
            }
        }


        map.put("record",record);
        map.put("inviteData", JsonKit.toJson(inviteData));
        return map;
    }

    private void getMeetAttendee(List<Node> attendeeList,List<Node> inviteData,List<Node> inMeetData){
        if(attendeeList!=null){
            for(Node attendee:attendeeList){
                if(attendee.getStatus().equals(Constant.CALL_STATUS_JOIN_MEET)||attendee.getStatus().equals(Constant.CALL_STATUS_ANSWER)){
                    inMeetData.add(attendee);
                }else{
                    inviteData.add(attendee);
                }
            }
            //将参会人按照是否主持人排序
            Collections.sort(inMeetData, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if(o1.isHost()&&!o2.isHost()){
                        return -1;
                    }
                    if(!o1.isHost()&&o2.isHost()){
                        return 1;
                    }
                    return 0;
                }
            });
            Collections.sort(inviteData, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if(o1.isHost()&&!o2.isHost()){
                        return -1;
                    }
                    if(!o1.isHost()&&o2.isHost()){
                        return 1;
                    }
                    return 0;
                }
            });
        }

    }

    /**
     * 从缓存中获取参会人信息
     * @return
     */
    private List<Node> getAttendeeData(long recordId){
        Set<Node> attendeeList=getAttendeeDataFromRedis(recordId);
        if(attendeeList==null){
            attendeeList=new HashSet<>();
            List<Attendee> alist = Attendee.dao.findByRecordId(recordId);
            for(Attendee atd:alist){
                Node node=new Node();
                node.setId(atd.getId());
                node.setName(atd.getName());
                node.setPhone(atd.getPhone());
                node.setRole(atd.getType());
                node.setStatus(atd.getStatus().toString());
                attendeeList.add(node);
            }
            addToRedis(attendeeList,recordId);
        }
        List<Node> list=new ArrayList<>(attendeeList);
        Collections.sort(list, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if(o1.isHost()&&!o2.isHost()){
                    return -1;
                }
                if(!o1.isHost()&&o2.isHost()){
                    return 1;
                }
                return 0;
            }
        });
        return list;
    }

    /**
     * 将会议邀请人添加到缓存中
     * @param attendee
     */
    private boolean addAttendeeData(Node attendee,long recordId){
        Set<Node> attendeeList=getAttendeeDataFromRedis(recordId);
        if(attendeeList==null){
            attendeeList=new HashSet<>();
        }
//        synchronized (lock){
            boolean isRepeat=false;
            for(Node node:attendeeList){
                if(node.getPhone().equals(attendee.getPhone())){
                    node.setRole(attendee.getRole());
                    node.setStatus(attendee.getStatus());
                    node.setHost(attendee.isHost());
                    isRepeat=true;
                    if(node.isDeleted()){
                        node.setDeleted(false);
                    }
                    break;
                }
            }
            if(!isRepeat){
                attendeeList.add(attendee);
            }
//        }
        addToRedis(attendeeList,recordId);
        return true;
    }

    private void addToRedis(Set<Node> nodeList,long recordId){
        //先清除缓存，再存入信息
        RedisUtil redisUtil=RedisUtil.getInstance();
        String key=ATTENDEE_REDIS_KEY_PREFIX+":"+recordId;
//        redisUtil.del(key);
        redisUtil.set(key,nodeList);
    }

    private void addToRedis(List<Node> nodeList,long recordId){
        addToRedis(new HashSet<Node>(nodeList),recordId);
    }

    private void addAttendeeData(Set<Node> attendeeList,long recordId){
        Set<Node> attendeeData=getAttendeeDataFromRedis(recordId);
        if(attendeeData==null){
            attendeeData=new HashSet<>();
        }
        boolean flag = attendeeData.addAll(attendeeList);
        if(flag){
            //先清除缓存，再存入信息
            addToRedis(attendeeData,recordId);
        }
    }

    private Set<Node> getAttendeeDataFromRedis(long recordId){
        return RedisUtil.getInstance().get(ATTENDEE_REDIS_KEY_PREFIX+":"+recordId);
    }

    /**
     * 从缓存中删除邀请人信息
     * @param phone
     * @param recordId
     * @return
     */
    public boolean removeAttendeeData(String phone,long recordId){
        Set<Node> attendeeList=getAttendeeDataFromRedis(recordId);
        Record record=Record.dao.findById(recordId);
        MeetState meetState = MeetState.valueOf(record.getStatus());
//        synchronized (lock){
            if(attendeeList!=null){
                boolean flag=false;
                if(meetState==MeetState.GOINGON){
                    for(Node attendee:attendeeList){
                        if(attendee.getPhone().equals(phone)){
                            attendee.setDeleted(true);
                            flag=true;
                            break;
                        }
                    }
                }else if(meetState==MeetState.STARTED){
                    Iterator<Node> iterator=attendeeList.iterator();
                    while (iterator.hasNext()){
                        Node attendee1=iterator.next();
                        if(attendee1.getPhone().equals(phone)){
                            flag=true;
                            iterator.remove();
                            break;
                        }
                    }
                }
                if(flag){
                    addToRedis(attendeeList,recordId);
                }
                return flag;
            }
//        }
        return false;
    }

    /**
     * 从缓存中清除会议邀请人数据
     * @param rid
     */
    private void clearAttendeeData(long rid){
        RedisUtil.getInstance().del(ATTENDEE_REDIS_KEY_PREFIX+":"+rid);
    }

    private String getStatusByCode(String code){
        String status="";
        switch (code){
            case Constant.WATING_CALL:
                status="等待呼叫";
                break;
            case Constant.CALL_STATUS_IDLE:
                status="空闲";
                break;
            case Constant.CALL_STATUS_TRING:
                status="处理中";
                break;
            case Constant.CALL_STATUS_RING:
                status="振铃中";
                break;
            case Constant.CALL_STATUS_ANSWER:
                status="应答";
                break;
            case Constant.CALL_STATUS_BYE:
                status="挂机";
                break;
            case Constant.CALL_STATUS_FAIL:
                status="失败";
                break;
            case Constant.CALL_STATUS_REJECT:
                status="拒接";
                break;
        }
        return status;
    }

    /**
     * 获取参会人信息
     * @param meetId
     * @param hostNum
     * @param cid
     * @param uid
     * @param rid
     * @return
     */
    private List<Node> getAttendeeList(String meetId,String hostNum,long cid,long uid,long rid){
        List<Node> attendeeList=getAttendeeData(rid);
        String phone=null;
        //获取第二主持人号码和移除会议的号码
        List<Node> removeNodeList=new ArrayList<>();
        if(attendeeList!=null){
            for(Node node:attendeeList){
                if(node.isHost()&&!hostNum.equals(node.getPhone())){
                    phone=node.getPhone();
                }
                if(node.isDeleted()){
                    removeNodeList.add(node);
                }
            }
        }
        attendeeList=getAttendeeList(meetId,hostNum,cid,uid,phone,removeNodeList);
        if(attendeeList!=null){
            addToRedis(attendeeList,rid);
        }else{
            attendeeList=new ArrayList<>();
        }
        return attendeeList;
    }

    /**
     * 获取参会者数据
     * @param meetId 会议id
     * @param hostNum 主持人号码
     * @param cid 公司id
     * @param uid 用户id
     * @param secondHost 第二主持人号码
     * @return
     */
    private List<Node> getAttendeeList(String meetId,String hostNum,long cid,long uid,String secondHost,List<Node> removeNodes){
        //从会议服务器上拉取参会人信息
        Map<String,Object> resultMap=null;
        try{
            resultMap = MeetManager.getInstance().getMeetCallStatus(meetId);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        List<Node> attendeeList=null;
        if(resultMap!=null&&Constant.SUCCESS.equals(resultMap.get(Constant.STATUS))){
            //解析参会人信息
            String content=resultMap.get(Constant.CONTENT).toString();
            Gson gson=new Gson();
            List<Map<String,String>> callers = gson.fromJson(content,new TypeToken<List<HashMap<String,String>>>(){}.getType());
            Set<Node> nodes=new HashSet<>();
            for(Map<String,String> map:callers){
                Node node=new Node();
                String phone=map.get(Constant.CALLER).toString();
                node.setPhone(phone);
                boolean isHost=hostNum.equals(phone)||phone.equals(secondHost);
                node.setHost(isHost);
                if(isHost){
                    node.setRole(AttendeeType.HOST.getCode());
                }else{
                    node.setRole(AttendeeType.ATTENDEE.getCode());
                }
                node.setDeleted(false);
                for(Node n:removeNodes){
                    if(n.getPhone().equals(phone)){
                        node.setDeleted(n.isDeleted());
                        break;
                    }
                }
                String name=map.get(Constant.NAME);
                if(StrKit.isBlank(name)||phone.equals(name)){
                    name=nameService.getNameByPhone(uid,phone);
                    name=name==null?phone:name;
                }
                node.setName(name);
                node.setStatus(map.get(Constant.STATUS));
                nodes.add(node);
            }
            attendeeList =new ArrayList<>(nodes);
            //按照参会人类型升序排列
            Collections.sort(attendeeList, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    if(o1.isHost()&&o2.isHost()){
                        return 0;
                    }
                    if(o1.isHost()&&!o2.isHost()){
                        return -1;
                    }
                    if(!o1.isHost()&&o2.isHost()){
                        return 1;
                    }
                    return 0;
                }
            });
        }
        return attendeeList;
    }

    /**
     * 从公共联系人和个人联系人中获取联系人姓名
     * @param phone
     * @param uid
     * @return
     */
    private String getName(String phone, final long cid, final long uid){
        //从公共联系人和私有联系人中获取联系人姓名
        List<com.jfinal.plugin.activerecord.Record> publicContactsList=CacheKit.get(Constant.CACHE_NAME_ADMDIV, Constant.PUBLIC_CONTACT_CACHE_KEY, new IDataLoader() {
            public Object load() {
                Map<String,Object> map=new HashMap<String, Object>();
                map.put("a.cid",cid);
                return PublicContacts.dao.getContacts(map);
            }
        });

        List<com.jfinal.plugin.activerecord.Record> privateContactsList=CacheKit.get(Constant.CACHE_NAME_ADMDIV, Constant.PRIVATE_CONTACT_CACHE_KEY, new IDataLoader() {
            public Object load() {
                Map<String,Object> map=new HashMap<String, Object>();
                map.put("a.uid",uid);
                return PrivateContacts.dao.getContacts(map);
            }
        });

        String name=null;
        for(com.jfinal.plugin.activerecord.Record record:publicContactsList){
            if(record.get("phone").equals(phone)){
                name=record.getStr("name");
                break;
            }
        }
        if(name==null){
            for(com.jfinal.plugin.activerecord.Record record:privateContactsList){
                if(record.get("phone").equals(phone)){
                    name=record.getStr("name");
                    break;
                }
            }
        }
        //如果没有找到则默认电话号码
        if(name==null){
            name=phone;
        }
        return name;
    }

    @Override
    public Result addAttendee(Controller controller) {
        Long rid=controller.getParaToLong("rid");
        String name=controller.getPara("name");
        String phone=controller.getPara("phone");
        Node attendee=new Node();
        attendee.setName(name);
        attendee.setPhone(phone);
        attendee.setRole(AttendeeType.ATTENDEE.getCode());
        attendee.setStatus(Constant.WATING_CALL);
        boolean flag=addAttendeeData(attendee,rid);
        return new Result(flag);
    }


    @Override
    public boolean removeAttendee(Controller controller) {
        Long rid=controller.getParaToLong("rid");
        String phone=controller.getPara("phone");
        boolean flag = removeAttendeeData(phone,rid);
        return flag;
    }


    /**
     * 创建会议
     * @param controller
     * @return
     */
    @Override
    public Result<Map<String,Object>> creeteMeet(Controller controller) {
        Result<Map<String,Object>> result=new Result<>(false);
//        String meetPwd=controller.getPara("meetPwd");
        boolean isRecord=controller.getParaToBoolean("isRecord");
        String phone=controller.getPara("hostNum");
        String hostName=controller.getPara("hostName");
        //会议开始是否允许发言
        boolean allowBegin=controller.getParaToBoolean("allowBegin");
        User user=controller.getSessionAttr(Constant.USER_KEY);
        int rid=controller.getParaToInt("rid");
        Record record=Record.dao.findById(rid);
        String meetPwd=record.getHostPwd();
        //获取计费号码和显示号码
        Company company=Company.dao.findById(user.getCid());
        String costNum=AccessNum.dao.findById(company.getCallNum()).getNum();
        String showNum=AccessNum.dao.findById(company.getShowNum()).getNum();

        Map<String,Object> map=new HashMap<>();
        map.put(Constant.CHAIRMANPWD,meetPwd);
        map.put(Constant.COSTNUM,costNum);
        map.put(Constant.SHOWNUM,showNum);
        map.put(Constant.CALLER,phone);
        map.put(Constant.NAME,hostName);
//        if(allowBegin){
//            map.put(Constant.LEVEL,Constant.MEET_LEVEL_CHARIMAN);
//        }else{
//            map.put(Constant.LEVEL,Constant.MEET_LEVEL_AUDIENCE);
//        }

        MeetManager meetManager=MeetManager.getInstance();
        Map<String,Object> resultMap = null;
        try{
            resultMap = meetManager.createMeet(map);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            result.setResult(false);
            result.setMsg("创建会议失败！");
            return result;
        }

        if(resultMap.get(Constant.STATUS).equals(Constant.SUCCESS)){
            result.setResult(true);
            //清除邀请人数据缓存
//            clearAttendeeData(rid);
            String meetId=resultMap.get(Constant.CONTENT).toString();
            Map<String,Object> data=new HashMap<>();
            data.put("meetId",meetId);
            data.put("hostNum",phone);
            result.setData(data);

            //更新会议记录
            String subject=controller.getPara("subject");
            boolean flag=updateRecord(controller,meetId,allowBegin);
            if(!flag){
                logger.error("更新会议记录失败，要更新的会议记录id为："+rid);
            }

            if(isRecord){//会议录音
                Map<String,Object> recordResult = meetManager.startRecord(meetId);
                if(!recordResult.get(Constant.STATUS).equals(Constant.SUCCESS)){
                    logger.error("会场录音失败，会议记录id："+rid+",meetId："+meetId);
                }
            }
        }
        return result;
    }

    private boolean updateRecord(Controller controller, final String meetId,boolean allowBegin){
        boolean isRecord=controller.getParaToBoolean("isRecord");
        String hostNum=controller.getPara("hostNum");
        String subject=controller.getPara("subject");
        String hostName=controller.getPara("hostName");
        int rid=controller.getParaToInt("rid");
        boolean notice=controller.getParaToBoolean("notice");
        if(StrKit.isBlank(subject)){//如果主题为空，则设置默认主题
            long eid=((User)controller.getSessionAttr(Constant.USER_KEY)).getEid();
            String name=Employee.dao.findById(eid).getName();
            subject=name.concat("的电话会议");
        }
        final Record record=Record.dao.findById(rid);
        if(StrKit.isBlank(hostName)){
            hostName=hostNum;
        }

        record.set("hostName", hostName).set("status", MeetState.GOINGON.getStateCode())//.set("startTime", new Date())
                .set("isRecord", isRecord?1:0).set("subject",subject).set("mid",meetId).set("host",hostNum).
                set("notice",notice?Constant.YES:Constant.NO).set("allow_begin",allowBegin?Constant.YES:Constant.NO);
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                if(record.getType()==MeetType.FIXED_MEET.getCode()){//如果是固定会议，同时还要更新固定会议mid
                    FixedMeet fixedMeet=FixedMeet.dao.findById(record.getOid());
                    if(fixedMeet!=null){
                        fixedMeet.set("mid",meetId).update();
                    }
                }
                return record.update();
            }
        });
    }

    @Override
    public boolean updateMeetStatus(final long recordId) {
        final Record record = Record.dao.findById(recordId);
        if(record==null){
            return false;
        }
        //清除缓存中参会人数据
        clearAttendeeData(recordId);
        //会议进行中
        if(MeetState.GOINGON.getStateCode()==record.getStatus()){
            String endTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            record.set("status",MeetState.FINSHED.getStateCode()).set("endTime",endTime).setGmtModified(new Date());
            return Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    if(record.update()){
                        Map<String,Object> params=new HashMap<String, Object>();
                        params.put("status",new String[]{Constant.CALL_STATUS_ANSWER,Constant.CALL_STATUS_JOIN_MEET});
                        params.put("rid",recordId);
                        Attendee.dao.updateAttendeeeStatus(Constant.CALL_STATUS_BYE,params);
                        return true;
                    }
                    return false;
                }
            });
        }else{//会议已开始未创建
            return Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Attendee.dao.deleteByRecordId(recordId);
                    return record.delete();
                }
            });
        }
    }

    @Override
    public boolean cancelMeet(final Long rid) {
        if(rid!=null){
            final Record record = Record.dao.findById(rid);
            if(record==null){
                return false;
            }
            if(MeetState.STARTED.getStateCode()==record.getStatus()){
//                return Db.tx(new IAtom() {
//                    @Override
//                    public boolean run() throws SQLException {
//                        Attendee.dao.deleteByRecordId(rid);
//                        return record.delete();
//                    }
//                });
                record.setStatus(MeetState.FINSHED.getStateCode());
                return record.update();
            }
            //清除缓存中会议参会人数据
            clearAttendeeData(rid);
        }
        return false;
    }

    @Override
    public Result addPersonalContact(final BaseContact baseContact, final long uid) {
        if(personalContactsService==null){
            personalContactsService= new PersonalContactService();
        }
        Result<BaseContact> result=validateContact(baseContact);
        if(!result.getResult()){//验证失败
            return result;
        }
        //判断该联系人的号码是否已存在与个人通讯录中
        boolean flag = personalContactsService.isExistContactByPhone(baseContact.getPhone(),uid);
        if(!flag){
            flag = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    //判断个人通讯中是否已存在该联系人
                    Map<String,Object> map=new HashMap<String, Object>(2);
                    map.put("a.name",baseContact.getName());
                    map.put("a.uid",uid);
                    com.jfinal.plugin.activerecord.Record contact=PrivateContacts.dao.findContact(map);
                    if(contact!=null){//存在则只用添加号码
                        PrivatePhone privatePhone=new PrivatePhone();
                        privatePhone.setPhone(baseContact.getPhone());
                        privatePhone.setPid(contact.getInt("pid"));
                        return privatePhone.save();
                    }else{
                        PrivateContacts privateContacts=new PrivateContacts();
                        privateContacts.setName(baseContact.getName());
                        privateContacts.setUid(Integer.parseInt(uid+""));
                        if(privateContacts.save()){
                            PrivatePhone privatePhone=new PrivatePhone();
                            privatePhone.setPid(Integer.parseInt(privateContacts.getId().toString()));
                            privatePhone.setPhone(baseContact.getPhone());
                            return privatePhone.save();
                        }
                    }
                    return false;
                }
            });

            if(flag){
                result.setResult(true);
                result.setData(baseContact);
            }else{
                result.setResult(false);
            }
            return result;

        }
        result.setResult(false);
        return result;
    }

    private Result validateContact(BaseContact baseContact){
        Result result=new Result(true);
        Res resCn= I18n.use("zh_CN");
        if(StrKit.isBlank(baseContact.getName())){
            result.setMsg(resCn.get("basecontact.name.not.empty"));
            return result;
        }
        if(StrKit.isBlank(baseContact.getPhone())){
            result.setMsg(resCn.get("basecontact.phone.not.empty"));
            return result;
        }else if(!ValidateUtil.validatePhone(baseContact.getPhone())){
            result.setMsg(resCn.get("basecontact.phone.format.error"));
            return result;
        }
        return result;
    }

    /**
     * 获取会议备注
     * @param rid
     * @return
     */
    @Override
    public String getRecordMark(long rid) {
        Record record=Record.dao.findById(rid);
        String remark = record.getRemark();
        return remark!=null?remark:"";
    }


    /**
     * 更新会议备注
     * @param rid
     * @param text
     * @return
     */
    @Override
    public boolean updateRemark(long rid, String text) {
        Record record=Record.dao.findById(rid);
        record.setRemark(text);
        return record.update();
    }

    @Before(RemoveBillCacheInterceptor.class)
    @Override
    public void updateAfterMeetStop(HttpServletRequest request) {
        String str= HttpKit.readData(request);
        logger.info(str);
        chargingService.charge(str);

        Map<String,Object> resultMap = MeetManager.getInstance().getResult(str);
        String meetId=resultMap.get(Constant.MEETID).toString();
        String type=resultMap.get(Constant.CALLMODE).toString();
        if(meetId!=null&&Constant.CALL_MODE_MEET.equals(type)){
            Record record=Record.dao.findByMeetId(meetId);
            if(record!=null){
                //添加话单记录
//                Bill bill=new Bill();
//                bill.setRid(record.getId());
//                bill.setCaller(resultMap.get(Constant.CALLER).toString());
//                bill.setCallee(resultMap.get(Constant.CALLEE).toString());
//                bill.setCallType(Integer.parseInt(resultMap.get(Constant.CALL_TYPE).toString()));
//                bill.setCallTime(Integer.parseInt(resultMap.get(Constant.CALL_TIME).toString()));
//                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                try {
//                    bill.setCallBeginTime(sdf.parse(resultMap.get(Constant.CALL_BEGIN_TIME).toString()));
//                    bill.setCallAnswerTime(sdf.parse(resultMap.get(Constant.CALL_ANSWER_TIME).toString()));
//                    bill.setHangupTime(sdf.parse(resultMap.get(Constant.HANGUP_TIME).toString()));
//                } catch (ParseException e) {
//                    logger.error(e.getMessage(),e);
//                }
//                bill.setCallMode(Integer.parseInt(resultMap.get(Constant.CALL_MODE).toString()));
//                bill.setAnswer(Integer.parseInt(resultMap.get(Constant.IS_ANSWER).toString()));
//                bill.setGmtCreate(new Date());
//                //计算费用
//                setFee(bill,record.getBelong());
//                bill.save();
                //将会议录音地址更新到会议记录中
                if(record.getRec()==null){
                    Object rec=resultMap.get(Constant.REC);
                    record.set("endTime",new Date()).set("status",MeetState.FINSHED.getStateCode());
                    if(rec!=null){
                        record.setRec(rec.toString());
                    }
                    record.update();
                }

                Integer oid=record.getOid();
                if(oid!=null){
                    OrderMeet orderMeet=OrderMeet.dao.findById(oid);
                    //如果当前会议记录是预约会议创建的，清空rid字段
                    if(orderMeet!=null){
                        orderMeet.setRid(null);
                        orderMeet.setGmtModified(new Date());
                        orderMeet.update();
                    }
                }

                Long rid=record.getId();
                String callee=resultMap.get(Constant.CALLEE).toString();
                Attendee attendee=new Attendee();
                attendee.set("phone",callee).set("rid",Integer.parseInt(rid.toString())).set("type",AttendeeType.ATTENDEE.getCode())
                        .set("status",Constant.CALL_STATUS_BYE);
                long uid=record.getBelong();
                User user=User.dao.findById(uid);
                String name=getName(callee,user.getCid(),uid);
                attendee.set("name",name);
                Attendee.dao.insert(attendee);
                //删除缓存中参会人数据
                clearAttendeeData(record.getId());
            }
        }
    }

    private void setFee(Bill bill,long uid){
        if(bill!=null){
            //呼出
            if(bill.getCallType().intValue()== CallTypeEnum.CALL_TYPE_CALLOUT.getCode()){
                Fee fee=Fee.dao.getCallOutFee();
                //分钟计费
                if(fee.getMode()==RateModeEnum.MINUTE.getCode()){
                    double minutes=Math.ceil(bill.getCallTime()/60.0D);
                    bill.setFee(fee.getRate().add(new BigDecimal(minutes)));
                    bill.setRate(fee.getRate().doubleValue()+"元/分钟");
                }
            }else{//呼入
                //获取账户计费模式
                User user=User.dao.findById(uid);
                AccountBalance accountBalance = AccountBalance.dao.findByCompanyId(user.getCid());

                com.jfinal.plugin.activerecord.Record record=AccountBalance.dao.getAccountBalanceInfoById(accountBalance.getId(),
                        new SimpleDateFormat("yyyyMM").format(new Date()));

                List<Fee> feeList=null;
                //分钟计费
                if(record.getInt(AccountBalance.dao.CALLIN_MODE)==RateModeEnum.MINUTE.getCode()){
                    Map<String,Object> paraMap=new HashMap<String,Object>();
                    paraMap.put(Fee.dao.MODE_KEY,RateModeEnum.MINUTE.getCode());
                    paraMap.put(Fee.dao.TYPE_KEY,CallTypeEnum.CALL_TYPE_CALLIN.getCode());
                    feeList=Fee.dao.getFee(paraMap);

                    Fee fee=null;
                    for(Fee f:feeList){
                        if(f.getMode()==RateModeEnum.MINUTE.getCode()){
                            fee=f;
                            break;
                        }
                    }
                    double minutes=Math.ceil(bill.getCallTime()/60.0D);
                    bill.setFee(fee.getRate().add(new BigDecimal(minutes)));
                    bill.setRate(fee.getRate().doubleValue()+"元/分钟");
                }else{//包月计费
                    bill.setFee(new BigDecimal(0));

                    Map<String,Object> paraMap=new HashMap<String,Object>();
                    paraMap.put(Fee.dao.MODE_KEY,RateModeEnum.MONTH.getCode());
                    paraMap.put(Fee.dao.TYPE_KEY,CallTypeEnum.CALL_TYPE_CALLIN.getCode());
                    feeList=Fee.dao.getFee(paraMap);
                    StringBuilder rate=new StringBuilder(20);
                    for(Fee fee:feeList){
                        if(rate.length()>0){
                            rate.append(",");
                        }
                        rate.append(fee.getRemark()+":");
                        rate.append(fee.getRate().doubleValue()+"元/方");
                    }
                    bill.setRate(rate.toString());
                }
            }
        }
    }



    @Override
    public void getStatus(HttpServletRequest request) {
        String str=HttpKit.readData(request);
        logger.info(str);
        //解析
        MeetManager meetManager=MeetManager.getInstance();
        Map<String,Object> resultMap = meetManager.getResult(str);
        String action=resultMap.get(Constant.ACTION).toString();
        String meetId=resultMap.get(Constant.MEETID).toString();

        FixedMeet fixedMeet=FixedMeet.dao.findByMeetId(meetId);
        Date date=new Date();
        Record record=Record.dao.findByMeetId(meetId);
        switch (action){
            case Constant.ACTION_CREATE_MEET://如果是创建会则新建会议记录
                if(record==null&&fixedMeet!=null){
                    String name=fixedMeet.getHostName();
                    String phone=fixedMeet.getHostPhone();
//                    logger.info("name:"+name+",phone:"+phone);
                    record=new Record().set("subject", fixedMeet.getSubject()).set("host", phone).set("startTime", date)
                            .set("isRecord", fixedMeet.getIsRecord().equals(Constant.YES)?1:0).set("belong", fixedMeet.getBelong()).set("status", 1)
                            .set("hostPwd", fixedMeet.getHostPwd()).set("listenerPwd", fixedMeet.getListenerPwd()).set("mid", meetId)
                            .set("hostName", name).set("meetNums", meetManager.getMeetNums()).set("type", MeetType.FIXED_MEET.getCode());
                    boolean flag=record.save();
                    logger.info("新建固定会议对应的会议记录成功");
                    if(flag){
                        Attendee attendee=new Attendee();
                        attendee.set("name", name).set("phone", phone).set("rid", record.getId())
                                .set("type", AttendeeType.HOST.getCode()).set("status", Constant.WATING_CALL).save();
                    }
                }
                break;
            case Constant.ACTION_CLOSE_MEET://结束会议
                if(record!=null){
                    int type=record.getType();
                    if(MeetType.FIXED_MEET.getCode()==type&&fixedMeet!=null){//如果是固定会议
                        fixedMeet.setMeetId(null);
                        fixedMeet.setCreated(null);
                        fixedMeet.update();
                    }
                    record.setEndTime(date);
                    record.setStatus(MeetState.FINSHED.getStateCode());
                    record.update();
                }
                break;
            case Constant.ACTION_CALL_STATUS_REPORT://参会人状态变更
                //如果会议开启呼叫失败短息通知
                Object status=resultMap.get(Constant.STATUS);
                if(record!=null&&record.getNotice()==Integer.parseInt(Constant.YES)
                        &&(Constant.CALL_STATUS_FAIL.equals(status)||Constant.CALL_STATUS_REJECT.equals(status))){
                    String phone=resultMap.get(Constant.CALLER).toString();
                    String name=nameService.getNameByPhone(Long.parseLong(record.getBelong().toString()),phone);
                    messageService.sendMsg(record.getId(),phone,name);
                }
                break;
        }

    }

    @Override
    public Map<String, Object> getMeetRestartData(long recordId, User user) {
        Map<String,Object> map=new HashMap<>();
        Employee employee=Employee.dao.findById(user.getEid());
        String name=employee.getName();
        map.put("uname",name);

        Company company=Company.dao.findById(user.getCid());
        String callNum=AccessNum.dao.findById(company.getCallNum()).getNum();
        map.put("callNum",callNum);

        long uid=user.getId();
        MeetManager meetManager=MeetManager.getInstance();
        Record record=Record.dao.findById(recordId);
        String hostNum=record.getHost();
        final Record record1=new Record().set("subject",record.getSubject()).set("host", hostNum).set("startTime", new Date())
                .set("isRecord", 0).set("belong", uid).set("status", 0).set("hostPwd", meetManager.getPassword())
                .set("listenerPwd", meetManager.getPassword()).set("hostName", name).set("meetNums", meetManager.getMeetNums())
                .set("type", MeetType.NORMAL_MEET.getCode());

        List<Attendee> alist=Attendee.dao.findByRecordId(recordId);

        Set<Node> inviteData=new HashSet<>();
        String phone=null;
        for(int i=0,len=alist.size();i<len;i++){
            Attendee attendee=alist.get(i);
            String name1=attendee.getName();
            String phone1=attendee.getPhone();

            Node node=new Node();
            node.setId(attendee.getId());
            node.setName(name1);
            node.setPhone(phone1);
            node.setStatus(Constant.WATING_CALL);
            node.setHost(false);
            node.setRole(AttendeeType.ATTENDEE.getCode());
            if(hostNum.equals(phone1)){//如果是主持人
                node.setRole(AttendeeType.HOST.getCode());
            }
            inviteData.add(node);
        }
        boolean result=record1.save();
        if(!result){
            throw new RuntimeException("会议重新开始失败！要重新开的会议记录id："+recordId);
        }
        map.put("hostNum", phone);
        map.put("subject", record.getSubject());
        map.put("record", record1);
        map.put("invite",inviteData);
        map.put("inviteData", JsonKit.toJson(inviteData));
        //将邀请人信息添加缓存
        addAttendeeData(inviteData,record1.getId());
        return map;
    }

    private int getCount(int[] result){
        int count=0;
        if(result!=null){
            for(int i=0;i<result.length;i++){
                count+=result[i];
            }
        }
        return count;
    }

    /**
     * 创建固定会议
     * @param id
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> createFixedMeet(long id, User user) {
        Map<String,Object> map=new HashMap<>();
        final FixedMeet fixedMeet=FixedMeet.dao.findById(id);

        if(fixedMeet!=null){
            String meetId=fixedMeet.getMeetId();
            MeetManager meetManager=MeetManager.getInstance();

            List<Node> inviteData=new ArrayList<>();
            List<Node> inMeetData=new ArrayList<>();

            String hostNum=fixedMeet.getHostPhone();
            String hostName=fixedMeet.getHostName();

            map.put("uname",hostName);
            String callNum=fixedMeet.getCallNum();
            map.put("callNum",callNum);

            long uid=user.getId();
            long cid=user.getCid();

            Record record=null;
            if(!StrKit.isBlank(meetId)){//会议已创建
                record=Record.dao.findByMeetId(meetId);
                List<Node> attendeeList=null;
                if(record==null){//会议已开始未创建会议记录
                    attendeeList=getAttendeeList(meetId,hostNum,cid,uid,null,null);
                    record=new Record().set("subject", fixedMeet.getSubject()).set("host", hostNum)
                            .set("startTime", new Date()).set("isRecord", fixedMeet.getIsRecord().equals(Constant.YES)?1:0)
                            .set("belong", uid).set("status", MeetState.STARTED.getStateCode()).set("hostPwd", fixedMeet.getHostPwd()).set("listenerPwd", fixedMeet.getListenerPwd())
                            .set("mid", fixedMeet.getMeetId()).set("hostName", fixedMeet.getHostName()).set("meetNums", meetManager.getMeetNums())
                            .set("type", MeetType.FIXED_MEET.getCode()).set("oid",fixedMeet.getId());

                    record.save();
                    //将参会人信息放进缓存
                    RedisUtil redisUtil=RedisUtil.getInstance();
                    String key=ATTENDEE_REDIS_KEY_PREFIX+":"+record.getId();
//                    redisUtil.del(key);
                    redisUtil.set(key,new HashSet<Node>(attendeeList));
                }else{
                    attendeeList=getAttendeeList(meetId,hostNum,cid,uid,record.getId());
                }
                getMeetAttendee(attendeeList,inviteData,inMeetData);
            }else{//会议还未创建
                String hostPwd=fixedMeet.getHostPwd();
                String listenerPwd=fixedMeet.getListenerPwd();

                Map<String,Object> dataMap=new HashMap<>();
                dataMap.put(Constant.CHAIRMANPWD,hostPwd);
                dataMap.put(Constant.AUDIENCEPWD,listenerPwd);
                dataMap.put(Constant.CALLER,hostNum);
                dataMap.put(Constant.NAME,hostName);
                dataMap.put(Constant.COSTNUM,fixedMeet.getCallNum());
                dataMap.put(Constant.SHOWNUM,fixedMeet.getShowNum());
                //创建呼入式会议
                Map<String,Object> resultMap=meetManager.createMeet(dataMap);
                if(Constant.SUCCESS.equals(resultMap.get(Constant.STATUS).toString())){
                    meetId=resultMap.get(Constant.CONTENT).toString();
                    final String mid=meetId;
                    //是否录音
                    boolean isRecord=fixedMeet.getIsRecord().equals(Constant.YES)?true:false;
                    if(isRecord){
                        resultMap = meetManager.startRecord(meetId);
                        if(!Constant.SUCCESS.equals(resultMap.get(Constant.STATUS))){
                            logger.error("开始录音失败："+resultMap.get(Constant.CONTENT).toString());
                        }
                    }

                    record=new Record();
                    record.set("belong", uid).set("subject", fixedMeet.getSubject()).set("startTime", new Date()).set("host", hostNum)
                            .set("isRecord", fixedMeet.getIsRecord().equals("3000")?1:0).set("hostPwd", hostPwd).set("listenerPwd", listenerPwd)
                            .set("mid", meetId).set("type", MeetType.FIXED_MEET.getCode()).set("status", MeetState.GOINGON.getStateCode())
                            .set("meetNums", meetManager.getMeetNums()).set("hostName", hostName);

                    final Attendee attendee=new Attendee().set("name", hostName).set("phone", hostNum).set("type", AttendeeType.HOST.getCode())
                            .set("status", Constant.WATING_CALL);
                    Node node=new Node();
                    node.setName(hostName);
                    node.setStatus(Constant.WATING_CALL);
                    node.setPhone(hostNum);
                    node.setHost(true);
                    node.setRole(AttendeeType.HOST.getCode());

                    final Record finalRecord = record;
                    boolean flag=Db.tx(new IAtom() {
                        @Override
                        public boolean run() throws SQLException {
                            if(finalRecord.save()){
                                attendee.setRid(Integer.parseInt(finalRecord.getId().toString()));
                                return fixedMeet.set("meetId",mid).update();
                            }
                            return false;
                        }
                    });

                    if(flag){
                        node.setId(attendee.getId());
                        inviteData.add(node);
                        addAttendeeData(node,record.getId());
                    }else{
                        logger.error("创建固定会议失败！");
                    }

                }else{//创建会议失败
                    logger.error("会议创建失败："+resultMap.get(Constant.CONTENT).toString());
                }
            }
            map.put("hostNum", hostNum);
            map.put("record", record);
            map.put("invite",inviteData);
            map.put("inviteData", JsonKit.toJson(inviteData));
            map.put("inMeetData", JsonKit.toJson(inMeetData));
        }
        return map;
    }

    @Override
    public boolean updateRecordState(Integer rid) {
        if(rid!=null){
            Record record = Record.dao.findById(rid);
            if(record!=null){
                record.setIsRecord(1);
                return record.update();
            }
        }
        return false;
    }

    @Override
    public Result setHost(Controller controller) {
        String meetId=controller.getPara("meetId");
        Long rid=controller.getParaToLong("rid");
        String phone=controller.getPara("phone");
        String hostNum=Record.dao.findById(rid).getHost();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        List<Node> nodeList=getAttendeeList(meetId,hostNum,user.getCid(),user.getId(),rid);
        boolean flag=false;
        for(Node node:nodeList){
            if(!hostNum.equals(node.getPhone())&&node.getPhone().equals(phone)){
                flag=true;
                node.setHost(true);
                node.setRole(AttendeeType.HOST.getCode());
//                if(node.getPhone().equals(phone)){
//                    flag=true;
//                    node.setHost(true);
//                    node.setRole(AttendeeType.HOST.getCode());
//                }else{
//                    node.setHost(false);
//                    node.setRole(AttendeeType.ATTENDEE.getCode());
//                }
            }
        }
        if(flag){
            RedisUtil redisUtil=RedisUtil.getInstance();
            String key=ATTENDEE_REDIS_KEY_PREFIX+":"+rid;
//            redisUtil.del(key);
            redisUtil.set(key,new HashSet<Node>(nodeList));
        }
        return new Result(flag);
    }
}
