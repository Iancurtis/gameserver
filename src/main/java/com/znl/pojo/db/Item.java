package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;


/**
 * Created by Administrator on 2015/11/16.
 */
public class Item extends BaseDbPojo {
    private Long playerId = 0l;
    private int typeId = 0;
    private int num = 0;


    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

}
