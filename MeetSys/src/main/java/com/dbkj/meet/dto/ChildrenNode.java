package com.dbkj.meet.dto;

/**
 * 封装子节点的相关数据
 * Created by MrQin on 2016/11/14.
 */
public class ChildrenNode {

    public ChildrenNode() {
    }

    public ChildrenNode(long id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    private long id;
    private String name;
    private String value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
