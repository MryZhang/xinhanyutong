package com.dbkj.meet.services.inter;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/25.
 */
public interface IMeetingRecordService {

    Map<String,Object> getRecordPageData(Controller controller);

    Map<String,Object> getRecordDetail(long id);
}
