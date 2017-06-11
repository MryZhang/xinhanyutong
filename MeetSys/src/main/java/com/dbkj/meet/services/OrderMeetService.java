package com.dbkj.meet.services;

import com.dbkj.meet.dic.*;
import com.dbkj.meet.interceptors.RemoveBillCacheInterceptor;
import com.dbkj.meet.services.common.MeetManager;
import com.dbkj.meet.services.inter.ISMTPService;
import com.dbkj.meet.services.ordermeet.*;
import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.ChildrenNode;
import com.dbkj.meet.dto.OrderModel;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.IOrderMeetService;
import com.dbkj.meet.services.inter.MessageService;
import com.dbkj.meet.utils.DateUtil;
import com.dbkj.meet.utils.RandomUtil;
import com.dbkj.meet.utils.ScheduleHelper;
import com.dbkj.meet.utils.ValidateUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.*;
import com.jfinal.plugin.activerecord.Record;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by MrQin on 2016/11/11.
 */
public class OrderMeetService implements IOrderMeetService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final String dateTimeFormat="yyyy-MM-dd HH:mm";
    private final String timeFormat="HH:mm";

    private final int NO=0;
    private final int YES=1;

    private MessageService messageService=new MessageServiceImpl();
    private ISMTPService smtpService=new SMTPServiceImpl();


    public Map<String, Object> getRenderData(Controller controller) {
        Map<String,Object> map=new HashMap<String, Object>();
        User user=controller.getSessionAttr(Constant.USER_KEY);
        Employee employee=Employee.dao.findById(user.getEid());
        map.put("name",employee.getName());
        map.put("phone",employee.getPhone());
        map.put("now",new Date());
        map.put("hostPwd",getPassword());
        map.put("listenerPwd",getPassword());
        return map;
    }

    /**
     * 生成不重复的4位会议密码
     * @return
     */
    public String getPassword() {
        String password=null;
        //循环生成密码直到没有
        do {
            password=RandomUtil.getPwd(4);
        }while (MeetManager.getInstance().isUsedPassword(password));
        return password;
    }

    private List<ChildrenNode> getChildrenNode(List<com.jfinal.plugin.activerecord.Record> list){
        List<ChildrenNode> childrenNodes=new ArrayList<ChildrenNode>();
        for(int n=0,size=list.size();n<size;n++){
            com.jfinal.plugin.activerecord.Record record=list.get(n);

            StringBuilder name=new StringBuilder(50);
            name.append(record.getStr("name"));
            name.append("(");
            name.append(record.getStr("phone"));
            name.append(")");
            ChildrenNode childrenNode=new ChildrenNode(record.getLong("id"),name.toString(),record.getStr("phone"));
            childrenNodes.add(childrenNode);
        }
        return childrenNodes;
    }

    /**
     * 获取联系人信息
     * @param search 搜索内容
     * @param user
     * @return
     */
    public List<BaseNode> getContacts(String search, User user) {
        //将查询条件封装为map
        Map<String,Object> map=new HashMap<String, Object>(2);

        List<BaseNode> root=new ArrayList<BaseNode>();//根节点
        BaseNode<BaseNode> node1=new BaseNode<BaseNode>();
        node1.setId(1);
        node1.setName("公共联系人");

        //获取公共联系人下的子节点
        List<BaseNode> publicContacts=new ArrayList<BaseNode>();

        //获取公共联系人信息
        //获取所有的部门
        List<Department> departmentList=Department.dao.findByCompanyId(user.getCid());
        for(int i=0,length=departmentList.size();i<length;i++){
            Department department=departmentList.get(i);
            BaseNode<ChildrenNode> baseNode=new BaseNode<ChildrenNode>();
            baseNode.setId(department.getId());
            baseNode.setName(department.getName());
            //封装查询条件
            if(!StrKit.isBlank(search)){
                map.put("a.name",search);
            }
            if(user!=null){
                map.put("b.did",department.getId());
            }

            //获取该部门下的子节点
            List<ChildrenNode> childrenNodes=new ArrayList<ChildrenNode>();
            //获取该部门下的所有联系人
            List<com.jfinal.plugin.activerecord.Record> list=PublicContacts.dao.getContacts(map);
            childrenNodes.addAll(getChildrenNode(list));
            baseNode.setChildren(childrenNodes);

            publicContacts.add(baseNode);
        }
        node1.setChildren(publicContacts);
        root.add(node1);

        //个人联系人
        BaseNode<BaseNode> node2=new BaseNode<BaseNode>();
        node2.setId(2);
        node2.setName("个人联系人");

        //获取个人联系人下所有子节点
        List<BaseNode> privateContacts=new ArrayList<BaseNode>();

        //获取个人联系人信息
        //获取所有分组信息
        List<Group> groups=Group.dao.findByUserId(user.getId());
        groups.add(new Group().set("id",0L).set("name","未分组"));
        for(int i=0,length=groups.size();i<length;i++){
            Group group=groups.get(i);
            BaseNode<ChildrenNode> baseNode=new BaseNode<ChildrenNode>();
            baseNode.setId(group.getId());
            baseNode.setName(group.getName());

            //封装查询条件
            map.clear();
            if(!StrKit.isBlank(search)){
                map.put("a.name",search);
            }
            if(user!=null){
                map.put("b.gid",group.getId());
                map.put("a.uid",user.getId());
            }
            List<ChildrenNode> childrenNodeList=new ArrayList<ChildrenNode>();
            List<com.jfinal.plugin.activerecord.Record> nodes=PrivateContacts.dao.getContacts(map);
            childrenNodeList.addAll(getChildrenNode(nodes));
            baseNode.setChildren(childrenNodeList);

            privateContacts.add(baseNode);
        }
        node2.setChildren(privateContacts);
        root.add(node2);

        return root;
    }

    /**
     * 创建预约会议
     * @param controller
     * @return
     */
    public Result<?> createOrderMeet(Controller controller) {
        Result<?> result=null;
        final OrderModel orderModel=controller.getBean(OrderModel.class,"order");
        result=validate(orderModel);
        if(result.getResult()){//验证通过
            final User user=controller.getSessionAttr(Constant.USER_KEY);
            //获取公司呼叫号码
            Company company=Company.dao.findById(user.getCid());
            String callNum=AccessNum.dao.findById(company.getCallNum()).getNum();
            String showNum=AccessNum.dao.findById(company.getShowNum()).getNum();

            Date now=new Date();//当前系统时间
            if(orderModel.getPeriod()==RepeatType.FIEXD.getCode()){//固定会议
//                String transferNum = PropKit.use("config.properties").get(Constant.TRANSFER_NUM);
                FixedMeet fixedMeet=new FixedMeet();
                fixedMeet.setSubject(orderModel.getSubject());
                fixedMeet.setHostName(orderModel.getHostName());
                fixedMeet.setHostPhone(orderModel.getHostNum());
                fixedMeet.setHostPwd(orderModel.getHostPwd());
                fixedMeet.setListenerPwd(orderModel.getListenerPwd());
                fixedMeet.setBelong(Integer.parseInt(user.getId().toString()));
                fixedMeet.setCallNum(callNum);
//                fixedMeet.setMeetNum(callNum);
                fixedMeet.setShowNum(showNum);
                fixedMeet.setCreateTime(now);
                if(orderModel.getIsRecord()==NO){
                    fixedMeet.setIsRecord(Constant.NO);
                }else{
                    fixedMeet.setIsRecord(Constant.YES);
                }
                boolean flag = fixedMeet.save();
                result.setResult(flag);
            }else{//预约会议
                BaseOrderMeetType orderMeetType=null;
                if(orderModel.getPeriod()==RepeatType.NONE.getCode()){//无重复的预约会议类型
                    orderMeetType=new NoRepeateOrderMeet();
                }else if(orderModel.getPeriod()==RepeatType.DAY.getCode()){//重复周期为天的预约会议
                    orderMeetType=new RepeatOfDayOrderMeet();
                }else if(orderModel.getPeriod()==RepeatType.WEEK.getCode()){//重复周期为星期的预约会议类型
                    orderMeetType=new RepeatOfWeekOrderMeet();
                }else if(orderModel.getPeriod()==RepeatType.MONTH.getCode()) {//重复周期为月的预约会议类型
                    orderMeetType=new RepeatOfMonthOrderMeet();
                }
                if(orderMeetType!=null){
                    orderMeetType.setCallNum(callNum);
                    orderMeetType.setShowNum(showNum);
                    orderMeetType.setOrderModel(orderModel);
                    orderMeetType.setUser(user);
                    orderMeetType.setNow(new Date());

                    try {
                        //添加
                        OrderMeetContext context=new OrderMeetContext();
                        context.setOrderMeetType(orderMeetType);
                        context.handle();
                    }catch (Exception e){
                        logger.error(e.getMessage(),e);
                        result.setResult(false);
                        result.setMsg("服务器内部错误！");
                    }
                }else{
                    result.setResult(false);
                }
            }
        }
        return result;
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
     * 验证数据
     * @param orderModel
     * @return
     */
    private Result<?> validate(OrderModel orderModel){
        Res res= I18n.use("zh_CN");
        if(StrKit.isBlank(orderModel.getSubject())){
            return new Result(false,res.get("subject.not.empty"));
        }
        if(StrKit.isBlank(orderModel.getHostNum())){
            return new Result(false,res.get("hostNum.not.empty"));
        }else if (!ValidateUtil.validatePhone(orderModel.getHostNum())){
            return new Result(false,res.get("hostNum.format.wrong"));
        }

        //判断会议密码是否正确
        if(StrKit.isBlank(orderModel.getHostPwd())||!ValidateUtil.validate4DigitalPassword(orderModel.getHostPwd())){
            return new Result(false,res.get("password.format.wrong"));
        }
        MeetManager meetManager=MeetManager.getInstance();
        if(meetManager.isUsedPassword(orderModel.getHostPwd())){
            return new Result(false,res.get("password.repeat"));
        }

        int type=orderModel.getPeriod();
        if(type==RepeatType.NONE.getCode()){//无重复周期
            if(!validateDateTime(orderModel.getStartTime())){
                return new Result(false,res.get("date.format.wrong"));
            }
        }else if(type==RepeatType.DAY.getCode()){//重复周期为天
            if(!validateTime(orderModel.getStartTime())){
                return new Result(false,res.get("time.format.wrong"));
            }
            if(!ScheduleHelper.WORKDAY.equals(orderModel.getInterval())){//如果重复周期为非工作日
                if(!ValidateUtil.isNum(orderModel.getInterval())){
                    return new Result(false,res.get("interval.format.wrong"));
                }
                int interval=Integer.parseInt(orderModel.getInterval());
                if(DateUtil.isLeapYear(new Date())){//当前年为闰年
                    if(interval>366){
                        return new Result(false,res.get("interval.format.wrong"));
                    }
                }else{
                    if(interval>365){
                        return new Result(false,res.get("interval.format.wrong"));
                    }
                }
            }

        }else if(type==RepeatType.WEEK.getCode()){//重复周期为星期
            if(!validateTime(orderModel.getStartTime())){
                return new Result(false,res.get("time.format.wrong"));
            }
            if(!ValidateUtil.isNum(orderModel.getInterval())){
                return new Result(false,res.get("interval.format.wrong"));
            }
            if(!isWeekday(orderModel.getOrderNum())){
                return new Result(false,res.get("date.not.select"));
            }

        }else if(type==RepeatType.MONTH.getCode()){//重复周期为月
            if(!validateTime(orderModel.getStartTime())){
                return new Result(false,res.get("time.format.wrong"));
            }
            if(StrKit.isBlank(orderModel.getOrderNum())){//重复周期为间隔多少天
                if(!ValidateUtil.isNum(orderModel.getInterval())){
                    return new Result(false,res.get("interval.format.wrong"));
                }
                if(Integer.parseInt(orderModel.getInterval())>31){
                    return new Result(false,res.get("interval.format.wrong"));
                }
            }else{//重复周期为每个月的第几个星期几
                if(!validateWeekNum(orderModel.getOrderNum())){
                    return new Result(false,res.get("data.format.wrong"));
                }
                if(!validateWeekday(orderModel.getWeekday())){
                    return new Result(false,res.get("data.format.wrong"));
                }
            }
        }else if(orderModel.getPeriod()==RepeatType.FIEXD.getCode()){//固定会议

        }else{
            return new Result(false,res.get("data.format.wrong"));
        }

        return new Result(true);
    }

    private boolean validateWeekNum(String weekNum){
        String[] array={"1","2","3","4","L"};
        return Arrays.asList(array).contains(weekNum);
    }

    private boolean validateWeekday(String weekday){
        String[] array={"1","2","3","4","5","6","7"};
        return Arrays.asList(array).contains(weekday);
    }

    private boolean isWeekday(String str){
        if(!StrKit.isBlank(str)){
            String[] arr=str.split(",");
            String[] weekdays=new String[]{
                    ScheduleHelper.MONDAY,ScheduleHelper.TUESDAY,
                    ScheduleHelper.WESDNESDAY,ScheduleHelper.THURSDAY,
                    ScheduleHelper.FRIDAY,ScheduleHelper.SATURDAY,
                    ScheduleHelper.SUNDAY
            };
            for(int i=0,len=arr.length;i<len;i++){
                boolean result =  Arrays.asList(weekdays).contains(arr[i]);
                if(!result){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean validateDateTime(String dateTime){
        SimpleDateFormat sdf=new SimpleDateFormat(dateTimeFormat);
        try {
            sdf.parse(dateTime);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private boolean validateTime(String time){
        SimpleDateFormat sdf=new SimpleDateFormat(timeFormat);
        try {
            sdf.parse(time);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /**
     * 取消预约会议
     * @param oid
     * @param type
     * @return
     */
    @Before({RemoveBillCacheInterceptor.class})
    @Override
    public boolean cancelMeet(final long oid, int type) {
        boolean result=false;
        if (MeetType.FIXED_MEET.getCode()==type) {//固定会议
            result=FixedMeet.dao.deleteById(oid);
        }else if(MeetType.ORDER_MEET.getCode()==type){//预约会议
            result=Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    removeScheduleTask(oid);
                    if(OrderMeet.dao.deleteById(oid)){
                        if(Schedule.dao.deleteByOrderMeetId(oid)>0){
                            com.dbkj.meet.model.Record.dao.updateRecordStatus(oid,MeetState.FINSHED);
                            return OrderAttendee.dao.deleteByOrderMeetId(oid)>0;
                        }
                    }
                    return false;
                }
            });

        }
        return result;
    }

    private void removeScheduleTask(Long oid){
        Map<String,Object> params=new HashMap<>();
        params.put("oid",oid);
        List<Schedule> scheduleList=Schedule.dao.getScheduleList(params);
        Iterator<Schedule> itr=scheduleList.iterator();
        OrderMeet orderMeet=OrderMeet.dao.findById(oid);
        //将定时器中的任务计划删除
        while(itr.hasNext()){
            Schedule sch=itr.next();
            ScheduleHelper.removeJob(sch.getJobName(), sch.getJobGroup());
            if(orderMeet.getSmsRemind()==Integer.parseInt(Constant.YES)){
                ScheduleHelper.removeJob(sch.getJobName()+"_remind",sch.getJobGroup());
            }
        }
    }

    @Override
    public Map<String, Object> getShowUpdatePageData(Long id, Integer type) {
        Map<String,Object> map=new HashMap<>();
        if(type==MeetType.ORDER_MEET.getCode()){//预约会议
            OrderMeet orderMeet=OrderMeet.dao.findById(id);
            map.put("order",orderMeet);

            Map<String,Object> params=new HashMap<>();
            params.put("oid",id);
            List<Schedule> scheduleList=Schedule.dao.getScheduleList(params);
            String jsonStr= JsonKit.toJson(scheduleList);
            map.put("type",jsonStr);

            params.put("type",AttendeeType.ATTENDEE.getCode());
            List<OrderAttendee> orderAttendeeList=OrderAttendee.dao.getOrderAttendeeList(params);

            List<Record> attendeeList=new ArrayList<>();
            for(int i=0,size=orderAttendeeList.size();i<size;i++){
                OrderAttendee orderAttendee=orderAttendeeList.get(i);
                Record record=new Record();
                record.set("name", orderAttendee.getName());
                record.set("phone", orderAttendee.getPhone());
                record.set("typeid", orderAttendee.getType());
                record.set("typename", AttendeeType.ATTENDEE.getText());
                attendeeList.add(record);
            }
            String attendeeJson=JsonKit.toJson(attendeeList);
            map.put("attendee",attendeeJson);
        }else{//固定会议
            FixedMeet fixedMeet=FixedMeet.dao.findById(id);
            Record record=new Record();
            record.set("id", fixedMeet.getId());
            record.set("subject", fixedMeet.getSubject());
            record.set("hostNum", fixedMeet.getHostPhone());
            record.set("hostName", fixedMeet.getHostName());
            record.set("hostPwd", fixedMeet.getHostPwd());
            record.set("listenerPwd", fixedMeet.getListenerPwd());
            record.set("isRecord", fixedMeet.getIsRecord().equals(Constant.YES)?1:0);
            map.put("order", record);
        }
        return map;
    }

}
