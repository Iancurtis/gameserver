package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.log.BuildingLog;
import com.znl.pojo.db.ResFunBuilding;
import com.znl.pojo.db.Timerdb;
import com.znl.proto.M10;
import com.znl.proto.M13;
import com.znl.proto.M3;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.tools.cmd.gen.AnyVals;
import sun.security.krb5.internal.LocalSeqNumber;
import sun.security.krb5.internal.ktab.KeyTabInputStream;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class ResFunBuildProxy extends BasicProxy {
    private Set<ResFunBuilding> rfbs = new ConcurrentHashSet<ResFunBuilding>();
    private Map<Integer,Set<ResFunBuilding>> rfblvmap=new HashMap<Integer,Set<ResFunBuilding>>();
    @Override
    public void shutDownProxy() {
        for (ResFunBuilding rfb : rfbs) {
            rfb.finalize();
        }
    }


    public ResFunBuildProxy(Set<Long> rfbIds, String areaKey) {
        this.areaKey = areaKey;
        for (Long id : rfbIds) {
            ResFunBuilding rfb = BaseDbPojo.get(id, ResFunBuilding.class, areaKey);
            rfbs.add(rfb);
        }
        init();
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

    private void addBuildPlayerPower(int id, long value) {

        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }


    }


    public void saveResFunBuildings() {
        List<ResFunBuilding> rfs = new ArrayList<ResFunBuilding>();
        synchronized (changerfbs) {
            while (true) {
                ResFunBuilding rfb = changerfbs.poll();
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

    private LinkedList<ResFunBuilding> changerfbs = new LinkedList<ResFunBuilding>();

    private void pushBuildToChangeList(ResFunBuilding rfb) {
        //插入更新队列
        synchronized (changerfbs) {
            if (!changerfbs.contains(rfb)) {
                changerfbs.offer(rfb);
            }
        }
        init();
    }


    //
    public void addResFunbuild(int bigType, int smallType, int index, int level, int state) {
        creaResFunbuild(bigType, smallType, index, level, state);
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
        rfb.setNextLevelTime(GameUtils.getServerDate().getTime());
        rfb.setState(state);
        playerProxy.addResFunBuildToPlayer(rfb.getId());
//        System.out.println("BigType()" + rfb.getBigType() + "SmallType" + rfb.getSmallType() + "Index" + rfb.getIndex());
        rfb.save();
        //增加繁荣度
        playerProxy.upBuilderOrCreate(smallType, level);
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

    private List<ResFunBuilding> getResFunBuildingTypeIndex(int bigType) {
        List<ResFunBuilding> list = new ArrayList<ResFunBuilding>();
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getBigType() == bigType) {
                list.add(rfb);
            }
        }
        return list;
    }


    private ResFunBuilding getResFunBuildingByIndexsmallyType(int smallType, int index) {
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getIndex() == index && rfb.getSmallType() == smallType) {
                return rfb;
            }
        }
        return null;
    }

    private ResFunBuilding getResFunBuildingByIndexbigType(int bigType, int index) {
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getIndex() == index && rfb.getBigType() == bigType) {
                return rfb;
            }
        }
        return null;
    }

    //初始化建筑
    public void initResFuBuild(List<List<Integer>> getlist) {
        //基地建筑初始化
        List<JSONObject> flist = ConfigDataProxy.getConfigAllInfo(DataDefine.FUNTIONBUILD);
        ResFunBuilding combuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        if (combuild == null) {
            JSONObject json = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.FUNTIONBUILD, "type", ResFunBuildDefine.BUILDE_TYPE_COMMOND);
            addResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 1);
            combuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        }
        List<ResFunBuilding> fblist = getResFunBuildingBybigType(ResFunBuildDefine.BUILDE_TYPE_FUNTION);
        for (JSONObject json : flist) {
            if (combuild.getLevel() == json.getInt("condition")) {
                getlist.add(Arrays.asList(json.getInt("type"), json.getInt("ID")));
            }
        }
        if (fblist.size() < flist.size()) {
            for (JSONObject json : flist) {
                if (getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("ID")) == null) {
                    if (combuild.getLevel() > json.getInt("condition")) {
                        addResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 1);
                    } else {
                        addResFunbuild(ResFunBuildDefine.BUILDE_TYPE_FUNTION, json.getInt("type"), json.getInt("ID"), json.getInt("initlevel"), 0);
                    }

                }
            }
            init();
        }
        //建设建筑初始化
        List<JSONObject> rlist = ConfigDataProxy.getConfigAllInfo(DataDefine.RESOUCEBUILD);
        List<ResFunBuilding> rblist = getResFunBuildingBybigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE);
        for (JSONObject json : rlist) {
            if (combuild.getLevel() == json.getInt("openlv")) {
                getlist.add(Arrays.asList(0, json.getInt("ID")));
            }
        }
        if (rblist.size() < rlist.size()) {
            for (JSONObject json : rlist) {
                if (getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, json.getInt("ID")) == null) {
                    if (combuild.getLevel() > json.getInt("openlv")) {
                        addResFunbuild(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, 0, json.getInt("ID"), 0, 1);
                    } else {
                        addResFunbuild(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, 0, json.getInt("ID"), 0, 0);
                    }
                }
            }
        }
    }

    //获得某个建筑的功能开启状态
    public boolean getResFuBuilStateByindex(int smalltype, int index) {
        ResFunBuilding combuild = getResFunBuildingByIndexsmallyType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        if (ResFunBuildDefine.BASEBUILDLIST.contains(smalltype)) {
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

    //是否可以建造该类型
    public boolean iscanbuidtype(int smalltype, int index) {
        if (ResFunBuildDefine.BASEBUILDLIST.contains(smalltype)) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.FUNTIONBUILD, index);
            if(jsonObject.getInt("type")==smalltype){
                return true;
            }
        } else {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOUCEBUILD, index);
           JSONArray array=jsonObject.getJSONArray("canbulid");
            for(int i=0;i<array.length();i++){
                if(array.getInt(i)==smalltype){
                    return true;
                }
            }
        }
        return false;
    }

    //设置某个建筑的功能开启状态
    public void setResFuBuilStateByindex(int bigType, int index, int state) {
        ResFunBuilding building = getResFunBuildingByIndexbigType(bigType, index);
        if (building != null) {
            building.setState(state);
            pushBuildToChangeList(building);
        }

    }


    //获得某个建筑的位置
    public int getResFuBuildIndexBybigsmall(int bigType, int smallType) {
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getBigType() == bigType && smallType == rfb.getSmallType()) {
                return rfb.getIndex();
            }
        }
        return -1;
    }

    //获得根据建筑类型获取等级
    public int getResFuBuildLevelBysmallType(int smallType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(smallType, index);
        if (building != null) {
            return building.getLevel();
        }
        return -1;
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

    //获得某个建筑的类型
    public int getResFuBuildType(int bigType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexbigType(bigType, index);
        if (building != null) {
            return building.getSmallType();
        }
        return -1;
    }


    //获得建筑通过大类和index
    public ResFunBuilding getResFuBuildByBigtypeIndex(int bigType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexbigType(bigType, index);
        return  building;
    }

    //改变建筑类型
    public void changeResFuBuildType(int buildType, int index, int changeType) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building != null) {
            building.setSmallType(changeType);
            building.save();
        }
    }

    //改变建筑类型
    public void changeResFuBuildLevel(int buildType, int index, int level) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building != null) {
            building.setLevel(level);
            building.save();
        }
    }


    //改变建筑等级
    public void addResFuBuildLeve(int builtype, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(builtype, index);
        if (building == null) {
            building = getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, index);
        }
        if (building != null) {
            int level = building.getLevel();
            building.setLevel(level + 1);
            pushBuildToChangeList(building);
            ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL, level + 1, playerProxy, builtype);
        }
    }

    //获取建筑所有
    public List<M10.BuildingInfo> getBuildingInfos() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        List<M10.BuildingInfo> list = new ArrayList<M10.BuildingInfo>();
        int level = getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
        for (ResFunBuilding rfb : rfbs) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOUCEBUILD, rfb.getIndex());
            if (rfb.getBigType() == ResFunBuildDefine.BUILDE_TYPE_FUNTION) {
                M10.BuildingInfo.Builder builder = M10.BuildingInfo.newBuilder();
                builder.setIndex(rfb.getIndex());
                builder.setLevel(rfb.getLevel());
                builder.setBuildingType(rfb.getSmallType());
                long time = 0;
                if (rfb.getNextLevelTime() != 0) {

                    time = rfb.getNextLevelTime() - GameUtils.getServerDate().getTime();
                }
                if (time < 0) {
                    time = 0;
                }
                int dbTime = timerdbProxy.getTimerlesTime(TimerDefine.BUILDING_LEVEL_UP, rfb.getSmallType(), rfb.getIndex());
                builder.setLevelTime(dbTime);
//                builder.setLevelTime((int) time / 1000);
                builder.addAllProductionInfos(timerdbProxy.getProductionInfo(rfb.getIndex(),rfb.getSmallType()));
                builder.addAllBuildingDetailInfos(getBuildingDetailInfo(rfb.getSmallType(), rfb.getIndex()));
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                int powertype = getBuildTypeBypower(rfb.getSmallType());
                if (powertype != 0) {
                    builder.setSpeedRate((int) playerProxy.getPowerValue(powertype));
                }
                builder.setProductNum(gethadWaitQueue());
                list.add(builder.build());
            } else if (level >= jsonObject.getInt("openlv")) {
                M10.BuildingInfo.Builder builder = M10.BuildingInfo.newBuilder();
                builder.setIndex(rfb.getIndex());
                builder.setLevel(rfb.getLevel());
                builder.setBuildingType(rfb.getSmallType());
                long time = 0;
                if (rfb.getNextLevelTime() != 0) {
                    time = rfb.getNextLevelTime() - GameUtils.getServerDate().getTime();
                }
                if (time < 0) {
                    time = 0;
                }
                int dbTime = timerdbProxy.getTimerlesTime(TimerDefine.BUILDING_LEVEL_UP, rfb.getSmallType(), rfb.getIndex());
                builder.setLevelTime(dbTime);
//                builder.setLevelTime((int) time / 1000);
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                int powertype = getBuildTypeBypower(rfb.getSmallType());
                if (powertype != 0) {
                    builder.setSpeedRate((int) playerProxy.getPowerValue(powertype));
                }
                builder.setProductNum(gethadWaitQueue());
                list.add(builder.build());
            }

        }
        return list;
    }

    //获得建筑信息通过大类和index
    public M10.BuildingInfo getBuildingInfoBbyBigtypeIndex(int bigtype, int index) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        ResFunBuilding rfb = getResFuBuildByBigtypeIndex(bigtype, index);
        if(rfb==null){
            return null;
        }
        M10.BuildingInfo.Builder builder = M10.BuildingInfo.newBuilder();
        builder.setIndex(rfb.getIndex());
        builder.setLevel(rfb.getLevel());
        builder.setBuildingType(rfb.getSmallType());
        long time = rfb.getNextLevelTime() - GameUtils.getServerDate().getTime();
        if (time < 0) {
            time = 0;
        }
        int dbTime = timerdbProxy.getTimerlesTime(TimerDefine.BUILDING_LEVEL_UP, rfb.getSmallType(), rfb.getIndex());
        builder.setLevelTime(dbTime);
