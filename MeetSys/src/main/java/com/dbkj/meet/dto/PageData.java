package com.dbkj.meet.dto;

import java.util.List;

/**
 * Created by MrQin on 2016/11/8.
 */
public class PageData<T> {

    private int currentPage;//当前页码
    private int pageSize;//每页显示的数据条数
    private int totalRows;//总数据条数
    private int totalPage;//总页数
    private List<T> list;//当前页的数据

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }


}
