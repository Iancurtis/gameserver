package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerBattle;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTeam;
import com.znl.core.PowerRanks;
import com.znl.define.*;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Dungeo;
import com.znl.pojo.db.Mail;
import com.znl.proto.Common;
import com.znl.proto.M6;
import com.znl.template.MailTemplate;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2015/11/10.
 */
public class DungeoProxy extends BasicProxy {
    private Set<Dungeo> dungeos = new ConcurrentHashSet<>();
    private Set<Dungeo> ristDungeos = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (Dungeo dungeo : dungeos) {
            dungeo.finalize();
        }
        for (Dungeo dungeo : ristDungeos) {
            dungeo.finalize();
        }
    }

    @Override
    protected void init() {

    }


    public void saveDungeo() {
        List<Dungeo> dungeoList = new ArrayList<>();
        synchronized (changeDungeos) {
            while (true) {
                Dungeo dungeo = changeDungeos.poll();
                if (dungeo == null) {
                    break;
                }
                dungeoList.add(dungeo);
            }
        }
        for (Dungeo dungeo : dungeoList) {
            dungeo.save();
        }

    }

    private LinkedList<Dungeo> changeDungeos = new LinkedList<>();

    private void pushDungeoToChangeList(Dungeo dungeo) {
        //插入更新队列
        synchronized (changeDungeos) {
            if (!changeDungeos.contains(dungeo)) {
                changeDungeos.offer(dungeo);
            }
        }
    }


    public DungeoProxy(Set<Long> dungeoIds, String areaKey) {
        this.areaKey = areaKey;
        for (Long id : dungeoIds) {
            Dungeo dungeo = BaseDbPojo.get(id, Dungeo.class, areaKey);
            if (dungeo.getDungeoId() >= ActorDefine.MIN_DUNGEO_ID) {
                dungeos.add(dungeo);
            } else {
                ristDungeos.add(dungeo);
            }

        }
    }


    public DungeoProxy() {

    }

    public List<Integer> getAllDungeoId() {
        List<Integer> res = new ArrayList<>();
        for (Dungeo dungeo : dungeos) {
            res.add(dungeo.getDungeoId());
        }
        return res;
    }

    public List<Integer> getAllRistDungeoId() {
        List<Integer> res = new ArrayList<>();
        for (Dungeo dungeo : ristDungeos) {
            res.add(dungeo.getDungeoId());
        }
        return res;
    }

    public void openNewDungeo(int dungeoId) {

        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Dungeo newDungeo = BaseDbPojo.create(Dungeo.class, areaKey);

        newDungeo.setPlayerId(playerProxy.getPlayerId());
        newDungeo.setDungeoId(dungeoId);
        playerProxy.addDungeoToPlayer(newDungeo.getId());
        if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
            dungeos.add(newDungeo);
        } else {
            ristDungeos.add(newDungeo);
        }
        playerProxy.setHigestDungId(dungeoId);
        newDungeo.save();
    }

    /*获得副本的星星总数*/
    public int getTotalStarNum(int dungeoId) {

        Dungeo dungeo = null;
        if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
            dungeo = getDungeoById(dungeoId);
        } else {
            dungeo = getRistDungeoById(dungeoId);
        }
        int totalStar = 0;
        for (Integer star : dungeo.getStarList()) {
            totalStar += star;
        }
        return totalStar;
    }

    /*通过副本id获得副本对象*/
    private Dungeo getDungeoById(int dungeoId) {
        if (dungeoId < ActorDefine.MIN_DUNGEO_ID) {
            return getRistDungeoById(dungeoId);
        }
        for (Dungeo dungeo : dungeos) {
            if (dungeo.getDungeoId() == dungeoId) {
                return dungeo;
            }
        }
        return null;
    }

    /*通过副本id获得冒险副本对象*/
    private Dungeo getRistDungeoById(int dungeoId) {
        for (Dungeo dungeo : ristDungeos) {
            if (dungeo.getDungeoId() == dungeoId) {
                return dungeo;
            }
        }
        return null;
    }

    //获得时间ID
    public int getMaxEvenId(int dungeoId) {
        Dungeo dungeo = getDungeoById(dungeoId);
        if (dungeo == null) {
            return 0;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EVENT, "chapter", dungeo.getDungeoId(), "sort", dungeo.getStarList().size());
        if (jsonObject == null) {
            JSONObject chjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeo.getDungeoId());
            int sort = chjson.getInt("sort") - 1;
            JSONObject dunJson = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", sort);
            if (dunJson == null) {
                return 0;
            }
            int dunId = dunJson.getInt("ID");
            ;
            List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.EVENT, "chapter", dunId);
            jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EVENT, "chapter", dunId, "sort", jsonObjectList.size());
        }
        if (jsonObject == null) {
            return 0;
        }
        return jsonObject.getInt("ID");
    }

    /*获取关卡怪物属性*/
    public Common.MonsterInfo getCommonMonsterInfo(int monsterId, int num, int post) {
        Common.MonsterInfo.Builder builder = Common.MonsterInfo.newBuilder();
        builder.setId(monsterId);
        builder.setPost(post);
        builder.setNum(num);
        return builder.build();
    }

    //获取common的副本事件列表
    public ArrayList<Common.EventInfo> getCommonEventList(int dungeoId) {
        Dungeo dungeo = getDungeoById(dungeoId);
        ArrayList<Common.EventInfo> resList = new ArrayList<>();
        ArrayList<JSONObject> eventList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.EVENT, "chapter", dungeo.getDungeoId());
        if (dungeoId < ActorDefine.MIN_DUNGEO_ID) {
            eventList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ADVENTURE_EVENT, "chapter", dungeo.getDungeoId());
        }
        for (int i = 1; i <= dungeo.getStarList().size() + 1; i++) {
            JSONObject eventDefine = null;
            for (JSONObject jsonObject : eventList) {
                int sort = jsonObject.getInt("sort");
                if (sort == i) {
                    eventDefine = jsonObject;
                    break;
                }
            }
            if (eventDefine == null) {
                //到顶处理
                break;
            }
            if (eventDefine != null) {
                Common.EventInfo.Builder builder = Common.EventInfo.newBuilder();
                builder.setId(eventDefine.getInt("ID"));
                List<Integer> starList = dungeo.getStarList();
                builder.setStar(getEventIndexStar(i, dungeo));
                JSONObject monsterGroup = getMonsterGroup(eventDefine.getInt("monstergroup"));
                builder.setForce(monsterGroup.getInt("force"));
                //获取6个槽位的怪物
                for (int index = 1; index <= 6; index++) {
                    JSONArray pos = monsterGroup.getJSONArray("position" + index);
                    int size = pos.length();
                    if (size > 1) {
                        builder.addMonsterInfos(getCommonMonsterInfo(pos.getInt(0), pos.getInt(1), index));
                    }
                }
                resList.add(builder.build());
            }
        }
        return resList;
    }

    /*获取rs*/
    private int getCommonDungeoInfo(List<Common.EventInfo> infos, Integer dungeoId) {
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        List<Integer> dungeoList = null;
        if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
            dungeoList = dungeoProxy.getAllDungeoId();
        } else {
            dungeoList = dungeoProxy.getAllRistDungeoId();
        }
        if (dungeoList.contains(dungeoId) == false) {
            return ErrorCodeDefine.M60001_1;
        }
        infos.addAll(getCommonEventList(dungeoId));
        SortUtil.anyProperSort(infos, "getId", true);
        return 0;
    }

    /*获取common副本信息*/
    public List<Common.DungeonInfo> getCommonDungeonInfo() {
        List<Common.DungeonInfo> res = new ArrayList<>();
        for (Dungeo dungeo : ristDungeos) {
            List<Common.EventInfo> infos = new ArrayList<>();
            int rs = getCommonDungeoInfo(infos, dungeo.getDungeoId());
            Common.DungeonInfo.Builder builder = Common.DungeonInfo.newBuilder();
            builder.setRs(rs);
            builder.addAllEventInfo(getCommonEventList(dungeo.getDungeoId()));
            builder.addAllBoxes(getDungeoBoxByDungeoId(dungeo.getDungeoId()));
            builder.setStar(getTotalStarNum(dungeo.getDungeoId()));

            builder.setDungeoId(dungeo.getDungeoId());
            JSONObject dungeoDefine;
            if (dungeo.getDungeoId() >= ActorDefine.MIN_DUNGEO_ID) {
                dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeo.getDungeoId());
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                builder.setTimes((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy));
                builder.setTimesTotal(20);
            } else {
                dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeo.getDungeoId());
                //  TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                //    builder.setTimes(timerdbProxy.getAdventureTimesById(dungeo.getDungeoId()));
                builder.setTimes(getRistbuyTimes(dungeo.getDungeoId()));
                builder.setTimesTotal(dungeoDefine.getInt("time"));
            }
            int totalstar = dungeoDefine.getInt("starNum");
            builder.setTotalStar(totalstar);
            res.add(builder.build());
        }

        for (Dungeo dungeo : dungeos) {
            List<Common.EventInfo> infos = new ArrayList<>();
            int rs = getCommonDungeoInfo(infos, dungeo.getDungeoId());
            Common.DungeonInfo.Builder builder = Common.DungeonInfo.newBuilder();
            builder.setRs(rs);
            builder.addAllEventInfo(getCommonEventList(dungeo.getDungeoId()));
            builder.addAllBoxes(getDungeoBoxByDungeoId(dungeo.getDungeoId()));
            builder.setStar(getTotalStarNum(dungeo.getDungeoId()));

            builder.setDungeoId(dungeo.getDungeoId());
            JSONObject dungeoDefine;
            if (dungeo.getDungeoId() >= ActorDefine.MIN_DUNGEO_ID) {
                dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeo.getDungeoId());
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                builder.setTimes((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy));
                builder.setTimesTotal(20);
            } else {
                dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeo.getDungeoId());
                //TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                //  builder.setTimes(timerdbProxy.getAdventureTimesById(dungeo.getDungeoId()));
                builder.setTimes(getRistChangeTimes(dungeo.getDungeoId()));
                builder.setTimesTotal(dungeoDefine.getInt("time"));
            }
            int totalstar = dungeoDefine.getInt("starNum");
            builder.setTotalStar(totalstar);
            res.add(builder.build());
        }
        return res;
    }


