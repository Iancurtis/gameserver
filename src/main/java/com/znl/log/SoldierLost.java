package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 佣兵失去日志
 */
public class SoldierLost extends BaseLog {
    private Long id;
    private int lostType;//失去方式
    private int typeId;
    private int reducenum;
    private int lostnum;

    public SoldierLost(int lostType, int typeId, int reducenum, int lostnum) {
        this.lostType = lostType;
        this.typeId = typeId;
        this.reducenum = reducenum;
        this.lostnum = lostnum;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLostType() {
        return lostType;
    }

    public void setLostType(int lostType) {
        this.lostType = lostType;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getReducenum() {
        return reducenum;
    }

    public void setReducenum(int reducenum) {
        this.reducenum = reducenum;
    }

    public int getLostnum() {
        return lostnum;
    }

    public void setLostnum(int lostnum) {
        this.lostnum = lostnum;
    }
}
