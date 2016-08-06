package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.core.SimplePlayer;
import com.znl.define.*;
import com.znl.pojo.db.*;
import com.znl.proto.M22;
import com.znl.service.PlayerService;
import com.znl.utils.GameUtils;
import com.znl.utils.RandomUtil;
import com.znl.utils.SortUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class ArmyGroupProxy extends BasicProxy {

    public LegionTimer legionTimer;

    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }


    public ArmyGroupProxy(Player playr, String areaKey) {
        this.areaKey = areaKey;
        legionTimer = BaseDbPojo.get(playr.getLegionTimerId(), LegionTimer.class, areaKey);
        if (legionTimer == null) {
            legionTimer = BaseDbPojo.create(LegionTimer.class, areaKey);
            playr.setLegionTimerId(legionTimer.getId());
            legionTimer.setPlayerId(playr.getId());
            legionTimer.save();
        }
    }

    public void saveDate() {
        legionTimer.save();
    }

    public void clearTechPlayerPower() {
        expandPowerMap.clear();
    }

    /**
     * 军团科技属性效果加成
     */
    public void addTechPlayerPower(Map<Integer, Long> techExpandPower) {
        expandPowerMap.clear();
        for (Integer id : techExpandPower.keySet()) {
            long value = techExpandPower.get(id);
            if (super.expandPowerMap.get(id) == null) {
                super.expandPowerMap.put(id, value);
            } else {
                super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
            }
        }
      refurceExpandPowerMap();
    }

    //创建军团
    public long createArmyGroup(int way, String name, int jointype, Map<Long, Armygroup> map) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        StringBuffer sb = new StringBuffer();
        if (way == 1) {
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < ArmyGroupDefine.CRETE_COST_GOLD) {
                return ErrorCodeDefine.M220103_1;//金币不足
            }
        } else {
            for (int power : ArmyGroupDefine.mapneed.keySet()) {
                if (playerProxy.getPowerValue(power) < ArmyGroupDefine.mapneed.get(power)) {
                    if (power == 201) {
                        return ErrorCodeDefine.M220103_2;//资源不足
                    }
                    if (power == 202) {
                        return ErrorCodeDefine.M220103_8;//资源不足
                    }
                    if (power == 203) {
                        return ErrorCodeDefine.M220103_9;//资源不足
                    }
                    if (power == 204) {
                        return ErrorCodeDefine.M220103_10;//资源不足
                    }
                    if (power == 205) {
                        return ErrorCodeDefine.M220103_11;//资源不足
                    }
                }
            }
        }
        if (ishasSameName(map, name)) {
            return ErrorCodeDefine.M220103_3;//名字已经存在
        }
        if (playerProxy.getLevel() < 12) {
            return ErrorCodeDefine.M220103_6;//
        }

        //判断字符合法
        if (!playerProxy.checkString(name)) {
            return ErrorCodeDefine.M220103_12;//军团名称非法
        }
        if (String_length(name) < 4 || String_length(name) > 12) {
            return ErrorCodeDefine.M220103_4;//字符不合法
        }
        if (playerProxy.getArmGrouId() > 0) {
            return ErrorCodeDefine.M220103_5;//已经拥有军团了
        }

        if (way == 1) {
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, ArmyGroupDefine.CRETE_COST_GOLD, LogDefine.LOST_CREATE_ARMY);
            sb.append(way);
            sb.append(",");
            sb.append(ArmyGroupDefine.CRETE_COST_GOLD);
        } else {
            for (int power : ArmyGroupDefine.mapneed.keySet()) {
                playerProxy.reducePowerValue(power, ArmyGroupDefine.mapneed.get(power), LogDefine.LOST_CREATE_ARMY);
                sb.append(ArmyGroupDefine.mapneed.get(power));
                sb.append(",");
            }
        }


        Armygroup armygroup = BaseDbPojo.create(Armygroup.class, areaKey);
        armygroup.setBuild(0);
        armygroup.setApplymenbers(new LinkedHashSet<Long>());
        armygroup.setCommander(playerProxy.getPlayerName());
        armygroup.setCommandid(playerProxy.getPlayerId());
        armygroup.setLevel(1);
        armygroup.setConditoncapity(0);
        armygroup.setConditonlevel(0);
        armygroup.setJoinWay(jointype);
        armygroup.setName(name);
        armygroup.setNotice("");
        armygroup.setSciencelevel(1);
        armygroup.setWelfarelevel(1);
        Set<Long> menbers = new LinkedHashSet<Long>();
        menbers.add(playerProxy.getPlayerId());
        armygroup.setMenbers(menbers);
        ArmygroupMenber armygroupMenber = BaseDbPojo.create(ArmygroupMenber.class, areaKey);
        armygroupMenber.setAccoutName(playerProxy.getAccountName());
        armygroupMenber.setActivityrank(1);
        armygroupMenber.setArmyId(armygroup.getId());
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        armygroupMenber.setCapity(soldierProxy.getHighestCapacity());
        armygroupMenber.setCapityrank(1);
        armygroupMenber.setContribute(0);
        armygroupMenber.setActivitycontributerank(0);
        armygroupMenber.setDevotrank(1);
        armygroupMenber.setDonatecontributeWeek(0);
        armygroupMenber.setFood(0);
        armygroupMenber.setIron(0);
        armygroupMenber.setName(playerProxy.getPlayerName());
        armygroupMenber.setSex(playerProxy.getPlayer().getSex());
        armygroupMenber.setJob(ArmyGroupDefine.JOB_MANGER);
        armygroupMenber.setPlayerId(playerProxy.getPlayerId());
        armygroupMenber.setLogintime(GameUtils.getServerDate().getTime());
        armygroupMenber.setOutlinetime(GameUtils.getServerDate().getTime() - 1000);
        armygroupMenber.setLevel(playerProxy.getLevel());
        armygroupMenber.setPendantId(playerProxy.getPlayer().getPendant());
        armygroupMenber.setIcon(playerProxy.getPlayer().getIcon());
        armygroupMenber.save();
        Set<Long> armmenbers = new LinkedHashSet<Long>();
        armmenbers.add(armygroupMenber.getId());
        armygroup.setArmmenbers(armmenbers);
        armygroup.setCapity(soldierProxy.getHighestCapacity());
        armygroup.save();
        //TODO 商城
        playerProxy.setArmgroupId(armygroup.getId());
        playerProxy.setLegionName(armygroup.getName());
        playerProxy.setPost(ArmyGroupDefine.JOB_MANGER);
        playerProxy.savePlayer();
        sendFunctionLog(FunctionIdDefine.CREATE_LEGION_FUNCTION_ID, armygroup.getId(), 0, 0, sb.toString());
        if (armygroup.getId() > 0) {
            int value = 1;
            ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION, value, playerProxy, 0);
        }
        return armygroup.getId();
    }

    //是否重名
    public boolean ishasSameName(Map<Long, Armygroup> map, String name) {
        for (Armygroup armyGroupTemp : map.values()) {
            if (name.equals(armyGroupTemp.getName())) {
                return true;
            }
        }
        return false;
    }


    //获得军团随机商城所有人购买次数
    public int getArmyRandouShopBuyTimes(Armygroup armygroup, int id) {
        String list = armygroup.getGetlist();
        if (list == null || list.equals("")) {
            return 0;
        }
        for (String str : list.split(";")) {
            if (!str.equals("")) {
                int armytype = Integer.parseInt(str.split("_")[0]);
                int times = Integer.parseInt(str.split("_")[1]);
                if (armytype == id) {
                    return times;
                }
            }
        }
        return 0;
    }


    //设置军团随机商城所有人购买次数
    public void setArmyRandouShopBuyTimes(Armygroup armygroup, int id) {
        String list = armygroup.getGetlist();
        if (list == null || list.equals("")) {
            String str = id + "_" + 1;
            list = str;
            armygroup.setGetlist(list);
            return;
        }
        boolean falg = false;
        int times = 0;
        String all = "";
        for (String str : list.split(";")) {
            if (!"".equals(str)) {
                int armytype = Integer.parseInt(str.split("_")[0]);
                times = Integer.parseInt(str.split("_")[1]);
                if (armytype == id) {
                    all += ";" + armytype + "_" + (times + 1);
                    falg = true;
                } else {
                    all += ";" + str;
                }
            }
        }
        if (falg == false) {
            String str = ";" + id + "_" + 1;
            all += str;
        }
        armygroup.setGetlist(all);
    }


    //获得玩家某个随机商品购买次数
    public int getPlayerRandouShopBuyTimes(Armygroup armygroup, int id, long playerId) {
        String list = armygroup.getPlayerrandomBuytimes();
        if (list == null || "".equals(list)) {
            return 0;
        }
        for (String str : list.split(";")) {
            if (!"".equals(str)) {
                long shopPlayerId = Long.parseLong(str.split("_")[0]);
                int shopid = Integer.parseInt(str.split("_")[1]);
                int times = Integer.parseInt(str.split("_")[2]);
                if (shopPlayerId == playerId && id == shopid) {
                    return times;
                }
            }
        }
        return 0;
    }


    //设置玩家某个随机商品购买次数
    public void setArmyRandouShopBuyTimes(Armygroup armygroup, int type, long playerId) {
        String list = armygroup.getPlayerrandomBuytimes();
        if (list == null || "".equals(list)) {
            list = playerId + "_" + type + "_" + 1;
            armygroup.setPlayerrandomBuytimes(list);
            return;
        }
        String all = "";
        int times = 0;
        boolean falg = false;
        for (String str : list.split(";")) {
            if (!"".equals(str)) {
                long shopPlayer = Integer.parseInt(str.split("_")[0]);
                int shopid = Integer.parseInt(str.split("_")[1]);
                times = Integer.parseInt(str.split("_")[2]);
                if (shopPlayer == playerId && type == shopid) {
                    all += ";" + shopPlayer + "_" + shopid + (times + 1);
                    falg = true;
                } else {
                    all += ";" + str;
                }
            }
        }
        if (falg == false) {
            String str = ";" + playerId + "_" + type + "_" + 1;
            all += str;
        }
        armygroup.setPlayerrandomBuytimes(all);
    }

    private int getshopBuyTimes(int id) {
        int num = 0;
        for (int temp : legionTimer.getIds()) {
            if (temp == id) {
                num++;
            }
        }
        return num;
    }

    private void addShopBuyid(int id){
        List<Integer> ids=legionTimer.getIds();
        ids.add(id);
        legionTimer.setIds(ids);
    }

    /**
     * 军团商店,贡献值兑换物品(固定)
     */
    public int exchangeItem(ArmygroupMenber armygroupMenber, Armygroup armygroup, int id, PlayerReward reward) {
        JSONObject obj = ConfigDataProxy.getConfigInfoFindById(DataDefine.LEGION_FIX_SHOP, id);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        //   timerdbProxy.addTimer(TimerDefine.ARMYGROUP_SHOP, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, id, 0,playerProxy);
        int exchangeNum = getshopBuyTimes(id);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_SHOP, id, 0);
        if (obj != null) {
            if (armygroupMenber.getContribute() < obj.getInt("contributeneed")) {
                return ErrorCodeDefine.M220002_1;
            } else if (exchangeNum >= obj.getInt("exchangemax")) {
                return ErrorCodeDefine.M220002_3;
            } else if (armygroup.getLevel() < obj.getInt("legionlv")) {
                return ErrorCodeDefine.M220002_6;
            }
            int itemId = obj.getInt("typeID");
            int num = obj.getInt("num");
            int needContribute = obj.getInt("contributeneed");
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            if (obj.getInt("type") == PlayerPowerDefine.BIG_POWER_ITEM) {
                ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
                itemProxy.addItem(itemId, num, LogDefine.GET_ARMYGROUP_SHOP);
                rewardProxy.getRewardContent(reward, PlayerPowerDefine.BIG_POWER_ITEM, itemId, num);
                //     timerdbProxy.addNum(TimerDefine.ARMYGROUP_SHOP, id, 0, 1);
                addShopBuyid(id);
            } else if (obj.getInt("type") == PlayerPowerDefine.BIG_POWER_GENERAL) {
                EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
                equipProxy.addEquip(itemId, LogDefine.GET_ARMYGROUP_SHOP);
                rewardProxy.getRewardContent(reward, PlayerPowerDefine.BIG_POWER_GENERAL, itemId, num);
                addShopBuyid(id);
                //    timerdbProxy.addNum(TimerDefine.ARMYGROUP_SHOP, id, 0, 1);
            }
            armygroupMenber.setContribute(armygroupMenber.getContribute() - needContribute);
            sendFunctionLog(FunctionIdDefine.LEGION_SHOP_GOODS_EXCHANGE_FUNCTION_ID, itemId, needContribute, playerProxy.getPlayerId());
        }
        return 0;
    }

    /**
     * 军团商店,贡献值兑换珍品(不固定)
     */
    public int exchangeGemItem(ArmygroupMenber armygroupMenber, Armygroup armygroup, int id, PlayerReward reward) {
        JSONObject obj = ConfigDataProxy.getConfigInfoFindById(DataDefine.LEGION_RANDOM_SHOP, id);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int exchangeNum = getPlayerRandouShopBuyTimes(armygroup, id, playerProxy.getPlayerId());
        int legexchamax = getArmyRandouShopBuyTimes(armygroup, id);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (obj != null) {
            if (armygroupMenber.getContribute() < obj.getInt("contributeneed")) {
                return ErrorCodeDefine.M220002_1;
            } else if (exchangeNum >= obj.getInt("exchangemax")) {
                return ErrorCodeDefine.M220002_3;
            } else if (legexchamax >= obj.getInt("legexchamax")) {
                return ErrorCodeDefine.M220002_4;
            } else if (armygroup.getRandomShops().contains(id) == false) {
                return ErrorCodeDefine.M220002_5;
            } else if (armygroup.getLevel() < obj.getInt("legionlv")) {
                return ErrorCodeDefine.M220002_6;
            }

            int num = obj.getInt("num");
            int itemId = obj.getInt("typeID");
            int needContribute = obj.getInt("contributeneed");
            if (obj.getInt("type") == PlayerPowerDefine.BIG_POWER_GENERAL) {
                EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
                equipProxy.addEquip(itemId, LogDefine.GET_ARMYGROUP_SHOP);
                rewardProxy.getRewardContent(reward, PlayerPowerDefine.BIG_POWER_GENERAL, itemId, num);
            } else if (obj.getInt("type") == PlayerPowerDefine.BIG_POWER_ITEM) {
                ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
                itemProxy.addItem(itemId, num, LogDefine.GET_ARMYGROUP_SHOP);
                rewardProxy.getRewardContent(reward, PlayerPowerDefine.BIG_POWER_ITEM, itemId, num);
            } else if (obj.getInt("type") == PlayerPowerDefine.BIG_POWER_SOLDIER) {
                SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                soldierProxy.addSoldierNum(itemId, num, LogDefine.GET_ARMYGROUP_SHOP);
                rewardProxy.getRewardContent(reward, PlayerPowerDefine.BIG_POWER_SOLDIER, itemId, num);
            }
            setArmyRandouShopBuyTimes(armygroup, id);//军团兑换上限
            setArmyRandouShopBuyTimes(armygroup, id, playerProxy.getPlayerId());//个人兑换上限
            armygroupMenber.setContribute(armygroupMenber.getContribute() - needContribute);
        }
        return 0;
    }

    //获得军团某个类型的捐赠次数
    public int gethallDonateTimes(int power){
        int num=0;
        for(int id:legionTimer.getHalldontes()){
            if(power==id){
                num++;
            }
        }
        return num;
    }

    //增加某个类型军团捐赠次数
    public void addhallDonateTimes(int power){
        List<Integer> ids=legionTimer.getHalldontes();
        ids.add(power);
        legionTimer.setHalldontes(ids);
    }


    //获得科技某个类型的捐赠次数
    public int getScienceDonateTimes(int power){
        int num=0;
        for(int id:legionTimer.getSciencedontes()){
            if(power==id){
                num++;
            }
        }
        return num;
    }

    //增加某个类型科技捐赠次数
    public void addScienceDonateTimes(int power){
        List<Integer> ids=legionTimer.getSciencedontes();
        ids.add(power);
        legionTimer.setSciencedontes(ids);
    }


    /**
     * 军团商店：物品显示
     */
    public List<M22.ArmyShopInfo> showArmyShopItem(Armygroup armygroup) {
        List<M22.ArmyShopInfo> itemIdList = new ArrayList<M22.ArmyShopInfo>();
        M22.ArmyShopInfo.Builder armyShop = M22.ArmyShopInfo.newBuilder();
        List<JSONObject> objList = ConfigDataProxy.getConfigAllInfo(DataDefine.LEGION_FIX_SHOP);
        //   TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int armyLv = armygroup.getLevel();
        if (objList.size() > 0) {
            for (JSONObject obj : objList) {
                if (armyLv >= obj.getInt("legionlv")) {
                    armyShop.setCanGetId(obj.getInt("ID"));
                    int exchangeNum = getshopBuyTimes(obj.getInt("ID"));// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_SHOP, obj.getInt("ID"), 0);
                    if (exchangeNum < 0) {
                        exchangeNum = 0;
                    }
                    armyShop.setNum(exchangeNum);
                    itemIdList.add(armyShop.build());
                }
            }
        }
        return itemIdList;
    }

    /**
     * 军团商店：珍品显示
     */
    public List<M22.ArmyShopInfo> showArmyShopGemItem(Armygroup armygroup) {
        List<Integer> list = armygroup.getRandomShops();
        List<M22.ArmyShopInfo> itemIdList = new ArrayList<M22.ArmyShopInfo>();
        M22.ArmyShopInfo.Builder armyShop = M22.ArmyShopInfo.newBuilder();
        for (Integer id : list) {
            int num = getArmyRandouShopBuyTimes(armygroup, id);
            armyShop.setCanGetId(id);
            armyShop.setNum(num);
            itemIdList.add(armyShop.build());
            System.err.println(" * 军团商店：珍品显示:Id:" + id + ",num:" + num);
        }
        return itemIdList;
    }

    /**
     * 科技捐献资源次数
     */
    public List<M22.ResInfo> resContributeNum() {
        List<M22.ResInfo> buildList = new ArrayList<M22.ResInfo>();
        // TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int goldNum = getScienceDonateTimes(200);//timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, 200, ArmyGroupDefine.CONTRUBUTE_TECH);
        M22.ResInfo.Builder builder = M22.ResInfo.newBuilder();
        builder.setResType(200);
          if (goldNum == 0) {
             goldNum = 1;
            }
            builder.setCurCount(goldNum);
        buildList.add(builder.build());
        int taelNum = getScienceDonateTimes(PlayerPowerDefine.POWER_tael);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, PlayerPowerDefine.POWER_tael, ArmyGroupDefine.CONTRUBUTE_TECH);
        M22.ResInfo.Builder builder1 = M22.ResInfo.newBuilder();
        builder1.setResType(PlayerPowerDefine.POWER_tael);
        if (taelNum == 0) {
            taelNum = 1;
        }
        builder1.setCurCount(taelNum);
        buildList.add(builder1.build());
        int ironNum = getScienceDonateTimes(PlayerPowerDefine.POWER_iron);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, PlayerPowerDefine.POWER_iron, ArmyGroupDefine.CONTRUBUTE_TECH);
        M22.ResInfo.Builder builder2 = M22.ResInfo.newBuilder();
        builder2.setResType(PlayerPowerDefine.POWER_iron);
        if (ironNum == 0) {
            ironNum = 1;
        }
        builder2.setCurCount(ironNum);
        buildList.add(builder2.build());
        int woodNum = getScienceDonateTimes(PlayerPowerDefine.POWER_wood);//timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, PlayerPowerDefine.POWER_wood, ArmyGroupDefine.CONTRUBUTE_TECH);
        M22.ResInfo.Builder builder4 = M22.ResInfo.newBuilder();
        builder4.setResType(PlayerPowerDefine.POWER_wood);
        if (woodNum == 0) {
            woodNum = 1;
        }
        builder4.setCurCount(woodNum);
        buildList.add(builder4.build());
        int stoneNum =getScienceDonateTimes( PlayerPowerDefine.POWER_stones);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, PlayerPowerDefine.POWER_stones, ArmyGroupDefine.CONTRUBUTE_TECH);
        M22.ResInfo.Builder builder3 = M22.ResInfo.newBuilder();
        builder3.setResType( PlayerPowerDefine.POWER_stones);
        if (stoneNum == 0) {
            stoneNum = 1;
        }
        builder3.setCurCount(stoneNum);
        buildList.add(builder3.build());

        int foodNum =getScienceDonateTimes( PlayerPowerDefine.POWER_food);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, PlayerPowerDefine.POWER_food, ArmyGroupDefine.CONTRUBUTE_TECH);
        M22.ResInfo.Builder builder5 = M22.ResInfo.newBuilder();
        builder5.setResType(PlayerPowerDefine.POWER_food);
        if (foodNum == 0) {
            foodNum = 1;
        }
        builder5.setCurCount(foodNum);
        buildList.add(builder5.build());
        return buildList;
    }

    /**
     * 军团大厅捐献次数
     */
    public List<M22.ResInfo> hallContributeNum() {
        List<M22.ResInfo> buildList = new ArrayList<M22.ResInfo>();
        //TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int goldNum = gethallDonateTimes(200);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, 200, ArmyGroupDefine.CONTRUBUTE_HALL);
        M22.ResInfo.Builder builder = M22.ResInfo.newBuilder();
        builder.setResType(200);
        if (goldNum == 0) {
            goldNum = 1;
        }
        builder.setCurCount(goldNum);
        buildList.add(builder.build());
        int taelNum = gethallDonateTimes(PlayerPowerDefine.POWER_tael);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, PlayerPowerDefine.POWER_tael, ArmyGroupDefine.CONTRUBUTE_HALL);
        M22.ResInfo.Builder builder1 = M22.ResInfo.newBuilder();
        builder1.setResType(PlayerPowerDefine.POWER_tael);
        if (taelNum == 0) {
            taelNum = 1;
        }
        builder1.setCurCount(taelNum);
        buildList.add(builder1.build());
        int ironNum =gethallDonateTimes(PlayerPowerDefine.POWER_iron);//timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, PlayerPowerDefine.POWER_iron, ArmyGroupDefine.CONTRUBUTE_HALL);
        M22.ResInfo.Builder builder2 = M22.ResInfo.newBuilder();
        builder2.setResType(PlayerPowerDefine.POWER_iron);
        if (ironNum == 0) {
            ironNum = 1;
        }
        builder2.setCurCount(ironNum);
        buildList.add(builder2.build());
        int woodNum = gethallDonateTimes(PlayerPowerDefine.POWER_wood);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, PlayerPowerDefine.POWER_wood, ArmyGroupDefine.CONTRUBUTE_HALL);
        M22.ResInfo.Builder builder4 = M22.ResInfo.newBuilder();
        builder4.setResType(PlayerPowerDefine.POWER_wood);
        if (woodNum == 0) {
            woodNum = 1;
        }
        builder4.setCurCount(woodNum);
        buildList.add(builder4.build());
        int stoneNum = gethallDonateTimes(PlayerPowerDefine.POWER_stones);//timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, PlayerPowerDefine.POWER_stones, ArmyGroupDefine.CONTRUBUTE_HALL);
        M22.ResInfo.Builder builder3 = M22.ResInfo.newBuilder();
        builder3.setResType(PlayerPowerDefine.POWER_stones);
        if (stoneNum == 0) {
            stoneNum = 1;
        }
        builder3.setCurCount(stoneNum);
        buildList.add(builder3.build());

        int foodNum = gethallDonateTimes(PlayerPowerDefine.POWER_food);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, PlayerPowerDefine.POWER_food, ArmyGroupDefine.CONTRUBUTE_HALL);
        M22.ResInfo.Builder builder5 = M22.ResInfo.newBuilder();
        builder5.setResType(PlayerPowerDefine.POWER_food);
        if (foodNum == 0) {
            foodNum = 1;
        }
        builder5.setCurCount(foodNum);
        buildList.add(builder5.build());
        return buildList;
    }

    /**
     * 军团科技，金币,资源捐献
     */
    public int legionTechGoldDonate(int power, int techId, int techLv, int techExp, int armyTechLv, ArmygroupMenber armygroupMenber, Armygroup armygroup) {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //     TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        JSONObject techObj = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.LEGIONLEVEL, "type", techId, "level", techLv);
        JSONObject scijson = ConfigDataProxy.getConfigInfoFindById(DataDefine.LEGIONSCIENCE, techId);
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if (power == 200) {
            //  timerdbProxy.addTimer(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, 1, 0, TimerDefine.TIMER_REFRESH_FOUR, power, ArmyGroupDefine.CONTRUBUTE_TECH,playerProxy);
            int donateNum = getScienceDonateTimes(power);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH);
            if (donateNum == 0) {
                addScienceDonateTimes(power);
                donateNum = getScienceDonateTimes(power);
                //timerdbProxy.addNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH, 1);
                //  donateNum = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH);
            }
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.GOLD_CONTIBUTE, "num", donateNum);
            List<JSONObject> objectList = ConfigDataProxy.getConfigAllInfo(DataDefine.GOLD_CONTIBUTE);
            JSONObject jsonObjectmession = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", ArmyGroupDefine.MESSIONTYPE1);
            long vipLv = vipProxy.getVipLevel();
            long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
            if (jsonObject == null) {
                return ErrorCodeDefine.M220009_2;
            } else if (vipLv < 1) {
                return ErrorCodeDefine.M220009_1;
            } else if (donateNum >= (objectList.size() + 1)) {
                return ErrorCodeDefine.M220009_2;
            }
            int needgold = (int) Math.ceil(jsonObject.getInt("goldneed") * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_LEGION_SCIENCE_GOLD_RETURN)) / 100.0);
            if (hadGold < needgold) {
                return ErrorCodeDefine.M220009_3;
            } else if (armyTechLv <= techLv) {
                return ErrorCodeDefine.M220009_6;
            } else if (armyTechLv < scijson.getInt("seciencelv")) {
                return ErrorCodeDefine.M220009_7;
            }
           /* if(armygroup.getMession1()>=jsonObjectmession.getInt("max")){
                return ErrorCodeDefine.M220009_2;
            }*/
            addScienceDonateTimes(power);
             //timerdbProxy.addNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH, 1);
            //  timerdbProxy.addNum(TimerDefine.LEGION_ALLTIME_DONATE,0, 0, 1);
            legionTimer.setAlldonetetimes(legionTimer.getAlldonetetimes()+1);
            // armygroupMenber.setContribute(armygroupMenber.getContribute()+jsonObject.getInt("contribute"));
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, needgold, LogDefine.LOST_ARMYGROUP_CONTRIBUTE);
            sendFunctionLog(FunctionIdDefine.LEGION_SCIENCE_GOLD_RESOURCE_DONATE_FUNCTION_ID, power, needgold, playerProxy.getPlayerId());
        } else {
            //  timerdbProxy.addTimer(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, 1, 0, TimerDefine.TIMER_REFRESH_FOUR, power, ArmyGroupDefine.CONTRUBUTE_TECH,playerProxy);
            int donateNum = getScienceDonateTimes(power);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH);
            if (donateNum == 0) {
                addScienceDonateTimes(power);
                donateNum = getScienceDonateTimes(power);
                //  timerdbProxy.addNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH, 1);
                //  donateNum = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH);
            }
            List<JSONObject> objectList = ConfigDataProxy.getConfigAllInfo(DataDefine.RES_CONTIBUTE);
            int num = 0;
            for (JSONObject obj : objectList) {
                if (obj.getInt("restype") == power) {
                    num += 1;
                }
            }
            JSONObject jsonObjectmession = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel());
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RES_CONTIBUTE, "restype", power, "num", donateNum);
            long hadNum = playerProxy.getPowerValue(power);
            if (jsonObject == null) {
                return ErrorCodeDefine.M220009_2;
            } else if (donateNum >= (num + 1)) {
                return ErrorCodeDefine.M220009_2;
            } else if (hadNum < jsonObject.getInt("reqneed")) {
                return ErrorCodeDefine.M220009_4;
            } else if (armyTechLv <= techLv) {
                return ErrorCodeDefine.M220009_6;
            } else if (armyTechLv < scijson.getInt("seciencelv")) {
                return ErrorCodeDefine.M220009_7;
            }
            if (armygroup.getMession1() >= jsonObjectmession.getInt("personNum") * ArmyGroupDefine.JUANZENGTIME) {
                return ErrorCodeDefine.M220009_2;
            }
            addScienceDonateTimes(power);
            //  timerdbProxy.addNum(TimerDefine.ARMYGROUP_TECH_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_TECH, 1);
            //timerdbProxy.addNum(TimerDefine.LEGION_ALLTIME_DONATE,0, 0, 1);
            legionTimer.setAlldonetetimes(legionTimer.getAlldonetetimes()+1);
            //   armygroupMenber.setContribute(armygroupMenber.getContribute()+jsonObject.getInt("contribute"));
            playerProxy.reducePowerValue(power, jsonObject.getInt("reqneed"), LogDefine.LOST_ARMYGROUP_CONTRIBUTE);
            sendFunctionLog(FunctionIdDefine.LEGION_SCIENCE_GOLD_RESOURCE_DONATE_FUNCTION_ID, power, jsonObject.getInt("reqneed"), playerProxy.getPlayerId());
        }
        return 0;
    }

    public int setPostName(int post, String name, Armygroup armygroup) {
        if (armygroup == null) {
            return -1;
        }
        if (post >= ArmyGroupDefine.JOB_NORMAL || post <= 0) {
            return -2;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONJOB, "ID", post);
        if (jsonObject == null) {
            return -3;
        }
        if (armygroup.getLevel() < jsonObject.getInt("openlv")) {
            return -4;
        }
        if (name.length() > 8) {
            return -5;
        }
        if (post == 1) {
            armygroup.setSelfname1(name);
        }
        if (post == 2) {
            armygroup.setSelfname2(name);
        }
        if (post == 3) {
            armygroup.setSelfname3(name);
        }
        if (post == 4) {
            armygroup.setSelfname4(name);
        }
        return 0;
    }


    /**
     * 军团福利，(日常福利)福利领取
     */
    public int getWelfareReward(int rewardId, PlayerReward reward, ArmygroupMenber armygroupMenber) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        // timerdbProxy.addTimer(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0,playerProxy);
        int num =legionTimer.getWalfRewad();// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0);
        if (num >= 1) {
            return ErrorCodeDefine.M220013_7;
        }
        JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WELFAREREWARD, "ID", rewardId);
        if (json == null) {
            return ErrorCodeDefine.M220013_5;
        } else if (armygroupMenber.getContribute() < json.getInt("contrineed")) {
            return ErrorCodeDefine.M220013_6;
        }
        //发送奖励
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        JSONArray rewardList = json.getJSONArray("reward");
        for (int i = 0; i < rewardList.length(); i++) {
            rewardProxy.getPlayerRewardByFixReward(rewardList.getInt(i), reward);
        }
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_ARMYGROUP_WELFAREREWARD);
        //  timerdbProxy.setNum(TimerDefine.ARMYGROUP_WELFAREREWARD, 0, 0, 1);
        legionTimer.setWalfRewad(UtilDefine.REWARD_STATE_HAS_GET);
        armygroupMenber.setContribute(armygroupMenber.getContribute() - json.getInt("contrineed"));
        return 0;
    }

    /***
     * 获得某个军团的详细信息
     ***/
    public int getArmyGroupDetailInfo(Armygroup arm, M22.M220101.S2C.Builder builder, int iconid, int penid) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M22.LegionDetailInfo.Builder lginfo = M22.LegionDetailInfo.newBuilder();
        lginfo.setId(arm.getId());
        lginfo.setLeaderName(arm.getCommander());
        lginfo.setJoinType(arm.getJoinWay());
        lginfo.setJoinCond1(arm.getConditonlevel());
        lginfo.setJoinCond2(arm.getConditoncapity());
        lginfo.setNotice(arm.getNotice());
        lginfo.setIconId(iconid);
        lginfo.setPendantId(penid);
        if (playerProxy.getPlayerApplylist().contains(arm.getId())) {
            lginfo.setApplyState(1);
        } else {
            lginfo.setApplyState(0);
        }
        builder.setDetailInfo(lginfo);
        return 0;
    }

    /****
     * 获得军团信息
     ******/
    public int getArmygroupInfos(Map<Long, Armygroup> map, M22.M220100.S2C.Builder builder) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
       /* List<Armygroup> list = new ArrayList<Armygroup>();
        for (Armygroup army : map.values()) {
            list.add(army);
        }
        SortUtil.anyProperSort(list, "getCapity", false);*/
        List<M22.LegionShortInfo> leglist = new ArrayList<M22.LegionShortInfo>();
        for (Armygroup armygroup : map.values()) {
            System.out.println(armygroup.getName() + "rank" + armygroup.getRank());
            M22.LegionShortInfo.Builder info = M22.LegionShortInfo.newBuilder();
            info.setId(armygroup.getId());
            info.setRank(armygroup.getRank());
            armygroup.setRank(armygroup.getRank());
            info.setName(armygroup.getName());
            info.setLevel(armygroup.getLevel());
            info.setCurNum(armygroup.getArmmenbers().size());
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel());
            if (jsonObject != null) {
                info.setMaxNum(jsonObject.getInt("personNum"));
            } else {
                info.setMaxNum(30);
            }
            info.setCapacity(armygroup.getCapity());
            if (armygroup.getApplymenbers().contains(playerProxy.getPlayerId())) {
                info.setApplyState(1);
            } else {
                info.setApplyState(0);
            }
            if (soldierProxy.getHighestCapacity() >= armygroup.getConditoncapity() && playerProxy.getLevel() >= armygroup.getLevel() && playerProxy.getArmGrouId() == 0) {
                info.setIsCanJoin(1);
            } else {
                info.setIsCanJoin(0);
            }
            leglist.add(info.build());
        }
        builder.addAllShortInfos(leglist);
        return 0;
    }


    /****
     * 按照名字搜索
     ******/
    public int getsearchInfos(Map<Long, Armygroup> map, M22.M220104.S2C.Builder builder, String name) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        List<M22.LegionShortInfo> leglist = new ArrayList<M22.LegionShortInfo>();
        for (Armygroup armygroup : map.values()) {
            if (armygroup.getName().indexOf(name) != -1) {
                M22.LegionShortInfo.Builder info = M22.LegionShortInfo.newBuilder();
                info.setId(armygroup.getId());
                info.setRank(armygroup.getRank());
                info.setName(armygroup.getName());
                info.setLevel(armygroup.getLevel());
                info.setCurNum(armygroup.getMenbers().size());
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel());
                info.setMaxNum(jsonObject.getInt("personNum"));
                info.setCapacity(armygroup.getCapity());
                if (armygroup.getApplymenbers().contains(playerProxy.getPlayerId())) {
                    info.setApplyState(1);
                } else {
                    info.setApplyState(0);
                }
                if (soldierProxy.getHighestCapacity() >= armygroup.getConditoncapity() && playerProxy.getLevel() >= armygroup.getLevel()) {
                    info.setIsCanJoin(1);
                } else {
                    info.setIsCanJoin(0);
                }
                leglist.add(info.build());
            }
        }
        builder.addAllInfos(leglist);
        return 0;
    }

    private int getMyDevate(List<ArmygroupMenber> menbers) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (ArmygroupMenber armygroupMenber : menbers) {
            if (armygroupMenber.getPlayerId() == playerProxy.getPlayerId()) {
                return armygroupMenber.getContribute();
            }
        }
        return 0;
    }

    /****
     * 查看军团信息
     ******/
    public int getMyGroupInfo(Armygroup armygroup, List<ArmygroupMenber> menbers, M22.M220200.S2C.Builder builder) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getArmGrouId() == 0) {
            return -1;
        }
        M22.LegionMineInfo.Builder lminfo = M22.LegionMineInfo.newBuilder();
        lminfo.setId(armygroup.getId());
        lminfo.setRank(armygroup.getRank());
        lminfo.setName(armygroup.getName());
        lminfo.setLeaderName(armygroup.getCommander());
        lminfo.setLevel(armygroup.getLevel());
        lminfo.setCurNum(menbers.size());
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel());
        lminfo.setMaxNum(jsonObject.getInt("personNum"));
        lminfo.setJoinType(armygroup.getJoinWay());
        lminfo.setJoinCond1(armygroup.getConditonlevel());
        lminfo.setJoinCond2(armygroup.getConditoncapity());
        lminfo.setNotice(armygroup.getNotice());
        lminfo.setBuildDegree(armygroup.getBuild());
        lminfo.setAffiche(armygroup.getFiche());
        lminfo.setMineJob(getMyJob(menbers));
        lminfo.setMyContribute(getMyDevate(menbers));
        for (int i = 1; i <= 4; i++) {
            M22.LegionCustomJobInfo.Builder lecutnemw = M22.LegionCustomJobInfo.newBuilder();
            lecutnemw.setIndex(i);
            JSONObject jsonObjectjob = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONJOB, "ID", i);
            lecutnemw.setCurNum(getJobNum(menbers, i));
            lecutnemw.setLevelNeed(jsonObjectjob.getInt("openlv"));
            if (i == 1) {
                lecutnemw.setName(armygroup.getSelfname1());
            }
            if (i == 2) {
                lecutnemw.setName(armygroup.getSelfname2());
            }
            if (i == 3) {
                lecutnemw.setName(armygroup.getSelfname3());
            }
            if (i == 4) {
                lecutnemw.setName(armygroup.getSelfname4());
            }
            lminfo.addCustomJobInfos(lecutnemw.build());

        }
        builder.setMineInfo(lminfo);
        builder.addAllMemberInfos(getLegionMenbers(menbers));
        return 0;
    }

    public List<M22.LegionMemberInfo> getLegionMenbers(List<ArmygroupMenber> menbers) {
        List<M22.LegionMemberInfo> list = new ArrayList<M22.LegionMemberInfo>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (ArmygroupMenber armygroupMenber : menbers) {
            M22.LegionMemberInfo.Builder legMen = M22.LegionMemberInfo.newBuilder();
            legMen.setId(armygroupMenber.getPlayerId());
            legMen.setDevoterank(armygroupMenber.getDevotrank());
            legMen.setCapacity(armygroupMenber.getCapity());
            legMen.setName(armygroupMenber.getName());
            legMen.setJob(armygroupMenber.getJob());
            legMen.setLevel(armygroupMenber.getLevel());
            legMen.setCapacity(armygroupMenber.getCapity());
            legMen.setDevote(armygroupMenber.getDonatecontributeWeek());
            legMen.setDevotoWeek(armygroupMenber.getContributeWeek());
            legMen.setSex(armygroupMenber.getSex());
            legMen.setCapityrank(armygroupMenber.getCapityrank());
            legMen.setActivityrank(armygroupMenber.getActivityrank());
            legMen.setActivityvalue(armygroupMenber.getVitality());
            legMen.setPendantId(armygroupMenber.getPendantId());
            legMen.setIconId(armygroupMenber.getIcon());
            SimplePlayer sp = PlayerService.getSimplePlayer(armygroupMenber.getPlayerId(), playerProxy.getAreaKey());
            if (sp.online) {
                legMen.setIsOnline(1);
            } else {
                legMen.setIsOnline(0);
            }
            list.add(legMen.build());
        }
        return list;
    }

    public M22.LegionMemberInfo.Builder getMenberInfo(ArmygroupMenber armygroupMenber) {
        M22.LegionMemberInfo.Builder legMen = M22.LegionMemberInfo.newBuilder();
        legMen.setId(armygroupMenber.getPlayerId());
        legMen.setDevoterank(armygroupMenber.getDevotrank());
        legMen.setCapacity(armygroupMenber.getCapity());
        legMen.setName(armygroupMenber.getName());
        legMen.setJob(armygroupMenber.getJob());
        legMen.setLevel(armygroupMenber.getLevel());
        legMen.setCapacity(armygroupMenber.getCapity());
        legMen.setDevote(armygroupMenber.getDonatecontributeWeek());
        legMen.setDevotoWeek(armygroupMenber.getContributeWeek());
        legMen.setSex(armygroupMenber.getSex());
        legMen.setCapityrank(armygroupMenber.getCapityrank());
        legMen.setActivityrank(armygroupMenber.getActivityrank());
        legMen.setActivityvalue(armygroupMenber.getVitality());
        legMen.setPendantId(armygroupMenber.getPendantId());
        legMen.setIconId(armygroupMenber.getIcon());
        if (armygroupMenber.getLogintime() > armygroupMenber.getOutlinetime()) {
            legMen.setIsOnline(1);
        } else {
            legMen.setIsOnline(0);
        }
        return legMen;
    }

    public int getMyJob(List<ArmygroupMenber> menbers) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (ArmygroupMenber agm : menbers) {
            if (agm.getPlayerId() == playerProxy.getPlayerId()) {
                return agm.getJob();
            }
        }
        return ArmyGroupDefine.JOB_NORMAL;
    }

    public int getJobNum(List<ArmygroupMenber> menbers, int job) {
        int num = 0;
        for (ArmygroupMenber menber : menbers) {
            if (menber.getJob() == job) {
                num++;
            }
        }
        return num;
    }

    public int getApplyList(List<SimplePlayer> simplePlayers, M22.M220202.S2C.Builder builder) {
        int num = 0;
        for (SimplePlayer simplePlayer : simplePlayers) {
            M22.LegionApplyInfo.Builder legApp = M22.LegionApplyInfo.newBuilder();
            legApp.setCapacity(simplePlayer.getCapacity());
            legApp.setId(simplePlayer.getId());
            legApp.setName(simplePlayer.getName());
            legApp.setLevel(simplePlayer.getLevel());
            builder.addApplyInfos(legApp);
        }
        return num;
    }

    /**
     * 军团科技大厅，提升等级
     */
    public int legionTechUp(int armyLv, int techLv, int buildNum) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONSCIENCELV, "level", techLv);
        if (playerProxy.getPost() != ArmyGroupDefine.JOB_MANGER) {
            return ErrorCodeDefine.M220010_1;
        } else if (buildNum < jsonObject.getInt("reqScore")) {
            return ErrorCodeDefine.M220010_2;
        } else if (jsonObject.getInt("reqScore") == 0) {
            return ErrorCodeDefine.M220010_3;
        } else if (techLv >= armyLv) {
            return ErrorCodeDefine.M220010_4;
        }
        return 0;
    }

    /**
     * 军团大厅，金币,资源捐献
     */
    public int legionHallGoldDonate(int power, Armygroup armygroup) {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if (power == 200) {
            //   timerdbProxy.addTimer(TimerDefine.ARMYGROUP_HALL_CONTIBUTE,1, 0, TimerDefine.TIMER_REFRESH_FOUR, power, ArmyGroupDefine.CONTRUBUTE_HALL,playerProxy);
            int donateNum = gethallDonateTimes(power);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL);
            if (donateNum == 0) {
                // timerdbProxy.addNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL, 1);
                //   donateNum = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL);
                addhallDonateTimes(200);
                donateNum=gethallDonateTimes(power);
            }
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.GOLD_CONTIBUTE, "num", donateNum);
            List<JSONObject> objectList = ConfigDataProxy.getConfigAllInfo(DataDefine.GOLD_CONTIBUTE);
            long vipLv = vipProxy.getVipLevel();
            JSONObject jsonObjectmession = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel());
            long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
            if (jsonObject == null) {
                return ErrorCodeDefine.M220008_5;
            } else if (vipLv < 1) {
                return ErrorCodeDefine.M220008_1;
            }
            int needgold = (int) Math.ceil(jsonObject.getInt("goldneed") * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_LEGION_DERONATE_RETRUEN)) / 100.0);
            if (donateNum > (objectList.size() + 1)) {
                return ErrorCodeDefine.M220008_2;
            } else if (hadGold < needgold) {
                return ErrorCodeDefine.M220008_3;
            }/*if(armygroup.getMession1()>=jsonObjectmession.getInt("personNum")){
                return ErrorCodeDefine.M220008_1;
            }*/
            addhallDonateTimes(power);
            // timerdbProxy.addNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL, 1);
            //playerProxy.addPowerValue(PlayerPowerDefine.POWER_contribute, jsonObject.getInt("contribute"), LogDefine.GET_ARMYGROUP_CONTRIBUTE);
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, needgold, LogDefine.LOST_ARMYGROUP_CONTRIBUTE);
            sendFunctionLog(FunctionIdDefine.LEGION_HALL_GOLD_RESOURCE_DONATE_FUNCTION_ID, power, needgold, playerProxy.getPlayerId());
        } else {
            // timerdbProxy.addTimer(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, 1, 0, TimerDefine.TIMER_REFRESH_FOUR, power, ArmyGroupDefine.CONTRUBUTE_HALL,playerProxy);
            int donateNum = gethallDonateTimes(power);// timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL);
            if (donateNum == 0) {
                addhallDonateTimes(power);
                //  timerdbProxy.addNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL, 1);
                //  donateNum = timerdbProxy.getTimerNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL);
                donateNum = gethallDonateTimes(power);
            }
            List<JSONObject> objectList = ConfigDataProxy.getConfigAllInfo(DataDefine.RES_CONTIBUTE);
            int num = 0;
            for (JSONObject obj : objectList) {
                if (obj.getInt("restype") == power) {
                    num += 1;
                }
            }
            JSONObject jsonObjectmession = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel());
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RES_CONTIBUTE, "restype", power, "num", donateNum);
            long hadNum = playerProxy.getPowerValue(power);
            if (jsonObject == null) {
                return ErrorCodeDefine.M220008_2;
            } else if (donateNum > (num + 1)) {
                return ErrorCodeDefine.M220008_2;
            } else if (hadNum < jsonObject.getInt("reqneed")) {
                return ErrorCodeDefine.M220008_4;
            }
            if (armygroup.getMession1() >= jsonObjectmession.getInt("personNum") * ArmyGroupDefine.JUANZENGTIME) {
                return ErrorCodeDefine.M220008_2;
            }
            addhallDonateTimes(power);
            //  timerdbProxy.addNum(TimerDefine.ARMYGROUP_HALL_CONTIBUTE, power, ArmyGroupDefine.CONTRUBUTE_HALL, 1);
            //playerProxy.addPowerValue(PlayerPowerDefine.POWER_contribute, jsonObject.getInt("contribute"), LogDefine.GET_ARMYGROUP_CONTRIBUTE);
            playerProxy.reducePowerValue(power, jsonObject.getInt("reqneed"), LogDefine.LOST_ARMYGROUP_CONTRIBUTE);
            sendFunctionLog(FunctionIdDefine.LEGION_HALL_GOLD_RESOURCE_DONATE_FUNCTION_ID, power, jsonObject.getInt("reqneed"), playerProxy.getPlayerId());
        }
        return 0;
    }

    /**
     * 军团大厅，提升等级
     */
    public int legionHallUp(int armyLv, int buildNum, ArmygroupMenber armygroupMenber) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armyLv);
        if (armygroupMenber.getJob() != ArmyGroupDefine.JOB_MANGER) {
            return ErrorCodeDefine.M220007_1;
        } else if (buildNum < jsonObject.getInt("reqScore")) {
            return ErrorCodeDefine.M220007_2;
        } else if (jsonObject.getInt("reqScore") == 0) {
            return ErrorCodeDefine.M220007_3;
        }
        return 0;
    }

    /**
     * 军团福利，(日常福利)福利院升级
     */
    public int welfareUp(int wefareLv, int armyLv, int buildNum) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WELFARELV, "lv", wefareLv);
        if (playerProxy.getPost() != ArmyGroupDefine.JOB_MANGER) {
            return ErrorCodeDefine.M220013_1;
        } else if (buildNum < json.getInt("scoreneed")) {
            return ErrorCodeDefine.M220013_2;
        } else if (wefareLv >= armyLv) {
            return ErrorCodeDefine.M220013_3;
        } else if (json.getInt("scoreneed") == 0) {
            return ErrorCodeDefine.M220013_4;
        }
        return 0;
    }

    /**
     * 领取军团福利资源
     */
    public int getWelfareRes(Map<Integer, Integer> resMap, ArmygroupMenber menber) {
        PlayerProxy playrProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        StringBuffer sb = new StringBuffer();
        int rs = -1;
        if (GameUtils.getServerDate().getTime() - menber.getJoinTime() < ArmyGroupDefine.needTime) {
            return ErrorCodeDefine.M220015_1;
        }
        for (int power : resMap.keySet()) {
            if (resMap.get(power) > 0) {
                rs = 0;
            }
        }
        if (rs == -1) {
            return rs;
        } else {
            for (int power : resMap.keySet()) {
                if (power == PlayerPowerDefine.POWER_tael) {
                    playrProxy.addPowerValue(PlayerPowerDefine.POWER_tael, resMap.get(power), LogDefine.GET_ARMYGROUP_WELFAREREWARD);
                    menber.setTael(menber.getTael() + resMap.get(power));
                    sb.append(PlayerPowerDefine.POWER_tael);
                    sb.append(":");
                    sb.append(resMap.get(power));
                    sb.append(",");
                } else if (power == PlayerPowerDefine.POWER_stones) {
                    playrProxy.addPowerValue(PlayerPowerDefine.POWER_stones, resMap.get(power), LogDefine.GET_ARMYGROUP_WELFAREREWARD);
                    menber.setStones(menber.getStones() + resMap.get(power));
                    sb.append(PlayerPowerDefine.POWER_stones);
                    sb.append(":");
                    sb.append(resMap.get(power));
                    sb.append(",");
                } else if (power == PlayerPowerDefine.POWER_wood) {
                    playrProxy.addPowerValue(PlayerPowerDefine.POWER_wood, resMap.get(power), LogDefine.GET_ARMYGROUP_WELFAREREWARD);
                    menber.setWood(menber.getWood() + resMap.get(power));
                    sb.append(PlayerPowerDefine.POWER_wood);
                    sb.append(":");
                    sb.append(resMap.get(power));
                    sb.append(",");
                } else if (power == PlayerPowerDefine.POWER_food) {
                    playrProxy.addPowerValue(PlayerPowerDefine.POWER_food, resMap.get(power), LogDefine.GET_ARMYGROUP_WELFAREREWARD);
                    menber.setFood(menber.getFood() + resMap.get(power));
                    sb.append(PlayerPowerDefine.POWER_food);
                    sb.append(":");
                    sb.append(resMap.get(power));
                    sb.append(",");
                } else if (power == PlayerPowerDefine.POWER_iron) {
                    playrProxy.addPowerValue(PlayerPowerDefine.POWER_iron, resMap.get(power), LogDefine.GET_ARMYGROUP_WELFAREREWARD);
                    menber.setIron(menber.getIron() + resMap.get(power));
                    sb.append(PlayerPowerDefine.POWER_iron);
                    sb.append(":");
                    sb.append(resMap.get(power));
                }
            }
            sendFunctionLog(FunctionIdDefine.LEGION_WELFAREHOUSE_ACTIVITY_WELFARE_RESOUTCE_GET_FUNCTION_ID, playrProxy.getPlayerId(), 0, 0, sb.toString());
        }
        return 0;
    }

    public int String_length(String value) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    public List<M22.BuildingInfo> getLeginBuildInfo(Armygroup armygroup) {
        List<M22.BuildingInfo> list = new ArrayList<M22.BuildingInfo>();
        M22.BuildingInfo.Builder info1 = M22.BuildingInfo.newBuilder();
        info1.setId(1);
        info1.setLevel(armygroup.getSciencelevel());
        list.add(info1.build());
        M22.BuildingInfo.Builder info2 = M22.BuildingInfo.newBuilder();
        info2.setId(2);
        info2.setLevel(armygroup.getLevel());
        list.add(info2.build());
        M22.BuildingInfo.Builder info3 = M22.BuildingInfo.newBuilder();
        info3.setId(3);
        info3.setLevel(armygroup.getWelfarelevel());
        list.add(info3.build());
        return list;
    }

    @Override
    public void fixedTimeEventHandler() {
       legionTimer.setHalldontes(new ArrayList<Integer>());
        legionTimer.setIds(new ArrayList<Integer>());
        legionTimer.setSciencedontes(new ArrayList<Integer>());
        legionTimer.setWalfRewad(0);
    }
}
