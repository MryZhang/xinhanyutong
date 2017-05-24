package com.dbkj.meet.vo;

import java.util.List;

/**
 * 封装费率显示的相关信息
 * Created by DELL on 2017/02/28.
 */
public class RateVo {

    public RateVo(){

    }

    public RateVo(String type, List<Rate> list) {
        this.type = type;
        this.list = list;
    }

    /**
     * 计费模式
     */
    private String type;
    /**
     * 当前计费模式下费率
     */
    private List<Rate> list;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Rate> getList() {
        return list;
    }

    public void setList(List<Rate> list) {
        this.list = list;
    }

    /**
     * 封装费率相关的信息
     */
    public static class Rate{

        public Rate(){

        }

        public Rate(Long id,String mode, String rate) {
            this.id=id;
            this.mode = mode;
            this.rate = rate;
        }

        private Long id;

        /**
         * 计费方式
         */
        private String mode;

        /**
         * 费率
         */
        private String rate;

        private Integer parentId;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String type) {
            this.mode = type;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }
    }
}
