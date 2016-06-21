package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 装备失去日志
 */
public class OrndanceLost extends BaseLog {
    private Long id;
    private int lostType;//失去方式
    private int typeId;


    public OrndanceLost(int lostType, int typeId) {
        this.lostType = lostType;
        this.typeId = typeId;
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
}
