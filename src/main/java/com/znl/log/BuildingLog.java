package com.znl.log;

import com.znl.base.BaseLog;

/**
 *建筑操作信息
 * Created by Administrator on 2015/12/4.
 */
public class BuildingLog extends BaseLog{
    private Long id;
    private int index;//位置
    private int buildType;//建筑类型
    private int opetype;//操作类型
    private int typeId;//生产类型
    private int num;//生产数量
    private int level;//建筑等级
    private int cost;//消耗金币
    private int itemCost;//道具消耗加速
    private int itemCostNum;//消耗的数量
    public BuildingLog(int index, int buildType, int opetype, int typeId, int num, int level) {
        this.index = index;
        this.buildType = buildType;
        this.opetype = opetype;
        this.typeId = typeId;
        this.num = num;
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBuildType() {
        return buildType;
    }

    public void setBuildType(int buildType) {
        this.buildType = buildType;
    }

    public int getOpetype() {
        return opetype;
    }

    public void setOpetype(int opetype) {
        this.opetype = opetype;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getItemCost() {
        return itemCost;
    }

    public void setItemCost(int itemCost) {
        this.itemCost = itemCost;
    }

    public int getItemCostNum() {
        return itemCostNum;
    }

    public void setItemCostNum(int itemCostNum) {
        this.itemCostNum = itemCostNum;
    }
}
