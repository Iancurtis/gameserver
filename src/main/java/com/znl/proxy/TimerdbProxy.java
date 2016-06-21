package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.Notice;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.log.BuildingLog;
import com.znl.log.CustomerLogger;
import com.znl.pojo.db.ItemBuff;
import com.znl.pojo.db.Timerdb;
import com.znl.proto.M10;
import com.znl.proto.M3;
import com.znl.utils.DateUtil;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class TimerdbProxy extends BasicProxy {
    private Set<Timerdb> tdbs = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (Timerdb tdb : tdbs) {
            tdb.finalize();
        }
    }

    @Override
    protected void init() {

    }

    public TimerdbProxy(Set<Long> tdbids, String areaKey) {
        this.areaKey = areaKey;
        for (Long id : tdbids) {
            Timerdb tdb = BaseDbPojo.get(id, Timerdb.class, areaKey);
            if (tdb != null) {
                this.tdbs.add(tdb);
            } else {
                CustomerLogger.warning("竟然出现空的:" + id);
            }
        }
    }

    public Set<Timerdb> getTdbs() {
        return tdbs;
    }

    public void setTdbs(Set<Timerdb> tdbs) {
        this.tdbs = tdbs;
    }

    public void saveTimers() {
        List<Timerdb> tdbs = new ArrayList<Timerdb>();
        synchronized (changetdbs) {
            while (true) {
                Timerdb tdb = changetdbs.poll();
                if (tdb == null) {
                    break;
                }
                tdbs.add(tdb);
            }
        }
        for (Timerdb tdb : tdbs) {
            tdb.save();
        }
    }

    private LinkedList<Timerdb> changetdbs = new LinkedList<Timerdb>();

    private void pushTimerToChangeList(Timerdb tdb) {
        //插入更新队列
        synchronized (changetdbs) {
            if (!changetdbs.contains(tdb)) {
                changetdbs.offer(tdb);
            }
        }
    }

    //获得定时器，没有的参数填0
    private Timerdb getTimerBytype(int bigType, int smllType, int otherType) {
        synchronized (this.tdbs){
            for (Timerdb tdb : tdbs) {
                if (tdb.getType() == bigType && tdb.getSmallType() == smllType && tdb.getOtherType() == otherType) {
                    return tdb;
                }
            }
        }
        return null;
    }


    private Timerdb getTimerByLongId(long id) {
        for (Timerdb tdb : tdbs) {
            if (tdb.getId() == id) {
                return tdb;
            }
        }
        return null;
    }

    //额外储存数据
    public void setAttrValue(long id, int type, int value) {
        Timerdb tdb = getTimerByLongId(id);
        if (tdb != null) {
            if (type == 1) {
                tdb.setAttr1(value);
            } else if (type == 2) {
                tdb.setAttr2(value);
            } else if (type == 3) {
                tdb.setAttr3(value);
            }
            pushTimerToChangeList(tdb);
        }
    }

    public void setAttrValue(int type1, int type2, int type3, int type, int value) {
        Timerdb tdb = getTimerBytype(type1, type2, type3);
        if (tdb != null) {
            if (type == 1) {
                tdb.setAttr1(value);
            } else if (type == 2) {
                tdb.setAttr2(value);
            } else if (type == 3) {
                tdb.setAttr3(value);
            }
            pushTimerToChangeList(tdb);
        }
    }

    public void setIsAutolv(int type1, int type2, int type3, int state) {
        Timerdb tdb = getTimerBytype(type1, type2, type3);
          if(tdb!=null) {
              tdb.setIsAutoBuildLv(state);
              pushTimerToChangeList(tdb);
          }
    }

    public int getIsAutolv(int type1, int type2, int type3) {
        Timerdb tdb = getTimerBytype(type1, type2, type3);
        if(tdb!=null) {
          return  tdb.getIsAutoBuildLv();
        }
        return 0;
    }


    private boolean isHasTypeId(int type, int smallyType, int orderType) {
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == type && tdb.getSmallType() == smallyType && tdb.getOtherType() == orderType) {
                return true;
            }
        }
        return false;
    }


    /**
     * 添加定时器
     * type 定时器大类
     * smallType 定时器小类
     * otherType 定时器其他类    type  smallType otherType 确定一个定时器
     * num 定时器初始数据
     * lestime  倒计时时间秒 0的话不发送给客户端 每次请求处理都会重新设置
     * refershType 每天刷新时间点  按小时算
     * *
     */
    public long addTimer(int type, int num, int lestime, int refershType, int smallType, int otherType, PlayerProxy playerProxy) {
        if (isHasTypeId(type, smallType, otherType)) {
            return 0;
        } else {
            return creatTimer(type, num, lestime, refershType, smallType, otherType, playerProxy);

        }

    }

    private long creatTimer(int type, int num, int lesTime, int refershType, int smallType, int otherType, PlayerProxy playerProxy) {
        Timerdb tdb = BaseDbPojo.create(Timerdb.class, areaKey);
        tdb.setNum(num);
        tdb.setPlayerId(playerProxy.getPlayerId());
        tdb.setType(type);
        tdb.setLasttime(GameUtils.getServerDate().getTime());
        tdb.setRefreshType(refershType);
        tdb.setLestime(lesTime);
        tdb.setSmallType(smallType);
        tdb.setOtherType(otherType);
        tdb.setBegintime(GameUtils.getServerDate().getTime());
        tdbs.add(tdb);
        playerProxy.addTimeIdToPlayer(tdb.getId());
        tdb.save();


        return tdb.getId();
    }


    public void addLesTime(int type, int smallyType, int orderType, int add) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            int lesTime = tdb.getLestime();
            tdb.setLestime(add + lesTime);
            pushTimerToChangeList(tdb);
        }

    }

    private M3.TimeInfo.Builder getTimeInfo(Timerdb tdb){
        M3.TimeInfo.Builder info = M3.TimeInfo.newBuilder();
        info.setBigtype(tdb.getType());
        info.setSmalltype(tdb.getSmallType());
        info.setOthertype(tdb.getOtherType());
        info.setRemainTime(tdb.getLestime());
        info.setIsAutoBuildLvTrigger(tdb.getIsAutoBuildLv());
        info.setAttr1(tdb.getAttr1());
        info.setAttr2(tdb.getAttr2());
        info.setAttr3(tdb.getAttr3());
        info.setLasttime((int) (tdb.getLasttime() / 1000));
        info.setNum(tdb.getNum());
        info.setBegintime((int) (tdb.getBegintime() / 1000));
        info.setIsReset(0);
        return info;
    }

    public M3.TimeInfo setLesTime(int type, int smallyType, int orderType, int lesTime) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            tdb.setLestime(lesTime);
            pushTimerToChangeList(tdb);
            M3.TimeInfo.Builder info = getTimeInfo(tdb);
            if (info != null){
                return info.build();
            }
        }
        return null;
    }

    public void addTimeDbToList(M3.TimeInfo info,List<M3.TimeInfo> infos){
        if (info == null || infos == null){
            return;
        }
        for (int i=0;i<infos.size();i++){
            M3.TimeInfo tf = infos.get(i);
            if (tf.getBigtype() == info.getBigtype() && tf.getOthertype() == info.getOthertype() && tf.getSmalltype() == info.getSmalltype()){
                if (tf.getDelete() == info.getDelete()){
                    return;
                }else{
                    infos.set(i,info);
                }
            }
        }
        infos.add(info);
    }

    public void reduceLesTime(int type, int smallyType, int orderType, int reduce) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            int lesTime = tdb.getLestime();
            tdb.setLestime(lesTime - reduce);
            pushTimerToChangeList(tdb);
        }

    }


    public void addNum(int type, int smallyType, int orderType, int add) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        reFreshTimer(tdb);
        if (tdb != null) {
            int num = tdb.getNum();
            tdb.setNum(num + add);
            pushTimerToChangeList(tdb);
        }

    }

    //上次操作的时间
    public long getLastOperatinTime(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb == null) {
            return -1;
        }
        return tdb.getLasttime();
    }

    //获取额外属性1
    public int getAttr1(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb == null) {
            return 0;
        }
        return tdb.getAttr1();
    }

    //获取额外属性2
    public int getAttr2(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb == null) {
            return 0;
        }
        return tdb.getAttr2();
    }

    /****
     * 设置操作时间
     *****/
    public M3.TimeInfo setLastOperatinTime(int type, int smallyType, int orderType, long time) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            tdb.setLasttime(time);
            pushTimerToChangeList(tdb);
            //添加到定时检查任务里面
            if (tdb.getRefreshType() == TimerDefine.TIMER_REFRESH_NONE){
                addTimerToCheckSet((int) (time /1000));
            }
            M3.TimeInfo.Builder info = getTimeInfo(tdb);
            //2016/04/11 增加设置定时时间后的检查
            if (getGameProxy() != null){
                SystemProxy systemProxy = getProxy(ActorDefine.SYSTEM_PROXY_NAME);
                switch (type){
                    case TimerDefine.ARENA_FIGHT:
                        systemProxy.checkArena(null);
                        break;
                }
            }
            if (info != null){
                return info.build();
            }
        }
        return null;
    }

    public void reduceNum(int type, int smallyType, int orderType, int reduce) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            int num = tdb.getNum();
            tdb.setNum(num - reduce);
            pushTimerToChangeList(tdb);
        }

    }

    public void setNum(int type, int smallyType, int orderType, int num) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            tdb.setNum(num);
            pushTimerToChangeList(tdb);
        }

    }

    public int getTimerNum(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        reFreshTimer(tdb);
        if (tdb == null) {
            return 0;
        }
        return tdb.getNum();
    }

    public long getTimerId(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        reFreshTimer(tdb);
        if (tdb == null) {
            return 0;
        }
        return tdb.getId();
    }

    public int getTimerRefreshTIme(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        reFreshTimer(tdb);
        if (tdb == null) {
            return 0;
        }
        return tdb.getRefreshType();
    }

    public void setTimerRefreshTIme(int type, int smallyType, int orderType, int time) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb != null) {
            tdb.setRefreshType(time);
            pushTimerToChangeList(tdb);
        }

    }


    public int getTimerlesTime(int type, int smallyType, int orderType) {
        Timerdb tdb = getTimerBytype(type, smallyType, orderType);
        if (tdb == null) {
            return 0;
        }
        int les = tdb.getLestime();
        if (les < 0) {
            les = 0;
        }
        return les;
    }

    //获得冒险剩余次数
    public int getAdventureTimesById(int id) {
        ActivityProxy activityProxy=getGameProxy().getProxy(ActorDefine.ACTIVITY_PROXY_NAME);
        int actype=0;
        if(id==BattleDefine.ADVANTRUE_TYPE_EQUIP){
          actype=ActivityDefine.ACTIVITY_CONDITION_EUIP_ADVANCE_ADDTIMES;
        }
        if(id==BattleDefine.ADVANTRUE_TYPE_ORNDANCE){
            actype=ActivityDefine.ACTIVITY_CONDITION_ORDANCE_ADVANCE_ADDTIMES;
        }
        JSONObject json = ConfigDataProxy.getConfigInfoFindById(DataDefine.ADVENTURE, id);
        Timerdb tdb = getTimerBytype(TimerDefine.ADVENCE_REFRESH, id, 0);
        int limit = json.getInt("time");
        int lesNum = tdb.getNum();
        if (lesNum >= 0) {
            reFreshTimer(tdb);
        }
        if (tdb == null) {
            return 0;
        }
        int num = limit - lesNum;
     /*   if (num < 0) {
            return 0;
        }*/
        return num+activityProxy.getEffectBufferPowerByType(actype);
    }


    //根据bigtype获得
    private List<Timerdb> getTimerdbListByType(int type) {
        List<Timerdb> list = new ArrayList<Timerdb>();
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == type) {
                list.add(tdb);
            }
        }
        return list;
    }


    //添加冒险次数
    public void addAdvanceTiems(int id, int add) {
        Timerdb tdb = getTimerBytype(TimerDefine.ADVENCE_REFRESH, id, 0);
        if (tdb != null) {
            int num = tdb.getNum();
            tdb.setNum(num - add);
            pushTimerToChangeList(tdb);
        }
    }

    //减少冒险次数冒险次数
    public void reduceAdvanceTiems(int id) {
        Timerdb tdb = getTimerBytype(TimerDefine.ADVENCE_REFRESH, id, 0);
        if (tdb != null) {
            int num = tdb.getNum();
            tdb.setNum(num + 1);
            pushTimerToChangeList(tdb);
        }
    }


    //初始化冒险地图
    private void initAdvanceMapTimer(TimerdbProxy timerdbProxy) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Timerdb> list = timerdbProxy.getTimerdbListByType(TimerDefine.ADVENCE_REFRESH);
        List<JSONObject> jsonlist = ConfigDataProxy.getConfigAllInfo(DataDefine.ADVENTURE);
        for (JSONObject json : jsonlist) {
            int id = json.getInt("ID");
            if (timerdbProxy.bigTypeHasSamllType(list, id) == false) {
                timerdbProxy.addTimer(TimerDefine.ADVENCE_REFRESH, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, id, 0, playerProxy);
            }
        }
    }

    private boolean bigTypeHasSamllType(List<Timerdb> list, int smallType) {
        for (Timerdb tdb : list) {
            if (tdb.getSmallType() == smallType) {
                return true;
            }
        }
        return false;
    }


    //获得建筑升级倒计时
    public Map<String, Long> getBuildingLeveUpTimer() {
        Map<String, Long> map = new HashMap<String, Long>();
        List<Timerdb> list = getTimerdbListByType(TimerDefine.BUILDING_LEVEL_UP);
        for (Timerdb tdb : list) {
            map.put(tdb.getSmallType() + "," + tdb.getOtherType(), tdb.getLasttime());
        }
        return map;
    }


    /*****
     * 刷新
     *******/
    private void reFreshTimer(Timerdb tdb) {
        if (tdb == null) {
            return;
        }
        if (tdb.getRefreshType() != TimerDefine.TIMER_REFRESH_NONE) {
            if (DateUtil.isCanGet(GameUtils.getServerDate().getTime(), tdb.getLasttime(), tdb.getRefreshType())&&!TimerDefine.TIMER_REFRESH_BYHAND.contains(tdb.getType())&&tdb.getType()!=TimerDefine.LOGIN_DAY_NUM) {
                tdb.setLasttime(GameUtils.getServerDate().getTime());
                tdb.setNum(0);
                tdb.setAttr1(0);
                pushTimerToChangeList(tdb);
            }
        }
    }

    /*****
     * 刷新
     *******/
    public void reFreshTimer() {
        for (Timerdb tdb : tdbs) {
            reFreshTimer(tdb);
        }
    }

    /***
     * 删除定时器--不需要的参数填0
     ********/
    public M3.TimeInfo delTimer(int bigType, int smallType, int oderType) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Timerdb> list = getTimerdbListByType(bigType);
        M3.TimeInfo.Builder info = null;
        for (Timerdb tdb : list) {
            if (tdb.getSmallType() == smallType && tdb.getOtherType() == oderType) {
                info = getTimeInfo(tdb);
                info.setDelete(1);
                tdbs.remove(tdb);
                tdb.del();
                changetdbs.remove(tdb);
                playerProxy.removeTimeIdToPlayer(tdb.getId());
            }
        }
        if (info != null){
            return info.build();
        }else {
            return null;
        }
    }

    //登陆初始化
    public void initTimer() {
        initAdvanceMapTimer(this);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        //资源产出定时器
        addTimer(TimerDefine.TIMER_TYPE_RESOUCE, 0, TimerDefine.DEFAULT_TIME_RESOUCE, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        long timePrestige = addTimer(TimerDefine.DEFAULT_GET_PRESTIGE, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy); //登录领取声望
        if (timePrestige != 0) {
            long nowTime = GameUtils.getServerTime() * 1000;
            setLastOperatinTime(TimerDefine.DEFAULT_GET_PRESTIGE, 0, 0, nowTime - TimerDefine.ONE_DAY);
        }
        addTimer(TimerDefine.FRIEND_BLESS, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy); //好友祝福获得1点体力
        //每日任务定时器
        long dayactivi = addTimer(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        if (dayactivi != 0) {
            setLastOperatinTime(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0, DateUtil.getNextHour(GameUtils.getServerDate().getTime(), TimerDefine.TIMER_REFRESH_FOUR));
        }
        long daymess = addTimer(TimerDefine.FRIEND_DAY_MESSION, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        if (daymess != 0) {
            setLastOperatinTime(TimerDefine.FRIEND_DAY_MESSION, 0, 0, DateUtil.getNextHour(GameUtils.getServerDate().getTime(), TimerDefine.TIMER_REFRESH_FOUR));
        }
        //竞技场挑战时间
        long arena = addTimer(TimerDefine.ARENA_FIGHT, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        if (arena != 0) {
            setLastOperatinTime(TimerDefine.ARENA_FIGHT, 0, 0, GameUtils.getServerDate().getTime());
        }
        addTimer(TimerDefine.ARENA_TIMES, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        addTimer(TimerDefine.ARENA_ADD_TIMES, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        //抽奖
        addTimer(TimerDefine.TIMIER_LOTTERY_TODAY, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 1, 0, playerProxy);
        addTimer(TimerDefine.TIMIER_LOTTERY_TODAY, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 2, 0, playerProxy);
        addTimer(TimerDefine.TIMIER_LOTTERY_TODAY, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 3, 0, playerProxy);
        //拉霸抽奖
        addTimer(TimerDefine.LABA_LOTTER_FREETIME, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        for (int i = 1; i <= 3; i++) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WARRIORGET, "type", i);
            long loId = addTimer(TimerDefine.TIMIER_LOTTERY, 0, 0, TimerDefine.TIMER_REFRESH_NONE, i, 0, playerProxy);
            if (loId != 0) {
                setLastOperatinTime(TimerDefine.TIMIER_LOTTERY, i, 0, GameUtils.getServerDate().getTime() - jsonObject.getInt("freetotal") * jsonObject.getInt("freetime") * 60000);
            }

        }
        //掏宝免费
        for (JSONObject jsonObject : ConfigDataProxy.getConfigAllInfo(DataDefine.TREASURE)) {
            addTimer(TimerDefine.TIMIER_LOTTERY_TAOBAO, 0, 0, jsonObject.getInt("freetime"), jsonObject.getInt("type"), 0, playerProxy);
            if (getTimerRefreshTIme(TimerDefine.TIMIER_LOTTERY_TAOBAO, 0, 0) != jsonObject.getInt("freetime")) {
                setTimerRefreshTIme(TimerDefine.TIMIER_LOTTERY_TAOBAO, 0, 0, jsonObject.getInt("freetime"));
            }
        }

        addTimer(TimerDefine.LASTARENAREWAED, 0, 0, TimerDefine.TIMER_REFRESH_ZERO, 0, 0, playerProxy);
        Timerdb aretimer=getTimerBytype(TimerDefine.LASTARENAREWAED, 0, 0);
        if(aretimer.getRefreshType()!=TimerDefine.TIMER_REFRESH_ZERO){
            aretimer.setRefreshType(TimerDefine.TIMER_REFRESH_ZERO);
            aretimer.save();
        }
        addTimer(TimerDefine.DAY_TASK_REST, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        addTimer(TimerDefine.LIMIT_CHANGET_TIMES, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.LIMIT_CHANGET_REST, 0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0, playerProxy);
        addTimer(TimerDefine.LIMIT_CHANGET_MOPPING, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.ACTIVITY_REFRESH, 0, 60, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.BUILD_AUTO_LEVLE_UP, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.BUILD_DEGREE, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.LEGION_ALLTIME_DONATE,0, 0, TimerDefine.TIMER_REFRESH_FOUR, 0, 0,playerProxy);
        //活定时器
        addTimer(TimerDefine.ACTIVITY_DEL_FREETIME, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.ACTIVITY_ADD_FREETIME, 0, 0, TimerDefine.TIMER_REFRESH_NONE, 0, 0, playerProxy);
        addTimer(TimerDefine.JUNSHILOTTERY,0,0,TimerDefine.TIMER_REFRESH_FOUR,LotterDefine.JUBSHI_LOTTERY_GOLD,0,playerProxy);
        addTimer(TimerDefine.JUNSHILOTTERY,0,0,TimerDefine.TIMER_REFRESH_FOUR,LotterDefine.JUBSHI_LOTTERY_RESOURSE,0,playerProxy);
    }

    /****
     * 获取佣兵定时器执行中最大orfer
     ****/
    public int getCreateBigNum(int smallType) {
        int num = 0;
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILD_CREATE && smallType == tdb.getSmallType() && tdb.getLasttime() > GameUtils.getServerDate().getTime()) {
                if (tdb.getOtherType() > num) {
                    num = tdb.getOtherType();
                }
            }
        }
        return num;
    }


    /****
     * 获取执行任务定时器执行中最大orfer
     ****/
    public int getPerformTaskBigNum(int smallType) {
        int num = 0;
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.PERFORM_TASK && smallType == tdb.getSmallType() && tdb.getLasttime() > GameUtils.getServerDate().getTime()) {
                if (tdb.getOtherType() > num) {
                    num = tdb.getOtherType();
                }
            }
        }
        return num;
    }


    /****
     * 获取生产队列定时器执行中数量
     ****/
    public int getCreateingNum(int smallType) {
        int num = 0;
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILD_CREATE && smallType == tdb.getSmallType() && tdb.getLasttime() > GameUtils.getServerDate().getTime()) {
                num++;
            }
        }
        return num;
    }

    /****
     * 某种科技是否可以升级
     ****/
    public boolean sienceIsCanLevel(int smallType, int typeId) {
        ;
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILD_CREATE && smallType == tdb.getSmallType() && tdb.getLasttime() > GameUtils.getServerDate().getTime() && tdb.getAttr1() == typeId) {
                return false;
            }
        }
        return true;
    }


    /********/
    public List<Timerdb> getBuildIndexCreate(int smallType) {
        List<Timerdb> list = new ArrayList<Timerdb>();
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILD_CREATE && smallType == tdb.getSmallType() && tdb.getLasttime() > GameUtils.getServerDate().getTime()) {
                list.add(tdb);
            }
        }
        return list;
    }

    /*************
     * 获得建筑生产队列
     *****/
    public List<M10.ProductionInfo> getProductionInfo(int index,int buildtype) {
        List<M10.ProductionInfo> list = new ArrayList<M10.ProductionInfo>();
        if(!ResFunBuildDefine.PRODUCTBUILD.contains(buildtype)){
            return list;
        }
        List<Timerdb> priolist = getBuildIndexCreate(index);
        Date now = GameUtils.getServerDate();
        SortUtil.anyProperSort(priolist, "getOtherType", true);
        for (Timerdb tdb : priolist) {
            long time = tdb.getLasttime() - now.getTime();
            long lestime = tdb.getAttr2();
            if (time > 0) {
                M10.ProductionInfo.Builder builder = M10.ProductionInfo.newBuilder();
                builder.setTypeid(tdb.getAttr1());
                builder.setNum(tdb.getNum());
                if (time - 900 <= lestime * 1000) {
                    builder.setState(1);
                    builder.setRemainTime((int) (time / 1000));
                } else {
                    builder.setState(2);
                    builder.setRemainTime((int) lestime);
                }
                builder.setOrder(tdb.getOtherType());
//                System.out.println("order" + tdb.getOtherType());
                list.add(builder.build());
            }
        }

        return list;
    }


    public long getLastCreateTime(int buildIndex, int order) {
        Timerdb tdb = getTimerBytype(TimerDefine.BUILD_CREATE, buildIndex, order);
        if (tdb == null) {
            return GameUtils.getServerDate().getTime();
        }
        return tdb.getLasttime();
    }

    /****
     * 获取生产时器执行中数量
     ****/
    public void checkBuildCreate(List<M3.TimeInfo> m3info, PlayerReward reward, List<PlayerTask> playerTasks, List<BaseLog> baseLogs) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        Date now = GameUtils.getServerDate();
        for (Timerdb tdb : getTimerdbListByType(TimerDefine.BUILD_CREATE)) {
            if (now.getTime() + 900 > tdb.getLasttime()) {
                //制造完成
                int num = tdb.getNum();
                int typeId = tdb.getAttr1();
                int buildtype = resFunBuildProxy.getResFuBuildType(ResFunBuildDefine.BUILDE_TYPE_FUNTION, tdb.getSmallType());
                int index = resFunBuildProxy.getResFuBuildIndexBybigsmall(ResFunBuildDefine.BUILDE_TYPE_FUNTION, buildtype);
                if (buildtype == ResFunBuildDefine.BUILDE_TYPE_TANK) {
                    soldierProxy.addSoldierNum(typeId, num, LogDefine.GET_BUILD_PRODUCTION);
                    RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                    rewardProxy.addSoldierToReward(reward, typeId, num);
                    SystemProxy systemProxy = getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
                    systemProxy.addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_CREATESODIER_NUM, num, typeId);
                    BuildingLog buildingLog = new BuildingLog(buildtype, index, LogDefine.BUILDINGPRODUCTFINISH, typeId, num, resFunBuildProxy.getFinishLevelTime(buildtype, index));
                    baseLogs.add(buildingLog);
                } else if (buildtype == ResFunBuildDefine.BUILDE_TYPE_SCIENCE) {
                    // 科技升级
                    TechnologyProxy technologyProxy = getGameProxy().getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME);
                    technologyProxy.addTechinologyLeve(typeId);
                    SystemProxy systemProxy = getGameProxy().getProxy(ActorDefine.SYSTEM_PROXY_NAME);
                    systemProxy.addPlayerTask(playerTasks, TaskDefine.TASK_TYPE_SCIENCELV_TIMES, 0, typeId);
                    technologyProxy.expandPowerTechnology();//升级完成重新init
                    BuildingLog buildingLog = new BuildingLog(buildtype, index, LogDefine.BUILDINGPRODUCTFINISH, typeId, 1, resFunBuildProxy.getFinishLevelTime(buildtype, index));
                    baseLogs.add(buildingLog);
                } else if (buildtype == ResFunBuildDefine.BUILDE_TYPE_CREATEROOM) {
                    ItemProxy itemProxy = getGameProxy().getProxy(ActorDefine.ITEM_PROXY_NAME);
                    itemProxy.addItem(typeId, num, LogDefine.GET_BUILD_PRODUCTION);
                    RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                    rewardProxy.addItemToReward(reward, typeId, num);
                    BuildingLog buildingLog = new BuildingLog(buildtype, index, LogDefine.BUILDINGPRODUCTFINISH, typeId, num, resFunBuildProxy.getFinishLevelTime(buildtype, index));
                    baseLogs.add(buildingLog);
                } else if (buildtype == ResFunBuildDefine.BUILDE_TYPE_RREFIT) {
                    soldierProxy.addSoldierNum(typeId, num, LogDefine.GET_BUILD_PRODUCTION);
                    RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                    rewardProxy.addSoldierToReward(reward, typeId, num);
                    BuildingLog buildingLog = new BuildingLog(buildtype, index, LogDefine.BUILDINGPRODUCTFINISH, typeId, num, resFunBuildProxy.getFinishLevelTime(buildtype, index));
                    baseLogs.add(buildingLog);
                }
                M3.TimeInfo.Builder timeInfo = M3.TimeInfo.newBuilder();
                timeInfo.setBigtype(tdb.getType());
                int type = resFunBuildProxy.getResFuBuildType(ResFunBuildDefine.BUILDE_TYPE_FUNTION, tdb.getSmallType());
                timeInfo.setSmalltype(type);
                timeInfo.setOthertype(tdb.getSmallType());
                timeInfo.setRemainTime(0);
                addTimeDbToList(timeInfo.build(), m3info);
                M3.TimeInfo info1 = delTimer(tdb.getType(), tdb.getSmallType(), tdb.getOtherType());
                if (resFunBuildProxy.isAutoLeveling(GameUtils.getServerDate().getTime())) {
                    addTimeDbToList(info1, m3info); //自动升级的时候要发给前端刷新
                }
            } else {
                long time = tdb.getLasttime() - now.getTime();
                long lestime = tdb.getAttr2();
//                System.out.println("time" + time + "lestime" + lestime + "========" + (time - (lestime * 1000)));
                if (time > 0) {
                    if (time - 900 >= lestime * 1000) {
                        tdb.setLestime(0);
                    } else {
                        tdb.setLestime((int) (time / 1000));
                    }
                    M3.TimeInfo info = getTimeInfo(tdb).build();
                    addTimeDbToList(info, m3info);
                }
            }
        }

    }

    //修改某个建筑的生产队列的完成时间
    public void modifBuildfinishTime(int index, long time, int order) {
        time -= getOtehrtime(order, index);
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILD_CREATE && tdb.getSmallType() == index) {
                if (tdb.getOtherType() >= order) {
                    tdb.setLasttime(tdb.getLasttime() - time);
                    addTimerToCheckSet((int) (tdb.getLasttime()/1000));
                    pushTimerToChangeList(tdb);
                }
            }
        }
    }

    //获得上面几个的时间
    public long getOtehrtime(int order, int index) {
        long time = 0l;
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILD_CREATE && tdb.getSmallType() == index) {
                if (tdb.getOtherType() < order) {
                    time += getLastOperatinTime(TimerDefine.BUILD_CREATE, index, tdb.getOtherType()) - GameUtils.getServerDate().getTime();
                }
            }
        }
        return time;
    }


    //获得正在升级的建筑完成时间

    public Set<Long> getLevelUpEndTime() {
        Set<Long> set = new HashSet<Long>();
        long now = GameUtils.getServerDate().getTime();
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILDING_LEVEL_UP) {
                if (now < tdb.getLasttime()) {
                    set.add(tdb.getLasttime());
                }
            }
        }
        return set;
    }


    //获得建筑剩余升级队列
    public int getBuildLeveNum() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        ItemBuffProxy itemBuffProxy = getGameProxy().getProxy(ActorDefine.ITEMBUFF_PROXY_NAME);
        int expandBbuildSize = itemBuffProxy.getValidBuildSize();
        int hadbuildSize = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_buildsize);
        int times = hadbuildSize + expandBbuildSize;
        int hasnum = 0;
        long now = GameUtils.getServerDate().getTime();
        for (Timerdb tdb : tdbs) {
            if (tdb.getType() == TimerDefine.BUILDING_LEVEL_UP) {
                if (now < tdb.getLasttime()) {
                    hasnum++;
                }
            }
        }
        return times - hasnum;
    }

    //获得执行任务剩余执行队列
    public int getPerformTaskLesNum() {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int queues = vipProxy.getVipNum(ActorDefine.VIP_TROOPCOUNT);
        return queues;
    }

    //获得消息通过类型
    public List<Notice> getNoticeByType(int type) {
        List<Notice> list = new ArrayList<Notice>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (type == TipDefine.NOTICE_TYPE_AUTUBUILD) {
            for (Timerdb tdb : tdbs) {
                if (tdb.getType() == TimerDefine.BUILDING_LEVEL_UP) {
                    Notice notice = new Notice(playerProxy.getPlayerId(), tdb.getBegintime(), playerProxy.getAreaKey(), tdb.getLasttime(), type, playerProxy.getPushChannelId());
                    list.add(notice);
                }
            }
        }
        if(type == TipDefine.NOTICE_TYPE_CREATESOLDIER){
            for (Timerdb tdb : tdbs) {
                if (tdb.getType() == TimerDefine.BUILD_CREATE&&tdb.getSmallType()>=2 && tdb.getSmallType()<=3) {
                    Notice notice = new Notice(playerProxy.getPlayerId(), tdb.getBegintime(), playerProxy.getAreaKey(), tdb.getLasttime(), type, playerProxy.getPushChannelId());
                    list.add(notice);
                }
            }
        }
        if(type == TipDefine.NOTICE_TYPE_SCIENCELEVEL){
            for (Timerdb tdb : tdbs) {
                if (tdb.getType() == TimerDefine.BUILD_CREATE&&tdb.getSmallType()==12) {
                    Notice notice = new Notice(playerProxy.getPlayerId(), tdb.getBegintime(), playerProxy.getAreaKey(), tdb.getLasttime(), type, playerProxy.getPushChannelId());
                    list.add(notice);
                }
            }
        }
        return list;
    }


    //定时触发逻辑
    Set<Integer> timerSet = new ConcurrentHashSet<>();
    //初始服务器定时触发
    public void initTimerSet(){
        synchronized (timerSet){
            timerSet.clear();
            for (Timerdb timer : tdbs){
                if (timer.getRefreshType() < 0){
                    timerSet.add((int) (timer.getLasttime()/1000));
                }
            }
        }
    }

    //是否有定时任务触发了
    public boolean checkTimerEverySecond(){
        Integer time = GameUtils.getServerTime();
        synchronized (timerSet){
            if (timerSet.contains(time)){
                timerSet.remove(time);
                return true;
            }
        }
        return false;
    }

    //添加时间到定时检查任务中
    public void addTimerToCheckSet(Integer time){
        synchronized (timerSet){
            timerSet.add(time);
        }
    }

}
