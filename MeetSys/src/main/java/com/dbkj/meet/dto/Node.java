package com.dbkj.meet.dto;

import java.io.Serializable;

/**
 * 封装会议联系人信息
 * Created by MrQin on 2016/11/25.
 */
public class Node implements Serializable{

    private static final long serialVersionUID = -6106072893711743992L;

    private Long id;
    private String name;
    private String phone;
    private String status;
    private boolean host;
    private Integer role;
    private boolean deleted;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (host != node.host) return false;
        if (id != null ? !id.equals(node.id) : node.id != null) return false;
        if (name != null ? !name.equals(node.name) : node.name != null) return false;
        if (phone != null ? !phone.equals(node.phone) : node.phone != null) return false;
        if (status != null ? !status.equals(node.status) : node.status != null) return false;
        return role != null ? role.equals(node.role) : node.role == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (host ? 1 : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
}
