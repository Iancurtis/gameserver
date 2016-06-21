package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2016/1/20.
 */
public class LimitDungeoReport extends BaseDbPojo {
    private long playerId;
    private int dungeoId;
    private long battleId;
    private long time;
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
