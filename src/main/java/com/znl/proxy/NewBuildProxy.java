package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.pojo.db.Production;
import com.znl.pojo.db.ResFunBuilding;
import com.znl.proto.M28;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2016/4/20.
 */
public class NewBuildProxy extends BasicProxy {

    private Set<ResFunBuilding> rfbs = new ConcurrentHashSet<>();
    private Set<Production> productions = new ConcurrentHashSet<>();
    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {
        super.expandPowerMap.clear();
        for (ResFunBuilding rfb : rfbs) {
            if (getBuildTypeByType(rfb.getSmallType()) == 1) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", rfb.getSmallType(), "lv", rfb.getLevel());
                if(jsonObject==null){
                    List<JSONObject> jsonObjectList=ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RESOUCEBUILDLEVEEFFECT,"type",rfb.getSmallType());
                    rfb.setLevel(jsonObjectList.size()-1);
                    rfb.save();
                    continue;
                }
                JSONArray jsonArray = jsonObject.getJSONArray("effect");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    addBuildPlayerPower(array.getInt(0), array.getLong(1));
                }
            } else if (getBuildTypeByType(rfb.getSmallType()) == 2) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", rfb.getSmallType(), "lv", rfb.getLevel());
                if(jsonObject==null){
                    List<JSONObject> jsonObjectList=ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.FUNTIONBUILDLEVEEFFECT,"type",rfb.getSmallType());
                    rfb.setLevel(jsonObjectList.size()-1);
                    rfb.save();
                    continue;
                }
                JSONArray jsonArray = jsonObject.getJSONArray("effect");
                if (jsonArray.length() > 0) {
                    addBuildPlayerPower(jsonArray.getInt(0), jsonArray.getLong(1));
                }
            }
        }
        if (getGameProxy() != null) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (playerProxy != null) {
                if (expandPowerMap.get(PlayerPowerDefine.NOR_POWER_depotprotect) != null) {
                    playerProxy.setDepotprotect(expandPowerMap.get(PlayerPowerDefine.NOR_POWER_depotprotect));
                }
            }
        }
    }

    //初始化建筑
    public void initResFuBuild() {
        //基地建筑初始化
        List<JSONObject> flist = ConfigDataProxy.getConfigAllInfo(DataDefine.FUNTIONBUILD);
        ResFunBuilding combuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        if (combuild == null) {
            JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.FUNTIONBUILD, "type", ResFunBuildDefine.BUILDE_TYPE_COMMOND);
            creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 1);
            combuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        }
        List<ResFunBuilding> fblist = getResFunBuildingBybigType(ResFunBuildDefine.BUILDE_TYPE_FUNTION);
        if (fblist.size() < flist.size()) {
            for (JSONObject json : flist) {
                if (getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("ID")) == null) {
//                    if (combuild.getLevel() > json.getInt("condition")) {
//                        creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 1);
//                    } else {
//                        creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 0);
//                    }
                    //一些功能性的建筑（如演武场）就不创建了，还有一些未建造的建筑也不建造了
                    if (!ResFunBuildDefine.NO_NEED_SAVE_FUNCTION.contains(json.getInt("ID")) && json.getInt("initlevel") > 0){
                        creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 1);
                    }
                }
            }
            init();
        }
        //建设建筑初始化
        List<JSONObject> rlist = ConfigDataProxy.getConfigAllInfo(DataDefine.RESOUCEBUILD);
        List<ResFunBuilding> rblist = getResFunBuildingBybigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE);
        //不要全部创建，只创建打开的部分
