package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.define.SoldierDefine;
import com.znl.proxy.ConfigDataProxy;
import org.json.JSONObject;

/**
 * Created by Administrator on 2015/10/24.
 */
public class Soldier extends BaseDbPojo {
    private Long playerId = 0l;
    private int typeId = 0;
    private int num = 0;
    private int lostNum = 0;
    private int baseNum = 0;
    private int hpMax = 0;
    private int hp = 0;
    private int atk = 0;
    private int hitRate = 0;
    private int dodgeRate = 0;
    private int critRate = 0;
    private int defRate = 0;
    private int wreck = 0;
    private int defend = 0;
    private int initiative = 0;
    private int hpMaxRate = 0;
    private int atkRate = 0;
    private int infantryHpMax = 0;
    private int infantryAtk = 0;
    private int cavalryHpMax = 0;
    private int cavalryAtk = 0;
    private int pikemanHpMax = 0;
    private int pikemanAtk = 0;
    private int archerHpMax = 0;
    private int archerHpatk = 0;
    private int load = 0;
    private int loadRate = 0;
    private int speedRate = 0;
    private int pveDamAdd = 0;
    private int pveDamDer = 0;
    private int pvpDamAdd = 0;
    private int pvpDamDer = 0;
    private int damadd = 0;
    private int damder = 0;

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

    public int getHpMax() {
        return hpMax;
    }

    public void setHpMax(int hpMax) {
        this.hpMax = hpMax;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getHitRate() {
        return hitRate;
    }

    public void setHitRate(int hitRate) {
        this.hitRate = hitRate;
    }

    public int getDodgeRate() {
        return dodgeRate;
    }

    public void setDodgeRate(int dodgeRate) {
        this.dodgeRate = dodgeRate;
    }

    public int getCritRate() {
        return critRate;
    }

    public void setCritRate(int critRate) {
        this.critRate = critRate;
    }

    public int getDefRate() {
        return defRate;
    }

    public void setDefRate(int defRate) {
        this.defRate = defRate;
    }

    public int getWreck() {
        return wreck;
    }

    public void setWreck(int wreck) {
        this.wreck = wreck;
    }

    public int getDefend() {
        return defend;
    }

    public void setDefend(int defend) {
        this.defend = defend;
    }

    public int getInitiative() {
        return initiative;
    }

    public void setInitiative(int initiative) {
        this.initiative = initiative;
    }

    public int getHpMaxRate() {
        return hpMaxRate;
    }

    public void setHpMaxRate(int hpMaxRate) {
        this.hpMaxRate = hpMaxRate;
    }

    public int getAtkRate() {
        return atkRate;
    }

    public void setAtkRate(int atkRate) {
        this.atkRate = atkRate;
    }

    public int getInfantryHpMax() {
        return infantryHpMax;
    }

    public void setInfantryHpMax(int infantryHpMax) {
        this.infantryHpMax = infantryHpMax;
    }

    public int getInfantryAtk() {
        return infantryAtk;
    }

    public void setInfantryAtk(int infantryAtk) {
        this.infantryAtk = infantryAtk;
    }

    public int getCavalryHpMax() {
        return cavalryHpMax;
    }

    public void setCavalryHpMax(int cavalryHpMax) {
        this.cavalryHpMax = cavalryHpMax;
    }

    public int getCavalryAtk() {
        return cavalryAtk;
    }

    public void setCavalryAtk(int cavalryAtk) {
        this.cavalryAtk = cavalryAtk;
    }

    public int getPikemanHpMax() {
        return pikemanHpMax;
    }

    public void setPikemanHpMax(int pikemanHpMax) {
        this.pikemanHpMax = pikemanHpMax;
    }

    public int getPikemanAtk() {
        return pikemanAtk;
    }

    public void setPikemanAtk(int pikemanAtk) {
        this.pikemanAtk = pikemanAtk;
    }

    public int getArcherHpMax() {
        return archerHpMax;
    }

    public void setArcherHpMax(int archerHpMax) {
        this.archerHpMax = archerHpMax;
    }

    public int getArcherHpatk() {
        return archerHpatk;
    }

    public void setArcherHpatk(int archerHpatk) {
        this.archerHpatk = archerHpatk;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getLoadRate() {
        return loadRate;
    }

    public void setLoadRate(int loadRate) {
        this.loadRate = loadRate;
    }

    public int getSpeedRate() {
        return speedRate;
    }

    public void setSpeedRate(int speedRate) {
        this.speedRate = speedRate;
    }

    public int getPveDamAdd() {
        return pveDamAdd;
    }

    public void setPveDamAdd(int pveDamAdd) {
        this.pveDamAdd = pveDamAdd;
    }

    public int getPveDamDer() {
        return pveDamDer;
    }

    public void setPveDamDer(int pveDamDer) {
        this.pveDamDer = pveDamDer;
    }

    public int getPvpDamAdd() {
        return pvpDamAdd;
    }

    public void setPvpDamAdd(int pvpDamAdd) {
        this.pvpDamAdd = pvpDamAdd;
    }

    public int getPvpDamDer() {
        return pvpDamDer;
    }

    public void setPvpDamDer(int pvpDamDer) {
        this.pvpDamDer = pvpDamDer;
    }

    public int getDamadd() {
        return damadd;
    }

    public void setDamadd(int damadd) {
        this.damadd = damadd;
    }

    public int getDamder() {
        return damder;
    }

    public void setDamder(int damder) {
        this.damder = damder;
    }

    public int getLostNum() {
        return lostNum;
    }

    public void setLostNum(int lostNum) {
        this.lostNum = lostNum;
    }

    public int getBaseNum() {
        return baseNum;
    }

    public void setBaseNum(int baseNum) {
        this.baseNum = baseNum;
    }
}
