package com.znl.modules.task;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseLog;
import com.znl.base.BasicModule;
import com.znl.core.Notice;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.TaskActivityLog;
import com.znl.log.TaskDaliyLog;
import com.znl.log.TaskGetLog;
import com.znl.log.admin.tbllog_box;
import com.znl.log.admin.tbllog_task;
import com.znl.msg.GameMsg;
import com.znl.proto.Common;
import com.znl.proto.M19;
import com.znl.proto.M2;
import com.znl.proto.M9;
import com.znl.proxy.*;
import com.znl.server.DbServer;import com.znl.utils.GameUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/11/6.
 */
public class TaskModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<TaskModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public TaskModule create() throws Exception {
                return new TaskModule(gameProxy);
            }
        });
    }

    public TaskModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M19);
      /*  PlayerReward reward=new PlayerReward();
        TaskProxy taskProxy=getProxy(ActorDefine.TASK_PROXY_NAME);
        M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_WINRESOURCE_LV, 0, reward);
        sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));*/
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.RefeshTask) {
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            M19.M190000.S2C.Builder builder = M19.M190000.S2C.newBuilder();
            builder.addAllTaskInfos(taskProxy.getTaskInfoBytableTypedel(TaskDefine.TABLE_TASK_DAY));
            taskProxy.initTask();
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            builder.addAllTaskInfos(taskProxy.getTaskInfoBytableType(TaskDefine.TABLE_TASK_ACTIVITY_DAY));
            builder.addAllTaskInfos(taskProxy.getTaskInfoBytableType(TaskDefine.TABLE_TASK_DAY));
            int dayilNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
            int dayactivity = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
            builder.setHasGetMaxId(dayactivity);
            builder.setDayActivityId(dayactivity + 1);
            builder.setDayliynum(dayilNum);
            builder.setRs(0);
            List<Integer> list=new ArrayList<Integer>();
            list.add(PlayerPowerDefine.POWER_active);
            pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20002,sendDifferent(list));
            pushNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190000, builder.build());
            sendPushNetMsgToClient();
        }
        if (object instanceof GameMsg.RefeshTaskUpdate) {
            M19.M190000.S2C.Builder builder = ((GameMsg.RefeshTaskUpdate) object).build();
            PlayerReward reward = ((GameMsg.RefeshTaskUpdate) object).reward();
            if (builder != null) {
                TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                TaskProxy taskProxy=getProxy(ActorDefine.TASK_PROXY_NAME);
                int dayilNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
                int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
                builder.setHasGetMaxId(num);
                builder.setDayActivityId(taskProxy.getActivityId());
                builder.setDayliynum(dayilNum);
                pushNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190000, builder.build());
                List<Integer> powerlist=new ArrayList<Integer>();
                powerlist.add(PlayerPowerDefine.POWER_active);
                sendPowerDiff(powerlist);
                sendPushNetMsgToClient();
            }
            if (reward.addItemMap.size() > 0 || reward.counsellorMap.size() > 0 || reward.generalList.size() > 0 || reward.generalMap.size() > 0 && reward.ordanceFragmentMap.size() > 0 || reward.ordanceList.size() > 0 || reward.ordanceMap.size() > 0 || reward.soldierMap.size() > 0) {
                RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                M2.M20007.S2C builder27 = rewardProxy.getRewardClientInfo(reward);
                pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, builder27);
                sendPushNetMsgToClient();
            }
            if(reward.addPowerMap.size()>0) {
                checkPlayerPowerValues(getPlayerPowerValues());
            }
        } if (object instanceof GameMsg.getOrePoint) {
            PerformTasksProxy performTasksProxy=getProxy(ActorDefine.PERFORMTASKS_PROXY_NAME);
            String accoutName=((GameMsg.getOrePoint) object).myaccontName();
            SimplePlayer simplePlayer=((GameMsg.getOrePoint) object).simple();
            List<String> list=performTasksProxy.getDigList();
            sendMsgToOtherPlayerModule(ActorDefine.MAP_MODULE_NAME,accoutName,new GameMsg.getOrePointback(simplePlayer,list));
        }

    }


    private void OnTriggerNet190000Event(Request request) {
        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
        taskProxy.initTaskAll();
        sendNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190000, taskProxy.getTaskInfoToClient().build());
    }

    private void OnTriggerNet190001Event(Request request) {
        M19.M190001.C2S c2S = request.getValue();
        int tableType = c2S.getTableType();
        int typeId = c2S.getTypeId();
        M19.M190001.S2C.Builder builder = M19.M190001.S2C.newBuilder();
        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = taskProxy.getSessionReward(tableType, typeId, reward, builder);
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int dayilNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
        builder.setDayliynum(dayilNum);
        builder.setRs(rs);
        builder.setTableType(tableType);
        sendNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190001, builder.build());
        if (rs == 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C msg = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, msg);
            TaskGetLog taskGetLog = new TaskGetLog(typeId, tableType);
            sendLog(taskGetLog);

            /**
             * tbllog_task 提交任务
             */
            taskCommitLog(typeId);
            //阵型
            sendModuleMsg(ActorDefine.TROOP_MODULE_NAME,new GameMsg.CheckBaseDefendFormation());
            if (reward.soldierMap.size() > 0){
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
            }
        }
    }


    private void OnTriggerNet190002Event(Request request) {
        M19.M190002.C2S c2S = request.getValue();
        int type = c2S.getType();
        int typeId = c2S.getTypeId();
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        M19.M190002.S2C.Builder builder = M19.M190002.S2C.newBuilder();
        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = taskProxy.dayTaskOperate(type, typeId, builder, reward);
        builder.setRs(rs);
        builder.setType(type);
        int dayilNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
        builder.setDayliynum(dayilNum);
        sendNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190002, builder.build());
        if (rs == 0 && type == 5) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C message = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message);
            TaskDaliyLog taskDaliyLog = new TaskDaliyLog(typeId, type);
            sendLog(taskDaliyLog);
            sendFuntctionLog(FunctionIdDefine.DAY_TASK_OPERATE_FUNCTION_ID);
        }
        if(rs == 0){
            /**
             * tbllog_task 提交任务
             */
            PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            PlayerCache cache = player.getPlayerCache();
            tbllog_task tasklog = new tbllog_task();
            tasklog.setPlatform(cache.getPlat_name());
            tasklog.setRole_id(player.getPlayerId());
            tasklog.setAccount_name(player.getAccountName());
            tasklog.setDim_prof(0);
            tasklog.setDim_level(player.getLevel());
            tasklog.setTask_id(typeId);
           if(type == 2){
                tasklog.setStatus(3);
            }else if(type == 0 || type ==5){
                tasklog.setStatus(2);
            }else{
                tasklog.setStatus(1);
            }
            tasklog.setHappend_time(GameUtils.getServerTime());
            sendLog(tasklog);
        }
    }

    private void OnTriggerNet190003Event(Request request) {
        M19.M190003.S2C.Builder builder = M19.M190003.S2C.newBuilder();
        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        TaskActivityLog baseLog = new TaskActivityLog();
        int rs = taskProxy.getDayActivity(reward, baseLog);
        builder.setRs(rs);
        builder.setDayActivityId(taskProxy.getActivityId());
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        builder.setHasGetMaxId(num);
        sendNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190003, builder.build());
        if (rs == 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C message = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, message);
            sendLog(baseLog);
        }
    }

    /**
     * tbllog_task 提交任务
     */
    public void taskCommitLog(int taskId){
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_task tasklog = new tbllog_task();
        tasklog.setPlatform(cache.getPlat_name());
        tasklog.setRole_id(player.getPlayerId());
        tasklog.setAccount_name(player.getAccountName());
        tasklog.setDim_prof(0);
        tasklog.setDim_level(player.getLevel());
        tasklog.setTask_id(taskId);
        tasklog.setStatus(4);
        tasklog.setHappend_time(GameUtils.getServerTime());
        sendLog(tasklog);
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
