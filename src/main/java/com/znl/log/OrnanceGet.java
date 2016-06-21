package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 军械获取日志
 */
public class OrnanceGet extends BaseLog {
    private Long id;
    private int getType;//获得方式
    private int typeId;


    public OrnanceGet(int getType, int typeId) {
        this.getType = getType;
        this.typeId = typeId;

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
}