//        if (rblist.size() < rlist.size()) {
//            for (JSONObject json : rlist) {
//                if (getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, json.getInt("ID")) == null) {
//                    if (combuild.getLevel() > json.getInt("openlv")) {
//                        creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, 0, json.getInt("ID"), 0, 1);
//                    } else {
//                        creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, 0, json.getInt("ID"), 0, 0);
//                    }
//                }
//            }
//        }

        if (rblist.size() == 0){
            //初次登陆还没创建过的要执行一次遍历创建
            for (JSONObject json : rlist) {
                if (combuild.getLevel() > json.getInt("openlv")) {
                    creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, 0, json.getInt("ID"), 0, 1);
                }
            }
        }
    }

    private long creaResFunbuild(int bigType, int smallType, int index, int level, int state) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ResFunBuilding rfb = BaseDbPojo.create(ResFunBuilding.class, areaKey);
        rfb.setBigType(bigType);
        rfb.setSmallType(smallType);
        rfb.setIndex(index);
        rfb.setLevel(level);
        rfb.setPlayerId(playerProxy.getPlayerId());
        rfb.setLastblanceTime(GameUtils.getServerDate().getTime());
        rfb.setNextLevelTime(0l);
        rfb.setState(state);
        playerProxy.addResFunBuildToPlayer(rfb.getId());
        rfb.save();
        if(level>0){
        //增加繁荣度
        playerProxy.upBuilderOrCreate(smallType, level);
        }
        rfbs.add(rfb);
        return rfb.getId();
    }

    private List<ResFunBuilding> getResFunBuildingBybigType(int bigType) {
        List<ResFunBuilding> list = new ArrayList<ResFunBuilding>();
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getBigType() == bigType) {
                list.add(rfb);
            }
        }
        return list;
    }

    public void addOfflineLevelAndProduction(){
        // 给离线玩家检验升级建筑
        //分为自动升级和非自动升级
        long now = GameUtils.getServerDate().getTime();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int autoState = playerProxy.getAutoBuildState();
        if (playerProxy.getAutoBuildState() == TimerDefine.BUILDAUTOLEVEL_OPEN){
            //执行自动升级检测
            long minFinishTime = getMinNextLevelTime();
            long endTime = playerProxy.getAutoBuildStateendtime();
            if (endTime > now){
                endTime = now;
            }
            if (minFinishTime > 0){
                while (minFinishTime < endTime){
                    //执行离线的自动升级逻辑
                    doBuildAutoLevelUp(minFinishTime);

                    //开始升级下一个建筑
                    ResFunBuilding nextBuild = getNextCanLevelUpBuild();
                    if (nextBuild == null){
                        break;//没有可以升级的了
                    }
                    JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", nextBuild.getSmallType(), "lv", nextBuild.getLevel());
                    long time = jsonObject.getInt("time");
                    long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
                    time = (long) Math.ceil(time / (1 + power / 100.0));
                    long finishTime = minFinishTime + (time * 1000);
                    setFinishLevelTime(nextBuild.getSmallType(),nextBuild.getIndex(),finishTime);

                    //重新获取最小的升级到期时间
                    minFinishTime = getMinNextLevelTime();
                    if (minFinishTime > 0){
                        break;
                    }
                }
            }
            //判断一下自动建造的时间，是否要关闭
            if (playerProxy.getAutoBuildStateendtime() < now){
                playerProxy.setAutoBuildStateendtime(0l);
                playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OFF);
            }

        }else {
            for (ResFunBuilding rfb : rfbs){
                if (rfb.getSmallType() > 0 && rfb.getNextLevelTime() > 0 && rfb.getNextLevelTime() < now){
                    doBuildLevelUp(rfb.getSmallType(),rfb.getIndex());
                }
            }
        }

        //给离线玩家检验生产
        boolean reCheck = true;
        PlayerReward reward = new PlayerReward();
        while (reCheck){
            reCheck = false;
            Set<Production> productionSet = new HashSet<>(productions);
            for (Production p : productionSet){
                if (p.getState() == ResFunBuildDefine.PRODUCTION_STATE_WORKING && p.getFinishTime() < now){
                    autoFinishProduction(p,reward);
                    reCheck = true;
                }
            }
        }

    }

    /**
     * getMinNextLevelTime()
     * 获取最小的完成升级时间
     * @return long
     */
    private long getMinNextLevelTime(){
        long minTime = 0l;
        for (ResFunBuilding rfb : rfbs){
            if (rfb.getNextLevelTime() > 0){
                if (minTime == 0 || rfb.getNextLevelTime() < minTime){
                    minTime = rfb.getNextLevelTime();
                }
            }
        }
        return minTime;
    }

    private void doBuildAutoLevelUp(long finishTime){
        for (ResFunBuilding rfb : rfbs){
            if (rfb.getLevel() > 0 && rfb.getSmallType() > 0){
                if (rfb.getNextLevelTime() <= finishTime){
                    doBuildLevelUp(rfb.getSmallType(),rfb.getIndex());
                }
            }
        }
    }

    private ResFunBuilding getNextCanLevelUpBuild(){
        ResFunBuilding resFunBuilding = null;
        for (ResFunBuilding rfb : rfbs){
            //遍历出不在升级中，并且等级最小的
            if (rfb.getLevel() > 0 && rfb.getSmallType() > 0){
                if (isCanBuildType(rfb.getSmallType(),rfb.getIndex())){
                    JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", rfb.getSmallType(), "lv", rfb.getLevel());
                    JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", rfb.getSmallType(), "lv", rfb.getLevel() + 1);
                    if (jsonUp != null && getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) >= jsonObject.getInt("commandlv")) {
                        if (resFunBuilding == null || resFunBuilding.getLevel() > rfb.getLevel()){
                            resFunBuilding = rfb;
                        }
                    }
                }
            }
        }
        return resFunBuilding;
    }

    private void addBuildPlayerPower(int id, long value) {

        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }


    }

    public NewBuildProxy(Set<Long> rfbIds,Set<Long> productions, String areaKey){
        this.areaKey = areaKey;
        for (Long id : rfbIds) {
            ResFunBuilding rfb = BaseDbPojo.get(id, ResFunBuilding.class, areaKey);
            rfbs.add(rfb);
        }
        for (Long id : productions){
            Production production = BaseDbPojo.get(id,Production.class,areaKey);
            this.productions.add(production);
        }
        init();
    }


    public int speedCost(int secend) {
        int num = secend / 60;
        if (secend % 60 > 0) {
            num += 1;
        }
        if(num < 0){
            num = -num;
        }
        return num;
    }

    //根据建筑类型获得确认建筑属于资源还是功能
    public int getBuildTypeByType(int buildType) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BUILDSHEET, "type", buildType);
        if (jsonObject == null) {
            return 0;
        }
        return jsonObject.getInt("typesheet");
    }

    //根据建筑类型获得确认建筑属于资源还是功能
    public int getBuildTypeBypower(int buildType) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BUILDSHEET, "type", buildType);
        if (jsonObject == null) {
            return 0;
        }
        JSONArray array = jsonObject.getJSONArray("power");
        if (array.length() > 0) {
            return array.getInt(0);
        }
        return 0;
    }

    //根据建筑类型获得建筑生产加速消耗的道具
    public int getSpeedLevelneedItem(int buildType, int index) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.BUILDSHEET, "type", buildType);
        JSONArray jsonArray = jsonObject.getJSONArray("prospeeditem");
        return jsonArray.getInt(index);
    }

    public int gethadWaitQueue() {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int hadWaitQueue = vipProxy.getVipNum(ActorDefine.VIP_WAITQUEUE) + ResFunBuildDefine.MIN_WAITQUEUE;
        return hadWaitQueue;
    }


    /**
     * getResFunBuildingByIndexsmallyType()获取建筑
     * @param index 建筑位置标志
     * @param smallType 建筑类型
     * @return ResFunBuilding
     */
    private ResFunBuilding getResFunBuildingByIndexsmallyType(int smallType, int index) {
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getIndex() == index && rfb.getSmallType() == smallType) {
                return rfb;
            }
        }
        return null;
    }

    /**
     * getResFunBuildingByIndexsmallyType()获取建筑
     * @param index 建筑位置标志
     * @param bigType 建筑大类（资源，兵营等）
     * @return ResFunBuilding
     */
    private ResFunBuilding getResFunBuildingByIndexbigType(int bigType, int index) {
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getIndex() == index && rfb.getBigType() == bigType) {
                return rfb;
            }
        }
        return null;
    }

    /**
     * getResFuBuildStateByIndex()
     *  获得某个建筑的功能开启状态
     * @param index 建筑位置标志
     * @param smallType 建筑类型
     * @return boolean
     */
    public boolean getResFuBuildStateByindex(int smallType, int index) {
        ResFunBuilding combuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        if (ResFunBuildDefine.BASEBUILDLIST.contains(smallType)) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.FUNTIONBUILD, index);
            if(combuild.getLevel()<jsonObject.getInt("condition")){
                return false;
            }
        } else {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOUCEBUILD, index);
            if(combuild.getLevel()<jsonObject.getInt("openlv")){
                return false;
            }
        }
        return true;
    }

    /**
     * isCanBuildType()
     *  是否可以建造该类型
     * @param index 建筑位置标志
     * @param smallType 建筑类型
     * @return boolean
     */
    public boolean isCanBuildType(int smallType, int index) {
        if (ResFunBuildDefine.BASEBUILDLIST.contains(smallType)) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.FUNTIONBUILD, index);
            if(jsonObject.getInt("type")==smallType){
                return true;
            }
        } else {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOUCEBUILD, index);
            JSONArray array=jsonObject.getJSONArray("canbulid");
            for(int i=0;i<array.length();i++){
                if(array.getInt(i)==smallType){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * getResFunBuildingByIndexsmallyType()
     *  获得根据建筑类型获取等级
     * @param index 建筑位置标志
     * @param smallType 建筑类型
     * @return boolean
     */
    public int getResFuBuildLevelBysmallType(int smallType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(smallType, index);
        if (building != null) {
            return building.getLevel();
        }
        return -1;
    }

    /**
     * saveResFunBuildings()
     *  保存修改的建筑信息
     * @return void
     */
    public void saveResFunBuildings() {
        List<ResFunBuilding> rfs = new ArrayList<>();
        synchronized (changeRfbs) {
            while (true) {
                ResFunBuilding rfb = changeRfbs.poll();
                if (rfb == null) {
                    break;
                }
                rfs.add(rfb);
            }
        }
        for (ResFunBuilding rfb : rfs) {
            rfb.save();
        }
    }

    private LinkedList<ResFunBuilding> changeRfbs = new LinkedList<>();

    /**
     * pushBuildToChangeList()
     *  将建筑缓存到修改列表中
     * @param rfb 建筑
     */
    private void pushBuildToChangeList(ResFunBuilding rfb) {
        //插入更新队列
        synchronized (changeRfbs) {
            if (!changeRfbs.contains(rfb)) {
                changeRfbs.offer(rfb);
            }
        }
        init();
    }

    /**
     * setFinishLevelTime()
     *  设置建筑升级完成时间
     * @param buildType 建筑类型
     * @param index 建筑位置标志
     * @param time 建筑完成时间
     */
    public void setFinishLevelTime(int buildType, int index, long time) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        building.setNextLevelTime(time);
        pushBuildToChangeList(building);
    }

    /**
     * changeResFuBuildType()
     *  获得根据建筑类型获取等级
     * @param index 建筑位置标志
     * @param buildType 原始建筑类型
     * @param changeType 改变后的建筑类型
     */
    public void changeResFuBuildType(int buildType, int index, int changeType) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building != null) {
            building.setSmallType(changeType);
            building.save();
        }
    }

    /**
     * changeResFuBuildType()
     *  获得根据建筑类型获取等级
     * @param index 建筑位置标志
     * @param buildType 原始建筑类型
     * @param level 改变后的建筑等级
     */
    public void changeResFuBuildLevel(int buildType, int index, int level) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building != null) {
            building.setLevel(level);
            building.save();
        }
    }

    /**
     * getBuildLevelNum()
     *  获得建筑剩余升级队列
     * @return int
     */
    public int getBuildLevelNum() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemBuffProxy itemBuffProxy = getGameProxy().getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        int expandBuildSize = itemBuffProxy.getValidBuildSize();
        int hadbuildSize = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_buildsize);
        int times = hadbuildSize + expandBuildSize;
        int hasnum = 0;
        for (ResFunBuilding build : rfbs) {
            if (build.getNextLevelTime()/1000 > GameUtils.getServerTime()) {
                hasnum++;
            }
        }
        return times - hasnum;
    }

    //获得某种建筑的最高等级
    public int getMaxLevelByBuildType(int buildType) {
        int level = 0;
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getSmallType() == buildType) {
                if (rfb.getLevel() > level) {
                    level = rfb.getLevel();
                }
            }
        }
        return level;
    }

    //获得某种类型建筑数量
    public int getBuildTypeNum(int buildType) {
        int num = 0;
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getSmallType() == buildType && rfb.getLevel() > 0) {
                num++;
            }
        }
        return num;
    }

    /**
     * buildingLevelUp()
     *  请求建筑升级，执行倒计时
     * @param index 建筑位置标志
     * @param buildType 原始建筑类型
     * @param type 1普通升级 2金币升级
     * @return int
     */
    public int buildingLevelUp(int buildType, int index, int type) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if (building == null && ResFunBuildDefine.RESOUCETYPELIST.contains(buildType)) {
            building = getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, index);
        }
        if (building == null) {
            //判断一下是不是0级创建
            JSONObject defineObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.FUNTIONBUILD, index);
            if (defineObject != null){
                ResFunBuilding comBuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
                if (defineObject.getInt("condition") <= comBuild.getLevel()){
                    creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, defineObject.getInt("type"), defineObject.getInt("ID"), defineObject.getInt("initlevel"), 1);
                    building = getResFunBuildingByIndexsmallyType(buildType, index);
                }
            }
        }
        if (building == null) {
            return ErrorCodeDefine.M2800001_1;
        }
        if(building.getSmallType()!=0 && building.getSmallType()!=buildType){
            return ErrorCodeDefine.M2800001_1;
        }
        long nextLevelTime = building.getNextLevelTime();
        int timeGap = (int) (nextLevelTime /1000 - GameUtils.getServerTime());//时间差值
        if (timeGap > TimerDefine.TOLERNACE_TIME) {
            return ErrorCodeDefine.M2800001_2;//超过服务器可允许的时间差值了
        }
        if(!getResFuBuildStateByindex(buildType, index)){
            return ErrorCodeDefine.M2800001_9;
        }
        if(!isCanBuildType(buildType, index)){
            return ErrorCodeDefine.M2800001_10;
        }
        if (getBuildLevelNum() <= 0){
            return ErrorCodeDefine.M2800001_6;
        }

        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (getBuildTypeByType(buildType) == 1) {
            //资源建筑
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", building.getLevel());
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", building.getLevel() + 1);
            if (jsonUp == null) {
                return ErrorCodeDefine.M2800001_3;
            }

            if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) < jsonObject.getInt("commandlv")) {
                return ErrorCodeDefine.M2800001_5;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            int coin = jsonObject.getInt("gold");
            if (type == 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    int num = array.getInt(1);
                    num = (int) Math.ceil(num * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE)) / 100.0);
                    if (playerProxy.getPowerValue(typeId) < num) {
                        return ErrorCodeDefine.M2800001_4;
                    }
                }
            } else {
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < coin) {
                    return ErrorCodeDefine.M2800001_8;
                }
            }

            if (type == 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    int num = array.getInt(1);
                    num= (int) Math.ceil(num*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE))/100.0);
                    playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_BUID_LEVELUP);
                }
            } else {
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, coin, LogDefine.LOST_BUID_LEVELUP);
            }
            if (building.getSmallType() != buildType){
                changeResFuBuildType(building.getSmallType(), index, buildType);
            }
            long time = jsonObject.getInt("time");
            long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
            time = (long) Math.ceil(time / (1 + power / 100.0));
            long needtime = GameUtils.getServerDate().getTime() + (time * 1000);
            //设置建筑升级完成时间
            setFinishLevelTime(buildType, index, needtime);
        }else {
            //功能建筑
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel() + 1);
            if (jsonUp == null) {
                return ErrorCodeDefine.M2800001_3;
            }
            if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) < jsonObject.getInt("commandlv")) {
                return ErrorCodeDefine.M2800001_5;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            int coin = jsonObject.getInt("gold");
            if (type == 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    int num = array.getInt(1);
                    num= (int) Math.ceil(num*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE))/100.0);
                    if (playerProxy.getPowerValue(typeId) < num) {
                        return ErrorCodeDefine.M2800001_4;
                    }
                }
            } else {
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < coin) {
                    return ErrorCodeDefine.M2800001_8;
                }
            }

            //扣除费用
            if (type == 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    int num = array.getInt(1);
                    num= (int) Math.ceil(num*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE))/100.0);
                    playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_BUID_LEVELUP);
                }
            } else {
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, coin, LogDefine.LOST_BUID_LEVELUP);
            }
            long time = jsonObject.getInt("time");
            long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
            time = (long) Math.ceil(time / (1 + power / 100.0));
            long needTime = GameUtils.getServerDate().getTime() + (time * 1000);
            //设置建筑升级完成时间
            setFinishLevelTime(buildType, index, needTime);
        }
        return 0;
    }

    /**
     * doBuildLevelUp()
     *  客户端倒计时完毕，建筑升级
     * @param index 建筑位置标志
     * @param buildType 原始建筑类型
     * @return int
     */
    public int doBuildLevelUp(int buildType, int index) {

        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
//        if (building == null) {
//            building = getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, index);
//        }

        if (building == null) {
            return ErrorCodeDefine.M2800002_1;
        }
        if (building.getNextLevelTime() <= 0){
            return ErrorCodeDefine.M2800002_2;
        }
        int timeGap = (int) (building.getNextLevelTime()/1000 - GameUtils.getServerTime());
        if (timeGap > TimerDefine.TOLERNACE_TIME) {
            return timeGap;//超过服务器可允许的时间差值了
        }
        int level = building.getLevel();
        building.setLevel(level + 1);
        //升级完毕的把之间置回0
        setFinishLevelTime(buildType, index, 0l);
//        building.setNextLevelTime(0l);
        pushBuildToChangeList(building);
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL, level + 1, playerProxy, buildType);

        if (buildType == ResFunBuildDefine.BUILDE_TYPE_COMMOND){
            //是个司令部的话要判断一下是不是有空地开启了
            List<JSONObject> openList = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RESOUCEBUILD, "openlv", level + 1);
            if (openList != null){
                for (JSONObject openDefine : openList){
                    creaResFunbuild(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, 0, openDefine.getInt("ID"), 0, 1);
                }
            }
        }
        playerProxy.upBuilderOrCreate(buildType, level+1);
        //增加相关任务条件
        TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
        taskProxy.doaddcompleteness(TaskDefine.TASK_TYPE_BUILDING_LV,1,0);
        taskProxy.doaddcompleteness(TaskDefine.TASK_TYPE_BUILDING_NUM,1,0);
        taskProxy.doaddcompleteness(TaskDefine.TASK_TYPE_BUILDLEVEUP_TIMES,1,0);
        return 0;
    }


    /**
     * cancelBuildLevelUp()
     *  取消升级
     * @param index 建筑位置标志
     * @param buildType 原始建筑类型
     * @return int
     */
    public int cancelBuildLevelUp(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building == null) {
            return ErrorCodeDefine.M2800003_1;//该建筑不存在
        }
        if (0 == building.getNextLevelTime()) {
            //建筑没有在升级中
            return ErrorCodeDefine.M2800003_2;//建筑没有在升级中
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (ResFunBuildDefine.RESOUCETYPELIST.contains(building.getSmallType())) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                long addValue = (long) array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100;
                playerProxy.addPowerValue(array.getInt(0), (int) addValue, LogDefine.GET_CANCEL_BUILD_LEVELUP);
            }
            setFinishLevelTime(buildType, index,0l);
        } else {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                long addValue = (long) array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100;
                playerProxy.addPowerValue(array.getInt(0), (int) addValue, LogDefine.GET_CANCEL_BUILD_LEVELUP);
            }
            setFinishLevelTime(buildType, index, 0l);
        }
        return 0;
    }

    /**
     * speedBuildLevelUp()
     *  建筑升级加速
     * @param index 建筑位置标志
     * @param buildType 原始建筑类型
     * @return int
     */
    public int speedBuildLevelUp(int buildType, int index, int costType, PlayerReward reward) {
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType, index);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (resFunBuilding == null) {
            return ErrorCodeDefine.M280004_1;//该建筑不存在
        }
        if (0 == resFunBuilding.getNextLevelTime()) {
            //建筑没有在升级中
            return ErrorCodeDefine.M280004_2;
        }
        if (costType == 1) {
            //金币加速
            int time = (int) (resFunBuilding.getNextLevelTime() - GameUtils.getServerDate().getTime());
            if (time < 0){
                time = 0;
            }
            int cost = speedCost(time / 1000);
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                return ErrorCodeDefine.M280004_3;
            }
            if (cost > 0){
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_BUILDING_SPEEDLV);
            }
            setFinishLevelTime(buildType, index, GameUtils.getServerDate().getTime());//设置结束时间
