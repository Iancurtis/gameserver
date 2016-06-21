package com.znl.proxy;

import com.znl.base.BasicProxy;
import com.znl.core.PlayerBattle;
import com.znl.core.PlayerTeam;
import com.znl.define.*;
import com.znl.proto.Common;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/11/11.
 */
public class BattleProxy  extends BasicProxy {

    public BattleProxy(String areaKey){
        this.areaKey = areaKey;
    }

    private int battleId = 0;//最新的战斗id标志位
    public int nowBattleId = 0;//当前的战斗id
    public ConcurrentHashMap<Integer,PlayerBattle> battles = new ConcurrentHashMap<Integer, PlayerBattle>();//<battleid，battle>

    public int addBattle(PlayerBattle battle){
        battleId++;
        while(true){
            if(battles.containsKey(battleId) == false){
                battle.id = battleId;//给当前这场战斗赋予一个唯一id
                battles.put(battleId, battle);
                break;
            }else{
                battleId++;
            }
        }

        return battleId;
    }


    private void addBuff(List<PlayerTeam> list, JSONObject auraDefine){
        JSONArray effs = auraDefine.getJSONArray("effect");
        for (int i=0;i<effs.length();i++){
            int auraType = auraDefine.getInt("armytype");
            for (PlayerTeam team : list){
                int type = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE);
                if(auraType==0 || type == auraType){
                    List<Integer> buffers = (List<Integer>) team.getValue(SoldierDefine.NOR_POWER_BUFF);
                    if(buffers.contains(effs.getInt(i)) == false){
                        buffers.add(effs.getInt(i));
                        team.basePowerMap.put(SoldierDefine.NOR_POWER_BUFF,buffers);
                        team.powerMap.put(SoldierDefine.NOR_POWER_BUFF,buffers);
                    }
                }
            }
        }
    }

    /***通过光环生成出生buff**/
    public void getBattleBirthBuff(List<PlayerTeam> soldiers,List<PlayerTeam> monsters){
        List<Integer> soldierAuras = (List<Integer>) soldiers.get(0).getValue(SoldierDefine.NOR_POWER_TYPE_AURAS);
        List<Integer> monsterAuras = (List<Integer>) monsters.get(0).getValue(SoldierDefine.NOR_POWER_TYPE_AURAS);

        for (Integer auraId : soldierAuras){
            JSONObject auraDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.AURA,auraId);
            if(auraDefine.getInt("target") == 1){
                //己方
                addBuff(soldiers,auraDefine);
            }else {
                //敌方
                addBuff(monsters,auraDefine);
            }
        }

        for (Integer auraId : monsterAuras){
            JSONObject auraDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.AURA,auraId);
            if(auraDefine.getInt("target") == 1){
                //己方
                addBuff(monsters,auraDefine);
            }else {
                //敌方
                addBuff(soldiers,auraDefine);
            }
        }
    }

    public void getSoldierAura(List<PlayerTeam> soldiers){
        HashMap<Integer,Integer> soldierAuraMap = new HashMap<>();
        for (PlayerTeam team : soldiers){
            int typeId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
            JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS,typeId);
            JSONArray auras = soldierDefine.getJSONArray("aura");
            for (int i=0;i<auras.length();i++){
                int auraId = auras.getInt(i);
                JSONObject auraDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.AURA,auraId);
                int auraType = auraDefine.getInt("type");
                if(soldierAuraMap.containsKey(auraType) == false){
                    soldierAuraMap.put(auraType,auraId);
                }else {
                    int _auraId = soldierAuraMap.get(auraType);
                    JSONObject _aruaDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.AURA,_auraId);
                    if(_aruaDefine.getInt("level") < auraDefine.getInt("level")){
                        soldierAuraMap.put(auraType,auraId);
                    }
                }
            }
        }
        List<Integer> aruaIds = new ArrayList<>();
        aruaIds.addAll(soldierAuraMap.values());
        for (PlayerTeam team : soldiers){
            team.basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_AURAS,aruaIds);
            team.powerMap.put(SoldierDefine.NOR_POWER_TYPE_AURAS,aruaIds);
        }
    }

    /*创建出战队列*/
    public List<PlayerTeam> createFightTeamList(List<Common.FightElementInfo> fightElementInfoList){
        List<PlayerTeam> fightList = new ArrayList<>();
        for(Common.FightElementInfo info : fightElementInfoList){
            int soldierId = info.getTypeid();
            if(soldierId==0){
                continue;
            }
            int num = info.getNum();
            if (num == 0){
                continue;
            }
            int post = info.getPost() + 10;
            fightList.add(creatFightTeam(soldierId,num,post));
        }
        getSoldierAura(fightList);
        return fightList;
    }

    /*战斗结束后调用销毁上一次的出战缓存*/
    public void battleEndHandle(int battleId){
        battles.remove(battleId);
    }


    /*创建佣兵的计算战力对象，不做战斗生成*/
    public PlayerTeam creatCountCapacityTeam(int soldierId, Integer num, int post){
        HashMap<Integer,Object> basePowerMap = new HashMap<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldierId);

        HashMap<Integer,Integer> map = soldierProxy.getSoldierPowerMap(soldierId, num);
        //TODO 处理装备等额外属性的加成逻辑
        EquipProxy equipProxy=getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        equipProxy.addBatterPower(map,post);
        int hpAddPercent = map.get(SoldierDefine.POWER_hpMaxRate);
        int atkAddPercent = map.get(SoldierDefine.POWER_atkRate);

        double hpMax = map.get(SoldierDefine.POWER_hp) * ( 1 + hpAddPercent/10000.0);
        double atk = map.get(SoldierDefine.POWER_atk) * ( 1 + atkAddPercent/10000.0);
