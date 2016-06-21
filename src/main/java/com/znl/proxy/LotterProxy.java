package com.znl.proxy;

import akka.actor.Actor;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.pojo.db.Player;
import com.znl.pojo.db.Timerdb;
import com.znl.proto.Common;
import com.znl.proto.M15;
import com.znl.proto.M3;
import com.znl.utils.GameUtils;
import com.znl.utils.RandomUtil;
import org.json.JSONObject;
import scala.tools.nsc.transform.patmat.Logic;
import scala.tools.nsc.transform.patmat.MatchCodeGen;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2015/10/28.
 */
public class LotterProxy extends BasicProxy {


    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }

    public LotterProxy(String areaKey) {
        this.areaKey = areaKey;
    }

    //获得抽奖奖励id
    public int getRandomReward(int ranId) {
        JSONObject rulejson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDRULE, ranId);
        int randone = rulejson.getJSONArray("numsec").getInt(0);
        int randtwo = rulejson.getJSONArray("numsec").getInt(1);
        int ranNum1 = RandomUtil.random(randone, randtwo);
        int ranNum2 = rulejson.getInt("totalnum") - ranNum1;
        int groupId1 = rulejson.getInt("rewardgroup1");
        int groupId2 = rulejson.getInt("rewardgroup2");
        List<Integer> contentDefineList1 = getContentDefines(ranNum1, groupId1);
        List<Integer> contentDefineList2 = getContentDefines(ranNum2, groupId2);
        contentDefineList1.addAll(contentDefineList2);
        List<Integer> ranlist = new ArrayList<Integer>();
        for (int id : contentDefineList1) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, id);
            int persent = jsonObject.getInt("percent");
            for (int i = 1; i <= persent; i++) {
                ranlist.add(id);
            }
        }
        int random = GameUtils.getRandomValueByRange(ranlist.size());
        return ranlist.get(random);

    }

    //淘宝抽奖
    public int getRandomRewardTaoBao(int ranId, List<Integer> alllist) {
        JSONObject rulejson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDRULE, ranId);
        int randone = rulejson.getJSONArray("numsec").getInt(0);
        int randtwo = rulejson.getJSONArray("numsec").getInt(1);
        int ranNum1 = RandomUtil.random(randone, randtwo);
        int ranNum2 = rulejson.getInt("totalnum") - ranNum1;
        int groupId1 = rulejson.getInt("rewardgroup1");
        int groupId2 = rulejson.getInt("rewardgroup2");
        List<Integer> contentDefineList1 = getContentDefines(ranNum1, groupId1);
        List<Integer> contentDefineList2 = getContentDefines(ranNum2, groupId2);
        contentDefineList1.addAll(contentDefineList2);
        alllist.addAll(contentDefineList1);
        List<Integer> ranlist = new ArrayList<Integer>();
        for (int id : contentDefineList1) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, id);
            int persent = jsonObject.getInt("percent");
            for (int i = 1; i <= persent; i++) {
                ranlist.add(id);
            }
        }
        int random = GameUtils.getRandomValueByRange(ranlist.size());
        return ranlist.get(random);

    }


    public List<Integer> getContentDefines(int ranNum, int groupId) {
        List<Integer> rs = new ArrayList<Integer>();
        List<JSONObject> defineList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RANDCONTENT, "groupId", groupId);
        if (ranNum <= 0) {
            return rs;
        }
        List<Integer> ranlist = new ArrayList<Integer>();
        for (JSONObject jsonObject : defineList) {
            int id = jsonObject.getInt("ID");
            int persent = jsonObject.getInt("percent");
            for (int i = 1; i <= persent; i++) {
                ranlist.add(id);
            }
        }
        for (int i = 1; i <= ranNum; i++) {
            int random = GameUtils.getRandomValueByRange(ranlist.size());
            rs.add(ranlist.get(random));
            removeAllId(ranlist, ranlist.get(random));
        }
        return rs;
    }

    private void removeAllId(List<Integer> list, int id) {
        List<Integer> remove = new ArrayList<Integer>();
        for (int i : list) {
            if (i == id) {
                remove.add(i);
            }
        }
        list.removeAll(remove);
    }

    //根据武将抽奖类型活的免费抽奖次数
    public int getFreeWarioLotterTimes(int type) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", type);
        if (jsonObject == null) {
            return 0;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttime = timerdbProxy.getLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0);
        long time = GameUtils.getServerDate().getTime() - lasttime;
        int times = (int) (time / 1000 / 60 / jsonObject.getInt("freetime"));
        int num = timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY_TODAY, type, 0);
        if (times >= jsonObject.getInt("freetotal")) {
            long reduce = GameUtils.getServerDate().getTime() - (jsonObject.getInt("freetotal") * jsonObject.getInt("freetime") * 60000);
            timerdbProxy.setLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0, reduce);
            times = jsonObject.getInt("freetotal");
        }
        if (times > jsonObject.getInt("freemax") - num) {
            times = jsonObject.getInt("freemax") - num;
        }
        return times;
    }

    //抽奖价格
    public int getWarioLotterCost(int type) {
        JSONObject jsonObject = null;
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if (type == 4) {
            jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", 3);
            int price=jsonObject.getInt("price") * 9;
            price= (int) Math.ceil(price*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_EQUIP_LOTTER_TEN)) / 100.0);
            return price;
        }
        jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", type);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttime = timerdbProxy.getLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0);
        long time = GameUtils.getServerDate().getTime() - lasttime;
        int times = (int) (time / 1000 / 60 / jsonObject.getInt("freetime"));
        int num = timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY_TODAY, type, 0);
        if (times >= jsonObject.getInt("freetotal")) {
            long reduce = GameUtils.getServerDate().getTime() - (jsonObject.getInt("freetotal") * jsonObject.getInt("freetime") * 60000);
            timerdbProxy.setLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0, reduce);
            times = jsonObject.getInt("freetotal");
        }
        if (times > jsonObject.getInt("freemax") - num) {
            times = jsonObject.getInt("freemax") - num;
        }
        if (times > 0 && type != LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            return 0;
        }
        int price=jsonObject.getInt("price");
        price= (int) Math.ceil(price*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_EQUIP_LOTTER_ONCE)) / 100.0);
        return price;
    }


    //抽奖价格
    public int getWarioLotterforShow(int type) {
        JSONObject jsonObject = null;
        if (type == 4) {
            jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", 3);
            return jsonObject.getInt("price") * 9;
        }
        jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", type);
   /*     TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttime = timerdbProxy.getLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0);
        long time = GameUtils.getServerDate().getTime() - lasttime;
        int times = (int) (time / 1000 / 60 / jsonObject.getInt("freetime"));
        int num = timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY_TODAY, type, 0);
        if (times >= jsonObject.getInt("freetotal")) {
            long reduce = GameUtils.getServerDate().getTime() - (jsonObject.getInt("freetotal") * jsonObject.getInt("freetime") * 60000);
            timerdbProxy.setLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0, reduce);
            times = jsonObject.getInt("freetotal");
        }
        if (times > jsonObject.getInt("freemax") - num) {
            times = jsonObject.getInt("freemax") - num;
        }
        if (times > 0 && type != LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            return 0;
        }*/
        return jsonObject.getInt("price");
    }

    //获得免费倒计时时间
    public int getFreeWarLotterLesTime(int type) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", type);

        if (jsonObject == null) {
            return -1;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttime = timerdbProxy.getLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0);
        long time = GameUtils.getServerDate().getTime() - lasttime;
        time = jsonObject.getInt("freetime") * 60000 - time;
        if (time < 0) {
            time = 0l;
        }
        return (int) (Math.ceil(time / 1000.0));
    }


    //根据淘宝抽奖类型活的免费抽奖次数
    public int getFreTaobaoLotterTimes(int type) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.TREASURE, "type", type);
        if (jsonObject == null) {
            return 0;
        }
        if (jsonObject.getInt("freetime") == 0) {
            return 0;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int max=jsonObject.getInt("freemax");
        int num=timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY_TAOBAO, type, 0);
        int valoue=max - num;
        if (valoue < 0) {
            valoue = 0;
        }
        return  valoue;
    }

    //获得某个类型抽奖的信息
    public M15.EquipLotterInfo getEquipLotterInfo(int type) {
        M15.EquipLotterInfo.Builder eli = M15.EquipLotterInfo.newBuilder();
        eli.setType(type);
        if(type==1) {
            TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            int num = timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY_TODAY, type, 0);
            if (num >= LotterDefine.WARIOOR_LOTTER_MAXNUM) {
                eli.setFreeTimes(0);
                eli.setTime(0);
            } else {
                eli.setFreeTimes(getFreeWarioLotterTimes(type));
                eli.setTime(getFreeWarLotterLesTime(type));
            }
        }else {
            eli.setFreeTimes(getFreeWarioLotterTimes(type));
            eli.setTime(getFreeWarLotterLesTime(type));
        }
        eli.setCost(getWarioLotterforShow(type));
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", type);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY, type, 0);
        if (jsonObject.getInt("purpletime") != 0) {
            if (num == 0) {
                eli.setWillNum(-1);
            } else {
                eli.setWillNum((jsonObject.getInt("purpletime") + 1) - num % (jsonObject.getInt("purpletime") + 1));
            }
        }

        return eli.build();
    }

    /****
     * 获取所有装备抽奖信息
     *******/
    public int getAllEquipLotterInfos(M15.M150000.S2C.Builder builder) {
        List<M15.EquipLotterInfo> list = new ArrayList<M15.EquipLotterInfo>();
        for (int i = 1; i <= 3; i++) {
            list.add(getEquipLotterInfo(i));
        }
        builder.addAllEquipLotterInfos(list);
        return 0;
    }

    /*********
     * 执行装备抽奖
     ***********/
    public int lotterEquip(int type, PlayerReward equreward, M15.M150001.S2C.Builder builder) {
        List<Integer> eqlist = new ArrayList<Integer>();
        JSONObject jsonObject = null;
        if (type == LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", 3);
        } else {
            jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", type);
        }
        if (jsonObject == null) {
            return ErrorCodeDefine.M150001_1;
        }
        //判断仓库
        EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
        int addnum = 1;
        if (type == LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            addnum = 9;
        }
        if (equipProxy.getEquipBagLesFree() < addnum) {
            return ErrorCodeDefine.M150001_3;
        }
        //是否免费
        int freetimes = getFreeWarioLotterTimes(type);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int cost = getWarioLotterCost(type);

        if (freetimes <= 0) {
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                return ErrorCodeDefine.M150001_2;
            }
        }
        if (freetimes <= 0 || type == LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_LOTTER_EQUIP);
        }
        int times = 1;
        if (type == LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            times = 9;
            type = LotterDefine.WARIOOR_LOTTER_BEST;
        }
        List<Integer> addlist = new ArrayList<Integer>();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= times; i++) {
            //抽过的次数
            int num = 0;
            int ranId = jsonObject.getInt("ruleID");
            num = timerdbProxy.getTimerNum(TimerDefine.TIMIER_LOTTERY, type, 0);
            if (jsonObject.getInt("purpletime") != 0 && num == 0) {
                ranId = jsonObject.getInt("purpleID");
            }
            if (jsonObject.getInt("purpletime") != 0 && (num + 1) % (jsonObject.getInt("purpletime") + 1) == 0) {
                ranId = jsonObject.getInt("purpleID");
            }
            timerdbProxy.addNum(TimerDefine.TIMIER_LOTTERY, type, 0, 1);
            int rewardId = getRandomReward(ranId);
            addlist.add(rewardId);
            JSONObject rewarjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, rewardId);
            int equipId = rewarjson.getInt("rewardInfo");
            eqlist.add(equipId);
            sb.append(equipId);
            sb.append(",");
            JSONObject equipjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.EQUIP_PRO, equipId);
            String generalname = equipjson.getString("name");
            int quality = equipjson.getInt("quality");
            int partId = equipjson.getInt("partId");
            if (partId == ActorDefine.RANK_FRIST_ONE && quality == ActorDefine.GENERAL_QUALITY) {
                playerProxy.sendSystemchat(ActorDefine.GENERAL_RECRUIT_NOTICE_TYPE, partId, ActorDefine.CONDITION_TWO, generalname);//发送系统公告4
            }
        }
        builder.addAllEquips(eqlist);
        addRewardToBag(addlist, LogDefine.GET_LOTTEREQUIP, equreward);
        if (freetimes > 0 && type != LotterDefine.WARIOOR_LOTTER_BEST_TEN) {
            timerdbProxy.addNum(TimerDefine.TIMIER_LOTTERY_TODAY, type, 0, 1);
            long lasttime = timerdbProxy.getLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0);
            long addtime = jsonObject.getInt("freetime") * 60000 * times;
            timerdbProxy.setLastOperatinTime(TimerDefine.TIMIER_LOTTERY, type, 0, lasttime + addtime);

        }
        sendFunctionLog(FunctionIdDefine.EQUIP_LOTTER_FUNCTION_ID, type, 0, 0, sb.toString());
        return 0;
    }


    /*********
     * 执行淘宝装备抽奖
     ***********/
    public int lotteTaobao(int type, int num, M15.M150002.S2C.Builder builder, PlayerReward reward) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.TREASURE, "type", type);
        if (jsonObject == null) {
            return ErrorCodeDefine.M150002_1;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        int itemId = jsonObject.getJSONArray("consume").getInt(0);
        int itemNum = jsonObject.getJSONArray("consume").getInt(1);
        if (num != EquipDefine.TAOBAO_NUM) {
            //是否免费
            int freetimes = getFreTaobaoLotterTimes(type);
            if (freetimes <= 0) {
                if (itemProxy.getItemNum(itemId) < itemNum) {
                    return ErrorCodeDefine.M150002_2;
                }
            }
            if (freetimes <= 0) {
                itemProxy.reduceItemNum(itemId, itemNum, LogDefine.LOST_LOTTER_TAO);
                RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                rewardProxy.addItemToReward(reward, itemId, itemNum);
            }
        } else {
            if (itemProxy.getItemNum(itemId) < itemNum * num) {
                return ErrorCodeDefine.M150002_2;
            } else {
                itemNum = itemNum * num;
                itemProxy.reduceItemNum(itemId, itemNum, LogDefine.LOST_LOTTER_TAO);
                RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                rewardProxy.addItemToReward(reward, itemId, itemNum);
            }
        }
        for (int i = 0; i < num; i++) {
            int ranId = jsonObject.getInt("ruleID");
            timerdbProxy.addNum(TimerDefine.TIMIER_LOTTERY_TAOBAO, type, 0, 1);
            List<Integer> alllist = new ArrayList<Integer>();
            int rewardId = getRandomRewardTaoBao(ranId, alllist);
            for (int getId : alllist) {
                JSONObject rewarjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, getId);
                Common.RewardInfo.Builder info = Common.RewardInfo.newBuilder();
                info.setPower(rewarjson.getInt("rewardType"));
                info.setTypeid(rewarjson.getInt("rewardInfo"));
                info.setNum(rewarjson.getInt("num"));
                builder.addIdlist(info.build());
            }
            JSONObject rewarjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, rewardId);
            Common.RewardInfo.Builder info = Common.RewardInfo.newBuilder();
            info.setPower(rewarjson.getInt("rewardType"));
            info.setTypeid(rewarjson.getInt("rewardInfo"));
            info.setNum(rewarjson.getInt("num"));
            builder.addGetid(info.build());
            List<Integer> addlist = new ArrayList<Integer>();
            addlist.add(rewardId);
            StringBuffer sb = new StringBuffer();
            sb.append(rewardId);
            sb.append(",");
            addRewardToBag(addlist, LogDefine.GET_LOTTER_TAOBAO, reward);
            timerdbProxy.addNum(TimerDefine.TIMIER_LOTTERY_TAOBAO, type, 0, 1);
            sendFunctionLog(FunctionIdDefine.LOTTER_TAOBAO_FUNCTION_ID, (long) type, 0, 0, sb.toString());
        }
        if (num > 1) {
            builder.clearIdlist();
        }
        return 0;
    }

    /*********
     * 淘宝购买幸运币
     *******/
    public int buyLuckyCoin(int type, PlayerReward reward, int buynum) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.TREASURE, "type", type);
        if (jsonObject == null) {
            return ErrorCodeDefine.M150003_1;
        }
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        int itemId = jsonObject.getJSONArray("consume").getInt(0);
        int itemNum = jsonObject.getJSONArray("consume").getInt(1) * buynum;
        int hasnum = itemProxy.getItemNum(itemId);
        if (hasnum > itemNum) {
            return ErrorCodeDefine.M150003_2;
        }
        int needbuy = itemNum - hasnum;
        int cost = needbuy * jsonObject.getInt("goldprice");
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
            return ErrorCodeDefine.M150003_3;
        }
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        rewardProxy.addItemToReward(reward, itemId, itemNum);
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_BUY_LUCKYCOIN);
        itemProxy.addItem(itemId, needbuy, LogDefine.GET_BUY_LOTTERTAOBAO);
        sendFunctionLog(FunctionIdDefine.TAOBAO_BUY_SEX_COIN_FUNCTION_ID, type, cost, needbuy);
        return 0;
    }

    //把抽奖结果发放到背包
    private void addRewardToBag(List<Integer> list, int logType, PlayerReward reward) {
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int id : list) {
            JSONObject rewarjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, id);
            int type = rewarjson.getInt("rewardType");
            int typeId = rewarjson.getInt("rewardInfo");
            int num = rewarjson.getInt("num");
            if (type == PlayerPowerDefine.BIG_POWER_ITEM) {
                ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
                itemProxy.addItem(typeId, num, logType);
                rewardProxy.addItemToReward(reward, typeId, num);
            } else if (type == PlayerPowerDefine.BIG_POWER_GENERAL) {
                EquipProxy equipProxy = getGameProxy().getProxy(ActorDefine.EQUIP_PROXY_NAME);
                long eqid = equipProxy.addEquip(typeId, logType);
                rewardProxy.addEquipIdtoReward(reward, eqid);

            } else if (type == PlayerPowerDefine.BIG_POWER_ORDNANCE) {
                OrdnanceProxy ordnanceProxy = getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
                long oid = ordnanceProxy.creatOrdnance(typeId, 0, 0, logType);
                rewardProxy.addOrdnanceIdtoReward(reward, oid);

            } else if (type == PlayerPowerDefine.BIG_POWER_ORDNANCE_FRAGMENT) {
                OrdnancePieceProxy ordnancePieceProxy = getGameProxy().getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
                ordnancePieceProxy.addOrdnancePiece(typeId, num, logType);
                rewardProxy.addOrdanceFragmentToReward(reward, typeId, num);

            } else if (type == PlayerPowerDefine.BIG_POWER_COUNSELLOR) {

            } else if (type == PlayerPowerDefine.BIG_POWER_SOLDIER) {
                SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                soldierProxy.addSoldierNum(typeId, num, logType);
                rewardProxy.addSoldierToReward(reward, typeId, num);

            } else if (type == PlayerPowerDefine.BIG_POWER_RESOURCE) {
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.addPowerValue(typeId, num, logType);
            }
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.sendSystemchat(ActorDefine.SEEKING_TREASURES_NOTICE_TYPE, type, typeId);//发送系统公告8
        }
    }

    public M15.M150003.S2C getTaoInfos(int type) {
        M15.M150003.S2C.Builder builder = M15.M150003.S2C.newBuilder();
        M15.Taobao.Builder t1 = M15.Taobao.newBuilder();
        t1.setType(1);
        t1.setTimes(getFreTaobaoLotterTimes(1));
        builder.addTaobaos(t1.build());
        M15.Taobao.Builder t2 = M15.Taobao.newBuilder();
        t2.setType(2);
        t2.setTimes(getFreTaobaoLotterTimes(2));
        builder.addTaobaos(t2.build());
        builder.setRs(0);
        builder.setType(type);
        return builder.build();
    }

    //获得装备所有免费抽奖次数
    public int getEquipFreeNum() {
        int num = 0;
        for (int i = 1; i <= 3; i++) {
            num += getFreeWarioLotterTimes(i);
        }
        return num;
    }

    public void test() {
        int x = 0;
        int y = 0;
        int add = 1;
        int num = 0;
        while (true) {
            if (num > 0) {
                break;
            }
            //产生四个点
            int lineminX = x - add;
            int linemaxX = x + add;
            int lineminY = y - add;
            int linemaxY = y + add;
            if (lineminX <= 0) {
                lineminX = 0;
            }
            if (lineminY < 0) {
                lineminY = 0;
            }
            if (linemaxY > ChatAndMailDefine.WORLD_MAX) {
                linemaxY = ChatAndMailDefine.WORLD_MAX;
            }
            if (linemaxX > ChatAndMailDefine.WORLD_MAX) {
                linemaxX = ChatAndMailDefine.WORLD_MAX;
            }
            //形成四条线
            for (int l1 = lineminX; l1 <= linemaxX; l1++) {
                //处理逻辑
            }
            for (int l2 = lineminY; l2 <= linemaxY; l2++) {
                //处理逻辑
            }
        }
    }

}
