package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.core.SoldierTeam;
import com.znl.define.*;
import com.znl.log.CustomerLogger;
import com.znl.log.SoldierGet;
import com.znl.log.SoldierLost;
import com.znl.log.admin.tbllog_ordnancepice;
import com.znl.log.admin.tbllog_soldier;
import com.znl.pojo.db.Player;
import com.znl.pojo.db.Soldier;
import com.znl.proto.Common;
import com.znl.proto.M4;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class SoldierProxy extends BasicProxy {
    private Set<Soldier> soldiers = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for(Soldier soldier : soldiers){
            soldier.finalize();
        }
    }

    @Override
    protected void init() {
        super.expandPowerMap.put(PlayerPowerDefine.NOR_POWER_highestCapacity,highestCapacity);
    }

    public SoldierProxy(Set<Long> soldierIds,String areaKey){
        this.areaKey = areaKey;
        for(Long id : soldierIds){
            Soldier soldier = BaseDbPojo.get(id,Soldier.class,areaKey);
            if(soldier!=null) {
                soldiers.add(soldier);
            }
        }
    }


    /**获得出战佣兵的powerMap*/
    public HashMap<Integer,Integer> getSoldierPowerMap(int soldierId,int soldierNum){
        HashMap<Integer,Integer> powerMap = new HashMap<>();
        Soldier soldier = getSoldierBySoldierId(soldierId);
        if(soldier == null){
            return powerMap;
        }
//        if(soldier.getNum() < soldierNum){
//            CustomerLogger.error("！！！！生成战斗单位的时候出战佣兵居然大于总佣兵");
//            soldierNum = soldier.getNum();
//        }
        for (int i=SoldierDefine.POWER_hpMax;i<=SoldierDefine.TOTAL_FIGHT_POWER;i++){
            int value = getPowerValue(i,soldier);
//            if (getGameProxy() != null){
//                //加上玩家身上的属性
//                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
//                int _value = (int) playerProxy.getPowerValue(i);
//                if (_value > 0){
//                    value += _value;
//                }
//            }
            if(i == SoldierDefine.POWER_atk){
//                value *= soldierNum;
            }else if(i == SoldierDefine.POWER_hp || i == SoldierDefine.POWER_hpMax){
//                value *= soldierNum;
            }
            powerMap.put(i,value);
        }
        powerMap.put(SoldierDefine.NOR_POWER_NUM,soldierNum);
        return powerMap;
    }

    public void saveSoldier(){
        List<Soldier> soldiers = new ArrayList<Soldier>();
        synchronized (changeSoldiers) {
            while (true) {
                Soldier soldier = changeSoldiers.poll();
                if (soldier == null) {
                    break;
                }
                soldiers.add(soldier);
            }
        }
        for (Soldier soldier : soldiers) {
            soldier.save();
        }

    }

    private LinkedList<Soldier> changeSoldiers = new LinkedList<Soldier>();
    private void pushSoldierToChangeList(Soldier soldier){
        //插入更新队列
        synchronized (changeSoldiers) {
            if(!changeSoldiers.contains(soldier)){
                changeSoldiers.offer(soldier);
            }
        }
//        if (getGameProxy() != null){
//            initHighestCapacity();
//        }
    }

    private Soldier getSoldierBySoldierId(int soldierId){
        for (Soldier soldier : soldiers){
            if(soldier.getTypeId() == soldierId){
                return soldier;
            }
        }
        return null;
    }

    private long highestCapacity = 0;
    public long getHighestCapacity(){
        return highestCapacity;
    }
    /**初始化最高战力**/
    public long initHighestCapacity(){
        long cap = 0l;
        DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Integer> fightPost = new ArrayList<>(playerProxy.getPlayerFightPost());
        int size = fightPost.size();
        int num = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_command);
        List<SoldierTeam> allteams = new ArrayList<>();
        for(Soldier soldier : soldiers){
            int totalNum = soldier.getBaseNum();
            if (totalNum == 0){
                continue;
            }
            for(int i=0;i<totalNum/num;i++){
                SoldierTeam team = new SoldierTeam();
                team.setNum(num);
                team.setTypeId(soldier.getTypeId());
                team.setCapacity(dungeoProxy.getTeamCapacity(soldier.getTypeId(), num,-1));//传入-1不会拿到装备的数值
                team.setLoad(getPowerValue(SoldierDefine.POWER_load,soldier) *num);
                allteams.add(team);
            }
            if(totalNum % num > 0){
                SoldierTeam team = new SoldierTeam();
                team.setNum(totalNum % num);
                team.setTypeId(soldier.getTypeId());
                team.setCapacity(dungeoProxy.getTeamCapacity(soldier.getTypeId(),totalNum% num,-1));
                team.setLoad(getPowerValue(SoldierDefine.POWER_load,soldier)*num);
                allteams.add(team);
            }
        }
