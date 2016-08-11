package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.PlayerTeam;
import com.znl.core.SimplePlayer;
import com.znl.core.WorldNodeData;
import com.znl.define.*;
import com.znl.pojo.db.WorldBuilding;
import com.znl.pojo.db.WorldTeamData;
import com.znl.proto.Common;
import com.znl.proto.M8;
import com.znl.service.PlayerService;
import com.znl.service.WorldService;
import com.znl.service.map.TileType;
import com.znl.service.map.WorldTile;
import com.znl.utils.GameUtils;
import com.znl.utils.RandomUtil;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONObject;
import scala.Tuple2;

import java.util.*;

/**
 * Created by Administrator on 2015/11/12.
 */
public class BuildingProxy extends BasicProxy {

    private WorldBuilding worldBuilding;

    @Override
    public void shutDownProxy() {
        //数据都缓存在worldService了，不做释放
    }

    @Override
    protected void init() {
        //2016/4/5增加容错，判断玩家的坐标是否正确
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (worldBuilding != null) {
            if (playerProxy.getPlayer().getWorldTileX() != worldBuilding.getWorldTileX() || playerProxy.getPlayer().getWorldTileY() != worldBuilding.getWorldTileY()) {
                playerProxy.getPlayer().setWorldTileX(worldBuilding.getWorldTileX());
                playerProxy.getPlayer().setWorldTileY(worldBuilding.getWorldTileY());
            }
        }
    }

    public BuildingProxy(Long worldBuildingId, String areaKey) {
        this.areaKey = areaKey;
        createWorldBuilding(worldBuildingId);
    }

    public Tuple2<Integer, Integer> updateBuildingId(Long worldBuildingId) {
        Tuple2<Integer, Integer> res = createWorldBuilding(worldBuildingId);
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        playerProxy.setWorldTilePoint(res._1(), res._2());
        return res;
    }

    private Tuple2<Integer, Integer> createWorldBuilding(Long id) {
        if (id >= 0) {
            worldBuilding = BaseDbPojo.get(id, WorldBuilding.class, areaKey);
            return getWorldTilePoint();
        }
        return new Tuple2<>(-1, -1);
    }

    public void setWorldBuildingPoint(int x, int y) {
        worldBuilding.setWorldTileX(x);
        worldBuilding.setWorldTileY(y);
        worldBuilding.save();
    }

    public M8.WorldTileInfo getWorldTileInfo(WorldTile tile) {

        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);

        M8.WorldTileInfo.Builder builder = M8.WorldTileInfo.newBuilder().setX(tile.x())
                .setY(tile.y())
                .setTileType(tile.tileType().id())
                .setLegionName(tile.legionName());

