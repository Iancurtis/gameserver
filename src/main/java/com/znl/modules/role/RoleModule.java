package com.znl.modules.role;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseLog;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.*;
import com.znl.log.admin.tbllog_event;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.set.RoleNameSetDb;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.service.PowerRanksService;
import com.znl.utils.GameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.Tuple2;

import java.util.*;

/**
 * Created by Administrator on 2015/10/27.
 */
public class RoleModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<RoleModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public RoleModule create() throws Exception {
                return new RoleModule(gameProxy);
            }
        });
    }

    public RoleModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M2);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.rankmap.putAll(PowerRanksService.GetRanks(playerProxy.getPlayerId()));
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.InitSendRoleInfo) {
            initSendRoleInfo();
        } else if (object instanceof GameMsg.RefrshTip) {
            OnTriggerNet20200Event(null);
        } else if (object instanceof GameMsg.addAtivity) {
            int val = ((GameMsg.addAtivity) object).value();
            int expendvalue = ((GameMsg.addAtivity) object).expandCondition();
            int type = ((GameMsg.addAtivity) object).conditionType();
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            activityProxy.addActivityConditionValue(type, val, playerProxy, expendvalue);
            GameMsg.RefrshTip msg=new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME,msg);
        } else if (object instanceof GameMsg.BanPlayerChat) {
            int time = ((GameMsg.BanPlayerChat) object).time();
            int status = ((GameMsg.BanPlayerChat) object).status();
            onBanPlayerChat(time, status);
        } else if (object instanceof GameMsg.InstructorGM) {
            int type = ((GameMsg.InstructorGM) object).instructorType();
            int startTime = ((GameMsg.InstructorGM) object).startTime();
            int endTime = ((GameMsg.InstructorGM) object).endTime();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.setPlayerType(type, startTime, endTime);
        } else if (object instanceof GameMsg.getlaterInfoback) {
            M2.M20400.S2C.Builder builder = ((GameMsg.getlaterInfoback) object).build();
            builder.setRs(0);
            pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20400, builder.build());
            sendPushNetMsgToClient();
        } else if (object instanceof GameMsg.getMyRanksback) {
            Map<Integer, Integer> map = ((GameMsg.getMyRanksback) object).map();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.rankmap.clear();
            playerProxy.rankmap.putAll(map);
        } else if (object instanceof GameMsg.changeRankBytype) {
            int type = ((GameMsg.changeRankBytype) object).retype();
            int value = ((GameMsg.changeRankBytype) object).value();
            if (value < 0) {
                value = 0;
            }
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.rankmap.put(type, value);
        }else if (object instanceof GameMsg.refreshengry) {
            OnTriggerNet20013Event(null);
        }else if (object instanceof GameMsg.PlayerSetContributeRank) {
            int rank = ((GameMsg.PlayerSetContributeRank) object).Activitycontributerank();
            onPlayerSetContributeRank(rank);
        }else if(object instanceof  GameMsg.ReshEveryDay){
            OnTriggerNet20015Event(null);
        }else if(object instanceof  GameMsg.addLegionShareRecord){
            //有福同享增加包箱奖励
            GameMsg.addLegionShareRecord legionObject= (GameMsg.addLegionShareRecord) object;
            int chargeId=legionObject.chargeId();
            int createTime=legionObject.createTime();
            String sharePlayerName=legionObject.sharePlayerName();
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M23.M230005.S2C legionShareRecBuilder=activityProxy.addLegionShareRecord(playerProxy,chargeId,sharePlayerName,createTime);
            //通知前端有新宝箱增加
            pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230005, legionShareRecBuilder);
            sendPushNetMsgToClient();
            GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
        }

    }

    private void onPlayerSetContributeRank(int i){
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.getPlayer().setActivitycontributerank(i);
    }
    private void onBanPlayerChat(int time, int status) {
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.onBanPlayerChat(time, status);
    }

    //初始化发送角色信息，第一次创建playerActor才会发送
    private void initSendRoleInfo() {
        OnTriggerNet20000Event(null);
        sendPushNetMsgToClient();
        sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.LoginInitCapacity());
    }


    private void OnTriggerNet20000Event(Request request) {
//        try{
        //M2.M20000.C2S c2s = request.getValue();
        CustomerLogger.info("-------onReceiveGetRoleInfoReq-----------------");
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Common.AttrInfo> powers = playerProxy.getPlayerAllPower();
        String playerName = playerProxy.getPlayerName();
        SoldierProxy soldierProxy = this.getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        ItemProxy itemProxy = this.getProxy(ActorDefine.ITEM_PROXY_NAME);
        OrdnanceProxy ordnanceProxy = this.getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        OrdnancePieceProxy ordnancePieceProxy = this.getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        ResFunBuildProxy resFunBuildProxy = this
                .getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        itemProxy.testAdditem();
        List<Common.SoldierInfo> soldiers = soldierProxy.getSoldierInfos();

        BuildingProxy buildingProxy = this.getProxy(ActorDefine.BUILDING_PROXY_NAME);
        Tuple2<Integer, Integer> point = buildingProxy.getWorldTilePoint();
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        NewBuildProxy newBuildProxy = getProxy(ActorDefine.NEW_BUILD_PROXY_NAME);
        EquipProxy equipProxy=getProxy(ActorDefine.EQUIP_PROXY_NAME);
        SystemProxy systemProxy=getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        TaskProxy taskProxy=getProxy(ActorDefine.TASK_PROXY_NAME);
        SkillProxy skillProxy = this.getProxy(ActorDefine.SKILL_PROXY_NAME);
        //ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        PerformTasksProxy performTasksProxy = getProxy(ActorDefine.PERFORMTASKS_PROXY_NAME);
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        AdviserProxy adviserProxy=getProxy(ActorDefine.ADVISER_PROXY_NAME);
        FriendProxy friendProxy=getProxy(ActorDefine.FRIEND_PROXY_NAME);
        List<M16.MailShortInfo> mails = mailProxy.getMailShortInfoList();
        //发送之前先执行一次检查
        newBuildProxy.addOfflineLevelAndProduction();

        M28.AutoUpgradeInfo.Builder autoInfo = M28.AutoUpgradeInfo.newBuilder();
        int autoTime = (int) (playerProxy.getAutoBuildStateendtime()/1000 - GameUtils.getServerTime());
        if (autoTime < 0){
            autoTime = 0;
        }
        int prestate=playerProxy.isGetTadayPrestige();
        if(prestate<0){
            prestate=1;
        }
        autoInfo.setAutoRemainTime(autoTime).setType(playerProxy.getAutoBuildState());
        M2.M20000.S2C s2c = M2.M20000.S2C.newBuilder()
                .setRs(0)
                .setActorInfo(M2.ActorInfo.newBuilder()
                        .addAllAttrInfos(powers)
                        .setName(playerName)
                        .setWorldTileX(point._1())
                        .setWorldTileY(point._2())
                        .setPlayerId(playerProxy.getPlayerId())
                        .setIconId(playerProxy.getIconId())
                        .setPendantId(playerProxy.getPendt())
                        .setLegionId(playerProxy.getArmGrouId())
                        .setLegionName(playerProxy.getPlayer().getLegionName())
                        .setLegionLevel(playerProxy.getPlayer().getLegionLevel())
                        .setNewGift(playerProxy.getHaveNewGift())
                        .setFameState(prestate)
                        .setEngryprice(playerProxy.askBuyEnergy())
                        //  .addAllRemainlist(playerProxy.getRemainList())
                        .build()
                )
                .addAllSoldierList(soldiers)
                .addAllItemList(itemProxy.getAllItemInfos())
                .addAllOdInfos(ordnanceProxy.getOrdnanceInfos())
                .addAllOdpInfos(ordnancePieceProxy.getOrdnancePieceInfos())
                .setDungeonInfos(dungeoProxy.getdungeonlist())
                .addAllBuildingInfos(newBuildProxy.getAllBuildingInfo())
                .addAllEquipinfos(equipProxy.getEquipInfos())
                .addAllCacheInfos(systemProxy.getallClientCacheInfos())
                .addAllInfo(formationProxy.getFormationInfos())
                .setTaskList(taskProxy.getTaskInfoList())
                .addAllSkillInfos(skillProxy.getAllSkillInfo())
               // .addAllItemBuffInfo(itemBuffProxy.sendItemBuffInfoToClient())
                .addAllList(performTasksProxy.getAllTaskTeamInfoList())
                .addAllMails(mails)
                .addAllActivitys(activityProxy.getAllActivityList())
                .addAllLimitActivitys(activityProxy.getAllLimitActivityInfo())
                .addAllAdviserinfo(adviserProxy.getAllAdviserInfo())
                .addAllSoldiers(soldierProxy.getAllLostSoldierInfos())
                .addAllCostInfos(adviserProxy.getCostInfo())
                .addAllRankinfos(playerProxy.getRankInfos())
                .setFriBleInfos(friendProxy.getFriendBlessInfo())
                .setLegionrewardinfo(playerProxy.getLogionRewadInfo())
                .build();
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20000, s2c);
        int preid = playerProxy.loginGetPrestige();
        sendPreAutoReward(preid);
