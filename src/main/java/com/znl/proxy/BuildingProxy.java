package com.znl.proxy;

import com.znl.base.BaseDbPojo;
import com.znl.base.BasicProxy;
import com.znl.core.SimplePlayer;
import com.znl.define.ActorDefine;
import com.znl.define.ChatAndMailDefine;
import com.znl.pojo.db.WorldBuilding;
import com.znl.proto.M8;
import com.znl.service.map.TileType;
import com.znl.service.map.WorldTile;
import com.znl.utils.RandomUtil;
import scala.Tuple2;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        if (worldBuilding != null){
            if (playerProxy.getPlayer().getWorldTileX() != worldBuilding.getWorldTileX() || playerProxy.getPlayer().getWorldTileY() != worldBuilding.getWorldTileY()){
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
            if (title.building() != null&&title.building().getPlayerId()>0 && !"".equals(title.playerName())) {
                peoplelist.add(title);
            } else {
                if (title.resType() == 1 ) {
                    resouce1list.add(title);
                }
                if (title.resType() == 2 ) {
                    resouce2list.add(title);
                }
                if (title.resType() == 3 ) {
                    resouce3list.add(title);
                }
                if (title.resType() == 4 ) {
                    resouce4list.add(title);
                }
                if (title.resType() == 5 ) {
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

}
