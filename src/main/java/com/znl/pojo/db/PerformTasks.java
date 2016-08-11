package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2015/11/27.
 */
public class PerformTasks extends BaseDbPojo {
    private long playerId = 0l;
    private int type = 0; //执行任务类型。人，矿，人的矿
    private String name = "";
    private int level = 0;
    private int state = 0;
    private int worldTileX = -1; //世界X坐标
    private int worldTileY = -1; //世界Y坐标
    private long timeer = 0l;//到期时间
    private long beginTime = 0l;//开始时间
    private long capacity = 0l;
    private long load = 0l;
    private int product = 0;
    private int icon = 0;
    private int startX = -1; //世界X坐标
    private int startY = -1; //世界Y坐标
    private long maxSoilderNum = 0; //带兵总量
    private Set<Long> membersSet = new HashSet<>();


    public long getMaxSoilderNum() {
        return maxSoilderNum;
    }

    public void setMaxSoilderNum(long maxSoilderNum) {
        this.maxSoilderNum = maxSoilderNum;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void addMembersId(long membersId) {
        this.membersSet.add(membersId);
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getWorldTileX() {
        return worldTileX;
    }

    public void setWorldTileX(int worldTileX) {
        this.worldTileX = worldTileX;
    }

    public int getWorldTileY() {
        return worldTileY;
    }

    public void setWorldTileY(int worldTileY) {
        this.worldTileY = worldTileY;
    }

    public long getTimeer() {
        return timeer;
    }

    public void setTimeer(long timeer) {
        this.timeer = timeer;
    }

    public Set<Long> getMembersSet() {
        return membersSet;
    }

    public void setMembersSet(Set<Long> membersSet) {
        this.membersSet = membersSet;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getLoad() {
        return load;
    }

    public void setLoad(long load) {
        this.load = load;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }
}
