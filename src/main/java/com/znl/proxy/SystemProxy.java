package com.znl.proxy;

import com.google.protobuf.ByteString;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.log.BuildingLog;
import com.znl.log.ResourceOut;
import com.znl.pojo.db.ClientCache;
import com.znl.pojo.db.Player;
import com.znl.proto.M3;
import com.znl.utils.GameUtils;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class SystemProxy extends BasicProxy {

    boolean isfirst = true;
    boolean isLeveup = false;

    @Override
    public void shutDownProxy() {
        cacheMap.values().forEach(cache -> cache.finalize());
        cacheMap.clear();
    }

    @Override
    protected void init() {

    }

    public boolean isLeveup() {
        return isLeveup;
    }

    public void setLeveup(boolean leveup) {
        isLeveup = leveup;
    }

    public boolean isfirst() {
        return isfirst;
    }

    public void setIsfirst(boolean isfirst) {
        this.isfirst = isfirst;
    }

    public SystemProxy(String areaKey) {
        this.areaKey = areaKey;
    }

    private Map<Integer, ClientCache> cacheMap = new java.util.concurrent.ConcurrentHashMap<>();

    private long lastbufferTime = 0l;

    private boolean falg = false;
    private boolean boomcheck = false;

    private long startime = 0l;
    private long tonormaltime = 0l;

    //只会调用一次
    public M3.M30100.S2C getClientCacheInfos(Set<Long> cacheIds) {
        M3.M30100.S2C.Builder builder = M3.M30100.S2C.newBuilder();
        cacheIds.forEach(id -> {
            M3.ClientCacheInfo.Builder cacheInfoBuilder = M3.ClientCacheInfo.newBuilder();
            ClientCache clientCache = getClientCache(id);
            if (clientCache != null) {
                cacheInfoBuilder.setMsgType(clientCache.getMsgType());

                ByteString bs = ByteString.copyFrom(clientCache.getMsg());
                cacheInfoBuilder.setMsg(bs);
                builder.addCacheInfos(cacheInfoBuilder.build());
            }
        });

        return builder.build();
    }


    //只会调用一次
    public List<M3.ClientCacheInfo> getallClientCacheInfos() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Set<Long> cacheIds = playerProxy.getClientCacheIds();
        List<M3.ClientCacheInfo> list = new ArrayList<M3.ClientCacheInfo>();
        cacheIds.forEach(id -> {
            M3.ClientCacheInfo.Builder cacheInfoBuilder = M3.ClientCacheInfo.newBuilder();
            ClientCache clientCache = getClientCache(id);
            if (clientCache != null) {
                cacheInfoBuilder.setMsgType(clientCache.getMsgType());

                ByteString bs = ByteString.copyFrom(clientCache.getMsg());
                cacheInfoBuilder.setMsg(bs);
                list.add(cacheInfoBuilder.build());
            }
        });

        return list;
    }

    public ClientCache getClientCache(Long id) {
        ClientCache clientCache = BaseDbPojo.get(id, ClientCache.class, areaKey);
        if (clientCache == null) {
            return null;
        }
        cacheMap.put(clientCache.getMsgType(), clientCache);

        return clientCache;
    }

    public ClientCache updateClientCache(M3.ClientCacheInfo cacheInfo) {
        ClientCache clientCache = cacheMap.get(cacheInfo.getMsgType());
        if (clientCache == null) {
            clientCache = BaseDbPojo.create(ClientCache.class, areaKey);
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.addClientCacheId(clientCache.getId());
        }

        clientCache.setMsgType(cacheInfo.getMsgType());
        clientCache.setMsg(cacheInfo.getMsg().toByteArray());

        cacheMap.put(cacheInfo.getMsgType(), clientCache);

        super.offerDbPojo(clientCache);

        return clientCache;
    }

    /**
     * 检查所有的定时器
     **/
    public void checkAllTimer(List<M3.TimeInfo> m3info, PlayerReward reward, List<PlayerTask> playerTasks, List<BaseLog> baseLogs, Set<Integer> powerList) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //校验繁荣度的时候要加一下其他的数据校验
        long oldCommond = playerProxy.getPowerValue(PlayerPowerDefine.POWER_command);
        long oldBoom = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boom);
        long oldBoomLimit = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        ActivityProxy activityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        activityProxy.checkeTimeActivity(m3info);
        //活动过期删除检测
        //activityProxy.checkDelActivity();
       // activityProxy.checkAddActivity();
        Set<Long> set = playerProxy.getbuildLevelTime();
        lastbufferTime = 0l;
        checkResouse();
        checkOutLineAuto(m3info, playerTasks, baseLogs, set);
