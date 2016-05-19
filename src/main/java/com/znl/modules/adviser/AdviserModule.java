package com.znl.modules.adviser;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.proto.M2;
import com.znl.proto.M23;
import com.znl.proto.M26;
import com.znl.proxy.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/23.
 */
public class AdviserModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<AdviserModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AdviserModule create() throws Exception {
                return new AdviserModule(gameProxy);
            }
        });
    }

    public AdviserModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M26);
    }


    @Override
    public void onReceiveOtherMsg(Object anyRef) {

    }

    private void OnTriggerNet260000Event(Request request) {
        M26.M260000.S2C.Builder s2c = M26.M260000.S2C.newBuilder();
        AdviserProxy adviserProxy = getProxy(ActorDefine.ADVISER_PROXY_NAME);
        s2c.setRs(0);
        s2c.addAllAdviserinfo(adviserProxy.getAllAdviserInfo());
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260000, s2c.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260000);
    }

    private void OnTriggerNet260001Event(Request request) {
        M26.M260001.C2S c2S=request.getValue();
        List<Integer> ids=c2S.getTypeIdsList();
        int quilty=c2S.getQuilty();
        PlayerReward reward=new PlayerReward();
        M26.M260001.S2C.Builder builder=M26.M260001.S2C.newBuilder();
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
        int rs=adviserProxy.adviserAdvance(ids,quilty,reward,builder);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260001,builder.build() );
        if(rs==0){
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007,rewardProxy.getRewardClientInfo(reward));
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260001);
    }

    private void OnTriggerNet260002Event(Request request) {
        M26.M260002.C2S c2S = request.getValue();
        int typeId = c2S.getTypeId();
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
       PlayerReward reward=new PlayerReward();
        M26.M260002.S2C.Builder builder= M26.M260002.S2C.newBuilder();
        int rs=adviserProxy.adviserLv(typeId,reward);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260002,builder.build() );
        if(rs==0){
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007,rewardProxy.getRewardClientInfo(reward));
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260002);
    }


    private void OnTriggerNet260003Event(Request request) {
        M26.M260003.C2S c2S = request.getValue();
        List<Integer> typeids=c2S.getTypeidsList();
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
        PlayerReward reward=new PlayerReward();
        M26.M260003.S2C.Builder builder= M26.M260003.S2C.newBuilder();
        int rs=adviserProxy. adviserdrop(typeids,reward);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260003,builder.build() );
        if(rs==0){
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007,rewardProxy.getRewardClientInfo(reward));
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260003);
    }



    private void OnTriggerNet260004Event(Request request) {
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
        M26.M260004.S2C.Builder builder= M26.M260004.S2C.newBuilder();
        builder.addAllCostInfos(adviserProxy.getCostInfo());
        builder.setRs(0);
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260004,builder.build() );
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260004);
    }


    private void OnTriggerNet260005Event(Request request) {
        M26.M260005.C2S c2S = request.getValue();
        int type=c2S.getType();
        int num=c2S.getNum();
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
        PlayerReward reward=new PlayerReward();
        M26.M260005.S2C.Builder builder= M26.M260005.S2C.newBuilder();
        int rs=adviserProxy.adviserLotter(type,num,reward,builder);
        builder.setRs(rs);
        builder.setType(type);
        builder.addAllCostInfos(adviserProxy.getCostInfo());
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260005,builder.build() );
        if(rs==0){
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007,rewardProxy.getRewardClientInfo(reward));
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260005);
    }

    private void OnTriggerNet260006Event(Request request) {
        M26.M260006.C2S c2S = request.getValue();
        int quilty=c2S.getQuilty();
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
        PlayerReward reward=new PlayerReward();
        M26.M260006.S2C.Builder builder= M26.M260006.S2C.newBuilder();
        int rs=adviserProxy.allAdvance(quilty,reward,builder);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M26, ProtocolModuleDefine.NET_M26_C260006,builder.build() );
        if(rs==0){
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007,rewardProxy.getRewardClientInfo(reward));
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M26_C260006);
    }
    /**
     * 重复协议请求处理
     * @param request 协议id
     */
    @Override
    public void repeatedProtocalHandler(Request request) {

    }

}
