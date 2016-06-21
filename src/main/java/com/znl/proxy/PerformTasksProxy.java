package com.znl.proxy;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.znl.GameMainServer;
import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerTeam;
import com.znl.define.*;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.FormationMember;
import com.znl.pojo.db.PerformTasks;
import com.znl.pojo.db.TeamNotice;
import com.znl.pojo.db.Timerdb;
import com.znl.proto.Common;
import com.znl.proto.M8;
import com.znl.utils.GameUtils;
import org.apache.mina.util.ConcurrentHashSet;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/11/28.
 */
public class PerformTasksProxy extends BasicProxy {
    private Set<PerformTasks> performTasks = new ConcurrentHashSet<>();
    private Set<TeamNotice> teamNotices = new ConcurrentHashSet<>();
    private ConcurrentHashMap<Long, FormationMember> memberHashMap = new ConcurrentHashMap<>();

    @Override
    public void shutDownProxy() {
        for (PerformTasks pt : performTasks) {
            pt.finalize();
        }
        for (FormationMember formationMember : memberHashMap.values()) {
//            formationMember.save();
            formationMember.finalize();
        }
        for(TeamNotice tn : teamNotices){
            tn.finalize();
        }
    }

    @Override
    protected void init() {

    }


    public PerformTasksProxy(Set<Long> performTask, Set<Long> teamNotice,String areaKey) {
        this.areaKey = areaKey;
        for (Long id : performTask) {
            PerformTasks performTk = BaseDbPojo.get(id, PerformTasks.class,areaKey);
            performTasks.add(performTk);
            for (Long memberId : performTk.getMembersSet()) {
                FormationMember member = BaseDbPojo.get(memberId, FormationMember.class,areaKey);
                memberHashMap.put(memberId, member);
            }
        }
        for (Long id : teamNotice) {
            TeamNotice notice = BaseDbPojo.get(id, TeamNotice.class,areaKey);
            if(notice!=null) {
                teamNotices.add(notice);
            }else{
                PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
                playerProxy.removeTeamNotice(id);
            }
        }
    }


    public List<M8.TeamNoticeInfo> getAllTeamNoticeInfo() {
        checkTeamNotices();
        List<M8.TeamNoticeInfo> res = new ArrayList<>();
        for (TeamNotice teamNotice :teamNotices ) {
            res.add(getTeamNoticeInfo(teamNotice));
        }
        return res;
    }
    private List<TeamNotice>  getNoticeByType(int type){
        List<TeamNotice> list=new ArrayList<TeamNotice>();
        for (TeamNotice teamNotice : teamNotices) {
            if(teamNotice.getType()==type){
                list.add(teamNotice);
            }
        }
        return list;
    }



    public M8.TeamNoticeInfo getTeamNoticeInfo(TeamNotice teamNotice) {
        M8.TeamNoticeInfo.Builder builder = M8.TeamNoticeInfo.newBuilder();
        builder.setIconId(teamNotice.getIconId());
        builder.setLevel(teamNotice.getLevel());
        if (teamNotice.getTargetId() > 0) {
            builder.setId(teamNotice.getTargetId());
        } else {
            builder.setId(-1);
        }
        builder.setName(teamNotice.getName());
        long now = GameUtils.getServerDate().getTime();
        builder.setTime((int) ((teamNotice.getArriveTime() - now) / 1000));
        builder.setX(teamNotice.getX());
        builder.setY(teamNotice.getY());
        return builder.build();
    }

    public void addTeamNotices(TeamNotice teamNotice,  PlayerProxy playerProxy) {
        teamNotices.add(teamNotice);
        playerProxy.addTeamNotice(teamNotice.getId());
    }

    private void checkTeamNotices() {
        long now = GameUtils.getServerDate().getTime();
        List<TeamNotice> delList = new ArrayList<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (TeamNotice notice : teamNotices) {
            if (notice.getArriveTime() < now) {
                delList.add(notice);
                notice.del();
                playerProxy.removeTeamNotice(notice.getId());
            }
        }
        if (delList.size() > 0) {
            teamNotices.removeAll(delList);
        }
    }

