package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.dto.ChangePwd;
import com.dbkj.meet.dto.Result;
import com.dbkj.meet.dto.UserData;
import com.dbkj.meet.model.*;
import com.dbkj.meet.services.inter.IUserService;
import com.dbkj.meet.utils.AESUtil;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.ValidateUtil;
import com.jfinal.core.Controller;
import com.jfinal.i18n.I18n;
import com.jfinal.i18n.Res;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Key;
import java.security.PrivateKey;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/9.
 */
public class UserService implements IUserService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int PAGE_SIZE=15;

    /**
     * 根据用户名判断用户是否存在
     * @param username
     * @return
     */
    public boolean isExistUser(String username) {
        return User.dao.findUserByUsername(username)!=null;
    }

    public List<Department> getDepartments(long cid) {
        return Department.dao.findByCompanyId(cid);
    }

    /**
     * 获取分页数据
     * @param controller
     * @return
     */
    public Map<String, Object> getUserPageData(Controller controller) {
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
        params.put("a.aid", UserType.NORMAL_USER.getTypeCode());
        params.put("a.cid",user.getCid());

        Map<String,String[]> paraMap = controller.getParaMap();
        String[] indexPara=paraMap.get("pageIndex");
        if(indexPara!=null){//当前页码
            params.put(Constant.CURRENT_PAGE_KEY,indexPara[0]);
        }
        String[] departIdPara=paraMap.get("departId");
        if(departIdPara!=null){//部门id
            String departId=departIdPara[0];
            if(!StrKit.isBlank(departId)){
                params.put("c.did",departId);
                map.put("departId",departId);
            }
        }
        String[] searchPara=paraMap.get("search");
        if(searchPara!=null){//搜索内容
            params.put("a.username",searchPara[0]);
            map.put("search",searchPara[0]);
        }

        Page<Record> page=User.dao.getUserPage(params);
        List<Record> userData=page.getList();

        Iterator<Record> itr=userData.iterator();
        while(itr.hasNext()){
            Record record=itr.next();
            try {
                record.set("password", AESUtil.decrypt(record.getStr("password"),Constant.ENCRYPT_KEY));
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            //获取部门
//            int did=record.getInt("did");
//            record.set("dname",Department.dao.findById(did).getName());
        }
        map.put("totalPage", page.getTotalPage());
        map.put("currentPage", page.getPageNumber());
        map.put("pageSize", page.getPageSize());
        map.put("totalRow", page.getTotalRow());
        map.put("users", userData);
        return map;
    }

    public List<Department> getDeparments(long cid) {
        return Department.dao.findByCompanyId(cid);
    }

    public boolean addUserData(final UserData userData, final int cid,HttpServletRequest request) {
        if(userData!=null){
            final User user=new User();
            user.setUsername(userData.getUsername());
            try {
                user.setPassword(AESUtil.encrypt(userData.getPassword(),Constant.ENCRYPT_KEY));
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            user.setCid(cid);
            user.setAid(UserType.NORMAL_USER.getTypeCode());

            final Employee employee=new Employee();
            employee.setName(userData.getName());
            employee.setPhone(userData.getUsername());
            employee.setDid(userData.getDid());
            employee.setEmail(userData.getEmail());
            employee.setPosition(userData.getPosition());

            return Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    if(employee.save()){
                        user.setEid(Integer.parseInt(employee.getId().toString()));
                        if(user.save()){
                            //账号添加成功后，将该账号的联系人方式添加到公共联系人中
                            PublicContacts publicContacts=new PublicContacts();
                            publicContacts.setName(userData.getName());
                            publicContacts.setCid(cid);
                            publicContacts.setPosition(userData.getPosition());
                            if(publicContacts.save()){
                                PublicPhone publicPhone=new PublicPhone();
                                publicPhone.setPhone(userData.getUsername());
                                publicPhone.setPid(Integer.parseInt(publicContacts.getId().toString()));
                                publicPhone.setDid(userData.getDid());
                                return publicPhone.save();
                            }
                        }
                    }
                    return false;
                }
            });
        }
        return false;
    }

    /**
     * 根据用户id获取用户相关信息
     * @param uid
     * @return
     */
    public UserData getUserData(long uid) {
        User user=User.dao.findById(uid);
        Employee employee = Employee.dao.findById(user.getEid());
        UserData userData=new UserData();
        if(user!=null){
            userData.setId(user.getId());
            userData.setUsername(user.getUsername());
            String password=user.getPassword();
            try {
                password=AESUtil.decrypt(password,Constant.ENCRYPT_KEY);
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            userData.setPassword(password);
            userData.setConfirmPwd(password);
        }

        if(employee!=null){
            userData.setEid(employee.getId());
            userData.setName(employee.getName());
            userData.setDid(employee.getDid());
            userData.setEmail(employee.getEmail());
            userData.setPosition(employee.getPosition());
        }
        return userData;
    }

    /**
     * 修改用户信息
     * @param userData
     * @return
     */
    public boolean updateUserData(UserData userData) {
        return updateUser(userData);
    }

    /**
     * 更新用户资料
     * @param userData
     * @return
     */
    private boolean updateUser(UserData userData){
        if(userData!=null){
            final User user=User.dao.findById(userData.getId());
//            Map<String,Key> keyMap= (Map<String, Key>) request.getSession().getAttribute(UserService.KEY_MAP);
//            Key privateKey=keyMap.get(RSAUtil2.PRIVATE_KEY);
//            String decryptPwd=RSAUtil2.decryptBase64(userData.getEncryptPwd(),privateKey);
            try {
                user.setPassword(AESUtil.encrypt(userData.getPassword(),Constant.ENCRYPT_KEY));
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            final Employee employee=Employee.dao.findById(user.getEid());
            employee.setEmail(userData.getEmail());
            if(userData.getName()!=null){
                employee.setName(userData.getName());
            }
            if(userData.getDid()!=null){
                employee.setDid(userData.getDid());
            }
            if(userData.getPosition()!=null){
                employee.setPosition(userData.getPosition());
            }
            return Db.tx(new IAtom() {
                public boolean run() throws SQLException {
                    if(user.update()){
                        return employee.update();
                    }
                    return false;
                }
            });
        }
        return false;
    }

    /**
     * 修改个人资料
     * @param userData
     * @return
     */
    @Override
    public Result<?> updateSelf(UserData userData, HttpServletRequest request) {
        Result result=validateUserData(userData,request);
        if(!result.getResult()){
            return result;
        }
        result.setResult(updateUser(userData));
        return result;
    }

    /**
     * 验证
     * @param userData
     * @return
     */
    private Result<?> validateUserData(UserData userData,HttpServletRequest request){
        Result result=new Result(false);

        Res res = I18n.use("zh_CN");
        Map<String,Key> keyMap= (Map<String, Key>) request.getSession().getAttribute(UserService.KEY_MAP);
        Key privateKey=keyMap.get(RSAUtil2.PRIVATE_KEY);

        String encryptPwd=userData.getEncryptPwd();
        String decryptPwd=null;
        if(StrKit.isBlank(encryptPwd)){
            result.setMsg(res.get("newPassword.not.empty"));
            return result;
        }else {
            decryptPwd= RSAUtil2.decryptBase64(encryptPwd,privateKey);
            userData.setPassword(decryptPwd);
            if (decryptPwd.length() < 6 || decryptPwd.length() > 16) {
                result.setMsg(res.get("password.length"));
                return result;
            }
        }
        String encryptConfimrPwd=userData.getEncryptConfirmPwd();
        if(StrKit.isBlank(encryptConfimrPwd)){
            result.setMsg(res.get("confirmPassword.not.empty"));
            return result;
        }else {
            String decryptConfimrPwd=RSAUtil2.decryptBase64(encryptConfimrPwd,privateKey);
            userData.setConfirmPwd(decryptConfimrPwd);
            if (!decryptConfimrPwd.equals(decryptPwd)) {
                result.setMsg(res.get("confirmPassword.not.equal"));
                return result;
            }
        }
        //如果当前用户是管理员，则验证姓名和部门
        UserType userType=UserType.valueOf(User.dao.findById(userData.getId()).getAid());
        if(UserType.ADMIN.equals(userType)){
            String name=userData.getName();
            if(StrKit.isBlank(name)){
                result.setMsg(res.get("employee.name.not.empty"));
               return result;
            }

            if(userData.getDid()==null){
                result.setMsg(res.get("employee.department.id.not.select"));
                return result;
            }

        }

        String email=userData.getEmail();
        if(!StrKit.isBlank(email)&& !ValidateUtil.validateEmail(email)){
            result.setMsg(res.get("employee.email.format.wrong"));
            return result;
        }

        result.setResult(true);
        return result;
    }

    public boolean deleteUsers(String idStr) {
        String[] arr=idStr.split(",");
        final int[] ids=new int[arr.length];
        for(int i=0;i<arr.length;i++){
            if(ValidateUtil.isNum(arr[i])){
                ids[i]=Integer.parseInt(arr[i]);
            }
        }
        return Db.tx(new IAtom() {
            public boolean run() throws SQLException {
                //注意删除先后顺序，顺序颠倒将会导致删除失败
                int count=Employee.dao.deleteBatchByUserId(ids);
                if(count==ids.length){
                    return User.dao.deleteBatch(ids)==ids.length;
                }
                return false;
            }
        });
    }

    public Result<?> changePwd(ChangePwd changePwd,String username,HttpServletRequest request) {
        Result result=null;
        try {
            Map<String,Key> keyMap= (Map<String, Key>) request.getSession().getAttribute(UserService.KEY_MAP);
            Key privateKey=keyMap.get(RSAUtil2.PRIVATE_KEY);
            result=validateChangePwd(changePwd,username,privateKey);
            if(result.getResult()){
//                //解密
//                String oldPwd=RSAUtil2.decryptBase64(changePwd.getOldPassword(),privateKey);
//                String newPwd=RSAUtil2.decryptBase64(changePwd.getNewPassword(),privateKey);

                User user=User.dao.findUser(username,AESUtil.encrypt(changePwd.getOldPassword(),Constant.ENCRYPT_KEY));
                user.setPassword(AESUtil.encrypt(changePwd.getNewPassword(),Constant.ENCRYPT_KEY));
                boolean flag = user.update();
                result.setResult(flag);
            }
        } catch (Exception e) {
            result=new Result(false,"服务内部错误");
            logger.error(e.getMessage(),e);
        }
        return result;
    }

    private Result<?> validateChangePwd(ChangePwd changePwd,String username,Key privateKey) throws Exception {
        Result<Object> result=new Result<Object>(true);
        if(changePwd!=null){
            Res res= I18n.use("zh_CN");
            String oldPwd=changePwd.getOldPassword();
            if(StrKit.isBlank(oldPwd)){
                result.setResult(false);
                result.setMsg(res.get("oldPassword.not.empty"));
                return result;
            }else {
                oldPwd=RSAUtil2.decryptBase64(oldPwd,privateKey);
                changePwd.setOldPassword(oldPwd);
                if(User.dao.findUser(username,AESUtil.encrypt(oldPwd, Constant.ENCRYPT_KEY))==null){
                    result.setResult(false);
                    result.setMsg(res.get("oldPassword.not.correct"));
                    return result;
                }
            }

            String newPwd=changePwd.getNewPassword();
            if(StrKit.isBlank(newPwd)){
                result.setResult(false);
                result.setMsg(res.get("newPassword.not.empty"));
                return result;
            }else{
                newPwd=RSAUtil2.decryptBase64(newPwd,privateKey);
                changePwd.setNewPassword(newPwd);
            }

            String confirmPwd=changePwd.getConfirmPassword();
            if(StrKit.isBlank(confirmPwd)){
                result.setResult(false);
                result.setMsg(res.get("confirmPassword.not.empty"));
                return result;
            }else{
                confirmPwd=RSAUtil2.decryptBase64(confirmPwd,privateKey);
                changePwd.setConfirmPassword(confirmPwd);
                if(!confirmPwd.equals(newPwd)) {
                    result.setResult(false);
                    result.setMsg(res.get("confirmPassword.not.equal"));
                    return result;
                }
            }

        }
        return result;
    }

    @Override
    public void setPageData(HttpServletRequest request) {
        Map<String,Key> keyMap = RSAUtil2.generateKeys();
        request.getSession().setAttribute(UserService.KEY_MAP,keyMap);
        request.setAttribute("publicKey",RSAUtil2.getPublicKey(keyMap));
    }
}
