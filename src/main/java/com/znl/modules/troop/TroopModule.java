package com.znl.modules.troop;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.PlayerTroop;
import com.znl.core.SimplePlayer;
import com.znl.core.PlayerTeam;
import com.znl.define.ActorDefine;
import com.znl.define.FunctionIdDefine;
import com.znl.define.ProtocolModuleDefine;
import com.znl.define.SoldierDefine;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.set.ArenaRankSetDb;
import com.znl.pojo.db.set.TeamDateSetDb;
import com.znl.proto.Common;
import com.znl.proto.M7;
import com.znl.proxy.*;
import com.znl.proxy.PlayerProxy;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/24.
 */
public class TroopModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<TroopModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public TroopModule create() throws Exception {
                return new TroopModule(gameProxy);
            }
        });
    }

    public TroopModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M7);
    }

    private void onRestArenaSuceess(GameMsg.RestArenaSuceess mess){
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ArenaProxy arenaProxy = this.getProxy(ActorDefine.ARENA_PROXY_NAME);
        Map<Long, Integer> ranks = mess.arenMap();
        PlayerTroop playerTroop = mess.team();
        SimplePlayer simplePlayer = mess.simplePlayer();
        M7.FormationInfo formationInfo = mess.feis();
        int genel = mess.genel();
        if (playerTroop == null) {
            playerTroop = arenaProxy.creatPlayerArena(formationInfo, genel);
            GameMsg.AddArenaRank msg = new GameMsg.AddArenaRank(playerProxy.getPlayerId(), ranks.size() + 1, playerProxy.getAreaKey());
            sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msg);
//            BaseSetDbPojo.getSetDbPojo(ArenaRankSetDb.class, playerProxy.getAreaKey()).addKeyValue(playerProxy.getPlayerId() + "", (long)(ranks.size() + 1));
        }else{
            arenaProxy.refreshPlayerArena(genel,playerTroop);
        }
