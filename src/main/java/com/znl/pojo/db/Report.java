package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/15.
 */
public class Report extends BaseDbPojo implements Serializable {
    private long playerId = 0l;
    private long attackerId = 0l;
    private String attackerName = "";
    private long defendId = 0l;
    private String defendName = "";
    private long messageId = 0l;
    private int reportType = 0;
    private int level = 0;
    private String name = "";
    private int result = 0; //进攻0赢1输
    private int x = -1;
    private int y = -1;
    private String garrisonName = "";//驻防部队
    private int honner = 0;
    private List<Integer> posResource = new ArrayList<>();
    private int resourceMapId = 0;
    private int totalResourceNum = 0;
    private List<Integer> defendSoldierTypeIds = new ArrayList<>();
    private List<Integer> defendSoldierNums = new ArrayList<>();
    private int attackVip = 0;
    private int defendVip = 0;
    private String defendLegion = "";
    private String attackLegion = "";
    private int attackExp = 0;
    private int defendExp = 0;
    private List<Integer> attackSoldierTypeIds = new ArrayList<>();
    private List<Integer> attackSoldierNums = new ArrayList<>();
    private int defendAdviserIcondId = 0;
    private int defendAdviserSkillId = 0;
    private String defendAdviserName = "";
    private String defendAdviserSkillName = "";
    private int attackAdviserIcondId = 0;
    private int attackAdviserSkillId = 0;
    private String attackAdviserName = "";
    private String attackAdviserSkillName = "";
    private int resourceGet = 0;//已经采集
    private int firstHand = 0;//先手
    private int attackCityIcon = 0;//攻击方的图标
    private int attackAddBoom = 0;//攻击方的繁荣度加成,可能为负值
    private int attackTotalBoom = 0;//攻击方的总繁荣度
    private int attackCurrBoom = 0;//攻击方当前繁荣度
    private int defentAddBoom = 0;//防守方的繁荣度加成,可能为负值
    private int defentTotalBoom = 0;//防守方的总繁荣度
    private int defentCurrBoom = 0;//防守方当前繁荣度
    private int defentIcon = 0;//防守方的图标
    private String reward = "";//奖励的打包字符串
    private long createTime = 0l;
    private String aim = "";
    private long garrisonId = 0l;
    private int defendLevel = 0;
    private int read = 2;//是否已读（竞技场使用）
    public int defendX = -1;
    public int defendY = -1;


    public int getDefendX() {
        return defendX;
    }

    public void setDefendX(int defendX) {
        this.defendX = defendX;
    }

    public int getDefendY() {
        return defendY;
    }

    public void setDefendY(int defendY) {
        this.defendY = defendY;
    }

    public int getDefendLevel() {
        return defendLevel;
    }

    public void setDefendLevel(int defendLevel) {
        this.defendLevel = defendLevel;
    }

    public long getGarrisonId() {
        return garrisonId;
    }

    public void setGarrisonId(long garrisonId) {
        this.garrisonId = garrisonId;
    }

    public String getAim() {
        return aim;
    }

    public void setAim(String aim) {
        this.aim = aim;
    }

    public long getAttackerId() {
        return attackerId;
    }

    public void setAttackerId(long attackerId) {
        this.attackerId = attackerId;
    }

