package com.znl.proxy;

import com.znl.GameMainServer;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseSetDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.log.CustomerLogger;
import com.znl.log.admin.tbllog_activity;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Activity;
import com.znl.pojo.db.set.VipActSetDb;
import com.znl.proto.Common;
import com.znl.proto.M23;
import com.znl.proto.M25;
import com.znl.proto.M3;
import com.znl.service.ArmyGroupService;
import com.znl.utils.DateUtil;
import com.znl.utils.GameUtils;
import com.znl.utils.SortUtil;
import org.apache.avro.generic.GenericData;
import org.apache.commons.lang.StringUtils;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2016/1/15.
 */
public class ActivityProxy extends BasicProxy {

    private Set<Activity> activitys = new ConcurrentHashSet<>();

    private static final int LEGION_SHARE_LIMIT_TIME = 60;//有福同享宝箱有效时间，24小时 86400

    //等待被删除的活动集合
    public static final ConcurrentHashSet<Integer> futureDeleteActivity = new ConcurrentHashSet<>();
    //等待被更新的活动集合
    public static final ConcurrentLinkedQueue<M23.M230007.S2C> futureUpdateActivityList = new ConcurrentLinkedQueue<>();
    public static final ConcurrentLinkedQueue<M23.M230009.S2C> futureUpdateLimitActivityList = new ConcurrentLinkedQueue<>();


    public ActivityProxy(Set<Long> ids, String areaKey) {
        this.areaKey = areaKey;
        for (Long id : ids) {
            Activity activity = BaseDbPojo.get(id, Activity.class, areaKey);
            if (activity == null) {
                CustomerLogger.error("活动出现空值");
                System.out.println("活动出现空值");
                continue;
            }
            activitys.add(activity);
        }

    }

