package com.dbkj.meet.dto;

import java.util.List;

/**
 * 封装树形节点，以便生成json数据
 * Created by MrQin on 2016/11/14.
 */
public class BaseNode<T> {

    public BaseNode(){
        this.isParent=true;
        this.open=true;
    }

    private long id;//节点id
    private String name;//节点文本信息
    private List<T> children;//节点的所有子节点
    private boolean isParent;//是否是父节点
    private boolean open;

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

    public List<T> getChildren() {
        return children;
    }

    public void setChildren(List<T> children) {
        this.children = children;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
