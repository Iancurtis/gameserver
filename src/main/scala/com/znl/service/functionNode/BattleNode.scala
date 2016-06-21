package com.znl.service.functionNode

import java.{util, lang}

import akka.actor.Actor.Receive
import akka.actor.{ActorContext, ActorLogging, Actor, Props}
import com.google.protobuf.GeneratedMessage
import com.znl.GameMainServer
import com.znl.base.{BaseSetDbPojo, BaseLog, BaseDbPojo}
import com.znl.core._
import com.znl.define.PlayerPowerDefine._
import com.znl.define._
import com.znl.log.{ResourceGet, CustomerLogger}
import com.znl.log.admin._
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg._
import com.znl.node.battle.BattleNodeActor
import com.znl.pojo.db.{PerformTasks, Player}
import com.znl.pojo.db.set.HelpTeamDateSetDb
import com.znl.proto.Common.RewardInfo
import com.znl.proto.{Common, M5}
import com.znl.proto.M5.PuppetAttr
import com.znl.proxy._
import com.znl.service.PlayerService
import com.znl.service.map.{TileType, TileBattleResult, WorldTile}
import com.znl.service.map.TileType.TileType
import com.znl.template.{MailTemplate, ReportTemplate}
import com.znl.utils.GameUtils
import org.json.JSONObject
import scala.concurrent.duration._

/**
  * Created by Administrator on 2015/12/24.
  */

object BattleNode {
  def props(x: Integer, y: Integer, sortId: Integer, tileType: TileType, tile: WorldTile, areaKey: String) = Props(classOf[BattleNode], x, y, sortId, tileType, tile, areaKey)
}

class BattleNode(x: Integer, y: Integer, sortId: Integer, tileType: TileType, tile: WorldTile, areaKey: String) extends Actor with ActorLogging {
  var BATTLE_NODE_NAME: String = "WORLD_BATTTLE"

  override def preStart() = {
    import context.dispatcher
    context.system.scheduler.schedule(0 milliseconds, 1 seconds, context.self, OnServerTrigger())
    context.watch(context.actorOf(Props.create(classOf[BattleNodeActor]), BATTLE_NODE_NAME))

  }

  import scala.collection.JavaConversions._

  def savaDate(): Unit = {
    //克隆出来队列，然后把队列清空返回给玩家
    print("开始关闭节点啦！！！！！！！！！！！！！！")
    val tempFightList = new util.ArrayList[Team](fightList)
    val tempBackList = new util.ArrayList[Team](backList)
    val temphelpList = new util.ArrayList[Team](helplist)
    val tempDiggingTeam = digingTeam
    fightList.clear()
    backList.clear()
    digingTeam = null
    if (tempFightList.size() > 0) {
      for (team: Team <- tempFightList) {
        val rewardMap = new util.HashMap[Integer, Integer]()
        stopServerResult(team, rewardMap)
      }
    }

    if (temphelpList.size() > 0) {
      for (team: Team <- temphelpList) {
        deleDefendDate(team)
        val rewardMap = new util.HashMap[Integer, Integer]()
        stopServerResult(team, rewardMap)
      }
    }

    if (tempBackList.size() > 0) {
      for (team: Team <- tempBackList) {
        var rewardMap = new util.HashMap[Integer, Integer]()
        if (team.result != null) {
          rewardMap = team.result.rewardMap
        }
        stopServerResult(team, rewardMap)
      }
    }

    if (tempDiggingTeam != null && occupyTime != 0) {
      //计算当前的奖励
      val now = GameUtils.getServerDate().getTime
      val digTime = now - occupyTime
      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
      var product: Double = (pointConfig.getInt("product") * digTime / 1000).toInt
      val rduce = (1 + (tempDiggingTeam.powerMap.get(NOR_POWER_rescollectrate) / UtilDefine.RUN_UNDER))
      product = product * rduce
      if (tempDiggingTeam.result == null) {
        tempDiggingTeam.result = new TileBattleResult
      }
      if (tempDiggingTeam.result.rewardMap == null) {
        tempDiggingTeam.result.rewardMap = new util.HashMap[Integer, Integer]()
      }
      tempDiggingTeam.result.powerMap = tempDiggingTeam.powerMap
      tempDiggingTeam.result.rewardMap.put(pointConfig.getInt("restype"), product.toInt)
      stopServerResult(tempDiggingTeam, tempDiggingTeam.result.rewardMap)
    }
  }

  def sendRewardMapToWorldService(): Unit = {
    val temphelpList = new util.ArrayList[Team](helplist)
    var rewardMap = new util.HashMap[Integer, Integer]()
    var playerId = 0l
    val tempBackList = new util.ArrayList[Team](backList)
    if (tempBackList.size() > 0) {
      for (team: Team <- tempBackList) {
        if (team.result != null) {
          rewardMap = team.result.rewardMap
          playerId = tile.building.getPlayerId
          context.parent ! SaveCloseNodeResource(playerId, rewardMap)
        }
      }
    }
    val tempDiggingTeam = digingTeam
    if (tempDiggingTeam != null && occupyTime != 0) {
      //计算当前的奖励
      val now = GameUtils.getServerDate().getTime
      val digTime = now - occupyTime
      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
      var product: Double = (pointConfig.getInt("product") * digTime / 1000).toInt
      val rduce = (1 + (tempDiggingTeam.powerMap.get(NOR_POWER_rescollectrate) / UtilDefine.RUN_UNDER))
      product = product * rduce
      if(product > occupyLoads){
        product = occupyLoads
      }
      rewardMap.put(pointConfig.getInt("restype"), product.toInt)
      playerId = occupyPlayerId
      context.parent ! SaveCloseNodeResource(playerId, rewardMap)
    }
    if (temphelpList.size() > 0) {
      for (team: Team <- temphelpList) {
        deleDefendDate(team)
      }
    }
  }

  override def postStop() = {
    //克隆出来队列，然后把队列清空返回给玩家
    print("开始关闭节点啦！！！！！！！！！！！！！！")
    sendRewardMapToWorldService()
    //    val tempFightList = new util.ArrayList[Team](fightList)
    //    val tempBackList = new util.ArrayList[Team](backList)
    //    val temphelpList = new util.ArrayList[Team](helplist)
    //    val tempDiggingTeam = digingTeam
    //    fightList.clear()
    //    backList.clear()
    //    digingTeam = null
    //    if (tempFightList.size() > 0) {
    //      for (team: Team <- tempFightList) {
    //        val rewardMap = new util.HashMap[Integer, Integer]()
    //        stopServerResult(team, rewardMap)
    //      }
    //    }
    //
    //    if (temphelpList.size() > 0) {
    //      for (team: Team <- temphelpList) {
    //        deleDefendDate(team)
    //        val rewardMap = new util.HashMap[Integer, Integer]()
    //        stopServerResult(team, rewardMap)
    //      }
    //    }
    //
    //    var rewardMap = new util.HashMap[Integer, Integer]()
    //    if (tempBackList.size() > 0) {
    //      for (team: Team <- tempBackList) {
    //        if (team.result != null) {
    //          rewardMap = team.result.rewardMap
    //        }
    //        stopServerResult(team, rewardMap)
    //      }
    //    }
    //
    //    if (tempDiggingTeam != null && occupyTime != 0) {
    //      //计算当前的奖励
    //      val now = GameUtils.getServerDate().getTime
    //      val digTime = now - occupyTime
    //      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
    //      var product : Double= (pointConfig.getInt("product") * digTime / 1000).toInt
    //      val rduce =(1+(tempDiggingTeam.powerMap.get(NOR_POWER_rescollectrate)/UtilDefine.RUN_UNDER))
    //      product = product * rduce
    //      rewardMap.put(pointConfig.getInt("restype"), product.toInt)
    //      if (tempDiggingTeam.result == null) {
    //        tempDiggingTeam.result = new TileBattleResult
    //      }
    //      if (tempDiggingTeam.result.rewardMap == null) {
    //        tempDiggingTeam.result.rewardMap = new util.HashMap[Integer, Integer]()
    //      }
    //      tempDiggingTeam.result.rewardMap.put(pointConfig.getInt("restype"), product.toInt)
    //      stopServerResult(tempDiggingTeam, tempDiggingTeam.result.rewardMap)
    //    }
  }

