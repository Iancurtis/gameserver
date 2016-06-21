package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.define.*;
import com.znl.log.BuildingLog;
import com.znl.pojo.db.Technology;
import com.znl.proto.M10;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/11/24.
 */
public class TechnologyProxy extends BasicProxy {
    private Set<Technology> technologies = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (Technology te : technologies) {
            te.finalize();
        }
    }

    @Override
    protected void init() {
        expandPowerTechnology();
    }

    public void expandPowerTechnology(){
        super.expandPowerMap.clear();
        for (Technology tech : technologies) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.MUSEUM, "ID", tech.getType());
            if (jsonObject != null) {
                int lv = tech.getLevel();
                JSONArray jsonArray = jsonObject.getJSONArray("property");
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONArray array = jsonArray.getJSONArray(i);
                        if (lv > 0) {
                            addTechnologyPlayerPower(array.getInt(0), array.getLong(1) * lv);
                        }
                    }
                }
            }
        }
    }

    /**
     * 属性效果加成
     */
    private void addTechnologyPlayerPower(int id, long value) {
        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }
    }


    public TechnologyProxy(Set<Long> techIds,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : techIds) {
            Technology tech = BaseDbPojo.get(id, Technology.class,areaKey);
            technologies.add(tech);
        }
        init();
    }

    //保存
    public void saveTechnology() {
        List<Technology> technologyList = new ArrayList<Technology>();
        synchronized (changeTechnology) {
            while (true) {
                Technology technology = changeTechnology.poll();
                if (technology == null) {
                    break;
                }
                technologyList.add(technology);
            }
        }
        for (Technology technology : technologyList) {
            technology.save();
        }
    }

    private LinkedList<Technology> changeTechnology = new LinkedList<Technology>();

    private void pushTechnologyToChangeList(Technology technology) {
        synchronized (changeTechnology) {
            if (!changeTechnology.contains(technology)) {
                changeTechnology.offer(technology);
            }
        }
        init();
    }


    private long createTechnology(int type, int level, int state) {
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Technology tech = BaseDbPojo.create(Technology.class,areaKey);
        tech.setPlayerId(playerProxy.getPlayerId());
        tech.setType(type);
        tech.setLevel(level);
        tech.setLastblanceTime(GameUtils.getServerDate().getTime());
        tech.setNextLevelTime(GameUtils.getServerDate().getTime());
        tech.setState(state);
        playerProxy.addTechnologyToPlayer(tech.getId());
        tech.save();
        technologies.add(tech);
        init();
        return tech.getId();
    }

    public void addTechnology(int type, int level, int state) {
        createTechnology(type, level, state);
    }


    /**
     * 获取某个科技信息
     */
    private Technology getTechnologyByType(int type) {
        for (Technology tech : technologies) {
            if (tech.getType() == type) {
                return tech;
            }
        }
        return null;
    }

    /**
     * 初始化科技信息
     */
    public void initTechnology() {
        GameProxy gameProxy = super.getGameProxy();
        ResFunBuildProxy resFunProxy = gameProxy.getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        int museumLevel = resFunProxy.getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_FUNTION, ResFunBuildDefine.BUILDE_TYPE_SCIENCE);
        List<JSONObject> techlist = ConfigDataProxy.getConfigAllInfo(DataDefine.MUSEUM);
        if (techlist.size() > 0) {
            for (JSONObject json : techlist) {
                if (getTechnologyByType(json.getInt("scienceType")) == null) {
                    if (museumLevel >= json.getInt("reqSCenterLv")) {
                        addTechnology(json.getInt("scienceType"), 0, 1);
                    } else {
                        addTechnology(json.getInt("scienceType"), 0, 0);
                    }
                }
            }
        }
    }

    /**
     * 获得某个科技的功能开启状态：1开启，0未
     */
    public int getTechnologyStateByType(int type) {
        Technology technology = getTechnologyByType(type);
        if (technology != null) {
            return technology.getState();
        } else {
            return 0;
        }
    }

    /**
     * 获得某个科技的等级
     */
    public int getTechnologyLevelByType(int type) {
        Technology technology = getTechnologyByType(type);
        if (technology != null) {
            return technology.getLevel();
        } else {
            return -1;
        }
    }


    /**
     * 科技升级
     */
    public int technologyLevelUp(int buildType, int index, int typeId, int num, List<BaseLog> baseLogs) {

        GameProxy gameProxy = super.getGameProxy();
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        VipProxy vipProxy = gameProxy.getProxy(ActorDefine.VIP_PROXY_NAME);
        int hadWaitQueue = vipProxy.getVipNum(ActorDefine.VIP_WAITQUEUE) + ResFunBuildDefine.MIN_WAITQUEUE;
        ResFunBuildProxy resFunProxy = gameProxy.getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        int museumLevel = resFunProxy.getResFuBuildLevelBysmallType(buildType, index);
        long prestigeLv = playerProxy.getPowerValue(PlayerPowerDefine.POWER_prestigeLevel);
        int technologyLv = getTechnologyLevelByType(typeId);
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        JSONObject jsonObj = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.SCIENCELV, "level", technologyLv,"scienceType",typeId);
        if (jsonObj == null) {
            return ErrorCodeDefine.M100006_8;
        } else {
            int reqSCenterLv = jsonObj.getInt("reqSCenterLv");
            int reqPrestigeLv = jsonObj.getInt("reqPrestigeLv");
            JSONArray needArray = jsonObj.getJSONArray("need");

            if (museumLevel < reqSCenterLv) {
                return ErrorCodeDefine.M100006_18;//科技馆等级不够
            } else if (prestigeLv < reqPrestigeLv) {
                return ErrorCodeDefine.M100006_19;//声望等级不够
            } else if (prestigeLv == 0 && museumLevel == 0) {
                return ErrorCodeDefine.M100006_20;//已是最高级
            } else if (timerdbProxy.getCreateingNum(index) >= hadWaitQueue) {
                return ErrorCodeDefine.M100006_9;//队列已满
            }
            if (timerdbProxy.sienceIsCanLevel(index, typeId) == false) {
                return ErrorCodeDefine.M100006_26;//
            }
            for (int i = 0; i < needArray.length(); i++) {
                JSONArray jarray = needArray.getJSONArray(i);
                int power = jarray.getInt(0);
                int count = jarray.getInt(1);
                //TODO 资源类科技需添加判断
                if (ResFunBuildDefine.RESOURCESCIENCE.contains(jsonObj.getInt("scienceType"))) {
                    count = (int) Math.ceil(count * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOURCE_SCIENCE_NEEEDRESOURCE)) / 100.0);
                }
                if (playerProxy.getPowerValue(power) < count) {
                    //所需资源不足
                    if (power == ResourceDefine.POWER_tael) {
                        return ErrorCodeDefine.M100006_21;
                    } else if (power == ResourceDefine.POWER_iron) {
                        return ErrorCodeDefine.M100006_22;
                    } else if (power == ResourceDefine.POWER_wood) {
                        return ErrorCodeDefine.M100006_23;
                    } else if (power == ResourceDefine.POWER_stones) {
                        return ErrorCodeDefine.M100006_24;
                    } else if (power == ResourceDefine.POWER_food) {
                        return ErrorCodeDefine.M100006_25;
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
            int powertype = resFunProxy.getBuildTypeBypower(buildType);
            //等级加成
            if (powertype != 0) {
                long power = playerProxy.getPowerValue(powertype);
                lessTime = (int) Math.ceil(lessTime / (1 + power / 100.0));
            }

            int order = timerdbProxy.getCreateBigNum(index);
            long timeId = timerdbProxy.addTimer(TimerDefine.BUILD_CREATE, num, lessTime, TimerDefine.TIMER_REFRESH_NONE, index, order + 1, playerProxy);
            timerdbProxy.setAttrValue(timeId, 1, typeId);
            timerdbProxy.setAttrValue(timeId, 2, lessTime);
            long timeadd = (long) lessTime * 1000;
            //TODO 资源类科技 需以后做判断
            if (ResFunBuildDefine.RESOURCESCIENCE.contains(jsonObj.getInt("scienceType"))){
                timeadd = (long) Math.ceil(timeadd * (100 - activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOURCE_SCIECE_SPEED)) / 100.0);
            }
            long lasttime = timerdbProxy.getLastCreateTime(index, order) + (timeadd);
            timerdbProxy.setLastOperatinTime(TimerDefine.BUILD_CREATE, index, order + 1, lasttime);
            BuildingLog buildingLog=new BuildingLog(buildType,index,LogDefine.BUILDINGPRODUCT,typeId,1,resFunProxy.getResFuBuildLevelBysmallType(buildType,index));
            baseLogs.add(buildingLog);
            return 0;
        }
    }


    /**
     * 科技等级改变
     */
    public void addTechinologyLeve(int type) {
        Technology technology = getTechnologyByType(type);
        if (technology != null) {
            int level = technology.getLevel();
            technology.setLevel(level + 1);
            technology.save();
            init();
        }
    }

    /**
     * 获取所有科技信息
     */
    public List<M10.BuildingDetailInfo> getAllTechnologyInfo() {
        List<M10.BuildingDetailInfo> techList = new ArrayList<M10.BuildingDetailInfo>();
        for (Technology technology : technologies) {
            M10.BuildingDetailInfo.Builder builder = M10.BuildingDetailInfo.newBuilder();
            builder.setTypeid(technology.getType());
            builder.setNum(technology.getLevel());
            techList.add(builder.build());
        }
        return techList;
    }
}
