package com.dbkj.meet.services;

import com.dbkj.meet.dic.Constant;
import com.dbkj.meet.dic.UserType;
import com.dbkj.meet.dto.ChangePwd;
import com.dbkj.meet.interceptors.RemoveKeyCacheInterceptor;
import com.dbkj.meet.model.Company;
import com.dbkj.meet.model.Employee;
import com.dbkj.meet.model.User;
import com.dbkj.meet.services.inter.IAdminService;
import com.dbkj.meet.services.inter.ILoginService;
import com.dbkj.meet.services.inter.RSAKeyService;
import com.dbkj.meet.utils.AESUtil;
import com.dbkj.meet.utils.ParameterUtil;
import com.dbkj.meet.utils.RSAUtil2;
import com.dbkj.meet.utils.ValidateUtil;
import com.dbkj.meet.vo.AdminUserVo;
import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.PrivateKey;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MrQin on 2016/11/8.
 */
public class AdminService implements IAdminService {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private final int PAGE_SIZE=15;

    private RSAKeyService rsaKeyService;

    /**
     * 获取所有公司信息
     * @return
     */
    public List<Company> getCompanys() {
        return Company.dao.getCompanys();
    }

    public Map<String,Object> getParameterMap(Map<String,String[]> map){
        Map<String,Object> paramMap= ParameterUtil.getParaMap(map);
        paramMap.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);
        return paramMap;
    }


    /**
     * 根据条件获取用户的分页的数据
     * @param paramMap
     * @return
     */
    public Page<Record> getUserPage(Map<String,Object> paramMap) {
        Page<Record> pages=User.dao.getUserPage(paramMap);

        List<Record> list=pages.getList();
        for(int i=0,length=list.size();i<length;i++){
            Record user=list.get(i);

            try {
                user.set("password",AESUtil.decrypt(user.getStr("password"), Constant.ENCRYPT_KEY));//密码解密
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            user.set("type",UserType.valueOf(user.getInt("aid")).getTypeText());
        }

        return pages;
    }

    public Page<Record> getUserPage() {
        Map<String,Object> map=new HashMap<String, Object>(10);
        map.put(Constant.PAGE_SIZE_KEY,PAGE_SIZE);
        return getUserPage(map);
    }

    @Before({RemoveKeyCacheInterceptor.class})
    public boolean addUser(AdminUserVo userVo,String key) {
        rsaKeyService=new RSAKeyServiceImpl();
        String privateKey=rsaKeyService.getPrivateKey(key);
        String password= null;
        password = RSAUtil2.decryptBase64(userVo.getEncryptPassword(),privateKey);

        User user=new User();
        user.setUsername(userVo.getUsername());
        user.setCid(userVo.getCid());
        try {
            user.setPassword(AESUtil.encrypt(password,Constant.ENCRYPT_KEY));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        user.setEid(0);
        user.setAid(UserType.ADMIN.getTypeCode());//管理员添加的用户为公司管理员用户
        boolean result = user.save();
        return result;
    }

    public User getUserById(long id) {
        User user = User.dao.findById(id);
        try {
            user.setPassword(AESUtil.decrypt(user.getPassword(),Constant.ENCRYPT_KEY));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return user;
    }

    @Before({RemoveKeyCacheInterceptor.class})
    public boolean updateUser(AdminUserVo userVo,String key) {
        rsaKeyService=new RSAKeyServiceImpl();
        String privateKey=rsaKeyService.getPrivateKey(key);
        String password= null;
        password = RSAUtil2.decryptBase64(userVo.getEncryptPassword(),privateKey);

        User user=new User();
        user.setId(userVo.getId());
        user.setUsername(userVo.getUsername());
        try {
            user.setPassword(AESUtil.encrypt(password,Constant.ENCRYPT_KEY));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return user.update();
    }

    public boolean isExistUser(String username) {
        return User.dao.findUserByUsername(username)!=null;
    }

    /**
     * 批量删除账号
     * @param idStr
     * @return
     */
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
                //int count=User.dao.deleteBatch(ids);
                //logger.info(count+"=="+ids.length);
                Employee.dao.deleteBatchByUserId(ids);
                return User.dao.deleteBatch(ids)==ids.length;
            }
        });
    }


    public boolean validatePassword(String username, String password) {
        try {
            return User.dao.findUser(username,AESUtil.encrypt(password,Constant.ENCRYPT_KEY))!=null;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    /**
     * 修改密码
     * @param id
     * @param changePwd
     * @return
     */
    @Before({RemoveKeyCacheInterceptor.class})
    public boolean updatePassword(long id, ChangePwd changePwd, HttpServletRequest request,String key) {
        User user=User.dao.findById(id);
        rsaKeyService=new RSAKeyServiceImpl();
        String privateKey=rsaKeyService.getPrivateKey(key);
        String password= null;
        password = RSAUtil2.decryptBase64(changePwd.getEncryptNewPwd(),privateKey);
        try {
            user.setPassword(AESUtil.encrypt(password,Constant.ENCRYPT_KEY));
            return user.update();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

}
