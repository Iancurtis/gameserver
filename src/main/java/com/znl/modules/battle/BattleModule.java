package com.znl.modules.battle;

import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerBattle;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerTeam;
import com.znl.proto.*;
import com.znl.template.ReportTemplate;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.CustomerLogger;
import com.znl.log.admin.tbllog_battle;
import com.znl.msg.GameMsg;
import com.znl.node.battle.BattleNodeActor;
import com.znl.proxy.*;

import akka.actor.Props;
import com.znl.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;


public class BattleModule extends BasicModule {

    private String BATTLE_NODE_NAME = "LOGIC_BATTLE";
    private M5.Battle.Builder battleBuilder = M5.Battle.newBuilder();
    private boolean isStartBattle = false;

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<BattleModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public BattleModule create() throws Exception {
                return new BattleModule(gameProxy);
            }
        });
    }


    public BattleModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M5);
        context().watch(
                context().actorOf(Props.create(BattleNodeActor.class),
                        "LOGIC_BATTLE")); // 启动战斗模块
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.ReqPuppetList) {
            PlayerBattle battle = (PlayerBattle) ((GameMsg.ReqPuppetList) object).message();
            starBattle(battle);
        } else if (object instanceof GameMsg.ServerBattleEndHandle) {
            sendModuleMsg(ActorDefine.DUNGEO_MODULE_NAME, object);
        } else if (object instanceof GameMsg.EndBattle) {
            PlayerBattle battle = ((GameMsg.EndBattle) object).battle();
            ServerEndBattleHandle(battle);
        } else if (object instanceof GameMsg.PackPuppet) {
            M5.PuppetAttr puppetAttr = ((GameMsg.PackPuppet) object).puppet();
            packPuppet(puppetAttr);
        } else if (object instanceof GameMsg.ErrorBattle) {
            int rs = ((GameMsg.ErrorBattle) object).rs();
            PlayerBattle battle = new PlayerBattle();
            battle.rs = rs;
            ServerEndBattleHandle(battle);
        } else if (object instanceof GameMsg.AutoFightDungeo) {
            GameMsg.AutoFightDungeo message = (GameMsg.AutoFightDungeo) object;
            onReqPuppetList(message.dungeoType(), message.eventId(), ProtocolModuleDefine.NET_M6_C60005, message.fightElementInfos(),0);
        }
    }


    //打包战斗元素
    private void packPuppet(M5.PuppetAttr puppetAttr) {
        M5.Puppet.Builder builder = M5.Puppet.newBuilder();
        builder.setAttr(puppetAttr);
        battleBuilder.addPuppets(builder.build());
    }

    // 获取到战斗结束后的相关处理 奖励，等待时间等
    // 打包数据发送到 客户端
    private void ServerEndBattleHandle(PlayerBattle battle) {
        isStartBattle = false;
        int rs = battle.rs;
        if (battle.cmd != ProtocolModuleDefine.NET_M5_C50000) {
            return;
        }
        M5.M50000.S2C.Builder s2c = M5.M50000.S2C.newBuilder();
        M5.M50000.S2C.Builder packet = M5.M50000.S2C.newBuilder();
        s2c.setRc(rs);
        packet.setRc(rs);
        if (rs < 0) { // 战斗出错
            pushNetMsg(ActorDefine.BATTLE_MODULE_ID, ProtocolModuleDefine.NET_M5_C50000, s2c.build());
        } else {

            battleBuilder.addAllRounds(battle._roundDataList);
            battleBuilder.setId(battle.id);
            battleBuilder.setType(battle.type);
            if (battle.bgIcon > 0){
                battleBuilder.setBgIcon(battle.bgIcon);
            }
            packReward(battle);
            countPercent(battle);
            //pack的不需要省流量模式
            packet.setSaveTraffic(0);
            packet.setBattle(battleBuilder.build());
            packet.setWaitTime(battle.waitSeconds);

            //发给前端的就要判断是否需要省流量模式
            s2c.setSaveTraffic(battle.saveTraffic);
            if (battle.saveTraffic == 1){
                //省流量模式就不发具体内容了
                battleBuilder.clearPuppets().clearRounds();
            }
            s2c.setBattle(battleBuilder.build());
            s2c.setWaitTime(battle.waitSeconds);
            M5.M50000.S2C mess = s2c.build();
            pushNetMsg(ActorDefine.BATTLE_MODULE_ID, ProtocolModuleDefine.NET_M5_C50000, mess);
            packMessToBattle(battle,packet.build());
        }
        sendPushNetMsgToClient();
        /**
         * tbllog_battle日志
         */
        battleLog(battle.id,battle._roundDataList.size());
    }

    /***将战斗协议包打包在playerBattle里面***/
    private void packMessToBattle(PlayerBattle battle, M5.M50000.S2C mess) {
        M5.M50000.S2C.Builder builder = M5.M50000.S2C.newBuilder(mess);
        builder.setRc(2);
        battle.message = builder.build();
    }

    private void countPercent(PlayerBattle battle) {
        List<PlayerTeam> soldiers = battle.soldierList;
        int totalSoldierNum = 0;
        int soldierNum = 0;
        int totalAtk = 0;
        int crit = 0;
        int beAtk = 0;
        int dodge = 0;
        for (PlayerTeam team : soldiers) {
            soldierNum += (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            totalSoldierNum += (int) team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM);
            totalAtk += (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ATK_COUNT);
            crit += (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_CIRT_COUNT);
            beAtk += (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_BE_ATKED_COUNT);
            dodge += (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_DODGE_COUNT);
        }
        battleBuilder.setLoseSoldierPercent((int) (Math.ceil(totalSoldierNum - soldierNum) * 1.0 / totalSoldierNum * 100));
        battleBuilder.setCritPercent((int) (crit * 1.0 / totalAtk * 100));
        battleBuilder.setDodgePercent((int) (dodge * 1.0 / beAtk * 100));
        battleBuilder.setTotalSoldierNum(totalSoldierNum);
    }

    private void packReward(PlayerBattle battle) {
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        List<Common.RewardInfo> infos = new ArrayList<>();
        rewardProxy.getRewardInfoByReward(battle.reward, infos);
        M5.Reward.Builder builder = M5.Reward.newBuilder();
        builder.addAllRewardInfo(infos);
        builder.setStar(battle.star);
        battleBuilder.setReward(builder.build());
    }

    private void starBattle(PlayerBattle battle) {
        /*PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if( battle.monsterList==null){
            GameMsg.getArmyGroupDungeoInfo message = new GameMsg.getArmyGroupDungeoInfo(battle.infoType);
            tellMsgToArmygroupNode(message,playerProxy.getPlayer().getArmygroupId());
        }*/
        GameMsg.ReqPuppetList message = new GameMsg.ReqPuppetList(battle);
        sendMessageToBattleNode(message);
    }

    private void sendMessageToBattleNode(Object object) {
        context().actorSelection(BATTLE_NODE_NAME).tell(object, self());
    }

    private void OnTriggerNet50000Event(Request request) {
        Object value = request.getValue();
        M5.M50000.C2S c2s = (M5.M50000.C2S) value;
        List<Common.FightElementInfo> fightElementInfoList = c2s.getInfosList();
        int battleType = c2s.getType();
        int eventId = c2s.getId();
        int cmd = ProtocolModuleDefine.NET_M5_C50000;
        int saveTraffic = c2s.getSaveTraffic();
        onReqPuppetList(battleType, eventId, cmd, fightElementInfoList, saveTraffic);

    }

    private void onReqPuppetList(int battleType, int eventId, int cmd, List<Common.FightElementInfo> fightElementInfoList, int saveTraffic) {
        if (isStartBattle == true) {
            CustomerLogger.error("=======战斗正在计算中==========");
            return;
        }
        CustomerLogger.debug("=========客户端请求战斗============");
        BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
        List<PlayerTeam> teams = null;
        int rs = 0;
        if (battleType == BattleDefine.BATTLE_TYPE_ARENA) {
             FormationProxy formationProxy=getProxy(ActorDefine.FORMATION_PROXY_NAME);
             teams=formationProxy.createFormationTeam(SoldierDefine.FORMATION_ARENA);
        } else {
            rs = battleProxy.checkFightMember(fightElementInfoList);
            if (rs >= 0) {
                teams = battleProxy.createFightTeamList(fightElementInfoList);
                DungeoProxy dungeoProxy=getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                dungeoProxy.checkActivityPlayerTeams(teams,battleType,eventId);
            } else {
                //如果检测到出战单位不符合，刷新佣兵
                if(rs==ErrorCodeDefine.M50000_1){
                    SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                    sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007,
                            M2.M20007.S2C.newBuilder().addAllSoldierList(soldierProxy.getSoldierInfos()).build());
                    sendPushNetMsgToClient();
                }else if (cmd == ProtocolModuleDefine.NET_M5_C50000) {
                    M5.M50000.S2C.Builder builder = M5.M50000.S2C.newBuilder();
                    builder.setRc(rs);
                    sendNetMsg(ActorDefine.BATTLE_MODULE_ID, ProtocolModuleDefine.NET_M5_C50000, builder.build());
                    sendPushNetMsgToClient();
                    return;
                } else if (cmd == ProtocolModuleDefine.NET_M6_C60005) {
                    M6.M60005.S2C.Builder builder = M6.M60005.S2C.newBuilder();
                    builder.setRs(rs - ErrorCodeDefine.M60005_6);
                    pushNetMsg(ActorDefine.BATTLE_MODULE_ID, ProtocolModuleDefine.NET_M5_C50000, builder.build());
                    sendPushNetMsgToClient();
                    return;
                }
            }
        }
        List<Object> dataList = new ArrayList<>();
        dataList.add(battleType);
        dataList.add(eventId);
        dataList.add(teams);
        dataList.add(cmd);
        dataList.add(saveTraffic);
        if(battleType == BattleDefine.BATTLE_TYPE_ARMYGROUP_DEFEND){
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
           // GameMsg.addPuppetList message = new GameMsg.addPuppetList(playerProxy.getAccountName(),battleType,eventId,cmd,teams,saveTraffic);
            //tellMsgToArmygroupNode(message,playerProxy.getPlayer().getArmygroupId());
        }else {
            GameMsg.ReqPuppetList message = new GameMsg.ReqPuppetList(dataList);
            sendModuleMsg(ActorDefine.DUNGEO_MODULE_NAME, message);
        }
        isStartBattle = true;
        battleBuilder.clear();

    }

    private void OnTriggerNet50001Event(Request request) {
        CustomerLogger.debug("=========客户端请求战斗结束============");
        Object value = request.getValue();
        M5.M50001.C2S c2s = (M5.M50001.C2S) value;
        int battleId = c2s.getId();
        BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
        PlayerBattle battle = battleProxy.battles.get(battleId);
        if (battle == null) {
            M5.M50001.S2C.Builder builder = M5.M50001.S2C.newBuilder();
            builder.setRc(1);
            sendNetMsg(ActorDefine.BATTLE_MODULE_ID,ProtocolModuleDefine.NET_M5_C50001,builder.build());
            sendPushNetMsgToClient();
            return;
        }
        GameMsg.ClientEndHandle message = new GameMsg.ClientEndHandle(battleId);
        sendModuleMsg(ActorDefine.DUNGEO_MODULE_NAME, message);
    }