//            BuildingLog baseLog = new BuildingLog(index, buildType, LogDefine.BUILDINGLEVELSPEEDBYCOIN, 0, 0, getResFuBuildLevelBysmallType(buildType, index));
//            baseLog.setCost(cost);
//            baseLogs.add(baseLog);
            doBuildLevelUp(buildType, index);//执行升级
            setFinishLevelTime(buildType, index, 0l);
        } else {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            //道具加速
            int typeId = ResFunBuildDefine.SPEEDBUILDLEVELUP.get(costType - 2);
            int hasnum = itemProxy.getItemNum(typeId);
            if (hasnum < 1) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", typeId);
                if (jsonObject == null) {
                    return ErrorCodeDefine.M280004_5;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < jsonObject.getInt("goldprice")) {
                    return ErrorCodeDefine.M280004_7;//金币不足
                }
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("goldprice"), LogDefine.LOST_ITEM_BUYANDUSE);
                //执行购买
                itemProxy.addItem(typeId, 1, LogDefine.GET_BUYITEMANDUSE);
            }
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
            itemProxy.reduceItemNum(typeId, 1, LogDefine.LOST_BUILDING_SPEEDLV);
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            rewardProxy.addItemToReward(reward, typeId, 1);
//            BuildingLog baseLog = new BuildingLog(index, buildType, LogDefine.BUILDINGLEVELSPEEDBYITEM, 0, 0, getResFuBuildLevelBysmallType(buildType, index));
//            baseLog.setItemCost(typeId);
//            baseLog.setItemCostNum(1);
//            baseLogs.add(baseLog);
            int reduceTime = jsonObject.getJSONArray("effect").getInt(0) * 1000 * 60;
            long needtime = resFunBuilding.getNextLevelTime() - (reduceTime);
            if (GameUtils.getServerDate().getTime() >= needtime) {
                doBuildLevelUp(buildType, index);
            }else{
                setFinishLevelTime(buildType, index, needtime);
                return (int) (needtime /1000 - GameUtils.getServerTime());
            }

        }
        return 0;
    }


    private void addProductionInfo(int buildType,int index,M28.BuildingInfo.Builder builder){
        for (Production production : productions){
            if (production.getBuildIndex() == index && production.getBuildType() == buildType){
                builder.addProductionInfos(getProductionInfo(production));
            }
        }
    }

    private M28.ProductionInfo getProductionInfo(Production production){
        M28.ProductionInfo.Builder info = M28.ProductionInfo.newBuilder();
        info.setNum(production.getNum());
        info.setOrder(production.getSort());
        int remainTime = production.getFinishTime() - GameUtils.getServerTime() ;
        if (remainTime < 0){
            remainTime = 0;
        }
        info.setRemainTime(remainTime);
        info.setState(production.getState());
        info.setTypeid(production.getTypeId());
        return info.build();
    }

    public M28.ProductionInfo getMaxSortProductionInfo(int buildType,int index){
        Production max = null;
        for (Production production : productions){
            if (production.getBuildIndex() == index && production.getBuildType() == buildType){
                if (max == null || max.getSort() < production.getSort()){
                    max = production;
                }
            }
        }
        if (max == null){
            return null;
        }else {
            return getProductionInfo(max);
        }
    }

    public List<M28.BuildingInfo> getAllBuildingInfo(){
        List<M28.BuildingInfo> res = new ArrayList<>();
        int level = getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        for (ResFunBuilding rfb : rfbs) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOUCEBUILD, rfb.getIndex());
            if(rfb.getSmallType() == 0){
                continue;
            }
            if (rfb.getBigType() == ResFunBuildDefine.BUILDE_TYPE_FUNTION || level >= jsonObject.getInt("openlv")) {
                M28.BuildingInfo info = getBuildingInfo(rfb);
                res.add(info);
            }else {
                System.out.println("还存在没开启的建筑呢？？");
            }
        }
        return res;
    }

    /**
     * getBuildingInfo()
     *  获取建筑信息
     * @param resFunBuilding 建筑
     * @return BuildingInfo
     */
    public M28.BuildingInfo getBuildingInfo(ResFunBuilding resFunBuilding){
        M28.BuildingInfo.Builder builder = M28.BuildingInfo.newBuilder();
        int buildType = resFunBuilding.getSmallType();
        int index = resFunBuilding.getIndex();
        builder.setBuildingType(buildType);
        builder.setIndex(index);

        addProductionInfo(buildType,index,builder);
        builder.setLevel(resFunBuilding.getLevel());
        int remainTime = (int) (resFunBuilding.getNextLevelTime()/1000 - GameUtils.getServerTime());
        if (remainTime < 0){
            remainTime = 0;
        }
        builder.setLevelTime(remainTime);
        if (buildType == ResFunBuildDefine.BUILDE_TYPE_SCIENCE){
            //科技馆。。。
            TechnologyProxy technologyProxy = getGameProxy().getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
            List<JSONObject> jsonArraylist = ConfigDataProxy.getConfigAllInfo(DataDefine.MUSEUM);
            for (JSONObject jsonObject : jsonArraylist) {
                int typeId = jsonObject.getInt("scienceType");
                int level = technologyProxy.getTechnologyLevelByType(typeId);
                if (level > 0){
                    M28.TechnologyInfo.Builder technologyInfo = M28.TechnologyInfo.newBuilder();
                    technologyInfo.setLevel(level);
                    technologyInfo.setTypeid(typeId);
                    builder.addTechnologyInfos(technologyInfo);
                }
            }
        }
        return builder.build();
    }


    /**
     * dropBuilding()
     *  拆除野外建筑
     * @param index 建筑位置标志
     * @return int
     */
    public int dropBuilding(int index) {
        ResFunBuilding building = getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, index);
        if (building == null) {
            return ErrorCodeDefine.M280005_1;//该建筑不存在
        }
        if (!ResFunBuildDefine.REMOVEBUILDLIST.contains(building.getSmallType())) {
            return ErrorCodeDefine.M280005_2;//该类型建筑不可以拆除
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.removeBuilder(building.getSmallType(), index);
        setFinishLevelTime(building.getSmallType(),index,0l);
        changeResFuBuildLevel(building.getSmallType(), index, 0);
        changeResFuBuildType(building.getSmallType(), index, 0);
        return 0;
    }

    /**
     * getProductNum()
     *  获得正在生产的数量
     * @param index 建筑位置标志
     * @param buildType 建筑类型
     * @return int
     */
    private int getProductNum(int buildType, int index){
        int num = 0;
        for (Production production : productions){
            if (production.getBuildIndex() == index && production.getBuildType() == buildType){
                num ++;
            }
        }
        return num;
    }

    /**
     * getLastCreateTime()
     *  获取该建筑的最后一个生产队伍的结束时间
     * @param index 建筑位置标志
     * @param buildType 建筑类型
     * @return int
     */
    private int getLastCreateTime(int buildType, int index){
        int startTime = GameUtils.getServerTime();
        for (Production production : productions){
            if (production.getBuildIndex() == index && production.getBuildType() == buildType){
                if (production.getFinishTime() > startTime){
                    startTime = production.getFinishTime();
                }
            }
        }
        return startTime;
    }

    private int setProductSortAndState(int buildType,int index,Production production){
        int sort = 0;
        int state = ResFunBuildDefine.PRODUCTION_STATE_WORKING;
        for (Production p : productions){
            if (p.getBuildIndex() == index && p.getBuildType() == buildType){
                if (sort < p.getSort()){
                    sort = p.getSort();//获得最大的顺序
                }
                if (p.getState() == ResFunBuildDefine.PRODUCTION_STATE_WORKING){
                    state = ResFunBuildDefine.PRODUCTION_STATE_WAITING;
                }
            }
        }
        production.setSort(sort+1);
        production.setState(state);
        if (state == ResFunBuildDefine.PRODUCTION_STATE_WORKING){
            //计算出结束时间
            production.setFinishTime(GameUtils.getServerTime()+production.getProductTime());
        }
        return sort+1;
    }

    /**
     * createProduction()
     *  创建一个生产队列
     * @param index 建筑位置标志
     * @param buildType 建筑类型
     * @param typeId 科技id
     * @return Production
     */
    private Production createProduction(int time,int buildType,int index,int num,int typeId){
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Production production = BaseDbPojo.create(Production.class,areaKey);
        production.setBuildIndex(index);
        production.setBuildType(buildType);
        production.setProductTime(time);
        production.setNum(num);
        production.setPlayerId(playerProxy.getPlayerId());
        production.setTypeId(typeId);
        production.save();
        setProductSortAndState(buildType, index, production);
        productions.add(production);
        playerProxy.getPlayer().addProduction(production.getId());

        return production;
    }

    /**
     * startNextProduction()
     *  将下一个生产队列设置为开始
     * @param index 建筑位置标志
     * @param buildType 建筑类型
     * @param nextStartTime 下个生产开始的时间
     */
    private void startNextProduction(int buildType, int index,int nextStartTime){
        Production minSort = null;
        for (Production p : productions){
            if (p.getBuildIndex() == index && p.getBuildType() == buildType){
                if (minSort == null || p.getSort() < minSort.getSort()){
                    minSort = p;
                }
            }
        }
        if (minSort != null){
            minSort.setState(ResFunBuildDefine.PRODUCTION_STATE_WORKING);
            //获得结束时间
            minSort.setFinishTime(nextStartTime + minSort.getProductTime());
            minSort.save();
        }
    }

    public int getWorkingSort(int buildType, int index){
        int sort = 0;
        for (Production p : productions){
            if (p.getBuildIndex() == index && p.getBuildType() == buildType){
                if (p.getState() == ResFunBuildDefine.PRODUCTION_STATE_WORKING){
                    sort = p.getSort();
                    break;
                }
            }
        }
        return sort;
    }

    /**
     * getProduction()
     *  获得生产队列
     * @param index 建筑位置标志
     * @param buildType 建筑类型
     * @param sort 顺序
     * @return Production
     */
    private Production getProduction(int buildType,int index,int sort){
        for (Production p : productions){
            if (p.getBuildIndex() == index && p.getBuildType() == buildType && p.getSort() == sort){
                return p;
            }
        }
        return null;
    }
    /**
     * scienceIsCanLevelUp()
     *  检查科技是否正在升级中
     * @param index 建筑位置标志
     * @param typeId 科技id
     * @return boolean
     */
    private boolean scienceIsCanLevelUp(int index, int typeId){
        for (Production production : productions){
            if (production.getBuildType() == ResFunBuildDefine.BUILDE_TYPE_SCIENCE && production.getBuildIndex() == index){
                if (production.getTypeId() == typeId){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * builderProduction()
     *  客户端请求建筑生产
     * @param buildType 建筑类型
     * @param index 建筑位置标志
     * @param typeId 生产的配置表id
     * @param num 生产的数量/等级
     * @param reward 修改的佣兵/道具缓存
     * @return int
     */
    public int builderProduction(int buildType, int index, int typeId, int num, PlayerReward reward) {
        int rs = 0;
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int waitQueueSize = vipProxy.getVipNum(ActorDefine.VIP_WAITQUEUE) + ResFunBuildDefine.MIN_WAITQUEUE;
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType,index);
        if (getProductNum(ResFunBuildDefine.BUILDE_TYPE_TANK,index) >= waitQueueSize) {
            return ErrorCodeDefine.M280006_4;
        }
        if (resFunBuilding == null){
            return ErrorCodeDefine.M280006_29;
        }
        if (buildType == ResFunBuildDefine.BUILDE_TYPE_TANK) {
            rs = productSoldier(index,typeId,num,reward);
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_SCIENCE) {
            rs = productTechnology(index, typeId, num);
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_CREATEROOM) {
            rs = productItemMade(index, typeId, num, reward);
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_RREFIT) {
            rs = productArmRemould(index, typeId, num, reward);
        }
        return rs;
    }


    /**
     * productSoldier()
     *  兵营生产佣兵
     * @param index 建筑位置标志
     * @param typeId 生产的配置表id
     * @param num 生产的数量/等级
     * @param reward 修改的佣兵/道具缓存
     * @return int
     */
    private int productSoldier(int index, int typeId, int num, PlayerReward reward){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (num > UtilDefine.SODIER_CREATE_MAX_NUM) {
            return ErrorCodeDefine.M280006_1;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_PRODUCT, typeId);
        JSONArray jsonArray = jsonObject.getJSONArray("need");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int needid = array.getInt(0);
            int neednum = array.getInt(1) * num;
            if (playerProxy.getPowerValue(needid) < neednum) {
                return ErrorCodeDefine.M280006_2;
            }
        }
        JSONArray itemjsonArray = jsonObject.getJSONArray("itemneed");
        for (int i = 0; i < itemjsonArray.length(); i++) {
            JSONArray array = itemjsonArray.getJSONArray(i);
            int needid = array.getInt(0);
            int neednum = array.getInt(1) * num;
            if (itemProxy.getItemNum(needid) < neednum) {
                return ErrorCodeDefine.M280006_3;
            }
        }

        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) < jsonObject.getInt("commanderLv")) {
            return ErrorCodeDefine.M280006_5;
        }
        JSONArray json = jsonObject.getJSONArray("Lvneed");
        if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_TANK, index) < json.getInt(1)) {
            return ErrorCodeDefine.M280006_6;
        }

        //扣除费用
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray array = jsonArray.getJSONArray(i);
            int needid = array.getInt(0);
            int neednum = array.getInt(1) * num;
            playerProxy.reducePowerValue(needid, neednum, LogDefine.LOST_TANK_PRODUCTION);
        }

        for (int i = 0; i < itemjsonArray.length(); i++) {
            JSONArray array = itemjsonArray.getJSONArray(i);
            int needid = array.getInt(0);
            int neednum = array.getInt(1) * num;
            itemProxy.reduceItemNum(needid, neednum, LogDefine.LOST_TANK_PRODUCTION);
            rewardProxy.addItemToReward(reward, needid, neednum);
        }
        int lessTime = jsonObject.getInt("timeneed");
        int powertype = getBuildTypeBypower(ResFunBuildDefine.BUILDE_TYPE_TANK);
        if (powertype != 0) {
            long power = playerProxy.getPowerValue(powertype);
            lessTime = (int)Math.ceil(lessTime / (1 + power / 100.0));
        }
        lessTime = lessTime * num;
