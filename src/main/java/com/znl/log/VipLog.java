package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class VipLog extends BaseLog {
    private Long id;
    private int vipExp;
    private int currentVipLv;
    private int nextVipLv;

    public Long getId() {
        return id;
    }

    public VipLog(Long id, int vipExp, int currentVipLv, int nextVipLv) {
        this.id = id;
        this.vipExp = vipExp;
        this.currentVipLv = currentVipLv;
        this.nextVipLv = nextVipLv;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public int getCurrentVipLv() {
        return currentVipLv;
    }

    public void setCurrentVipLv(int currentVipLv) {
        this.currentVipLv = currentVipLv;
    }

    public int getNextVipLv() {
        return nextVipLv;
    }

    public void setNextVipLv(int nextVipLv) {
        this.nextVipLv = nextVipLv;
    }
}
