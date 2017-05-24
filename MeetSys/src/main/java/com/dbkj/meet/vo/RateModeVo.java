package com.dbkj.meet.vo;

/**
 * Created by DELL on 2017/04/03.
 */
public class RateModeVo {

    public RateModeVo(){

    }

    public RateModeVo(int value, String text) {
        this.value = value;
        this.text = text;
    }

    private int value;
    private String text;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
