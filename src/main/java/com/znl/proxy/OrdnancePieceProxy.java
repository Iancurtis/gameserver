package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.log.OrdnanceLog;
import com.znl.log.PieceGet;
import com.znl.log.PieceLost;
import com.znl.log.admin.tbllog_items;
import com.znl.log.admin.tbllog_ordnancepice;
import com.znl.pojo.db.OrdnancePiece;
import com.znl.proto.Common;
import com.znl.proto.M13;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONObject;

import javax.accessibility.AccessibleAction;
import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class OrdnancePieceProxy extends BasicProxy {
    private Set<OrdnancePiece> odps = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (OrdnancePiece odp : odps) {
            odp.finalize();
        }
    }

    @Override
    protected void init() {

    }

    public OrdnancePieceProxy(Set<Long> odpIds,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : odpIds) {
            OrdnancePiece odp = BaseDbPojo.get(id, OrdnancePiece.class,areaKey);
            if (odp == null) {
                System.out.println("碎片出现空值了");
            } else {
                this.odps.add(odp);
            }
        }
    }


    public void saveOrdnancePieces() {
        List<OrdnancePiece> ordnancePieces = new ArrayList<OrdnancePiece>();
        synchronized (changeOrdnancePieces) {
            while (true) {
                OrdnancePiece odp = changeOrdnancePieces.poll();
                if (odp == null) {
                    break;
                }
                ordnancePieces.add(odp);
            }
        }
        for (OrdnancePiece odp : ordnancePieces) {
            odp.save();
        }

    }

    private LinkedList<OrdnancePiece> changeOrdnancePieces = new LinkedList<OrdnancePiece>();

    private void pushOrdnancePieceToChangeList(OrdnancePiece odp) {
        //插入更新队列
        synchronized (changeOrdnancePieces) {
            if (!changeOrdnancePieces.contains(odp)) {
                changeOrdnancePieces.offer(odp);
            }
        }
    }


    public boolean isHasTypeId(int typeId) {
        for (OrdnancePiece odp : odps) {
            if (odp.getTypeId() == typeId) {
                return true;
            }
        }
        return false;
    }


    public void addOrdnancePiece(int typeId, int num,int logtype) {
        if (isHasTypeId(typeId)) {
            addOrdnancePieceNum(typeId, num,logtype);
        } else {
            creatOrdnancePiece(typeId, num,logtype);
        }
        ordnancePiceLog(1,typeId,num,logtype);

    }


    private long creatOrdnancePiece(int typeId, int num,int logtype) {
        if(odps.size()>=EquipDefine.ORDANCE_MAX_SIZE){
            return 0;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE_PIECE, typeId);
        if (jsonObject == null) {
            return -1;
        }
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        OrdnancePiece odp = BaseDbPojo.create(OrdnancePiece.class,areaKey);
        odp.setNum(num);
        odp.setPlayerId(playerProxy.getPlayerId());
        odp.setTypeId(typeId);
        odps.add(odp);
        playerProxy.addOrdnancePieceToPlayer(odp.getId());
        odp.save();
        PieceGet pieceGet=new PieceGet(logtype,typeId,num);
        sendPorxyLog(pieceGet);
        return odp.getId();
    }

    public long getOrdnancePieceId(int OrdnancePieceId) {
        OrdnancePiece OrdnancePiece = getOrdnancePieceByOrdnancePieceId(OrdnancePieceId);
        if (OrdnancePiece == null) {
            return 0;
        }
        return OrdnancePiece.getId();
    }

    public int getOrdnancePieceNum(int typeId) {
        OrdnancePiece odp = getOrdnancePieceByOrdnancePieceId(typeId);
        if (odp == null) {
            return 0;
        }
        return odp.getNum();
    }


    private OrdnancePiece getOrdnancePieceByOrdnancePieceId(int typeId) {
        for (OrdnancePiece odp : odps) {
            if (odp.getTypeId() == typeId) {
                return odp;
            }
        }
        return null;
    }

    private void addOrdnancePieceNum(int typeId, int add,int logtype) {
        OrdnancePiece odp = getOrdnancePieceByOrdnancePieceId(typeId);
        if (add < 0) {
            System.out.println("增加道具数量的时候出现负数了！！！");
            add = 0;
        }
        int value = getOrdnancePieceNum(typeId);
        System.out.print("OrdnancePieceId=" + typeId);
        odp.setNum(value + add);
        pushOrdnancePieceToChangeList(odp);
        PieceGet pieceGet=new PieceGet(logtype,typeId,add);
        sendPorxyLog(pieceGet);
    }

    public void reduceOrdnancePieceNum(int typeId, int reduce,int logtype) {
        OrdnancePiece odp = getOrdnancePieceByOrdnancePieceId(typeId);
        if(odp==null){
            return;
        }
        if (reduce < 0) {
            System.out.println("减少道具数量的时候出现负数了！！！");
            reduce -= reduce;
        }
        int value = getOrdnancePieceNum(typeId);
        int result = value - reduce;
        odp.setNum(result);
        PieceLost pieceLost=new PieceLost(logtype,typeId,reduce);
        sendPorxyLog(pieceLost);
        if (result <= 0) {
            odps.remove(odp);
            odp.del();
            changeOrdnancePieces.remove(odp);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.reduceOrdnancePieceToPlayer(odp.getId());
        } else {
            pushOrdnancePieceToChangeList(odp);
        }
        ordnancePiceLog(0,typeId,reduce,logtype);
    }

    public List<Common.OrdnancePieceInfo> getOrdnancePieceInfos() {
        List<Common.OrdnancePieceInfo> list = new ArrayList<Common.OrdnancePieceInfo>();
        for (OrdnancePiece odp : odps) {
            Common.OrdnancePieceInfo.Builder builder = Common.OrdnancePieceInfo.newBuilder();
            builder.setTypeid(odp.getTypeId());
            builder.setNum(odp.getNum());
            list.add(builder.build());
        }
        return list;
    }

    public Common.OrdnancePieceInfo getOrdnancePieceInfo(int typeid) {
        OrdnancePiece odp = getOrdnancePieceByOrdnancePieceId(typeid);
        Common.OrdnancePieceInfo.Builder builder = Common.OrdnancePieceInfo.newBuilder();
        builder.setTypeid(typeid);
        if (odp == null) {
            builder.setNum(0);
        } else {
            builder.setNum(odp.getNum());
        }

        return builder.build();
    }

    /******
     * 军械碎片合成军械
     *******/
    public int piecetoOrndance(int typeid, M13.M130102.S2C.Builder builder, List<BaseLog> baseLogs) {
        OrdnancePiece odp = getOrdnancePieceByOrdnancePieceId(typeid);
        if (odp == null) {
            return ErrorCodeDefine.M130102_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE_PIECE, typeid);
        if (jsonObject == null) {
            return ErrorCodeDefine.M130102_2;
        }
        OrdnancePiece superodp = getOrdnancePieceByOrdnancePieceId(EquipDefine.SUPER_ORDNANCEPICE);
        int lesnum=0;
        int needNum = jsonObject.getInt("num");
        if (odp.getNum() < needNum) {
            lesnum=jsonObject.getInt("num")-odp.getNum();
            if(superodp==null||superodp.getNum()<lesnum||odp.getNum()==0){
                return ErrorCodeDefine.M130102_3;
            }
        }
        int ordanId = jsonObject.getInt("compound");
        if(ordanId==0){
            return ErrorCodeDefine.M130102_4;
        }
        OrdnanceProxy ordnanceProxy = getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        OrdnancePieceProxy ordnancePieceProxy=getGameProxy().getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        reduceOrdnancePieceNum(typeid, jsonObject.getInt("num")-lesnum,LogDefine.LOST_PIECE_TO_ORNDANCE);
        reduceOrdnancePieceNum(EquipDefine.SUPER_ORDNANCEPICE, lesnum,LogDefine.LOST_PIECE_TO_ORNDANCE);
        long id = ordnanceProxy.creatOrdnance(ordanId, 0, 0,LogDefine.GET_PICETOORNDANCE);
        builder.addOdInfos(ordnanceProxy.getOrdnanceInfo(id));
        builder.addOdpInfos(ordnancePieceProxy.getOrdnancePieceInfo(EquipDefine.SUPER_ORDNANCEPICE));
        builder.addOdpInfos(ordnancePieceProxy.getOrdnancePieceInfo(typeid));
        OrdnanceLog ordnanceLog=new OrdnanceLog(LogDefine.ORDNANCECOMPL);
        ordnanceLog.setPicenum(jsonObject.getInt("num"));
        ordnanceLog.setPiceTypeId(typeid);
        ordnanceLog.setOrdanceTypeId(ordanId);
        baseLogs.add(ordnanceLog);
        sendFunctionLog(FunctionIdDefine.PIECE_TO_ORDNANCE_FUNCTION_ID, ordanId,jsonObject.getInt("num"),typeid);
        return 0;
    }


    /***********
     * 军械碎片分解
     *******/
    public int dropOrndancePiece(List<Integer> list, PlayerReward reward, int type,List<BaseLog> baseLogs) {
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int typeId : list) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE_PIECE, typeId);
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            if (jsonObject == null) {
                return ErrorCodeDefine.M130103_1;
            }
            if (getOrdnancePieceNum(typeId) < 1) {
                return ErrorCodeDefine.M130103_2;
            }
            int num=1;
            if(type==2){
                num=getOrdnancePieceNum(typeId);
            }
            reduceOrdnancePieceNum(typeId, num,LogDefine.LOST_DROP_DROP);
            OrdnanceLog ordnanceLog=new OrdnanceLog(LogDefine.ORDNANCEPIECEDROP);
            ordnanceLog.setPicenum(num);
            ordnanceLog.setPiceTypeId(typeId);
            baseLogs.add(ordnanceLog);
            itemProxy.addItem(EquipDefine.DROP_ORDANC_RETURN_ITEM, jsonObject.getInt("remouldItemFour")*num,LogDefine.GET_DROP_ORNDANCEPIECE);
            rewardProxy.addItemToReward(reward, EquipDefine.DROP_ORDANC_RETURN_ITEM, jsonObject.getInt("remouldItemFour")*num);
            sendFunctionLog(FunctionIdDefine.DROP_ORDNANCE_PIECE_FUNCTION_ID,typeId,num,0);
        }
        return 0;
    }


    public List<Integer> delAllItem(){
        List<Integer> list=new ArrayList<Integer>();
        for(OrdnancePiece  item:odps){
            list.add(item.getTypeId());
        }
        for(int id:list){
            reduceOrdnancePieceNum(id,900000000,LogDefine.LOST_BUY_CHEAT);
        }

        return list;
    }


    /**
     * ordnancePiceLog:1增加，0使用
     */
    public void ordnancePiceLog(int opt, int itemId, int num,int logType) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_ordnancepice itemslog = new tbllog_ordnancepice();
        itemslog.setPlatform(cache.getPlat_name());
        itemslog.setRole_id(player.getPlayerId());
        itemslog.setAccount_name(player.getAccountName());
        itemslog.setDim_level(player.getLevel());
        itemslog.setOpt(opt);
        itemslog.setAction_id(logType);
        itemslog.setType_id(itemId);
        itemslog.setItem_number((long) num);
        itemslog.setMap_id(0);
        itemslog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(itemslog);
    }


}
