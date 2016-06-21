package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2015/12/15.
 * 资源失去日志
 */
public class ResourceLost extends BaseLog {
    private Long id;
    private int getType;//失去方式
    private int power;
    private long value;
    private long playerId;
    public ResourceLost(int getType, int power, long value,long playerId) {
        this.getType = getType;
        this.power = power;
        this.value = value;
        this.playerId = playerId;
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}
