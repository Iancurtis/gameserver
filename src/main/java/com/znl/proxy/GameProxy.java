package com.znl.proxy;

import com.znl.GameMainServer;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.define.ActorDefine;
import com.znl.define.TimerDefine;
import com.znl.pojo.db.Player;
import com.znl.utils.GameUtils;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.Map;

/**
 * Created by Administrator on 2015/10/27.
 */
public class GameProxy {

    private Map<String, BasicProxy> proxyMap = new ConcurrentHashMap<String, BasicProxy>();

    public GameProxy(Player player, PlayerCache cache) {
        initProxys(player, cache);
        //判断是否已经4点重置了数据
        if(GameUtils.hasNotResetDataHandler(player.getResetDataTime(), 4)){
            fixedTimeHandler();
        }
        //判断是否已经0点重置了数据
        if(GameUtils.hasNotResetDataHandler(player.getZeroTime(), 0)){
            zeroTimeHandler();
        }
        afterLoginEvent();
    }

    public void finalize() {
        for (BasicProxy basicProxy : proxyMap.values()){
            try {
                basicProxy.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
//        proxyMap.values().forEach(v -> v.finalize());
    }

    public void save() {
        proxyMap.values().forEach(v -> v.saveDbPojo());
    }

    private String areaKey;

    public String getAreaKey(){
        return areaKey;
    }

    private void initProxys(Player player, PlayerCache cache) {
        this.areaKey = GameMainServer.getAreaKeyByAreaId(player.getAreaId());
        PlayerProxy playerProxy = new PlayerProxy(player,areaKey);
        playerProxy.setGameProxy(this);
        playerProxy.setPlayerCache(cache);
        proxyMap.put(ActorDefine.PLAYER_PROXY_NAME, playerProxy);

        SoldierProxy soldierProxy = new SoldierProxy(player.getSoldierSet(),areaKey);
        soldierProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.SOLDIER_PROXY_NAME, soldierProxy);

        ItemProxy itemProxy = new ItemProxy(player.getItemSet(),areaKey);
        itemProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ITEM_PROXY_NAME, itemProxy);

        DungeoProxy dungeoProxy = new DungeoProxy(player.getDungeoSet(),areaKey);
        dungeoProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.DUNGEO_PROXY_NAME, dungeoProxy);