//        builder.setLevelTime((int) time / 1000);
        builder.addAllProductionInfos(timerdbProxy.getProductionInfo(rfb.getIndex(),rfb.getSmallType()));
        builder.addAllBuildingDetailInfos(getBuildingDetailInfo(rfb.getSmallType(), rfb.getIndex()));
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int powertype = getBuildTypeBypower(rfb.getSmallType());
        if (powertype != 0) {
            builder.setSpeedRate((int) playerProxy.getPowerValue(powertype));
        }
        builder.setProductNum(gethadWaitQueue());
        return builder.build();
    }


    public M10.BuildingInfo getBuildingInfo(int buildType, int index) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        ResFunBuilding rfb = getResFunBuildingByIndexsmallyType(buildType, index);
        if(rfb==null){
            return null;
        }
        M10.BuildingInfo.Builder builder = M10.BuildingInfo.newBuilder();
        builder.setIndex(rfb.getIndex());
        builder.setLevel(rfb.getLevel());
        builder.setBuildingType(rfb.getSmallType());
        long time = rfb.getNextLevelTime() - GameUtils.getServerDate().getTime();
        if (time < 0) {
            time = 0;
        }
        int dbTime = timerdbProxy.getTimerlesTime(TimerDefine.BUILDING_LEVEL_UP, rfb.getSmallType(), rfb.getIndex());
        builder.setLevelTime(dbTime);