//    private List<Common.DungeonInfo> getCommonRistDungeoInfo() {
//        List<Common.DungeonInfo> res = new ArrayList<>();
//        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
//        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
//        for (JSONObject define : list) {
//            int dungeoId = define.getInt("ID");
//            Common.DungeonInfo.Builder builder =Common.DungeonInfo.newBuilder();
//            builder.setDungeoId(dungeoId);
//            if (dungeoId == DungeonDefine.EXTREME_ADVENTRUE) {
//                builder.set(dungeoProxy.getDungeoEventPassNum(dungeoId));
//                builder.setCount(-1);
//                builder.setTotalCount(-1);
//                builder.setHaveBox(0);
//                builder.setStar(-1);
//                builder.setTotalStar(-1);
//            } else {
//                //在计数器里面补充次数time
//                TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
//                builder.setCount(timerdbProxy.getAdventureTimesById(dungeoId));
//                //builder.setCount(5);
//                builder.setTotalCount(define.getInt("time"));
//                builder.setLen(-1);
//                List<Integer> boxList = dungeoProxy.getRistDungeoBoxByDungeoId(dungeoId);
//                if (boxList.size() > 0) {
//                    builder.setHaveBox(1);
//                } else {
//                    builder.setHaveBox(0);
//                }
//                builder.setStar(dungeoProxy.getTotalStarNum(dungeoId));
//                builder.setTotalStar(define.getInt("starNum"));
//            }
//            res.add(builder.build());
//        }
//        return res;
//    }


    /*获得副本的事件列表*/
    public ArrayList<M6.EventInfo> getEventList(int dungeoId) {
        Dungeo dungeo = getDungeoById(dungeoId);
        ArrayList<M6.EventInfo> res = new ArrayList<>();
        ArrayList<JSONObject> eventList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.EVENT, "chapter", dungeo.getDungeoId());
        if (dungeoId < ActorDefine.MIN_DUNGEO_ID) {
            eventList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ADVENTURE_EVENT, "chapter", dungeo.getDungeoId());
        }
        for (int i = 1; i <= dungeo.getStarList().size() + 1; i++) {
            JSONObject eventDefine = null;
            for (JSONObject jsonObject : eventList) {
                int sort = jsonObject.getInt("sort");
                if (sort == i) {
                    eventDefine = jsonObject;
                    break;
                }
            }
            if (eventDefine == null) {
                //到顶处理
                break;
            }
            if (eventDefine != null) {
                M6.EventInfo.Builder builder = M6.EventInfo.newBuilder();
                builder.setId(eventDefine.getInt("ID"));
                List<Integer> starList = dungeo.getStarList();
//                if(dungeo.getStarList().size() < i){
//                    builder.setStar(0);//未没打赢的关卡做个特殊处理
//                }else{
//                    builder.setStar(dungeo.getStarList().get(i-1));
//                }
                builder.setStar(getEventIndexStar(i, dungeo));
                JSONObject monsterGroup = getMonsterGroup(eventDefine.getInt("monstergroup"));
             //   builder.setForce(monsterGroup.getInt("force"));
                //获取6个槽位的怪物
                for (int index = 1; index <= 6; index++) {
                    JSONArray pos = monsterGroup.getJSONArray("position" + index);
                    int size = pos.length();
                    if (size > 1) {
                        // builder.addMonsterInfos(getMonsterInfo(pos.getInt(0), pos.getInt(1), index));
                    }
                }
                res.add(builder.build());
            }
        }
        return res;
    }


    public int getEventIndexStar(int sort, Dungeo dungeo) {
        if (dungeo.getStarList().size() < sort) {
            return 0;
        } else {
            return dungeo.getStarList().get(sort - 1);
        }
    }

    /*通过怪物组id获得怪物组数据*/
    public JSONObject getMonsterGroup(int groupId) {
        return ConfigDataProxy.getConfigInfoFindById(DataDefine.MONSTER_GROUP, groupId);
    }


    public List<PlayerTeam> creatEventMonsterList(int eventId) {
        JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, eventId);
        int monsterGroupId = eventDefine.getInt("monstergroup");
        return getMonsterList(monsterGroupId);
    }

    public List<PlayerTeam> createAdvantrueMonsterList(int eventId) {
        JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, eventId);
        int monsterGroupId = eventDefine.getInt("monstergroup");
        return getMonsterList(monsterGroupId);
    }

    //军团副本每日4点刷新(270004计算血量百分比也会用到改方法)
    public List<PlayerTeam> createArmyGroupDungeoMonsterList(int eventId) {
        JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.LegionEvent, eventId);
        int monsterGroupId = eventDefine.getInt("monstergroup");
        return getMonsterList(monsterGroupId);
    }

     public List<PlayerTeam> getMonsterList(int groupId) {
        List<PlayerTeam> monsters = new ArrayList<>();
        JSONObject monsterGroup = getMonsterGroup(groupId);
        //获取6个槽位的怪物
        for (int index = 1; index <= 6; index++) {
            JSONArray pos = monsterGroup.getJSONArray("position" + index);
            int size = pos.length();
            if (size > 1) {
                String name = monsterGroup.getString("name");
                PlayerTeam monster = creatMonster(pos.getInt(0), pos.getInt(1), index + 20, name);//index从 21开始
                monsters.add(monster);
            }
        }
        getMonsterAura(monsters);
        return monsters;
    }


    public List<PlayerTeam> getArenaMonster(long rank, int type) {
        List<PlayerTeam> monsters = new ArrayList<>();
        JSONObject monsterGroup = null;
        if (ArenaDefine.ROBOTGETTYPR1 == type) {
            monsterGroup = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ArenaRobot, "rank", rank);
        } else {
            monsterGroup = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ArenaRobot, "ID", -rank);
        }
        //获取6个槽位的怪物
        for (int index = 1; index <= 6; index++) {
            JSONArray pos = monsterGroup.getJSONArray("position" + index);
            int size = pos.length();
            if (size > 1) {
                String name = monsterGroup.getString("name");
                PlayerTeam monster = creatMonster(pos.getInt(0), pos.getInt(1), index + 20, name);//index从 21开始
                monsters.add(monster);
            }
        }
        getMonsterAura(monsters);
        return monsters;
    }

    public ArrayList<Integer> getSoldierTypeIdListFormPlayerTeam(List<PlayerTeam> teams) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int index = 1; index <= 6; index++) {
            int typeId = 0;
            for (PlayerTeam team : teams) {
                int post = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
                if (post - 10 == index || post - 20 == index) {
                    typeId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
                    break;
                }
            }
            list.add(typeId);
        }
        return list;
    }

    public ArrayList<Integer> getSoldierNumListFormPlayerTeam(List<PlayerTeam> teams) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int index = 1; index <= 6; index++) {
            int num = 0;
            for (PlayerTeam team : teams) {
                int post = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
                if (post - 10 == index || post - 20 == index) {
                    num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
                    break;
                }
            }
            list.add(num);
        }
        return list;
    }

    private void getMonsterAura(List<PlayerTeam> monsters) {
        HashMap<Integer, Integer> monsterAuraMap = new HashMap<>();
        for (PlayerTeam team : monsters) {
            int typeId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
            JSONObject monsterDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.MONSTER, typeId);
            JSONArray auras = monsterDefine.getJSONArray("aura");
            for (int i = 0; i < auras.length(); i++) {
                int auraId = auras.getInt(i);
                JSONObject auraDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.AURA, auraId);
                int auraType = auraDefine.getInt("type");
                if (monsterAuraMap.containsKey(auraType) == false) {
                    monsterAuraMap.put(auraType, auraId);
                } else {
                    int _auraId = monsterAuraMap.get(auraType);
                    JSONObject _aruaDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.AURA, _auraId);
                    if (_aruaDefine.getInt("level") < auraDefine.getInt("level")) {
                        monsterAuraMap.put(auraType, auraId);
                    }
                }
            }
        }
        List<Integer> aruaIds = new ArrayList<>();
        aruaIds.addAll(monsterAuraMap.values());
        for (PlayerTeam team : monsters) {
            team.basePowerMap.put(SoldierDefine.NOR_POWER_TYPE_AURAS, aruaIds);
            team.powerMap.put(SoldierDefine.NOR_POWER_TYPE_AURAS, aruaIds);
        }
    }

    private static PlayerTeam creatMonster(int monsterId, int num, int post, String name) {
        JSONObject monster = ConfigDataProxy.getConfigInfoFindById(DataDefine.MONSTER, monsterId);
        HashMap<Integer, Object> map = new HashMap<>();
        for (int i = 1; i <= SoldierDefine.TOTAL_FIGHT_POWER; i++) {
            map.put(i, 0);
        }
        map.put(SoldierDefine.POWER_hp, monster.getInt("HP") * num);
        map.put(SoldierDefine.POWER_hpMax, monster.getInt("HP") * num);
//        map.put(SoldierDefine.POWER_hp,1);
//        map.put(SoldierDefine.POWER_hpMax,1);
        map.put(SoldierDefine.NOR_POWER_TYPE, monster.getInt("type"));
        map.put(SoldierDefine.NOR_POWER_INDEX, post);
        map.put(SoldierDefine.NOR_POWER_NUM, num);
        JSONObject modelDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.MODEL_GRO, monster.getInt("model"));
        map.put(SoldierDefine.NOR_POWER_ICON, modelDefine.getInt("modelID"));
        map.put(SoldierDefine.POWER_atk, monster.getInt("ATK") * num);
        map.put(SoldierDefine.POWER_hitRate, monster.getInt("hitRate"));
        map.put(SoldierDefine.POWER_dodgeRate, monster.getInt("dodgeRate"));
        map.put(SoldierDefine.POWER_critRate, monster.getInt("critRate"));
        map.put(SoldierDefine.POWER_defRate, monster.getInt("defRate"));
        map.put(SoldierDefine.POWER_wreck, monster.getInt("wreck"));
        map.put(SoldierDefine.POWER_defend, monster.getInt("defend"));
        map.put(SoldierDefine.NOR_POWER_TYPE_ATK_COUNT, 0);
        map.put(SoldierDefine.NOR_POWER_TYPE_CIRT_COUNT, 0);
        map.put(SoldierDefine.NOR_POWER_TYPE_BE_ATKED_COUNT, 0);
        map.put(SoldierDefine.NOR_POWER_TYPE_DODGE_COUNT, 0);
        map.put(SoldierDefine.NOR_POWER_NAME, name);
        List<Integer> skills = new ArrayList<>();
        skills.add(monster.getInt("skill"));
        map.put(SoldierDefine.NOR_POWER_SKILL, skills);
        List<Integer> buffs = new ArrayList<>();
        map.put(SoldierDefine.NOR_POWER_BUFF, buffs);
        map.put(SoldierDefine.NOR_POWER_SOLDIER_TYPE_HP, monster.getInt("HP"));
        map.put(SoldierDefine.NOR_POWER_SOLDIER_TYPE_ATK, monster.getInt("ATK"));
        map.put(SoldierDefine.NOR_POWER_TYPE_ID, monsterId);
        List<Integer> restrainList = new ArrayList<>();
        JSONArray array = monster.getJSONArray("restrain");
        for (int i = 0; i < array.length(); i++) {
            restrainList.add(array.getInt(i));
        }
        map.put(SoldierDefine.NOR_POWER_SOLDIER_RESTRAIN, restrainList);
        PlayerTeam team = new PlayerTeam(map, -monsterId);
        return team;
    }

    /*获取关卡怪物属性*/
    public M6.MonsterInfo getMonsterInfo(int monsterId, int num, int post) {
        M6.MonsterInfo.Builder builder = M6.MonsterInfo.newBuilder();
        builder.setId(monsterId);
        builder.setPost(post);
        builder.setNum(num);
        return builder.build();
    }

    /*获取副本可领的宝箱*/
    public List<Integer> getDungeoBoxByDungeoId(int dungeoId) {
        List<Integer> res = new ArrayList<>();
        Dungeo dungeo = getDungeoById(dungeoId);
        res.addAll(dungeo.getGetBox().stream().map(Long::intValue).collect(Collectors.toList()));
        return res;
    }

    /*获取冒险副本可领的宝箱*/
    public List<Integer> getRistDungeoBoxByDungeoId(int dungeoId) {
        List<Integer> res = new ArrayList<>();
        Dungeo dungeo = getRistDungeoById(dungeoId);
        res.addAll(dungeo.getGetBox().stream().map(Long::intValue).collect(Collectors.toList()));
        return res;
    }

    /*战斗前调用查询该关卡是否能挑战*/
    public int fightEventAsk(int evenId, int battleType, boolean fight) {
        JSONObject eventDefine = null;
        EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        if (battleType == BattleDefine.BATTLE_TYPE_DUNGEON) {
            eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, evenId);
        } else if (battleType == BattleDefine.BATTLE_TYPE_ADVANTRUE) {
            eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, evenId);
            OrdnanceProxy ordnanceProxy = getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
            /*if(ordnanceProxy.ordanceFreeSize() <=0){
                return ErrorCodeDefine.M60002_19;
            }*/
            if (eventDefine.getInt("chapter") == 1) {
                if (equipProxy.getEquipBagLesFree() <= 0) {
                    return ErrorCodeDefine.M60002_18;
                }
            }
            //TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int lestimes = getRistChangeTimes(eventDefine.getInt("chapter"));
            if (lestimes <= 0) {
                return ErrorCodeDefine.M60002_6;
            }
        }
       /* EquipProxy equipProxy=getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        if(equipProxy.getEquipBagLesFree()<=0){
            return ErrorCodeDefine.M60002_20;
        }*/
        if (eventDefine == null) {
            return ErrorCodeDefine.M60002_1;
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (battleType == BattleDefine.BATTLE_TYPE_DUNGEON && playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy) <= 0) {
            return ErrorCodeDefine.M60002_5;
        }

        int dungeoId = eventDefine.getInt("chapter");
        Dungeo dungeo = getDungeoById(dungeoId);
        if (dungeo == null) {
            return ErrorCodeDefine.M60002_2;
        }
        JSONObject dungeoDefine;
        if (dungeoId < ActorDefine.MIN_DUNGEO_ID) {
            dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeoId);
        } else {
            dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
            int rankneed = dungeoDefine.getInt("rankneed");
            long playerRank = playerProxy.getPowerValue(PlayerPowerDefine.POWER_militaryRank);
            if (playerRank < rankneed) {
                return ErrorCodeDefine.M60002_3;
            }
        }
        int sort = eventDefine.getInt("sort");
        int size = dungeo.getStarList().size();
        if (sort > size + 1) {
            return ErrorCodeDefine.M60002_4;
        }
        if (fight == false) {
            if (sort - 1 >= size || dungeo.getStarList().get(sort - 1) < 3) {
                return ErrorCodeDefine.M60005_12;
            }
        }

        if (eventDefine.getInt("chapter") == 4 || eventDefine.getInt("chapter") == 1) {
            if (equipProxy.getEquipBagLesFree() <= 0) {
                return ErrorCodeDefine.M60005_21;
            }
        }
        if (eventDefine.getInt("chapter") == 4) {
            if (checkmopp() == 1) {
                return ErrorCodeDefine.M60002_13;
            }
            if (playerProxy.getPlayer().getLimitChangeMaxId() < eventDefine.getInt("sort")) {
                return ErrorCodeDefine.M60002_14;
            }
            if (playerProxy.getPlayer().getGetLimitChangeId() != eventDefine.getInt("sort")) {
                return ErrorCodeDefine.M60002_15;
            }

            if (DungeonDefine.DEOGEO_LIMIT_CHANGE - playerProxy.getPlayer().getDungeolimitchange() <= 0) {
                return ErrorCodeDefine.M60002_16;
            }
        }
        return 0;
    }


    public int getDungeoEventPassNum(int dungeoId) {
        Dungeo dungeo = getDungeoById(dungeoId);
        return dungeo.getStarList().size() + 1;
    }

    /**
     * 关卡挑战结束逻辑，开启关卡返回true，其他情况返回false
     **/
    public boolean eventFightEndHandle(PlayerBattle battle) {
        //关卡进阶
        boolean rs = countEventStar(battle);
        //发放奖励
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        rewardProxy.getRewardToPlayer(battle.reward, LogDefine.GET_GUANQIA);
        return rs;
    }


    public int countFightStar(List<PlayerTeam> soldiers, int totalCapacity) {
        int capacity = countSoldierCapacity(soldiers);
        double percent = capacity * 1.0 / totalCapacity * 100;
        int newStar = 0;
        //计算副本星星的数量
        if (percent <= DungeonDefine.ONE_STAR_CONDITION) {
            newStar = 1;
        } else if (percent <= DungeonDefine.TWO_STAR_CONDITION) {
            newStar = 2;
        } else {
            newStar = 3;
        }
        return newStar;
    }

    //判断是否首次通关
    public boolean isfirstThrough(PlayerBattle battle) {
        if (battle.battleResult == false) {
            return false;
        }
        JSONObject eventDefine = null;
        int dungeoId = 0;
        if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
            eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, battle.infoType);
            dungeoId = eventDefine.getInt("chapter");
        } else if (battle.type == BattleDefine.BATTLE_TYPE_ADVANTRUE) {
            eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, battle.infoType);
            dungeoId = eventDefine.getInt("chapter");
        }
        Dungeo dungeo = getDungeoById(dungeoId);
        int starListSize = dungeo.getStarList().size();
        int sort = eventDefine.getInt("sort");
        if (sort == starListSize + 1) {
            //首次通关
            return true;
        }
        return false;
    }

    public boolean countEventStar(PlayerBattle battle) {
        boolean result = false;
        JSONObject eventDefine = null;
        int dungeoId = 0;
        JSONObject dungeoDefine = null;
        if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
            eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.EVENT, battle.infoType);
            dungeoId = eventDefine.getInt("chapter");
            dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
            int sorts = eventDefine.getInt("sort");
            JSONObject nextEvent = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EVENT, "chapter", dungeoId, "sort", sorts + 1);
           /* if (nextEvent == null) {
                PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.sendSystemchat(ActorDefine.CLEARANCE_NOTICE_TYPE, dungeoId, ActorDefine.CONDITION_TWO);//发送系统公告2
            }*/
        } else if (battle.type == BattleDefine.BATTLE_TYPE_ADVANTRUE) {
            eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, battle.infoType);
            dungeoId = eventDefine.getInt("chapter");
            dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeoId);
        }

        Dungeo dungeo = getDungeoById(dungeoId);
        int oldTotalStar = getTotalStarNum(dungeoId);
        int starListSize = dungeo.getStarList().size();
        int newStar = countFightStar(battle.soldierList, battle.totalCapacity);
        int sort = eventDefine.getInt("sort");

        if (sort == starListSize + 1) {
            //攻打最新的关卡
            dungeo.getStarList().add(newStar);
            //判断这个关卡是否最高关卡，是的话要开启新的副本
            if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
                JSONObject nextEvent = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.EVENT, "chapter", dungeoId, "sort", sort + 1);
                if (nextEvent == null) {
                    JSONObject nextDungeo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", dungeoDefine.getInt("sort") + 1);
                    openNewDungeo(nextDungeo.getInt("ID"));
                    result = true;
                    PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    playerProxy.sendSystemchat(ActorDefine.CLEARANCE_NOTICE_TYPE, dungeoId, ActorDefine.CONDITION_TWO);//发送系统公告2
                }
            }

        } else {
            int oldSatr = dungeo.getStarList().get(sort - 1);
            if (oldSatr < newStar) {
                dungeo.getStarList().set(sort - 1, newStar);
            }
        }

        //判断一下是否有宝箱开启了
        int newTotalStar = getTotalStarNum(dungeoId);
        for (int i = 1; i <= 3; i++) {
            int needSatr = dungeoDefine.getInt("star" + i);
            if (needSatr > 0 && oldTotalStar < needSatr && newTotalStar >= needSatr) {
                dungeo.getGetBox().add((long) i);
            }
        }
        //保存
        pushDungeoToChangeList(dungeo);

        if (battle.type == BattleDefine.BATTLE_TYPE_DUNGEON) {
            //发送到关卡排行榜
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            GameMsg.AddPlayerToRank msg = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId(), rankStarNum(), PowerRanksDefine.POWERRANK_TYPE_CUSTOMS);
            sendRankServiceMsg(msg);
        }
        return result;
    }


    //检查是否有新关卡开放只在登陆时候调用
    public void checkOpenNewDungeo(int dungeoId) {
        Dungeo dungeo = getDungeoById(dungeoId);
        if (dungeo == null) {
            return;
        }
        List<JSONObject> dungeolist = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.EVENT, "chapter", dungeoId);
        if (dungeo.getStarList().size() == dungeolist.size()) {
            //有新副本开启执行开启
            JSONObject nextEvent = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
            if (nextEvent != null) {
                JSONObject nextDungeo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", nextEvent.getInt("sort") + 1);
                if (nextDungeo != null) {
                    openNewDungeo(nextDungeo.getInt("ID"));
                }
            }
        }
    }

    //计算星星总数
    public long rankStarNum() {
        long starNum = 0;
        for (Dungeo dg : dungeos) {
            for (Integer star : dg.getStarList()) {
                starNum += star;
            }
        }
        return starNum;
    }

    public List<Integer> reduceDeadSoldier(List<PlayerTeam> soldiers, int battleType, HashMap<Integer, Integer> deadNumMap, SoldierProxy soldierProxy) {
        List<Integer> ids = new ArrayList<>();
        if (getGameProxy() != null && battleType == BattleDefine.BATTLE_TYPE_DUNGEON) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (playerProxy.getLevel() < 10) {
                return ids;
            }
        }
        HashMap<Integer, Integer> reduceMap = new HashMap<>();//<id,数量>
        if (soldiers != null) {
            for (PlayerTeam team : soldiers) {
                int soldierId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
                int totalNum = (int) team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM);
                int nowNum = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
                if (totalNum > nowNum) {
                    if (reduceMap.containsKey(soldierId)) {
                        int num = totalNum - nowNum + reduceMap.get(soldierId);
                        reduceMap.put(soldierId, num);
                    } else {
                        reduceMap.put(soldierId, totalNum - nowNum);
                    }
                }
            }
        }
