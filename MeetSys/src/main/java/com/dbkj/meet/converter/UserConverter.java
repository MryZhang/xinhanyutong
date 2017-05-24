package com.dbkj.meet.converter;

import com.dbkj.meet.dto.BaseUser;
import com.dbkj.meet.model.User;
import com.dbkj.meet.vo.UserLoginVo;

/**
 * Created by DELL on 2017/03/23.
 */
public class UserConverter {

    public static User of(BaseUser baseUser){
        User user=new User();
        if(baseUser!=null){
            user.setUsername(baseUser.getUsername());
            if(baseUser instanceof UserLoginVo){
                UserLoginVo ulo=(UserLoginVo) baseUser;
                user.setPassword(ulo.getEncryptPwd());
            }else{
                user.setPassword(baseUser.getPassword());
            }

        }
        return user;
    }
}