        if (tile.tileType() == TileType.Building()) {
            builder.setBuildingInfo(M8.WorldBuildingInfo.newBuilder().setX(tile.x())
                    .setY(tile.y())
                    .setLevel(tile.playerLevel())
                    .setName(tile.playerName())
                    .setProtect(tile.protect() ? 1 : 0)
                    .setBuildIcon(tile.cityicon())
                    .setPendant(tile.pendant())
                    .setDegree(tile.degree())
                    .setDegreemax(tile.degreemax())
                    .setIcon(tile.icon())
                    .setPlayerId(tile.building().getPlayerId()).build());

        } else if (tile.tileType() == TileType.Resource()) {
            builder.setResId(tile.resId())
                    .setResType(tile.resType())
                    .setResLv(tile.resLv())
                    .setResPointId(tile.resPointId());
        }
        return builder.build();
    }

    public Tuple2<Integer, Integer> getWorldTilePoint() {
        Tuple2<Integer, Integer> point = null;
        if (worldBuilding != null) {
            point = new Tuple2<>(worldBuilding.getWorldTileX(), worldBuilding.getWorldTileY());
//            System.out.println("成功拿到坐标！！！！！！！！！！");
        } else {
            point = new Tuple2<>(-1, -1);
//            System.out.println("拿坐标失败！！！！！！！！！！");
        }

        return point;
    }

    public HashMap<Integer, Integer> getWorldAddPower() {
        HashMap<Integer, Integer> res = new HashMap<>();
        PlayerProxy playerProxy = getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);

        return res;
    }

    //获得放大镜信息
    public int getMagifyInfo(List<WorldTile> list, int x, int y, M8.M80015.S2C.Builder builder) {
        //获得所有玩家
        List<WorldTile> peoplelist = new ArrayList<WorldTile>();
        List<WorldTile> resouce1list = new ArrayList<WorldTile>();
        List<WorldTile> resouce2list = new ArrayList<WorldTile>();
        List<WorldTile> resouce3list = new ArrayList<WorldTile>();
        List<WorldTile> resouce4list = new ArrayList<WorldTile>();
        List<WorldTile> resouce5list = new ArrayList<WorldTile>();
        for (WorldTile title : list) {
            if (title.building() != null && title.building().getPlayerId() > 0 && !"".equals(title.playerName())) {
                peoplelist.add(title);
            } else {
                if (title.resType() == 1) {
                    resouce1list.add(title);
                }
                if (title.resType() == 2) {
                    resouce2list.add(title);
                }
                if (title.resType() == 3) {
                    resouce3list.add(title);
                }
                if (title.resType() == 4) {
                    resouce4list.add(title);
                }
                if (title.resType() == 5) {
                    resouce5list.add(title);
                }
            }
        }
     /*   List<WorldTile> getlist = new ArrayList<WorldTile>();
        //随机玩家
        while (true) {
            if (getlist.size() >= peoplelist.size() || getlist.size() >= ChatAndMailDefine.PEOPLE_RANDOM) {
                break;
            }
            int random = RandomUtil.random(peoplelist.size() - 1);
            WorldTile tile = peoplelist.get(random);
            if (!getlist.contains(tile)) {
                getlist.add(tile);
            }

        }*/
        for (WorldTile tile : peoplelist) {
            builder.addWorldTileInfos(getWorldTileInfo(tile));
        }
        //获得最近的资源点
        WorldTile resoutile1 = getLatelyTitle(resouce1list, x, y);
        WorldTile resoutile2 = getLatelyTitle(resouce2list, x, y);
        WorldTile resoutile3 = getLatelyTitle(resouce3list, x, y);
        WorldTile resoutile4 = getLatelyTitle(resouce4list, x, y);
        WorldTile resoutile5 = getLatelyTitle(resouce5list, x, y);
        if (resoutile1 != null) {
            builder.addWorldTileInfos(getWorldTileInfo(resoutile1));
        }
        if (resoutile2 != null) {
            builder.addWorldTileInfos(getWorldTileInfo(resoutile2));
        }
        if (resoutile3 != null) {
            builder.addWorldTileInfos(getWorldTileInfo(resoutile3));
        }
        if (resoutile4 != null) {
            builder.addWorldTileInfos(getWorldTileInfo(resoutile4));
        }
        if (resoutile5 != null) {
            builder.addWorldTileInfos(getWorldTileInfo(resoutile5));
        }
        return 0;
    }

    public WorldTile getLatelyTitle(List<WorldTile> list, int x, int y) {
        WorldTile tile = null;
        for (WorldTile wt : list) {
            if (tile == null) {
                tile = wt;
            } else {
                if (getPoinLdistace(wt.x(), wt.y(), x, y) < getPoinLdistace(tile.x(), tile.y(), x, y)) {
                    tile = wt;
                }
            }
        }
        return tile;
    }

    public int getPoinLdistace(int x1, int y1, int x2, int y2) {
        return (int) (Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }

    public int fightWorld(int x, int y, List<Common.FightElementInfo> list) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Tuple2<Integer, Integer> point = getWorldTilePoint();
        BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
        int rs = battleProxy.checkFightMember(list);
        if (rs >= 0) {
            if (point._1() == x && point._2() == y) {
                return ErrorCodeDefine.M80001_10;
            }
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy) <= 0) {
                return ErrorCodeDefine.M80001_13;
            }

            WorldNodeData targetNode = WorldService.getWorldNode(playerProxy.getAreaKey(), x, y);
            if (targetNode == null) {
                return ErrorCodeDefine.M80001_13;
            }

            VipProxy vipProxy = getProxy(ActorDefine.VIP_PROXY_NAME);
            List<Long> teams = WorldService.getPlayerTeamIds(playerProxy.getPlayerId());
            if (teams != null && teams.size() >= vipProxy.getMaxTaskTeamSize()) {
                return ErrorCodeDefine.M80001_9;
            }

            if (targetNode.getOccupyPlayerId() > 0) {
                SimplePlayer targetSimplePlayer = PlayerService.getSimplePlayer(targetNode.getOccupyPlayerId(), playerProxy.getAreaKey());
                if (targetSimplePlayer.getArmygrouid() > 0 && targetSimplePlayer.getArmygrouid() == playerProxy.getArmGrouId()) {
                    return ErrorCodeDefine.M80001_14;
                }
            }

            WorldTile targetTile = WorldService.getWorldTitleByPoint(x, y, areaKey);
            if (targetTile.tileType() == TileType.Empty()) {
                return ErrorCodeDefine.M80001_7;
            }
            if (targetTile.tileType() == TileType.Building()) {
                SimplePlayer targetSimplePlayer = PlayerService.getSimplePlayer(targetNode.getOccupyPlayerId(), playerProxy.getAreaKey());
//                if (targetSimplePlayer == null){
//                    System.out.println("!!!");
//                }
                if (targetSimplePlayer.getProtectOverDate() > GameUtils.getServerDate().getTime()) {
                    return ErrorCodeDefine.M80001_11;
                }
            }
        } else {
            return rs;
        }
        return 0;
    }

    //判断被攻打的是资源点还是玩家
    public boolean checkPiontType(int x ,int y){
        boolean falg=false;
        WorldTile targetTile = WorldService.getWorldTitleByPoint(x, y, areaKey);
        if (targetTile.tileType() == TileType.Resource()){
            falg=true;
        }
        return falg;
    }

    public WorldTeamData createTeamData(int x, int y, List<PlayerTeam> teams, int teamType, HashMap<Integer, Integer> addMap) {
        //创建队伍
        DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        WorldTeamData teamData = BaseDbPojo.create(WorldTeamData.class, areaKey);
        teamData.setPlayerId(playerProxy.getPlayerId());
        Tuple2<Integer, Integer> point = getWorldTilePoint();
        int myX = point._1();
        int myY = point._2();
        teamData.setStartX(myX);
        teamData.setStartY(myY);
        teamData.setTargetX(x);
        teamData.setTargetY(y);
        teamData.setType(teamType);
        WorldTile targetTile = WorldService.getWorldTitleByPoint(x, y, areaKey);
        if (targetTile.tileType() == TileType.Building()) {
            SimplePlayer targetSimplePlayer = PlayerService.getSimplePlayer(targetTile.building().getPlayerId(), playerProxy.getAreaKey());
            teamData.setLevel(targetSimplePlayer.getLevel());
            teamData.setName(targetSimplePlayer.getName());
        } else if (targetTile.tileType() == TileType.Resource()) {
            int pointId = targetTile.resPointId();
            JSONObject pointDefine = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, pointId);
            teamData.setLevel(pointDefine.getInt("level"));
            teamData.setName(pointDefine.getString("name"));
        }
        int posSize = playerProxy.getPlayerFightPost().size();
        teamData.setMaxSoldierNum((int) (playerProxy.getPowerValue(PlayerPowerDefine.POWER_command) * posSize));
        teamData.setEndTime(WorldService.getTheWayTime(myX, myY, x, y, (int) playerProxy.getPowerValue(PlayerPowerDefine.NOR_POWER_speedRate)));
        teamData.setStartTime(GameUtils.getServerTime());
        teamData.setBasePowerMap(GameUtils.encodePlayerTeam(teams, 0));
        teamData.setPowerMap(GameUtils.encodePlayerTeam(teams, 1));
        teamData.setAddMap(GameUtils.encodeIntegerMapToString(addMap));
        teamData.setIconId(playerProxy.getIconId());
        teamData.setCapacity(dungeoProxy.countSoldierCapacity(teams));
        teamData.save();
        return teamData;
    }

    public M8.TaskTeamInfo getTaskTeamInfo(WorldTeamData teamData) {
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        M8.TaskTeamInfo.Builder builder = M8.TaskTeamInfo.newBuilder();
        builder.setId(teamData.getId());
        List<PlayerTeam> teams = GameUtils.decodePlayerTeam(teamData.getBasePowerMap(), teamData.getPowerMap(), teamData.getPlayerId());
        // DungeoProxy dungeoProxy = getProxy(ActorDefine.DUNGEO_PROXY_NAME);
        // int capacity = dungeoProxy.countSoldierCapacity(teams);
        builder.setCapacity(teamData.getCapacity());
        builder.setX(teamData.getTargetX());
        builder.setY(teamData.getTargetY());
        builder.setLevel(teamData.getLevel());
        builder.setName(teamData.getName());
        int soldierNum = 0;
        long totalLoad = 0l;
        for (PlayerTeam team : teams) {
            int num = (int) team.getValue(SoldierDefine.NOR_POWER_NUM);
            soldierNum += num;
            long load = (int) team.getValue(SoldierDefine.POWER_load) * num;
            totalLoad += load;
        }
        builder.setSoldierNum(soldierNum);
        builder.setLoad(totalLoad);
        builder.setType(teamData.getType());
        int now = GameUtils.getServerTime();
        int alreadyTime=now - teamData.getStartTime();
        if(alreadyTime<0){
            alreadyTime=0;
            System.err.println("马要跑出去了!!!!!!时间为负数");
        }
        builder.setAlreadyTime(alreadyTime);
        builder.setTotalTime(teamData.getEndTime() - now);
        if (teamData.getType() == TaskDefine.PERFORM_TASK_DIGGING) {
            long alreadyGet = teamData.getProduct() * (alreadyTime);
            if(alreadyGet > totalLoad){
                alreadyGet = totalLoad;
            }
            builder.setAlreadyTime(alreadyGet);
            builder.setTotalTime(totalLoad);
            builder.setProduct(teamData.getProduct());
        } else if (teamData.getType() == TaskDefine.PERFORM_TASK_HELPBACK || teamData.getType() == TaskDefine.PERFORM_TASK_GOHELP) {
            //驻军中
            if (teamData.getPlayerId() != playerProxy.getPlayerId()) {
                //别人驻军自己的
                builder.setType(TaskDefine.PERFORM_TASK_OTHERHELPBACK);
                builder.setState(2);
                builder.setIcon(teamData.getIconId());
                if (playerProxy.getPlayer().getUsedefine() == teamData.getId()) {
                    builder.setState(1);//设置防守
                }
            }
        }
        FormationProxy formationProxy = getProxy(ActorDefine.FORMATION_PROXY_NAME);
        builder.addAllFightInfos(formationProxy.createFightElementInfoList(teams));
        builder.setStartx(teamData.getStartX());
        builder.setStarty(teamData.getStartY());
        builder.setMaxSoldierNum(teamData.getMaxSoldierNum());

        return builder.build();
    }

    //被攻击的时候的警示
    public M8.TeamNoticeInfo getBeAttackTeamNoticeInfo(WorldTeamData teamData) {
        M8.TeamNoticeInfo.Builder builder = M8.TeamNoticeInfo.newBuilder();
        SimplePlayer attackSimplePlayer = PlayerService.getSimplePlayer(teamData.getPlayerId(), areaKey);
        builder.setIconId(attackSimplePlayer.getIconId());
        builder.setName(attackSimplePlayer.getName());
        builder.setX(teamData.getTargetX());
        builder.setY(teamData.getTargetY());
        Tuple2<Integer, Integer> point = getWorldTilePoint();
        int myX = point._1();
        int myY = point._2();
        if (myX == teamData.getTargetX() && myY == teamData.getTargetY()) {
            //攻击的是我的主城
            builder.setId(-1);
        } else {
            WorldTile tile = WorldService.getWorldTitleByPoint(teamData.getTargetX(), teamData.getTargetY(), areaKey);
            builder.setId(tile.resPointId());
        }
        builder.setTime(teamData.getEndTime() - GameUtils.getServerTime());
        builder.setKey(teamData.getId());
        builder.setLevel(teamData.getLevel());
        return builder.build();
    }

    public int helpDefendBuilding(int x, int y, List<Common.FightElementInfo> list) {
        BattleProxy battleProxy = getProxy(ActorDefine.BATTLE_PROXY_NAME);
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        int rs = 0;
        if (list != null) {
            rs = battleProxy.checkFightMember(list);
        }
        if (rs < 0) {
            return rs;
        }
        if (playerProxy.getArmGrouId() <= 0) {
            return ErrorCodeDefine.M80013_1;
        }

        Tuple2<Integer, Integer> tuple = getWorldTilePoint();
        if (tuple._1() == x && tuple._2() == y) {
            return ErrorCodeDefine.M80013_2;
        }
        if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_energy) <= 0) {
            return ErrorCodeDefine.M80013_5;
        }
        VipProxy vipProxy = getProxy(ActorDefine.VIP_PROXY_NAME);
        List<Long> teams = WorldService.getPlayerTeamIds(playerProxy.getPlayerId());
        if (teams != null && teams.size() >= vipProxy.getMaxTaskTeamSize()) {
            return ErrorCodeDefine.M80013_7;
        }
        WorldNodeData nodeData = WorldService.getWorldNode(playerProxy.getAreaKey(), x, y);
        WorldTile tile = WorldService.getWorldTitleByPoint(x, y, playerProxy.getAreaKey());
        if (tile == null || tile.tileType() != TileType.Building()) {
            return ErrorCodeDefine.M80013_3;
        }
        SimplePlayer targetPlayer = PlayerService.getSimplePlayer(nodeData.getOccupyPlayerId(), playerProxy.getAreaKey());
        if (targetPlayer.getArmygrouid() != playerProxy.getArmGrouId()) {
            return ErrorCodeDefine.M80013_4;
        }
        if (targetPlayer.getGardNum() >= 5) {
            return ErrorCodeDefine.M80013_6;
        }
        return 0;
    }

    public int buyQuickFinishTaskTeam(WorldTeamData teamData) {
        if (teamData == null) {
            return ErrorCodeDefine.M80004_1;
        }
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        if (teamData.getType() == TaskDefine.PERFORM_TASK_GOHELP && teamData.getPlayerId() != playerProxy.getPlayerId()) {
            if (teamData.getEndTime() > GameUtils.getServerTime()) {
                return ErrorCodeDefine.M80004_3;
            }
            return 0;
        }
        if (teamData.getType() != TaskDefine.PERFORM_TASK_DIGGING && teamData.getType() != TaskDefine.PERFORM_TASK_HELPBACK) {
            ResFunBuildProxy resFunBuildProxy = getGameProxy().getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME);
            long now = GameUtils.getServerTime();
            int second = (int) (teamData.getEndTime() - now);
            int gold = resFunBuildProxy.speedCost(second);
            if (playerProxy.getPowerValue(PlayerPowerDefine.POWER_gold) < gold) {
                return ErrorCodeDefine.M80004_2;
            }
            playerProxy.reducePowerValue(PlayerPowerDefine.POWER_gold, gold, LogDefine.LOST_TASK_BUY_QUICK);
        }
        return 0;
    }

    public Set<Long> taskSet = new ConcurrentHashSet<>();
    public Set<Long> noticeSet = new ConcurrentHashSet<>();

    public List<M8.TaskTeamInfo> getAllTaskTeamInfo() {
        List<M8.TaskTeamInfo> res = new ArrayList<>();
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Long> teamIds = WorldService.getPlayerTeamIds(playerProxy.getPlayerId());
        //自己的部队
        if (teamIds != null) {
            for (Long teamId : teamIds) {
                WorldTeamData teamData = WorldService.getTeamData(teamId);
                res.add(getTaskTeamInfo(teamData));
            }
        }

        //别人来驻军的部队
        Tuple2<Integer, Integer> tuple = getWorldTilePoint();
        WorldNodeData nodeData = WorldService.getWorldNode(playerProxy.getAreaKey(), tuple._1(), tuple._2());
        if (nodeData != null) {
            for (Long teamId : nodeData.getHelplist()) {
                WorldTeamData teamData = WorldService.getTeamData(teamId);
                res.add(getTaskTeamInfo(teamData));
            }
        }
        return res;
    }

    public List<M8.TeamNoticeInfo> getAllTeamNoticeInfo() {
        List<M8.TeamNoticeInfo> res = new ArrayList<>();
        //先查看自己的据点有没有被攻击
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        Tuple2<Integer, Integer> tuple = getWorldTilePoint();
        WorldNodeData nodeData = WorldService.getWorldNode(playerProxy.getAreaKey(), tuple._1(), tuple._2());
        if (nodeData != null) {
            for (Long teamId : nodeData.getFightList()) {
                WorldTeamData teamData = WorldService.getTeamData(teamId);
                res.add(getBeAttackTeamNoticeInfo(teamData));
            }
        }

        //在遍历自己占领的所有据点，找出有被攻击的
        for (Long pointKey : playerProxy.getPlayer().getWorldResPoint()) {
            int x = (int) (pointKey / 1000);
            int y = (int) (pointKey % 1000);
            WorldNodeData resNode = WorldService.getWorldNode(playerProxy.getAreaKey(), x, y);
            for (Long teamId : resNode.getFightList()) {
                WorldTeamData teamData = WorldService.getTeamData(teamId);
                res.add(getBeAttackTeamNoticeInfo(teamData));
            }
        }
        return res;
    }

    /***派出部队数量***/
    public int getTaskNum(){
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Long> teamIds = WorldService.getPlayerTeamIds(playerProxy.getPlayerId());
        if(teamIds==null){
            return 0;
        }
        return teamIds.size();
    }

    /**是否玩家身上有该任务部队***/
    public boolean isHasTask(long id){
        PlayerProxy playerProxy=getGameProxy().getProxy(ActorDefine.PLAYER_PROXY_NAME);
        List<Long> teamIds = WorldService.getPlayerTeamIds(playerProxy.getPlayerId());
        if(teamIds==null){
            return false;
        }
        return teamIds.contains(id);
    }
}
