package com.dbkj.meet.vo;

import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 2017/03/02.
 */
public class BillVo<T> {

    public BillVo() {
    }

    public BillVo(int curPage, int totalRecords, List<T> data) {
        this.curPage = curPage;
        this.totalRecords = totalRecords;
        this.data = data;
    }

    /**
     * 当前页码
     */
    private int curPage;
    /**
     * 总数据行数
     */
    private int totalRecords;
    /**
     * 分页数据
     */
    private List<T> data=new ArrayList<>();

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
