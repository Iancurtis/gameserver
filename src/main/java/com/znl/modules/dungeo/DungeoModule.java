package com.znl.modules.dungeo;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.*;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.admin.tbllog_box;
import com.znl.log.admin.tbllog_fb;
import com.znl.log.admin.tbllog_pvp;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.set.LimitDungeonFastSetDb;
import com.znl.pojo.db.set.TeamDateSetDb;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.template.ReportTemplate;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/11/10.
 */
public class DungeoModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<DungeoModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public DungeoModule create() throws Exception {
                return new DungeoModule(gameProxy);
            }
        });
    }


    public DungeoModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M6);
        DungeoProxy dungeoProxy=getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
        dungeoProxy.checkOpenNewDungeo(playerProxy.getHigestDungId());
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.ReqPuppetList) {
            List<Object> dataList = (List<Object>) ((GameMsg.ReqPuppetList) object).message();
            reqPuppetList(dataList);
        } else if (object instanceof GameMsg.ServerBattleEndHandle) {
            PlayerBattle battle = ((GameMsg.ServerBattleEndHandle) object).battle();
            serverEndBattleHandle(battle);
        } else if (object instanceof GameMsg.ClientEndHandle) {
            int battleId = ((GameMsg.ClientEndHandle) object).battleId();
            clientEndBattleHandle(battleId);
            GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
        } else if (object instanceof GameMsg.GetAllArenaRankSuceess) {
            Map<Long, Integer> ranks = ((GameMsg.GetAllArenaRankSuceess) object).arenMap();
            String cmd = ((GameMsg.GetAllArenaRankSuceess) object).cmd();
            changeRank(cmd, ranks);
        } else if (object instanceof GameMsg.GetWinTimesReward) {
            List<SimplePlayer> simplePlayers = ((GameMsg.GetWinTimesReward) object).simplePlayer();
            PlayerBattle battle = ((GameMsg.GetWinTimesReward) object).battle();
            String cmd = ((GameMsg.GetWinTimesReward) object).cmd();
            doGetPlayerSimpleInfo(simplePlayers, cmd, battle);
        } else if (object instanceof GameMsg.changeSnucess) {
            GameMsg.sendAreaInfo msginfo = new GameMsg.sendAreaInfo();
            sendModuleMsg(ActorDefine.ARENA_MODULE_NAME, msginfo);
        } else if (object instanceof GameMsg.getLimitChangetInfoBack) {
            DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
            M6.M60100.S2C.Builder builder = ((GameMsg.getLimitChangetInfoBack) object).builder();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            builder.setBackCount(1 - timerdbProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_REST, 0, 0));
            builder.setFightCount(3 - timerdbProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_TIMES, 0, 0));
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ADVENTURE_EVENT, "chapter", 4, "sort", playerProxy.getPlayer().getGetLimitChangeId());
            if (jsonObject == null) {
                builder.setId(1);
            } else {
                builder.setId(jsonObject.getInt("ID"));
            }
            JSONObject monsterGroup = dungeoProxy.getMonsterGroup(jsonObject.getInt("monstergroup"));
            M6.LimitEventInfo.Builder eventbuild = M6.LimitEventInfo.newBuilder();
            //获取6个槽位的怪物
            for (int index = 1; index <= 6; index++) {
                JSONArray pos = monsterGroup.getJSONArray("position" + index);
                int size = pos.length();
                if (size > 1) {
                    eventbuild.addMonsterInfos(dungeoProxy.getLimitMonsterInfo(pos.getInt(0), pos.getInt(1), index));
                }
            }
            eventbuild.setForce(monsterGroup.getInt("force"));
            builder.setEventInfo(eventbuild);
            JSONObject jsonObjectmax = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ADVENTURE_EVENT, "chapter", 4, "sort", playerProxy.getPlayer().getLimitChangeMaxId() - 1);
            if (jsonObjectmax != null) {
                builder.setMaxId(jsonObjectmax.getInt("ID"));
            } else {
                builder.setMaxId(0);
            }
            builder.setRs(0);
            long time = (timerdbProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0) - GameUtils.getServerDate().getTime()) / 1000;
            if (time > 0) {
                builder.setIsmop(1);
            } else {
                builder.setIsmop(0);
            }
            pushNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60100, builder.build());
            sendPushNetMsgToClient();
        } else if (object instanceof GameMsg.AddLimitchangeBattleProtoBack) {
            Long id = ((GameMsg.AddLimitchangeBattleProtoBack) object).id();
            int order = ((GameMsg.AddLimitchangeBattleProtoBack) object).dungeoOrder();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            BaseSetDbPojo.getSetDbPojo(LimitDungeonFastSetDb.class, playerProxy.getAreaKey()).addLimitDungeonSet(order, id, playerProxy.getPlayerId(), GameUtils.getServerDate().getTime());
            GameMsg.AddLimitchangeNearList adnmsg = new GameMsg.AddLimitchangeNearList(id, order, playerProxy.getPlayerId());
            sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, adnmsg);
        }
    }

    private void doGetPlayerSimpleInfo(List<SimplePlayer> simplePlayers, String cmd, PlayerBattle battle) {
        if (cmd.equals(ArenaDefine.CMD_CHANGE_WINTIMES)) {
            ArenaProxy arenaProxy = getProxy(ActorDefine.ARENA_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            for (SimplePlayer simplePlayer : simplePlayers) {
                if (simplePlayer.getId() == playerProxy.getPlayerId()) {
                    if (battle.battleResult == true) {
                        //迁移走了
                       /* int num = simplePlayer.getArenaTroop().getWintimes();
                        simplePlayer.getArenaTroop().setWintimes(num + 1);
                        playerProxy.sendSystemchat(ActorDefine.WIN_STEAK_NOTICE_TYPE, simplePlayer.getArenaTroop().getWintimes(), ActorDefine.CONDITION_TWO);//发送系统公告3
                         sendArenWinReward(simplePlayer.getArenaTroop().getWintimes(), battle);*/
                    } else {
                        simplePlayer.getArenaTroop().setWintimes(0);
                    }
                    // DO 添加竞技场积分
                } else {
                    if (simplePlayer.getId() > 0 && arenaProxy.result == true) {
                        simplePlayer.getArenaTroop().setWintimes(0);
                    }
                }
                BaseSetDbPojo.getSetDbPojo(TeamDateSetDb.class, playerProxy.getAreaKey()).addTeamDate(simplePlayer.getArenaTroop(), simplePlayer.getId(), SoldierDefine.FORMATION_ARENA);
            }

        }

    }

    public void sendArenWinReward(int wintimes, PlayerBattle battle) {
        // PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<JSONObject> jsonObjectlist = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ARENA_GRADE, "resulttype", 1);
        JSONObject jsonObject = null;
        for (JSONObject jsonObject1 : jsonObjectlist) {
            if (jsonObject1.getInt("timemin") <= wintimes + 1 && jsonObject1.getInt("timemax") >= wintimes + 1) {
                jsonObject = jsonObject1;
            }
        }
        if (jsonObject == null) {
            return;
        }
        //playerProxy.addPowerValue(PlayerPowerDefine.POWER_arenaGrade, jsonObject.getInt("score"), LogDefine.GET_AREAN_FIGHT);
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        rewardProxy.getPlayerRewardByRandContent(jsonObject.getInt("rateward"), reward, new ArrayList<>());
        //  rewardProxy.getRewardToPlayer(reward, LogDefine.GET_AREAN_FIGHT);
        battle.reward = reward;
        battle.reward.addPowerMap.put(PlayerPowerDefine.POWER_arenaGrade, jsonObject.getInt("score"));
        // M2.M20007.S2C msg20007 = rewardProxy.getRewardClientInfo(reward);
        // pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg20007);
        ///   List<Integer> list = new ArrayList<>();
        //  list.add(PlayerPowerDefine.POWER_arenaGrade);
        //   M2.M20002.S2C different = sendDifferent(list);
        //   pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, different);
        sendPushNetMsgToClient();
    }


    private void reduceEnergy(int logtype) {
        boolean sendTimer = false;
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long preEnergy = playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy);
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_energy, 1, logtype);
        long afterEnergy = playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy);
        if (preEnergy >= ActorDefine.MAX_ENERGY && afterEnergy < ActorDefine.MAX_ENERGY) {
            sendTimer = true;
            int type = TimerDefine.DEFAULT_ENERGY_RECOVER;
            int times = TimerDefine.DEFAULT_TIME_RECOVER;
            timerdbProxy.addTimer(type, 0, times, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
            if (timerdbProxy.getTimerlesTime(type, 0, 0) == 0) {
                timerdbProxy.setLesTime(type, 0, 0, (int) Math.ceil(times / 1000.0));
            }
        }
        if (sendTimer) {
            SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
            List<M3.TimeInfo> m3info = new ArrayList<>();
            PlayerReward reward = new PlayerReward();
            GameMsg.SystemTimer message = new GameMsg.SystemTimer();
            sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, message);
        }
    }

    private void clientEndBattleHandle(int battleId) {
        int tastType = 0;
        //清空战斗出战队列
        BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
        PlayerBattle battle = battleProxy.battles.get(battleId);
        if (battle == null) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        int dungeoId = 0;
        if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
            JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, battle.infoType);
            dungeoId = eventDefine.getInt("chapter");
        } else if (battle.type == BattleDefine.BATTLE_TYPE_ADVANTRUE) {
            JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, battle.infoType);
            dungeoId = eventDefine.getInt("chapter");

            if (dungeoId == BattleDefine.ADVANTRUE_TYPE_EQUIP) {
                tastType = TaskDefine.TASK_TYPE_EQUIPTANXIAN_TIMES;
            } else if (dungeoId == BattleDefine.ADVANTRUE_TYPE_ORNDANCE) {
                tastType = TaskDefine.TASK_TYPE_ORNDANCETANXIAN_TIMES;
            } else if (dungeoId == BattleDefine.ADVANTRUE_TYPE_LIMIT) {
                tastType = TaskDefine.TASK_TYPE_JIXIANTANXIAN_TIMES;
            }
            if (tastType != 0) {
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                PlayerReward reward = new PlayerReward();
                M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(tastType, 1, reward);
                if (builder19 != null) {
                    sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                }
            }
        }
        boolean openNewDungeo = false;
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int level = playerProxy.getLevel();
        int rs = 0;
        HashMap<Integer, Integer> deadNums = new HashMap<>();//没打之前的佣兵数量缓存
        M6.M60001.S2C dungeoInfo = null;
        if (battle == null) {
            rs = ErrorCodeDefine.M50001_1;
        } else {
            switch (battle.type) {
                case BattleDefine.BATTLE_TYPE_DUNGEON: {
                    JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, battle.infoType);
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    if (battle.battleResult == true) {
                        reduceEnergy(LogDefine.LOST_CHALLENGE_DUNGON);
                        //将奖励发送到玩家背包,实现星星等计算逻辑
                        openNewDungeo = dungeoProxy.eventFightEndHandle(battle);
                        //发送任务完成
                        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                        PlayerReward reward = new PlayerReward();
                        List<PlayerTask> playerTasks = new ArrayList<PlayerTask>();
                        playerTasks.add(new PlayerTask(TaskDefine.TASK_TYPE_WINGATE_ID, dungeoId, 0));
                        if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
                            playerTasks.add(new PlayerTask(TaskDefine.TASK_TYPE_BEATGATE_TIMES, 0, 1));
                        }
                        M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(playerTasks, reward);
                        if (builder19 != null) {
                            sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                        }
                        if (openNewDungeo) {
                            JSONObject dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
                            JSONObject nextDungeo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", dungeoDefine.getInt("sort") + 1);
                            if(nextDungeo!=null){
                                pushNetMsg(ActorDefine.DUNGEO_MODULE_ID, ProtocolModuleDefine.NET_M6_C60006, sendDungeoListById(nextDungeo.getInt("ID")));
                            }
                        }
                    }
                    SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                    ids = dungeoProxy.reduceDeadSoldier(battle.soldierList, battle.type, deadNums, soldierProxy);
                    dungeoInfo = sendDungeoInfo(dungeoId);
//                    pushNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60001, sendDungeoInfo(dungeoId));
                    pushNetMsg(ActorDefine.DUNGEO_MODULE_ID, ProtocolModuleDefine.NET_M6_C60006, sendDungeoListById(dungeoId));
                    ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
                    playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_ZHENGFU_GUANQIA_TIMES, 1, playerProxy, 0);
                    GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
                    sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
                    break;
                }
                case BattleDefine.BATTLE_TYPE_ADVANTRUE: {
                    DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, battle.infoType);
                    if (dungeoProxy.isPass(battle, eventDefine)) {
                        if (eventDefine.getInt("chapter") != 4) {
                            timerdbProxy.reduceAdvanceTiems(eventDefine.getInt("chapter"));
                        } else {
                            //TODO 极限副本
                            playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                            JSONObject eventDefinenext = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ADVENTURE_EVENT, "chapter", 4, "sort", eventDefine.getInt("sort") + 1);
                            if (eventDefine.getInt("sort") >= playerProxy.getPlayer().getLimitChangeMaxId()) {
                                if (eventDefinenext != null) {
                                    playerProxy.getPlayer().setLimitChangeMaxId(eventDefine.getInt("sort") + 1);
                                    List<JSONObject> chatinfos = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.SYSTEM_NOTICE, "type", ActorDefine.CHAT_TYPE_12);
                                    for (JSONObject json : chatinfos) {
                                        if (json.getInt("condition1") == eventDefine.getInt("ID")) {
                                            playerProxy.sendSystemchat(ActorDefine.LIMIT_EXPLORE_NOTICE_TYPE, eventDefine.getInt("ID"), ActorDefine.CONDITION_TWO);//发送系统公告12
                                            break;
                                        }
                                    }
                                }
                                GameMsg.AddLimitchangeBattleProto battlemsg = new GameMsg.AddLimitchangeBattleProto(battle.message, eventDefine.getInt("sort"));
                                sendServiceMsg(ActorDefine.BATTLE_REPORT_SERVICE_NAME, battlemsg);
                                GameMsg.AddPlayerToRank msg = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), eventDefine.getInt("sort"), PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE);
                                sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
                                updateMySimplePlayerData();
                            }
                            if (eventDefinenext != null) {
                                playerProxy.getPlayer().setGetLimitChangeId(eventDefine.getInt("sort") + 1);
                                timerdbProxy.setNum(TimerDefine.LIMIT_CHANGET_TIMES, 0, 0, 0);
                            }

                        }

                        //将奖励发送到玩家背包,实现星星等计算逻辑
                        if (dungeoProxy.isPass(battle, eventDefine)) {
                            openNewDungeo = dungeoProxy.eventFightEndHandle(battle);
//                            JSONArray array=eventDefine.getJSONArray("fixdrop");
//                            for(int i=0;i<array.length();i++) {
//                                rewardProxy.getPlayerRewardByFixReward(array.getInt(i), battle.reward);
//                            }
                        }
                        //发送任务完成
                        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                        PlayerReward reward = new PlayerReward();
                        List<PlayerTask> playerTasks = new ArrayList<PlayerTask>();
                        playerTasks.add(new PlayerTask(TaskDefine.TASK_TYPE_WINGATE_ID, dungeoId, 0));
                        M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(playerTasks, reward);
                        if (builder19 != null) {
                            sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                        }

                    } else {
                        if (eventDefine.getInt("chapter") == 4) {
                            timerdbProxy.addNum(TimerDefine.LIMIT_CHANGET_TIMES, 0, 0, 1);
                        }
                    }

                    SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                    if (eventDefine.getInt("chapter") != 4) {
                        ids = dungeoProxy.reduceDeadSoldier(battle.soldierList, battle.type, deadNums, soldierProxy);

                    } else {
                        OnTriggerNet60100Event(null);
                    }
                    dungeoInfo = sendDungeoInfo(dungeoId);