    public String getAttackerName() {
        return attackerName;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getGarrisonName() {
        return garrisonName;
    }

    public void setGarrisonName(String garrisonName) {
        this.garrisonName = garrisonName;
    }

    public int getHonner() {
        return honner;
    }

    public void setHonner(int honner) {
        this.honner = honner;
    }

    public List<Integer> getPosResource() {
        return posResource;
    }

    public void setPosResource(List<Integer> posResource) {
        this.posResource = posResource;
    }

    public int getResourceMapId() {
        return resourceMapId;
    }

    public void setResourceMapId(int resourceMapId) {
        this.resourceMapId = resourceMapId;
    }

    public int getTotalResourceNum() {
        return totalResourceNum;
    }

    public void setTotalResourceNum(int totalResourceNum) {
        this.totalResourceNum = totalResourceNum;
    }

    public List<Integer> getDefendSoldierTypeIds() {
        return defendSoldierTypeIds;
    }

    public void setDefendSoldierTypeIds(List<Integer> defendSoldierTypeIds) {
        this.defendSoldierTypeIds = defendSoldierTypeIds;
    }

    public List<Integer> getDefendSoldierNums() {
        return defendSoldierNums;
    }

    public void setDefendSoldierNums(List<Integer> defendSoldierNums) {
        this.defendSoldierNums = defendSoldierNums;
    }

    public int getAttackVip() {
        return attackVip;
    }

    public void setAttackVip(int attackVip) {
        this.attackVip = attackVip;
    }

    public int getDefendVip() {
        return defendVip;
    }

    public void setDefendVip(int defendVip) {
        this.defendVip = defendVip;
    }

    public String getDefendLegion() {
        return defendLegion;
    }

    public void setDefendLegion(String defendLegion) {
        this.defendLegion = defendLegion;
    }

    public String getAttackLegion() {
        return attackLegion;
    }

    public void setAttackLegion(String attackLegion) {
        this.attackLegion = attackLegion;
    }

    public int getAttackExp() {
        return attackExp;
    }

    public void setAttackExp(int attackExp) {
        this.attackExp = attackExp;
    }

    public int getDefendExp() {
        return defendExp;
    }

    public void setDefendExp(int defendExp) {
        this.defendExp = defendExp;
    }

    public List<Integer> getAttackSoldierTypeIds() {
        return attackSoldierTypeIds;
    }

    public void setAttackSoldierTypeIds(List<Integer> attackSoldierTypeIds) {
        this.attackSoldierTypeIds = attackSoldierTypeIds;
    }

    public List<Integer> getAttackSoldierNums() {
        return attackSoldierNums;
    }

    public void setAttackSoldierNums(List<Integer> attackSoldierNums) {
        this.attackSoldierNums = attackSoldierNums;
    }

    public int getDefendAdviserIcondId() {
        return defendAdviserIcondId;
    }

    public void setDefendAdviserIcondId(int defendAdviserIcondId) {
        this.defendAdviserIcondId = defendAdviserIcondId;
    }

    public int getDefendAdviserSkillId() {
        return defendAdviserSkillId;
    }

    public void setDefendAdviserSkillId(int defendAdviserSkillId) {
        this.defendAdviserSkillId = defendAdviserSkillId;
    }

    public String getDefendAdviserName() {
        return defendAdviserName;
    }

    public void setDefendAdviserName(String defendAdviserName) {
        this.defendAdviserName = defendAdviserName;
    }

    public String getDefendAdviserSkillName() {
        return defendAdviserSkillName;
    }

    public void setDefendAdviserSkillName(String defendAdviserSkillName) {
        this.defendAdviserSkillName = defendAdviserSkillName;
    }

    public int getAttackAdviserIcondId() {
        return attackAdviserIcondId;
    }

    public void setAttackAdviserIcondId(int attackAdviserIcondId) {
        this.attackAdviserIcondId = attackAdviserIcondId;
    }

    public int getAttackAdviserSkillId() {
        return attackAdviserSkillId;
    }

    public void setAttackAdviserSkillId(int attackAdviserSkillId) {
        this.attackAdviserSkillId = attackAdviserSkillId;
    }

    public String getAttackAdviserName() {
        return attackAdviserName;
    }

    public void setAttackAdviserName(String attackAdviserName) {
        this.attackAdviserName = attackAdviserName;
    }

    public String getAttackAdviserSkillName() {
        return attackAdviserSkillName;
    }

    public void setAttackAdviserSkillName(String attackAdviserSkillName) {
        this.attackAdviserSkillName = attackAdviserSkillName;
    }

    public void setAttackerName(String attackerName) {
        this.attackerName = attackerName;
    }

    public long getDefendId() {
        return defendId;
    }

    public void setDefendId(long defendId) {
        this.defendId = defendId;
    }

    public String getDefendName() {
        return defendName;
    }

    public void setDefendName(String defendName) {
        this.defendName = defendName;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public int getReportType() {
        return reportType;
    }

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    public int getResourceGet() {
        return resourceGet;
    }

    public void setResourceGet(int resourceGet) {
        this.resourceGet = resourceGet;
    }

    public int getAttackCityIcon() {
        return attackCityIcon;
    }

    public void setAttackCityIcon(int attackCityIcon) {
        this.attackCityIcon = attackCityIcon;
    }

    public int getAttackAddBoom() {
        return attackAddBoom;
    }

    public void setAttackAddBoom(int attackAddBoom) {
        this.attackAddBoom = attackAddBoom;
    }

    public int getAttackTotalBoom() {
        return attackTotalBoom;
    }

    public void setAttackTotalBoom(int attackTotalBoom) {
        this.attackTotalBoom = attackTotalBoom;
    }

    public int getAttackCurrBoom() {
        return attackCurrBoom;
    }

    public void setAttackCurrBoom(int attackCurrBoom) {
        this.attackCurrBoom = attackCurrBoom;
    }

    public int getDefentAddBoom() {
        return defentAddBoom;
    }

    public void setDefentAddBoom(int defentAddBoom) {
        this.defentAddBoom = defentAddBoom;
    }

    public int getDefentTotalBoom() {
        return defentTotalBoom;
    }

    public void setDefentTotalBoom(int defentTotalBoom) {
        this.defentTotalBoom = defentTotalBoom;
    }

    public int getDefentCurrBoom() {
        return defentCurrBoom;
    }

    public void setDefentCurrBoom(int defentCurrBoom) {
        this.defentCurrBoom = defentCurrBoom;
    }

    public int getDefentIcon() {
        return defentIcon;
    }

    public void setDefentIcon(int defentIcon) {
        this.defentIcon = defentIcon;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public int getFirstHand() {
        return firstHand;
    }

    public void setFirstHand(int firstHand) {
        this.firstHand = firstHand;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }
}