//        long t1 = System.currentTimeMillis();
        SortUtil.anyProperSort(allteams,"getCapacity",false);
        //获得最高战力的6个队伍
        List<SoldierTeam> highestCapTeam = new ArrayList<>(size);
        for (int i=0;i<size && i<allteams.size();i++){
//            cap += allteams.get(i).getCapacity();
            highestCapTeam.add(allteams.get(i));
        }
//        long t2 = System.currentTimeMillis();
//        System.err.println("第一阶段时间为："+(t2-t1));
        //将最高战力的6个队伍放到槽位中再算一次
        for (SoldierTeam team : highestCapTeam){
            int teamCapacity = 0;
            int capIndex = 0;
            for (int i=0;i< fightPost.size();i++){
                int post = fightPost.get(i);
                int capacity = dungeoProxy.getTeamCapacity(team.getTypeId(),team.getNum(),post);
                if(capacity > teamCapacity){
                    capIndex = i;
                    teamCapacity = capacity;
                }
            }
            fightPost.remove(capIndex);
            cap += teamCapacity;
        }
//        long t3 = System.currentTimeMillis();
//        System.err.println("第二阶段时间为："+(t3-t2));
        highestCapacity = cap;
        refurceExpandPowerMap();
        playerProxy.setCapacity(highestCapacity);
        return highestCapacity;
    }


    public List<Common.SoldierInfo> checkBaseNumAndNum(){
        List<Common.SoldierInfo> soldierInfos = new ArrayList<>();
        for (Soldier soldier : soldiers){
            if (soldier.getBaseNum() > soldier.getNum()){
                CustomerLogger.error("还有部队没回来呢,执行补回逻辑");
                addSoldierNumWithoutBaseNum(soldier.getTypeId(), soldier.getBaseNum() - soldier.getNum(), LogDefine.GET_CLOSE_WORLD);
                pushSoldierToChangeList(soldier);
                soldierInfos.add(getSoldierInfo(soldier.getTypeId()));
            }else if(soldier.getBaseNum() < soldier.getNum()){
                soldier.setNum(soldier.getBaseNum());
                pushSoldierToChangeList(soldier);
                soldierInfos.add(getSoldierInfo(soldier.getTypeId()));
            }
        }

        return soldierInfos;
    }

    public List<Common.SoldierInfo> refurceSoldierPowerValue(List<Integer> powers){
        List<Common.SoldierInfo> infos = new ArrayList<>();
        synchronized (soldiers){
            List<Integer> ids = new ArrayList<>();
            //筛选出要刷新的佣兵列表
            for (Integer power:powers){
                switch (power){
                    case SoldierDefine.POWER_infantryHpMax:
                    case SoldierDefine.POWER_infantryAtk:
                        getSoldierIdBySoldierType(SoldierDefine.SOLDIER_TYPE_INFANTRY,ids);
                        break;
                    case SoldierDefine.POWER_cavalryHpMax:
                    case SoldierDefine.POWER_cavalryAtk:
                        getSoldierIdBySoldierType(SoldierDefine.SOLDIER_TYPE_CAVALRY,ids);
                        break;
                    case SoldierDefine.POWER_pikemanHpMax:
                    case SoldierDefine.POWER_pikemanAtk:
                        getSoldierIdBySoldierType(SoldierDefine.SOLDIER_TYPE_PIKEMAN,ids);
                        break;
                    case SoldierDefine.POWER_archerHpMax:
                    case SoldierDefine.POWER_archerHpatk:
                        getSoldierIdBySoldierType(SoldierDefine.SOLDIER_TYPE_ARCHER,ids);
                        break;
                    default:
                        getSoldierIdBySoldierType(0,ids);
                }
            }
            for(Integer id : ids){
                initPowerValue(id);
                Common.SoldierInfo info = getSoldierInfo(id);
                infos.add(info);
            }
        }
        return infos;
    }

    public void getSoldierIdBySoldierType(int type,List<Integer> ids){
        for(Soldier soldier :this.soldiers){
            if(type == 0 && ids.contains(soldier.getTypeId()) == false){
                    ids.add(soldier.getTypeId());
            }else {
                JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS,soldier.getTypeId());
                if(soldierDefine.getInt("type") == type && ids.contains(soldier.getTypeId()) == false){
                    ids.add(soldier.getTypeId());
                }
            }
        }
    }

    public List<M4.FixSoldierInfo> getAllLostSoldierInfos(){
        List<M4.FixSoldierInfo> infos = new ArrayList<>();
        for (Soldier soldier : soldiers){
            if(soldier.getLostNum() > 0){
                M4.FixSoldierInfo.Builder info = M4.FixSoldierInfo.newBuilder();
                info.setNum(soldier.getLostNum());
                info.setTypeid(soldier.getTypeId());
                JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS,soldier.getTypeId());
                info.setRepairMoney((int) Math.ceil(soldierDefine.getInt("repairMoney") /10.0 * soldier.getLostNum()));
                info.setRepairCrys(soldierDefine.getInt("repairCrys") * soldier.getLostNum());
                infos.add(info.build());
            }
        }
        return infos;
    }


    public List<Common.SoldierInfo> getSoldierInfosInlost(){
        List<Common.SoldierInfo> infos = new ArrayList<>();
        for (Soldier soldier : soldiers){
            if(soldier.getLostNum() > 0){
                Common.SoldierInfo.Builder info = Common.SoldierInfo.newBuilder();
                info.setNum(soldier.getNum());
                info.setTypeid(soldier.getTypeId());
                /*HashMap<Integer,Integer> map = getEmptySoldierPowerMap(soldier.getTypeId());
                info.setHp(map.get(SoldierDefine.POWER_hpMax));
                info.setAttack(map.get(SoldierDefine.POWER_atk));
                for (int i=SoldierDefine.POWER_hpMax;i<=SoldierDefine.TOTAL_FIGHT_POWER;i++){
                    info.addPowerList(map.get(i));
                }*/
                infos.add(info.build());
            }
        }
        return infos;
    }

    public List<Common.SoldierInfo> getSoldierInfos(){
        List<Common.SoldierInfo> infos = new ArrayList<Common.SoldierInfo>();
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ARM_KINDS);
        for (JSONObject define : list){
            infos.add(this.getSoldierInfo(define.getInt("ID")));
        }
