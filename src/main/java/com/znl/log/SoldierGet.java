package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 佣兵获得日志
 */
public class SoldierGet extends BaseLog {
    private Long id;
    private int lostType;//获得方式
    private int typeId;
    private int num;

    public SoldierGet(int lostType, int typeId, int num) {
        this.lostType = lostType;
        this.typeId = typeId;
        this.num = num;
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

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
