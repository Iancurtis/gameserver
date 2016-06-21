package com.znl.modules.system;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseLog;
import com.znl.base.BasicModule;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.framework.http.HttpMessage;
import com.znl.framework.socket.Request;
import com.znl.log.admin.tbllog_event;
import com.znl.msg.GameMsg;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.template.ChargeTemplate;
import com.znl.template.MailTemplate;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/29.
 */
public class SystemModule extends BasicModule {
    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<SystemModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public SystemModule create() throws Exception {
                return new SystemModule(gameProxy);
            }
        });
    }

    public SystemModule(GameProxy gameProxy) {
        setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M3);
//        initTimerSet();
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.AutoSavePlayer) {
            autoSavePlayer();
        } else if (object instanceof GameMsg.SystemTimer) {
            System.out.println("======SystemTime==============================!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            OnTriggerNet30000Event(null);
        } else if (object instanceof GameMsg.EachHourNotice) {
            eachHourHandle();
        } else if (object instanceof GameMsg.EachMinuteNotice) {
            eachMiuteandle();
        } else if (object instanceof GameMsg.BuildTimer) {
            int cn = ((GameMsg.BuildTimer) object).cn();
            int cm = ((GameMsg.BuildTimer) object).cmd();
            Object obj = ((GameMsg.BuildTimer) object).obj();
            int buildType = ((GameMsg.BuildTimer) object).buildType();
            int index = ((GameMsg.BuildTimer) object).index();
            List<Integer> powerlist = ((GameMsg.BuildTimer) object).powerlist();
            sendBuildTimer(cn, cm, obj, powerlist, buildType, index);
        } else if (object instanceof GameMsg.ChargeToPlayer) {
            GameMsg.ChargeToPlayer mess = (GameMsg.ChargeToPlayer) object;
            chargeHandle(mess);
            GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, new GameMsg.refreshengry());
        } else if (object instanceof GameMsg.notitySomeOneCharge) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            List<JSONObject> jsonObjects = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT, "conditiontype", 119);
            for (JSONObject jsonObject : jsonObjects) {
                ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
                int lv = jsonObject.getInt("condition1");
                int num = activityProxy.getVipMap(lv);
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_VIPPEOPLE_NUM, lv, playerProxy, num);
            }
        } else if (object instanceof GameMsg.RefeshItemBuff) {
            //生效的道具buff
            ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
            M9.M90003.S2C.Builder m9s2c = M9.M90003.S2C.newBuilder();
            m9s2c.addAllItemBuffInfo(itemBuffProxy.sendItemBuffInfoToClient());
            m9s2c.setRs(0);
            pushNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90003, m9s2c.build());
            sendPushNetMsgToClient();
        } else if (object instanceof GameMsg.CheckAllTimerAndSend30000) {
            System.out.println("======CheckAllTimerAndSend30000==================!!!!!!!!!!!!!!!!!!!!!!!!");
            checkAllTimer(2);
            OnTriggerNet30000Event(null);
        } else if (object instanceof GameMsg.EachSecondNotice) {
            everySecondCheckTimer();
        } else if (object instanceof GameMsg.RefTimerSet) {
            initTimerSet(((GameMsg.RefTimerSet) object).initType());
        }
    }

    private void initTimerSet(int initType) {
        checkAllTimer(initType);
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        timerdbProxy.initTimerSet();
        if (TimerDefine.CHANGE_TIMER_INIT_TYPE_BUY_TIME == initType) {
            OnTriggerNet30000Event(null);
        }
    }


    /***
     * 每秒执行一次检查是否有定时器到点了
     ***/
    private void everySecondCheckTimer() {
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (timerdbProxy.checkTimerEverySecond()) {
            List<M3.TimeInfo> infos = checkAllTimer(2);
//            if (infos.size() > 0){
//                M3.M30000.S2C.Builder builder = M3.M30000.S2C.newBuilder();
//                builder.addAllTimeInfos(infos);
//                builder.setType(1);
//                pushNetMsg(ActorDefine.SYSTEM_MODULE_ID,ProtocolModuleDefine.NET_M3_C30000,builder.build());
//                sendPushNetMsgToClient();
//            }
            //暂时先把所有的都发过去前端吧

            OnTriggerNet30000Event(null);
        }
        //活动相关
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        ConcurrentHashSet<Integer> delActivitySet = activityProxy.futureDeleteActivity;
        if (!delActivitySet.isEmpty()) {
            M23.M230008.S2C.Builder deleteBuilder = M23.M230008.S2C.newBuilder();
            for (int id : delActivitySet) {
                deleteBuilder.addActivityIds(id);
                delActivitySet.remove(id);
            }
            //通知前端删除
            pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230008, deleteBuilder.build());
            sendPushNetMsgToClient();
        }
        //限时活动开启检测
        if (!activityProxy.futureUpdateLimitActivityList.isEmpty()) {
            //通知前端更新
            for (M23.M230009.S2C upAct : activityProxy.futureUpdateLimitActivityList) {
                pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230009, upAct);
                activityProxy.futureUpdateLimitActivityList.remove(upAct);
            }
            sendPushNetMsgToClient();
        }
       /* ConcurrentLinkedQueue<M23.M230007.S2C>updateActivitys= activityProxy.futureUpdateActivityList;
        if(updateActivitys.size()>0){
            //通知前端更新
            for(M23.M230007.S2C upAct:updateActivitys){
                pushNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230007, upAct);
                updateActivitys.remove(upAct);
            }
            sendPushNetMsgToClient();
        }

*/
    }


    private void chargeHandle(GameMsg.ChargeToPlayer mess) {
        ChargeTemplate chargeTemplate = mess.chargeTemplate();
        HttpMessage message = mess.http();
        ActorRef actorRef = mess.actor();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerReward reward = new PlayerReward();//奖励
        String res = playerProxy.chargeToPlayer(chargeTemplate.getChargeValue(), chargeTemplate.getChargeType(), chargeTemplate.getOrderId(), reward);
        GameMsg.ChargeToPlayerDone done = new GameMsg.ChargeToPlayerDone(message, res);
        sender().tell(done, self());
        autoSavePlayer();
        //TODO 各种数值推送
        List<Integer> powers = new ArrayList<>();
        powers.add(PlayerPowerDefine.POWER_gold);
        powers.add(PlayerPowerDefine.POWER_vipExp);
        powers.add(PlayerPowerDefine.POWER_vipLevel);
        pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, sendDifferent(powers));
        //TODO 充值邮件发送
        if (res.indexOf("成功") > -1) {
            MailTemplate template = new MailTemplate("充值成功", "祝贺你已经成功充值！", 0, "系统邮件", ChatAndMailDefine.MAIL_TYPE_INBOX);
            Set<Long> ids = new HashSet<>();
            ids.add(playerProxy.getPlayerId());
            GameMsg.SendMail mail = new GameMsg.SendMail(ids, template, "系统邮件", 0l);
            sendServiceMsg(ActorDefine.MAIL_SERVICE_NAME, mail);
            M3.M30102.S2C.Builder builder = M3.M30102.S2C.newBuilder();
            builder.setAmount(chargeTemplate.getChargeValue());
            pushNetMsg(ActorDefine.SYSTEM_MODULE_ID, ProtocolModuleDefine.NET_M3_C30102, builder.build());
        }
