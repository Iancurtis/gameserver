package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2016/1/20.
 */
public class LimitDungeoReport extends BaseDbPojo {
    private long playerId = 0l;
    private int dungeoId = 0;
    private long battleId = 0l;
    private long time = 0l;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getDungeoId() {
        return dungeoId;
    }

    public void setDungeoId(int dungeoId) {
        this.dungeoId = dungeoId;
    }

    public long getBattleId() {
        return battleId;
    }

    public void setBattleId(long battleId) {
        this.battleId = battleId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
