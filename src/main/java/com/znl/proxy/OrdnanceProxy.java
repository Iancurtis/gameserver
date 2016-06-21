package com.znl.proxy;

import com.google.gson.JsonObject;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.log.OrdnanceLog;
import com.znl.log.OrnanceGet;
import com.znl.log.OrndanceLost;
import com.znl.log.admin.tbllog_ordnance;
import com.znl.log.admin.tbllog_ordnancepice;
import com.znl.pojo.db.Equip;
import com.znl.pojo.db.Ordnance;
import com.znl.pojo.db.Ordnance;
import com.znl.pojo.db.OrdnancePiece;
import com.znl.proto.Common;
import com.znl.proto.M13;
import com.znl.proto.M4;
import com.znl.utils.GameUtils;
import com.znl.utils.RandomUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.filefilter.ConditionalFileFilter;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.tools.nsc.transform.patmat.Logic;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class OrdnanceProxy extends BasicProxy {
    private Set<Ordnance> ods = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (Ordnance odp : ods) {
            odp.finalize();
        }
    }

    @Override
    protected void init() {

    }

    public OrdnanceProxy(Set<Long> odIds,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : odIds) {
            Ordnance odp = BaseDbPojo.get(id, Ordnance.class,areaKey);
            if (odp != null) {
                this.ods.add(odp);
            }
        }
    }


    public void saveOrdnances() {
        List<Ordnance> Ordnances = new ArrayList<Ordnance>();
        synchronized (changeOrdnances) {
            while (true) {
                Ordnance odp = changeOrdnances.poll();
                if (odp == null) {
                    break;
                }
                Ordnances.add(odp);
            }
        }
        for (Ordnance odp : Ordnances) {
            odp.save();
        }

    }

    private LinkedList<Ordnance> changeOrdnances = new LinkedList<Ordnance>();

    private void pushOrdnanceToChangeList(Ordnance odp) {
        //插入更新队列
        synchronized (changeOrdnances) {
            if (!changeOrdnances.contains(odp)) {
                changeOrdnances.offer(odp);
            }
        }
    }


    private Ordnance getOrdnanceByid(long id) {
        for (Ordnance od : ods) {
            if (od.getId() == id) {
                return od;
            }
        }
        return null;
    }

    private List<Ordnance> getListBytype(int type) {
        List<Ordnance> list = new ArrayList<Ordnance>();
        for (Ordnance od : ods) {
            if (od.getType() == type) {
                list.add(od);
            }
        }
        return list;
    }

    public boolean isHasOrdnance(int type, int part) {
        for (Ordnance od : ods) {
            if (od.getType() == type && od.getPart() == part && od.getPosition() != 0) {
                return true;
            }
        }
        return false;
    }

    private Ordnance getOrdnance(int type, int part) {
        for (Ordnance od : ods) {
            if (od.getType() == type && od.getPart() == part && od.getPosition() != 0) {
                return od;
            }
        }
        return null;
    }


    private List<Ordnance> getListBytPosition(int postion) {
        List<Ordnance> list = new ArrayList<Ordnance>();
        for (Ordnance od : ods) {
            if (od.getPosition() == postion) {
                list.add(od);
            }
        }
        return list;
    }

    private List<Ordnance> getListBytpart(int part) {
        List<Ordnance> list = new ArrayList<Ordnance>();
        for (Ordnance od : ods) {
            if (od.getPart() == part) {
                list.add(od);
            }
        }
        return list;
    }

    private void addOrdnanceStrthLevel(Ordnance od) {
        int level = od.getStrengthLevel();
        od.setStrengthLevel(level + 1);
        pushOrdnanceToChangeList(od);
    }

    private void reduceOrdnanceStrthLevel(Ordnance od, int rduce) {
        int level = od.getStrengthLevel();
        level = level - rduce;
        if (level <= 0) {
            level = 0;
        }
        od.setStrengthLevel(level);
        pushOrdnanceToChangeList(od);
    }

    private void addremoLv(Ordnance od) {
        int level = od.getRemouldLv();
        od.setRemouldLv(level + 1);
        pushOrdnanceToChangeList(od);
    }

    public int getOrdnanceStrenLv(long id) {
        for (Ordnance od : ods) {
            if (od.getId() == id) {
                return od.getStrengthLevel();
            }
        }
        return -1;
    }

    public int getOrdnanceRemouLv(long id) {
        for (Ordnance od : ods) {
            if (od.getId() == id) {
                return od.getRemouldLv();
            }
        }
        return -1;
    }

    private void addStrengthLevel(Ordnance od) {
        int old = od.getStrengthLevel();
        od.setStrengthLevel(old + 1);
        pushOrdnanceToChangeList(od);
    }

    private void addRemoveLevel(Ordnance od) {
        int old = od.getRemouldLv();
        od.setRemouldLv(old + 1);
        pushOrdnanceToChangeList(od);
    }

    private void changOrdnancePostion(Ordnance od, int posi) {
        od.setPosition(posi);
        pushOrdnanceToChangeList(od);
    }

    //获得背包的数量
    public int getBagNum(){
        int num=0;
        for(Ordnance od:ods){
            if (od.getPosition()==0){
                num++;
            }
        }
        return num;
    }

    public long creatOrdnance(int typeId, int strenthlevel, int remoudLv, int logtype) {
        if(getBagNum()>=EquipDefine.ORDANCE_MAX_SIZE){
            return 0;
        }
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Ordnance odp = BaseDbPojo.create(Ordnance.class,areaKey);
        odp.setStrengthLevel(strenthlevel);
        odp.setRemouldLv(remoudLv);
        odp.setPlayerId(playerProxy.getPlayerId());
        odp.setTypeId(typeId);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE, typeId);
        if (jsonObject == null) {
            return -1;
        }
        odp.setPart(jsonObject.getInt("part"));
        odp.setQuality(jsonObject.getInt("quality"));
        odp.setType(jsonObject.getInt("type"));
        odp.setPosition(0);
        ods.add(odp);
        playerProxy.addOrdnanceToPlayer(odp.getId());
        odp.save();
        OrnanceGet ornanceGet = new OrnanceGet(logtype, typeId);
        sendPorxyLog(ornanceGet);
        ordnanceLog(1,typeId,logtype,odp.getId());
        return odp.getId();
    }

    public void delOrdnce(long id, int logtype) {
        Ordnance del = null;
        for (Ordnance od : ods) {
            if (od.getId() == id) {
                del = od;
            }
        }
        if (del != null) {
            del.del();
            changeOrdnances.remove(del);
            ods.remove(del);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.reduceOrdnanceToPlayer(del.getId());
            OrndanceLost orndanceLost = new OrndanceLost(logtype, del.getTypeId());
            sendPorxyLog(orndanceLost);
            ordnanceLog(0,del.getTypeId(),logtype,del.getId());
        }
    }

    public List<Common.OrdnanceInfo> getOrdnanceInfos() {
        List<Common.OrdnanceInfo> list = new ArrayList<Common.OrdnanceInfo>();
        for (Ordnance od : ods) {
            Common.OrdnanceInfo.Builder builder = Common.OrdnanceInfo.newBuilder();
            builder.setId(od.getId());
            builder.setPart(od.getPart());
            builder.setPosition(od.getPosition());
            builder.setQuality(od.getQuality());
            builder.setRemoulv(od.getRemouldLv());
            builder.setStrgthlv(od.getStrengthLevel());
            builder.setTypeid(od.getTypeId());
            builder.setType(od.getType());
            builder.setStrength(getOrdnanceStrength(od.getId()));
            list.add(builder.build());

        }
        return list;
    }


    public Common.OrdnanceInfo getOrdnanceInfo(long id) {
        Ordnance od = getOrdnanceByid(id);
        if (od != null) {
            Common.OrdnanceInfo.Builder builder = Common.OrdnanceInfo.newBuilder();
            builder.setId(od.getId());
            builder.setPart(od.getPart());
            builder.setPosition(od.getPosition());
            builder.setQuality(od.getQuality());
            builder.setRemoulv(od.getRemouldLv());
            builder.setStrgthlv(od.getStrengthLevel());
            builder.setTypeid(od.getTypeId());
            builder.setType(od.getType());
            builder.setStrength(getOrdnanceStrength(od.getId()));
            return builder.build();
        } else {
            Common.OrdnanceInfo.Builder builder = Common.OrdnanceInfo.newBuilder();
            builder.setId(id);
            builder.setPart(0);
            builder.setPosition(0);
            builder.setQuality(0);
            builder.setRemoulv(0);
            builder.setStrgthlv(0);
            builder.setTypeid(0);
            builder.setType(0);
            builder.setStrength(0);
            return builder.build();
        }

    }

    //获得某种佣兵配件加成
    public void getPower(int type, int sodierId) {
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        addpower(type, map);
        for (int i = 1; i <= 29; i++) {
            int add = 0;
            if (map.get(i) != null) {
                add = (int) Math.ceil(map.get(i));
            }
            soldierProxy.addPowerValue(i, add, sodierId);
        }
    }


    private void addpower(int type, Map<Integer, Double> map) {
        List<Ordnance> list = getListBytype(type);
        for (Ordnance od : list) {
            if (od.getPosition() != 0) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE, od.getTypeId());
                //穿刺
                double wreck = (jsonObject.getInt("wreckIni") + jsonObject.getInt("wreckStr") * od.getStrengthLevel() + jsonObject.getInt("wreckRem") * od.getRemouldLv());
                //防护
                double defend = (jsonObject.getInt("defIni") + jsonObject.getInt("defStr") * od.getStrengthLevel() + jsonObject.getInt("defRem") * od.getRemouldLv());
                //攻击
                double atk = (jsonObject.getInt("atkRateIni") + jsonObject.getInt("atkRateStr") * od.getStrengthLevel() + jsonObject.getInt("atkRateRem") * od.getRemouldLv());
                //生命
                double hp = (jsonObject.getInt("hpRateIni") + jsonObject.getInt("hpRateStr") * od.getStrengthLevel() + jsonObject.getInt("hpRateRem") * od.getRemouldLv());
                if (map.get(SoldierDefine.POWER_wreck) == null) {
                    map.put(SoldierDefine.POWER_wreck, wreck);
                } else {
                    double value = map.get(SoldierDefine.POWER_wreck);
                    map.put(SoldierDefine.POWER_wreck, wreck + value);
                }
                if (map.get(SoldierDefine.POWER_defend) == null) {
                    map.put(SoldierDefine.POWER_defend, defend);
                } else {
                    double value = map.get(SoldierDefine.POWER_defend);
                    map.put(SoldierDefine.POWER_defend, defend + value);
                }
                if (map.get(SoldierDefine.POWER_hpMaxRate) == null) {
                    map.put(SoldierDefine.POWER_hpMaxRate, hp);
                } else {
                    double value = map.get(SoldierDefine.POWER_hpMaxRate);
                    map.put(SoldierDefine.POWER_hpMaxRate, hp + value);
                }
                if (map.get(SoldierDefine.POWER_atkRate) == null) {
                    map.put(SoldierDefine.POWER_atkRate, atk);
                } else {
                    double value = map.get(SoldierDefine.POWER_atkRate);
                    map.put(SoldierDefine.POWER_atkRate, atk + value);
                }
            }
        }
    }


    //获得配件强度
    public int getOrdnanceStrength(long id) {
        Ordnance od = getOrdnanceByid(id);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE, od.getTypeId());
        //穿刺
        double wreck = (jsonObject.getInt("wreckIni") + jsonObject.getInt("wreckStr") * od.getStrengthLevel() + jsonObject.getInt("wreckRem") * od.getRemouldLv()) / 100.0;
        //防护
        double defend = (jsonObject.getInt("defIni") + jsonObject.getInt("defStr") * od.getStrengthLevel() + jsonObject.getInt("defRem") * od.getRemouldLv()) / 100.0;
        //攻击
        double atk = (jsonObject.getInt("atkRateIni") + jsonObject.getInt("atkRateStr") * od.getStrengthLevel() + jsonObject.getInt("atkRateRem") * od.getRemouldLv()) / 10000.0;
        //生命
        double hp = (jsonObject.getInt("hpRateIni") + jsonObject.getInt("hpRateStr") * od.getStrengthLevel() + jsonObject.getInt("hpRateRem") * od.getRemouldLv()) / 10000.0;
        double value = wreck * EquipDefine.ORDANC_STRENTH_TYPE2 + defend * EquipDefine.ORDANC_STRENTH_TYPE2 + atk * EquipDefine.ORDANC_STRENTH_TYPE1 + hp * EquipDefine.ORDANC_STRENTH_TYPE1;
        return (int) value;

    }


    //获得某个位置最好的配件
    private Ordnance getBestOrdnance(int type, int part) {
        Ordnance odtemp = null;
        if (isHasOrdnance(type, part) == false) {
            for (Ordnance od : ods) {
                if (od.getPart() == part && od.getType() == type && od.getPosition() == 0) {
                    if (odtemp == null) {
                        odtemp = od;
                    } else {
                        if (od.getQuality() > odtemp.getQuality()) {
                            odtemp = od;
                        } else if (od.getQuality() == odtemp.getQuality()) {
                            if (od.getRemouldLv() > odtemp.getRemouldLv()) {
                                odtemp = od;
                            } else if (od.getRemouldLv() == odtemp.getRemouldLv()) {
                                if (od.getStrengthLevel() > odtemp.getStrengthLevel()) {
                                    odtemp = od;
                                }
                            }
                        }
                    }
                }
            }
        }
        return odtemp;
    }

    /********
     * 穿上军械
     *********/
    public int ordnanceOn(long id, List<M4.M40000.S2C.Builder> builder1, List<BaseLog> baseLogs, M13.M130104.S2C.Builder builder) {
        Ordnance od = getOrdnanceByid(id);
        if (od == null) {
            return ErrorCodeDefine.M130104_1;
        }
        if (od.getPosition() != 0) {
            return ErrorCodeDefine.M130104_2;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ORDNANCE_PART, "part", od.getPart());
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) < jsonObject.getInt("openlv")) {
            return ErrorCodeDefine.M130104_4;
        }
        Ordnance ordnanceold = getOrdnance(od.getType(), od.getPart());
        if (ordnanceold != null) {
            changOrdnancePostion(ordnanceold, 0);
            builder.addOdInfos(getOrdnanceInfo(ordnanceold.getId()));
        }
        changOrdnancePostion(od, 1);
        builder1.add(initSoldierInfoBytype(od.getType()));
        OrdnanceLog ordnanceLog = new OrdnanceLog(od.getId(), LogDefine.ORDNANCEON, od.getTypeId(), od.getStrengthLevel(), od.getRemouldLv());
        baseLogs.add(ordnanceLog);
        sendFunctionLog(FunctionIdDefine.ORDNANCE_ON_FUNCTION_ID, id, od.getTypeId(),od.getPart(),od.getType()+"");
        return 0;
    }

    /****************
     * 卸下军械
     ***********/
    public int ordnanceOff(long id, List<M4.M40000.S2C.Builder> builder1, List<BaseLog> baseLogs) {
        Ordnance od = getOrdnanceByid(id);
        if (od == null) {
            return ErrorCodeDefine.M130105_1;
        }
        if (od.getPosition() == 0) {
            return ErrorCodeDefine.M130105_2;
        }
        if(getBagNum()>=EquipDefine.ORDANCE_MAX_SIZE){
            return ErrorCodeDefine.M130105_3;
        }
        changOrdnancePostion(od, 0);
        builder1.add(initSoldierInfoBytype(od.getType()));
        OrdnanceLog ordnanceLog = new OrdnanceLog(od.getId(), LogDefine.ORDNANCEOFF, od.getTypeId(), od.getStrengthLevel(), od.getRemouldLv());
        baseLogs.add(ordnanceLog);
        sendFunctionLog(FunctionIdDefine.ORDNANCE_OFF_FUNCTION_ID, id, od.getTypeId(), od.getPart(), od.getType() + "");
        return 0;
    }

    /****************
     * 分解军械
     ***********/
    public int dropOrdnanceOff(List<Long> idlist, List<Integer> itemlist, List<BaseLog> baseLogs) {
        for (long id : idlist) {
            Ordnance od = getOrdnanceByid(id);
            if (od == null) {
                return ErrorCodeDefine.M130106_1;
            }
            if (od.getPosition() != 0) {
                return ErrorCodeDefine.M130106_2;//已经穿在身上的军械不能分解
            }
        }
        StringBuffer sb = new StringBuffer();
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (long id : idlist) {
            Ordnance od = getOrdnanceByid(id);
            //z执行分解
            delOrdnce(id, LogDefine.LOST_ORDANCE_DROP);
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            JSONObject jsonObjectstr = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ORDNANCE_STR, "level", od.getStrengthLevel(), "quality", od.getQuality());
            sb.append(od.getStrengthLevel()+","+od.getQuality()+","+od.getPart()+","+od.getRemouldLv()+"&");
            JSONArray arraystr = null;
            if (od.getPart() <= 4) {
                arraystr = jsonObjectstr.getJSONArray("onetofourresolve");
            } else {
                arraystr = jsonObjectstr.getJSONArray("fivetoreightresolve");
            }
            for (int i = 0; i < arraystr.length(); i++) {
                JSONArray array = arraystr.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                playerProxy.addPowerValue(typeId,num, LogDefine.GET_DROP_ORNDANCE);
            }
            JSONObject jsonObjectremo = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.ORDNANCE_REM, "quality", od.getQuality(), "part", od.getPart(), "remouldLv", od.getRemouldLv());
            JSONArray arrayremo = jsonObjectremo.getJSONArray("remould");
            for (int i = 0; i < arrayremo.length(); i++) {
                JSONArray array = arrayremo.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                itemProxy.addItem(typeId, num, LogDefine.GET_DROP_ORNDANCE);
                itemlist.add(typeId);
            }
            OrdnanceLog ordnanceLog = new OrdnanceLog(od.getId(), LogDefine.ORDNANCEDROP, od.getTypeId(), od.getStrengthLevel(), od.getRemouldLv());
            baseLogs.add(ordnanceLog);
        }
        sendFunctionLog(FunctionIdDefine.ORDNANCE_DROP_FUNCTION_ID,0,0,0,sb.toString());
        return 0;
    }

    /**************
     * 强化军械
     ***********/
    public int intensifyOrdnance(long id, int num, M13.M130107.S2C.Builder builder, List<Integer> itemlist, List<M4.M40000.S2C.Builder> builder1, List<BaseLog> baseLogs) {
        Ordnance od = getOrdnanceByid(id);
        if (od == null) {
            return ErrorCodeDefine.M130107_1;//没有该军械
        }
        JSONObject jsonObjectstr = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ORDNANCE_STR, "level", od.getStrengthLevel(), "quality", od.getQuality());
        if (jsonObjectstr == null) {
            return ErrorCodeDefine.M130107_2;
        }
        int rate = jsonObjectstr.getInt("rate");
        if (rate == 0) {
            return ErrorCodeDefine.M130107_3;
        }
        JSONArray array = null;
        if (od.getPart() <= 4) {
            array = jsonObjectstr.getJSONArray("onetofourneed");
        } else {
            array = jsonObjectstr.getJSONArray("fivetoreightneed");
        }
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (int i = 0; i < array.length(); i++) {
            JSONArray jsonArray = array.getJSONArray(i);
            int typeId = jsonArray.getInt(0);
            int itemnum = jsonArray.getInt(1);
            if (playerProxy.getPowerValue(typeId) < itemnum) {
                return ErrorCodeDefine.M130107_4;
            }
        }
        if (itemProxy.getItemNum(EquipDefine.ORDNANCE_STRENGTH_ADDRATE_ITEM) < num) {
            return ErrorCodeDefine.M130107_5;
        }
        int addrate = num * EquipDefine.ORDNANCE_STRENGTH_ADDRATE_NUM;
       /* if (addrate + rate - 1000 > EquipDefine.ORDNANCE_STRENGTH_ADDRATE_NUM) {
            return ErrorCodeDefine.M130107_7;
        }*/
        //vip加成功率
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int vipRate = vipProxy.getVipNum(ActorDefine.VIP_STRENGBASERATE);
        rate += (addrate + vipRate);
        if (num > 0) {
            itemProxy.reduceItemNum(EquipDefine.ORDNANCE_STRENGTH_ADDRATE_ITEM, num, LogDefine.LOST_ORNDANCE_STRENGTH);
            itemlist.add(EquipDefine.ORDNANCE_STRENGTH_ADDRATE_ITEM);
        }
        int random = RandomUtil.random(0, 1000);
        int falg = 0;
        if (random > rate) {
            ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            //扣除资源
            for (int i = 0; i < array.length(); i++) {
                JSONArray jsonArray = array.getJSONArray(i);
                int typeId = jsonArray.getInt(0);
                int itemnum = jsonArray.getInt(1);
                itemnum= (int) Math.ceil(itemnum * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_ORDANCE_STRENGTH)) / 100.0);
                playerProxy.reducePowerValue(typeId, itemnum, LogDefine.LOST_ORNDANCE_STRENGTH);
            }
            return ErrorCodeDefine.M130107_6;//幸运值不足
        } else {
            addOrdnanceStrthLevel(od);
            falg = 1;
            //扣除资源
            for (int i = 0; i < array.length(); i++) {
                JSONArray jsonArray = array.getJSONArray(i);
                int typeId = jsonArray.getInt(0);
                int itemnum = jsonArray.getInt(1);
                playerProxy.reducePowerValue(typeId, itemnum, LogDefine.LOST_ORNDANCE_STRENGTH);
            }
        }
        OrdnanceLog ordnanceLog = new OrdnanceLog(od.getId(), LogDefine.ORDNANCESTRENGTH, od.getTypeId(), od.getStrengthLevel(), od.getRemouldLv());
        ordnanceLog.setFalg(falg);
        ordnanceLog.setUseItem(num);
        baseLogs.add(ordnanceLog);
        builder.addOdInfos(getOrdnanceInfo(id));
        builder1.add(initSoldierInfoBytype(od.getType()));
        sendFunctionLog(FunctionIdDefine.INTENSIFY_ORDNANCE_FUNCTION_ID, id,od.getTypeId(),num);
        return 0;
    }


    /***********
     * 改造军械
     ************/
    public int remouldOrndance(long id, int type, M13.M130108.S2C.Builder builder, List<Integer> itemlist, List<M4.M40000.S2C.Builder> builder1, List<BaseLog> baseLogs) {
        Ordnance od = getOrdnanceByid(id);
        if (od == null) {
            return ErrorCodeDefine.M130108_1;
        }
        JSONObject jsonObjectremo = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.ORDNANCE_REM, "quality", od.getQuality(), "part", od.getPart(), "remouldLv", od.getRemouldLv());
        JSONArray jsonArray = jsonObjectremo.getJSONArray("need");
        if (jsonArray.length() == 0) {
            return ErrorCodeDefine.M130108_2;//改造已经满级了
        }
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int typeId = array.getInt(0);
            int num = array.getInt(1);
            if (itemProxy.getItemNum(typeId) < num) {
                return ErrorCodeDefine.M130108_3;//道具不足
            }
        }
        if (type == 1) {
            if (itemProxy.getItemNum(EquipDefine.ORDNANCE_REMOULD_ADDRATE_NUM) < 5) {
                return ErrorCodeDefine.M130108_4;
            }
        }
       /* if (od.getStrengthLevel() < EquipDefine.ORDNANCE_REMOULD_NEED_STRENGTH_LEVEL) {
            return ErrorCodeDefine.M130108_5;//强化等级不足
        }*/
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int typeId = array.getInt(0);
            int num = array.getInt(1);
            itemProxy.reduceItemNum(typeId, num, LogDefine.LOST_ORNDANCE_GAIZAO);
            itemlist.add(typeId);
        }
        if (type == 1) {
            itemProxy.reduceItemNum(EquipDefine.ORDNANCE_REMOULD_ADDRATE_NUM, 5, LogDefine.LOST_ORNDANCE_GAIZAO);
            itemlist.add(EquipDefine.ORDNANCE_REMOULD_ADDRATE_NUM);
        } else {
            ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            if(activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_ORDANCE_remou)!=0) {
                reduceOrdnanceStrthLevel(od, 3);
            }
        }
        addremoLv(od);
        builder.addOdInfos(getOrdnanceInfo(id));
        builder1.add(initSoldierInfoBytype(od.getType()));
        OrdnanceLog ordnanceLog = new OrdnanceLog(od.getId(), LogDefine.ORDNANCESTRGAIZAO, od.getTypeId(), od.getStrengthLevel(), od.getRemouldLv());
        ordnanceLog.setUseItem(type);
        baseLogs.add(ordnanceLog);
        sendFunctionLog(FunctionIdDefine.REMOULD_ORDNANCE_FUNCTION_ID, id,od.getTypeId(),od.getRemouldLv());
        return 0;
    }

    /*********
     * 军械进化
     **********/
    public int ordnanceAdvance(long id, M13.M130109.S2C.Builder builder, PlayerReward reward, List<M4.M40000.S2C.Builder> builder1, List<BaseLog> baseLogs) {
        Ordnance od = getOrdnanceByid(id);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (od == null) {
            return ErrorCodeDefine.M130109_1;//没有该军械
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ORDNANCE, od.getTypeId());
        if (jsonObject.getInt("isadvance") == 0) {
            return ErrorCodeDefine.M130109_2;//该军械不能进化
        }
        JSONObject jsonObjectremo = ConfigDataProxy.getConfigInfoFindByThreeKey(DataDefine.ORDNANCE_ADVANCE, "quality", od.getQuality(), "part", od.getPart(), "remouldLv", od.getRemouldLv());
        if (jsonObjectremo == null) {
            return ErrorCodeDefine.M130109_4;//碎片不足
        }
        int position = od.getPosition();
        OrdnancePieceProxy ordnancePieceProxy = getGameProxy().getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        JSONArray advanceitem = jsonObject.getJSONArray("advanceitem");
        for (int i = 0; i < advanceitem.length(); i++) {
            JSONArray array = advanceitem.getJSONArray(i);
            int typeId = array.getInt(0);
            int num = array.getInt(1);
            if (ordnancePieceProxy.getOrdnancePieceNum(typeId) < num) {
                return ErrorCodeDefine.M130109_3;//
            }
        }
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        //返还道具
        JSONObject jsonObjectstr = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ORDNANCE_STR, "level", od.getStrengthLevel(), "quality", od.getQuality());
        JSONArray arraystr = null;
        if (od.getPart() <= 4) {
            arraystr = jsonObjectstr.getJSONArray("onetofourresolve");
        } else {
            arraystr = jsonObjectstr.getJSONArray("fivetoreightresolve");
        }
        for (int i = 0; i < arraystr.length(); i++) {
            JSONArray array = arraystr.getJSONArray(i);
            int typeId = array.getInt(0);
            int num = array.getInt(1);
            itemProxy.addItem(typeId, num, LogDefine.GET_ORNDANCE_ADVANCE);
            rewardProxy.addItemToReward(reward, typeId, num);
        }
        JSONArray jsonArray = jsonObjectremo.getJSONArray("reback");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int typeId = array.getInt(0);
            int num = array.getInt(1);
            itemProxy.addItem(typeId, num, LogDefine.GET_ORNDANCE_ADVANCE);
            System.out.print("");
            rewardProxy.addItemToReward(reward, typeId, num);
            ;
        }

        for (int i = 0; i < advanceitem.length(); i++) {
            JSONArray array = advanceitem.getJSONArray(i);
            int typeId = array.getInt(0);
            int num = array.getInt(1);
            ordnancePieceProxy.reduceOrdnancePieceNum(typeId, num, LogDefine.LOST_ORDANCE_ADVANCE);
            rewardProxy.addOrdanceFragmentToReward(reward, typeId, num);
        }
        delOrdnce(od.getId(), LogDefine.LOST_ORDANCE_ADVANCE);
        long newid = creatOrdnance(jsonObject.getInt("advancetarget"), 0, 0, LogDefine.GET_ORNDANCE_ADVANCE);
        changOrdnancePostion(getOrdnanceByid(newid), position);
        builder.addOdInfos(getOrdnanceInfo(newid));
        builder1.add(initSoldierInfoBytype(od.getType()));
        OrdnanceLog ordnanceLog = new OrdnanceLog(od.getId(), LogDefine.ORDNANCESTRADVANCE, od.getTypeId(), od.getStrengthLevel(), od.getRemouldLv());
        ordnanceLog.setAdTypeId(jsonObject.getInt("advancetarget"));
        baseLogs.add(ordnanceLog);
        sendFunctionLog(FunctionIdDefine.ORDNANCE_ADVANCE_FUNCTION_ID, id, od.getTypeId(),newid);
        return 0;
    }

    public M4.M40000.S2C.Builder initSoldierInfoBytype(int type) {
        M4.M40000.S2C.Builder builder = M4.M40000.S2C.newBuilder();
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        List<Integer> solList = soldierProxy.getSodierByType(type);
        for (int soldierId : solList) {
            soldierProxy.initPowerValue(soldierId);
        }
        for (int soldierId : solList) {
            builder.addSoldiers(soldierProxy.getSoldierInfo(soldierId));
        }
        return builder;
    }


    public List<Long> delAllEquip() {
        List<Long> list = new ArrayList<Long>();
        for (Ordnance od : ods) {
            list.add(od.getId());
        }
        for (Long id : list) {
            delOrdnce(id, LogDefine.LOST_BUY_CHEAT);
        }
        return list;
    }

    //获得仓库军械的数量
    public int getOrdnanceOnbag() {
        int num = 0;
        for (Ordnance od : ods) {
            if (od.getPosition() == 0) {
                num++;
            }
        }
        return num;
    }

    public int ordanceFreeSize(){
        int num=0;
        for(Ordnance odn:ods){
            if(odn.getPosition()==0){
                num++;
            }
        }
        return EquipDefine.ORDANCE_MAX_SIZE-num;
    }


    /**
     * tbllog_items 军械opt:1增加，0使用
     */
    public void ordnanceLog(int opt, int typeid, int logType,long id) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_ordnance itemslog = new tbllog_ordnance();
        itemslog.setPlatform(cache.getPlat_name());
        itemslog.setRole_id(player.getPlayerId());
        itemslog.setAccount_name(player.getAccountName());
        itemslog.setDim_level(player.getLevel());
        itemslog.setOpt(opt);
        itemslog.setAction_id(logType);
        itemslog.setType_id(typeid);
        itemslog.setOrdance_id(id);
        itemslog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(itemslog);
    }

}
