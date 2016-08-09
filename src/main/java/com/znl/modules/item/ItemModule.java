package com.znl.modules.item;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseLog;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.PlayerChat;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.ItemLog;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Armygroup;
import com.znl.pojo.db.set.RoleNameSetDb;
import com.znl.proto.Common;
import com.znl.proto.M2;
import com.znl.proto.M9;
import com.znl.proxy.*;
import com.znl.template.MailTemplate;
import com.znl.utils.GameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/11/6.
 */
public class ItemModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<ItemModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ItemModule create() throws Exception {
                return new ItemModule(gameProxy);
            }
        });
    }

    public ItemModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M9);
    }

    @Override
    public void onReceiveOtherMsg(Object object) {
        if (object instanceof GameMsg.GetPlayerIdByNameSucess) {
            Long playerId = ((GameMsg.GetPlayerIdByNameSucess) object).playerId();
            int typeId = ((GameMsg.GetPlayerIdByNameSucess) object).typeId();
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
            ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
            int rs = itemProxy.sendRedBag(playerId, typeId);
            M9.M90004.S2C.Builder builder = M9.M90004.S2C.newBuilder();
            if (rs == 0) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                MailTemplate mailTemplate = new MailTemplate(jsonObject.getString("name"), playerProxy.getPlayerName()+ "赠送你一个"+jsonObject.getString("name")+"记得来领哦", playerProxy.getPlayerId(), playerProxy.getPlayerName(), ChatAndMailDefine.MAIL_TYPE_SYSTEM);
                Set<Long> list = new LinkedHashSet<Long>();
                list.add(playerId);
                List<Integer> rewards = new ArrayList<>();
                JSONArray array=jsonObject.getJSONArray("effect");
                for(int i=0; i<array.length();i++){
                    rewards.add(array.getInt(i));
                }
                builder.addIteminfos(itemProxy.getItemInfo(typeId));
                mailTemplate.setRewards(rewards);
                GameMsg.SendMail mess = new GameMsg.SendMail(list, mailTemplate, playerProxy.getPlayerName(), playerProxy.getPlayerId());
                sendServiceMsg(ActorDefine.MAIL_SERVICE_NAME, mess);
                List<Integer> items=new ArrayList<Integer>();
                items.add(typeId);
                RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
                //TODO 发送更新
                M2.M20007.S2C itembuild=rewardProxy.getItemListClientInfo(items);
                pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, itembuild);
            }
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90004, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90004);
        } else if (object instanceof GameMsg.getAllLegionSucess) {
            String name = ((GameMsg.getAllLegionSucess) object).name();
            Map<Long, Armygroup> map = ((GameMsg.getAllLegionSucess) object).armymap();
            int typeId=((GameMsg.getAllLegionSucess) object).typeId();
            ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
            M9.M90004.S2C.Builder builder = M9.M90004.S2C.newBuilder();
            int rs=itemProxy.changLegionName(name,typeId,map);
            builder.setRs(rs);
            if(rs==0){
               GameMsg.changeLegionName msg=new  GameMsg.changeLegionName(name);
                PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
                tellMsgToArmygroupNode(msg,playerProxy.getArmGrouId());
                builder.addIteminfos(itemProxy.getItemInfo(typeId));
                playerProxy.setLegionName(name);
                sendLegionameDiffer();
                updateMySimplePlayerData();
            }
            pushNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90004, builder.build());
            sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90004);
        }
    }


    /***该协议已经屏蔽**/
    private void OnTriggerNet90000Event(Request request) {
        ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
        List<Common.ItemInfo> infos = new ArrayList<Common.ItemInfo>();
        M9.M90000.S2C.Builder builder = M9.M90000.S2C.newBuilder();
        int rs = itemProxy.getAllItemInfo(infos, builder);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90000, builder.build());
    }

    private void OnTriggerNet90001Event(Request request) {
        ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
        M9.M90001.C2S c2s = request.getValue();
        int typeId = c2s.getTypeId();
        int num = c2s.getNum();
        int costType = c2s.getCostType();
        PlayerReward reward = new PlayerReward();
        ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        int expandBbuildoldSize = itemBuffProxy.getValidBuildSize();
        int rs = itemProxy.useitem(typeId, num, reward, costType);
        M9.M90001.S2C.Builder builder = M9.M90001.S2C.newBuilder();
        builder.setRs(rs);
        builder.setNum(num);
        builder.setTypeId(typeId);
        if (rs == 0) {
            builder.addIteminfos(itemProxy.getItemInfo(typeId));
            BaseLog baseLog = new ItemLog(typeId, num);
            sendLog(baseLog);
            sendFuntctionLog(FunctionIdDefine.ITEM_EMPLOY_FUNCTION_ID,typeId,num,0);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90001, builder.build());
        if (rs == 0) {//生效的道具buff
            M9.M90003.S2C s2c90003=itemBuffProxy.getM90003EffectItemBufferByItemId(typeId);
            if(s2c90003!=null&&s2c90003.getItemBuffInfoCount()>0){
                pushNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90003, s2c90003);
            }
           // ItemBuffProxy ibfp = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
           // sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, new GameMsg.RefeshItemBuff());

            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
            int expandBbuildnewSize = itemBuffProxy.getValidBuildSize();
            if(expandBbuildnewSize>expandBbuildoldSize){
                ResFunBuildProxy resFunBuildProxy=getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                resFunBuildProxy.changetAutoBuildState(1);
            }
            //阵型
            sendModuleMsg(ActorDefine.TROOP_MODULE_NAME, new GameMsg.CheckBaseDefendFormation());
            if (reward.soldierMap.size() > 0){
                sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
            }
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
            if(jsonObject.getInt("type")==ItemDefine.ITEM_REWARD_AVOID_WAR){
               updateMySimplePlayerData();
            }
//            int type = jsonObject.getInt("type");
//            if(type==ItemDefine.ITEM_REWARD_AVOID_WAR){
//                List<Integer> poweerlist=new ArrayList<Integer>();
//                poweerlist.add(PlayerPowerDefine.NOR_POWER_protect_date);
//                sendDifferent(poweerlist);
//            }
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90001);
    }

    private void OnTriggerNet90007Event(Request request) {
        M9.M90007.C2S c2s = request.getValue();
        int typeId=c2s.getTypeId();
        int num=c2s.getNum();
        ItemProxy itemProxy=getProxy(ActorDefine.ITEM_PROXY_NAME);
        int rs=itemProxy.useLegionItem(typeId,num);
        M9.M90007.S2C.Builder builder= M9.M90007.S2C.newBuilder();
        if(rs<0){
            builder.setRs(rs);
        }else{
            PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
            tellMsgToArmygroupNode(new GameMsg.addShareValue(playerProxy.getPlayerId(),rs),playerProxy.getArmGrouId());
            builder.setRs(0);
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            List<Integer> list =new ArrayList<Integer>();
            list.add(typeId);
            M2.M20007.S2C build20007=rewardProxy.getItemListClientInfo(list);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, build20007);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90007, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90007);
    }

    private void OnTriggerNet90003Event(Request request) {
//        //生效的道具buff
//        ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
//        M9.M90003.S2C.Builder m9s2c = M9.M90003.S2C.newBuilder();
//        m9s2c.setRs(0);
//        m9s2c.addAllItemBuffInfo(itemBuffProxy.sendItemBuffInfoToClient());
//        sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90003, m9s2c.build());
        sendModuleMsg(ActorDefine.SYSTEM_MODULE_NAME, new GameMsg.RefeshItemBuff());
    }


    private void OnTriggerNet90004Event(Request request) {
        M9.M90004.C2S c2s = request.getValue();
        String name = c2s.getName();
        int typeId = c2s.getTypeId();
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
        ItemProxy itemProxy = getProxy(ActorDefine.ITEM_PROXY_NAME);
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (jsonObject.getInt("type") == ItemDefine.ITEM_SEND_RED_EVEOPE) {
            String areaKey = playerProxy.getAreaKey();
            RoleNameSetDb roleNameSetDb = BaseSetDbPojo.getSetDbPojo(RoleNameSetDb.class, areaKey);
            Boolean isRepeat = roleNameSetDb.isKeyExist(name);
            if (isRepeat) {
                GameMsg.GetPlayerIdByName msg = new GameMsg.GetPlayerIdByName(name, typeId);
                sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, msg);
            } else {
                M9.M90004.S2C.Builder builder = M9.M90004.S2C.newBuilder();
                builder.setRs(ErrorCodeDefine.M90004_10);
                sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90004, builder.build());
            }
        }
        if (jsonObject.getInt("type") == ItemDefine.ITEM_CHANGE_NAME) {
            M9.M90004.S2C.Builder builder = M9.M90004.S2C.newBuilder();
            int rs = itemProxy.changPlayerName(name, typeId);
            builder.setRs(rs);
            if (rs == 0) {
                updateMySimplePlayerData();
                GameMsg.changeMenberName msg=new GameMsg.changeMenberName(name,playerProxy.getPlayerId());
                tellMsgToArmygroupNode(msg,playerProxy.getArmGrouId());
                builder.addIteminfos(itemProxy.getItemInfo(typeId));
                M2.M20008.S2C.Builder builder20008=M2.M20008.S2C.newBuilder();
                builder20008.setRs(0);
                builder20008.setName(name);
                sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20008, builder20008.build());
            }
            sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90004, builder.build());
        }
        if (jsonObject.getInt("type") == ItemDefine.ITEM_CHANGE_UNION_NAME) {
            if (playerProxy.getArmGrouId() == 0||playerProxy.getPlayer().getPost()!=ArmyGroupDefine.JOB_MANGER) {
                M9.M90004.S2C.Builder builder = M9.M90004.S2C.newBuilder();
                if(playerProxy.getArmGrouId() == 0) {
                    builder.setRs(ErrorCodeDefine.M90004_6);
                }else{
                    builder.setRs(ErrorCodeDefine.M90004_12);
                }
                sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90004, builder.build());
            } else {
                GameMsg.getAllLegion msg = new GameMsg.getAllLegion(name,typeId);
                sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
            }

        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90004);
    }


    private void OnTriggerNet90005Event(Request request) {
        PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M9.M90005.S2C.Builder builder = M9.M90005.S2C.newBuilder();
        if(playerProxy.getPlayer().getFacadeendTime()> GameUtils.getServerDate().getTime()){
            builder.setTypeId(playerProxy.getPlayer().getFacade());
        }else{
            builder.setTypeId(0);
        }
         builder.setRs(0);
        sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90005, builder.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90005);
    }


    private void OnTriggerNet90006Event(Request request) {
        PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy=getProxy(ActorDefine.ITEM_PROXY_NAME);
        M9.M90006.C2S c2S=request.getValue();
        int typeId=c2S.getTypeId();
        String mess=c2S.getMess();
        M9.M90006.S2C.Builder builder = M9.M90006.S2C.newBuilder();
        int rs=itemProxy.trumpeItem(typeId,mess);
        builder.setRs(rs);
        if(rs==0){
            List<Integer> list=new ArrayList<Integer>();
            RewardProxy rewardProxy=getProxy(ActorDefine.REWARD_PROXY_NAME);
            list.add(typeId);
            M2.M20007.S2C itembuild=rewardProxy.getItemListClientInfo(list);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, itembuild);
        }
        sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90006, builder.build());
        if(rs==0){
            sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME,new GameMsg.trumpeNotity(playerProxy.getPlayerId(),playerProxy.getPlayerName(),mess,typeId));
            PlayerChat chat=new PlayerChat();
            chat.type=ChatAndMailDefine.CHAT_TYPE_WORLD;
            chat.legionId=playerProxy.getArmGrouId();
            chat.context=mess;
            chat.iconId=playerProxy.getIconId();
            chat.playerId=playerProxy.getPlayerId();
            chat.playerName=playerProxy.getPlayerName();
            chat.vipLevel = (int)playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
            chat.playerType = playerProxy.getPlayerType();
            chat.level = playerProxy.getLevel();
            chat.legionName = playerProxy.getLegionName();
            sendModuleMsg(ActorDefine.CHAT_MODULE_NAME,new GameMsg.sendAChat(chat));
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90006);
    }


    /*****该方法已经屏蔽******/
  /*  private void send90000toClient(List<Common.ItemInfo> list) {
        if (list.size() > 0) {
            M9.M90000.S2C.Builder builder = M9.M90000.S2C.newBuilder();
            builder.setRs(0);
            builder.addAllIteminfos(list);
            sendNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90000, builder.build());
        }

    }*/

    /***********
     * 获取所有道具信息
     ***/
    public int getAllIntemInfo() {
        return 0;
    }

    private void tellMsgToArmygroupNode(Object mess, Long id) {
        context().actorSelection("../../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE + id).tell(mess, self());
    }


    private void sendLegionameDiffer() {
        PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M2.M20201.S2C.Builder different = M2.M20201.S2C.newBuilder();
        different.setName(playerProxy.getPlayer().getLegionName());
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20201, different.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M2_C20201);
    }


    /**
     * //buff倒计时完成后请求
     * @param request
     */
    private void OnTriggerNet90002Event(Request request) {
        M9.M90002.C2S c2S=request.getValue();
        int powerId=c2S.getPowerId();
        int type=c2S.getType();
        ItemBuffProxy itemBuffProxy=getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        M9.M90002.S2C.Builder builder90002=M9.M90002.S2C.newBuilder();
        M9.M90003.S2C.Builder builder90003=M9.M90003.S2C.newBuilder();
        itemBuffProxy.checkBufferIsOverTime(powerId,type,builder90002,builder90003);
        System.err.println("+++++++++++++检测buff返回:"+builder90002.getRs());
        System.err.println("+++++++++++++检测下一个buff返回:"+builder90003.getItemBuffInfoList().size());
        pushNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90002, builder90002.build());
        if(!builder90003.getItemBuffInfoList().isEmpty()){
            //刷新新的buffer
            pushNetMsg(ProtocolModuleDefine.NET_M9, ProtocolModuleDefine.NET_M9_C90003, builder90003.build());
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M9_C90002);
    }

    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {
        //检测itemBuff是否过期
        if(request.getCmd()==ProtocolModuleDefine.NET_M9_C90002){
            //返回itemBuff列表
            OnTriggerNet90003Event(null);
        }
    }

}