    public void clearPerformTasks(){
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<PerformTasks> removeList = new ArrayList<>();
        for (PerformTasks task : performTasks) {
            removeList.add(task);
        }
        performTasks.clear();
        if (removeList.size() > 0) {
            for (PerformTasks task : removeList) {
                for (Long id : task.getMembersSet()) {
                    FormationMember member = memberHashMap.get(id);
                    memberHashMap.remove(id);
                    member.del();
                }
                if (task.getType() == TaskDefine.PERFORM_TASK_DIGGING){
                    sendFunctionLog(FunctionIdDefine.TASK_TEAM_REMOVE_FUNCTION_ID,task.getType(),0,0,"clearPerformTasks");
                }
                task.del();
                playerProxy.reducePerformTaskfromPlayer(task.getId());
            }
        }
    }

    public void clearTeamNotice() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        for (TeamNotice notice : teamNotices) {
            playerProxy.removeTeamNotice(notice.getId());
            notice.del();
        }
        teamNotices.clear();
    }

    public void removeNoticeByhelpId(long id, PlayerProxy playerProxy) {
        List<TeamNotice> delList = new ArrayList<>();
        for (TeamNotice notice : teamNotices) {
            if (notice.getHelpId() == id) {
                delList.add(notice);
                playerProxy.removeTeamNotice(notice.getId());
                notice.del();
            }
        }
        if (delList.size() > 0) {
            teamNotices.removeAll(delList);
        }
    }

    public void deleteTaskByhelpId(long id) {
        PerformTasks task = null;
        for (PerformTasks performTask : performTasks) {
            if (performTask.getId() == id) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
                    task = performTask;
                }
            }
        }
        if (task != null) {
            setLoad(task, 0, "deleteTaskByhelpId");
            pushPerformTaskToChangeList(task);
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        checkPerformTask(playerProxy);
    }

    public void setLoad(PerformTasks task,long load,String functionName){
        task.setLoad(load);
        pushPerformTaskToChangeList(task);
        if (load == 0){
            //记录行为日志，有队伍的载重被置0了
            sendFunctionLog(FunctionIdDefine.TASK_TEAM_LOAD_SET_ZERO_FUNCTION_ID,task.getType(),0,0,functionName);
        }
    }

    public PerformTasks getTaskByhelpId(long id) {
        for (PerformTasks performTask : performTasks) {
            if (performTask.getId() == id) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
                    return performTask;
                }
            }
        }
        return null;
    }

    public void removeTeamNoticeByXY(int x, int y, PlayerProxy playerProxy) {
        List<TeamNotice> delList = new ArrayList<>();
        for (TeamNotice notice : teamNotices) {
            if (notice.getX() == x && notice.getY() == y) {
                delList.add(notice);
                playerProxy.removeTeamNotice(notice.getId());
                notice.del();
            }
        }
        if (delList.size() > 0) {
            teamNotices.removeAll(delList);
        }
    }

    public int getTaskNumforTip(){
        int num=0;
        for(PerformTasks task : performTasks){
            if(task.getType()!= TaskDefine.PERFORM_TASK_OTHERHELPBACK){
                num++;
            }
        }
        return num;
    }

    public void removeTeamNotice(int x, int y, long time, PlayerProxy playerProxy) {
        TeamNotice delNotice = null;
        for (TeamNotice notice : teamNotices) {
            if (notice.getArriveTime() == time && notice.getX() == x && notice.getY() == y) {
                delNotice = notice;
                break;
            }
        }
        if (delNotice != null) {
            delNotice.del();
            teamNotices.remove(delNotice);
            playerProxy.removeTeamNotice(delNotice.getId());
        }
        deleteDiggingTask(x,y,time,playerProxy);
    }

    public void removeTeamNotice( long time, PlayerProxy playerProxy) {
        TeamNotice delNotice = null;
        for (TeamNotice notice : teamNotices) {
            if (notice.getArriveTime() == time) {
                delNotice = notice;
                break;
            }
        }
        if (delNotice != null) {
            delNotice.del();
            teamNotices.remove(delNotice);
            playerProxy.removeTeamNotice(delNotice.getId());
        }
        deleteDiggingTask(time,playerProxy);
    }

    private LinkedList<PerformTasks> changePerformTasks = new LinkedList<>();

    public void savePerformTasks() {
        List<PerformTasks> pftk = new ArrayList<PerformTasks>();
        synchronized (changePerformTasks) {
            while (true) {
                PerformTasks ptk = changePerformTasks.poll();
                if (ptk == null) {
                    break;
                }
                pftk.add(ptk);
            }
        }
        for (PerformTasks pt : pftk) {
            pt.save();
        }

    }

    //插入更新队列
    private void pushPerformTaskToChangeList(PerformTasks ptk) {
        synchronized (changePerformTasks) {
            if (!changePerformTasks.contains(ptk)) {
                changePerformTasks.offer(ptk);
            }
        }
    }

    /**
     * 创建执行任务
     */
    public PerformTasks createPerformTasks(int type, String name, int level, int X, int Y, long timeer, Set<Long> formatmembers, long capacity, long load, int product,PlayerProxy playerProxy ,int icon,int starX,int starY,Map<Integer, Long> map) {
        PerformTasks tasks = BaseDbPojo.create(PerformTasks.class,areaKey);
        tasks.setPlayerId(playerProxy.getPlayerId());
        tasks.setState(TaskDefine.PERFORM_TASK_STATE_TODO);
        tasks.setName(name);
        tasks.setLevel(level);
        tasks.setWorldTileX(X);
        tasks.setWorldTileY(Y);
        tasks.setType(type);
        tasks.setCapacity(capacity);
        tasks.setBeginTime(GameUtils.getServerDate().getTime());
        tasks.setTimeer(timeer);
//        tasks.setLoad(load);
        setLoad(tasks,load,"createPerformTasks");
        for (Long mbId : formatmembers) {
            tasks.addMembersId(mbId);
        }
        tasks.setStartX(starX);
        tasks.setStartY(starY);
        tasks.setProduct(product);
        tasks.setIcon(icon);
        if(map.get(PlayerPowerDefine.POWER_command)!=null) {
            tasks.setMaxSoilderNum(map.get(PlayerPowerDefine.POWER_command));
        }
        tasks.save();
        performTasks.add(tasks);
        playerProxy.addPerformTaskfromPlayer(tasks.getId());
        return tasks;
    }

    private void sendMsgToWorldNode(int x,int y,Object msg, PlayerProxy playerProxy){
        String path = ActorDefine.AREA_SERVER_PRE_PATH+ GameMainServer.getLogicAreaIdByAreaId(playerProxy.getAreaId())+"/"+ActorDefine.WORLD_SERVICE_NAME+ "/" +x+"_"+y;
        ActorSelection worldNode = GameMainServer.system().actorSelection(path);
        worldNode.tell(msg, ActorRef.noSender());
    }

    private void checkPerformTask( PlayerProxy playerProxy) {
        long now = GameUtils.getServerDate().getTime();
        List<PerformTasks> removeList = new ArrayList<>();
        for (PerformTasks task : performTasks) {
            if (task.getType() != TaskDefine.PERFORM_TASK_GOHELP && task.getType() != TaskDefine.PERFORM_TASK_DIGGING && task.getType() != TaskDefine.PERFORM_TASK_HELPBACK && task.getType() != TaskDefine.PERFORM_TASK_OTHERHELPBACK || task.getTimeer()==0) {
                if (now >= task.getTimeer() && task.getType() != TaskDefine.PERFORM_TASK_DIGGING) {
                    removeList.add(task);
                }
            } else {
//                int total = (int) ((now / 1000 - task.getBeginTime() /1000)*task.getProduct());
                if (task.getLoad() == 0) {
                    removeList.add(task);
                    if(task.getType() == TaskDefine.PERFORM_TASK_DIGGING){
                        //删除后向世界请求一次校验，如果是误删除的还要给他加回来
                        sendMsgToWorldNode(task.getWorldTileX(),task.getWorldTileY(),new GameMsg.CheckDeleteDiggingTask(playerProxy.getPlayerId()),playerProxy);
                    }
                }
            }
        }
        if (removeList.size() > 0) {
            performTasks.removeAll(removeList);
            changePerformTasks.removeAll(removeList);
            for (PerformTasks task : removeList) {
                for (Long id : task.getMembersSet()) {
                    FormationMember member = memberHashMap.get(id);
                    memberHashMap.remove(id);
                    member.del();
                }
                if (task.getType() == TaskDefine.PERFORM_TASK_DIGGING){
                    sendFunctionLog(FunctionIdDefine.TASK_TEAM_REMOVE_FUNCTION_ID,task.getType(),0,0,"checkPerformTask");
                }
                task.del();
                playerProxy.reducePerformTaskfromPlayer(task.getId());
            }
        }
    }

    public boolean checkTaskSize() {
        TimerdbProxy timerdbProxy = getGameProxy().getProxy(ActorDefine.TIMERDB_PROXY_NAME);
        int num=0;
        for(PerformTasks tasks :performTasks){
            if(tasks.getType()!=TaskDefine.PERFORM_TASK_OTHERHELPBACK){
                num++;
            }
        }
        if (timerdbProxy.getPerformTaskLesNum() <= num) {
            return false;
        }
        return true;
    }

    public List<M8.TaskTeamInfo> getAllTaskTeamInfoList() {
        List<M8.TaskTeamInfo> list = new ArrayList<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        checkPerformTask(playerProxy);
        for (PerformTasks task : this.performTasks) {
            list.add(getTaskTeamInfo(task));
        }
        return list;
    }

    public M8.TaskTeamInfo getTaskTeamInfo(PerformTasks task) {
        M8.TaskTeamInfo.Builder builder = M8.TaskTeamInfo.newBuilder();
        long now = GameUtils.getServerDate().getTime();
        builder.setCapacity(task.getCapacity());
        builder.setLevel(task.getLevel());
        builder.setLoad(task.getLoad());
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Integer> openPost = playerProxy.getPlayerFightPost();
        builder.setMaxSoldierNum((int) playerProxy.getPowerValue(PlayerPowerDefine.POWER_command) * openPost.size());
        if(task.getType()==TaskDefine.PERFORM_TASK_OTHERHELPBACK){
            builder.setMaxSoldierNum((int) task.getMaxSoilderNum());
        }
        builder.setName(task.getName());
        int totalSoldierNum = 0;
        for (Long id : task.getMembersSet()) {
            FormationMember member = memberHashMap.get(id);
            Common.FightElementInfo.Builder fightInfo = Common.FightElementInfo.newBuilder();
            fightInfo.setNum(member.getNum());
            fightInfo.setPost(member.getPost());
            if (member.getNum() == 0) {
                fightInfo.setTypeid(0);
            } else {
                fightInfo.setTypeid(member.getTypeId());
            }
            builder.addFightInfos(fightInfo.build());
            totalSoldierNum += member.getNum();
        }
        builder.setSoldierNum(totalSoldierNum);
        if (task.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
            builder.setAlreadyTime(((now / 1000 - task.getBeginTime() / 1000) * task.getProduct()));
            builder.setTotalTime(task.getTimeer());
            if (builder.getAlreadyTime() > builder.getTotalTime()) {
                builder.setAlreadyTime(task.getTimeer());
            }
            builder.setProduct(task.getProduct());
        } else {
            builder.setAlreadyTime((int) (now / 1000 - task.getBeginTime() / 1000));
            if((now / 1000 - task.getBeginTime()/1000)<0){
                builder.setAlreadyTime(0);
            }
            builder.setTotalTime((int) (task.getTimeer() / 1000 - task.getBeginTime() / 1000));
        }
        builder.setType(task.getType());
        builder.setX(task.getWorldTileX());
        builder.setY(task.getWorldTileY());
        builder.setId(task.getId());
        builder.setIcon(task.getIcon());
        if (playerProxy.getPlayer().getUsedefine() == task.getId()) {
            builder.setState(1);
        } else {
            builder.setState(2);
        }
        builder.setStartx(task.getStartX());
        builder.setStarty(task.getStartY());
        return builder.build();
    }

    public void setBeginTime(long id, long beginTime) {
        PerformTasks tasks = getPerformTaskById(id);
        tasks.setBeginTime(beginTime);
        pushPerformTaskToChangeList(tasks);
    }

    public void changeTaskType(int x,int y, long time,int type){
     for(PerformTasks tasks: performTasks){
         if(tasks.getWorldTileX() == x && tasks.getWorldTileY()== y && time== tasks.getTimeer()){
             tasks.setType(type);
             tasks.save();
         }
     }
    }

    public void chanbeginttime(long time){
        for(PerformTasks tasks: performTasks){
            if(time== tasks.getTimeer()){
                tasks.setBeginTime(time);
                tasks.save();
            }
        }
    }


    public long addPerformTaskForOffLine(int type, String name, int level, int X, int Y,
                                         long timeer, List<PlayerTeam> teams, DungeoProxy dungeoProxy,
                                         int product, long playerId,PlayerProxy playerProxy,int icon,
                                         int starX,int starY,Map<Integer, Long> map){
        Set<Long> formatmembers = new HashSet<>();
        for (PlayerTeam team : teams) {
            int post = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
            int typeId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            FormationMember member = BaseDbPojo.create(FormationMember.class,areaKey);
            member.setNum(num);
            member.setBaseNum(num);
            member.setTypeId(typeId);
            member.setPost(post);
            member.setPlayerId(playerId);
            member.save();
            formatmembers.add(member.getId());
            memberHashMap.put(member.getId(), member);
        }
        long capacity = dungeoProxy.countSoldierCapacity(teams);
        long load = 0;
        for (PlayerTeam team : teams) {
            int teamLoad = (int) team.getValue(SoldierDefine.POWER_load);
            int loadRate = (int) team.getValue(SoldierDefine.POWER_loadRate);
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            load += teamLoad * (1 + loadRate / 10000.0) * num;
        }
        long addLoad=0l;
        if(map.get(PlayerPowerDefine.NOR_POWER_loadRate)!=null){
            addLoad=   map.get(PlayerPowerDefine.NOR_POWER_loadRate);
        }
        load= (long) (load*(1+(addLoad/10000.0)));
        PerformTasks tasks = createPerformTasks(type, name, level, X, Y, timeer, formatmembers, capacity, load, product,playerProxy,icon,starX,starY,map);
        if (getGameProxy() != null) {
            checkPerformTask(playerProxy);
        }
        return tasks.getId();
    }

    /**
     * 创建执行任务
     */
    public long addPerformTask(int type, String name, int level, int X, int Y,
                               long timeer, List<PlayerTeam> teams, DungeoProxy dungeoProxy,
                               int product, long playerId,PlayerProxy playerProxy,int icon,
                               int starX,int starY) {

        Set<Long> formatmembers = new HashSet<>();
        for (PlayerTeam team : teams) {
            int post = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
            int typeId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            FormationMember member = BaseDbPojo.create(FormationMember.class,areaKey);
            member.setNum(num);
            member.setBaseNum(num);
            member.setTypeId(typeId);
            member.setPost(post);
            member.setPlayerId(playerId);
            member.save();
            formatmembers.add(member.getId());
            memberHashMap.put(member.getId(), member);
        }
        long capacity = dungeoProxy.countSoldierCapacity(teams);
        long load = dungeoProxy.getTeamLoad(teams);
        PerformTasks tasks = createPerformTasks(type, name, level, X, Y, timeer, formatmembers, capacity, load, product,playerProxy,icon,starX,starY,new HashMap<Integer,Long>());
        if (getGameProxy() != null) {
            checkPerformTask(playerProxy);
        }
        return tasks.getId();
    }


    private PerformTasks getPerformTaskById(long id) {
        for (PerformTasks performTask : performTasks) {
            if (performTask.getId() == id) {
                return performTask;
            }
        }
        return null;
    }


    /**
     * 召回在挖矿的队伍
     */
    public int callBackDiggingTeam(PerformTasks tasks, List<Long> point) {
        point.add((long) tasks.getWorldTileX());
        point.add((long) tasks.getWorldTileY());
        point.add(tasks.getTimeer());
        point.add((long) tasks.getType());
        point.add((long)tasks.getProduct());
//        tasks.setLoad(0); //先不删除，等node那边返回删除指令
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        checkPerformTask(playerProxy);
        return 0;
    }

