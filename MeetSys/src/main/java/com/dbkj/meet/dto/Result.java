package com.dbkj.meet.dto;

/**
 * 封装结果
 * Created by MrQin on 2016/11/8.
 */
public class Result<T> {

    public Result(){}

    public Result(boolean result,T data){
        this.result=result;
        this.data=data;
    }

    public Result(boolean result) {
        this.result = result;
    }

    public Result(boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    private boolean result;//是否成功
    private String msg;//添加错误的原因
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