  def deleDefendDate(team: Team): Unit = {
    val defendsimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
    if (defendsimplePlayer.online) {
      tellMsgToPlayerModule(defendsimplePlayer.getAccountName, RemoveBeAttackedNotifyBytime(team.fightTime))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(defendsimplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      performTasksProxy.deleteFormTask(team.fightTime, playerProxy)
      player.setGardNum(0)
      player.save()
    }

    val atcksimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
    if (atcksimplePlayer.online) {
      tellMsgToPlayerModule(atcksimplePlayer.getAccountName, DelformTask(x, y, team.fightTime))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(atcksimplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      performTasksProxy.changeTaskType(x, y, team.fightTime, TaskDefine.PERFORM_TASK_HELPBACK)
      performTasksProxy.deleteFormTask(team.fightTime, playerProxy)
      player.save()
    }
  }

  def stopServerResult(team: Team, rewardMap: util.HashMap[Integer, Integer]) = {
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
    if (simplePlayer.online) {
      tellMsgToPlayerModule(simplePlayer.getAccountName, StopNodeTeamBack(team.fightTeams, rewardMap))
    } else {
      //给玩家加佣兵和数据
      addTeamToOffLinePlayer(simplePlayer, team, true)
    }

  }

  var isStartBattle: Boolean = false
  var occupyTime: Long = 0
  var occupyPlayerId : java.lang.Long= 0l
  var occupyLoads: Long = 0;
  var occupyLoadstemp: Long = 0;
  //载重
  //等待战斗队列
  var fightList: util.ArrayList[Team] = new util.ArrayList[Team]
  //挖掘资源
  var digingTeam: Team = null
  //返回队列
  var backList: util.ArrayList[Team] = new util.ArrayList[Team]
  //驻守的军队
  var helplist: util.ArrayList[Team] = new util.ArrayList[Team]

  var attackSimplePlayer: SimplePlayer = null
  var defendSimplePlayer: SimplePlayer = null

  def getFightTeam(): Team = {
    fightList synchronized {
      val time = GameUtils.getServerDate().getTime
      for (team <- fightList) {
        if (team.fightTime <= time) {
          return team
        }
      }
    }
    return null
  }

  def getHelpTeam(): Team = {
    helplist synchronized {
      val time = GameUtils.getServerDate().getTime
      for (team <- helplist) {
        if (team.fightTime <= time && team.tasttype != TaskDefine.PERFORM_TASK_HELPBACK) {
          return team
        }
      }
    }
    return null
  }


  /** *检查是否要删除，删除时间缓冲为一分钟 ***/
  var pillTime = 0
  val PROTECT_TIME = 60

  def checkPillTime() {
    if (fightList.size() == 0 && digingTeam == null && backList.size() == 0 && helplist.size() == 0) {
      if (pillTime == 0) {
        pillTime = GameUtils.getServerTime + PROTECT_TIME
        CustomerLogger.error("世界节点设置结束时间：" + x + "," + y + "=" + pillTime)
      } else if (pillTime < GameUtils.getServerTime) {
        context.parent ! GiveMeAPill(x, y)
        CustomerLogger.error("世界节点向上层请求一个结束：" + x + "," + y)
      }
    } else {
      pillTime = 0
    }
  }

  def onServerTrigger(): Unit = {
    checkPillTime
    battleListTrigger
    //    digTeamTrigger      //屏蔽掉自动返回功能
    backListTrigger
    helpListTrigger
  }

  def digTeamTrigger(): Unit = {
    //判断载重是否已满
    val now = GameUtils.getServerDate().getTime
    if (occupyTime == 0) {
      return
    }
    val digTime = now - occupyTime
    val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
    var product: Double = pointConfig.getInt("product") * digTime / 1000
    product = product * getDigAdd()
    if (product >= occupyLoads) {
      product = occupyLoads
      //满了可以回去了
      digTeamBackToPlayer(pointConfig.getInt("restype"), product.toInt)

      //置回没占领状态
      clearDigState(tile)
    }
  }

  def onHelpTeamBack(x: Int, y: Int, time: Long, taskType: Int): Unit = {
    val team: Team = gethelpTeamByTime(time)
    if (team == null) {
      sender() ! DelformTask(x, y, time)
      return
    }
    val atcksimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
    val addtime: Double = Math.sqrt(Math.pow(this.x - atcksimplePlayer.getX, 2) + Math.pow(this.y - atcksimplePlayer.getY, 2)) * TaskDefine.WORLD_TASK_TIME_EACH + TaskDefine.WORLD_TASK_TIME_LIMIT
    val backTime: Long = GameUtils.getServerDate().getTime + (addtime.toInt) * 1000
    val result: TileBattleResult = new TileBattleResult
    result.attackSortId = team.attackSortId
    result.attackTeam = team.fightTeams
    result.attackX = team.attackX
    result.attackY = team.attackY
    result.attackId = team.attackId
    result.defendSortId = sortId
    result.defendX = this.x
    result.defendY = this.y
    result.winner = 2
    result.defendTileType = TileType.Building
    result.powerMap = team.powerMap
    sendResultHandleToWolrd(result)
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
    if (simplePlayer.online) {
      tellMsgToPlayerModule(simplePlayer.getAccountName, RemoveBeAttackedNotifyBytime(time))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      performTasksProxy.removeTeamNotice(time, playerProxy)
      player.setGardNum(player.getGardNum-1)
      val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
      tellService(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(sp))
      player.save()
    }
    if (atcksimplePlayer.online) {
      tellMsgToPlayerModule(atcksimplePlayer.getAccountName, DelformTask(x, y, time))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(atcksimplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      performTasksProxy.changeTaskType(x, y, team.fightTime, TaskDefine.PERFORM_TASK_HELPBACK)
      performTasksProxy.deleteFormTask(time, playerProxy)
      player.save()
      performTasksProxy.savePerformTasks()
    }
    if (taskType == TaskDefine.PERFORM_TASK_OTHERHELPBACK) {
      val template: MailTemplate = new MailTemplate("驻军遣返", simplePlayer.getName + "无需您的驻守，遣返了您的驻守部队", 0, atcksimplePlayer.getName, ChatAndMailDefine.MAIL_TYPE_INBOX)
      val allid: util.Set[java.lang.Long] = new util.HashSet[lang.Long]()
      allid.add(atcksimplePlayer.getId)
      tellService(ActorDefine.MAIL_SERVICE_NAME, GameMsg.SendMail(allid, template, "邮件", 0l))
    } else if (taskType == TaskDefine.PERFORM_TASK_HELPBACK) {
      val template: MailTemplate = new MailTemplate("驻军更变通知", atcksimplePlayer.getName + "收回驻军的派遣部队，请指挥官及时调整自己的防御部署", 0, defendSimplePlayer.getName, ChatAndMailDefine.MAIL_TYPE_INBOX)
      val allid: util.Set[java.lang.Long] = new util.HashSet[lang.Long]()
      allid.add(defendSimplePlayer.getId)
      tellService(ActorDefine.MAIL_SERVICE_NAME, GameMsg.SendMail(allid, template, "邮件", 0l))
    }
    helplist.remove(team)
  }


  def digTeamBackToPlayer(resourceType: Int, resourceValue: Int): Unit = {
    //    val result : TileBattleResult = new TileBattleResult
    //    result.rewardMap.put(resourceType,resourceValue)
    //    result.attackSortId = digingTeam.attackSortId
    //    result.attackTeam = digingTeam.fightTeams
    //    result.attackX = digingTeam.attackX
    //    result.attackY = digingTeam.attackY
    //    result.attackId = digingTeam.attackId
    //    result.defendSortId = sortId
    //    result.defendX = x
    //    result.defendY = y
    if (digingTeam.result.rewardMap == null) {
      digingTeam.result.rewardMap = new util.HashMap[Integer, Integer]()
    }
    /*  val reduce :Double=(1+(digingTeam.powerMap.get(PlayerPowerDefine.NOR_POWER_rescollectrate)/UtilDefine.RUN_UNDER))
      var temp:Double=resourceValue
      temp = (temp * reduce)*/
    digingTeam.result.rewardMap.put(resourceType, resourceValue.toInt)
    context.parent ! DigTeamBackNotice(digingTeam.result)
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(digingTeam.attackId, areaKey)
    if (simplePlayer.online) {
      tellMsgToPlayerModule(simplePlayer.getAccountName, RemoveBeAttackedNotifyByXY(x, y))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      performTasksProxy.removeTeamNoticeByXY(x, y, playerProxy)
      player.save()
    }
  }

  //置回没占领状态
  def clearDigState(worldTile: WorldTile): Unit = {
    occupyLoads = 0
    occupyTime = 0l
    occupyPlayerId = 0l
    digingTeam = null
    worldTile.defendPlayerId_(0)
    worldTile.defendTeams_(null)
  }

  def addTeamToOffLinePlayer(simplePlayer: SimplePlayer, team: Team, stopNode: Boolean): Unit = {
    //不在线的玩家做资源、佣兵的增加
    val player: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
    //TODO WARNING！这里要做出战队列的加法和创建，只能对整个完整的玩家GameProxy进行创建，不然拿不到完整的属性
    val cache: PlayerCache = new PlayerCache()
    cache.setAreId(player.getAreaId)
    val gameProxy: GameProxy = new GameProxy(player, cache)

    val playerProxy: PlayerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME)
    if (team.result != null) {
      if (team.result.rewardMap != null) {
        playerProxy.addOffLinePowerValue(team.result.rewardMap)
        for (power <- team.result.rewardMap.keySet()) {
          val value: Int = team.result.rewardMap.get(power)
          if (power >= 201 && power <= 206) {
            goldLog(player, power, 1, LogDefine.GET_WORLD_FIGHT_TEAM_RETURN, value)
          }
        }
      }
      if (team.result.reward != null) {
        playerProxy.addOffLinePowerValue(team.result.reward.addPowerMap)
        val itemProxy: ItemProxy = gameProxy.getProxy(ActorDefine.ITEM_PROXY_NAME)
        itemProxy.addItemByMap(team.result.reward.addItemMap, LogDefine.GET_WORLD_FIGHT_TEAM_RETURN)
        itemProxy.saveItems()
        for (typeId <- team.result.reward.addItemMap.keySet()) {
          val num: Int = team.result.reward.addItemMap.get(typeId)
          itemLog(player, 1, num, typeId, LogDefine.GET_WORLD_FIGHT_TEAM_RETURN)
        }
      }
    }
    val soldierProxy: SoldierProxy = gameProxy.getProxy(ActorDefine.SOLDIER_PROXY_NAME)
    for (playerTeam: PlayerTeam <- team.fightTeams) {
      val num: Int = playerTeam.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      val typeId: Int = playerTeam.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      soldierProxy.addSoldierNumWithoutBaseNum(typeId, num, LogDefine.GET_WORLD_FIGHT_TEAM_RETURN)
//      soldierLog(player, 1, num, typeId, LogDefine.GET_WORLD_FIGHT_TEAM_RETURN)
    }
    soldierProxy.saveSoldier()
    //阵型
    val formationProxy: FormationProxy = gameProxy.getProxy(ActorDefine.FORMATION_PROXY_NAME)
    val refurce: Boolean = formationProxy.checkBaseDefendTroop(soldierProxy)
    if (refurce) {
      val teams = formationProxy.createFormationTeam(SoldierDefine.FORMATION_DEFEND)
      val troop: PlayerTroop = formationProxy.refurceDefendTeam(teams, playerProxy.getPlayerId)
      simplePlayer.setDefendTroop(troop)
      simplePlayer.setRefDefendTroop(true)
    }
    GameUtils.player2SimplePlayer(player, simplePlayer)

    //如果是关服的逻辑，则要清空玩家身上的任务部队
    if (stopNode) {
      val performTasksProxy: PerformTasksProxy = gameProxy.getProxy(ActorDefine.PERFORMTASKS_PROXY_NAME)
      performTasksProxy.clearPerformTasks()
      performTasksProxy.clearTeamNotice()
      performTasksProxy.savePerformTasks()
    }
    playerProxy.savePlayer()
    gameProxy.finalize()
  }

  def backListTrigger(): Unit = {
    val time = GameUtils.getServerDate().getTime
    val list = new util.ArrayList[Team]()
    for (team <- backList) {
      if (team.fightTime <= time) {
        list.add(team)
      }
    }
    if (list.size() > 0) {
      backList.removeAll(list)
    }
    if (list.size() > 0) {
      //每次有返回队伍到期的时候取刷新一次simplePlayer
      defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
    }
    for (team <- list) {
      if (defendSimplePlayer.online) {
        tellMsgToPlayerModule(defendSimplePlayer.getAccountName, BuildBattleEndBack(team.result))
      } else {
        addTeamToOffLinePlayer(defendSimplePlayer, team, false)
      }
      //写入行为日志
      val sb: StringBuffer = new StringBuffer
      import scala.collection.JavaConversions._
      var playerId = 0l
      for (team <- team.fightTeams) {
        val typeId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
        val num: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
        playerId = team.playerId
        if (num > 0) {
          sb.append(typeId)
          sb.append(",")
          sb.append(num)
          sb.append("&")
        }
      }
      writeFunctionLog(defendSimplePlayer, FunctionIdDefine.RETURN_WORLD_TILE_INFO_FUNCTION_ID, team.attackX.toLong, team.attackY.toLong, 0, sb.toString)
    }

  }

  def sendResultHandleToWolrd(result: TileBattleResult): Unit = {
    tellService(ActorDefine.WORLD_SERVICE_NAME, GameMsg.BuildBattleResult(result))
  }

  def checkDefendTeamList(defendList: util.List[PlayerTeam]) = {
    for (team: PlayerTeam <- defendList) {
      var index: Integer = team.getValue(SoldierDefine.NOR_POWER_INDEX).asInstanceOf[Integer]
      if (index < 20) {
        index = index + 10
        team.powerMap.put(SoldierDefine.NOR_POWER_INDEX, index)
        team.basePowerMap.put(SoldierDefine.NOR_POWER_INDEX, index)
      }
      //2016/04/05 增加判断是否有会重置队伍的情况出现
      if (team.reset == true){
        team.reset = false
      }
    }
  }

  //检查驻军
  def helpListTrigger(): Unit = {
    checkLegionHelp()
    val team: Team = getHelpTeam()
    if (team != null) {
      if (team.tasttype == TaskDefine.PERFORM_TASK_HELPBACK) {
        return
      }
    } else {
      return
    }
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
    if (simplePlayer.online) {
      tellMsgToPlayerModule(simplePlayer.getAccountName, changeformTask(x, y, team.fightTime))
      team.tasttype = TaskDefine.PERFORM_TASK_HELPBACK
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      performTasksProxy.changeTaskType(x, y, team.fightTime, TaskDefine.PERFORM_TASK_HELPBACK)
      team.tasttype = TaskDefine.PERFORM_TASK_HELPBACK
    }
    if (defendSimplePlayer == null) {
      defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
    }
    val template: MailTemplate = new MailTemplate("驻军通知", simplePlayer.getName + "派遣了一支部队辅助您驻守基地，请下达命令", 0, defendSimplePlayer.getName, ChatAndMailDefine.MAIL_TYPE_INBOX)
    val allid: util.Set[java.lang.Long] = new util.HashSet[lang.Long]()
    allid.add(defendSimplePlayer.getId)
    tellService(ActorDefine.MAIL_SERVICE_NAME, GameMsg.SendMail(allid, template, "系统邮件", 0l))
    if (defendSimplePlayer == null) {
      defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
    }
    if (defendSimplePlayer.online) {
      tellMsgToPlayerModule(defendSimplePlayer.getAccountName, tellHasArried(team.fightTime))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      performTasksProxy.chanbeginttime(team.fightTime)
    }
    /* if(defendSimplePlayer.online){
       tellMsgToPlayerModule(defendSimplePlayer.getAccountName, changeformTask(x, y, team.fightTime))
     }*/
    /*
      if (defendsimplePlayer.online) {
        tellMsgToPlayerModule(defendsimplePlayer.getAccountName, RemoveBeAttackedNotify(simplePlayer.getX, simplePlayer.getY,team.fightTime))
      } else {
        val player: Player = BaseDbPojo.get(defendsimplePlayer.getId, classOf[Player])
        val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet)
        val playerProxy: PlayerProxy = new PlayerProxy(player)
        performTasksProxy.removeTeamNotice(simplePlayer.getX, simplePlayer.getY,team.fightTime, playerProxy)
      }*/
  }

  def checkLegionHelp(): Unit = {
    var helplisttemp: util.ArrayList[Team] = new util.ArrayList[Team](helplist)
    for (ta <- helplisttemp) {
      defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
      val helpsimple: SimplePlayer = PlayerService.getSimplePlayer(ta.attackId, areaKey)
      if (helpsimple.getArmygrouid != defendSimplePlayer.getArmygrouid) {
        onHelpTeamBack(ta.attackX, ta.attackY, ta.fightTime, 7)
      }
    }
  }

  /** *每秒或者战斗完成遍历一次，将到时间的队伍拿出来战斗（一次只处理一个避免战斗包还在处理中） ****/
  def battleListTrigger(): Unit = {
    if (isStartBattle == false) {
      val team: Team = getFightTeam()
      if (team != null) {
        fightList.remove(team)
      } else {
        //没进攻队伍了就什么也不需要干了
        return
      }

      //战斗准备逻辑
      isStartBattle = false
      battleBuilder.clear()
      attackSimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
      defendSimplePlayer = null


      //defendSimplePlayer的初始化，如果是玩家建筑时获取建筑的玩家id，如果是资源点时获得资源点防守的id
      if (tile.tileType == TileType.Resource && tile.defendPlayerId > 0) {
        defendSimplePlayer = PlayerService.getSimplePlayer(tile.defendPlayerId, areaKey)
      } else if (tile.tileType == TileType.Building) {
        defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
      }


      //获取该资源点的防守队伍
      var defendList: util.List[PlayerTeam] = null
      var defendId = 0l
      if (tile.tileType == TileType.Empty || (tile.tileType == TileType.Resource && team.attackId == occupyPlayerId) || (defendSimplePlayer != null && attackSimplePlayer.getArmygrouid == defendSimplePlayer.getArmygrouid && attackSimplePlayer.getArmygrouid > 0) || (tile.tileType == TileType.Building && defendSimplePlayer.getProtectOverDate > GameUtils.getServerDate().getTime)) {
        //有可能玩家迁移走了，这块低变空地了，或者是这个点已经被自己占领了。所以大家就白跑一趟咯
        val result: TileBattleResult = new TileBattleResult
        result.attackSortId = team.attackSortId
        result.attackTeam = team.fightTeams
        result.attackX = team.attackX
        result.attackY = team.attackY
        result.attackId = team.attackId
        result.defendSortId = sortId
        result.defendX = x
        result.defendY = y
        result.winner = 2
        result.powerMap = team.powerMap
        result.defendTileType = TileType.Building
        context.parent ! BuildBattleResult(result)
        return
      } else if (tile.tileType == TileType.Resource && tile.defendPlayerId == 0) {
        //创建一个空的副本代理，来调用副本的创建怪物
        val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[java.lang.Long], areaKey)
        defendList = dungeoProxy.getMonsterList(tile.monsterGroupId)
        defendId = -tile.monsterGroupId
      } else if (tile.tileType == TileType.Resource && tile.defendPlayerId > 0) {
        defendList = digingTeam.fightTeams
        defendId = tile.defendPlayerId
      } else {
        if (defendSimplePlayer.getHelpId != 0) {
          val task: PerformTasks = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getHelpId, classOf[PerformTasks], areaKey)
          if (task != null) {
            val team: Team = gethelpTeamByTime(task.getTimeer)
            if (team != null) {
              defendList = team.fightTeams
            } else {
              defendList = defendSimplePlayer.getDefendTroop.getPlayerTeams
            }
          }
        } else {
          defendList = defendSimplePlayer.getDefendTroop.getPlayerTeams
        }
        defendId = defendSimplePlayer.getId
      }


      //没防守队伍就是进攻赢了
      if (defendList == null || defendList.size() == 0) {
        if (tile.tileType == TileType.Building) {
          val totalLoad = countLoad(team.fightTeams, team.powerMap)
          val honner = 0
          defendSimplePlayer = PlayerService.getSimplePlayer(defendSimplePlayer.getId, areaKey)
          val attBoomAdd = attackHandle(team.attackX, team.attackY, team.attackSortId, team.fightTeams, true, honner)
          val rewardMap: util.HashMap[Integer, Integer] = getRewardmap(tile, defendSimplePlayer, totalLoad)
          val defBoomReduce = defendHandle(null, false, null, defendSimplePlayer, rewardMap, honner)
          val result: TileBattleResult = getEndBattleResult(null, tile, rewardMap, null, team, attBoomAdd, defBoomReduce)
          sendResultHandle(true, result, tile)
          if (defendSimplePlayer.getArmygrouid > 0) {
            val situation: Situation = new Situation(defendSimplePlayer.getArmygrouid, defendSimplePlayer.getId, attackSimplePlayer.getId, GameUtils.getServerDate().getTime, 0, getallLost(rewardMap), ArmyGroupDefine.SITUATION_ARMY, 0)
            tellArmyNode(defendSimplePlayer.getArmygrouid, addSituation(situation))
          }
        } else {
          CustomerLogger.error("资源点居然会没有拿到防守队列！！！")
        }
      } else {
        //有防守队伍的话就要进入战斗
        val battle: PlayerBattle = new PlayerBattle
        battle.soldierList=new util.ArrayList[PlayerTeam]()
        for(tem <- team.fightTeams ){
          val newTeam:PlayerTeam=new PlayerTeam(tem.basePowerMap, tem.playerId)
          newTeam.powerMap=tem.powerMap
          newTeam.reset=tem.reset
          newTeam.capacityMap=tem.capacityMap
          print(newTeam)
          battle.soldierList.add(newTeam)
        }
        battle.soldierList = team.fightTeams
        checkDefendTeamList(defendList)
        battle.monsterList = defendList
        battle.id = -1
        battle.attackId = attackSimplePlayer.getId
        battle.defendId = defendId
        battle.`type` = BattleDefine.BATTLE_TYPE_WORLD
        battle.x = team.attackX
        battle.y = team.attackY
        battle.infoType = team.attackSortId
        battle.powerMap = team.powerMap
        context.actorSelection(BATTLE_NODE_NAME) ! GameMsg.ReqPuppetList(battle)
      }
    }
  }


