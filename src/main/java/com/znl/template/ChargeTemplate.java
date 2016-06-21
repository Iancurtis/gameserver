package com.znl.template;

/**
 * Created by Administrator on 2015/12/23.
 */
public class ChargeTemplate {
    private String orderId = "";
    private int chargeValue = 0;
    private long playerId = 0l;
    private int chargeType = 0;

    public ChargeTemplate(String orderId, int chargeValue, long playerId, int chargeType) {
        this.orderId = orderId;
        this.chargeValue = chargeValue;
        this.playerId = playerId;
        this.chargeType = chargeType;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getChargeValue() {
        return chargeValue;
    }

    public void setChargeValue(int chargeValue) {
        this.chargeValue = chargeValue;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getChargeType() {
        return chargeType;
    }

    public void setChargeType(int chargeType) {
        this.chargeType = chargeType;
    }
}
