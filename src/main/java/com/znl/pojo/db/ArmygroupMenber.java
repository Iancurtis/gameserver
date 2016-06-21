package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.msg.GameMsg;
import com.znl.utils.GameUtils;

import java.util.List;

/**
 * Created by Administrator on 2015/11/16.
 */
public class ArmygroupMenber extends BaseDbPojo {
    private String accoutName;
    private int contribute;  //贡献值
    private  long capity;   //玩家战力
    private Long  playerId;
    private int level; //等级
    private String name;//玩家名字
    private int sex;//玩家性别
    private long logintime= GameUtils.getServerDate().getTime();//登陆时间
    private long outlinetime=GameUtils.getServerDate().getTime();//下线时间
    private long armyId;
    private int job;
    private int capityrank; //战力排名
    private int devotrank; //贡献排名
    private int vitality; //活跃度
    private int donatecontributeWeek=0;  //捐献周贡献
    private int contributeWeek=0;  //打副本贡献
    private int tael = 0;
    private int iron = 0;
    private int wood = 0;
    private int stones = 0;
    private int food = 0;
    private int activityrank;
    private int isgetwelfare=0; //0还没领1领过了  福利院
    private int pendantId=0;//挂件Id
    private int icon=0;
    private long joinTime=GameUtils.getServerDate().getTime();//加入时间
    private int activitycontributerank;//活动期间贡献值

    public int getActivitycontributerank() {
        return activitycontributerank;
    }

    public void setActivitycontributerank(int activitycontributerank) {
        this.activitycontributerank = activitycontributerank;
    }
    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getPendantId() {
        return pendantId;
    }

    public void setPendantId(int pendantId) {
        this.pendantId = pendantId;
    }

    public int getContributeWeek() {
        return contributeWeek;
    }

    public void setContributeWeek(int contributeWeek) {
        this.contributeWeek = contributeWeek;
    }

    public int getActivityrank() {
        return activityrank;
    }

    public int getIsgetwelfare() {
        return isgetwelfare;
    }

    public void setIsgetwelfare(int isgetwelfare) {
        this.isgetwelfare = isgetwelfare;
    }

    public void setActivityrank(int activityrank) {
        this.activityrank = activityrank;
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

    public int getCapityrank() {
        return capityrank;
    }

    public void setCapityrank(int capityrank) {
        this.capityrank = capityrank;
    }

    public int getDevotrank() {
        return devotrank;
    }

    public void setDevotrank(int devotrank) {
        this.devotrank = devotrank;
    }

    public int getDonatecontributeWeek() {
        return donatecontributeWeek;
    }

    public void setDonatecontributeWeek(int donatecontributeWeek) {
        this.donatecontributeWeek = donatecontributeWeek;
    }

    public int getVitality() {
        return vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public String getAccoutName() {
        return accoutName;
    }

    public void setAccoutName(String accoutName) {
        this.accoutName = accoutName;
    }

    public int getContribute() {
        return contribute;
    }

    public void setContribute(int contribute) {
        this.contribute = contribute;
    }


    public long getCapity() {
        return capity;
    }

    public void setCapity(long capity) {
        this.capity = capity;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }


    public long getLogintime() {
        return logintime;
    }

    public void setLogintime(long logintime) {
        this.logintime = logintime;
    }

    public long getOutlinetime() {
        return outlinetime;
    }

    public void setOutlinetime(long outlinetime) {
        this.outlinetime = outlinetime;
    }

    public long getArmyId() {
        return armyId;
    }

    public void setArmyId(long armyId) {
        this.armyId = armyId;
    }
}
