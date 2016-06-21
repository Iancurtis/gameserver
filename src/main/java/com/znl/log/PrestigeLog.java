package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class PrestigeLog extends BaseLog {
    private long id;
    private int level;
    private int takePrestige;
    private String takeTime;

    public PrestigeLog(int level, int takePrestige, String takeTime) {
        this.level = level;
        this.takePrestige = takePrestige;
        this.takeTime = takeTime;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTakePrestige() {
        return takePrestige;
    }

    public void setTakePrestige(int takePrestige) {
        this.takePrestige = takePrestige;
    }

    public String getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(String takeTime) {
        this.takeTime = takeTime;
    }
}
