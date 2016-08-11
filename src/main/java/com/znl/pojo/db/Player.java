package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;
import com.znl.utils.GameUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by woko on 2015/10/7.
 */
public class Player extends BaseDbPojo implements Serializable {

    private String accountName = "";
    private Integer areaId = 0;
    private Integer level = 0;
    private String name = "";
    private Integer sex = 0;
    private Integer icon = 0;
    private Integer pendant = 0;
    private Long exp = 0l;
    private Integer energy = 0;
    private Long vipExp = 0l;
    private Integer vipLevel = 0;
    private Long prestige = 0l;
    private Long honour = 0l;
    private Long command = 0l;
    private Long boom = 0l;
    private Integer boomLevel = 0;
    private Long boomUpLimit = 0l;
    private Integer boomState = 0;
    private long boomRefTime = 0;
    private Long tael = 0l;
    private Long iron = 0l;
    private Long wood = 0l;
    private Long stones = 0l;
    private Long food = 0l;
    private Long gold = 0l;
    private Long capacity = 0l;
    private Integer militaryRank = 0;
    private Integer arenaGrade = 0;
    private Integer commandLevel = 0;
    private Integer prestigeLevel = 0;
    private Long giftgold = 0l;
    private Integer equipsize = 0;
    private Long buildingId = -1L;
    private Integer dungeoId = 0;//当前最高副本id
    private Integer buildsize = 0;
    private Integer worldResouceLevel = 0;//攻打最高世界资源等级
    private Long arena = 0l;
    private Integer loginTime = 0;//登录时间
    private Integer loginLevel = 0;//登录等级
    private Integer totalCharge = 0;//累计充值
    private long firstChargeTime = 0l;//首次充值时间
    private long lastChargeTime = 0l;//最后一次充值时间
    private Long depotprotect = 0l;//仓库保护量
    private Integer settingAutoAddDefendlist = 1;//自动补充防守阵型
    private Long armygroupId = 0l;//军团id
    private Set<Long> applyArmylist = new HashSet<Long>();
    private Integer post = 0;//职位
    private Integer atklv = 0; //攻击装备最高等级
    private Integer critlv = 0;//暴击装备最高等级
    private Integer dogelv = 0;//闪避装备最高等级
    private long protectOverDate = 01;//免战保护过期时间
    private Integer loginDayNum = 0;//登录天数
    private List<Integer> rewardNum = new ArrayList<>();//领取登录30天的未领取数
    private Long loginOutTime = 0l;//登出时间
    private Long armmenberid = 0l;//juntuanchengyuanid
    private int worldTileX = -1;
    private int worldTileY = -1;
    private long active = 0l;
    private Integer banAct = 0;  //禁号状态 0解禁 1封禁
    private Integer banChatAct = 0;//禁言状态 0解禁 1封禁
    private Integer banActDate = 0; //禁号时间
    private Integer banChatActDate = 0; //禁言时间
    private Integer limitChangeMaxId = 1;//极限挑战 Id
    private Integer getLimitChangeId = 1;//当前Id
    private int regTime = 0; //创角时间
    private Integer lastLoginTime = 0; //最后登录时间
    private String legionName = "";//军团名字
    private int facade = 0;//外观道具Id
    private long facadeendTime = 0l;//外观道具结束时间
    private int faceIcon = 0;
    private Set<Long> outLevelTime = new HashSet<Long>();//下线前还在升级的建筑完成时间
    private int autoBuild = 0;//0关闭1开启
    private long autoBuildendTime = 0l;//自动升结束时间
    private int legionLevel = 1;//军团等级
    private Set<Long> colsets = new HashSet<Long>();
    private String platName = "unknown";
    private String cdkeyStr = "";
    private Set<Long> helpteams = new HashSet<Long>();
    private Set<Long> outhelpteams = new HashSet<Long>();
    private long usedefine = 0l;
    private Set<Integer> remianset = new HashSet<Integer>();
    private String pushId = "";
    private int gardNum = 0;//驻军数量
    private int playerType = 0;//0:正常玩家，1新手指导员
    private int typeBeginTime = 0;
    private int typeEndTime = 0;
    private int haveGetNewGift = 0;
    private String laterPeople = "";//_隔开
    private Set<Long> resouceLeve = new HashSet<>();
    private int activitycontributerank = 0;//活动贡献排名
    private long friendbleestimeId = 0l;//好友被祝福的定时器
    private Set<Long> advids = new HashSet<Long>();

