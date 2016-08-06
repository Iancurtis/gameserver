package com.znl.proxy;


import com.znl.GameMainServer;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.*;
import com.znl.define.*;
import com.znl.log.*;
import com.znl.log.admin.*;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Player;
import com.znl.pojo.db.Report;
import com.znl.pojo.db.set.BillOrderSetDb;
import com.znl.proto.*;
import com.znl.service.ArenaService;
import com.znl.service.PlayerService;
import com.znl.service.PowerRanksService;
import com.znl.utils.DateUtil;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/10/27.
 */
public class PlayerProxy extends BasicProxy {
    private final  Player player;
    public Map<Integer, Integer> rankmap = new ConcurrentHashMap<>();
    private  long reduceEnergytime;

    @Override
    public void shutDownProxy() {
        String name = player.getName();
        player.finalize();
    }

    @Override
    protected void init() {
    }

    private  SimplePlayer simplePlayer=new SimplePlayer();

    public void setSimplePlayer(SimplePlayer value) {
        this.simplePlayer = value;
    }

    //登录用户信息缓存
    private PlayerCache playerCache;
    //竞技场对手缓存
    public List<Integer> rivalsCache;

    public PlayerCache getPlayerCache() {
        return playerCache;
    }

    public void setPlayerCache(PlayerCache playerCache) {
        pushChannelId = playerCache.getPushChanelId();
        this.playerCache = playerCache;
        this.player.setPushId(pushChannelId);
    }

    private String pushChannelId;

    public String getPushChannelId() {
        return this.pushChannelId;
    }

    public PlayerProxy(final Player player, String areaKey) {
        this.player = player;
        this.areaKey = areaKey;
    }

    public void savePlayer() {
        player.save();
        //更新角色信息日志
        // tbllog_player
        playerLog();
    }

    //只处理INT 值的 power值
    //string Long字段较少，直接传单独字段
    public Long getPlayerPowerValue(String powerName) {
        Object value = player.getter(powerName);
        if (value instanceof Integer) {
            return (int) value + 0l;
        }
        return (long) player.getter(powerName);
    }


    public List<Long> getlaterlist() {
        List<Long> list = new ArrayList<Long>();
        String str[] = player.getLaterPeople().split("_");
        for (String strid : str) {
            if (!strid.equals("")) {
                list.add(Long.parseLong(strid));
            }
        }
        return list;
    }

    public void setLaterPlayer(long playerid) {
        String str = player.getLaterPeople();
        str = playerid + "_" + str;
        player.setLaterPeople(str);
    }

    public void setLaterPlayer(String str) {
        player.setLaterPeople(str);
    }

    public String getPlayerName() {
        return player.getName();
    }

    public int getPlayerIcon() {
        return player.getIcon();
    }


    public String getAccountName() {
        return player.getAccountName();
    }

    public Integer getLevel() {
        return player.getLevel();
    }

    public long getReduceEnergytime() {
        return reduceEnergytime;
    }

    public void setReduceEnergytime(long reduceEnergytime) {
        this.reduceEnergytime = reduceEnergytime;
    }

    public List<Common.AttrInfo> getPlayerAllPower() {
        List<Common.AttrInfo> powerList = new ArrayList<>();
        List<JSONObject> sendPowers = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RESOURCE, "isshow", 1);
        for (JSONObject powerDefine : sendPowers) {
            int powerId = powerDefine.getInt("ID");
            long powerValue = getPowerValue(powerId);
            Common.AttrInfo.Builder builder = Common.AttrInfo.newBuilder();
            builder.setTypeid(powerId);
            builder.setValue(powerValue);
            powerList.add(builder.build());
        }
        Common.AttrInfo.Builder builder = Common.AttrInfo.newBuilder();
        builder.setTypeid(PlayerPowerDefine.POWER_armygroupId);
        builder.setValue(getPowerValue(PlayerPowerDefine.POWER_armygroupId));
        powerList.add(builder.build());