//        }catch (Exception e){
//            CustomerLogger.error("OnTriggerNet20000Event时出现错误",e);
//            e.printStackTrace();
//        }
        List<JSONObject> jsonObjects = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT,"conditiontype",119);
        for (JSONObject jsonObject : jsonObjects) {
            int lv = jsonObject.getInt("condition1");
            int num =activityProxy.getVipMap(lv);
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_VIPPEOPLE_NUM, lv, playerProxy, num);
        }
        activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_ENERY_EVERYDAY,0,playerProxy,0);
//        sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME,new GameMsg.RefTimerSet(TimerDefine.CHANGE_TIMER_INIT_TYPE_LOGIN));
    }

    private void sendPreAutoReward(int preId) {
        M2.M20017.S2C.Builder builder = M2.M20017.S2C.newBuilder();
        builder.setRs(0);
        builder.setPreid(preId);
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20017, builder.build());
        sendPushNetMsgToClient();
    }

    //军衔升级
    private void OnTriggerNet20001Event(Request request) {
        try {
            CustomerLogger.info("-------onReceiveAddMilitaryReq-----------------");
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int rs = playerProxy.addMilitaryHandle();
            M2.M20001.S2C.Builder builder = M2.M20001.S2C.newBuilder();
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20001, builder.build());
            if (rs == 0) {
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                PlayerReward reward19 = new PlayerReward();
                M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_JUNXIAN_LV, 1, reward19);
                if (builder19 != null) {
                    sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward19));
                }
                //日志记录
                int level = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_militaryRank);
                JSONObject militaryInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MILITARYRANK, "ID", level - 1);
                MilitaryLog log = new MilitaryLog(level, militaryInfo.getInt("crysneed"), militaryInfo.getInt("prestige"));
                sendLog(log);
                sendFuntctionLog(FunctionIdDefine.MILITARY_RANK_FUNCTION_ID);
            }
        } catch (Exception e) {
            CustomerLogger.error("OnTriggerNet20001Event时出现错误", e);
            e.printStackTrace();
        }
    }

    //购买繁荣度
    public void OnTriggerNet20003Event(Request request) {
        try {
            CustomerLogger.info("-------onReceiveBuyBoomReq-----------------");
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            List<BoomLog> log = new ArrayList<BoomLog>();
            int rs = playerProxy.buyBoom(log);
            M2.M20003.S2C.Builder builder = M2.M20003.S2C.newBuilder();
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20003, builder.build());
            //日志记录
            if (rs == 0) {
                for (BoomLog lg : log) {
                    sendLog(lg);
                }
                sendFuntctionLog(FunctionIdDefine.BUY_BOOM_FUNCTION_ID);
            }
        } catch (Exception e) {
            CustomerLogger.error("OnTriggerNet20003Event时出现错误", e);
            e.printStackTrace();
        }
    }

    //统率升级
    public void OnTriggerNet20004Event(Request request) {
        try {
            M2.M20004.C2S c2s = request.getValue();
            int type = c2s.getType();
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            List<Integer> itemList = new ArrayList<Integer>();
            int rs = playerProxy.upCommandLv(type, itemList);
            M2.M20004.S2C.Builder builder = M2.M20004.S2C.newBuilder();
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20004, builder.build());
            if (rs == 0) {
                SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            }
            if (type == ActorDefine.DEFINE_UPLV_CMBOOK) {//统帅书升级
                GameMsg.SystemTimer message = new GameMsg.SystemTimer();
//                sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, message); //2016/04/11屏蔽发送
                if (itemList.size() > 0) {
                    RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                    M2.M20007.S2C msg = rewardProxy.getItemListClientInfo(itemList);
                    sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg);
                }
            }
            //日志记录
            if (rs == 0) {
                CommandLog log;
                if (type == ActorDefine.DEFINE_UPLV_CMBOOK) {
                    log = new CommandLog((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_commandLevel), 0, 1);
                } else {
                    log = new CommandLog((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_commandLevel), ActorDefine.MAX_UPCOMMD_GOLD, 0);
                }
                sendLog(log);
                sendFuntctionLog(FunctionIdDefine.UP_COMMAND_LV_FUNCTION_ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //授勋声望
    public void OnTriggerNet20005Event(Request request) {
        try {
            CustomerLogger.info("-------onReceiveGetPrestigeInfoReq-----------------");
            M2.M20005.C2S c2s = request.getValue();
            int type = c2s.getType();
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M2.M20005.S2C.Builder builder = M2.M20005.S2C.newBuilder();
            int rs = playerProxy.medalGetPrestige(type, builder);
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20005, builder.build());
            if (rs == 0) {
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                PlayerReward reward = new PlayerReward();
                M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_SHENGWANG_LV, 1, reward);
                if (builder19 != null) {
                    sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                }
                //日志记录
                JSONObject medalInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.PRESTIGE_GIVE, "type", type);
                int prestigeNum = medalInfo.getInt("prestige");
                PrestigeLog log = new PrestigeLog((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_prestigeLevel), prestigeNum, GameUtils.getServerDateStr());
                sendLog(log);

            }
        } catch (Exception e) {
            CustomerLogger.error("-------OnTriggerNet20005Event出现错误！！-----------------");
            e.printStackTrace();
        }
    }

    //创建角色 需要判断名称是否重复及进行长度校验
    public void OnTriggerNet20008Event(Request request) {
        M2.M20008.C2S c2s = request.getValue();
        String name = c2s.getName();
        int sex = c2s.getSex();
        int rs = 0;
        M2.M20008.S2C.Builder builder = M2.M20008.S2C.newBuilder();
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        String areaKey = playerProxy.getAreaKey();
        Long playerId = playerProxy.getPlayerId();
        RoleNameSetDb roleNameSetDb = BaseSetDbPojo.getSetDbPojo(RoleNameSetDb.class, areaKey);
        Boolean isRepeat = roleNameSetDb.isKeyExist(name);

        int ro = playerProxy.roleNameCheck(name);
        if (ro == ActorDefine.CHARS_ROLE_NAME) {
            rs = ErrorCodeDefine.M20008_6;
        } else if (ro == ActorDefine.CHINESE_ROLE_NAME) {
            rs = ErrorCodeDefine.M20008_2;
        } else if (ro == ActorDefine.ENGLISH_ROLE_NAME) {
            rs = ErrorCodeDefine.M20008_3;
        } else if (ro == ActorDefine.CHINESE_ENGLISH_ROLE_NAME) {
            if (name.length() > ActorDefine.ROLE_ENGlISHNAME_LENGTH_MAX) {
                rs = ErrorCodeDefine.M20008_4;
            }
        } else if (isRepeat) {
            rs = ErrorCodeDefine.M20008_1;
        } else if (playerProxy.getPlayerName().length() > 0) {
            rs = ErrorCodeDefine.M20008_5;
        }

        if (rs >= 0) {
            //名字合法性 还需要校验
            //先默认直接可以创建
            playerProxy.createRole(name, sex);
            roleNameSetDb.addKeyValue(name, playerId);
            //创建名字的时候添加到世界
            GameMsg.CreateWorldBuild mess = new GameMsg.CreateWorldBuild();
            sendModuleMsg(ActorDefine.LOGIN_MODULE_NAME, mess);
            builder.setName(name);
            sendFuntctionLog(FunctionIdDefine.CREATE_ROLE_FUNCTION_ID);
        }


        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20008, builder.build());
        //已经创建好名字了，更新一下自己的在线数据
        updateMySimplePlayerData();

        if (rs >= 0) {
            //推送给前端
            M2.M20012.S2C.Builder builder12 = M2.M20012.S2C.newBuilder();
            builder12.setRs(0);
            builder12.setIconId(playerProxy.getIconId());
            builder12.setPendantId(playerProxy.getPendt());
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20012, builder12.build());
            // sendModuleMsg(ActorDefine.TROOP_MODULE_NAME,new GameMsg.AutoPushToArena());
        }
    }

    //打开领取声望
    public void OnTriggerNet20010Event(Request request) {
        try {
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M2.M20010.C2S c2s = request.getValue();
            int state = c2s.getState();
            M2.M20010.S2C.Builder builder = M2.M20010.S2C.newBuilder();
            int rs = playerProxy.isGetTadayPrestige();
            if (state == 0) {
                builder.setRs(0);
                builder.setState(rs);
            } else if (state == 1) {
                builder.setRs(rs);
            }
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20010, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //请求是否可以购买体力
    public void OnTriggerNet20013Event(Request request) {
        try {
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M2.M20013.S2C.Builder builder = M2.M20013.S2C.newBuilder();
            int rs = playerProxy.askBuyEnergy();
            builder.setPrice(rs);
            if (rs > 0) {
                rs = 0;
            }
            builder.setRs(rs);
            if (request == null) {
                sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20013, builder.build());
            } else {
                pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20013, builder.build());
                sendPushNetMsgToClient();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //购买体力
    public void OnTriggerNet20011Event(Request request) {
        try {
            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M2.M20011.S2C.Builder builder = M2.M20011.S2C.newBuilder();
            List<EnergyLog> log = new ArrayList<EnergyLog>();
            int rs = playerProxy.buyEnergy(log);
            builder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20011, builder.build());
            //日志记录
            if (rs == 0) {
                for (EnergyLog lg : log) {
                    sendLog(lg);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置头像，挂件
    public void OnTriggerNet20012Event(Request request) {
        M2.M20012.C2S c2s = request.getValue();
        int headId = c2s.getIconId();
        int pendantId = c2s.getPendantId();
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int rs = playerProxy.setPlayerHeadPendant(headId, pendantId);
        M2.M20012.S2C.Builder builder = M2.M20012.S2C.newBuilder();
        builder.setRs(rs);
        if (rs == 0) {
            builder.setIconId(headId);
            builder.setPendantId(pendantId);
            sendFuntctionLog(FunctionIdDefine.SET_HEAD_PENDANT_FUNCTION_ID);
            tellMsgToArmygroupNode(new GameMsg.changetIconPend(playerProxy.getPlayerId(), playerProxy.getPlayerIcon(), playerProxy.getPendt()), playerProxy.getArmGrouId());
            updateMySimplePlayerData();
        }
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20012, builder.build());
    }

    //领取30天登录奖励
    public void OnTriggerNet20015Event(Request request) {
        int dayNum=0;
        if(request!=null) {
            M2.M20015.C2S c2s = request.getValue();
            dayNum = c2s.getDayNum();
        }
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        M2.M20015.S2C.Builder builder = M2.M20015.S2C.newBuilder();
        int type = 0;
        int rs = 0;
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = timerdbProxy.getTimerNum(TimerDefine.LOGIN_LOTTERY, 0, 0);
        try {
            if (dayNum == 0) {//请求可不可领
                if (playerProxy.getRewardNum().size() <= 0 && playerProxy.getLoginDayNum() >= ActorDefine.LOGIN_DAY_NUM) {
                    type = 2;//显示每日抽奖
                    builder.addAllCanGet(new ArrayList<Integer>());
                    if (num == 0) {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 1);
                    } else {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 2);
                    }
                } else {
                    boolean open = playerProxy.checkeOpenLevel(ActorDefine.OPEN_THIRTY_LOGIN_AWARD_ID);
                    type = 1;//显示30登录领取奖励
                  /*  if (!open) {
                        rs = ErrorCodeDefine.M20015_5;
                    } else {*/
                        builder.addAllCanGet(playerProxy.getRewardNum());
                        builder.setAllDay(playerProxy.getLoginDayNum());
                   // }
                }
                builder.setRs(rs);
                builder.setType(type);
                if(request!=null) {
                    sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20015, builder.build());
                }else{
                    pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20015, builder.build());
                    sendPushNetMsgToClient();
                }
            } else {//领取情况
                if (playerProxy.getRewardNum().size() == 0 && playerProxy.getLoginDayNum() >= ActorDefine.LOGIN_DAY_NUM) {
                    type = 2;//每日抽奖
                    builder.addAllCanGet(null);
                    rs = 0;
                    if (num == 0) {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 1);
                    } else {
                        builder.setAllDay(ActorDefine.LOGIN_DAY_NUM + 2);
                    }
                } else {
                    rs = playerProxy.getRewardDayNum(dayNum, reward);
                    type = 1;//30登录领取奖励
                    builder.addAllCanGet(playerProxy.getRewardNum());
                    builder.setAllDay(playerProxy.getLoginDayNum());
                }
                builder.setType(type);
                builder.setRs(rs);
                sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20015, builder.build());
                RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                M2.M20007.S2C builder1 = rewardProxy.getRewardClientInfo(reward);
                sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, builder1);
                if (reward.soldierMap.size() > 0) {
                    sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
                }
                //阵型
                sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.CheckBaseDefendFormation());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //每日登录抽奖
    public void OnTriggerNet20016Event(Request request) {
        M2.M20016.C2S c2s = request.getValue();
        int type = c2s.getType();
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        M2.M20016.S2C.Builder builder = M2.M20016.S2C.newBuilder();
        TimerdbProxy timerProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        timerProxy.addTimer(TimerDefine.LOGIN_LOTTERY, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        int num = timerProxy.getTimerNum(TimerDefine.LOGIN_LOTTERY, 0, 0);
        int rs = 0;
        if (type == 0) {//请求
            if (num >= 1) {
                rs = 2;
            } else {
                rs = 1;
            }
        } else {//抽奖
            rs = 1;
            if (playerProxy.getRewardNum().size() <= 0 && playerProxy.getLoginDayNum() >= ActorDefine.LOGIN_DAY_NUM) {
                List<M2.loginLottery> lotteryInfo = playerProxy.loginLottery(reward);
                if (lotteryInfo.size() > 0) {
                    builder.addAllRewardInfo(lotteryInfo);

                } else {
                    rs = ErrorCodeDefine.M20016_1;
                }
            } else {
                rs = ErrorCodeDefine.M20016_2;
            }

        }
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20016, builder.build());
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        if (reward.soldierMap.size() > 0) {
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
        }
        //阵型
        sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.CheckBaseDefendFormation());
    }

    //提示
    public void OnTriggerNet20200Event(Request request) {
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M2.M20200.S2C.Builder builder = M2.M20200.S2C.newBuilder();
        builder.addAllTipInfos(playerProxy.getTipInfos());
        if (request != null) {
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20200, builder.build());
        } else {
            pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20200, builder.build());
            sendPushNetMsgToClient();
        }
    }

    //通知
    public void OnTriggerNet20300Event(Request request) {
        M2.M20300.C2S c2S = request.getValue();
        List<Integer> list = c2S.getRemainlistList();
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M2.M20300.S2C.Builder builder = M2.M20300.S2C.newBuilder();
        builder.setRs(0);
        Set<Integer> setlist = new HashSet<Integer>();
        setlist.addAll(list);
        playerProxy.setRemainList(setlist);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20300, builder.build());
    }


    public void OnTriggerNet20301Event(Request request) {
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M2.M20301.S2C.Builder builder = M2.M20301.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
        int rs = playerProxy.getNewGift(reward);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20301, builder.build());
        if (rs >= 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
            sendFuntctionLog(FunctionIdDefine.GET_NEW_GIFT_FUNCTION_ID);
        }
    }


    public void OnTriggerNet20400Event(Request request) {
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M2.M20400.S2C.Builder builder = M2.M20400.S2C.newBuilder();
        List<Long> playerIds = playerProxy.getlaterlist();
        List<Long> set = new ArrayList<>();
        for (int i = 0; i <= playerIds.size() - 1; i++) {
            if (set.size() <= ChatAndMailDefine.LATERMAX) {
                set.add(playerIds.get(i));
            }
        }
        set.remove(playerProxy.getPlayerId());
        sendServiceMsg(ActorDefine.MAIL_SERVICE_NAME, new GameMsg.getlaterInfo(set, builder));
    }

    private void tellMsgToArmygroupNode(Object mess, Long id) {
        context().actorSelection("../../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE + id).tell(mess, self());
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