//        int finishTime = getLastCreateTime(ResFunBuildDefine.BUILDE_TYPE_TANK,index)+lessTime;
        createProduction(lessTime,ResFunBuildDefine.BUILDE_TYPE_TANK,index,num,typeId);
        return 0;
    }



    /**
     * productTechnology()
     *  科技馆升级科技
     * @param index 建筑位置标志
     * @param typeId 生产的配置表id
     * @param num 生产的数量/等级
     * @return int
     */
    private int productTechnology(int index, int typeId, int num) {
        TechnologyProxy technologyProxy = getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);

        int museumLevel = getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_SCIENCE, index);
        long prestigeLv = playerProxy.getPowerValue(PlayerPowerDefine.POWER_prestigeLevel);
        int technologyLv = technologyProxy.getTechnologyLevelByType(typeId);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        JSONObject jsonObj = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.SCIENCELV, "level", technologyLv,"scienceType",typeId);
        if (jsonObj == null) {
            return ErrorCodeDefine.M280006_8;
        } else {
            int reqSCenterLv = jsonObj.getInt("reqSCenterLv");
            int reqPrestigeLv = jsonObj.getInt("reqPrestigeLv");
            JSONArray needArray = jsonObj.getJSONArray("need");

            if (museumLevel < reqSCenterLv) {
                return ErrorCodeDefine.M280006_18;//科技馆等级不够
            } else if (prestigeLv < reqPrestigeLv) {
                return ErrorCodeDefine.M280006_19;//声望等级不够
            } else if (prestigeLv == 0 && museumLevel == 0) {
                return ErrorCodeDefine.M280006_20;//已是最高级
            }
            if (scienceIsCanLevelUp(index, typeId) == false) {
                return ErrorCodeDefine.M280006_26;//
            }
            for (int i = 0; i < needArray.length(); i++) {
                JSONArray array = needArray.getJSONArray(i);
                int power = array.getInt(0);
                int count = array.getInt(1);
                if (ResFunBuildDefine.RESOURCESCIENCE.contains(jsonObj.getInt("scienceType"))) {
                    count = (int) Math.ceil(count * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOURCE_SCIENCE_NEEEDRESOURCE)) / 100.0);
                }
                if (playerProxy.getPowerValue(power) < count) {
                    //所需资源不足
                    if (power == ResourceDefine.POWER_tael) {
                        return ErrorCodeDefine.M280006_21;
                    } else if (power == ResourceDefine.POWER_iron) {
                        return ErrorCodeDefine.M280006_22;
                    } else if (power == ResourceDefine.POWER_wood) {
                        return ErrorCodeDefine.M280006_23;
                    } else if (power == ResourceDefine.POWER_stones) {
                        return ErrorCodeDefine.M280006_24;
                    } else if (power == ResourceDefine.POWER_food) {
                        return ErrorCodeDefine.M280006_25;
                    }
                }
            }
            //扣掉升级所需资源
            for (int j = 0; j < needArray.length(); j++) {
                JSONArray json = needArray.getJSONArray(j);
                int count = json.getInt(1);
                int power = json.getInt(0);
                if (ResFunBuildDefine.RESOURCESCIENCE.contains(jsonObj.getInt("scienceType"))) {
                    count = (int) Math.ceil(count * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOURCE_SCIENCE_NEEEDRESOURCE)) / 100.0);
                }
                playerProxy.reducePowerValue(power, count, LogDefine.LOST_SCIENCE_LEVEUP);
            }

            int lessTime = jsonObj.getInt("time");
            int powertype = getBuildTypeBypower(ResFunBuildDefine.BUILDE_TYPE_SCIENCE);
            //等级加成
            if (powertype != 0) {
                long power = playerProxy.getPowerValue(powertype);
                lessTime = (int) Math.ceil(lessTime / (1 + power / 100.0));
            }
            lessTime = (int) Math.ceil(lessTime * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOURCE_SCIECE_SPEED)) / 100.0);