        return powerList;
    }

    //是否被禁号了
    public boolean isBanAct() {
        boolean flag = false;
        if (player.getBanAct() == 1) {
            if (player.getBanActDate() >= GameUtils.getServerTime()) {
                flag = true;
            } else {
                player.setBanAct(0);
                player.setBanActDate(0);
            }
        }

        return flag;
    }

    public Long getPlayerArena() {
        return player.getArena();
    }

    public Long getArmGrouId() {
        return player.getArmygroupId();
    }

    public void setApplylist(Set<Long> list) {
        player.setApplyArmylist(list);
    }


    public void setArmgroupId(Long armgroupId) {
        player.setArmygroupId(armgroupId);
    }


    public void setLegionName(String name) {
        player.setLegionName(name);
    }

    public String getLegionName() {
        return player.getLegionName();
    }


    public Set<Long> getColSets() {
        return player.getColsets();
    }

    public void addColllecttoPlayer(long id) {
        Set<Long> sets = player.getColsets();
        sets.add(id);
        player.setColsets(sets);
    }

    public void setPost(int post) {
        player.setPost(post);
    }

    public int getPost() {
        return player.getPost();
    }

    public Long getPlayerArm() {
        return player.getArmygroupId();
    }

    public Set<Long> getPlayerApplylist() {
        return player.getApplyArmylist();
    }

    public void setPlayerArena(long arenId) {
        player.setArena(arenId);
    }

    //免战保护过期时间
    public void setProtectOverDate(long protectOverDate) {
        player.setProtectOverDate(protectOverDate);
    }

    public Long getProtectOverDate() {
        return player.getProtectOverDate();
    }

    public int getCreatePlayerTime() {
        return player.getRegTime();
    }

    //30登录领取奖励
    public void setLoginDayNum(int loginDayNum) {
        player.setLoginDayNum(loginDayNum);
    }

    public int getLoginDayNum() {
        return player.getLoginDayNum();
    }

    public List<Integer> getRewardNum() {
        initRewardNum();
        return player.getRewardNum();
    }

    public void initRewardNum() {
        List<Integer> list = player.getRewardNum();
        List<Integer> newlist = new ArrayList<Integer>();
        for (int day : list) {
            if (day <= 30) {
                newlist.add(day);
            }
        }
        player.setRewardNum(newlist);
    }

    public void addRewardNum(Integer day) {
        player.getRewardNum().add(day);
    }

    public void removeRewardNum(Integer day) {
        player.getRewardNum().remove(day);
    }

    public int getHigestDungId() {
        return player.getDungeoId();
    }

    public void setHigestDungId(int dungonId) {
        int olddungonId = player.getDungeoId();
        if (dungonId > olddungonId) {
            player.setDungeoId(dungonId);
        }
    }

    public int getWorldResourceLevel() {
        return player.getWorldResouceLevel();
    }

    public Set<Long> getClientCacheIds() {
        return player.getClientCacheSet();
    }

    public void addClientCacheId(Long id) {
        player.getClientCacheSet().add(id);
    }

    public void setWorldResourceLevel(int level) {
        int oldlevel = player.getWorldResouceLevel();
        player.setWorldResouceLevel(level);
    }


    public void setWorldResourceLevellist(Set<Long> setlist) {
        player.setResouceLeve(setlist);
    }

    public Set<Long> getWorldResourLevellist() {
        return player.getResouceLeve();
    }

    public void addSoldierToPlayer(long soldierId) {
        player.addSoldierId(soldierId);
    }

    public void removeSoldierToPlayer(long soldierId) {
        player.reduceSoldierId(soldierId);
    }

    public void addOrdnancePieceToPlayer(long opdId) {
        player.addOdpId(opdId);
    }

    public void reduceOrdnancePieceToPlayer(long opdId) {
        player.reduceOdpId(opdId);
    }

    public void addOrdnanceToPlayer(long opId) {
        player.addodId(opId);
    }

    public void reduceOrdnanceToPlayer(long opdId) {
        player.removedId(opdId);
    }

    public void addItemToPlayer(long itemId) {
        player.addItemId(itemId);
    }

    public void addSkillToPlayer(long skillId) {
        player.addSkillId(skillId);
    }

    public void reduceItemfromPlayer(long itemId) {
        player.reduceitemId(itemId);
    }

    public void addItemBuff(long itemBuffId) {
        player.addItemBuffId(itemBuffId);
    }

    public void reduceItemBuffFormPlayer(long itemBuffId) {
        player.reduceItemBuffId(itemBuffId);
    }

    public void reducePerformTaskfromPlayer(long performTaskId) {
        player.reducePerformTaskId(performTaskId);
    }

    public void addPerformTaskfromPlayer(long performTaskId) {
        player.addPerformTaskId(performTaskId);
    }

    public void addEquipToPlayer(long itemId) {
        player.addEquipId(itemId);
    }

    public void reduceEquipfromPlayer(long itemId) {
        player.reduceEquipId(itemId);
    }

    public void addResFunBuildToPlayer(long resFuId) {
        player.addResFuId(resFuId);
    }

    public void addTechnologyToPlayer(long techId) {
        player.addTechnologyId(techId);
    }

    public void addTimeIdToPlayer(long timeId) {
        player.addTimeId(timeId);
    }

    public void removeTimeIdToPlayer(long timeId) {
        player.reduceTimeId(timeId);
    }

    public void addAdviseIdToPlayer(long id) {
        player.addAdviseId(id);
    }
    public void setArenaId(long arenaId) {player.setArenaId(arenaId);}
    public void removeAdviseIdToPlayer(long id) {
        player.removeAdviseId(id);
    }

    public void addDungeoToPlayer(long dungeoId) {
        player.addDungeo(dungeoId);
    }

    public void addMailToPlayer(long mailId) {
        player.addMail(mailId);
    }

    public void removeMailToPlayer(List<Long> ids) {
        for (Long id : ids) {
            player.removeMail(id);
        }
    }


    public int getLimitChangeMaxId() {
        return player.getLimitChangeMaxId();
    }

    public void setLimitChangeMaxId(int id) {
        player.setLimitChangeMaxId(id);
    }

    public int getLimitChangeNowId() {
        return player.getGetLimitChangeId();
    }

    public void setLimitChangeNowId(int id) {
        player.setGetLimitChangeId(id);
    }

    public Player getPlayer() {
        return player;
    }

    public void addTaskIdToPlayer(long taskId) {
        player.addTaskId(taskId);
    }

    public void removeTaskIdFormPlayer(long taskId) {
        player.reducTaskId(taskId);
    }

    public Set<Long> getFriendSet() {
        return player.getFriendSet();
    }

    //添加好友
    public void addFriend(Long playerId) {
        player.addFriend(playerId);
    }

    public void removeFriend(Long playerId) {
        player.getFriendSet().remove(playerId);
    }

    //是否有好友
    public boolean isFriend(Long playerId) {
        return player.getFriendSet().contains(playerId);
    }

    //好友数
    public int friendNum() {
        return player.getFriendSet().size();
    }

    //清除掉祝福池
    public void clearBlessSet() {
        player.getBlessSet().clear();
    }

    //添加祝福的玩家 4点过后需要清空
    public void addBlessPlayerId(Long playerId) {
        player.getBlessSet().add(playerId);
    }

    //是否祝福过了
    public boolean isBlessed(Long playerId) {
        return player.getBlessSet().contains(playerId);
    }

    //接受祝福
    public void acceptBless(Long blesser) {
        player.getBeBlessSet().add(blesser);  //添加到被祝福池
    }

    //获取被祝福的数量，大于上限则不添加推送
    public int getBeBlessNum() {
        return player.getBeBlessSet().size();
    }

    //设置祝福id
    public void setBlessTimerId(Long id) {
        player.setFriendbleestimeId(id);  //添加到被祝福池
    }

    //获取祝福定时器id
    public void getBlessTimerId() {
        player.getFriendbleestimeId();
    }

    //获取被祝福（祝福我）的玩家集合，用来初始化查看数据
    public Set<Long> getBeBlessSet() {
        return player.getBeBlessSet();
    }

    //是否有接受过这个玩家的祝福
    public boolean isAcceptBless(Long blesser) {
        return player.getBeBlessSet().contains(blesser);
    }

    //是否已经领取祝福了
    public boolean isGetBless(Long playerId) {
        return player.getGetBlessSet().contains(playerId);
    }

    //这个玩家已经领取过了  4点过后需要清空
    public void addGetBless(Long playerId) {
        player.getGetBlessSet().add(playerId);
    }

    public void clearGetBless() {
        player.getGetBlessSet().clear();
    }

    public void setAutoBuildState(int state) {
        player.setAutoBuild(state);
    }

    public int getAutoBuildState() {
        return player.getAutoBuild();
    }

    public long getAutoBuildStateendtime() {
        return player.getAutoBuildendTime();
    }

    public void setAutoBuildStateendtime(long time) {
        player.setAutoBuildendTime(time);
    }

    public void setRemainList(Set<Integer> setlist) {
        player.setRemianset(setlist);
    }

    public Set<Integer> getRemainList() {
        return player.getRemianset();
    }

    //4点清空
    public void clearBeBlessByGet() {
        List<Long> removeList = new ArrayList<>();
        Set<Long> beBlessSet = player.getBeBlessSet();
        Set<Long> getBlessSet = player.getGetBlessSet();
        beBlessSet.forEach(id -> {
            if (getBlessSet.contains(id)) {
                removeList.add(id);
            }
        });

        removeList.forEach(id -> beBlessSet.remove(id));
    }

    //是否可以领取祝福，没有在getBlessSet里面 且在BeBlessSet里面
    public boolean isCanGetBless(Long playerId) {
        return isAcceptBless(playerId) && (!isGetBless(playerId));
    }

    public void addFormationToPlayer(Set<Long> formation, int type) {
        switch (type) {
            case SoldierDefine.FORMATION_DUNGEO: {
                player.setFormationMember1Set(formation);
                break;
            }
            case SoldierDefine.FORMATION_DEFEND: {
                player.setFormationMember2Set(formation);
                break;
            }
            case SoldierDefine.FORMATION_ARENA: {
                player.setFormationMember3Set(formation);
                break;
            }
        }
    }

    public void setbuildLevelTime(Set<Long> list) {
        player.setOutLevelTime(list);
    }

    public Set<Long> getbuildLevelTime() {
        return player.getOutLevelTime();
    }

    public long getPowerValue(int power) {
        String powerName = PlayerPowerDefine.NameMap.get(power);
        if (powerName == null) {
            //从扩展属性拿取
            JSONObject resource = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE, power);
            if (resource == null) {
                return 0l;
            }
            int metic = resource.getInt("meticID");
            long value = PowerMetic.getPowerMetic().getMeticValue(metic, getGameProxy(), power);
            return value;
        } else {
            return getPlayerPowerValue(powerName);
        }
    }


    public void setPowerValue(int power, Long value) {
        String powerName = PlayerPowerDefine.NameMap.get(power);
        if (powerName == null) {
            CustomerLogger.info("出现未知的power值了！！！" + power);
            return;
        }
        if (power == 110) {
            System.err.println("出现0值");
        }
        player.setter(powerName, value.toString());
    }

    public void addOffLinePowerValue(HashMap<Integer, Integer> rewardMap) {
        PlayerCache cache = new PlayerCache();
        cache.setAreId(player.getAreaId());
        this.setPlayerCache(cache);
        for (Integer key : rewardMap.keySet()) {
            //TODO 需要增加日志处理
            String powerName = PlayerPowerDefine.NameMap.get(key);
            long value = getPlayerPowerValue(powerName);
            setPowerValue(key, value + rewardMap.get(key));
        }
    }

    public void reduceOffLinePowerValue(HashMap<Integer, Integer> rewardMap) {
        PlayerCache cache = new PlayerCache();
        cache.setAreId(player.getAreaId());
        this.setPlayerCache(cache);
        for (Integer key : rewardMap.keySet()) {
            //TODO 需要增加日志处理
            String powerName = PlayerPowerDefine.NameMap.get(key);
            long value = getPlayerPowerValue(powerName);
            setPowerValue(key, value - rewardMap.get(key));
        }
    }

    public Set<Integer> getChangePower() {
        Set<Integer> res = new HashSet<>(changePower);
        changePower.clear();
        return res;
    }

    private Set<Integer> changePower = new ConcurrentHashSet<>();

    public void addPowerToChangePower(Integer power) {
        changePower.addAll(getRealEffectPower(power));
        if (power >= 57 && power <= 61) {
            //产量相关的要触发刷新一下任务
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_RESOURCE_VALUE, 1);
        }
        if (power == PlayerPowerDefine.NOR_POWER_depotprotect || power == PlayerPowerDefine.NOR_POWER_depotprotectrate){
            setDepotprotect(getPowerValue(PlayerPowerDefine.NOR_POWER_depotprotect));
        }
    }

    public Set<Integer> getRealEffectPower(int power){
        Set<Integer> powers = new HashSet<>();
        powers.add(power);
        switch (power){
            case PlayerPowerDefine.NOR_POWER_depotcontent:
            case PlayerPowerDefine.NOR_POWER_depotcontentrate:
                powers.add(PlayerPowerDefine.NOR_POWER_taelcontent);
                powers.add(PlayerPowerDefine.NOR_POWER_ironcontent);
                powers.add(PlayerPowerDefine.NOR_POWER_woodcontent);
                powers.add(PlayerPowerDefine.NOR_POWER_stonescontent);
                powers.add(PlayerPowerDefine.NOR_POWER_foodcontent);
                break;
            case PlayerPowerDefine.NOR_POWER_taelcontentrate:
                powers.add(PlayerPowerDefine.NOR_POWER_taelcontent);
                break;
            case PlayerPowerDefine.NOR_POWER_woodcontentrate:
                powers.add(PlayerPowerDefine.NOR_POWER_woodcontent);
                break;
            case PlayerPowerDefine.NOR_POWER_ironcontentrate:
                powers.add(PlayerPowerDefine.NOR_POWER_ironcontent);
                break;
            case PlayerPowerDefine.NOR_POWER_foodcontentrate:
                powers.add(PlayerPowerDefine.NOR_POWER_foodcontent);
                break;
            case PlayerPowerDefine.NOR_POWER_stonescontentrate:
                powers.add(PlayerPowerDefine.NOR_POWER_stonescontent);
                break;

            case PlayerPowerDefine.NOR_POWER_taelyieldrate:
                powers.add(PlayerPowerDefine.NOR_POWER_taelyield);

                break;
            case PlayerPowerDefine.NOR_POWER_ironyieldrate:
                powers.add(PlayerPowerDefine.NOR_POWER_ironyield);
                break;
            case PlayerPowerDefine.NOR_POWER_woodyieldrate:
                powers.add(PlayerPowerDefine.NOR_POWER_woodyield);
                break;
            case PlayerPowerDefine.NOR_POWER_stonesyieldrate:
                powers.add(PlayerPowerDefine.NOR_POWER_stonesyield);
                break;
            case PlayerPowerDefine.NOR_POWER_foodyieldrate:
                powers.add(PlayerPowerDefine.NOR_POWER_foodyield);
                break;
            case PlayerPowerDefine.NOR_POWER_allresyield:
            case PlayerPowerDefine.NOR_POWER_allresyieldrate:
                powers.add(PlayerPowerDefine.NOR_POWER_taelyield);
                powers.add(PlayerPowerDefine.NOR_POWER_ironyield);
                powers.add(PlayerPowerDefine.NOR_POWER_woodyield);
                powers.add(PlayerPowerDefine.NOR_POWER_stonesyield);
                powers.add(PlayerPowerDefine.NOR_POWER_foodyield);
                break;

            case PlayerPowerDefine.NOR_POWER_depotprotectrate:
                powers.add(PlayerPowerDefine.NOR_POWER_depotprotect);
                break;

        }
        return powers;
    }

    public void addPowerValue(int power, int add, int logType) {
        long value = getPowerValue(power);
        if (add < 0) {
            add = 0;
        }
        value += add;
        addPowerToChangePower(power);
        if (power == PlayerPowerDefine.POWER_exp) {
            value = addExpHandle(add, logType);
        } else if (power == PlayerPowerDefine.POWER_boom) {
            if (value > getPowerValue(PlayerPowerDefine.POWER_boomUpLimit)) {
                add = (int) (add - (value - getPowerValue(PlayerPowerDefine.POWER_boomUpLimit)));
                value = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
            }
        } else if (power == PlayerPowerDefine.POWER_level) {
            ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_COM_LEVEL, (int) value, this, 0);
        }
        setPowerValue(power, value);
        if (power == PlayerPowerDefine.POWER_vipExp) {
            upVIPLevel(add, logType);
        }
        refreshValue(power, value, 1, logType, add);
        ResourceGet resourceGet = new ResourceGet(logType, power, value, getPlayerId());
        sendPorxyLog(resourceGet);
    }


    public void addActivity(long id) {
        player.addActivity(id);
    }

    public void removeActivity(long id) {
        player.removeActivity(id);
    }

    public void setFacade(int itemId, long time, int icon) {
        player.setFacade(itemId);
        player.setFacadeendTime(time);
        player.setFaceIcon(icon);
    }

    private void refreshValue(int power, long value, int opt, int dict_action, int amount) {
        switch (power) {
            case PlayerPowerDefine.POWER_boom: {
                refreshBoomLevel();
                break;
            }
            case PlayerPowerDefine.POWER_prestige: {
                refreshPrestigeLv();
                break;
            }
            case PlayerPowerDefine.POWER_gold: {
                goldLog(value, PlayerPowerDefine.POWER_gold, opt, dict_action, amount);
                break;
            }
            case PlayerPowerDefine.POWER_tael: {
                goldLog(value, PlayerPowerDefine.POWER_tael, opt, dict_action, amount);
                break;
            }
            case PlayerPowerDefine.POWER_iron: {
                goldLog(value, PlayerPowerDefine.POWER_iron, opt, dict_action, amount);
                break;
            }
            case PlayerPowerDefine.POWER_wood: {
                goldLog(value, PlayerPowerDefine.POWER_wood, opt, dict_action, amount);
                break;
            }
            case PlayerPowerDefine.POWER_stones: {
                goldLog(value, PlayerPowerDefine.POWER_stones, opt, dict_action, amount);
                break;
            }
            case PlayerPowerDefine.POWER_food: {
                goldLog(value, power, opt, dict_action, amount);
                break;
            }
            case PlayerPowerDefine.POWER_vipExp: {
                goldLog(value, power, opt, dict_action, amount);
                break;
            }
        }
    }

    /***
     * 判断资源power是否足够的统一接口
     ***/
    public boolean isPowerEnough(int power, long value) {
        if (value < 0) {
            value = -value;
        }
        long myValue = getPowerValue(power);
        return myValue >= value;
    }

    public void reducePowerValue(int power, int reduce, int logtype) {
        long value = getPowerValue(power);
        if (power == PlayerPowerDefine.POWER_energy && value == ActorDefine.MAX_ENERGY) {
                setEnergyRefTime(GameUtils.getServerDate().getTime());
                //setReduceEnergytime(TimerDefine.DEFAULT_TIME_RECOVER);
        }
        if(power== PlayerPowerDefine.POWER_boom){
            long limitvalue=getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
            if(value>=limitvalue){
                setBoomRefTime(GameUtils.getServerDate().getTime());
            }
        }
        addPowerToChangePower(power);
        if (power == PlayerPowerDefine.POWER_gold){
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_COSTGOLD_TIMES, 1);
        }

        if (reduce < 0) {
            reduce = -reduce;
        }
        if (value < reduce) {
            value = 0;
        } else {
            value -= reduce;
        }
        setPowerValue(power, value);
        refreshValue(power, value, 2, logtype, reduce);
        ResourceLost resourceLost = new ResourceLost(logtype, power, value, getPlayerId());
        sendPorxyLog(resourceLost);
    }

    public long getPlayerId() {
        return player.getId();
    }

    public int getAreaId() {
        return player.getAreaId();
    }

    public String getAreaKey() {
        return GameMainServer.getAreaKeyByAreaId(player.getAreaId());
    }

    public void createRole(String name, int sex) {
        this.player.setName(name);
        this.player.setSex(sex);
        if (sex == 1) {
            this.player.setIcon(101);
        } else {
            this.player.setIcon(201);
        }
        this.savePlayer();  //关键数据，操作不频繁，直接保存

        //创建角色日志
        roleLog(name, sex);
    }

    public SimplePlayer getSimplePlayer() {
        return GameUtils.player2SimplePlayer(player, simplePlayer);
    }

    public void setSimplePlayerTroop(int type, PlayerTroop troop) {
        if (type == SoldierDefine.FORMATION_DEFEND) {
            simplePlayer.setDefendTroop(troop);
        } else if (type == SoldierDefine.FORMATION_ARENA) {
            simplePlayer.setArenaTroop(troop);
        }
    }

    private int expAdder = 0;

    public int getExpAdder() {
        int valiue = expAdder;
        expAdder = 0;
        return valiue;
    }

    public void setExpAdder(int expAdder) {
        this.expAdder = expAdder;
    }

    //经验值升级
    private long addExpHandle(int add, int logtype) {
        //增加经验逻辑，返回加经验后的正确经验值
        long level = getPowerValue(PlayerPowerDefine.POWER_level);
        JSONObject info = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.COMMANDER, "leve", level);
        if (info != null) {
            if (info.getInt("exp") == 0) {
                return 0;
            }
        }
        long currentExp = getPowerValue(PlayerPowerDefine.POWER_exp);
        if (add <= 0) {
            return currentExp;
        }
        expAdder = add;
        currentExp += add;
        boolean levelUp = false;
        boolean flg = false;
        while (!flg) {
            long currentLevel = getPowerValue(PlayerPowerDefine.POWER_level);
            JSONObject currentLevelInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.COMMANDER, "leve", currentLevel);
            if (currentLevelInfo != null) {
                long lastExp = currentExp;
                if (currentExp >= currentLevelInfo.getInt("exp") && currentLevelInfo.getInt("exp") != 0) {
                    addPowerValue(PlayerPowerDefine.POWER_level, 1, logtype);
                    addPowerValue(PlayerPowerDefine.POWER_energy, 1, logtype);
                    currentExp = currentExp - currentLevelInfo.getInt("exp");
                    allTakeSoldierNum();
                    levelUp = true;
                    //tbllog_level_up 升级日志
                    long newLevel = getPowerValue(PlayerPowerDefine.POWER_level);
                    levelUPLog((int) currentLevel, (int) newLevel, lastExp, currentExp);

                } else {
                    flg = true;
                }
            } else {
                flg = true;
            }
        }
        if (levelUp) {
            getSimplePlayer();
        }
        return currentExp;
    }


    //军衔升级
    public int addMilitaryHandle() {
        long currentTael = getPowerValue(ResourceDefine.POWER_tael);
        long captainLevel = getPowerValue(PlayerPowerDefine.POWER_level);
        long currentMilitaryRank = getPowerValue(PlayerPowerDefine.POWER_militaryRank);
        if (currentMilitaryRank == 0) {
            currentMilitaryRank = 1;
        }
        JSONObject militaryInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MILITARYRANK, "ID", currentMilitaryRank);
        JSONObject nextMilitaryInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MILITARYRANK, "ID", currentMilitaryRank + 1);

        if (militaryInfo == null) {
            return ErrorCodeDefine.M20001_1;
        } else if (militaryInfo.getInt("captainLv") == 0 && nextMilitaryInfo == null) {
            return ErrorCodeDefine.M20001_2;
        } else if (currentTael < militaryInfo.getInt("crysneed")) {
            return ErrorCodeDefine.M20001_3;
        } else if (captainLevel < militaryInfo.getInt("captainLv") && militaryInfo.getInt("captainLv") != 0) {
            return ErrorCodeDefine.M20001_4;
        } else {
            reducePowerValue(ResourceDefine.POWER_tael, militaryInfo.getInt("crysneed"), LogDefine.LOST_MilitaryLevetUp);
            addPowerValue(PlayerPowerDefine.POWER_militaryRank, 1, LogDefine.GET_JUNXIAN_LV);
            long newMilitaryRank = getPowerValue(PlayerPowerDefine.POWER_militaryRank);
            int newrank = (int) newMilitaryRank;
            JSONObject militarynoticeInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SYSTEM_NOTICE, "type", ActorDefine.CHAT_TYPE_7);
            if (newrank >= militarynoticeInfo.getInt("condition1")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.sendSystemchatNoChange(ActorDefine.RANK_UPGRADE_NOTICE_TYPE, ActorDefine.CONDITION_TWO, nextMilitaryInfo.getString("name"));//发送系统公告7
            }
            return 0;
        }
    }

    public void setCapacity(long capacity) {
        player.setCapacity(capacity);
    }


    //繁荣等级升级
    public int refreshBoomLevel() {
        int res = 0;
        long currentBoomLv = getPowerValue(PlayerPowerDefine.POWER_boomLevel);
        long currentBoom = getPowerValue(PlayerPowerDefine.POWER_boom);
        long boomlimit = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        List<JSONObject> boomlist = ConfigDataProxy.getConfigAllInfo(DataDefine.BOOMLEVEL);
        JSONObject jsonObject = null;
        for (int i = 0; i < boomlist.size(); i++) {
            JSONObject min = boomlist.get(i);
            if (i == boomlist.size() - 1) {
                jsonObject = min;
            } else {
                JSONObject max = boomlist.get(i + 1);
                if (currentBoom >= min.getInt("numneed") && currentBoom < max.getInt("numneed")) {
                    jsonObject = min;
                    break;
                }
            }
        }
    /*    JSONObject boomInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BOOMLEVEL, "boomlv", currentBoomLv);
        JSONObject nextBoomInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BOOMLEVEL, "boomlv", currentBoomLv + 1);
        if (boomInfo == null) {
            res = ErrorCodeDefine.M20003_1;
        } else if (currentBoom < boomInfo.getInt("numneed")) {
            reducePowerValue(PlayerPowerDefine.POWER_boomLevel, 1, LogDefine.LOST_BUY_BOOLD);
            res = ErrorCodeDefine.M20003_2;
        } else if (nextBoomInfo == null) {
            res = ErrorCodeDefine.M20003_3;
        } else if (currentBoom >= nextBoomInfo.getInt("numneed")) {
            addPowerValue(PlayerPowerDefine.POWER_boomLevel, 1, LogDefine.GET_BUYBOOM);
            allTakeSoldierNum();
            res = 0;
        }*/
        setPowerValue(PlayerPowerDefine.POWER_boomLevel, (long) jsonObject.getInt("boomlv"));
        allTakeSoldierNum();
        long newBoomLv = getPowerValue(PlayerPowerDefine.POWER_boomLevel);
        if (currentBoomLv != newBoomLv) {
            addPowerToChangePower(PlayerPowerDefine.POWER_boomLevel);
        }
        if (simplePlayer != null) {
            simplePlayer.setBoom(getPowerValue(PlayerPowerDefine.POWER_boom));
            simplePlayer.setBoomUpLimit(getPowerValue(PlayerPowerDefine.POWER_boomUpLimit));
        }

        checkBoomState();
        return res;
    }


    public long getTimeFromRuinsToNormal() {
        long currentBoom = getPowerValue(PlayerPowerDefine.POWER_boom);
        long currentBoomUplimit = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        long num = 0;
        if (currentBoomUplimit * 0.41 < 600) {
            num = (long) (currentBoomUplimit * 0.41 - currentBoom);
        } else {
            num = (600 - currentBoom);
        }
        long time = num * 90 * 1000;//恢复到正常状态所需要的时间
        return time;
    }

    //获得繁荣度恢复到满的时间
    public int boom2foolTime(){
        SystemProxy systemProxy=getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        systemProxy.checkBoomTimer(playerProxy);
        long boom=playerProxy.getPowerValue(PlayerPowerDefine.POWER_boom);
        long limit=playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        long lasttime=getBoomRefTime();
        long needtime=0;
        long now=GameUtils.getServerDate().getTime();
        int times=TimerDefine.DEFAULT_TIME_BOOM;
        int boomState = playerProxy.checkBoomState();//繁荣状态 0正常，1废墟
        if (boomState == ActorDefine.DEFINE_BOOM_RUINS) {
            times = (int) (Math.ceil(times * 1.5));
        }
        needtime=now-lasttime;
        int second=(int)(times- (needtime%times))/1000;
        if(second<=0||boom>=limit){
            second=-1;
        }
        return second;
    }

    public int getBoon2Normalneed() {
        long currentBoom = getPowerValue(PlayerPowerDefine.POWER_boom);
        long currentBoomUplimit = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        if (currentBoomUplimit * 0.41 < 600) {
            if (currentBoom < currentBoomUplimit * 0.41) {
                //废墟
                return (int) ((currentBoomUplimit * 0.41) - currentBoom);
            } else {
                return 0;
            }
        } else {
            if (currentBoom < 600) {
                return (int) (600 - currentBoom);
            } else {
                return 0;
            }
        }

    }

    public long getBoomPoorLimit() {
        long currentBoomUplimit = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        long boomun = 0;
        if (currentBoomUplimit * 0.41 < ActorDefine.BOOM_MAX_POOR) {
            boomun = (long) (Math.ceil(currentBoomUplimit * 0.41));
        } else {
            boomun = ActorDefine.BOOM_MAX_POOR;
        }
        return boomun;
    }

    //繁荣度状态 0正常，1废墟
    public int checkBoomState() {
        long currentBoom = getPowerValue(PlayerPowerDefine.POWER_boom);
        long currentBoomUplimit = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        int state = 0;
        if (currentBoomUplimit * 0.41 < 600) {
            if (currentBoom < currentBoomUplimit * 0.41) {
                //废墟
                state = ActorDefine.DEFINE_BOOM_RUINS;
            } else {
                state = ActorDefine.DEFINE_BOOM_NORMAL;
            }
        } else {
            if (currentBoom < 600) {
                //废墟
                state = ActorDefine.DEFINE_BOOM_RUINS;
            } else {
                state = ActorDefine.DEFINE_BOOM_NORMAL;
            }
        }
        if (state != player.getBoomState()) {
            player.setBoomState(state);
            simplePlayer.setBoomState(state);
        }
        return state;
    }

    //元宝购买繁荣度
    public int buyBoom(List<BoomLog> log) {
        long currentBoom = getPowerValue(PlayerPowerDefine.POWER_boom);
        long currentBoomUplimit = getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        long needAddBoom = currentBoomUplimit - currentBoom;
        int boomState = checkBoomState();
        int needCost = 0;
        if (currentBoom < currentBoomUplimit) {
            //繁荣废墟状态
            if (boomState == ActorDefine.DEFINE_BOOM_RUINS) {
                //从废墟状态到正常状态翻倍
                double needTael = Math.ceil((getBoomPoorLimit() - currentBoom) * ActorDefine.MIN_BOOM_TAEL);
                needTael *= 2;
                needCost += needTael;
                if (getPowerValue(PlayerPowerDefine.POWER_gold) < needTael) {
                    return ErrorCodeDefine.M20003_4;
                }
                reducePowerValue(PlayerPowerDefine.POWER_gold, (int) needTael, LogDefine.LOST_BUY_BOOLD);
                addPowerValue(PlayerPowerDefine.POWER_boom, (int) (getBoomPoorLimit() - currentBoom), LogDefine.GET_BUYBOOM);
            } else {//繁荣正常状态
                long nowBoom = getPowerValue(PlayerPowerDefine.POWER_boom);
                double needTael = Math.ceil((currentBoomUplimit - nowBoom) * ActorDefine.MIN_BOOM_TAEL);
                needCost += needTael;
                if (getPowerValue(PlayerPowerDefine.POWER_gold) < needTael) {
                    return ErrorCodeDefine.M20003_4;
                }
                reducePowerValue(PlayerPowerDefine.POWER_gold, (int) needTael, LogDefine.LOST_BUY_BOOLD);
                addPowerValue(PlayerPowerDefine.POWER_boom, (int) (currentBoomUplimit - nowBoom), LogDefine.GET_BUYBOOM);
            }
        }
        BoomLog lg = new BoomLog((int) getPowerValue(PlayerPowerDefine.POWER_boomLevel), (int) (needAddBoom), needCost);
        log.add(lg);
        refreshBoomLevel();
        return 0;
    }

    //攻击玩家基地 获得25点繁荣度
    public void attackPlayer() {
        addPowerValue(PlayerPowerDefine.POWER_boom, ActorDefine.DEFINE_GET_BOOM, LogDefine.GET_ATTACK_PLAYER_HOME);
    }

    //攻击世界资源点,获得等级比例繁荣度
    public int attackWorldRes() {
        long currentBoomLv = getPowerValue(PlayerPowerDefine.POWER_boomLevel);
        JSONObject boomInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BOOMLEVEL, "boomlv", currentBoomLv);
        if (boomInfo != null) {
            int ratio = boomInfo.getInt("boomAddRate");
            double rate = (double) ratio / 100;
            int addboom = (int) Math.floor((ActorDefine.DEFINE_GET_BOOM * rate));
            addPowerValue(PlayerPowerDefine.POWER_boom, addboom, LogDefine.GET_ATTACK_WOLDER_RES);
        }
        return 0;
    }

    // 建筑升级或者建造获得繁荣度
    public void upBuilderOrCreate(int type, int buildLevel) {
        if (ResFunBuildDefine.RESOUCETYPELIST.contains(type)) {   //资源建筑
            JSONObject builderInfo = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", type, "lv", buildLevel);
            if (builderInfo != null) {
                int boomNum = builderInfo.getInt("boom");
                addPowerValue(PlayerPowerDefine.POWER_boomUpLimit, boomNum, LogDefine.GET_BUILDLEVE_UP);
                addPowerValue(PlayerPowerDefine.POWER_boom, boomNum, LogDefine.GET_BUILDLEVE_UP);
            }
        } else if (ResFunBuildDefine.FUNCTIONTYPELIST.contains(type)) {   //功能建筑
            JSONObject builderFunInfo = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", type, "lv", buildLevel);
            if (builderFunInfo != null) {
                int boomNum = builderFunInfo.getInt("boom");
                addPowerValue(PlayerPowerDefine.POWER_boomUpLimit, boomNum, LogDefine.GET_BUILDLEVE_UP);
                addPowerValue(PlayerPowerDefine.POWER_boom, boomNum, LogDefine.GET_BUILDLEVE_UP);
            }
        }
    }

    //拆除建筑(铜铁油)将扣掉相应繁荣度
    public void removeBuilder(int type, int builderLevel) {
        if (ResFunBuildDefine.REMOVEBUILDLIST.contains(type)) {
            JSONObject boomResInfo = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", type, "lv", builderLevel);
            if (boomResInfo != null) {
                int boomResNum = boomResInfo.getInt("boom");
                int reduceResBoom = (boomResNum * builderLevel);
                if (getPowerValue(PlayerPowerDefine.POWER_boom) > reduceResBoom
                        && getPowerValue(PlayerPowerDefine.POWER_boomUpLimit) > reduceResBoom) {
                    reducePowerValue(PlayerPowerDefine.POWER_boom, reduceResBoom, LogDefine.LOST_DROP_BUILD);
                    reducePowerValue(PlayerPowerDefine.POWER_boomUpLimit, reduceResBoom, LogDefine.LOST_DROP_BUILD);
                }
            }
        }

    }

    //体力消耗:征战副本,攻打世界资源点/玩家消耗
    public void expendEnergy() {
        if (getPowerValue(PlayerPowerDefine.POWER_energy) > 0) {
            reducePowerValue(PlayerPowerDefine.POWER_energy, 1, LogDefine.LOST_BEAT_WORLD);
        } else {
            CustomerLogger.error("*** 体力不足！！！***");
        }
    }

    //请求购买体力
    public int askBuyEnergy() {
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameProxy gameProxy = super.getGameProxy();
      //  TimerdbProxy timerdbProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
       // int type = TimerDefine.BUY_ENERGY;
       /// timerdbProxy.addTimer(type, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this);
        long vipLv = getPowerValue(PlayerPowerDefine.POWER_vipLevel);
       // timerdbProxy.getTimerNum(type, 0, 0);
        int num = playerProxy.getPlayer().getBuyenergytimes();
        if (vipLv == 0) {//非VIP玩家
            if (num < 1) {
                if (getPowerValue(PlayerPowerDefine.POWER_gold) < ActorDefine.MIN_BUY_ENERGY) {
                    return ErrorCodeDefine.M20013_2;
                } else {
                return ActorDefine.MIN_BUY_ENERGY;
                 }
            } else {
                return ErrorCodeDefine.M20013_1;
            }
        } else {//VIP
            VipProxy vipProxy = gameProxy.getProxy(ActorDefine.VIP_PROXY_NAME);
            if (num < vipProxy.getVipNum(ActorDefine.VIP_ENERGYBUY)) {
               if (getPowerValue(PlayerPowerDefine.POWER_gold) < ActorDefine.MIN_BUY_ENERGY) {
                    return ErrorCodeDefine.M20013_2;
                } else {
                if (vipLv == 0) {
                    return ActorDefine.MIN_BUY_ENERGY;
                } else {
                    num += 1;
                    return (int) (num * ActorDefine.MIN_BUY_ENERGY);
                }
                  }
            } else {
                return ErrorCodeDefine.M20013_1;
            }
        }
    }

    //元宝购买体力
    public int buyEnergy(List<EnergyLog> log) {
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameProxy gameProxy = super.getGameProxy();
       // TimerdbProxy timerdbProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        //int type = TimerDefine.BUY_ENERGY;
       // timerdbProxy.addTimer(type, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this);
        //long aa = getPowerValue(PlayerPowerDefine.POWER_gold);
        long vipLv = getPowerValue(PlayerPowerDefine.POWER_vipLevel);
        int num = playerProxy.getPlayer().getBuyenergytimes();
        int cost = (num + 1) * ActorDefine.MIN_BUY_ENERGY;
        if (vipLv == 0) {//非VIP玩家
            if (num < 1) {
                if (getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                    return ErrorCodeDefine.M20011_2;
                } else {
                    addPowerValue(PlayerPowerDefine.POWER_energy, ActorDefine.MIN_BUY_ENERGY, LogDefine.GET_ENERGY);
                    reducePowerValue(PlayerPowerDefine.POWER_gold, ActorDefine.MIN_BUY_ENERGY, LogDefine.LOST_BUYENERGY);
                  //  timerdbProxy.addNum(type, 0, 0, 1);
                    playerProxy.getPlayer().setBuyenergytimes(playerProxy.getPlayer().getBuyenergytimes() + 1);
                }
            } else {
                return ErrorCodeDefine.M20011_1;
            }
        } else {//VIP
            VipProxy vipProxy = gameProxy.getProxy(ActorDefine.VIP_PROXY_NAME);
            if (num < vipProxy.getVipNum(ActorDefine.VIP_ENERGYBUY)) {
                if (getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                    return ErrorCodeDefine.M20011_2;
                } else {
                    addPowerValue(PlayerPowerDefine.POWER_energy, ActorDefine.MIN_BUY_ENERGY, LogDefine.GET_ENERGY);
                    reducePowerValue(PlayerPowerDefine.POWER_gold, ((num + 1) * ActorDefine.MIN_BUY_ENERGY), LogDefine.LOST_BUYENERGY);
                //    timerdbProxy.addNum(type, 0, 0, 1);
                    playerProxy.getPlayer().setBuyenergytimes(playerProxy.getPlayer().getBuyenergytimes()+1);
                }
            } else {
                return ErrorCodeDefine.M20011_1;
            }
        }
        sendFunctionLog(FunctionIdDefine.BUY_ENERGY_FUNCTION_ID, cost, 0, 0);
        //日志记录
        EnergyLog lg;
        if (vipLv == 0) {
            lg = new EnergyLog(num, ActorDefine.MIN_BUY_ENERGY, ActorDefine.MIN_BUY_ENERGY);
        } else {
            lg = new EnergyLog(num, ActorDefine.MIN_BUY_ENERGY, ((num + 1) * ActorDefine.MIN_BUY_ENERGY));
        }
        log.add(lg);
        return 0;
    }

    //好友祝福增加体力
    public int friendAddEnergy(int type) {
       /* GameProxy gameProxy = super.getGameProxy();
        TimerdbProxy timerdbProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (timerdbProxy.addTimer(type, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this) == 0) {
            int num = timerdbProxy.getTimerNum(type, 0, 0);
            if (num > 10) {  //每天不超过10点
                return ErrorCodeDefine.M20006_1;
            } else {
                addPowerValue(PlayerPowerDefine.POWER_energy, 1, LogDefine.GET_FRIEND_WISH);
                timerdbProxy.addNum(type, 0, 0, 1);
            }
        }*/
        return 0;
    }

    //获得体力恢复1点剩余时间
    public int getEnergyRefTime(){
        SystemProxy systemProxy=getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        systemProxy.checkEnergyTimer(playerProxy);

        long boom=playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy);
        long energyUplimit =ActorDefine.ENERGY_LIMIT;
        long lasttime=getEnergyRefTimes();
        long needtime=0;
        long now=GameUtils.getServerDate().getTime();
        int times=0;
        needtime=now-lasttime;
        long less = needtime % TimerDefine.DEFAULT_TIME_RECOVER;
        if (less == 0) {
            times=TimerDefine.DEFAULT_TIME_RECOVER/1000;
        } else {
            times=(int) (TimerDefine.DEFAULT_TIME_RECOVER - less)/1000;
        }
        if(times <=0 || boom >=energyUplimit ){
            return -1;
        } else{
            return times;
        }
        }
    //获得在线礼包剩余时间
   /* public int getOnlineTime(){
        if(player.getLoginOutTime()==0){
            JSONObject commandInfo = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ACTIVE_EFFECT, "commandlv", commandLv);
        }
       player.setOnlinetime(player.getOnlinetime()+(int)(player.getLoginOutTime()-player.getLoginTime());
        return 0;
    }*/


    //统帅升级 0统帅书，1金币
    public int upCommandLv(int type, List<Integer> itemlist) {
        int randomNum = GameUtils.getRandomValueByRange(ActorDefine.MAX_UPCOMMD_RATE);
        long commandLv = getPowerValue(PlayerPowerDefine.POWER_commandLevel);
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        JSONObject commandInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.COMMANDLV, "commandlv", commandLv);
        if (commandInfo != null) {
            int rate = commandInfo.getInt("rate");
            rate = (int) Math.ceil(rate * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_LEVEL_TONGSHUAI_SUCCESS)) / 100.0);
            int captainLv = getLevel();
            int price = commandInfo.getInt("price");
            GameProxy gameProxy = super.getGameProxy();
            ItemProxy itemProxy = gameProxy.getProxy(ActorDefine.ITEM_PROXY_NAME);
            int commandBook = itemProxy.getItemNum(ItemDefine.COMMANDBOOK_ID);
            long gold = getPowerValue(PlayerPowerDefine.POWER_gold);
            if (commandLv >= captainLv) {
                return ErrorCodeDefine.M20004_1;
            } else if (rate == 0 && captainLv == 0 && price == 0) {
                return ErrorCodeDefine.M20004_5;
            }
            if (type == ActorDefine.DEFINE_UPLV_CMBOOK) {
                if (commandBook < 1) {
                    return ErrorCodeDefine.M20004_3;
                } else if (randomNum > rate) {
                    itemlist.add(ItemDefine.COMMANDBOOK_ID);
                    itemProxy.reduceItemNum(ItemDefine.COMMANDBOOK_ID, 1, LogDefine.LOST_UP_COMMOND_LV);
                    return ErrorCodeDefine.M20004_2;
                }
                addPowerValue(PlayerPowerDefine.POWER_commandLevel, 1, LogDefine.GET_COMMOND_LVUP);
                Long newcommandLv = getPowerValue(PlayerPowerDefine.POWER_commandLevel);
                String stringnewcommandlv = String.valueOf(newcommandLv);
                int intconnandlv = newcommandLv.intValue();
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.sendSystemchat(ActorDefine.COMMAND_UPGRADE_NOTICE_TYPE, intconnandlv, ActorDefine.CONDITION_TWO, stringnewcommandlv);//发送系统公告6
                itemlist.add(ItemDefine.COMMANDBOOK_ID);
                itemProxy.reduceItemNum(ItemDefine.COMMANDBOOK_ID, 1, LogDefine.LOST_UP_COMMOND_LV);
            } else if (type == ActorDefine.DEFINE_UPLV_CMGOLD) {
                if (gold < ActorDefine.MAX_UPCOMMD_GOLD) {
                    return ErrorCodeDefine.M20004_4;
                } else if (randomNum > rate) {
                    reducePowerValue(PlayerPowerDefine.POWER_gold, ActorDefine.MAX_UPCOMMD_GOLD, LogDefine.LOST_UP_COMMOND_LV);
                    return ErrorCodeDefine.M20004_2;
                }
                addPowerValue(PlayerPowerDefine.POWER_commandLevel, 1, LogDefine.GET_COMMOND_LVUP);
                reducePowerValue(PlayerPowerDefine.POWER_gold, ActorDefine.MAX_UPCOMMD_GOLD, LogDefine.LOST_UP_COMMOND_LV);
                Long newcommandLv = getPowerValue(PlayerPowerDefine.POWER_commandLevel);
                String stringnewcommandlv = String.valueOf(newcommandLv);
                int intconnandlv = newcommandLv.intValue();
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.sendSystemchat(ActorDefine.COMMAND_UPGRADE_NOTICE_TYPE, intconnandlv, ActorDefine.CONDITION_TWO, stringnewcommandlv);//发送系统公告6
            }
            allTakeSoldierNum();
        }
        return 0;
    }

    //每日登陆，自动领取声望
    public int loginGetPrestige() {
        Long dayTime = (long) GameUtils.getServerTime() * 1000;
        GameProxy gameProxy = super.getGameProxy();
     //   TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
      //  Long lastTime = timerProxy.getLastOperatinTime(TimerDefine.DEFAULT_GET_PRESTIGE, 0, 0);
       // boolean sameDay = false;
      //  sameDay = DateUtil.isCanGet(dayTime, lastTime, TimerDefine.TIMER_REFRESH_FOUR);
        int state=getPlayer().getPrestaeReward();
        if (state == UtilDefine.REWARD_STATE_HAS_GET) {
            return ErrorCodeDefine.M20005_1;
        } else {
            long captainLevel = getPowerValue(PlayerPowerDefine.POWER_militaryRank);
            JSONObject captainInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MILITARYRANK, "ID", captainLevel);
            if (captainInfo != null) {
                int prestigeNum = captainInfo.getInt("prestige");
                addPowerValue(PlayerPowerDefine.POWER_prestige, prestigeNum, LogDefine.GET_LOGIN_EVERDAY);
                //timerProxy.setLastOperatinTime(TimerDefine.DEFAULT_GET_PRESTIGE, 0, 0, dayTime);
                getPlayer().setPrestaeReward(UtilDefine.REWARD_STATE_HAS_GET);
                sendFunctionLog(FunctionIdDefine.GET_PRESTIGE_FUNCTION_ID, prestigeNum, 0, 0);
                return captainInfo.getInt("ID");
            }
            return 0;
        }
    }

    //今日是否已经领取声望
    public int isGetTadayPrestige() {
        long dayTime = GameUtils.getServerDate().getTime();
        GameProxy gameProxy = super.getGameProxy();
        //TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
     //  Long lastTime =0l;// timerProxy.getLastOperatinTime(TimerDefine.DEFAULT_TODAYGET_PRESTIGE, 0, 0);
      //  boolean sameDay = false;
        //sameDay = DateUtil.isCanGet(dayTime, lastTime, TimerDefine.TIMER_REFRESH_FOUR);
        int state=getPlayer().getPrestaeshouxun();
        if (state ==UtilDefine.REWARD_STATE_HAS_GET) {
            return ErrorCodeDefine.M20010_1;
        } else {
            return 0;
        }

    }

    //授勋获取声望
    public int medalGetPrestige(int type, M2.M20005.S2C.Builder builder) {
        builder.setType(0);
        JSONObject medalInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.PRESTIGE_GIVE, "type", type);
        if (medalInfo == null) {
            return ErrorCodeDefine.M20005_2;
        }
        if (isGetTadayPrestige() == 0) {
            int prestigeNum = medalInfo.getInt("prestige");
            List<Integer> priceList = new ArrayList<>();
            JSONArray array = medalInfo.getJSONArray("price");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    priceList.add(array.getInt(i));
                }
            }
            int power = priceList.get(0);
            int value = priceList.get(1);
            long tael = getPowerValue(ResourceDefine.POWER_tael);
            long gold = getPowerValue(PlayerPowerDefine.POWER_gold);
            if (priceList.get(0) == ResourceDefine.POWER_tael) {
                if (priceList.get(1) > tael) {
                    return ErrorCodeDefine.M20005_3;//银两不足
                }
                addPowerValue(PlayerPowerDefine.POWER_prestige, prestigeNum, LogDefine.GET_MADEL);
                reducePowerValue(ResourceDefine.POWER_tael, priceList.get(1), LogDefine.LOST_medalPRESTIGE);
            } else if (priceList.get(0) == PlayerPowerDefine.POWER_gold) {
                if (priceList.get(1) > gold) {
                    return ErrorCodeDefine.M20005_4;//金币不足
                }
                addPowerValue(PlayerPowerDefine.POWER_prestige, prestigeNum, LogDefine.GET_MADEL);
                reducePowerValue(PlayerPowerDefine.POWER_gold, priceList.get(1), LogDefine.LOST_medalPRESTIGE);
            }
        //    GameProxy gameProxy = super.getGameProxy();
            //TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
         //   long timePrestige = timerProxy.addTimer(TimerDefine.DEFAULT_TODAYGET_PRESTIGE, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this); //登录领取声望
          //  if (timePrestige == 0) {
           //     Long nowTime = (long) GameUtils.getServerTime() * 1000;
           //     timerProxy.setLastOperatinTime(TimerDefine.DEFAULT_TODAYGET_PRESTIGE, 0, 0, nowTime);
        //    }
            getPlayer().setPrestaeshouxun(UtilDefine.REWARD_STATE_HAS_GET);
            sendFunctionLog(FunctionIdDefine.MEDAL_GET_PRESTIGE_FUNCTION_ID, type, power, value);
            builder.setType(medalInfo.getInt("ID"));
            return 0;
        } else {
            return ErrorCodeDefine.M20010_1;
        }

    }

    //声望升级
    public void refreshPrestigeLv() {
        boolean flg = false;
        while (!flg) {
            long prestige = getPowerValue(PlayerPowerDefine.POWER_prestige);
            long prestigeLv = getPowerValue(PlayerPowerDefine.POWER_prestigeLevel);
            JSONObject prestigeInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.PRESTIGE_LEVEL, "prestigelv", prestigeLv);
            int prestigeNeed = prestigeInfo.getInt("prestigeneed");
            if (prestige >= prestigeNeed && prestigeNeed != 0) {
                addPowerValue(PlayerPowerDefine.POWER_prestigeLevel, 1, LogDefine.GET_PRESURE_LVUP);
                reducePowerValue(PlayerPowerDefine.POWER_prestige, prestigeNeed, LogDefine.LOST_PRESTIGE_LEVEUP);
            } else {
                flg = true;
            }
        }
    }

    //总带兵量
    public long allTakeSoldierNum() {
        long oldNum = getPowerValue(PlayerPowerDefine.POWER_command);
        long currentBoomLv = getPowerValue(PlayerPowerDefine.POWER_boomLevel);
        long commandLv = getPowerValue(PlayerPowerDefine.POWER_commandLevel);
        long currentLv = getPowerValue(PlayerPowerDefine.POWER_level);
        JSONObject levelInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.COMMANDER, "leve", currentLv);
        long lvNum = 0;
        long commandNum = 0;
        long boomNum = 0;
        if (levelInfo != null) {
            lvNum = levelInfo.getInt("command");
        }
        JSONObject commandInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.COMMANDLV, "commandlv", commandLv);
        if (commandInfo != null) {
            commandNum = commandInfo.getInt("command");
        }
        JSONObject boomInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BOOMLEVEL, "boomlv", currentBoomLv);
        if (boomInfo != null) {
            boomNum = boomInfo.getInt("command");
        }
        long allNum = lvNum + commandNum + boomNum;
        if (allNum != oldNum){
            setPowerValue(PlayerPowerDefine.POWER_command, allNum);
            addPowerToChangePower(PlayerPowerDefine.POWER_command);
        }
        return allNum;
    }


    /**
     * 获取玩家的出战槽位
     **/
    public List<Integer> getPlayerFightPost() {
        List<Integer> res = new ArrayList<>();
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.TROOP_START);
        long level = getPowerValue(PlayerPowerDefine.POWER_level);
        for (JSONObject define : list) {
            int comLevel = define.getInt("captainLv");
            if (level >= comLevel) {
                res.add(define.getInt("troopsID"));
            } else {
                //测试用
//                res.add(define.getInt("troopsID"));
            }
        }
        return res;
    }

    public String chargeToPlayer(int chargeValue, int chargeType, String OrderId, PlayerReward reward) {
        try {
            CustomerLogger.info("进入充值逻辑了！！playerId = " + getPlayerId() + "，chargeValue = " + chargeValue);
            ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            if (chargeType == AdminCodeDefine.CHARGE_TYPE_MONTH_CARD) {
                addPowerValue(PlayerPowerDefine.POWER_gold, chargeValue * 10, LogDefine.GET_CHARGE);
                reducePowerValue(PlayerPowerDefine.POWER_gold, chargeValue * 10, LogDefine.LOST_BUY_MONTH_CARD);
                //TODO 月卡需求

            } else if (chargeType == AdminCodeDefine.CHARGE_TYPE_NORMAL) {
                addPowerValue(PlayerPowerDefine.POWER_gold, chargeValue * 10, LogDefine.GET_CHARGE);
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.Charge, "limit", chargeValue);
                if (jsonObject != null) {
                    addPowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("restore"), LogDefine.GET_CHARGE);
                }
            } else {
                return GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.CHARGE_UNKOWNEN_TYPE, "未知的充值类型");
            }
