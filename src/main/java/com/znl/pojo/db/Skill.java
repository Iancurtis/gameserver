package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2015/11/26.
 */
public class Skill extends BaseDbPojo {
    private long playerId=0l;
    private int skillId=0;
    private int level=0;
    private int soldierType=0;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }
}
