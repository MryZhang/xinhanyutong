package com.dbkj.meet.vo;

import java.math.BigDecimal;

/**
 * Created by DELL on 2017/02/27.
 */
public class ChargeVo {

    public ChargeVo(){

    }

    public ChargeVo(Long id,BigDecimal chargeAmount, BigDecimal actualCharge) {
        this.id=id;
        this.chargeAmount = chargeAmount;
        this.actualCharge = actualCharge;
    }

    /**
     * 要充值的id
     */
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 充值金额
     */
    private BigDecimal chargeAmount;
    /**
     * 实际到账金额
     */
    private BigDecimal actualCharge;

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public BigDecimal getActualCharge() {
        return actualCharge;
    }

    public void setActualCharge(BigDecimal actualCharge) {
        this.actualCharge = actualCharge;
    }
}