//        for(Soldier soldier :this.soldiers){
//            infos.add(this.getSoldierInfo(soldier));
//        }
        return infos;
    }

    public Common.SoldierInfo getSoldierInfo(int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        Common.SoldierInfo.Builder info = Common.SoldierInfo.newBuilder();
        if(soldier == null){
            info.setNum(0);
            info.setTypeid(soldierId);
            //HashMap<Integer,Integer> map = getEmptySoldierPowerMap(soldierId);
            //info.setHp(map.get(SoldierDefine.POWER_hpMax));
            //info.setAttack(map.get(SoldierDefine.POWER_atk));
            /*for (int i=SoldierDefine.POWER_hpMax;i<=SoldierDefine.TOTAL_FIGHT_POWER;i++){
                info.addPowerList(map.get(i));
            }*/
            return info.build();
        }
        Common.SoldierInfo.Builder builder = Common.SoldierInfo.newBuilder();
        builder.setNum(soldier.getNum());
        builder.setTypeid(soldier.getTypeId());
        return builder.build();
    }

    //分享佣兵是调用
    public Common.SoldierInfo getSoldierInfotoshare(int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        Common.SoldierInfo.Builder info = Common.SoldierInfo.newBuilder();
        if(soldier == null){
            info.setNum(0);
            info.setTypeid(soldierId);
            HashMap<Integer,Integer> map = getEmptySoldierPowerMap(soldierId);
            //info.setHp(map.get(SoldierDefine.POWER_hpMax));
            //info.setAttack(map.get(SoldierDefine.POWER_atk));
            for (int i=SoldierDefine.POWER_hpMax;i<=SoldierDefine.TOTAL_FIGHT_POWER;i++){
                info.addPowerList(map.get(i));
            }
            return info.build();
        }
        return getSoldierInfo(soldier);
    }

    private Common.SoldierInfo getSoldierInfo(Soldier soldier){
        Common.SoldierInfo.Builder builder = Common.SoldierInfo.newBuilder();
        builder.setNum(soldier.getNum());
        builder.setTypeid(soldier.getTypeId());
        for (int i=SoldierDefine.POWER_hpMax;i<= SoldierDefine.TOTAL_FIGHT_POWER;i++){
            if(SoldierDefine.POWER_hp == i || SoldierDefine.POWER_hpMax == i){
                int hp = getPowerValue(SoldierDefine.POWER_hp,soldier);

                builder.addPowerList(hp);
            }else if(SoldierDefine.POWER_atk == i){
                int atk = getPowerValue(SoldierDefine.POWER_atk,soldier);
//                int atkPer = getPowerValue(SoldierDefine.POWER_atkRate,soldier);
//                atk = (int) (atk * (1+atkPer/10000.0));
                builder.addPowerList(atk);
            }else if(SoldierDefine.POWER_load == i){
                int load = getPowerValue(SoldierDefine.POWER_load,soldier);
                int loadPer = getPowerValue(SoldierDefine.POWER_loadRate,soldier);
                load = (int) (load * (1+loadPer/10000.0));
                builder.addPowerList(load);
            }else {
                builder.addPowerList(getPowerValue(i,soldier));
            }

        }
        /*int hp = getPowerValue(SoldierDefine.POWER_hp,soldier);
        int hpPercent = getPowerValue(SoldierDefine.POWER_hpMaxRate,soldier);
        hp = (int) (hp *(1+hpPercent /10000.0));
        builder.setHp(hp);
        int atk = getPowerValue(SoldierDefine.POWER_atk,soldier);
        int atkPer = getPowerValue(SoldierDefine.POWER_atkRate,soldier);
        atk = (int) (atk * (1+atkPer/10000.0));
        builder.setAttack(atk);*/
        return builder.build();
    }

    public void addOffLinePlayerSoldierNum(int typeId,int num,int logtype){
        if(num <= 0){
            return;
        }
        Soldier soldier = getSoldierBySoldierId(typeId);
        if (soldier != null){
            soldier.setNum(soldier.getNum() + num);
        }
        soldier.save();
        SoldierGet soldierGet=new SoldierGet(logtype,typeId,num);
        sendPorxyLog(soldierGet);
    }

    public void reduceSoldierBaseNum(int typeId,int num){
        Soldier soldier = getSoldierBySoldierId(typeId);
        soldier.setBaseNum(soldier.getBaseNum() - num);
        pushSoldierToChangeList(soldier);
    }


    public void reduceSoldierofferlineBaseNum(int typeId,int num){
        Soldier soldier = getSoldierBySoldierId(typeId);
        soldier.setBaseNum(soldier.getBaseNum() - num);
        soldier.save();
    }

    public Long addSoldierNumWithoutBaseNum(int typeId,int num,int logtype){
        Soldier soldier = getSoldierBySoldierId(typeId);
        soldier.setNum(soldier.getNum() + num);
        pushSoldierToChangeList(soldier);
        soldeierLog(1,typeId,num,logtype);
        return soldier.getId();
    }

    public void reduceSoldierNumWithoutBaseNum(int typeId,int num,int logtype){
        Soldier soldier = getSoldierBySoldierId(typeId);
        soldier.setNum(soldier.getNum()-num);
        pushSoldierToChangeList(soldier);
        soldeierLog(0,typeId,num,logtype);
    }

    public long addSoldierNum(int typeId,int num,int logtype){
        Soldier soldier = getSoldierBySoldierId(typeId);
        SoldierGet soldierGet=new SoldierGet(logtype,typeId,num);
        sendPorxyLog(soldierGet);
        long rs = 0;
        if(soldier == null){
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            creatSoldier(typeId,num,playerProxy.getPlayerId());
        } else {
            soldier.setNum(soldier.getNum()+num);
            soldier.setBaseNum(soldier.getBaseNum() + num);
            pushSoldierToChangeList(soldier);
            rs = soldier.getId();
        }
        soldeierLog(1,typeId,num,logtype);
        return rs;
    }

    private long addSoldierLostNum(Soldier soldier,int num){
        int lostNum = soldier.getLostNum() + num;
        soldier.setLostNum(lostNum);
        pushSoldierToChangeList(soldier);
        return soldier.getId();
    }

    public long reduceSoldierNum(int typeId,int num,int lostNum,int logtype){
        if(num < 0){
            num = -num;
        }
        Soldier soldier = getSoldierBySoldierId(typeId);
        if(soldier == null){
            CustomerLogger.error("！！！！！删除玩家的佣兵的是一个他没有的！！");
            return 0;
        }

        if(num == 0 && lostNum == 0){
            return soldier.getId();
        }
        int _num = soldier.getNum();
        if(num >= _num){
            num = _num;
        }
        soldier.setNum(_num - num);
        soldier.setBaseNum(soldier.getBaseNum() - num);
        addSoldierLostNum(soldier, lostNum);
//        if(soldier.getNum() == 0 && soldier.getLostNum() == 0){
//            soldier.del();
//            removeSoldier(soldier);
//        }
        pushSoldierToChangeList(soldier);
        SoldierLost soldierLost=new SoldierLost(logtype,typeId,num,lostNum);
        soldeierLog(0,typeId,num,logtype);
        return soldier.getId();
    }


    public long reduceofflineSoldierNum(int typeId,int num,int lostNum,int logtype){
        if(num < 0){
            num = -num;
        }
        Soldier soldier = getSoldierBySoldierId(typeId);
        if(soldier == null){
            CustomerLogger.error("！！！！！删除玩家的佣兵的是一个他没有的！！");
            return 0;
        }

        if(num == 0 && lostNum == 0){
            return soldier.getId();
        }
        int _num = soldier.getNum();
        if(num >= _num){
            num = _num;
        }
        soldier.setNum(_num - num);
        soldier.setBaseNum(soldier.getBaseNum() - num);
        addSoldierLostNum(soldier, lostNum);
        pushSoldierToChangeList(soldier);
        soldier.save();
        soldeierLog(0,typeId,num,logtype);
        return soldier.getId();
    }

    private void removeSoldier(Soldier soldier){
        soldiers.remove(soldier);
        soldier.del();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.removeSoldierToPlayer(soldier.getId());
        if(changeSoldiers.contains(soldier)){
            changeSoldiers.remove(soldier);
        }
    }

    public long creatSoldier(int typeId,int num,long playerId){
        Soldier soldier = BaseDbPojo.create(Soldier.class,areaKey);
        soldier.setNum(num);
        soldier.setBaseNum(num);
        soldier.setTypeId(typeId);
        soldier.setPlayerId(playerId);
        soldiers.add(soldier);
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.addSoldierToPlayer(soldier.getId());
        initPowerValue(typeId);
        soldier.save();
        return soldier.getId();
    }

    public long getSoldierId(int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        if(soldier == null){
            return 0;
        }
        return soldier.getId();
    }

    public int getSoldierNum(int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        if(soldier == null){
            return 0;
        }
        return soldier.getNum();
    }


    public int getAllSoldierNum(){
        int num=0;
        for (Soldier soldier : soldiers){
           num+=soldier.getNum();
        }
        return num;
    }

    public int getSolierLostTypeNum(){
        int num=0;
        for(Soldier soldier:soldiers){
            if(soldier.getLostNum()>0){
                num++;
            }
        }
        return num;
    }

    public boolean isCotentSSoldier(int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        if(soldier == null){
            return false;
        }
        return true;
    }
    public Integer getSoldierPowerValue(String powerName,Soldier soldier){
        return (Integer)soldier.getter(powerName);
    }

    public int getPowerValue(int power,int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        return getPowerValue(power,soldier);
    }

    private int getPowerValue(int power,Soldier soldier){
        String powerName = SoldierDefine.NameMap.get(power);
        if(powerName ==null){
            CustomerLogger.info("soldierget出现未知的power值了！！！"+power);
            return 0;
        }
        return getSoldierPowerValue(powerName, soldier);
    }

    public void setPowerValue(int power,Integer value,Soldier soldier){
        String powerName = SoldierDefine.NameMap.get(power);
        if(powerName ==null){
            CustomerLogger.info("soldierset出现未知的power值了！！！"+power);
            return;
        }
        soldier.setter(powerName,value.toString());
    }


    private HashMap<Integer,Integer> getEmptySoldierPowerMap(int soldierId){
        HashMap<Integer,Integer> map = new HashMap<>();
        JSONObject soldierDefine =  ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS,soldierId);
        if (soldierDefine != null) {
            int hp = soldierDefine.getInt("hpmax");
            int atk = soldierDefine.getInt("atk");
            int hitRate = soldierDefine.getInt("hitRate");
            int dodgeRate = soldierDefine.getInt("dodgeRate");
            int critRate = soldierDefine.getInt("critRate");
            int defRate = soldierDefine.getInt("defRate");
            int wreck = soldierDefine.getInt("wreck");
            int defend = soldierDefine.getInt("defend");
            int load = soldierDefine.getInt("load");
            map.put(SoldierDefine.POWER_hpMax, hp);
            map.put(SoldierDefine.POWER_atk, atk);
            map.put(SoldierDefine.POWER_hitRate, hitRate);
            map.put(SoldierDefine.POWER_dodgeRate, dodgeRate);
            map.put(SoldierDefine.POWER_critRate, critRate);
            map.put(SoldierDefine.POWER_defRate, defRate);
            map.put(SoldierDefine.POWER_wreck, wreck);
            map.put(SoldierDefine.POWER_defend, defend);
            map.put(SoldierDefine.POWER_load, load);
        }

        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for(int i= 1 ;i<=SoldierDefine.TOTAL_FIGHT_POWER;i++){
            int add = (int) playerProxy.getPowerValue(i);
            if (map.containsKey(i)){
                int value = map.get(i) +add;
                map.put(i,value);
            }else {
                map.put(i,add);
            }
        }

        return map;
    }

    /***初始化所有佣兵的战斗数据，初始化调用***/
    public void loginInitPowerValue(){
        synchronized (soldiers){
            for (Soldier soldier : soldiers){
                initPowerValue(soldier.getTypeId());
            }
        }
    }

    public void initPowerValue(int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        //先清空属性，重新计算
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for(int i= 1 ;i<=SoldierDefine.TOTAL_FIGHT_POWER;i++){
            //先将玩家加成的属性值算出来
            setPowerValue(i, (int) playerProxy.getPowerValue(i),soldier);
        }
        JSONObject soldierDefine =  ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldier.getTypeId());
        int hp = soldierDefine.getInt("hpmax");
        int atk = soldierDefine.getInt("atk");
        int hitRate = soldierDefine.getInt("hitRate");
        int dodgeRate = soldierDefine.getInt("dodgeRate");
        int critRate = soldierDefine.getInt("critRate");
        int defRate = soldierDefine.getInt("defRate");
        int wreck = soldierDefine.getInt("wreck");
        int defend = soldierDefine.getInt("defend");
        int load = soldierDefine.getInt("load");
        addPowerValue(SoldierDefine.POWER_hpMax, hp, soldierId);
        addPowerValue(SoldierDefine.POWER_hp, hp, soldierId);
        addPowerValue(SoldierDefine.POWER_atk, atk, soldierId);
        addPowerValue(SoldierDefine.POWER_hitRate, hitRate, soldierId);
        addPowerValue(SoldierDefine.POWER_dodgeRate, dodgeRate, soldierId);
        addPowerValue(SoldierDefine.POWER_critRate, critRate, soldierId);
        addPowerValue(SoldierDefine.POWER_defRate, defRate, soldierId);
        addPowerValue(SoldierDefine.POWER_wreck, wreck, soldierId);
        addPowerValue(SoldierDefine.POWER_defend, defend, soldierId);
        addPowerValue(SoldierDefine.POWER_load, load, soldierId);
        addPowerValue(SoldierDefine.POWER_hpMaxRate, getHpAddPercent(soldier), soldierId);
        addPowerValue(SoldierDefine.POWER_atkRate, getAtkAddPercent(soldier), soldierId);
        OrdnanceProxy ordnanceProxy=getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        ordnanceProxy.getPower(soldierDefine.getInt("type"),soldierId);
        pushSoldierToChangeList(soldier);