    private void addBuildPlayerPower(int id, long value) {
        if (super.expandPowerMap == null) {
            return;
        }
        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }


    }


    private HashMap<Integer, JSONObject> activityMap = new HashMap<>();
    private HashMap<Integer, List<JSONObject>> activityEffectMap = new HashMap<>();

    /***
     * 因为读取配置数据过于频繁，所以先缓存起来把
     ***/
    public void initDefineMap() {
        activityMap.clear();
        activityEffectMap.clear();
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ACTIVE_DESIGN);
        for (JSONObject define : list) {
            int id = define.getInt("ID");
            activityMap.put(id, define);
        }
        list = ConfigDataProxy.getConfigAllInfo(DataDefine.ACTIVE_EFFECT);
        for (JSONObject define : list) {
            int id = define.getInt("effectID");
            List<JSONObject> effList = activityEffectMap.get(id);
            if (effList == null) {
                effList = new ArrayList<>();
            }
            effList.add(define);
            activityEffectMap.put(id, effList);
        }
    }


    public Activity getActivityByTypeId(int typeId) {
        for (Activity activity : activitys) {
            if (activity.getActivityId() == typeId) {
                return activity;
            }
        }
        return null;
    }

    private long getNextRefurceTime(JSONObject define, long lastRefTime, PlayerProxy playerProxy) {
        //获得服务器这个活动的下次刷新时间
        Calendar calendar = Calendar.getInstance();
        Date nowDate = GameUtils.getServerDate();
        long now = GameUtils.getServerDate().getTime();
        if (lastRefTime == 0) {
            long starTime = getActivityStarTime(define, playerProxy);
            /*PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);*/
            if (starTime == -1) {
                starTime = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId()).getTime();
            }
            calendar.setTimeInMillis(starTime);
            if (calendar.getTime().after(nowDate)) {
                return starTime;
            } else {
                while (calendar.getTime().before(nowDate)) {
                    calendar.add(Calendar.DAY_OF_YEAR, define.getInt("resettime"));
                }
                calendar.set(Calendar.HOUR_OF_DAY, TimerDefine.TIMER_REFRESH_FOUR);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTimeInMillis();
            }
        } else {
            calendar.setTimeInMillis(lastRefTime);
            Date lastRefDate = calendar.getTime();
            while (lastRefDate.before(nowDate)) {
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("resettime"));
                lastRefDate = calendar.getTime();
            }
            calendar.set(Calendar.HOUR_OF_DAY, TimerDefine.TIMER_REFRESH_FOUR);
            calendar.set(Calendar.HOUR_OF_DAY, TimerDefine.TIMER_REFRESH_FOUR);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }
    }

    public void checkActivity(PlayerProxy playerProxy) {
        Set<Integer> defineIds = new HashSet<>();
        //检查要加上的
        for (JSONObject define : activityMap.values()) {
            int id = define.getInt("ID");
            Activity activity = getActivityByTypeId(id);
            if (activity == null) {
                //检查这个活动是否要加到玩家身上
                activity = BaseDbPojo.create(Activity.class, areaKey);
                activity.setPlayerId(playerProxy.getPlayerId());
                activity.setActivityId(id);
                int effectID = define.getInt("effectID");

                //普通活动的逻辑
                if (define.getInt("show") == 1) {
                    JSONObject effectDefine = activityEffectMap.get(effectID).get(0);
                    int conditionType = effectDefine.getInt("conditiontype");

                    activity.setActivityType(effectDefine.getInt("type"));
                    int resetTime = define.getInt("resettime");

                    if (resetTime > 0) {
                        activity.setRefurceTime(getNextRefurceTime(define, 0, playerProxy));
                    }
                    //活动添加储存完成度
                    List<JSONObject> effectlist = activityEffectMap.get(define.getInt("effectID"));
                    List<Integer> valuelist = new ArrayList<Integer>();
                    for (int i = 0; i < effectlist.size(); i++) {
                        valuelist.add(i, 0);
                    }
                    activity.setValuelist(valuelist);
                    //新创建的对象避免出错还是马上保存好
                    activity.save();
                    activitys.add(activity);
                    playerProxy.addActivity(activity.getId());
                  /*  if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL) {
                        if (getGameProxy() != null) {
                            activity.setExpand(effectDefine.getInt("condition1"));
                            ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                            int level = resFunBuildProxy.getMaxLevelByBuildType(activity.getExpand());
                            addActivityConditionValue(conditionType, level, playerProxy, activity.getExpand());
                        }
                    } else if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_COM_LEVEL) {
                        int level = playerProxy.getLevel();
                        addActivityConditionValue(conditionType, level, playerProxy, 0);
                    } else if (conditionType == ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION) {
                        if (playerProxy.getPlayer().getArmygroupId() > 0) {
                            int value = 1;
                            addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION, value, playerProxy, 0);
                        }
                    }*/
                } else {
                    //TODO 限时活动逻辑
                    int resetTime = define.getInt("resettime");
                    if (resetTime > 0) {
                        activity.setRefurceTime(getNextRefurceTime(define, 0, playerProxy));
                    }
                    activity.save();
                    activitys.add(activity);
                    playerProxy.addActivity(activity.getId());
                }
            } else {
                //旧的活动进行转换
                if (activity.getValuelist().size() == 0) {
                    List<JSONObject> effectlist = activityEffectMap.get(define.getInt("effectID"));
                    List<Integer> valuelist = new ArrayList<Integer>();
                    for (int i = 0; i < effectlist.size(); i++) {
                        valuelist.add(i, 0);
                    }
                    for (JSONObject jsonObject : effectlist) {
                        valuelist.add(jsonObject.getInt("sort"), (int) activity.getConditionValue());
                    }
                    activity.setValuelist(valuelist);
                }
            }
            defineIds.add(id);
        }

        //检查要减去的
        List<Activity> removeList = new ArrayList<>();
        for (Activity activity : activitys) {
            if (defineIds.contains(activity.getActivityId()) == false) {
                //服务器已经没这个活动了，判断要做扣除操作
                removeList.add(activity);
                playerProxy.removeActivity(activity.getId());
                activity.del();
            }
        }
        activitys.removeAll(removeList);
    }


    private void specialActivity(PlayerProxy playerProxy) {
        for (JSONObject define : activityMap.values()) {
            int id = define.getInt("ID");
            Activity activity = getActivityByTypeId(id);
            int effectID = define.getInt("effectID");
            if (define.getInt("show") == 1) {
                for (JSONObject effectDefine : activityEffectMap.get(effectID)) {
                    int conditionType = effectDefine.getInt("conditiontype");
                    if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL) {
                        if (getGameProxy() != null) {
                            activity.setExpand(effectDefine.getInt("condition1"));
                            ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                            int level = resFunBuildProxy.getMaxLevelByBuildType(activity.getExpand());
                            addActivityConditionValue(conditionType, level, playerProxy, activity.getExpand());
                        }
                    } else if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_COM_LEVEL) {
                        int level = playerProxy.getLevel();
                        addActivityConditionValue(conditionType, level, playerProxy, 0);
                    } else if (conditionType == ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION) {
                        if (playerProxy.getPlayer().getArmygroupId() > 0) {
                            int value = 1;
                            addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION, value, playerProxy, 0);
                        }
                    }
                }
            }
        }
    }


    public void saveActivity() {
        List<Activity> activityList = new ArrayList<>();
        synchronized (changeActivitys) {
            while (true) {
                Activity activity = changeActivitys.poll();
                if (activity == null) {
                    break;
                }
                activityList.add(activity);
            }
        }
        for (Activity activity : activityList) {
            activity.save();
        }

    }

    private LinkedList<Activity> changeActivitys = new LinkedList<>();

    private void pushActivityToChangeList(Activity activity) {
        //插入更新队列
        synchronized (changeActivitys) {
            if (!changeActivitys.contains(activity)) {
                changeActivitys.offer(activity);
            }
        }
    }

    interface ConditionFormula {
        void calc(Activity activity, int value, int expandCondition, int sort);
    }

    private Map<Integer, ConditionFormula> _mapCondition = new HashMap<>();

    private void initCondition() {
        ConditionFormula condition;
        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    List<Integer> valuelist = activity.getValuelist();
                    long _value = valuelist.get(sort - 1) + value;
                    valuelist.add(sort - 1, (int) _value);
                    activity.setConditionValue(_value);
                    activity.setValuelist(valuelist);
                    if (define.getInt("condition2") <= _value) {
                        if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                            //如果满足条件，还未领取过则设置为可领
                            activity.addCanGetList(define.getInt("sort"));
                            //设置活动已经被修改，修改更新列表
                            //addFutureUpdateActivityList(getActivityInfo(activity));
                        }
                    }
                }
            }
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_FIRST_CHARGE, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_EVERY_DAY_CHARGE, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_BEAR_WORLD_TIMES, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_BEAR_ARNENA_WINTITIMES_EVERYDAY, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_CHARGE_EVERYDAY_ATWILL, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_CHARGE_ATWILL, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_HALL_RESOURCE, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_SCIENCE_RESOURCE, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_HALL_COIN, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_SCIENCE_COIN, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_HALL, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_IN_SCIENCE, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_HIT_GUANQIA_TIMES, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_ZHENGFU_GUANQIA_TIMES, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_VIP_COST, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                int conditionValue = define.getInt("condition2");
                if (define.getInt("sort") == sort) {
                    List<Integer> valuelist = activity.getValuelist();
                    valuelist.add(sort - 1,  conditionValue);
                    if (conditionValue <= value) {
                        if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                            activity.addCanGetList(define.getInt("sort"));
                        }
                    }
                }
            }
            activity.setConditionValue(value);
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_COM_LEVEL, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_PUTON_PURPLE_EQUIP_NUM, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_VIP_GETREWARD, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_AFTER_LONGIN, condition);


        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                int conditionValue1 = define.getInt("condition1");
                int conditionValue2 = define.getInt("condition2");
                if (value == conditionValue1 && expandCondition >= conditionValue2 && define.getInt("sort") == sort) {
                    if (activity.getCanGetList().contains(define.getInt("sort")) == false
                            && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                        activity.addCanGetList(define.getInt("sort"));
                    }
                }
            }
            activity.setConditionValue(value);
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_PURPLE_EQUIP_NUM, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_VIPPEOPLE_NUM, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            if (list != null) {
                for (JSONObject define : list) {
                    int conditionValue1 = define.getInt("condition1");
                    int conditionValue2 = define.getInt("condition2");
                    if (conditionValue1 <= value && conditionValue2 >= value && define.getInt("sort") == sort) {
                        activity.setConditionValue(value);
                        activity.addCanGetList(define.getInt("sort"));
                        pushActivityToChangeList(activity);
                    }
                }
            }
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_CAPITY_RANK, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_GUANQIA_RANK, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_HONOR_RANK, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    List<Integer> valuelist = activity.getValuelist();
                    Long lastTime = activity.getLastCheckTime();
                    if (lastTime == 0) {
                        lastTime = GameUtils.getServerDate().getTime();
                    }
                    long addtime = (GameUtils.getServerDate().getTime() - lastTime) / 1000;
                    long _value = valuelist.get(sort - 1) + addtime;
                    valuelist.add(sort - 1, (int) _value);
                    activity.setLastCheckTime(GameUtils.getServerDate().getTime());
                    if (define.getInt("condition2") <= _value) {
                        if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                            //如果满足条件，还未领取过则设置为可领
                            activity.addCanGetList(define.getInt("sort"));
                        }
                    }
                }
            }
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_FIRST_ONLINETIME, condition);
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_ONLINE_TIME, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    List<Integer> valuelist = activity.getValuelist();
                    long _value = valuelist.get(sort - 1) + value;
                    valuelist.add(sort - 1, (int) _value);
                    activity.setValuelist(valuelist);
                    activity.setConditionValue(_value);
                    if (define.getInt("condition2") <= _value) {
                        if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                            //如果满足条件，还未领取过则设置为可领
                            activity.addCanGetList(define.getInt("sort"));
                        }
                    }
                }
            }
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_EVERY_CHARGE_FIRST, condition);
        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    List<Integer> valuelist = activity.getValuelist();
                    long _value = 0;
                    if (value == ActivityDefine.NO_CONTINUOUS) {
                        _value = ActivityDefine.CONTINUOUS;
                    } else {
                        _value = valuelist.get(sort - 1) + value;
                    }
                    valuelist.add(sort - 1, (int) _value);
                    activity.setValuelist(valuelist);
                    activity.setConditionValue(_value);
                    if (define.getInt("condition2") <= _value) {
                        if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                            //如果满足条件，还未领取过则设置为可领
                            activity.addCanGetList(define.getInt("sort"));
                        }
                    }
                }
            }
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_CHARGE_CONTIUNES, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    if (define.getInt("sort") == sort) {
                        List<Integer> valuelist = activity.getValuelist();
                        long _value = valuelist.get(sort - 1) + expandCondition;
                        valuelist.add(sort - 1, (int) _value);
                        activity.setValuelist(valuelist);
                        activity.setConditionValue(_value);
                        if (define.getInt("condition1") <= _value && define.getInt("condition2") == value) {
                            if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                    && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                                //如果满足条件，还未领取过则设置为可领
                                activity.addCanGetList(define.getInt("sort"));
                            }
                        }
                    }
                }
            }
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_TYPE_RESOUCE_GETNUM, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                if (value == define.getInt("condition1") && expandCondition <= define.getInt("condition2") && define.getInt("sort") == sort) {
                    if (activity.getCanGetList().contains(define.getInt("sort")) == false
                            && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                        //如果满足条件，还未领取过则设置为可领
                        activity.addCanGetList(define.getInt("sort"));
                    }
                }
            }
            activity.setConditionValue(value);
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            int nowtime = DateUtil.getNowHoureMinitetoInt();
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    if (define.getInt("condition1") <= nowtime && define.getInt("condition2") > nowtime) {
                        if (activity.getCanGetList().contains(define.getInt("sort")) == false
                                && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                            //如果满足条件，还未领取过则设置为可领
                            activity.addCanGetList(define.getInt("sort"));
                        }
                    } else {
                        List<Integer> cangetlist = activity.getCanGetList();
                        cangetlist.remove(new Integer(define.getInt("sort")));
                        activity.setCanGetList(cangetlist);
                    }
                }
            }
            activity.setConditionValue(value);
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_ENERY_EVERYDAY, condition);

        condition = (Activity activity, int value, int expandCondition, int sort) -> {
            JSONObject acDefine = activityMap.get(activity.getActivityId());
            List<JSONObject> list = activityEffectMap.get(acDefine.getInt("effectID"));
            for (JSONObject define : list) {
                int conditionValue = define.getInt("condition2");
                if (conditionValue == value && define.getInt("sort") == sort) {
                    if (activity.getCanGetList().contains(define.getInt("sort")) == false
                            && activity.getAlreadyGetList().contains(define.getInt("sort")) == false) {
                        activity.addCanGetList(define.getInt("sort"));
                    }
                }
            }
            activity.setConditionValue(value);
            pushActivityToChangeList(activity);
        };
        _mapCondition.put(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION, condition);
    }

    public Map<Integer, Long> getEffectBufferPower() {
        super.expandPowerMap.clear();
        Map<Integer, Long> map = new HashMap<Integer, Long>();
        if (getGameProxy() == null) {
            return map;
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (JSONObject activityDefine : activityMap.values()) {
            if (isEffect(activityDefine, playerProxy)) {
                if (activityDefine.getInt("show") != 1 || activityEffectMap.get(activityDefine.getInt("effectID")) == null) {
                    continue;
                }
                for (JSONObject jsonObject : activityEffectMap.get(activityDefine.getInt("effectID"))) {
                    map.put(jsonObject.getInt("effecttype"), (long) jsonObject.getInt("effect"));
                    if (jsonObject.getInt("effecttype") == ActivityDefine.ACTIVITY_CONDITION_BUILD_LEVEL_SPEED) {
                        addBuildPlayerPower(PlayerPowerDefine.NOR_POWER_buildspeedrate, jsonObject.getInt("effect"));
                    }
                    if (jsonObject.getInt("effecttype") == ActivityDefine.ACTIVITY_CONDITION_CREATE_TANKE_SPEED) {
                        addBuildPlayerPower(PlayerPowerDefine.NOR_POWER_armyprorate, jsonObject.getInt("effect"));
                    }
                    if (jsonObject.getInt("effecttype") == ActivityDefine.ACTIVITY_CONDITION_CHANGE_TANK_SPEED) {
                        addBuildPlayerPower(PlayerPowerDefine.NOR_POWER_armyremrate, jsonObject.getInt("effect"));
                    }
                       /* if(jsonObject.getInt("conditiontype")==ActivityDefine.ACTIVITY_CONDITION_RESOURCE_SCIECE_SPEED){
                            addBuildPlayerPower(PlayerPowerDefine.NOR_POWER_scirespeedrate,jsonObject.getInt("effect"));
                        }*/
                }
            }
        }
        return map;
    }

    public int getEffectBufferPowerByType(int type) {
        Map<Integer, Long> map = getEffectBufferPower();
        if (map.get(type) != null) {
            return map.get(type).intValue();
        }
        return 0;
    }

    public List<M23.ActivityInfo> getAllActivityList() {
        List<M23.ActivityInfo> list = new ArrayList<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Activity> activitielist = new ArrayList<Activity>();
        activitielist.addAll(activitys);
        SortUtil.anyProperSort(activitielist, "getActivityId", true);
        for (Activity activity : activitielist) {
            JSONObject activityDefine = activityMap.get(activity.getActivityId());
            if (activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE && activity.getCanGetList().size() <= 0) {
                continue;
            }
            if (activityDefine.getInt("show") != 1) {
                continue;
            }
            if (isEffect(activityDefine, playerProxy)) {
                list.add(getActivityInfo(activity));
            } else if (activityDefine.getInt("endjudge") !=0 && activity.getCanGetList().size() > 0) {
                list.add(getActivityInfo(activity));//虽然过期但是还是可领取的
            }
        }
        return list;
    }

    /**
     * getAllLimitActivityInfo()获取限时活动列表
     *
     * @return list
     */
    public List<M23.LimitActivityInfo> getAllLimitActivityInfo() {
        List<M23.LimitActivityInfo> list = new ArrayList<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Activity> activitielist = new ArrayList<Activity>();
        activitielist.addAll(activitys);
        SortUtil.anyProperSort(activitielist, "getActivityId", true);
        for (Activity activity : activitielist) {
            JSONObject activityDefine = activityMap.get(activity.getActivityId());
            if (activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                continue;
            }
            if (activityDefine.getInt("show") == 2) {

                if (activityDefine.getInt("endjudge") == 1 && activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                    //不过活动开启关闭，只要奖励领取完毕，都会关闭
                    continue;
                }
                if (isEffect(activityDefine, playerProxy)) {
                    list.add(getLimitActivityInfo(activity));
                } else if (activityDefine.getInt("endjudge") > 0) {
                    //特殊活动判断
                    if (activityDefine.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LEGIONSHARE_ID && activityDefine.getInt("show") == 2) {
                        if (getLegionShareCangetCount(activity.getLegionShare()) > 0) {
                            list.add(getLimitActivityInfo(activity));
                        }
                    } else {
                        //其他活动
                    }
                }

            }
        }
        // Collections.
        return list;
    }

    //获得生效拉霸活动
    public List<JSONObject> getLaBaActivitid() {
        List<JSONObject> list = new ArrayList<JSONObject>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (Activity activity : activitys) {
            JSONObject activityDefine = activityMap.get(activity.getActivityId());
            if (activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                continue;
            }
            if (activityDefine.getInt("show") == 2) {

                if (activityDefine.getInt("endjudge") == 1 && activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                    //不过活动开启关闭，只要奖励领取完毕，都会关闭
                    continue;
                }
                if (isEffect(activityDefine, playerProxy)) {
                    //特殊活动判断
                    if (activityDefine.getInt("uitype") == ActivityDefine.LABA_UITYPE && activityDefine.getInt("show") == 2) {
                        list.add(activityDefine);
                    }
                }
            }
        }
        return list;
    }

    /**
     * getLimitActivityInfo()获取限时活动信息
     *
     * @param activity
     * @return builder.build()
     */
    public M23.LimitActivityInfo getLimitActivityInfo(Activity activity) {
        M23.LimitActivityInfo.Builder builder = M23.LimitActivityInfo.newBuilder();
        JSONObject define = activityMap.get(activity.getActivityId());
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        builder.setUitype(define.getInt("uitype"));
        builder.setActivityId(define.getInt("ID"));
        builder.setEffectId(define.getInt("effectID"));
        builder.setName(define.getString("name"));
        builder.setBgIcon(define.getInt("bgIcon"));
        builder.setInfo(define.getString("info"));
        builder.setStartTime((int) (getActivityStarTime(define, playerProxy) / 1000));
        builder.setEndTime((int) (getActivityEndTime(define) / 1000));
        return builder.build();
    }


    /**
     * getLaBaActivityByEffectId()通过effectId获取对应的活动
     *
     * @param effectId 活动id
     * @return
     */
    private Activity getLaBaActivityByEffectId(int effectId) {
        for (Activity activity : activitys) {
            if (activity.getActivityId() == effectId) {
                return activity;
            }
        }
        return null;
    }

    /**
     * getLaBaInfo()获取拉霸活动信息
     *
     * @param effectId 活动ID
     * @param builder  M23.LaBaInfo.Builder
     * @return rs
     */
    public int getLaBaInfo(int effectId, M23.LaBaInfo.Builder builder, int type) {
        int rs = 0;
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int times = timerdbProxy.getTimerNum(TimerDefine.LABA_LOTTER_FREETIME, 0, 0);
        List<JSONObject> jsonObjects = getLaBaActivitid();
        boolean falg = false;
        for (JSONObject jsonObject : jsonObjects) {
            if (jsonObject.getInt("effectID") == effectId) {
                falg = true;
            }
        }
        if (falg == false) {
            rs = ErrorCodeDefine.M230003_11;
        }
        if (getLaBaActivityByEffectId(effectId) == null) {
            rs = ErrorCodeDefine.M230003_11;
        } else {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LABA, "ID", effectId);
            List<JSONObject> define = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_DESIGN, "effectID", effectId);
            for (JSONObject json : define) {
                if (json.getInt("uitype") == ActivityDefine.LABA_UITYPE && json.getInt("effectID") == effectId) {
                    builder.setId(jsonObject.getInt("ID"));
                    builder.setPrice(jsonObject.getInt("price"));
                    builder.setTenPrice(jsonObject.getInt("tenprice"));
                    if (times < jsonObject.getInt("freetime")) {
                        builder.setFree(jsonObject.getInt("freetime"));
                    } else {
                        builder.setFree(0);
                    }
                    builder.setRewardgroup(jsonObject.getInt("rewardgroup"));
                    builder.setStartTime((int) (getActivityStarTime(json, playerProxy) / 1000));
                    builder.setEndTime((int) (getActivityEndTime(json) / 1000));
                }
            }
            builder.setType(type);
            builder.build();
        }
        return rs;
    }

    /**
     * getLaBaPrice()获取拉霸抽奖价格
     *
     * @param type 拉霸抽奖类型（非免费）
     * @return price
     */
    public int getLaBaPrice(int type) {
        JSONObject laba = ConfigDataProxy.getConfigInfoFindById(DataDefine.LABA, 1);
        int price = 0;
        if (type == ActivityDefine.LABA_TEN_LOTTER_TYPE) {
            price = laba.getInt("tenprice");
        }
        if (type == ActivityDefine.LABA_ONE_LOTTER_TYPE) {
            price = laba.getInt("price");
        }
        return price;
    }

    /**
     * getRewardById()随机获取拉霸奖励
     *
     * @param rewardgroup 活动奖励组id
     * @return
     */
    public JSONObject getRewardById(int rewardgroup) {
        List<JSONObject> reward = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.LABA_REWARD, "rewardgroup", rewardgroup);
        if (reward == null || reward.size() == 0) {
            return null;
        }
        int maxRate = 0;
        for (JSONObject jsonObject : reward) {
            maxRate += jsonObject.getInt("rate");
        }
        int rand = GameUtils.getRandomValueByRange(maxRate);
        JSONObject define = null;
        int rate = 0;
        for (JSONObject jsonObject1 : reward) {
            rate += jsonObject1.getInt("rate");
            if (rand <= rate) {
                define = jsonObject1;
                break;
            }
        }
        return define;
    }

    /**
     * showFreeTime()显示拉霸免费次数
     *
     * @return freetime
     */
    public int showFreeTime() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int times = timerdbProxy.getTimerNum(TimerDefine.LABA_LOTTER_FREETIME, 0, 0);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LABA, "ID", 1);
        if (times < jsonObject.getInt("freetime")) {
            return jsonObject.getInt("freetime");
        } else {
            return 0;
        }
    }


    /**
     * LaBaLotter()执行拉霸抽奖
     *
     * @param type         抽奖类型
     * @param effectId     活动id
     * @param playerReward 玩家奖励
     * @param builder      M23.LaBaInfo.Builder
     * @return rs
     */
    public int LaBaLotter(int type, int effectId, PlayerReward playerReward, M23.LaBaInfo.Builder builder) {
        int rs = 0;
        List<JSONObject> jsonObjects = getLaBaActivitid();
        boolean falg = false;
        for (JSONObject jsonObject : jsonObjects) {
            if (jsonObject.getInt("effectID") == effectId) {
                falg = true;
            }
        }
        if (falg == false) {
            return ErrorCodeDefine.M230003_11;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int times = timerdbProxy.getTimerNum(TimerDefine.LABA_LOTTER_FREETIME, 0, 0);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.LABA, 1);
        int rewardGroup = jsonObject.getInt("rewardgroup");
        if (type == ActivityDefine.LABA_FREE_LOTTER_TYPE && times < jsonObject.getInt("freetime")) {
            timerdbProxy.addNum(TimerDefine.LABA_LOTTER_FREETIME, 0, 0, 1);
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            JSONObject define = getRewardById(rewardGroup);
            int id = define.getInt("ID");
            builder.setRewardgroupId(id);
            JSONArray rewardList = define.getJSONArray("reward");
            for (int i = 0; i < rewardList.length(); i++) {
                rewardProxy.getPlayerRewardByFixReward(rewardList.getInt(i), playerReward);
            }
            rewardProxy.getRewardToPlayer(playerReward, LogDefine.GET_ARENA_LASTREWARD);
            //获取拉霸信息
            getLaBaInfo(effectId, builder, type);
        } else {
            int cost = getLaBaPrice(type);
            if (type != ActivityDefine.LABA_FREE_LOTTER_TYPE) {
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < cost) {
                    return ErrorCodeDefine.M230003_10;
                }
            }
            if (type != ActivityDefine.LABA_FREE_LOTTER_TYPE && playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) >= cost) {
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, cost, LogDefine.LOST_LABA_LOTTER);
                RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                if (type == ActivityDefine.LABA_ONE_LOTTER_TYPE) {
                    JSONObject define = getRewardById(rewardGroup);
                    int id = define.getInt("ID");
                    builder.setRewardgroupId(id);
                    JSONArray rewardList = define.getJSONArray("reward");
                    for (int i = 0; i < rewardList.length(); i++) {
                        rewardProxy.getPlayerRewardByFixReward(rewardList.getInt(i), playerReward);
                    }
                    rewardProxy.getRewardToPlayer(playerReward, LogDefine.GET_LABA_LOTTER);
                }
                if (type == ActivityDefine.LABA_TEN_LOTTER_TYPE) {
                    JSONObject tenDefine = getRewardById(rewardGroup);
                    int tenId = tenDefine.getInt("ID");
                    builder.setRewardgroupId(tenId);
                    for (int i = 0; i < ActivityDefine.LABA_TEN_LOTTER_TYPE; i++) {
                        JSONArray rewardList = tenDefine.getJSONArray("reward");
                        for (int j = 0; j < rewardList.length(); j++) {
                            rewardProxy.getPlayerRewardByFixReward(rewardList.getInt(j), playerReward);
                        }
                    }
                    rewardProxy.getRewardToPlayer(playerReward, LogDefine.GET_LABA_LOTTER);
                }
            }
        }
        return rs;
    }


    /**
     * 获得活动的可领取个数，包括限时活动和普通活动
     *
     * @return [普通个数, 限时个数]
     */
    public int[] getActivityCanGetNum() {
        int num = 0;
        int limitNum = 0;
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (Activity activity : activitys) {
            if (activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                continue;
            }
            JSONObject activityDefine = activityMap.get(activity.getActivityId());
            if (activityDefine.getInt("show") == 1) {
                //普通活动
                num += activity.getCanGetList().size();
            } else if (activityDefine.getInt("show") == 2) {
                //限时活动
                int endjudge = activityDefine.getInt("endjudge");
                if (endjudge == 0) {
                    //活动时间到就结束
                    if (!isEffect(activityDefine, playerProxy)) continue;
                    if (activityDefine.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LABA_ID) {
                        //拉霸
                        limitNum += showFreeTime();
                    }
                } else if (endjudge == 1) {
                    //领取完奖励就结束
                    if (activity.getState() != ActivityDefine.ACTIVITY_STATE_DONE) {
                        limitNum += activity.getCanGetList().size();
                    }
                } else if (endjudge == 2) {
                    //活动结束，并且奖励已经领取完
                    if (activityDefine.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LEGIONSHARE_ID) {
                        //有福同享，特殊处理
                        limitNum += getLegionShareCangetCount(activity.getLegionShare());
                    }
                }
            }

        }
        return new int[]{num, limitNum};
    }

    @Override
    protected void sendSystemChatToPlayerService(M25.M250000.S2C msg) {
        super.sendSystemChatToPlayerService(msg);
    }

    public M23.ActivityInfo getActivityInfo(Activity activity) {
        M23.ActivityInfo.Builder builder = M23.ActivityInfo.newBuilder();
        JSONObject define = activityMap.get(activity.getActivityId());
        builder.setActivityId(activity.getActivityId());
        builder.setName(define.getString("name"));
        builder.setArtIcon(define.getInt("artIcon"));
        builder.setInfo(define.getString("info"));
        builder.setUitype(define.getInt("uitype"));
        builder.setTitle(define.getString("title"));
        builder.setSort(define.getInt("sort"));
        JSONArray buttonArray = define.getJSONArray("button");
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (define.getInt("uitype") == ActivityDefine.TEXT_UITYPE) {
            builder.setText(define.getString("text"));
        }
        builder.setShow(define.getInt("show"));
        int effectId = define.getInt("effectID");
        List<JSONObject> list = activityEffectMap.get(effectId);
        if (define.getInt("uitype") == ActivityDefine.FIRST_CHARGE_UITYPE) {//首充
            M23.ButtonInfo.Builder buttonBuilder = M23.ButtonInfo.newBuilder();
            if (activity.getConditionValue() > 0) {
                buttonBuilder.setType(2);
                buttonBuilder.setName("立即领取");
            } else {
                JSONArray button = buttonArray.getJSONArray(0);
                buttonBuilder.setType(button.getInt(0));
                buttonBuilder.setName(button.getString(1));
                buttonBuilder.setJump(button.getString(2));
                if (button.length() > 3) {
                    buttonBuilder.setJumpPanel(button.getString(3));
                }
            }
            List<JSONObject> list1 = activityEffectMap.get(define.getInt("effectID"));
            if (define.getString("name").equals("军团好礼") && playerProxy.getPlayer().getArmygroupId() <= 0) {
                for (JSONObject define1 : list1) {
                    activity.removeCanGetList(define1.getInt("sort"));
                }
                JSONArray button = buttonArray.getJSONArray(0);
                buttonBuilder.setType(button.getInt(0));
                buttonBuilder.setName(button.getString(1));
                buttonBuilder.setJump(button.getString(2));
                if (button.length() > 3) {
                    buttonBuilder.setJumpPanel(button.getString(3));
                }
            }
            builder.addButtons(buttonBuilder);
        } else if (define.getInt("uitype") == ActivityDefine.INVESTMENT_UITYPE) {
            //投资计划
            if (activity.getBuyInv() == ActivityDefine.ACTIVITY_STATE_INVEST) {
                M23.ButtonInfo.Builder buttonBuilder = M23.ButtonInfo.newBuilder();
                JSONArray button = buttonArray.getJSONArray(0);
                buttonBuilder.setType(button.getInt(0));
                buttonBuilder.setName(button.getString(1));
                if (button.length() > 2) {
                    buttonBuilder.setJump(button.getString(2));
                }
                if (button.length() > 3) {
                    buttonBuilder.setJumpPanel(button.getString(3));
                }
                builder.addButtons(buttonBuilder);
            } else {
                for (int i = 0; i < buttonArray.length(); i++) {
                    JSONArray button = buttonArray.getJSONArray(i);
                    M23.ButtonInfo.Builder buttonBuilder = M23.ButtonInfo.newBuilder();
                    buttonBuilder.setType(button.getInt(0));
                    buttonBuilder.setName(button.getString(1));
                    if (button.length() > 2) {
                        buttonBuilder.setJump(button.getString(2));
                    }
                    if (button.length() > 3) {
                        buttonBuilder.setJumpPanel(button.getString(3));
                    }
                    builder.addButtons(buttonBuilder);
                }
            }
        } else {
            for (int i = 0; i < buttonArray.length(); i++) {
                JSONArray button = buttonArray.getJSONArray(i);
                M23.ButtonInfo.Builder buttonBuilder = M23.ButtonInfo.newBuilder();
                buttonBuilder.setType(button.getInt(0));
                buttonBuilder.setName(button.getString(1));
                if (button.length() > 3) {
                    buttonBuilder.setJumpPanel(button.getString(3));
                }
                if (button.length() > 2) {
                    buttonBuilder.setJump(button.getString(2));
                }

                //判断领取按钮的状态
                if (buttonBuilder.getType() == 2) {
                    JSONObject canEff = null;
                    for (JSONObject effD : list) {
                        if (activity.getCanGetList().contains(effD.getInt("sort"))) {
                            //执行领取
                            canEff = effD;
                            break;
                        }
                    }
                    if (canEff == null) {
                        buttonBuilder.setType(3);//未可领
                        buttonBuilder.setName("未可领取");
                    } else {
                        buttonBuilder.setName("立即领取");
                    }
                }

                builder.addButtons(buttonBuilder);
            }
        }


        builder.setTotal(0);
        boolean falg = false;
        int max = 0;
        for (JSONObject effectDefine : list) {
            builder.addEffectInfos(getActivityEffectInfo(effectDefine, activity, define));
            if (define.getInt("uitype") == ActivityDefine.CHARGE_EVERY_DAY_UITYPE) {
                int total = effectDefine.getInt("condition2");
                if (total > max) {
                    max = total;
                }
                if (activity.getConditionValue() < total) {
                    if (builder.getTotal() == 0) {
                        builder.setTotal(total);
                        falg = true;
                    } else if (builder.getTotal() > total) {
                        builder.setTotal(total);
                        falg = true;
                    }
                }
            }
        }
        if (falg == false) {
            builder.setTotal(max);
        }

        long starTime = getActivityStarTime(define, playerProxy);
        if (starTime != -1) {
            builder.setStartTime((int) (starTime / 1000));
        }
        long endTime = getActivityEndTime(define);
        if (endTime != -1) {
            builder.setEndTime((int) (endTime / 1000));
        }
        JSONObject effectjson = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ACTIVE_EFFECT, "effectID", define.getInt("effectID"));
        builder.setConditiontype(effectjson.getInt("conditiontype"));
        builder.setAlready((int) activity.getConditionValue());
       /* PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);*/
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_CAPITY_RANK) {
            builder.setAlready(playerProxy.rankmap.get(PowerRanksDefine.POWERRANK_TYPE_CAPACITY));
            if (activity.getConditionValue() > 0) {
                builder.setAlready(-(int) activity.getConditionValue());
            }
        }
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_GUANQIA_RANK) {
            builder.setAlready(playerProxy.rankmap.get(PowerRanksDefine.POWERRANK_TYPE_CUSTOMS));
            if (activity.getConditionValue() > 0) {
                builder.setAlready(-(int) activity.getConditionValue());
            }
        }
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_HONOR_RANK) {
            builder.setAlready(playerProxy.rankmap.get(PowerRanksDefine.POWERRANK_TYPE_HONOR));
            if (activity.getConditionValue() > 0) {
                builder.setAlready(-(int) activity.getConditionValue());
            }
        }
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK) {
            if (playerProxy.getArmGrouId() > 0) {
                builder.setAlready(ArmyGroupService.armymap().get(playerProxy.getArmGrouId()).getLevelrank());
            }
            if (activity.getConditionValue() > 0) {
                builder.setAlready(-(int) activity.getConditionValue());
            }
        }
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION) {
            if (playerProxy.getArmGrouId() > 0) {
                builder.setAlready(ArmyGroupService.armymap().get(playerProxy.getArmGrouId()).getRank());
            }
            if (activity.getConditionValue() > 0) {
                builder.setAlready(-(int) activity.getConditionValue());
            }
        }
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK) {
            if (playerProxy.getArmGrouId() > 0) {
                builder.setAlready(playerProxy.getPlayer().getActivitycontributerank());
            }
            if (activity.getConditionValue() > 0) {
                builder.setAlready(-(int) activity.getConditionValue());
            }
        }
        if (effectjson.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_VIPPEOPLE_NUM) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVE_EFFECT, activity.getActivityId());
            int num = jsonObject.getInt("condition1");
            builder.setAlready(getVipMap(num));
        }
        builder.setBgIcon(define.getInt("bgIcon"));
        return builder.build();
    }

    private int getHaveBuyTimes(Activity activity, int sort) {
        List<Integer> timeList = activity.getBuyTimesList();
        JSONObject activityDefine = activityMap.get(activity.getActivityId());
        int effectId = activityDefine.getInt("effectID");
        List<JSONObject> effects = activityEffectMap.get(effectId);
        if (timeList == null || timeList.size() != effects.size()) {
            timeList.clear();
            for (int i = 0; i < effects.size(); i++) {
                timeList.add(0);
            }
            activity.setBuyTimesList(timeList);
            pushActivityToChangeList(activity);
        }
        return timeList.get(sort - 1);
    }

    private M23.ActivityEffectInfo getActivityEffectInfo(JSONObject effectDefine, Activity activity, JSONObject activityDefine) {
        M23.ActivityEffectInfo.Builder effBuilder = M23.ActivityEffectInfo.newBuilder();
        effBuilder.setType(effectDefine.getInt("type"));
        effBuilder.setEffectId(effectDefine.getInt("effectID"));
        effBuilder.setConditionName(effectDefine.getString("name"));
        int sort = effectDefine.getInt("sort");
        effBuilder.setSort(sort);
        effBuilder.setInfo(effectDefine.getString("info"));
        int iscanget = effectDefine.getInt("iscanget");
        if (iscanget == 0) {
            effBuilder.setIscanget(iscanget);
        } else if (iscanget == 2) {
            effBuilder.setIscanget(iscanget);
            int totalTimes = effectDefine.getInt("limit");
            effBuilder.setTotalLimit(totalTimes);
            effBuilder.setLimit(getHaveBuyTimes(activity, sort));
        } else {
            if (activity.getCanGetList().contains(sort)) {
                effBuilder.setIscanget(1);
            } else if (activity.getAlreadyGetList().contains(sort)) {
                effBuilder.setIscanget(4);
            } else {
                effBuilder.setIscanget(3);
            }
        }
        if (effectDefine.getString("name").equals("加入军团")) {
            effBuilder.setIscanget(0);
        }
        effBuilder.setIcon(effectDefine.getString("icon"));
        if (effectDefine.getString("jumpbutton").equals("无") == false) {
            effBuilder.setJumpbutton(effectDefine.getString("jumpbutton"));
            effBuilder.setJumpmodule(effectDefine.getString("jumpmodule"));
        }
        effBuilder.addAllRewards(packRewardInfo(effectDefine.getJSONArray("reward")));
        effBuilder.setEffecttype(effectDefine.getInt("effecttype"));
        effBuilder.setEffect(effectDefine.getInt("effect"));

        JSONArray orgPriceArray = effectDefine.getJSONArray("originalprice");
        for (int i = 0; i < orgPriceArray.length(); i++) {
            M23.PriceInfo.Builder orgPrice = M23.PriceInfo.newBuilder();
            JSONArray priceDefine = orgPriceArray.getJSONArray(i);
            orgPrice.setPower(priceDefine.getInt(0));
            orgPrice.setTypeId(priceDefine.getInt(1));
            orgPrice.setNum(priceDefine.getInt(2));
            effBuilder.addOriginalprice(orgPrice);
        }
        JSONArray disPriceArray = effectDefine.getJSONArray("disprice");
        for (int i = 0; i < disPriceArray.length(); i++) {
            M23.PriceInfo.Builder disPrice = M23.PriceInfo.newBuilder();
            JSONArray priceDefine = disPriceArray.getJSONArray(i);
            disPrice.setPower(priceDefine.getInt(0));
            disPrice.setTypeId(priceDefine.getInt(1));
            disPrice.setNum(priceDefine.getInt(2));
            effBuilder.addDisprice(disPrice);
        }
        return effBuilder.build();
    }

    private List<Common.RewardInfo> packRewardInfo(JSONArray rewards) {
        List<Common.RewardInfo> infos = new ArrayList<>();
        for (int i = 0; i < rewards.length(); i++) {
            JSONArray reward = rewards.getJSONArray(i);
            Common.RewardInfo.Builder builder = Common.RewardInfo.newBuilder();
            builder.setPower(reward.getInt(0));
            builder.setTypeid(reward.getInt(1));
            builder.setNum(reward.getInt(2));
            infos.add(builder.build());
        }
        return infos;
    }


    /****
     * 各种增加活动的领取条件入口(expandCondition为扩展条件)
     ****/
    public void addActivityConditionValue(int conditionType, int value, PlayerProxy playerProxy, long expandCondition) {
        for (Activity activity : activitys) {
            JSONObject define = activityMap.get(activity.getActivityId());
            if (define.getInt("show") != 1) {
                //不是普通活动不走这个逻辑
                continue;
            }
            int defCondition = define.getInt("effectID");
            List<JSONObject> list = activityEffectMap.get(defCondition);
            if (list != null) {
                for (JSONObject jsonObject : list) {
                    if (jsonObject.getInt("conditiontype") == conditionType) {
                        //如果是活动期间满足条件
                        if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL) {
                            if (activity.getExpand() != expandCondition) {
                                continue;//不是升级的同种建筑的话就不处理了
                            }
                        }
//                    if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_FIRST_ONLINETIME){
//                        long regTime=(long)playerProxy.getCreatePlayerTime()*1000;
//                        if (DateUtil.getDayIntwoTime(regTime,GameUtils.getServerDate().getTime()) != 1){
//                            continue;//不是第一天登陆了
//                        }
//                    }
                        if (define.getInt("uitype") == ActivityDefine.INVESTMENT_UITYPE && activity.getBuyInv() != ActivityDefine.ACTIVITY_STATE_INVEST) {
                            continue;//没有在投资中
                        }
                        if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_VIP_COST) {

                        }
                        if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_VIPPEOPLE_NUM) {
                    /*    JSONObject jsonObject=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ACTIVE_EFFECT,"ID",activity.getActivityId());
                        value = jsonObject.getInt("condition1");
                        expandCondition = getVipMap(value);*/
                            _mapCondition.get(conditionType).calc(activity, value, (int) expandCondition, jsonObject.getInt("sort"));
                        }
                        if(conditionType == ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION){
                            _mapCondition.get(conditionType).calc(activity, value, (int) expandCondition, jsonObject.getInt("sort"));
                        }
                        if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_CAPITY_RANK ||
                                conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_GUANQIA_RANK ||
                                conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_HONOR_RANK ||
                                conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK ||
                                conditionType == ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK) {
                            if (addRankPowerCondition(define, playerProxy)) {
                                _mapCondition.get(conditionType).calc(activity, value, 0, jsonObject.getInt("sort"));
                            }
                            _mapCondition.get(conditionType).calc(activity, value, (int) expandCondition, jsonObject.getInt("sort"));
                        } else if (isEffect(define, playerProxy)) {
                            if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_PURPLE_EQUIP_NUM) {
                                _mapCondition.get(conditionType).calc(activity, value, (int) expandCondition, jsonObject.getInt("sort"));
                            }  else if (conditionType == ActivityDefine.ACTIVITY_CONDITION_TYPE_RESOUCE_GETNUM) {
                                _mapCondition.get(conditionType).calc(activity, value, (int) expandCondition, jsonObject.getInt("sort"));
                            }else {
                                _mapCondition.get(conditionType).calc(activity, value, 0, jsonObject.getInt("sort"));
                            }
                        }
                    }
                }
            }
        }

    }


    private long getActivityStarTime(JSONObject define, PlayerProxy playerProxy) {
        //  PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int timeType = define.getInt("timetype");
        long starTime = -1;
        switch (timeType) {
            case 1: {
                Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(openServerDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                starTime = calendar.getTimeInMillis();
                break;
            }
            case 2: {//开服后第几天开启，持续多久	A开服后第几天开启	 B持续时间
                Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
                starTime = openServerDate.getTime();
                break;
            }
            case 3: {
                break;
            }
            case 4: {
                Date beginDate = GameUtils.getDateFromStryyyyMMdd(define.getInt("timeA") + "");
                starTime = beginDate.getTime();
                break;
            }
            case 5: {
                Date createDate = new Date((long) playerProxy.getCreatePlayerTime() * 1000);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                starTime = calendar.getTimeInMillis();
                break;
            }
        }
        return starTime;
    }

    //获得下次要删除活动的时间
    public long nextDelActivitEndTime(PlayerProxy playerProxy) {
        long nowtime = GameUtils.getServerDate().getTime();
        long mintime = 0l;
        for (Activity activity : activitys) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVE_DESIGN, activity.getActivityId());
            if (jsonObject.getInt("show") == 1) continue;
            if (jsonObject != null && isEffect(jsonObject, playerProxy)) {
                long endtime = getActivityEndTime(jsonObject);
                if (endtime > nowtime) {
                    if (mintime == 0) {
                        mintime = endtime;
                    } else if (endtime < mintime) {
                        mintime = endtime;
                    }
                }
            }
        }
        return mintime;
    }


    //获得下次要开启的活动的时间
    public long nextAddActivitStartTime(PlayerProxy playerProxy) {
        long nowtime = GameUtils.getServerDate().getTime();
        long mintime = 0l;
        for (Activity activity : activitys) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVE_DESIGN, activity.getActivityId());
            if (jsonObject.getInt("show") == 1) continue;//现阶段只限制限时活动
            if (jsonObject != null && !isEffect(jsonObject, playerProxy)) {
                long startTime = getActivityStarTime(jsonObject, playerProxy);
                if (startTime > nowtime) {
                    if (mintime == 0) {
                        mintime = startTime;
                    } else if (startTime < mintime) {
                        mintime = startTime;
                    }
                }
            }
        }
        return mintime;
    }


    private long getActivityEndTime(JSONObject define) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int timeType = define.getInt("timetype");
        long endTime = -1;
        switch (timeType) {
            case 1: {//开服后第几天开启，持续多久	A开服后第几天开启	 B持续时间
                Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(openServerDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                long starTime = calendar.getTimeInMillis();
                endTime = starTime + (define.getInt("timeB") * 60 * 1000);
                break;
            }
            case 2: {//开服后第几天的某个点结算奖励	A开服后第几天 	B结算时间点
                Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(openServerDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                int timeB = define.getInt("timeB");
                int hour = timeB / 10000;
                int minute = timeB / 100 % 100;
                int seconds = timeB % 100;
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, seconds);
                Date endDate = calendar.getTime();
                endTime = endDate.getTime();
                break;
            }
            case 3: {
                break;
            }
            case 4: {
                Date endDate = GameUtils.getDateFromStryyyyMMdd(define.getInt("timeB") + "");
                endTime = endDate.getTime();
                break;
            }
            case 5: {
                Date createDate = new Date((long) playerProxy.getCreatePlayerTime() * 1000);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                long starTime = calendar.getTimeInMillis();
                endTime = starTime + (define.getInt("timeB") * 60 * 1000);
                break;
            }
        }
        return endTime;
    }


    private boolean isEffect(JSONObject define, PlayerProxy playerProxy) {
        int timeType = define.getInt("timetype");
//        System.out.println("替换成功了！！！");
//        timeType = 3;//测试用！！
        //判断时间
        Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
        if (openServerDate.after(GameUtils.getServerDate()) && timeType != 3) {
            return false;
        }
        switch (timeType) {
            case 1: {//开服后第几天开启，持续多久	A开服后第几天开启	 B持续时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(openServerDate);
                long serverTime = calendar.getTimeInMillis();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                long starTime = calendar.getTimeInMillis();
                calendar.setTime(GameUtils.getServerDate());
                long nowTime = calendar.getTimeInMillis();
                long between_days = (nowTime - serverTime) / (1000 * 60 * 60 * 24);
                if (between_days < define.getInt("timeA")) {
                    return false;
                }
                if (nowTime > starTime + (define.getInt("timeB") * 60 * 1000)) {
                    return false;
                }
                break;
            }
            case 2: {//开服后第几天的某个点结算奖励	A开服后第几天 	B结算时间点
//                Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(openServerDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long serverTime = calendar.getTimeInMillis();
                calendar.setTime(GameUtils.getServerDate());
                long nowTime = calendar.getTimeInMillis();
                long between_days = (nowTime - serverTime) / (1000 * 60 * 60 * 24);
                if (between_days > define.getInt("timeA")) {
                    return false;
                }
                break;
            }
            case 3: {//3	永久类型
                break;
            }
            case 4: {
                Date beginDate = GameUtils.getDateFromStryyyyMMdd(define.getInt("timeA") + "");
                Date endDate = GameUtils.getDateFromStryyyyMMdd(define.getInt("timeB") + "");
                Date now = GameUtils.getServerDate();
                if (beginDate.after(now) || endDate.before(now)) {
                    return false;
                }
                break;
            }
            case 5: {
                Date createDate = new Date((long) playerProxy.getCreatePlayerTime() * 1000);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createDate);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
                long beginTime = calendar.getTimeInMillis();//
                calendar.setTime(GameUtils.getServerDate());
                long nowTime = calendar.getTimeInMillis();
                if (nowTime < beginTime) {//还没到开启时间
                    return false;
                }
                if (nowTime > beginTime + (define.getInt("timeB") * 60 * 1000)) {
                    return false;
                }
                break;
            }
            default:
                return false;
        }

        //TODO 判断区
        int areaId = playerProxy.getAreaId();
        JSONArray areaArray = define.getJSONArray("server");
        boolean areaResult = false;
        for (int i = 0; i < areaArray.length(); i++) {
            int defineAreaId = areaArray.getInt(i);
            if (defineAreaId == areaId || defineAreaId == 0) {
                areaResult = true;
                break;
            }
        }
        if (areaResult == false) {
            return false;
        }
        //TODO 判断渠道

        //TODO 判断平台

        return true;
    }

    public boolean addRankPowerCondition(JSONObject define, PlayerProxy playerProxy) {
        Date openServerDate = GameMainServer.getOpenServerDateByAreaId(playerProxy.getAreaId());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openServerDate);
        int hour = define.getInt("timeB") / 10000;
        int minute = define.getInt("timeB") / 100 % 100;
        int second = define.getInt("timeB") % 100;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"));
        long activeTime = calendar.getTimeInMillis() / 1000;
        Calendar serverCalendar = Calendar.getInstance();
        serverCalendar.setTime(GameUtils.getServerDate());
        long serverTime = GameUtils.getServerDate().getTime() / 1000;
        if (activeTime == serverTime - 1 || activeTime == serverTime || activeTime == serverTime + 1 || activeTime == serverTime + 2) {
            return true;
        } else {
            return true;
        }
    }

    public int getActivityReward(int activityId, int effectId, int sort, PlayerReward reward) {
        JSONObject activityDefine = activityMap.get(activityId);
        // 各种逻辑判断

        List<JSONObject> list = activityEffectMap.get(activityDefine.getInt("effectID"));
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
//        if (isEffect(activityDefine,playerProxy) == false){
//            return ErrorCodeDefine.M230001_1;
//        }
        Activity activity = getActivityByTypeId(activityId);
        if (effectId == -1 && sort == -1) {
            //领取外层按钮,找出能领取的内层项执行领取
            JSONObject effectDefine = null;
            if (activityDefine.getInt("uitype") == ActivityDefine.INVESTMENT_UITYPE) {
                //投资计划点外层的按钮就是购买
                if (activity.getBuyInv() == ActivityDefine.ACTIVITY_STATE_INVEST) {
                    //已购买就别重复购买了
                    return ErrorCodeDefine.M230001_6;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < ActivityDefine.INVESTMENT_PRICE) {
                    return ErrorCodeDefine.M230001_7;
                }
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel) < ActivityDefine.INVESTMENT_VIP) {
                    return ErrorCodeDefine.M230001_8;
                }
                playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, ActivityDefine.INVESTMENT_PRICE, LogDefine.LOST_ACTIVITY_BUY);
                activity.setBuyInv(ActivityDefine.ACTIVITY_STATE_INVEST);
                ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
                int level = resFunBuildProxy.getResFuBuildLevelBysmallType(ResFunBuildDefine.BUILDE_TYPE_COMMOND, 1);
                addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_BUILD_LEVEL, level, playerProxy, ResFunBuildDefine.BUILDE_TYPE_COMMOND);
                pushActivityToChangeList(activity);
                //写入行为日志
                sendFunctionLog(FunctionIdDefine.GET_BUY_ACTIVITY_REWARD_FUNCTION_ID, activityId, -1, 0);
            } else {
                for (JSONObject define : list) {
                    if (activity.getCanGetList().contains(define.getInt("sort"))) {
                        //执行领取
                        effectDefine = define;
                        break;
                    }
                }
                if (effectDefine == null) {
                    return ErrorCodeDefine.M230001_2;
                }
                int rs = getActivityEffect(effectDefine, activity, reward);
                if (rs < 0) {
                    return rs;
                }
                if (activityDefine.getInt("endjudge") == 1) {
                    activity.setState(ActivityDefine.ACTIVITY_STATE_DONE);
                }
                //写入行为日志
                sendFunctionLog(FunctionIdDefine.GET_BUY_ACTIVITY_REWARD_FUNCTION_ID, activityId, effectDefine.getInt("sort"), 0);
            }
        } else {
            //领取里层按钮
            for (JSONObject define : list) {
                if (define.getInt("sort") == sort) {
                    int rs = getActivityEffect(define, activity, reward);
                    if (rs < 0) {
                        return rs;
                    }
                }
            }
        }
        //写入行为日志
        sendFunctionLog(FunctionIdDefine.GET_BUY_ACTIVITY_REWARD_FUNCTION_ID, activityId, sort, 0);
        if (activity.getAlreadyGetList().size() == list.size() && activityDefine.getInt("endjudge") == 1) {
            activity.setState(ActivityDefine.ACTIVITY_STATE_DONE);
            writeInvolvedActivityLog(activity, playerProxy, true);
        } else {
            writeInvolvedActivityLog(activity, playerProxy, false);
        }
        pushActivityToChangeList(activity);
        return 0;
    }

    //写入日志
    private void writeInvolvedActivityLog(Activity activity, PlayerProxy playerProxy, boolean done) {
        tbllog_activity log = new tbllog_activity();
        log.setAccount_name(playerProxy.getAccountName());
        log.setAction_id(activity.getActivityId());
        log.setDim_level(playerProxy.getLevel());
        log.setHappend_time(GameUtils.getServerTime());
        log.setLog_time(GameUtils.getServerTime());
        log.setPlatform(playerProxy.getPlayerCache().getPlat_name());
        log.setRole_id(playerProxy.getPlayerId());
        log.setStatus(1);
        if (done) {
            log.setStatus(2);
        }
        sendPorxyLog(log);
    }

    private int getActivityEffect(JSONObject effectDefine, Activity activity, PlayerReward reward) {
        if (effectDefine.getInt("iscanget") == ActivityDefine.ACTIVITY_BUTTON_TYPE_BUY) {
            //购买
            int sort = effectDefine.getInt("sort");
            int times = getHaveBuyTimes(activity, sort);
            if (times >= effectDefine.getInt("limit")) {
                return ErrorCodeDefine.M230001_3;
            }
            JSONArray array = effectDefine.getJSONArray("disprice");
            //先检查一次价格是否足够支付
            for (int i = 0; i < array.length(); i++) {
                JSONArray price = array.getJSONArray(i);
                int power = price.getInt(0);
                int typeId = price.getInt(1);
                int num = price.getInt(2);
                boolean enough = priceHandle(power, typeId, num, false);
                if (enough == false) {
                    return ErrorCodeDefine.M230001_4;
                }
            }

            //增加购买次数
            activity.getBuyTimesList().set(sort - 1, times + 1);
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            JSONArray rewardArray = effectDefine.getJSONArray("reward");
            for (int i = 0; i < rewardArray.length(); i++) {
                JSONArray rArray = rewardArray.getJSONArray(i);
                int power = rArray.getInt(0);
                int typeId = rArray.getInt(1);
                int num = rArray.getInt(2);
                rewardProxy.getRewardContent(reward, power, typeId, num);
            }
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_ACTIVITY_GET);
            for (int i = 0; i < array.length(); i++) {
                //扣除货币
                JSONArray price = array.getJSONArray(i);
                int power = price.getInt(0);
                int typeId = price.getInt(1);
                int num = price.getInt(2);
                priceHandle(power, typeId, num, true);
            }
        } else