//            int finishTime = getLastCreateTime(ResFunBuildDefine.BUILDE_TYPE_SCIENCE,index)+lessTime;
            createProduction(lessTime,ResFunBuildDefine.BUILDE_TYPE_SCIENCE,index,num,typeId);
            return 0;
        }
    }


    /**
     * productItemMade()
     *  制造车间生产道具
     * @param index 建筑位置标志
     * @param typeId 生产的配置表id
     * @param num 生产的数量/等级
     * @param reward 修改的佣兵/道具缓存
     * @return int
     */
    private int productItemMade(int index, int typeId, int num, PlayerReward reward) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (num > UtilDefine.SODIER_CREATE_MAX_NUM) {
            return ErrorCodeDefine.M280006_7;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ITEM_MADE, "ID", typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M280006_8;
        }
        JSONArray rejsonArray = jsonObject.getJSONArray("need");
        for (int i = 0; i < rejsonArray.length(); i++) {
            JSONArray array = rejsonArray.getJSONArray(i);
            int id = array.getInt(0);
            int resnum = array.getInt(1) * num;
            if (playerProxy.getPowerValue(id) < resnum) {
                return ErrorCodeDefine.M280006_11;
            }
        }

        JSONArray itemjsonArray = jsonObject.getJSONArray("itemneed");
        for (int i = 0; i < itemjsonArray.length(); i++) {
            JSONArray array = itemjsonArray.getJSONArray(i);
            int itemId = array.getInt(0);
            int itemnum = array.getInt(1) * num;
            if (itemProxy.getItemNum(itemId) < itemnum) {
                return ErrorCodeDefine.M280006_12;
            }
        }
        //扣除费用
        for (int i = 0; i < rejsonArray.length(); i++) {
            JSONArray array = rejsonArray.getJSONArray(i);
            int id = array.getInt(0);
            int resnum = array.getInt(1) * num;
            playerProxy.reducePowerValue(id, resnum, LogDefine.LOST_MADE_PRODUTION);
        }
        for (int i = 0; i < itemjsonArray.length(); i++) {
            JSONArray array = itemjsonArray.getJSONArray(i);
            int itemId = array.getInt(0);
            int itemnum = array.getInt(1) * num;
            itemProxy.reduceItemNum(itemId, itemnum, LogDefine.LOST_MADE_PRODUTION);
            rewardProxy.addItemToReward(reward, itemId, itemnum);
        }
        //执行创建生产队列
        int lessTime = jsonObject.getInt("timeneed");
        int powertype = getBuildTypeBypower(ResFunBuildDefine.BUILDE_TYPE_CREATEROOM);
        if (powertype != 0) {
            long power = playerProxy.getPowerValue(powertype);
            lessTime = (int) Math.ceil(lessTime / (1 + power / 100.0));
        }
        lessTime = lessTime * num;
