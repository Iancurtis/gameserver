package com.znl.modules.friend;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.admin.tbllog_user_friend;
import com.znl.msg.GameMsg;
import com.znl.proto.M17;
import com.znl.proxy.*;
import com.znl.service.PlayerService;
import com.znl.template.MailTemplate;
import com.znl.utils.DateUtil;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 好友模块
 * Created by Administrator on 2015/12/7.
 */
public class FriendModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<FriendModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public FriendModule create() throws Exception {
                return new FriendModule(gameProxy);
            }
        });
    }

    public FriendModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M10);
    }

    private final String FRIEND_ADD_CMD = "AddFriend";
    private final String SEARCH_ROLE_CMD = "SearchRole";
    private final String ACCEPT_BLESS_CMD = "AcceptBless";

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.WatchFriendInfoListBack) {
            onWatchFriendInfoListBack((GameMsg.WatchFriendInfoListBack) anyRef);
        } else if (anyRef instanceof GameMsg.GetPlayerSimpleInfoSuccess) {
            onGetPlayerSimpleInfoSuccess((GameMsg.GetPlayerSimpleInfoSuccess) anyRef);
        } else if (anyRef instanceof GameMsg.AcceptFriendBless) {
            onAcceptFriendBless((GameMsg.AcceptFriendBless) anyRef);
        } else if (anyRef instanceof GameMsg.RefreshBlessState) {
            onResetDayNum();
        }
    }


    //接受到别人的祝福
    private void onAcceptFriendBless(GameMsg.AcceptFriendBless msg) {
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);

        //小于最大的领取祝福数，才添加
        if (playerProxy.getBeBlessNum() < FriendDefine.MAX_DAY_GET_BLESS_NUM) {
            playerProxy.acceptBless(msg.playerId());
            // 查看该玩家信息，然后通知客户端
            this.sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new GameMsg.GetPlayerSimpleInfo(msg.playerId(), ACCEPT_BLESS_CMD));
        }
    }

    private void onResetDayNum() {
      /*  TimerdbProxy timerdbProxy = this.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        timerdbProxy.addTimer(TimerDefine.FRIEND_DAY_BLESS,
                0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        timerdbProxy.addTimer(TimerDefine.FRIEND_DAY_GET_BLESS,
                0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        long id = timerdbProxy.addTimer(TimerDefine.FRIEND_DAY_BE_BLESS,
                0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        if (id == 0) {
            id = timerdbProxy.getTimerId(TimerDefine.FRIEND_DAY_BLESS, 0, 0);
        }
        playerProxy.setBlessTimerId(id);
        long curLongTime = GameUtils.getServerTime() * 1000L;
        long lastTime = timerdbProxy.getLastOperatinTime(TimerDefine.FRIEND_DAY_BE_BLESS, 0, 0);
        if (DateUtil.isCanGet(curLongTime, lastTime, TimerDefine.TIMER_REFRESH_FOUR)) {  //已经不是在同一天了
            playerProxy.clearBeBlessByGet();//清除那些已经领取的被祝福列表
            playerProxy.clearBlessSet();
            timerdbProxy.setNum(TimerDefine.FRIEND_DAY_BLESS, 0, 0,0);
        }


        timerdbProxy.setLastOperatinTime(TimerDefine.FRIEND_DAY_BE_BLESS, 0, 0, curLongTime);

        int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_BLESS, 0, 0);


        if (num == 0) { //没有使用，直接重置
            playerProxy.clearBlessSet();
        }

        int getNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_GET_BLESS, 0, 0);
        if (getNum == 0) {
            playerProxy.clearGetBless();
        }*/


    }

    private void onWatchFriendInfoListBack(GameMsg.WatchFriendInfoListBack msg) {

        //发送给客户端
        FriendProxy friendProxy = this.getProxy(ActorDefine.FRIEND_PROXY_NAME);
        List<M17.FriendInfo> list = friendProxy.getFriendInfoList(msg.friendList());
        List<M17.BlessInfo> blessInfoList = friendProxy.getBlessInfoList(msg.beBlessList());

        M17.M170000.S2C s2c = M17.M170000.S2C.newBuilder()
                .setRs(0)
                .addAllFriendInfos(list)
                .addAllBlessInfos(blessInfoList).build();

        pushNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170000, s2c);
        sendPushNetMsgToClient();
        sendFuntctionLog(FunctionIdDefine.GET_FRIENDS_INFOS_LIST_FUNCTION_ID);
    }

    //通过获取到的玩家信息来推送给对应的协议
    private void onGetPlayerSimpleInfoSuccess(GameMsg.GetPlayerSimpleInfoSuccess msg) {
        if (msg.cmd().equals(FRIEND_ADD_CMD)) {
            onAddFriend(msg.simplePlayer());
        } else if (msg.cmd().equals(SEARCH_ROLE_CMD)) {
            onSearchRole(msg.simplePlayer());
        } else if (msg.cmd().equals(ACCEPT_BLESS_CMD)) {
            onAcceptBless(msg.simplePlayer());
        }
    }

    private void onAddFriend(SimplePlayer simplePlayer) {
        int rs = 0;
        if (simplePlayer == null) {
            rs = ErrorCodeDefine.M170001_1;    //该玩家不存在
        }

        M17.M170001.S2C.Builder s2cBuilder = M17.M170001.S2C.newBuilder();
        s2cBuilder.setRs(rs);

        if (rs == 0) {

            PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.addFriend(simplePlayer.getId());

            FriendProxy friendProxy = this.getProxy(ActorDefine.FRIEND_PROXY_NAME);
            if(simplePlayer!= null) {
                s2cBuilder.setFriendInfo(friendProxy.getFriendInfo(simplePlayer));
            }

            // 添加对方好友成功，这里可能需要邮件推送等
            String mailContent = String.format("%s已添加您为好友，打个招呼吧！", playerProxy.getPlayerName());
            MailTemplate template = new MailTemplate("添加好友", mailContent, playerProxy.getPlayerId(), playerProxy.getPlayerName(), ChatAndMailDefine.MAIL_TYPE_INBOX, playerProxy.getPlayerId());
            Set<Long> roleIdList = new HashSet<>();
            roleIdList.add(simplePlayer.getId());
            sendServiceMsg(ActorDefine.MAIL_SERVICE_NAME, new GameMsg.SendMail(roleIdList, template, template.getSenderName(), template.getSenderId()));

            /**
             * tbllog_user_friend 加好友
             */
            addFriendLog(simplePlayer.getId());
        }

        pushNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170001, s2cBuilder.build());
        sendPushNetMsgToClient();
        sendFuntctionLog(FunctionIdDefine.ADD_FRIENDS_FUNCTION_ID, simplePlayer.getId(), 0, 0);

    }

    private void onSearchRole(SimplePlayer simplePlayer) {
        int rs = 0;

        if (simplePlayer == null) {
            rs = ErrorCodeDefine.M170002_1;    //该玩家不存在
        }

        M17.M170002.S2C.Builder s2cBuilder = M17.M170002.S2C.newBuilder();
        s2cBuilder.setRs(rs);

        if (rs == 0) {

            FriendProxy friendProxy = this.getProxy(ActorDefine.FRIEND_PROXY_NAME);
            if(simplePlayer!=null) {
                s2cBuilder.setFriendInfo(friendProxy.getFriendInfo(simplePlayer));
            }
            sendFuntctionLog(FunctionIdDefine.SEARCH_ROLE_FUNCTION_ID, simplePlayer.getId(), 0, 0);
        }

        pushNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170002, s2cBuilder.build());
        sendPushNetMsgToClient();
    }

    //查看信息后，将被祝福的信息推送给客户端
    private void onAcceptBless(SimplePlayer simplePlayer) {
        if (simplePlayer == null) //玩家不存在，不理了
            return;

        M17.M170005.S2C.Builder s2cBuilder = M17.M170005.S2C.newBuilder();
        FriendProxy friendProxy = this.getProxy(ActorDefine.FRIEND_PROXY_NAME);
        s2cBuilder.addBlessInfos(friendProxy.getBlessInfo(simplePlayer));

        pushNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170005, s2cBuilder.build());
        sendPushNetMsgToClient();
    }


    //请求获取好友列表
    private void OnTriggerNet170000Event(Request request) {

        onResetDayNum();  //处理次数

        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Set<Long> friendIdSet = playerProxy.getFriendSet();
        Set<Long> beBlessIdSet = playerProxy.getBeBlessSet();

        //发送给客户端
        FriendProxy friendProxy = this.getProxy(ActorDefine.FRIEND_PROXY_NAME);
        List<SimplePlayer> friendList=PlayerService.onGetPlayerSimpleInfoList(friendIdSet,playerProxy.getAreaKey());
        List<SimplePlayer> beBlessList=PlayerService.onGetPlayerSimpleInfoList(beBlessIdSet,playerProxy.getAreaKey());
        List<M17.FriendInfo> list = friendProxy.getFriendInfoList(friendList);
        List<M17.BlessInfo> blessInfoList = friendProxy.getBlessInfoList(beBlessList);

        M17.M170000.S2C s2c = M17.M170000.S2C.newBuilder()
                .setRs(0)
                .addAllFriendInfos(list)
                .addAllBlessInfos(blessInfoList).build();

        sendNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170000, s2c);
        sendFuntctionLog(FunctionIdDefine.GET_FRIENDS_INFOS_LIST_FUNCTION_ID);
        //    this.sendServiceMsg(ActorDefine.FRIEND_SERVICE_NAME, new GameMsg.WatchFriendInfoList(friendIdSet, beBlessIdSet)); //查看自己的好友列表信息
    }

    //请求添加好友
    private void OnTriggerNet170001Event(Request request) {
        M17.M170001.C2S c2s = request.getValue();
        Long playerId = c2s.getPlayerId();  //查看信息 判断这个玩家是否存在

        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Long ownId = playerProxy.getPlayerId();

        int rs = 0;
        if (playerId.equals(ownId)) {  //加自己好友，返回错误码
            rs = ErrorCodeDefine.M170001_2;
        } else if (playerProxy.isFriend(playerId)) {
            rs = ErrorCodeDefine.M170001_3;
        } else if (playerProxy.friendNum() >= FriendDefine.MAX_FRIEND_NUM) {
            rs = ErrorCodeDefine.M170001_4;
        }


        if (rs == 0) {
            this.sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new GameMsg.GetPlayerSimpleInfo(playerId, FRIEND_ADD_CMD)); //请求数据，然后通知客户端
            sendFuntctionLog(FunctionIdDefine.ADD_FRIENDS_FUNCTION_ID, playerId, 0, 0);
        } else { //直接发送错误码给客户端

            M17.M170001.S2C.Builder s2cBuilder = M17.M170001.S2C.newBuilder();
            s2cBuilder.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170001, s2cBuilder.build());
            sendPushNetMsgToClient();
        }
    }

    //搜索玩家
    private void OnTriggerNet170002Event(Request request) {
        M17.M170002.C2S c2s = request.getValue();
        String roleName = c2s.getRoleName();


        this.sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new GameMsg.GetPlayerSimpleInfoByRoleName(roleName, SEARCH_ROLE_CMD));

    }

    //删除好友
    private void OnTriggerNet170003Event(Request request) {
        M17.M170003.C2S c2s = request.getValue();
        Long playerId = c2s.getPlayerId();

        M17.M170003.S2C.Builder s2cBuilder = M17.M170003.S2C.newBuilder();

        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);

        int rs = 0;
        if (!playerProxy.isFriend(playerId)) {
            rs = ErrorCodeDefine.M170003_1;
        }

        if (rs == 0) {
            playerProxy.removeFriend(playerId);
            s2cBuilder.setPlayerId(playerId);
            sendFuntctionLog(FunctionIdDefine.REMOVE_FRIENDS_FUNCTION_ID, playerId, 0, 0);
        }
        SimplePlayer simplePlayer = PlayerService.getSimplePlayer(playerId, playerProxy.getAreaKey());
        if (simplePlayer != null) {
            s2cBuilder.setName(simplePlayer.getName());
        }
        s2cBuilder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170003, s2cBuilder.build());
        sendPushNetMsgToClient();
    }

    //请求祝福
    private void OnTriggerNet170004Event(Request request) {
        M17.M170004.C2S c2s = request.getValue();
        List<Long> ids = c2s.getPlayerIdsList();
        //TimerdbProxy timerdbProxy = this.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Integer> rewardIds = new ArrayList<>();
        //boolean blessed =false;
        int i =0;
        for (Long playerId : ids){
            //int rs = 0;
            if (playerProxy.isBlessed(playerId)) {
                i++;
               /* rs = ErrorCodeDefine.M170004_1;
                M17.M170004.S2C.Builder s2cBuilder = M17.M170004.S2C.newBuilder();
                s2cBuilder.setRs(rs);
                sendNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170004, s2cBuilder.build());*/
            } else {
                //blessed = true;
                //一次使用次数
                playerProxy.addBlessPlayerId(playerId);
                //timerdbProxy.addNum(TimerDefine.FRIEND_DAY_BLESS, 0, 0, 1);
                playerProxy.getPlayer().setDaybless(playerProxy.getPlayer().getDaybless()+1);
                //int num = 0;//timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_BLESS, 0, 0);
                int num = playerProxy.getPlayer().getDaybless();
                if (num <= FriendDefine.MAX_DAY_BLESS_NUM) {
                        //TODO 这里有奖励 多个的话，有多个
                        JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.FRIEND_GIFT, "level", playerProxy.getLevel());
                        int getreward = json.getInt("blessreward");
                        rewardIds.add(getreward);
                    }
            }
        }
        //通知客户端
        if (rewardIds.size() > 0) {
            blessRewardHandler(rewardIds);
        }
        M17.M170004.S2C.Builder s2cBuilder = M17.M170004.S2C.newBuilder();
        if(i ==ids.size()){
            s2cBuilder.setRs(ErrorCodeDefine.M170004_1);
        }else{
            s2cBuilder.setRs(0);
        }
        s2cBuilder.addAllPlayerIds(ids);
        sendNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170004, s2cBuilder.build());
        sendFuntctionLog(FunctionIdDefine.ASK_WISH_FUNCTION_ID);
        //通知那些被祝福的玩家，在线的话就推送到具体模块，不在线的话，直接修改数据库数据
        sendServiceMsg(ActorDefine.FRIEND_SERVICE_NAME, new GameMsg.FriendBlessPlayers(playerProxy.getPlayerId(), ids));
        sendPushNetMsgToClient();
    }

    //请求领取祝福
    private void OnTriggerNet170006Event(Request request) {
        M17.M170006.C2S c2s = request.getValue();
        List<Long> ids = c2s.getPlayerIdsList();

       //TimerdbProxy timerdbProxy = this.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);

        List<Long> getIdList = new ArrayList<>(); //保存有处理的玩家ID
        List<Integer> rewardIds = new ArrayList<>();
        int rs = 0;
        for (long id : ids) {
            //int num = 0;//timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_GET_BLESS, 0, 0);
            int num = playerProxy.getPlayer().getGetbless();
            if (num < FriendDefine.MAX_DAY_GET_BLESS_NUM) {  //小于领取的最大次数，可以领取
                boolean isCanGetBless = playerProxy.isCanGetBless(id);
                if (isCanGetBless) {
                    //timerdbProxy.addNum(TimerDefine.FRIEND_DAY_GET_BLESS, 0, 0, 1);
                    playerProxy.getPlayer().setGetbless(playerProxy.getPlayer().getGetbless()+1);
                    getIdList.add(id);
                    playerProxy.addGetBless(id); //
                    //TODO 获取领取奖励
                    JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.FRIEND_GIFT, "level", playerProxy.getLevel());
                    int getreward = json.getInt("getreward");
                    rewardIds.add(getreward);
                } else {
                    rs = ErrorCodeDefine.M170006_1;
                }
            } else {
                //次数不够了
                rs = ErrorCodeDefine.M170006_2;
            }
        }

        if (getIdList.size() > 0) {
            rs = 0;
        }

        if (rewardIds.size() > 0) {
            blessRewardHandler(rewardIds);
        }


        M17.M170006.S2C.Builder s2cBuilder = M17.M170006.S2C.newBuilder();
        s2cBuilder.setRs(rs);
        s2cBuilder.addAllPlayerIds(getIdList);
        sendNetMsg(ProtocolModuleDefine.NET_M17, ProtocolModuleDefine.NET_M17_C170006, s2cBuilder.build());
        sendFuntctionLog(FunctionIdDefine.ASK_GET_WISH_FUNCTION_ID);
        sendPushNetMsgToClient();
    }

    private void blessRewardHandler(List<Integer> rewardId) {
        PlayerReward playerReward = new PlayerReward();
        RewardProxy rewardProxy = this.getProxy(ActorDefine.REWARD_PROXY_NAME);

        rewardId.forEach(id -> rewardProxy.getPlayerReward(id, playerReward));

        rewardProxy.getRewardToPlayer(playerReward, LogDefine.GET_REQUEST_WISH);

        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(playerReward));
    }

    /**
     * tbllog_user_friend 加好友
     */
    public void addFriendLog(long roleId) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        tbllog_user_friend friendlog = new tbllog_user_friend();
        friendlog.setOpt_role_id(player.getPlayerId());
        friendlog.setRole_id(roleId);
        friendlog.setOpt(1);
        friendlog.setOpt_role_friend_number(player.getFriendSet().size());
        friendlog.setHappend_time(GameUtils.getServerTime());
        sendLog(friendlog);
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }

}




