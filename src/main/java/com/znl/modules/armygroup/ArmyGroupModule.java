package com.znl.modules.armygroup;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseDbPojo;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Armygroup;
import com.znl.pojo.db.ArmygroupMenber;
import com.znl.proto.Common;
import com.znl.proto.M19;
import com.znl.proto.M2;
import com.znl.proto.M22;
import com.znl.proxy.*;

import java.util.*;

/**
 * Created by Administrator on 2015/12/4.
 */
public class ArmyGroupModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<ArmyGroupModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public ArmyGroupModule create() throws Exception {
                return new ArmyGroupModule(gameProxy);
            }
        });
    }

    private String areaKey;

    public ArmyGroupModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M22);
        areaKey = gameProxy.getAreaKey();
        //军团科技属性加成
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long groupId = playerProxy.getArmGrouId();
        GameMsg.TechExpandPowerMap msg = new GameMsg.TechExpandPowerMap();
        tellMsgToArmygroupNode(msg, groupId);
        if (playerProxy.getArmGrouId() != 0) {
            GameMsg.checkArmy chemsg = new GameMsg.checkArmy(playerProxy.getArmGrouId());
            sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, chemsg);
            GameMsg.changeMenberLevel groupmsg = new GameMsg.changeMenberLevel(playerProxy.getPlayerId(),playerProxy.getLevel());
            tellMsgToArmygroupNode(groupmsg,playerProxy.getArmGrouId());
        }
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {
        if (anyRef instanceof GameMsg.createArmyArmyGroupSucess) {
            String name = ((GameMsg.createArmyArmyGroupSucess) anyRef).name();
            int jointype = ((GameMsg.createArmyArmyGroupSucess) anyRef).joinType();
            int way = ((GameMsg.createArmyArmyGroupSucess) anyRef).way();
            Map<Long, Armygroup> map = ((GameMsg.createArmyArmyGroupSucess) anyRef).armymap();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            M22.M220103.S2C.Builder builder = M22.M220103.S2C.newBuilder();
            long rs = armyGroupProxy.createArmyGroup(way, name, jointype, map);
            if (rs >= 0) {
                builder.setRs(0);
            } else {
                builder.setRs((int) rs);
            }
            if (rs >= 0) {
                builder.setLegionId(rs);
                Armygroup armygroup = BaseDbPojo.get(rs, Armygroup.class, areaKey);
                GameMsg.AddArmyGroup msg = new GameMsg.AddArmyGroup(armygroup);
                sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                for (long id : playerProxy.getPlayerApplylist()) {
                    GameMsg.removeApplyid msgremove = new GameMsg.removeApplyid(id);
                    tellMsgToArmygroupNode(msgremove, id);
                }
                playerProxy.setApplylist(new LinkedHashSet<>());
                updateMySimplePlayerData();
                sendModuleMsg(ActorDefine.CHAT_MODULE_NAME, new GameMsg.JoinLegionNotify());
                sendLegionameDiffer();
            }
            sendarmoupIdIdffer();
            List<Integer> list = new ArrayList<Integer>();
            list.add(PlayerPowerDefine.POWER_gold);
            list.add(201);
            list.add(202);
            list.add(203);
            list.add(204);
            list.add(205);
            M2.M20002.S2C different = sendDifferent(list);
            pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, different);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220103, builder.build());
            GameMsg.RefrshTip msg=new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME,msg);
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.getArmyGroupByidSucess) {
            Armygroup armygroup = ((GameMsg.getArmyGroupByidSucess) anyRef).apparm();
            int cmd = ((GameMsg.getArmyGroupByidSucess) anyRef).cmd();
            int icond=((GameMsg.getArmyGroupByidSucess) anyRef).icon();
            int pend=((GameMsg.getArmyGroupByidSucess) anyRef).peniCon();
            dogetArmyGroupByidSucess(armygroup, cmd,icond,pend);
        } else if (anyRef instanceof GameMsg.getArmyShop) {
            //军团商店
            Armygroup armygroup = ((GameMsg.getArmyShop) anyRef).armygroup();
            int itemId = ((GameMsg.getArmyShop) anyRef).itemId();
            int opt = ((GameMsg.getArmyShop) anyRef).opt();
            int typeId = ((GameMsg.getArmyShop) anyRef).typeId();
            ArmygroupMenber armygroupMenber = ((GameMsg.getArmyShop) anyRef).armymenber();
            int legionlv = armygroup.getLevel();
            M22.M220002.S2C.Builder s2c = M22.M220002.S2C.newBuilder();
            PlayerReward reward = new PlayerReward();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            s2c.setLegionlv(legionlv);
            if (opt == 0) {//opt=0显示面板
                if (typeId == 0) {
                    s2c.addAllCanGet(armyGroupProxy.showArmyShopItem(armygroup));
                } else {
                    s2c.addAllCanGet(armyGroupProxy.showArmyShopGemItem(armygroup));
                }
                s2c.setRs(0);
                s2c.setMyContribute(armygroupMenber.getContribute());
                s2c.setType(typeId);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220002, s2c.build());
                sendPushNetMsgToClient();
                sendFuntctionLog(FunctionIdDefine.LEGION_SHOP_GOODS_EXCHANGE_FUNCTION_ID);
            } else {//领取
                if (typeId == 0) {
                    int rs = armyGroupProxy.exchangeItem(armygroupMenber, armygroup, itemId, reward);
                    s2c.addAllCanGet(armyGroupProxy.showArmyShopItem(armygroup));
                    s2c.setRs(rs);
                    s2c.setMyContribute(armygroupMenber.getContribute());
                    s2c.setType(typeId);
                    if (rs == 0) {
                        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                        GameMsg.ArmyGroupShop msg = new GameMsg.ArmyGroupShop(playerProxy.getPlayerId(), itemId, 2, typeId);
                        long groupId = playerProxy.getArmGrouId();
                        tellMsgToArmygroupNode(msg, groupId);
                        GameMsg.editLegionFinish actimsg = new GameMsg.editLegionFinish(ArmyGroupDefine.MESSIONTYPE3, playerProxy.getPlayerId());
                        tellMsgToArmygroupNode(actimsg, playerProxy.getArmGrouId());
                        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                        M2.M20007.S2C rewardbuild = rewardProxy.getRewardClientInfo(reward);
                        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardbuild);
                        sendPushNetMsgToClient();
                        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                        M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_UNIOMCONVER_TIMES, 1, reward);
                        if(builder19!=null) {
                            sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                        }
                        sendActivitDiff();
                    } else {
                        pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220002, s2c.build());
                        sendPushNetMsgToClient();
                        sendFuntctionLog(FunctionIdDefine.LEGION_SHOP_GOODS_EXCHANGE_FUNCTION_ID);
                    }
                } else {
                    int rs = armyGroupProxy.exchangeGemItem(armygroupMenber, armygroup, itemId, reward);
                    s2c.addAllCanGet(armyGroupProxy.showArmyShopGemItem(armygroup));
                    s2c.setRs(rs);
                    s2c.setMyContribute(armygroupMenber.getContribute());
                    s2c.setType(typeId);
                    if (rs == 0) {
                        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                        long groupId = playerProxy.getArmGrouId();
                        GameMsg.ArmyGroupShop msg = new GameMsg.ArmyGroupShop(playerProxy.getPlayerId(), itemId, 2, typeId);
                        tellMsgToArmygroupNode(msg, groupId);
                        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                        M2.M20007.S2C rewardbuild = rewardProxy.getRewardClientInfo(reward);
                        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, rewardbuild);
                        sendPushNetMsgToClient();
                        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                        M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_UNIOMCONVER_TIMES, 1, reward);
                        if(builder19!=null) {
                            sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
                        }
                        sendActivitDiff();
                        GameMsg.editLegionFinish actimsg = new GameMsg.editLegionFinish(ArmyGroupDefine.MESSIONTYPE3, playerProxy.getPlayerId());
                        tellMsgToArmygroupNode(actimsg, playerProxy.getArmGrouId());
                    } else {
                        pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220002, s2c.build());
                        sendPushNetMsgToClient();
                        sendFuntctionLog(FunctionIdDefine.LEGION_SHOP_GOODS_EXCHANGE_FUNCTION_ID);
                    }
                }
                if (reward.soldierMap.size() > 0) {
                    sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
                }
            }
        } else if (anyRef instanceof GameMsg.getArmyShopSucess) {//军团商店兑换成功
            Armygroup armygroup = ((GameMsg.getArmyShopSucess) anyRef).armygroup();
            ArmygroupMenber armygroupMenber = ((GameMsg.getArmyShopSucess) anyRef).armymenber();
            int typeId = ((GameMsg.getArmyShopSucess) anyRef).typeId();
            int legionlv = armygroup.getLevel();
            M22.M220002.S2C.Builder s2c = M22.M220002.S2C.newBuilder();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            if (typeId == 0) {
                s2c.addAllCanGet(armyGroupProxy.showArmyShopItem(armygroup));
            } else {
                s2c.addAllCanGet(armyGroupProxy.showArmyShopGemItem(armygroup));
            }
            s2c.setLegionlv(legionlv);
            s2c.setRs(100);
            s2c.setMyContribute(armygroupMenber.getContribute());
            s2c.setType(typeId);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220002, s2c.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.notiyKickArmy) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.setPost(0);
            playerProxy.setArmgroupId(0l);
            playerProxy.setLegionName("");
            playerProxy.getPlayer().setLegionLevel(1);
            playerProxy.savePlayer();
            sendarmoupIdIdffer();
            updateMySimplePlayerData();
            sendModuleMsg(ActorDefine.CHAT_MODULE_NAME, new GameMsg.LeaveLegionNotify());
            clearTechPlayerPower();
            setLegionLevelDiff(1);
            sendLegionameDiffer();
            updateMySimplePlayerData();
        } else if (anyRef instanceof GameMsg.notiyTrueManger) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.setPost(ArmyGroupDefine.JOB_MANGER);
            playerProxy.savePlayer();
        } else if (anyRef instanceof GameMsg.notiyCancelApply) {
            long id = ((GameMsg.notiyCancelApply) anyRef).id();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            Set<Long> list = playerProxy.getPlayerApplylist();
            list.remove(id);
            playerProxy.setApplylist(list);
            playerProxy.savePlayer();
        } else if (anyRef instanceof GameMsg.notiychangeJob) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int job = ((GameMsg.notiychangeJob) anyRef).job();
            playerProxy.setPost(job);
            playerProxy.savePlayer();
        } else if (anyRef instanceof GameMsg.notiyaddArmgroup) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            Armygroup armygroup = ((GameMsg.notiyaddArmgroup) anyRef).armygroup();
            playerProxy.setArmgroupId(armygroup.getId());
            playerProxy.setLegionName(armygroup.getName());
            playerProxy.getPlayer().setLegionLevel(armygroup.getLevel());
            playerProxy.setPost(ArmyGroupDefine.JOB_NORMAL);
            playerProxy.setApplylist(new HashSet<>());
            playerProxy.savePlayer();
            sendarmoupIdIdffer();
            sendModuleMsg(ActorDefine.CHAT_MODULE_NAME, new GameMsg.JoinLegionNotify());
            //请求科技增益信息
            getLegionTechnologyPowerMapFromService();
            setLegionLevelDiff(armygroup.getLevel());
            sendLegionameDiffer();
            updateMySimplePlayerData();
        } else if (anyRef instanceof GameMsg.applyArmyJoinBack) {
            int rs = ((GameMsg.applyArmyJoinBack) anyRef).rs();
            Armygroup armygroup = ((GameMsg.applyArmyJoinBack) anyRef).army();
            int type = ((GameMsg.applyArmyJoinBack) anyRef).retype();
            Long armyId = ((GameMsg.applyArmyJoinBack) anyRef).armyId();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M22.M220102.S2C.Builder builder = M22.M220102.S2C.newBuilder();
            builder.setType(type);
            builder.setId(armyId);
            if (rs == 2) {
                builder.setType(3);
                playerProxy.setArmgroupId(armygroup.getId());
                playerProxy.setLegionName(armygroup.getName());
                playerProxy.getPlayer().setLegionLevel(armygroup.getLevel());
                playerProxy.setApplylist(new HashSet<>());
                playerProxy.savePlayer();
                updateMySimplePlayerData();
                sendModuleMsg(ActorDefine.CHAT_MODULE_NAME, new GameMsg.JoinLegionNotify());
                //请求科技增益信息
                getLegionTechnologyPowerMapFromService();
                setLegionLevelDiff(armygroup.getLevel());
                sendLegionameDiffer();
                updateMySimplePlayerData();
            }
            if (rs == 1) {
                Set<Long> applist = playerProxy.getPlayerApplylist();
                applist.add(armygroup.getId());
                playerProxy.setApplylist(applist);
                playerProxy.savePlayer();
            }
            if (rs == 3) {
                Set<Long> applist = playerProxy.getPlayerApplylist();
                applist.remove(armygroup.getId());
                playerProxy.setApplylist(applist);
                playerProxy.savePlayer();
            }
            if (rs >= 0) {
                builder.setRs(0);
            } else {
                builder.setRs(rs);
            }
            builder.setId(armygroup.getId());
            sendarmoupIdIdffer();
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220102, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.opeRateArmyBack) {
            int rs = ((GameMsg.opeRateArmyBack) anyRef).rs();
            int type = ((GameMsg.opeRateArmyBack) anyRef).retype();
            int oldjob = ((GameMsg.opeRateArmyBack) anyRef).oldJob();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (rs == 0 && type == ArmyGroupDefine.OPERATE_transfer) {
                playerProxy.setPost(oldjob);
            }
            if (rs == 0 && type == ArmyGroupDefine.OPERATE_Level) {
                playerProxy.setArmgroupId(0l);
                playerProxy.setLegionName("");
                playerProxy.getPlayer().setLegionLevel(1);
                sendarmoupIdIdffer();
                updateMySimplePlayerData();
                sendModuleMsg(ActorDefine.CHAT_MODULE_NAME, new GameMsg.LeaveLegionNotify());
                //清除公会增益
                clearTechPlayerPower();
                setLegionLevelDiff(1);
                sendLegionameDiffer();
            }
            List<M22.LegionMemberInfo> list = ((GameMsg.opeRateArmyBack) anyRef).legInfos();
            Long otherid = ((GameMsg.opeRateArmyBack) anyRef).playerid();
            M22.M220201.S2C.Builder builder = M22.M220201.S2C.newBuilder();
            builder.setRs(rs);
            builder.setType(type);
            builder.addAllInfo(list);
            builder.setId(otherid);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220201, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.LEGION_MENBER_OPERATE_FUNCTION_ID,type,otherid,0);
            updateMySimplePlayerData();
        } else if (anyRef instanceof GameMsg.clearApplylistBack) {
            int rs = ((GameMsg.clearApplylistBack) anyRef).rs();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M22.M220204.S2C.Builder builder = M22.M220204.S2C.newBuilder();
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220204, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.editArmyGroupback) {
            int rs = ((GameMsg.editArmyGroupback) anyRef).rs();
            int jointype = ((GameMsg.editArmyGroupback) anyRef).joinType();
            List<Integer> list = ((GameMsg.editArmyGroupback) anyRef).list();
            long capity = ((GameMsg.editArmyGroupback) anyRef).capity();
            int level = ((GameMsg.editArmyGroupback) anyRef).level();
            String content = ((GameMsg.editArmyGroupback) anyRef).content();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            M22.M220210.S2C.Builder builder = M22.M220210.S2C.newBuilder();
            builder.setRs(rs);
            builder.setJoinType(jointype);
            builder.setJoinCond1(level);
            builder.setJoinCond2(capity);
            builder.setNotice(content);
            builder.addAllUpdateList(list);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220210, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.COMPILE_LEGION_MANIFESTO_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.editJobNameback) {
            int rs = ((GameMsg.editJobNameback) anyRef).rs();
            List<M22.LegionCustomJobShortInfo> list = ((GameMsg.editJobNameback) anyRef).list();
            M22.M220220.S2C.Builder builder = M22.M220220.S2C.newBuilder();
            builder.setRs(rs);
            builder.addAllInfos(list);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220220, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.POST_COMPILE_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.setorUpJobBack) {
            int rs = ((GameMsg.setorUpJobBack) anyRef).rs();
            int type = ((GameMsg.setorUpJobBack) anyRef).retype();
            long otherid = ((GameMsg.setorUpJobBack) anyRef).otherId();
            int job = ((GameMsg.setorUpJobBack) anyRef).job();
            int up = ((GameMsg.setorUpJobBack) anyRef).upjob();
            List<M22.LegionMemberInfo> list = ((GameMsg.setorUpJobBack) anyRef).legInfos();
            M22.M220221.S2C.Builder builder = M22.M220221.S2C.newBuilder();
            builder.addAllInfo(list);
            builder.setRs(rs);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (rs == 0 && type == 2) {
                playerProxy.setPost(up);
                builder.setJob(up);
            } else {
                builder.setJob(job);
            }
            builder.setType(type);
            builder.setId(otherid);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220221, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.agreeApplyBack) {
            int rs = ((GameMsg.agreeApplyBack) anyRef).rs();
            long id = ((GameMsg.agreeApplyBack) anyRef).armId();
            int type = ((GameMsg.agreeApplyBack) anyRef).retype();
            M22.LegionMemberInfo menber = ((GameMsg.agreeApplyBack) anyRef).info();
            M22.M220203.S2C.Builder builder = M22.M220203.S2C.newBuilder();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            builder.setRs(rs);
            builder.setType(type);
            builder.setRs(rs);
            builder.setLegionId(id);
            if (rs == 0 && type == 1) {
                builder.setMemberInfo(menber);
            }
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220203, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.getAllArmyGroupSucess) {
            int cmd = ((GameMsg.getAllArmyGroupSucess) anyRef).cmd();
            Object object = ((GameMsg.getAllArmyGroupSucess) anyRef).obj();
            Map<Long, Armygroup> map = ((GameMsg.getAllArmyGroupSucess) anyRef).armymap();
            dogetAllArmyGroupSucess(map, cmd, object);
        } else if (anyRef instanceof GameMsg.getMyGroupInfosback) {
            Armygroup armygroup = ((GameMsg.getMyGroupInfosback) anyRef).armygroup();
            List<ArmygroupMenber> list = ((GameMsg.getMyGroupInfosback) anyRef).list();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            M22.M220200.S2C.Builder builder = M22.M220200.S2C.newBuilder();
            int rs = armyGroupProxy.getMyGroupInfo(armygroup, list, builder);
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220200, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.GET_LEGION_INFO_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.lookAppListback) {
            List<SimplePlayer> simplePlayers = ((GameMsg.lookAppListback) anyRef).list();
            M22.M220202.S2C.Builder builder = M22.M220202.S2C.newBuilder();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            int rs = armyGroupProxy.getApplyList(simplePlayers, builder);
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220202, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.GET_APPROVAL_LISTS_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.GetTechExpandPowerMap) {//军团科技属性加成
            Map<Integer, Long> techExpandPower = ((GameMsg.GetTechExpandPowerMap) anyRef).techExpandPowerMap();
            refLegionTechnologyPowerMap(techExpandPower);
        } else if (anyRef instanceof GameMsg.GetWelfareRes) {//军团福利资源
            Map<Integer, Integer> resMap = ((GameMsg.GetWelfareRes) anyRef).resMap();
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            long groupId = playerProxy.getArmGrouId();
            GameMsg.GetWelfareRes msg = new GameMsg.GetWelfareRes(resMap);
            tellMsgToArmygroupNode(msg, groupId);
        } else if (anyRef instanceof GameMsg.test) {
            int type = ((GameMsg.test) anyRef).retype();
            if (type == 1) {
                GameMsg.createArmyArmyGroup msg = new GameMsg.createArmyArmyGroup("测试", 1, 1);
                sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
            }
            if (type == 2) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                GameMsg.test msg = new GameMsg.test(1);
                long groupId = playerProxy.getArmGrouId();
                tellMsgToArmygroupNode(msg, groupId);
            }
        } else if (anyRef instanceof GameMsg.StMTechUpInfo) {//科技大厅升级请求
            int armyLv = ((GameMsg.StMTechUpInfo) anyRef).armyLv();
            int techLv = ((GameMsg.StMTechUpInfo) anyRef).techLv();
            int buildNum = ((GameMsg.StMTechUpInfo) anyRef).buildNum();
            M22.TechInfo techInfo = ((GameMsg.StMTechUpInfo) anyRef).techInfo();
            int opt = ((GameMsg.StMTechUpInfo) anyRef).opt();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.resContributeNum();
            if (opt == 0) {//0返回信息
                M22.M220010.S2C.Builder builder = M22.M220010.S2C.newBuilder();
                builder.setRs(0);
                builder.setTechInfo(techInfo);
                builder.addAllResInfo(resInfo);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220010, builder.build());
                sendPushNetMsgToClient();
            } else {//1请求升级
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                int rs = armyGroupProxy.legionTechUp(armyLv, techLv, buildNum);
                if (rs == 0) {
                    GameMsg.MtSTechUpReq msg = new GameMsg.MtSTechUpReq(playerProxy.getPlayerId(), 2);
                    sendToArmyGroupNode(msg);
                }
                M22.M220010.S2C.Builder builder = M22.M220010.S2C.newBuilder();
                builder.setRs(rs);
                builder.addAllResInfo(resInfo);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220010, builder.build());
                sendPushNetMsgToClient();
            }

        } else if (anyRef instanceof GameMsg.StMTechUpInfoSucc) {//科技大厅升级成功
            M22.M220010.S2C.Builder builder = M22.M220010.S2C.newBuilder();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.resContributeNum();
            M22.TechInfo techInfo = ((GameMsg.StMTechUpInfoSucc) anyRef).techInfo();
            builder.setRs(0);
            builder.setTechInfo(techInfo);
            builder.addAllResInfo(resInfo);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220010, builder.build());
            sendFuntctionLog(FunctionIdDefine.LEGION_SCIENCE_HALL_UPGRADE_FUNCTION_ID,techInfo.getTechLv(),0,playerProxy.getArmGrouId());
            sendPushNetMsgToClient();

        } else if (anyRef instanceof GameMsg.StMTechContributeInfo) {//科技捐献
            int techExp = ((GameMsg.StMTechContributeInfo) anyRef).techExp();
            int techLv = ((GameMsg.StMTechContributeInfo) anyRef).techLv();
            int techId = ((GameMsg.StMTechContributeInfo) anyRef).techId();
            ArmygroupMenber armygroupMenber = ((GameMsg.StMTechContributeInfo) anyRef).armymen();
            Armygroup armygroup = ((GameMsg.StMTechContributeInfo) anyRef).armygroup();
            int power = ((GameMsg.StMTechContributeInfo) anyRef).power();
            int armyTechLv = ((GameMsg.StMTechContributeInfo) anyRef).armyTechLv();
            M22.TechInfo techInfo = ((GameMsg.StMTechContributeInfo) anyRef).techInfo();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int timerNum = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH);
            int alltime=timerdbProxy.getTimerNum(TimerDefine.LEGION_ALLTIME_DONATE, 0, 0);
            int rs = armyGroupProxy.legionTechGoldDonate(power, techId, techLv, techExp, armyTechLv, armygroupMenber, armygroup);
            List<M22.ResInfo> resInfo = armyGroupProxy.resContributeNum();
            if (rs == 0) {
                List<Integer> powerlist=new ArrayList<Integer>();
                powerlist.add(PlayerPowerDefine.POWER_tael);
                powerlist.add(PlayerPowerDefine.POWER_iron);
                powerlist.add(PlayerPowerDefine.POWER_wood);
                powerlist.add(PlayerPowerDefine.POWER_stones);
                powerlist.add(PlayerPowerDefine.POWER_food);
                powerlist.add(PlayerPowerDefine.POWER_gold);
                sendPowerDiff(powerlist);
                if (timerNum == 0) {
                    timerNum = 1;
                }
                GameMsg.MtSTechContributeReq msg = new GameMsg.MtSTechContributeReq(techId, power, playerProxy.getPlayerId(), ArmyGroupDefine.UP_OPT,timerNum ,alltime);
                sendToArmyGroupNode(msg);
                M2.M20002.S2C.Builder dif = M2.M20002.S2C.newBuilder();
                Common.AttrDifInfo.Builder diff = Common.AttrDifInfo.newBuilder();
                if (power == 200) {
                    power = 206;
                }
                diff.setTypeid(power);
                long value = playerProxy.getPowerValue(power);
                diff.setValue(value);
                dif.addDiffs(diff.build());
                pushNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, dif.build());
                sendPushNetMsgToClient();
            } else {
                M22.M220009.S2C.Builder builder = M22.M220009.S2C.newBuilder();
                builder.setRs(rs);
                builder.setPower(power);
                builder.setTechId(techId);
                builder.setTechInfo(techInfo);
                builder.addAllResInfo(resInfo);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220009, builder.build());
                sendPushNetMsgToClient();
            }
        } else if (anyRef instanceof GameMsg.StMContributeSucc) {//科技捐献成功
            M22.M220009.S2C.Builder builder = M22.M220009.S2C.newBuilder();
            int techId = ((GameMsg.StMContributeSucc) anyRef).techId();
            int power = ((GameMsg.StMContributeSucc) anyRef).power();
            M22.TechInfo techInfo = ((GameMsg.StMContributeSucc) anyRef).techInfo();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.resContributeNum();
            builder.setRs(0);
            builder.setPower(power);
            builder.setTechId(techId);
            builder.setTechInfo(techInfo);
            builder.addAllResInfo(resInfo);
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (power == 200) {
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_SCIENCE_COIN, 1, playerProxy, 0);
            } else {
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_SCIENCE_RESOURCE, 1, playerProxy, 0);
            }
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_SCIENCE, 1, playerProxy, 0);
            GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220009, builder.build());
            TaskProxy taskProxy=getProxy(ActorDefine.TASK_PROXY_NAME);
            PlayerReward reward=new PlayerReward();
            M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_UNIONCONTRIBUTE_TIMES, 1, reward);
            if(builder19!=null) {
                sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
            }
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220008, builder.build());
            sendPushNetMsgToClient();
            GameMsg.RefrshTip rmsg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, rmsg);
        } else if (anyRef instanceof GameMsg.StMHallUpInfo) {//军团大厅请求升级
            int armyLv = ((GameMsg.StMHallUpInfo) anyRef).armyLv();
            int buildNum = ((GameMsg.StMHallUpInfo) anyRef).buildNum();
            int opt = ((GameMsg.StMHallUpInfo) anyRef).opt();
            ArmygroupMenber armygroupMenber=((GameMsg.StMHallUpInfo) anyRef).menber();
            M22.ArmyInfo armyInfo = ((GameMsg.StMHallUpInfo) anyRef).hallInfo();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.hallContributeNum();
            if (opt == 0) {
                M22.M220007.S2C.Builder builder = M22.M220007.S2C.newBuilder();
                builder.setRs(0);
                builder.setArmyInfo(armyInfo);
                builder.addAllResInfo(resInfo);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220007, builder.build());
                sendPushNetMsgToClient();
            } else {
                int rs = armyGroupProxy.legionHallUp(armyLv, buildNum,armygroupMenber);
                if (rs == 0) {
                    GameMsg.StMHallUpSucesstonode msg = new GameMsg.StMHallUpSucesstonode();
                    sendToArmyGroupNode(msg);
                } else {
                    M22.M220007.S2C.Builder builder = M22.M220007.S2C.newBuilder();
                    builder.setRs(rs);
                    builder.setArmyInfo(armyInfo);
                    builder.addAllResInfo(resInfo);
                    pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220007, builder.build());
                    sendPushNetMsgToClient();
                }
            }
        } else if (anyRef instanceof GameMsg.StMHallUpInfoFaild) {//军团大厅升级失败
            M22.M220007.S2C.Builder builder = M22.M220007.S2C.newBuilder();
            M22.ArmyInfo armyInfo = ((GameMsg.StMHallUpInfoFaild) anyRef).hallInfo();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.hallContributeNum();
            builder.setRs(ErrorCodeDefine.M220007_2);
            builder.setArmyInfo(armyInfo);
            builder.addAllResInfo(resInfo);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220007, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.StMHallUpInfoSucc) {//军团大厅升级成功
            M22.M220007.S2C.Builder builder = M22.M220007.S2C.newBuilder();
            M22.ArmyInfo armyInfo = ((GameMsg.StMHallUpInfoSucc) anyRef).hallInfo();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.hallContributeNum();
            builder.setRs(0);
            builder.setArmyInfo(armyInfo);
            builder.addAllResInfo(resInfo);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220007, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.LEGION_HALL_UPGRADE_FUNCTION_ID, armyInfo.getArmyLv(), 0, 0,armyInfo.getArmyName());
            setLegionLevelDiff(armyInfo.getArmyLv());
        } else if (anyRef instanceof GameMsg.StMHallContributeSucc) {//军团大厅捐献成功
            M22.M220008.S2C.Builder builder = M22.M220008.S2C.newBuilder();
            M22.ArmyInfo armyInfo = ((GameMsg.StMHallContributeSucc) anyRef).hallInfo();
            int power = ((GameMsg.StMHallContributeSucc) anyRef).power();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<M22.ResInfo> resInfo = armyGroupProxy.hallContributeNum();
            builder.setRs(0);
            builder.setArmyInfo(armyInfo);
            builder.addAllResInfo(resInfo);
            ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (power == 200) {
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_HALL_COIN, 1, playerProxy, 0);
            } else {
                activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_HALL_RESOURCE, 1, playerProxy, 0);
            }
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            M19.M190000.S2C.Builder builder19 = taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_UNIONCONTRIBUTE_TIMES, 1, reward);
            if(builder19!=null) {
                sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(builder19, reward));
            }
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220008, builder.build());
            sendPushNetMsgToClient();
            sendActivitDiff();
            GameMsg.RefrshTip msg = new GameMsg.RefrshTip();
            sendModuleMsg(ActorDefine.ROLE_MODULE_NAME, msg);
        } else if (anyRef instanceof GameMsg.StMwelfarReqInfo) {//请求面板
            M22.M220013.S2C.Builder s2c = M22.M220013.S2C.newBuilder();
            M22.PanelInfo info = ((GameMsg.StMwelfarReqInfo) anyRef).welfareInfo();
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
            int type = ((GameMsg.StMwelfarReqInfo) anyRef).typeId();
            s2c.setRs(0);
            s2c.setPanelInfo(info);
            s2c.setType(type);
            s2c.setIscangetWelf(num);
            System.err.println("我的贡献值：" + info.getMyContribute());
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220013, s2c.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.StMwelfareUpInfo) {//福利院请求升级
            int wefareLv = ((GameMsg.StMwelfareUpInfo) anyRef).wefareLv();
            int armyLv = ((GameMsg.StMwelfareUpInfo) anyRef).armyLv();
            int buildNum = ((GameMsg.StMwelfareUpInfo) anyRef).buildNum();
            int type = ((GameMsg.StMwelfareUpInfo) anyRef).typeId();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
            int rs = armyGroupProxy.welfareUp(wefareLv, armyLv, buildNum);
            if (rs == 0) {
                GameMsg.MtSwelfareUpReq msg = new GameMsg.MtSwelfareUpReq(playerProxy.getPlayerId(), ArmyGroupDefine.UP_OPT, type);
                sendToArmyGroupNode(msg);
            } else {
                M22.M220013.S2C.Builder builder = M22.M220013.S2C.newBuilder();
                builder.setRs(rs);
                builder.setType(type);
                builder.setIscangetWelf(num);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220013, builder.build());
                sendPushNetMsgToClient();
            }
        } else if (anyRef instanceof GameMsg.MtSHallContributeReqSucess) {
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int power = ((GameMsg.MtSHallContributeReqSucess) anyRef).power();
            Armygroup armygroup = ((GameMsg.MtSHallContributeReqSucess) anyRef).armygroup();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL);
            int rs = armyGroupProxy.legionHallGoldDonate(power, armygroup);
            if (rs == 0) {
                if (num == 0) {
                    num = 1;
                }
                List<Integer> powerlist=new ArrayList<Integer>();
                powerlist.add(PlayerPowerDefine.POWER_tael);
                powerlist.add(PlayerPowerDefine.POWER_iron);
                powerlist.add(PlayerPowerDefine.POWER_wood);
                powerlist.add(PlayerPowerDefine.POWER_stones);
                powerlist.add(PlayerPowerDefine.POWER_food);
                powerlist.add(PlayerPowerDefine.POWER_gold);
                sendPowerDiff(powerlist);
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                GameMsg.MtSHallContributeReq msg = new GameMsg.MtSHallContributeReq(playerProxy.getPlayerId(), power, num);
                sendToArmyGroupNode(msg);
            } else {
                M22.M220008.S2C.Builder s2c = M22.M220008.S2C.newBuilder();
                s2c.setRs(rs);
                pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220008, s2c.build());
                sendPushNetMsgToClient();
            }
        } else if (anyRef instanceof GameMsg.StMwelfareUpInfoSucc) {//福利院升级成功
            M22.M220013.S2C.Builder builder = M22.M220013.S2C.newBuilder();
            M22.PanelInfo info = ((GameMsg.StMwelfareUpInfoSucc) anyRef).welfareInfo();
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
            int type = ((GameMsg.StMwelfareUpInfoSucc) anyRef).typeId();
            builder.setRs(0);
            builder.setPanelInfo(info);
            builder.setType(type);
            builder.setIscangetWelf(num);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220013, builder.build());
            sendFuntctionLog(FunctionIdDefine.LEGION_WELFAREHOUSE_WELFARE_UPGRADE_GET_FUNCTION_ID,2,0,0);
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.StMGetwelfare) {//福利院领取
            M22.PanelInfo info = ((GameMsg.StMGetwelfare) anyRef).welfareInfo();
            int canGetId = ((GameMsg.StMGetwelfare) anyRef).canGetid();
            int typeId = ((GameMsg.StMGetwelfare) anyRef).typeId();
            long myContribute = info.getMyContribute();
            ArmygroupMenber armygroupMenber = ((GameMsg.StMGetwelfare) anyRef).menber();
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            PlayerProxy playerProxy=getProxy(ActorDefine.PLAYER_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            M22.M220013.S2C.Builder s2c = M22.M220013.S2C.newBuilder();
            long prelevel=playerProxy.getPowerValue(PlayerPowerDefine.POWER_prestigeLevel);
            int rs = armyGroupProxy.getWelfareReward(canGetId, reward, armygroupMenber);
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
            s2c.setRs(rs);
            s2c.setType(typeId);
            s2c.setPanelInfo(info);
            s2c.setIscangetWelf(num);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220013, s2c.build());
            sendFuntctionLog(FunctionIdDefine.LEGION_WELFAREHOUSE_WELFARE_UPGRADE_GET_FUNCTION_ID,3,0,0);
            if (rs == 0) {
                GameMsg.requestSlMwelfareInfo msg = new GameMsg.requestSlMwelfareInfo(playerProxy.getPlayerId());
                tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
                RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                M2.M20007.S2C build2007 = rewardProxy.getRewardClientInfo(reward);
                pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, build2007);
                List<Integer> powerlist=new ArrayList<Integer>();
                powerlist.add(PlayerPowerDefine.POWER_prestige);
                if(playerProxy.getPowerValue(PlayerPowerDefine.POWER_prestigeLevel)!=prelevel) {
                    powerlist.add(PlayerPowerDefine.POWER_prestigeLevel);
                }
                sendPowerDiff(powerlist);
            }
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.MtSwelfareGetSucc) {//资源列表
            Map<Integer, Integer> resMap = ((GameMsg.MtSwelfareGetSucc) anyRef).canGetfiveResMap();
            ArmygroupMenber menber = ((GameMsg.MtSwelfareGetSucc) anyRef).menber();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            int rs = armyGroupProxy.getWelfareRes(resMap, menber);
            M22.M220015.S2C.Builder builder = M22.M220015.S2C.newBuilder();
            builder.setRs(rs);
            List<Integer> powerlist=new ArrayList<Integer>();
            powerlist.add(PlayerPowerDefine.POWER_tael);
            powerlist.add(PlayerPowerDefine.POWER_iron);
            powerlist.add(PlayerPowerDefine.POWER_wood);
            powerlist.add(PlayerPowerDefine.POWER_stones);
            powerlist.add(PlayerPowerDefine.POWER_food);
            sendPowerDiff(powerlist);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220015, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.requestSlMwelfareInfoBack) {//请求福利院信息
            M22.PanelInfo info = ((GameMsg.requestSlMwelfareInfoBack) anyRef).welfareInfo();
            M22.M220012.S2C.Builder builder = M22.M220012.S2C.newBuilder();
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            List<ArmygroupMenber> menbers = ((GameMsg.requestSlMwelfareInfoBack) anyRef).menbers();
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
            builder.addAllMenberInfo(armyGroupProxy.getLegionMenbers(menbers));
            builder.setRs(0);
            builder.setPanelInfo(info);
            builder.setIscangetWelf(num);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220012, builder.build());
            sendPushNetMsgToClient();
        } else if (anyRef instanceof GameMsg.editAffecheSucess) {//请求军团公告
            int rs = ((GameMsg.editAffecheSucess) anyRef).rs();
            M22.M220211.S2C.Builder builder = M22.M220211.S2C.newBuilder();
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220211, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.LEGION_NOTICE_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.getLegionLevelInfoback) {//请求建筑信息
            Armygroup armygroup = ((GameMsg.getLegionLevelInfoback) anyRef).army();
            M22.M220000.S2C.Builder builder = M22.M220000.S2C.newBuilder();
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            builder.setRs(0);
            builder.addAllInfo(armyGroupProxy.getLeginBuildInfo(armygroup));
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220000, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.GET_LEGION_LEVEL_INFO_FUNCTION_ID);
        } else if (anyRef instanceof GameMsg.notityLegionLevel) {
            int level = ((GameMsg.notityLegionLevel) anyRef).level();
            setLegionLevelDiff(level);
        } else if (anyRef instanceof GameMsg.checkeNoneId) {
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.setArmgroupId(0l);
            setLegionLevelDiff(1);
            updateMySimplePlayerData();
        } else if (anyRef instanceof GameMsg.getSituationInfoback) {
            M22.M220300.S2C.Builder  build= ((GameMsg.getSituationInfoback) anyRef ).build();
            build.setRs(0);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220300, build.build());
            sendPushNetMsgToClient();
        }else if (anyRef instanceof GameMsg.applistNumback) {
             int num  = ((GameMsg.applistNumback) anyRef ).num();
            M22.M220205.S2C.Builder builder= M22.M220205.S2C.newBuilder();
            builder.setRs(0);
            builder.setNum(num);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220205, builder.build());
            sendPushNetMsgToClient();
        }
    }

    public void setLegionLevelDiff(int level) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.setPowerValue(PlayerPowerDefine.POWER_legionLevel, (long) level);
        List<Integer> list = new ArrayList<Integer>();
        list.add(PlayerPowerDefine.POWER_legionLevel);
        M2.M20002.S2C different = sendDifferent(list);
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20002, different);
        sendPushNetMsgToClient();
    }

    public void sendActivitDiff() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(PlayerPowerDefine.POWER_active);
        M2.M20002.S2C different = sendDifferent(list);
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20002, different);
        sendPushNetMsgToClient();
    }

    /*****
     * 请求公会科技增益，进入时调用
     ******/
    private void getLegionTechnologyPowerMapFromService() {
        //军团科技属性加成
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long groupId = playerProxy.getArmGrouId();
        GameMsg.TechExpandPowerMap msg = new GameMsg.TechExpandPowerMap();
        tellMsgToArmygroupNode(msg, groupId);
    }

    /*****
     * 清除公会科技增益，离开时调用
     ******/
    private void clearTechPlayerPower() {
        Map<Integer, Long> powerMap = getPlayerPowerValues();
        ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
        armyGroupProxy.clearTechPlayerPower();
        checkPlayerPowerValues(powerMap);
        multicastNetToClient();
    }

    /*****
     * 刷新公会的增益
     *****/
    private void refLegionTechnologyPowerMap(Map<Integer, Long> techExpandPower) {
        Map<Integer, Long> powerMap = getPlayerPowerValues();
        ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
        armyGroupProxy.addTechPlayerPower(techExpandPower);
        checkPlayerPowerValues(powerMap);
        multicastNetToClient();
    }

    /**
     * 发送到军团节点Node
     **/
    private void sendToArmyGroupNode(Object msg) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long groupId = playerProxy.getArmGrouId();
        tellMsgToArmygroupNode(msg, groupId);
    }

    public void dogetArmyGroupByidSucess(Armygroup army, int cmd,int iconid,int penid) {
        if (cmd == ArmyGroupDefine.OPERATE_GETGROUP_SEND220101) {
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            M22.M220101.S2C.Builder builder = M22.M220101.S2C.newBuilder();
            int rs = armyGroupProxy.getArmyGroupDetailInfo(army, builder,iconid,penid);
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220101, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.GET_LEGION_DETAIL_INFO_FUNCTION_ID);
        }
    }

    public void dogetAllArmyGroupSucess(Map<Long, Armygroup> map, int cmd, Object object) {
        if (cmd == ArmyGroupDefine.OPERATE_GETALLGROUP_SEND220100) {
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            M22.M220100.S2C.Builder builder = M22.M220100.S2C.newBuilder();
            int rs = armyGroupProxy.getArmygroupInfos(map, builder);
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220100, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.LEGION_LISTS_FUNCTION_ID);
        } else if (cmd == ArmyGroupDefine.OPERATE_GETALLGROUP_SEND220104) {
            ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
            M22.M220104.S2C.Builder builder = M22.M220104.S2C.newBuilder();
            String name = (String) object;
            int rs = armyGroupProxy.getsearchInfos(map, builder, name);
            builder.setRs(rs);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220104, builder.build());
            sendPushNetMsgToClient();
            sendFuntctionLog(FunctionIdDefine.LEGION_SEARCH_FUNCTION_ID,0,0,0,name);
        }
    }


    /***
     * 军团创建
     ***/
    private void OnTriggerNet220103Event(Request request) {
        M22.M220103.C2S c2s = request.getValue();
        int way = c2s.getWay();
        int jointype = c2s.getJoinway();
        String name = c2s.getName();
        GameMsg.createArmyArmyGroup msg = new GameMsg.createArmyArmyGroup(name, jointype, way);
        sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
    }

    /**
     * 军团商店
     *
     * @param request
     */
    private void OnTriggerNet220002Event(Request request) {
        M22.M220002.C2S c2s = request.getValue();
        int id = c2s.getId();
        int opt = c2s.getOpt();
        int type = c2s.getType();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.ArmyGroupShop msg = new GameMsg.ArmyGroupShop(playerProxy.getPlayerId(), id, opt, type);
        long groupId = playerProxy.getArmGrouId();
        tellMsgToArmygroupNode(msg, groupId);

    }


    /******
     * 申请取消申请
     *******/
    private void OnTriggerNet220102Event(Request request) {
        M22.M220102.C2S c2s = request.getValue();
        int type = c2s.getType();
        long id = c2s.getId();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        if (playerProxy.getArmGrouId() > 0) {
            M22.M220102.S2C.Builder builder = M22.M220102.S2C.newBuilder();
            builder.setRs(ErrorCodeDefine.M220108_8);
            builder.setId(id);
            builder.setType(type);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220102, builder.build());
        } else {
            GameMsg.applyArmyJoin msg = new GameMsg.applyArmyJoin(playerProxy.getPlayerApplylist(), type, playerProxy.getLevel(), soldierProxy.getHighestCapacity(), playerProxy.getPlayerId());
            sendFuntctionLog(FunctionIdDefine.LEGION_ASK_FUNCTION_ID,id,playerProxy.getPlayerId(),0);
            tellMsgToArmygroupNode(msg, id);
        }
    }


    /******
     * 1 踢出军团 2转让团长 3退出军团
     *******/
    private void OnTriggerNet220201Event(Request request) {
        M22.M220201.C2S c2s = request.getValue();
        long playerId = c2s.getId();
        int type = c2s.getType();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.opeRateArmy msg = new GameMsg.opeRateArmy(playerId, playerProxy.getPlayerId(), type);
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /***
     * 自己军团信息
     ***/
    private void OnTriggerNet220200Event(Request request) {
        GameMsg.getMyGroupInfos msg = new GameMsg.getMyGroupInfos();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getArmGrouId() > 0) {
            tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
        } else {
            M22.M220200.S2C.Builder builder = M22.M220200.S2C.newBuilder();
            builder.setRs(ErrorCodeDefine.M220200_1);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220200, builder.build());
            sendFuntctionLog(FunctionIdDefine.GET_LEGION_INFO_FUNCTION_ID);
        }
    }


    /****
     * 编辑职位名
     ****/
    private void OnTriggerNet220220Event(Request request) {
        M22.M220220.C2S c2s = request.getValue();
        List<M22.LegionCustomJobShortInfo> list = c2s.getInfosList();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.editJobName msg = new GameMsg.editJobName(playerProxy.getPlayerId(), list);
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /****
     * 编辑公告
     ****/
    private void OnTriggerNet220211Event(Request request) {
        M22.M220211.C2S c2s = request.getValue();
        String cont = c2s.getAffiche();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.editAffeche msg = new GameMsg.editAffeche(cont, playerProxy.getPlayerId());
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /**
     * 查看申请列表
     ***/
    private void OnTriggerNet220202Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.lookAppList msg = new GameMsg.lookAppList();
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }


    /***
     * 清空列表
     ***/
    private void OnTriggerNet220204Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.clearApplylist msg = new GameMsg.clearApplylist(playerProxy.getPlayerId());
        sendFuntctionLog(FunctionIdDefine.EMPTY_APPLY_LISTS_FUNCTION_ID,playerProxy.getArmGrouId(),0,0);
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /***
     * 同意申请
     ***/
    private void OnTriggerNet220203Event(Request request) {
        M22.M220203.C2S c2s = request.getValue();
        long otherid = c2s.getId();
        int type = c2s.getType();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.agreeApply msg = new GameMsg.agreeApply(playerProxy.getPlayerId(), otherid, type);
        if(type == 1) {
            sendFuntctionLog(FunctionIdDefine.AGREE_APPLY_FUNCTION_ID,otherid,0,0);
        }
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /***
     * 申请列表
     ***/
    private void OnTriggerNet220205Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.applistNum msg = new GameMsg.applistNum();
        if(playerProxy.getArmGrouId()>0) {
            tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
        }else {
            M22.M220205.S2C.Builder builder=M22.M220205.S2C.newBuilder();
            builder.setRs(0);
            builder.setNum(0);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220205, builder.build());
        }
    }
    /***
     * 升职任职
     ***/
    private void OnTriggerNet220221Event(Request request) {
        M22.M220221.C2S c2s = request.getValue();
        int type = c2s.getType();
        long otherid = c2s.getId();
        int job = c2s.getJob();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.setorUpJob msg = new GameMsg.setorUpJob(playerProxy.getPlayerId(), type, otherid, job);
        sendFuntctionLog(FunctionIdDefine.LEGION_PROMOTION_FUNCTION_ID,job,otherid,0);
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /**
     * 编辑军团信息
     ***/
    private void OnTriggerNet220210Event(Request request) {
        M22.M220210.C2S c2s = request.getValue();
        List<Integer> list = c2s.getUpdateListList();
        int joinType = c2s.getJoinType();
        int level = c2s.getJoinCond1();
        long capity = c2s.getJoinCond2();
        String str = c2s.getNotice();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.editArmyGroup msg = new GameMsg.editArmyGroup(playerProxy.getPlayerId(), joinType, list, level, capity, str);
        tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
    }

    /**
     * 军团列表
     ***/
    private void OnTriggerNet220100Event(Request request) {
        GameMsg.getAllArmyGroup msg = new GameMsg.getAllArmyGroup(1, ArmyGroupDefine.OPERATE_GETALLGROUP_SEND220100);
        sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
    }

    /**
     * 获得某个军团的详细信息
     ***/
    private void OnTriggerNet220101Event(Request request) {
        M22.M220101.C2S c2s = request.getValue();
        long armId = c2s.getId();
        GameMsg.getArmyGroupByid msg = new GameMsg.getArmyGroupByid(armId, ArmyGroupDefine.OPERATE_GETGROUP_SEND220101);
        sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);
    }

    /**
     * 军团搜索
     ***/
    private void OnTriggerNet220104Event(Request request) {
        M22.M220104.C2S c2s = request.getValue();
        String name = c2s.getName();
        GameMsg.getAllArmyGroup msg = new GameMsg.getAllArmyGroup(name, ArmyGroupDefine.OPERATE_GETALLGROUP_SEND220104);
        sendServiceMsg(ActorDefine.ARMYGROUP_SERVICE_NAME, msg);

    }

    /**
     * 军团大厅升级
     ***/
    private void OnTriggerNet220007Event(Request request) {
        M22.M220007.C2S c2s = request.getValue();
        int opt = c2s.getOpt();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long groupId = playerProxy.getArmGrouId();
        if (groupId <= 0) {
            M22.M220010.S2C.Builder builder = M22.M220010.S2C.newBuilder();
            builder.setRs(ErrorCodeDefine.M220007_4);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220007, builder.build());
        } else {
            GameMsg.MtSHallUpReq msg = new GameMsg.MtSHallUpReq(playerProxy.getPlayerId(), opt);
            sendToArmyGroupNode(msg);
        }
    }

    /**
     * 军团科技大厅升级
     ***/
    private void OnTriggerNet220010Event(Request request) {
        M22.M220010.C2S c2s = request.getValue();
        int opt = c2s.getOpt();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long groupId = playerProxy.getArmGrouId();
        if (groupId <= 0) {
            M22.M220010.S2C.Builder builder = M22.M220010.S2C.newBuilder();
            builder.setRs(0);
            pushNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220010, builder.build());
        } else {
            GameMsg.MtSTechUpReq msg = new GameMsg.MtSTechUpReq(playerProxy.getPlayerId(), opt);
            tellMsgToArmygroupNode(msg, groupId);
        }
    }

    /**
     * 军团大厅捐献
     ***/
    private void OnTriggerNet220008Event(Request request) {
        M22.M220008.C2S c2s = request.getValue();
        int power = c2s.getPower();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        M22.M220008.S2C.Builder s2c = M22.M220008.S2C.newBuilder();
       /* int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL);
        int rs = armyGroupProxy.legionHallGoldDonate(power);*/
        GameMsg.MtSHallContribute msg = new GameMsg.MtSHallContribute(power);
        sendToArmyGroupNode(msg);
     /*   if (rs == 0) {
            if (num == 0) {
                num = 1;
            }
            GameMsg.MtSHallContributeReq msg = new GameMsg.MtSHallContributeReq(playerProxy.getPlayerId(), power, num);
            sendToArmyGroupNode(msg);
        } else {
            s2c.setRs(rs);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220008, s2c.build());
            sendFuntctionLog(FunctionIdDefine.LEGION_HALL_GOLD_RESOURCE_DONATE_FUNCTION_ID);
        }*/
    }

    /**
     * 军团科技捐献
     ***/
    private void OnTriggerNet220009Event(Request request) {
        M22.M220009.C2S c2s = request.getValue();
        int techId = c2s.getTechId();
        int power = c2s.getPower();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.MtSTechContributeReq msg = new GameMsg.MtSTechContributeReq(techId, power, playerProxy.getPlayerId(), ArmyGroupDefine.UP_REQ, 0,0);
        sendToArmyGroupNode(msg);
    }

    /**
     * 军团福利院升级,
     ***/
    private void OnTriggerNet220013Event(Request request) {
        M22.M220013.C2S c2s = request.getValue();
        int type = c2s.getType();
        int canGetId = c2s.getCanGetId();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (type == 1) {
            GameMsg.MtSwelfareReq msg = new GameMsg.MtSwelfareReq(playerProxy.getPlayerId(), type);
            sendToArmyGroupNode(msg);
        } else if (type == 2) {
            GameMsg.MtSwelfareUpReq msg = new GameMsg.MtSwelfareUpReq(playerProxy.getPlayerId(), ArmyGroupDefine.UP_REQ, type);
            sendToArmyGroupNode(msg);
        } else if (type == 3) {
            GameMsg.MtSGetwelfareReq msg = new GameMsg.MtSGetwelfareReq(playerProxy.getPlayerId(), ArmyGroupDefine.UP_REQ, canGetId, type);
            sendToArmyGroupNode(msg);
        }
    }


    /**
     * 军团等级信息
     ***/
    private void OnTriggerNet220000Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M22.M220000.S2C.Builder builder = M22.M220000.S2C.newBuilder();
        if (playerProxy.getArmGrouId() <= 0) {
            builder.setRs(-1);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220000, builder.build());
            sendFuntctionLog(FunctionIdDefine.GET_LEGION_LEVEL_INFO_FUNCTION_ID);
        } else {
            GameMsg.getLegionLevelInfo msg = new GameMsg.getLegionLevelInfo();
            sendToArmyGroupNode(msg);
        }
    }

    /**
     * 情报站信息
     ***/
    private void OnTriggerNet220300Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M22.M220300.S2C.Builder builder = M22.M220300.S2C.newBuilder();
        if (playerProxy.getArmGrouId() <= 0) {
            builder.setRs(ErrorCodeDefine.M220300_1);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220300, builder.build());
        } else {
            tellMsgToArmygroupNode(new GameMsg.getSituationInfo(builder), playerProxy.getArmGrouId());
        }
    }

    /**
     * 情报站信息
     ***/
    private void OnTriggerNet220400Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M22.M220400.S2C.Builder builder = M22.M220400.S2C.newBuilder();
        if (playerProxy.getArmGrouId() <= 0) {
            builder.setRs(ErrorCodeDefine.M220400_3);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220400, builder.build());
        } else {
            tellMsgToArmygroupNode(new GameMsg.legionenlist(playerProxy.getPlayerId()), playerProxy.getArmGrouId());
        }
    }

    /**
     * 军团福利院领取福利
     ***/
    private void OnTriggerNet220014Event(Request request) {
      /*  M22.M220014.C2S c2s = request.getValue();
        int canGetId = c2s.getCanGetId();
        ArmyGroupProxy armyGroupProxy = getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
        PlayerReward reward = new PlayerReward();
        M22.M220014.S2C.Builder s2c = M22.M220014.S2C.newBuilder();
        int rs = armyGroupProxy.getWelfareReward(myContribute,canGetId, reward);
        s2c.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220014, s2c.build());*/
    }

    /**
     * 福利院信息
     */
    private void OnTriggerNet220012Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getArmGrouId() > 0) {
            GameMsg.requestSlMwelfareInfo msg = new GameMsg.requestSlMwelfareInfo(playerProxy.getPlayerId());
            tellMsgToArmygroupNode(msg, playerProxy.getArmGrouId());
        } else {
            M22.M220012.S2C.Builder builder = M22.M220012.S2C.newBuilder();
            TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int num = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
            builder.setRs(-1);
            builder.setIscangetWelf(num);
            sendNetMsg(ProtocolModuleDefine.NET_M22, ProtocolModuleDefine.NET_M22_C220012, builder.build());
        }
    }


    /**
     * 军团福利院活跃，资源福利领取
     */
    private void OnTriggerNet220015Event(Request request) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        GameMsg.MtSwelfareGetRes msg = new GameMsg.MtSwelfareGetRes(playerProxy.getPlayerId());
        sendToArmyGroupNode(msg);
    }


    private void tellMsgToArmygroupNode(Object mess, Long id) {
        context().actorSelection("../../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE + id).tell(mess, self());
    }

    private void sendarmoupIdIdffer() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(PlayerPowerDefine.POWER_armygroupId);
        M2.M20002.S2C different = sendDifferent(list);
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20002, different);
        sendPushNetMsgToClient();
    }

    private void sendLegionameDiffer() {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M2.M20201.S2C.Builder different = M2.M20201.S2C.newBuilder();
        different.setName(playerProxy.getPlayer().getLegionName());
        pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20201, different.build());
        sendPushNetMsgToClient();
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
