package com.dbkj.meet.dto;

import java.util.Date;

/**
 * 时间范围
 * Created by DELL on 2017/03/03.
 */
public class DatetimeRange {

    public DatetimeRange() {
    }

    public DatetimeRange(Date begin, Date end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * 开始时间
     */
    private Date begin;

    /**
     * 结束时间
     */
    private Date end;

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
