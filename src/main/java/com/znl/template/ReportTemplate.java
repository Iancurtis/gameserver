package com.znl.template;

import com.google.protobuf.GeneratedMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/15.
 */
public class ReportTemplate {
    public long attackId ;
    public String attackName="";
    public long defendId;
    public String defendName="";
    public int reportType;
    public int level;
    public int attackLevel;
    public String name ="";
    public int result; //0:进攻赢了，1进攻输了
    public int x;
    public int y;
    public int attackX;
    public int attackY;
    public int defendX;
    public int defendY;
    public String garrisonName="";//驻防部队
    public int honner;
    public List<Integer> posResource = new ArrayList<>();
    public int resourceMapId;
    public int totalResourceNum;
    public List<Integer> defendSoldierTypeIds = new ArrayList<>();
    public List<Integer> defendSoldierNums = new ArrayList<>();
    public int attackVip;
    public int defendVip;
    public String defendLegion="";
    public String attackLegion="";
    public int attackExp;//编制经验
    public int defendExp;//编制经验
    public List<Integer> attackSoldierTypeIds = new ArrayList<>();
    public List<Integer> attackSoldierNums = new ArrayList<>();
    public int defendAdviserIcondId;
    public int defendAdviserSkillId;
    public String defendAdviserName="";
    public String defendAdviserSkillName="";
    public int attackAdviserIcondId;
    public int attackAdviserSkillId;
    public String attackAdviserName="";
    public String attackAdviserSkillName="";
    private int firstHand;//先手
    public int resourceGet;//已经采集
    public int attackCityIcon;//攻击方的图标
    public int attackAddBoom;//攻击方的繁荣度加成,可能为负值
    public int attackTotalBoom;//攻击方的总繁荣度
    public int attackCurrBoom;//攻击方当前繁荣度
    public int defentAddBoom;//防守方的繁荣度加成,可能为负值
    public int defentTotalBoom;//防守方的总繁荣度
    public int defentCurrBoom;//防守方当前繁荣度
    public int defentIcon;//防守方的图标
    public String reward = "";
    public GeneratedMessage message;
    public String aim="";//目标
    public long garrisonid=0l;


    public ReportTemplate(long attackId, String attackName, long defendId, String defendName, int reportType, GeneratedMessage message) {
        this.attackId = attackId;
        this.attackName = attackName;
        this.defendId = defendId;
        this.defendName = defendName;
        this.reportType = reportType;
        this.message = message;
    }

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

    public long getGarrisonid() {
        return garrisonid;
    }

    public void setGarrisonid(long garrisonid) {
        this.garrisonid = garrisonid;
    }

    public String getAim() {
        return aim;
    }

    public void setAim(String aim) {
        this.aim = aim;
    }

    public int getAttackLevel() {
        return attackLevel;
    }

    public void setAttackLevel(int attackLevel) {
        this.attackLevel = attackLevel;
    }

    public long getAttackId() {
        return attackId;
    }

    public void setAttackId(long attackId) {
        this.attackId = attackId;
    }

    public String getAttackName() {
        return attackName;
    }

    public void setAttackName(String attackName) {
        this.attackName = attackName;
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

    public int getReportType() {
        return reportType;
    }

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    public GeneratedMessage getMessage() {
        return message;
    }

    public void setMessage(GeneratedMessage message) {
        this.message = message;
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

    public int getAttackX() {
        return attackX;
    }

    public void setAttackX(int attackX) {
        this.attackX = attackX;
    }

    public int getAttackY() {
        return attackY;
    }

    public void setAttackY(int attackY) {
        this.attackY = attackY;
    }
}
