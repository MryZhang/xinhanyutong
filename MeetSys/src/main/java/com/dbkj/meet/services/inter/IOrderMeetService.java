package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.BaseNode;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.User;
import com.jfinal.core.Controller;

import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/7.
 */
public interface IOrderMeetService {

    /**
     * 获取要渲染的数据
     * @param controller
     * @return
     */
    Map<String,Object> getRenderData(Controller controller);

    /**
     * 生成不重复的会议密码
     * @return
     */
    String getPassword();

    /**
     * 根据条件获取个人联系人数据和公共联系人数据
     * @param search 搜索内容
     * @param uid 用户id
     * @return
     */
    List<BaseNode> getContacts(String search, User user);


    Result<?> createOrderMeet(Controller controller);

    /**
     * 取消会议
     * @param oid
     * @param type
     * @return
     */
    boolean cancelMeet(long oid,int type);

    Map<String,Object> getShowUpdatePageData(Long id,Integer type);
}
