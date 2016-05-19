package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/15.
 */
public class Activity extends BaseDbPojo {
    private int activityId = 0;
    private long playerId = 0l;
    private int activityType = 0;
    private long refurceTime = 0l;
    private long conditionValue = 0l;
    private int state = 0;//是否已经领取
    private int expand = 0;//存放一些扩展参数
    private int buyInv = 0;//是否已经购买了投资计划
    private Long lastCheckTime = 0l;
    private List<Integer> canGetList = new ArrayList<>();
    private List<Integer> alreadyGetList = new ArrayList<>();
    private List<Integer> valuelist = new ArrayList<Integer>();//完成度sort为底

    private List<Integer> buyTimesList = new ArrayList<>();//用于限购次数存储

    private String legionShare = "";//用于限时活动-有福同享 领奖礼箱的存储:格式：礼包id_分享人id_分享时间


    public List<Integer> getValuelist() {
        return valuelist;
    }

    public void setValuelist(List<Integer> valuelist) {
        this.valuelist = valuelist;
    }

    public int getBuyInv() {
        return buyInv;
    }

    public void setBuyInv(int buyInv) {
        this.buyInv = buyInv;
    }

    public Long getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(Long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public long getRefurceTime() {
        return refurceTime;
    }

    public void setRefurceTime(long refurceTime) {
        this.refurceTime = refurceTime;
    }

    public long getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(long conditionValue) {
        this.conditionValue = conditionValue;
    }

    public int getExpand() {
        return expand;
    }

    public void setExpand(int expand) {
        this.expand = expand;
    }

    public List<Integer> getCanGetList() {
        return canGetList;
    }

    public void setCanGetList(List<Integer> canGetList) {
        this.canGetList = canGetList;
    }

    public void addCanGetList(Integer add) {
        this.canGetList.add(add);
    }

    public void removeCanGetList(Integer remove) {
        this.canGetList.remove(remove);
    }

    public void clearCanGetList() {
        this.canGetList.clear();
    }

    public void clearBuyTimesList() {
        this.buyTimesList.clear();
    }

    public List<Integer> getAlreadyGetList() {
        return alreadyGetList;
    }

    public void setAlreadyGetList(List<Integer> alreadyGetList) {
        this.alreadyGetList = alreadyGetList;
    }

    public void addAlreadyGetList(Integer add) {
        this.alreadyGetList.add(add);
    }

    public void removeAlreadyGetList(Integer remove) {
        this.alreadyGetList.remove(remove);
    }

    public void clearAlreadyGetList() {
        this.alreadyGetList.clear();
    }

    public List<Integer> getBuyTimesList() {
        return buyTimesList;
    }

    public void setBuyTimesList(List<Integer> buyTimesList) {
        this.buyTimesList = buyTimesList;
    }

    public void addBuyTimesList(Integer add) {
        this.buyTimesList.add(add);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getLegionShare() {
        return legionShare;
    }

    public void setLegionShare(String legionShare) {
        this.legionShare = legionShare;
    }
}
