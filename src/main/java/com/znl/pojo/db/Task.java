package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.define.TaskDefine;

/**
 * Created by Administrator on 2015/11/16.
 */
public class Task extends BaseDbPojo {
    private Long playerId = 0l;
    private int tableType = 0;//任务表类型1主线任务 2日常任务 3日常活跃
    private int taskType = 0;//任务类型
    private int tastId = 0;//任务表的Id
    private long num = 0l;//达成度
    private int state = 0;//任务状态 0未接受，1接受
    private int isget = 0;//领取状态 0未达到要求，1达到要求了，2领取了

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getTableType() {
        return tableType;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getTastId() {
        return tastId;
    }

    public void setTastId(int tastId) {
        this.tastId = tastId;
    }

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public int getIsget() {
        return isget;
    }

    public void setIsget(int isget) {
        this.isget = isget;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