//        builder.setLevelTime((int) time / 1000);
        builder.addAllProductionInfos(timerdbProxy.getProductionInfo(rfb.getIndex(),rfb.getSmallType()));
        builder.addAllBuildingDetailInfos(getBuildingDetailInfo(rfb.getSmallType(), rfb.getIndex()));
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int powertype = getBuildTypeBypower(buildType);
        if (powertype != 0) {
            builder.setSpeedRate((int) playerProxy.getPowerValue(powertype));
        }
        builder.setProductNum(gethadWaitQueue());
        return builder.build();
    }


    //获得某个类型建筑信息
    public List<M10.BuildingInfo> getBuildingInfobytype(int buildType) {
        List<M10.BuildingInfo> infos = new ArrayList<M10.BuildingInfo>();
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        for (ResFunBuilding rfb : rfbs) {
            if (rfb.getSmallType() == buildType) {
                M10.BuildingInfo.Builder builder = M10.BuildingInfo.newBuilder();
                builder.setIndex(rfb.getIndex());
                builder.setLevel(rfb.getLevel());
                builder.setBuildingType(rfb.getSmallType());
                long time = rfb.getNextLevelTime() - GameUtils.getServerDate().getTime();
                if (time < 0) {
                    time = 0;
                }
                int dbTime = timerdbProxy.getTimerlesTime(TimerDefine.BUILDING_LEVEL_UP, rfb.getSmallType(), rfb.getIndex());
                builder.setLevelTime(dbTime);
//                builder.setLevelTime((int) time / 1000);
                builder.addAllProductionInfos(timerdbProxy.getProductionInfo(rfb.getIndex(),rfb.getSmallType()));
                builder.addAllBuildingDetailInfos(getBuildingDetailInfo(rfb.getSmallType(), rfb.getIndex()));
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                int powertype = getBuildTypeBypower(buildType);
                if (powertype != 0) {
                    builder.setSpeedRate((int) playerProxy.getPowerValue(powertype));
                }
                builder.setProductNum(gethadWaitQueue());
                infos.add(builder.build());
            }
        }
        return infos;
    }

    //设置建筑的结算时间
    public void setBuilderCountTime(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        building.setLastblanceTime(GameUtils.getServerDate().getTime());
        pushBuildToChangeList(building);
    }

    public long getBuilderLasetCountTime(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        return building.getLastblanceTime();
    }

    //设置建筑升级完成时间
    public void setFinishLevelTime(int buildType, int index, long time) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        building.setNextLevelTime(time);
        pushBuildToChangeList(building);
    }

    public void setBuildSmallyType(int bigType, int index, int value) {
        ResFunBuilding building = getResFunBuildingByIndexbigType(bigType, index);
        if (building != null) {
            building.setSmallType(value);
            pushBuildToChangeList(building);
        }
    }

    //获得建筑升级完成时间
    public int getFinishLevelTime(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        long time = building.getNextLevelTime() - GameUtils.getServerDate().getTime();
        if (time < 0) {
            time = 0;
        }
        return (int) time;
    }

    //拆除资源建筑
    public void deleResouBuilding(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        building.setSmallType(0);
        building.setLevel(0);
        pushBuildToChangeList(building);
    }

    //建筑升级
    public int buildingLeveUp(int buildType, int index, int type, List<Integer> powerlist,int lvtype , int upLevel) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        if (building == null) {
            building = getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, index);
        }
        if (building == null) {
            return ErrorCodeDefine.M100001_1;
        }
        if(building.getSmallType()!=0){
            if(building.getSmallType()!=buildType){
                return ErrorCodeDefine.M100001_11;
            }
        }

        long nextLevelTime = timerdbProxy.getLastOperatinTime(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex());
        int timeGap = (int) (nextLevelTime /1000 - GameUtils.getServerTime());//时间差值
        if (timeGap > TimerDefine.TOLERNACE_TIME) {
            return ErrorCodeDefine.M100001_2;//超过服务器可允许的时间差值了
        }
        if (timerdbProxy.getBuildLeveNum() <= 0) {
            return ErrorCodeDefine.M100001_6;
        }
        if(getResFuBuilStateByindex(buildType,index)==false){
            return ErrorCodeDefine.M100001_9;
        }
        if(iscanbuidtype(buildType,index)==false){
            return ErrorCodeDefine.M100001_10;
        }

        if(upLevel > 0){
            if (upLevel > building.getLevel() + 1){
                //判断一下容差值，太大就不给了，不是那么大就直接设置升上去吧
                int upLevelTimeGap = 0;//计算升级到客户端要求的逻辑需要多少时间
                if (getBuildTypeByType(buildType) == 1) {
                    for (int level = building.getLevel();level<upLevel;level++){
                        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", level);
                        upLevelTimeGap += jsonObject.getInt("time");
                    }
                }else{
                    for (int level = building.getLevel();level<upLevel;level++){
                        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", buildType, "lv", level);
                        upLevelTimeGap += jsonObject.getInt("time");
                    }
                }
                if (upLevelTimeGap < TimerDefine.TOLERNACE_TIME){
                    building.setLevel(upLevel-1);
                }
            }else if(upLevel <= building.getLevel()){
                //要求的等级还没有比自己的等级高，不能升级了
                return ErrorCodeDefine.M100001_12;
            }
        }

        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (getBuildTypeByType(buildType) == 1) {
            //资源建筑
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", building.getLevel());
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", building.getLevel() + 1);
            if (jsonUp == null) {
                return ErrorCodeDefine.M100001_3;
            }
            if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) < jsonObject.getInt("commandlv")) {
                return ErrorCodeDefine.M100001_5;
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
                        return ErrorCodeDefine.M100001_4;
                    }
                }
            } else {
           /*     for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                if (playerProxy.getPowerValue(typeId) >= num) {
                    return ErrorCodeDefine.M100001_7;
                }
            }*/
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < coin) {
                    return ErrorCodeDefine.M100001_8;
                }
            }
            //扣除费用
            if (type == 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    powerlist.add(typeId);
                    int num = array.getInt(1);
                    num= (int) Math.ceil(num*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE))/100.0);
                    playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_BUID_LEVELUP);
                }
            } else {
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, coin, LogDefine.LOST_BUID_LEVELUP);
            }
            changeResFuBuildType(building.getSmallType(), index, buildType);
            long time = jsonObject.getInt("time");
            long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
            time = (long) Math.ceil(time / (1 + power / 100.0));
            long needtime = GameUtils.getServerDate().getTime() + (time * 1000);
           // needtime= (long) Math.ceil(needtime*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_SPEED))/100.0);
            //设置建筑升级完成时间
            setFinishLevelTime(buildType, index, needtime);
            //设置定时器
            timerdbProxy.addTimer(TimerDefine.BUILDING_LEVEL_UP, 0, (int) time, -1, building.getSmallType(), building.getIndex(), playerProxy);
            timerdbProxy.setIsAutolv(TimerDefine.BUILDING_LEVEL_UP,building.getSmallType(), building.getIndex(),lvtype);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex(), needtime);
            timerdbProxy.setIsAutolv(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex(),lvtype);
        } else {
            //功能建筑
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel() + 1);
            if (jsonUp == null) {
                return ErrorCodeDefine.M100001_3;
            }
            if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) < jsonObject.getInt("commandlv")) {
                return ErrorCodeDefine.M100001_5;
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
                        return ErrorCodeDefine.M100001_4;
                    }
                    System.out.print("");
                }
            } else {
         /*       for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    int num = array.getInt(1);
                    if (playerProxy.getPowerValue(typeId) >= num) {
                        return ErrorCodeDefine.M100001_7;
                    }
                }*/
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < coin) {
                    return ErrorCodeDefine.M100001_8;
                }
            }

            //扣除费用
            if (type == 1) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray array = jsonArray.getJSONArray(i);
                    int typeId = array.getInt(0);
                    int num = array.getInt(1);
                    num= (int) Math.ceil(num*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_REDUCE))/100.0);
                    powerlist.add(typeId);
                    playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_BUID_LEVELUP);
                    System.out.print("");
                }
            } else {
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, coin, LogDefine.LOST_BUID_LEVELUP);
            }
            long time = jsonObject.getInt("time");
            long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
            time = (long) Math.ceil(time / (1 + power / 100.0));
            long needtime = GameUtils.getServerDate().getTime() + (time * 1000);
           // needtime= (long) Math.ceil(needtime*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_SPEED))/100.0);
            //设置建筑升级完成时间
            setFinishLevelTime(buildType, index, needtime);
            //设置定时器
            timerdbProxy.addTimer(TimerDefine.BUILDING_LEVEL_UP, 0, (int) time, -1, building.getSmallType(), building.getIndex(), playerProxy);
            timerdbProxy.setIsAutolv(TimerDefine.BUILDING_LEVEL_UP,building.getSmallType(), building.getIndex(),lvtype);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex(), needtime);
            timerdbProxy.setIsAutolv(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex(),lvtype);
        }
        return 0;
    }

    /**************
     * 取消升级
     *******/
    private int cancelBuildLevelUp(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building == null) {
            return ErrorCodeDefine.M100003_1;//该建筑不存在
        }
        if (GameUtils.getServerDate().getTime() >= building.getNextLevelTime()) {
            //建筑没有在升级中
            return ErrorCodeDefine.M100003_2;//建筑没有在升级中
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (ResFunBuildDefine.RESOUCETYPELIST.contains(building.getSmallType())) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                long addValue = (long) array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100;
                playerProxy.addPowerValue(array.getInt(0), (int) addValue, LogDefine.GET_CANCEL_BUILD_LEVELUP);
            }
            //删除定时器
            timerdbProxy.delTimer(TimerDefine.BUILDING_LEVEL_UP, buildType, index);
            setFinishLevelTime(buildType, index, GameUtils.getServerDate().getTime());
        } else {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                long addValue = (long) array.getInt(1) * ResFunBuildDefine.CANCEL_LEVEL_RETURN / 100;
                playerProxy.addPowerValue(array.getInt(0), (int) addValue, LogDefine.GET_CANCEL_BUILD_LEVELUP);
            }

            //删除定时器
            timerdbProxy.delTimer(TimerDefine.BUILDING_LEVEL_UP, buildType, index);
            setFinishLevelTime(buildType, index, GameUtils.getServerDate().getTime());
        }

        return 0;
    }

    private int cancelCreate(int buildType, int index, int order, PlayerReward reward) {
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType, index);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SystemProxy systemProxy = getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (resFunBuilding == null) {
            return ErrorCodeDefine.M100003_1;//该建筑不存在
        }
        long time = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_CREATE, index, order);
        int num = timerdbProxy.getTimerNum(TimerDefine.BUILD_CREATE, index, order);
        time = time - GameUtils.getServerDate().getTime();
        if (time <= 0) {
            return ErrorCodeDefine.M100003_3;
        }
        int id = timerdbProxy.getAttr1(TimerDefine.BUILD_CREATE, index, order);
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
        //TODO 其它类型添加
        //删除oreder定时器
        timerdbProxy.delTimer(TimerDefine.BUILD_CREATE, index, order);
        timerdbProxy.modifBuildfinishTime(index, time, order);
        return 0;
    }

    /**************
     * 取消升级生产
     *******/
    public int cancelLevelCreate(int buildType, int index, int order, PlayerReward reward) {
        int rs = 0;
        if (order == -1) {
            rs = cancelBuildLevelUp(buildType, index);
        } else {
            rs = cancelCreate(buildType, index, order, reward);
        }

        return rs;
    }

    /*************
     * 拆除野外建筑
     ******************/
    public int dropBuilding(int buildType, int index) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        if (building == null) {
            return ErrorCodeDefine.M100005_1;//该建筑不存在
        }
        if (ResFunBuildDefine.REMOVEBUILDLIST.contains(buildType) == false) {
            return ErrorCodeDefine.M100005_2;//该类型建筑不可以拆除
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        //定时器删除
        timerdbProxy.delTimer(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), index);
        playerProxy.removeBuilder(building.getSmallType(), index);
        changeResFuBuildLevel(buildType, index, 0);
        changeResFuBuildType(buildType, index, 0);
        return 0;
    }


    //建筑升级加速
    private int speedbuildLevelup(int buildType, int index, int costType, List<BaseLog> baseLogs, PlayerReward reward, List<PlayerTask> playerTasks) {
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType, index);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SystemProxy systemProxy = getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (resFunBuilding == null) {
            return ErrorCodeDefine.M100004_1;//该建筑不存在
        }
        if (GameUtils.getServerDate().getTime() >= resFunBuilding.getNextLevelTime()) {
            //建筑没有在升级中
            return ErrorCodeDefine.M100004_2;
        }
        if (costType == 1) {
            //金币加速
            int time = (int) (resFunBuilding.getNextLevelTime() - GameUtils.getServerDate().getTime());
            int cost = speedCost(time / 1000);
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                return ErrorCodeDefine.M100004_3;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_BUILDING_SPEEDLV);
            BuildingLog baseLog = new BuildingLog(index, buildType, LogDefine.BUILDINGLEVELSPEEDBYCOIN, 0, 0, getResFuBuildLevelBysmallType(buildType, index));
            baseLog.setCost(cost);
            baseLogs.add(baseLog);
            systemProxy.doBuildingLevelUp(buildType, index);
            setFinishLevelTime(buildType, index, GameUtils.getServerTime());
            return 1;
        } else {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            //道具加速
            int typeId = ResFunBuildDefine.SPEEDBUILDLEVELUP.get(costType - 2);
            int hasnum = itemProxy.getItemNum(typeId);
            if (hasnum < 1) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", typeId);
                if (jsonObject == null) {
                    return ErrorCodeDefine.M100004_5;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < jsonObject.getInt("goldprice")) {
                    return ErrorCodeDefine.M100004_7;//金币不足
                }
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("goldprice"), LogDefine.LOST_ITEM_BUYANDUSE);
                //执行购买
                itemProxy.addItem(typeId, 1, LogDefine.GET_BUYITEMANDUSE);
            }
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
            itemProxy.reduceItemNum(typeId, 1, LogDefine.LOST_BUILDING_SPEEDLV);
            rewardProxy.addItemToReward(reward, typeId, 1);
            BuildingLog baseLog = new BuildingLog(index, buildType, LogDefine.BUILDINGLEVELSPEEDBYITEM, 0, 0, getResFuBuildLevelBysmallType(buildType, index));
            baseLog.setItemCost(typeId);
            baseLog.setItemCostNum(1);
            baseLogs.add(baseLog);
            int reducetime = jsonObject.getJSONArray("effect").getInt(0) * 1000 * 60;
            long needtime = resFunBuilding.getNextLevelTime() - (reducetime);
            setFinishLevelTime(buildType, index, needtime);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILDING_LEVEL_UP, resFunBuilding.getSmallType(), resFunBuilding.getIndex(), needtime);
            if (GameUtils.getServerDate().getTime() >= resFunBuilding.getNextLevelTime()) {
                systemProxy.doBuildingLevelUp(buildType, index);
            }
            if (needtime <= GameUtils.getServerDate().getTime()) {
                return 1;
            }
        }
        return 0;
    }


    //生产队加速完成
    private int sepeedProduct(int buildType, int index, int order, int costType, PlayerReward reward, List<BaseLog> baseLogs, List<PlayerTask> playerTasks) {
        ResFunBuilding resFunBuilding = getResFunBuildingByIndexsmallyType(buildType, index);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SystemProxy systemProxy = getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int rs = 0;
        if (resFunBuilding == null) {
            return ErrorCodeDefine.M100004_1;//该建筑不存在
        }
        long time = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_CREATE, index, order);
        time = time - GameUtils.getServerDate().getTime();
        if (time <= 0) {
            return ErrorCodeDefine.M100004_8;
        }
        if (costType == 1) {
            int cost = speedCost((int) (time / 1000));
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                return ErrorCodeDefine.M100004_3;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_BUILDING_SPEEDPRODUCTION);
            timerdbProxy.modifBuildfinishTime(index, time, order);
            BuildingLog buildingLog = new BuildingLog(buildType, index, LogDefine.BUILDINGPRODUCTSPEEDBYCOIN, 0, 0, getResFuBuildLevelBysmallType(buildType, index));
            buildingLog.setCost(cost);
            baseLogs.add(buildingLog);
            rs = 2;
        } else {
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            //道具加速
            int typeId = getSpeedLevelneedItem(buildType, costType - 2);
            int hasnum = itemProxy.getItemNum(typeId);
            if (hasnum < 1) {
                JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.SHOP, "itemID", typeId);
                if (jsonObject == null) {
                    return ErrorCodeDefine.M100004_5;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < jsonObject.getInt("goldprice")) {
                    return ErrorCodeDefine.M100004_7;//金币不足
                }
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("goldprice"), LogDefine.LOST_ITEM_BUYANDUSE);
                //执行购买
                itemProxy.addItem(typeId, 1, LogDefine.GET_BUYITEMANDUSE);
            }
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            rewardProxy.addItemToReward(reward, typeId, 1);
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ITEM_METIC, typeId);
            itemProxy.reduceItemNum(typeId, 1, LogDefine.LOST_BUILDING_SPEEDPRODUCTION);
            long reducetime = jsonObject.getJSONArray("effect").getInt(0) * 1000 * 60;
            if (reducetime > time) {
                reducetime = time;
                rs = 2;
            }
            timerdbProxy.modifBuildfinishTime(index, reducetime, order);
            BuildingLog buildingLog = new BuildingLog(buildType, index, LogDefine.BUILDINGPRODUCTSPEEDBYITEM, 0, 0, getResFuBuildLevelBysmallType(buildType, index));
            buildingLog.setItemCost(typeId);
            buildingLog.setItemCostNum(1);
            baseLogs.add(buildingLog);
        }