    private Set<Long> productions = new HashSet<>();
    private int resetDataTime = 0;//重置时间(4点)
    private long resourereftime = GameUtils.getServerDate().getTime();
    private int junshigoldtimes = 0;//军师金币抽奖次数  四点耍刷新
    private int junshiresoucetimes = 0;//军师金币抽奖次数   四点耍刷新
    private long arenaId = 0l;//竞技场id
    private int zeroTime = 0;//零点重置时间

    public int getZeroTime() {
        return zeroTime;
    }

    public void setZeroTime(int zeroTime) {
        this.zeroTime = zeroTime;
    }

    public long getArenaId() {
        return arenaId;
    }

    public void setArenaId(long arenaId) {
        this.arenaId = arenaId;
    }

    private int dungeolimitrest = 0;//极限副本重置次数  四点耍刷新
    private int dungeolimitchange = 0;//极限副本挑战次数  四点耍刷新
    private int dungeolimitmoptimes = 0;//极限副本扫荡
    private long energyaddtime = 0;//体力恢复定时器
    private long equipLottertime3 = 0;//顶级武将抽奖时间
    private long equipLottertime2 = 0;//高级武将抽奖时间
    private long equipLottertime1 = 0;//普通武将抽奖时间
    private long dungeolimitmop = GameUtils.getServerDate().getTime();//极限副本扫荡
    private int lottertime1 = 0;//普通武将已抽次数
    private int lottertime2 = 0;//高级武将已抽次数
    private int lottertime3 = 0;//顶级武将已抽次数
    private int firstlotter = 1;//第一次抽必出紫将
    private int prestaeReward = 0;//登陆自动领取声望 0为未领取 1为领取  四点耍刷新
    private int prestaeshouxun = 0;//授勋声望 0为未领取 1为领取  四点耍刷新
    private long taskTimerId = 0;//任务定时器id
    private long legionTimerId = 0;//军团定时器id
    private List<Integer> getbox = new ArrayList<Integer>();//每日已领军团副本宝箱
    private int armygroupdungeotimes = 5;//军团副本剩余攻打次数
    private int everylottery = 0;//每日抽奖   四点耍刷新
    private int firthlogin = 0;//每日登录 四点耍刷新
    private int buyenergytimes = 0;//购买体力次数
    private int labafree = 0;//拉霸免费次数  四点耍刷新
    private int daybless;//每日的祝福可获取奖励的次数
    private int getbless;//每日可领取祝福奖励的次数
    private int taobaofree;//探宝免费
    private int onlinetime;//在线累计时长
    private Set<Long> worldResPoint = new HashSet<>();//野外占领的据点id 以x*1000+y的形式

    public Set<Long> getWorldResPoint() {
        return worldResPoint;
    }

    public void setWorldResPoint(Set<Long> worldResPoint) {
        this.worldResPoint = worldResPoint;
    }

    public void addtWorldResPoint(Long pointKey) {
        this.worldResPoint.add(pointKey);
    }

    public void removeWorldResPoint(Long pointKey) {
        this.worldResPoint.remove(pointKey);
    }

    public int getTaobaofree() {
        return taobaofree;
    }

    public int getFirstlotter() {
        return firstlotter;
    }

    public void setFirstlotter(int firstlotter) {
        this.firstlotter = firstlotter;
    }

    public void setTaobaofree(int taobaofree) {
        this.taobaofree = taobaofree;
    }

    public int getOnlinetime() {
        return onlinetime;
    }

    public void setOnlinetime(int onlinetime) {
        this.onlinetime = onlinetime;
    }

    public int getLabafree() {
        return labafree;
    }

    public void setLabafree(int labafree) {
        this.labafree = labafree;
    }

    public int getDaybless() {
        return daybless;
    }

    public void setDaybless(int daybless) {
        this.daybless = daybless;
    }

    public int getGetbless() {
        return getbless;
    }

    public void setGetbless(int getbless) {
        this.getbless = getbless;
    }

    public int getFirthlogin() {
        return firthlogin;
    }

    public void setFirthlogin(int firthlogin) {
        this.firthlogin = firthlogin;
    }


    public int getEverylottery() {
        return everylottery;
    }

    public void setEverylottery(int everylottery) {
        this.everylottery = everylottery;
    }

    public int getBuyenergytimes() {
        return buyenergytimes;
    }

    public void setBuyenergytimes(int buyenergytimes) {
        this.buyenergytimes = buyenergytimes;
    }

    public List<Integer> getGetbox() {
        return getbox;
    }

    public void setGetbox(List<Integer> getbox) {
        this.getbox = getbox;
    }

    public int getArmygroupdungeotimes() {
        return armygroupdungeotimes;
    }

