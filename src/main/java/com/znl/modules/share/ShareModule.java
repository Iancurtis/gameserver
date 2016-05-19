package com.znl.modules.share;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerChat;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.msg.PushShareMsg;
import com.znl.msg.SendSystemChatToPlayerService;
import com.znl.msg.ShareMsg;
import com.znl.proto.*;
import com.znl.proxy.*;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/19.
 */
public class ShareModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<ShareModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ShareModule create() throws Exception {
                return new ShareModule(gameProxy);
            }
        });
    }

    public ShareModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M25);
    }

    private Integer chatTime = 0;
    private Integer leginChatTime = 0;

    public int getChatTime() {
        if (chatTime == null) {
            return -1;
        }
        return chatTime;
    }

    public int getLeginChatTime() {
        if (leginChatTime == null) {
            return -1;
        }
        return leginChatTime;
    }

    public void setChatTime(Integer chatTime) {
        this.chatTime = chatTime;
    }

    public void setLeginChatTime(Integer leginChatTime) {
        this.leginChatTime = leginChatTime;
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof ShareMsg) {
            M25.M250000.S2C msg = ((ShareMsg) anyRef).msg();
            pushNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, msg);
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M25_C250000);
        }
        if (anyRef instanceof SendSystemChatToPlayerService) {
            M25.M250000.S2C msg = ((SendSystemChatToPlayerService) anyRef).msg();
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new ShareMsg(msg, 0));
        } else if (anyRef instanceof GameMsg.legionenlistback) {
            int rs = ((GameMsg.legionenlistback) anyRef).rs();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (rs == 0) {
                M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
                builder.setType(ChatAndMailDefine.CHAT_TYPE_LEGIONENLIAT);
                builder.setShareType(ChatAndMailDefine.ShARE_TYPE_WORLD);
                PlayerChat chat = new PlayerChat();
                chat.context = playerProxy.getLegionName() + "军团发出招募告示：欢迎加入我们军团！去看看";
                chat.legionId = playerProxy.getArmGrouId();
                chat.type = ChatAndMailDefine.CHAT_TYPE_WORLD;
                chat.playerId = playerProxy.getPlayerId();
                chat.playerName = playerProxy.getPlayerName();
                chat.iconId = playerProxy.getPlayerIcon();
                chat.vipLevel = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
                chat.playerType = playerProxy.getPlayerType();
                chat.pendantId = playerProxy.getPendt();
                chat.legionName = playerProxy.getLegionName();
                chat.level = playerProxy.getLevel();
                chat.legionName = playerProxy.getLegionName();
                builder.setChat(playerProxy.getChatInfo(chat));
                builder.setRs(0);
                sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new ShareMsg(builder.build(), 0));
            }
            M22.M220400.S2C.Builder builder = M22.M220400.S2C.newBuilder();
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220400, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M22_C220400);
        } else if (anyRef instanceof PushShareMsg) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            long legionId = playerProxy.getArmGrouId();
            List<ShareMsg> msgs = ((PushShareMsg) anyRef).pushList();
            for (ShareMsg msg : msgs) {
                if (msg.legionId() > 0 && msg.legionId() == legionId) {
                    M25.M250000.S2C S2CMsg = msg.msg();
                    pushNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, S2CMsg);
                } else if (msg.legionId() == 0) {
                    M25.M250000.S2C S2CMsg = msg.msg();
                    pushNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, S2CMsg);
                }
            }
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M25_C250000);
        }
    }

    private void OnTriggerNet250000Event(Request request) {
        int time = GameUtils.getServerTime();
        int Legintime = GameUtils.getServerTime();
        int rs = 0;
        M25.M250000.S2C.Builder builder = M25.M250000.S2C.newBuilder();
        M25.M250000.C2S c2s = request.getValue();
        int type = c2s.getType();
        long id = c2s.getId();
        int shareType = c2s.getShareType();
        PlayerChat chat = new PlayerChat();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long legionId = playerProxy.getArmGrouId();
        if (shareType == ChatAndMailDefine.CHAT_TYPE_LEGION) {
            if (legionId <= 0) {
                rs = ErrorCodeDefine.M250000_4;
            }
            if (time >= chatTime + 30 && rs >= 0) {

                switch (type) {
                    case 1: {//佣兵
                        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                        id = c2s.getTypeId();
                        Common.SoldierInfo soldierInfo = soldierProxy.getSoldierInfotoshare((int) id);
                        if (soldierInfo == null || soldierInfo.getNum() == 0) {
                            rs = ErrorCodeDefine.M250000_1;
                        } else {
                            builder.setSoldierInfo(soldierInfo);
                            JSONObject soldier = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldierInfo.getTypeid());
                            chat.context = soldier.getString("name");
                            builder.setTypeId((int) id);
                        }
                        break;
                    }
                    case 2: {//竞技场战报
                        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
                        M20.PerDetailInfos detail = mailProxy.getArenaReoprtDetailInfoById(id);
                        if (detail == null) {
                            rs = ErrorCodeDefine.M250000_2;
                        } else {
                            builder.setAreanInfo(detail);
                            if (detail.getType() == 1) {
                                chat.context = "竞技场战报：挑战 " + detail.getProtect().getName();
                            } else {
                                chat.context = "竞技场战报：被 " + detail.getAttack().getName() + " 挑战";
                            }
                        }
                        break;
                    }
                    case 3: {
                        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
                        M16.MailDetalInfo.Builder mailBuilder = M16.MailDetalInfo.newBuilder();
                        rs = mailProxy.getDetalInfo(id, mailBuilder);
                        if (rs < 0) {
                            rs = ErrorCodeDefine.M250000_3;
                        } else {
                            M16.Report.Builder reportBuilder = M16.Report.newBuilder(mailBuilder.getInfos());
                            if (shareType == ChatAndMailDefine.CHAT_TYPE_LEGION) {
                                if ((reportBuilder.getIsPerson() == 1 && reportBuilder.getMailType() != ReportDefine.REPORT_TYPE_BE_ATTACK) ||
                                        (reportBuilder.getIsPerson() == 0 && reportBuilder.getMailType() == ReportDefine.REPORT_TYPE_ATTACK
                                                && reportBuilder.getResourcePanel().getCityIcon() < ActorDefine.WORLD_RESOURCE_ICON_MAX)) {
                                    M16.TargetInfo.Builder targetBuilder = M16.TargetInfo.newBuilder(reportBuilder.getInfoPanel());
                                    targetBuilder.setPosX(-1);
                                    targetBuilder.setPosY(-1);
                                    reportBuilder.setInfoPanel(targetBuilder.build());
                                }
                            } else {
                                M16.TargetInfo.Builder targetBuilder = M16.TargetInfo.newBuilder(reportBuilder.getInfoPanel());
                                targetBuilder.setPosX(-1);
                                targetBuilder.setPosY(-1);
                                reportBuilder.setInfoPanel(targetBuilder.build());
                            }
                            mailBuilder.setInfos(reportBuilder.build());
                            builder.setReportInfo(mailBuilder.build());
                            int reportType = mailBuilder.getInfos().getMailType();
                            if (reportType == ReportDefine.REPORT_TYPE_SPY) {
                                chat.context = "侦查  " + mailBuilder.getInfos().getInfoPanel().getName() + " " + mailBuilder.getInfos().getInfoPanel().getLevel();
                            } else if (reportType == ReportDefine.REPORT_TYPE_ATTACK) {
                                chat.context = "攻击 " + mailBuilder.getInfos().getInfoPanel().getName() + " " + mailBuilder.getInfos().getInfoPanel().getLevel();
                            } else {
                                chat.context = "遭到 " + mailBuilder.getInfos().getInfoPanel().getAim() + " " + mailBuilder.getInfos().getInfoPanel().getLevel() + " 攻击";
                            }
                        }
                    }
                }
                builder.setRs(rs);
                if (rs >= 0) {
                    chat.vipLevel = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
                    chat.iconId = playerProxy.getIconId();
                    chat.legionId = playerProxy.getArmGrouId();
                    chat.playerId = playerProxy.getPlayerId();
                    chat.playerName = playerProxy.getPlayerName();
                    chat.time = GameUtils.getServerDate().getTime();
                    chat.type = shareType;
                    chat.level = playerProxy.getLevel();
                    chat.legionName = playerProxy.getLegionName();
                    builder.setChat(playerProxy.getChatInfo(chat));
                    builder.setId(id);
                    builder.setShareType(shareType);
                    builder.setType(type);
                    // 发送到playerServer里面去分发到每个玩家
                    sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new ShareMsg(builder.build(), legionId));
                } else {
                    sendNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, builder.build());
                }
                setChatTime(time);
            } else {
                if(rs == 0){
                    rs = ErrorCodeDefine.M250000_5;
                }
                builder.setRs(rs);
                sendNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, builder.build());
            }

        } else {
            legionId=0;
            if ( Legintime >= leginChatTime + 30) {

                switch (type) {
                    case 1: {//佣兵
                        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                        id = c2s.getTypeId();
                        Common.SoldierInfo soldierInfo = soldierProxy.getSoldierInfo((int) id);
                        if (soldierInfo == null || soldierInfo.getNum() == 0) {
                            rs = ErrorCodeDefine.M250000_1;
                        } else {
                            builder.setSoldierInfo(soldierInfo);
                            JSONObject soldier = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldierInfo.getTypeid());
                            chat.context = soldier.getString("name");
                            builder.setTypeId((int) id);
                        }
                        break;
                    }
                    case 2: {//竞技场战报
                        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
                        M20.PerDetailInfos detail = mailProxy.getArenaReoprtDetailInfoById(id);
                        if (detail == null) {
                            rs = ErrorCodeDefine.M250000_2;
                        } else {
                            builder.setAreanInfo(detail);
                            if (detail.getType() == 1) {
                                chat.context = "竞技场战报：挑战 " + detail.getProtect().getName();
                            } else {
                                chat.context = "竞技场战报：被 " + detail.getAttack().getName() + " 挑战";
                            }
                        }
                        break;
                    }
                    case 3: {
                        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
                        M16.MailDetalInfo.Builder mailBuilder = M16.MailDetalInfo.newBuilder();
                        rs = mailProxy.getDetalInfo(id, mailBuilder);
                        if (rs < 0) {
                            rs = ErrorCodeDefine.M250000_3;
                        } else {
                            M16.Report.Builder reportBuilder = M16.Report.newBuilder(mailBuilder.getInfos());
                            if (shareType == ChatAndMailDefine.CHAT_TYPE_LEGION) {
                                if ((reportBuilder.getIsPerson() == 1 && reportBuilder.getMailType() != ReportDefine.REPORT_TYPE_BE_ATTACK) ||
                                        (reportBuilder.getIsPerson() == 0 && reportBuilder.getMailType() == ReportDefine.REPORT_TYPE_ATTACK
                                                && reportBuilder.getResourcePanel().getCityIcon() < ActorDefine.WORLD_RESOURCE_ICON_MAX)) {
                                    M16.TargetInfo.Builder targetBuilder = M16.TargetInfo.newBuilder(reportBuilder.getInfoPanel());
                                    targetBuilder.setPosX(-1);
                                    targetBuilder.setPosY(-1);
                                    reportBuilder.setInfoPanel(targetBuilder.build());
                                }
                            } else {
                                M16.TargetInfo.Builder targetBuilder = M16.TargetInfo.newBuilder(reportBuilder.getInfoPanel());
                                targetBuilder.setPosX(-1);
                                targetBuilder.setPosY(-1);
                                reportBuilder.setInfoPanel(targetBuilder.build());
                            }
                            mailBuilder.setInfos(reportBuilder.build());
                            builder.setReportInfo(mailBuilder.build());
                            int reportType = mailBuilder.getInfos().getMailType();
                            if (reportType == ReportDefine.REPORT_TYPE_SPY) {
                                chat.context = "侦查  " + mailBuilder.getInfos().getInfoPanel().getName() + " " + mailBuilder.getInfos().getInfoPanel().getLevel();
                            } else if (reportType == ReportDefine.REPORT_TYPE_ATTACK) {
                                chat.context = "攻击 " + mailBuilder.getInfos().getInfoPanel().getName() + " " + mailBuilder.getInfos().getInfoPanel().getLevel();
                            } else {
                                chat.context = "遭到 " + mailBuilder.getInfos().getInfoPanel().getAim() + " " + mailBuilder.getInfos().getInfoPanel().getLevel() + " 攻击";
                            }
                        }
                    }
                }
                builder.setRs(rs);
                if (rs >= 0) {
                    chat.vipLevel = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
                    chat.iconId = playerProxy.getIconId();
                    chat.legionId = playerProxy.getArmGrouId();
                    chat.playerId = playerProxy.getPlayerId();
                    chat.playerName = playerProxy.getPlayerName();
                    chat.time = GameUtils.getServerDate().getTime();
                    chat.type = shareType;
                    chat.legionName = playerProxy.getLegionName();
                    chat.level = playerProxy.getLevel();
                    builder.setChat(playerProxy.getChatInfo(chat));
                    builder.setId(id);
                    builder.setShareType(shareType);
                    builder.setType(type);
                    // 发送到playerServer里面去分发到每个玩家
                    sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, new ShareMsg(builder.build(), legionId));
                } else {
                    sendNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, builder.build());
                }
                setLeginChatTime(Legintime);
            } else {
                builder.setRs(ErrorCodeDefine.M250000_5);
                sendNetMsg(ProtocolModuleDefine.NET_M25, ProtocolModuleDefine.NET_M25_C250000, builder.build());
            }


        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M25_C250000);
    }


    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {
//       int cmd=request.getCmd();

    }
}