//        if (effectDefine.getInt("iscanget") == ActivityDefine.ACTIVITY_BUTTON_TYPE_GET)
        {
            //领取
            int sort = effectDefine.getInt("sort");
            if (activity.getCanGetList().contains(sort) == false) {
                return ErrorCodeDefine.M230001_5;
            }
            activity.removeCanGetList(sort);
            activity.addAlreadyGetList(sort);
            //领取奖励逻辑
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            JSONArray rewardArray = effectDefine.getJSONArray("reward");
            for (int i = 0; i < rewardArray.length(); i++) {
                JSONArray rArray = rewardArray.getJSONArray(i);
                int power = rArray.getInt(0);
                int typeId = rArray.getInt(1);
                int num = rArray.getInt(2);
                rewardProxy.getRewardContent(reward, power, typeId, num);
            }
            if (effectDefine.getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_FIRST_CHARGE) {
                //首冲奖励的话需要做点特殊处理
                if (reward.addPowerMap.containsKey(PlayerPowerDefine.POWER_gold)) {
                    int gold = (int) (reward.addPowerMap.get(PlayerPowerDefine.POWER_gold) + activity.getValuelist().get(effectDefine.getInt("sort")-1));
                    reward.addPowerMap.put(PlayerPowerDefine.POWER_gold, gold);
                } else {
                    reward.addPowerMap.put(PlayerPowerDefine.POWER_gold, (int) ( activity.getValuelist().get(effectDefine.getInt("sort")-1)));
                }
            }
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_ACTIVITY_GET);
        }
        //单个活动刷新缓存
        // addFutureUpdateActivityList(getActivityInfo(activity));
        return 0;
    }

    private boolean priceHandle(int power, int typeId, int num, boolean remove) {
        switch (power) {
            case PlayerPowerDefine.BIG_POWER_RESOURCE: {
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                long value = playerProxy.getPowerValue(typeId);
                if (value < num) {
                    return false;
                }
                if (remove == true) {
                    playerProxy.reducePowerValue(typeId, num, LogDefine.LOST_ACTIVITY_BUY);
                }
                break;
            }
            case PlayerPowerDefine.BIG_POWER_ITEM: {
                //TODO 也许会有道具兑换之类的逻辑
            }
        }
        return true;
    }

    public void initActivityCondition() {
        initDefineMap();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        checkActivity(playerProxy);
        initCondition();
        specialActivity(playerProxy);
        checkActivityRefresh(playerProxy);
        checkDelActivity();
        checkAddActivity();
    }

    public void reloadDefineData(PlayerProxy playerProxy) {
        initDefineMap();
        checkActivity(playerProxy);
        initCondition();
        specialActivity(playerProxy);
        checkActivityRefresh(playerProxy);
        checkDelActivity();
        checkAddActivity();
    }

    /***
     * 检查活动刷新
     ***/
    public void checkActivityRefresh(PlayerProxy playerProxy) {
        long now = GameUtils.getServerDate().getTime();
        for (Activity activity : activitys) {
            JSONObject define = activityMap.get(activity.getActivityId());
            if (isEffect(define, playerProxy)) {
                if (activity.getRefurceTime() != 0 && activity.getRefurceTime() <= now) {
                    if (define.getInt("resettime") == 0) {
                        continue;
                    }
                    activity.clearAlreadyGetList();
                    activity.clearCanGetList();
                    activity.setConditionValue(0);
                    activity.setState(ActivityDefine.ACTIVITY_STATE_GOING);
                    activity.setLastCheckTime(GameUtils.getServerDate().getTime());
                    int size = activity.getBuyTimesList().size();
                    activity.clearBuyTimesList();
                    for (int i = 0; i < size; i++) {
                        activity.addBuyTimesList(0);
                    }
                    activity.setRefurceTime(getNextRefurceTime(define, activity.getRefurceTime(), playerProxy));
                }
            }
        }
        getEffectBufferPower();//刷新增益活动


    }

    //登陆刷新校验时间
    public void loginCheckRefreshTime() {
        for (Activity activity : activitys) {
            activity.setLastCheckTime(GameUtils.getServerDate().getTime());
        }
    }

    //时间活动检验
    public void checkeTimeActivity(List<M3.TimeInfo> infos) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        M3.TimeInfo info = timerdbProxy.setLesTime(TimerDefine.ACTIVITY_REFRESH, 0, 0, 60);
        timerdbProxy.addTimeDbToList(info, infos);
        if (info != null) {
            infos.add(info);
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_FIRST_ONLINETIME, 0, playerProxy, 0);
        addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_ONLINE_TIME, 0, playerProxy, 0);
        long regTime = (long) playerProxy.getCreatePlayerTime() * 1000;
        int days = DateUtil.getDayIntwoTime(regTime, GameUtils.getServerDate().getTime());
        addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_AFTER_LONGIN, days, playerProxy, 0);
    }

    //获取全服相应vip等级的玩家数量
    public HashMap<Integer, Integer> getAllVipPlayerNum() {
        HashMap<Integer, Integer> vipMap = new HashMap();
        VipActSetDb vipActSetDb = BaseSetDbPojo.getSetDbPojo(VipActSetDb.class, areaKey);
        for (long level : vipActSetDb.getAllVipExp().values()) {
            if (vipMap.get((int) level) != null) {
                int anum = vipMap.get((int) level);
                vipMap.put((int) level, anum + 1);
            } else {
                vipMap.put((int) level, 1);
            }
        }
        List<JSONObject> viplist = ConfigDataProxy.getConfigAllInfo(DataDefine.VIPDATA);
        for (int lv = 0; lv <= viplist.size(); lv++) {
            int max = 0;
            for (int lvtemp : vipMap.keySet()) {
                if (lvtemp >= lv) {
                    max += vipMap.get(lvtemp);
                }
            }
            vipMap.put(lv, max);
        }
        return vipMap;
    }

    //判断vipMap是否为空
    public int getVipMap(int level) {
        if (getAllVipPlayerNum().get(level) == null) {
            return 0;
        }
        return getAllVipPlayerNum().get(level);
    }

    @Override
    public void shutDownProxy() {
        for (Activity activity : activitys) {
            activity.finalize();
        }
    }

    @Override
    protected void init() {

    }


    /**
     * 获取有福同享活动activity
     *
     * @return
     */
    private Activity getPlayerYouFuTongXiangActivity() {
        Activity result = null;
        for (Activity activity : activitys) {
            JSONObject define = activityMap.get(activity.getActivityId());
            if (define.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LEGIONSHARE_ID && define.getInt("show") == 2) {
                result = activity;
                break;
            }
        }
        return result;
    }

    /**
     * 获得有福同享配置
     *
     * @return
     */
    private JSONObject getYouFuTongXiangActivityConfig() {
        JSONObject result = null;
        for (JSONObject define : activityMap.values()) {
            if (define.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LEGIONSHARE_ID && define.getInt("show") == 2) {
                result = define;
                break;
            }
        }
        return result;
    }

    /**
     * 根据id返回有福同享奖励
     *
     * @param id
     * @param playerReward
     */
    private void generateLegionShareReward(int id, PlayerReward playerReward) {
        JSONObject jsonObj = ConfigDataProxy.getConfigInfoFindById(DataDefine.LegionShare, id);
        if (jsonObj == null) {
            return;
        }
        JSONArray jsonArray = jsonObj.getJSONArray("reward");
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int i = 0; i < jsonArray.length(); i++) {
            rewardProxy.getPlayerRewardByFixReward(jsonArray.getInt(i), playerReward);
        }
    }

    /**
     * 有福同享充值回调
     *
     * @param playerProxy 当前玩家
     * @param money       money
     */
    public void handleLegionShareCharge(PlayerProxy playerProxy, int money, PlayerReward playerReward) {
        //判断活动是否过期
        if (!isEffect(getYouFuTongXiangActivityConfig(), playerProxy))
            return;
        //判断是否达到了充值条件
        Map<Integer, Integer> conditionMap = getLegionShareCondition();
        int chargeId = 0;
        List<Integer> tempList = new ArrayList<>(conditionMap.size());
        tempList.addAll(conditionMap.keySet());
        for (int i = 0; i <= tempList.size(); i++) {
            if (money >= tempList.get(i) && (i + 1) <= tempList.size() - 1 && money < tempList.get(i + 1)) {
                chargeId = conditionMap.get(tempList.get(i));
                break;
            } else if (i == tempList.size() - 1 && money >= tempList.get(i)) {
                //充值的额度超出了配置的最大值
                chargeId = conditionMap.get(tempList.get(i));
                break;
            }
        }
        if (chargeId == 0) {
            CustomerLogger.warning("有福同享找不到id money=" + money);
            return;
        }
        //充值的人 物品直接发到背包
        generateLegionShareReward(chargeId, playerReward);
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        //发送奖励
        rewardProxy.getRewardToPlayer(playerReward, LogDefine.GET_LEGIONSHARE_RECHARGE);
        //军团不存在
        long armId = playerProxy.getArmGrouId();
        if (armId <= 0) return;
        //拿到所有军团的人，增加记录
        //addLegionShareRecord(playerId :Long,chargeId:Int,createTime:Int, sharePlayerName: String)//有福同享 活动 增加宝箱记录
        GameMsg.addLegionShareRecord recordMsg = new GameMsg.addLegionShareRecord(playerProxy.getPlayerId(), chargeId, GameUtils.getServerTime(), playerProxy.getPlayerName());
        //通知ArmygroupNode
        sendArmygroupNodeMsg(recordMsg, armId);
    }


    /**
     * 增加有福同享宝箱记录
     *
     * @param playerProxy     玩家
     * @param chargeId        充值所在段位
     * @param sharePlayerName 分享者名字
     * @param recordTime      时间
     * @return M23.M230005.S2C
     */
    public M23.M230005.S2C addLegionShareRecord(PlayerProxy playerProxy, int chargeId, String sharePlayerName, int recordTime) {
        M23.M230005.S2C.Builder builder = M23.M230005.S2C.newBuilder();
        builder.setRs(0);
        Activity activity = getPlayerYouFuTongXiangActivity();//自己的活动对象
        if (activity == null) {
            //如果不存在，新增
            JSONObject activityDefine = getYouFuTongXiangActivityConfig();
            if (activityDefine == null) return builder.build();
            activity = BaseDbPojo.create(Activity.class, areaKey);
            activity.setPlayerId(playerProxy.getPlayerId());
            activity.setActivityId(activityDefine.getInt("ID"));
            activity.setRefurceTime(getNextRefurceTime(activityDefine, 0, playerProxy));
        }
        String records = new StringBuilder().append("&").append(sharePlayerName).append("_")
                .append(chargeId).append("_")
                .append(recordTime).toString();
        String newRecord = activity.getLegionShare() + records;
        activity.setLegionShare(newRecord);
        activity.save();
        //通知activityModel 通知前端有礼包增加
        M23.LegionShareInfo.Builder legionBuilder = M23.LegionShareInfo.newBuilder();
        legionBuilder.setId(chargeId);
        legionBuilder.setPlayer(sharePlayerName);
        legionBuilder.setTimeLeft(LEGION_SHARE_LIMIT_TIME - (GameUtils.getServerTime() - recordTime));
        builder.addLegionShareInfo(legionBuilder);
        //activity.getAlreadyGetList().isEmpty()?activity.getCanGetList().add(1):activity.getCanGetList().set()
        return builder.build();
    }


    /**
     * 获得有福同享活动的充值额度<limitMoney,id>
     *
     * @return Map<Integer, Integer>
     */
    private Map<Integer, Integer> getLegionShareCondition() {
        List<JSONObject> configJson = ConfigDataProxy.getConfigAllInfo(DataDefine.LegionShare);
        if (configJson == null) return null;
        Map<Integer, Integer> conditionMap = new TreeMap<>();//limitMoney,id
        JSONObject chargeConfig;
        for (JSONObject define : configJson) {
            int chargeId = define.getInt("chargeID");
            chargeConfig = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.Charge, "ID", chargeId);
            conditionMap.put(chargeConfig.getInt("limit"), define.getInt("ID"));
        }
        return conditionMap;
    }

    /**
     * 有福同享活动 字符串数据转换
     *
     * @param records legionShare
     * @return List<String[]>
     */
    private List<String[]> convertLegionShareStr2Arr(String records) {
        String[] recordArr = records.split("&");
        List<String[]> recordList = new ArrayList<String[]>(recordArr.length);
        for (String rec : recordArr) {
            if (StringUtils.isBlank(rec)) continue;
            String[] childArr = rec.split("_");//礼包id_分享人id_分享时间
            recordList.add(childArr);
        }
        return recordList;
    }


    /**
     * 获取有福同享可以领取的个数
     *
     * @param legionShareStr 有福同享字段
     * @return 奖励个数
     */
    private int getLegionShareCangetCount(String legionShareStr) {
        int result = 0;
        if (!StringUtils.isBlank(legionShareStr)) {
            List<String[]> recordList = convertLegionShareStr2Arr(legionShareStr);
            Iterator<String[]> recordItor = recordList.iterator();
            while (recordItor.hasNext()) {
                String[] record = recordItor.next();//name_id_time
                int recordTime = Integer.parseInt(record[2]);
                //判断过期，如果过期移除，24小时
                int currentTime = GameUtils.getServerTime();
                int leftTime = LEGION_SHARE_LIMIT_TIME - (currentTime - recordTime);
                if (leftTime <= 0) {
                    continue;
                }
                result++;
            }
        }
        return result;
    }

    /**
     * 有福同享记录转换成 str
     *
     * @param recordList
     * @return
     */
    private String legionShareRecordList2String(List<String[]> recordList) {
        StringBuilder sb = new StringBuilder();
        for (String[] tempStr : recordList) {
            sb.append(tempStr[0] + "_" + tempStr[1] + "_" + tempStr[2] + "&");
        }
        return sb.toString();
    }


    /**
     * 获取有福同享宝箱信息
     *
     * @return M23.M230005.S2C
     */
    public M23.M230005.S2C getLegionShareBoxInfo() {
        M23.M230005.S2C.Builder builder = M23.M230005.S2C.newBuilder();
        builder.setRs(0);
        Activity activity = getPlayerYouFuTongXiangActivity();
        if (activity == null) {
            builder.setRs(ErrorCodeDefine.M2300006_1);
            return builder.build();
        }
        String legionShareStr = activity.getLegionShare();
        if (StringUtils.isBlank(legionShareStr)) {
            return builder.build();
        }
        List<String[]> recordList = convertLegionShareStr2Arr(legionShareStr);

        //格式：礼包id_分享人id_分享时间
        //  M23.ActivityInfo.Builder builder = M23.ActivityInfo.newBuilder();
        M23.LegionShareInfo.Builder legionBuilder;
        Iterator<String[]> recordIter = recordList.iterator();
        boolean isUpdate = false;
        while (recordIter.hasNext()) {
            String[] record = recordIter.next();//name_id_time
            int recordTime = Integer.parseInt(record[2]);
            //判断过期，如果过期移除，24小时
            int currentTime = GameUtils.getServerTime();
            int leftTime = LEGION_SHARE_LIMIT_TIME - (currentTime - recordTime);
            if (leftTime <= 0) {
                recordIter.remove();
                isUpdate = true;
                continue;
            }
            int id = Integer.parseInt(record[1]);
            String playerName = record[0];
            legionBuilder = M23.LegionShareInfo.newBuilder();
            legionBuilder.setId(id);
            legionBuilder.setPlayer(playerName);
            legionBuilder.setTimeLeft(leftTime);
            builder.addLegionShareInfo(legionBuilder);
        }
        if (isUpdate) {
            activity.setLegionShare(legionShareRecordList2String(recordList));
            activity.save();
        }
        return builder.build();
    }


    /**
     * 领取宝箱操作
     *
     * @param id     {"ID" : 1 ,"group" : 1 ,"chargeID" : 1 ,"reward" : [451]  }中的ID
     * @param reward PlayerReward
     * @return M23.M230006.S2C
     */
    public M23.M230006.S2C getLegionShareBoxAward(int id, PlayerReward reward) {
        M23.M230006.S2C.Builder builder = M23.M230006.S2C.newBuilder();
        builder.setResult(0);
        Activity activity = getPlayerYouFuTongXiangActivity();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (activity == null) {
            builder.setResult(ErrorCodeDefine.M2300006_1);
            return builder.build();
        }
        String legionShareStr = activity.getLegionShare();

        if (StringUtils.isBlank(legionShareStr)) {
            return builder.build();
        }
        List<String[]> recordList = convertLegionShareStr2Arr(legionShareStr);
        //格式：礼包id_分享人id_分享时间
        Iterator<String[]> recordItor = recordList.iterator();
        boolean hasUpdate = false;
        boolean isOverTimeOrNotExit = true;
        while (recordItor.hasNext()) {
            String[] record = recordItor.next();//name_id_time
            int exitId = Integer.parseInt(record[1]);
            if (exitId == id) {
                isOverTimeOrNotExit = false;
                int recordTime = Integer.parseInt(record[2]);
                //判断过期，如果过期移除，24小时
                if (LEGION_SHARE_LIMIT_TIME - (GameUtils.getServerTime() - recordTime) <= 0) {
                    //builder.setResult(ErrorCodeDefine.M2300006_2);//已经过期
                    recordItor.remove();
                    hasUpdate = true;
                    isOverTimeOrNotExit = true;
                    continue;
                } else {
                    generateLegionShareReward(id, reward);
                    recordItor.remove();//领取就移除
                    hasUpdate = true;
                }
                break;
            }
        }
        if (hasUpdate) {
            //保存数据Ac
            String legionStr = legionShareRecordList2String(recordList);
            activity.setLegionShare(legionStr);
            activity.save();
        }
        if (isOverTimeOrNotExit) {
            //提示不存在或已过期
            builder.setResult(ErrorCodeDefine.M2300006_2);
        }
        //判断是否可以结束掉活动
        if (!isEffect(getYouFuTongXiangActivityConfig(), playerProxy) && recordList.size() <= 0) {
            //通知这个活动已经结束，
           /* activity.setState(ActivityDefine.ACTIVITY_STATE_DONE);
            activity.save();*/
            futureDeleteActivity.add(activity.getActivityId());
        }
        return builder.build();
    }

    /**
     * 退出或被提出军团 把有福同享的宝箱记录清空
     */
    public void removeLegionShareAllRecord() {
        Activity activity = getPlayerYouFuTongXiangActivity();
        if (activity == null) return;
        activity.setLegionShare("");
        CustomerLogger.info(activity.getPlayerId() + " success to quit the legion legionShareRecord=" + activity.getLegionShare());
        activity.save();
    }


    /**
     * 定时器检测活动删除
     *
     * @return 要删除的活动ids
     */
    public void checkDelActivity() {
        if (getGameProxy() == null) return;
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long minDelActivityTime = nextDelActivitEndTime(playerProxy);
        long lastOpTime = timerdbProxy.getLastOperatinTime(TimerDefine.ACTIVITY_DEL_FREETIME, 0, 0);//上一次设置的触发时间
        int now = GameUtils.getServerTime();
        if (lastOpTime != minDelActivityTime) {
            ////设置下一个要删除的活动时间
            timerdbProxy.setLastOperatinTime(TimerDefine.ACTIVITY_DEL_FREETIME, 0, 0, minDelActivityTime);
        }
        if (lastOpTime <= 0 || now < (lastOpTime / 1000)) {
            return;
        }
        for (Activity activity : activitys) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVE_DESIGN, activity.getActivityId());
            //现阶段只做限时活动
            if (jsonObject != null && jsonObject.getInt("show") == 2) {
                long endtime = getActivityEndTime(jsonObject);
                if (endtime <= lastOpTime) {
                    int endJudge = jsonObject.getInt("endjudge");//0.时间到就结束，1.领取完所领取的奖励就结束，2.活动时间到，并且领取完了奖励 才结束
                    //int type=jsonObject.getInt("show");//1.普通,2.限时
                    if (endJudge == 0) {
                        futureDeleteActivity.add(activity.getActivityId());
                    } else if (endJudge > 0) {
                        //特殊活动判断-有福同享
                        if (jsonObject.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LEGIONSHARE_ID) {
                            if (getLegionShareCangetCount(activity.getLegionShare()) <= 0) {
                                futureDeleteActivity.add(activity.getActivityId());
                            }
                        } else if (activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                            futureDeleteActivity.add(activity.getActivityId());
                        }
                    }
                }
            }
        }
    }


    /**
     * 定时器检测活动增加（先阶段只对限时活动生效）
     *
     * @return 要删除的活动ids
     */
    public void checkAddActivity() {
        if (getGameProxy() == null) return;
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        long currentTime = GameUtils.getServerTime();
        long minAddActivityTime = nextAddActivitStartTime(playerProxy);
        long lastOpTime = timerdbProxy.getLastOperatinTime(TimerDefine.ACTIVITY_ADD_FREETIME, 0, 0);//上一次设置的触发时间
        if (lastOpTime != minAddActivityTime) {
            ////设置下一个要增加的活动时间
            timerdbProxy.setLastOperatinTime(TimerDefine.ACTIVITY_ADD_FREETIME, 0, 0, minAddActivityTime);
        }
        if (lastOpTime <= 0 || currentTime < lastOpTime / 1000) return;
        for (Activity activity : activitys) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVE_DESIGN, activity.getActivityId());
            if (jsonObject != null) {
                long starTime = getActivityStarTime(jsonObject, playerProxy);
                if (starTime == lastOpTime) {
                    if (jsonObject.getInt("show") == 1) {
                        //普通活动
                        // addFutureUpdateActivityList(getActivityInfo(activity));
                    } else if (jsonObject.getInt("show") == 2) {
                        //限时活动
                        addFutureUpdateLimitActivityList(getLimitActivityInfo(activity));
                    }
                }
            }
        }
    }


    /**
     * 增加需要更新到前端的普通活动集合
     *
     * @param info
     */
    private void addFutureUpdateActivityList(M23.ActivityInfo info) {
        M23.M230007.S2C.Builder builder = M23.M230007.S2C.newBuilder();
        builder.setActivityInfo(info);
        futureUpdateActivityList.add(builder.build());
    }

    /**
     * 增加需要更新到前端的限时活动集合
     *
     * @param info
     */
    private void addFutureUpdateLimitActivityList(M23.LimitActivityInfo info) {
        M23.M230009.S2C.Builder builder = M23.M230009.S2C.newBuilder();
        builder.setActivityInfo(info);
        futureUpdateLimitActivityList.add(builder.build());
    }


    //获得关于请客下次开始时间
  /*  public long getGuanyuqingkeTme(){
        List<JSONObject> jsonObjectlist=ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT,"conditiontype",ActivityDefine.ACTIVITY_CONDITION_ENERY_EVERYDAY);
        Calendar c=Calendar.getInstance();
        c.setTime(GameUtils.getServerDate());
        if(jsonObjectlist.size()==0){
            return 0l;
        }
        int nowTime=c.get(Calendar.HOUR_OF_DAY)*100+c.get(Calendar.MINUTE);
        //最小时间点
        int mintime=2329;
        for(JSONObject jsonObject:jsonObjectlist){
         if(jsonObject.getInt("condition2")>nowTime){
             //今天的处理
             int hour=jsonObject.getInt("condition1")/100;
             int min=jsonObject.getInt("condition1")-(hour*100);
             c.set(Calendar.HOUR_OF_DAY,hour);
             c.set(Calendar.MINUTE,min);
             long nexttime=c.getTimeInMillis()-GameUtils.getServerDate().getTime();
             return nexttime;
         }
            if(jsonObject.getInt("condition1")<mintime){
                mintime=jsonObject.getInt("condition1");
            }
        }
        //明天的处理
        int hour=mintime/100;
        int min=mintime-(hour*100);
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,min);
        c.add(Calendar.DATE,1);
        long nexttime=c.getTimeInMillis()-GameUtils.getServerDate().getTime();
        return nexttime;
    }*/


    //获得军团大礼的可领红点
    public int getArmyActivityNum() {
        JSONObject effectJson = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ACTIVE_EFFECT, "conditiontype", ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION);
        if (effectJson == null) {
            return 0;
        }
        Activity act = getLaBaActivityByEffectId(effectJson.getInt("effectID"));
        if (act == null) {
            return 0;
        }
        return act.getCanGetList().size();
    }


    /**
     * 客户端请求检测活动是否要删除
     *
     * @param ids
     * @return 返回要删除的活动
     */
    public M23.M230008.S2C checkActivityToDelete(List<Integer> ids) {
        M23.M230008.S2C.Builder deleteBuilder = M23.M230008.S2C.newBuilder();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (Activity activity : activitys) {
            if (ids.contains(activity.getActivityId())) {
                //有福同享特殊判断
                JSONObject define = activityMap.get(activity.getActivityId());
                if (define.getInt("uitype") == ActivityDefine.LIMIT_ACTION_LEGIONSHARE_ID && define.getInt("show") == 2) {
                    if (!isEffect(define, playerProxy) && getLegionShareCangetCount(activity.getLegionShare()) <= 0) {
                        //过期并且没有活动可以领取了
                        deleteBuilder.addActivityIds(activity.getActivityId());
                    }
                } else {
                    int endjudge = define.getInt("endjudge");
                    if (endjudge == 0 && !isEffect(define, playerProxy)) {
                        deleteBuilder.addActivityIds(activity.getActivityId());
                    } else if (endjudge == 1 && activity.getState() == ActivityDefine.ACTIVITY_STATE_DONE) {
                        deleteBuilder.addActivityIds(activity.getActivityId());
                    }
                }
            }
        }
        return deleteBuilder.build();
    }
}
