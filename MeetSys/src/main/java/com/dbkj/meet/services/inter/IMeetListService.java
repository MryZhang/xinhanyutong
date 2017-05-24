package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.MeetListItem;
import com.jfinal.core.Controller;

import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/7.
 */
public interface IMeetListService {

    /**
     * 通话用户id获取用户的正在进行中的会议列表
     * @param uid
     * @return
     */
    List<MeetListItem> getMeetList(Long uid);

    /**
     * 通过用户id获取用户未开始的会议列表
     * @param uid
     * @return
     */
    List<MeetListItem> getNotStartMeetList(Long uid);

    Map<String,Object> getMeetDetail(Controller controller);
}
