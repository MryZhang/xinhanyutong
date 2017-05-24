package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.ChangePwd;
import com.dbkj.meet.model.Company;
import com.dbkj.meet.vo.AdminUserVo;
import com.jfinal.plugin.activerecord.Record;
import com.dbkj.meet.model.User;
import com.jfinal.plugin.activerecord.Page;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/8.
 */
public interface IAdminService {

    String KEY_MAP="keyMap";

    List<Company> getCompanys();

    Page<Record> getUserPage(Map<String,Object> map);

    Page<Record>  getUserPage();

    boolean addUser(AdminUserVo userVo, Map<String,Key> keyMap);

    User getUserById(long id);

    boolean updateUser(AdminUserVo userVo, Map<String,Key> keyMap);

    boolean isExistUser(String username);

    boolean deleteUsers(String idStr);

    /**
     * 验证用户密码是否正确
     * @param password
     * @param username
     * @return
     */
    boolean validatePassword(String username,String password);

    /**
     * 修改密码
     * @param id
     * @param changePwd
     * @return
     */
    boolean updatePassword(long id, ChangePwd changePwd, HttpServletRequest request);

    Map<String,Object> getParameterMap(Map<String,String[]> map);

    void setPageData(HttpServletRequest request);
}
