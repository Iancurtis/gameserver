package com.znl.core;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/22.
 */
public class Notice implements Serializable {
    public Long playerId;
    public long beginTime;
    public String areakey;
    public long endTime;
    public int type;
    public String pushId;

    public Notice(Long playerId, long beginTime, String areakey, long endTime, int type, String pushId) {
        this.playerId = playerId;
        this.beginTime = beginTime;
        this.areakey = areakey;
        this.endTime = endTime;
        this.type = type;
        this.pushId = pushId;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public String getAreakey() {
        return areakey;
    }

    public void setAreakey(String areakey) {
        this.areakey = areakey;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
