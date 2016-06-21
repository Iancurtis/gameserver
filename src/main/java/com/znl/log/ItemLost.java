package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 道具失去日志
 */
public class ItemLost extends BaseLog {
    private Long id;
    private int lostType;//失去方式
    private int typeId;
    private int num;

    public ItemLost(int lostType, int typeId, int num) {
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
