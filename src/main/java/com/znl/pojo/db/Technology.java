package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.util.Date;

/**
 * Created by Administrator on 2015/11/24.
 */
public class Technology extends BaseDbPojo {
    private long playerId = 0l;
    private int type = 0;
    private int level = 0;
    private long nextLevelTime = 0l;//下一级升级完成时间
    private long lastblanceTime = 0l;//上次结算时间

    public long getNextLevelTime() {
        return nextLevelTime;
    }

    public void setNextLevelTime(long nextLevelTime) {
        this.nextLevelTime = nextLevelTime;
    }

    public long getLastblanceTime() {
        return lastblanceTime;
    }

    public void setLastblanceTime(long lastblanceTime) {
        this.lastblanceTime = lastblanceTime;
    }

    private int state=0;//功能0未开启，1开启

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