    public void setArmygroupdungeotimes(int armygroupdungeotimes) {
        this.armygroupdungeotimes = armygroupdungeotimes;
    }

    public long getLegionTimerId() {
        return legionTimerId;
    }

    public void setLegionTimerId(long legionTimerId) {
        this.legionTimerId = legionTimerId;
    }

    public long getTaskTimerId() {
        return taskTimerId;
    }

    public void setTaskTimerId(long taskTimerId) {
        this.taskTimerId = taskTimerId;
    }

    public int getPrestaeshouxun() {
        return prestaeshouxun;
    }

    public void setPrestaeshouxun(int prestaeshouxun) {
        this.prestaeshouxun = prestaeshouxun;
    }

    public int getPrestaeReward() {
        return prestaeReward;
    }

    public void setPrestaeReward(int prestaeReward) {
        this.prestaeReward = prestaeReward;
    }

    private int lottereedtime3 = 0;//顶级武将已抽次数（再抽几次出紫）

    public int getLottereedtime3() {
        return lottereedtime3;
    }

    public void setLottereedtime3(int lottereedtime3) {
        this.lottereedtime3 = lottereedtime3;
    }

    public int getLottertime1() {
        return lottertime1;
    }

    public void setLottertime1(int lottertime1) {
        this.lottertime1 = lottertime1;
    }

    public int getLottertime2() {
        return lottertime2;
    }

    public void setLottertime2(int lottertime2) {
        this.lottertime2 = lottertime2;
    }

    public int getLottertime3() {
        return lottertime3;
    }

    public void setLottertime3(int lottertime3) {
        this.lottertime3 = lottertime3;
    }

    public int getDungeolimitmoptimes() {
        return dungeolimitmoptimes;
    }

    public void setDungeolimitmoptimes(int dungeolimitmoptimes) {
        this.dungeolimitmoptimes = dungeolimitmoptimes;
    }

    public long getDungeolimitmop() {
        return dungeolimitmop;
    }

    public void setDungeolimitmop(long dungeolimitmop) {
        this.dungeolimitmop = dungeolimitmop;
    }

    public int getDungeolimitrest() {
        return dungeolimitrest;
    }

    public void setDungeolimitrest(int dungeolimitrest) {
        this.dungeolimitrest = dungeolimitrest;
    }

    public int getDungeolimitchange() {
        return dungeolimitchange;
    }

    public void setDungeolimitchange(int dungeolimitchange) {
        this.dungeolimitchange = dungeolimitchange;
    }

    public int getResetDataTime() {
        return resetDataTime;
    }

    public void setResetDataTime(int resetDataTime) {
        this.resetDataTime = resetDataTime;
    }

    public long getEquipLottertime1() {
        return equipLottertime1;
    }

    public void setEquipLottertime1(long equipLottertime1) {
        this.equipLottertime1 = equipLottertime1;
    }

    public long getEquipLottertime2() {
        return equipLottertime2;
    }

    public void setEquipLottertime2(long equipLottertime2) {
        this.equipLottertime2 = equipLottertime2;
    }

    public long getEquipLottertime3() {
        return equipLottertime3;
    }

    public void setEquipLottertime3(long equipLottertime3) {
        this.equipLottertime3 = equipLottertime3;
    }

    public int getJunshigoldtimes() {
        return junshigoldtimes;
    }

    public void setJunshigoldtimes(int junshigoldtimes) {
        this.junshigoldtimes = junshigoldtimes;
    }

    public int getJunshiresoucetimes() {
        return junshiresoucetimes;
    }

    public void setJunshiresoucetimes(int junshiresoucetimes) {
        this.junshiresoucetimes = junshiresoucetimes;
    }

    public long getResourereftime() {
        return resourereftime;
    }

    public void setResourereftime(long resourereftime) {
        this.resourereftime = resourereftime;
    }

    public long getEnergyaddtime() {
        return energyaddtime;
    }

    public void setEnergyaddtime(long energyaddtime) {
        this.energyaddtime = energyaddtime;
    }

    public void setRegTime(int regTime) {
        this.regTime = regTime;
    }

    public Set<Long> getAdvids() {
        return advids;
    }

    public void setAdvids(Set<Long> advids) {
        this.advids = advids;
    }

    public long getFriendbleestimeId() {
        return friendbleestimeId;
    }

    public void setFriendbleestimeId(long friendbleestimeId) {
        this.friendbleestimeId = friendbleestimeId;
    }

    public int getActivitycontributerank() {
        return activitycontributerank;
    }

    public void setActivitycontributerank(int activitycontributerank) {
        this.activitycontributerank = activitycontributerank;
    }