//        GameMsg.addTeamDate msgt = new GameMsg.addTeamDate(playerTroop, playerProxy.getAreaKey() + "", simplePlayer.getId(), SoldierDefine.FORMATION_ARENA);
//        DbProxy.tell(msgt, ActorRef.noSender());
        BaseSetDbPojo.getSetDbPojo(TeamDateSetDb.class, playerProxy.getAreaKey()).addTeamDate(playerTroop, simplePlayer.getId(), SoldierDefine.FORMATION_ARENA);
        simplePlayer.setArenaTroop(playerTroop);
       // updateSimplePlayerData(simplePlayer);
       // GameMsg.UpdateSimplePlayerDefendTroop troopmess = new GameMsg.UpdateSimplePlayerDefendTroop(playerProxy.getPlayerId(),playerTroop);
       // sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME,mess);
        playerProxy.setSimplePlayer(simplePlayer);
        GameMsg.sendAreaInfo sendmsg=new  GameMsg.sendAreaInfo();
        sendModuleMsg(ActorDefine.ARENA_MODULE_NAME, sendmsg);
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.RestArenaSuceess) {
            onRestArenaSuceess((GameMsg.RestArenaSuceess) anyRef);
        }else if(anyRef instanceof GameMsg.SendFormationToClient){
            updateNewDefendFormation();
        }else if(anyRef instanceof GameMsg.CheckBaseDefendFormation){
            onCheckBaseDefendFormation();
        }else if(anyRef instanceof GameMsg.AutoPushToArena){
            autoSetArenaTroop();
        }
    }

    private void onCheckBaseDefendFormation(){
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        boolean refurce = formationProxy.checkBaseDefendTroop(soldierProxy);
        if (refurce){
            updateNewDefendFormation();
        }
    }

    private void updateNewDefendFormation() {
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<PlayerTeam> teams =formationProxy.createFormationTeam(SoldierDefine.FORMATION_DEFEND);
        //把队伍设置到怪物那边去
        for (PlayerTeam team : teams){
            int index = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
            if (index < 20){
                team.basePowerMap.put(SoldierDefine.NOR_POWER_INDEX,index+10);
                team.powerMap.put(SoldierDefine.NOR_POWER_INDEX,index+10);
            }
        }
        PlayerTroop troop = formationProxy.refurceDefendTeam(teams,playerProxy.getPlayerId());
        playerProxy.setSimplePlayerTroop(SoldierDefine.FORMATION_DEFEND,troop);
        pushNetMsg(ActorDefine.TROOP_MODULE_ID, ProtocolModuleDefine.NET_M7_C70000, getFormationListMess());
        sendPushNetMsgToClient(0);
        //2016/04/05 推送到playerService刷新
        GameMsg.UpdateSimplePlayerDefendTroop mess = new GameMsg.UpdateSimplePlayerDefendTroop(playerProxy.getPlayerId(),troop);
        sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME,mess);
    }

    private M7.M70000.S2C getFormationListMess(){
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        List<M7.FormationInfo> list = formationProxy.getFormationInfos();
        M7.M70000.S2C.Builder builder = M7.M70000.S2C.newBuilder();
        builder.setRs(0);
        builder.addAllInfo(list);
        return builder.build();
    }

    private void OnTriggerNet70000Event(Request request) {
        sendNetMsg(ActorDefine.TROOP_MODULE_ID, ProtocolModuleDefine.NET_M7_C70000, getFormationListMess());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M7_C70000);
    }

    private void OnTriggerNet70001Event(Request request) {
        M7.M70001.C2S proto = request.getValue();
        M7.FormationInfo formationInfo = proto.getInfo();
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        int rs = formationProxy.setFormation(formationInfo);
        M7.M70001.S2C.Builder builder = M7.M70001.S2C.newBuilder();
        builder.setRs(rs);
        if (rs >= 0) {
            builder.addInfo(formationInfo);
            sendFuntctionLog(FunctionIdDefine.SET_FORMATION_FUNCTION_ID);
        }
        sendNetMsg(ActorDefine.TROOP_MODULE_ID, ProtocolModuleDefine.NET_M7_C70001, builder.build());
        if (rs >= 0 && formationInfo.getType() == SoldierDefine.FORMATION_ARENA) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            GameMsg.RestArena msg = new GameMsg.RestArena(formationInfo, 1, playerProxy.getPlayerId(), playerProxy.getAreaKey());
            sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msg);
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M7_C70001);
        if (formationInfo.getType() == SoldierDefine.FORMATION_DEFEND && rs >= 0){
            List<PlayerTeam> defendTeams = formationProxy.createFormationTeam(formationInfo.getType());
            //把队伍设置到怪物那边去
            for (PlayerTeam team : defendTeams){
                int index = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
                if (index < 20){
                    team.basePowerMap.put(SoldierDefine.NOR_POWER_INDEX,index+10);
                    team.powerMap.put(SoldierDefine.NOR_POWER_INDEX,index+10);
                }
            }
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            PlayerTroop troop = formationProxy.createPlayerTroop(defendTeams, playerProxy.getPlayerId());
            GameMsg.UpdateSimplePlayerDefendTroop mess = new GameMsg.UpdateSimplePlayerDefendTroop(playerProxy.getPlayerId(),troop);
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);
//            updateMySimplePlayerData();
        }
    }


    private void autoSetArenaTroop(){
        M7.FormationInfo.Builder formationInfo = M7.FormationInfo.newBuilder();
        formationInfo.addMembers(M7.FormationMember.newBuilder().setPost(2).setTypeid(101).setNum(7));
        formationInfo.addMembers(M7.FormationMember.newBuilder().setPost(5).setTypeid(401).setNum(7));
        formationInfo.setType(SoldierDefine.FORMATION_ARENA);
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        int rs = formationProxy.setFormation(formationInfo.build());
        if (rs >= 0) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            GameMsg.RestArena msg = new GameMsg.RestArena(formationInfo.build(), 1, playerProxy.getPlayerId(), playerProxy.getAreaKey());
            sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msg);
        }
    }


    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {

    }

}