  def getallLost(rewardMap: util.HashMap[Integer, Integer]): java.lang.Long = {
    var lost: Long = 0
    for (value <- rewardMap.values()) {
      lost = value + lost
    }
    return lost
  }


  def getWorldTitleByPoint(x: Int, y: Int, sortId: Int): WorldTile = {
    val worldTile: WorldTile = askService(ActorDefine.WORLD_SERVICE_NAME, AskBuildTitle(x, y, sortId))
    worldTile
  }

  var battleBuilder: M5.Battle.Builder = M5.Battle.newBuilder()

  def onPackPuppet(puppet: PuppetAttr): Unit = {
    val builder: M5.Puppet.Builder = M5.Puppet.newBuilder
    builder.setAttr(puppet)
    battleBuilder.addPuppets(builder.build)
  }

  def attackHandle(attackX: Int, attackY: Int, attackSort: Int, attackTeams: util.List[PlayerTeam], result: Boolean, honner: Int): Int = {
    //计算繁荣度
    var addBoom = (25 * (1.0 + GameUtils.getBoomConfig(attackSimplePlayer.getBoom.asInstanceOf[Int]).getInt("boomlv") * 0.1)).toInt
    if (defendSimplePlayer == null) {
      addBoom = 0;
    }
    var myHon = honner
    if (result == false) {
      myHon = -honner
    }
    //进攻方处理
    if (attackSimplePlayer.online == true) {
      //在线玩家通知到具体模块去执行佣兵扣除
      tellMsgToPlayerModule(attackSimplePlayer.getAccountName, FightBuildResult(attackTeams, addBoom, myHon,result))
    } else {
      //离线玩家从数据库获取出来扣除
      val player = BaseDbPojo.getOfflineDbPojo(attackSimplePlayer.getId, classOf[Player], areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      val dungeoProxy: DungeoProxy = new DungeoProxy(player.getDungeoSet, areaKey)
      val soldierProxy: SoldierProxy = new SoldierProxy(player.getSoldierSet, areaKey)
      val deathMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
      reduceDeadSoldier(attackTeams, BattleDefine.BATTLE_TYPE_WORLD, deathMap, soldierProxy, player)
      if(result == false){
        addAttackFailLeftSoldier(attackTeams,soldierProxy,player)
      }
      playerProxy.setSimplePlayer(attackSimplePlayer)
      soldierProxy.saveSoldier()
      soldierProxy.finalize()
      dungeoProxy.finalize()
      player.setBoom(player.getBoom + addBoom)
      if (player.getBoom > player.getBoomUpLimit) {
        player.setBoom(player.getBoomUpLimit)
      }
      player.setHonour(player.getHonour + myHon)
      if (player.getHonour < 0) {
        player.setHonour(0l)
      }
      playerProxy.refreshBoomLevel()
      //更新simplePlayer
      val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, attackSimplePlayer)
      tellService(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(sp))
      player.save

      //推送到排行服务
      if (honner != 0) {
        tellService(ActorDefine.POWERRANKS_SERVICE_NAME, AddPlayerToRank(player.getId, player.getHonour, PowerRanksDefine.POWERRANK_TYPE_HONOR))
      }
    }


    //写入平台日志
    var logType: Int = 0
    if (tileType == TileType.Building) {
      logType = LogDefine.ADMIN_BATTLE_ID_WORLD
    } else {
      if (digingTeam == null) {
        logType = LogDefine.ADMIN_BATTLE_ID_WORLD_RESOURCE
      } else {
        logType = LogDefine.ADMIN_BATTLE_ID_WORLD_FIGHT
      }
    }
    var state = 2
    if (result == true) {
      state = 1
    }
    val pvplog: tbllog_pvp = new tbllog_pvp(attackSimplePlayer.getPlatform, logType, attackSimplePlayer.getId, attackSimplePlayer.getAccountName,
      attackSimplePlayer.getLevel, state, GameUtils.getServerTime, GameUtils.getServerTime)
    sendLog(pvplog)


    //写入行为日志
    val sb: StringBuffer = new StringBuffer
    import scala.collection.JavaConversions._
    var playerId = 0l
    for (team <- attackTeams) {
      val typeId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      val num: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      playerId = team.playerId
      if (num > 0) {
        sb.append(typeId)
        sb.append(",")
        sb.append(num)
        sb.append("&")
      }
    }
    sb.append("$")
    sb.append(result)
    var id = 0l
    if (tileType == TileType.Resource) {
      id = -tile.resPointId
    } else {
      id = defendSimplePlayer.getId
    }
    writeFunctionLog(attackSimplePlayer, FunctionIdDefine.ATTACK_WORLD_TILE_INFO_FUNCTION_ID, x.toLong, y.toLong, id, sb.toString)

    addBoom
  }

  //发送日志
  def sendLog(log: BaseLog) = {
    tellService(ActorDefine.ADMIN_LOG_SERVICE_NAME, SendAdminLog(log, ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0))
  }