    public String getLaterPeople() {
        return laterPeople;
    }

    public void setLaterPeople(String laterPeople) {
        this.laterPeople = laterPeople;
    }

    public Set<Long> getResouceLeve() {
        return resouceLeve;
    }

    public void setResouceLeve(Set<Long> resouceLeve) {
        this.resouceLeve = resouceLeve;
    }


    public int getHaveGetNewGift() {
        return haveGetNewGift;
    }

    public void setHaveGetNewGift(int haveGetNewGift) {
        this.haveGetNewGift = haveGetNewGift;
    }

    public int getTypeBeginTime() {
        return typeBeginTime;
    }

    public void setTypeBeginTime(int typeBeginTime) {
        this.typeBeginTime = typeBeginTime;
    }

    public int getTypeEndTime() {
        return typeEndTime;
    }

    public void setTypeEndTime(int typeEndTime) {
        this.typeEndTime = typeEndTime;
    }

    public int getPlayerType() {
        return playerType;
    }

    public void setPlayerType(int playerType) {
        this.playerType = playerType;
    }

    public int getGardNum() {
        return gardNum;
    }

    public void setGardNum(int gardNum) {
        this.gardNum = gardNum;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public Set<Integer> getRemianset() {
        return remianset;
    }

    public void setRemianset(Set<Integer> remianset) {
        this.remianset = remianset;
    }

    public String getCdkeyStr() {
        return cdkeyStr;
    }

    public void setCdkeyStr(String cdkeyStr) {
        this.cdkeyStr = cdkeyStr;
    }

    public long getUsedefine() {
        return usedefine;
    }

    public void setUsedefine(long usedefine) {
        this.usedefine = usedefine;
    }

    public Set<Long> getHelpteams() {
        return helpteams;
    }

    public void setHelpteams(Set<Long> helpteams) {
        this.helpteams = helpteams;
    }

    public Set<Long> getOuthelpteams() {
        return outhelpteams;
    }

    public void setOuthelpteams(Set<Long> outhelpteams) {
        this.outhelpteams = outhelpteams;
    }

    public String getPlatName() {
        return platName;
    }

    public void setPlatName(String platName) {
        this.platName = platName;
    }

    public Set<Long> getColsets() {
        return colsets;
    }

    public void setColsets(Set<Long> colsets) {
        this.colsets = colsets;
    }

    public void setLegionLevel(int legionLevel) {
        this.legionLevel = legionLevel;
    }

    public int getAutoBuild() {
        return autoBuild;
    }

    public void setAutoBuild(int autoBuild) {
        this.autoBuild = autoBuild;
    }

    public Set<Long> getOutLevelTime() {
        return outLevelTime;
    }

    public void setOutLevelTime(Set<Long> outLevelTime) {
        this.outLevelTime = outLevelTime;
    }

    public int getFaceIcon() {
        return faceIcon;
    }

    public void setFaceIcon(int faceIcon) {
        this.faceIcon = faceIcon;
    }

    public int getFacade() {
        return facade;
    }

    public void setFacade(int facade) {
        this.facade = facade;
    }

    public long getFacadeendTime() {
        return facadeendTime;
    }

    public void setFacadeendTime(long facadeendTime) {
        this.facadeendTime = facadeendTime;
    }

    public Integer getRegTime() {
        return regTime;
    }

    public void setRegTime(Integer regTime) {
        this.regTime = regTime;
    }

    public Integer getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Integer lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getLimitChangeMaxId() {
        return limitChangeMaxId;
    }

    public void setLimitChangeMaxId(Integer limitChangeMaxId) {
        this.limitChangeMaxId = limitChangeMaxId;
    }

    public Integer getGetLimitChangeId() {
        return getLimitChangeId;
    }

    public long getAutoBuildendTime() {
        return autoBuildendTime;
    }

    public void setAutoBuildendTime(long autoBuildendTime) {
        this.autoBuildendTime = autoBuildendTime;
    }

    public void setGetLimitChangeId(Integer getLimitChangeId) {
        this.getLimitChangeId = getLimitChangeId;
    }

    public Integer getBanAct() {
        return banAct;
    }

    public void setBanAct(Integer banAct) {
        this.banAct = banAct;
    }

    public Integer getBanChatAct() {
        return banChatAct;
    }

    public void setBanChatAct(Integer banChatAct) {
        this.banChatAct = banChatAct;
    }

    public Integer getBanActDate() {
        return banActDate;
    }

    public void setBanActDate(Integer banActDate) {
        this.banActDate = banActDate;
    }

    public Integer getBanChatActDate() {
        return banChatActDate;
    }

    public void setBanChatActDate(Integer banChatActDate) {
        this.banChatActDate = banChatActDate;
    }

    public void setPost(Integer post) {
        this.post = post;
    }

    public Long getArmmenberid() {
        return armmenberid;
    }

    public void setArmmenberid(Long armmenberid) {
        this.armmenberid = armmenberid;
    }

    public Long getLoginOutTime() {
        return loginOutTime;
    }

    public void setLoginOutTime(Long loginOutTime) {
        this.loginOutTime = loginOutTime;
    }


    public Integer getLoginDayNum() {
        return loginDayNum;
    }

    public void setLoginDayNum(Integer loginDayNum) {
        this.loginDayNum = loginDayNum;
    }

    public List<Integer> getRewardNum() {
        return rewardNum;
    }

    public void setRewardNum(List<Integer> rewardNum) {
        this.rewardNum = rewardNum;
    }

    public long getProtectOverDate() {
        return protectOverDate;
    }

    public void setProtectOverDate(long protectOverDate) {
        this.protectOverDate = protectOverDate;
    }

    public Integer getAtklv() {
        return atklv;
    }

    public void setAtklv(Integer atklv) {
        this.atklv = atklv;
    }

    public Integer getCritlv() {
        return critlv;
    }

    public void setCritlv(Integer critlv) {
        this.critlv = critlv;
    }

    public Integer getDogelv() {
        return dogelv;
    }

    public void setDogelv(Integer dogelv) {
        this.dogelv = dogelv;
    }

    public Integer getLoginLevel() {
        return loginLevel;
    }

    public void setLoginLevel(Integer loginLevel) {
        this.loginLevel = loginLevel;
    }

    public Integer getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Integer loginTime) {
        this.loginTime = loginTime;
    }

