package com.dbkj.meet.vo;

/**
 *
 * Created by DELL on 2017/04/06.
 */
public class ChangePackageVo {

    private Long id;
    //公司名称
    private String name;
    //套餐名称
    private String pname;
    //套餐id
    private Long pid;
    //包月方数
    private Long count;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