//        int finishTime = getLastCreateTime(ResFunBuildDefine.BUILDE_TYPE_CREATEROOM,index)+lessTime;
        createProduction(lessTime,ResFunBuildDefine.BUILDE_TYPE_CREATEROOM,index,num,typeId);
        return 0;
    }

    /**
     * productArmRemould()
     *  兵种改造改造佣兵
     * @param index 建筑位置标志
     * @param typeId 生产的配置表id
     * @param num 生产的数量/等级
     * @param reward 修改的佣兵/道具缓存
     * @return int
     */
    private int productArmRemould(int index, int typeId, int num, PlayerReward reward) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        if (num > UtilDefine.SODIER_CREATE_MAX_NUM) {
            return ErrorCodeDefine.M280006_13;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_REMOULD, typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M280006_14;
        }
        JSONArray buildarry = jsonObject.getJSONArray("Lvneed");
        if (getMaxLevelByBuildType(buildarry.getInt(0)) < buildarry.getInt(1)) {
            return ErrorCodeDefine.M280006_27;
        }
        if (playerProxy.getLevel() < jsonObject.getInt("commanderLv")) {
            return ErrorCodeDefine.M280006_28;
        }
        //资源判断
        int sodierId = jsonObject.getJSONArray("tankneed").getJSONArray(0).getInt(0);
        int sodierNum = jsonObject.getJSONArray("tankneed").getJSONArray(0).getInt(1) * num;
        if (soldierProxy.getSoldierNum(sodierId) < sodierNum) {
            return ErrorCodeDefine.M280006_15;
        }
        JSONArray resouarry = jsonObject.getJSONArray("need");
        for (int i = 0; i < resouarry.length(); i++) {
            JSONArray array = resouarry.getJSONArray(i);
            int reId = array.getInt(0);
            int renum = array.getInt(1) * num;
            if (playerProxy.getPowerValue(reId) < renum) {
                return ErrorCodeDefine.M280006_16;
            }
        }
        JSONArray itemouarry = jsonObject.getJSONArray("itemneed");
        for (int i = 0; i < itemouarry.length(); i++) {
            JSONArray array = itemouarry.getJSONArray(i);
            int itemId = array.getInt(0);
            int itemnum = array.getInt(1) * num;
            if (itemProxy.getItemNum(itemId) < itemnum) {
                return ErrorCodeDefine.M280006_17;
            }
        }
        //扣除资源
        soldierProxy.reduceSoldierNum(sodierId, sodierNum, 0, LogDefine.LOST_SOLDIER_GAIZAO);
        rewardProxy.addSoldierToReward(reward, sodierId, sodierNum);
        for (int i = 0; i < resouarry.length(); i++) {
            JSONArray array = resouarry.getJSONArray(i);
            int reId = array.getInt(0);
            int renum = array.getInt(1) * num;
            playerProxy.reducePowerValue(reId, renum, LogDefine.LOST_SOLDIER_GAIZAO);
        }
        for (int i = 0; i < itemouarry.length(); i++) {
            JSONArray array = itemouarry.getJSONArray(i);
            int itemId = array.getInt(0);
            int itemnum = array.getInt(1) * num;
            rewardProxy.addItemToReward(reward, itemId, itemnum);
            itemProxy.reduceItemNum(itemId, itemnum, LogDefine.LOST_BUILDING_SPEEDPRODUCTION);
        }
        //执行创建生产队列
        int lessTime = jsonObject.getInt("timeneed");
        int powertype = getBuildTypeBypower(ResFunBuildDefine.BUILDE_TYPE_RREFIT);
        if (powertype != 0) {
            long power = playerProxy.getPowerValue(powertype);
            lessTime = (int) Math.ceil(lessTime / (1 + power / 100.0));
        }
        lessTime = lessTime * num;
