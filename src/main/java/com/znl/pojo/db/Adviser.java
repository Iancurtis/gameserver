package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;


/**
 * Created by Administrator on 2015/11/16.
 */
public class Adviser extends BaseDbPojo {
    private Long playerId;
    private int typeId = 0;
    private int num = 0;
    private int fightnum=0;//出战数量
    private int quilty;//品质
    private int sort;//用来排序会变动

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getQuilty() {
        return quilty;
    }

    public void setQuilty(int quilty) {
        this.quilty = quilty;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public int getFightnum() {
        return fightnum;
    }

    public void setFightnum(int fightnum) {
        this.fightnum = fightnum;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
