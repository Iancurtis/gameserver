package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class MilitaryLog extends BaseLog {
    private long id;
    private int level;
    private int costTeal;
    private int prestige;

    public MilitaryLog(int level, int costTeal, int prestige) {
        this.level = level;
        this.costTeal = costTeal;
        this.prestige = prestige;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCostTeal() {
        return costTeal;
    }

    public void setCostTeal(int costTeal) {
        this.costTeal = costTeal;
    }

    public int getPrestige() {
        return prestige;
    }

    public void setPrestige(int prestige) {
        this.prestige = prestige;
    }
}
