package com.znl.modules.build;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseLog;
import com.znl.base.BasicModule;
import com.znl.core.Notice;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.BuildingLog;
import com.znl.log.admin.tbllog_shop;
import com.znl.msg.GameMsg;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/6.
 */
public class BuildModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<BuildModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public BuildModule create() throws Exception {
                return new BuildModule(gameProxy);
            }

        });
    }

    public BuildModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M10);
    }

    /****将建筑信息打包进List（去重逻辑）****/
    private void addBuildInfoToList(List<M10.BuildingInfo> buildingInfos,M10.BuildingInfo info){
        for (M10.BuildingInfo buildingInfo : buildingInfos){
            if (buildingInfo.getIndex() == info.getIndex() && buildingInfo.getBuildingType() == info.getBuildingType()){
                return;
            }
        }
        buildingInfos.add(info);
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.BuildInfo) {
            List<List<Object>> infoList = ((GameMsg.BuildInfo) object).infos();
            boolean firstInit = ((GameMsg.BuildInfo) object).firstInit();
            M3.M30000.S2C m30000 = ((GameMsg.BuildInfo) object).m30000();
            M10.M100000.S2C.Builder builder = M10.M100000.S2C.newBuilder();
            List<M10.BuildingInfo> buildingInfos = new ArrayList<>();
            ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
            List<List<Integer>> getlist = new ArrayList<List<Integer>>();
            for (List list : infoList) {
                int buildtype = (Integer) list.get(0);
                int index = (Integer) list.get(1);
                int bigType = (Integer) list.get(2);
                if (buildtype == ResFunBuildDefine.BUILDE_TYPE_COMMOND) {
                    resFunBuildProxy.initResFuBuild(getlist);
                }
                M10.BuildingInfo info = null;
                if (bigType == TimerDefine.BUILD_CREATE){
                    info = resFunBuildProxy.getBuildingInfoBbyBigtypeIndex(ResFunBuildDefine.BUILDE_TYPE_FUNTION,buildtype);
                }else{
                    info = resFunBuildProxy.getBuildingInfo(buildtype, index);
                }
                if (info  != null){
                    addBuildInfoToList(buildingInfos,info);
//                    builder.addBuildingInfos(info);
                }else{
                    System.out.println("出现空值啦，buildtype=" + buildtype + "，index=" + index);
                }
                if (buildtype == ResFunBuildDefine.BUILDE_TYPE_TANK) {
                    List<M10.BuildingInfo> refList = resFunBuildProxy.getBuildingInfobytype(ResFunBuildDefine.BUILDE_TYPE_RREFIT);
                    for (M10.BuildingInfo buildingInfo : refList){
                        addBuildInfoToList(buildingInfos, buildingInfo);
                    }
//                    builder.addAllBuildingInfos();
                }
            }
            for (List list : getlist) {
                int buildtype = (Integer) list.get(0);
                int index = (Integer) list.get(1);
                if(resFunBuildProxy.getBuildingInfo(buildtype, index)!=null) {
                    addBuildInfoToList(buildingInfos, resFunBuildProxy.getBuildingInfo(buildtype, index));
//                    builder.addBuildingInfos(resFunBuildProxy.getBuildingInfo(buildtype, index));
                }
            }
            builder.addAllBuildingInfos(buildingInfos);
            //   builder.addAllBuildingInfos(resFunBuildProxy.getBuildingInfos());
            //初始化的时候先把30000推过去
            if (firstInit){
                pushNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, m30000);
            }
            if (builder.getBuildingInfosBuilderList().size() > 0) {
                builder.setRs(0);
                if (firstInit == true){
                    builder.setRs(2);//初始化给2
                }
                pushNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100000, builder.build());
            }
            sendPushNetMsgToClient();
        } else if (object instanceof GameMsg.ReshBuildings) {
            OnTriggerNet100000Event(null);
        } else if (object instanceof GameMsg.BuyBuildSite) {
            OnTriggerNet100009Event(null);
        }
    }


    private void OnTriggerNet100000Event(Request request) {
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100000.S2C.Builder builder = M10.M100000.S2C.newBuilder();
        builder.addAllBuildingInfos(resFunBuildProxy.getBuildingInfos());
        builder.setRs(1);
        if (request != null) {
            sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100000, builder.build());
        } else {
            pushNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100000, builder.build());
            sendPushNetMsgToClient();
        }
    }

    private void OnTriggerNet100001Event(Request request) {
        M10.M100001.C2S c2s = request.getValue();
        int buildType = c2s.getBuildingType();
        int index = c2s.getIndex();
        int type = c2s.getType();
        int isAutoLv = c2s.getIsAutoLv();
        int upLevel = c2s.getUplevel();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100001.S2C.Builder builder = M10.M100001.S2C.newBuilder();
        List<Integer> powerlist = new ArrayList<Integer>();
        int rs = resFunBuildProxy.buildingLeveUp(buildType, index, type, powerlist,0,upLevel);
        if (rs == ErrorCodeDefine.M100001_6 && isAutoLv != 1) {
            int needGold = resFunBuildProxy.askBuyBuildSize();
            if (needGold > 0) {
                M10.M100009.S2C.Builder s2c = M10.M100009.S2C.newBuilder();
                s2c.setRs(needGold);
                s2c.setGold(resFunBuildProxy.buyBuildSizePrice());
                sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100009, s2c.build());
                builder.setRs(rs);
                sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100001, builder.build());
            } else {
                builder.setRs(rs);
                if (rs == 0) {
                    builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(buildType, index));
                    BuildingLog baseLog = new BuildingLog(index, buildType, LogDefine.BUILDINGLEVEL, 0, 0, resFunBuildProxy.getResFuBuildLevelBysmallType(buildType, index));
                    sendLog(baseLog);
                } else {
                    sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100001, builder.build());
                }
            }
        } else {
            builder.setRs(rs);
            if (rs == 0 || isAutoLv == 1) {
                builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(buildType, index));
                BuildingLog baseLog = new BuildingLog(index, buildType, LogDefine.BUILDINGLEVEL, 0, 0, resFunBuildProxy.getResFuBuildLevelBysmallType(buildType, index));
                sendLog(baseLog);
//                sendFuntctionLog(FunctionIdDefine.GET_BUILDING_INFO_FUNCTION_ID);
                sendNoticeToPushService(TipDefine.NOTICE_TYPE_AUTUBUILD);
                sendFuntctionLog(FunctionIdDefine.BUILDING_UPGRADE_FUNCTION_ID, buildType, index, type);
            } else {
                sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100001, builder.build());
            }
        }
    }

    private void OnTriggerNet100003Event(Request request) {
        M10.M100003.C2S c2s = request.getValue();
        int buildType = c2s.getBuildingType();
        int index = c2s.getIndex();
        int order = c2s.getOrder();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100003.S2C.Builder builder = M10.M100003.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
        int rs = resFunBuildProxy.cancelLevelCreate(buildType, index, order, reward);
        builder.setRs(rs);
        builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(buildType, index));
