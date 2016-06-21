package com.znl.modules.activity;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.proto.M2;
import com.znl.proto.M23;
import com.znl.proxy.ActivityProxy;
import com.znl.proxy.GameProxy;
import com.znl.proxy.PlayerProxy;
import com.znl.proxy.RewardProxy;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/1/23.
 */
public class ActivityModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<ActivityModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ActivityModule create() throws Exception {
                return new ActivityModule(gameProxy);
            }
        });
    }

    public ActivityModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M23);
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        activityProxy.loginCheckRefreshTime();
    }


    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.Reload) {
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            activityProxy.reloadDefineData(playerProxy);
            OnTriggerNet230000Event(null);
            OnTriggerNet230002Event(null);
            //获取限时活动列表
        }else if (anyRef instanceof GameMsg.RefreshActivity) {
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            M23.M230002.S2C.Builder builder = M23.M230002.S2C.newBuilder();
            int rs = 0;
            builder.addAllActivitys(activityProxy.getAllLimitActivityInfo());
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230002, builder.build());
            sendFuntctionLog(FunctionIdDefine.GET_LIMIT_ACTIVITY_LISTS_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.RefreshLaba) {
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            List<JSONObject> jsonObjectList = activityProxy.getLaBaActivitid();
            for (JSONObject jsonObject : jsonObjectList) {
                sendLaBaInfo(jsonObject.getInt("effectID"));
            }
            sendPushNetMsgToClient();
        }
    }


    private void sendLaBaInfo(int effectId) {
        int rs = 0;
        M23.LaBaInfo.Builder laBaInfo = M23.LaBaInfo.newBuilder();
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        M23.M230003.S2C.Builder builder = M23.M230003.S2C.newBuilder();
        rs = activityProxy.getLaBaInfo(effectId, laBaInfo, 0);
        if(rs==0) {
            builder.setLabaInfo(laBaInfo);
        }
        builder.setRs(rs);
        pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230003, builder.build());
    }

    private void OnTriggerNet230000Event(Request request) {
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        M23.M230000.S2C.Builder builder = M23.M230000.S2C.newBuilder();
        int rs = 0;
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        boolean open = playerProxy.checkeOpenLevel(ActorDefine.OPEN_ACTIVITY_ID);
        if (!open) {
            rs = ErrorCodeDefine.M230000_9;
        } else {
            builder.addAllActivitys(activityProxy.getAllActivityList());
        }
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230000, builder.build());
        sendFuntctionLog(FunctionIdDefine.GET_ACTIVITY_LISTS_FUNCTION_ID);
    }

    private void OnTriggerNet230001Event(Request request) {
        M23.M230001.C2S c2S = request.getValue();
        int activityId = c2S.getActivityId();
        int effectId = c2S.getEffectId();
        int sort = c2S.getSort();
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        M23.M230001.S2C.Builder builder = M23.M230001.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
        int rs = activityProxy.getActivityReward(activityId, effectId, sort, reward);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230001, builder.build());
        if (rs == 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C rewardbuild = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardbuild);
            OnTriggerNet230000Event(null);
            if (reward.soldierMap.size() > 0) {
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            }
//            sendFuntctionLog(FunctionIdDefine.GET_BUY_ACTIVITY_REWARD_FUNCTION_ID);
        }

    }

    private void OnTriggerNet230002Event(Request request) {
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        M23.M230002.S2C.Builder builder = M23.M230002.S2C.newBuilder();
        int rs = 0;
        builder.addAllActivitys(activityProxy.getAllLimitActivityInfo());
        builder.setRs(rs);
        if(request!=null) {
            sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230002, builder.build());
        }else{
            pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230002, builder.build());
            sendPushNetMsgToClient();
        }
        sendFuntctionLog(FunctionIdDefine.GET_LIMIT_ACTIVITY_LISTS_FUNCTION_ID);
    }

    /**
     * 获取有福同享宝箱列表
     *
     * @param request
     */
    private void OnTriggerNet230005Event(Request request) {
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        M23.M230005.S2C  builder= activityProxy.getLegionShareBoxInfo();
        sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230005, builder);
       // pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230005, builder);
        //sendPushNetMsgToClient();
    }

    /**
     * 请求领取有福同享宝箱奖励
     * @param request
     */
    private void OnTriggerNet230006Event(Request request) {
        M23.M230006.C2S c2S = request.getValue();
        int id=c2S.getId();
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        M23.M230006.S2C resultBuilder= activityProxy.getLegionShareBoxAward(id, reward);
        sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230006, resultBuilder);
//        pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230006, resultBuilder);
//        sendPushNetMsgToClient();
        if(resultBuilder.getResult()==0&&reward.haveReward()){
            //领取成功才通知发送奖励
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_LEGIONSHARE_SHARE);
            M2.M20007.S2C rewardbuild = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardbuild);
            //小红点操作d
            GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
        }

    }

    private void OnTriggerNet230003Event(Request request) {
        M23.M230003.C2S c2S = request.getValue();
        int effectId = c2S.getEffectId();
        int type = c2S.getType();
        int rs = 0;
        M23.LaBaInfo.Builder laBaInfo = M23.LaBaInfo.newBuilder();
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        PlayerReward playerReward = new PlayerReward();
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        M23.M230003.S2C.Builder builder = M23.M230003.S2C.newBuilder();
        if (type == 0) {
            rs = activityProxy.getLaBaInfo(effectId, laBaInfo, type);
            if(rs==0) {
                builder.setLabaInfo(laBaInfo);
            }
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230003, builder.build());
            sendFuntctionLog(FunctionIdDefine.GET_LIMIT_ACTIVITY_INFO);
        } else {
            activityProxy.getLaBaInfo(effectId, laBaInfo, type);
            rs = activityProxy.LaBaLotter(type, effectId, playerReward, laBaInfo);
            M2.M20007.S2C reward = rewardProxy.getRewardClientInfo(playerReward);
            if(rs==0) {
                builder.setLabaInfo(laBaInfo);
            }
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230003, builder.build());
            if (reward.getSoldierListList().size() == 0) {
                System.err.println("出现0值了");
            }
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, reward);
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            sendFuntctionLog(FunctionIdDefine.BUY_LABA_LOTTER);
        }
    }

    /**
     * 请求校验要删除的活动
     * @param request
     */
    private void OnTriggerNet230008Event(Request request) {
        M23.M230008.C2S c2S = request.getValue();
        List<Integer>ids=c2S.getCheckActivityIdsList();
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        M23.M230008.S2C resultBuilder= activityProxy.checkActivityToDelete(c2S.getCheckActivityIdsList());
        sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230008, resultBuilder);
    }


    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