//        //推送VIP加成属性
//        GameMsg.ReshBuildings rbmsg = new GameMsg.ReshBuildings();
//        sendModuleMsg(ActorDefine.BUILD_MODULE_NAME, rbmsg);
        if (reward.haveReward()) {
            //如果有奖励发送前端提示
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C rewardbuild = rewardProxy.getRewardClientInfo(reward);
            pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardbuild);
        }
        sendPushNetMsgToClient();
    }

    private void sendBuildTimer(int cn, int cm, Object object, List<Integer> powerlist, int buildType, int index) {
        M2.M20002.S2C different = sendDifferent(powerlist);
        pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, different);
        sendPushNetMsgToClient();
        if (cn == ProtocolModuleDefine.NET_M10 && ProtocolModuleDefine.NET_M10_C100001 == cm) {
            OnTriggerNet30000Event(null);
            try {
                M10.M100001.S2C.Builder builder = (M10.M100001.S2C.Builder) object;
                ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                builder.setBuildingInfo(resFunBuildProxy.getBuildingInfo(buildType, index));
                pushNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100001, ((M10.M100001.S2C.Builder) object).build());
            } catch (Exception e) {
                e.printStackTrace();
            }
            sendPushNetMsgToClient();
        } else if (ProtocolModuleDefine.NET_M10 == cn && ProtocolModuleDefine.NET_M10_C100006 == cm) {
            OnTriggerNet30000Event(null);
            pushNetMsg(ProtocolModuleDefine.NET_M10, ProtocolModuleDefine.NET_M10_C100006, ((M10.M100006.S2C.Builder) object).build());
            sendPushNetMsgToClient();

        }

    }

    private void eachMiuteandle() {
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int size = playerProxy.getTipInfos().size();
        activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_ENERY_EVERYDAY, 0, playerProxy, 0);
        int newsize = playerProxy.getTipInfos().size();
        if (size != newsize) {
            GameMsg.RefrshTip tipmsg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, tipmsg);
        }
        checkAllTimer(2);
    }

    private void eachHourHandle() {
        System.out.println("=============SystemModule接收到每小时定时器");
        Calendar c = Calendar.getInstance();
        c.setTime(GameUtils.getServerDate());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //   activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_ENERY_EVERYDAY, 0, playerProxy, 0);
        switch (hour) {
            case TimerDefine.TIMER_REFRESH_ZERO: {
//                GameMsg.RefeshTask taskmessge = new GameMsg.RefeshTask();
//                sendModuleMsg(ActorDefine.TASK_MODULE_NAME, taskmessge);
//                break;
            }
            case TimerDefine.TIMER_REFRESH_FOUR: {
                activityProxy.checkActivityRefresh(playerProxy);
                boolean falg = playerProxy.loginDayNum();
                if (falg) {
                    GameMsg.ReshEveryDay reshmsg = new GameMsg.ReshEveryDay();
                    sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, reshmsg);
                }
                GameMsg.RefeshTask taskmessge = new GameMsg.RefeshTask();
                sendModuleMsg(ActorDefine.TASK_MODULE_NAME, taskmessge);

                GameMsg.RefreshActivity activityMsg = new GameMsg.RefreshActivity();
                sendModuleMsg(ActorDefine.ACTIVITY_MODULE_NAME, activityMsg);

                GameMsg.RefreshLaba labamsg = new GameMsg.RefreshLaba();
                sendModuleMsg(ActorDefine.ACTIVITY_MODULE_NAME, labamsg);

                GameMsg.RefreshBlessState blessState = new GameMsg.RefreshBlessState();
                sendModuleMsg(ActorDefine.FRIEND_MODULE_NAME, blessState);

                sendTimeFourNoticity(hour);
                break;
            }
        }
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        timerdbProxy.reFreshTimer();
        OnTriggerNet30000Event(null);
        sendPushNetMsgToClient();
        GameMsg.RefrshTip tipmsg = new GameMsg.RefrshTip();
        sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, tipmsg);
    }

    private void autoSavePlayer() {

        this.saveAllProxy();
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        timerdbProxy.saveTimers();
        ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        resFunBuildProxy.saveResFunBuildings();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.setbuildLevelTime(timerdbProxy.getLevelUpEndTime());
        playerProxy.savePlayer();
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        soldierProxy.saveSoldier();
        ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
        itemProxy.saveItems();
        //TODO 改变开关
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        dungeoProxy.saveDungeo();
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        activityProxy.saveActivity();
        TechnologyProxy technologyProxy = getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
        technologyProxy.saveTechnology();
        SkillProxy skillProxy = getProxy(ActorDefine.SKILL_PROXY_NAME);
        skillProxy.saveSkill();
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        equipProxy.saveEquips();
        OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        ordnancePieceProxy.saveOrdnancePieces();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        ordnanceProxy.saveOrdnances();
        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
        taskProxy.saveTasks();
    }


    /***
     * 检查所有的定时器
     ****/
    private List<M3.TimeInfo> checkAllTimer(int type) {
        SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        List<M3.TimeInfo> infoList = new ArrayList<>();
        PlayerReward reward = new PlayerReward();
        List<PlayerTask> playerTasks = new ArrayList<>();
        List<BaseLog> baseLogs = new ArrayList<>();
        Set<Integer> powerList = new HashSet<>();
        checkMop();
        systemProxy.checkAllTimer(infoList, reward, playerTasks, baseLogs, powerList);
        if (reward.haveReward() || powerList.size() > 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C message = rewardProxy.getRewardClientInfo(reward);
            M2.M20002.S2C message20002 = sendDifferent(new ArrayList<>(powerList));
            if (type == 1) {
                if (reward.haveReward() == true) {
                    sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message);
                }
                if (powerList.size() > 0) {
                    sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, message20002);
                }
            } else {
                if (reward.haveReward() == true) {
                    pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, message);
                }
                if (powerList.size() > 0) {
                    pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, message20002);
                }
                sendPushNetMsgToClient();
            }
            if (reward.soldierMap.size() > 0) {
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            }

        }
        //判断是否有科技升级
        boolean falg = false;
        for (PlayerTask pt : playerTasks) {
            if (pt.taskType == TaskDefine.TASK_TYPE_SCIENCELV_TIMES) {
                falg = true;
            }
        }
        if (falg) {
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
        }
        dueInfo(infoList, playerTasks, type == 0);
        //推送刷新buff
        ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        if (itemBuffProxy.isPushBuff == true) {
            GameMsg.RefeshItemBuff msg = new GameMsg.RefeshItemBuff();
            sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, msg);
        }
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (GameUtils.getServerDate().getTime() - playerProxy.getProtectOverDate() <= TimerDefine.RONCUOONEMINUTE) {
            sendRrfrshProtect();
        }
        return infoList;
    }

    private void OnTriggerNet30000Event(Request request) {
        int init = 0;
        SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        //2016/04/11 修改定时器机制（只做发送，不管刷新了）
//        List<M3.TimeInfo> infoList = new ArrayList<M3.TimeInfo>();
//        PlayerReward reward = new PlayerReward();
//        List<PlayerTask> playerTasks = new ArrayList<PlayerTask>();
//        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
//        checkMop();
//        M3.M30000.S2C.Builder builder = systemProxy.getTimerNotify(infoList, reward, playerTasks, baseLogs, init);
//        builder.setType(0);
//        if (infoList.size() > 0) {
//            systemProxy.getTimerNotify(infoList, reward, playerTasks, baseLogs, init);
//        }
//        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
//        M2.M20009.S2C message20009 = rewardProxy.getRewardInfoClientMsg(reward);
//        if (request != null) {
//            sendNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, builder.build());
//            if (reward.addItemMap.size() > 0 || reward.generalList.size() > 0 || reward.ordanceList.size() > 0 || reward.soldierMap.size() > 0) {
//                sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20009, message20009);
//            }
//        } else {
//            pushNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, builder.build());
//            if (reward.addItemMap.size() > 0 || reward.generalList.size() > 0 || reward.ordanceList.size() > 0 || reward.soldierMap.size() > 0) {
//                pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20009, message20009);
//            }
//            sendPushNetMsgToClient();
//        }
//        M2.M20007.S2C message = rewardProxy.getRewardClientInfo(reward);
//        if (reward.soldierMap.size() > 0) {
//            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
//        }
//        sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message);
//        dueInfo(infoList, playerTasks);
//        //推送刷新buff
//        ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
//        if (itemBuffProxy.isPushBuff == true) {
//            GameMsg.RefeshItemBuff msg = new GameMsg.RefeshItemBuff();
//            sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, msg);
//        }
//        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
//        if (GameUtils.getServerDate().getTime() - playerProxy.getProtectOverDate() <= TimerDefine.RONCUOONEMINUTE) {
//            sendRrfrshProtect();
//        }

        if (request != null) {
            M3.M30000.C2S c2s = request.getValue();
            init = c2s.getIsInit();
            if (init > 0) {
                //进入初始化逻辑
                initTimerSet(TimerDefine.CHANGE_TIMER_INIT_TYPE_LOGIN);
                return;
            } else {
                M3.M30000.S2C.Builder builder = systemProxy.getTimerNotify(init);
                builder.setType(0);
                sendNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, builder.build());
            }
//            System.out.println("发一次30000");
        } else {
            M3.M30000.S2C.Builder builder = systemProxy.getTimerNotify(init);
            builder.setType(0);
            pushNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, builder.build());
            sendPushNetMsgToClient();