//        checkPlayerPowerValues(request.getPowerMap());
        if (rs == 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C message1 = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message1);
            sendFuntctionLog(FunctionIdDefine.CANCEL_BUILDING_UPGRADE_FUNCTION_ID, buildType, index, order);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100003, builder.build());
    }

    private void OnTriggerNet100004Event(Request request) {
        M10.M100004.C2S c2s = request.getValue();
        int builtype = c2s.getBuildingType();
        int index = c2s.getIndex();
        int otder = c2s.getOrder();
        int usetype = c2s.getUseType();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100004.S2C.Builder builder = M10.M100004.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        List<PlayerTask> playerTasks = new ArrayList<PlayerTask>();
        int rs = resFunBuildProxy.buildSpeed(builtype, index, otder, usetype, reward, baseLogs, playerTasks);
        if (rs >= 0) {
            builder.setRs(0);
        } else {
            builder.setRs(rs);
        }
       /* if (rs >= 0) {
            builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(builtype, index));
        }*/
        if (rs >= 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C message1 = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message1);
            SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
            List<M3.TimeInfo> m3info = new ArrayList<M3.TimeInfo>();
            M3.TimeInfo.Builder tb = M3.TimeInfo.newBuilder();
            tb.setOthertype(index);
            tb.setBigtype(TimerDefine.BUILD_CREATE);
            tb.setRemainTime(0);
            tb.setSmalltype(builtype);
            m3info.add(tb.build());
            if (otder == -1 && rs == 1) {
                playerTasks.add(new PlayerTask(TaskDefine.TASK_TYPE_BUILDING_LV, 0, 1));
                playerTasks.add(new PlayerTask(TaskDefine.TASK_TYPE_BUILDING_NUM, 0, 1));
                playerTasks.add(new PlayerTask(TaskDefine.TASK_TYPE_BUILDLEVEUP_TIMES, 0, 1));
                updateMySimplePlayerData();
            }
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            taskProxy.getTaskUpdate(playerTasks);
            for (BaseLog baseLog : baseLogs) {
                sendLog(baseLog);
            }
            sendNoticeToPushService(TipDefine.NOTICE_TYPE_AUTUBUILD);
            sendFuntctionLog(FunctionIdDefine.EXPEDITE_UPGRADE_FUNCTION_ID, builtype, index, otder);
            if(builtype == ResFunBuildDefine.BUILDE_TYPE_TANK && otder != -1){
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            }

        }
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100004, builder.build());

    }

    private void OnTriggerNet100005Event(Request request) {
        M10.M100005.C2S c2s = request.getValue();
        int builtype = c2s.getBuildingType();
        int index = c2s.getIndex();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100005.S2C.Builder builder = M10.M100005.S2C.newBuilder();
        int rs = resFunBuildProxy.dropBuilding(builtype, index);
        builder.setRs(rs);
        if (rs == 0) {
            builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(0, index));
            sendNoticeToPushService(TipDefine.NOTICE_TYPE_AUTUBUILD);
            sendNoticeToPushService(TipDefine.NOTICE_TYPE_CREATESOLDIER);
            sendNoticeToPushService(TipDefine.NOTICE_TYPE_SCIENCELEVEL);
            sendFuntctionLog(FunctionIdDefine.DISMANT_BUILDING_FUNCTION_ID, builtype, index, 0);
        }
        builder.setIndex(index);
        builder.setBuildingType(builtype);
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100005, builder.build());

    }


    private void OnTriggerNet100006Event(Request request) {
        M10.M100006.C2S c2s = request.getValue();
        int builtype = c2s.getBuildingType();
        int index = c2s.getIndex();
        int typeId = c2s.getTypeid();
        int num = c2s.getNum();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100006.S2C.Builder builder = M10.M100006.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = resFunBuildProxy.builderProduction(builtype, index, typeId, num, reward, baseLogs);
        builder.setRs(rs);
        if (rs == 0) {
            builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(builtype, index));
        } else {
            sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100006, builder.build());
        }
        if (rs == 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C msg = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg);
            for (BaseLog baseLog : baseLogs) {
                sendLog(baseLog);
            }
            if (ResFunBuildDefine.BUILDE_TYPE_RREFIT == builtype) {
                //判断防守阵型是否需要刷新
                SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
                boolean refurce = formationProxy.checkDefendTroop(soldierProxy, ActorDefine.SETTING_AUTO_ADD_DEFEND_TEAM_ON, null, null);
                if (refurce) {
                    //刷新防守队伍的playerTeam
                    sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.SendFormationToClient());
                }
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            }
            if(ResFunBuildDefine.BUILDE_TYPE_SCIENCE==builtype){
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                PlayerReward reward19000 = new PlayerReward();
                taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_SCIENCELV_TIMES, 1);
            }
            sendFuntctionLog(FunctionIdDefine.DISMANT_PRODUCE_FUNCTION_ID, builtype, typeId, num);
        }
    }


    private void OnTriggerNet100007Event(Request request) {
        M10.M100007.C2S c2s = request.getValue();
        int id = c2s.getId();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100007.S2C.Builder builder = M10.M100007.S2C.newBuilder();
        List<Integer> itemlist = new ArrayList<Integer>();
        int rs = resFunBuildProxy.buyItemandUse(id, itemlist);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100007, builder.build());

        if (itemlist.size() > 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C msg = rewardProxy.getItemListClientInfo(itemlist);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg);
        }
        if (rs == 0) {
            sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, new GameMsg.RefeshItemBuff());
            updateMySimplePlayerData();
            sendNoticeToPushService(TipDefine.NOTICE_TYPE_CREATESOLDIER);
            sendNoticeToPushService(TipDefine.NOTICE_TYPE_SCIENCELEVEL);
        }
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet100008Event(Request request) {
        M10.M100008.C2S c2s = request.getValue();
        int id = c2s.getId();
        int num = c2s.getNum();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100008.S2C.Builder builder = M10.M100008.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
//        List<Integer> itemlist = new ArrayList<Integer>();
        int rs = resFunBuildProxy.shopBuyItem(id, num, reward);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100008, builder.build());
        if (rs >= 0) {
            sendFuntctionLog(FunctionIdDefine.ITEM_BUY_EMPLOY_FUNCTION_ID, id, num, 0);
            //推送背包刷新，奖励飘字等
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
            for (Integer itemId : reward.addItemMap.keySet()) {
                shopLog(itemId, reward.addItemMap.get(itemId), id);
            }
        }
        sendPushNetMsgToClient();
    }


    //请求购买建筑位
    private void OnTriggerNet100009Event(Request request) {
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100009.S2C.Builder builder = M10.M100009.S2C.newBuilder();
        int rs = resFunBuildProxy.askBuyBuildSize();
        builder.setRs(rs);
        builder.setGold(resFunBuildProxy.buyBuildSizePrice());
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100009, builder.build());
        if (rs >= 0) {
            sendFuntctionLog(FunctionIdDefine.BUY_ITEM_IN_SHOP_FUNCTION_ID, rs, 0, 0);
        }
    }

    //购买建筑位
    private void OnTriggerNet100010Event(Request request) {
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100010.S2C.Builder builder = M10.M100010.S2C.newBuilder();
        int rs = resFunBuildProxy.buyBuildSize();
        builder.setRs(rs);
        if (rs == 0) {
//            builder.addAllBuildingInfos(resFunBuildProxy.getBuildingInfos());
            resFunBuildProxy.changetAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OPEN);
        }
        if (resFunBuildProxy.isAutoLeveling(GameUtils.getServerDate().getTime())) {
            builder.setType(1);
        } else {
            builder.setType(2);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100010, builder.build());
        if (rs == 0) {
            resFunBuildProxy.changetAutoBuildState(1);
        }
    }


    //请求购买自动升级
    private void OnTriggerNet100011Event(Request request) {
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100011.S2C.Builder builder = M10.M100011.S2C.newBuilder();
        int rs = resFunBuildProxy.buyAutoLevel();
        builder.setRs(rs);
        if (resFunBuildProxy.isAutoLeveling(GameUtils.getServerDate().getTime())) {
            builder.setType(1);
        } else {
            builder.setType(2);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100011, builder.build());
    }

    //设置定时器的状态
    private void OnTriggerNet100012Event(Request request) {
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        M10.M100012.C2S c2S = request.getValue();
        int state = c2S.getType();
        M10.M100012.S2C.Builder builder = M10.M100012.S2C.newBuilder();
        int rs = resFunBuildProxy.changetAutoBuildState(state);
        builder.setRs(rs);
        builder.setType(state);
        sendNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100012, builder.build());
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
    }

    /**
     * 商城日志
     */
    public void shopLog(int itemId, int num, int shopId) {
        tbllog_shop shoplog = new tbllog_shop();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = playerProxy.getPlayerCache();
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.SHOP, shopId);
        int type = 0;
        int amount = 0;
        if (jsonObject != null) {
            type = jsonObject.getInt("type");
            amount = jsonObject.getInt("goldprice");
        }
        shoplog.setPlatform(cache.getPlat_name());
        shoplog.setRole_id(playerProxy.getPlayerId());
        shoplog.setShopId(type);
        shoplog.setAccount_name(playerProxy.getAccountName());
        shoplog.setDim_level(playerProxy.getLevel());
        shoplog.setDim_prof(0);
        shoplog.setMoney_type(206);
        shoplog.setAmount(amount * num);
        shoplog.setItem_type_1(PlayerPowerDefine.BIG_POWER_ITEM);
        shoplog.setItem_type_2(PlayerPowerDefine.BIG_POWER_ITEM);
        shoplog.setItem_id(itemId);
        shoplog.setItem_number((long) num);
        shoplog.setHappend_time(GameUtils.getServerTime());
        sendLog(shoplog);
    }

    //通过某个类型发送通知
    public void sendNoticeToPushService(int type) {
        //TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
     //   List<Notice> notices = timerdbProxy.getNoticeByType(type);
      //  sendServiceMsg(ActorDefine.PUSH_SERVICE_NAME, new GameMsg.addNoticelist(notices, type));
    }
    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
