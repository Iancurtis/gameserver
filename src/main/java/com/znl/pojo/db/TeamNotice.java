package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2016/1/28.
 */
public class TeamNotice extends BaseDbPojo {
    private int type = 0;
    private long playerId = 0l;
    private String name = "";
    private int iconId = 0;
    private int level = 0;
    private int x = -1;
    private int y = -1;
    private int targetId = 0;
    private long arriveTime = 0l;//到达时间
    private long helpId = 0l;

    public long getHelpId() {
        return helpId;
    }

    public void setHelpId(long helpId) {
        this.helpId = helpId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public long getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(long arriveTime) {
        this.arriveTime = arriveTime;
    }
}
