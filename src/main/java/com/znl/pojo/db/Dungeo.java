package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/11/10.
 */
public class Dungeo extends BaseDbPojo implements Serializable {
    private long playerId;
    private int dungeoId = 0;
    private Set<Long> getBox = new HashSet<Long>();//已经领取的宝箱index
    private List<Integer> starList = new ArrayList<>();

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

    public Set<Long> getGetBox() {
        return getBox;
    }

    public void setGetBox(Set<Long> getBox) {
        this.getBox = getBox;
    }

    public List<Integer> getStarList() {
        return starList;
    }

    public void setStarList(List<Integer> starList) {
        this.starList = starList;
    }

}
