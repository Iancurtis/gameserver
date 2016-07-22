package com.znl.modules.equip;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicModule;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.admin.tbllog_equipment;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Equip;
import com.znl.proto.M13;
import com.znl.proto.M19;
import com.znl.proto.M2;
import com.znl.proto.M4;
import com.znl.proxy.*;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/6.
 */
public class EquipModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<EquipModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public EquipModule create() throws Exception {
                return new EquipModule(gameProxy);
            }
        });
    }

    public EquipModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M13);
    }

    @Override
    public void onReceiveOtherMsg(Object object) {

    }

   //TODO 该协议做迁移
    private void OnTriggerNet130000Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130000.S2C.Builder builder = M13.M130000.S2C.newBuilder();
        builder.addAllEquipinfos(equipProxy.getEquipInfos());
        builder.setRs(0);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130000, builder.build());

    }

    private void OnTriggerNet130001Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130001.C2S c2s = request.getValue();
        long id = c2s.getId();
        List<Long> useId = c2s.getUseidsList();
        M13.M130001.S2C.Builder builder = M13.M130001.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int preLv = equipProxy.getEquipLevel(id);
        int rs = equipProxy.equipLevelUp(id, useId, builder, baseLogs);
        int newLv = equipProxy.getEquipLevel(id);
        builder.setRs(rs);
        builder.setId(id);
        builder.addEquipinfos(equipProxy.getEquipInfo(id));
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130001, builder.build());
        if (rs == 0) {
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_EQUIPLVUP_TIMES, 1);
            sendLog(baseLogs);
            /**
             * tbllog_equipment日志
             */
            equipmentLog(id, preLv, newLv, useId.toString());
            /**
             * 加入排行榜
             */
            equipProxy.addToRankList(id);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            updateSimplePlayerData(playerProxy.getSimplePlayer());
            equipProxy.initPurpleEquipLeveNum();
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());

        }
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130002Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130002.C2S c2s = request.getValue();
        long id = c2s.getId();
        int position = c2s.getPosition();
        int type = c2s.getType();
        int funid = c2s.getUpproperty();
        M13.M130002.S2C.Builder builder = M13.M130002.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = equipProxy.equipOn(id, position, type, funid, builder, baseLogs);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130002, builder.build());
        if (rs == 0) {
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
            sendLog(baseLogs);

            /**
             * 加入排行榜
             */
            equipProxy.addToRankList(id);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            updateSimplePlayerData(playerProxy.getSimplePlayer());
            equipProxy.initPutOnEquipNumByQuilty(4);

        }
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet130003Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130003.C2S c2s = request.getValue();
        long id = c2s.getId();
        M13.M130003.S2C.Builder builder = M13.M130003.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = equipProxy.equipOff(id, baseLogs);
        builder.setRs(rs);
        builder.addEquipinfos(equipProxy.getEquipInfo(id));
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130003, builder.build());
        if (rs == 0) {
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            sendLog(baseLogs);
            equipProxy.initPutOnEquipNumByQuilty(4);
            /**
             * 加入排行榜
             */
            equipProxy.addToRankList(id);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            updateSimplePlayerData(playerProxy.getSimplePlayer());

        }
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130005Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130005.C2S c2s = request.getValue();
        List<Long> idlist = c2s.getIdList();
        M13.M130005.S2C.Builder builder = M13.M130005.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = equipProxy.equipSale(idlist, builder, baseLogs);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130005, builder.build());
        if (rs == 0) {
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            sendLog(baseLogs);
            equipProxy.initPurpleEquipLeveNum();

        }
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet130006Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130006.C2S c2s = request.getValue();
        int positione = c2s.getPosione();
        int positiontwo = c2s.getPositwo();
        M13.M130006.S2C.Builder builder = M13.M130006.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = equipProxy.changetEquipPosition(positione, positiontwo, builder, baseLogs);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130006, builder.build());
        if (rs == 0) {
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, new GameMsg.CountCapacity());
            sendLog(baseLogs);
            sendFuntctionLog(FunctionIdDefine.CHANGE_EQUIP_POSITION_FUNCTION_ID,positione,positiontwo,0);
        }
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet130007Event(Request request) {
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        M13.M130007.S2C.Builder builder = M13.M130007.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = equipProxy.addEuipBagSize(baseLogs);
        builder.setRs(rs);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        builder.setCount((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_equipsize));
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130007, builder.build());
        sendLog(baseLogs);
        sendFuntctionLog(FunctionIdDefine.EQUIP_BAGS_EXTEND_FUNCTION_ID);
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet130100Event(Request request) {
        OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        M13.M130100.S2C.Builder builder = M13.M130100.S2C.newBuilder();
        builder.addAllOdpInfos(ordnancePieceProxy.getOrdnancePieceInfos());
        builder.setRs(0);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130100, builder.build());
        sendFuntctionLog(FunctionIdDefine.GET_ORDNANCE_PIECE_INFOS_FUNCTION_ID);
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130101Event(Request request) {
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130101.S2C.Builder builder = M13.M130101.S2C.newBuilder();
        builder.addAllOdInfos(ordnanceProxy.getOrdnanceInfos());
        builder.setRs(0);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130101, builder.build());
        sendFuntctionLog(FunctionIdDefine.GET_ORDANCE_INFOS_FUNCTION_ID);
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130102Event(Request request) {
        M13.M130102.C2S c2s = request.getValue();
        int typeId = c2s.getTypeid();
        OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        M13.M130102.S2C.Builder builder = M13.M130102.S2C.newBuilder();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnancePieceProxy.piecetoOrndance(typeId, builder, baseLogs);
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130102, builder.build());
        sendLog(baseLogs);
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130103Event(Request request) {
        M13.M130103.C2S c2s = request.getValue();
        List<Integer> list = c2s.getTypeidList();
        int type = c2s.getType();
        OrdnancePieceProxy ordnancePieceProxy = getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        M13.M130103.S2C.Builder builder = M13.M130103.S2C.newBuilder();
        PlayerReward reward = new PlayerReward();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnancePieceProxy.dropOrndancePiece(list, reward, type, baseLogs);
        for (int typeId : list) {
            builder.addOdpInfos(ordnancePieceProxy.getOrdnancePieceInfo(typeId));
        }
        builder.setRs(rs);
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130103, builder.build());
        sendLog(baseLogs);
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        M2.M20007.S2C builder1 = rewardProxy.getRewardClientInfo(reward);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, builder1);
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet130104Event(Request request) {
        M13.M130104.C2S c2s = request.getValue();
        long id = c2s.getId();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130104.S2C.Builder builder = M13.M130104.S2C.newBuilder();
        List<M2.M20007.S2C.Builder> builder1 = new ArrayList<M2.M20007.S2C.Builder>();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnanceProxy.ordnanceOn(id, builder1, baseLogs, builder);
        builder.setRs(rs);
        builder.addOdInfos(ordnanceProxy.getOrdnanceInfo(id));
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130104, builder.build());
        sendM4000(builder1);
        sendLog(baseLogs);
        sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
        sendPushNetMsgToClient();
    }


    private void OnTriggerNet130105Event(Request request) {
        M13.M130105.C2S c2s = request.getValue();
        long id = c2s.getId();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130105.S2C.Builder builder = M13.M130105.S2C.newBuilder();
        List<M2.M20007.S2C.Builder> builder1 = new ArrayList<M2.M20007.S2C.Builder>();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnanceProxy.ordnanceOff(id, builder1, baseLogs);
        builder.setRs(rs);
        builder.addOdInfos(ordnanceProxy.getOrdnanceInfo(id));
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130105, builder.build());
        sendM4000(builder1);
        sendLog(baseLogs);
        sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130106Event(Request request) {
        M13.M130106.C2S c2s = request.getValue();
        List<Long> idlist = c2s.getIdList();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130106.S2C.Builder builder = M13.M130106.S2C.newBuilder();
        List<Integer> itemlist = new ArrayList<Integer>();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnanceProxy.dropOrdnanceOff(idlist, itemlist, baseLogs);
        builder.setRs(rs);
        for (long id : idlist) {
            builder.addOdInfos(ordnanceProxy.getOrdnanceInfo(id));
        }
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130106, builder.build());
        if (rs == 0) {
            sendM20007(itemlist);
            sendLog(baseLogs);
        }
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130107Event(Request request) {
        M13.M130107.C2S c2s = request.getValue();
        long id = c2s.getId();
        int num = c2s.getNum();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130107.S2C.Builder builder = M13.M130107.S2C.newBuilder();
        List<Integer> itemlist = new ArrayList<Integer>();
        List<M2.M20007.S2C.Builder> builder1 = new ArrayList<M2.M20007.S2C.Builder>();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnanceProxy.intensifyOrdnance(id, num, builder, itemlist, builder1, baseLogs);
        builder.setRs(rs);
        if (rs >= 0 || rs == -6) {
//            checkPlayerPowerValues(request.getPowerMap());
            sendM20007(itemlist);

        }
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130107, builder.build());
        if (rs == 0) {
            TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
            taskProxy.getTaskUpdate(TaskDefine.TASK_TYPE_ORNDANCESTENGTH_TIMES, 1);
            sendM4000(builder1);
            sendLog(baseLogs);
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
        }
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130108Event(Request request) {
        M13.M130108.C2S c2s = request.getValue();
        long id = c2s.getId();
        int type = c2s.getType();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130108.S2C.Builder builder = M13.M130108.S2C.newBuilder();
        List<Integer> itemlist = new ArrayList<Integer>();
        List<M2.M20007.S2C.Builder> builder1 = new ArrayList<M2.M20007.S2C.Builder>();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        int rs = ordnanceProxy.remouldOrndance(id, type, builder, itemlist, builder1, baseLogs);
        builder.setRs(rs);
        if (rs == 0) {
//            checkPlayerPowerValues(request.getPowerMap());
            sendM20007(itemlist);

        }
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130108, builder.build());
        if (rs == 0) {
            sendM4000(builder1);
            sendLog(baseLogs);
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
        }
        sendPushNetMsgToClient();
    }

    private void OnTriggerNet130109Event(Request request) {
        M13.M130109.C2S c2s = request.getValue();
        long id = c2s.getId();
        OrdnanceProxy ordnanceProxy = getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        M13.M130109.S2C.Builder builder = M13.M130109.S2C.newBuilder();
        List<M2.M20007.S2C.Builder> builder1 = new ArrayList<M2.M20007.S2C.Builder>();
        List<BaseLog> baseLogs = new ArrayList<BaseLog>();
        PlayerReward reward = new PlayerReward();
        int rs = ordnanceProxy.ordnanceAdvance(id, builder, reward, builder1, baseLogs);
        builder.addOdInfos(ordnanceProxy.getOrdnanceInfo(id));
        builder.setRs(rs);
        if (rs == 0) {
//            checkPlayerPowerValues(request.getPowerMap());
            sendM20007(reward);

        }
        sendNetMsg(ProtocolModuleDefine.NET_M13, ProtocolModuleDefine.NET_M13_C130109, builder.build());
        if (rs == 0) {
            sendM4000(builder1);
            sendLog(baseLogs);
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());
        }
        sendPushNetMsgToClient();
    }

    private void sendM4000(List<M2.M20007.S2C.Builder> builder1) {
        for (M2.M20007.S2C.Builder builder : builder1) {
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, builder.build());
        }
    }

    private void sendM20007(List<Integer> itemlist) {
        if (itemlist.size() > 0) {
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            M2.M20007.S2C msg = rewardProxy.getItemListClientInfo(itemlist);
            sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, msg);
        }
    }

    private void sendM20007(PlayerReward reward) {
        RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
        M2.M20007.S2C msg = rewardProxy.getRewardClientInfo(reward);
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, msg);
    }


    public void sendLog(List<BaseLog> baseLogs) {
        for (BaseLog baseLog : baseLogs) {
            sendLog(baseLog);
        }
    }

    /**
     * tbllog_equipment日志
     */
    public void equipmentLog(long eqId, int preLv, int newLv, String material) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        EquipProxy equipProxy = getProxy(ActorDefine.EQUIP_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        int equipId = equipProxy.getEquipTypeId(eqId);
        int quality = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.EQUIP_POWER, "ID", equipId).getInt("quality");
        tbllog_equipment equipmentlog = new tbllog_equipment();
        equipmentlog.setPlatform(cache.getPlat_name());
        equipmentlog.setRole_id(player.getPlayerId());
        equipmentlog.setAccount_name(player.getAccountName());
        equipmentlog.setDim_level(player.getLevel());
        equipmentlog.setItem_id(equipId);
        equipmentlog.setItem_property(quality);
        equipmentlog.setValue_before(preLv);
        equipmentlog.setValue_after(newLv);
        equipmentlog.setChange_type(1);
        equipmentlog.setMaterial(material);
        equipmentlog.setHappend_time(GameUtils.getServerTime());
        sendLog(equipmentlog);
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }

}
