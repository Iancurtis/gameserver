package com.znl.modules.lotter;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.proto.M15;
import com.znl.proto.M19;
import com.znl.proto.M2;
import com.znl.proxy.*;

/**
 * Created by Administrator on 2015/12/1.
 */
public class LotterModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<LotterModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public LotterModule create() throws Exception {
                return new LotterModule(gameProxy);
            }
        });
    }

    public LotterModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M15);
    }


    @Override
    public void onReceiveOtherMsg(Object object) {

    }



    //TODO 该协议需做迁移
    private void OnTriggerNet150000Event(Request request) {
        M15.M150000.S2C.Builder builder = M15.M150000.S2C.newBuilder();
        LotterProxy lotterProxy = getProxy(ActorDefine.LOTTER_PROXY_NAME);
        int rs=0;

        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        boolean open = playerProxy.checkeOpenLevel(ActorDefine.OPEN_LOTTERY_EQUIP_ID);
        if(!open){
            rs = ErrorCodeDefine.M150000_1;
        }else{
            rs = lotterProxy.getAllEquipLotterInfos(builder);
        }
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M15, ProtocolModuleDefine.NET_M15_C150000, builder.build());
        sendFuntctionLog(FunctionIdDefine.GET_EQUIP_LOTTER_INFOS_FUNCTION_ID);
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet150001Event(Request request) {
        M15.M150001.C2S c2s = request.getValue();
        int type = c2s.getType();
        M15.M150001.S2C.Builder builder = M15.M150001.S2C.newBuilder();
        LotterProxy lotterProxy = getProxy(ActorDefine.LOTTER_PROXY_NAME);
        PlayerReward equreward = new PlayerReward();
        int rs = lotterProxy.lotterEquip(type, equreward, builder);
        builder.setRs(rs);
        builder.setType(type);
        for (int i = 1; i <= 3; i++) {
            builder.addEquipLotterInfos(lotterProxy.getEquipLotterInfo(i));
        }
        sendNetMsg(ProtocolModuleDefine.NET_M15, ProtocolModuleDefine.NET_M15_C150001, builder.build());
        if (rs == 0) {
            sendM20007(equreward);
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_LOTTEREQUIP_TIEMS, 1);
        }
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet150002Event(Request request) {
        M15.M150002.C2S c2s = request.getValue();
        int type = c2s.getType();
        int num=c2s.getNum();
        M15.M150002.S2C.Builder builder = M15.M150002.S2C.newBuilder();
        LotterProxy lotterProxy = getProxy(ActorDefine.LOTTER_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = lotterProxy.lotteTaobao(type,num,builder, reward);
        builder.setRs(rs);
        builder.setType(type);
        sendNetMsg(ProtocolModuleDefine.NET_M15, ProtocolModuleDefine.NET_M15_C150002, builder.build());
        if (rs == 0) {
            sendM20007(reward);
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
        }
        sendNetMsg(ProtocolModuleDefine.NET_M15, ProtocolModuleDefine.NET_M15_C150003, lotterProxy.getTaoInfos(type));
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet150003Event(Request request) {
        M15.M150003.C2S c2s = request.getValue();
        int type = c2s.getType();
        int buynum=c2s.getNum();
        int autoTen=c2s.getAutoTenBuy();
        M15.M150003.S2C.Builder builder = M15.M150003.S2C.newBuilder();
        LotterProxy lotterProxy = getProxy(ActorDefine.LOTTER_PROXY_NAME);
        int rs = 0;
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        boolean open = playerProxy.checkeOpenLevel(ActorDefine.OPEN_TREASURE_MODULE_ID);
        if(!open){
            rs = ErrorCodeDefine.M150003_4;
         }else {
            if (type == 3) {
                M15.Taobao.Builder t1 = M15.Taobao.newBuilder();
                t1.setType(1);
                t1.setTimes(lotterProxy.getFreTaobaoLotterTimes(1));
                builder.addTaobaos(t1.build());
                M15.Taobao.Builder t2 = M15.Taobao.newBuilder();
                t2.setType(2);
                t2.setTimes(lotterProxy.getFreTaobaoLotterTimes(2));
                builder.addTaobaos(t2.build());
            } else {
                PlayerReward reward = new PlayerReward();
                rs = lotterProxy.buyLuckyCoin(type, reward,buynum);
                M15.Taobao.Builder t1 = M15.Taobao.newBuilder();
                t1.setType(1);
                t1.setTimes(lotterProxy.getFreTaobaoLotterTimes(1));
                builder.addTaobaos(t1.build());
                M15.Taobao.Builder t2 = M15.Taobao.newBuilder();
                t2.setType(2);
                t2.setTimes(lotterProxy.getFreTaobaoLotterTimes(2));
                builder.addTaobaos(t2.build());
                if (rs == 0) {
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
                }
            }
        }
        if(autoTen==10){
            builder.setRs(rs);
            builder.setType(type);
            builder.setAutoTenBuy(autoTen);
        }else{
            builder.setRs(rs);
            builder.setType(type);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M15, ProtocolModuleDefine.NET_M15_C150003, builder.build());
        sendPushNetMsgToClient();
    }

    private void sendM20007(PlayerReward reward) {
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        M2.M20007.S2C msg = rewardProxy.getRewardClientInfo(reward);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, msg);
    }


    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
