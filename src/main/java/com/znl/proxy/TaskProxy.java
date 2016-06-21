package com.znl.proxy;

import akka.actor.dsl.Creators;
import com.mysql.jdbc.log.Log;
import com.znl.base.BaseDbPojo;
import com.znl.base.BaseLog;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.*;
import com.znl.log.TaskActivityLog;
import com.znl.pojo.db.Task;
import com.znl.proto.Common;
import com.znl.proto.M19;
import com.znl.utils.DateUtil;
import com.znl.utils.GameUtils;
import com.znl.utils.RandomUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Administrator on 2015/10/28.
 */
public class TaskProxy extends BasicProxy {
    private Set<Task> tasks = new ConcurrentHashSet<>();

    @Override
    public void shutDownProxy() {
        for (Task Task : tasks) {
            Task.finalize();
        }
    }

    @Override
    protected void init() {
        initaddcompletenessMetic();
        initGetRewardMetic();
    }


    public TaskProxy(Set<Long> Taskids, String areaKey) {
        this.areaKey = areaKey;
        for (Long id : Taskids) {
            Task task = BaseDbPojo.get(id, Task.class, areaKey);
            if (task == null) {
                System.out.print("任务出现空值");
            } else {
                tasks.add(task);
            }
        }
        init();
    }


    public void saveTasks() {
        List<Task> Tasks = new ArrayList<Task>();
        synchronized (changeTasks) {
            while (true) {
                Task Task = changeTasks.poll();
                if (Task == null) {
                    break;
                }
                Tasks.add(Task);
            }
        }
        for (Task Task : Tasks) {
            Task.save();
        }

    }

    private LinkedList<Task> changeTasks = new LinkedList<Task>();

    private void pushTaskToChangeList(Task Task) {
        //插入更新队列
        synchronized (changeTasks) {
            if (!changeTasks.contains(Task)) {
                changeTasks.offer(Task);
            }
        }
    }


    private boolean ishasTypeId(int tableType, int typeId) {
        for (Task task : tasks) {
            if (task.getTableType() == tableType && task.getTastId() == typeId) {
                return true;
            }
        }
        return false;
    }

    public long addTask(int tableType, int tastType, int typeId) {
        if (ishasTypeId(tableType, typeId)) {
            return 0;
        } else {
            return creatTask(tableType, tastType, typeId);
        }
    }

    private long creatTask(int tableType, int tastType, int typeId) {
        GameProxy gameProxy = super.getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Task task = BaseDbPojo.create(Task.class, areaKey);
        task.setNum(0);
        task.setState(0);
        task.setTastId(typeId);
        task.setPlayerId(playerProxy.getPlayerId());
        task.setTableType(tableType);
        task.setTaskType(tastType);
        tasks.add(task);
        playerProxy.addTaskIdToPlayer(task.getId());
        task.save();
        doaddcompleteness(tastType, 0, new PlayerReward(), 0);
        return task.getId();
    }

    public void delTask(int tableType, int typeId) {
        Task del = null;
        for (Task task : tasks) {
            if (task.getTableType() == tableType && task.getTastId() == typeId) {
                del = task;
                break;
            }
        }
        if (del != null) {
            delTask(del);
            tasks.remove(del);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            playerProxy.removeTaskIdFormPlayer(del.getId());
        }

    }

    private void delTask(Task task) {
        task.del();
        changeTasks.remove(task);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.removeTaskIdFormPlayer(task.getId());
    }


