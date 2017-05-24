package com.dbkj.meet.dto;

/**
 * 封装会议列表数据
 * Created by MrQin on 2016/11/7.
 */
public class MeetListItem {

    private Long id;
    private String subject;//主题
    private String hostName;//主持人
    private String type;
    private String createTime;//创建时间
    private int status;//会议状态

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
