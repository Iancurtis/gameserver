package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/14.
 */
public class TaskGetLog extends BaseLog {
    private Long id;
    private int typeId;
    private int tableType;

    public TaskGetLog(int typeId, int tableType) {
        this.typeId = typeId;
        this.tableType = tableType;
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

    public int getTableType() {
        return tableType;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }
}
