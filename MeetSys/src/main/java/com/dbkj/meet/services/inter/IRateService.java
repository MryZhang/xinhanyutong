package com.dbkj.meet.services.inter;

import com.dbkj.meet.vo.PackageListItemVo;
import com.dbkj.meet.vo.RateVo;

import java.util.List;

/**
 * Created by DELL on 2017/02/28.
 */
public interface IRateService {

    List<PackageListItemVo> getRateInfo();
}