//            System.out.println("推送一次30000");
        }
    }

    public void sendRrfrshProtect() {
        M8.M80016.S2C.Builder builder = M8.M80016.S2C.newBuilder();
        builder.setRs(0);
        pushNetMsg(ProtocolModuleDefine.NET_M8, ProtocolModuleDefine.NET_M8_C80016, builder.build());
        sendPushNetMsgToClient();
    }

    //扫荡校验
    public void checkMop() {
        TimerdbProxy timerProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttime = timerProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        int num = timerProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        long time = lasttime - GameUtils.getServerDate().getTime();
        if (num >= 1) {
            long timenum = time / 30 / 1000;
            if (time < 0) {
                timenum = 0;
            }
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int maxid = playerProxy.getLimitChangeMaxId();
            playerProxy.setLimitChangeNowId((int) (maxid - timenum));
        }
        if (num != 0 && time <= 0) {
            DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            dungeoProxy.getMopReward(reward);
            List<Integer[]> list = dungeoProxy.rewardtoList(reward);
            MailTemplate template = new MailTemplate("扫荡奖励邮件", "扫荡奖励邮件", 0, "系统邮件", ChatAndMailDefine.MAIL_TYPE_SYSTEM);
            template.setAttachments(list);
            sendModuleMsg(ActorDefine.MAIL_MODULE_NAME, new GameMsg.ReceiveMailNotice(template));
            timerProxy.setNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, 0);
        }
        if (time <= 0) {
            time = 0;
        }
        timerProxy.setLesTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, (int) time / 1000);
    }

    private void OnTriggerNet30100Event(Request request) {
        SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Set<Long> ids = playerProxy.getClientCacheIds();
        M3.M30100.S2C s2c = systemProxy.getClientCacheInfos(ids);
        sendNetMsg(ActorDefine.SYSTEM_MODULE_ID, ProtocolModuleDefine.NET_M3_C30100, s2c);
    }

    private void OnTriggerNet30101Event(Request request) {
        M3.M30101.C2S c2s = request.getValue();
        SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        systemProxy.updateClientCache(c2s.getCacheInfo());

        sendNetMsg(ActorDefine.SYSTEM_MODULE_ID, ProtocolModuleDefine.NET_M3_C30101, M3.M30101.S2C.newBuilder().build());
    }

    private void send30000(M3.M30000.S2C.Builder builder, List<M3.TimeInfo> infoList) {
        builder.setType(0);
        pushNetMsg(ProtocolModuleDefine.NET_M3, ProtocolModuleDefine.NET_M3_C30000, builder.build());
        dueInfo(infoList, new ArrayList<PlayerTask>(), false);

        sendPushNetMsgToClient();
    }

    private void addTimeInfoToList(List<List<Object>> buillist, M3.TimeInfo ti) {
        for (List<Object> list : buillist) {
            int ob0 = (Integer) list.get(0);
            int ob1 = (Integer) list.get(1);
            int ob2 = (Integer) list.get(2);
            if (ti.getBigtype() == TimerDefine.BUILD_CREATE || ti.getBigtype() == TimerDefine.BUILDING_LEVEL_UP) {
                if (ob0 == ti.getSmalltype() && ob1 == ti.getSmalltype() && ob2 == ti.getSmalltype()) {
                    return;
                }
            }
        }
        List<Object> list = new ArrayList<Object>();
        list.add(ti.getSmalltype());
        list.add(ti.getOthertype());
        list.add(ti.getBigtype());
        buillist.add(list);
    }

    private void dueInfo(List<M3.TimeInfo> infoList, List<PlayerTask> playerTasks, boolean firstInit) {
        List<List<Object>> buillist = new ArrayList<List<Object>>();
        for (M3.TimeInfo ti : infoList) {
            if (ti.getBigtype() == TimerDefine.BUILD_CREATE) {
                addTimeInfoToList(buillist, ti);
            } else if (ti.getBigtype() == TimerDefine.BUILDING_LEVEL_UP) {
                addTimeInfoToList(buillist, ti);
            }
        }
        SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        if (systemProxy.isLeveup()) {
            systemProxy.addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDING_LV, 1, 0);
            systemProxy.addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDING_NUM, 1, 0);
            systemProxy.addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDLEVEUP_TIMES, 1, 0);
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(playerTasks, reward);
            if (builder19 != null) {
                sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, new GameMsg.RefrshTip());
            }
            systemProxy.setLeveup(false);
        }
        if (playerTasks.size() > 0) {
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(playerTasks, reward);
            if (builder19 != null) {
                sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, new GameMsg.RefrshTip());
            }
        }
        if (infoList.size() > 0 || firstInit) {
            if (firstInit) {
                M3.M30000.S2C.Builder builder = systemProxy.getTimerNotify(1);
                builder.setType(1);
                GameMsg.BuildInfo message = new GameMsg.BuildInfo(buillist, firstInit, builder.build());
                sendModuleMsg(ActorDefine.BUILD_MODULE_NAME, message);
            } else {
                GameMsg.BuildInfo message = new GameMsg.BuildInfo(buillist, firstInit, null);
                sendModuleMsg(ActorDefine.BUILD_MODULE_NAME, message);
            }
            if (playerTasks.size() > 0) {
                updateMySimplePlayerData();
                for (PlayerTask task : playerTasks) {
                    if (task.taskType == TaskDefine.TASK_TYPE_CREATESODIER_NUM) {
                        //阵型
                        sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.CheckBaseDefendFormation());
                    }
                }
            }
        }


    }

    /**
     * 触发事件
     *
     * @param request
     */
    private void OnTriggerNet30001Event(Request request) {
        M3.M30001.C2S c2s = request.getValue();
        int eventId = c2s.getEventId();
        eventLog(eventId);
    }

    /**
     * 整点刷新推送通知
     */
    private void sendTimeFourNoticity(int hour){
        M3.M30103.S2C.Builder buider =M3.M30103.S2C.newBuilder();
        buider.setBlessStateLog(hour);
        pushNetMsg(ProtocolModuleDefine.NET_M3,ProtocolModuleDefine.NET_M3_C30103,buider.build());
        sendPushNetMsgToClient();
    }

    /**
     * 事件触发日志
     *
     * @param eventId
     */
    public void eventLog(int eventId) {
        PlayerProxy ppxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = ppxy.getPlayerCache();
        tbllog_event eventlog = new tbllog_event();
        eventlog.setPlatform(cache.getPlat_name());
        eventlog.setRole_id(ppxy.getPlayerId());
        eventlog.setAccount_name(ppxy.getAccountName());
        eventlog.setEvent_id(eventId);
        eventlog.setUser_ip(cache.getUser_ip());
        eventlog.setDid(cache.getImei());
        eventlog.setGame_version(cache.getGame_version());
        eventlog.setOs(cache.getOsName());
        eventlog.setOs_version(cache.getModel());
        eventlog.setDevice(cache.getModel());
        eventlog.setDevice_type(cache.getModel());
        eventlog.setScreen(cache.getScreen());
        eventlog.setMno(cache.getOperators());
        eventlog.setNm(cache.getNet());
        eventlog.setHappend_time(GameUtils.getServerTime());
        sendLog(eventlog);
    }

//    private void sendTimer(int cn, int cmd, Object obj, List<Integer> powerlist) {
//        SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
//        List<M3.TimeInfo> m3info = new ArrayList<M3.TimeInfo>();
//        PlayerReward reward = new PlayerReward();
//        GameMsg.BuildTimer message = new GameMsg.BuildTimer(systemProxy.getTimerNotify(m3info, reward, new ArrayList<PlayerTask>(), new ArrayList<BaseLog>(), 0), cn, cmd, obj, powerlist);
//        sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, message);
//    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }

}
