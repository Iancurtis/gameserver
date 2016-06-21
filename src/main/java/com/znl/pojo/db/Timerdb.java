package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.define.TimerDefine;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2015/11/17.
 */
public class Timerdb extends BaseDbPojo {

    private Long playerId;
    private int type;
    private int lestime;
    private Long  lasttime; //到期的时间戳
    private int refreshType;//点击触发的通过通过调用方法填具体时间，其它填-1
    private int otherType;
    private int smallType;
    private int num;
    private int attr1=0;
    private int attr2=0;
    private int attr3=0;
    private long begintime;
    private int isAutoBuildLv;

    public int getIsAutoBuildLv() {
        return isAutoBuildLv;
    }

    public void setIsAutoBuildLv(int isAutoBuildLv) {
        this.isAutoBuildLv = isAutoBuildLv;
    }

    public long getBegintime() {
        return begintime;
    }

    public void setBegintime(long begintime) {
        this.begintime = begintime;
    }

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

    public int getLestime() {
        return lestime;
    }

    public void setLestime(int lestime) {
        this.lestime = lestime;
    }


    public Long getLasttime() {
        return lasttime;
    }

    public void setLasttime(Long lasttime) {
        this.lasttime = lasttime;
    }

    public int getRefreshType() {
        return refreshType;
    }

    public void setRefreshType(int refreshType) {
        this.refreshType = refreshType;
    }

    public int getSmallType() {
        return smallType;
    }

    public void setSmallType(int smallType) {
        this.smallType = smallType;
    }

    public int getOtherType() {
        return otherType;
    }

    public void setOtherType(int otherType) {
        this.otherType = otherType;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getAttr1() {
        return attr1;
    }

    public void setAttr1(int attr1) {
        this.attr1 = attr1;
    }

    public int getAttr2() {
        return attr2;
    }

    public void setAttr2(int attr2) {
        this.attr2 = attr2;
    }

    public int getAttr3() {
        return attr3;
    }

    public void setAttr3(int attr3) {
        this.attr3 = attr3;
    }
}
