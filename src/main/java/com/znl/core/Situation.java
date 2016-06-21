package com.znl.core;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/22.
 */
public class Situation implements Serializable {
    public Long armyId;
    public Long defendPlayerid;
    public Long attackPlayerid;
    public long evenTime;
    public int result; //1防守胜利 ，防守失败
    public long lose; //失去的资源
    public int type; // 1军情 2 民情
    public int smalltype;//1试炼场 2团长发奖 3 加入军团 4 退出军团 5被踢 6团长换 7 任命
    public String newName="";//新职位
    public String buildup="";//建筑升级

    public Situation(Long armyId, Long defendPlayerid, Long attackPlayerid, long evenTime, int result, long lose, int type, int smalltype) {
        this.armyId = armyId;
        this.defendPlayerid = defendPlayerid;
        this.attackPlayerid = attackPlayerid;
        this.evenTime = evenTime;
        this.result = result;
        this.lose = lose;
        this.type = type;
        this.smalltype = smalltype;
    }

    public String getBuildup() {
        return buildup;
    }

    public void setBuildup(String buildup) {
        this.buildup = buildup;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public Long getArmyId() {
        return armyId;
    }

    public void setArmyId(Long armyId) {
        this.armyId = armyId;
    }

    public Long getDefendPlayerid() {
        return defendPlayerid;
    }

    public void setDefendPlayerid(Long defendPlayerid) {
        this.defendPlayerid = defendPlayerid;
    }

    public Long getAttackPlayerid() {
        return attackPlayerid;
    }

    public void setAttackPlayerid(Long attackPlayerid) {
        this.attackPlayerid = attackPlayerid;
    }

    public long getEvenTime() {
        return evenTime;
    }

    public void setEvenTime(long evenTime) {
        this.evenTime = evenTime;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public long getLose() {
        return lose;
    }

    public void setLose(long lose) {
        this.lose = lose;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSmalltype() {
        return smalltype;
    }

    public void setSmalltype(int smalltype) {
        this.smalltype = smalltype;
    }
}