//        int finishTime = getLastCreateTime(ResFunBuildDefine.BUILDE_TYPE_RREFIT,index)+lessTime;
        createProduction(lessTime,ResFunBuildDefine.BUILDE_TYPE_RREFIT,index,num,typeId);
        return 0;
    }

    /**
     * doProductionFinish()
     *  客户端请求生产完成
     * @param index 建筑位置标志
     * @param buildType 建筑类型
     * @param order 生产的序号
     * @param reward 修改的佣兵/道具缓存
     * @return int
     */
    public int doProductionFinish(int buildType, int index, int order, PlayerReward reward) {
        Production production = getProduction(buildType,index,order);
        if (production == null){
            return ErrorCodeDefine.M280007_1;
        }
        int timeGap = production.getFinishTime() - GameUtils.getServerTime();
        if (timeGap > TimerDefine.TOLERNACE_TIME) {
            return timeGap;//超过服务器可允许的时间差值了
        }
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        switch (buildType){
            case ResFunBuildDefine.BUILDE_TYPE_TANK:
            case ResFunBuildDefine.BUILDE_TYPE_RREFIT:{
                rewardProxy.addSoldierToReward(reward, production.getTypeId(), production.getNum());
                rewardProxy.getRewardToPlayer(reward,LogDefine.GET_BUILD_PRODUCTION);
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                taskProxy.doaddcompleteness(TaskDefine.TASK_TYPE_CREATESODIER_NUM,production.getNum(),0);
                break;
            }
            case ResFunBuildDefine.BUILDE_TYPE_SCIENCE:{
                TechnologyProxy technologyProxy = getGameProxy().getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
                technologyProxy.addTechinologyLeve(production.getTypeId());
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                taskProxy.doaddcompleteness(TaskDefine.TASK_TYPE_SCIENCELV_TIMES,1,0);
                break;
            }
            case ResFunBuildDefine.BUILDE_TYPE_CREATEROOM:{
                rewardProxy.addItemToReward(reward, production.getTypeId(), production.getNum());
                rewardProxy.getRewardToPlayer(reward,LogDefine.GET_BUILD_PRODUCTION);
                break;
            }
            default:
                return ErrorCodeDefine.M280007_3;
        }
        //删除生产队伍，并让下一个开始
        finishProduction(production,GameUtils.getServerTime());
        return 0;
    }


    /**
     * autoFinishProduction()
     *  登陆的时候自动校验生产队列
     * @param production 生产队列
     * @param reward 修改的佣兵/道具缓存
     * @return int
     */
    private int autoFinishProduction(Production production ,PlayerReward reward){
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        switch (production.getBuildType()){
            case ResFunBuildDefine.BUILDE_TYPE_TANK:
            case ResFunBuildDefine.BUILDE_TYPE_RREFIT:{
                rewardProxy.addSoldierToReward(reward, production.getTypeId(), production.getNum());
                break;
            }
            case ResFunBuildDefine.BUILDE_TYPE_SCIENCE:{
                TechnologyProxy technologyProxy = getGameProxy().getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
                technologyProxy.addTechinologyLeve(production.getTypeId());
                TaskProxy taskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
                taskProxy.doaddcompleteness(TaskDefine.TASK_TYPE_SCIENCELV_TIMES,1,0);
                break;
            }
            case ResFunBuildDefine.BUILDE_TYPE_CREATEROOM:{
                rewardProxy.addItemToReward(reward, production.getTypeId(), production.getNum());
                break;
            }
            default:
                return ErrorCodeDefine.M280007_3;
        }
        int startTime = production.getFinishTime();
        //删除生产队伍，并让下一个开始
        finishProduction(production,startTime);
        return startTime;
    }

    public int cancelCreate(int buildType, int index, int sort, PlayerReward reward) {
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType, index);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (resFunBuilding == null) {
            return ErrorCodeDefine.M280008_1;//该建筑不存在
        }
        Production production = getProduction(buildType,index,sort);
        if (production == null){
            return ErrorCodeDefine.M280008_3;
        }
        int id = production.getTypeId();
        int num = production.getNum();
        //返还资源
        if (buildType == ResFunBuildDefine.BUILDE_TYPE_TANK) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_PRODUCT, id);
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                playerProxy.addPowerValue(array.getInt(0), num * (array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100), LogDefine.GET_CANCEL_PRODUCTION);
            }
            JSONArray itemArray = jsonObject.getJSONArray("itemneed");
            for (int i = 0; i < itemArray.length(); i++) {
                JSONArray array = itemArray.getJSONArray(i);
                ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
                itemProxy.addItem(array.getInt(0), num * array.getInt(1), LogDefine.GET_CANCEL_BUILD_LEVELUP);
                RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                rewardProxy.addItemToReward(reward, array.getInt(0), num * array.getInt(1));
            }
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_SCIENCE) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.SCIENCELV, id);
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                playerProxy.addPowerValue(array.getInt(0), array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100, LogDefine.GET_CANCEL_PRODUCTION);
            }
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_RREFIT) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_REMOULD, id);
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                playerProxy.addPowerValue(array.getInt(0), num * array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100, LogDefine.GET_CANCEL_PRODUCTION);
            }
            JSONArray soldierArray = jsonObject.getJSONArray("tankneed");
            for (int i = 0; i < soldierArray.length(); i++) {
                JSONArray array = soldierArray.getJSONArray(i);
                SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                soldierProxy.addSoldierNum(array.getInt(0), num * array.getInt(1), LogDefine.GET_CANCEL_BUILD_LEVELUP);
                RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                rewardProxy.addSoldierToReward(reward, array.getInt(0), num * array.getInt(1));
            }

        }
        //删除生产队伍，并让下一个开始
        finishProduction(production,GameUtils.getServerTime());
        return 0;
    }

    /**
     * finishProduction()
     *  删除完成的，并开始新的生产
     * @param production 生产队伍
     */
    private void finishProduction(Production production,int nextStartTime){
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.getPlayer().removeProduction(production.getId());
        productions.remove(production);
        startNextProduction(production.getBuildType(), production.getBuildIndex(),nextStartTime);
        production.del();
    }

    public int sepeedProduct(int buildType, int index, int order, int costType, PlayerReward reward) {
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType, index);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int rs = 0;
        if (resFunBuilding == null) {
            return ErrorCodeDefine.M280009_1;//该建筑不存在
        }
        Production production = getProduction(buildType,index,order);
        if (production == null || production.getState() != ResFunBuildDefine.PRODUCTION_STATE_WORKING){
            return ErrorCodeDefine.M280009_8;
        }
        int time = production.getFinishTime() - GameUtils.getServerTime();
        if (time < 0){
            time = 0;
        }
        if (costType == 1) {
            int cost = speedCost(time / 1000);
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                return ErrorCodeDefine.M280009_3;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_BUILDING_SPEEDPRODUCTION);
            //删除生产队伍，并让下一个开始