//        List<M3.TimeInfo> list = new ArrayList<M3.TimeInfo>();
//        timerdbProxy.checkBuildCreate(list, reward, playerTasks, baseLogs);
        return rs;
    }

    public int buildSpeed(int buildType, int index, int order, int costType, PlayerReward reward, List<BaseLog> baseLogs, List<PlayerTask> playerTasks) {
        int rs = 0;
        if (order == -1) {
            rs = speedbuildLevelup(buildType, index, costType, baseLogs, reward, playerTasks);
        } else {
            rs = sepeedProduct(buildType, index, order, costType, reward, baseLogs, playerTasks);
        }
        return rs;
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

    /*****************
     * 建筑生产
     ***********/
    public int builderProduction(int buildType, int index, int typeId, int num, PlayerReward reward, List<BaseLog> baseLogs) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int hadWaitQueue = vipProxy.getVipNum(ActorDefine.VIP_WAITQUEUE) + ResFunBuildDefine.MIN_WAITQUEUE;
        if (buildType == ResFunBuildDefine.BUILDE_TYPE_TANK) {
            if (num > UtilDefine.SODIER_CREATE_MAX_NUM) {
                return ErrorCodeDefine.M100006_1;
            }
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_PRODUCT, typeId);
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                int needid = array.getInt(0);
                int neednum = array.getInt(1) * num;
                if (playerProxy.getPowerValue(needid) < neednum) {
                    return ErrorCodeDefine.M100006_2;
                }
            }
            JSONArray itemjsonArray = jsonObject.getJSONArray("itemneed");
            for (int i = 0; i < itemjsonArray.length(); i++) {
                JSONArray array = itemjsonArray.getJSONArray(i);
                int needid = array.getInt(0);
                int neednum = array.getInt(1) * num;
                if (itemProxy.getItemNum(needid) < neednum) {
                    return ErrorCodeDefine.M100006_3;
                }
            }
            if (timerdbProxy.getCreateingNum(index) >= hadWaitQueue) {
                return ErrorCodeDefine.M100006_4;
            }
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) < jsonObject.getInt("commanderLv")) {
                return ErrorCodeDefine.M100006_5;
            }
            JSONArray json = jsonObject.getJSONArray("Lvneed");
            if (getResFuBuildLevelBysmallType(buildType, index) < json.getInt(1)) {
                return ErrorCodeDefine.M100006_6;
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
            long lessTime = jsonObject.getInt("timeneed");
            int powertype = getBuildTypeBypower(buildType);
            if (powertype != 0) {
                long power = playerProxy.getPowerValue(powertype);
                lessTime = (long) Math.ceil(lessTime / (1 + power / 100.0));
            }
            lessTime = lessTime * num;
            //添加计数器   smallType=工厂的index  otherType 队列的第几个
            int order = timerdbProxy.getCreateBigNum(index);
            long timeId = timerdbProxy.addTimer(TimerDefine.BUILD_CREATE, num, (int) lessTime, -1, index, order + 1, playerProxy);
            timerdbProxy.setAttrValue(timeId, 1, typeId);
            timerdbProxy.setAttrValue(timeId, 2, (int) lessTime);
            long timeadd = lessTime * 1000;
         //   timeadd= (long) Math.ceil(timeadd*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_CREATE_TANKE_SPEED))/100.0);
            long lasttime = timerdbProxy.getLastCreateTime(index, order) + (timeadd);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_CREATE, index, order + 1, lasttime);
            BuildingLog buildingLog = new BuildingLog(index, buildType, LogDefine.BUILDINGPRODUCT, typeId, num, getResFuBuildLevelBysmallType(buildType, index));
            baseLogs.add(buildingLog);
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_SCIENCE) {
            //科技...
            GameProxy gameProxy = super.getGameProxy();
            TechnologyProxy technologyProxy = getGameProxy().getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
            return technologyProxy.technologyLevelUp(buildType, index, typeId, num, baseLogs);
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_CREATEROOM) {
            return manufacturing(index, typeId, num, buildType, reward, baseLogs);
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_RREFIT) {
            return armsTransformation(index, typeId, num, buildType, reward, baseLogs);
        }
        return 0;

    }

    //兵种改造
    private int armsTransformation(int index, int typeId, int num, int buildType, PlayerReward reward, List<BaseLog> baseLogs) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int hadWaitQueue = vipProxy.getVipNum(ActorDefine.VIP_WAITQUEUE) + ResFunBuildDefine.MIN_WAITQUEUE;
        if (num > UtilDefine.SODIER_CREATE_MAX_NUM) {
            return ErrorCodeDefine.M100006_13;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_REMOULD, typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M100006_14;
        }
        if (timerdbProxy.getCreateingNum(index) >= hadWaitQueue) {
            return ErrorCodeDefine.M100006_4;
        }
        JSONArray buildarry = jsonObject.getJSONArray("Lvneed");
        if (getMaxLevelByBuildType(buildarry.getInt(0)) < buildarry.getInt(1)) {
            return ErrorCodeDefine.M100006_27;
        }
        if (playerProxy.getLevel() < jsonObject.getInt("commanderLv")) {
            return ErrorCodeDefine.M100006_28;
        }
        //资源判断
        int sodierId = jsonObject.getJSONArray("tankneed").getJSONArray(0).getInt(0);
        int sodierNum = jsonObject.getJSONArray("tankneed").getJSONArray(0).getInt(1) * num;
        if (soldierProxy.getSoldierNum(sodierId) < sodierNum) {
            return ErrorCodeDefine.M100006_15;
        }
        JSONArray resouarry = jsonObject.getJSONArray("need");
        for (int i = 0; i < resouarry.length(); i++) {
            JSONArray array = resouarry.getJSONArray(i);
            int reId = array.getInt(0);
            int renum = array.getInt(1) * num;
            if (playerProxy.getPowerValue(reId) < renum) {
                return ErrorCodeDefine.M100006_16;
            }
        }
        JSONArray itemouarry = jsonObject.getJSONArray("itemneed");
        for (int i = 0; i < itemouarry.length(); i++) {
            JSONArray array = itemouarry.getJSONArray(i);
            int itemId = array.getInt(0);
            int itemnum = array.getInt(1) * num;
            if (itemProxy.getItemNum(itemId) < itemnum) {
                return ErrorCodeDefine.M100006_17;
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
        long lessTime = jsonObject.getInt("timeneed");
        int powertype = getBuildTypeBypower(buildType);
        if (powertype != 0) {
            long power = playerProxy.getPowerValue(powertype);
            lessTime = (long) Math.ceil(lessTime / (1 + power / 100.0));
        }
        lessTime = lessTime * num;
        //添加计数器   smallType=工厂的index  otherType 队列的第几个
        int order = timerdbProxy.getCreateBigNum(index);
        long timeId = timerdbProxy.addTimer(TimerDefine.BUILD_CREATE, num, (int) lessTime, -1, index, order + 1, playerProxy);
        timerdbProxy.setAttrValue(timeId, 1, typeId);
        timerdbProxy.setAttrValue(timeId, 2, (int) lessTime);
        long timeadd = (long) lessTime * 1000;
     //   timeadd= (long) Math.ceil(timeadd*(100-activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_CHANGE_TANK_SPEED))/100.0);
        long lasttime = timerdbProxy.getLastCreateTime(index, order) + (timeadd);
        timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_CREATE, index, order + 1, lasttime);
        BuildingLog buildingLog = new BuildingLog(buildType, index, LogDefine.BUILDINGPRODUCT, typeId, num, getResFuBuildLevelBysmallType(buildType, index));
        baseLogs.add(buildingLog);
        return 0;


    }

    //制造车间生产
    private int manufacturing(int index, int typeId, int num, int buildtype, PlayerReward reward, List<BaseLog> baseLogs) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int hadWaitQueue = vipProxy.getVipNum(ActorDefine.VIP_WAITQUEUE) + ResFunBuildDefine.MIN_WAITQUEUE;
        if (num > UtilDefine.SODIER_CREATE_MAX_NUM) {
            return ErrorCodeDefine.M100006_7;
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ITEM_MADE, "ID", typeId);
        if (jsonObject == null) {
            return ErrorCodeDefine.M100006_8;
        }
        if (timerdbProxy.getCreateingNum(index) >= hadWaitQueue) {
            return ErrorCodeDefine.M100006_9;
        }
        JSONArray rejsonArray = jsonObject.getJSONArray("need");
        for (int i = 0; i < rejsonArray.length(); i++) {
            JSONArray array = rejsonArray.getJSONArray(i);
            int id = array.getInt(0);
            int resnum = array.getInt(1) * num;
            if (playerProxy.getPowerValue(id) < resnum) {
                return ErrorCodeDefine.M100006_11;
            }
        }

        JSONArray itemjsonArray = jsonObject.getJSONArray("itemneed");
        for (int i = 0; i < itemjsonArray.length(); i++) {
            JSONArray array = itemjsonArray.getJSONArray(i);
            int itemId = array.getInt(0);
            int itemnum = array.getInt(1) * num;
            if (itemProxy.getItemNum(itemId) < itemnum) {
                return ErrorCodeDefine.M100006_12;
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
        int powertype = getBuildTypeBypower(buildtype);
        if (powertype != 0) {
            long power = playerProxy.getPowerValue(powertype);
            lessTime = (int) Math.ceil(lessTime / (1 + power / 100.0));
        }
        lessTime = lessTime * num;
        //添加计数器   smallType=工厂的index  otherType 队列的第几个
        int order = timerdbProxy.getCreateBigNum(index);
        long timeId = timerdbProxy.addTimer(TimerDefine.BUILD_CREATE, num, lessTime, -1, index, order + 1, playerProxy);
        timerdbProxy.setAttrValue(timeId, 1, typeId);
        timerdbProxy.setAttrValue(timeId, 2, lessTime);
        long timeadd = (long) lessTime * 1000;
        long lasttime = timerdbProxy.getLastCreateTime(index, order) + timeadd;
        timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_CREATE, index, order + 1, lasttime);
        BuildingLog buildingLog = new BuildingLog(buildtype, index, LogDefine.BUILDINGPRODUCT, typeId, num, getResFuBuildLevelBysmallType(buildtype, index));
        baseLogs.add(buildingLog);
        return 0;
    }


    /*********
     * 建筑详细信息
     ******/
    public List<M10.BuildingDetailInfo> getBuildingDetailInfo(int buildType, int index) {
        List<M10.BuildingDetailInfo> list = new ArrayList<M10.BuildingDetailInfo>();
        if (buildType == ResFunBuildDefine.BUILDE_TYPE_TANK) {
            SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            List<JSONObject> jsonArraylist = ConfigDataProxy.getConfigAllInfo(DataDefine.ARM_PRODUCT);
            for (JSONObject jsonObject : jsonArraylist) {
                int typeId = jsonObject.getInt("ID");
                JSONObject json = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_PRODUCT, typeId);
                JSONArray jsonArray = json.getJSONArray("Lvneed");
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) >= json.getInt("commanderLv") && getResFuBuildLevelBysmallType(buildType, index) >= jsonArray.getInt(1)) {
                    int num = soldierProxy.getSoldierNum(typeId);
                    M10.BuildingDetailInfo.Builder builder = M10.BuildingDetailInfo.newBuilder();
                    builder.setNum(num);
                    builder.setTypeid(typeId);
                    list.add(builder.build());
                }
            }
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_SCIENCE) {
            //TODO 科技馆。。。
            TechnologyProxy technologyProxy = getGameProxy().getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
            List<JSONObject> jsonArraylist = ConfigDataProxy.getConfigAllInfo(DataDefine.MUSEUM);
            ResFunBuildProxy resFunProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
            int museumLevel = resFunProxy.getResFuBuildLevelBysmallType(buildType, index);
            for (JSONObject jsonObject : jsonArraylist) {
                int typeId = jsonObject.getInt("scienceType");
                int level = technologyProxy.getTechnologyLevelByType(typeId);
                M10.BuildingDetailInfo.Builder builder = M10.BuildingDetailInfo.newBuilder();
                builder.setTypeid(typeId);
                builder.setNum(level);
                list.add(builder.build());
            }


        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_CREATEROOM) {
            List<JSONObject> jsonArraylist = ConfigDataProxy.getConfigAllInfo(DataDefine.ITEM_MADE);
            ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
            for (JSONObject jsonObject : jsonArraylist) {
                int typeId = jsonObject.getInt("ID");
                int num = itemProxy.getItemNum(typeId);
                M10.BuildingDetailInfo.Builder builder = M10.BuildingDetailInfo.newBuilder();
                builder.setNum(num);
                builder.setTypeid(typeId);
                list.add(builder.build());
            }
        } else if (buildType == ResFunBuildDefine.BUILDE_TYPE_RREFIT) {
            List<JSONObject> jsonArraylist = ConfigDataProxy.getConfigAllInfo(DataDefine.ARM_REMOULD);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
            for (JSONObject jsonObject : jsonArraylist) {
                JSONArray typeNeedarray = jsonObject.getJSONArray("tankneed");
                JSONArray jsonArray = jsonObject.getJSONArray("Lvneed");
                int typeId = typeNeedarray.getJSONArray(0).getInt(0);
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_commandLevel) >= jsonObject.getInt("commanderLv") && getMaxLevelByBuildType(jsonArray.getInt(0)) >= jsonArray.getInt(1)) {
                    int sodiernum = soldierProxy.getSoldierNum(typeId);
                    M10.BuildingDetailInfo.Builder builder = M10.BuildingDetailInfo.newBuilder();
                    builder.setNum(sodiernum);
                    builder.setTypeid(jsonObject.getInt("ID"));
                    list.add(builder.build());
                }
            }
        }
        return list;
    }

    public M10.M100000.S2C getBuildInfoMsg() {
        M10.M100000.S2C.Builder s2c = M10.M100000.S2C.newBuilder();
        s2c.setRs(0);
        s2c.addAllBuildingInfos(getBuildingInfos());
        return s2c.build();
    }


    /*****
     * 物品购买使用
     ********/
    public int buyItemandUse(int id, List<Integer> itemlist) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.FillITEM, id);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        if (jsonObject == null) {
            return ErrorCodeDefine.M100007_5;
        }
        int itemId = jsonObject.getInt("itemID");
        int num = jsonObject.getInt("num");
        if (itemProxy.getItemNum(itemId) > 0) {
            return ErrorCodeDefine.M100007_6;//已经拥有不需要购买
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < jsonObject.getInt("price")) {
            return ErrorCodeDefine.M100007_7;//金币不足
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, jsonObject.getInt("price"), LogDefine.LOST_ITEM_BUYANDUSE);
        itemlist.add(itemId);
        //执行购买
        itemProxy.addItem(itemId, num, LogDefine.GET_BUYITEMANDUSE);
        PlayerReward reward = new PlayerReward();
        itemProxy.useitem(itemId, num, reward, 1);
        sendFunctionLog(FunctionIdDefine.BUY_BUILD_RESOURCE_FUNCTION_ID, id, itemId, num);
        return 0;
    }

    /**************
     * 8商店购买获得道具
     *********/
    public int shopBuyItem(int id, int num, PlayerReward reward) {
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.SHOP, id);
        if (jsonObject == null) {
            return ErrorCodeDefine.M100008_1;
        }
        if (num < 0) {
            num = -num;
        }
        if (num > 100) {
            return ErrorCodeDefine.M100008_2;
        }
        int price = jsonObject.getInt("goldprice") * num;
        int itemId = jsonObject.getInt("itemID");
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < price) {
            return ErrorCodeDefine.M100008_3;
        }
        //执行购买
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, price, LogDefine.LOST_SHOP_BUY);
        ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
        int jsonnNum = jsonObject.getInt("num");
        itemProxy.addItem(itemId, num * jsonnNum, LogDefine.GET_SHOP_BUYITEM);
        reward.addItemMap.put(itemId, num * jsonnNum);
