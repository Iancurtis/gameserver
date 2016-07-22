package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.*;
import com.znl.define.*;
import com.znl.log.admin.tbllog_advisers;
import com.znl.log.admin.tbllog_items;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Adviser;
import com.znl.pojo.db.Item;
import com.znl.pojo.db.ResFunBuilding;
import com.znl.proto.Common;
import com.znl.proto.M20;
import com.znl.proto.M26;
import com.znl.proto.M7;
import com.znl.template.MailTemplate;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class AdviserProxy extends BasicProxy {
    private Set<Adviser> advis = new HashSet<Adviser>();

    @Override
    public long getExpandPowerValue(int power) {
        return super.getExpandPowerValue(power);
    }

    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {
        super.expandPowerMap.clear();
        for (Adviser adviser : advis) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.Counsellor, adviser.getTypeId());
            if (jsonObject.getInt("type") != EquipDefine.FIGHT_ADVISER_TYPE) {
                JSONArray jsonArray = jsonObject.getJSONArray("property");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    addPlayerPower(array.getInt(0), array.getInt(1));
                }
            }
        }
    }

    private void addPlayerPower(int id, long value) {

        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }


    }

    public AdviserProxy(Set<Long> ids, String areaKey) {
        this.areaKey = areaKey;
        for (long id : ids) {
            Adviser adviser = BaseDbPojo.get(id, Adviser.class, areaKey);
            advis.add(adviser);
        }
        init();
    }

    private LinkedList<Adviser> changeAdvis = new LinkedList<Adviser>();

    private void pushAdviserToChangeList(Adviser adviser) {
        //插入更新队列
        synchronized (changeAdvis) {
            if (!changeAdvis.contains(adviser)) {
                changeAdvis.offer(adviser);
            }
        }
    }

    public void saveAdviser() {
        List<Adviser> advisers = new ArrayList<Adviser>();
        synchronized (changeAdvis) {
            while (true) {
                Adviser adviser = changeAdvis.poll();
                if (adviser == null) {
                    break;
                }
                advisers.add(adviser);
            }
        }
        for (Adviser adviser : advisers) {
            adviser.save();
        }
    }

    //获取某个id的军师
    private Adviser getAdviserByTypeId(int typeId) {
        for (Adviser adviser : advis) {
            if (adviser.getTypeId() == typeId) {
                return adviser;
            }
        }
        return null;
    }

    //添加军师
    public void addAdviser(int typeId, int num, int logtype) {
        if (num < 0) {
            return;
        }
        if (getAdviserMaxNum() + num > EquipDefine.ADVISER_JUNSHIFU_MAXNUM) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            MailTemplate template = new MailTemplate("军师派遣", "尊敬的主公：由于您的军师府邸已经人满为患，您新获得的军师仍流落在外，请先整顿或扩充军师府邸，再来招募您的军师。", 0, "系统", ChatAndMailDefine.MAIL_TYPE_SYSTEM);
            Set<Long> allid = new HashSet<>();
            allid.add(playerProxy.getPlayerId());
            List<Integer[]> add = new ArrayList<Integer[]>();
            add.add(new Integer[]{405, typeId, num});
            template.setAttachments(add);
            GameMsg.SendMail mail = new GameMsg.SendMail(allid, template, "系统邮件", 0l);
            sendMailServiceMsg(mail);
            return;
        }
        if (isHasAdviser(typeId)) {
            Adviser adviser = getAdviserByTypeId(typeId);
            int oldnum = adviser.getNum();
            adviser.setNum(oldnum + num);
            pushAdviserToChangeList(adviser);
        } else {
            createAdviser(typeId, num);
        }
        adviserLog(1, typeId, num, logtype,getAdviserNumBytypeId(typeId));
        refurceExpandPowerMap();
    }

    private int getAdviserNumBytypeId(int typeId){
        for (Adviser adviser : advis) {
           if(adviser.getTypeId()==typeId){
               return adviser.getNum();
           }
        }
        return 0;
    }

    //获得军师府军师的数量
    public int getAdviserMaxNum() {
        int num = 0;
        for (Adviser adviser : advis) {
            num += adviser.getNum();
        }
        return num;
    }

    //删除军师
    public void reduceAdviser(int typeId, int num, int logtype) {
        //TODO 添加日志
        if (num == 0) {
            num = -num;
        }
        Adviser adviser = getAdviserByTypeId(typeId);
        if (adviser == null) {
            return;
        }
        int oldnum = adviser.getNum();
        adviser.setNum(oldnum - num);
        if (adviser.getNum() <= 0) {
            //执行删除
            adviser.del();
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.removeAdviseIdToPlayer(adviser.getId());
            advis.remove(adviser);
            changeAdvis.remove(adviser);
        } else {
            pushAdviserToChangeList(adviser);
        }
        adviserLog(0, typeId, num, logtype,getAdviserNumBytypeId(typeId));
        refurceExpandPowerMap();
    }

    private void createAdviser(int typeId, int num) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.Counsellor, typeId);
        if (jsonObject == null) {
            System.err.println("军师数据不存在" + typeId);
            return;
        }
        Adviser adviser = BaseDbPojo.create(Adviser.class, areaKey);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        adviser.setNum(num);
        adviser.setPlayerId(playerProxy.getPlayerId());
        adviser.setFightnum(0);
        adviser.setTypeId(typeId);
        adviser.setQuilty(jsonObject.getInt("quality"));
        adviser.save();
        advis.add(adviser);
        playerProxy.addAdviseIdToPlayer(adviser.getId());
    }


    //判断是否该typeid的军师了
    public boolean isHasAdviser(int typeId) {
        for (Adviser adviser : advis) {
            if (adviser.getTypeId() == typeId) {
                return true;
            }
        }
        return false;
    }

    public Common.AdviserInfo getAdviserInfoBytypeId(int typeId) {
        Adviser adviser = getAdviserByTypeId(typeId);
        if (adviser == null) {
            Common.AdviserInfo.Builder info = Common.AdviserInfo.newBuilder();
            info.setTypeId(typeId);
            info.setNum(0);
            info.setFightnum(0);
            info.setQuilty(0);
            return info.build();
        }
        return getAdviserInfoByAdviser(adviser);
    }

    private Common.AdviserInfo getAdviserInfoByAdviser(Adviser adviser) {
        Common.AdviserInfo.Builder info = Common.AdviserInfo.newBuilder();
        info.setTypeId(adviser.getTypeId());
        info.setNum(adviser.getNum());
        info.setFightnum(adviser.getFightnum());
        info.setQuilty(adviser.getQuilty());
        return info.build();
    }

    public List<Common.AdviserInfo> getAllAdviserInfo() {
        List<Common.AdviserInfo> list = new ArrayList<Common.AdviserInfo>();
        for (Adviser adviser : advis) {
            list.add(getAdviserInfoByAdviser(adviser));
        }
        return list;
    }

    //军师升级
    public int adviserLv(int typeId, PlayerReward reward) {
        Adviser adviser = getAdviserByTypeId(typeId);
        if (adviser == null || adviser.getNum() - adviser.getFightnum() <= 0) {
            return ErrorCodeDefine.M260002_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.CounsellorLv, "oriID", typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M260002_2;
        }
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        JSONArray jsonArray = jsonObject.getJSONArray("itemneed");
        int needcost = 0;
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int itemtypeid = array.getInt(1);
            int num = array.getInt(2);
            if (itemProxy.getItemNum(itemtypeid) < num) {
                if (jsonObject.getInt("goldlevelup") == 0) {
                    return ErrorCodeDefine.M260002_4;
                }
                int costnum = num-itemProxy.getItemNum(itemtypeid);
                JSONObject prijson = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", itemtypeid);
                if (prijson == null) {
                    return ErrorCodeDefine.M260002_2;
                }
                needcost += prijson.getInt("goldprice") * costnum;
                map.put(itemtypeid, itemProxy.getItemNum(itemtypeid));
            } else {
                map.put(itemtypeid, num);
            }
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < needcost) {
            return ErrorCodeDefine.M260002_3;
        }
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        //扣除费用
        for (int itemtypeid : map.keySet()) {
            int num = map.get(itemtypeid);
            if (num == 0) {
                continue;
            }
            rewardProxy.addItemToReward(reward, itemtypeid, num);
            itemProxy.reduceItemNum(itemtypeid, num, LogDefine.LOST_ADVISER_LV);
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, needcost, LogDefine.LOST_ADVISER_LV);
        reduceAdviser(typeId, 1, LogDefine.LOST_ADVISER_LV);
        addAdviser(jsonObject.getInt("targetID"), 1, LogDefine.GET_ADVISER_LV);
        rewardProxy.addCounsellorToReward(reward, typeId, 1);
        rewardProxy.addCounsellorToReward(reward, jsonObject.getInt("targetID"), 1);
        //TODO 添加日志
        return 0;
    }

    //军师分解
    public int adviserdrop(List<Integer> typeids, PlayerReward reward) {
        for (int typeId : typeids) {
            Adviser adviser = getAdviserByTypeId(typeId);
            if (adviser == null || adviser.getNum() - adviser.getFightnum() <= 0) {
                return ErrorCodeDefine.M260003_1;
            }
        }
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int typeId : typeids) {
            Adviser adviser = getAdviserByTypeId(typeId);
            int num = adviser.getNum() - adviser.getFightnum();
            reduceAdviser(typeId, num, LogDefine.LOST_ADVISER_DROP);
            rewardProxy.addCounsellorToReward(reward, typeId, adviser.getNum());
            if (num > 0) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.Counsellor, typeId);
                //获得道具
                JSONArray jsonArray = jsonObject.getJSONArray("resolve");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int itemtypeId = array.getInt(1);
                    int addnum = array.getInt(2) * num;
                    itemProxy.addItem(itemtypeId, addnum, LogDefine.GET_ADVISER_DROP);
                    rewardProxy.addItemToReward(reward, itemtypeId, addnum);
                }
            }
        }
        //TODO 添加日志
        return 0;
    }

    //军师进阶
    public int adviserAdvance(List<Integer> ids, int quilty, PlayerReward reward, M26.M260001.S2C.Builder builder) {
        if (ids.size() != EquipDefine.ADVISER_ADVANCE_NUM) {
            return ErrorCodeDefine.M260001_1;
        }
        for (int typeid : ids) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.Counsellor, typeid);
            if (jsonObject.getInt("quality") != quilty) {
                return ErrorCodeDefine.M260001_4;
            }
            Adviser adviser = getAdviserByTypeId(typeid);
            if (adviser == null || adviser.getNum() - adviser.getFightnum() <= 0) {
                return ErrorCodeDefine.M260001_2;
            }
        }
        JSONObject jsonObjectcost = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.CounsellorAdcost, "quality", quilty);
        if(jsonObjectcost==null){
            return ErrorCodeDefine.M260001_5;
        }
        JSONArray jsonArray = jsonObjectcost.getJSONArray("need");
        Map<Integer,Integer> costmap=new HashMap<Integer,Integer>();
        int price =0;
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int power = array.getInt(0);
            int typeId = array.getInt(1);
            int num = array.getInt(2);
            if (itemProxy.getItemNum(typeId) < num) {
               costmap.put(typeId,itemProxy.getItemNum(typeId));
                int costnum =num- itemProxy.getItemNum(typeId);
                JSONObject prijson = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", typeId);
                if (prijson==null){
                    return ErrorCodeDefine.M260001_6;
                }
               price += prijson.getInt("goldprice") * costnum;
            }else{
                costmap.put(typeId,num);
            }
        }
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold)<price){
            return ErrorCodeDefine.M260001_3;
        }
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int itemtypeid :costmap.keySet()) {
            int num = costmap.get(itemtypeid);
            if (num == 0) {
                continue;
            }
            rewardProxy.addItemToReward(reward, itemtypeid, num);
            itemProxy.reduceItemNum(itemtypeid, num, LogDefine.LOST_ADVISER_JINJIE);
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, price, LogDefine.LOST_ADVISER_JINJIE);
        for (int id : ids) {
            rewardProxy.addCounsellorToReward(reward, id, 1);
            reduceAdviser(id, 1, LogDefine.LOST_ADVISER_JINJIE);
        }
        int newid = todoAvance(quilty);
        builder.setNewid(newid);
        addAdviser(newid, 1, LogDefine.GET_ADVISER_JINJIE);
        rewardProxy.addCounsellorToReward(reward, newid, 1);
        return 0;
    }

    public int allAdvance(int quilty, PlayerReward reward, M26.M260006.S2C.Builder builder){
           List<List<Integer>> list=getadvancelist(quilty);
           if(list.size()==0){
               return ErrorCodeDefine.M260006_2;
           }
        JSONObject jsonObjectcost = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.CounsellorAdcost, "quality", quilty);
        if(jsonObjectcost==null){
            return ErrorCodeDefine.M260006_4;
        }
        int price =0;
        JSONArray jsonArray = jsonObjectcost.getJSONArray("need");
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        Map<Integer,Integer> costmap=new HashMap<Integer,Integer>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int power = array.getInt(0);
            int typeId = array.getInt(1);
            int num = array.getInt(2)*list.size();
            if (itemProxy.getItemNum(typeId) < num) {
                costmap.put(typeId,itemProxy.getItemNum(typeId));
                int costnum =num- itemProxy.getItemNum(typeId);
                JSONObject prijson = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", typeId);
                if (prijson==null){
                    return ErrorCodeDefine.M260006_5;
                }
                price += prijson.getInt("goldprice") * costnum;
            }else{
                costmap.put(typeId,num);
            }
        }
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold)<price){
            return ErrorCodeDefine.M260001_3;
        }
        //扣除费用
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int itemtypeid :costmap.keySet()) {
            int num = costmap.get(itemtypeid);
            if (num == 0) {
                continue;
            }
            rewardProxy.addItemToReward(reward, itemtypeid, num);
            itemProxy.reduceItemNum(itemtypeid, num, LogDefine.LOST_ADVISER_JINJIE);
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, price, LogDefine.LOST_ADVISER_JINJIE);
        for (List<Integer> ids : list) {
            for(int id:ids) {
                rewardProxy.addCounsellorToReward(reward, id, 1);
                reduceAdviser(id, 1, LogDefine.LOST_ADVISER_JINJIE);
            }
        }
        for(int i=0;i<list.size();i++){
            int newid = todoAvance(quilty);
            builder.addNewids(newid);
            addAdviser(newid, 1, LogDefine.GET_ADVISER_JINJIE);
            rewardProxy.addCounsellorToReward(reward, newid, 1);
        }
        return 0;
    }

    private List<List<Integer>> getadvancelist(int quilty) {
        List<Adviser> list = new ArrayList<Adviser>();
        for (Adviser adviser : advis) {
            if (adviser.getQuilty() == quilty) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.Counsellor, adviser.getTypeId());
                adviser.setSort(jsonObject.getInt("sort"));
                list.add(adviser);
            }
        }
        SortUtil.anyProperSort(list,"getSort",false);
        List<List<Integer>> advancelist=new ArrayList<List<Integer>>();
        List<Integer> newlist = new ArrayList<Integer>();
        for(Adviser adviser:list){
            for(int i=0;i<adviser.getNum()-adviser.getFightnum();i++) {
                newlist.add(adviser.getTypeId());
                if (newlist.size() >= EquipDefine.ADVISER_ADVANCE_NUM) {
                    advancelist.add(newlist);
                    newlist = new ArrayList<Integer>();
                }
            }
        }
        return advancelist;
    }

    public void cost(PlayerReward reward, int power, int typeId, int num) {
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (power == PlayerPowerDefine.BIG_POWER_ITEM) {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            itemProxy.reduceItemNum(typeId, num, LogDefine.LOST_ADVISER_JINJIE);
            rewardProxy.addItemToReward(reward, typeId, num);
        }
        if (power == PlayerPowerDefine.BIG_POWER_GENERAL) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_ORDNANCE) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_ORDNANCE_FRAGMENT) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_COUNSELLOR) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_SOLDIER) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_RESOURCE) {

        }
    }

    public int checkcost(int power, int typeId, int num) {
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (power == PlayerPowerDefine.BIG_POWER_ITEM) {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            if (itemProxy.getItemNum(typeId) < num) {
                return ErrorCodeDefine.M260001_3;
            }
        }
        if (power == PlayerPowerDefine.BIG_POWER_GENERAL) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_ORDNANCE) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_ORDNANCE_FRAGMENT) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_COUNSELLOR) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_SOLDIER) {

        }
        if (power == PlayerPowerDefine.BIG_POWER_RESOURCE) {

        }
        return 0;
    }

    //进阶执行
    private int todoAvance(int quailty) {
        List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.CounsellorDetail, "type", quailty);
        //获得总概率
        int allrate = 0;
        for (JSONObject jsonObject : jsonObjectList) {
            allrate += jsonObject.getInt("rate");
        }
        int randum = GameUtils.getRandomValueByRange(allrate);
        int min = 0;
        JSONObject json = null;
        for (JSONObject jsonObject : jsonObjectList) {
            if (randum >= min && jsonObject.getInt("rate") + min >= randum) {
                json = jsonObject;
                break;
            }
            min+=jsonObject.getInt("rate");
        }
        return json.getInt("counsellorID");
    }

    //军师抽奖
    public int adviserLotter(int type, int num, PlayerReward reward, M26.M260005.S2C.Builder builder) {
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        JSONObject jsonrule = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.CounsellorRule, "type", type);
        if (jsonrule == null) {
            return ErrorCodeDefine.M260005_2;
        }
        if (getAdviserMaxNum() + num > EquipDefine.ADVISER_JUNSHIFU_MAXNUM) {
            return ErrorCodeDefine.M260005_3;
        }
        int lotterId = jsonrule.getInt("rewardId");
      //  TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int times =0;
        if(type==LotterDefine.JUBSHI_LOTTERY_GOLD){
            times=playerProxy.getjunshigoldTimes();
        }else{
            times=playerProxy.getjunshiresouceTimes();
        }
        int price = getCost(type, times, num);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.CounsellorPrice, "type", type);
        int power = jsonObject.getInt("pricetype");
        if (playerProxy.getPowerValue(power) < price) {
            return ErrorCodeDefine.M260005_1;
        }
        //扣除资源
        playerProxy.reducePowerValue(power, price, LogDefine.LOST_ADVISER_LOTTERY);
        //执行抽奖
        LotterProxy lotterProxy = getGameProxy().getProxy(ActorDefine.LOTTER_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int i = 1; i <= num; i++) {
            int reward1 = lotterProxy.getRandomReward(lotterId);
            JSONObject rewarjson = ConfigDataProxy.getConfigInfoFindById(DataDefine.RANDCONTENT, reward1);
            int typeId = rewarjson.getInt("rewardInfo");
            builder.addGetids(typeId);
            int getnum = rewarjson.getInt("num");
            addAdviser(typeId, getnum, LogDefine.GET_ADVISER_LOTTERY);
            rewardProxy.addCounsellorToReward(reward, typeId, getnum);
        }
        if(type==LotterDefine.JUBSHI_LOTTERY_GOLD){
          playerProxy.addjunshigoldTimes(num);
        }else{
         playerProxy.addjunshiresouceTimes(num);
        }
        return 0;
    }

    //根据类型和次数获得费用
    private int getCost(int type, int times, int num) {
        times++;
        int price = 0;
        List<JSONObject> jsonObjectList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.CounsellorPrice, "type", type);
        for (int i = 1; i <= num; i++) {
            for (JSONObject jsonObject : jsonObjectList) {
                if (jsonObject.getInt("timemin") <= times && jsonObject.getInt("timemax") >= times) {
                    price += jsonObject.getInt("price");
                    break;
                }
            }
            times++;
        }
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if(type==LotterDefine.JUBSHI_LOTTERY_GOLD) {
            price = (int) Math.ceil(price * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_LOTTER_GENEL_GOLD)) / 100.0);
        }
        if(type==LotterDefine.JUBSHI_LOTTERY_RESOURSE){
            price = (int) Math.ceil(price * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_LOTTER_GENEL_BAOSHI)) / 100.0);
        }
        return price;
    }


    //获得抽奖信息
    public List<M26.CostInfo> getCostInfo() {
        List<M26.CostInfo> list = new ArrayList<M26.CostInfo>();
     //   TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M26.CostInfo.Builder info1 = M26.CostInfo.newBuilder();
        info1.setType(LotterDefine.JUBSHI_LOTTERY_GOLD);
       int times =playerProxy.getjunshigoldTimes();// timerdbProxy.getTimerNum(TimerDefine.JUNSHILOTTERY, LotterDefine.JUBSHI_LOTTERY_GOLD, 0);
       info1.setOnceprice(getCost(LotterDefine.JUBSHI_LOTTERY_GOLD, times, 1));
       info1.setFiveprice(getCost(LotterDefine.JUBSHI_LOTTERY_GOLD, times, 5));
        list.add(info1.build());
        M26.CostInfo.Builder info2 = M26.CostInfo.newBuilder();
        info2.setType(LotterDefine.JUBSHI_LOTTERY_RESOURSE);
        int rtimes =playerProxy.getjunshiresouceTimes();// timerdbProxy.getTimerNum(TimerDefine.JUNSHILOTTERY, LotterDefine.JUBSHI_LOTTERY_RESOURSE, 0);
        info2.setOnceprice(getCost(LotterDefine.JUBSHI_LOTTERY_RESOURSE, rtimes, 1));
        info2.setFiveprice(getCost(LotterDefine.JUBSHI_LOTTERY_RESOURSE, rtimes, 5));
        list.add(info2.build());
        return list;

    }

    /**
     * tbllog_advisers 道具opt:1增加，0使用
     */
    public void adviserLog(int opt, int typeid, int num, int logType,long remian_num) {
        if (getGameProxy() == null) {
            return;
        }
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_advisers itemslog = new tbllog_advisers();
        itemslog.setPlatform(cache.getPlat_name());
        itemslog.setRole_id(player.getPlayerId());
        itemslog.setAccount_name(player.getAccountName());
        itemslog.setDim_level(player.getLevel());
        itemslog.setOpt(opt);
        itemslog.setAction_id(logType);
        itemslog.setType_id(typeid);
        itemslog.setItem_number((long) num);
        itemslog.setMap_id(0);
        itemslog.setRemain_num(remian_num);
        itemslog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(itemslog);
    }

}