//    /**
//     * 获得阵型里面的佣兵数量
//     */
//    public int getSoldierNumFromPerFormTask(int typeId){
//        int num = 0;
//        for(PerformTasks tasks : performTasks){
//            for (Long id : tasks.getMembersSet()){
//                FormationMember member = memberHashMap.get(id);
//                if (member.getTypeId() == typeId){
//                    num += member.getNum();
//                }
//            }
//        }
//        return num;
//    }

    /**
     * 快速完成任务队伍
     */
    public int buyQuickFinishPerformTask(Long id, List<Long> point) {
        PerformTasks tasks = getPerformTaskById(id);
        if (tasks == null) {
            return ErrorCodeDefine.M80004_1;
        }
        if (tasks.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
            return callBackDiggingTeam(tasks, point);
        }
        if(tasks.getType()==TaskDefine.PERFORM_TASK_OTHERHELPBACK){
            if(tasks.getTimeer()!=tasks.getBeginTime()){
                return ErrorCodeDefine.M80004_3;
            }
        }
        ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
        long now = GameUtils.getServerDate().getTime();
        int second = (int) ((tasks.getTimeer() - now) / 1000);
        int gold = resFunBuildProxy.speedCost(second);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < gold &&tasks.getType() != TaskDefine.PERFORM_TASK_HELPBACK&&tasks.getType() != TaskDefine.PERFORM_TASK_OTHERHELPBACK) {
            return ErrorCodeDefine.M80004_2;
        }
        if(tasks.getType() != TaskDefine.PERFORM_TASK_HELPBACK&&tasks.getType() != TaskDefine.PERFORM_TASK_OTHERHELPBACK) {
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, gold, LogDefine.LOST_TASK_BUY_QUICK);
        }
        if(tasks.getType()==TaskDefine.PERFORM_TASK_OTHERHELPBACK){
            setLoad(tasks,0,"buyQuickFinishPerformTask");
//            tasks.setLoad(0);
        }
        //直接把玩家的队伍删除了
        if (tasks.getType() == TaskDefine.PERFORM_TASK_ATTACK || tasks.getType() == TaskDefine.PERFORM_TASK_GOHELP || tasks.getType() == TaskDefine.PERFORM_TASK_HELPBACK ) {
            point.add((long) tasks.getWorldTileX());
            point.add((long) tasks.getWorldTileY());
        } else if (tasks.getType() == TaskDefine.PERFORM_TASK_RETURN || tasks.getType() == TaskDefine.PERFORM_TASK_OTHERHELPBACK) {
            BuildingProxy buildingProxy = getProxy(ActorDefine.BUILDING_PROXY_NAME);
            Tuple2<Integer, Integer> p = buildingProxy.getWorldTilePoint();
            point.add((long) p._1());
            point.add((long) p._2());
        }
        point.add(tasks.getTimeer());
        point.add((long) tasks.getType());
        if(tasks.getType() != TaskDefine.PERFORM_TASK_GOHELP && tasks.getType() != TaskDefine.PERFORM_TASK_HELPBACK && tasks.getType() != TaskDefine.PERFORM_TASK_OTHERHELPBACK ) {
            tasks.setTimeer(0);
        }
        point.add((long)tasks.getProduct());
        checkPerformTask(playerProxy);
        return 0;
    }

    public void deleteDiggingTask(int x, int y,PlayerProxy playerProxy) {
        PerformTasks task = null;
        for (PerformTasks performTask : performTasks) {
            if (performTask.getWorldTileX() == x && performTask.getWorldTileY() == y) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
                    task = performTask;
                }
            }
        }
        if (task != null) {
            setLoad(task,0,"deleteDiggingTask3");
//            task.setLoad(0);
//            pushPerformTaskToChangeList(task);
        }
        checkPerformTask(playerProxy);
    }

    public void deleteDiggingTask(int x, int y,long time,PlayerProxy playerProxy) {
        PerformTasks task = null;
        for (PerformTasks performTask : performTasks) {
            if (performTask.getWorldTileX() == x && performTask.getWorldTileY() == y&& performTask.getTimeer()==time) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_GOHELP || performTask.getType() == TaskDefine.PERFORM_TASK_HELPBACK  || performTask.getType() == TaskDefine.PERFORM_TASK_OTHERHELPBACK ) {
                    task = performTask;
                }
            }
        }
        if (task != null) {
            setLoad(task,0,"deleteDiggingTask4");
//            task.setLoad(0);
            pushPerformTaskToChangeList(task);
        }
        checkPerformTask(playerProxy);
    }

    public void deleteDiggingTask(long time,PlayerProxy playerProxy) {
        PerformTasks task = null;
        for (PerformTasks performTask : performTasks) {
            if (performTask.getTimeer()==time) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_GOHELP || performTask.getType() == TaskDefine.PERFORM_TASK_HELPBACK  || performTask.getType() == TaskDefine.PERFORM_TASK_OTHERHELPBACK ) {
                    task = performTask;
                }
            }
        }
        if (task != null) {
            setLoad(task,0,"deleteDiggingTask2");
//            task.setLoad(0);
            pushPerformTaskToChangeList(task);
        }
        checkPerformTask(playerProxy);
    }


    public void deleteFormTask(long time,PlayerProxy playerProxy) {
        PerformTasks task = null;
        for (PerformTasks performTask : performTasks) {
            if (performTask.getTimeer()==time) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_GOHELP || performTask.getType() == TaskDefine.PERFORM_TASK_HELPBACK  || performTask.getType() == TaskDefine.PERFORM_TASK_OTHERHELPBACK ) {
                    task = performTask;
                }
            }
        }
        if (task != null) {
            setLoad(task,0,"deleteFormTask");
//            task.setLoad(0);
            if (task.getType() == TaskDefine.PERFORM_TASK_DIGGING){
                sendFunctionLog(FunctionIdDefine.TASK_TEAM_REMOVE_FUNCTION_ID,task.getType(),0,0,"deleteFormTask");
            }
            task.del();
            pushPerformTaskToChangeList(task);
            performTasks.remove(task);
            changePerformTasks.remove(task);
            playerProxy.reducePerformTaskfromPlayer(task.getId());
        }
        checkPerformTask(playerProxy);
    }

    public List<String> getDigList() {
        List<String> list = new ArrayList<>();
        for (PerformTasks performTask : performTasks) {
            if (performTask.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
                String str = performTask.getWorldTileX() + "," + performTask.getWorldTileY();
                list.add(str);
            }
        }
        return list;
    }

    public void updateDiggingTask(List<PlayerTeam> teams, int x, int y, long load) {
        PerformTasks task = null;
        for (PerformTasks performTask : performTasks) {
            if (performTask.getWorldTileX() == x && performTask.getWorldTileY() == y) {
                if (performTask.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
                    task = performTask;
                }
            }
        }
        if (task != null) {
            setLoad(task,load,"updateDiggingTask");
//            task.setLoad(load);
            //刷新防守成员
            for (PlayerTeam team : teams) {
                int post = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
                int typeId = (int) team.getValue(SoldierDefine.NOR_POWER_TYPE_ID);
                int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
                for (Long id : task.getMembersSet()) {
                    FormationMember member = memberHashMap.get(id);
                    if (member.getPost() == post) {
                        member.setNum(num);
                        member.setTypeId(typeId);
                        member.save();
                    }
                }
            }
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        checkPerformTask(playerProxy);
    }

    public int changerHelp(long id){
        PerformTasks performTasks=getPerformTaskById(id);
        if(performTasks==null){
            return  ErrorCodeDefine.M80014_1;
        }
        if(performTasks.getType()!=TaskDefine.PERFORM_TASK_OTHERHELPBACK){
            return  ErrorCodeDefine.M80014_2;
        }
        if(performTasks.getTimeer() != performTasks.getBeginTime()){
            return  ErrorCodeDefine.M80014_3;
        }
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if(playerProxy.getPlayer().getUsedefine()==id){
            playerProxy.getPlayer().setUsedefine(0l);
        }else {
            playerProxy.getPlayer().setUsedefine(performTasks.getId());
        }
        playerProxy.getPlayer().save();
        return 0;
    }


    public int getTaskNum() {
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        checkPerformTask(playerProxy);
        return performTasks.size();
    }

    private PerformTasks getTaskByTime(long time){
        for(PerformTasks tasks : performTasks){
            if(tasks.getTimeer()==time){
                return tasks;
            }
        }
        return null;
    }

    /****检查是否需要扣除防守阵型（减佣兵的时候调用）****/
    public void checkDefendTroop(DungeoProxy dungeoProxy,  List<PlayerTeam> teams, long time) {
        PerformTasks task = getTaskByTime(time);
        if (task == null) {
            return;
        }
        for (PlayerTeam team : teams) {
            //没有开启自动补充的话就直接扣除就好啦
            int nowNum = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            int index = (int) team.getValue(SoldierDefine.NOR_POWER_INDEX);
            for (Long id : task.getMembersSet()) {
                FormationMember member =memberHashMap.get(id);
                if (index - 20 == member.getPost() || index - 20 == member.getPost()-10 ) {
                    member.setNum(nowNum);
                    member.setBaseNum(nowNum);
                    if (nowNum <= 0) {
                        member.setNum(0);
                        member.setBaseNum(0);
                        member.setTypeId(0);
                    }
                }
                member.save();
            }
        }
        long capiry=dungeoProxy.countSoldierCapacity(teams);
        task.setCapacity(capiry);
        task.save();
    }
    public int getguardNum(){
        int num=0;
        for(PerformTasks tasks: performTasks){
            if(tasks.getType()==TaskDefine.PERFORM_TASK_OTHERHELPBACK){
                num++;
            }
        }
        return num;
    }
}
