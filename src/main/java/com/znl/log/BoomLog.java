package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class BoomLog extends BaseLog {
    private long id;
    private int level;
    private int addboom;
    private int costTeal;

    public BoomLog(int level, int addboom, int costTeal) {
        this.level = level;
        this.addboom = addboom;
        this.costTeal = costTeal;
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

    public int getAddboom() {
        return addboom;
    }

    public void setAddboom(int addboom) {
        this.addboom = addboom;
    }

    public int getCostTeal() {
        return costTeal;
    }

    public void setCostTeal(int costTeal) {
        this.costTeal = costTeal;
    }
}