//            finishProduction(production,GameUtils.getServerTime());
            production.setFinishTime(0);
            doProductionFinish(buildType, index, order, reward);
        } else {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            //道具加速
            int typeId = getSpeedLevelneedItem(buildType, costType - 2);
            int hasnum = itemProxy.getItemNum(typeId);
            if (hasnum < 1) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", typeId);
                if (jsonObject == null) {
                    return ErrorCodeDefine.M280009_5;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < jsonObject.getInt("goldprice")) {
                    return ErrorCodeDefine.M280009_7;//金币不足
                }
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("goldprice"), LogDefine.LOST_ITEM_BUYANDUSE);
                //执行购买
                itemProxy.addItem(typeId, 1, LogDefine.GET_BUYITEMANDUSE);
            }
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            rewardProxy.addItemToReward(reward, typeId, 1);
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
            itemProxy.reduceItemNum(typeId, 1, LogDefine.LOST_BUILDING_SPEEDPRODUCTION);
            int reducetime = jsonObject.getJSONArray("effect").getInt(0) * 60;
            if (reducetime > time) {
                //完成了
                //删除生产队伍，并让下一个开始
//                finishProduction(production,GameUtils.getServerTime());
                production.setFinishTime(0);
                doProductionFinish(buildType, index, order, reward);
            }else {
                rs = time - reducetime;
                production.setFinishTime(GameUtils.getServerTime()+rs);
                production.save();
            }
        }
        return rs;
    }


    /**
     * buyBuildSize()
     *  vip购买建筑位
     * @return int
     */
    public int buyBuildSize() {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int canBuy = vipProxy.getVipNum(ActorDefine.VIP_BULIDQUEUE) - ResFunBuildDefine.MIN_BUILD_SIZE;
        int hadBuildSize = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_buildsize);
        long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
        JSONObject vipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", vipProxy.getMaxVIPLv());
        int vipMaxHadNum = vipInfo.getInt(ActorDefine.VIP_BULIDQUEUE);//vip 最大可拥有数
        int vipHadnum = vipProxy.getVipNum(ActorDefine.VIP_BULIDQUEUE);//当前vip可拥有数
        if (canBuy <= 0) {
            return ErrorCodeDefine.M280011_2;
        } else if (hadBuildSize >= vipMaxHadNum) {
            return ErrorCodeDefine.M280011_1;
        } else if (hadBuildSize >= vipHadnum && vipHadnum < vipMaxHadNum) {
            return ErrorCodeDefine.M280011_2;
        } else if (hadGold < ResFunBuildDefine.BUY_BUILD_SIZE_GOLD) {
            return ErrorCodeDefine.M280011_3;
        }
        int needGold = ((hadBuildSize - ResFunBuildDefine.MIN_BUILD_SIZE) + 1) * ResFunBuildDefine.MIN_BUY_BUILD_GOlD;
        playerProxy.addPowerValue(PlayerPowerDefine.POWER_buildsize, 1, LogDefine.GET_BUY_BUILD_POSITION);
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, needGold, LogDefine.LOST_BUY_BUILD_POSITION);
        if (playerProxy.getAutoBuildStateendtime()/1000 > GameUtils.getServerTime()){
            playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OPEN);
        }
        return 0;
    }


    /**
     * buyBuildSize()
     *  购买自动升级
     * @return int
     */
    public int buyAutoLevel() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < TimerDefine.BUILDAUTOLEVELPRICE) {
            return ErrorCodeDefine.M280012_1;
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, TimerDefine.BUILDAUTOLEVELPRICE, LogDefine.LOST_BUY_AUTO);
        long startTime = GameUtils.getServerDate().getTime();
        long autoBuildEndTime = playerProxy.getAutoBuildStateendtime();
        if (autoBuildEndTime > startTime){
            startTime = autoBuildEndTime;
        }else {
            playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OPEN);
        }
        playerProxy.setAutoBuildStateendtime(startTime + TimerDefine.BUILDAUTOLEVEL_ADDTIME);
        return 0;
    }

    /**
     * buyBuildSize()
     *  设置自动升级状态
     * @return int
     */
    public int setAutoLevelUp(int state){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long autoBuildEndTime = playerProxy.getAutoBuildStateendtime();
        if (autoBuildEndTime <= GameUtils.getServerTime()){
            return ErrorCodeDefine.M280013_1;
        }
        playerProxy.setAutoBuildState(state);
        return 0;
    }
}