//	private void OnTriggerNet50002Event(Request request){
//		CustomerLogger.debug("=========客户端请求战斗队伍生成============");
//		Object value = request.getValue();
//		M5.M50002.C2S c2s = (M5.M50002.C2S) value;
//		List<Common.FightElementInfo> fightElementInfoList = c2s.getInfosList();
//		BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
//		int rs = battleProxy.checkFightMember(fightElementInfoList);
//		if(rs >= 0){
//			battleProxy.createFightTeamList(fightElementInfoList);
//		}
//		M5.M50002.S2C.Builder builder = M5.M50002.S2C.newBuilder();
//		builder.setRc(rs);
//		sendNetMsg(ActorDefine.BATTLE_MODULE_ID,ProtocolModuleDefine.NET_M5_C50002,builder.build());
//	}

    /**
     * tbllog_battle日志
     */
    public void battleLog(int battleId,int duration){
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_battle battlelog = new tbllog_battle();
        battlelog.setPlatform(cache.getPlat_name());
        battlelog.setRole_id(player.getPlayerId());
        battlelog.setAccount_name(player.getAccountName());
        battlelog.setDim_level(player.getLevel());
        battlelog.setBattle_id(battleId);
        battlelog.setTime_duration(duration);
        battlelog.setHappend_time(GameUtils.getServerTime());
        sendLog(battlelog);

    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }

}
