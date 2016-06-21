package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.log.EquipGet;
import com.znl.log.EquipLog;
import com.znl.log.EquipLost;
import com.znl.log.admin.tbllog_equip;
import com.znl.log.admin.tbllog_ordnance;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Activity;
import com.znl.pojo.db.Equip;
import com.znl.pojo.db.Item;
import com.znl.pojo.db.Player;
import com.znl.proto.Common;
import com.znl.proto.M13;
import com.znl.proto.M9;
import com.znl.template.MailTemplate;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.tools.cmd.gen.AnyVals;
import scala.tools.reflect.FormatInterpolator;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class EquipProxy extends BasicProxy {
    private Set<Equip> equips = new ConcurrentHashSet<Equip>();

    @Override
    public void shutDownProxy() {
        for (Equip equip : equips) {
            equip.finalize();
        }
    }

    @Override
    protected void init() {

    }

    public EquipProxy(Set<Long> eqIds,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : eqIds) {
            Equip eq = BaseDbPojo.get(id, Equip.class,areaKey);
            if (eq != null) {
                equips.add(eq);
            }
        }
    }


    public void saveEquips() {
        List<Equip> eqs = new ArrayList<Equip>();
        synchronized (changeEquips) {
            while (true) {
                Equip eq = changeEquips.poll();
                if (eq == null) {
                    break;
                }
                eqs.add(eq);
            }
        }
        for (Equip eq : eqs) {
            eq.save();
        }

    }

    private LinkedList<Equip> changeEquips = new LinkedList<Equip>();

    private void pushEquipToChangeList(Equip eq) {
        //插入更新队列
        synchronized (changeEquips) {
            if (!changeEquips.contains(eq)) {
                changeEquips.offer(eq);
            }
        }
    }

    //返回-1没有该装备
    public int getEquipTypeId(long id) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                return eq.getTypeId();
            }
        }
        return -1;
    }

    //返回null没有该装备
    public Equip getEquip(long id) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                return eq;
            }
        }
        return null;
    }

    private List<Equip> getEquipbyListid(List<Long> list) {
        List<Equip> eqlist = new ArrayList<Equip>();
        for (Equip eq : equips) {
            if (list.contains(eq.getId())) {
                eqlist.add(eq);
            }
        }
        return eqlist;
    }

    //返回-1没有该装备
    public int getEquipLevel(long id) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                return eq.getLevel();
            }
        }
        return -1;
    }

    //获取装备经验
    public int getEquipExp(long id) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                return eq.getExp();
            }
        }
        return -1;
    }


    private void addEquipLevel(Equip eq) {
        eq.setLevel(eq.getLevel() + 1);
        pushEquipToChangeList(eq);
    }

    private void setEquipExp(Equip eq, int exp) {
        eq.setExp(exp);
        pushEquipToChangeList(eq);
    }

    public int getEquipPosition(long id) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                return eq.getPosition();
            }
        }
        return -1;
    }

    public void changEquipPostion(long id, int position) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                eq.setPosition(position);
                pushEquipToChangeList(eq);
            }
        }
    }

    private void changEquipPostion(Equip eq, int position) {

        eq.setPosition(position);
        pushEquipToChangeList(eq);

    }


    //获得装备的信息
    public Common.EquipInfo getEquipInfo(long id) {
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                Common.EquipInfo.Builder info = Common.EquipInfo.newBuilder();
                info.setExp(eq.getExp());
                info.setId(eq.getId());
                info.setLevel(eq.getLevel());
                info.setPosition(eq.getPosition());
                info.setTypeid(eq.getTypeId());
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
                info.setQuality(jsonObject.getInt("quality"));
                info.setType(jsonObject.getInt("functionType"));
                info.setUpproperty(jsonObject.getInt("upproperty"));
                return info.build();
            }
        }
        Common.EquipInfo.Builder info = Common.EquipInfo.newBuilder();
        info.setExp(0);
        info.setId(id);
        info.setLevel(0);
        info.setPosition(0);
        info.setTypeid(0);
        info.setQuality(0);
        info.setType(0);
        info.setUpproperty(0);
        return info.build();
    }


    //获取所有装备的信息
    public List<Common.EquipInfo> getEquipInfos() {
        List<Common.EquipInfo> list = new ArrayList<Common.EquipInfo>();
        for (Equip eq : equips) {
            Common.EquipInfo.Builder info = Common.EquipInfo.newBuilder();
            info.setExp(eq.getExp());
            info.setId(eq.getId());
            info.setLevel(eq.getLevel());
            info.setPosition(eq.getPosition());
            info.setTypeid(eq.getTypeId());
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
            if (jsonObject == null) {
                System.out.print(1);
            }
            info.setQuality(jsonObject.getInt("quality"));
            info.setType(jsonObject.getInt("functionType"));
            info.setUpproperty(jsonObject.getInt("upproperty"));
            list.add(info.build());
        }
        if(list.size()==0) {
            PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            System.err.println(playerProxy.getPlayerName()+"装备是空的！！！！！！！！！！");
        }
        return list;
    }

    //获取所有装备的信息
    public List<Common.EquipInfo> getEquipInfos(List<Equip> listeq) {
        List<Common.EquipInfo> list = new ArrayList<Common.EquipInfo>();
        for (Equip eq : listeq) {
            Common.EquipInfo.Builder info = Common.EquipInfo.newBuilder();
            info.setExp(eq.getExp());
            info.setId(eq.getId());
            info.setLevel(eq.getLevel());
            info.setPosition(eq.getPosition());
            info.setTypeid(eq.getTypeId());
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
            info.setQuality(jsonObject.getInt("quality"));
            info.setType(jsonObject.getInt("functionType"));
            info.setUpproperty(jsonObject.getInt("upproperty"));
            list.add(info.build());
        }
        return list;
    }

    public int getBagNum(){
        int num=0;
        for(Equip eq:equips){
            if (eq.getPosition()==0){
                num++;
            }
        }
        return num;
    }


    public Long addEquip(int typeId, int logType) {

        return creatEquip(typeId, logType);
    }

    private long creatEquip(int typeId, int logType) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(getBagNum()>=playerProxy.getPowerValue(PlayerPowerDefine.POWER_equipsize)){
            MailTemplate template = new MailTemplate("武将派遣", "尊敬的主公：由于您的武将府邸已经人满为患，您新获得的武将仍流落在外，请先整顿或扩充将军府邸，再来招募您的武将。", 0, "系统", ChatAndMailDefine.MAIL_TYPE_SYSTEM);
            Set<Long> allid = new HashSet<>();
            allid.add(playerProxy.getPlayerId());
            List<Integer[]> add=new ArrayList<Integer[]>();
            add.add(new Integer[]{402,typeId,1});
            template.setAttachments(add);
            GameMsg.SendMail mail = new GameMsg.SendMail(allid, template, "系统邮件", 0l);
            sendMailServiceMsg(mail);
            return 0;
        }
        Equip equip = BaseDbPojo.create(Equip.class,areaKey);
        equip.setTypeId(typeId);
        equip.setPlayerId(playerProxy.getPlayerId());
        equip.setPosition(0);
        equip.setExp(0);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, typeId);
        equip.setLevel(jsonObject.getInt("dropLvl"));
        equip.setPartId(jsonObject.getInt("partId"));
        equips.add(equip);
        playerProxy.addEquipToPlayer(equip.getId());
        equip.save();
        EquipGet equipGet = new EquipGet(logType, typeId);
        sendPorxyLog(equipGet);
        tbllog_equip(1,typeId,logType,equip.getId());
        return equip.getId();
    }

    private void delEquip(long id, int logtype) {
        Equip deq = null;
        for (Equip eq : equips) {
            if (eq.getId() == id) {
                deq = eq;
                break;
            }
        }
        if (deq != null) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            deq.del();
            equips.remove(deq);
            changeEquips.remove(deq);
            playerProxy.reduceEquipfromPlayer(id);
            EquipLost equipLost = new EquipLost(logtype, deq.getTypeId());
            sendPorxyLog(equipLost);
            tbllog_equip(0,deq.getTypeId(),logtype,deq.getId());
        }

    }



    //装备升级
    public boolean equipLevelUp(long id, int exp) {
        Equip eq = getEquip(id);
        if (eq == null) {
            return false;
        }
        JSONObject eqjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
        int color = eqjson.getInt("quality");
        boolean falg = false;
        while (true) {
            JSONObject poerjson = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", color, "lv", eq.getLevel());
            int needexp = poerjson.getInt("expneed");
            if (needexp == 0) {
                //已经满级了
                int equipexp = eq.getExp();
                setEquipExp(eq, equipexp + exp);
                break;
            }
            int equipexp = eq.getExp();
            if (exp + equipexp >= needexp) {
                addEquipLevel(eq);
                setEquipExp(eq, 0);
                exp -= (needexp - equipexp);
                falg = true;
            } else {
                setEquipExp(eq, equipexp + exp);
                break;
            }
        }
        return falg;
    }

    //获得某个位置的所有装备
    private List<Equip> getEquipListByPosition(int position) {
        List<Equip> list = new ArrayList<Equip>();
        for (Equip eq : equips) {
            if (eq.getPosition() == position) {
                list.add(eq);
            }
        }
        return list;
    }

    //获得某个槽位的power加成
    public int getPositionPower(int postion, int power) {
        Map<Integer, Integer> map = getpowerMapByPostion(postion);
        if (map.get(power) == null) {
            return 0;
        } else {
            return map.get(power);
        }
    }

    public long getMaxPower(int type) {
        int power = 0;
        switch (type) {
            case PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN:
                power = PlayerPowerDefine.NOR_POWER_atkRate;
                break;
            case PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN:
                power = PlayerPowerDefine.NOR_POWER_critRate;
                break;
            case PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN:
                power = PlayerPowerDefine.NOR_POWER_dodgeRate;
                break;
        }
        long max = 0;
        int level = 0;
        for (Equip eq : equips) {
            if (eq.getPosition() != 0) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
                JSONArray proarray = jsonObject.getJSONArray("fixproperty");
                int poweradd = getAddPower(jsonObject.getInt("upproperty"), jsonObject.getInt("quality"), eq.getLevel());
                if (poweradd > max && jsonObject.getInt("upproperty") == power) {
                    max = poweradd;
                    level = eq.getLevel();
                    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    if(level > 0){
                        if(type == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN){
                            playerProxy.setPowerValue(PlayerPowerDefine.POWER_atklv,(long)level);
                        }else if(type == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN){
                            playerProxy.setPowerValue(PlayerPowerDefine.POWER_critlv,(long)level);
                        }else if(type == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN){
                            playerProxy.setPowerValue(PlayerPowerDefine.POWER_dogelv,(long)level);
                        }
                    }
                }
            }
        }
        return max;
    }

    public Map<Integer, Integer> getpowerMapByPostion(int postion) {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        Set<Equip> equiptemps = new HashSet<Equip>();
        equiptemps.addAll(equips);
        for (Equip eq : equiptemps) {
            if (eq.getPosition() == postion) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
                JSONArray proarray = jsonObject.getJSONArray("fixproperty");
                if (proarray.length() > 0) {
                    addPower(map, proarray.getInt(0), proarray.getInt(1));
                }
                int poweradd = getAddPower(jsonObject.getInt("upproperty"), jsonObject.getInt("quality"), eq.getLevel());
                addPower(map, jsonObject.getInt("upproperty"), poweradd);
            }
        }
        //套装效果
        int suitId = getSuitEffect(postion);
        if (suitId != -1) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_SUIT, suitId);
            JSONArray jsonArray = jsonObject.getJSONArray("property");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                int power = array.getInt(0);
                int value = array.getInt(1);
                addPower(map, power, value);
            }
        }
        return map;
    }

    private void addPower(Map<Integer, Integer> map, int key, int value) {
        if (map.get(key) == null) {
            map.put(key, value);
        } else {
            int sum = map.get(key) + value;
            map.put(key, sum);
        }
    }

    private int getAddPower(int power, int color, int level) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", color, "lv", level);
        int value = 0;
        switch (power) {
            case 12:
                value = jsonObject.getInt("atkRate");
                break;
            case 11:
                value = jsonObject.getInt("hpRate");
                break;
            case 4:
                value = jsonObject.getInt("hitRate");
                break;
            case 5:
                value = jsonObject.getInt("dodgeRate");
                break;
            case 6:
                value = jsonObject.getInt("critRate");
                break;
            case 7:
                value = jsonObject.getInt("defRate");
                break;

        }

        return value;
    }


    //某个位置的套装效果
    public int getSuitEffect(int position) {
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
        List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigAllInfo(DataDefine.EQUIP_SUIT);
        for (JSONObject jsonObject : jsonObjectList) {
            JSONArray jsonArray = jsonObject.getJSONArray("partneed");
            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < jsonArray.length(); i++) {
                int typeId = jsonArray.getInt(i);
                list.add(typeId);
            }
            map.put(jsonObject.getInt("ID"), list);
        }
        for (Equip eq : equips) {
            if (eq.getPosition() == position) {
                for (int key : map.keySet()) {
                    List maplist = map.get(key);
                    maplist.remove((Integer) eq.getTypeId());
                    map.put(key, maplist);
                }
            }
        }
        for (int key : map.keySet()) {
            List maplist = map.get(key);
            if (maplist.size() == 0) {
                return key;
            }
        }
        return -1;
    }


    /*****
     * 装备升级
     ******/
    public int equipLevelUp(long id, List<Long> useid, M13.M130001.S2C.Builder builder, List<BaseLog> baseLogs) {
        Equip equip = getEquip(id);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (equip == null) {
            return ErrorCodeDefine.M130001_1;//装备不存在
        }
        JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, equip.getTypeId());
        if (jsoneq.getInt("functionType") != EquipDefine.EQUIP_TYPE_EQUOP) {
            return ErrorCodeDefine.M130001_2;//该类型装备不能升级
        }
        JSONObject jsonObjectlevel = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", jsoneq.getInt("quality"), "lv", equip.getLevel());
        if (jsonObjectlevel.getInt("expneed") == 0) {
            return ErrorCodeDefine.M130001_3;//已经满级不能升级
        }
        int level = equip.getLevel();
        int oldlevel = equip.getLevel();
        int maxneedexp = 0;
        if(level+1> playerProxy.getLevel()){
            return ErrorCodeDefine.M130001_6;//武将的等级不能超过主公等级
        }
        while (true) {
            JSONObject jsonObjectlevelup = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", jsoneq.getInt("quality"), "lv", level);
            level++;
            maxneedexp += jsonObjectlevelup.getInt("expneed");
            if (jsonObjectlevelup.getInt("expneed") == 0) {
                break;
            }
        }
        List<Equip> useequip = getEquipbyListid(useid);
        if (useid.size() != useequip.size()) {
            return ErrorCodeDefine.M130001_4;//有不存在的装备
        }
        if (getEquipaddExp(useequip, maxneedexp) == false) {
            List<Equip> useReally = getNeedEquip(useequip, maxneedexp);
            useequip.clear();
            useequip.addAll(useReally);
        }
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int add = getEquipaddExp(useequip);
        add= (int) Math.ceil(add*(100+activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_EQUIP_LEVEL_UP_ADDEXP))/100.0);
        //扣除装备
        StringBuffer sb = new StringBuffer();
        for (Equip equip1 : useequip) {
            delEquip(equip1.getId(), LogDefine.LOST_UPEQUIP_LV);
            builder.addEquipinfos(getEquipInfo(equip1.getId()));
            sb.append(equip1.getTypeId());
            sb.append(",");
        }
        //执行升级
        equipLevelUp(id, add);
        EquipLog equipLog = new EquipLog(LogDefine.EQUIPLEVEL, id, equip.getTypeId(), equip.getPosition(), equip.getLevel(), level);
        baseLogs.add(equipLog);
        //发送系统聊天判断
        List<JSONObject> list = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.SYSTEM_NOTICE,"type",ActorDefine.GENERAL_UPGRADE_NOTICE_TYPE);
        for(JSONObject lists :list) {
            if(lists.getInt("condition1") == jsoneq.getInt("partId") && jsoneq.getInt("quality") == ActorDefine.GENERAL_QUALITY){
                if(equip.getLevel() == lists.getInt("condition2") && equip.getLevel() > oldlevel){
                    float pcent1 = getAddPower(jsoneq.getInt("upproperty"), jsoneq.getInt("quality"), equip.getLevel());
                    float pcent = pcent1/100;
                    String property = String.valueOf(pcent);
                    playerProxy.sendSystemchat(ActorDefine.GENERAL_UPGRADE_NOTICE_TYPE, lists.getInt("condition1"), jsoneq.getString("name"), String.valueOf(equip.getLevel()), property);//发送系统公告5
                }
                break;
            }
        }
        sendFunctionLog(FunctionIdDefine.EQUIP_UPGRADE_FUNCTION_ID, id, equip.getTypeId(), useid.size(), sb.toString());
        return 0;
    }

    private List<Equip> getNeedEquip(List<Equip> eqlist, int need) {
        List<Equip> listeq = new ArrayList<Equip>();
        Map<Equip, Integer> map = new HashMap<Equip, Integer>();
        for (int i = 0; i < eqlist.size(); i++) {
            Equip eq = eqlist.get(i);
            JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
            int value = 0;
            if (jsoneq.getInt("functionType") == EquipDefine.EQUIP_TYPE_EQUOP) {
                JSONObject jsonObjectlevel = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", jsoneq.getInt("quality"), "lv", eq.getLevel());
                value = jsonObjectlevel.getInt("exp");
            } else {
                value += jsoneq.getInt("eatedExp");
            }
            map.put(eq, value);
        }
        List<Map.Entry<Equip, Integer>> list = new ArrayList<Map.Entry<Equip, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Equip, Integer>>() {
            //升序排序
            public int compare(Map.Entry<Equip, Integer> o1,
                               Map.Entry<Equip, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });

        for (Map.Entry<Equip, Integer> mapping : list) {
            need -= mapping.getValue();
            listeq.add(mapping.getKey());
            if (need <= 0) {
                break;
            }
        }

        return listeq;
    }


    private boolean getEquipaddExp(List<Equip> eqlist, int need) {
        int sum = 0;
        int num = 0;
        for (Equip eq : eqlist) {
            JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
            if (jsoneq.getInt("functionType") == EquipDefine.EQUIP_TYPE_EQUOP) {
                JSONObject jsonObjectlevel = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", jsoneq.getInt("quality"), "lv", eq.getLevel());
                sum += jsonObjectlevel.getInt("exp");
            } else {
                sum += jsoneq.getInt("eatedExp");
            }
            num++;
            if (sum >= need) {
                break;
            }
        }
        if (num < eqlist.size()) {
            return false;
        }
        return true;
    }

    private int getEquipaddExp(List<Equip> eqlist) {
        int sum = 0;
        for (Equip eq : eqlist) {
            JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
            if (jsoneq.getInt("functionType") == EquipDefine.EQUIP_TYPE_EQUOP) {
                JSONObject jsonObjectlevel = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EQUIP_POWER, "quality", jsoneq.getInt("quality"), "lv", eq.getLevel());
                sum += jsonObjectlevel.getInt("exp");
            } else {
                sum += jsoneq.getInt("eatedExp");
            }
        }
        return sum;
    }


    /************
     * 装备穿戴
     *******/
    public int equipOn(long id, int position, int type, int powerindex, M13.M130002.S2C.Builder builder, List<BaseLog> baseLogs) {
        int rs = 0;
        if (type == 1) {
            rs = equipOnone(id, position, powerindex, builder, baseLogs);
        } else {
            rs = equipOnAll(position, builder, baseLogs);
        }
        sendFunctionLog(FunctionIdDefine.DRESS_EQUIP_FUNCTION_ID, type,id,position);
        return rs;
    }

    private int equipOnAll(int position, M13.M130002.S2C.Builder builder, List<BaseLog> baseLogs) {
        List<Equip> list = new ArrayList<Equip>();
     /*   List<Equip> posequip = getEquipListByPosition(position);
        List<Integer> hasPart = new ArrayList<Integer>();*/
        //先把所有脱掉
        for(Equip equip : equips){
            if(equip.getPosition()==position){
                equip.setPosition(0);
                pushEquipToChangeList( equip);
            }
        }
       /* for (Equip equip : posequip) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, equip.getTypeId());
            hasPart.add(jsonObject.getInt("partId"));
        }*/
        for (int i = 1; i <= 6; i++) {
                Equip eq = getBestEquipBypartId(i);
                if (eq != null) {
                    changEquipPostion(eq, position);
                    list.add(eq);
                    EquipLog equipLog = new EquipLog(LogDefine.EQUIPON, eq.getId(), eq.getTypeId(), eq.getPosition(), eq.getLevel(), eq.getLevel());
                    baseLogs.add(equipLog);
                }
        }
        builder.addAllEquipinfos(getEquipInfos());
        return 0;
    }


    //获取背包中最好某个部位最好的装备
    private Equip getBestEquipBypartId(int parId) {
        Equip eq = null;
        List<Equip> posequip = getEquipListByPosition(0);
        for (Equip equip : posequip) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, equip.getTypeId());
            if (jsonObject.getInt("partId") == parId) {
                if (eq == null) {
                    eq = equip;
                } else {
                    JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
                    if (jsonObject.getInt("quality") > jsoneq.getInt("quality")) {
                        eq = equip;
                    } else if (jsonObject.getInt("quality") == jsoneq.getInt("quality")) {
                        if (equip.getLevel() > eq.getLevel()) {
                            eq = equip;
                        }
                    }
                }

            }
        }
        return eq;
    }

    private int equipOnone(long id, int position, int powerindex, M13.M130002.S2C.Builder builder, List<BaseLog> baseLogs) {
        Equip eq = getEquip(id);
        if (eq == null) {
            return ErrorCodeDefine.M130002_1;//装备不存在
        }
        JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
        if (jsoneq.getInt("functionType") != EquipDefine.EQUIP_TYPE_EQUOP) {
            return ErrorCodeDefine.M130002_2;
            // 该装备类型不能穿戴
        }
        if (jsoneq.getInt("upproperty") != powerindex) {
            return ErrorCodeDefine.M130002_3;//该装备不能穿戴在这里
        }
        if (troopsStart(position) == false) {
            return ErrorCodeDefine.M130002_4;//该槽位未开启
        }

        List<Equip> list = getEquipByPositionIndex(position, powerindex);
        for (Equip equ : list) {
            changEquipPostion(equ, 0);
            pushEquipToChangeList(equ);
            builder.addEquipinfos(getEquipInfo(equ.getId()));
        }
        //执行穿戴
        changEquipPostion(eq, position);
        EquipLog equipLog = new EquipLog(LogDefine.EQUIPON, eq.getId(), eq.getTypeId(), eq.getPosition(), eq.getLevel(), eq.getLevel());
        baseLogs.add(equipLog);
        builder.addEquipinfos(getEquipInfo(eq.getId()));
        return 0;
    }


    private List<Equip> getEquipByPositionIndex(int positon, int powerIndex) {
        List<Equip> list = new ArrayList<Equip>();
        for (Equip eq : equips) {
            if (eq.getPosition() == positon) {
                JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
                if (jsoneq.getInt("upproperty") == powerIndex) {
                    list.add(eq);
                }
            }
        }
        return list;
    }


    /*******
     * 卸下装备
     ******/
    public int equipOff(long id, List<BaseLog> baseLogs) {
        Equip eq = getEquip(id);
        if (eq == null) {
            return ErrorCodeDefine.M130003_1;//装备不存在
        }
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(getBagNum()==playerProxy.getPowerValue(PlayerPowerDefine.POWER_equipsize)){
            return ErrorCodeDefine.M130003_2;//装备不存在
        }
        changEquipPostion(eq, 0);
        EquipLog equipLog = new EquipLog(LogDefine.EQUIPOFF, eq.getId(), eq.getTypeId(), eq.getPosition(), eq.getLevel(), eq.getLevel());
        baseLogs.add(equipLog);
        sendFunctionLog(FunctionIdDefine.DEMOUNT_EQUIP_FUNCTION_ID, id, eq.getTypeId(),eq.getPosition());
        return 0;
    }

    /************
     * 装备出售
     **********/
    public int equipSale(List<Long> idlist, M13.M130005.S2C.Builder builder, List<BaseLog> baseLogs) {
        for (long id : idlist) {
            Equip eq = getEquip(id);
            if (eq == null) {
                return ErrorCodeDefine.M130005_1;//装备不存在
            }
            if (eq.getPosition() != 0) {
                return ErrorCodeDefine.M130005_2;//穿在身上不能卖
            }
        }

        StringBuffer sb = new StringBuffer();
        int getMoney = 0;
        for (long id : idlist) {
            Equip eq = getEquip(id);
            JSONObject jsoneq = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, eq.getTypeId());
            sb.append(eq.getTypeId());
            sb.append(",");
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int coin = jsoneq.getInt("sellPrice");
            getMoney += coin;
            playerProxy.addPowerValue(PlayerPowerDefine.POWER_tael, coin, LogDefine.GET_SALE_EQUIP);
            delEquip(id, LogDefine.LOST_EQUIP_SALE);
            builder.addEquipinfos(getEquipInfo(id));
            EquipLog equipLog = new EquipLog(LogDefine.EQUIPLDROP, eq.getId(), eq.getTypeId(), eq.getPosition(), eq.getLevel(), eq.getLevel());
            baseLogs.add(equipLog);
        }
        sendFunctionLog(FunctionIdDefine.EQUIP_Sale_FUNCTION_ID,getMoney,0,0,sb.toString());
        return 0;
    }

    /******
     * 调整槽位位置
     *********/
    public int changetEquipPosition(int positionone, int positiontwo, M13.M130006.S2C.Builder builder, List<BaseLog> baseLogs) {
        if (positionone == 0 || positiontwo == 0) {
            return ErrorCodeDefine.M130006_1;//位置不对不可调换
        }
        if (troopsStart(positionone) == false) {
            return ErrorCodeDefine.M130006_2;//该槽位未开启
        }
        List<Equip> posiOnelist = getEquipListByPosition(positionone);
        List<Equip> posiTwolist = getEquipListByPosition(positiontwo);
        for (Equip equip : posiOnelist) {
            changEquipPostion(equip, positiontwo);
            EquipLog equipLog = new EquipLog(LogDefine.EQUIPCHANGE, equip.getId(), equip.getTypeId(), equip.getPosition(), equip.getLevel(), 0);
            equipLog.setOldposition(positionone);
            baseLogs.add(equipLog);
        }
        for (Equip equip : posiTwolist) {
            changEquipPostion(equip, positionone);
            EquipLog equipLog = new EquipLog(LogDefine.EQUIPCHANGE, equip.getId(), equip.getTypeId(), equip.getPosition(), equip.getLevel(), equip.getLevel());
            equipLog.setOldposition(positiontwo);
            baseLogs.add(equipLog);
        }
        List<Equip> posiOnenewlist = getEquipListByPosition(positionone);
        List<Equip> posiTwonewlist = getEquipListByPosition(positiontwo);
        builder.addAllEquipinfos(getEquipInfos(posiOnenewlist));
        builder.addAllEquipinfos(getEquipInfos(posiTwonewlist));
        return 0;
    }


    /************
     * 背包扩充
     **********/
    public int addEuipBagSize(List<BaseLog> baseLogs) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_equipsize) >= EquipDefine.EQUIP_MAX_SIZE) {
            return ErrorCodeDefine.M130007_1;//已经最大了
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < EquipDefine.EQUIP_ADD_BAG_PRICR) {
            return ErrorCodeDefine.M130007_2;//金币不足
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, EquipDefine.EQUIP_ADD_BAG_PRICR, LogDefine.LOST_ADD_BAGSIZE);
        long size = playerProxy.getPowerValue(PlayerPowerDefine.POWER_equipsize);
        long add = EquipDefine.EQUIP_ADD_BAG_SIZE;
        if (EquipDefine.EQUIP_MAX_SIZE - size < EquipDefine.EQUIP_ADD_BAG_SIZE) {
            add = EquipDefine.EQUIP_MAX_SIZE - size;
        }
        playerProxy.addPowerValue(PlayerPowerDefine.POWER_equipsize, (int) add, LogDefine.GET_ADD_BAGSIZE);
        EquipLog equipLog = new EquipLog();
        equipLog.setAddSize((int) add);
        equipLog.setAddSizeCost(EquipDefine.EQUIP_ADD_BAG_PRICR);
        equipLog.setOpetype(LogDefine.BUYEQUIPBAG);
        baseLogs.add(equipLog);
        return 0;
    }

    //获得装备剩余空位
    public int getEquipBagLesFree(){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int mysize= (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_equipsize);
        return mysize-equipbagSize();
    }

    public int equipbagSize(){
        int num=0;
        for(Equip eq:equips){
            if(eq.getPosition()==0){
                num++;
            }
        }
        return num;
    }

    public void addBatterPower(HashMap<Integer, Integer> map, int position) {
        if (position > 10) {
            position -= 10;
        }
        for (int i = 1; i < SoldierDefine.TOTAL_FIGHT_POWER; i++) {
            int value=0;
            if(map.get(i)!=null){
                value = map.get(i);
            }
            map.put(i, value + getPositionPower(position, i));
        }
    }

    //槽位是否开启
    public boolean troopsStart(int id) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.TROOP_START, "troopsID", id);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) >= jsonObject.getInt("captainLv")) {
            return true;
        }
        return false;
    }

    public List<Long> delAllEquip() {
        List<Long> list = new ArrayList<Long>();
        for (Equip eq : equips) {
            list.add(eq.getId());
        }
        for (Long id : list) {
            delEquip(id, LogDefine.LOST_BUY_CHEAT);
        }
        return list;
    }

    //获得仓库的装备数
    public int getEquipOnBagnum() {
        int num = 0;
        for (Equip eq : equips) {
            if (eq.getPosition() == 0) {
                num++;
            }
        }
        return num;
    }

    /**
     * 根据类型加入到排行榜
     */
    public void addToRankList(long id) {
        Equip equip = getEquip(id);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (equip != null) {
            int typeId = equip.getTypeId();
            JSONObject equipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.EQUIP_PRO, "ID", typeId);
            if (equipInfo != null) {
                int type = equipInfo.getInt("upproperty");
                if (type > 0) {
                    if (type == PlayerPowerDefine.NOR_POWER_atkRate) {
                        //发送到攻击强化排行榜
                        GameMsg.AddPlayerToRank atk = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(),
                                getMaxPower(PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN), PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN);
                        sendRankServiceMsg(atk);
                    } else if (type == PlayerPowerDefine.NOR_POWER_critRate) {
                        //发送到暴击强化排行榜
                        GameMsg.AddPlayerToRank crit = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(),
                                getMaxPower(PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN), PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN);
                        sendRankServiceMsg(crit);
                    } else if (type == PlayerPowerDefine.NOR_POWER_dodgeRate) {
                        //发送到闪避强化排行榜
                        GameMsg.AddPlayerToRank dodge = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(),
                                getMaxPower(PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN), PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN);
                        sendRankServiceMsg(dodge);
                    }
                }
            }

        }else if(id == 0 && equip == null){
            //发送到攻击强化排行榜
            GameMsg.AddPlayerToRank atk = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(),
                    getMaxPower(PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN), PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN);
            sendRankServiceMsg(atk);
            //发送到暴击强化排行榜
            GameMsg.AddPlayerToRank crit = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(),
                    getMaxPower(PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN), PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN);
            sendRankServiceMsg(crit);
            //发送到闪避强化排行榜
            GameMsg.AddPlayerToRank dodge = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(),
                    getMaxPower(PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN), PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN);
            sendRankServiceMsg(dodge);
        }
    }
    //穿戴某种品质装备的数量
    public void initPutOnEquipNumByQuilty(int quilty){
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int num=0;
        for(Equip eq:equips){
            if(eq.getPosition()>0){
                JSONObject jsonObject= ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO,eq.getTypeId());
                if(jsonObject.getInt("quality")==quilty){
                 num++;
                }
            }
        }
        if(quilty==4){
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_PUTON_PURPLE_EQUIP_NUM,num,playerProxy,0);
        }
    }

    //紫色装备某个等级的数量
    public void initPurpleEquipLeveNum(){
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        Map<Integer,Integer> map=new HashMap<Integer,Integer>();
        for(Equip eq:equips){
                JSONObject jsonObject= ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO,eq.getTypeId());
                if(jsonObject.getInt("quality")==4){
                    if(map.get(eq.getLevel())==null){
                        map.put(eq.getLevel(),1);
                    }else{
                        map.put(eq.getLevel(),map.get(eq.getLevel())+1);
                    }
                }
        }
        List<Integer> list = new ArrayList<>();
        List<JSONObject> objects = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT, "conditiontype", ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_PURPLE_EQUIP_NUM);
        for(JSONObject obj:objects){
            list.add(obj.getInt("condition2"));
        }
        for(int levle:list){
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_PURPLE_EQUIP_NUM,getUpLevelNum(map,levle),playerProxy,levle);
        }
    }

    private int getUpLevelNum(Map<Integer,Integer> map,int level){
        int num = 0;
        for(int key:map.keySet()){
            if (key >= level){
                num+=map.get(key);
            }
        }
        return num;
    }

    /**
     * tbllog_items 装备opt:1增加，0使用
     */
    public void tbllog_equip(int opt, int typeid, int logType,long id) {
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_equip itemslog = new tbllog_equip();
        itemslog.setPlatform(cache.getPlat_name());
        itemslog.setRole_id(player.getPlayerId());
        itemslog.setAccount_name(player.getAccountName());
        itemslog.setDim_level(player.getLevel());
        itemslog.setOpt(opt);
        itemslog.setAction_id(logType);
        itemslog.setType_id(typeid);
        itemslog.setEquip_id(id);
        itemslog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(itemslog);
    }

}