//                    pushNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60001, sendDungeoInfo(dungeoId));
                    pushNetMsg(ActorDefine.DUNGEO_MODULE_ID, ProtocolModuleDefine.NET_M6_C60006, sendDungeoListById(dungeoId));
                    //活动
                    ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
                    playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_HIT_GUANQIA_TIMES, 1, playerProxy, 0);
                    GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
                    sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
                    break;
                }
                case BattleDefine.BATTLE_TYPE_ARENA: {
                    TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                    // TODO  增加挑战次数 刷新挑战时间  增加连胜次数       更新对手的连胜次数    改变排名
                    //增加挑战次数 刷新挑战时间
                    timerdbProxy.addNum(TimerDefine.ARENA_TIMES, 0, 0, 1);
                    // 改变排名 增加连胜次数       更新对手的连胜次数
                    ArenaProxy arenaProxy = getProxy(ActorDefine.ARENA_PROXY_NAME);
                    battle.defendId = arenaProxy.changeArenaId;
                    arenaProxy.result = battle.battleResult;
                    GameMsg.GetSimplePlayerBysection msgwin = new GameMsg.GetSimplePlayerBysection(arenaProxy.changeArenaId, playerProxy.getPlayerId(), ArenaDefine.CMD_CHANGE_WINTIMES, battle);
                    sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msgwin);
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    if (battle.battleResult == true) {
                        int num = playerProxy.getSimplePlayer().getArenaTroop().getWintimes();
                        playerProxy.getSimplePlayer().getArenaTroop().setWintimes(num + 1);
                        playerProxy.sendSystemchat(ActorDefine.WIN_STEAK_NOTICE_TYPE, playerProxy.getSimplePlayer().getArenaTroop().getWintimes(), ActorDefine.CONDITION_TWO);//发送系统公告3
                        GameMsg.GetAllArenaFromMoudle msg = new GameMsg.GetAllArenaFromMoudle(ArenaDefine.CMD_CHANGE_RANK);
                        sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msg);
                        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
                        activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_BEAR_ARNENA_WINTITIMES_EVERYDAY, 1, playerProxy, 0);
                        sendReport(battle, 0, rewardProxy.reward2String(battle.reward));
                        GameMsg.RefrshTip rmsg = new GameMsg.RefrshTip();
                        sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, rmsg);
                    } else {
                        timerdbProxy.setLastOperatinTime(TimerDefine.ARENA_FIGHT, 0, 0, GameUtils.getServerDate().getTime() + ArenaDefine.ARENA_TIME_WAIT);
                        GameMsg.SystemTimer message = new GameMsg.SystemTimer();
                        sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, message);
                        GameMsg.sendAreaInfo msginfo = new GameMsg.sendAreaInfo();
                        sendModuleMsg(ActorDefine.ARENA_MODULE_NAME, msginfo);
                        sendReport(battle, 1, rewardProxy.reward2String(battle.reward));
                    }
                    M2.M20007.S2C msg20007 = rewardProxy.getRewardClientInfo(battle.reward);
                    pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg20007);
                    List<Integer> list = new ArrayList<>();
                    list.add(PlayerPowerDefine.POWER_arenaGrade);
                    M2.M20002.S2C different = sendDifferent(list);
                    pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, different);
                    rewardProxy.getRewardToPlayer(battle.reward, LogDefine.GET_AREAN_FIGHT);
                    TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                    PlayerReward reward = new PlayerReward();
                    M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_ARENAFIGHT_TIMES, 1, reward);
                    if (builder19 != null) {
                        sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                    }
                    int state = 1;
                    if (battle.battleResult == false) {
                        state = 2;
                    }
                    //平台日志
                    tbllog_pvp pvplog = new tbllog_pvp(playerProxy.getPlayerCache().getPlat_name(), LogDefine.ADMIN_BATTLE_ID_ARENA,
                            playerProxy.getPlayerId(), playerProxy.getAccountName(), playerProxy.getLevel(), state, GameUtils.getServerTime(), GameUtils.getServerTime());
                    sendLog(pvplog);
                    break;
                }
                default: {
                    break;
                }

            }
        }
        battleProxy.battleEndHandle(battleId);
        if (ids.size() >= 0 && battle.cmd != ProtocolModuleDefine.NET_M6_C60005) {
            SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            List<Common.SoldierInfo> infos = new ArrayList<>();
            for (Integer soldierId : ids) {
                infos.add(soldierProxy.getSoldierInfo(soldierId));
//                battle.reward.soldierMap.put(soldierId, 0);
            }
            pushNetMsg(ProtocolModuleDefine.NET_M4, ProtocolModuleDefine.NET_M4_C40000, M4.M40000.S2C.newBuilder().addAllSoldiers(infos).build());
        }
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        List<Integer> power = new ArrayList<>();

        //检查各个power值，发送different
        power.add(PlayerPowerDefine.POWER_energy);
        power.add(PlayerPowerDefine.NOR_POWER_highestCapacity);
        power.add(PlayerPowerDefine.POWER_active);
        power.addAll(battle.reward.addPowerMap.keySet());
        M2.M20007.S2C message = rewardProxy.getRewardClientInfo(battle.reward);
        pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message);
        int newlevel = playerProxy.getLevel();
        if (newlevel != level) {
            power.add(PlayerPowerDefine.POWER_level);
            power.add(PlayerPowerDefine.POWER_command);
            GameMsg.changeMenberLevel groupmsg = new GameMsg.changeMenberLevel(playerProxy.getPlayerId(), playerProxy.getLevel());
            tellMsgToArmygroupNode(groupmsg, playerProxy.getArmGrouId());
        }
        sendPowerDiff(power);
        sendPushNetMsgToClient();
        if (dungeoInfo != null) {
            pushNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60001, dungeoInfo);
        }
        if (battle.cmd == ProtocolModuleDefine.NET_M6_C60005) {//执行挂机逻辑
            power.add(PlayerPowerDefine.POWER_tael);
            autoDungeoHandle(battle, deadNums, ids);
            sendFuntctionLog(FunctionIdDefine.ON_HOOK_FUNCTION_ID, battle.infoType, 0, 0);
        } else {
            M5.M50001.S2C.Builder builder = M5.M50001.S2C.newBuilder();
            builder.setRc(rs);
            pushNetMsg(ActorDefine.BATTLE_MODULE_ID, ProtocolModuleDefine.NET_M5_C50001, builder.build());
            sendModuleMsg(ActorDefine.SOLDIER_MODULE_NAME, new GameMsg.FixSoldierList());
        }

        /**
         * tbllog_fb日志 结束
         */
        fbLog(dungeoId, battle.infoType, deadNums.size());

        //判断防守阵型是否需要刷新
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        boolean refurce = formationProxy.checkDefendTroop(soldierProxy, ActorDefine.SETTING_AUTO_ADD_DEFEND_TEAM_ON, null, null);
        if (refurce) {
            //刷新防守队伍的playerTeam
            sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.SendFormationToClient());
        }
        sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.CheckBaseDefendFormation());
        sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
        //新副本开启通知
        if (openNewDungeo) {
            M6.M60104.S2C.Builder builder = M6.M60104.S2C.newBuilder();
            builder.setRs(0);
            pushNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60104, builder.build());
        }

        sendPushNetMsgToClient();
    }

    private void autoDungeoHandle(PlayerBattle battle, HashMap<Integer, Integer> deadNums, List<Integer> ids) {
        Map<Integer, Integer> restoremap = new HashMap<>();
        //修复伤兵,发送挂机协议
        M6.M60005.S2C.Builder builder = M6.M60005.S2C.newBuilder();
        builder.setRs(0);
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        soldierProxy.getfixLostNum(restoremap);
        List<Common.SoldierInfo> infos = new ArrayList<>();
        int price = soldierProxy.fixLostSoldier(0, 2, infos);
        builder.setContinue(0);
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        int rs = dungeoProxy.fightEventAsk(battle.infoType, battle.type, false);
        if (rs < 0) {
            if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
                if (rs == ErrorCodeDefine.M60002_5) {
                    builder.setContinue(1);
                }
            } else if (battle.type == BattleDefine.BATTLE_TYPE_ADVANTRUE) {
                if (rs == ErrorCodeDefine.M60002_6) {
                    builder.setContinue(4);
                }
            }
        }
        if (price < 0) {
            builder.setContinue(2);
            sendModuleMsg(ActorDefine.SOLDIER_MODULE_NAME, new GameMsg.FixSoldierList());
            pushNetMsg(ProtocolModuleDefine.NET_M4, ProtocolModuleDefine.NET_M4_C40000, M4.M40000.S2C.newBuilder().addAllSoldiers(soldierProxy.getSoldierInfosInlost()).build());
        } else {
            builder.setCostTael(price);
        }
        int capacity = dungeoProxy.countSoldierCapacity(battle.soldierList);
        if (capacity * 1.0 / battle.totalCapacity * 100 < 90) {
            builder.setContinue(3);
        }
        List<Common.RewardInfo> rewardInfoList = new ArrayList<>();
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        rewardProxy.getRewardInfoByReward(battle.reward, rewardInfoList);
        builder.addAllRewards(rewardInfoList);
        builder.addAllSoldierInfo(infos);
        for (Integer soldierId : ids) {
            int restnum = 0;
            if (price > 0) {
                if (restoremap.get(soldierId) != null) {
                    restnum = restoremap.get(soldierId);
                }
            }
            M6.CostSoldierInfo.Builder cosInfo = M6.CostSoldierInfo.newBuilder();
            int deadNum = deadNums.get(soldierId);
            cosInfo.setNum(deadNum - restnum);
            cosInfo.setTypeid(soldierId);
            if (deadNum - restnum > 0) {
                builder.addCostInfos(cosInfo);
            }
        }
        if (battle.battleResult) {
            builder.setResult(1);
        } else {
            builder.setResult(0);
        }
        pushNetMsg(ActorDefine.DUNGEO_MODULE_ID, ProtocolModuleDefine.NET_M6_C60005, builder.build());
        GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
        sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
    }


    private void serverEndBattleHandle(PlayerBattle battle) {
        int battleType = battle.type;
        battle.rs = 0;
        switch (battleType) {
            case BattleDefine.BATTLE_TYPE_DUNGEON: {
                ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
                boolean result = battle.battleResult;
                JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, battle.infoType);
                if (eventDefine == null) {
                    sendErrorBattle(-1);
                    return;
                }
                if (result == true) {
                    //生成未到背包的奖励
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    JSONArray array = null;

                    DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    JSONArray fixarray = null;
                    if (dungeoProxy.isfirstThrough(battle)) {
                        array = eventDefine.getJSONArray("firstratedrop");
                        fixarray = eventDefine.getJSONArray("firstfixdrop");
                    } else {
                        array = eventDefine.getJSONArray("ratedrop");
                        fixarray = eventDefine.getJSONArray("fixdrop");
                    }
                    for (int i = 0; i < fixarray.length(); i++) {
                        rewardProxy.getPlayerRewardByFixReward(fixarray.getInt(i), battle.reward);
                    }
                    int rateRewardId = 0;
                    for (int i = 0; i < array.length(); i++) {
                        rewardProxy.getPlayerRewardByRandFullContent(array.getInt(i), battle.reward);
                        rateRewardId = array.getInt(i);
                    }
                    int exp = eventDefine.getInt("exp");
                    PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    long addpower = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_exprate);
                    exp = (int) Math.ceil(exp * (1 + (addpower / 100.0)));
                    exp = (int) Math.ceil(exp * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_FIGHT_GUANQIA_ADDEXP)) / 100.0);
                    if (exp > 0) {
                        if (battle.reward.addPowerMap.containsKey(PlayerPowerDefine.POWER_exp)) {
                            exp += battle.reward.addPowerMap.get(PlayerPowerDefine.POWER_exp);
                        }
                        battle.reward.addPowerMap.put(PlayerPowerDefine.POWER_exp, exp);
                    }
                    //写入行为日志，副本胜利
                    sendFuntctionLog(FunctionIdDefine.FIGHT_EVENT_ASK_FUNCTION_ID, battle.infoType, rateRewardId, 1);
                } else {
                    //写入行为日志，副本失败
                    sendFuntctionLog(FunctionIdDefine.FIGHT_EVENT_ASK_FUNCTION_ID, battle.infoType, 0, 0);
                }
                break;
            }
            case BattleDefine.BATTLE_TYPE_ADVANTRUE: {
                JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, battle.infoType);
                if (eventDefine == null) {
                    sendErrorBattle(-1);
                    return;
                }
                if (battle.battleResult == true) {
                    //生成未到背包的奖励
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    JSONArray array = eventDefine.getJSONArray("ratedrop");
                    int rateRewardId = 0;
                    for (int i = 0; i < array.length(); i++) {
                        rewardProxy.getPlayerRewardByRandFullContent(array.getInt(i), battle.reward);
                        rateRewardId = array.getInt(i);
                    }
                    PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    int exp = eventDefine.getInt("exp");
                    long addpower = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_exprate);
                    exp = (int) Math.ceil(exp * (1 + (addpower / 100.0)));
                    if (exp > 0) {
                        if (battle.reward.addPowerMap.containsKey(PlayerPowerDefine.POWER_exp)) {
                            exp += battle.reward.addPowerMap.get(PlayerPowerDefine.POWER_exp);
                        }
                        battle.reward.addPowerMap.put(PlayerPowerDefine.POWER_exp, exp);
                    }
                    DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, battle.infoType);
                    if (dungeoProxy.isPass(battle, eventDefine)) {
                        array = eventDefine.getJSONArray("fixdrop");
                        for (int i = 0; i < array.length(); i++) {
                            //先生成一个未如背包的固定奖励
                            rewardProxy.getPlayerRewardByFixReward(array.getInt(i), battle.reward);
                        }
                    }
                    //写入行为日志，副本胜利
                    sendFuntctionLog(FunctionIdDefine.FIGHT_EVENT_ASK_FUNCTION_ID, battle.infoType, rateRewardId, 1);
                } else {
                    //写入行为日志，副本失败
                    sendFuntctionLog(FunctionIdDefine.FIGHT_EVENT_ASK_FUNCTION_ID, battle.infoType, 0, 0);
                }
                break;
            }
            case BattleDefine.BATTLE_TYPE_ARENA: {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                // 打包奖励
                ArenaProxy arenaProxy = getProxy(ActorDefine.ARENA_PROXY_NAME);
                battle.defendId = arenaProxy.changeArenaId;
                arenaProxy.result = battle.battleResult;
                PlayerReward reward = new PlayerReward();
                if (battle.battleResult == true) {

                    sendArenWinReward(playerProxy.getSimplePlayer().getArenaTroop().getWintimes(), battle);
                } else {
                    JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ARENA_GRADE, "resulttype", 2);
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    rewardProxy.getPlayerRewardByRandContent(jsonObject.getInt("rateward"), reward, new ArrayList<>());
                    battle.reward = reward;
                    battle.reward.addPowerMap.put(PlayerPowerDefine.POWER_arenaGrade, jsonObject.getInt("score"));
                }
                break;
            }
            default:
                battle.rs = -1;
                break;
        }
        if (battle.rs >= 0) {
            //缓存玩家的这次战斗包，等客户端战斗结束的时候可以提取到
            BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
            battleProxy.addBattle(battle);
            if (battle.battleResult == true) {
                DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                battle.star = dungeoProxy.countFightStar(battle.soldierList, battle.totalCapacity);
            }
            if (battle.battleResult == false) {
                battle.rs = 1;
            }
        }
        GameMsg.EndBattle message = new GameMsg.EndBattle(battle);
        sendModuleMsg(ActorDefine.BATTLE_MODULE_NAME, message);
        if (battle.cmd == ProtocolModuleDefine.NET_M6_C60005) {
            //是挂机战斗的话返回结果
            clientEndBattleHandle(battle.id);
        }
    }

    private void sendErrorBattle(int rs) {
        PlayerBattle battle = new PlayerBattle();
        battle.rs = rs;
        GameMsg.EndBattle message = new GameMsg.EndBattle(battle);
        sendModuleMsg(ActorDefine.BATTLE_MODULE_NAME, message);
    }


    private void reqPuppetList(List<Object> dataList) {
        int battleType = (int) dataList.get(0);
        int id = (int) dataList.get(1);
        List<PlayerTeam> fightList = (List<PlayerTeam>) dataList.get(2);
        int cmd = (int) dataList.get(3);
        int saveTraffic = (int) dataList.get(4);
        List<PlayerTeam> monsterList = null;
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerBattle battle = new PlayerBattle();
        switch (battleType) {
            case BattleDefine.BATTLE_TYPE_DUNGEON: {
                DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                if (dungeoProxy.fightEventAsk(id, battleType, cmd != ProtocolModuleDefine.NET_M6_C60005) < 0) {
                    sendErrorBattle(-1);
                    return;
                }
                JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, id);
                battle.bgIcon = eventDefine.getInt("fightbg");
                monsterList = dungeoProxy.creatEventMonsterList(id);
                break;
            }
            case BattleDefine.BATTLE_TYPE_ADVANTRUE: {
                DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                if (dungeoProxy.fightEventAsk(id, battleType, cmd != ProtocolModuleDefine.NET_M6_C60005) < 0) {
                    sendErrorBattle(-1);
                    return;
                }
                JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, id);
                battle.bgIcon = eventDefine.getInt("fightbg");
                monsterList = dungeoProxy.createAdvantrueMonsterList(id);
                break;
            }
            case BattleDefine.BATTLE_TYPE_ARENA: {
                ArenaProxy arenaProxy = getProxy(ActorDefine.ARENA_PROXY_NAME);
                for (PlayerTeam playerTeam : arenaProxy.rivaltems) {
                    if (playerTeam.basePowerMap.get(SoldierDefine.NOR_POWER_INDEX) != null && (int) playerTeam.basePowerMap.get(SoldierDefine.NOR_POWER_INDEX) <= 20) {
                        playerTeam.basePowerMap.put(SoldierDefine.NOR_POWER_INDEX, (int) playerTeam.basePowerMap.get(SoldierDefine.NOR_POWER_INDEX) + 10);
                    }
                    if (playerTeam.powerMap.get(SoldierDefine.NOR_POWER_INDEX) != null && (int) playerTeam.powerMap.get(SoldierDefine.NOR_POWER_INDEX) <= 20) {
                        playerTeam.powerMap.put(SoldierDefine.NOR_POWER_INDEX, (int) playerTeam.powerMap.get(SoldierDefine.NOR_POWER_INDEX) + 10);
                    }
                }
                monsterList = arenaProxy.rivaltems;
                battle.attackId = playerProxy.getPlayerId();
                battle.attackName = playerProxy.getPlayerName();
                battle.defendId = arenaProxy.rivalId;
                battle.defendName = arenaProxy.rivalName;
                break;
            }
            default:
                sendErrorBattle(-1);
                return;
        }

        BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
        battleProxy.getBattleBirthBuff(fightList, monsterList);
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        battle.totalCapacity = dungeoProxy.countSoldierCapacity(fightList);
        battle.monsterList = monsterList;
        battle.soldierList = fightList;
        battle.type = battleType;
        battle.infoType = id;
        battle.cmd = cmd;
        battle.saveTraffic = saveTraffic;
        GameMsg.ReqPuppetList message = new GameMsg.ReqPuppetList(battle);
        sendModuleMsg(ActorDefine.BATTLE_MODULE_NAME, message);
    }

    private void showBattListInfo(List<PlayerTeam> soldierList) {
        for (PlayerTeam team : soldierList) {
            System.out.println();
            System.out.println("Index :" + team.getValue(SoldierDefine.NOR_POWER_INDEX));
            for (int i = SoldierDefine.POWER_hpMax; i <= SoldierDefine.TOTAL_FIGHT_POWER; i++) {
                System.out.println("power   " + i + " : " + team.getValue(i));
            }
            List<Integer> buffs = (List<Integer>) team.getValue(SoldierDefine.NOR_POWER_BUFF);
            System.out.print("buffs :{ ");
            for (Integer buffId : buffs) {
                System.out.print(buffId + ", ");
            }
            System.out.println(" }");
        }
    }

    private M6.M60006.S2C sendDungeoListById(int dungeoId) {
        M6.M60006.S2C.Builder builder = M6.M60006.S2C.newBuilder();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        builder.setRs(0);
        List<M6.DungeoInfo> res = new ArrayList<>();
        M6.DungeoInfo info = null;
        if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
            info = dungeoProxy.getDungeoInfo(dungeoId);
            builder.setDungeoInfos(info);
            //副本
            builder.setType(1);
        } else {
            builder.setDungeoExplore(getRistDungeoInfoByid(dungeoId));
            //冒险
            builder.setType(2);
        }
        int maxOrder = dungeoProxy.getHighestDungeoOrder();
        JSONObject nextDungeo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", maxOrder + 1);
        if (nextDungeo == null) {
            builder.setIsPassAll(1);
        } else {
            builder.setIsPassAll(0);
        }
        return builder.build();
    }





    private M6.M60000.S2C sendDungeoList() {
        M6.M60000.S2C.Builder builder = M6.M60000.S2C.newBuilder();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        builder.setRs(0);
        List<M6.DungeoInfo> res = new ArrayList<>();
        for (Integer dungeoId : dungeoProxy.getAllDungeoId()) {
            M6.DungeoInfo info = dungeoProxy.getDungeoInfo(dungeoId);
            res.add(info);
        }
        SortUtil.anyProperSort(res, "getId", true);
        builder.addAllDungeoInfos(res);
        builder.addAllDungeoExplore(dungeoProxy.getRistDungeoInfo());
        int maxOrder = dungeoProxy.getHighestDungeoOrder();
        JSONObject nextDungeo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", maxOrder + 1);
        if (nextDungeo == null) {
            builder.setIsPassAll(1);
        } else {
            builder.setIsPassAll(0);
        }
//        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60000, builder.build());
        return builder.build();
    }


    /***该协议已经屏蔽*****/
    private void OnTriggerNet60000Event(Request request) {
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60000, sendDungeoList());
    }


    private M6.M60001.S2C sendDungeoInfo(int dungeoId) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        List<M6.EventInfo> infos = new ArrayList<>();
        int rs = getDungeoInfo(infos, dungeoId);
        M6.M60001.S2C.Builder builder = M6.M60001.S2C.newBuilder();
        builder.setRs(rs);
        if (rs >= 0) {
            builder.addAllEventInfo(infos);
            List<Integer> boxList = dungeoProxy.getDungeoBoxByDungeoId(dungeoId);
            builder.addAllBoxes(boxList);
            builder.setDungeoId(dungeoId);
            builder.setStar(dungeoProxy.getTotalStarNum(dungeoId));
            JSONObject dungeoDefine;
            if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
                dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                builder.setTimes((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy));
                builder.setTimesTotal(20);
            } else {
                dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeoId);
                TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                builder.setTimes(timerdbProxy.getAdventureTimesById(dungeoId));
                builder.setTimesTotal(dungeoDefine.getInt("time"));
            }
            int totalStar = dungeoDefine.getInt("starNum");
            builder.setTotalStar(totalStar);
        }
