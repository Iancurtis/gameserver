package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * 世界地图建筑
 * 并记录一些抢夺信息
 * Created by Administrator on 2015/11/10.
 */
public class WorldBuilding extends BaseDbPojo {
    private Long playerId;

    private Integer worldTileX = -1; //世界X坐标
    private Integer worldTileY = -1; //世界Y坐标

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Integer getWorldTileX() {
        return worldTileX;
    }

    public void setWorldTileX(Integer worldTileX) {
        this.worldTileX = worldTileX;
    }

    public Integer getWorldTileY() {
        return worldTileY;
    }

    public void setWorldTileY(Integer worldTileY) {
        this.worldTileY = worldTileY;
    }
}
