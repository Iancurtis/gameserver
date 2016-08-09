package com.znl.modules.chat;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.*;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.CustomerLogger;
import com.znl.log.admin.tbllog_chat;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.set.VipActSetDb;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.service.PlayerService;
import com.znl.service.map.WorldTile;
import com.znl.template.ChargeTemplate;
import com.znl.template.MailTemplate;
import com.znl.utils.GameUtils;
import org.json.JSONObject;
import scala.concurrent.duration.Duration;

import java.util.*;

/**
 * Created by Administrator on 2015/12/1.
 */
public class ChatModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<ChatModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ChatModule create() throws Exception {
                return new ChatModule(gameProxy);
            }
        });
    }

    public ChatModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M14);
        starGetChatTimer();
        //发一条请求到聊天服务拿index
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.GetChat message = new GameMsg.GetChat(getChatWorldIndex(), playerProxy.getAccountName());
        sendServiceMsg(ActorDefine.CHAT_SERVICE_NAME, message);
        GameMsg.GetLegionChat messageLegion = new GameMsg.GetLegionChat(getChatLegionIndex(), playerProxy.getAccountName(), playerProxy.getArmGrouId());
        sendServiceMsg(ActorDefine.CHAT_SERVICE_NAME, messageLegion);
    }

    public int getChatWorldIndex() {
        if (chatWorldIndex == null) {
            return -1;
        }
        return chatWorldIndex;
    }

    public int getChatLegionIndex() {
        if (chatLegionIndex == null) {
            return -1;
        }
        return chatLegionIndex;
    }

    public int getChatTime() {
        if (chatTime == null) {
            return -1;
        }
        return chatTime;
    }

    public void setChatWorldIndex(Integer chatWorldIndex) {
        this.chatWorldIndex = chatWorldIndex;
    }

    public void setChatLegionIndex(Integer chatLegionIndex) {
        this.chatLegionIndex = chatLegionIndex;
    }

    public void setChatTime(Integer chatTime) {
        this.chatTime = chatTime;
    }

    private Integer chatWorldIndex = null;
    private Integer chatLegionIndex = null;
    private Integer chatTime = 0;

    private void starGetChatTimer() {
        GameMsg.GetChatTimer message = new GameMsg.GetChatTimer();
        context().system().scheduler().schedule(
                Duration.Zero(), Duration.create(3, "second"), self(), message,
                context().dispatcher(), null);
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.SendChatToPlayer) {
            List<PlayerChat> chats = ((GameMsg.SendChatToPlayer) object).chats();
            int chatType = ((GameMsg.SendChatToPlayer) object).chattype();
            int chatIndex = ((GameMsg.SendChatToPlayer) object).index();
            sendChatToClient(chats, chatType, chatIndex);
        } else if (object instanceof GameMsg.GetChatTimer) {
            if (chatWorldIndex != null) {//要等到拿到服务发过来的index才可以开始请求
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                GameMsg.GetChat message = new GameMsg.GetChat(getChatWorldIndex(), playerProxy.getAccountName());
                sendServiceMsg(ActorDefine.CHAT_SERVICE_NAME, message);
            }
            if (chatLegionIndex != null) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                GameMsg.GetLegionChat message = new GameMsg.GetLegionChat(
                        getChatLegionIndex(), playerProxy.getAccountName(), playerProxy.getArmGrouId());
                sendServiceMsg(ActorDefine.CHAT_SERVICE_NAME, message);
            }
        } else if (object instanceof GameMsg.GetSystemChatIndex) {
            int index = ((GameMsg.GetSystemChatIndex) object).index();
            int chatType = ((GameMsg.GetSystemChatIndex) object).chatType();
            synchronousIndex(index, chatType);
        } else if (object instanceof GameMsg.GetPlayerSimpleInfoSuccess) {
            GameMsg.GetPlayerSimpleInfoSuccess mess = (GameMsg.GetPlayerSimpleInfoSuccess) object;
            SimplePlayer simplePlayer = mess.simplePlayer();
            String cmd = mess.cmd();
            getSimplePlayerHandle(simplePlayer, cmd);
        } else if (object instanceof GameMsg.PrivateChatHandle) {
            //接收到私聊，发送到客户端
            PlayerChat chat = ((GameMsg.PrivateChatHandle) object).playerChat();
            sendPrivateChatToClient(chat);
        } else if (object instanceof GameMsg.GetPlayerSimpleInfoListSuccess) {
            GameMsg.GetPlayerSimpleInfoListSuccess mess = (GameMsg.GetPlayerSimpleInfoListSuccess) object;
            List<SimplePlayer> list = mess.simplePlayer();
            String cmd = mess.cmd();
            getSimplePlayerListHandle(list, cmd);
        } else if (object instanceof GameMsg.JoinLegionNotify) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            GameMsg.GetLegionChat messageLegion = new GameMsg.GetLegionChat(getChatLegionIndex(), playerProxy.getAccountName(), playerProxy.getArmGrouId());
            sendServiceMsg(ActorDefine.CHAT_SERVICE_NAME, messageLegion);
        } else if (object instanceof GameMsg.LeaveLegionNotify) {
            setChatLegionIndex(null);
        } else if (object instanceof GameMsg.trumpeNotity) {
            String name = ((GameMsg.trumpeNotity) object).name();
            String mess = ((GameMsg.trumpeNotity) object).mess();
            Long playerId = ((GameMsg.trumpeNotity) object).playerId();
            int type = ((GameMsg.trumpeNotity) object).retype();
            M14.M140008.S2C.Builder builder = M14.M140008.S2C.newBuilder();
            M14.TrumpeInfo.Builder info = M14.TrumpeInfo.newBuilder();
            info.setName(name);
            info.setMess(mess);
            info.setType(type);
            builder.addInfo(info.build());
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (playerProxy.getChatInfo2Trumpe(playerId, mess) != null) {
                builder.setChatInfo(playerProxy.getChatInfo2Trumpe(playerId, mess));
            }
            pushNetMsg(ProtocolModuleDefine.NET_M14, ProtocolModuleDefine.NET_M14_C140008, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140008);
            sendFuntctionLog(FunctionIdDefine.TRUMPET_FUNCTION_ID);
        } else if (object instanceof GameMsg.sendAChat) {
            PlayerChat chat = ((GameMsg.sendAChat) object).chat();
            sendChatMessageToChatService(chat);
        }
    }

    private void getSimplePlayerListHandle(List<SimplePlayer> list, String cmd) {
        if ("140006".equals(cmd)) {
            sendShieldInfosToClient(list);
        }
    }

    /**
     * 发送屏蔽列表到客户端
     **/
    private void sendShieldInfosToClient(List<SimplePlayer> list) {
        M14.M140006.S2C.Builder builder = M14.M140006.S2C.newBuilder();
        for (SimplePlayer simplePlayer : list) {
            builder.addInfos(getShieldInfoBySimplePlayer(simplePlayer));
        }
        builder.setRs(0);
        pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140006, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140006);
        sendFuntctionLog(FunctionIdDefine.GET_SHIELD_LISTS_FUNCTION_ID);
    }


    /**
     * 接收到私聊，发送到客户端
     **/
    private void sendPrivateChatToClient(PlayerChat chat) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.isShield(chat.playerId, ChatAndMailDefine.SHIELD_TYPE_CHAT)) {
            return;
        }
        M14.M140003.S2C.Builder builder = M14.M140003.S2C.newBuilder();
        builder.setRs(0);
        builder.setChatInfo(playerProxy.getChatInfo(chat));
        pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140003, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140003);
        List<Long> laterlist = playerProxy.getlaterlist();
        laterlist.remove(chat.playerId);
        laterlist.remove(playerProxy.getPlayerId());
        String str1 = "";
        for (long id : laterlist) {
            str1 = str1 + "_" + id;
        }
        playerProxy.setLaterPlayer(str1);
        playerProxy.setLaterPlayer(chat.playerId);
        List<Long> laternewlist = playerProxy.getlaterlist();
        if (laternewlist.size() > ChatAndMailDefine.LATERMAX) {
            laternewlist.remove(ChatAndMailDefine.LATERMAX);
        }
        String str = "";
        for (long id : laternewlist) {
            str = str + "_" + id;
        }
        playerProxy.setLaterPlayer(str);
    }

    private M14.SimplePlayerInfo getSimplePlayerInfo(SimplePlayer simplePlayer) {
        M14.SimplePlayerInfo.Builder info = M14.SimplePlayerInfo.newBuilder();
        info.setBoom(simplePlayer.getBoom());
        info.setBoomUpLimit(simplePlayer.getBoomUpLimit());
        info.setCapacity(simplePlayer.getCapacity());
        info.setIconId(simplePlayer.getIconId());
        info.setLevel(simplePlayer.getLevel());
        info.setMilitaryRank(simplePlayer.getMilitaryRank());
        info.setName(simplePlayer.getName());
        info.setPlayerId(simplePlayer.getId());
        if (simplePlayer.getArmygrouid() > 0) {
            info.setLegion(simplePlayer.getLegionName());
        }
        info.setX(simplePlayer.getX());
        info.setY(simplePlayer.getY());
        info.setPendantId(simplePlayer.getPendant());
        int cityIcon = 0;
        JSONObject boomConfig = GameUtils.getBoomConfig((int) simplePlayer.getBoom());
        Long now = GameUtils.getServerDate().getTime();
        if (simplePlayer.getFacadeendTime() > now) {
            cityIcon = simplePlayer.getFaceIcon();
        } else if (simplePlayer.getBoomState() == ActorDefine.DEFINE_BOOM_RUINS) {
            cityIcon = ActorDefine.RUINS_ICON;
        } else {
            cityIcon = boomConfig.getInt("BaseLook");
        }
        info.setCityIcon(cityIcon);
        return info.build();
    }

    /**
     * 发送简要信息到客户端
     **/
    private void sendSimplePlayerToClient(SimplePlayer simplePlayer) {
        M14.M140001.S2C.Builder builder = M14.M140001.S2C.newBuilder();
        builder.setRs(0);
        builder.setInfo(getSimplePlayerInfo(simplePlayer));
        pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140001, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140001);
    }

    private void getSimplePlayerHandle(SimplePlayer simplePlayer, String cmd) {
        Object ob = new Object();

        if ("140001".equals(cmd)) {
            if (simplePlayer == null) {
                M14.M140001.S2C.Builder builder = M14.M140001.S2C.newBuilder();
                builder.setRs(ErrorCodeDefine.M140001_1);
                pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140001, builder.build());
                sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140001);

                return;
            }