  def countLoad(teams: util.List[PlayerTeam], powerMap: util.Map[Integer, java.lang.Long]): Long = {
    var totalLoad = 0l
    for (team: PlayerTeam <- teams) {
      val load: Double = team.getValue(SoldierDefine.POWER_load).asInstanceOf[Integer].doubleValue()
      val loadPercent: Double = team.getValue(SoldierDefine.POWER_loadRate).asInstanceOf[Integer].doubleValue()
      val num: Double = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Integer].doubleValue()
      totalLoad = totalLoad + ((load * (1 + loadPercent / 10000.0)) * num).toInt
    }
    //totalLoad=totalLoad
    totalLoad
  }

  def defendHandle(teams: util.List[PlayerTeam], result: Boolean, defendTitle: WorldTile, simplePlayer: SimplePlayer, rewardMap: util.HashMap[Integer, Integer], honner: Int): Int = {
    var reduce = 0
    var hon = honner
    if (result == false) {
      val config = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", attackSimplePlayer.getVipLevel)
      reduce = ((config.getJSONArray("boomloss").getInt(1) / 100.0) * 100).asInstanceOf[Int]
      hon = -honner
    }
    if (teams != null && teams.size() > 0 && teams.get(0).playerId > 0 && teams.get(0).playerId != simplePlayer.getId) {
      val helpSimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(teams.get(0).playerId, areaKey)
      if (helpSimplePlayer == null) {
        return 0
      }
      if (helpSimplePlayer.online == true) {
        //在线玩家通知到具体模块执行扣除佣兵、刷新阵型
        tellMsgToPlayerModule(helpSimplePlayer.getAccountName, DefendBuildResult(teams, new util.HashMap[Integer, Integer](), 0, 0))
      }
    }
      /*else {
        //离线玩家从数据库获取出来扣除
        val player = BaseDbPojo.getOfflineDbPojo(helpSimplePlayer.getId, classOf[Player], areaKey)
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        playerProxy.reduceOffLinePowerValue(rewardMap)
        val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, helpSimplePlayer)
        /*        if (teams != null) {
                  val dungeoProxy: DungeoProxy = new DungeoProxy(player.getDungeoSet, areaKey)
                  val soldierProxy: SoldierProxy = new SoldierProxy(player.getSoldierSet, areaKey)
                  val deathMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
                  reduceDeadSoldier(teams, BattleDefine.BATTLE_TYPE_WORLD, deathMap, soldierProxy, player)
                  val formationProxy: FormationProxy = new FormationProxy(player.getFormationMember1Set, player.getFormationMember2Set, player.getFormationMember3Set, areaKey)
                  val refurce: Boolean = formationProxy.checkDefendTroop(soldierProxy, playerProxy.getSettingAutoAddDefendList, teams, deathMap)
                  if (refurce) {
                    val troop: PlayerTroop = formationProxy.refurceDefendTeam(teams, playerProxy.getPlayerId)
                    sp.setDefendTroop(troop)
                    sp.setRefDefendTroop(true)
                  }
                }*/
        player.save()
        tellService(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(sp))
        //推送到排行服务
        if (honner != 0) {
          tellService(ActorDefine.POWERRANKS_SERVICE_NAME, AddPlayerToRank(player.getId, player.getHonour, PowerRanksDefine.POWERRANK_TYPE_HONOR))
        }
      }
    } else {*/
      if (simplePlayer.online == true) {
        //在线玩家通知到具体模块执行扣除佣兵、刷新阵型
        if (teams != null && teams.size() > 0 && teams.get(0).playerId > 0 && teams.get(0).playerId != simplePlayer.getId) {
          tellMsgToPlayerModule(simplePlayer.getAccountName, DefendBuildResult(new util.ArrayList[PlayerTeam](), rewardMap, reduce, hon))
        }else{
          tellMsgToPlayerModule(simplePlayer.getAccountName, DefendBuildResult(teams, rewardMap, reduce, hon))
        }
      } else {
        //离线玩家从数据库获取出来扣除
        val player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        if (reduce > 0) {
          //        playerProxy.reducePowerValue(PlayerPowerDefine.POWER_boom,reduce,LogDefine.LOST_WORLD_BE_ATTACK)
          //需要增加日志处理
          playerProxy.setSimplePlayer(simplePlayer)
          player.setBoom(player.getBoom - reduce)
          if (player.getBoom < 0) {
            player.setBoom(0l)
          }
          player.setHonour(player.getHonour + hon)
          if (player.getHonour < 0) {
            player.setHonour(0l)
          }
          playerProxy.refreshBoomLevel()
        }
        playerProxy.reduceOffLinePowerValue(rewardMap)
        val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)

        /*if (teams != null) {
          val dungeoProxy: DungeoProxy = new DungeoProxy(player.getDungeoSet,areaKey)
          val soldierProxy: SoldierProxy = new SoldierProxy(player.getSoldierSet,areaKey)
          val deathMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
          reduceDeadSoldier(teams, BattleDefine.BATTLE_TYPE_WORLD_DEFEND, deathMap, soldierProxy,player)
          val formationProxy: FormationProxy = new FormationProxy(player.getFormationMember1Set, player.getFormationMember2Set, player.getFormationMember3Set,areaKey)
          val refurce: Boolean = formationProxy.checkDefendTroop(soldierProxy, playerProxy.getSettingAutoAddDefendList, teams, deathMap)
          if (refurce) {
            val troop: PlayerTroop = formationProxy.refurceDefendTeam(teams, playerProxy.getPlayerId)
            sp.setDefendTroop(troop)
            sp.setRefDefendTroop(true)
          }
        }*/
        player.save()
        tellService(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(sp))
        //推送到排行服务
        if (honner != 0) {
          tellService(ActorDefine.POWERRANKS_SERVICE_NAME, AddPlayerToRank(player.getId, player.getHonour, PowerRanksDefine.POWERRANK_TYPE_HONOR))
        }
     // }
    }

    //写入行为日志
    val sb: StringBuffer = new StringBuffer
    import scala.collection.JavaConversions._
    var playerId = 0l
    if (teams != null) {
      for (team <- teams) {
        val typeId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
        val num: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
        playerId = team.playerId
        if (num > 0) {
          sb.append(typeId)
          sb.append(",")
          sb.append(num)
          sb.append("&")
        }
      }
    }
    sb.append("$")
    sb.append(result)
    var id = 0l
    if (tileType == TileType.Resource) {
      id = -tile.resPointId
    } else {
      id = defendSimplePlayer.getId
    }
    writeFunctionLog(defendSimplePlayer, FunctionIdDefine.BE_ATTACK_WORLD_TILE_INFO_FUNCTION_ID, x.toLong, y.toLong, id, sb.toString)

    reduce

  }

  //计算荣誉
  def getHonner(attackTeams: util.List[PlayerTeam]): Int = {
    var totalHonner: java.lang.Double = 0.0
    if (tileType != TileType.Building) {
      return totalHonner.toInt
    }
    for (team: PlayerTeam <- attackTeams) {
      val nowNum: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      val baseNum: Int = team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      if (baseNum > nowNum) {
        val typeId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
        val config = ConfigDataProxy.getConfigInfoFindById(DataDefine.ARM_KINDS, typeId)
        val honner: java.lang.Double = (baseNum - nowNum) * 1.0 / config.getInt("honorlose")
        totalHonner = totalHonner + honner
      }
    }
    val res: Int = Math.ceil(totalHonner).toInt
    res
  }

  def getRewardmap(defendTitle: WorldTile, simplePlayer: SimplePlayer, load: Long): util.HashMap[Integer, Integer] = {
    var resMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]()
    var tael = (simplePlayer.getTael - simplePlayer.getProtectNum) * 0.1
    if (tael < 0) {
      tael = 0
    }

    var iron = (simplePlayer.getIron - simplePlayer.getProtectNum) * 0.1
    if (iron < 0) {
      iron = 0
    }

    var wood = (simplePlayer.getWood - simplePlayer.getProtectNum) * 0.1
    if (wood < 0) {
      wood = 0
    }

    var stones = (simplePlayer.getStones - simplePlayer.getProtectNum) * 0.1
    if (stones < 0) {
      stones = 0
    }

    var food = (simplePlayer.getFood - simplePlayer.getProtectNum) * 0.1
    if (food < 0) {
      food = 0
    }

    val total: Double = food + stones + wood + iron + tael
    var getPercent: Double = 1.0
    if (load < total) {
      getPercent = load / total
    }
    resMap.put(POWER_tael, (tael * getPercent).toInt)
    resMap.put(POWER_food, (food * getPercent).toInt)
    resMap.put(POWER_stones, (stones * getPercent).toInt)
    resMap.put(POWER_wood, (wood * getPercent).toInt)
    resMap.put(POWER_iron, (iron * getPercent).toInt)
    resMap
  }


  /** **打包结果包 ****/
  def getEndBattleResult(battle: PlayerBattle, defendTile: WorldTile, rewardMap: util.HashMap[Integer, Integer], builder: M5.Battle.Builder, team: Team, attBoomAdd: Int, defBoomReduce: Int): TileBattleResult = {
    val result: TileBattleResult = new TileBattleResult
    if (battle != null) {
      result.attackSortId = battle.infoType
      result.attackX = battle.x
      result.attackY = battle.y
      result.defendTeam = battle.monsterList
      result.attackTeam = battle.soldierList
      result.defendId = battle.defendId
      result.attackId = battle.attackId
      result.powerMap = battle.powerMap
      val defendFirstHand = GameUtils.countFirstHandValue(battle.monsterList)
      val attackFirstHand = GameUtils.countFirstHandValue(battle.soldierList)
      if (attackFirstHand <= defendFirstHand) {
        result.firstHandle = 1
      }
      if (tile.building == null && tile.defendPlayerId <= 0) {
        result.firstHandle = 0
      }
      if (battle.monsterList.size() == 0) {
        result.firstHandle = 0
      }
      if (battle.battleResult == true) {
        result.winner = 2
      } else {
        result.winner = 1
      }
    } else {
      result.attackSortId = team.attackSortId
      result.attackX = team.attackX
      result.attackY = team.attackY
      result.attackTeam = team.fightTeams
      result.defendId = defendTile.building.getPlayerId
      result.attackId = team.attackId
      result.powerMap = team.powerMap
      result.winner = 2
    }

    if (occupyPlayerId > 0) {
      result.defendId = occupyPlayerId
    }
    result.defendSortId = sortId
    result.defendTileType = tileType
    result.defendX = x
    result.defendY = y
    result.rewardMap = rewardMap
    result.attBoomAdd = attBoomAdd
    result.defBoomReduce = defBoomReduce
    result.battleBuilder = builder
    if (result.attackId == 0) {
      print("id为0的又出现啦！！")
    }
    result
  }

  /** ******生成战报 ********/
  def createBattleReport(result: TileBattleResult, tile: WorldTile) = {
    //生成发送战报
    var attackName = ""
    var defendName = ""
    if (result.defendId > 0) {
      defendName = defendSimplePlayer.getName
      attackName = attackSimplePlayer.getName
    } else {
      attackName = attackSimplePlayer.getName
      defendName = ConfigDataProxy.getConfigInfoFindById(DataDefine.MONSTER_GROUP, -result.defendId).getString("name")
    }
    var battleMess: GeneratedMessage = null
    if (result.battleBuilder != null) {
      val mess: M5.M50000.S2C.Builder = M5.M50000.S2C.newBuilder()
      mess.setBattle(result.battleBuilder)
      mess.setRc(3)
      mess.setWaitTime(0)
      battleMess = mess.build()
    }
    val report: ReportTemplate = new ReportTemplate(result.attackId, attackName, result.defendId, defendName, ReportDefine.REPORT_TYPE_ATTACK, battleMess)
    //填补完整战报
    report.setFirstHand(result.firstHandle)
    if (tileType == TileType.Resource) {
      val config = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
      report.setName(config.getString("name"))
      report.setLevel(config.getInt("level"))
      report.setAttackLevel(config.getInt("level"))
      report.setResourceMapId(tile.resPointId)
      if (result.defendId > 0) {
        report.setResourceGet((((GameUtils.getServerDate().getTime - occupyTime) / 1000 * config.getInt("product")) * getDigAdd()).toInt)
        report.defentIcon = GameUtils.getCityIcon(defendSimplePlayer.getBoomLevel)
        /* if(defendSimplePlayer.getFacadeendTime>GameUtils.getServerDate().getTime){
           report.defentIcon=defendSimplePlayer.getFaceIcon
         }*/
        report.setAim(defendSimplePlayer.getName)
        report.setAttackLevel(defendSimplePlayer.getLevel)
        report.setGarrisonName(defendName)
        report.setAim(defendName)
        report.setDefendVip(defendSimplePlayer.getVipLevel)
        report.setAttackVip(attackSimplePlayer.getVipLevel)
      } else {
        report.setAim(config.getString("name"))
        report.setResourceGet(0)
        defendName = config.getString("name")
      }
      report.defentIcon = config.getInt("icon")
    } else {
      report.setAim(defendName)
      //   report.setGarrisonName(defendName)
      onGetReportInfo(report)
      report.setName(defendName)
      report.setLevel(defendSimplePlayer.getLevel)
      report.setAttackLevel(attackSimplePlayer.getLevel)
      report.setDefentAddBoom(-result.defBoomReduce)
      report.setDefentCurrBoom(defendSimplePlayer.getBoom.toInt)
      report.setDefentTotalBoom(defendSimplePlayer.getBoomUpLimit.toInt)
      report.setDefentIcon(GameUtils.getCityIcon(defendSimplePlayer.getBoomLevel))
      if (defendSimplePlayer.getFacadeendTime > GameUtils.getServerDate().getTime) {
        report.defentIcon = defendSimplePlayer.getFaceIcon
      }
      report.setDefendVip(defendSimplePlayer.getVipLevel)
      report.setAttackVip(attackSimplePlayer.getVipLevel)
    }
    if (result.winner == 2) {
      report.setResult(0)
      report.setHonner(result.honner)
    } else {
      report.setResult(1)
      report.setHonner(-result.honner)
    }
    report.setX(x)
    report.setY(y)
    report.setDefendX(x)
    report.setDefendY(y)
    report.setAttackX(result.attackX)
    report.setAttackY(result.attackY)
    var totalResoult = 0
    if (result.rewardMap != null && result.rewardMap.get(POWER_tael) != null) {
      val add = result.rewardMap.get(POWER_tael)
      report.posResource.add(add)
      totalResoult += add
    } else {
      report.posResource.add(0)
    }

    if (result.rewardMap != null && result.rewardMap.get(POWER_iron) != null) {
      val add = result.rewardMap.get(POWER_iron)
      report.posResource.add(add)
      totalResoult += add
    } else {
      report.posResource.add(0)
    }

    if (result.rewardMap != null && result.rewardMap.get(POWER_stones) != null) {
      val add = result.rewardMap.get(POWER_stones)
      report.posResource.add(add)
      totalResoult += add
    } else {
      report.posResource.add(0)
    }

    if (result.rewardMap != null && result.rewardMap.get(POWER_wood) != null) {
      val add = result.rewardMap.get(POWER_wood)
      report.posResource.add(add)
      totalResoult += add
    } else {
      report.posResource.add(0)
    }

    if (result.rewardMap != null && result.rewardMap.get(POWER_food) != null) {
      val add = result.rewardMap.get(POWER_food)
      report.posResource.add(add)
      totalResoult += add
    } else {
      report.posResource.add(0)
    }
    report.setTotalResourceNum(totalResoult)

    //获得战损的部队
    val defendLost: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
    val attackLost: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
    val defendLostIds = new util.ArrayList[Integer]
    val attackLostIds = new util.ArrayList[Integer]
    for (team: PlayerTeam <- result.defendTeam) {
      val typeId = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      val num = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      val _num = team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      var lostNum = _num - num
      if (lostNum > 0) {
        if (defendLost.containsKey(typeId)) {
          lostNum = lostNum + defendLost.get(typeId)
        } else {
          defendLostIds.add(typeId)
        }
        defendLost.put(typeId, lostNum)
      }
    }

    for (team: PlayerTeam <- result.attackTeam) {
      val typeId = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      val num = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      val _num = team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      var lostNum = _num - num
      if (lostNum > 0) {
        if (attackLost.containsKey(typeId)) {
          lostNum = lostNum + attackLost.get(typeId)
        } else {
          attackLostIds.add(typeId)
        }
        attackLost.put(typeId, lostNum)
      }
    }
    report.defendSoldierTypeIds = new util.ArrayList[Integer]
    report.defendSoldierNums = new util.ArrayList[Integer]
    for (typeId: Integer <- defendLostIds) {
      report.defendSoldierTypeIds.add(typeId)
      report.defendSoldierNums.add(defendLost.get(typeId))
    }
    report.attackSoldierTypeIds = new util.ArrayList[Integer]
    report.attackSoldierNums = new util.ArrayList[Integer]
    for (typeId: Integer <- attackLostIds) {
      report.attackSoldierTypeIds.add(typeId)
      report.attackSoldierNums.add(attackLost.get(typeId))
    }
    report.setDefendName(defendName)
    report.setAttackVip(attackSimplePlayer.getVipLevel)
    report.setAttackLegion(attackSimplePlayer.getLegionName)
    val attackBoomConfig = GameUtils.getBoomConfig(attackSimplePlayer.getBoom.toInt)
    report.setAttackCityIcon(attackBoomConfig.getInt("BaseLook"))
    //计算繁荣度
    report.setAttackAddBoom(result.attBoomAdd)
    report.setAttackCurrBoom(attackSimplePlayer.getBoom.toInt)
    report.setAttackTotalBoom(attackSimplePlayer.getBoomUpLimit.toInt)

    if (result.rewardInfo.size() > 0) {
      val buff: StringBuffer = new StringBuffer
      for (info: RewardInfo <- result.rewardInfo) {
        buff.append(info.getPower)
        buff.append(",")
        buff.append(info.getTypeid)
        buff.append(",")
        buff.append(info.getNum)
        buff.append("&")
      }
      report.setReward(buff.toString)
    }

    if (defendSimplePlayer != null) {
      report.setDefendVip(defendSimplePlayer.getVipLevel)
      report.setDefendLegion(defendSimplePlayer.getLegionName)
    }
    if (result.battleBuilder != null) {
      tellService(ActorDefine.BATTLE_REPORT_SERVICE_NAME, AddMailBattleProto(report, "80001"))
    } else {
      tellService(ActorDefine.MAIL_SERVICE_NAME, SendReport(report, 0))
    }
  }


  def sendResultHandle(fightResult: Boolean, result: TileBattleResult, tile: WorldTile) = {

    if (tileType == TileType.Resource) {
      if (fightResult == true) {
        val team: Team = new Team
        team.attackSortId = result.attackSortId
        team.attackX = result.attackX
        team.attackY = result.attackY
        team.attackId = result.attackId
        team.fightTeams = result.attackTeam
        team.powerMap = result.powerMap
        //给防守队伍加1点先手值
        import scala.collection.JavaConversions._
        var break: Boolean = false
        for (pt: PlayerTeam <- team.fightTeams) {
          val num: Int = pt.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
          if (num > 0 && break == false) {
            val initiative: Integer = pt.getValue(SoldierDefine.POWER_initiative).asInstanceOf[Int] + 1
            pt.powerMap.put(SoldierDefine.POWER_initiative, initiative)
            pt.basePowerMap.put(SoldierDefine.POWER_initiative, initiative)
            break = true
          }
        }
        tile.defendTeams_(team.fightTeams)
        occupyPlayerId = result.attackId
        if (occupyTime == 0) {
          //开始挖掘
          occupyTime = GameUtils.getServerDate().getTime
        }
        occupyLoads = countLoad(result.attackTeam, result.powerMap)
        tile.defendPlayerId_(occupyPlayerId)
        sendDiggingTaskToPlayer(team, tile)
        //打包获得的奖励到result
        if (result.defendId <= 0) {
          //打怪物的才有
          val rewardProxy: RewardProxy = new RewardProxy(areaKey)
          val config = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
          var groupId = config.getInt("trophy")
          if(team.powerMap.get(ActivityDefine.ACTIVITY_CONDITION_FIGHT_WORLD_REWARD_RATE)!=null&&team.powerMap.get(ActivityDefine.ACTIVITY_CONDITION_FIGHT_WORLD_REWARD_RATE)!=0l){
            groupId=config.getInt("activetrophy");
          }
          //TODO 玩家身上有可能会有活动影响这个组id，去simplePlayer拿吧
          val playerReward: PlayerReward = new PlayerReward
          var addexp = config.getInt("exp")
          addexp = addexp * (1 + team.powerMap.get(NOR_POWER_resexprate).toInt / 100)
          playerReward.addPowerMap.put(POWER_exp, config.getInt("exp"))
          rewardProxy.getPlayerRewardByRandFullContent(groupId, playerReward)
          val rewardInfoList: util.List[Common.RewardInfo] = new util.ArrayList[Common.RewardInfo]
          rewardProxy.getRewardInfoByReward(playerReward, rewardInfoList)
          result.rewardInfo = rewardInfoList
          result.reward = playerReward
        }
        team.result = result
        digingTeam = team
      }
    }

    if (fightResult == true) {
      //赢了的话就将结果包告诉上层，通知玩家节点产生一个返回队伍
      context.parent ! BuildBattleResult(result)
    } else {
      tellMsgToPlayerModule(attackSimplePlayer.getAccountName, RefTaskListNotify())
    }
    createBattleReport(result, tile)
    if (tileType == TileType.Resource) {
      if (digingTeam != null) {
        //把当前的状态置为原始标志
        for (teams: PlayerTeam <- digingTeam.fightTeams) {
          teams.basePowerMap.putAll(teams.powerMap)
        }
      }
    }
  }

  def defendResourceFailHandle(isWin: Boolean, attackId: Long): Unit = {
    //需要实时知道是不是在线，所以需要去拿一下simplePlayer
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(digingTeam.attackId, areaKey)
    val accountName = simplePlayer.getAccountName
    occupyLoadstemp = occupyLoads
    //重新刷一下载重
    occupyLoads = countLoad(digingTeam.fightTeams, digingTeam.powerMap)
    if (simplePlayer.online) {
      tellMsgToPlayerModule(accountName, DefendResourceFight(x, y, digingTeam.fightTeams, isWin, occupyLoads))
    } else {
      //给离线玩家增加
      //赢了就只做增加伤兵，输了需要删除掉对应的任务部队
      val player: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val dungeoProxy: DungeoProxy = new DungeoProxy(player.getDungeoSet, areaKey)
      val soldierProxy: SoldierProxy = new SoldierProxy(player.getSoldierSet, areaKey)
      val deathMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
      reduceDeadSoldier(digingTeam.fightTeams, BattleDefine.BATTLE_TYPE_WORLD, deathMap, soldierProxy, player)
      if (isWin == false) {
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        performTasksProxy.deleteDiggingTask(x, y, playerProxy)
        performTasksProxy.savePerformTasks()
      }
      soldierProxy.saveSoldier()
      soldierProxy.finalize()
      dungeoProxy.finalize()
    }

    //写入行为日志
    writeFunctionLog(simplePlayer, FunctionIdDefine.RESOURCE_BE_ATTACK_FUNCTION_ID, x.toLong, y.toLong, tile.resPointId, attackId + "," + isWin)
  }

  def onEndBattleHandle(battle: PlayerBattle): Unit = {
    battleBuilder.addAllRounds(battle._roundDataList)
    battleBuilder.setId(battle.id)
    battleBuilder.setType(battle.`type`)

    val totalLoad = countLoad(battle.soldierList, battle.powerMap)
    var rewardMap: util.HashMap[Integer, Integer] = null
    val honner = getHonner(battle.soldierList)
    val  soldierList:util.List[PlayerTeam] =new util.ArrayList[PlayerTeam]()
    for(tem <- battle.soldierList ){
      val newTeam:PlayerTeam=new PlayerTeam(tem.basePowerMap, tem.playerId)
      newTeam.powerMap= new util.HashMap[Integer, AnyRef](tem.powerMap)
      newTeam.reset=tem.reset
      newTeam.basePowerMap=new util.HashMap[Integer, AnyRef](tem.basePowerMap)
      newTeam.capacityMap=tem.capacityMap
      soldierList.add(newTeam)
    }
    val attBoomAdd = attackHandle(battle.x, battle.y, battle.infoType, soldierList, battle.battleResult, honner)
    var defBoomReduce = 0
    val now = GameUtils.getServerDate().getTime
    if (tile.tileType == TileType.Building) {
      rewardMap = getRewardmap(tile, defendSimplePlayer, totalLoad)
      defendSimplePlayer = PlayerService.getSimplePlayer(defendSimplePlayer.getId, areaKey)
      defBoomReduce = defendHandle(battle.monsterList, !battle.battleResult, tile, defendSimplePlayer, rewardMap, honner)
      var endresult: Int = 0
      if (battle.battleResult == false) {
        endresult = 1
      }
      if (defendSimplePlayer.getArmygrouid > 0) {
        val situation: Situation = new Situation(defendSimplePlayer.getArmygrouid, defendSimplePlayer.getId, attackSimplePlayer.getId, GameUtils.getServerDate().getTime, endresult, getallLost(rewardMap), ArmyGroupDefine.SITUATION_ARMY, 0)
        tellArmyNode(defendSimplePlayer.getArmygrouid, addSituation(situation))
      }
    } else {
      if (occupyPlayerId > 0) {
        //先把战斗后的队伍覆盖一下再说
        digingTeam.fightTeams = battle.monsterList
        tile.defendTeams_(battle.monsterList)
        // 防守是有人的话要把自己占领资源点的伤兵返回回去，并且通知删除掉这个定时队伍
        defendResourceFailHandle(!battle.battleResult, attackSimplePlayer.getId)
        //把防守矿点玩家的资源夺过来先
        val digTime = now - occupyTime
        val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
        var product: Double = (pointConfig.getInt("product") * digTime / 1000).toInt
        val rduce = (1 + (digingTeam.powerMap.get(NOR_POWER_rescollectrate) / UtilDefine.RUN_UNDER))
        product = product * rduce
        if (product >= occupyLoadstemp) {
          product = occupyLoadstemp
        }
        if (rewardMap == null) {
          rewardMap = new util.HashMap[Integer, Integer]()
        }
        if (battle.battleResult == true) {
          rewardMap.put(pointConfig.getInt("restype"), product.toInt)
        }
      }
      if (tile.tileType == TileType.Resource && battle.battleResult && battle.attackId > 0) {
        val helpsimple: SimplePlayer = PlayerService.getSimplePlayer(battle.attackId, areaKey)
        if (helpsimple != null) {
          if (helpsimple.online) {
            tellMsgToPlayerModule(helpsimple.getAccountName, GameMsg.finishResouceTask(tile.resLv))
          } else {
            val player: Player = BaseDbPojo.getOfflineDbPojo(battle.attackId, classOf[Player], areaKey)
            if (player.getWorldResouceLevel < tile.resLv) {
              player.setWorldResouceLevel(tile.resLv)
              val setlist: util.Set[java.lang.Long] = player.getResouceLeve
              setlist.add(tile.resLv.toLong)
              player.setResouceLeve(setlist)
              player.save()
            }
          }
        }

      }
      if (defendSimplePlayer != null) {
        if (defendSimplePlayer.getArmygrouid > 0) {
          var endresult: Int = 0
          if (battle.battleResult == false) {
            endresult = 1
          }
          val situation: Situation = new Situation(defendSimplePlayer.getArmygrouid, defendSimplePlayer.getId, attackSimplePlayer.getId, GameUtils.getServerDate().getTime, endresult, getallLost(rewardMap), ArmyGroupDefine.SITUATION_ARMY, 0)
          tellArmyNode(defendSimplePlayer.getArmygrouid, addSituation(situation))
        }
      }
    }


    val result: TileBattleResult = getEndBattleResult(battle, tile, rewardMap, battleBuilder, null, attBoomAdd, defBoomReduce)
    //结果包处理
    result.honner = honner
    sendResultHandle(battle.battleResult, result, tile)
    if (tile.tileType == TileType.Building) {
      refreshTeams(battle.monsterList, defendSimplePlayer)
    }
    if (battle.`type` == BattleDefine.BATTLE_TYPE_WORLD || battle.`type` == BattleDefine.BATTLE_TYPE_WORLD_DEFEND) {
      if (defendSimplePlayer != null && defendSimplePlayer.getHelpId != 0 && tileType == TileType.Building) {
        val player: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player], areaKey)
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        val defendperformTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
        val task: PerformTasks = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getHelpId, classOf[PerformTasks], areaKey)
        if (task != null) {
          val team: Team = gethelpTeamByTime(task.getTimeer)
          if (team != null) {
            if (battle.battleResult == false) {
              team.fightTeams = battle.monsterList
              //通知防守者和驻军者 军队改变
              val helpsimple: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
              if (helpsimple.online) {
                tellMsgToPlayerModule(helpsimple.getAccountName, GameMsg.changefightTeam(task.getTimeer, battle.monsterList))
              } else {
                val helpplayer: Player = BaseDbPojo.getOfflineDbPojo(team.attackId, classOf[Player], areaKey)
                val helpperformTasksProxy: PerformTasksProxy = new PerformTasksProxy(helpplayer.getPerformTaskSet, helpplayer.getTeamNoticeSet, areaKey)
                val helpdungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
                val helpplayerProxy: PlayerProxy = new PlayerProxy(helpplayer, areaKey)
                helpperformTasksProxy.checkDefendTroop(helpdungeoProxy, battle.monsterList, task.getTimeer)
              }
              if (defendSimplePlayer.online) {
                tellMsgToPlayerModule(defendSimplePlayer.getAccountName, GameMsg.changefightTeam(task.getTimeer, battle.monsterList))
              } else {
                val defenddungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
                val defendplayer: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player], areaKey)
                val defendplayerProxy: PlayerProxy = new PlayerProxy(defendplayer, areaKey)
                val defendformTasksProxy: PerformTasksProxy = new PerformTasksProxy(defendplayer.getPerformTaskSet, defendplayer.getTeamNoticeSet, areaKey)
                defendformTasksProxy.checkDefendTroop(defenddungeoProxy, battle.monsterList, task.getTimeer)
                // defendperformTasksProxy.deleteDiggingTask(task.getTimeer, playerProxy)
                //  defendperformTasksProxy.checkDefendTroop(defenddungeoProxy, battle.monsterList, task.getTimeer)
              }
            } else {
              //防守输了，删除数据
              val helpsimple: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
              if (helpsimple.online) {
                tellMsgToPlayerModule(helpsimple.getAccountName, GameMsg.DelformTask(task.getWorldTileX, task.getWorldTileY, team.fightTime))
              } else {
                val helpplayer: Player = BaseDbPojo.getOfflineDbPojo(team.attackId, classOf[Player], areaKey)
                val helpperformTasksProxy: PerformTasksProxy = new PerformTasksProxy(helpplayer.getPerformTaskSet, helpplayer.getTeamNoticeSet, areaKey)
                val helpdungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
                val helpplayerProxy: PlayerProxy = new PlayerProxy(helpplayer, areaKey)
                helpperformTasksProxy.deleteFormTask(team.fightTime, helpplayerProxy)
                helpplayer.save()
              }
              if (defendSimplePlayer.online) {
                tellMsgToPlayerModule(defendSimplePlayer.getAccountName, GameMsg.DelformTask(task.getWorldTileX, task.getWorldTileY, team.fightTime))
             //   tellMsgToPlayerModule(defendSimplePlayer.getAccountName, GameMsg.Delhelper(0))
              } else {
                player.setUsedefine(0l)
                player.save();
                val defenddungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
                defendperformTasksProxy.deleteFormTask(team.fightTime, playerProxy)
              }
              helplist.remove(team)
            }
          }
        }
      }
    }
    isStartBattle = false
    //处理完一场马上触发下一场的判断
    onServerTrigger()
  }

  def refreshTeams(teams: util.List[PlayerTeam], simplePlayer: SimplePlayer): Unit = {
    if (teams != null && teams.size() > 0 && teams.get(0).playerId > 0 && teams.get(0).playerId != simplePlayer.getId) {
      val helpSimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(teams.get(0).playerId, areaKey)
      if (helpSimplePlayer == null) {
        return 0
      }
      if (helpSimplePlayer.online == true) {
        return
      } else {
        val player = BaseDbPojo.getOfflineDbPojo(helpSimplePlayer.getId, classOf[Player], areaKey)
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        //TODO 需要增加日志处理
        playerProxy.setSimplePlayer(helpSimplePlayer)
        val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, helpSimplePlayer)
        if (teams != null) {
          val dungeoProxy: DungeoProxy = new DungeoProxy(player.getDungeoSet, areaKey)
          val soldierProxy: SoldierProxy = new SoldierProxy(player.getSoldierSet, areaKey)
          val deathMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
          reduceDeadSoldier(teams, BattleDefine.BATTLE_TYPE_WORLD, deathMap, soldierProxy, player)
          val formationProxy: FormationProxy = new FormationProxy(player.getFormationMember1Set, player.getFormationMember2Set, player.getFormationMember3Set, areaKey)
          val refurce: Boolean = formationProxy.checkDefendTroop(soldierProxy, playerProxy.getSettingAutoAddDefendList, teams, deathMap)
          for (team <- teams) {
            team.basePowerMap = new util.HashMap[Integer, AnyRef](team.powerMap);
          }
        }
      }
    } else {
      if (simplePlayer.online) {
        return;
      }
      val player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      if (teams != null) {
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
        val dungeoProxy: DungeoProxy = new DungeoProxy(player.getDungeoSet, areaKey)
        val soldierProxy: SoldierProxy = new SoldierProxy(player.getSoldierSet, areaKey)
        val deathMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
        reduceDeadSoldier(teams, BattleDefine.BATTLE_TYPE_WORLD_DEFEND, deathMap, soldierProxy, player)
        val formationProxy: FormationProxy = new FormationProxy(player.getFormationMember1Set, player.getFormationMember2Set, player.getFormationMember3Set, areaKey)
        val refurce: Boolean = formationProxy.checkDefendTroop(soldierProxy, playerProxy.getSettingAutoAddDefendList, teams, deathMap)
        if (refurce) {
          val troop: PlayerTroop = formationProxy.refurceDefendTeam(teams, playerProxy.getPlayerId)
          sp.setDefendTroop(troop)
          sp.setRefDefendTroop(true)
          tellService(ActorDefine.PLAYER_SERVICE_NAME, GameMsg.UpdateSimplePlayerDefendTroop(player.getId, troop))
        }
        for (team <- teams) {
          team.basePowerMap = new util.HashMap[Integer, AnyRef](team.powerMap);
        }
      }
      player.save()
    }
  }


  def getNodeResourceAlreadyGet(): Int = {
    var alreadyGet: Double = 0l
    if (occupyTime > 0) {
      val digTime = GameUtils.getServerDate().getTime - occupyTime
      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
      alreadyGet = pointConfig.getInt("product") * digTime / 1000
      alreadyGet = alreadyGet * getDigAdd()
      if (alreadyGet > occupyLoads) {
        alreadyGet = occupyLoads
      }
    }
    alreadyGet.toInt
  }

  def getDigAdd(): Double = {
    return 1 + (digingTeam.powerMap.get(NOR_POWER_rescollectrate) / 100)
  }


  def onGetReportInfo(report: ReportTemplate): Unit = {
    if (tile.tileType == TileType.Resource) {
      report.setResourceGet(getNodeResourceAlreadyGet())
    } else {
      defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
      //      val player: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player],areaKey)
      //      val playerProxy: PlayerProxy = new PlayerProxy(player,areaKey)
      //      val defendperformTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet,areaKey)
      val task: PerformTasks = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getHelpId, classOf[PerformTasks], areaKey)
      if (task != null) {
        val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
        var defendTypeIdList = new util.ArrayList[Integer]
        var defendNumList = new util.ArrayList[Integer]
        val team: Team = gethelpTeamByTime(task.getTimeer)
        if (team != null) {
          val helpsimp: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
          defendTypeIdList = dungeoProxy.getSoldierTypeIdListFormPlayerTeam(team.fightTeams)
          defendNumList = dungeoProxy.getSoldierNumListFormPlayerTeam(team.fightTeams)
          report.setDefendSoldierTypeIds(defendTypeIdList)
          report.setDefendSoldierNums(defendNumList)
          report.setGarrisonName(helpsimp.getName)
          report.setDefendName(helpsimp.getName)
          report.setGarrisonid(helpsimp.getId)
        }
      }
    }

  }

  def onGetDefendTeam(): Unit = {
    if (defendSimplePlayer == null) {
      defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
    }
    val player: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player], areaKey)
    val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
    val defendperformTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
    val task: PerformTasks = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getHelpId, classOf[PerformTasks], areaKey)
    if (task != null) {
      val team: Team = gethelpTeamByTime(task.getTimeer)
      if (team != null) {
        sender() ! Some(team.fightTeams)
      } else {
        sender() ! Some(new util.ArrayList[PlayerTeam]())
      }
    }
    sender() ! Some(new util.ArrayList[PlayerTeam]())
  }

  def onPushWorldFightToNode(attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, fightTime: Long, attackSort: Int, powerMap: util.Map[Integer, java.lang.Long]): Unit = {
    fightList.add(createTeam(attackTeams, attackX, attackY, fightTime, attackSort, powerMap))


    //写入行为日志
    val sb: StringBuffer = new StringBuffer
    import scala.collection.JavaConversions._
    var playerId = 0l
    for (team <- attackTeams) {
      val typeId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      val num: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      playerId = team.playerId
      sb.append(typeId)
      sb.append(",")
      sb.append(num)
      sb.append("&")
    }
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId, areaKey)
    var id = 0l
    if (tileType == TileType.Resource) {
      id = -tile.resPointId
    } else if (tileType == TileType.Building) {
      id = tile.building.getPlayerId
    }
    writeFunctionLog(simplePlayer, FunctionIdDefine.FIGHT_WORLD_MAP_FUNCTION_ID, x.toLong, y.toLong, id, sb.toString)
  }

  override def receive: Receive = {
    case PushWorldFightToNode(attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, fightTime: Long, attackSort: Int, powerMap: util.Map[Integer, java.lang.Long]) =>
      onPushWorldFightToNode(attackTeams, attackX, attackY, fightTime, attackSort, powerMap)
    case OnServerTrigger() =>
      onServerTrigger()
    case GameMsg.PackPuppet(puppet: M5.PuppetAttr) =>
      onPackPuppet(puppet)
    case ServerBattleEndHandle(battle: PlayerBattle) =>
      onEndBattleHandle(battle)
    case FightTeamBack(attackTeams: util.List[PlayerTeam], getBackTime: Long, targetX: Int, targetY: Int, targetSortId: Int, result: TileBattleResult) =>
      onFightTeamBack(attackTeams, getBackTime, targetX, targetY, targetSortId, result)
    case AskNodeResourceGet() =>
      getNodeResourceAlreadyGet()
    case AskNodeDefendGet() =>
      onGetDefendTeam()
    case GetReportInfo(report: ReportTemplate) =>
      onGetReportInfo(report)
      tellService(ActorDefine.MAIL_SERVICE_NAME, SendReport(report, 0))
    case CallBackTask(x: Int, y: Int, playerId: Long, time: Long, taskType: Int, product: Long) =>
      onCallBackTask(x, y, playerId, time, taskType, product)
    case PushHelpToNode(attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, fightTime: Long, attackSort: Int, powerMap: util.Map[Integer, java.lang.Long]) =>
      onPushHelpToNode(attackTeams, attackX, attackY, fightTime, attackSort, powerMap)
    case saveDateBeforeStop() =>
    //      savaDate()
    case CheckDeleteDiggingTask(playerId : Long) =>
      println("=============开始判断玩家的挖掘队伍是否存在误删除")
      if(occupyPlayerId == playerId){
        println("！！！！！！！！！！发现玩家删除了不该删除的挖掘队伍啦，执行加回逻辑")
        CustomerLogger.error("发现玩家删除了不该删除的挖掘队伍啦，执行加回逻辑")
        fixTaskBackToPlayer(playerId)
      }else{
        println("=============并没有玩家的挖掘队伍存在误删除")
      }
    case _ =>
  }

  def fixTaskBackToPlayer(playerId : Long): Unit ={
    val digPlayer = PlayerService.getSimplePlayer(playerId,areaKey)
    if (digPlayer != null){
      writeErrorLog(digPlayer)
//      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
//      val name = pointConfig.getString("name")
//      // level = pointConfig.getInt("level")
//      val level = -1
//      var product: Double = pointConfig.getInt("product")
//      product = (1 + (digingTeam.powerMap.get(NOR_POWER_rescollectrate) / 100.0)) * product
//      if(digPlayer.online == true){
//        val accountName = digPlayer.getAccountName
//        tellMsgToPlayerModule(accountName, CreateDiggingTask(x, y, digingTeam.fightTeams, occupyLoads, name, level, product.toInt, occupyTime, digPlayer.getX, digPlayer.getY))
//      } else {
//        val player: Player = BaseDbPojo.getOfflineDbPojo(digPlayer.getId, classOf[Player], areaKey)
//        val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
//        val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
//        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
//        performTasksProxy.addPerformTaskForOffLine(TaskDefine.PERFORM_TASK_DIGGING, name, level, x, y,
//          occupyLoads, digingTeam.fightTeams, dungeoProxy, product.toInt, digPlayer.getId,
//          playerProxy, 0, digPlayer.getX, digPlayer.getY, digingTeam.powerMap)
//      }
    }
  }

  def onPushHelpToNode(attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, fightTime: Long, attackSort: Int, powerMap: util.Map[Integer, java.lang.Long]) {
    helplist.add(createTeam(attackTeams, attackX, attackY, fightTime, attackSort, powerMap))
    // onFightTeamBack(attackTeams,fightTime,attackX,attackY,attackSort,new TileBattleResult)
  }

  //加速队伍时间
  def onCallBackTask(x: Int, y: Int, playerId: Long, time: Long, taskType: Int, product: Long): Unit = {
    if (taskType == TaskDefine.PERFORM_TASK_ATTACK) {
      for (team <- fightList) {
        if (team.fightTime == time && playerId == team.attackId) {
          //将其提前
          team.fightTime = 0
          //通知到防守玩家
          var defendSimplePlayer: SimplePlayer = null
          if (tile.tileType == TileType.Building) {
            defendSimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
          } else if (tile.tileType == TileType.Resource) {
            if (occupyPlayerId > 0) {
              defendSimplePlayer = PlayerService.getSimplePlayer(occupyPlayerId, areaKey)
            }
          }
          if (defendSimplePlayer != null) {
            if (defendSimplePlayer.online == true) {
              tellMsgToPlayerModule(defendSimplePlayer.getAccountName, RemoveBeAttackedNotify(this.x, this.y, time))
            } else {
              val player: Player = BaseDbPojo.getOfflineDbPojo(defendSimplePlayer.getId, classOf[Player], areaKey)
              val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
              val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
              performTasksProxy.removeTeamNotice(this.x, this.y, time, playerProxy)
              performTasksProxy.savePerformTasks()
              player.save()
            }
          }
        }
      }
    } else if (taskType == TaskDefine.PERFORM_TASK_GOHELP) {
      for (team <- helplist) {
        if (team.fightTime == time && playerId == team.attackId) {
          //通知攻击者
          val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(team.attackId, areaKey)
          if (simplePlayer.online) {
            tellMsgToPlayerModule(simplePlayer.getAccountName, changeformTask(x, y, team.fightTime))
          } else {
            val player: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
            val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
            performTasksProxy.changeTaskType(x, y, team.fightTime, TaskDefine.PERFORM_TASK_HELPBACK)
          }
          //改变防守者的任务时间
          val defendsimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
          if (defendsimplePlayer.online) {
            tellMsgToPlayerModule(defendsimplePlayer.getAccountName, tellHasArried(team.fightTime))
          } else {
            val player: Player = BaseDbPojo.getOfflineDbPojo(defendsimplePlayer.getId, classOf[Player], areaKey)
            val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
            performTasksProxy.chanbeginttime(team.fightTime)
          }
          val template: MailTemplate = new MailTemplate("驻军通知", simplePlayer.getName + "派遣了一支部队辅助您驻守基地，请下达命令", 0, defendsimplePlayer.getName, ChatAndMailDefine.MAIL_TYPE_INBOX)
          val allid: util.Set[java.lang.Long] = new util.HashSet[lang.Long]()
          allid.add(defendsimplePlayer.getId)
          tellService(ActorDefine.MAIL_SERVICE_NAME, GameMsg.SendMail(allid, template, "系统邮件", 0l))
        }
      }
    } else if (taskType == TaskDefine.PERFORM_TASK_RETURN) {
      for (team <- backList) {
        if (team.fightTime == time && playerId == team.attackId) {
          //将其提前
          team.fightTime = 0
        }
      }
      sender() ! CallBackTaskBack()
    } else if (taskType == TaskDefine.PERFORM_TASK_HELPBACK) {
      onHelpTeamBack(x, y, time, taskType)
    } else if (taskType == TaskDefine.PERFORM_TASK_OTHERHELPBACK) {
      onHelpTeamBack(x, y, time, taskType)
    } else {
      //挖掘返回处理
      if (occupyPlayerId == playerId) {
        occupyPlayerId.synchronized{
          val now = GameUtils.getServerDate().getTime
          val digTime = now - occupyTime
          val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
          var product: Double = (pointConfig.getInt("product") * digTime / 1000).toInt
          product = (1 + (digingTeam.powerMap.get(NOR_POWER_rescollectrate) / 100.0)) * product
          if (product >= occupyLoads) {
            product = occupyLoads
          }

          //通知玩家可以删除任务部队了
          sender() ! DeleteDiggingTask(x,y)

          digTeamBackToPlayer(pointConfig.getInt("restype"), product.toInt)
          //置回没占领状态
          clearDigState(tile)
        }
      }
    }
  }


  def onFightTeamBack(attackTeams: util.List[PlayerTeam], getBackTime: Long, targetX: Int, targetY: Int, targetSortId: Int, result: TileBattleResult) = {
    val backTeam = new Team
    backTeam.attackSortId = targetSortId
    backTeam.attackX = targetX
    backTeam.attackY = targetY
    backTeam.fightTeams = attackTeams
    backTeam.fightTime = getBackTime
    backTeam.result = result
    backTeam.attackId = result.attackId
    backList.add(backTeam)
    if (backTeam.attackId == 0) {
      print("出现0的id啦！！！！！")
    }
    sendReturnTaskToPlayer(backTeam)
  }

  def sendDiggingTaskToPlayer(digTeam: Team, defendTile: WorldTile): Unit = {
    var name = ""
    var level = 0
    var accountName = ""
    val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, defendTile.resPointId)
    name = pointConfig.getString("name")
    // level = pointConfig.getInt("level")
    level = -1
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(digTeam.attackId, areaKey)
    var product: Double = pointConfig.getInt("product")
    product = (1 + (digTeam.powerMap.get(NOR_POWER_rescollectrate) / 100.0)) * product
    val now = GameUtils.getServerDate().getTime
    val digTime = now - occupyTime
    val hasvalue: Double = (product * digTime / 1000).toInt
    if (hasvalue > occupyLoadstemp && occupyLoadstemp != 0) {
      val needsecond: Int = (occupyLoadstemp / product).toInt
      occupyTime = now - needsecond * 1000
    }
    if (simplePlayer.online) {
      accountName = simplePlayer.getAccountName
      tellMsgToPlayerModule(accountName, CreateDiggingTask(x, y, digTeam.fightTeams, occupyLoads, name, level, product.toInt, occupyTime, simplePlayer.getX, simplePlayer.getY))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      performTasksProxy.addPerformTaskForOffLine(TaskDefine.PERFORM_TASK_DIGGING, name, level, x, y,
        occupyLoads, digTeam.fightTeams, dungeoProxy, product.toInt, simplePlayer.getId,
        playerProxy, 0, simplePlayer.getX, simplePlayer.getY, digTeam.powerMap)
    }
    //写入日志
    val sb: StringBuffer = new StringBuffer
    import scala.collection.JavaConversions._
    for (team <- digTeam.fightTeams) {
      val typeId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      val num: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      sb.append(typeId)
      sb.append(",")
      sb.append(num)
      sb.append("&")
    }
    writeFunctionLog(simplePlayer, FunctionIdDefine.DIG_WORLD_MAP_FUNCTION_ID, x.toLong, y.toLong, tile.resPointId, sb.toString)
  }

  /** 写入行为日志逻辑 **/
  def writeFunctionLog(simplePlayer: SimplePlayer, functionId: Int, expand1: Long, expand2: Long, expand3: Long, expandstr: String): Unit = {
    val log: tbllog_function = new tbllog_function
    log.setAccount_name(simplePlayer.getAccountName)
    log.setRole_id(simplePlayer.getId)
    log.setPlatform(simplePlayer.getPlatform)
    log.setDim_level(simplePlayer.getLevel)
    log.setAction_id(functionId)
    log.setStatus(FunctionIdDefine.ACCOMPLISH)
    val now: Int = GameUtils.getServerTime
    log.setLog_time(now)
    log.setHappend_time(now)
    log.setExpand1(expand1)
    log.setExpand2(expand2)
    log.setExpand3(expand3)
    log.setExpandstr(expandstr)
    sendLog(log)
  }

  def writeErrorLog(simplePlayer: SimplePlayer): Unit = {
    val log: tbllog_error = new tbllog_error
    log.setAccount_name(simplePlayer.getAccountName)
    log.setRole_id(simplePlayer.getId)
    log.setPlatform(simplePlayer.getPlatform)
    log.setError_msg("错误删除了玩家的挖掘队伍")
    log.setDevice("")
    log.setDevice_type("")
    log.setDid("")
    log.setGame_version("")
    log.setMno("")
    log.setNm("")
    log.setOs("")
    log.setOs_version("")
    log.setScreen("")
    val now: Int = GameUtils.getServerTime
    log.setLog_time(now)
    log.setHappend_time(now)

    sendLog(log)
  }

  def getBackTeam(x: Int, y: Int, fighttime: Long): Team = {
    for (tem <- backList) {
      if (tem.attackX == x && tem.attackY == y && tem.fightTime == fighttime) {
        return tem
      }
    }
    return null
  }

  def gethelpTeamByTime(fighttime: Long): Team = {
    for (tem <- helplist) {
      if (tem.fightTime == fighttime) {
        return tem
      }
    }
    return null
  }

  def sendReturnTaskToPlayer(backTeam: Team): Unit = {
    val defendTile = getWorldTitleByPoint(backTeam.attackX, backTeam.attackY, backTeam.attackSortId)
    var name = ""
    var level = 0
    var accountName = ""
    var mysimple: SimplePlayer = null
    if (defendTile.tileType == TileType.Resource) {
      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, defendTile.resPointId)
      name = pointConfig.getString("name")
      // level = pointConfig.getInt("level")
      level = -1
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(backTeam.attackId, areaKey)
      accountName = simplePlayer.getAccountName
      mysimple = simplePlayer
    } else if (defendTile.tileType == TileType.Building) {
      val ids = new util.HashSet[java.lang.Long]()
      ids.add(backTeam.attackId)
      ids.add(defendTile.building.getPlayerId)
      val simplePlayers: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(ids, areaKey)

      for (simplePlayer: SimplePlayer <- simplePlayers) {
        if (simplePlayer.getId == backTeam.attackId) {
          accountName = simplePlayer.getAccountName
          mysimple = simplePlayer
        } else {
          name = simplePlayer.getName
          level = simplePlayer.getLevel
        }
      }
    } else if (defendTile.tileType == TileType.Empty) {
      if (mysimple == null) {
        mysimple = PlayerService.getSimplePlayer(backTeam.attackId, areaKey)
        accountName = mysimple.getAccountName
      }
    }

    if (mysimple != null) {
      if (mysimple.online) {
        tellMsgToPlayerModule(accountName, CreateReturnTask(x, y, backTeam.fightTeams, backTeam.fightTime, name, level, backTeam.attackX, backTeam.attackY))
      } else {
        val player: Player = BaseDbPojo.getOfflineDbPojo(mysimple.getId, classOf[Player], areaKey)
        val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
        val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
        val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[java.lang.Long], areaKey)
        performTasksProxy.addPerformTaskForOffLine(TaskDefine.PERFORM_TASK_RETURN, name, level, x, y,
          backTeam.fightTime, backTeam.fightTeams, dungeoProxy, 0, mysimple.getId, playerProxy, 0,
          backTeam.attackX, backTeam.attackY, backTeam.powerMap)
        player.save()
      }
    }

  }

  //获取某个service的消息值
  def askService[T](serviceName: String, msg: AnyRef) = {
    val ref = context.actorSelection("../../" + serviceName)
    val value: Option[T] = GameUtils.futureAsk(ref, msg, 10)
    val result: T = value.getOrElse(null).asInstanceOf[T]
    result
  }

  //通知到service
  def tellService(serviceName: String, msg: AnyRef) = {
    context.actorSelection("../../" + serviceName) ! msg
  }

  //通知到service
  def tellArmyNode(legionId: Long, msg: AnyRef) = {
    context.actorSelection("../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE + legionId) ! msg
  }

  def tellMsgToPlayerModule(accountName: String, msg: AnyRef): Unit = {
    context.actorSelection("../../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.MAP_MODULE_NAME) ! msg
  }

  def createTeam(attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, fightTime: Long, attackSort: Int, powerMap: util.Map[Integer, java.lang.Long]): Team = {
    val team: Team = new Team
    team.attackX = attackX
    team.attackY = attackY
    team.fightTime = fightTime
    team.fightTeams = attackTeams
    team.attackSortId = attackSort
    team.attackId = attackTeams.get(0).playerId
    team.powerMap = powerMap
    team
  }

  def soldierLog(player: Player, opt: Int, num: Int, typeid: Int, logtype: Int,remain_num :Long): Unit = {
    val cache: PlayerCache = GameUtils.getPlayercace(player)
    val itemslog: tbllog_soldier = new tbllog_soldier
    itemslog.setPlatform(cache.getPlat_name)
    itemslog.setRole_id(player.getId)
    itemslog.setAccount_name(player.getAccountName)
    itemslog.setDim_level(player.getLevel)
    itemslog.setOpt(opt)
    itemslog.setAction_id(logtype)
    itemslog.setType_id(typeid)
    itemslog.setSoldier_number(num.toLong)
    itemslog.setMap_id(0)
    itemslog.setHappend_time(GameUtils.getServerTime)
    itemslog.setRemain_num(remain_num)
    sendLog(itemslog)
  }

  def itemLog(player: Player, opt: Int, num: Int, typeid: Int, logtype: Int): Unit = {
    val cache: PlayerCache = GameUtils.getPlayercace(player)
    val itemslog: tbllog_items = new tbllog_items
    itemslog.setPlatform(cache.getPlat_name)
    itemslog.setRole_id(player.getId)
    itemslog.setAccount_name(player.getAccountName)
    itemslog.setDim_level(player.getLevel)
    itemslog.setOpt(opt)
    itemslog.setAction_id(logtype)
    itemslog.setItem_id(typeid)
    itemslog.setItem_number(num.toLong)
    itemslog.setMap_id(0)
    itemslog.setHappend_time(GameUtils.getServerTime)
    sendLog(itemslog)
  }

  def goldLog(player: Player, `type`: Int, opt: Int, dict_action: Int, amount: Int): Unit = {
    val cache: PlayerCache = GameUtils.getPlayercace(player)
    val goldlog: tbllog_gold = new tbllog_gold
    goldlog.setPlatform(cache.getPlat_name)
    goldlog.setRole_id(player.getId)
    goldlog.setAccount_name(player.getAccountName)
    goldlog.setDim_level(player.getLevel)
    goldlog.setDim_prof(0)
    goldlog.setAction_1(dict_action)
    goldlog.setAmount(amount.toLong)
    goldlog.setMoney_type(`type`)
    var value: Long = 0
    if (`type` == PlayerPowerDefine.POWER_tael) {
      value = player.getTael
    }
    if (`type` == PlayerPowerDefine.POWER_iron) {
      value = player.getIron
    }
    if (`type` == PlayerPowerDefine.POWER_food) {
      value = player.getFood
    }
    if (`type` == PlayerPowerDefine.POWER_wood) {
      value = player.getWood
    }
    if (`type` == PlayerPowerDefine.POWER_stones) {
      value = player.getStones
    }
    if (`type` == PlayerPowerDefine.POWER_gold) {
      value = player.getGold
    }
    goldlog.setMoney_remain(value)
    goldlog.setOpt(opt)
    goldlog.setHappend_time(GameUtils.getServerTime)
    sendLog(goldlog)
  }

  /***给失败的队伍做个佣兵返还****/
  def addAttackFailLeftSoldier(soldiers: util.List[PlayerTeam],soldierProxy: SoldierProxy, player: Player): Unit ={
    for (team <- soldiers) {
      val soldierId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
      val nowNum: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
      if (nowNum > 0){
        soldierProxy.addSoldierNumWithoutBaseNum(soldierId,nowNum,LogDefine.GET_WORLD_ATTACK_FAIL)
        soldierLog(player,1,nowNum,soldierId,LogDefine.GET_WORLD_ATTACK_FAIL,soldierProxy.getSoldierNum(soldierId))
      }
    }
  }

  def reduceDeadSoldier(soldiers: util.List[PlayerTeam], battleType: Int, deadNumMap: util.HashMap[Integer, Integer], soldierProxy: SoldierProxy, player: Player): util.List[Integer] = {
    val ids: util.List[Integer] = new util.ArrayList[Integer]
    val reduceMap: util.HashMap[Integer, Integer] = new util.HashMap[Integer, Integer]
    if (soldiers != null) {
      import scala.collection.JavaConversions._
      for (team <- soldiers) {
        val soldierId: Int = team.getValue(SoldierDefine.NOR_POWER_TYPE_ID).asInstanceOf[Int]
        val totalNum: Int = team.basePowerMap.get(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
        val nowNum: Int = team.getValue(SoldierDefine.NOR_POWER_NUM).asInstanceOf[Int]
        if (totalNum > nowNum) {
          if (reduceMap.containsKey(soldierId)) {
            val num: Int = totalNum - nowNum + reduceMap.get(soldierId)
            reduceMap.put(soldierId, num)
          }
          else {
            reduceMap.put(soldierId, totalNum - nowNum)
          }
        }
      }
    }
    import scala.collection.JavaConversions._
    for (id <- reduceMap.keySet) {
      var deadNum: Int = reduceMap.get(id)
      var realyDeadNum: Int = 0
      var hurtNum: Int = 0
      battleType match {
        case BattleDefine.BATTLE_TYPE_ADVANTRUE =>
        case BattleDefine.BATTLE_TYPE_DUNGEON =>
        case BattleDefine.BATTLE_TYPE_WORLD_DEFEND =>
          realyDeadNum = (deadNum * 0.2).toInt
          hurtNum = deadNum - realyDeadNum
        case BattleDefine.BATTLE_TYPE_WORLD =>
          realyDeadNum = (deadNum * 0.2).toInt
          hurtNum = deadNum - realyDeadNum
          soldierProxy.reduceSoldierofferlineBaseNum(id, deadNum)
          deadNum = 0
        case _ =>
          deadNum = 0
      }
      deadNumMap.put(id, deadNum)
      soldierProxy.reduceofflineSoldierNum(id, deadNum, hurtNum, LogDefine.LOST_WORLD_FIGHT)
      soldierLog(player,0,deadNum,id,LogDefine.LOST_WORLD_FIGHT,soldierProxy.getSoldierNum(id))
      ids.add(id)
    }
    return ids
  }


}

class Team {
  var attackSortId: Int = 0
  var attackX: Int = 0
  var attackY: Int = 0
  var fightTime: Long = 0l
  var fightTeams: util.List[PlayerTeam] = null
  //进攻队列也要保存好
  var result: TileBattleResult = null
  var attackId: Long = 0
  var tasttype: Int = 0
  var powerMap: util.Map[Integer, java.lang.Long] = new util.HashMap[Integer, java.lang.Long]() {
    {
      put(NOR_POWER_speedRate, 0l);
      put(NOR_POWER_resexprate, 0l);
      put(NOR_POWER_rescollectrate, 0l);
    }
  }
}
