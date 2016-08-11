package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2015/11/16.
 */
public class Museum extends BaseDbPojo {
    private Long playerId = 0l;
    private int type = 0;
    private int level = 0;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
