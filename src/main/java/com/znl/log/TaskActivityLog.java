package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class TaskActivityLog extends BaseLog {
    private Long id;
    private int typeId;

    public TaskActivityLog(){

    }
    public TaskActivityLog(int typeId) {
        this.typeId = typeId;
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
}