//            try {

//            GameMsg.AddBillOrder addMsg = new GameMsg.AddBillOrder(OrderId, getPlayerId());
//            DbProxy.tell(addMsg, ActorRef.noSender());

            BillOrderSetDb billOrderSetDb = BaseSetDbPojo.getSetDbPojo(BillOrderSetDb.class, this.getAreaKey());
            billOrderSetDb.addKeyValue(OrderId, getPlayerId());

            addPowerValue(PlayerPowerDefine.POWER_vipExp, chargeValue * 10, LogDefine.GET_CHARGE);
            if (player.getFirstChargeTime() == 0) {
                player.setFirstChargeTime(GameUtils.getServerDate().getTime());
                // 首冲处理
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_FIRST_CHARGE, chargeValue * 10, this, -1);
            }
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_EVERY_CHARGE_FIRST, chargeValue * 10, this, 0);
            player.setTotalCharge(player.getTotalCharge() + chargeValue * 10);
            long lastchargetime = player.getLastChargeTime();
            long nowchargetime = GameUtils.getServerDate().getTime();
            player.setLastChargeTime(GameUtils.getServerDate().getTime());
            // 活动相关处理
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_CHARGE_EVERYDAY_ATWILL, chargeValue * 10, this, 0);
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_CHARGE_ATWILL, chargeValue * 10, this, 0);
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_EVERY_DAY_CHARGE, chargeValue * 10, this, 0);
            //有福同享充值处理
            activityProxy.handleLegionShareCharge(this, chargeValue, reward);


            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(nowchargetime));
            long now = cal.getTimeInMillis();

            Calendar oldcal = Calendar.getInstance();
            oldcal.setTime(new Date(lastchargetime));
            long ago = oldcal.getTimeInMillis();

            if ((now - ago) / (24 * 60 * 60 * 1000) >= 2) {
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_CHARGE_CONTIUNES, ActivityDefine.NO_CONTINUOUS, this, 0);
            }
            if ((now - ago) / (24 * 60 * 60 * 1000) == 1) {
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_CHARGE_CONTIUNES, ActivityDefine.CONTINUOUS, this, 0);
            }
            /**
             * 充值日志
             */
            payLog(chargeValue, chargeType, OrderId);
            savePlayer();
        } catch (Exception e) {
            CustomerLogger.error("充值活动处理异常", e);
            e.printStackTrace();
        }
        return GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.ACTION_SUCCESS, "充值成功");
    }

    public void removeTeamNotice(long id) {
        player.removeTeamNotice(id);
    }

    public void addTeamNotice(long id) {
        player.addTeamNotice(id);
    }

    /***
     * 购买金币 VIP升级
     ***/
    private int upVIPLevel(int vipValue, int logType) {
        long oldvipLv = getPowerValue(PlayerPowerDefine.POWER_vipLevel);
        if (vipValue > 0) {
//            addPowerValue(PlayerPowerDefine.POWER_vipExp, vipValue, LogDefine.GET_BUY_GLOD);
            boolean flg = false;
            while (!flg) {
                long vipLv = getPowerValue(PlayerPowerDefine.POWER_vipLevel);
                long vipExp = getPowerValue(PlayerPowerDefine.POWER_vipExp);
                JSONObject nextVipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", vipLv + 1);
                if (nextVipInfo != null) {
                    JSONArray nextArray = nextVipInfo.getJSONArray("value");
                    if (vipExp >= nextArray.getInt(1)) {
                        addPowerValue(PlayerPowerDefine.POWER_vipLevel, 1, logType);
                    } else {
                        flg = true;
                    }
                } else {
                    flg = true;
                }
            }
        }
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        vipProxy.initVipData();
        int viplevel = (int) getPowerValue(PlayerPowerDefine.POWER_vipLevel);
        if (viplevel > 0 && viplevel > oldvipLv) {
            sendSystemchat(ActorDefine.VIP_NOTICE_TYPE, viplevel, ActorDefine.CONDITION_TWO);//发送系统公告1
        }
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_VIP_GETREWARD, viplevel, playerProxy, 0);
        return viplevel;
    }

    public Set<Long> getShieldsByType(int type) {
        if (type == ChatAndMailDefine.SHIELD_TYPE_MAIL) {
            return player.getShieldMailSet();
        } else {
            return player.getShieldChatSet();
        }
    }

    public int addShieldPlayer(long id, int type) {
        if (type == ChatAndMailDefine.SHIELD_TYPE_MAIL) {
            if (player.getShieldMailSet().contains(id)) {
                return ErrorCodeDefine.M140005_1;
            }
            player.addShieldMai(id);
        } else {
            if (player.getShieldChatSet().contains(id)) {
                return ErrorCodeDefine.M140005_1;
            }
            player.addShieldChat(id);
        }
        return 0;
    }

    public int removeShield(int type, long playerId) {
        if (type == ChatAndMailDefine.SHIELD_TYPE_MAIL) {
            player.removeShieldMail(playerId);
        } else {
            player.removeShieldChat(playerId);
        }
        return 0;
    }

    public boolean isShield(long id, int type) {
        if (type == ChatAndMailDefine.SHIELD_TYPE_MAIL) {
            if (player.getShieldMailSet().contains(id)) {
                return true;
            }
        } else {
            if (player.getShieldChatSet().contains(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 头像,挂件设置
     **/
    public int setPlayerHeadPendant(int iconId, int pendantId) {
        int icon = player.getIcon();
        int pendt = player.getPendant();
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        long vipLv = vipProxy.getVipLevel();
        //头像
        if (iconId != 0) {
            JSONObject headInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.HEADPORTRAIT, "id", iconId);
            JSONArray vipheadNeed = headInfo.getJSONArray("useneed");
            if (headInfo == null) {
                return ErrorCodeDefine.M20012_4;
            }
            if (vipheadNeed == null) {
                return ErrorCodeDefine.M20012_2;
            }
            if (vipLv == 0) {//普通玩家
                if (vipheadNeed.length() > 0) {
                    return ErrorCodeDefine.M20012_1;//vip等级不足
                } else {
                    player.setIcon(iconId);
                    player.setSex(headInfo.getInt("gender"));
                }
            } else {//VIP玩家
                if (vipheadNeed.length() > 0) {
                    if (vipLv < vipheadNeed.getInt(1)) {
                        return ErrorCodeDefine.M20012_1;//vip等级不足
                    } else {
                        player.setIcon(iconId);
                        player.setSex(headInfo.getInt("gender"));
                    }
                } else {
                    player.setIcon(iconId);
                    player.setSex(headInfo.getInt("gender"));
                }
            }
        }

        //挂件
        if (pendantId != 0) {
            JSONObject pendant = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.PENDANT, "id", pendantId);
            JSONArray vippendantneed = pendant.getJSONArray("useneed");
            if (vippendantneed == null) {
                return ErrorCodeDefine.M20012_3;
            }
            if (pendant == null) {
                return ErrorCodeDefine.M20012_4;
            }

            if (vipLv == 0) {//普通玩家
                if (vippendantneed.length() > 0) {
                    return ErrorCodeDefine.M20012_1;
                } else {
                    player.setPendant(pendantId);
                }
            } else {//VIP玩家
                if (vippendantneed.length() > 0) {
                    if (vipLv < vippendantneed.getInt(1)) {
                        return ErrorCodeDefine.M20012_1;
                    } else {
                        player.setPendant(pendantId);
                    }
                } else {
                    player.setPendant(pendantId);
                }
            }
        }
        if (icon != iconId || pendt != pendantId) {
            player.save();
        }
        return 0;
    }

    public int getIconId() {
        return player.getIcon();
    }

    public int getPendt() {
        return player.getPendant();
    }

    public void addReport(Report report) {
        player.addReport(report.getId());
    }

    public void removeReport(long id) {
        player.removeReport(id);
    }


    /**
     * tbllog_quit 退出日志
     */
    public void quitLog() {
        PlayerCache cache = getPlayerCache();
        int level = (int) getPowerValue(PlayerPowerDefine.POWER_level);
        int nowTime = GameUtils.getServerTime();
        int time_duration = nowTime - player.getLoginTime();
        tbllog_quit quitlog = new tbllog_quit();
        quitlog.setPlatform(cache.getPlat_name());
        quitlog.setRole_id(player.getId());
        quitlog.setAccount_name(player.getAccountName());
        quitlog.setLogin_level(player.getLoginLevel());
        quitlog.setLogout_level(level);
        quitlog.setLogout_ip(cache.getUser_ip());
        quitlog.setLogin_time(player.getLoginTime());
        quitlog.setLogout_time(nowTime);
        quitlog.setTime_duration(time_duration);
        quitlog.setLogout_map_id(0);
        quitlog.setReason_id(0);
        quitlog.setMsg("");
        quitlog.setDid(cache.getImei());
        quitlog.setGame_version(cache.getGame_version());
        sendPorxyLog(quitlog);
    }

    /**
     * 金币变动日志
     *
     * @param value
     * @param opt
     */
    public void goldLog(long value, int type, int opt, int dict_action, int amount) {
        PlayerCache cache = getPlayerCache();
        tbllog_gold goldlog = new tbllog_gold();
        goldlog.setPlatform(cache.getPlat_name());
        goldlog.setRole_id(player.getId());
        goldlog.setAccount_name(player.getAccountName());
        goldlog.setDim_level(player.getLevel());
        goldlog.setDim_prof(0);
        goldlog.setAction_1(dict_action);
        goldlog.setAmount((long) amount);
        goldlog.setMoney_type(type);
        goldlog.setMoney_remain(value);
        goldlog.setOpt(opt);
        goldlog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(goldlog);
    }

    public void setDepotprotect(long value) {
        player.setDepotprotect(value);
    }

    /**
     * tbllog_player 日志
     */
    public void playerLog() {
        PlayerCache cache = getPlayerCache();
        tbllog_player playerlog = new tbllog_player();
        playerlog.setPlatform(cache.getPlat_name());
        playerlog.setRole_id(player.getId());
        playerlog.setRole_name(player.getName());
        playerlog.setAccount_name(player.getAccountName());
        playerlog.setUser_name(player.getName());
        playerlog.setDim_sex(player.getSex());
        playerlog.setDid(cache.getImei());
        playerlog.setDim_level(player.getLevel());
        playerlog.setDim_vip_level(player.getVipLevel());
        playerlog.setDim_exp(player.getExp());
        playerlog.setDim_power(player.getCapacity());
        playerlog.setDim_iron(player.getIron());
        playerlog.setDim_tael(player.getTael());
        playerlog.setDim_wood(player.getWood());
        playerlog.setDim_stones(player.getStones());
        playerlog.setDim_food(player.getFood());
        playerlog.setGold_number((player.getVipExp()));
        playerlog.setCoin_number((player.getGold()));
        playerlog.setPay_money((player.getVipExp()));
        playerlog.setFirst_pay_time((player.getFirstChargeTime() / 1000));
        playerlog.setLast_pay_time((player.getLastChargeTime() / 1000));
        playerlog.setHappend_time(GameUtils.getServerTime());
        sendUpdateLog(playerlog);
    }

    /**
     * 充值日志
     */
    public void payLog(int chargeValue, int chargeType, String OrderId) {
        PlayerCache cache = getPlayerCache();
        tbllog_pay paylog = new tbllog_pay();
        paylog.setPlatform(cache.getPlat_name());
        paylog.setRole_id(player.getId());
        paylog.setAccount_name(player.getAccountName());
        paylog.setUser_ip(cache.getUser_ip());
        paylog.setDim_level(player.getLevel());
        paylog.setPay_type(chargeType);
        paylog.setOrder_id(OrderId);
        paylog.setPay_money(chargeValue);
        paylog.setPay_gold((long) chargeValue * 10);
        paylog.setDid(cache.getImei());
        paylog.setGame_version(cache.getGame_version());
        paylog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(paylog);
    }

    /**
     * tbllog_level_up 升级日志
     */

    public void levelUPLog(int lastLevel, int newLevel, long lastExp, long newExp) {
        PlayerCache cache = getPlayerCache();
        tbllog_level_up levelUplog = new tbllog_level_up();
        levelUplog.setPlatform(cache.getPlat_name());
        levelUplog.setRole_id(player.getId());
        levelUplog.setAccount_name(player.getAccountName());
        levelUplog.setLast_level(lastLevel);
        levelUplog.setCurrent_level(newLevel);
        levelUplog.setLast_exp(lastExp);
        levelUplog.setCurrent_exp(newExp);
        levelUplog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(levelUplog);
    }

    /**
     * tbllog_role 创建角色日志
     */
    public void roleLog(String name, int sex) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_role rolelog = new tbllog_role();
        rolelog.setPlatform(cache.getPlat_name());
        rolelog.setRole_id(player.getPlayerId());
        rolelog.setRole_name(name);
        rolelog.setAccount_name(player.getAccountName());
        rolelog.setUser_ip(cache.getUser_ip());
        rolelog.setDim_prof(0);
        rolelog.setDim_sex(sex);
        rolelog.setDid(cache.getImei());
        rolelog.setGame_version(cache.getGame_version());
        rolelog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(rolelog);
    }

    public boolean checkString(String name) {
        boolean falg = false;
        if (name.indexOf(":") >= 0 || name.indexOf(";") >= 0 || name.indexOf("\"") >= 0 || name.indexOf("'") >= 0 || name.indexOf(",") >= 0 || name.indexOf("/") >= 0 || name.indexOf("\\") >= 0 || name.indexOf("*") >= 0) {
            return falg;
        } else {
            return true;
        }
    }

    //角色名称长度校验
    public int roleNameCheck(String name) {
        int ss = 0;
        if (!checkString(name)) {
            ss = ActorDefine.CHARS_ROLE_NAME;
            return ss;
        } else if (isChinese(name)) {
            //若角色名为全中文，进行长度校验
            if (name.length() > ActorDefine.ROLE_CHINESENAME_LENGTH_MAX || name.length() < ActorDefine.ROLE_CHINESENAME_LENGTH_MIN) {
                ss = ActorDefine.CHINESE_ROLE_NAME;

            }
        } else if (name.matches("^[a-zA-Z]*")) {
            //若角色名为全英文，进行长度校验
            if (name.length() > ActorDefine.ROLE_ENGlISHNAME_LENGTH_MAX || name.length() < ActorDefine.ROLE_ENGlISHNAME_LENGTH_MIN) {
                ss = ActorDefine.ENGLISH_ROLE_NAME;
                return ss;
            }
        } else {
            //中英混合则只进行最大长度限制
            ss = ActorDefine.CHINESE_ENGLISH_ROLE_NAME;
        }
        return ss;
    }

    //判断字符窜是否为全中文
    public boolean isChinese(String name) {
        {
            boolean temp = false;
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(name);
            if (m.find()) {
                temp = true;
            }
            return temp;
        }
    }

    /**
     * tblog_checkin 签到参与度(30天礼包)
     */
    private void checkinLog() {
        PlayerCache cache = getPlayerCache();
        tbllog_checkin checkinlog = new tbllog_checkin();
        checkinlog.setPlatform(cache.getPlat_name());
        checkinlog.setAccount_name(player.getAccountName());
        checkinlog.setDim_level(player.getLevel());
        checkinlog.setRole_id(player.getId());
        checkinlog.setHappend_time(GameUtils.getServerTime());
        checkinlog.setLog_time(GameUtils.getServerTime());
        sendPorxyLog(checkinlog);
    }

    /***
     * 系统提示
     ***/
    public List<M2.TipInfo> getTipInfos() {
        List<M2.TipInfo> list = new ArrayList<M2.TipInfo>();
        Map<Integer, Integer> map = getTipmap();
        for (int key : map.keySet()) {
            list.add(getTipInfo(key, map.get(key)));

        }
        return list;
    }

    public M2.TipInfo getTipInfo(int type, int num) {
        M2.TipInfo.Builder builder = M2.TipInfo.newBuilder();
        builder.setType(type);
        builder.setNum(num);
        return builder.build();
    }

    public Map<Integer, Integer> getTipmap() {
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
      //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        TaskProxy taskProxy = getGameProxy().getProxy(ActorDefine.TASK_PROXY_NAME);
        MailProxy mailProxy = getGameProxy().getProxy(ActorDefine.MAIL_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        OrdnanceProxy ordnanceProxy = getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        LotterProxy lotterProxy = getGameProxy().getProxy(ActorDefine.LOTTER_PROXY_NAME);
        ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        //部队
        int troopnum = soldierProxy.getSolierLostTypeNum();//TODO 出征部队
        PerformTasksProxy performTasksProxy = getGameProxy().getProxy(ActorDefine.PERFORMTASKS_PROXY_NAME);
        troopnum += performTasksProxy.getTaskNumforTip();
        map.put(TipDefine.TIP_TYPE_TROOP, troopnum);
        //关卡
        List<JSONObject> jsonObjects = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
        int advenum = 0;
        for (JSONObject jsonObject : jsonObjects) {
         //   int num = timerdbProxy.getTimerNum(TimerDefine.ADVENCE_REFRESH, jsonObject.getInt("type"), 0);
          //  int less = timerdbProxy.getAdventureTimesById(jsonObject.getInt("ID"));
            if (jsonObject.getInt("type") == 4) {
             //   if (getPowerValue(PlayerPowerDefine.POWER_level) > jsonObject.getInt("level") && less > 0) {
              //      advenum++;
             //   }
            } else {
             //   if (less > 0) {
             //       advenum += less;
           //     }
            }
        }
        map.put(TipDefine.TIP_TYPE_PASS, advenum);
        //任务
        map.put(TipDefine.TIP_TYPE_TASK, taskProxy.getTaskTipNum());
        //邮件
        map.put(TipDefine.TIP_TYPE_MAIL, mailProxy.unReadMail());
        //背包
        map.put(TipDefine.TIP_TYPE_BAG, itemProxy.getItemCanUse());
        //装备
        map.put(TipDefine.TIP_TYPE_EQUIP, equipProxy.getEquipOnBagnum());
        //军靴
        map.put(TipDefine.TIP_TYPE_ORDNANCE, ordnanceProxy.getOrdnanceOnbag());
        //抽装备免费
        map.put(TipDefine.TIP_TYPE_LOTTERY_EQUIP, lotterProxy.getEquipFreeNum());
        //社交祝福数
        map.put(TipDefine.TIP_TYPE_CSOCIAL, getCanGetBless());
        map.put(TipDefine.TIP_TYPE_TAOBAO, lotterProxy.getFreTaobaoLotterTimes(1));
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        //普通活动
        int[] activityCount = activityProxy.getActivityCanGetNum();
        map.put(TipDefine.TIP_TYPE_ACTIVITY, activityCount[0]);
        //限时活动
        map.put(TipDefine.TIP_TYPE_ACTIVITY_LIMIT, activityCount[1]);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        map.put(TipDefine.TIP_TYPE_THIRTHTY, playerProxy.getRewardNum().size());
        map.put(TipDefine.TIP_TYPE_ACTIVITY_ARMY_BIGREWARD, activityProxy.getArmyActivityNum());
        return map;
    }

    //获得可领取祝福的数量
    public int getCanGetBless() {
        int num = 0;
        Set<Long> set = getBeBlessSet();
        for (long id : set) {
            if (!isGetBless(id)) {
                num++;
            }
        }
        return num;
    }

    //是否设置了自动补充防守队列
    public int getSettingAutoAddDefendList() {
        return player.getSettingAutoAddDefendlist();
    }

    //30天登陆记录第几天
    public boolean loginDayNum() {
        long dayTime = GameUtils.getServerDate().getTime();
        GameProxy gameProxy = super.getGameProxy();
       // TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
 /*       long isNew = timerProxy.addTimer(TimerDefine.LOGIN_DAY_NUM, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this);
        if (isNew != 0) {
            timerProxy.setLastOperatinTime(TimerDefine.LOGIN_DAY_NUM, 0, 0, dayTime - TimerDefine.ONE_DAY);
        }*/
      //  Long lastTime = timerProxy.getLastOperatinTime(TimerDefine.LOGIN_DAY_NUM, 0, 0);
       // boolean sameDay = false;
        int state=getPlayer().getFirthlogin();
     //   sameDay = DateUtil.isCanGet(dayTime, lastTime, TimerDefine.TIMER_REFRESH_FOUR);
        if (state == UtilDefine.REWARD_STATE_NONE_GET) {
            if (getLoginDayNum() < ActorDefine.LOGIN_DAY_NUM) {
                setLoginDayNum(getLoginDayNum() + 1);
                addRewardNum(getLoginDayNum());
                System.err.println("今天是登录第：" + getLoginDayNum() + "天");
            //    timerProxy.setLastOperatinTime(TimerDefine.LOGIN_DAY_NUM, 0, 0, dayTime);
                player.setFirthlogin(UtilDefine.REWARD_STATE_HAS_GET);
                return true;
            }
        }
        return false;
    }

    //30天登陆，领取奖励
    public int getRewardDayNum(int dayNum, PlayerReward reward) {
      //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
    //    timerdbProxy.addTimer(TimerDefine.LOGIN_DAY_NUM, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this);
        if (getRewardNum().size() <= 0) {
            return ErrorCodeDefine.M20015_2;
        }
        if (!getRewardNum().contains(dayNum)) {
            return ErrorCodeDefine.M20015_4;
        }
        JSONObject jsonObj = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYLAND, dayNum);
        if (jsonObj == null) {
            return ErrorCodeDefine.M20015_3;
        } else {
            JSONArray jsonArray = jsonObj.getJSONArray("rewardId");
            if (jsonArray.length() > 0) {
                RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                for (int i = 0; i < jsonArray.length(); i++) {
                    //发送奖励
                    rewardProxy.getPlayerRewardByFixReward(jsonArray.getInt(i), reward);
                }
                EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
                if (equipProxy.getEquipBagLesFree() < reward.generalMap.size()) {
                    return ErrorCodeDefine.M20015_6;
                }
                removeRewardNum(dayNum);
                rewardProxy.getRewardToPlayer(reward, LogDefine.GET_ARMYGROUP_WELFAREREWARD);
                /**
                 * tblog_checkin 签到参与度(30天礼包)
                 */
                checkinLog();
                sendFunctionLog(FunctionIdDefine.LOGIN_REWARD_FUNCTION_ID, dayNum, 0, 0);
            }
        }
        return 0;
    }

    //每日登录抽奖
    public List<M2.loginLottery> loginLottery(PlayerReward reward) {
        GameProxy gameProxy = super.getGameProxy();
        List<M2.loginLottery> rewardList = new ArrayList<>();
      //  TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
     //   timerProxy.addTimer(TimerDefine.LOGIN_LOTTERY, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, this);
        int num = getPlayer().getEverylottery();//timerProxy.getTimerNum(TimerDefine.LOGIN_LOTTERY, 0, 0);
        if (num < 1) {
            RewardProxy rewardProxy = gameProxy.getProxy(ActorDefine.REWARD_PROXY_NAME);
            List<Integer> reward1 = new ArrayList<>();
            int rewardId1 = rewardProxy.getPlayerRewardByRandContent(LotterDefine.LOGIN_LOTTERY_G1, reward, reward1);
            M2.loginLottery.Builder loginlt = M2.loginLottery.newBuilder();
            loginlt.setPower(reward1.get(0));
            loginlt.setItemId(reward1.get(1));
            loginlt.setNum(reward1.get(2));
            rewardList.add(loginlt.build());
            List<Integer> reward2 = new ArrayList<>();
            int rewardId2 = rewardProxy.getPlayerRewardByRandContent(LotterDefine.LOGIN_LOTTERY_G2, reward, reward2);
            M2.loginLottery.Builder loginlt2 = M2.loginLottery.newBuilder();
            loginlt2.setPower(reward2.get(0));
            loginlt2.setItemId(reward2.get(1));
            loginlt2.setNum(reward2.get(2));
            rewardList.add(loginlt2.build());
            List<Integer> reward3 = new ArrayList<>();
            int rewardId3 = rewardProxy.getPlayerRewardByRandContent(LotterDefine.LOGIN_LOTTERY_G3, reward, reward3);
            M2.loginLottery.Builder loginlt3 = M2.loginLottery.newBuilder();
            loginlt3.setPower(reward3.get(0));
            loginlt3.setItemId(reward3.get(1));
            loginlt3.setNum(reward3.get(2));
            rewardList.add(loginlt3.build());
           // timerProxy.addNum(TimerDefine.LOGIN_LOTTERY, 0, 0, 1);
            getPlayer().setEverylottery(UtilDefine.REWARD_STATE_HAS_GET);
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLOTTERY);
            sendFunctionLog(FunctionIdDefine.EVERYDAY_SWEEPSTAKE_FUNCTION_ID, rewardId1, rewardId2, rewardId3);
        }
        return rewardList;
    }


    public void setBoomRefTime(long time) {
        player.setBoomRefTime(time);
        if(simplePlayer!=null) {
            simplePlayer.setBoomRefTime(time);
        }
        player.save();
    }

    public void setEnergyRefTime(long time) {
        player.setEnergyaddtime(time);
        player.save();
    }


    public long getBoomRefTime() {
        return    player.getBoomRefTime();
    }

    public long getEnergyRefTimes() {
        return    player.getEnergyaddtime();
    }

    public void setWorldTilePoint(int x, int y) {
        player.setWorldTileX(x);
        player.setWorldTileY(y);
        simplePlayer.setX(x);
        simplePlayer.setY(y);
        //修改了位置保存一下
        player.save();
    }

    public int getCdKeyTimes(int type) {
        String cdkeyStr = player.getCdkeyStr();
        if (cdkeyStr.length() == 0) {
            return 0;
        }
        int times = 0;
        for (String temp : cdkeyStr.split("&")) {
            String[] strs = temp.split(",");
            int cdtype = Integer.parseInt(strs[0]);
            int cdNum = Integer.parseInt(strs[1]);
            if (cdtype == type) {
                times = cdNum;
                break;
            }
        }
        return times;
    }

    public void addCdKeyTimes(int type) {
        String cdkeyStr = player.getCdkeyStr();
        if (cdkeyStr.length() == 0) {
            cdkeyStr = type + "," + 1 + "&";
            player.setCdkeyStr(cdkeyStr);
            return;
        }
        HashMap<Integer, Integer> nums = new HashMap<>();
        for (String temp : cdkeyStr.split("&")) {
            String[] strs = temp.split(",");
            int cdtype = Integer.parseInt(strs[0]);
            int cdNum = Integer.parseInt(strs[1]);
            nums.put(cdtype, cdNum);
        }
        if (nums.containsKey(type)) {
            nums.put(type, nums.get(type) + 1);
        } else {
            nums.put(type, 1);
        }
        StringBuffer sb = new StringBuffer();
        for (Integer key : nums.keySet()) {
            sb.append(key);
            sb.append(",");
            sb.append(nums.get(key));
            sb.append("&");
        }
        player.setCdkeyStr(sb.toString());
    }

    //判断角色等级是否符合活动开启等级
    public boolean checkeOpenLevel(int openId) {
        JSONObject requirelevelinfos = ConfigDataProxy.getConfigInfoFindById(DataDefine.FUNCTION_OPEN, openId);
        int requirelevel = requirelevelinfos.getInt("openlevel");
        int level = getLevel();
        boolean falg = false;
        if (level < requirelevel) {
            return falg;
        } else {
            falg = true;
            return falg;
        }
    }


    public void onBanPlayerChat(int time, int status) {
        player.setBanChatAct(status);
        player.setBanChatActDate(time);
        player.save();
    }

    public boolean isBanChat() {
        return player.getBanChatAct() == ActorDefine.BAN_STATUS_BAN && player.getBanChatActDate() > GameUtils.getServerTime();
    }

    //打包系统公告(不替换任何值)
    public M25.M250000.S2C packSystemChatNoChange(int type, int condition, int condition2) {
        PlayerChat chat = new PlayerChat();
        chat.playerName = "系统公告";
        JSONObject notice = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.SYSTEM_NOTICE, "type", type, "condition1", condition, "condition2", condition2);
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        if (notice != null) {
            String notices = notice.getString("notice");
            chat.context = notices;
            chat.type = ActorDefine.CHAT_TYPE;
            chat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE;
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            chat.legionName = playerProxy.getLegionName();
            chat.level = playerProxy.getLevel();
            builder.setChat(getChatInfo(chat));
            builder.setTypeId(notice.getInt("ID"));
        }
        builder.setRs(0);
        builder.setType(4);
        builder.setShareType(1);
        return builder.build();
    }

    //打包系统公告(只替换notice中的单个值)
    public M25.M250000.S2C packSystemChat(int type, int condition, int condition2) {
        PlayerChat chat = new PlayerChat();
        chat.playerName = "系统公告";
        JSONObject notice = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.SYSTEM_NOTICE, "type", type, "condition1", condition, "condition2", condition2);
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        if (notice != null) {
            String rolename = player.getName();
            String notices = notice.getString("notice");
            String newnotices = notices.replaceAll("NAME", rolename);
            chat.type = ActorDefine.CHAT_TYPE;
            chat.context = newnotices;
            chat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE;
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            chat.legionName = playerProxy.getLegionName();
            chat.level = playerProxy.getLevel();
            builder.setChat(getChatInfo(chat));
            builder.setTypeId(notice.getInt("ID"));
        } else {
            return null;
        }
        builder.setRs(0);
        builder.setType(4);
        builder.setShareType(1);
        return builder.build();
    }

    //打包系统公告(替换notice中的2个值)
    public M25.M250000.S2C packSystemChat(int type, int condition, int condition2, String string) {
        PlayerChat chat = new PlayerChat();
        JSONObject notice = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.SYSTEM_NOTICE, "type", type, "condition1", condition, "condition2", condition2);
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        if (notice != null) {
            String rolename = player.getName();
            chat.playerName = "系统公告";
            String notices = notice.getString("notice");
            String newnotices = notices.replaceAll("NAME", rolename);
            String nownotice;
            if (type == ActorDefine.COMMAND_UPGRADE_NOTICE_TYPE) {
                nownotice = newnotices.replaceAll("XX", string);
            } else {
                nownotice = newnotices.replaceAll("XXX", string);
            }
            chat.type = ActorDefine.CHAT_TYPE;
            chat.context = nownotice;
            chat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE;
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            chat.legionName = playerProxy.getLegionName();
            chat.level = playerProxy.getLevel();
            builder.setChat(getChatInfo(chat));
            builder.setTypeId(notice.getInt("ID"));
            builder.setType(4);
            builder.setShareType(1);
        }
        builder.setRs(0);
        return builder.build();
    }

    //打包系统公告(替换notice中的3个值)
    public M25.M250000.S2C packSystemChat(int type, int condition, int condition2, String stirng, String stirngone) {
        PlayerChat chat = new PlayerChat();
        JSONObject notice = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.SYSTEM_NOTICE, "type", type, "condition1", condition, "condition2", condition2);
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        if (notice != null) {
            String rolename = player.getName();
            chat.playerName = "系统公告";
            String notices = notice.getString("notice");
            String newnotices = notices.replaceAll("NAME", rolename);
            String newnoticeses = newnotices.replaceFirst("XXX", stirng);
            String newnoticess = newnoticeses.replaceAll("XXX", stirngone);
            chat.type = ActorDefine.CHAT_TYPE;
            chat.context = newnoticess;
            chat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE;
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            chat.legionName = playerProxy.getLegionName();
            chat.level = playerProxy.getLevel();
            builder.setChat(getChatInfo(chat));
            builder.setTypeId(notice.getInt("ID"));
        }

        builder.setRs(0);
        builder.setType(4);
        builder.setShareType(1);
        return builder.build();
    }

    //打包系统公告(替换notice中的4个值)
    public M25.M250000.S2C packSystemChat(int type, int condition, String string, String Lv, String percent) {
        PlayerChat chat = new PlayerChat();
        chat.playerName = "系统公告";
        JSONObject notice = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.SYSTEM_NOTICE, "type", type, "condition1", condition);
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        if (notice != null) {
            String rolename = player.getName();
            String notices = notice.getString("notice");
            String newnotices = notices.replaceAll("NAME", rolename);
            String nownotice1 = newnotices.replaceFirst("XXX", string);
            String nownotice2 = nownotice1.replaceFirst("X", Lv);
            String nownotice3 = nownotice2.replaceAll("XXX", percent);
            chat.type = ActorDefine.CHAT_TYPE;
            chat.context = nownotice3;
            chat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE;
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            chat.legionName = playerProxy.getLegionName();
            chat.level = playerProxy.getLevel();
            builder.setChat(getChatInfo(chat));
            builder.setTypeId(notice.getInt("ID"));

            builder.setType(4);
            builder.setShareType(1);
        }
        builder.setRs(0);
        return builder.build();
    }

    //打包系统公告(替换notice中的2个值 不用condition1遍历notice表)
    public M25.M250000.S2C packSystemChat(int type, int condition2, String string) {
        PlayerChat chat = new PlayerChat();
        chat.playerName = "系统公告";
        JSONObject notice = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.SYSTEM_NOTICE, "type", type, "condition2", condition2);
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        if (notice != null) {
            String rolename = player.getName();
            String notices = notice.getString("notice");
            String newnotices = notices.replaceAll("NAME", rolename);
            String nownotice3 = newnotices.replaceAll("XXX", string);
            chat.type = ActorDefine.CHAT_TYPE;
            chat.context = nownotice3;
            chat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE;
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            chat.legionName = playerProxy.getLegionName();
            chat.level = playerProxy.getLevel();
            builder.setChat(getChatInfo(chat));
            builder.setTypeId(notice.getInt("ID"));

            builder.setType(4);
            builder.setShareType(1);
        }
        builder.setRs(0);
        return builder.build();
    }


    //发送系统公告
    public void sendSystemchatNoChange(int type, int condition, int condition2) {
        sendSystemChatToPlayerService(packSystemChatNoChange(type, condition, condition2));

    }

    public void sendSystemchatNoChange(int type, int condition2, String string) {
        sendSystemChatToPlayerService(packSystemChat(type, condition2, string));

    }

    public void sendSystemchat(int type, int condition, int condition2) {
        M25.M250000.S2C s2c = packSystemChat(type, condition, condition2);
        if (s2c != null) {
            sendSystemChatToPlayerService(s2c);
        }

    }

    public void sendSystemchat(int type, int condition, int condition2, String string) {
        sendSystemChatToPlayerService(packSystemChat(type, condition, condition2, string));

    }

    public void sendSystemchat(int type, int condition, int condition2, String string, String stirngone) {
        sendSystemChatToPlayerService(packSystemChat(type, condition, condition2, string, stirngone));

    }

    public void sendSystemchat(int type, int condition, String string, String Lv, String percent) {
        sendSystemChatToPlayerService(packSystemChat(type, condition, string, Lv, percent));

    }

    public int getPlayerType() {
        int type = 0;
        int now = GameUtils.getServerTime();
        if (now >= player.getTypeBeginTime() && now <= player.getTypeEndTime()) {
            type = player.getPlayerType();
        }
        return type;
    }

    public void setPlayerType(int type, int beginTime, int endTime) {
        player.setPlayerType(type);
        player.setTypeBeginTime(beginTime);
        player.setTypeEndTime(endTime);
    }


    public M2.LogionRewardInfo getLogionRewadInfo() {
        M2.LogionRewardInfo.Builder builder = M2.LogionRewardInfo.newBuilder();
        int dayNum = 0;
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int type = 0;
        int rs = 0;
        //TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
       int num = player.getEverylottery();//timerdbProxy.getTimerNum(TimerDefine.LOGIN_LOTTERY, 0, 0);
        try {
            if (dayNum == 0) {//请求可不可领
                if (playerProxy.getRewardNum().size() <= 0 && playerProxy.getLoginDayNum() >= ActorDefine.LOGIN_DAY_NUM) {
                    type = 2;//显示每日抽奖
                    builder.addAllCanGet(new ArrayList<Integer>());
                    if (num == 0) {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 1);
                    } else {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 2);
                    }
                } else {
                    boolean open = playerProxy.checkeOpenLevel(ActorDefine.OPEN_THIRTY_LOGIN_AWARD_ID);
                    type = 1;//显示30登录领取奖励
                  /*  if (!open) {
                        rs = ErrorCodeDefine.M20015_5;
                    } else {*/
                    builder.addAllCanGet(playerProxy.getRewardNum());
                    builder.setAllDay(playerProxy.getLoginDayNum());
                    // }
                }
                builder.setType(type);
            } else {//领取情况
                if (playerProxy.getRewardNum().size() == 0 && playerProxy.getLoginDayNum() >= ActorDefine.LOGIN_DAY_NUM) {
                    type = 2;//每日抽奖
                    builder.addAllCanGet(null);
                    rs = 0;
                    if (num == 0) {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 1);
                    } else {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 2);
                    }
                } else {
                    rs = playerProxy.getRewardDayNum(dayNum, reward);
                    type = 1;//30登录领取奖励
                    builder.addAllCanGet(playerProxy.getRewardNum());
                    builder.setAllDay(playerProxy.getLoginDayNum());
                }
                builder.setType(type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public M14.ChatInfo getChatInfo(PlayerChat chat) {
        M14.ChatInfo.Builder builder = M14.ChatInfo.newBuilder();
        builder.setIconId(chat.iconId);
        builder.setName(chat.playerName);
        builder.setContext(chat.context);
        builder.setType(chat.type);
        builder.setPlayerId(chat.playerId);
        builder.setTime((int) (chat.time / 1000));
        builder.setVipLevel(chat.vipLevel);
        builder.setPlayerType(chat.playerType);
        builder.setPendantId(chat.pendantId);
        builder.setLegionName(chat.legionName);
        builder.setLevel(chat.level);
        return builder.build();
    }

    /*******
     * 分享获得聊天信息*分享协议添加
     ******/
    public M14.ChatInfo getChatInfo2Trumpe(Long playerId, String mess) {
        SimplePlayer simplePlayer = PlayerService.getSimplePlayer(playerId, areaKey);
        if (simplePlayer == null) {
            return null;
        }
        M14.ChatInfo.Builder builder = M14.ChatInfo.newBuilder();
        builder.setIconId(simplePlayer.getIconId());
        builder.setName(simplePlayer.getName());
        builder.setContext(mess);
        builder.setType(ChatAndMailDefine.CHAT_TYPE_WORLD);
        builder.setPlayerId(simplePlayer.getId());
        builder.setTime(GameUtils.getServerTime());
        builder.setVipLevel(simplePlayer.getVipLevel());
        builder.setPlayerType(0);
        builder.setPendantId(simplePlayer.getPendant());
        builder.setLegionName(simplePlayer.getLegionName());
        builder.setLevel(simplePlayer.getLevel());
        return builder.build();
    }


    public int getNewGift(PlayerReward reward) {
        if (getHaveNewGift() > 0) {
            return ErrorCodeDefine.M20301_1;
        }
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        rewardProxy.getPlayerReward(ActorDefine.NEW_PLAYER_REWARD_ID, reward);
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_NEW_PLAYER);
        player.setHaveGetNewGift(1);
        return 0;
    }

    public int getHaveNewGift() {
        return player.getHaveGetNewGift();
    }


    public List<M21.RankListInfo> getRankInfos() {
        List<M21.RankListInfo> infos = new ArrayList<M21.RankListInfo>();
        for (int i = 1; i <= PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE; i++) {
            M21.PowerRankInfo.Builder myRankInfo = M21.PowerRankInfo.newBuilder();
            int typeId = i;
            M21.RankListInfo.Builder info = null;
            if (i == PowerRanksDefine.POWERRANK_TYPE_ARENA) {
                info = ArenaService.getArenaRankMap(areaKey);
            } else {
                info = PowerRanksService.onGetRankByType(i, getPlayerId(), areaKey);
            }
            boolean onRank = false;
            if (info.getPowerRankInfoList().size() > 0) {
                //我的排名
                for (M21.PowerRankInfo myinfo : info.getPowerRankInfoList()) {
                    if (myinfo.getPlayerId() == getPlayerId()) {
                        myRankInfo.setLevel(myinfo.getLevel());
                        myRankInfo.setName(getAccountName());
                        myRankInfo.setTypeId(myinfo.getTypeId());
                        myRankInfo.setRankValue(myinfo.getRankValue());
                        myRankInfo.setPlayerId(getPlayerId());
                        myRankInfo.setRank(myinfo.getRank());
                        onRank = true;
                    }
                }
            }
            //我的排名
            if (onRank) { //我在榜上
                info.setMyRank(myRankInfo);
            } else {//我未上榜
//                    int typeId = builder.getPowerRankInfoList().get(0).getTypeId();
                if (typeId == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) {
                    myRankInfo.setLevel((int) getPowerValue(PlayerPowerDefine.POWER_atklv));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) {
                    myRankInfo.setLevel((int) getPowerValue(PlayerPowerDefine.POWER_critlv));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) {
                    myRankInfo.setLevel((int) getPowerValue(PlayerPowerDefine.POWER_dogelv));
                } else {
                    myRankInfo.setLevel(getLevel());
                }
                myRankInfo.setName(getAccountName());
                myRankInfo.setTypeId(typeId);
                if (typeId == PowerRanksDefine.POWERRANK_TYPE_CAPACITY) {
                    myRankInfo.setRankValue(getPowerValue(PlayerPowerDefine.NOR_POWER_highestCapacity));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_CUSTOMS) {
                    DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    myRankInfo.setRankValue(dungeoProxy.rankStarNum());
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) {
                    myRankInfo.setRankValue(getPowerValue(PlayerPowerDefine.NOR_POWER_atkRate));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) {
                    myRankInfo.setRankValue(getPowerValue(PlayerPowerDefine.NOR_POWER_critRate));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) {
                    myRankInfo.setRankValue(getPowerValue(PlayerPowerDefine.NOR_POWER_dodgeRate));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_HONOR) {
                    myRankInfo.setRankValue(getPowerValue(PlayerPowerDefine.POWER_honour));
                } else {
                    myRankInfo.setRankValue(0);
                }
                myRankInfo.setPlayerId(getPlayerId());
                myRankInfo.setRank(0);
            }
            info.setTypeId(typeId);
            info.setMyRank(myRankInfo);
            infos.add(info.build());
        }
        return infos;
    }

    //获得拉吧免费过次数
    public int getLabafreetimes(){
        return player.getLabafree();
    }
    //增加拉吧免费抽过次数
    public void addlabafreetimes(int add){
        player.setLabafree(player.getLabafree()+1);
    }


    /**
     * 零点重置
     */
    @Override

    public void zeroTimerEventHandler() {
        player.setZeroTime(GameUtils.getTodayTimeForHourInt(0));
    }

    public void addjunshigoldTimes(int times){
        player.setJunshigoldtimes(player.getJunshigoldtimes() + times);
    }

    public int getjunshigoldTimes(){
        return player.getJunshigoldtimes();
    }

    public void addjunshiresouceTimes(int times){
        player.setJunshiresoucetimes(player.getJunshiresoucetimes()+times);
    }

    public int getjunshiresouceTimes(){
        return player.getJunshiresoucetimes();
    }

    public M2.M20500.S2C getBommTimeInfo(){
        M2.M20500.S2C.Builder builder = M2.M20500.S2C.newBuilder();
        builder.setRs(0);
        int needtime=boom2foolTime();
        builder.setBoomRefTime(needtime);
        return builder.build();
    }
    @Override
    public void fixedTimeEventHandler() {
        player.setResetDataTime(GameUtils.getTodayTimeForHourInt(4));
        //军师抽奖重置
        player.setJunshigoldtimes(0);
        player.setJunshiresoucetimes(0);
        //西域远征
        player.setDungeolimitrest(0);
        player.setDungeolimitchange(0);
       //每日自动声望刷新
        getPlayer().setPrestaeReward(0);
        getPlayer().setPrestaeshouxun(0);
       //每日抽奖 30天开服
        getPlayer().setEverylottery(0);
        player.setFirthlogin(0);
        loginDayNum();
      //每日体力购买次数刷新
        player.setBuyenergytimes(0);
      //普通武将每日刷新免费次数
        player.setLottertime1(0);
       //拉霸免费次数
        player.setLabafree(0);
      //刷新每日的祝福可获取奖励的次数
        player.setDaybless(0);
       //刷新每日可领取祝福奖励的次数
        player.setGetbless(0);
        Set<Long> blessSet = new HashSet<>();
        //刷新已祝福玩家列表
        player.setBlessSet(blessSet);
        //刷新已领祝福玩家列表
        Set<Long> blessSet1 = new HashSet<>();
        player.setGetBlessSet(blessSet1);
        //刷新祝福自己的玩家列表
        Set<Long> blessSet2 = new HashSet<>();
        player.setBeBlessSet(blessSet2);
        //掏宝刷新
        player.setTaobaofree(0);
        //每日重置在线总时长
        player.setOnlinetime(0);
    }

    public int getRistChangeTimes(){
        JSONObject jsonObject=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ADVENTURE,"type",BattleDefine.ADVANTRUE_TYPE_LIMIT);
        if(getPowerValue(PlayerPowerDefine.POWER_level)<jsonObject.getInt("level")){
            return 0;
        }
        return DungeonDefine.DEOGEO_LIMIT_CHANGE -getPlayer().getDungeolimitchange();
    }

    public int getRistRestTimes(){
        JSONObject jsonObject=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ADVENTURE,"type",BattleDefine.ADVANTRUE_TYPE_LIMIT);
        if(getPowerValue(PlayerPowerDefine.POWER_level)<jsonObject.getInt("level")){
            return 0;
        }
        return DungeonDefine.DEOGEO_LIMIT_REST - getPlayer().getDungeolimitrest();
    }

}
