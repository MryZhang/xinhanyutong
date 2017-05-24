package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.vo.PackageListItemVo;
import com.dbkj.meet.vo.PackageVo;

import java.util.List;
import java.util.Map;

/**
 * Created by DELL on 2017/04/01.
 */
public interface PackageService {

    List<PackageListItemVo> list();

    Map<String,Object> showAdd();

    /**
     * 判断套餐名是否重复
     * @param name
     * @return
     */
    Result exist(String name);

    boolean add(PackageVo packageVo);
}
