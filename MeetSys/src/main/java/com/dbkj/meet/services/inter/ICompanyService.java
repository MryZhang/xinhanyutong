package com.dbkj.meet.services.inter;

import com.dbkj.meet.dto.Result;
import com.dbkj.meet.model.AccessNum;
import com.dbkj.meet.model.Company;
import com.dbkj.meet.vo.CompanyVo;

import java.util.List;

/**
 * Created by MrQin on 2016/11/8.
 */
public interface ICompanyService {

    /**
     * 验证Company数据
     * @param company
     * @return
     */
    Result validateCompany(Company company);

    @Deprecated
    Result<Company> addCompany(Company company);

    boolean addCompany(CompanyVo companyVo);

    Company getCompany(Long id);

    Result<Company> updateCompany(Company company);

    boolean deleteCompany(Long id);

    /**
     * 判断公司是否存在
     * @param company
     * @return
     */
    boolean isExistCompany(Company company);

    /**
     * 判断公司是否存在
     * @param name
     * @return
     */
    boolean isExistCompany(String name);

    /**
     * 判断是否存在公司
     * @param id
     * @param name
     * @return
     */
    boolean isExistCompany(long id,String name);

    /**
     * 获取所有接入号码
     * @return
     */
    List<AccessNum> getNumList();

}
