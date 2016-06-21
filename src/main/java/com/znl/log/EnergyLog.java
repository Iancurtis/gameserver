package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 */
public class EnergyLog extends BaseLog {
    private long id;
    private int buyNum;
    private int addEnergy;
    private int costGold;

    public EnergyLog(int buyNum,int addEnergy, int costGold) {
        this.buyNum = buyNum;
        this.addEnergy = addEnergy;
        this.costGold = costGold;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(int buyNum) {
        this.buyNum = buyNum;
    }

    public int getAddEnergy() {
        return addEnergy;
    }

    public void setAddEnergy(int addEnergy) {
        this.addEnergy = addEnergy;
    }

    public int getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }
}