//        map.put(SoldierDefine.NOR_POWER_SOLDIER_TYPE_HP,hpMax);
//        map.put(SoldierDefine.NOR_POWER_SOLDIER_TYPE_ATK,atk);
        int solderNum = map.get(SoldierDefine.NOR_POWER_NUM);
        hpMax *= solderNum;
        atk *= solderNum;
//        basePowerMap.putAll(map);
        for (int key : map.keySet()){
            basePowerMap.put(key,map.get(key)*1.0);
        }
        basePowerMap.put(SoldierDefine.POWER_hp, hpMax);
        basePowerMap.put(SoldierDefine.POWER_hpMax,hpMax);
        basePowerMap.put(SoldierDefine.POWER_atk,atk);
        basePowerMap.put(SoldierDefine.NOR_POWER_INDEX,post);
        basePowerMap.put(SoldierDefine.NOR_POWER_NAME,playerProxy.getPlayerName());
        JSONObject modelDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.MODEL_GRO,soldierDefine.getInt("model"));
        basePowerMap.put(SoldierDefine.NOR_POWER_ICON,modelDefine.getInt("modelID"));
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE,soldierDefine.getInt("type"));
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_ID,soldierId);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_ATK_COUNT,0);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_CIRT_COUNT,0);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_BE_ATKED_COUNT,0);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_DODGE_COUNT,0);

        List<Integer> restrainList = new ArrayList<>();
        JSONArray array = soldierDefine.getJSONArray("restrain");
        for(int i=0;i<array.length();i++){
            restrainList.add(array.getInt(i));
        }
        basePowerMap.put(SoldierDefine.NOR_POWER_SOLDIER_RESTRAIN,restrainList);
        List<Integer> skillList = new ArrayList<>();
        skillList.add(soldierDefine.getInt("skill"));
        basePowerMap.put(SoldierDefine.NOR_POWER_SKILL,skillList);
        List<Integer> buffList = new ArrayList<>();
        basePowerMap.put(SoldierDefine.NOR_POWER_BUFF,buffList);
        PlayerTeam team = new PlayerTeam(basePowerMap,playerProxy.getPlayerId());
        return team;
    }

    /*创建佣兵的出战对象*/
    public PlayerTeam creatFightTeam(int soldierId, Integer num, int post){
        HashMap<Integer,Object> basePowerMap = new HashMap<>();
        HashMap<Integer,Object> capacityPowerMap = new HashMap<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, soldierId);

        HashMap<Integer,Integer> map = soldierProxy.getSoldierPowerMap(soldierId, num);
        //TODO 处理装备等额外属性的加成逻辑
        EquipProxy equipProxy=getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        equipProxy.addBatterPower(map,post);
        int hpAddPercent = map.get(SoldierDefine.POWER_hpMaxRate);
        int atkAddPercent = map.get(SoldierDefine.POWER_atkRate);
        int hpMax = (int) (map.get(SoldierDefine.POWER_hp) * ( 1 + hpAddPercent/10000.0));
        int atk = (int) (map.get(SoldierDefine.POWER_atk) * ( 1 + atkAddPercent/10000.0));
        map.put(SoldierDefine.NOR_POWER_SOLDIER_TYPE_HP,hpMax);
        map.put(SoldierDefine.NOR_POWER_SOLDIER_TYPE_ATK,atk);
        int solderNum = map.get(SoldierDefine.NOR_POWER_NUM);
        hpMax *= solderNum;
        atk *= solderNum;
        basePowerMap.putAll(map);
        basePowerMap.put(SoldierDefine.POWER_hp, hpMax);
        basePowerMap.put(SoldierDefine.POWER_hpMax,hpMax);
        basePowerMap.put(SoldierDefine.POWER_atk,atk);
        basePowerMap.put(SoldierDefine.NOR_POWER_INDEX,post);
        basePowerMap.put(SoldierDefine.NOR_POWER_NAME,playerProxy.getPlayerName());
        JSONObject modelDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.MODEL_GRO,soldierDefine.getInt("model"));
        basePowerMap.put(SoldierDefine.NOR_POWER_ICON,modelDefine.getInt("modelID"));
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE,soldierDefine.getInt("type"));
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_ID,soldierId);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_ATK_COUNT,0);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_CIRT_COUNT,0);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_BE_ATKED_COUNT,0);
        basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_DODGE_COUNT,0);

        List<Integer> restrainList = new ArrayList<>();
        JSONArray array = soldierDefine.getJSONArray("restrain");
        for(int i=0;i<array.length();i++){
            restrainList.add(array.getInt(i));
        }
        basePowerMap.put(SoldierDefine.NOR_POWER_SOLDIER_RESTRAIN,restrainList);
        List<Integer> skillList = new ArrayList<>();
        skillList.add(soldierDefine.getInt("skill"));
        basePowerMap.put(SoldierDefine.NOR_POWER_SKILL,skillList);
        List<Integer> buffList = new ArrayList<>();
        basePowerMap.put(SoldierDefine.NOR_POWER_BUFF,buffList);
        PlayerTeam team = new PlayerTeam(basePowerMap,playerProxy.getPlayerId());
        for (int i= 1;i<SoldierDefine.TOTAL_FIGHT_POWER;i++){
            Object value = basePowerMap.get(i);
            double doubleValue = (Integer)value * 1.0;
            capacityPowerMap.put(i,doubleValue);
        }
        capacityPowerMap.put(SoldierDefine.POWER_hp,map.get(SoldierDefine.NOR_POWER_SOLDIER_TYPE_HP) *1.0);
        capacityPowerMap.put(SoldierDefine.POWER_hpMax,map.get(SoldierDefine.NOR_POWER_SOLDIER_TYPE_HP) *1.0);
        capacityPowerMap.put(SoldierDefine.POWER_atk,map.get(SoldierDefine.NOR_POWER_SOLDIER_TYPE_ATK) *1.0);
        team.capacityMap = capacityPowerMap;
        return team;
    }


    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }

    public int checkFightMember(List<Common.FightElementInfo> fightElementInfoList){
        //判断出战单位是否合法
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        HashMap<Integer,Object> posMap = new HashMap<>();
        HashMap<Integer,Integer> numMap = new HashMap<>();
        if (fightElementInfoList.size() == 0){
            return ErrorCodeDefine.M50000_4;
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Integer> openPost =playerProxy.getPlayerFightPost();
        for(Common.FightElementInfo info : fightElementInfoList){
            int soldierTypeId = info.getTypeid();
            int num = info.getNum();
            int post = info.getPost();
            if(soldierProxy.getSoldierId(soldierTypeId) == 0){
                return ErrorCodeDefine.M50000_1;
            }
            if(playerProxy.getPowerValue(PlayerPowerDefine.POWER_command) < num){
                return ErrorCodeDefine.M50000_6;
            }
            if(openPost.contains(post) == false){
                return ErrorCodeDefine.M50000_5;
            }
            if(num == 0){
                return ErrorCodeDefine.M50000_4;
            }
            if(numMap.containsKey(soldierTypeId) == false){
                numMap.put(soldierTypeId,num);
            }else{
                num = num + numMap.get(soldierTypeId);
                numMap.put(soldierTypeId,num);
            }
            if(posMap.containsKey(post) == true){
                return ErrorCodeDefine.M50000_2;
            }
            if(post < 1 || post > 6){
                return ErrorCodeDefine.M50000_3;
            }
            posMap.put(post,new Object());
        }
        Iterator<Map.Entry<Integer,Integer>> iter = numMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer,Integer> entry = (Map.Entry) iter.next();
            Integer soldierTypeId = entry.getKey();
            Integer num = entry.getValue();
            if(soldierProxy.getSoldierNum(soldierTypeId) < num){
                return ErrorCodeDefine.M50000_1;
            }
        }
        return 0;
    }
}