//        int loadRate = (int) playerProxy.getPowerValue(SoldierDefine.POWER_loadRate);
//        setPowerValue(SoldierDefine.POWER_loadRate, loadRate, soldier);
    }

    private int getHpAddPercent(Soldier soldier){
        int adder = getPowerValue(SoldierDefine.POWER_hpMaxRate,soldier);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        adder += playerProxy.getPowerValue(SoldierDefine.POWER_hpMaxRate);
        JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldier.getTypeId());
        int type = soldierDefine.getInt("type");
        int addPower = 0;
        switch (type){
            case SoldierDefine.SOLDIER_TYPE_CAVALRY:
                addPower = SoldierDefine.POWER_cavalryHpMax;
                break;
            case SoldierDefine.SOLDIER_TYPE_INFANTRY:
                addPower = SoldierDefine.POWER_infantryHpMax;
                break;
            case SoldierDefine.SOLDIER_TYPE_ARCHER:
                addPower = SoldierDefine.POWER_archerHpMax;
                break;
            case SoldierDefine.SOLDIER_TYPE_PIKEMAN:
                addPower = SoldierDefine.POWER_pikemanHpMax;
                break;
        }
        adder+= getPowerValue(addPower,soldier);
//        adder+= playerProxy.getPowerValue(addPower);

        return adder;
    }

    private int getAtkAddPercent(Soldier soldier){
        int adder = getPowerValue(SoldierDefine.POWER_atkRate,soldier);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        adder += playerProxy.getPowerValue(SoldierDefine.POWER_atkRate);
        JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldier.getTypeId());
        int type = soldierDefine.getInt("type");
        int addPower = 0;
        switch (type){
            case SoldierDefine.SOLDIER_TYPE_CAVALRY:
                addPower = SoldierDefine.POWER_cavalryAtk;
                break;
            case SoldierDefine.SOLDIER_TYPE_INFANTRY:
                addPower = SoldierDefine.POWER_infantryAtk;
                break;
            case SoldierDefine.SOLDIER_TYPE_ARCHER:
                addPower = SoldierDefine.POWER_archerHpatk;
                break;
            case SoldierDefine.SOLDIER_TYPE_PIKEMAN:
                addPower = SoldierDefine.POWER_pikemanAtk;
                break;
        }
        adder+= getPowerValue(addPower,soldier);
