package com.znl.service.actor

import java.util

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.znl.base.{BaseSetDbPojo, BaseDbPojo}
import com.znl.define.DataDefine
import com.znl.msg.GameMsg._
import com.znl.pojo.db.WorldBuilding
import com.znl.pojo.db.set.WorldTileSetDb
import com.znl.proxy.{ConfigDataProxy, DbProxy}
import com.znl.service.WorldService
import com.znl.service.map.{WorldBlock, TileType, WorldTile}
import com.znl.utils.{GameUtils, RandomEmitter}
import org.json.JSONObject

import scala.collection.JavaConversions._
import scala.util.parsing.json.JSONArray

object WorldBlockActor {
  def props(worldBlock: WorldBlock, areaKey: String) = Props(classOf[WorldBlockActor], worldBlock, areaKey)
}

/** 50 * 50的地图块
  * Created by Administrator on 2015/11/10.
  */
class WorldBlockActor(worldBlock: WorldBlock, areaKey: String) extends Actor with ActorLogging {

  override def preStart() = {
  }

  override def postRestart(throwable: Throwable) = {
    log.error(throwable, throwable.getMessage)
  }

  override def receive: Receive = {
    case AddWorldBuildingToTile(playerId: Long, accountName: String) =>
      addWorldBuildingToTile(playerId, accountName)
    case WatchBuildingTileInfo(x, y) =>
      val value = getBuildingTileInfo(x, y)
      sender().tell(value, self)
    case GetRandomEmpty() =>
      getEmptyKey()
    case _ =>
  }

  def getBuildingTileInfo(x: Int, y: Int) = {
    val key = getBuildingTileKey(x, y)
    if (worldBlock.worldTileMap.contains(key)) {
      val value = worldBlock.worldTileMap.get(key)
      Some(value)
    } else {
      None
    }
  }

  def getEmptyKey(): Unit = {
    val tileEmptyKeyList = new java.util.ArrayList[String]()
    worldBlock.worldTileMap.foreach(tile => {
      val worldTile = tile._2
      if (worldTile.tileType == TileType.Empty) {
        tileEmptyKeyList.add(tile._1)
      }
    })
    if (tileEmptyKeyList.size() == 0) {
      log.error("!!!!!!没有空地!!!!!!!!")
    } else {
      val index = GameUtils.getRandomValueByRange(tileEmptyKeyList.size())
      val randomKey = tileEmptyKeyList.get(index)
      val worldTile: WorldTile = worldBlock.worldTileMap.get(randomKey)
      sender() ! Some(worldTile)
    }
  }

  //添加建筑到世界Tile
  def addWorldBuildingToTile(playerId: Long, accountName: String) = {
    //默认可以直接添加建筑，除非没有空地了 由上层统筹处理

    val tileEmptyKeyList = new java.util.ArrayList[String]()
    worldBlock.worldTileMap.foreach(tile => {
      val worldTile = tile._2
      if (worldTile.tileType == TileType.Empty) {
        tileEmptyKeyList.add(tile._1)
      }
    })

    if (tileEmptyKeyList.size() == 0) {
      log.error("!!!!!!没有空地!!!!!!!!")
    } else {
      val index = GameUtils.getRandomValueByRange(tileEmptyKeyList.size())
      val randomKey = tileEmptyKeyList.get(index)
      val worldTile = worldBlock.worldTileMap.get(randomKey)
      val noData = WorldService.getWorldNode(areaKey, worldTile.x, worldTile.y)
      noData.setOccupyPlayerId(playerId)
      val worldTileSetDb = BaseSetDbPojo.getSetDbPojo(classOf[WorldTileSetDb], areaKey)

      val building = worldTileSetDb.createDbPojo(randomKey, classOf[WorldBuilding])
      building.setPlayerId(playerId)
      building.setWorldTileX(worldTile.x)
      building.setWorldTileY(worldTile.y)
      building.save()

      context.parent ! AddWorldBuildingSuccess(playerId, accountName, building.getId(), worldTile.x, worldTile.y)

      worldTile.building_(building)
      worldTile.tileType_(TileType.Building)


      worldBlock.curEmptyTileNum_(worldBlock.curEmptyTileNum - 1)
      worldBlock.curPlayerNum_(worldBlock.curPlayerNum + 1)

      //
      context.parent ! UpdateWorldBlock(worldBlock)
    }


  }


  def getBuildingTileKey(x: Int, y: Int) = {
    "buildTile" + x + "_" + y
  }

}
