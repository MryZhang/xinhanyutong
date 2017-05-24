package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.ChangePwd;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.dto.UserData;
import com.dbkj.meet.model.Department;
import com.jfinal.core.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/9.
 */
public interface IUserService {

    String KEY_MAP="keyMap";

    /**
     * 根据用户名判断用户是否存在
     * @param username
     * @return
     */
    boolean isExistUser(String username);


    /**
     * 通过公司id获取该公司下的所有部门信息
     * @param cid
     * @return
     */
    List<Department> getDepartments(long cid);

    Map<String,Object> getUserPageData(Controller controller);

    boolean addUserData(UserData userData,int cid,HttpServletRequest request);

    /**
     * 根据用户id获取用户相关信息
     * @param uid
     * @return
     */
    UserData getUserData(long uid);

    /**
     * 修改用户信息
     * @param userData
     * @return
     */
    boolean updateUserData(UserData userData);

    boolean deleteUsers(String idStr);

    Result<?> changePwd(ChangePwd changePwd,String username,HttpServletRequest request);

    void setPageData(HttpServletRequest request);

    /**
     * 修改个人资料
     * @param userData
     * @return
     */
    Result<?> updateSelf(UserData userData, HttpServletRequest request);
}