//        sendNetMsg(ProtocolModuleDefine.NET_M6,ProtocolModuleDefine.NET_M6_C60001,builder.build());
        return builder.build();
    }


    private M6.DungeoInfo getRistDungeoInfoByid(int dungeoId) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        M6.DungeoInfo.Builder builder = M6.DungeoInfo.newBuilder();
        builder.setId(dungeoId);
        JSONObject define = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeoId);
        if (dungeoId == DungeonDefine.EXTREME_ADVENTRUE) {
            builder.setLen(dungeoProxy.getDungeoEventPassNum(dungeoId));
            builder.setCount(-1);
            builder.setTotalCount(-1);
            builder.setHaveBox(0);
            builder.setStar(-1);
            builder.setTotalStar(-1);
        } else {
            //在计数器里面补充次数time
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            builder.setCount(timerdbProxy.getAdventureTimesById(dungeoId));
            //builder.setCount(5);
            builder.setTotalCount(define.getInt("time"));
            builder.setLen(-1);
            List<Integer> boxList = dungeoProxy.getRistDungeoBoxByDungeoId(dungeoId);
            if (boxList.size() > 0) {
                builder.setHaveBox(1);
            } else {
                builder.setHaveBox(0);
            }
            builder.setStar(dungeoProxy.getTotalStarNum(dungeoId));
            builder.setTotalStar(define.getInt("starNum"));
        }
        return builder.build();
    }

   /* private List<M6.DungeoInfo> getRistDungeoInfo() {
        List<M6.DungeoInfo> res = new ArrayList<>();
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        for (JSONObject define : list) {
            int dungeoId = define.getInt("ID");
            M6.DungeoInfo.Builder builder = M6.DungeoInfo.newBuilder();
            builder.setId(dungeoId);
            if (dungeoId == DungeonDefine.EXTREME_ADVENTRUE) {
                builder.setLen(dungeoProxy.getDungeoEventPassNum(dungeoId));
                builder.setCount(-1);
                builder.setTotalCount(-1);
                builder.setHaveBox(0);
                builder.setStar(-1);
                builder.setTotalStar(-1);
            } else {
                //在计数器里面补充次数time
                TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                builder.setCount(timerdbProxy.getAdventureTimesById(dungeoId));
                //builder.setCount(5);
                builder.setTotalCount(define.getInt("time"));
                builder.setLen(-1);
                List<Integer> boxList = dungeoProxy.getRistDungeoBoxByDungeoId(dungeoId);
                if (boxList.size() > 0) {
                    builder.setHaveBox(1);
                } else {
                    builder.setHaveBox(0);
                }
                builder.setStar(dungeoProxy.getTotalStarNum(dungeoId));
                builder.setTotalStar(define.getInt("starNum"));
            }
            res.add(builder.build());
        }
        return res;
    }*/

  /*  private M6.DungeoInfo getDungeoInfo(int dungeoId) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        M6.DungeoInfo.Builder dungeoInfo = M6.DungeoInfo.newBuilder();
        dungeoInfo.setId(dungeoId);
        dungeoInfo.setStar(dungeoProxy.getTotalStarNum(dungeoId));
        JSONObject dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
        int totalStar = dungeoDefine.getInt("starNum");
        dungeoInfo.setTotalStar(totalStar);
        List<Integer> boxList = dungeoProxy.getDungeoBoxByDungeoId(dungeoId);
        if (boxList.size() > 0) {
            dungeoInfo.setHaveBox(1);
        } else {
            dungeoInfo.setHaveBox(0);
        }
        dungeoInfo.setLen(-1);
        dungeoInfo.setCount(-1);
        dungeoInfo.setTotalCount(-1);
        return dungeoInfo.build();
    }*/

    private void OnTriggerNet60001Event(Request request) {
        M6.M60001.C2S c2s = request.getValue();
        int id = c2s.getId();
        M6.M60001.S2C mess = sendDungeoInfo(id);
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60001, mess);
    }

    private void OnTriggerNet60002Event(Request request) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        M6.M60002.C2S c2s = request.getValue();
        int eventId = c2s.getEvendId();
        int battleType = c2s.getBattleType();
        int rs = dungeoProxy.fightEventAsk(eventId, battleType, true);
        M6.M60002.S2C.Builder builder = M6.M60002.S2C.newBuilder();
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60002, builder.build());
        if (rs == 0) {
            /**
             * tbllog_fb日志 请求
             */
            fbLog(eventId, eventId, 0);
        }
    }

    private void OnTriggerNet60003Event(Request request) {
        M6.M60003.C2S c2s = request.getValue();
        int dungeoId = c2s.getDungeoId();
        int box = c2s.getBoxNum();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = dungeoProxy.openDungeoBox(dungeoId, box, reward);
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        M6.M60003.S2C.Builder builder = M6.M60003.S2C.newBuilder();
        List<Integer> boxList = dungeoProxy.getDungeoBoxByDungeoId(dungeoId);
        builder.setRs(rs);
        if (rs >= 0) {
            builder.addAllBoxes(boxList);
            /**
             * tbllog_box 开启宝箱日志
             */
            boxLog(boxList.toString());

        }
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60003, builder.build());
        if (rs >= 0) {
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
            sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60006, sendDungeoListById(dungeoId));
        }
    }

    private int getDungeoInfo(List<M6.EventInfo> infos, Integer dungeoId) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        List<Integer> dungeoList = null;
        if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
            dungeoList = dungeoProxy.getAllDungeoId();
        } else {
            dungeoList = dungeoProxy.getAllRistDungeoId();
        }
        if (dungeoList.contains(dungeoId) == false) {
            return ErrorCodeDefine.M60001_1;
        }
        infos.addAll(dungeoProxy.getEventList(dungeoId));
        SortUtil.anyProperSort(infos, "getId", true);
        return 0;
    }

    public void changeRank(String cmd, Map<Long, Integer> ranks) {
        if (ArenaDefine.CMD_CHANGE_RANK.equals(cmd)) {
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            ArenaProxy arenaProxy = this.getProxy(ActorDefine.ARENA_PROXY_NAME);
            if (arenaProxy.changeArenaId != 0) {
                List<ArenaRank> arenaRanks = arenaProxy.changeArenaRank(arenaProxy.changeArenaId, playerProxy.getPlayerId(), ranks);
                arenaProxy.changeArenaId = 0l;
                if (arenaRanks.size() != 0) {
                    GameMsg.ChangeArenaRank msg = new GameMsg.ChangeArenaRank(arenaRanks);
                    sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msg);
                } else {
                    GameMsg.sendAreaInfo msginfo = new GameMsg.sendAreaInfo();
                    sendModuleMsg(ActorDefine.ARENA_MODULE_NAME, msginfo);
                }
            }
        }
    }

    /**
     * vip购买冒险次数
     **/
    private void OnTriggerNet60004Event(Request request) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        TimerdbProxy tdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        M6.M60004.C2S c2s = request.getValue();
        int dungeonId = c2s.getDungeoId();
        int type = c2s.getType();
        int letTimes = tdbProxy.getAdventureTimesById(dungeonId);
        M6.M60004.S2C.Builder s2c = M6.M60004.S2C.newBuilder();
        int rs = 0;
        if (type == DungeonDefine.BUY_ADVANCE_TIMES_TYPE_ASK) { //请求购买
            rs = dungeoProxy.askBuyTimes(dungeonId);
            s2c.setAdvanceTimes(letTimes);
        } else if (type == DungeonDefine.BUY_ADVANCE_TIMES_TYPE_BUY) { //购买
            rs = dungeoProxy.buyAdvanceTimes(dungeonId);
            if (rs == 0) {
                s2c.setAdvanceTimes(letTimes + DungeonDefine.ADVANCE_TIMES);//剩余的冒险数+购买的次数

            } else {
                s2c.setAdvanceTimes(letTimes);
            }
        }
        int needGold = dungeoProxy.needGold(dungeonId);
        s2c.setRs(rs);
        s2c.setMoney(needGold);
        s2c.setType(type);
        s2c.setAdvanceTimes(tdbProxy.getAdventureTimesById(dungeonId));
        s2c.setDungeoId(dungeonId);
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60004, s2c.build());
        if (rs >= 0) {
            sendNetMsg(ActorDefine.DUNGEO_MODULE_ID, ProtocolModuleDefine.NET_M6_C60006, sendDungeoListById(dungeonId));
        }
    }

    private void OnTriggerNet60005Event(Request request) {
        M6.M60005.C2S c2S = request.getValue();
        int dungeoType = c2S.getType();
        int eventId = c2S.getId();
        List<Common.FightElementInfo> fightElementInfos = c2S.getInfosList();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        int rs = dungeoProxy.fightEventAsk(eventId, dungeoType, false);
        //判断vip是否足够
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        JSONObject vipDefine = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel));
        if (vipDefine.getInt("isonhook") == 0) {
            rs = ErrorCodeDefine.M60005_17;
        }
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        if (soldierProxy.getSolierLostTypeNum() > 0) {
            List<Common.SoldierInfo> infos = new ArrayList<>();
            int price = soldierProxy.fixLostSoldier(0, 2, infos);
            if (price < 0) {
                rs = ErrorCodeDefine.M60005_20;
            } else {
                M4.M40000.S2C.Builder soldierInfo = M4.M40000.S2C.newBuilder();
                soldierInfo.addAllSoldiers(infos);
                sendNetMsg(ProtocolModuleDefine.NET_M4, ProtocolModuleDefine.NET_M4_C40000, soldierInfo.build());
                if (infos.size() > 0) {
                    sendModuleMsg(ActorDefine.SOLDIER_MODULE_NAME, new GameMsg.FixSoldierList());
                }
            }
        }
        if (rs < 0) {
            M6.M60005.S2C.Builder builder = M6.M60005.S2C.newBuilder();
            builder.setRs(rs);
            builder.setContinue(1);
            sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60005, builder.build());
            return;
        }
        GameMsg.AutoFightDungeo mess = new GameMsg.AutoFightDungeo(dungeoType, eventId, fightElementInfos);
        sendModuleMsg(ActorDefine.BATTLE_MODULE_NAME, mess);
    }


    private void OnTriggerNet60100Event(Request request) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        dungeoProxy.checkmopp();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ADVENTURE, "type", 4);
        M6.M60100.S2C.Builder builder = M6.M60100.S2C.newBuilder();
        if (playerProxy.getLevel() < jsonObject.getInt("level")) {
            builder.setRs(ErrorCodeDefine.M60100_1);
            sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60100, builder.build());
        } else {
            builder.setIsmop(dungeoProxy.checkmopp());
            GameMsg.getLimitChangetInfo msg = new GameMsg.getLimitChangetInfo(builder, playerProxy.getPlayerId(), playerProxy.getPlayer().getGetLimitChangeId());
            sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
        }
    }


    private void OnTriggerNet60101Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M6.M60101.S2C.Builder builder = M6.M60101.S2C.newBuilder();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        int rs = dungeoProxy.limitRest();
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60101, builder.build());
        if (rs == 0) {
            M6.M60100.S2C.Builder builder1 = M6.M60100.S2C.newBuilder();
            GameMsg.getLimitChangetInfo msg = new GameMsg.getLimitChangetInfo(builder1, playerProxy.getPlayerId(), playerProxy.getPlayer().getGetLimitChangeId());
            sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
            sendFuntctionLog(FunctionIdDefine.RESET_LIMIT_DARE_FUNCTION_ID);
        }
    }

    private void OnTriggerNet60102Event(Request request) {
        M6.M60102.S2C.Builder builder = M6.M60102.S2C.newBuilder();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        int rs = dungeoProxy.startMop();
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60102, builder.build());
        if (rs == 0) {
            OnTriggerNet60100Event(null);
            GameMsg.CheckAllTimerAndSend30000 msg = new GameMsg.CheckAllTimerAndSend30000();
            sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, msg);
            sendFuntctionLog(FunctionIdDefine.START_MOP_FUNCTION_ID);
        }
    }

    private void OnTriggerNet60103Event(Request request) {
        M6.M60103.S2C.Builder builder = M6.M60103.S2C.newBuilder();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = dungeoProxy.stopMop(reward);
        builder.setRs(rs);
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        List<Common.RewardInfo> list = new ArrayList<Common.RewardInfo>();
        rewardProxy.getRewardInfoByReward(reward, list);
        builder.addAllRewards(list);
        sendNetMsg(ProtocolModuleDefine.NET_M6, ProtocolModuleDefine.NET_M6_C60103, builder.build());
        if (rs == 0) {
            OnTriggerNet60100Event(null);
            M2.M20007.S2C rewardbuild = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardbuild);
            sendFuntctionLog(FunctionIdDefine.STOP_MOP_FUNCTION_ID);
        }
    }

    /**
     * tbllog_fb日志 结束
     */
    public void fbLog(int dungeoId, int fblevel, int deathcnt) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_fb fblog = new tbllog_fb();
        fblog.setPlatform(cache.getPlat_name());
        fblog.setRole_id(player.getPlayerId());
        fblog.setFb_id(dungeoId);
        fblog.setAccount_name(player.getAccountName());
        fblog.setDim_level(player.getLevel());
        fblog.setFb_level(fblevel);
        fblog.setStatus(2);
        fblog.setDeath_cnt(deathcnt);
        fblog.setHappend_time(GameUtils.getServerTime());
        sendLog(fblog);
    }

    /**
     * tbllog_box 开启宝箱日志
     */
    public void boxLog(String data) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_box boxlog = new tbllog_box();
        boxlog.setPlatform(cache.getPlat_name());
        boxlog.setRole_id(player.getPlayerId());
        boxlog.setAccount_name(player.getAccountName());
        boxlog.setSource_data(data);
        boxlog.setHappend_time(GameUtils.getServerTime());
        sendLog(boxlog);
    }


    public void sendReport(PlayerBattle battle, int result, String reward) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ReportTemplate reportTemplate = new ReportTemplate(
                battle.attackId, battle.attackName, battle.defendId, battle.defendName,
                ReportDefine.REPORT_TYPE_ARENA, battle.message);
        List<Integer> defendSoldierTypeIds = new ArrayList<Integer>();
        List<Integer> defendSoldierNums = new ArrayList<Integer>();
        List<Integer> attackSoldierTypeIds = new ArrayList<Integer>();
        List<Integer> attackSoldierNums = new ArrayList<Integer>();
        getTeamList(defendSoldierTypeIds, defendSoldierNums, attackSoldierTypeIds, attackSoldierNums, battle);
        reportTemplate.setDefendSoldierTypeIds(defendSoldierTypeIds);
        reportTemplate.setDefendSoldierNums(defendSoldierNums);
        reportTemplate.setAttackSoldierTypeIds(attackSoldierTypeIds);
        reportTemplate.setAttackSoldierNums(attackSoldierNums);
        reportTemplate.setAttackId(playerProxy.getPlayerId());
        reportTemplate.setResult(result);
        reportTemplate.setReward(reward);
        reportTemplate.setMessage(battle.message);
        int myfirst = 0;
        int otherfirst = 0;
        for (PlayerTeam team : battle.soldierList) {
            myfirst += (int) team.basePowerMap.get(SoldierDefine.POWER_initiative);
        }
        for (PlayerTeam team : battle.monsterList) {
            otherfirst += (int) team.basePowerMap.get(SoldierDefine.POWER_initiative);
        }
        if (myfirst >= otherfirst) {
            reportTemplate.setFirstHand(0);
        } else {
            reportTemplate.setFirstHand(1);
        }
        reportTemplate.setDefendId(battle.defendId);
        reportTemplate.setAttackVip((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel));
        reportTemplate.setAttackLegion(playerProxy.getArmGrouId() + "");
        GameMsg.AddMailBattleProto msg = new GameMsg.AddMailBattleProto(reportTemplate, "200001");
        sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
        ArenaProxy arenaProxy = getProxy(ActorDefine.ARENA_PROXY_NAME);
        for (PlayerTeam team : arenaProxy.rivaltems) {
            team.init();
        }
        for (PlayerTeam team : arenaProxy.mytems) {
            team.init();
        }
    }

    private void getTeamList(List<Integer> defendSoldierTypeIds, List<Integer> defendSoldierNums, List<Integer> attackSoldierTypeIds, List<Integer> attackSoldierNums, PlayerBattle battle) {
        for (PlayerTeam team : battle.monsterList) {
            int after = (int) team.powerMap.get(SoldierDefine.NOR_POWER_NUM);
            int before = (int) team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM);
            if (after != before) {
                defendSoldierNums.add(before - after);
                defendSoldierTypeIds.add((Integer) team.basePowerMap.get(SoldierDefine.NOR_POWER_TYPE_ID));
            }
        }
        for (PlayerTeam team : battle.soldierList) {
            int after = (int) team.powerMap.get(SoldierDefine.NOR_POWER_NUM);
            int before = (int) team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM);
            if (after != before) {
                attackSoldierNums.add(before - after);
                attackSoldierTypeIds.add((Integer) team.basePowerMap.get(SoldierDefine.NOR_POWER_TYPE_ID));
            }
        }
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }

}
