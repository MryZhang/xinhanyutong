package com.dbkj.meet.dto;

/**
 * Created by MrQin on 2016/11/8.
 */
public class BasePageWhere {

    private int pageSize;//每页显示的数据条数
    private int currentPage;//当前页码

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
