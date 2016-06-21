package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 碎片失去日志
 */
public class PieceLost extends BaseLog {
    private Long id;
    private int getType;//获得方式
    private int typeId;
    private int num;

    public PieceLost(int getType, int typeId, int num) {
        this.getType = getType;
        this.typeId = typeId;
        this.num = num;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getGetType() {
        return getType;
    }

    public void setGetType(int getType) {
        this.getType = getType;
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
