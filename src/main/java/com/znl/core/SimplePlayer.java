package com.znl.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 简单的玩家数据
 * Created by Administrator on 2015/12/3.
 */
public class SimplePlayer {
    private Long id = 0l;
    private String name = "";
    private String accountName = "";
    private int iconId ;
    private int level ;
    private long capacity ;
    private long boom ;
    private long boomUpLimit ;
    private int boomState;
    private long boomRefTime = 0;
    private long protectNum ;
    private Long tael = 0l;
    private Long iron = 0l;
    private Long wood = 0l;
    private Long stones = 0l;
    private Long food = 0l;
    private int enery;
    private int militaryRank ;
    private String legionName = "";//军团名字
    private int createTime; //创建时间
    private int x;
    private int y;
    private String areaKey;
    public boolean online = false;
    public PlayerTroop arenaTroop;
    private PlayerTroop defendTroop;
    private Integer settingAutoAddDefendlist;//自动补充防守阵型
    private boolean refDefendTroop = false;
    private long armygrouid ;//军团id
    private  int post;//职位
    private int atklv; //攻击装备最高等级
    private int critlv;//暴击装备最高等级
    private int dogelv;//闪避装备最高等级
    private long protectOverDate;//免战保护过期时间
    private long facadeendTime=0l;//外观道具结束时间
    private int faceIcon = 0;
    private int autobuild ; //自动升级
    private long autobuildendtime;//自动升级结束时间
    private Set<Integer> remianset=new HashSet<Integer>();
    private int gardNum;//驻军数量
    private int boomLevel;//繁荣度等级
    private Integer pendant =0;
    private int activitycontributerank;//活动军团贡献排名
    private Set<Long> appArmylist=new HashSet<Long>();//军团申请列表


    public Set<Long> getAppArmylist() {
        return appArmylist;
    }

    public void setAppArmylist(Set<Long> appArmylist) {
        this.appArmylist = appArmylist;
    }

    public int getActivitycontributerank() {
        return activitycontributerank;
    }

    public void setActivitycontributerank(int activitycontributerank) {
        this.activitycontributerank = activitycontributerank;
    }

    public int getBoomLevel() {
        return boomLevel;
    }

    public void setBoomLevel(int boomLevel) {
        this.boomLevel = boomLevel;
    }

    public Integer getPendant() {
        return pendant;
    }

    public void setPendant(Integer pendant) {
        this.pendant = pendant;
    }

    public int getGardNum() {
        return gardNum;
    }

    public void setGardNum(int gardNum) {
        this.gardNum = gardNum;
    }

    private int vipLevel;
    private Long LoginOut; //上次下线时间
    private Long logintime;//登陆时间
    private int contrbute;
    private int sex;
    private Integer getLimitChangeId;//当前Id

    private String platform = ""; //所属的渠道
    private String device = ""; //所用设备

    private long helpId=0;

    public int getEnery() {
        return enery;
    }

    public void setEnery(int enery) {
        this.enery = enery;
    }

    public Set<Integer> getRemianset() {
        return remianset;
    }

    public void setRemianset(Set<Integer> remianset) {
        this.remianset = remianset;
    }

    public long getHelpId() {
        return helpId;
    }

    public void setHelpId(long helpId) {
        this.helpId = helpId;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getFaceIcon() {
        return faceIcon;
    }

    public void setFaceIcon(int faceIcon) {
        this.faceIcon = faceIcon;
    }

    public long getFacadeendTime() {
        return facadeendTime;
    }

    public void setFacadeendTime(long facadeendTime) {
        this.facadeendTime = facadeendTime;
    }

    public Integer getGetLimitChangeId() {
        return getLimitChangeId;
    }

    public void setGetLimitChangeId(Integer getLimitChangeId) {
        this.getLimitChangeId = getLimitChangeId;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getContrbute() {
        return contrbute;
    }

    public int getAutobuild() {
        return autobuild;
    }

    public void setAutobuild(int autobuild) {
        this.autobuild = autobuild;
    }

    public long getAutobuildendtime() {
        return autobuildendtime;
    }

    public void setAutobuildendtime(long autobuildendtime) {
        this.autobuildendtime = autobuildendtime;
    }

    public void setContrbute(int contrbute) {
        this.contrbute = contrbute;
    }

    public long getProtectOverDate() {
        return protectOverDate;
    }

    public void setProtectOverDate(long protectOverDate) {
        this.protectOverDate = protectOverDate;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {


        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getBoom() {
        return boom;
    }

    public void setBoom(long boom) {
        this.boom = boom;
    }

    public long getBoomUpLimit() {
        return boomUpLimit;
    }

    public void setBoomUpLimit(long boomUpLimit) {
        this.boomUpLimit = boomUpLimit;
    }

    public int getMilitaryRank() {
        return militaryRank;
    }

    public void setMilitaryRank(int militaryRank) {
        this.militaryRank = militaryRank;
    }

    public String getLegionName() {
        return legionName;
    }

    public void setLegionName(String legionName) {
        this.legionName = legionName;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
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

    public PlayerTroop getArenaTroop() {
        return arenaTroop;
    }

    public void setArenaTroop(PlayerTroop arenaTroop) {
        this.arenaTroop = arenaTroop;
    }

    public String getAreaKey() {
        return areaKey;
    }

    public void setAreaKey(String areaKey) {
        this.areaKey = areaKey;
    }

    public PlayerTroop getDefendTroop() {
        return defendTroop;
    }

    public void setDefendTroop(PlayerTroop defendTroop) {
        this.defendTroop = defendTroop;
    }

    public long getProtectNum() {
        return protectNum;
    }

    public void setProtectNum(long protectNum) {
        this.protectNum = protectNum;
    }

    public Long getTael() {
        return tael;
    }

    public void setTael(Long tael) {
        this.tael = tael;
    }

    public Long getIron() {
        return iron;
    }

    public void setIron(Long iron) {
        this.iron = iron;
    }

    public Long getWood() {
        return wood;
    }

    public void setWood(Long wood) {
        this.wood = wood;
    }

    public Long getStones() {
        return stones;
    }

    public void setStones(Long stones) {
        this.stones = stones;
    }

    public Long getFood() {
        return food;
    }

    public void setFood(Long food) {
        this.food = food;
    }

    public Integer getSettingAutoAddDefendlist() {
        return settingAutoAddDefendlist;
    }

    public void setSettingAutoAddDefendlist(Integer settingAutoAddDefendlist) {
        this.settingAutoAddDefendlist = settingAutoAddDefendlist;
    }

    public boolean isRefDefendTroop() {
        return refDefendTroop;
    }

    public void setRefDefendTroop(boolean refDefendTroop) {
        this.refDefendTroop = refDefendTroop;
    }

    public long getArmygrouid() {
        return armygrouid;
    }

    public void setArmygrouid(long armygrouid) {
        this.armygrouid = armygrouid;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public Long getLoginOut() {
        return LoginOut;
    }

    public void setLoginOut(Long loginOut) {
        LoginOut = loginOut;
    }

    public Long getLogintime() {
        return logintime;
    }

    public void setLogintime(Long logintime) {
        this.logintime = logintime;
    }

    public int getBoomState() {
        return boomState;
    }

    public void setBoomState(int boomState) {
        this.boomState = boomState;
    }

    public long getBoomRefTime() {
        return boomRefTime;
    }

    public void setBoomRefTime(long boomRefTime) {
        this.boomRefTime = boomRefTime;
    }

}
