package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.core.PlayerTeam;

import java.util.*;

/**
 * Created by Administrator on 2015/11/16.
 */
public class Armygroup extends BaseDbPojo {
    private String name;//名字
    private String commander;//军团长
    private Long commandid;//军团长id
    private long capity ;//战力
    private int rank ;//排名
    private int level; //等级
    private int build;//建设度
    private int joinWay;//申请加入方式
    private int conditonlevel;//等级要求
    private long conditoncapity;//战力要求
    private Set<Long>  menbers = new HashSet<>();//成员 玩家id
    private Set<Long> armmenbers = new HashSet<>();//成员 玩家军团成员id
    private Set<Long>  applymenbers = new HashSet<>();//申请成员
    private String notice;//宣言
    private int sciencelevel=1; //科技馆等级
    private int welfarelevel=1;//福利院等级
    private String getlist;//id_次数;id_次数
    private List<Integer> randomShops = new ArrayList<>();// 随机商店
    private String playerrandomBuytimes;//long_id_次数;long_id_次数
    private String selfname1;//自定义职位
    private String selfname2;//自定义职位
    private String selfname3;//自定义职位
    private String selfname4;//自定义职位
    private Set<Long> techs = new HashSet<>();//军团科技
    private String fiche="" ;//军团公告
    private long nextenlistiem;//下次招募时间
    private Integer maxLegionDungeoid =0;//最高军团副本
    private List<Integer> LegionDungeoidbox = new ArrayList<>();//每天可领宝箱

    public List<Integer> getLegionDungeoidbox() {
        return LegionDungeoidbox;
    }

    public void setLegionDungeoidbox(List<Integer> legionDungeoidbox) {
        LegionDungeoidbox = legionDungeoidbox;
    }

    public Integer getMaxLegionDungeoid() {
        return maxLegionDungeoid;
    }

    public void setMaxLegionDungeoid(Integer maxLegionDungeoid) {
        this.maxLegionDungeoid = maxLegionDungeoid;
    }

    private int tael = 0;
    private int iron = 0;
    private int wood = 0;
    private int stones = 0;
    private int food = 0;
    private int vitality; //活跃度
    private int mession1;
    private int mession2;
    private int mession3;
    private int mession4;
    private int mession5;

    private int levelrank;//军团等级排行

    public int getLevelrank() {
        return levelrank;
    }

    public void setLevelrank(int levelrank) {
        this.levelrank = levelrank;
    }

    public long getNextenlistiem() {
        return nextenlistiem;
    }

    public void setNextenlistiem(long nextenlistiem) {
        this.nextenlistiem = nextenlistiem;
    }

    public String getFiche() {
        return fiche;
    }

    public void setFiche(String fiche) {
        this.fiche = fiche;
    }

    public int getTael() {
        return tael;
    }

    public void setTael(int tael) {
        this.tael = tael;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public int getStones() {
        return stones;
    }

    public void setStones(int stones) {
        this.stones = stones;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public Set<Long> getTechs() {
        return techs;
    }

    public void setTechs(Set<Long> techs) {
        this.techs = techs;
    }

    public int getMession1() {
        return mession1;
    }

    public void setMession1(int mession1) {
        this.mession1 = mession1;
    }

    public int getMession2() {
        return mession2;
    }

    public void setMession2(int mession2) {
        this.mession2 = mession2;
    }

    public int getMession3() {
        return mession3;
    }

    public void setMession3(int mession3) {
        this.mession3 = mession3;
    }

    public int getMession4() {
        return mession4;
    }

    public void setMession4(int mession4) {
        this.mession4 = mession4;
    }

    public int getMession5() {
        return mession5;
    }

    public void setMession5(int mession5) {
        this.mession5 = mession5;
    }

    public int getVitality() {
        return vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }


    public long getCapity() {
        return capity;
    }

    public void setCapity(long capity) {
        this.capity = capity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommander() {
        return commander;
    }

    public void setCommander(String commander) {
        this.commander = commander;
    }

    public Long getCommandid() {
        return commandid;
    }

    public void setCommandid(Long commandid) {
        this.commandid = commandid;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public int getJoinWay() {
        return joinWay;
    }

    public void setJoinWay(int joinWay) {
        this.joinWay = joinWay;
    }

    public int getConditonlevel() {
        return conditonlevel;
    }

    public void setConditonlevel(int conditonlevel) {
        this.conditonlevel = conditonlevel;
    }

    public long getConditoncapity() {
        return conditoncapity;
    }

    public void setConditoncapity(long conditoncapity) {
        this.conditoncapity = conditoncapity;
    }

    public void setConditoncapity(int conditoncapity) {
        this.conditoncapity = conditoncapity;
    }

    public Set<Long> getMenbers() {
        return menbers;
    }

    public void setMenbers(Set<Long> menbers) {
        this.menbers = menbers;
    }

    public Set<Long> getArmmenbers() {
        return armmenbers;
    }

    public void setArmmenbers(Set<Long> armmenbers) {
        this.armmenbers = armmenbers;
    }

    public Set<Long> getApplymenbers() {
        return applymenbers;
    }

    public void setApplymenbers(Set<Long> applymenbers) {
        this.applymenbers = applymenbers;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public int getSciencelevel() {
        return sciencelevel;
    }

    public void setSciencelevel(int sciencelevel) {
        this.sciencelevel = sciencelevel;
    }

    public int getWelfarelevel() {
        return welfarelevel;
    }

    public void setWelfarelevel(int welfarelevel) {
        this.welfarelevel = welfarelevel;
    }



    public List<Integer> getRandomShops() {
        return randomShops;
    }

    public void setRandomShops(List<Integer> randomShops) {
        this.randomShops = randomShops;
    }

    public String getGetlist() {
        return getlist;
    }

    public void setGetlist(String getlist) {
        this.getlist = getlist;
    }

    public String getPlayerrandomBuytimes() {
        return playerrandomBuytimes;
    }

    public void setPlayerrandomBuytimes(String playerrandomBuytimes) {
        this.playerrandomBuytimes = playerrandomBuytimes;
    }

    public String getSelfname4() {
        return selfname4;
    }

    public void setSelfname4(String selfname4) {
        this.selfname4 = selfname4;
    }

    public String getSelfname1() {
        return selfname1;
    }

    public void setSelfname1(String selfname1) {
        this.selfname1 = selfname1;
    }

    public String getSelfname2() {
        return selfname2;
    }

    public void setSelfname2(String selfname2) {
        this.selfname2 = selfname2;
    }

    public String getSelfname3() {
        return selfname3;
    }

    public void setSelfname3(String selfname3) {
        this.selfname3 = selfname3;
    }


}
