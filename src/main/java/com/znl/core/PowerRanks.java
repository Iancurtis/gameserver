package com.znl.core;

/**
 * Created by Administrator on 2015/12/24.
 */
public class PowerRanks {
    private int type;//排行榜类型
    private String areaKey;//区、服
    private long playerId;//玩家ID
    private long value;//power值
    private int level;//攻击，强化，闪避要的等级
    private int sumLevel;//角色等级
    private int atklv; //攻击装备最高等级
    private int critlv;//暴击装备最高等级
    private int dogelv;//闪避装备最高等级
    private String name="";//名字
    public PowerRanks() {
    }

    public int getSumLevel() {
        return sumLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSumLevel(int sumLevel) {
        this.sumLevel = sumLevel;
    }

    public int getAtklv() {
        return atklv;
    }

    public void setAtklv(int atklv) {
        this.atklv = atklv;
    }

    public int getCritlv() {
        return critlv;
    }

    public void setCritlv(int critlv) {
        this.critlv = critlv;
    }

    public int getDogelv() {
        return dogelv;
    }

    public void setDogelv(int dogelv) {
        this.dogelv = dogelv;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAreaKey() {
        return areaKey;
    }

    public void setAreaKey(String areaKey) {
        this.areaKey = areaKey;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }


    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