//        adder+= playerProxy.getPowerValue(addPower);
        return adder;
    }

    public void addPowerValue(int power ,int add,int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        if(add < 0){
            System.out.println("增加佣兵属性的时候出现负数了！！！");
            add = 0;
        }
        int value = getPowerValue(power,soldier);
        setPowerValue(power,value+add,soldier);
    }

    public void reducePowerValue(int power ,int reduce,int soldierId){
        Soldier soldier = getSoldierBySoldierId(soldierId);
        if(reduce < 0){
            System.out.println("减少佣兵属性的时候出现负数了！！！");
            reduce -= reduce;
        }
        int value = getPowerValue(power,soldier);
        setPowerValue(power,value-reduce,soldier);
    }

    public void getfixLostNum(Map<Integer,Integer> map){
        for (Soldier soldier : soldiers){
            map.put(soldier.getTypeId(),soldier.getLostNum());
        }
    }

    /**修复佣兵**/
    public int fixLostSoldier(int typeId, int type,List<Common.SoldierInfo> soldierInfos) {
        int price = 0;
        StringBuffer sb = new StringBuffer();
        if(typeId == 0){
            for (Soldier soldier : soldiers){
                JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS,soldier.getTypeId());
                if(type == 1){
                    price += (int)Math.ceil(soldierDefine.getInt("repairMoney") / 10.0 * soldier.getLostNum());
                }else {
                    price += soldierDefine.getInt("repairCrys") * soldier.getLostNum();
                }
            }
        }else {
            Soldier soldier = getSoldierBySoldierId(typeId);
            if(soldier == null){
                return ErrorCodeDefine.M20007_1;
            }
            JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS,typeId);
            if(type == 1){
                price += (int)Math.ceil(soldierDefine.getInt("repairMoney") / 10.0 * soldier.getLostNum());
            }else {
                price += soldierDefine.getInt("repairCrys") * soldier.getLostNum();
            }
        }
        long value = 0;
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (type == 1){
            value = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
            if(value < price){
                return ErrorCodeDefine.M20007_2;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold,price,LogDefine.LOST_REPAIR_SOLDIER);
        }else {
            value = playerProxy.getPowerValue(PlayerPowerDefine.POWER_tael);
            if(value < price){
                return ErrorCodeDefine.M20007_3;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_tael,price,LogDefine.LOST_REPAIR_SOLDIER);
        }
        if(typeId == 0){
            for (Soldier soldier : soldiers){
                int num = soldier.getNum();
                soldierInfos.add(doFixSoldier(soldier.getTypeId()));
                int _num = soldier.getNum();
                if(_num > num){
                    sb.append(soldier.getTypeId()+"&"+(_num-num)+",");
                }
            }
        }else {
            int num = getSoldierNum(typeId);
            soldierInfos.add(doFixSoldier(typeId));
            int _num = getSoldierNum(typeId);
            if(_num > num){
                sb.append(typeId+"&"+(_num-num)+",");
            }
        }
        sendFunctionLog(FunctionIdDefine.FIX_LOST_SOLDIER_FUNCTION_ID, type,price,0,sb.toString());
        return price;
    }

    private Common.SoldierInfo doFixSoldier(int typeId){
        Soldier soldier = getSoldierBySoldierId(typeId);
        int lostNum = soldier.getLostNum();
        addSoldierNum(typeId, lostNum,LogDefine.GET_REPAIRE_SOLDIER);
        soldier.setLostNum(0);
        pushSoldierToChangeList(soldier);
        return getSoldierInfo(typeId);
    }



    public List<Integer> getSodierByType(int type){
        List<Integer> list=new ArrayList<Integer>();
        for(Soldier sd:soldiers){
            JSONObject soldierDefine =  ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, sd.getTypeId());
            if(soldierDefine.getInt("type")==type){
              list.add(sd.getTypeId());
            }
        }
        return  list;
    }

    /**
     * SoldierLog:1增加，0使用
     */
    public void soldeierLog(int opt, int itemId, int num,int logType) {
        if(getGameProxy()==null){
            return;
        }
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_soldier itemslog = new tbllog_soldier();
        itemslog.setPlatform(cache.getPlat_name());
        itemslog.setRole_id(player.getPlayerId());
        itemslog.setAccount_name(player.getAccountName());
        itemslog.setDim_level(player.getLevel());
        itemslog.setOpt(opt);
        itemslog.setAction_id(logType);
        itemslog.setType_id(itemId);
        itemslog.setSoldier_number((long) num);
        itemslog.setMap_id(0);
        itemslog.setHappend_time(GameUtils.getServerTime());
        itemslog.setRemain_num((long) getSoldierNum(itemId));
        sendPorxyLog(itemslog);
    }

}
