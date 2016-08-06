package com.znl.proxy;

import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerCache;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.log.ItemGet;
import com.znl.log.ItemLost;
import com.znl.log.admin.tbllog_items;
import com.znl.pojo.db.Armygroup;
import com.znl.pojo.db.Item;
import com.znl.pojo.db.set.RoleNameSetDb;
import com.znl.proto.Common;
import com.znl.proto.M9;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class ItemProxy extends BasicProxy {
    private Set<Item> items = new ConcurrentHashSet<Item>();

    @Override
    public void shutDownProxy() {
        for (Item item : items) {
            item.finalize();
        }
    }

    public int pointItem=0;

    @Override
    protected void init() {
        initMetic();
    }


    public ItemProxy(Set<Long> itemIds,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : itemIds) {
            Item item = BaseDbPojo.get(id, Item.class,areaKey);
//            if (item.getTypeId() > 10000) {
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>item竟然出现:" + item.getTypeId() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>item竟然出现:" + item.getTypeId() + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//            }
            if(item!=null) {
                items.add(item);
            }
        }
        init();
    }


    public void saveItems() {
        List<Item> items = new ArrayList<Item>();
        synchronized (changeItems) {
            while (true) {
                Item item = changeItems.poll();
                if (item == null) {
                    break;
                }
                items.add(item);
            }
        }
        for (Item item : items) {
            item.save();
        }

    }

    private LinkedList<Item> changeItems = new LinkedList<Item>();

    private void pushItemToChangeList(Item item) {
        //插入更新队列
        synchronized (changeItems) {
            if (!changeItems.contains(item)) {
                changeItems.offer(item);
            }
        }
    }

    private Item getItemByitemId(int itemId) {
        for (Item item : items) {
            if (item.getTypeId() == itemId) {
                return item;
            }
        }
        return null;
    }


    private Item getItemByLongId(long id) {
        for (Item item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }


    public boolean isHasTypeId(int typeId) {
        for (Item item : items) {
            if (item.getTypeId() == typeId) {
                return true;
            }
        }
        return false;
    }

    private void sendItemGetLog(int typeId, int num, int addType) {
        ItemGet itemGet = new ItemGet(addType, typeId, num);
        if (getGameProxy() != null) {
            sendPorxyLog(itemGet);
        }
    }

    public void addItemByMap(HashMap<Integer, Integer> map, int logType) {
        for (Integer key : map.keySet()) {
            //TODO 离线玩家调用的时候要加日志处理
            addItem(key, map.get(key), logType);
        }
    }

    public void addItem(int typeId, int num, int addType) {
        if (isHasTypeId(typeId)) {
            addItemNum(typeId, num, addType);
        } else {
            creatItem(typeId, num, addType);
        }

        /**
         * tbllog_items 道具（增加）
         */
        itemLog(1, typeId, num,addType);
    }


    private long creatItem(int typeId, int num, int addtype) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
        if (jsonObject == null) {
            return -1;
        }
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Item item = BaseDbPojo.create(Item.class,areaKey);
        item.setNum(num);
        item.setPlayerId(playerProxy.getPlayerId());
        item.setTypeId(typeId);
        items.add(item);
        playerProxy.addItemToPlayer(item.getId());
        item.save();
        //道具获取写入日志
        sendItemGetLog(typeId, num, addtype);
        return item.getId();
    }

    public long getItemId(int itemId) {
        Item item = getItemByitemId(itemId);
        if (item == null) {
            return 0;
        }
        return item.getId();
    }

    public int getItemNum(int itemId) {
        Item item = getItemByitemId(itemId);
        if (item == null) {
            return 0;
        }
        return item.getNum();
    }


    private void addItemNum(int itemId, int add, int addType) {
        Item item = getItemByitemId(itemId);
        if (add < 0) {
            System.out.println("增加道具数量的时候出现负数了！！！");
            add = 0;
        }
        int value = getItemNum(itemId);
//        System.out.print("itemId=" + itemId);
        item.setNum(value + add);
        pushItemToChangeList(item);
        //道具获取写入日志
        sendItemGetLog(itemId, add, addType);
    }

    public void reduceItemNum(int itemId, int reduce, int lostType) {
        Item item = getItemByitemId(itemId);
        if (reduce < 0) {
            System.out.println("减少道具数量的时候出现负数了！！！");
            reduce -= reduce;
        }
        int value = getItemNum(itemId);
        int result = value - reduce;
        item.setNum(result);
        ItemLost itemLost = new ItemLost(lostType, itemId, reduce);
        if (result <= 0) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            items.remove(item);
            item.del();
            changeItems.remove(item);
            playerProxy.reduceItemfromPlayer(item.getId());
        } else {
            pushItemToChangeList(item);
        }

        /**
         * tbllog_items 道具(使用)
         */
        itemLog(0, itemId, reduce,lostType);
    }

    //获取获取一个道具的Info
    public Common.ItemInfo getItemInfo(int typeId) {
        Item item = getItemByitemId(typeId);
        Common.ItemInfo.Builder info = Common.ItemInfo.newBuilder();
        if (item != null) {
            info.setNum(item.getNum());
            info.setTypeid(item.getTypeId());
        } else {
            info.setNum(0);
            info.setTypeid(typeId);
        }
        return info.build();
    }

    //获取获取一个道具的Info
    public Common.ItemInfo createItemInfo(int typeId, int num) {
        Common.ItemInfo.Builder info = Common.ItemInfo.newBuilder();
        info.setNum(num);
        info.setTypeid(typeId);
        return info.build();
    }


    //获取所有道具的信息
    public int getAllItemInfo(List<Common.ItemInfo> infos, M9.M90000.S2C.Builder builder) {
        builder.addAllIteminfos(getAllItemInfos());
        return 0;
    }

    public List<Common.ItemInfo> getAllItemInfos() {
        List<Common.ItemInfo> list = new ArrayList<Common.ItemInfo>();
        for (Item item : items) {
            Common.ItemInfo.Builder info = Common.ItemInfo.newBuilder();
            info.setNum(item.getNum());
            info.setTypeid(item.getTypeId());
            list.add(info.build());
        }
        return list;
    }

    interface ItemFormula {
        int calc(JSONObject itemDefine, int num, PlayerReward reward, int costType);
    }

    private Map<Integer, ItemFormula> _mapItemMetic = new HashMap<>();

    private int doUserItem(JSONObject itemDefine, int num, PlayerReward reward, int costType) {
        int rs = _mapItemMetic.get(itemDefine.getInt("type")).calc(itemDefine, num, reward, costType);
        //TODO 检测系统公告
        List<JSONObject> noticeDefineList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.SYSTEM_NOTICE,"type",ActorDefine.USE_ITERM_NOTICE_TYPE);
        for (JSONObject define : noticeDefineList){
            int power = define.getInt("condition1");
            int id = define.getInt("condition2");
            switch (power){
                case PlayerPowerDefine.BIG_POWER_ITEM:{
                    //TODO 判断reward的addItemMap有没有对应的id为key值
                    if (reward.addItemMap.containsKey(id)){
                        List<JSONObject> getitem = ConfigDataProxy.getConfigInfoFilterById(DataDefine.ITEM_METIC, id);
                        for(JSONObject item : getitem){
                            String newitem = item.getString("name");
                            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
                            playerProxy.sendSystemchat(ActorDefine.USE_ITERM_NOTICE_TYPE,PlayerPowerDefine.BIG_POWER_ITEM,id,itemDefine.getString("name"),newitem);//发送系统公告9
                        }
                    }
                    break;
                }
                //TODO 其他BIG_POWER判断
            }
        }
        return rs ;
    }

    private void initMetic() {

        ItemFormula formula;
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //发送奖励
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            JSONArray jsonArray = itemDefine.getJSONArray("effect");
            int rewardId = jsonArray.getInt(0);
            for (int i = 1; i <= num; i++) {
                rewardProxy.getPlayerReward(rewardId, reward);
            }
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_USE_ITEM);
            return 0;
        };
        for (int type : ItemDefine.ITEM_REWARD_DATE) {
            _mapItemMetic.put(type, formula);
        }
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //发送奖励
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            JSONArray jsonArray = itemDefine.getJSONArray("effect");
            int rewardId = jsonArray.getInt(0);
            rewardProxy.getPlayerRewardByRandContent(rewardId, reward,new ArrayList<>());
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_USE_ITEM);
            return 0;
        };
        for (int type : ItemDefine.ITEM_REWARD_RATE) {
            _mapItemMetic.put(type, formula);
        }
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            // 加玩家buff逻辑
            ItemBuffProxy ibffProxy = getGameProxy().getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int type = itemDefine.getInt("type");
            int itemId = itemDefine.getInt("ID");
            if (type == ItemDefine.ITEM_CHANGE_ATTR||type == ItemDefine.ITEM_TEMPORORY_BUILDING) {
                //外观 buff
                JSONArray faceInfo = itemDefine.getJSONArray("effect");
                for (int i = 0; i < faceInfo.length(); i++) {
                    int id = faceInfo.getInt(i);
                    JSONObject obj = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_BUFF_FACE, id);
                    JSONArray array = obj.getJSONArray("power");
                    int buferrtimes=0;
                    for (int j = 0; j < array.length(); j++) {
                        JSONArray defien = array.getJSONArray(j);
                        int powerId = defien.getInt(0);
                        int times = defien.getInt(2);
                        buferrtimes=times;
                        int value = defien.getInt(1);
                        ibffProxy.addItemBuff(itemId, type, powerId, value, times, num);
                        if (type == ItemDefine.ITEM_CHANGE_ATTR && powerId == 90) {
                            playerProxy.setFacade(itemId,GameUtils.getServerDate().getTime()+(times*1000),value);
                        }
                    }
                    ibffProxy.addItemBuff(itemId, type, id, 0, buferrtimes, num);
                }

            } else {/******道具buff ******/
                JSONArray array = itemDefine.getJSONArray("effect");
                for (int i = 0; i < array.length(); i++) {
                    JSONArray defien = array.getJSONArray(i);
                    int powerId = defien.getInt(0);
                    int value = defien.getInt(1);
                    int times = defien.getInt(2);
                    ibffProxy.addItemBuff(itemId, type, powerId, value, times, num);
                }
            }
            return 0;
        };
        _mapItemMetic.put(ItemDefine.ITEM_TEMPORORY_BUILDING, formula);
        _mapItemMetic.put(ItemDefine.ITEM_REWARD_BUFFER, formula);
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_ATTR, formula);
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_RESCOUCE_OUTPUT, formula);
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_FACADE, formula);
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_BUILD_POSITION, formula);
        _mapItemMetic.put(ItemDefine.ITEM_REWARD_AVOID_WAR, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 建筑升级加速类
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_REWARD_BUILD_SPEED, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 建筑升级加速类
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_REWARD_BUILD_SPEED, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 坦克生产加速
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_SPEED_TAKEED_PRODUCTION, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 科技升级加速
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_SPEED_SCIENCE_PRODUCTION, formula);

        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 查找玩家基地坐标类
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_SEARCH_POINT, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 基地随机搬迁
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_BASE_RANDOM, formula);

        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 查找玩家世界矿点
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_SEARCH_MINES, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 玩家改名
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_NAME, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_CHANGE_UNION_NAME, formula);
        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 喇叭类
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_SAY_HAI, formula);

        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            //TODO 军团增加贡献度
            return ErrorCodeDefine.M90001_9;
        };
        _mapItemMetic.put(ItemDefine.ITEM_ADD_LEGION_SHARE, formula);

        formula = (JSONObject itemDefine, int num, PlayerReward reward, int costType) -> {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            JSONArray array = itemDefine.getJSONArray("openneed");
            int useItemId = array.getInt(1);
            int usenum = array.getInt(2);
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            if (costType == 1) {
                //使用道具
                if (getItemNum(useItemId) < usenum) {
                    return ErrorCodeDefine.M90001_5;
                }
                reduceItemNum(useItemId, 1, LogDefine.LOST_USEITEM);
            } else {
                if (getItemNum(useItemId) > usenum) {
                    return ErrorCodeDefine.M90001_7;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < ItemDefine.GOLD_REPACEITEM_PRICE) {
                    return ErrorCodeDefine.M90001_6;
                }
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, ItemDefine.GOLD_REPACEITEM_PRICE, LogDefine.LOST_USEITEM);
            }
            JSONArray jsonArray = itemDefine.getJSONArray("effect");
            int rewardId = jsonArray.getInt(0);
            rewardProxy.getPlayerRewardByRandContent(rewardId, reward,new ArrayList<>());
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_USE_ITEM);
            if (costType == 1) {
                rewardProxy.addItemToReward(reward, useItemId, num);
            }
            return 0;
        };
        _mapItemMetic.put(ItemDefine.ITEM_SPEED_USE_NEED_ITEM, formula);
    }

    //发红包
    public int sendRedBag(Long playerId, int typeId) {
        Item item = getItemByitemId(typeId);
        if (item == null) {
            return ErrorCodeDefine.M90004_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M90004_2;
        }
        if (playerId == 0) {
            return ErrorCodeDefine.M90004_9;
        }
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerId==playerProxy.getPlayerId()){
            return ErrorCodeDefine.M90004_13;
        }
        if (jsonObject.getInt("type") != ItemDefine.ITEM_SEND_RED_EVEOPE) {
            return ErrorCodeDefine.M90004_8;
        }
        if (item.getNum() < 1) {
            return ErrorCodeDefine.M90004_4;
        }
        reduceItemNum(typeId, 1, LogDefine.LOST_USEITEM);
        return 0;
    }

    public int changLegionName(String name, int typeId, Map<Long, Armygroup> map) {
        ArmyGroupProxy armyGroupProxy = getGameProxy().getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
        Item item = getItemByitemId(typeId);
        if (item == null) {
            return ErrorCodeDefine.M90004_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M90004_2;
        }
        if (item.getNum() < 1) {
            return ErrorCodeDefine.M90004_4;
        }
        if (armyGroupProxy.ishasSameName(map, name)) {
            return ErrorCodeDefine.M90004_10;//名字已经存在
        }
        //判断字符合法
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(!playerProxy.checkString(name)){
            return ErrorCodeDefine.M90004_15;//军团名称非法
        }
        if (armyGroupProxy.String_length(name) < 4 || armyGroupProxy.String_length(name) > 12) {
            return ErrorCodeDefine.M90004_11;//
        }
        reduceItemNum(typeId, 1, LogDefine.LOST_USEITEM);
        return 0;
    }

    public int changPlayerName(String name, int typeId) {
        Item item = getItemByitemId(typeId);
        if (item == null) {
            return ErrorCodeDefine.M90004_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M90004_2;
        }
        if (item.getNum() < 1) {
            return ErrorCodeDefine.M90004_4;
        }
        ArmyGroupProxy armyGroupProxy = getGameProxy().getProxy(ActorDefine.ARMYGROUP_PROXY_NAME);
        if (armyGroupProxy.String_length(name) < 4 || armyGroupProxy.String_length(name) > 12) {
            return ErrorCodeDefine.M90004_11;//
        }
        PlayerProxy playerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        String areaKey = playerProxy.getAreaKey();
        RoleNameSetDb roleNameSetDb = BaseSetDbPojo.getSetDbPojo(RoleNameSetDb.class, areaKey);
        Boolean isRepeat = roleNameSetDb.isKeyExist(name);
        if (isRepeat) {
            return ErrorCodeDefine.M90004_10;
        }
        if(!playerProxy.checkString(name)){
            return ErrorCodeDefine.M90004_14;//角色名称非法(英文字符)
        }
        //替换掉名字集合
        roleNameSetDb.replaceKeyValue(playerProxy.getPlayerName(),name,playerProxy.getPlayerId());
        playerProxy.getPlayer().setName(name);
        reduceItemNum(typeId, 1, LogDefine.LOST_USEITEM);
        return 0;
    }


    //道具使用
    public int useitem(int itemId, int num, PlayerReward reward, int costType) {
        Item item = getItemByitemId(itemId);
        if (item == null) {
            return ErrorCodeDefine.M90001_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, itemId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M90001_2;
        }
        int type = jsonObject.getInt("type");
        if (ItemDefine.ITEM_REWARD_NONE.contains(type)) {
            return ErrorCodeDefine.M90001_3;
        }

        if (num < 0) {
            num = -num;
        }
        if (item.getNum() < num) {
            return ErrorCodeDefine.M90001_4;
        }

        //扣除道具
        reduceItemNum(itemId, num, LogDefine.LOST_USEITEM);
        return doUserItem(jsonObject, num, reward, costType);

    }


    //测试增加道具
    public void testAdditem() {
      /*  List<JSONObject> orndnlist=ConfigDataProxy.getConfigAllInfo(DataDefine.ORDNANCE);
        List<JSONObject> piecelist=ConfigDataProxy.getConfigAllInfo(DataDefine.ORDNANCE_PIECE);
        OrdnanceProxy ordnanceProxy=getGameProxy().getProxy(ActorDefine.ORDANCE_PROXY_NAME);
        OrdnancePieceProxy ordnancePieceProxy=getGameProxy().getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
        if(ordnanceProxy.getOrdnanceInfos().size()==0){
            for(JSONObject jsonObject:orndnlist){
                ordnanceProxy.creatOrdnance(jsonObject.getInt("ID"),0,0,LogDefine.GET_CHAT);
            }
        }
        if(ordnancePieceProxy.getOrdnancePieceInfos().size()==0){
            for(JSONObject jsonObject:piecelist){
                ordnancePieceProxy.addOrdnancePiece(jsonObject.getInt("ID"),60,LogDefine.GET_CHAT);
            }
        }
        addItem(4016,90000,5);
        addItem(4017,90000,5);
        addItem(4018,90000,5);
        addItem(4019,90000,5);
        addItem(4020,90000,5);

        List<JSONObject> jsonObjectList=ConfigDataProxy.getConfigAllInfo(DataDefine.ITEM_METIC);
        for(JSONObject jsonObject:jsonObjectList){
            addItem(jsonObject.getInt("ID"),60000000,5);
        }*/
    }

    public List<Integer> delAllItem() {
        List<Integer> list = new ArrayList<Integer>();
        for (Item item : items) {

            list.add(item.getTypeId());
        }
        for (int typeId : list) {
            reduceItemNum(typeId, getItemNum(typeId), LogDefine.LOST_BUY_CHEAT);
        }
        return list;
    }

    public int getItemCanUse() {
        int num = 0;
        for (Item item : items) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, item.getTypeId());
            if(jsonObject==null){
               continue;
            }
            if (!ItemDefine.ITEM_REWARD_NONE.contains(jsonObject.getInt("type"))) {
                num++;
            }
        }
        return num;
    }

    /**
     * tbllog_items 道具opt:1增加，0使用
     */
    public void itemLog(int opt, int itemId, int num,int logType) {
        if(getGameProxy()==null){
            return;
        }
        PlayerProxy player = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        PlayerCache cache = player.getPlayerCache();
        tbllog_items itemslog = new tbllog_items();
        itemslog.setPlatform(cache.getPlat_name());
        itemslog.setRole_id(player.getPlayerId());
        itemslog.setAccount_name(player.getAccountName());
        itemslog.setDim_level(player.getLevel());
        itemslog.setOpt(opt);
        itemslog.setAction_id(logType);
        itemslog.setItem_id(itemId);
        itemslog.setItem_number((long) num);
        itemslog.setMap_id(0);
        itemslog.setHappend_time(GameUtils.getServerTime());
        sendPorxyLog(itemslog);
    }


    public int trumpeItem(int typeId,String mess){
        JSONObject jsonObject=ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC,typeId);
        if(jsonObject==null){
            return ErrorCodeDefine.M90006_1;
        }
        Item item=getItemByitemId(typeId);
        if(item==null){
            return ErrorCodeDefine.M90006_4;
        }
        if(jsonObject.getInt("type")!=ItemDefine.ITEM_SAY_HAI){
            return ErrorCodeDefine.M90006_3;
        }
        if(mess.length()>30){
            return ErrorCodeDefine.M90006_2;
        }
        reduceItemNum(typeId,1,LogDefine.LOST_USEITEM);
        return 0;
    }

    /**矿点侦察定位**/
    public int getPointItem(int typeId){
        JSONObject jsonObject=ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC,typeId);
        if(jsonObject==null){
            return ErrorCodeDefine.M80006_3;
        }
        Item item=getItemByitemId(typeId);
        if(item==null&&item.getNum()<=0){
            return ErrorCodeDefine.M80006_1;
        }
        if(jsonObject.getInt("type")!=ItemDefine.ITEM_CHANGE_SEARCH_POINT||jsonObject.getInt("type")!=ItemDefine.ITEM_SEARCH_MINES){
            return ErrorCodeDefine.M80006_4;
        }
        return 0;
    }

    //军团贡献道具使用
    public int useLegionItem(int typeId,int num){
        if(num<0){
            num=-num;
        }
        JSONObject jsonObject= ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC,typeId);
        if(jsonObject.getInt("type")!=ItemDefine.ITEM_ADD_LEGION_SHARE){
            return  ErrorCodeDefine.M90007_1;
        }
        Item item=getItemByitemId(typeId);
        if(item.getNum()<num){
            return ErrorCodeDefine.M90007_2;
        }
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerProxy.getArmGrouId()<=0){
            return ErrorCodeDefine.M90007_3;
        }
        reduceItemNum(typeId,num,LogDefine.LOST_USEITEM);
        int addnum=jsonObject.getJSONArray("effect").getInt(0)*num;
        return addnum;
    }

}
