package com.dbkj.meet.services.inter;

import java.util.Map;

/**
 * 用户名称的相关操作的服务
 * Created by DELL on 2017/06/02.
 */
public interface INameService {

    /**
     * 名称在缓存中的键
     */
    String NAME_OF_CACHE="nameOfCache";

    /**
     * 根据用户id获取公司所有的电话与号码的对应关系
     * @param uid 当前用户用户id
     * @return
     */
    Map<String,String> getAllName(Long uid);

    /**
     * 根据号码获取对应的名称
     * @param uid 用户id
     * @param phone
     * @return
     */
    String getNameByPhone(Long uid,String phone);
}