    private Task getTaskById(long id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    //根据某个表的和id获得任务
    private Task getTaskByTableTypeandId(int tableType, int id) {
        for (Task task : tasks) {
            if (task.getTableType() == tableType && task.getTastId() == id) {
                return task;
            }
        }
        return null;
    }

    //获得某个表某个类型的任务
    private List<Task> getTaskByTableTypeandTaskType(int tableType, int taskType) {
        List<Task> list = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getTableType() == tableType && task.getTaskType() == taskType) {
                list.add(task);
            }
        }
        return list;
    }

    //获得某个表里面的所有任务
    private List<Task> getTaskByTableTypeandTaskType(int tableType) {
        List<Task> list = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getTableType() == tableType) {
                list.add(task);
            }
        }
        return list;
    }


    //获得某个表里面的所有任务
    private List<Integer> getIdByTableType(int tableType) {
        List<Integer> list = new ArrayList<Integer>();
        for (Task task : tasks) {
            if (task.getTableType() == tableType) {
                list.add(task.getTastId());
            }
        }
        return list;
    }

    //删除某个表里面的所有任务
    private List<Task> delTaskByTableTypeandTaskType(int tableType) {
        List<Task> list = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getTableType() == tableType) {
                delTask(task);
                task.setIsget(TaskDefine.TASK_STATUS_DELETE);
                list.add(task);
            }
        }
        tasks.removeAll(list);
        return list;
    }


    //获得所有某个类型的任务
    private List<Task> getTaskBYTaskType(int taskType) {
        List<Task> list = new ArrayList<Task>();
        for (Task task : tasks) {
            if (task.getTaskType() == taskType) {
                list.add(task);
            }
        }
        return list;
    }

    private void addTaskNum(Task task, int add) {
        long num = task.getNum();
        task.setNum(num + add);
        pushTaskToChangeList(task);
    }


    private void setTaskNum(Task task, long num) {
        if (task.getNum() != num) {
            task.setNum(num);
            pushTaskToChangeList(task);
        }
    }

    private void changStau(Task task, int statu) {
        task.setState(statu);
        pushTaskToChangeList(task);
    }

    private void changIsget(Task task, int isget) {
        task.setIsget(isget);
        pushTaskToChangeList(task);
    }

    interface AddcompletenessFormula {
        boolean addcompleteness(int tastType, int addnum, PlayerReward reward, int type);
    }

    interface TaskFormula {
        void calc(JSONObject TaskDefine, int num, PlayerReward reward);
    }

    interface GetRewardFormula {
        int getMessionReward(int typeId, PlayerReward reward, M19.M190001.S2C.Builder builder);
    }

    private Map<Integer, AddcompletenessFormula> _mapTaskaddCompletedMetic = new HashMap<>();

    private Map<Integer, GetRewardFormula> _mapgetRewardMetic = new HashMap<>();

    public boolean doaddcompleteness(int tastType, int addnum, PlayerReward reward, int type) {
        return _mapTaskaddCompletedMetic.get(tastType).addcompleteness(tastType, addnum, reward, type);
    }

    private int dogetReward(int tableType, int typeId, PlayerReward reward, List<Long> chanlist, List<Task> dellist, M19.M190001.S2C.Builder builder) {
        return _mapgetRewardMetic.get(tableType).getMessionReward(typeId, reward, builder);
    }

    private int initGetRewardMetic() {
        GetRewardFormula formula;
        formula = (int typeId, PlayerReward reward, M19.M190001.S2C.Builder builder) -> {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.MAINMISSION, typeId);
            Task task = getTaskByTableTypeandId(TaskDefine.TABLE_TASK_MAIN_LINE, typeId);
            if (task == null) {
                return ErrorCodeDefine.M190001_1;//任务不存在
            }
            if (task.getIsget() != TaskDefine.TASK_STATUS_FINISH) {
                return ErrorCodeDefine.M190001_2;//不能领取的任务
            }
            //执行领取
            changIsget(task, TaskDefine.TASK_STATUS_HASGET);
            finishMainTaskgetNext(reward);
            JSONArray jsonArray = jsonObject.getJSONArray("fixreward");
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                rewardProxy.getPlayerRewardByFixReward(jsonArray.getInt(i), reward);
            }
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_MAIN_TASK_GETREWARD);
            initTaskAll();
            builder.addAllTaskInfos(getTaskInfoBytableType(TaskDefine.TABLE_TASK_MAIN_LINE));
            builder.addTaskInfos(getDelTaskInfoByTask(task));
            return 0;
        };
        _mapgetRewardMetic.put(TaskDefine.TABLE_TASK_MAIN_LINE, formula);
        formula = (int typeId, PlayerReward reward, M19.M190001.S2C.Builder builder) -> {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, typeId);
            Task task = getTaskByTableTypeandId(TaskDefine.TABLE_TASK_DAY, typeId);
            if (task == null) {
                return ErrorCodeDefine.M190001_1;//任务不存在
            }
            if (task.getIsget() != TaskDefine.TASK_STATUS_FINISH) {
                return ErrorCodeDefine.M190001_2;//不能领取的任务
            }
            changIsget(task, TaskDefine.TASK_STATUS_HASGET);
            JSONArray jsonArray = jsonObject.getJSONArray("fixreward");
            RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int exp = (int) ((500 + playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) * 25) * (1 + 0.2 * jsonObject.getInt("star")));
            playerProxy.addPowerValue(PlayerPowerDefine.POWER_exp, exp, LogDefine.GET_DAYLIY_TASK_DAYACTIVITY);
            randomDayMessionOne(typeId);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < jsonArray.length(); i++) {
                rewardProxy.getPlayerRewardByFixReward(jsonArray.getInt(i), reward);
                sb.append(jsonArray.getInt(i));
                sb.append(",");
            }
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLIY_TASK_GETREWARD);
            builder.addAllTaskInfos(getTaskInfoBytableType(TaskDefine.TABLE_TASK_DAY));
            sendFunctionLog(FunctionIdDefine.GET_TASK_REWOARD_FUNCTION_ID, typeId, 0, 0, sb.toString());
            return 0;
        };
        _mapgetRewardMetic.put(TaskDefine.TABLE_TASK_DAY, formula);
        formula = (int typeId, PlayerReward reward, M19.M190001.S2C.Builder builder) -> {
            //TODO 日常活跃
            return ErrorCodeDefine.M190001_2;//该类型不能领取
        };
        _mapgetRewardMetic.put(TaskDefine.TABLE_TASK_ACTIVITY_DAY, formula);
        return 0;

    }

    private void initaddcompletenessMetic() {
        AddcompletenessFormula formula;
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            boolean falg = false;
            //TODO 次数
            List<Task> taskList = getTaskBYTaskType(tastType);
            for (Task task : taskList) {

                if (isCanadd(task) && isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    addTaskNum(task, addnum);
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), task.getTastId());
                    if (jsonObject == null || task == null) {
                        System.err.println("空值出现了---------");
                        continue;
                    }
                    checkFinish(task, jsonObject, reward);
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_BEATGATE_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_BEATWORLD_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_COSTGOLD_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_BUILDLEVEUP_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_SCIENCELV_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_EQUIPLVUP_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_ARENAFIGHT_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_EQUIPTANXIAN_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_ORNDANCETANXIAN_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_JIXIANTANXIAN_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_FEIXUXUNBAO_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_BEATEWORLDRESOUCE_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_UNIONCONTRIBUTE_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_UNIONARENA_LV, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_GETUNIONTESTBOX_NUM, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_UNIOMCONVER_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_UNIONREST_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_LOTTEREQUIP_TIEMS, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_ORNDANCESTENGTH_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_ZHAOMUGENERAIS_TIMES, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_ADVANCEGENERAIS_TIMES, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO 1	声望等级
            boolean falg = false;
            List<Task> taskList = getTaskBYTaskType(tastType);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            for (Task task : taskList) {
                if (isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    setTaskNum(task, playerProxy.getPowerValue(PlayerPowerDefine.POWER_prestigeLevel));
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), task.getTastId());
                    checkFinish(task, jsonObject, reward);
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_SHENGWANG_LV, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO  2	军衔等级
            boolean falg = false;
            List<Task> taskList = getTaskBYTaskType(tastType);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            for (Task task : taskList) {
                if (isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    setTaskNum(task, playerProxy.getPowerValue(PlayerPowerDefine.POWER_militaryRank));
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), task.getTastId());
                    checkFinish(task, jsonObject, reward);
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_JUNXIAN_LV, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO 10	战胜关卡		关卡ID
            boolean falg = false;
            List<Task> taskList = getTaskBYTaskType(tastType);
            for (Task task : taskList) {
                if (isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    DungeoProxy dungeoProxy = getGameProxy().getProxy(ActorDefine.DUNGEO_PROXY_NAME);
                    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    int dungonId = dungeoProxy.getMaxEvenId(playerProxy.getHigestDungId());
                    setTaskNum(task, dungonId);
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), task.getTastId());
                    checkFinish(task, jsonObject, reward);
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_WINGATE_ID, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO  5	战胜资源点等级		资源点等级
            boolean falg = false;
            List<Task> taskList = getTaskBYTaskType(tastType);
            PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
            for (Task task : taskList) {
                if (isCanadd(task) && isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    int lv = playerProxy.getWorldResourceLevel();
                    if (task.getNum() > lv) {
                        continue;
                    }
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), task.getTastId());
                    setTaskNum(task, lv);
                    playerProxy.setWorldResourceLevel(0);
                    checkFinish(task, jsonObject, reward);
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_WINRESOURCE_LV, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO 6	建筑物等级	建筑物类型	建筑物等级  7	建筑物建造数量	建筑物类型	建筑物数量
            boolean falg = false;
            ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
            List<Task> taskList = getTaskBYTaskType(tastType);
            for (Task task : taskList) {
                if (isCanadd(task) && isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    int typeId = task.getTastId();
                    int buildType = 0;
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), typeId);
                    buildType = jsonObject.getInt("finishcond1");
                    if (buildType != 0) {
                        if (tastType == TaskDefine.TASK_TYPE_BUILDING_LV) {
                            int maxlevel = resFunBuildProxy.getMaxLevelByBuildType(buildType);
                            if (task.getIsget() != TaskDefine.TASK_STATUS_UNFISH) {
                                falg = false;
                            }
                            setTaskNum(task, maxlevel);
                            checkFinish(task, jsonObject, reward);
                        } else if (tastType == TaskDefine.TASK_TYPE_BUILDING_NUM) {
                            int num = resFunBuildProxy.getBuildTypeNum(buildType);
                            falg = true;
                            setTaskNum(task, num);
                            checkFinish(task, jsonObject, reward);
                        }
                    }
                }

            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_BUILDING_LV, formula);
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_BUILDING_NUM, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO 8	生成兵种	兵种类型	数量
            boolean falg = false;
            List<Task> taskList = getTaskBYTaskType(tastType);
            for (Task task : taskList) {
                if (isCanadd(task) && isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    int typeId = task.getTastId();
                    int soldierId = 0;
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), typeId);
                    soldierId = jsonObject.getInt("finishcond1");
                    long maxnum = task.getNum();
                    SoldierProxy soldierProxy = getGameProxy().getProxy(ActorDefine.SOLDIER_PROXY_NAME);
                    if (soldierId != 0) {
                      int  newaddnum = soldierProxy.getSoldierNum(soldierId);
                      /*  if(addnum<maxnum){
                            return;
                        }*/
                        setTaskNum(task, newaddnum);
                        checkFinish(task, jsonObject, reward);
                    } else {
                      /*  addnum=soldierProxy.getAllSoldierNum();*/
                      /*  if(addnum<maxnum) {
                         return;
                        }*/
                        addTaskNum(task, addnum);
                        checkFinish(task, jsonObject, reward);
                    }
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_CREATESODIER_NUM, formula);
        formula = (int tastType, int addnum, PlayerReward reward, int type) -> {
            //TODO 9	资源产量	资源类型	产量
            boolean falg = false;
            List<Task> taskList = getTaskBYTaskType(tastType);
            for (Task task : taskList) {
                if (isCanadd(task) && isCanAddTask(task)&&iscanChangeTaskNum(task)) {
                    falg = true;
                    int typeId = task.getTastId();
                    int power = 0;
                    JSONObject jsonObject = getTastJsonObject(task.getTableType(), typeId);
                    power = jsonObject.getInt("finishcond1");
                    PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                    if (power != TaskDefine.TASK_STATUS_UNFISH) {
                        long num = playerProxy.getPowerValue(power);
                        checkFinish(task, num, addnum, jsonObject, reward);
                    }
                }
            }
            return falg;
        };
        _mapTaskaddCompletedMetic.put(TaskDefine.TASK_TYPE_RESOURCE_VALUE, formula);
    }

    private boolean isCanadd(Task task) {
        if (task.getTableType() == TaskDefine.TABLE_TASK_DAY) {
            if (task.getState() != TaskDefine.TASK_STATUS_ACCEPT) {
                return false;
            }
        }
        return true;
    }

    private void checkWorldResouceFinish(Task task, JSONObject jsonObject, PlayerReward reward, int lv) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getWorldResourLevellist().contains((long) jsonObject.getInt("finishcond2")) && task.getIsget() == TaskDefine.TASK_STATUS_UNFISH) {
            if (task.getTableType() == TaskDefine.TABLE_TASK_ACTIVITY_DAY) {
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) >= jsonObject.getInt("opencond")) {
                    task.setNum(jsonObject.getInt("finishcond2"));
                    task.setIsget(TaskDefine.TASK_STATUS_FINISH);
                    playerProxy.setWorldResourceLevellist(new HashSet<>());
                    RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                    JSONArray array = jsonObject.getJSONArray("reward");
                    for (int i = 0; i < array.length(); i++) {
                        int rewardId = array.getInt(i);
                        rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
                    }
                    task.setIsget(TaskDefine.TASK_STATUS_HASGET);
                    rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLIY_TASK_DAYACTIVITY);
                }
            } else {
                task.setNum(jsonObject.getInt("finishcond2"));
                task.setIsget(TaskDefine.TASK_STATUS_FINISH);
                playerProxy.setWorldResourceLevellist(new HashSet<>());
            }
        }
    }


    private void checkFinish(Task task, JSONObject jsonObject, PlayerReward reward) {
        if (task.getNum() >= jsonObject.getInt("finishcond2") && task.getIsget() == TaskDefine.TASK_STATUS_UNFISH) {
            if (task.getTableType() == TaskDefine.TABLE_TASK_ACTIVITY_DAY) {
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) >= jsonObject.getInt("opencond")) {
                    task.setIsget(TaskDefine.TASK_STATUS_FINISH);
                    RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                    JSONArray array = jsonObject.getJSONArray("reward");
                    for (int i = 0; i < array.length(); i++) {
                        int rewardId = array.getInt(i);
                        rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
                    }
                    task.setIsget(TaskDefine.TASK_STATUS_HASGET);
                    rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLIY_TASK_DAYACTIVITY);
                }
            } else {
                task.setIsget(TaskDefine.TASK_STATUS_FINISH);
            }
        }
    }

    private void checkFinish(Task task, long num, int addnum, JSONObject jsonObject, PlayerReward reward) {
        if (task.getIsget() == TaskDefine.TASK_STATUS_UNFISH && task.getIsget() != TaskDefine.TASK_STATUS_HASGET) {
            if (task.getTableType() == TaskDefine.TABLE_TASK_ACTIVITY_DAY) {
                PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) >= jsonObject.getInt("opencond")) {
                    if (task.getNum() > num) {
                        addTaskNum(task, addnum);
                    } else {
                        if (num != 0) {
                            setTaskNum(task, num);
                        }
                    }
                    if (task.getNum() >= jsonObject.getInt("finishcond2")) {
                        task.setIsget(TaskDefine.TASK_STATUS_FINISH);
                        JSONArray array = jsonObject.getJSONArray("reward");
                        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
                        for (int i = 0; i < array.length(); i++) {
                            int rewardId = array.getInt(i);
                            rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
                        }
                        task.setIsget(TaskDefine.TASK_STATUS_HASGET);
                        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLIY_TASK_DAYACTIVITY);
                    }
                }
            } else {
                if (task.getNum() > num) {
                    addTaskNum(task, addnum);
                } else {
                    if (num != 0) {
                        setTaskNum(task, num);
                    }
                }
                if (task.getNum() >= jsonObject.getInt("finishcond2")) {
                    task.setIsget(TaskDefine.TASK_STATUS_FINISH);
                }
            }

        }
    }

    public JSONObject getTastJsonObject(int tableType, int typeId) {
        JSONObject jsonObject = null;
        if (tableType == TaskDefine.TABLE_TASK_MAIN_LINE) {
            jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.MAINMISSION, typeId);
        } else if (tableType == TaskDefine.TABLE_TASK_DAY) {
            jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, typeId);


        } else if (tableType == TaskDefine.TABLE_TASK_ACTIVITY_DAY) {
            jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYACTIVI, typeId);

        }
        return jsonObject;
    }

    /*********
     * 主线任务
     **********/
    private void finishMainTaskgetNext(PlayerReward reward) {
        List<JSONObject> objectList = ConfigDataProxy.getConfigAllInfo(DataDefine.MAINMISSION);
        for (JSONObject jsonObject : objectList) {
            JSONArray jsonArray = jsonObject.getJSONArray("premission");
            boolean falg = true;
            for (int i = 0; i < jsonArray.length(); i++) {
                int id = jsonArray.getInt(i);
                Task task = getTaskByTableTypeandId(TaskDefine.TABLE_TASK_MAIN_LINE, id);
                if (task == null) {
                    falg = false;
                    break;
                }
                if (task.getIsget() != TaskDefine.TASK_STATUS_HASGET) {
                    falg = false;
                    break;
                }
            }
            if (falg) {
                if (jsonObject.getInt("ID") == 2034) {
                    System.err.println("出现特殊任务");
                }
                addTask(TaskDefine.TABLE_TASK_MAIN_LINE, jsonObject.getInt("stype"), jsonObject.getInt("ID"));
            }
        }
        doaddcompleteness(TaskDefine.TASK_TYPE_CREATESODIER_NUM, 0, reward, 0);
    }

    //初始化主线任务
    private void initMainLine() {
        if (getTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_MAIN_LINE).size() == 0) {
            List<JSONObject> objectList = ConfigDataProxy.getConfigAllInfo(DataDefine.MAINMISSION);
            for (JSONObject jsonObject : objectList) {
                if (jsonObject.getJSONArray("premission").length() == 0) {
                    addTask(TaskDefine.TABLE_TASK_MAIN_LINE, jsonObject.getInt("stype"), jsonObject.getInt("ID"));
                }
            }
        }
    }

    /*********
     * 主线任务
     **********/

    public void initTask() {
        initMainLine();
        initDayActivity();
        initDayMission(new ArrayList<Common.TaskInfo>());
    }

    public void initTaskAll() {
        initDayMission(new ArrayList<Common.TaskInfo>());
        for (int i = 1; i <= 29; i++) {
            doaddcompleteness(i, 0, new PlayerReward(), 0);
        }
    }

    /**********
     * 日常任务
     **********/
    public void initDayMission(List<Common.TaskInfo> taskInfos) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lastime = timerdbProxy.getLastOperatinTime(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
        if (getTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_DAY).size() == 0 || GameUtils.getServerDate().getTime() >= lastime) {
            List<Task> taskList = new ArrayList<Task>();
            List<Integer> list = randomDayMession(new ArrayList<Task>());
            for (int id : list) {
                JSONObject jsonObject1 = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, id);
                addTask(TaskDefine.TABLE_TASK_DAY, jsonObject1.getInt("stype"), id);
            }
            taskInfos.addAll(getTaskInfosByTasklist(taskList));
            timerdbProxy.setNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0, 0);
            timerdbProxy.setLastOperatinTime(TimerDefine.FRIEND_DAY_MESSION, 0, 0, DateUtil.getNextHour(GameUtils.getServerDate().getTime(), TimerDefine.TIMER_REFRESH_FOUR));
            timerdbProxy.setAttrValue(TimerDefine.FRIEND_DAY_MESSION, 0, 0, 1, 0);
        }
    }


    //日常任务随机单个
    public void randomDayMessionOne(int delId) {
        delTask(TaskDefine.TABLE_TASK_DAY, delId);
        List<JSONObject> defineList = ConfigDataProxy.getConfigAllInfo(DataDefine.DAYMISSION);
        List<Integer> hasIds = getIdByTableType(TaskDefine.TABLE_TASK_DAY);
        List<Integer> ranlist = new ArrayList<Integer>();
        for (JSONObject jsonObject : defineList) {
            int typeId = jsonObject.getInt("ID");
            int persent = jsonObject.getInt("rate");
            if (!hasIds.contains(typeId)) {
                for (int i = 1; i <= persent; i++) {
                    ranlist.add(typeId);
                }
            }
        }
        int random = RandomUtil.randomByShuffleCards(ranlist.size()) + 1;
        JSONObject jsonObject1 = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, ranlist.get(random - 1));
        addTask(TaskDefine.TABLE_TASK_DAY, jsonObject1.getInt("stype"), ranlist.get(random - 1));
    }

    //日常任务随机
    public List<Integer> randomDayMession(List<Task> del) {
        del.addAll(delTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_DAY));
        List<Integer> rs = new ArrayList<Integer>();
        List<JSONObject> defineList = ConfigDataProxy.getConfigAllInfo(DataDefine.DAYMISSION);
        List<Integer> ranlist = new ArrayList<Integer>();
        for (JSONObject jsonObject : defineList) {
            int typeId = jsonObject.getInt("ID");
            int persent = jsonObject.getInt("rate");
            for (int i = 1; i <= persent; i++) {
                ranlist.add(typeId);
            }
        }
        for (int i = 1; i <= 5; i++) {
            int random = RandomUtil.randomByShuffleCards(ranlist.size()) + 1;
            rs.add(ranlist.get(random - 1));
            removeAllId(ranlist, ranlist.get(random - 1));
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


    /**********
     * 日常活跃
     **********/

    public void initDayActivity() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        long lastime = timerdbProxy.getLastOperatinTime(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        List<JSONObject> defineList = ConfigDataProxy.getConfigAllInfo(DataDefine.DAYACTIVI);
        List<Task> list = getTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_ACTIVITY_DAY);
        if (list.size() == 0) {
            for (JSONObject jsonObject : defineList) {
                addTask(TaskDefine.TABLE_TASK_ACTIVITY_DAY, jsonObject.getInt("stype"), jsonObject.getInt("ID"));
            }
        }
        System.out.println(new Date(lastime) + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        if (GameUtils.getServerDate().getTime() >= lastime) {
            for (Task task : list) {
                task.setNum(0);
                task.setState(0);
                task.setIsget(0);
                pushTaskToChangeList(task);
            }
            PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
            int value = (int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_active);
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_active, value, LogDefine.LOST_DAYLIY_REFRESH);
            timerdbProxy.setNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0, 0);
            timerdbProxy.setLastOperatinTime(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0, DateUtil.getNextHour(GameUtils.getServerDate().getTime(), TimerDefine.TIMER_REFRESH_FOUR));
            timerdbProxy.setAttrValue(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0, 1, 0);
        }
    }

    /**********
     * 日常活跃
     **********/
    public List<Common.TaskInfo> getTaskInfos() {
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        for (Task task : tasks) {
            if (getTaskInfoByTask(task) != null) {
                taskInfos.add(getTaskInfoByTask(task));
            }
        }
        return taskInfos;
    }

    public boolean isCanAddTask(Task task) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        boolean falg = true;
        if (task.getTableType() == TaskDefine.TABLE_TASK_MAIN_LINE) {
            JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.MAINMISSION, task.getTastId());
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) < jsonObject.getInt("opencond") || task.getIsget() == TaskDefine.TASK_STATUS_HASGET ) {
                falg = false;
            }
        }
        return falg;
    }

    public boolean iscanChangeTaskNum(Task task){
        boolean falg = true;
        if (task.getIsget() == TaskDefine.TASK_STATUS_HASGET || task.getIsget() == TaskDefine.TASK_STATUS_FINISH || task.getIsget() == TaskDefine.TASK_STATUS_DELETE) {
            falg = false;
        }
        return falg;
    }

    private Common.TaskInfo getTaskInfoByTask(Task task) {
        if (isCanAddTask(task)) {
            Common.TaskInfo.Builder builder =Common.TaskInfo.newBuilder();
            builder.setTableType(task.getTableType());
            builder.setTypeId(task.getTastId());
            builder.setNum(task.getNum());
            builder.setState(task.getIsget());
            builder.setAccept(task.getState());
            return builder.build();
        }
        return null;
    }

    private Common.TaskInfo getDelTaskInfoByTask(Task task) {
        Common.TaskInfo.Builder builder =Common.TaskInfo.newBuilder();
        builder.setTableType(task.getTableType());
        builder.setTypeId(task.getTastId());
        builder.setNum(task.getNum());
        builder.setState(TaskDefine.TASK_STATUS_DELETE);
        builder.setAccept(task.getState());
        return builder.build();
    }

    public List<Common.TaskInfo> getTaskInfoBytableType(int tableType) {
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        for (Task task : tasks) {
            if (task.getTableType() == tableType) {
                if (getTaskInfoByTask(task) != null) {
                    taskInfos.add(getTaskInfoByTask(task));
                }
            }
        }
        return taskInfos;
    }


    public List<Common.TaskInfo> getTaskInfoBytableTypedel(int tableType) {
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        for (Task task : tasks) {
            if (task.getTableType() == tableType) {
                if (getTaskInfoByTask(task) != null) {
                    taskInfos.add(getDelTaskInfoByTask(task));
                }
            }
        }
        return taskInfos;
    }


    public List<Common.TaskInfo> getTaskInfoBytaskType(int taskType) {
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        for (Task task : tasks) {
            if (isCanadd(task) && task.getTaskType() == taskType) {
                if (getTaskInfoByTask(task) != null) {
                    taskInfos.add(getTaskInfoByTask(task));
                }
            }
        }
        return taskInfos;
    }


    public List<Common.TaskInfo> getTaskInfosByTasklist(List<Task> tasklist) {
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        for (Task task : tasklist) {
            if (getTaskInfoByTask(task) != null) {
                taskInfos.add(getTaskInfoByTask(task));
            }
        }
        return taskInfos;
    }

    /*************
     * 协议
     ***********************/
    //任务领取奖励
    public int getSessionReward(int tableType, int typeId, PlayerReward reward, M19.M190001.S2C.Builder builder) {
        List<Long> chanlist = new ArrayList<Long>();
        List<Task> delTask = new ArrayList<Task>();
        int rs = dogetReward(tableType, typeId, reward, chanlist, delTask, builder);
        return rs;
    }

    //日常任务操作
    public int dayTaskOperate(int type, int typeId, M19.M190002.S2C.Builder builder, PlayerReward reward) {
        if (type == 1) {
            return acceptTask(typeId, builder);
        }
        if (type == 2) {
            return giveUpTask(typeId, builder);
        }
        if (type == 3) {
            return resetTask(builder);
        }
        if (type == 4) {
            return refreshTask(builder);
        }
        if (type == 5) {
            return finishTaskByCoin(typeId, builder, reward);
        }

        return 0;
    }

    //接受任务
    private int acceptTask(int typeId, M19.M190002.S2C.Builder builder) {
        Task task = getTaskByTableTypeandId(TaskDefine.TABLE_TASK_DAY, typeId);
        if (task == null) {
            return ErrorCodeDefine.M190002_1;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0) >= 5) {
            return ErrorCodeDefine.M190002_2;//不能接受任务了请重置
        }
        for (Task tas1 : getTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_DAY)) {
            if (tas1.getState() == TaskDefine.TASK_STATUS_ACCEPT) {
                return ErrorCodeDefine.M190002_3;//有任务在接受中
            }
        }
        changStau(task, TaskDefine.TASK_STATUS_ACCEPT);
        if (getTaskInfoByTask(task) != null) {
            builder.addTaskInfos(getTaskInfoByTask(task));
        }
        timerdbProxy.addNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0, 1);
        builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
        return 0;
    }

    //放弃任务
    private int giveUpTask(int typeId, M19.M190002.S2C.Builder builder) {
        Task task = null;
        for (Task task1 : getTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_DAY)) {
            if (task1.getState() == TaskDefine.TASK_STATUS_ACCEPT) {
                task = task1;
            }
        }
        if (task == null) {
            return ErrorCodeDefine.M190002_1;//
        }

        if (task.getIsget() != TaskDefine.TASK_STATUS_UNFISH) {
            return ErrorCodeDefine.M190002_16;//
        }
        changStau(task, TaskDefine.TASK_STATUS_UNACCEPT);
        setTaskNum(task, 0);
        if (getTaskInfoByTask(task) != null) {
            builder.addTaskInfos(getTaskInfoByTask(task));
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        timerdbProxy.reduceNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0, 1);
        builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
        return 0;
    }

    //重置任务
    private int resetTask(M19.M190002.S2C.Builder builder) {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < 25) {
            return ErrorCodeDefine.M190002_4;
        }
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        if (timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0) < 5) {
            return ErrorCodeDefine.M190002_5;//还不能重置
        }
        if (getDayResetTimes() <= 0) {
            return ErrorCodeDefine.M190002_6;//重置数次上限
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, 25, LogDefine.LOST_RESET_TASK);
     /*   List<Task> taskList = new ArrayList<Task>();
        List<Integer> list = randomDayMession(new ArrayList<Task>());
        for (int id : list) {
            JSONObject jsonObject1 = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, id);
            addTask(TaskDefine.TABLE_TASK_DAY, jsonObject1.getInt("stype"), id);
        }
        builder.addAllTaskInfos(getTaskInfosByTasklist(taskList));*/
        builder.addAllTaskInfos(getTaskInfoBytableType(TaskDefine.TABLE_TASK_DAY));
        timerdbProxy.setNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0, 0);
        timerdbProxy.addNum(TimerDefine.DAY_TASK_REST, 0, 0, 1);
        builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
        return 0;
    }

    //刷新任务
    private int refreshTask(M19.M190002.S2C.Builder builder) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        for (Task task : getTaskByTableTypeandTaskType(TaskDefine.TABLE_TASK_DAY)) {
            if (task.getState() == TaskDefine.TASK_STATUS_ACCEPT) {
                return ErrorCodeDefine.M190002_7;//有任务在接受中
            }
        }
        if (timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0) >= 5) {
            return ErrorCodeDefine.M190002_8;//
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < 5) {
            return ErrorCodeDefine.M190002_9;//
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, 5, LogDefine.LOST_REFRESH_TASK);
        List<Task> taskList = new ArrayList<Task>();
        List<Integer> list = randomDayMession(new ArrayList<Task>());
        for (int id : list) {
            JSONObject jsonObject1 = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, id);
            addTask(TaskDefine.TABLE_TASK_DAY, jsonObject1.getInt("stype"), id);
        }
        builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
        builder.addAllTaskInfos(getTaskInfoBytableType(TaskDefine.TABLE_TASK_DAY));
        return 0;
    }


    //用金币完成任务
    private int finishTaskByCoin(int typeId, M19.M190002.S2C.Builder builder, PlayerReward reward) {
        Task task = getTaskByTableTypeandId(TaskDefine.TABLE_TASK_DAY, typeId);
        if (task == null) {
            return ErrorCodeDefine.M190002_10;//
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < 5) {
            return ErrorCodeDefine.M190002_11;//
        }
        if (task.getState() != TaskDefine.TASK_STATUS_ACCEPT) {
            return ErrorCodeDefine.M190002_12;//
        }
        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, 5, LogDefine.LOST_FINISH_TASK_BYGOLD);
        randomDayMessionOne(typeId);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.DAYMISSION, typeId);
        JSONArray array = jsonObject.getJSONArray("fixreward");
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        for (int i = 0; i < array.length(); i++) {
            int rewardId = array.getInt(i);
            rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
        }
        int exp = (int) ((500 + playerProxy.getPowerValue(PlayerPowerDefine.POWER_level) * 25) * (1 + 0.2 * jsonObject.getInt("star")));
        // playerProxy.addPowerValue(PlayerPowerDefine.POWER_exp, exp, LogDefine.GET_DAYLIY_TASK_DAYACTIVITY);
        reward.addPowerMap.put(PlayerPowerDefine.POWER_exp, exp);
        builder.addAllTaskInfos(getTaskInfoBytableType(TaskDefine.TABLE_TASK_DAY));
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLIY_TASK_GETREWARD);
        return 0;
    }


    public int getDayActivity(PlayerReward reward, TaskActivityLog baseLog) {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        if (num >= getMaxActivity()) {
            return ErrorCodeDefine.M190003_13;//
        }
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVEREWARD, num + 1);
        if (jsonObject == null) {
            return ErrorCodeDefine.M190003_14;//
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_active) < jsonObject.getInt("activeneed")) {
            return ErrorCodeDefine.M190003_15;//
        }
        baseLog.setTypeId(num + 1);
        timerdbProxy.addNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0, 1);
        JSONArray array = jsonObject.getJSONArray("fixreward");
        RewardProxy rewardProxy = getGameProxy().getProxy(ActorDefine.REWARD_PROXY_NAME);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length(); i++) {
            int rewardId = array.getInt(i);
            rewardProxy.getPlayerRewardByFixReward(rewardId, reward);
            sb.append(rewardId);
            sb.append(",");
        }
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_TAST_ACTIVITY);
        sendFunctionLog(FunctionIdDefine.GET_DAY_ACTIVITY_FUNCTION_ID, playerProxy.getPowerValue(PlayerPowerDefine.POWER_active), num + 1, 0, sb.toString());
        return 0;
    }


    /*************
     * 协议
     ***********************/

    //活跃最大值
    public int getMaxActivity() {
        List<JSONObject> list = ConfigDataProxy.getConfigAllInfo(DataDefine.ACTIVEREWARD);
        return list.size();
    }

    //获得当前活跃领取id
    public int getActivityId() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        if (num >= getMaxActivity()) {
            return getMaxActivity();
        }
        return num + 1;
    }


    //获得日常任务重置次数
    public int getDayResetTimes() {
        VipProxy vipProxy = getGameProxy().getProxy(ActorDefine.VIP_PROXY_NAME);
        int maxTimes = vipProxy.getVipNum(ActorDefine.VIP_DAYQUESTRESET);
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = timerdbProxy.getTimerNum(TimerDefine.DAY_TASK_REST, 0, 0);
        return maxTimes - num;
    }

    public M19.M190000.S2C.Builder getTaskUpdate(List<PlayerTask> list, PlayerReward reward) {
        boolean falg = false;
        for (PlayerTask playerTask : list) {
            boolean checkfalg = doaddcompleteness(playerTask.taskType, playerTask.addnum, reward, playerTask.codition);
            if (checkfalg) {
                falg = checkfalg;
            }
        }
        if(falg==false){
            return null;
        }
        M19.M190000.S2C.Builder builder = M19.M190000.S2C.newBuilder();
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        for (PlayerTask playerTask : list) {
            taskInfos.addAll(getTaskInfoBytaskType(playerTask.taskType));
        }
        if (taskInfos.size() > 0) {
            builder.addAllTaskInfos(taskInfos);
            builder.setRs(0);
            TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
            return builder;
        }
        return null;
    }

    public M19.M190000.S2C.Builder getTaskUpdate(int type, int add, PlayerReward reward) {
        M19.M190000.S2C.Builder builder = M19.M190000.S2C.newBuilder();
        List<Common.TaskInfo> taskInfos = new ArrayList<Common.TaskInfo>();
        boolean falg = doaddcompleteness(type, add, reward, 0);
        if (falg == false) {
            return null;
        }
        taskInfos.addAll(getTaskInfoBytaskType(type));
        if (taskInfos.size() > 0) {
            builder.addAllTaskInfos(taskInfos);
            builder.setRs(0);
            TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
            builder.setDayliynum(timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0));
            return builder;
        }
        return null;
    }

    public M19.M190000.S2C.Builder getTaskInfoToClient() {
        M19.M190000.S2C.Builder builder = M19.M190000.S2C.newBuilder();
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        builder.addAllTaskInfos(getTaskInfos());
        int dayilNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
        int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        builder.setHasGetMaxId(num);
        builder.setDayActivityId(getActivityId());
        builder.setDayliynum(dayilNum);
        builder.setRs(0);
        return builder;
    }

    public Common.TaskInfoList getTaskInfoList(){
        Common.TaskInfoList.Builder taskInfoList=Common.TaskInfoList.newBuilder();
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        taskInfoList.addAllTaskInfos(getTaskInfos());
        int dayilNum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
        int num = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        taskInfoList.setHasGetMaxId(num);
        taskInfoList.setDayActivityId(getActivityId());
        taskInfoList.setDayliynum(dayilNum);
        taskInfoList.setRs(0);
        return  taskInfoList.build();
    }

    //获得任务提示数量
    public int getTaskTipNum() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        TimerdbProxy timerdbProxy = getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num = 0;
        for (Task task : tasks) {
            if (task.getTableType() == TaskDefine.TABLE_TASK_MAIN_LINE || task.getTableType() == TaskDefine.TABLE_TASK_DAY) {
                if (task.getIsget() == TaskDefine.TASK_STATUS_FINISH) {
                    num++;
                }
            }
        }
        int acnum = timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindById(DataDefine.ACTIVEREWARD, acnum + 1);
        if (jsonObject != null && playerProxy.getPowerValue(PlayerPowerDefine.POWER_active) >= jsonObject.getInt("activeneed")) {
            num++;
        }
        return num;
    }


}