//        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        for (Integer id : reduceMap.keySet()) {
            int deadNum = reduceMap.get(id);
            int realyDeadNum = 0;
            int hurtNum = 0;
            switch (battleType) {
                case BattleDefine.BATTLE_TYPE_ADVANTRUE:
                case BattleDefine.BATTLE_TYPE_DUNGEON:
                case BattleDefine.BATTLE_TYPE_WORLD_DEFEND:
                    realyDeadNum = (int) (deadNum * 0.2);
                    hurtNum = deadNum - realyDeadNum;
                    break;
                case BattleDefine.BATTLE_TYPE_WORLD:
                    realyDeadNum = (int) (deadNum * 0.2);
                    hurtNum = deadNum - realyDeadNum;
                    soldierProxy.reduceSoldierBaseNum(id, deadNum);
                    deadNum = 0;
                    break;
                default:
                    deadNum = 0;
            }
            deadNumMap.put(id, deadNum);
            soldierProxy.reduceSoldierNum(id, deadNum, hurtNum, LogDefine.LOST_FIGHT_REDUCE);
            ids.add(id);
        }
        return ids;
    }


    public int countSoldierCapacity(List<PlayerTeam> soldiers) {
        int capacity = 0;
        for (PlayerTeam team : soldiers) {
            capacity += countSoldierCapacity(team);
        }
        return capacity;
    }

    private int countSoldierCapacity(PlayerTeam soldier) {
        int capacity = 0;
        double actC = 0;
        double hpC = 0;
        double hitRateC = 0;
        double dodgeRateC = 0;
        double critRateC = 0;
        double defRateC = 0;
        double wreckC = 0;
        double defendC = 0;
        int num = (int) soldier.getValue(SoldierDefine.NOR_POWER_NUM);
        int type = (int) soldier.basePowerMap.get(SoldierDefine.NOR_POWER_TYPE);
        double soldierTypeAdd = 0;
        switch (type) {
            case 1:
                soldierTypeAdd = 1;
                break;
            case 2:
                soldierTypeAdd = 2.4;
                break;
            case 3:
                soldierTypeAdd = 1.7;
                break;
            case 4:
                soldierTypeAdd = 4.08;
                break;
        }
        actC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_atk)) * 0.5 * soldierTypeAdd;
        hpC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_hp)) * 0.1;
        hitRateC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_hitRate)) / 10000.0 * 100;
        dodgeRateC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_dodgeRate)) / 10000.0 * 100;
        critRateC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_critRate)) / 10000.0 * 100;
        defRateC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_defRate)) / 10000.0 * 100;
        wreckC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_wreck)) / 100.0 * 10;
        defendC = ((double) soldier.capacityMap.get(SoldierDefine.POWER_defend)) / 100.0 * 10;
        capacity = (int) ((actC + hpC + hitRateC + dodgeRateC + critRateC + defRateC + wreckC + defendC) * num);
        return capacity;
    }

    /***
     * 获得佣兵载重
     ****/
    public long getTeamLoad(List<PlayerTeam> teams) {
        double load = 0;
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long addload = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_loadRate);
        for (PlayerTeam team : teams) {
            int teamLoad = (int) team.getValue(SoldierDefine.POWER_load);
            int loadRate = (int) team.getValue(SoldierDefine.POWER_loadRate);
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            double value = teamLoad * (1 + loadRate / 10000.0) * num;
            // value=Math.floor(value*(10000+addload)/10000.0);
            load += value;
        }
        return (long) load;
    }

    public int getTeamCapacity(int typeId, int num, int post) {
        JSONObject soldierDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, typeId);
        int cap = 0;
        double actC = 0;
        double hpC = 0;
        double hitRateC = 0;
        double dodgeRateC = 0;
        double critRateC = 0;
        double defRateC = 0;
        double wreckC = 0;
        double defendC = 0;
        int type = soldierDefine.getInt("type");
        double soldierTypeAdd = 0;
        switch (type) {
            case 1:
                soldierTypeAdd = 1;
                break;
            case 2:
                soldierTypeAdd = 2.4;
                break;
            case 3:
                soldierTypeAdd = 1.7;
                break;
            case 4:
                soldierTypeAdd = 4.08;
                break;
        }
        BattleProxy battleProxy = getGameProxy().getProxy(ActorDefine.BATTLE_PROXY_NAME);
        PlayerTeam team = battleProxy.creatCountCapacityTeam(typeId, 1, post);
        double atk = (double) team.getValue(SoldierDefine.POWER_atk);
        double hp = (double) team.getValue(SoldierDefine.POWER_hp);
        double hitRate = (double) team.getValue(SoldierDefine.POWER_hitRate);
        double dodgeRate = (double) team.getValue(SoldierDefine.POWER_dodgeRate);
        double critRate = (double) team.getValue(SoldierDefine.POWER_critRate);
        double defRate = (double) team.getValue(SoldierDefine.POWER_defRate);
        double wreck = (double) team.getValue(SoldierDefine.POWER_wreck);
        double defend = (double) team.getValue(SoldierDefine.POWER_defend);
        actC = atk * 0.5 * soldierTypeAdd;
        hpC = hp * 0.1;
        hitRateC = hitRate / 10000.0 * 100;
        dodgeRateC = dodgeRate / 10000.0 * 100;
        critRateC = critRate / 10000.0 * 100;
        defRateC = defRate / 10000.0 * 100;
        wreckC = wreck / 100.0 * 10;
        defendC = defend / 100.0 * 10;
        cap = (int) ((actC + hpC + hitRateC + dodgeRateC + critRateC + defRateC + wreckC + defendC) * num);
        return cap;
    }

    public M6.DungeoInfo getDungeoInfo(int dungeoId) {
        M6.DungeoInfo.Builder dungeoInfo = M6.DungeoInfo.newBuilder();
        dungeoInfo.setId(dungeoId);
        dungeoInfo.setStar(getTotalStarNum(dungeoId));
        JSONObject dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
        int totalStar = dungeoDefine.getInt("starNum");
        dungeoInfo.setTotalStar(totalStar);
        List<Integer> boxList = getDungeoBoxByDungeoId(dungeoId);
        if (boxList.size() > 0) {
            dungeoInfo.setHaveBox(1);
        } else {
            dungeoInfo.setHaveBox(0);
        }
        dungeoInfo.setLen(-1);
        dungeoInfo.setCount(-1);
        dungeoInfo.setTotalCount(-1);
        return dungeoInfo.build();
    }

    public List<M6.DungeoInfo> getRistDungeoInfo() {
        List<M6.DungeoInfo> res = new ArrayList<>();
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
        for (JSONObject define : list) {
            int dungeoId = define.getInt("ID");
            M6.DungeoInfo.Builder builder = M6.DungeoInfo.newBuilder();
            builder.setId(dungeoId);
            if (dungeoId == DungeonDefine.EXTREME_ADVENTRUE) {
                builder.setLen(getDungeoEventPassNum(dungeoId));
                builder.setCount(-1);
                builder.setTotalCount(-1);
                builder.setHaveBox(0);
                builder.setStar(-1);
                builder.setTotalStar(-1);
            } else {
                //在计数器里面补充次数time
                // TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                // builder.setCount(timerdbProxy.getAdventureTimesById(dungeoId));
                builder.setCount(getRistChangeTimes(define.getInt("type")));
                builder.setTotalCount(define.getInt("time"));
                builder.setLen(-1);
                List<Integer> boxList = getRistDungeoBoxByDungeoId(dungeoId);
                if (boxList.size() > 0) {
                    builder.setHaveBox(1);
                } else {
                    builder.setHaveBox(0);
                }
                builder.setStar(getTotalStarNum(dungeoId));
                builder.setTotalStar(define.getInt("starNum"));
            }
            res.add(builder.build());
        }
        return res;
    }


    public M6.dungeonlist getdungeonlist() {
        M6.dungeonlist.Builder builder = M6.dungeonlist.newBuilder();
        List<M6.DungeoInfo> res = new ArrayList<>();
        for (Integer dungeoId : getAllDungeoId()) {
            M6.DungeoInfo info = getDungeoInfo(dungeoId);
            res.add(info);
        }
        SortUtil.anyProperSort(res, "getId", true);
        builder.addAllDungeoInfos(res);
        builder.addAllDungeoExplore(getRistDungeoInfo());
        int maxOrder = getHighestDungeoOrder();
        JSONObject nextDungeo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DUNGEO, "sort", maxOrder + 1);
        if (nextDungeo == null) {
            builder.setIsPassAll(1);
        } else {
            builder.setIsPassAll(0);
        }
        return builder.build();
    }


    public int getHighestDungeoOrder() {
        int maxOrder = 0;
        for (Dungeo dungeo : dungeos) {
            int dungeoId = dungeo.getDungeoId();
            JSONObject dungeoDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
            int order = dungeoDefine.getInt("sort");
            if (order > maxOrder) {
                maxOrder = order;
            }
        }
        return maxOrder;
    }

    /**
     * 打开副本宝箱
     **/
    public int openDungeoBox(int dungeoId, int box, PlayerReward reward) {
        Dungeo dungeo = getDungeoById(dungeoId);
        if (dungeo == null) {
            return ErrorCodeDefine.M60003_1;
        }
        if (dungeo.getGetBox().contains((long) box) == false) {
            return ErrorCodeDefine.M60003_2;
        }
        JSONObject dungeonDefine;
        if (dungeoId >= ActorDefine.MIN_DUNGEO_ID) {
            dungeonDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.DUNGEO, dungeoId);
        } else {
            dungeonDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, dungeoId);
        }
        JSONArray rewards = dungeonDefine.getJSONArray("rewardId" + box);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int i = 0; i < rewards.length(); i++) {
            int rewardId = rewards.getInt(i);
            rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
        }
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_OPEN_DUNGON_BOX);
        dungeo.getGetBox().remove((long) box);
        pushDungeoToChangeList(dungeo);
        sendFunctionLog(FunctionIdDefine.OPEN_DUNGEO_BOX_FUNCTION_ID, dungeoId, box, 0);
        return 0;
    }

    /**
     * 打开军团副本宝箱
     */
    public int openArmygroupDungeoBox(int dungeoId, PlayerReward reward) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerProxy.getPlayer().getGetbox().contains(dungeoId)){
            return ErrorCodeDefine.M270003_1;
        }else{
        JSONObject json =  ConfigDataProxy.getConfigInfoFindById(DataDefine.LegionEvent, dungeoId);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        rewardProxy.getPlayerRewardByFixReward(json.getInt("dropid"), reward);
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_LEGION_DUNGEO);
        List<Integer> boxlist =  playerProxy.getPlayer().getGetbox();
        boxlist.add(dungeoId);
        playerProxy.getPlayer().setGetbox(boxlist);
        return 0;
        }
    }

    /**
     * 购买冒险次数
     */
    public int buyAdvanceTimes(int id) {
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int actype = 0;
        if (id == BattleDefine.ADVANTRUE_TYPE_EQUIP) {
            actype = ActivityDefine.ACTIVITY_CONDITION_ORDANCE_ADVACE_BUYTIMES;
        }
        if (id == BattleDefine.ADVANTRUE_TYPE_ORNDANCE) {
            actype = ActivityDefine.ACTIVITY_CONDITION_EQUIP_BUYADDVACES_RETURN_GOLD;
        }
        // TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //timerdbProxy.addTimer(TimerDefine.BUY_ADVANCE_TIMES, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, id, 0, playerProxy);
        int resetTimes = vipProxy.getVipNum(ActorDefine.VIP_FITRESET);//可买次数
        int resetedNum = getRistHasbuyTimes(id);// timerdbProxy.getTimerNum(TimerDefine.BUY_ADVANCE_TIMES, id, 0);//已买了次数
        int maxVIPLv = vipProxy.getMaxVIPLv();
        JSONObject info = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", maxVIPLv);
        int maxBuy = 0;
        if (info != null) {
            maxBuy = info.getInt(ActorDefine.VIP_FITRESET);
        }
        int needGold = 0;
        if (resetedNum >= 5) {
            needGold = DungeonDefine.BUY_ADVANCE_TIMES_FIX_EXPEND;
        } else {
            if (resetedNum <= 0) {
                needGold = DungeonDefine.BUY_ADVANCE_TIMES_EXPEND;
            } else {
                needGold = DungeonDefine.BUY_ADVANCE_TIMES_EXPEND + (resetedNum * DungeonDefine.BUY_ADVANCE_TIMES_EXPEND_ADD);
            }
        }
        needGold = (int) (needGold * (100 - activityProxy.getEffectBufferPowerByType(actype)) / 100.0);
        long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
        if (resetedNum == maxBuy) {
            return ErrorCodeDefine.M60004_2;
        }
        if (resetTimes <= 0) {
            return ErrorCodeDefine.M60004_1;
        }
        if (resetedNum >= resetTimes) {
            return ErrorCodeDefine.M60004_1;
        }
        if (hadGold < needGold) {
            return 2;
        }

        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, needGold, LogDefine.LOST_RISK_TIMES);
        // timerdbProxy.addAdvanceTiems(id, DungeonDefine.ADVANCE_TIMES);
        //  timerdbProxy.addNum(TimerDefine.BUY_ADVANCE_TIMES, id, 0, 1);
        addRistbuyTimes(id,1);
        addRistChangeTimes(id, DungeonDefine.ADVANCE_TIMES);
        sendFunctionLog(FunctionIdDefine.BUY_ADVANCE_TIMES_FUNCTION_ID, needGold, id, 0);
        return 0;
    }

    /**
     * 返回购买冒险次数的金币
     *
     * @return
     */
    public int needGold(int dungeoId) {
        //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        // timerdbProxy.addTimer(TimerDefine.BUY_ADVANCE_TIMES, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, dungeoId, 0, playerProxy);
        int resetedNum = getRistHasbuyTimes(dungeoId); //timerdbProxy.getTimerNum(TimerDefine.BUY_ADVANCE_TIMES, dungeoId, 0);//已买了次数
        int needGold = 0;
        if (resetedNum >= 5) {
            needGold = DungeonDefine.BUY_ADVANCE_TIMES_FIX_EXPEND;
        } else {
            if (resetedNum <= 0) {
                needGold = DungeonDefine.BUY_ADVANCE_TIMES_EXPEND;
            } else {
                needGold = DungeonDefine.BUY_ADVANCE_TIMES_EXPEND + (resetedNum * DungeonDefine.BUY_ADVANCE_TIMES_EXPEND_ADD);
            }
        }
        return needGold;
    }

    /**
     * 请求购买冒险次数
     */
    public int askBuyTimes(int id) {
        //TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //   timerdbProxy.addTimer(TimerDefine.BUY_ADVANCE_TIMES, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, id, 0, playerProxy);
        int resetTimes = vipProxy.getVipNum(ActorDefine.VIP_FITRESET);//可买次数

        int resetedNum = getRistHasbuyTimes(id) ;// timerdbProxy.getTimerNum(TimerDefine.BUY_ADVANCE_TIMES, id, 0);//已买了次数
        int maxVIPLv = vipProxy.getMaxVIPLv();
        JSONObject info = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", maxVIPLv);
        int maxBuy = 0;
        if (info != null) {
            maxBuy = info.getInt(ActorDefine.VIP_FITRESET);
        }
        int needGold = 0;
        if (resetedNum >= 5) {
            needGold = DungeonDefine.BUY_ADVANCE_TIMES_FIX_EXPEND;
        } else {
            if (resetedNum <= 0) {
                needGold = DungeonDefine.BUY_ADVANCE_TIMES_EXPEND;
            } else {
                needGold = DungeonDefine.BUY_ADVANCE_TIMES_EXPEND + (resetedNum * DungeonDefine.BUY_ADVANCE_TIMES_EXPEND_ADD);
            }
        }
        long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
        if (resetedNum == maxBuy) {
            return ErrorCodeDefine.M60004_2;
        }
        if (resetTimes <= 0) {
            return ErrorCodeDefine.M60004_1;
        }
        if (resetedNum >= resetTimes) {
            return ErrorCodeDefine.M60004_1;
        }
        if (hadGold < needGold) {
            return 2;
        }

        return 0;
    }

    /*获取关卡怪物属性*/
    public M6.LimitMonsterInfo getLimitMonsterInfo(int monsterId, int num, int post) {
        M6.LimitMonsterInfo.Builder builder = M6.LimitMonsterInfo.newBuilder();
        builder.setTypeid(monsterId);
        builder.setPost(post);
        builder.setNum(num);
        return builder.build();
    }


    public int limitRest() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //   TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (DungeonDefine.DEOGEO_LIMIT_REST - playerProxy.getPlayer().getDungeolimitrest() <= 0) {
            return ErrorCodeDefine.M60101_1;
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy) < 5) {
            return ErrorCodeDefine.M60101_2;
        }
        playerProxy.getPlayer().setGetLimitChangeId(1);
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_energy, 5, LogDefine.LOST_LIMIT_CHANGE);
        //   timerdbProxy.addNum(TimerDefine.LIMIT_CHANGET_REST, 0, 0, 1);
        playerProxy.getPlayer().setDungeolimitrest(1);
        // timerdbProxy.setNum(TimerDefine.LIMIT_CHANGET_TIMES, 0, 0, 0);
        playerProxy.getPlayer().setDungeolimitchange(0);
        // timerdbProxy.setNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, 0);
        playerProxy.getPlayer().setDungeolimitmoptimes(0);
        return 0;
    }

    public int checkmopp() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int max = playerProxy.getPlayer().getLimitChangeMaxId();
        int num = playerProxy.getPlayer().getDungeolimitmoptimes();//timerdbProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        if (num == 0) {
            return 0;
        }
        long time = (playerProxy.getPlayer().getDungeolimitmop() - GameUtils.getServerDate().getTime()) / 1000;// (timerdbProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0) - GameUtils.getServerDate().getTime()) / 1000;
        if (time > 0) {
            int checktimes = (int) time / 30;
            if ((int) time % 30 > 0) {
                checktimes += 1;
            }
            playerProxy.getPlayer().setGetLimitChangeId(max - checktimes);
        } else {
            playerProxy.getPlayer().setGetLimitChangeId(max);
            return 0;
        }
        return 1;
    }

    //获得扫荡的reward
    public void getMopReward(PlayerReward reward) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        int nowId = playerProxy.getPlayer().getGetLimitChangeId();
        int num = playerProxy.getPlayer().getDungeolimitmoptimes();// timerdbProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        for (; num < nowId; num++) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.ADVENTURE_EVENT, "chapter", 4, "sort", num);
            JSONArray fixjson = jsonObject.getJSONArray("fixdrop");
            for (int i = 0; i < fixjson.length(); i++) {
                rewardProxy.getPlayerRewardByFixReward(fixjson.getInt(i), reward);
            }
            JSONArray ratehson = jsonObject.getJSONArray("ratedrop");
            for (int i = 0; i < ratehson.length(); i++) {
                rewardProxy.getPlayerRewardByRandFullContent(ratehson.getInt(i), reward);
            }
        }
    }

    public List<Integer[]> rewardtoList(PlayerReward reward) {
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        List<Integer[]> list = new ArrayList<Integer[]>();
        List<Common.RewardInfo> infos = new ArrayList<Common.RewardInfo>();
        rewardProxy.getRewardInfoByReward(reward, infos);
        for (Common.RewardInfo info : infos) {
            list.add(new Integer[]{info.getPower(), info.getTypeid(), info.getNum()});
        }
        return list;
    }

    public int startMop() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        // TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int max = playerProxy.getPlayer().getLimitChangeMaxId();
        int nowId = playerProxy.getPlayer().getGetLimitChangeId();
        long time = (playerProxy.getPlayer().getDungeolimitmop() - GameUtils.getServerDate().getTime()) / 1000;//(timerdbProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0) - GameUtils.getServerDate().getTime()) / 1000;
        if (time > 0) {
            return ErrorCodeDefine.M60102_1;
        }
        if (playerProxy.getPlayer().getGetLimitChangeId() == playerProxy.getPlayer().getLimitChangeMaxId()) {
            return ErrorCodeDefine.M60102_2;
        }
        //  timerdbProxy.setNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, nowId);
        playerProxy.getPlayer().setDungeolimitmoptimes(nowId);
        int needtime = (max - nowId) * 30 * 1000;
        //    timerdbProxy.setLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, GameUtils.getServerDate().getTime() + needtime);
        playerProxy.getPlayer().setDungeolimitmop(GameUtils.getServerDate().getTime() + needtime);
        return 0;
    }


    public int stopMop(PlayerReward reward) {
        //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int num = playerProxy.getPlayer().getDungeolimitmoptimes();// timerdbProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (num == 0) {
            return ErrorCodeDefine.M60103_1;
        }
        long time = (playerProxy.getPlayer().getDungeolimitmop() - GameUtils.getServerDate().getTime()) / 1000;// (timerdbProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0) - GameUtils.getServerDate().getTime()) / 1000;
        if (time < 0) {
            return ErrorCodeDefine.M60103_1;
        }
        getMopReward(reward);
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_LIMITCHANGE_MOP);
        playerProxy.getPlayer().setDungeolimitmop(GameUtils.getServerDate().getTime());
        playerProxy.getPlayer().setDungeolimitmoptimes(0);
        //   timerdbProxy.setNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, 0);
        //  timerdbProxy.setLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, GameUtils.getServerDate().getTime());
        return 0;
    }

    public boolean isPass(PlayerBattle battle, JSONObject jsonObject) {
        if (jsonObject == null) {
            return true;
        }
        JSONArray jsonArray = jsonObject.getJSONArray("passneed");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int type = array.getInt(0);
            int value = array.getInt(1);
            if (type == 101) {
                if (getenemyHit(battle) > value) {
                    return false;
                }
            }
            if (type == 102) {
                if (getIndexExitnum(battle, value) <= 0) {
                    return false;
                }
            }
            if (type == 103) {
                if (battle.roundCount > value) {
                    return false;
                }
            }
            if (type == 104) {
                if (getLostSoldier(battle) > value) {
                    return false;
                }
            }
        }
        return true;
    }

    //获得损兵比例
    public int getLostSoldier(PlayerBattle battle) {
        double after = 0;
        double before = 0;
        for (PlayerTeam team : battle.soldierList) {
            after += (int) team.powerMap.get(SoldierDefine.NOR_POWER_NUM);
            before += (int) team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM);
        }
        double value = (before - after) / after;
        return (int) (value * 100);
    }

    //获得剩余
    public int getenemyHit(PlayerBattle battle) {
        int after = 0;
        for (PlayerTeam team : battle.monsterList) {
            after += (int) team.powerMap.get(SoldierDefine.NOR_POWER_NUM);
        }
        return after;
    }

    //获得存活兵
    public int getIndexExitnum(PlayerBattle battle, int index) {
        for (PlayerTeam team : battle.soldierList) {
            int num = (int) team.powerMap.get(SoldierDefine.NOR_POWER_NUM);
            int posi = (int) team.powerMap.get(SoldierDefine.NOR_POWER_INDEX);
            if (index == posi || index - 10 == posi) {
                return num;
            }
        }
        return 0;
    }


    //极限副本扫荡时间
    public M6.M60105.S2C getMopTimeInfo() {
        checkMop();
        M6.M60105.S2C.Builder builder = M6.M60105.S2C.newBuilder();
        builder.setRs(0);
        builder.setMoptime(getcheckMopTime());
        return builder.build();
    }


    //扫荡校验
    public int getcheckMopTime() {
        //TimerdbProxy timerProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        //    long lasttime = timerProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);

        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long lasttime = playerProxy.getPlayer().getDungeolimitmop();
        int num = playerProxy.getPlayer().getDungeolimitmoptimes();//timerProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        long time = lasttime - GameUtils.getServerDate().getTime();
        if (num >= 1) {
            long timenum = time / 30 / 1000;
            if (time < 0) {
                timenum = 0;
            }
            int maxid = playerProxy.getLimitChangeMaxId();
            playerProxy.setLimitChangeNowId((int) (maxid - timenum));
        }
        if (num != 0 && time <= 0) {
            DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            dungeoProxy.getMopReward(reward);
            List<Integer[]> list = dungeoProxy.rewardtoList(reward);
            MailTemplate template = new MailTemplate("扫荡奖励邮件", "扫荡奖励邮件", 0, "系统邮件", ChatAndMailDefine.MAIL_TYPE_SYSTEM);
            template.setAttachments(list);
            sendMailServiceMsg(new GameMsg.ReceiveMailNotice(template));
            playerProxy.getPlayer().setDungeolimitmoptimes(0);
        }
        if (time <= 0) {
            time = 0;
        }
        return (int) (time / 1000);
        // timerProxy.setLesTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, (int) time / 1000);
    }

    private void checkMop() {
        //  TimerdbProxy timerProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long lasttime = playerProxy.getPlayer().getDungeolimitmop();//timerProxy.getLastOperatinTime(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        int num = playerProxy.getPlayer().getDungeolimitmoptimes();//timerProxy.getTimerNum(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0);
        long time = lasttime - GameUtils.getServerDate().getTime();
        if (num != 1) {
            long timenum = time / 30 / 1000;
            if (time <= 0) {
                timenum = 0;
            }
            int maxid = playerProxy.getLimitChangeMaxId();
            playerProxy.setLimitChangeNowId((int) (maxid - timenum));
        }
    }

    public void checkActivityPlayerTeams(List<PlayerTeam> teamslist, int battletype, int evenId) {
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int type = 0;
        if (battletype == BattleDefine.BATTLE_TYPE_ADVANTRUE) {
            JSONObject eventDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE_EVENT, evenId);
            type = eventDefine.getInt("chapter");
        }
        if (type == BattleDefine.ADVANTRUE_TYPE_ORNDANCE) {
            for (PlayerTeam team : teamslist) {
                if (team.basePowerMap.get(SoldierDefine.POWER_pvpDamAdd) != null) {
                    int value = (activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_ORDANCE_DAMAGE_ADD));
                    int oldvalue = (int) team.basePowerMap.get(SoldierDefine.POWER_pvpDamAdd);
                    oldvalue = (int) Math.ceil(oldvalue * (100 + value) / 100.0);
                    team.basePowerMap.put(SoldierDefine.POWER_pvpDamAdd, oldvalue);
                }/*else{
                  team.basePowerMap.put(SoldierDefine.POWER_pvpDamAdd,activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_ORDANCE_DAMAGE_ADD));
              }*/
                team.init();
            }
        }
        if (type == BattleDefine.ADVANTRUE_TYPE_EQUIP) {
            for (PlayerTeam team : teamslist) {
                if (team.basePowerMap.get(SoldierDefine.POWER_pvpDamAdd) != null) {
                    int value = activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_EQUIP_DAMAGE_ADD);
                    int oldvalue = (int) team.basePowerMap.get(SoldierDefine.POWER_pvpDamAdd);
                    oldvalue = (int) Math.ceil(oldvalue * (100 + value) / 100.0);
                    team.basePowerMap.put(SoldierDefine.POWER_pvpDamAdd, oldvalue);
                }/*else{
                    team.basePowerMap.put(SoldierDefine.POWER_pvpDamAdd,activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_EQUIP_DAMAGE_ADD));
                }*/
                team.init();
            }
        }
    }

    //获得冒险副本剩余挑战次数
    public int getRistChangeTimes(int dungeoid) {
        Dungeo dungeo = getRistDungeoById(dungeoid);
        if (dungeo == null) {
            return 0;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ADVENTURE, "type", dungeoid);
        if (jsonObject == null) {
            return 0;
        }
        int addtimes=0;
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if(dungeoid==BattleDefine.ADVANTRUE_TYPE_EQUIP){
            addtimes = (activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_EUIP_ADVANCE_ADDTIMES));
        }else if(dungeoid==BattleDefine.ADVANTRUE_TYPE_ORNDANCE){
            addtimes = (activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_ORDANCE_ADVANCE_ADDTIMES));
        }
      return jsonObject.getInt("time")+addtimes-dungeo.getChangetimes();
    }

    //增加冒险副本挑战次数
    public void addRistChangeTimes(int dungeoid, int times) {
        Dungeo dungeo = getRistDungeoById(dungeoid);
        if (dungeo == null) {
            return ;
        }
       int hatimes=dungeo.getChangetimes();
        dungeo.setChangetimes(hatimes-times);
        pushDungeoToChangeList(dungeo);
    }

    //减少冒险副本挑战次数
    public void reduceRistChangeTimes(int dungeoid, int times) {
        Dungeo dungeo = getRistDungeoById(dungeoid);
        if (dungeo == null) {
            return ;
        }
        int hatimes=dungeo.getChangetimes();
        dungeo.setChangetimes(hatimes+times);
        pushDungeoToChangeList(dungeo);
    }

    //获得冒险副本剩余挑战购买次数
    public int getRistbuyTimes(int dungeoid) {
        Dungeo dungeo = getRistDungeoById(dungeoid);
        if (dungeo == null) {
            return 0;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ADVENTURE, "type", dungeoid);
        if (jsonObject == null) {
            return 0;
        }

        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int resetTimes = vipProxy.getVipNum(ActorDefine.VIP_FITRESET);//可买次数
        return resetTimes-dungeo.getBuytimes();
    }


    //获得冒险副本剩余挑战购买次数
    public int getRistHasbuyTimes(int dungeoid) {
        Dungeo dungeo = getRistDungeoById(dungeoid);
        if (dungeo == null) {
            return 0;
        }

        return dungeo.getBuytimes();
    }

    //增加冒险副本挑战购买次数
    public void addRistbuyTimes(int dungeoid, int times) {
        Dungeo dungeo = getRistDungeoById(dungeoid);
        if (dungeo == null) {
            return ;
        }
       int oldtimes=dungeo.getBuytimes();
        dungeo.setBuytimes(oldtimes+times);
        pushDungeoToChangeList(dungeo);
    }

    @Override
    public void fixedTimeEventHandler() {
        for (Dungeo dungeo : ristDungeos) {
            dungeo.setChangetimes(0);
            dungeo.setBuytimes(0);
            dungeo.save();
        }
    }
}