//            if (simplePlayer.online){
//                //丢到其他玩家的CHAT模块获取他的战力
//                GameMsg.GetRoleCapacity mess = new GameMsg.GetRoleCapacity(simplePlayer);
//                sendMsgToOtherPlayerModule(ActorDefine.CHAT_MODULE_NAME, simplePlayer.getAccountName(),mess);
//            }else {
//                //不在线的话就直接发吧
//            }
            sendSimplePlayerToClient(simplePlayer);

        } else if ("140002".equals(cmd)) {
            M14.M140002.S2C.Builder builder = M14.M140002.S2C.newBuilder();
            if (simplePlayer == null) {
                builder.setRs(ErrorCodeDefine.M140002_1);
                pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140002, builder.build());
                sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140002);
            } else if (simplePlayer.online == false) {
                //无法私聊不在线的玩家
                builder.setRs(ErrorCodeDefine.M140002_3);
                pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140002, builder.build());
                sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140002);
            } else if (privateChatText == null) {
                builder.setRs(ErrorCodeDefine.M140002_2);
            } else {
                builder.setRs(0);
                //通知其他玩家
                PlayerChat chat = new PlayerChat();
                chat.context = privateChatText;
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                chat.type = ChatAndMailDefine.CHAT_TYPE_PRIVATE;
                chat.playerId = playerProxy.getPlayerId();
                chat.playerName = playerProxy.getPlayerName();
                chat.iconId = playerProxy.getPlayerIcon();
                chat.vipLevel = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
                chat.playerType = playerProxy.getPlayerType();
                chat.legionName = playerProxy.getLegionName();
                chat.level = playerProxy.getLevel();
                GameMsg.PrivateChatHandle mess = new GameMsg.PrivateChatHandle(chat);
                sendMsgToOtherPlayerModule(ActorDefine.CHAT_MODULE_NAME, simplePlayer.getAccountName(), mess);
                List<Long> laterlist = playerProxy.getlaterlist();
                laterlist.remove(simplePlayer.getId());
                laterlist.remove(playerProxy.getPlayerId());
                String str1 = "";
                for (long id : laterlist) {
                    str1 = str1 + "_" + id;
                }
                playerProxy.setLaterPlayer(str1);
                playerProxy.setLaterPlayer(simplePlayer.getId());
                List<Long> laternewlist = playerProxy.getlaterlist();
                if (laternewlist.size() > ChatAndMailDefine.LATERMAX) {
                    laternewlist.remove(ChatAndMailDefine.LATERMAX);
                }
                String str = "";
                for (long id : laternewlist) {
                    str = str + "_" + id;
                }
                playerProxy.setLaterPlayer(str);
            }
            pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140002, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140002);
            privateChatText = null;
        } else if ("140004".equals(cmd)) {
            M14.M140004.S2C.Builder builder = M14.M140004.S2C.newBuilder();
            if (simplePlayer == null) {
                builder.setRs(ErrorCodeDefine.M140004_1);
            } else if (simplePlayer.online == false) {
                builder.setRs(ErrorCodeDefine.M140004_2);
            } else {
                builder.setRs(0);
                builder.setInfo(getSimplePlayerInfo(simplePlayer));
                sendFuntctionLog(FunctionIdDefine.PRIVATE_CHAT_ASK_FUNCTION_ID);
            }
            pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140004, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140004);
        }
    }

    /**
     * 初始化聊天index
     **/
    private void synchronousIndex(int index, int chatType) {
        if (chatType == ChatAndMailDefine.CHAT_TYPE_WORLD) {
            setChatWorldIndex(index);
        }
        if (chatType == ChatAndMailDefine.CHAT_TYPE_LEGION) {
            setChatLegionIndex(index);
        }
    }

    private void sendChatToClient(List<PlayerChat> chats, int chatType, int chatIndex) {
        M14.M140000.S2C.Builder builder = M14.M140000.S2C.newBuilder();
        builder.setType(chatType);
        if (chatType == ChatAndMailDefine.CHAT_TYPE_WORLD) {
            setChatWorldIndex(chatIndex);
        }
        if (chatType == ChatAndMailDefine.CHAT_TYPE_LEGION) {
            setChatLegionIndex(chatIndex);
        }
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (PlayerChat chat : chats) {
            if (playerProxy.isShield(chat.playerId, ChatAndMailDefine.SHIELD_TYPE_CHAT)) {
                continue;
            }
//            if (chat.playerId == playerProxy.getPlayerId()){
//                continue;
//            }
            builder.addChats(playerProxy.getChatInfo(chat));
        }
        pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140000, builder.build());
        sendPushNetMsgToClient(0);
    }

    String lastChat = "";
    private void OnTriggerNet140000Event(Request request) {
        int time = GameUtils.getServerTime();
        M14.M140000.C2S proto = request.getValue();
        int type = proto.getType();
        String context = proto.getContext();
        /**** 测试代码*****/
        if(context.startsWith("zn")){
            String[] strs = context.split(" ");
            doCheatLogic(strs);
            return;
        }
        /**** 测试代码结束*****/
        if (GameUtils.test() && context.startsWith("zb ")) {
            String[] strs = context.split(" ");
            doCheatLogic(strs);
            return;
        }
//        type = ChatAndMailDefine.CHAT_TYPE_LEGION;
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.isBanChat()) {
            return;
        }
        if(context.equals(lastChat)){
            return;
        }
        lastChat = context;
        PlayerChat chat = new PlayerChat();
        if (time >= chatTime + 5) {
            chat.context = context;
            chat.legionId = playerProxy.getArmGrouId();
            chat.type = type;
            chat.playerId = playerProxy.getPlayerId();
            chat.playerName = playerProxy.getPlayerName();
            chat.iconId = playerProxy.getPlayerIcon();
            chat.vipLevel = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
            chat.playerType = playerProxy.getPlayerType();
            chat.pendantId = playerProxy.getPendt();
            chat.level = playerProxy.getLevel();
            chat.legionName = playerProxy.getLegionName();
            setChatTime(time);
            sendChatMessageToChatService(chat);
        } else {
            return;
        }

        /**
         * tbllog_chat日志
         */
        chatLog(type, context, chat.playerId);

    }

    private void OnTriggerNet140001Event(Request request) {
        M14.M140001.C2S proto = request.getValue();
        long playerId = proto.getPlayerId();
        if (proto.hasName() == false) {
            GameMsg.GetPlayerSimpleInfo mess = new GameMsg.GetPlayerSimpleInfo(playerId, "140001");
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);
            sendFuntctionLog(FunctionIdDefine.CHECK_INFOS_FUNCTION_ID, playerId, 0, 0);
        } else {
            String name = proto.getName();
            GameMsg.GetPlayerSimpleInfoByRoleName mess = new GameMsg.GetPlayerSimpleInfoByRoleName(name, "140001");
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);
            sendFuntctionLog(FunctionIdDefine.CHECK_INFOS_FUNCTION_ID, 0, 0, 0, name);
        }
    }

    private String privateChatText;

    private void OnTriggerNet140002Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.isBanChat()) {//禁言
            M14.M140002.S2C.Builder builder = M14.M140002.S2C.newBuilder();
            builder.setRs(ErrorCodeDefine.M140002_4);
            pushNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140002, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140002);
            return;
        }
        M14.M140002.C2S proto = request.getValue();
        long playerId = proto.getPlayerId();
        String text = proto.getContext();
        privateChatText = text;
        GameMsg.GetPlayerSimpleInfo mess = new GameMsg.GetPlayerSimpleInfo(playerId, "140002");
        sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);

    }

    private void OnTriggerNet140003Event(Request request) {
        //nothing to do~
    }

    private void OnTriggerNet140004Event(Request request) {
        M14.M140004.C2S proto = request.getValue();
        int type = proto.getType();
        if (type == 1) {
            String name = proto.getName();
            GameMsg.GetPlayerSimpleInfoByRoleName mess = new GameMsg.GetPlayerSimpleInfoByRoleName(name, "140004");
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);
        } else {
            long id = proto.getPlayerId();
            GameMsg.GetPlayerSimpleInfo mess = new GameMsg.GetPlayerSimpleInfo(id, "140004");
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);
        }
    }


    private void OnTriggerNet140005Event(Request request) {
        M14.M140005.C2S proto = request.getValue();
        long id = proto.getPlayerId();
        int type = proto.getType();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int rs = playerProxy.addShieldPlayer(id, type);
        M14.M140005.S2C.Builder builder = M14.M140005.S2C.newBuilder();
        builder.setRs(rs);
        sendNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140005, builder.build());
        sendFuntctionLog(FunctionIdDefine.ADD_SHIELD_PLAYER_FUNCTION_ID, id, type, 0);
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140005);
    }

    private void OnTriggerNet140006Event(Request request) {
        M14.M140006.C2S proto = request.getValue();
        int type = proto.getType();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Set<Long> ids = playerProxy.getShieldsByType(type);
        GameMsg.GetPlayerSimpleInfoListByIds mess = new GameMsg.GetPlayerSimpleInfoListByIds(ids, "140006");
        sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, mess);
    }

    private void OnTriggerNet140007Event(Request request) {
        M14.M140007.C2S proto = request.getValue();
        int type = proto.getType();
        long playerId = proto.getPlayerId();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int rs = playerProxy.removeShield(type, playerId);
        M14.M140007.S2C.Builder builder = M14.M140007.S2C.newBuilder();
        builder.setRs(rs);
        sendNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140007, builder.build());
        sendFuntctionLog(FunctionIdDefine.REMOVE_SHIELD_LISTS_FUNCTION_ID, playerId, type, 0);
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140007);
    }

    private void OnTriggerNet140009Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M14.M140009.S2C.Builder builder = M14.M140009.S2C.newBuilder();
        builder.setType(playerProxy.getPlayerType());
        sendNetMsg(ActorDefine.CHAT_MODULE_ID, ProtocolModuleDefine.NET_M14_C140009, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140009);
    }

    private M14.ShieldInfo getShieldInfoBySimplePlayer(SimplePlayer simplePlayer) {
        M14.ShieldInfo.Builder builder = M14.ShieldInfo.newBuilder();
        builder.setIconId(simplePlayer.getIconId());
        builder.setLevel(simplePlayer.getLevel());
        builder.setName(simplePlayer.getName());
        builder.setPlayerId(simplePlayer.getId());
        return builder.build();
    }

    private void doCheatLogic(String[] strs) {
        try {
            String cmd = strs[1];
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            if (cmd.equals("addItem")) {
                Integer itemId = Integer.parseInt(strs[2]);
                Integer num = Integer.parseInt(strs[3]);
                ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
                itemProxy.addItem(itemId, num, LogDefine.GET_CHAT);
                reward.addItemMap.put(itemId, num);
            } else if (cmd.equals("add")) {
                Integer power = Integer.parseInt(strs[2]);
                Integer typeId = Integer.parseInt(strs[3]);
                Integer num = Integer.parseInt(strs[4]);
                rewardProxy.getRewardContent(reward, power, typeId, num);
                rewardProxy.getRewardToPlayer(reward, LogDefine.GET_CHAT);

                //更新到战力排行榜
                if (power == 406) {
                    PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    Long capacity = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_highestCapacity);
                    GameMsg.AddPlayerToRank msg = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), capacity, PowerRanksDefine.POWERRANK_TYPE_CAPACITY);
                    sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
                }
            } else if (cmd.equals("reduce")) {
                Integer power = Integer.parseInt(strs[2]);
                Integer typeId = Integer.parseInt(strs[3]);
                Integer num = Integer.parseInt(strs[4]);
                PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.reducePowerValue(typeId,num, LogDefine.GET_CHAT);

            }else if (cmd.equals("addSoldier")) {
                Integer soldier = Integer.parseInt(strs[2]);
                Integer num = Integer.parseInt(strs[3]);
                rewardProxy.getRewardContent(reward, PlayerPowerDefine.BIG_POWER_SOLDIER, soldier, num);
                rewardProxy.getRewardToPlayer(reward, LogDefine.GET_CHAT);

                //更新到战力排行榜
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                Long capacity = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_highestCapacity);
                GameMsg.AddPlayerToRank msg = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), capacity, PowerRanksDefine.POWERRANK_TYPE_CAPACITY);
                sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg);
            } else if (cmd.equals("addOd")) {
                Integer soldier = Integer.parseInt(strs[2]);
                OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
                long id = ordnanceProxy.creatOrdnance(soldier, 0, 0, LogDefine.GET_CHAT);
                rewardProxy.addOrdnanceIdtoReward(reward, id);
            } else if (cmd.equals("addOdp")) {
                Integer soldier = Integer.parseInt(strs[2]);
                Integer num = Integer.parseInt(strs[3]);
                OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
                ordnancePieceProxy.addOrdnancePiece(soldier, num, LogDefine.GET_CHAT);
                rewardProxy.addOrdanceFragmentToReward(reward, soldier, num);
            } else if (cmd.equals("cl")) {
                Integer chId = Integer.parseInt(strs[2]);
                LotterProxy lotterProxy = getProxy(ActorDefine.LOTTER_PROXY_NAME);
                lotterProxy.getRandomReward(chId);
                List<Integer> list = new ArrayList<Integer>();
                lotterProxy.getRandomRewardTaoBao(chId, list);
            } else if (cmd.equals("addEq")) {
                Integer soldier = Integer.parseInt(strs[2]);
                Integer num = Integer.parseInt(strs[3]);
                EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
                for (int i = 0; i < num; i++) {
                    long di = equipProxy.addEquip(soldier, LogDefine.GET_CHAT);
                    rewardProxy.addEquipIdtoReward(reward, di);
                }

            } else if (cmd.equals("addalleq")) {
                List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.EQUIP_PRO);
                EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
                for (JSONObject define : list) {
                    for (int i = 0; i < 10; i++) {
                        equipProxy.addEquip(define.getInt("ID"), LogDefine.GET_CHAT);
                    }
                    reward.generalMap.put(define.getInt("ID"), 10);
                }
            } else if (cmd.equals("addTimes")) {
              /*  Integer advanceTimes = Integer.parseInt(strs[2]);
                List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
             //   TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                for (JSONObject define : list) {
                    int times = define.getInt("time");
                    int dungeonId = define.getInt("ID");
                    if (times > 0) {
                        timerdbProxy.addAdvanceTiems(dungeonId, advanceTimes);
                    }
                }*/
            } else if (cmd.equals("addVIP")) {
                Integer vipExp = Integer.parseInt(strs[2]);
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.addPowerValue(PlayerPowerDefine.POWER_vipExp, vipExp, LogDefine.GET_CHAT);
                //推送VIP加成属性
                GameMsg.ReshBuildings rbmsg = new GameMsg.ReshBuildings();
                sendModuleMsg(ActorDefine.BUILD_MODULE_NAME, rbmsg);
            } else if (cmd.equals("addllItems")) {
                Integer num = Integer.parseInt(strs[2]);
                List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigAllInfo(DataDefine.ITEM_METIC);
                for (JSONObject object : jsonObjectList) {
                    ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
                    itemProxy.addItem(object.getInt("ID"), num, LogDefine.GET_CHAT);
                    reward.addItemMap.put(object.getInt("ID"), num);
                }
            } else if (cmd.equals("addMail")) {
                Integer num = Integer.parseInt(strs[2]);
                MailTemplate template = new MailTemplate("作弊测试邮件", "作弊测试邮件", 0, "系统邮件", ChatAndMailDefine.MAIL_TYPE_SYSTEM);
                List<Integer> rewards = new ArrayList<>();
                rewards.add(1081);
                template.setRewards(rewards);
                List<Integer[]> att = new ArrayList<>();
                att.add(new Integer[]{401, 1046, 2});
                template.setAttachments(att);
                for (int i = 0; i < num; i++) {
                    sendModuleMsg(ActorDefine.MAIL_MODULE_NAME, new GameMsg.ReceiveMailNotice(template));
                }
            } else if (cmd.equals("addallod")) {
                Integer num = Integer.parseInt(strs[2]);
                /*军械*/
                List<JSONObject> ordnanceList = ConfigDataProxy.getConfigAllInfo(DataDefine.ORDNANCE);
                OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
                for (JSONObject define : ordnanceList) {
                    for (int i = 0; i < num; i++) {
                        long id = ordnanceProxy.creatOrdnance(define.getInt("ID"), 0, 0, LogDefine.GET_CHAT);
                        rewardProxy.addOrdnanceIdtoReward(reward, id);
                    }
                }
            } else if (cmd.equals("addallodp")) {
                Integer num = Integer.parseInt(strs[2]);
                 /*军械碎片*/
                List<JSONObject> OrdnancePieceList = ConfigDataProxy.getConfigAllInfo(DataDefine.ORDNANCE_PIECE);
                OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
                for (JSONObject define : OrdnancePieceList) {
                    for (int i = 0; i < num; i++) {
                        ordnancePieceProxy.addOrdnancePiece(define.getInt("ID"), num, LogDefine.GET_CHAT);
                        rewardProxy.addOrdanceFragmentToReward(reward, define.getInt("ID"), num);
                    }
                }
            } else if (cmd.equals("addallres")) {
                Integer power = 407;
                Integer num = Integer.parseInt(strs[2]);
                rewardProxy.getRewardContent(reward, power, 201, num);
                rewardProxy.getRewardContent(reward, power, 202, num);
                rewardProxy.getRewardContent(reward, power, 203, num);
                rewardProxy.getRewardContent(reward, power, 204, num);
                rewardProxy.getRewardContent(reward, power, 205, num);
                rewardProxy.getRewardToPlayer(reward, LogDefine.GET_CHAT);
            } else if (cmd.equals("delallres")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                for (int i = 201; i <= 205; i++) {
                    playerProxy.reducePowerValue(i, (int) playerProxy.getPowerValue(i), LogDefine.GET_CHAT);
                }
            }else if (cmd.equals("addjunshi")) {
                AdviserProxy adviserProxy= getProxy(ActorDefine.ADVISER_PROXY_NAME);
                Integer typeid = Integer.parseInt(strs[2]);
                Integer num = Integer.parseInt(strs[3]);
                adviserProxy.addAdviser(typeid,num,LogDefine.GET_CHAT);
                rewardProxy.addCounsellorToReward(reward,typeid,num);
            } else if (cmd.equals("alljunshi")) {
                AdviserProxy adviserProxy= getProxy(ActorDefine.ADVISER_PROXY_NAME);
                for(JSONObject jsonObject:ConfigDataProxy.getConfigAllInfo(DataDefine.Counsellor)){
                    Integer typeid = jsonObject.getInt("ID");
                    Integer num = Integer.parseInt(strs[2]);
                    adviserProxy.addAdviser(typeid,num,LogDefine.GET_CHAT);
                    rewardProxy.addCounsellorToReward(reward,typeid,num);
                }
            }else if (cmd.equals("delItem")) {
                ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
                List<Integer> list = itemProxy.delAllItem();
                for (int typeId : list) {
                    rewardProxy.addItemToReward(reward, typeId, 0);
                }
            } else if (cmd.equals("delEq")) {
                EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
                List<Long> list = equipProxy.delAllEquip();
                for (Long eqId : list) {
                    rewardProxy.addEquipIdtoReward(reward, eqId);
                }

            } else if (cmd.equals("delOdp")) {
                OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
                List<Integer> list = ordnancePieceProxy.delAllItem();
                for (int typeId : list) {
                    rewardProxy.addOrdanceFragmentToReward(reward, typeId, 1);
                }
            } else if (cmd.equals("delOd")) {
                OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
                List<Long> list = ordnanceProxy.delAllEquip();
                for (long typeId : list) {
                    rewardProxy.addOrdnanceIdtoReward(reward, typeId);
                }
            } else if (cmd.equals("bl")) {
                Integer type = Integer.parseInt(strs[2]);
                Integer index = Integer.parseInt(strs[3]);
                Integer level = Integer.parseInt(strs[4]);
                ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                resFunBuildProxy.addLevl(type, level, index);
            } else if (cmd.equals("yiwen")) {
                Integer type = Integer.parseInt(strs[2]);
                GameMsg.test msg = new GameMsg.test(type);
                sendModuleMsg(ActorDefine.ARMYGROUP_MODULE_NAME, msg);
            } else if (cmd.equals("buildup")) {
                Integer type = Integer.parseInt(strs[2]);
                Integer level = Integer.parseInt(strs[3]);
                ResFunBuildProxy resFunBuildProxy = getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                resFunBuildProxy.addLevl(type, level);
            } else if (cmd.equals("changeTime")) {
                //setServerDate=2016-01-15 05:59:30
                String time1 = strs[2];
                String time2 = strs[3];
                String time = time1 + " " + time2;
                GameUtils.setServerDate(time);
                System.err.println(GameUtils.getServerDateStr());
            } else if (cmd.equals("day")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                Integer type = Integer.parseInt(strs[2]);
                playerProxy.setLoginDayNum(type);
                for (int i = 1; i <= type; i++) {
                    playerProxy.addRewardNum(i);
                }
            }else if (cmd.equals("laohuji")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.setLoginDayNum(31);
                playerProxy.getPlayer().setRewardNum(new ArrayList<>());
            }else if (cmd.equals("czlaohuji")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                //TimerdbProxy timerdbProxy=getProxy(ActorDefine.TIMERDB_PROXY_NAME);
               // timerdbProxy.setNum(TimerDefine.LOGIN_LOTTERY, 0, 0,0);
                playerProxy.setLoginDayNum(playerProxy.getLoginDayNum()+1);
            } else if (cmd.equals("as")) {
                SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                Integer num = Integer.parseInt(strs[2]);
                List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigAllInfo(DataDefine.ARM_KINDS);
                for (JSONObject jsonObject : jsonObjectList) {
                    soldierProxy.addSoldierNum(jsonObject.getInt("ID"), num, LogDefine.GET_CHAT);
                    rewardProxy.addSoldierToReward(reward, jsonObject.getInt("ID"), num);
                }
            } else if (cmd.equals("charge")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                Integer num = Integer.parseInt(strs[2]);
                playerProxy.chargeToPlayer(num, AdminCodeDefine.CHARGE_TYPE_NORMAL, "test" + playerProxy.getPlayerId(), reward);
                VipActSetDb vipactset = BaseSetDbPojo.getSetDbPojo(VipActSetDb.class, playerProxy.getAreaKey());
                long lastvalue = vipactset.getAllVipExpByplayerId(playerProxy.getPlayerId());
                vipactset.addKeyValue(playerProxy.getPlayerId() + "", lastvalue + num * 10);
                sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, new GameMsg.refreshengry());
                for (SimplePlayer simple : PlayerService.onlineMap().get(playerProxy.getAreaKey()).values()) {
                    sendMsgToOtherPlayerModule(ActorDefine.SYSTEM_MODULE_NAME, simple.getAccountName(), new GameMsg.notitySomeOneCharge());
                }
                M3.M30102.S2C.Builder builder = M3.M30102.S2C.newBuilder();
                builder.setAmount(num);
                sendNetMsg(ActorDefine.SYSTEM_MODULE_ID, ProtocolModuleDefine.NET_M3_C30102, builder.build());
            } else if (cmd.equals("boom")) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                int num = Integer.parseInt(strs[2]);
                playerProxy.setPowerValue(PlayerPowerDefine.POWER_boom, (long) num);
                playerProxy.allTakeSoldierNum();
                rewardProxy.getRewardContent(reward, 407, PlayerPowerDefine.POWER_boom, num);
            }else if (cmd.equals("openzb")) {
                Integer state = Integer.parseInt(strs[2]);
                if(state==1){
                    //作弊开
                    GameUtils.setTest(true);
                }else if(state==0){
                    //作弊关闭
                    GameUtils.setTest(true);
                }
                System.out.println("当前的作弊指令是"+GameUtils.test());
            }

            M2.M20007.S2C message = rewardProxy.getRewardClientInfo(reward);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, message);
            if (reward.soldierMap.size() > 0) {
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            }
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M14_C140000);
        } catch (Exception e) {
            e.printStackTrace();
            CustomerLogger.error("作弊逻辑的时候出现异常", e);
        }

    }

    private void sendChatMessageToChatService(PlayerChat chat) {
        GameMsg.AddChat message = new GameMsg.AddChat(chat);
        sendServiceMsg(ActorDefine.CHAT_SERVICE_NAME, message);
    }

    /**
     * tbllog_chat日志
     */
    public void chatLog(int type, String msg, long targetRoleId) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_chat chatlog = new tbllog_chat();
        chatlog.setPlatform(cache.getPlat_name());
        chatlog.setAccount_name(player.getAccountName());
        chatlog.setRole_id(player.getPlayerId());
        chatlog.setRole_name(player.getPlayerName());
        chatlog.setDim_level(player.getLevel());
        chatlog.setUser_ip(cache.getUser_ip());
        chatlog.setType(type);
        chatlog.setChannel(1);
        chatlog.setMsg(msg);
        chatlog.setTarget_role_id(targetRoleId);
        chatlog.setHappend_time(GameUtils.getServerTime());
        sendLog(chatlog);
    }

    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {

    }

}
