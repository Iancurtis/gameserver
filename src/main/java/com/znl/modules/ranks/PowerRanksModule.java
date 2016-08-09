package com.znl.modules.ranks;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.ArenaRank;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Player;
import com.znl.proto.M21;
import com.znl.proxy.DungeoProxy;
import com.znl.proxy.GameProxy;
import com.znl.proxy.PlayerProxy;
import com.znl.utils.GameUtils;

import java.util.*;

/**
 * Created by Administrator on 2015/12/30.
 */
public class PowerRanksModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<PowerRanksModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public PowerRanksModule create() throws Exception {
                return new PowerRanksModule(gameProxy);
            }
        });
    }

    public PowerRanksModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M21);
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.GetAnRankMessageByType) {
           /* M21.M210000.S2C.Builder rankInfo = M21.M210000.S2C.newBuilder();
            M21.M210000.S2C builder = ((GameMsg.GetAnRankMessageByType) anyRef).build();
            M21.PowerRankInfo.Builder myRankInfo = M21.PowerRankInfo.newBuilder();
            int typeId = ((GameMsg.GetAnRankMessageByType) anyRef).rankType();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            boolean onRank = false;
            if (builder.getPowerRankInfoList().size() > 0) {
                //我的排名
                for (M21.PowerRankInfo myinfo : builder.getPowerRankInfoList()) {
                    if (myinfo.getPlayerId() == playerProxy.getPlayerId()) {
                        myRankInfo.setLevel(myinfo.getLevel());
                        myRankInfo.setName(playerProxy.getAccountName());
                        myRankInfo.setTypeId(myinfo.getTypeId());
                        myRankInfo.setRankValue(myinfo.getRankValue());
                        myRankInfo.setPlayerId(playerProxy.getPlayerId());
                        myRankInfo.setRank(myinfo.getRank());
                        onRank = true;
                    }
                }

                rankInfo.addAllPowerRankInfo(builder.getPowerRankInfoList());
            }
            //我的排名
            if (onRank) { //我在榜上
                rankInfo.setMyRank(myRankInfo);
            } else {//我未上榜
//                    int typeId = builder.getPowerRankInfoList().get(0).getTypeId();
                if (typeId == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) {
                    myRankInfo.setLevel((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_atklv));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) {
                    myRankInfo.setLevel((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_critlv));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) {
                    myRankInfo.setLevel((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_dogelv));
                } else {
                    myRankInfo.setLevel(playerProxy.getLevel());
                }
                myRankInfo.setName(playerProxy.getAccountName());
                myRankInfo.setTypeId(typeId);
                if (typeId == PowerRanksDefine.POWERRANK_TYPE_CAPACITY) {
                    myRankInfo.setRankValue(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_highestCapacity));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_CUSTOMS) {
                    DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    myRankInfo.setRankValue(dungeoProxy.rankStarNum());
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) {
                    myRankInfo.setRankValue(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_atkRate));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) {
                    myRankInfo.setRankValue(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_critRate));
                } else if (typeId == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) {
                    myRankInfo.setRankValue(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_dodgeRate));
                }else if(typeId == PowerRanksDefine.POWERRANK_TYPE_HONOR){
                    myRankInfo.setRankValue(playerProxy.getPowerValue(PlayerPowerDefine.POWER_honour));
                }else {
                    myRankInfo.setRankValue(0);
                }
                myRankInfo.setPlayerId(playerProxy.getPlayerId());
                myRankInfo.setRank(0);
                rankInfo.setMyRank(myRankInfo);
            }

            rankInfo.setTypeId(typeId);
            pushNetMsg(ProtocolModuleDefine.NET_M21, ProtocolModuleDefine.NET_M21_C210000, rankInfo.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.GET_POWER_RANK_INFO_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.GetArenaRankMap) {   //竞技场
            M21.M210000.S2C.Builder ranksInfo = M21.M210000.S2C.newBuilder();
            M21.PowerRankInfo.Builder myRankInfo = M21.PowerRankInfo.newBuilder();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M21.M210000.S2C ranks = ((GameMsg.GetArenaRankMap) anyRef).build();
            long tempId = 0;
            if (ranks.getPowerRankInfoList().size() > 0) {
                for (M21.PowerRankInfo info : ranks.getPowerRankInfoList()) {
                    if (info.getPlayerId() == playerProxy.getPlayerId()) {
                        myRankInfo.setPlayerId(info.getPlayerId());
                        myRankInfo.setRankValue(info.getRankValue());
                        myRankInfo.setTypeId(PowerRanksDefine.POWERRANK_TYPE_ARENA);
                        myRankInfo.setLevel(playerProxy.getLevel());
                        myRankInfo.setName(playerProxy.getPlayerName());
                        myRankInfo.setRank(info.getRank());

                        tempId = info.getPlayerId();
                    }
                }
                ranksInfo.addAllPowerRankInfo(ranks.getPowerRankInfoList());
            }
            if (tempId != 0) {
                ranksInfo.setMyRank(myRankInfo);
            } else {
                myRankInfo.setRank(0);
                myRankInfo.setRankValue(0);
                myRankInfo.setLevel(playerProxy.getLevel());
                myRankInfo.setName(playerProxy.getPlayerName());
                myRankInfo.setPlayerId(playerProxy.getPlayerId());
                myRankInfo.setTypeId(PowerRanksDefine.POWERRANK_TYPE_ARENA);
                ranksInfo.setMyRank(myRankInfo);
            }
            ranksInfo.setTypeId(PowerRanksDefine.POWERRANK_TYPE_ARENA);
            pushNetMsg(ProtocolModuleDefine.NET_M21, ProtocolModuleDefine.NET_M21_C210000, ranksInfo.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.GET_POWER_RANK_INFO_FUNCTION_ID);*/
        }
    }

    private void OnTriggerNet210000Event(Request request) {
      /*  M21.M210000.C2S c2s = request.getValue();
        int type = c2s.getTypeId();
        if (type == PowerRanksDefine.POWERRANK_TYPE_ARENA) {  //竞技场
            GameMsg.GetArenaRankInfos msg = new GameMsg.GetArenaRankInfos();
            sendServiceMsg(ActorDefine.ARENA_SERVICE_NAME, msg);
        } else {
            GameMsg.GetAnRankByType msg = new GameMsg.GetAnRankByType(type);
            sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
        }*/
        M21.M210000.S2C.Builder builder=M21.M210000.S2C.newBuilder();
        PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
        builder.addAllRankListInfo(playerProxy.getRankInfos());
        sendNetMsg(ActorDefine.RANKS_MODULE_ID, ProtocolModuleDefine.NET_M21_C210000, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M21_C210000);
    }

    public void checkTimer() {
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        List<ArenaRank> list = new ArrayList<ArenaRank>();
        for (Long id : map.keySet()) {
            ArenaRank ar = new ArenaRank(id, map.get(id), "");
            list.add(ar);
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
