package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/4/21.
 */
public class Production extends BaseDbPojo implements Serializable {
    private long playerId = 0l;
    private int typeId = 0;
    private int num = 0;
    private int state = 0;//生产类型 1生产中， 2等待生产
    private int finishTime = 0;//结束的时间（秒）
    private int productTime = 0;//持续时间（秒）
    private int sort = 0;//顺序
    private int buildType = 0;//建筑类型
    private int buildIndex = 0;//建筑标志
    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getBuildType() {
        return buildType;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }

    public int getBuildIndex() {
        return buildIndex;
    }

    public void setBuildIndex(int buildIndex) {
        this.buildIndex = buildIndex;
    }

    public int getProductTime() {
        return productTime;
    }

    public void setProductTime(int productTime) {
        this.productTime = productTime;
    }
}
