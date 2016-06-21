package com.znl.modules.newBuild;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.define.ActorDefine;
import com.znl.define.ErrorCodeDefine;
import com.znl.define.ProtocolModuleDefine;
import com.znl.define.TimerDefine;
import com.znl.framework.socket.Request;
import com.znl.proto.M28;
import com.znl.proxy.GameProxy;
import com.znl.proxy.NewBuildProxy;
import com.znl.proxy.PlayerProxy;
import com.znl.proxy.RewardProxy;
import com.znl.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/20.
 */
public class NewBuildModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<NewBuildModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public NewBuildModule create() throws Exception {
                return new NewBuildModule(gameProxy);
            }

        });
    }

    public NewBuildModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M28);
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {

    }

    //建筑请求升级 包括建造（0级升1级）
    private void OnTriggerNet280001Event(Request request) {
        M28.M280001.C2S c2s = request.getValue();
        List<M28.BuildingShortInfo> shortInfoList = c2s.getBuildingShortInfosList();
        int type = c2s.getType();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        List<M28.BuildingShortInfo> res = new ArrayList<>();
        for (M28.BuildingShortInfo info : shortInfoList){
            int index = info.getIndex();
            int buildType = info.getBuildingType();
            int rs = newBuildProxy.buildingLevelUp(buildType, index, type);
            M28.BuildingShortInfo.Builder builder = M28.BuildingShortInfo.newBuilder();
            builder.setRs(rs);
            builder.setBuildingType(buildType);
            builder.setIndex(index);
            res.add(builder.build());
        }
        M28.M280001.S2C.Builder builder = M28.M280001.S2C.newBuilder();
        builder.addAllBuildingShortInfos(res);
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280001, builder.build());
    }

    //请求完成升级 包括建造（0级升1级）完成成功后，客户端做对应的逻辑
    private void OnTriggerNet280002Event(Request request) {
        M28.M280002.C2S c2s = request.getValue();
        List<M28.BuildingShortInfo> shortInfoList = c2s.getBuildingShortInfosList();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        List<M28.BuildingShortInfo> res = new ArrayList<>();
        for (M28.BuildingShortInfo info : shortInfoList){
            int buildType = info.getBuildingType();
            int index = info.getIndex();
            int rs = newBuildProxy.doBuildLevelUp(buildType, index);
            M28.BuildingShortInfo.Builder builder = M28.BuildingShortInfo.newBuilder();
            if (rs > 0){
                builder.setRs(ErrorCodeDefine.M2800002_3);
                builder.setLevelTime(rs);
            }else {
                builder.setRs(rs);
            }
            builder.setBuildingType(buildType);
            builder.setIndex(index);
            res.add(builder.build());
        }

        M28.M280002.S2C.Builder builder = M28.M280002.S2C.newBuilder();
        builder.addAllBuildingShortInfos(res);
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280002, builder.build());
    }

    //取消建筑升级
    private void OnTriggerNet280003Event(Request request) {
        M28.M280003.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        int buildType = c2s.getBuildingType();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.cancelBuildLevelUp(buildType, index);
        M28.M280003.S2C.Builder builder = M28.M280003.S2C.newBuilder();
        builder.setRs(rs);
        builder.setBuildingType(buildType);
        builder.setIndex(index);
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280003, builder.build());
    }

    //建筑加速升级
    private void OnTriggerNet280004Event(Request request) {
        M28.M280004.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        int buildType = c2s.getBuildingType();
        int costType = c2s.getUseType();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = newBuildProxy.speedBuildLevelUp(buildType, index, costType, reward);
        M28.M280004.S2C.Builder builder = M28.M280004.S2C.newBuilder();
        builder.setRs(rs);
        builder.setIndex(index);
        builder.setBuildingType(buildType);
        if (rs >= 0){
            builder.setRs(0);
            builder.setLevelTime(rs);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280004, builder.build());
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (reward.haveReward()){
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        }
    }

    //野外建筑拆除
    private void OnTriggerNet280005Event(Request request) {
        M28.M280005.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.dropBuilding(index);
        M28.M280005.S2C.Builder builder = M28.M280005.S2C.newBuilder();
        builder.setIndex(index);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280005, builder.build());
    }

    //建筑生产 包括 兵营，校场，工匠坊，科技
    private void OnTriggerNet280006Event(Request request) {
        M28.M280006.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        int buildType = c2s.getBuildingType();
        int num = c2s.getNum();
        int typeId = c2s.getTypeid();
        PlayerReward reward = new PlayerReward();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.builderProduction(buildType,index,typeId,num,reward);
        M28.M280006.S2C.Builder builder = M28.M280006.S2C.newBuilder();
        builder.setRs(rs);
        builder.setBuildingType(buildType);
        builder.setIndex(index);
        if (rs >= 0){
            builder.setProductionInfo(newBuildProxy.getMaxSortProductionInfo(buildType,index));
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280006, builder.build());
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (reward.haveReward()){
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        }
    }

    private void OnTriggerNet280007Event(Request request) {
        M28.M280007.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        int buildType = c2s.getBuildingType();
        int order = c2s.getOrder();
        PlayerReward reward = new PlayerReward();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.doProductionFinish(buildType, index, order, reward);
        M28.M280007.S2C.Builder builder = M28.M280007.S2C.newBuilder();
        builder.setBuildingType(buildType);
        builder.setIndex(index);
        builder.setOrder(order);
        if (rs > 0){
            builder.setRemainTime(rs);
            builder.setRs(ErrorCodeDefine.M280007_2);
        }else if (rs == 0){
            builder.setRs(rs);
            builder.setNextOrder(newBuildProxy.getWorkingSort(buildType,index));

        }else {
            builder.setRs(rs);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280007, builder.build());
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (reward.haveReward()){
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        }
    }

    //取消生产
    private void OnTriggerNet280008Event(Request request) {
        M28.M280008.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        int buildType = c2s.getBuildingType();
        int order = c2s.getOrder();
        PlayerReward reward = new PlayerReward();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.cancelCreate(buildType, index, order, reward);
        M28.M280008.S2C.Builder builder = M28.M280008.S2C.newBuilder();
        if (rs >= 0){
            builder.setNextOrder(newBuildProxy.getWorkingSort(buildType,index));
        }
        builder.setBuildingType(buildType);
        builder.setIndex(index);
        builder.setOrder(order);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280008, builder.build());
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (reward.haveReward()){
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        }
    }

    //加速生产
    private void OnTriggerNet280009Event(Request request) {
        M28.M280009.C2S c2s = request.getValue();
        int index = c2s.getIndex();
        int buildType = c2s.getBuildingType();
        int order = c2s.getOrder();
        int useType = c2s.getUseType();
        PlayerReward reward = new PlayerReward();
        M28.M280009.S2C.Builder builder = M28.M280009.S2C.newBuilder();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.sepeedProduct(buildType,index,order,useType,reward);
        if (rs > 0){
            builder.setRs(0);
            builder.setRemainTime(rs);
            builder.setOrder(order);
        }else {
            builder.setRs(rs);
            if (rs == 0){
                builder.setNextOrder(newBuildProxy.getWorkingSort(buildType,index));
                builder.setOrder(order);
            }
        }
        builder.setBuildingType(buildType);
        builder.setIndex(index);
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280009, builder.build());
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (reward.haveReward()){
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        }
    }

    //客户端自己判断是否可以购买VIP购买建筑位，且计算出价格
    private void OnTriggerNet280011Event(Request request) {
        M28.M280011.S2C.Builder builder = M28.M280011.S2C.newBuilder();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.buyBuildSize();
        builder.setRs(rs);
        if (rs >= 0){
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int autoEndTime = (int) (playerProxy.getAutoBuildStateendtime() /1000);
            int now = GameUtils.getServerTime();
            if (autoEndTime > now){
                builder.setAutoRemainTime(autoEndTime - now);
                builder.setType(playerProxy.getAutoBuildState());
            }else {
                builder.setType(TimerDefine.BUILDAUTOLEVEL_OFF);
                builder.setAutoRemainTime(0);
            }
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280011, builder.build());
    }

    //购买自动升级建筑
    private void OnTriggerNet280012Event(Request request) {
        M28.M280012.S2C.Builder builder = M28.M280012.S2C.newBuilder();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.buyAutoLevel();
        builder.setRs(rs);
        if (rs >= 0){
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int autoEndTime = (int) (playerProxy.getAutoBuildStateendtime() /1000);
            int now = GameUtils.getServerTime();
            if (autoEndTime > now){
                builder.setAutoRemainTime(autoEndTime - now);
                builder.setType(playerProxy.getAutoBuildState());
            }else {
                builder.setType(TimerDefine.BUILDAUTOLEVEL_OFF);
                builder.setAutoRemainTime(0);
            }
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280012, builder.build());
    }

    //自动升级建筑开关
    private void OnTriggerNet280013Event(Request request) {
        M28.M280013.C2S c2s = request.getValue();
        int type = c2s.getType();
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        int rs = newBuildProxy.setAutoLevelUp(type);
        M28.M280013.S2C.Builder builder = M28.M280013.S2C.newBuilder();
        builder.setRs(rs);
        builder.setType(type);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int autoEndTime = (int) (playerProxy.getAutoBuildStateendtime() /1000);
        int now = GameUtils.getServerTime();
        if (autoEndTime > now){
            builder.setAutoRemainTime(autoEndTime - now);
        }else {
            builder.setAutoRemainTime(0);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280013, builder.build());
    }

    //完成自动升级建筑 升级建筑倒计时已经结束
    private void OnTriggerNet280014Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int now = GameUtils.getServerTime();
        int autoEndTime = (int) (playerProxy.getAutoBuildStateendtime() /1000);
        M28.M280014.S2C.Builder builder = M28.M280014.S2C.newBuilder();
        if (autoEndTime > now){
            builder.setRs(ErrorCodeDefine.M280014_1);
            builder.setAutoRemainTime(autoEndTime - now);
        }else {
            builder.setRs(0);
            //关闭自动建造
            playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OFF);
            playerProxy.setAutoBuildStateendtime(0l);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M28, ProtocolModuleDefine.NET_M28_C280014, builder.build());
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
