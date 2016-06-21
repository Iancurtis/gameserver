package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.*;
import com.znl.define.*;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Armygroup;
import com.znl.proto.Common;
import com.znl.proto.M20;
import com.znl.proto.M7;
import com.znl.utils.GameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.Object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/10/28.
 */
public class ArenaProxy extends BasicProxy {


    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }

    public ArenaProxy(String areaKey) {
        this.areaKey = areaKey;
    }

    public Long changeArenaId = 0l;
    public List<PlayerTeam> rivaltems = new ArrayList<PlayerTeam>();
    public String rivalName = null;
    public long rivalId = 0l;
    public List<PlayerTeam> mytems = new ArrayList<PlayerTeam>();
    public boolean result = false;

    public PlayerTroop creatPlayerArena(M7.FormationInfo formationInfo, int general) {
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerTroop arena = new PlayerTroop();
        arena.setPlayerId(playerProxy.getPlayerId());
        arena.setFightElementInfos(formationInfo.getMembersList());
        List<PlayerTeam> playerTeams = new ArrayList<PlayerTeam>();
        FormationProxy formationProxy = getGameProxy().getProxy(ActorDefine.FORMATION_PROXY_NAME);
        playerTeams.addAll(formationProxy.createFormationTeam(SoldierDefine.FORMATION_ARENA));
        arena.setPlayerTeams(playerTeams);
        arena.setProtime(GameUtils.getServerDate().getTime());
        arena.setWintimes(0);
        DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        arena.setCapity(dungeoProxy.countSoldierCapacity(playerTeams));
        //发送到竞技场排名
        GameMsg.AddPlayerToRank arenaRank = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), dungeoProxy.countSoldierCapacity(playerTeams), PowerRanksDefine.POWERRANK_TYPE_ARENA);
        sendRankServiceMsg(arenaRank);
        return arena;

    }

    public void refreshPlayerArena(int general, PlayerTroop arena) {
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        arena.setPlayerId(playerProxy.getPlayerId());
        List<PlayerTeam> playerTeams = new ArrayList<PlayerTeam>();
        FormationProxy formationProxy = getGameProxy().getProxy(ActorDefine.FORMATION_PROXY_NAME);
        playerTeams.addAll(formationProxy.createFormationTeam(SoldierDefine.FORMATION_ARENA));
//        for(PlayerTeam team:playerTeams){
//            team.reset=true;
//        }
        arena.setPlayerTeams(playerTeams);
        DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        arena.setCapity(dungeoProxy.countSoldierCapacity(playerTeams));
        //发送到竞技场排名
        GameMsg.AddPlayerToRank arenaRank = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), dungeoProxy.countSoldierCapacity(playerTeams), PowerRanksDefine.POWERRANK_TYPE_ARENA);
        sendRankServiceMsg(arenaRank);
    }


    /*****
     * 保存阵形
     ******/
    public int saveQuen(M7.FormationInfo formationInfo, int general, PlayerTroop arena) {
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        for (M7.FormationMember trechInfo : formationInfo.getMembersList()) {
            int posi = trechInfo.getPost();
            int typeId = trechInfo.getTypeid();
            int num = trechInfo.getNum();
            if (soldierProxy.getSoldierNum(typeId) < num) {
                return -1;
            }
        }
        arena.setFightElementInfos(formationInfo.getMembersList());
        //设置将领
        arena.setGeneral(general);
        return 0;
    }


    //获得玩家的排名//没有该玩家返回-1
    public int getPlayerRank(Long playerId, Map<Long, Integer> ranks) {
        if (ranks.get(playerId) == null) {
            return -1;
        }
        return ranks.get(playerId);
    }

    //获得某个区间排名的玩家
    public List<Long> getSomePlayer(int begin, int end, Map<Long, Integer> ranks) {
        List<Long> list = new ArrayList<Long>();
        for (Map.Entry<Long, Integer> entry : ranks.entrySet()) {
            if (entry.getValue() >= begin && entry.getValue() <= end) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    //设置玩家的保护时间
    public void setProTime(PlayerTroop arena) {
        arena.setProtime(GameUtils.getServerDate().getTime() + 5000);
    }

    //改变玩家的排名
    public List<ArenaRank> changeArenaRank(Long rivalId, Long playerId, Map<Long, Integer> ranks) {
        List<ArenaRank> list = new ArrayList<ArenaRank>();
        int rivalrank = getPlayerRank(rivalId, ranks);
        int myrank = getPlayerRank(playerId, ranks);
        if (myrank > rivalrank) {
            List<Long> playerList = getSomePlayer(rivalrank, myrank, ranks);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            for (Long pId : playerList) {
                if (pId.toString().equals(playerId + "")) {
                    if (rivalrank == ActorDefine.RANK_FRIST_ONE) {
                        playerProxy.sendSystemchatNoChange(ActorDefine.CONTEST_FIELD_FIRST_CHANGE_NOTICE_TYPE, ActorDefine.CONDITION_TWO, ActorDefine.CONDITION_TWO);//发送系统公告10
                    }
                    ArenaRank arenaRank = new ArenaRank(pId, rivalrank, playerProxy.getAreaKey());
                    list.add(arenaRank);
                } else {
                    ArenaRank arenaRank = new ArenaRank(pId, ranks.get(pId) + 1, playerProxy.getAreaKey());
                    list.add(arenaRank);
                }
            }
        }
        return list;
    }

    public List<M20.FightInfo> getFightInfos(List<SimplePlayer> simplePlayers, Map<Long, Integer> ranks) {
        List<M20.FightInfo> fightInfos = new ArrayList<M20.FightInfo>();
        DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        for (SimplePlayer simplePlayer : simplePlayers) {
            M20.FightInfo.Builder builder = M20.FightInfo.newBuilder();
            builder.setLevel(simplePlayer.getLevel());
            builder.setName(simplePlayer.getName());
            builder.setRank(ranks.get(simplePlayer.getId()));
            int capity = 0;
            if (simplePlayer.getId() > 0) {
                capity = dungeoProxy.countSoldierCapacity(simplePlayer.getArenaTroop().getPlayerTeams());
            } else {
                JSONObject    monsterGroup=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ArenaRobot,"ID",-simplePlayer.getId());
                capity = monsterGroup.getInt("force");
            }
            builder.setCapity(capity);
            builder.setPlayerId(simplePlayer.getId());
            fightInfos.add(builder.build());
        }
        return fightInfos;
    }

    //获得可战斗剩余时间
    public int lesstime(SimplePlayer simplePlayer) {
        long time = simplePlayer.getArenaTroop().getProtime() - GameUtils.getServerDate().getTime();
        if (time < 0) {
            time = 0;
        }
        return (int) time / 1000;
    }


    public int askFight(SimplePlayer rival, SimplePlayer simplePlayer) {

        if (rival == null && simplePlayer == null) {
            return ErrorCodeDefine.M200001_1;
        }
        if (rival.getId() > 0) {
            if (rival.getArenaTroop().getProtime() >= GameUtils.getServerDate().getTime()) {
                return ErrorCodeDefine.M200001_2;//该玩家处于保护期
            }
            if (rival.getArenaTroop().getPlayerTeams().size() == 0) {
                return ErrorCodeDefine.M200001_3;
            }
            if (rival.getArenaTroop().getPlayerTeams().size() == 0 || simplePlayer.getArenaTroop().getPlayerTeams().size() == 0) {
                return ErrorCodeDefine.M200001_6;
            }

            if (rival.getArenaTroop().getPlayerTeams().get(0).getValue(SoldierDefine.NOR_POWER_TYPE_AURAS) == null) {
                return ErrorCodeDefine.M200001_7;//数据有异常
            }
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int challangeTimes = timerdbProxy.getTimerNum(TimerDefine.ARENA_TIMES, 0, 0);
        if (simplePlayer.getArenaTroop().getPlayerTeams().get(0).getValue(SoldierDefine.NOR_POWER_TYPE_AURAS) == null) {
            return -5;//数据有异常
        }
        if (simplePlayer.getArenaTroop().getProtime() >= GameUtils.getServerDate().getTime()) {
            return ErrorCodeDefine.M200001_2;//该玩家处于保护期
        }
        if (TimerDefine.ARENA_FIGHT_TIMES - challangeTimes <= 0) {
            return ErrorCodeDefine.M200001_4;//没有挑战次数
        }
        if (simplePlayer.getArenaTroop().getPlayerTeams().size() == 0) {
            return ErrorCodeDefine.M200001_3;
        }
        long lastime = timerdbProxy.getLastOperatinTime(TimerDefine.ARENA_FIGHT, 0, 0);
        if (lastime > GameUtils.getServerDate().getTime()) {
            return ErrorCodeDefine.M200001_5;//时间未到
        }
        DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        //缓存playterTem
        rivaltems.clear();
        if (rival.getId() > 0) {
            for(PlayerTeam team:rival.getArenaTroop().getPlayerTeams()){
                PlayerTeam newTeam=new PlayerTeam(team.basePowerMap, team.playerId);
                newTeam.powerMap= new HashMap<Integer, java.lang.Object >(newTeam.powerMap);
                newTeam.reset=newTeam.reset;
                newTeam.basePowerMap= new HashMap<Integer, java.lang.Object >(newTeam.basePowerMap);
                newTeam.capacityMap=newTeam.capacityMap;
                rivaltems.add(newTeam);
            }
        } else {
            rivaltems.addAll(dungeoProxy.getArenaMonster(rival.getId(),ArenaDefine.ROBOTGETTYPR2));
        }
        rivalName = rival.getName();
        rivalId = rival.getId();
//        for(PlayerTeam team : rivaltems){
//            team.reset=true;
//        }
        mytems = simplePlayer.getArenaTroop().getPlayerTeams();
        changeArenaId = rival.getId();
        sendFunctionLog(FunctionIdDefine.ASK_FIGHT_FUNCTION_ID, simplePlayer.getId(), rival.getId(), 0);
        return 0;
    }


    //增加竞技场挑战次数
    public int addArenaFightTimes() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int bunum = timerdbProxy.getTimerNum(TimerDefine.ARENA_ADD_TIMES, 0, 0);
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int mastimes = vipProxy.getVipNum(ActorDefine.VIP_ARENABUY);
        if (bunum > mastimes) {
            return ErrorCodeDefine.M200003_1;
        }
        int hasnum = timerdbProxy.getTimerNum(TimerDefine.ARENA_TIMES, 0, 0);
       /* if (ArenaDefine.FIGHTTIME - hasnum > 0) {
            return ErrorCodeDefine.M200003_3;
        }*/
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ARENA_PRICE, "times", bunum + 1);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < jsonObject.getInt("goldprice")) {
            return ErrorCodeDefine.M200003_2;
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("goldprice"), LogDefine.LOST_BUY_FIGHT_TIMES);
        timerdbProxy.addNum(TimerDefine.ARENA_ADD_TIMES, 0, 0, 1);
        timerdbProxy.reduceNum(TimerDefine.ARENA_TIMES, 0, 0, 1);
        sendFunctionLog(FunctionIdDefine.ADD_ARENA_FIGHT_TIMES_FUNCTION_ID, jsonObject.getInt("goldprice"), 0, 0);
        return 0;
    }

    //竞技场商店后买
    public int arenaShopBuy(int id, PlayerReward reward) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARENA_SHOP, id);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (jsonObject == null) {
            return ErrorCodeDefine.M200004_1;
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_arenaGrade) < jsonObject.getInt("scoreprice")) {
            return ErrorCodeDefine.M200004_2;
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_arenaGrade, jsonObject.getInt("scoreprice"), LogDefine.LOST_ARENA_SHOP_BUY);
        sendArenaShopReward(reward, jsonObject.getInt("type"), jsonObject.getInt("typeID"), jsonObject.getInt("num"));
        sendFunctionLog(FunctionIdDefine.ARENA_SHOP_BUY_FUNCTION_ID, id, jsonObject.getInt("scoreprice"), 0);
        return 0;
    }

    //发送竞技场商店奖励
    private void sendArenaShopReward(PlayerReward reward, int type, int typeId, int num) {
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (type == 401) {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            itemProxy.addItem(typeId, num, LogDefine.GET_ARENA_SHOP);
            rewardProxy.addItemToReward(reward, typeId, num);
        } else if (type == 402) {

        } else if (type == 403) {

        } else if (type == 404) {

        } else if (type == 405) {

        }

    }


    //重置竞技场的战力
    public void resetArenaCapity(SimplePlayer simplePlaye) {
        DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        simplePlaye.arenaTroop.setCapity(dungeoProxy.countSoldierCapacity(simplePlaye.arenaTroop.getPlayerTeams()));
        //发送到竞技场排名
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.AddPlayerToRank arenaRank = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), dungeoProxy.countSoldierCapacity(simplePlaye.arenaTroop.getPlayerTeams()), PowerRanksDefine.POWERRANK_TYPE_ARENA);
        sendRankServiceMsg(arenaRank);
    }


    //领取竞技场排名奖励
    public int getLaskArenaReward(int rank, PlayerReward reward) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int state = timerdbProxy.getTimerNum(TimerDefine.LASTARENAREWAED, 0, 0);
        if (state > 0) {
            return ErrorCodeDefine.M200005_1;
        }
        if (rank == -1) {
            return ErrorCodeDefine.M200005_2;
        }
        List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigAllInfo(DataDefine.ARENA_REWARD);
        JSONObject jsonObject1 = null;
        for (JSONObject jsonObject : jsonObjectList) {
            if (jsonObject.getInt("rankmin") <= rank && jsonObject.getInt("ranmax") >= rank) {
                jsonObject1 = jsonObject;
                break;
            }
        }
        if (jsonObject1 == null) {
            return -2;
        }
        StringBuffer sb = new StringBuffer();

        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        JSONArray array = jsonObject1.getJSONArray("fixreward");
        for (int i = 0; i < array.length(); i++) {
            int rewardId = array.getInt(i);
            rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
            sb.append(rewardId);
            sb.append(",");
        }
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_ARENA_LASTREWARD);
        timerdbProxy.addNum(TimerDefine.LASTARENAREWAED, 0, 0, 1);
        sendFunctionLog(FunctionIdDefine.GET_LAST_TIME_ARENA_RANKING_FUNCTION_ID, rank, 0, 0, sb.toString());
        return 0;
    }


    public int removefightTime() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lastime = timerdbProxy.getLastOperatinTime(TimerDefine.ARENA_FIGHT, 0, 0);
        long needTime = lastime - GameUtils.getServerDate().getTime();
        if (needTime < 0) {
            return ErrorCodeDefine.M200006_1;
        }
        ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        int cost = resFunBuildProxy.speedCost((int) needTime / 1000);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
            return ErrorCodeDefine.M200006_2;
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_SPEED_ARENA);
        timerdbProxy.setLastOperatinTime(TimerDefine.ARENA_FIGHT, 0, 0, GameUtils.getServerDate().getTime());
        sendFunctionLog(FunctionIdDefine.EXPEDITE_ARENA_FUNCTION_ID, cost, 0, 0);
        return 0;
    }

}
