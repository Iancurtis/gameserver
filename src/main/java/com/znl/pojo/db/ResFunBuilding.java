package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.utils.GameUtils;

import java.util.Date;

/**
 * Created by Administrator on 2015/11/16.
 */
public class ResFunBuilding extends BaseDbPojo {
    private Long playerId;
    private int bigType = 0;//建筑大类
    private int smallType = 0;//建筑类型
    private int index=0;//建筑位置
    private int level=0;
    private long nextLevelTime;//下一级升级完成时间
    private long lastblanceTime;//上次结算时间
    private int state;//0功能未开启
   private long lvneedTime;//升级所需时间 只做排序

    public long getLvneedTime() {
        return lvneedTime;
    }

    public void setLvneedTime(long lvneedTime) {
        this.lvneedTime = lvneedTime;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getBigType() {
        return bigType;
    }

    public void setBigType(int bigType) {
        this.bigType = bigType;
    }

    public int getSmallType() {
        return smallType;
    }

    public void setSmallType(int smallType) {
        this.smallType = smallType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

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


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
