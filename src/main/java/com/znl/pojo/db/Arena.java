package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2016/6/26.
 */
public class Arena extends BaseDbPojo {

    /**
     * 玩家id
     */
    private long playerId;

    /**
     * 挑战次数
     */
    private int challengetimes;

    /**
     * 上期排名
     */
    private int lastOrder;

    /**
     * 购买次数
     */
    private int buytimes;

    /**
     * 上期排名奖励 0不可领取 1可领取
     */
    private int lastReward;

    /**
     * 上次操作时间
     */
    private long lastOperateTime;


    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getChallengetimes() {
        return challengetimes;
    }

    public void setChallengetimes(int challengetimes) {
        this.challengetimes = challengetimes;
    }

    public int getLastOrder() {
        return lastOrder;
    }

    public void setLastOrder(int lastOrder) {
        this.lastOrder = lastOrder;
    }

    public int getBuytimes() {
        return buytimes;
    }

    public void setBuytimes(int buytimes) {
        this.buytimes = buytimes;
    }

    public int getLastReward() {
        return lastReward;
    }

    public void setLastReward(int lastReward) {
        this.lastReward = lastReward;
    }

    public long getLastOperateTime() {
        return lastOperateTime;
    }

    public void setLastOperateTime(long lastOperateTime) {
        this.lastOperateTime = lastOperateTime;
    }

    public Arena(){

    }

    public Arena(long playerId, int challengetimes, int lastOrder, int buytimes, int lastReward, long lastOperateTime) {
        this.playerId = playerId;
        this.challengetimes = challengetimes;
        this.lastOrder = lastOrder;
        this.buytimes = buytimes;
        this.lastReward = lastReward;
        this.lastOperateTime = lastOperateTime;
    }
}
