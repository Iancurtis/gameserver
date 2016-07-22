package com.znl.modules.mail;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.define.*;
import com.znl.proto.M2;
import com.znl.template.MailTemplate;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.framework.socket.Request;
import com.znl.log.admin.tbllog_mail;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Report;
import com.znl.proto.M16;
import com.znl.proto.M5;
import com.znl.proxy.GameProxy;
import com.znl.proxy.MailProxy;
import com.znl.proxy.PlayerProxy;
import com.znl.proxy.RewardProxy;
import com.znl.utils.GameUtils;

import java.util.*;

/**
 * Created by Administrator on 2015/12/4.
 */
public class MailModule  extends BasicModule {

    public static Props props(final GameProxy gameProxy){
        return Props.create(new Creator<MailModule>(){
            private static final long serialVersionUID = 1L;
            @Override
            public MailModule create() throws Exception {
                return new MailModule(gameProxy) ;
            }
        });
    }

    public MailModule(GameProxy gameProxy){
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M16);
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.ReceiveMailNotice){
            MailTemplate mailTemplate = ((GameMsg.ReceiveMailNotice) anyRef).mailTemplate();
            getNewMailNotify(mailTemplate);
//        }else if (anyRef instanceof GameMsg.GetPlayerSimpleInfoSuccess){
//            SimplePlayer simplePlayer = ((GameMsg.GetPlayerSimpleInfoSuccess) anyRef).simplePlayer();
//            String cmd = ((GameMsg.GetPlayerSimpleInfoSuccess) anyRef).cmd();
//            getSimpleInfoHandle(simplePlayer,cmd);
        }else if (anyRef instanceof GameMsg.ReceiveReportNotice){
            Report report = ((GameMsg.ReceiveReportNotice) anyRef).report();
            getNewReportNotify(report);
        }else if(anyRef instanceof GameMsg.ReceiveArenaReportNotice){
            Report report = ((GameMsg.ReceiveArenaReportNotice) anyRef).report();
            getNewArenaReportNotify(report);
        }else if (anyRef instanceof GameMsg.GetPlayerSimpleInfoListSuccess){
            List<SimplePlayer> simplePlayers = ((GameMsg.GetPlayerSimpleInfoListSuccess) anyRef).simplePlayer();
            String cmd = ((GameMsg.GetPlayerSimpleInfoListSuccess) anyRef).cmd();
            getSimpleInfoHandle(simplePlayers, cmd);
        }else if (anyRef instanceof GameMsg.GetBattleProtoSuccess){
            GameMsg.GetBattleProtoSuccess mess = (GameMsg.GetBattleProtoSuccess) anyRef;
            onGetBattleProtoSucessHandle(mess);
        }else if (anyRef instanceof GameMsg.AddlaterPlayer){
            long playerid=( (GameMsg.AddlaterPlayer) anyRef).playerid();
            PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
            List<Long> laterlist=playerProxy.getlaterlist();
            laterlist.remove(playerid);
            laterlist.remove(playerProxy.getPlayerId());
            String str1="";
            for(long id:  laterlist){
                str1=str1+"_"+id;
            }
            playerProxy.setLaterPlayer(str1);
            playerProxy.setLaterPlayer(playerid);
            List<Long> laternewlist=playerProxy.getlaterlist();
            if(laternewlist.size()>ChatAndMailDefine.LATERMAX) {
                laternewlist.remove(ChatAndMailDefine.LATERMAX);
            }
            String str="";
            for(long id: laternewlist){
                str=str+"_"+id;
            }
            playerProxy.setLaterPlayer(str);
        }
    }



    private void onGetBattleProtoSucessHandle(GameMsg.GetBattleProtoSuccess mess) {
        String cmd = mess.cmd();
        if ((ProtocolModuleDefine.NET_M16_C160005+"").equals(cmd)){
            if (mess.msg() == null){
                M16.M160005.S2C.Builder builder = M16.M160005.S2C.newBuilder();
                builder.setRs(ErrorCodeDefine.M160005_3);
                pushNetMsg(ActorDefine.MAIL_MODULE_ID, ProtocolModuleDefine.NET_M16_C160005, builder.build());
            }else {
                M5.M50000.S2C battleMess = (M5.M50000.S2C) mess.msg();
                M5.M50000.S2C.Builder builder = M5.M50000.S2C.newBuilder(battleMess);
                builder.setRc(2);
                pushNetMsg(ActorDefine.BATTLE_MODULE_ID, ProtocolModuleDefine.NET_M5_C50000, battleMess);
            }
        }
        sendPushNetMsgToClient();
    }

    private void getNewArenaReportNotify(Report report) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.addReport(report);
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        Long id = mailProxy.createArenaReport(report);
      //  sendModuleMsg(ActorDefine.ARENA_MODULE_NAME,new GameMsg.NewArenaReportNotify());
    }

    private void getNewReportNotify(Report report){
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.addReport(report);
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        Long id = mailProxy.createReport(report);
        //检查邮箱是否已满
        boolean full = mailProxy.checkMailFullHandle();
        pushNetMsg(ActorDefine.MAIL_MODULE_ID, ProtocolModuleDefine.NET_M16_C160002, sendNewMailNotifyToClient());
        if (full == true){
            M16.M160000.S2C mess = getMailListToClientMess();
            pushNetMsg(ActorDefine.MAIL_MODULE_ID, ProtocolModuleDefine.NET_M16_C160000, mess);
        }
        if(report.getReportType() == ReportDefine.REPORT_TYPE_SPY){
            sendModuleMsg(ActorDefine.MAP_MODULE_NAME,new GameMsg.GetSpyReportNotify(id));
        }else {
            sendPushNetMsgToClient();
        }

    }

    private void getNewMailNotify(MailTemplate mailTemplate){
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.isShield(mailTemplate.getSenderId(),ChatAndMailDefine.SHIELD_TYPE_MAIL)){
            return;//屏蔽列表中的玩家就不接收了
        }
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        mailProxy.createMail(mailTemplate);
        //检查邮箱是否已满
        boolean full = mailProxy.checkMailFullHandle();
        pushNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160002,sendNewMailNotifyToClient());
        if (full == true){
            pushNetMsg(ActorDefine.MAIL_MODULE_ID, ProtocolModuleDefine.NET_M16_C160000, getMailListToClientMess());
        }
        sendPushNetMsgToClient();
    }

    private void getSimpleInfoHandle(List<SimplePlayer> simplePlayers, String cmd) {
        if ("160003".equals(cmd)){
            M16.M160003.S2C.Builder builder = M16.M160003.S2C.newBuilder();
            MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
            if (simplePlayers == null || simplePlayers.size() == 0){
                builder.setRs(ErrorCodeDefine.M160003_2);
            }else if (title == null){
                builder.setRs(ErrorCodeDefine.M160003_1);
            }else {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                MailTemplate mailTemplate = new MailTemplate(title,text,playerProxy.getPlayerId(),playerProxy.getPlayerName(), ChatAndMailDefine.MAIL_TYPE_INBOX);
                HashSet<Long> list = new HashSet<>();
                List<Long> addList = new ArrayList<>();
                for (SimplePlayer simplePlayer : simplePlayers){
                    list.add(simplePlayer.getId());
                    long id = mailProxy.createSendingMail(simplePlayer,text,title);
                    sendmMaillog(id,simplePlayer.getId(),simplePlayer.getName());//邮件发送日志
                    addList.add(id);
                }
                GameMsg.SendMail mess = new GameMsg.SendMail(list,mailTemplate,playerProxy.getPlayerName(),playerProxy.getPlayerId());
                sendServiceMsg(ActorDefine.MAIL_SERVICE_NAME,mess);
                builder.setRs(0);
                for (Long id : addList){
                    builder.addInfo(mailProxy.getMailShortInfo(id));
                }
                for (SimplePlayer simplePlayer : simplePlayers){
                    if (reciverNames.contains(simplePlayer.getName()) == false){
                        builder.addNotGetNameList(simplePlayer.getName());
                    }
                }
            }
            pushNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160003,builder.build());
            //检查邮箱是否已满
            boolean full = mailProxy.checkMailFullHandle();
            if (full){
                pushNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160000,getMailListToClientMess());
            }
            sendPushNetMsgToClient();
            text = null;
            title = null;
            reciverNames = null;
        }
    }

    private String title;
    private String text;
    private ArrayList<String> reciverNames;

    private void OnTriggerNet160006Event(Request request) {
        M16.M160006.C2S proto = request.getValue();
        long mailId = proto.getMailId();
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        int rs = mailProxy.extractMail(mailId,reward);
        M16.M160006.S2C.Builder builder = M16.M160006.S2C.newBuilder();
        builder.setRs(rs);
        sendNetMsg(ActorDefine.MAIL_MODULE_ID, ProtocolModuleDefine.NET_M16_C160006, builder.build());
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (rs==0){
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
        }
        //阵型
        sendModuleMsg(ActorDefine.TROOP_MODULE_NAME,new GameMsg.CheckBaseDefendFormation());
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet160005Event(Request request) {
        M16.M160005.C2S proto = request.getValue();
        long battleId = proto.getBattleId();
        GameMsg.GetBattleProto mess = new GameMsg.GetBattleProto(battleId,"160005");
        sendServiceMsg(ActorDefine.BATTLE_REPORT_SERVICE_NAME,mess);
    }


    private void OnTriggerNet160004Event(Request request) {
        M16.M160004.C2S proto = request.getValue();
        List<Long> ids = proto.getIdlistList();
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        int rs = mailProxy.deleteMail(ids);
        M16.M160004.S2C.Builder builder = M16.M160004.S2C.newBuilder();
        builder.setRs(rs);
        builder.addAllIdlist(ids);
        sendNetMsg(ActorDefine.MAIL_MODULE_ID, ProtocolModuleDefine.NET_M16_C160004, builder.build());
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet160003Event(Request request) {
        if (title != null){
            M16.M160003.S2C.Builder builder = M16.M160003.S2C.newBuilder();
            builder.setRs(ErrorCodeDefine.M160003_1);
            sendNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160003,builder.build());
            sendFuntctionLog(FunctionIdDefine.SEND_MAIL_FUNCTION_ID);
            return;
        }
        M16.M160003.C2S proto = request.getValue();
        String title = proto.getTitle();
        String text = proto.getContext();
        List<String> recName = proto.getNameList();
        ArrayList<String> names = new ArrayList<>(recName);
        GameMsg.GetPlayerSimpleInfoListByRoleNameList mess = new GameMsg.GetPlayerSimpleInfoListByRoleNameList(names,"160003");
        sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME,mess);
        this.text = text;
        this.title = title;
        this.reciverNames = names;

    }
    private void OnTriggerNet160002Event(Request request) {
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        mailProxy.checkMailFullHandle();
        sendNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160002,sendNewMailNotifyToClient());
        sendPushNetMsgToClient();
    }

    private M16.M160002.S2C sendNewMailNotifyToClient() {
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        int num = mailProxy.getUnreadMailNum();
        M16.M160002.S2C.Builder builder = M16.M160002.S2C.newBuilder();
        builder.setNum(num);
        return builder.build();
    }


    private void OnTriggerNet160001Event(Request request) {
        M16.M160001.C2S proto = request.getValue();
        long id = proto.getId();
//        testShowBattle(id);
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        M16.MailDetalInfo.Builder info = M16.MailDetalInfo.newBuilder();
        int rs = mailProxy.getDetalInfo(id, info);
        M16.M160001.S2C.Builder builder = M16.M160001.S2C.newBuilder();
        builder.setRs(rs);
        builder.setInfo(info.build());
        sendNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160001,builder.build());
        sendPushNetMsgToClient();
        /**
         *tbllog_mail 查看邮件日志
         */
       readMailLog((int)id,info.getPlayerId(),info.getName(),info.getTitle(),info.getContext(),info.getType());
        sendFuntctionLog(FunctionIdDefine.GET_MAIL_INFOS_FUNCTION_ID,id,0,0);
    }

    private M16.M160000.S2C getMailListToClientMess(){
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        List<M16.MailShortInfo> mails = mailProxy.getMailShortInfoList();
        M16.M160000.S2C.Builder builder = M16.M160000.S2C.newBuilder();
        builder.addAllMails(mails);
        builder.setRs(0);
        return builder.build();
    }

    private void OnTriggerNet160000Event(Request request) {
        MailProxy mailProxy = getProxy(ActorDefine.MAIL_PROXY_NAME);
        //检查邮箱是否已满
        mailProxy.checkMailFullHandle();
        sendNetMsg(ActorDefine.MAIL_MODULE_ID,ProtocolModuleDefine.NET_M16_C160000,getMailListToClientMess());
        sendFuntctionLog(FunctionIdDefine.GET_MAIL_LIST_FUNCTION_ID);
        sendPushNetMsgToClient();
    }

    /**
     * tbllog_mail 发送邮件日志
     */
    public void sendmMaillog(long id,long receiver_id,String receiver_name){
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_mail maillog = new tbllog_mail();
        maillog.setPlatform(cache.getPlat_name());
        maillog.setMail_id((int)id);
        maillog.setMail_sender_id(player.getPlayerId());
        maillog.setMail_sender_name(player.getAccountName());
        maillog.setMail_receiver_id(receiver_id);
        maillog.setMail_receiver_name(receiver_name);
        maillog.setMail_title(title);
        maillog.setMail_content(text);
        maillog.setMail_type(1);
        maillog.setMail_status(2);
        maillog.setHappend_time(GameUtils.getServerTime());
        sendLog(maillog);
    }

    /**
     *tbllog_mail 查看邮件日志
     */
    public void readMailLog(int id,long senderId,String senderName,String title,String context,int type){
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_mail maillog = new tbllog_mail();
        maillog.setPlatform(cache.getPlat_name());
        maillog.setMail_id(id);
        maillog.setMail_sender_id(senderId);
        maillog.setMail_sender_name(senderName);
        maillog.setMail_receiver_id(player.getPlayerId());
        maillog.setMail_receiver_name(player.getAccountName());
        maillog.setMail_title(title);
        maillog.setMail_content(context);
        maillog.setMail_type(type);
        maillog.setMail_status(1);
        maillog.setHappend_time(GameUtils.getServerTime());
        sendLog(maillog);
    }
    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }

}