    public Integer getPendant() {
        return pendant;
    }

    public void setPendant(Integer pendant) {
        this.pendant = pendant;
    }

    public Integer getBuildsize() {
        return buildsize;
    }

    public void setBuildsize(Integer buildsize) {
        this.buildsize = buildsize;
    }

    private Set<Long> soldierSet = new HashSet<>();
    private Set<Long> dungeoSet = new HashSet<>();
    private Set<Long> itemSet = new HashSet<>();
    private Set<Long> timerdbSet = new HashSet<>();
    private Set<Long> resFunBuildingSet = new HashSet<>();
    private Set<Long> formationMember1Set = new HashSet<>();
    private Set<Long> formationMember2Set = new HashSet<>();
    private Set<Long> formationMember3Set = new HashSet<>();
    private Set<Long> technologySet = new HashSet<>();
    private Set<Long> skillSet = new HashSet<>();
    private Set<Long> equipSet = new HashSet<>();
    private Set<Long> ordnancePieceSet = new HashSet<>();
    private Set<Long> ordnanceSet = new HashSet<>();
    private Set<Long> performTaskSet = new HashSet<>();
    private Set<Long> itemBuffSet = new HashSet<>();
    private Set<Long> mailSet = new HashSet<>();
    private Set<Long> taskSet = new HashSet<>();
    private Set<Long> friendSet = new HashSet<>();
    private Set<Long> blessSet = new HashSet<>(); //祝福好友的列表
    private Set<Long> beBlessSet = new HashSet<>(); //被祝福的玩家列表 请求领取时，需要判断是否在这个池里面

    private Set<Long> getBlessSet = new HashSet<>(); //领取祝福的列表  0点时 有些需要清空
    private Set<Long> activitySet = new HashSet<>();
    private Set<Long> shieldChatSet = new HashSet<>();//屏蔽聊天列表
    private Set<Long> shieldMailSet = new HashSet<>();//屏蔽邮件列表
    private Set<Long> reportSet = new HashSet<>();//战报
    private Set<Long> clientCacheSet = new HashSet<>(); //客户端保存到服务端的缓存列表
    private Set<Long> teamNoticeSet = new HashSet<>();

    public Set<Long> getTeamNoticeSet() {
        return teamNoticeSet;
    }

    public void setTeamNoticeSet(Set<Long> teamNoticeSet) {
        this.teamNoticeSet = teamNoticeSet;
    }

    public void addTeamNotice(long id) {
        this.teamNoticeSet.add(id);
    }

    public void removeTeamNotice(long id) {
        this.teamNoticeSet.remove(id);
    }

    public Set<Long> getClientCacheSet() {
        return clientCacheSet;
    }

    public void setClientCacheSet(Set<Long> clientCacheSet) {
        this.clientCacheSet = clientCacheSet;
    }

