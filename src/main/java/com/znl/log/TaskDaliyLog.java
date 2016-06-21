package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class TaskDaliyLog extends BaseLog {
    private Long id;
    private int typeId;
    private int type;

    public TaskDaliyLog(int typeId, int type) {
        this.typeId = typeId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
