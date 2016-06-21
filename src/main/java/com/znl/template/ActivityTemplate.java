package com.znl.template;

/**
 * Created by Administrator on 2016/3/29.
 */
public class ActivityTemplate {
    private int activityId;
    private long playerId;
    private int activityType;
    private long refurceTime;
    private long conditionValue ;
    private int state;//是否已经领取
    private int expand = 0;//存放一些扩展参数
    private int buyInv = 0;//是否已经购买了投资计划
    private Long lastCheckTime=0l;

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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getExpand() {
        return expand;
    }

    public void setExpand(int expand) {
        this.expand = expand;
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
}