    public Set<Long> getItemBuffSet() {
        return itemBuffSet;
    }

    public void setItemBuffSet(Set<Long> itemBuffSet) {
        this.itemBuffSet = itemBuffSet;
    }

    public Set<Long> getPerformTaskSet() {
        return performTaskSet;
    }

    public void setPerformTaskSet(Set<Long> performTaskSet) {
        this.performTaskSet = performTaskSet;
    }

    public void addodId(long id) {
        ordnanceSet.add(id);
    }

    public void removedId(long id) {
        ordnanceSet.remove(id);
    }

    public Set<Long> getOrdnanceSet() {
        return ordnanceSet;
    }

    public void setOrdnanceSet(Set<Long> ordnanceSet) {
        this.ordnanceSet = ordnanceSet;
    }

    public Set<Long> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<Long> skillSet) {
        this.skillSet = skillSet;
    }

    public Set<Long> getTechnologySet() {
        return technologySet;
    }

    public void setTechnologySet(Set<Long> technologySet) {
        this.technologySet = technologySet;
    }

    public Integer getPrestigeLevel() {
        return prestigeLevel;
    }

    public void setPrestigeLevel(Integer prestigeLevel) {
        this.prestigeLevel = prestigeLevel;
    }

    public Long getBoomUpLimit() {
        return boomUpLimit;
    }

    public void setBoomUpLimit(Long boomUpLimit) {
        this.boomUpLimit = boomUpLimit;
    }

    public Integer getBoomLevel() {
        return boomLevel;
    }

    public void setBoomLevel(Integer boomLevel) {
        this.boomLevel = boomLevel;
    }

    public Integer getCommandLevel() {
        return commandLevel;
    }

    public void setCommandLevel(Integer commandLevel) {
        this.commandLevel = commandLevel;
    }

    public Long getCapacity() {
        return capacity;
    }

    public void setCapacity(Long capacity) {
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
        if(this.areaId>0&&areaId==0){
            System.err.println("3333333bbbbb");
        }
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        if (level==0){
            System.out.println("！！！玩家"+getId()+"等级要被设置为0了");
        }
        if(getId()==20691&&level==0){
            System.err.println("4444444444444444444444444444444444");
        }
        this.level = level;
    }

    public Set<Long> getSoldierSet() {
        return soldierSet;
    }

    public Long getDepotprotect() {
        return depotprotect;
    }

    public void setDepotprotect(Long depotprotect) {
        this.depotprotect = depotprotect;
    }

    public void setSoldierSet(Set<Long> soldierSet) {
        this.soldierSet = soldierSet;
    }

    public Set<Long> getItemSet() {
        return itemSet;
    }

    public Set<Long> getTimerdbSet() {
        return timerdbSet;
    }

    public Set<Long> getEquipSet() {
        return equipSet;
    }

    public Set<Long> getOrdnancePieceSet() {
        return ordnancePieceSet;
    }

    public void setOrdnancePieceSet(Set<Long> ordnancePieceSet) {
        this.ordnancePieceSet = ordnancePieceSet;
    }

    public void setEquipSet(Set<Long> equipSet) {
        this.equipSet = equipSet;
    }

    public Set<Long> getResFunBuildingSet() {
        return resFunBuildingSet;
    }

    public void setResFunBuildingSet(Set<Long> resFunBuildingSet) {
        this.resFunBuildingSet = resFunBuildingSet;
    }

    public void setTimerdbSet(Set<Long> timerdbSet) {
        this.timerdbSet = timerdbSet;
    }

    public void setItemSet(Set<Long> itemSet) {
        this.itemSet = itemSet;
    }

    public void addSoldierId(long soldierId) {
        this.soldierSet.add(soldierId);
    }

    public void addItemId(long itemId) {
        this.itemSet.add(itemId);
    }

    public void addItemBuffId(long itemBuffId) {
        this.itemBuffSet.add(itemBuffId);
    }

    public void reduceItemBuffId(long itemBuffId) {
        this.itemBuffSet.remove(itemBuffId);
    }

    public void addTimeId(long timeId) {
        this.timerdbSet.add(timeId);
    }

    public void addTechnologyId(long technologyId) {
        this.technologySet.add(technologyId);
    }

    public void addPerformTaskId(long performTaskId) {
        this.performTaskSet.add(performTaskId);
    }

    public Integer getWorldResouceLevel() {
        return worldResouceLevel;
    }

    public void setWorldResouceLevel(Integer worldResouceLevel) {
        this.worldResouceLevel = worldResouceLevel;
    }

    public void reducePerformTaskId(long performTaskId) {
        this.performTaskSet.remove(performTaskId);
    }

    public void addSkillId(long skillId) {
        this.skillSet.add(skillId);
    }

    public void reduceTimeId(long timeId) {
        this.timerdbSet.remove(timeId);
    }

    public void addResFuId(long resFuId) {
        this.resFunBuildingSet.add(resFuId);
    }

    public void reduceSoldierId(Long soldierId) {
        this.soldierSet.remove(soldierId);
    }

    public void reduceitemId(Long itemId) {
        this.itemSet.remove(itemId);
    }

    public void reduceEquipId(Long equipId) {
        this.equipSet.remove(equipId);
    }

    public void addEquipId(Long equipId) {
        this.equipSet.add(equipId);
    }

    public void addOdpId(Long odpId) {
        this.ordnancePieceSet.add(odpId);
    }

    public void reduceOdpId(Long odpId) {
        this.ordnancePieceSet.remove(odpId);
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Integer getEnergy() {
        return energy;
    }

    public void setEnergy(Integer energy) {
        this.energy = energy;
    }

    public Long getVipExp() {
        return vipExp;
    }

    public void setVipExp(Long vipExp) {
        this.vipExp = vipExp;
    }

    public Long getPrestige() {
        return prestige;
    }

    public void setPrestige(Long prestige) {
        this.prestige = prestige;
    }

    public Long getHonour() {
        return honour;
    }

    public void setHonour(Long honour) {
        this.honour = honour;
    }

    public Long getCommand() {
        return command;
    }

    public void setCommand(Long command) {
        this.command = command;
    }

    public Long getBoom() {
        return boom;
    }

    public void setBoom(Long boom) {
        this.boom = boom;
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

    public Long getGold() {
        return gold;
    }

    public void setGold(Long gold) {
        this.gold = gold;
    }

    public Set<Long> getTaskSet() {
        return taskSet;
    }

    public void setTaskSet(Set<Long> taskSet) {
        this.taskSet = taskSet;
    }

    public void addTaskId(Long taskid) {
        this.taskSet.add(taskid);
    }

    public void reducTaskId(Long taskId) {
        this.taskSet.remove(taskId);
    }

    public Set<Long> getDungeoSet() {
        return dungeoSet;
    }

    public void setDungeoSet(Set<Long> dungeoSet) {
        this.dungeoSet = dungeoSet;
    }

    public void addDungeo(long id) {
        dungeoSet.add(id);
    }

    public void addAdviseId(long id) {
        advids.add(id);
    }

    public void removeAdviseId(long id) {
        advids.remove(id);
    }

    public int getLegionLevel() {
        if (armygroupId == 0) {
            legionLevel = 1;
        }
        return legionLevel;
    }

    public void addMail(long id) {
        mailSet.add(id);
    }

    public void removeMail(long id) {
        mailSet.remove(id);
    }

    public Integer getDungeoId() {
        return dungeoId;
    }

    public void setDungeoId(Integer dungeoId) {
        this.dungeoId = dungeoId;
    }

    public Integer getMilitaryRank() {
        return militaryRank;
    }

    public void setMilitaryRank(Integer militaryRank) {
        this.militaryRank = militaryRank;
    }

    public Integer getArenaGrade() {
        return arenaGrade;
    }

    public void setArenaGrade(Integer arenaGrade) {
        this.arenaGrade = arenaGrade;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    public Set<Long> getFormationMember1Set() {
        return formationMember1Set;
    }

    public void setFormationMember1Set(Set<Long> formationMember1Set) {
        this.formationMember1Set = formationMember1Set;
    }

    public Set<Long> getFormationMember2Set() {
        return formationMember2Set;
    }

    public void setFormationMember2Set(Set<Long> formationMember2Set) {
        this.formationMember2Set = formationMember2Set;
    }

    public Set<Long> getFormationMember3Set() {
        return formationMember3Set;
    }

    public void setFormationMember3Set(Set<Long> formationMember3Set) {
        this.formationMember3Set = formationMember3Set;
    }


    public Long getGiftgold() {
        return giftgold;
    }

    public void setGiftgold(Long giftgold) {
        this.giftgold = giftgold;
    }

    public Integer getEquipsize() {
        return equipsize;
    }

    public void setEquipsize(Integer equipsize) {
        this.equipsize = equipsize;
    }

    public Set<Long> getMailSet() {
        return mailSet;
    }

    public void setMailSet(Set<Long> mailSet) {
        this.mailSet = mailSet;
    }

    public Set<Long> getFriendSet() {
        return friendSet;
    }

    public void setFriendSet(Set<Long> friendSet) {
        this.friendSet = friendSet;
    }

    public void addFriend(Long playerId) {
        this.friendSet.add(playerId);
    }

    public void removeFriend(Long playerId) {
        this.friendSet.remove(playerId);
    }

    public Set<Long> getGetBlessSet() {
        return getBlessSet;
    }

    public void setGetBlessSet(Set<Long> getBlessSet) {
        this.getBlessSet = getBlessSet;
    }

    public Set<Long> getBlessSet() {
        return blessSet;
    }

    public void setBlessSet(Set<Long> blessSet) {
        this.blessSet = blessSet;
    }

    public Set<Long> getBeBlessSet() {
        return beBlessSet;
    }

    public void setBeBlessSet(Set<Long> beBlessSet) {
        this.beBlessSet = beBlessSet;
    }

    public void addBeBlesser(Long playerId) {
        this.beBlessSet.add(playerId);
    }

    public Set<Long> getShieldChatSet() {
        return shieldChatSet;
    }

    public void setShieldChatSet(Set<Long> shieldChatSet) {
        this.shieldChatSet = shieldChatSet;
    }

    public Set<Long> getShieldMailSet() {
        return shieldMailSet;
    }

    public void setShieldMailSet(Set<Long> shieldMailSet) {
        this.shieldMailSet = shieldMailSet;
    }

    public void addShieldMai(Long id) {
        this.shieldMailSet.add(id);
    }

    public void addShieldChat(Long id) {
        this.shieldChatSet.add(id);
    }

    public void removeShieldChat(Long id) {
        this.shieldChatSet.remove(id);
    }

    public void removeShieldMail(Long id) {
        this.shieldMailSet.remove(id);
    }

    public Set<Long> getReportSet() {
        return reportSet;
    }

    public void setReportSet(Set<Long> reportSet) {
        this.reportSet = reportSet;
    }

    public void addReport(long id) {
        this.reportSet.add(id);
    }

    public void removeReport(long id) {
        this.reportSet.remove(id);
    }

    public Long getArena() {
        return arena;
    }

    public void setArena(Long arena) {
        this.arena = arena;
    }

    public long getFirstChargeTime() {
        return firstChargeTime;
    }

    public void setFirstChargeTime(long firstChargeTime) {
        this.firstChargeTime = firstChargeTime;
    }

    public Integer getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(Integer totalCharge) {
        this.totalCharge = totalCharge;
    }

    public long getLastChargeTime() {
        return lastChargeTime;
    }

    public void setLastChargeTime(long lastChargeTime) {
        this.lastChargeTime = lastChargeTime;
    }

    public Integer getSettingAutoAddDefendlist() {
        return settingAutoAddDefendlist;
    }

    public void setSettingAutoAddDefendlist(Integer settingAutoAddDefendlist) {
        this.settingAutoAddDefendlist = settingAutoAddDefendlist;
    }

    public Long getArmygroupId() {
        return armygroupId;
    }

    public void setArmygroupId(Long armygroupId) {
        this.armygroupId = armygroupId;
    }

    public String getLegionName() {
        return legionName;
    }

    public void setLegionName(String legionName) {
        this.legionName = legionName;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public Set<Long> getApplyArmylist() {
        return applyArmylist;
    }

    public void setApplyArmylist(Set<Long> applyArmylist) {
        this.applyArmylist = applyArmylist;
    }

    public int getWorldTileY() {
        return worldTileY;
    }

    public void setWorldTileY(int worldTileY) {
        this.worldTileY = worldTileY;
    }

    public int getWorldTileX() {
        return worldTileX;
    }

    public void setWorldTileX(int worldTileX) {
        this.worldTileX = worldTileX;
    }

    public Set<Long> getActivitySet() {
        return activitySet;
    }

    public void addActivity(long id) {
        activitySet.add(id);
    }

    public void removeActivity(long id) {
        activitySet.remove(id);
    }

    public void setActivitySet(Set<Long> activitySet) {
        this.activitySet = activitySet;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }

    public Integer getBoomState() {
        return boomState;
    }

    public void setBoomState(Integer boomState) {
        this.boomState = boomState;
    }

    public long getBoomRefTime() {
        return boomRefTime;
    }

    public void setBoomRefTime(long boomRefTime) {
        this.boomRefTime = boomRefTime;
    }

    public Set<Long> getProductions() {
        return productions;
    }

    public void setProductions(Set<Long> productions) {
        this.productions = productions;
    }

    public void addProduction(long id) {
        this.productions.add(id);
    }

    public void removeProduction(long id) {
        this.productions.remove(id);
    }
}
