package com.dbkj.meet.vo;

/**
 * Created by DELL on 2017/04/05.
 */
public class CompanyVo {

    /**
     * 公司id
     */
    private Integer id;

    /**
     * 公司名称
     */
    private String name;
    /**
     * 呼入号码
     */
    private Integer callNum;
    /**
     * 呼出号码
     */
    private Integer showNum;
    /**
     * 套餐id
     */
    private Long pid;
    /**
     * 套餐名称
     */
    private String cname;
    /**
     * 包月方数
     */
    private Integer count;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCallNum() {
        return callNum;
    }

    public void setCallNum(Integer callNum) {
        this.callNum = callNum;
    }

    public Integer getShowNum() {
        return showNum;
    }

    public void setShowNum(Integer showNum) {
        this.showNum = showNum;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
