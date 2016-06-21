package com.znl.modules.capacity;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import com.znl.GameMainServer;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Timerdb;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.service.WorldService;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/2/2.
 */
public class CapacityModule extends BasicModule {

    private boolean fixSoldier = false;

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.CountCapacity) {
            SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            long capacity = soldierProxy.getHighestCapacity();
            long _capacity = soldierProxy.initHighestCapacity();
            if (capacity != _capacity) {
                M2.M20002.S2C.Builder builder = M2.M20002.S2C.newBuilder();
                Common.AttrDifInfo.Builder diff = Common.AttrDifInfo.newBuilder();
                diff.setTypeid(PlayerPowerDefine.NOR_POWER_highestCapacity);
                diff.setValue(_capacity);
                builder.addDiffs(diff.build());
                pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20002, builder.build());
                sendPushNetMsgToClient();
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                GameMsg.AddPlayerToRank msg = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), soldierProxy.getHighestCapacity(), PowerRanksDefine.POWERRANK_TYPE_CAPACITY);
                sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
                if (playerProxy.getArmGrouId() > 0) {
                    GameMsg.changeMenberCapity armymsg = new GameMsg.changeMenberCapity(playerProxy.getPlayerId(), soldierProxy.getHighestCapacity());
                    tellMsgToArmygroupNode(armymsg, playerProxy.getArmGrouId());
                }
                PlayerReward reward = new PlayerReward();
                M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_CREATESODIER_NUM, 0, reward);
                if(builder19!=null) {
                    sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                }
            }
        }else if(anyRef instanceof GameMsg.LoginInitCapacity){
            SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if(fixSoldier == false && playerProxy.getPlayer().getLoginOutTime()/1000 < GameMainServer.OPEN_SERVER_TIME()){
                List<Common.SoldierInfo> soldierInfos = soldierProxy.checkBaseNumAndNum();
                if (soldierInfos.size() > 0){
                    HashMap<Integer,Integer> map = WorldService.getWorldCloseReward(playerProxy.getPlayerId(), playerProxy.getAreaKey());
                    if(map != null){
                        List<Integer> powerList = new ArrayList<>();
                        for (Integer power : map.keySet()){
                            playerProxy.addPowerValue(power,map.get(power),LogDefine.GET_CLOSE_WORLD);
                            powerList.add(power);
                        }
                        pushNetMsg(ProtocolModuleDefine.NET_M2,ProtocolModuleDefine.NET_M2_C20002,sendDifferent(powerList));
                    }
                    M4.M40000.S2C.Builder builder = M4.M40000.S2C.newBuilder().addAllSoldiers(soldierInfos);
                    pushNetMsg(ProtocolModuleDefine.NET_M4,ProtocolModuleDefine.NET_M4_C40000,builder.build());
                    PerformTasksProxy performTasksProxy = getProxy(ActorDefine.PERFORMTASKS_PROXY_NAME);
                    performTasksProxy.clearPerformTasks();
                    performTasksProxy.clearTeamNotice();
                    sendPushNetMsgToClient();
                }
            }
            soldierProxy.loginInitPowerValue();
            self().tell(new GameMsg.CountCapacity(), ActorRef.noSender());
            sendModuleMsg(ActorDefine.MAP_MODULE_NAME,new GameMsg.SendTeamTaskInfo());
            fixSoldier = true;
        }
    }

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<CapacityModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public CapacityModule create() throws Exception {
                return new CapacityModule(gameProxy);
            }
        });
    }

    public CapacityModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ActorDefine.CAPACITY_MODULE_ID);
    }


    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