//        itemlist.add(itemId);
        return 0;
    }

    public int buyBuildSizePrice(){
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int canBuy = vipProxy.getVipNum(ActorDefine.VIP_BULIDQUEUE) - ResFunBuildDefine.MIN_BUILD_SIZE;
        int hadBuildSize = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_buildsize);
        long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
        int needGold = ((hadBuildSize - ResFunBuildDefine.MIN_BUILD_SIZE) + 1) * ResFunBuildDefine.MIN_BUY_BUILD_GOlD;
        return needGold;
    }

    /**
     * 请求购买建筑位
     */
    public int askBuyBuildSize() {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int canBuy = vipProxy.getVipNum(ActorDefine.VIP_BULIDQUEUE) - ResFunBuildDefine.MIN_BUILD_SIZE;
        int hadBuildSize = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_buildsize);
        long hadGold = playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold);
        int needGold = ((hadBuildSize - ResFunBuildDefine.MIN_BUILD_SIZE) + 1) * ResFunBuildDefine.MIN_BUY_BUILD_GOlD;
        JSONObject vipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", vipProxy.getMaxVIPLv());
        int vipMaxHadNum = vipInfo.getInt(ActorDefine.VIP_BULIDQUEUE);//vip 最大可拥有数
        int vipHadnum = vipProxy.getVipNum(ActorDefine.VIP_BULIDQUEUE);//当前vip可拥有数
        if (canBuy <= 0) {
            return 2;
        } else if (hadBuildSize >= vipMaxHadNum) {
            return 1;
        } else if (hadBuildSize >= vipHadnum && vipHadnum < vipMaxHadNum) {
            return 2;
        } else if (hadGold < needGold) {
            return 3;
        }

        return needGold;
    }

    /**
     * vip购买建筑位
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
            return 2;
        } else if (hadBuildSize >= vipMaxHadNum) {
            return 1;
        } else if (hadBuildSize >= vipHadnum && vipHadnum < vipMaxHadNum) {
            return 2;
        } else if (hadGold < ResFunBuildDefine.BUY_BUILD_SIZE_GOLD) {
            return 3;
        }
        int needGold = ((hadBuildSize - ResFunBuildDefine.MIN_BUILD_SIZE) + 1) * ResFunBuildDefine.MIN_BUY_BUILD_GOlD;
        playerProxy.addPowerValue(PlayerPowerDefine.POWER_buildsize, 1, LogDefine.GET_BUY_BUILD_POSITION);
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, needGold, LogDefine.LOST_BUY_BUILD_POSITION);
        return 0;
    }

    //获得三个生产队列数量
    public int getProductNum() {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int sum = 0;
        //2 3 4 12
        if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_TANK, 2) > 0) {
            if (timerdbProxy.getCreateBigNum(2) == 0) {
                sum += 1;
            }
        }
        if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_TANK, 3) > 0) {
            if (timerdbProxy.getCreateBigNum(3) == 0) {
                sum += 1;
            }
        }
        if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_RREFIT, 11) > 0) {
            if (timerdbProxy.getCreateBigNum(11) == 0) {
                sum += 1;
            }
        }
        if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_SCIENCE, 12) > 0) {
            if (timerdbProxy.getCreateBigNum(12) == 0) {
                sum += 1;
            }
        }
        return sum;

    }

    public void addLevl(int type, int level, int index) {
        for (ResFunBuilding resFunBuilding : rfbs) {
            if (resFunBuilding.getSmallType() == type && resFunBuilding.getIndex() == index) {
                resFunBuilding.setLevel(level);
            }
        }
    }

    public void addLevl(int type, int level) {
        for (ResFunBuilding resFunBuilding : rfbs) {
            if (resFunBuilding.getSmallType() == type) {
                resFunBuilding.setLevel(level);
            }
        }
    }

    //获得某个建筑的升级所需要的时间还未做处理前
    private int getBuildLevelNeedTime(ResFunBuilding resFunBuilding) {
        if (getBuildTypeByType(resFunBuilding.getSmallType()) == 1) {
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", resFunBuilding.getSmallType(), "lv", resFunBuilding.getLevel());
            if (jsonUp == null) {
                return -1;
            }
            return jsonUp.getInt("time");
        } else {
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", resFunBuilding.getSmallType(), "lv", resFunBuilding.getLevel());
            if (jsonUp == null) {
                return -1;
            }
            return jsonUp.getInt("time");
        }
    }

    public void buildAutoLevelUp(List<M3.TimeInfo> m3info) {
        List<ResFunBuilding> list = new ArrayList<ResFunBuilding>();
        list.addAll(rfbs);
        SortUtil.anyProperSort(list, "getLevel", true);
        long time=System.currentTimeMillis();
        for (ResFunBuilding resFunBuilding : list) {
            if (resFunBuilding.getSmallType() != 0 && resFunBuilding.getLevel() != 0) {
                List<ResFunBuilding> newlist = getSameLevelBuild(resFunBuilding.getLevel());
                for (ResFunBuilding building : newlist) {
                    int rs = buildingLeveUp(building.getSmallType(), building.getIndex(), 1, new ArrayList<Integer>(),TimerDefine.AUTLUBUILDLEVE, -1);
                    if (rs == 0) {
                        System.err.println(building.getSmallType() + "正在升级" + building.getIndex());
                        M3.TimeInfo.Builder timeInfo = M3.TimeInfo.newBuilder();
                        timeInfo.setRemainTime(0);
                        timeInfo.setBigtype(TimerDefine.BUILDING_LEVEL_UP);
                        timeInfo.setSmalltype(building.getSmallType());
                        timeInfo.setOthertype(building.getIndex());
                        //System.err.println("========buildAutoLevelUp=================" + building.getSmallType() + "  " + building.getIndex());
                        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
                        timerdbProxy.addTimeDbToList(timeInfo.build(),m3info);
//                        m3info.add(timeInfo.build());
                    }
                }
            }
        }
        System.err.print(System.currentTimeMillis()-time);
    }


    public void buildOutLineAutoLevelUp(List<M3.TimeInfo> m3info, List<PlayerTask> playerTasks, List<BaseLog> baseLogs, Long nowtime, Set<Long> longSet) {
        SystemProxy systemProxy = getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
        List<ResFunBuilding> list = new ArrayList<ResFunBuilding>();
        list.addAll(rfbs);
        SortUtil.anyProperSort(list, "getLevel", true);
        systemProxy.checkOutLineBuildingLeveUp(m3info, playerTasks, baseLogs, nowtime);
        for (ResFunBuilding resFunBuilding : list) {
            if (resFunBuilding.getSmallType() != 0 && resFunBuilding.getLevel() != 0) {
                List<ResFunBuilding> newlist = getSameLevelBuild(resFunBuilding.getLevel());
                for (ResFunBuilding building : newlist) {
                    Long id = outlinebuilLevelTime(building.getSmallType(), building.getIndex(), nowtime);
//                    System.err.println(new Date(nowtime).toString() + "此时");
//                    System.err.println(id + "原因");
                    if (id > 0) {
//                        System.err.println(building.getSmallType() + "类型-------" + building.getIndex());
                        longSet.add(id);
                    }
                }
            }
        }
    }

    private List<ResFunBuilding> getSameLevelBuild(int level) {
        List<ResFunBuilding> list = new ArrayList<ResFunBuilding>();
     //   List<ResFunBuilding> newlist = new ArrayList<ResFunBuilding>();
        for (ResFunBuilding resFunBuilding : rfbs) {
            if (level == resFunBuilding.getLevel()) {
                int needtime = getBuildLevelNeedTime(resFunBuilding);
                resFunBuilding.setLvneedTime(needtime);
                list.add(resFunBuilding);
            }
        }
        SortUtil.anyProperSort(list,"getLvneedTime",true);
     /*   List<Long> newtime = new ArrayList<Long>();
        for (int i = 0; i < list.size(); i++) {
            int time = 2100000000;
            ResFunBuilding building = null;
            for (int j = 0; j < list.size(); j++) {
                int needtime = getBuildLevelNeedTime(list.get(j));
                if (needtime > 0 && needtime < time && !newlist.contains(list.get(j))) {
                    building = list.get(j);
                    time = needtime;
                }
            }
            if (building != null && !newlist.contains(building)) {
                newlist.add(building);
                newtime.add((long) time);
            }
        }*/
        return list;
    }


    //离线建筑升级
    public long outlinebuilLevelTime(int buildType, int index, long starTime) {
        ResFunBuilding building = getResFunBuildingByIndexsmallyType(buildType, index);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (building == null) {
            building = getResFunBuildingByIndexbigType(ResFunBuildDefine.BUILDE_TYPE_RESOUCE, index);
        }
        if (building == null) {
            return ErrorCodeDefine.M100001_1;
        }
        if (starTime < building.getNextLevelTime()) {
            return ErrorCodeDefine.M100001_2;
        }
        if (timerdbProxy.getBuildLeveNum() <= 0) {
            return ErrorCodeDefine.M100001_6;
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (getBuildTypeByType(buildType) == 1) {
            //资源建筑
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", building.getLevel());
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOUCEBUILDLEVEEFFECT, "type", buildType, "lv", building.getLevel() + 1);
            if (jsonUp == null) {
                return ErrorCodeDefine.M100001_3;
            }
            if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) < jsonObject.getInt("commandlv")) {
                return ErrorCodeDefine.M100001_5;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            int coin = jsonObject.getInt("gold");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                if (playerProxy.getPowerValue(typeId) < num) {
                    return ErrorCodeDefine.M100001_4;
                }
            }
            //扣除费用
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_BUID_LEVELUP);
            }

            changeResFuBuildType(building.getSmallType(), index, buildType);
            long time = jsonObject.getInt("time");
            long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
            time = (long) Math.ceil(time / (1 + power / 100.0));
            long needtime = starTime + (time * 1000);
            //设置建筑升级完成时间
            setFinishLevelTime(buildType, index, needtime);
            //设置定时器
            timerdbProxy.addTimer(TimerDefine.BUILDING_LEVEL_UP, 0, (int) time, -1, building.getSmallType(), building.getIndex(), playerProxy);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex(), needtime);
            return needtime;
        } else {
            //功能建筑
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel());
            JSONObject jsonUp = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.FUNTIONBUILDLEVEEFFECT, "type", building.getSmallType(), "lv", building.getLevel() + 1);
            if (jsonUp == null) {
                return ErrorCodeDefine.M100001_3;
            }
            if (getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1) < jsonObject.getInt("commandlv")) {
                return ErrorCodeDefine.M100001_5;
            }
            JSONArray jsonArray = jsonObject.getJSONArray("need");
            int coin = jsonObject.getInt("gold");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                if (playerProxy.getPowerValue(typeId) < num) {
                    return ErrorCodeDefine.M100001_4;
                }
            }

            //扣除费用
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray array = jsonArray.getJSONArray(i);
                int typeId = array.getInt(0);
                int num = array.getInt(1);
                playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_BUID_LEVELUP);
                System.out.print("");
            }
            long time = jsonObject.getInt("time");
            long power = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_buildspeedrate);
            time = (long) Math.ceil(time / (1 + power / 100.0));
            long needtime = starTime + (time * 1000);
            //设置建筑升级完成时间
            setFinishLevelTime(buildType, index, needtime);
            //设置定时器
            timerdbProxy.addTimer(TimerDefine.BUILDING_LEVEL_UP, 0, (int) time, -1, building.getSmallType(), building.getIndex(), playerProxy);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILDING_LEVEL_UP, building.getSmallType(), building.getIndex(), needtime);
            return needtime;
        }
    }


    /********
     * 购买自动升级
     ********/
    public int buyAutoLevel() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < TimerDefine.BUILDAUTOLEVELPRICE) {
            return ErrorCodeDefine.M100011_1;
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, TimerDefine.BUILDAUTOLEVELPRICE, LogDefine.LOST_BUY_AUTO);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int hastime = timerdbProxy.getTimerNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
        int state = playerProxy.getAutoBuildState();
        long lasttiem = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
        long endTime = GameUtils.getServerDate().getTime() - lasttiem;
        if (hastime > 0) {
          /*  if (state == TimerDefine.BUILDAUTOLEVEL_OFF) {
                //表示默认不开
                timerdbProxy.setNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, TimerDefine.BUILDAUTOLEVEL_ADDTIME / 1000 + hastime);
                playerProxy.setAutoBuildStateendtime(0l);
            }*/
            timerdbProxy.setNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, 0);
            playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OPEN);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, GameUtils.getServerDate().getTime() +hastime*1000+ TimerDefine.BUILDAUTOLEVEL_ADDTIME);
            playerProxy.setAutoBuildStateendtime(GameUtils.getServerDate().getTime()+hastime*1000 + TimerDefine.BUILDAUTOLEVEL_ADDTIME);
        } else {
            if (endTime < 0) {
                //拥有自动升级
                timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, lasttiem + TimerDefine.BUILDAUTOLEVEL_ADDTIME);
                playerProxy.setAutoBuildStateendtime(lasttiem + TimerDefine.BUILDAUTOLEVEL_ADDTIME);
            } else {
                playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OPEN);
                timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, GameUtils.getServerDate().getTime() + TimerDefine.BUILDAUTOLEVEL_ADDTIME);
                playerProxy.setAutoBuildStateendtime(GameUtils.getServerDate().getTime() + TimerDefine.BUILDAUTOLEVEL_ADDTIME);
            }
        }
        return 0;
    }


    /********
     * 改变自动升状态
     ********/
    public int changetAutoBuildState(int type) {
        if (type == TimerDefine.BUILDAUTOLEVEL_OPEN) {
            if (isHasAutoLevel() == false) {
                return ErrorCodeDefine.M100012_1;
            }
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long hastime = timerdbProxy.getTimerNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
        if (playerProxy.getAutoBuildState() == TimerDefine.BUILDAUTOLEVEL_OFF) {
            if (type == TimerDefine.BUILDAUTOLEVEL_OPEN) {
                timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, GameUtils.getServerDate().getTime() + (hastime * 1000));
                timerdbProxy.setNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, 0);
                playerProxy.setAutoBuildState(type);
                playerProxy.setAutoBuildStateendtime(GameUtils.getServerDate().getTime() + (hastime * 1000));
            }
        }
        if (playerProxy.getAutoBuildState() == TimerDefine.BUILDAUTOLEVEL_OPEN) {
            if (type == TimerDefine.BUILDAUTOLEVEL_OFF) {
                long lestime = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0) - GameUtils.getServerDate().getTime();
                timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, GameUtils.getServerDate().getTime());
                timerdbProxy.setNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, (int) (lestime / 1000));
                playerProxy.setAutoBuildState(type);
                playerProxy.setAutoBuildStateendtime(0l);
            }
        }
        return 0;
    }

    //判断有没有在自动升级
    public boolean isHasAutoLevel() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttiem = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
        long endTime = GameUtils.getServerDate().getTime() - lasttiem;
        if (endTime < 0) {
            return true;
        }
        if (timerdbProxy.getTimerNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0) > 0) {
            return true;
        }
        return false;
    }

    //判断某个时间是否有在自动升级
    public boolean isAutoLeveling(long nowTime) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttiem = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
        long endTime = nowTime - lasttiem;
        if (endTime >= 0) {
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            if (playerProxy.getAutoBuildState() == TimerDefine.BUILDAUTOLEVEL_OPEN) {
                playerProxy.setAutoBuildState(TimerDefine.BUILDAUTOLEVEL_OFF);
            }
            return false;
        }
        return true;
    }


}