//        checkBuildingLeveUp(m3info, playerTasks, baseLogs);
        checkEnergyTimer(m3info, powerList);
        checkBoomTimer(playerProxy);
        checkArena(m3info);
        ItemBuffProxy itemBuffProxy = getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        itemBuffProxy.overTimeClearBuff(GameUtils.getServerDate().getTime());
//        timerdbProxy.checkBuildCreate(m3info, reward, playerTasks, baseLogs);


        for (BaseLog baseLog : baseLogs) {
            sendPorxyLog(baseLog);
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_command) != oldCommond) {
            powerList.add(PlayerPowerDefine.POWER_command);
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_boom) != oldBoom) {
            powerList.add(PlayerPowerDefine.POWER_boom);
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomUpLimit) != oldBoomLimit) {
            powerList.add(PlayerPowerDefine.POWER_boomUpLimit);
        }
    }


    public M3.M30000.S2C.Builder getTimerNotify(int init) {
//        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
//        GameProxy gameProxy = super.getGameProxy();
//        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
//        activityProxy.checkeTimeActivity();
//        Set<Long> set = playerProxy.getbuildLevelTime();
//        lastbufferTime = 0l;
//        checkResouse(baseLogs);
//        checkOutLineAuto(m3info, playerTasks, baseLogs, set);
//        checkBuildingLeveUp(m3info, playerTasks, baseLogs);
//        checkEnergyTimer();
//        checkBoomTimer(timerdbProxy, playerProxy);
//        checkArena();
//        ItemBuffProxy itemBuffProxy = getGameProxy().getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
//        itemBuffProxy.overTimeClearBuff(GameUtils.getServerDate().getTime());
//        timerdbProxy.checkBuildCreate(m3info, reward, playerTasks, baseLogs);
        List<M3.TimeInfo> infoList = new ArrayList<>();
        M3.M30000.S2C.Builder builder = M3.M30000.S2C.newBuilder();
        /*for (Timerdb tdb : timerdbProxy.getTdbs()) {
            if (tdb.getLestime() > 0) {
                M3.TimeInfo.Builder info = M3.TimeInfo.newBuilder();
                info.setBigtype(tdb.getType());
                info.setSmalltype(tdb.getSmallType());
                info.setOthertype(tdb.getOtherType());
                info.setRemainTime(tdb.getLestime());
                info.setIsAutoBuildLvTrigger(tdb.getIsAutoBuildLv());
//                if (init == 1) {
                info.setAttr1(tdb.getAttr1());
                info.setAttr2(tdb.getAttr2());
                info.setAttr3(tdb.getAttr3());
                info.setLasttime((int) (tdb.getLasttime() / 1000));
                info.setNum(tdb.getNum());
                info.setBegintime((int) (tdb.getBegintime() / 1000));
//                }
                info.setIsReset(0);
                infoList.add(info.build());
            } else if (tdb.getType() == TimerDefine.BUILD_AUTO_LEVLE_UP && init == 1) {
                M3.TimeInfo.Builder info = M3.TimeInfo.newBuilder();
                info.setBigtype(tdb.getType());
                info.setSmalltype(tdb.getSmallType());
                info.setOthertype(tdb.getOtherType());
                info.setRemainTime(tdb.getLestime());
                info.setAttr1(tdb.getAttr1());
                info.setAttr2(tdb.getAttr2());
                info.setAttr3(tdb.getAttr3());
                info.setLasttime((int) (tdb.getLasttime() / 1000));
                info.setNum(tdb.getNum());
                info.setBegintime((int) (tdb.getBegintime() / 1000));
                info.setIsReset(1);
                infoList.add(info.build());
            }
        }
        builder.addAllTimeInfos(infoList);*/
//        for (BaseLog baseLog : baseLogs) {
//            sendPorxyLog(baseLog);
//        }
        return builder;
    }


    //竞技场检验
    public void checkArena(List<M3.TimeInfo> m3info) {
    /*    TimerdbProxy timerProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lasttime = timerProxy.getLastOperatinTime(TimerDefine.ARENA_FIGHT, 0, 0);
        long time = lasttime - GameUtils.getServerDate().getTime();
        if (time <= 0) {
            time = 0;
        }
        M3.TimeInfo info = timerProxy.setLesTime(TimerDefine.ARENA_FIGHT, 0, 0, (int) time / 1000);
        if (m3info != null) {
            timerProxy.addTimeDbToList(info, m3info);
        }*/
    }

    //获得set最小的值
    public Long getSamllInSet(Set<Long> setlist) {
        long min = GameUtils.getServerDate().getTime();
        for (long id : setlist) {
            if (id < min) {
                min = id;
            }
        }
        return min;
    }

    public void checkOutLineAuto(List<M3.TimeInfo> m3info, List<PlayerTask> playerTasks, List<BaseLog> baseLogs, Set<Long> setlist) {
        long mintime = getSamllInSet(setlist);
        if (startime != 0l && mintime < GameUtils.getServerDate().getTime()) {
            ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
            if (resFunBuildProxy.isAutoLeveling(mintime)) {
                if (mintime < GameUtils.getServerDate().getTime()) {
                    resFunBuildProxy.buildOutLineAutoLevelUp(m3info, playerTasks, baseLogs, mintime, setlist);
                    Set<Long> outtime = new HashSet<>();
                    for (Long id : setlist) {
                        if (id <= mintime) {
                            outtime.add(id);
                        }
                    }
                    setlist.removeAll(outtime);
                }
            } else {
                startime = 0l;
                return;
            }
        } else {
            startime = 0l;
            return;
        }
        startime = mintime;
        checkOutLineAuto(m3info, playerTasks, baseLogs, setlist);
    }

    //资源时间检验
    public void checkResouse() {
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ActivityProxy activityProxy = getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        long stattime = playerProxy.getPlayer().getResourereftime();
        if (falg == false) {
            startime = stattime;
            falg = true;
        }
        if (boomcheck == false) {
            tonormaltime = stattime + feixun2normalNeedTime();
        }
        ItemBuffProxy itemBuffProxy = getGameProxy().getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        //产量
        long addtale = (long) Math.ceil(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_taelyield) * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOUCE_ADD)) / 100.0);
        long addiron = (long) Math.ceil(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_ironyield) * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOUCE_ADD)) / 100.0);
        long addwood = (long) Math.ceil(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_woodyield) * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOUCE_ADD)) / 100.0);
        long addstone = (long) Math.ceil(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_stonesyield) * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOUCE_ADD)) / 100.0);
        long addfood = (long) Math.ceil(playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_foodyield) * (100 + activityProxy.getEffectBufferPowerByType(ActivityDefine.ACTIVITY_CONDITION_RESOUCE_ADD)) / 100.0);
        //当前数量
        long tale = playerProxy.getPowerValue(PlayerPowerDefine.POWER_tael);
        long rion = playerProxy.getPowerValue(PlayerPowerDefine.POWER_iron);
        long wood = playerProxy.getPowerValue(PlayerPowerDefine.POWER_wood);
        long stones = playerProxy.getPowerValue(PlayerPowerDefine.POWER_stones);
        long food = playerProxy.getPowerValue(ResourceDefine.POWER_food);
        //容量
        long talelimt = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_taelcontent);
        long rionlimt = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_ironcontent);
        long woodlimt = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_woodcontent);
        long stoneslimt = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_stonescontent);
        long foodlimt = playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_foodcontent);
        long checktime = 0l;
        if (lastbufferTime == 0l || stattime >= lastbufferTime) {
            lastbufferTime = itemBuffProxy.getWillBeOverdueBuff(stattime);
            checktime = lastbufferTime;
        } else {
            checktime = lastbufferTime;
        }
        if (feixun2normalNeedTime() > 0 && lastbufferTime > tonormaltime && stattime <= tonormaltime && boomcheck == false) {
            checktime = tonormaltime;
            boomcheck = true;
        }
        double second = 0.0;
        if (checktime <= tonormaltime) {
            second = (checktime - stattime) / 1000 * 0.5;
        } else {
            second = (checktime - stattime) / 1000;
        }
        second = second / 3600.0;
        addtale = (long) (addtale * second);
        addiron = (long) (addiron * second);
        addwood = (long) (addwood * second);
        addstone = (long) (addstone * second);
        addfood = (long) (addfood * second);
        if (tale < talelimt) {
            long addt = addtale + tale;
            if (addt > talelimt) {
                addt = talelimt - tale;
//                addt -= talelimt;
            } else {
                addt = addtale;
            }
            playerProxy.addPowerValue(ResourceDefine.POWER_tael, (int) addt, LogDefine.GET_RESOURCE_REGET);
            ResourceOut resourceOut = new ResourceOut(ResourceDefine.POWER_tael, (int) addt);
            sendPorxyLog(resourceOut);
        }

        if (rion < rionlimt) {
            long addr = addiron + rion;
            if (addr > rionlimt) {
                addr = rionlimt - rion;
//                addr -= rionlimt;
            } else {
                addr = addiron;
            }
            playerProxy.addPowerValue(ResourceDefine.POWER_iron, (int) addr, LogDefine.GET_RESOURCE_REGET);
            ResourceOut resourceOut = new ResourceOut(ResourceDefine.POWER_iron, (int) addr);
            sendPorxyLog(resourceOut);
        }

        if (wood < woodlimt) {
            long addw = addwood + wood;
            if (addw > woodlimt) {
                addw = woodlimt - wood;
//                addw -= woodlimt;
            } else {
                addw = addwood;
            }
            playerProxy.addPowerValue(ResourceDefine.POWER_wood, (int) addw, LogDefine.GET_RESOURCE_REGET);
            ResourceOut resourceOut = new ResourceOut(ResourceDefine.POWER_wood, (int) addw);
            sendPorxyLog(resourceOut);
        }

        if (stones < stoneslimt)

        {
            long adds = addstone + stones;
            if (adds > stoneslimt) {
                adds = stoneslimt - stones;
//                adds -= stoneslimt;
            } else {
                adds = addstone;
            }
            playerProxy.addPowerValue(ResourceDefine.POWER_stones, (int) adds, LogDefine.GET_RESOURCE_REGET);
            ResourceOut resourceOut = new ResourceOut(ResourceDefine.POWER_stones, (int) adds);
            sendPorxyLog(resourceOut);
        }

        if (food < foodlimt) {
            long addf = addfood + food;
            if (addf > foodlimt) {
                addf = foodlimt - food;
//                addf -= foodlimt;
            } else {
                addf = addfood;
            }
            playerProxy.addPowerValue(ResourceDefine.POWER_food, (int) addf, LogDefine.GET_RESOURCE_REGET);
            ResourceOut resourceOut = new ResourceOut(ResourceDefine.POWER_food, (int) addf);
            sendPorxyLog(resourceOut);
        }

       /* M3.TimeInfo info1 = timerProxy.setLesTime(TimerDefine.TIMER_TYPE_RESOUCE, 0, 0, TimerDefine.DEFAULT_TIME_RESOUCE);
        timerProxy.addTimeDbToList(info1, m3info);*/
     /*   M3.TimeInfo info2 = timerProxy.setLastOperatinTime(TimerDefine.TIMER_TYPE_RESOUCE, 0, 0, checktime);
        timerProxy.addTimeDbToList(info2, m3info);*/
        playerProxy.getPlayer().setResourereftime(checktime);
        if (checktime + 3000 > GameUtils.getServerDate().getTime()) {
            playerProxy.getPlayer().setResourereftime(GameUtils.getServerDate().getTime());
            boomcheck = false;
            return;
        }

        checkResouse();
    }


    //校验恢复体力时间
    private void checkEnergyTimer(List<M3.TimeInfo> m3info, Set<Integer> powerList) {
    /*    GameProxy gameProxy = super.getGameProxy();
        TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int type = TimerDefine.DEFAULT_ENERGY_RECOVER;
        int times = TimerDefine.DEFAULT_TIME_RECOVER;
        long energy = playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy);
        Long nowTime = GameUtils.getServerDate().getTime();
        if (energy < ActorDefine.MAX_ENERGY) {
            long isExist = timerProxy.addTimer(type, 0, times, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
            if (isExist != 0 || timerProxy.getLastOperatinTime(type, 0, 0) == 0) {
                M3.TimeInfo info = timerProxy.setLastOperatinTime(type, 0, 0, nowTime);
                timerProxy.addTimeDbToList(info, m3info);
            }
            long lastTime = timerProxy.getLastOperatinTime(type, 0, 0);
            long intervalTime = 0;
            if (lastTime == 0) {
                M3.TimeInfo info = timerProxy.setLastOperatinTime(type, 0, 0, nowTime);
                timerProxy.addTimeDbToList(info, m3info);
            } else {
                intervalTime = nowTime - lastTime;
            }
            if (intervalTime >= times) {
                long les = intervalTime % times;
                int nextTime = 0;
                if (les > 0) {
                    nextTime = times - (int) les;
                } else {
                    nextTime = times;
                }
                int count = (int) intervalTime / times;
                if ((ActorDefine.MAX_ENERGY - energy) >= count) {
                    playerProxy.addPowerValue(PlayerPowerDefine.POWER_energy, count, LogDefine.GET_RESOURCE_REGET);
                    powerList.add(PlayerPowerDefine.POWER_energy);
                } else if (energy < ActorDefine.MAX_ENERGY) {
                    playerProxy.addPowerValue(PlayerPowerDefine.POWER_energy, (int) (ActorDefine.MAX_ENERGY - energy), LogDefine.GET_RESOURCE_REGET);
                    powerList.add(PlayerPowerDefine.POWER_energy);
                }
                long newEnergy = playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy);
                if (newEnergy >= ActorDefine.MAX_ENERGY) {
                    timerProxy.setLesTime(type, 0, 0, 0);
                    M3.TimeInfo info = timerProxy.setLastOperatinTime(type, 0, 0, 0);
                    timerProxy.addTimeDbToList(info, m3info);
                } else {
                    Long nextLastTime = GameUtils.getServerDate().getTime();
                    M3.TimeInfo info = timerProxy.setLastOperatinTime(type, 0, 0, nextLastTime);
                    timerProxy.setLesTime(type, 0, 0, (int) Math.ceil(nextTime / 1000.0));
                    timerProxy.addTimeDbToList(info, m3info);
                }
            } else {
                int les = (int) Math.ceil((times - intervalTime) / 1000.0);
                M3.TimeInfo info = timerProxy.setLesTime(type, 0, 0, les);
                timerProxy.addTimeDbToList(info, m3info);
            }
        } else {
            M3.TimeInfo info = timerProxy.setLesTime(type, 0, 0, 0);
            timerProxy.addTimeDbToList(info, m3info);
        }*/
    }

    public int feixun2normalNeedTime() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int needboom = playerProxy.getBoon2Normalneed();
        return (int) (TimerDefine.DEFAULT_TIME_BOOM * needboom * 1.5);
    }

  /*  //校验繁荣度恢复时间
    public void checkBoomTimer(TimerdbProxy timerProxy, PlayerProxy playerProxy, List<M3.TimeInfo> m3info, Set<Integer> powerList) {
        Long date = GameUtils.getServerDate().getTime();
        long nowBoom = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boom);
        long currentBoomLv = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomLevel);
        long nowBoomUplimit = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        int type = TimerDefine.DEFAULT_BOOM_RECOVER;
        int times = TimerDefine.DEFAULT_TIME_BOOM;
        int boomState = playerProxy.checkBoomState();//繁荣状态 0正常，1废墟
        playerProxy.setBoomRefTime(date);
        if (nowBoom < nowBoomUplimit) {
            if (boomState == ActorDefine.DEFINE_BOOM_RUINS) {  //废墟
                M3.TimeInfo info = timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, 0);
                long b = timerProxy.addTimer(type, 0, (int) Math.ceil(times * 1.5), TimerDefine.TIMER_REFRESH_NONE, ActorDefine.DEFINE_BOOM_RUINS, 0, playerProxy);
                if (b != 0) {
                    info = timerProxy.setLastOperatinTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, date);
                }
                timerProxy.addTimeDbToList(info, m3info);
            } else { //正常
                M3.TimeInfo info = timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, 0);
                long a = timerProxy.addTimer(type, 0, times, TimerDefine.TIMER_REFRESH_NONE, ActorDefine.DEFINE_BOOM_NORMAL, 0, playerProxy);
                if (a != 0) {
                    info = timerProxy.setLastOperatinTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, date);
                }
                timerProxy.addTimeDbToList(info, m3info);
            }
            long lastTime = playerProxy.getBoomRefTime();
            if (boomState == ActorDefine.DEFINE_BOOM_RUINS) {
                //lastTime = timerProxy.getLastOperatinTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0);
            } else {
                //  lastTime = timerProxy.getLastOperatinTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0);
            }
            long intervalTime = 0;
            intervalTime = date - lastTime;

            if (intervalTime >= times) {
                long les = intervalTime % times;
                int nextTime = 0;
                if (les > 0) {
                    nextTime = times - (int) les;
                } else {
                    nextTime = times;
                }
                int count = (int) intervalTime / times;
                if ((nowBoomUplimit - nowBoom) >= count) {
                    playerProxy.addPowerValue(PlayerPowerDefine.POWER_boom, count, LogDefine.GET_RESOURCE_REGET);
                    playerProxy.setBoomRefTime(date);
                } else {
                    playerProxy.addPowerValue(PlayerPowerDefine.POWER_boom, (int) (nowBoomUplimit - nowBoom), LogDefine.GET_RESOURCE_REGET);
                    playerProxy.setBoomRefTime(date);
                }
                int boomStateNow = playerProxy.checkBoomState();//繁荣状态
                Long nextLastTime = GameUtils.getServerDate().getTime();
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_boom) >= playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomUpLimit)) {
                    timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, 0);
                    timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, 0);
                    M3.TimeInfo info1 = timerProxy.setLastOperatinTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, nextLastTime);
                    M3.TimeInfo info2 = timerProxy.setLastOperatinTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, nextLastTime);
                    timerProxy.addTimeDbToList(info1, m3info);
                    timerProxy.addTimeDbToList(info2, m3info);
                } else {//当前繁荣小于繁荣上限，继续恢复。
                    if (boomStateNow == ActorDefine.DEFINE_BOOM_RUINS) {
                        long nextNeedTime = (int) Math.ceil((nextTime * 1.5) / 1000.0);
                        timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, (int) nextNeedTime);
                        M3.TimeInfo info = timerProxy.setLastOperatinTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, nextLastTime);
                        timerProxy.addTimeDbToList(info, m3info);
                    } else {
                        timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, (int) Math.ceil(nextTime / 1000.0));
                        M3.TimeInfo info = timerProxy.setLastOperatinTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, nextLastTime);
                        timerProxy.addTimeDbToList(info, m3info);
                    }
                }
            } else {
                long les = (int) Math.ceil((times - intervalTime) / 1000.0);
                if (boomState == ActorDefine.DEFINE_BOOM_RUINS) {
                    long nextNeedTime = (int) Math.ceil(les * 1.5);
                    M3.TimeInfo info = timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, (int) nextNeedTime);
                    timerProxy.addTimeDbToList(info, m3info);
                } else {
                    M3.TimeInfo info = timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, (int) les);
                    timerProxy.addTimeDbToList(info, m3info);
                }
            }
        } else {//繁荣满的
            M3.TimeInfo info1 = timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_NORMAL, 0, 0);
            M3.TimeInfo info2 = timerProxy.setLesTime(type, ActorDefine.DEFINE_BOOM_RUINS, 0, 0);
            playerProxy.setBoomRefTime(date);
            timerProxy.addTimeDbToList(info1, m3info);
            timerProxy.addTimeDbToList(info2, m3info);

        }

    }*/


    //校验繁荣度恢复时间  新定时器
    public void checkBoomTimer(PlayerProxy playerProxy) {
        Long date = GameUtils.getServerDate().getTime();
        long nowBoomUplimit = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boomUpLimit);
        boolean falg = true;
        while (falg) {
            long nowBoom = playerProxy.getPowerValue(PlayerPowerDefine.POWER_boom);
            int times = TimerDefine.DEFAULT_TIME_BOOM;
            int boomState = playerProxy.checkBoomState();//繁荣状态 0正常，1废墟
            if (nowBoom < nowBoomUplimit) {
                long lastTime = playerProxy.getBoomRefTime();
                if (boomState == ActorDefine.DEFINE_BOOM_RUINS) {
                    times = (int) (Math.ceil(times * 1.5));
                }
                long intervalTime = 0;
                intervalTime = date - lastTime;
                if (intervalTime >= times) {
                    int count = (int) (intervalTime / times);
                    if (count > 0) {
                        playerProxy.addPowerValue(PlayerPowerDefine.POWER_boom, 1, LogDefine.GET_RESOURCE_REGET);
                        playerProxy.setBoomRefTime(lastTime+times);
                    } else {
                        falg = false;
                    }
                }else{
                    falg=false;
                }
            } else {
                falg = false;
                playerProxy.setBoomRefTime(date);
            }
        }
    }

    //校验体力恢复时间 新定时器
    public void checkEnergyTimer(PlayerProxy playerProxy){
        Long date = GameUtils.getServerDate().getTime();
        long energyUplimit =ActorDefine.ENERGY_LIMIT;
        boolean falg = true;
        while (falg) {
            long energy = playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy);
            int times = TimerDefine.DEFAULT_TIME_RECOVER;
            if (energy < energyUplimit) {
                long lastTime = playerProxy.getEnergyRefTimes();
                long intervalTime = 0;
                intervalTime = date - lastTime;
                if (intervalTime >= times) {
                    int count = (int) (intervalTime / times);
                    if (count > 0) {
                        playerProxy.addPowerValue(PlayerPowerDefine.POWER_energy, 1, LogDefine.GET_RESOURCE_REGET);
                        playerProxy.setEnergyRefTime(lastTime + times);
                    } else {
                        falg = false;
                    }
                }else{
                    falg=false;
                }
            } else {
                falg = false;
                playerProxy.setEnergyRefTime(date);
            }
       }

    }

    //建筑升级校验
    private void checkBuildingLeveUp(List<M3.TimeInfo> m3info, List<PlayerTask> playerTasks, List<BaseLog> baseLogs) {
     /*   GameProxy gameProxy = super.getGameProxy();
        TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        Map<String, Long> map = timerProxy.getBuildingLeveUpTimer();
        ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
//        Date now = GameUtils.getServerDate();
        int now = GameUtils.getServerTime();
        for (String str : map.keySet()) {
            int time = (int) (map.get(str) / 1000);
            int smalltype = Integer.parseInt(str.split(",")[0]);
            int index = Integer.parseInt(str.split(",")[1]);
            if (now >= time) {   ///1000
                M3.TimeInfo.Builder timeInfo = M3.TimeInfo.newBuilder();
                timeInfo.setRemainTime(0);
                timeInfo.setBigtype(TimerDefine.BUILDING_LEVEL_UP);
                timeInfo.setSmalltype(smalltype);
                timeInfo.setOthertype(index);
                timerProxy.addTimeDbToList(timeInfo.build(), m3info);
//                m3info.add(timeInfo.build());
                //升级成功执行操作并且删除timer
                doBuildingLevelUp(smalltype, index);
                BaseLog baseLog = new BuildingLog(index, smalltype, LogDefine.BUILDINGLEVELFINISH, 0, 0, resFunBuildProxy.getResFuBuildLevelBysmallType(smalltype, index));
                addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDING_LV, 1, 0);
                addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDING_NUM, 1, 0);
                addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDLEVEUP_TIMES, 1, 0);
            } else {
                //重新给客户端发送新的校验时间
                long les = time - now;
                M3.TimeInfo info = timerProxy.setLesTime(TimerDefine.BUILDING_LEVEL_UP, smalltype, index, (int) les);
                if (resFunBuildProxy.isAutoLeveling(GameUtils.getServerDate().getTime())) {
                    timerProxy.addTimeDbToList(info, m3info);
                }
//                timerProxy.addTimeDbToList(info,m3info);  //2016/04/15没升级完成的建筑就不要给客户端了
            }
        }
        //TODO 添加开关限制
        if (resFunBuildProxy.isAutoLeveling(GameUtils.getServerDate().getTime())) {
            resFunBuildProxy.buildAutoLevelUp(m3info);
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lastime = timerdbProxy.getLastOperatinTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
        long lestime = lastime - GameUtils.getServerDate().getTime();
        if (lestime <= 0) {
            lestime = 0;
            long num = timerdbProxy.getTimerNum(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0);
            if (num > 0) {
                lestime = num * 1000;
            }
        }
        M3.TimeInfo info = timerdbProxy.setLesTime(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, (int) (lestime / 1000));
        timerProxy.addTimeDbToList(info, m3info);*/
    }

    /*****
     * 离线检验升级
     *******/
    public void checkOutLineBuildingLeveUp(List<M3.TimeInfo> m3info, List<PlayerTask> playerTasks, List<BaseLog> baseLogs, long now) {
      /*  GameProxy gameProxy = super.getGameProxy();
        TimerdbProxy timerProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        Map<String, Long> map = timerProxy.getBuildingLeveUpTimer();
        ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        for (String str : map.keySet()) {
            long time = map.get(str);
            int smalltype = Integer.parseInt(str.split(",")[0]);
            int index = Integer.parseInt(str.split(",")[1]);
            if (now + 800 >= time) {
                M3.TimeInfo.Builder timeInfo = M3.TimeInfo.newBuilder();
                timeInfo.setRemainTime(0);
                timeInfo.setBigtype(TimerDefine.BUILDING_LEVEL_UP);
                timeInfo.setSmalltype(smalltype);
                timeInfo.setOthertype(index);
                timerProxy.addTimeDbToList(timeInfo.build(), m3info);
                //升级成功执行操作并且删除timer
                doBuildingLevelUp(smalltype, index);
                BaseLog baseLog = new BuildingLog(index, smalltype, LogDefine.BUILDINGLEVELFINISH, 0, 0, resFunBuildProxy.getResFuBuildLevelBysmallType(smalltype, index));
                addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDING_LV, 1, 0);
                addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDING_NUM, 1, 0);
                addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_BUILDLEVEUP_TIMES, 1, 0);
            }
        }*/

    }

    /******
     * 执行升级成功
     ********/
    public void doBuildingLevelUp(int buildType, int index) {
       /* ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        //建筑附加
        int level = resFunBuildProxy.getResFuBuildLevelBysmallType(buildType, index);
        timerdbProxy.delTimer(TimerDefine.BUILDING_LEVEL_UP, buildType, index);
        //繁荣度
        playerProxy.upBuilderOrCreate(buildType, level);

        resFunBuildProxy.addResFuBuildLeve(buildType, index);
        setLeveup(true);
        setLeveup(true);*/
    }

    public void addPlayerTask(List<PlayerTask> playerTasks, int taskType, int addnum, int type) {
        PlayerTask playerTask = null;
        for (PlayerTask playerTask1 : playerTasks) {
            if (playerTask1.taskType == taskType && playerTask1.codition == type) {
                playerTask = playerTask1;
            }
        }
        if (playerTask != null) {
            playerTasks.remove(playerTask);
            playerTask.addnum += addnum;
            playerTasks.add(playerTask);
        } else {
            playerTasks.add(new PlayerTask(taskType, type, addnum));
        }
    }

}