        BuildingProxy buildingProxy = new BuildingProxy(player.getBuildingId(),areaKey);
        buildingProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.BUILDING_PROXY_NAME, buildingProxy);
        buildingProxy.init();

        BattleProxy battleProxy = new BattleProxy(areaKey);
        battleProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.BATTLE_PROXY_NAME, battleProxy);

        EquipProxy equipProxy = new EquipProxy(player.getEquipSet(),areaKey);
        equipProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.EQUIP_PROXY_NAME, equipProxy);

        OrdnancePieceProxy ordnancePieceProxy = new OrdnancePieceProxy(player.getOrdnancePieceSet(),areaKey);
        ordnancePieceProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ORDANCEPIECE_PROXY_NAME, ordnancePieceProxy);

        OrdnanceProxy ordnanceProxy = new OrdnanceProxy(player.getOrdnanceSet(),areaKey);
        ordnanceProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ORDANCE_PROXY_NAME, ordnanceProxy);

        ResFunBuildProxy resFunBuildProxy = new ResFunBuildProxy(player.getResFunBuildingSet(),areaKey);
        resFunBuildProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.RESFUNBUILD_PROXY_NAME, resFunBuildProxy);

        RewardProxy rewardProxy = new RewardProxy(areaKey);
        rewardProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.REWARD_PROXY_NAME, rewardProxy);


        TechnologyProxy technologyProxy = new TechnologyProxy(player.getTechnologySet(),areaKey);
        technologyProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.TECHNOLOGY_PROXY_NAME, technologyProxy);

        FormationProxy formationProxy = new FormationProxy(player.getFormationMember1Set(), player.getFormationMember2Set(), player.getFormationMember3Set(),areaKey);
        formationProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.FORMATION_PROXY_NAME, formationProxy);

        SkillProxy skillProxy = new SkillProxy(player.getSkillSet(),areaKey);
        skillProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.SKILL_PROXY_NAME, skillProxy);

        PerformTasksProxy performTasksProxy = new PerformTasksProxy(player,areaKey);
        performTasksProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.PERFORMTASKS_PROXY_NAME, performTasksProxy);

        ItemBuffProxy itemBuffProxy = new ItemBuffProxy(player.getItemBuffSet(),areaKey);
        itemBuffProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ITEMBUFF_PROXY_NAME, itemBuffProxy);

        LotterProxy lotterProxy = new LotterProxy(areaKey);
        lotterProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.LOTTER_PROXY_NAME, lotterProxy);

        MailProxy mailProxy = new MailProxy(player.getMailSet(), player.getReportSet(),areaKey);
        mailProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.MAIL_PROXY_NAME, mailProxy);

        SystemProxy systemProxy = new SystemProxy(areaKey);
        systemProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.SYSTEM_PROXY_NAME, systemProxy);

        TaskProxy taskProxy = new TaskProxy(player,areaKey);
        taskProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.TASK_PROXY_NAME, taskProxy);

        FriendProxy friendProxy = new FriendProxy(areaKey);
        friendProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.FRIEND_PROXY_NAME, friendProxy);

        VipProxy vipProxy = new VipProxy(areaKey);
        vipProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.VIP_PROXY_NAME, vipProxy);

        ArenaProxy arenaProxy = new ArenaProxy(player,areaKey);
        arenaProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ARENA_PROXY_NAME, arenaProxy);

        ArmyGroupProxy armyGroupProxy = new ArmyGroupProxy(player,areaKey);
        armyGroupProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ARMYGROUP_PROXY_NAME, armyGroupProxy);

        ActivityProxy activityProxy = new ActivityProxy(player.getActivitySet(),areaKey);
        activityProxy.setGameProxy(this);
        activityProxy.initActivityCondition();
        proxyMap.put(ActorDefine.ACTIVITY_PROXY_NAME,activityProxy);

        CollectProxy collectProxy = new CollectProxy(player.getColsets(),areaKey);
        collectProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.COLLECT_PROXY_NAME,collectProxy);

        AdviserProxy adviserProxy=new AdviserProxy(player.getAdvids(),areaKey);
        adviserProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.ADVISER_PROXY_NAME,adviserProxy);

        NewBuildProxy newBuildProxy = new NewBuildProxy(player.getResFunBuildingSet(),player.getProductions(),areaKey);
        newBuildProxy.setGameProxy(this);
        proxyMap.put(ActorDefine.NEW_BUILD_PROXY_NAME,newBuildProxy);

        equipProxy.initPurpleEquipLeveNum();
    }

    public void registerProxy(String name, BasicProxy proxy) {
        this.proxyMap.put(name, proxy);
    }

    public <T extends BasicProxy> T getProxy(String name) {
        return (T) proxyMap.get(name);
    }

    public long getExpandPowerValueForMetic(int power) {
        long value = 0;
        for (String key : this.proxyMap.keySet()) {
            BasicProxy proxy = proxyMap.get(key);
            value += proxy.getExpandPowerValue(power);
        }
        return value;
    }

    /**
     * 每天4点事件
     */
    public void fixedTimeHandler(){
        for(BasicProxy proxy:proxyMap.values()){
            proxy.fixedTimeEventHandler();
        }
    }

    /**
     * 每天零点事件
     */
    public void zeroTimeHandler(){
        for(BasicProxy proxy:proxyMap.values()){
            proxy.zeroTimerEventHandler();
        }
    }

    /**
     * 每次成功登录之后事件
     */
    public void afterLoginEvent(){
        for(BasicProxy proxy:proxyMap.values()){
            proxy.afterLoginEvent();
        }
    }

}
