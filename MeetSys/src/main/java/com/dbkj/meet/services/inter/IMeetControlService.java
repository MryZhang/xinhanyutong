package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.BaseContact;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.User;
import com.jfinal.core.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 会议控制的相关业务逻辑
 * Created by MrQin on 2016/11/25.
 */
public interface IMeetControlService {

    /**
     * 获取会议的相关数据
     * @param uid
     * @param id
     * @return
     */
    Map<String,Object> getMeetData(User user, Integer id);

    Result addAttendee(Controller controller);

    boolean removeAttendee(Controller controller);

    Result<Map<String,Object>> creeteMeet(Controller controller);

    /**
     * 会议结束，更新会议和参会人状态
     * @param recordId
     * @return
     */
    boolean updateMeetStatus(long recordId);

    String getRecordMark(long rid);

    boolean updateRemark(long rid,String text);

    /**
     * 会议结束后获取通报接口发送过来的消息更新会议记录
     * @param request
     */
    void updateAfterMeetStop(HttpServletRequest request);

    void getStatus(HttpServletRequest request);

    /**
     * 会议重新开始
     * @param recordId
     * @param uid
     * @param cid
     * @return
     */
    Map<String,Object> getMeetRestartData(long recordId,User user);

    Map<String,Object> createFixedMeet(long id,User user);

    boolean updateRecordState(Integer rid);

    boolean cancelMeet(Long rid);

    /**
     * 添加个人联系人
     * @param baseContact
     * @param uid
     * @return
     */
    Result addPersonalContact(BaseContact baseContact,long uid);

    /**
     * 设置第二主持人
     * @param rid
     * @param phone
     * @return
     */
    Result setHost(Controller controller);
}
