package com.znl.service

import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.{util, lang}

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import com.znl.base.{BaseDbPojo, BaseSetDbPojo}
import com.znl.core.{PlayerTroop, PlayerTeam, SimplePlayer}
import com.znl.define._
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg.CollectMsg
import com.znl.msg.GameMsg._
import com.znl.pojo.db.set._
import com.znl.pojo.db._
import com.znl.proto.M8
import com.znl.proxy._
import com.znl.service.actor.WorldBlockActor
import com.znl.service.functionNode.{Team, BattleNode}
import com.znl.service.map._
import com.znl.service.trigger.{TriggerType, TriggerEvent}
import com.znl.template.ReportTemplate
import com.znl.utils.{RandomEmitter, RandomUtil, GameUtils}
import org.json.JSONObject
import scala.collection.JavaConversions._

object WorldService {
  def props(areaKey: String) = Props(classOf[WorldService], areaKey)
  private[this] val worldCloseReward = new ConcurrentHashMap[String,util.HashMap[Long,String]]()
//  def getWorldCloseReward(areaKey: String): util.HashMap[Long,String] ={
//    worldCloseReward.get(areaKey)
//  }

  def initRewardMap(areaKeyMap : ConcurrentHashMap[Int, String]): Unit ={
    areaKeyMap.foreach( f => {
      worldCloseReward.put(f._2,new util.HashMap[Long,String])
    })
  }

  def addWorldCloseReward(areaKey: String,playerId :Long,rewardMap : util.HashMap[Integer,Integer]): Unit ={
    worldCloseReward.synchronized{
      var reward = worldCloseReward.get(areaKey).get(playerId)
      if(reward != null){
        val map = getRewardStrToMap(reward)
        for(key <- rewardMap.keySet()){
          var value = rewardMap.get(key)
          if(map.containsKey(key)){
            value = value+map.get(key)
          }
          map.put(key,value)
        }
        reward = getRewardMapToStr(map)
      }else{
        reward = getRewardMapToStr(rewardMap)
      }
      println("玩家"+playerId+"获得的奖励："+reward)
      worldCloseReward.get(areaKey).put(playerId,reward)
    }
  }

  def getRewardStrToMap(reward : String): util.HashMap[Integer,Integer] ={
    val rewardMap:util.HashMap[Integer,Integer] = new util.HashMap[Integer,Integer]
    if (reward != null ||reward.equals("null")==false){
      for(strTemp <- reward.split("&")){
        val temp = strTemp.split(",")
        val id = temp(0).toInt
        var num = temp(1).toInt
        if(rewardMap.containsKey(id) == false){
          rewardMap.put(id,num)
        }else{
          num = rewardMap.get(id)+num
          rewardMap.put(id,num)
        }
      }
    }
    rewardMap
  }

  def getRewardMapToStr(rewardMap:util.HashMap[Integer,Integer]): String ={
    var sb :StringBuffer = new StringBuffer
    try{
      for (key <- rewardMap.keySet()){
        sb.append(key)
        sb.append(",")
        val value = rewardMap.get(key)
        sb.append(value)
        sb.append("&")
      }
    }catch{
      case e:Exception=>
        sb = new StringBuffer
    }
    sb.toString
  }

  def saveWorldCloseReward(): Unit ={
    for (areaKey: String <- worldCloseReward.keySet()){
      val map = worldCloseReward.get(areaKey)
      for (playerId <- map.keySet()){
        val reward = map.get(playerId)
        val worldCloseSaveReward = BaseSetDbPojo.getSetDbPojo(classOf[WorldCloseSaveReward], areaKey)
        var rewardObj = worldCloseSaveReward.getWorldCloseRewardObj(playerId)
        if (rewardObj == null){
          rewardObj = BaseDbPojo.create(classOf[WorldCloseReward] , areaKey)
        }
        rewardObj.setReward(reward)
        rewardObj.setPlayerId(playerId)
        rewardObj.save()
        worldCloseSaveReward.addKeyValue(playerId+"",rewardObj.getId)
//        val sb :StringBuffer = new StringBuffer
//        sb.append(playerId)
//        sb.append("_")
//        sb.append(reward)
//        println(sb.toString)
//        BaseSetDbPojo.getSetDbPojo(classOf[WorldRewardSetDb], areaKey).addKeyValue(sb.toString, playerId)
      }
      println("保存areaKey="+areaKey+" 数据完毕")
    }
  }

  def loadAllWorldRewardSetDb(areaKey : String): Unit ={
    val keys = BaseSetDbPojo.getSetDbPojo(classOf[WorldRewardSetDb], areaKey).getAllKey
    for (key <- keys){
      try {
        val str = key.split("_")
        val playerId = str(0).toLong
        var reward = str(1)
        var rewardObj = BaseSetDbPojo.getSetDbPojo(classOf[WorldCloseSaveReward], areaKey).getWorldCloseRewardObj(playerId)
        if (rewardObj == null){
          //没有就创建吧
          rewardObj = BaseDbPojo.create(classOf[WorldCloseReward] , areaKey)
          rewardObj.setPlayerId(playerId)
          BaseSetDbPojo.getSetDbPojo(classOf[WorldCloseSaveReward], areaKey).addKeyValue(playerId+"",rewardObj.getId)
        }
        if(rewardObj.getReward != null){
          reward = reward+rewardObj.getReward
        }
        val map = getRewardStrToMap(reward)
        rewardObj.setReward(getRewardMapToStr(map))
        rewardObj.save()
        println("玩家"+playerId+"的补偿数据融合完毕"+rewardObj.getReward)
      }catch {
        case e:Exception=>
          CustomerLogger.error("解析旧的关服世界补偿的时候报错啦！"+key,e)
          e.printStackTrace()
      }
    }
    BaseSetDbPojo.getSetDbPojo(classOf[WorldRewardSetDb], areaKey).removeAllKey()
  }

  def getWorldCloseReward(playerId : Long,areaKey : String): util.HashMap[Integer,Integer] ={
    val reward = BaseSetDbPojo.getSetDbPojo(classOf[WorldCloseSaveReward], areaKey).getReward(playerId)
    var res = new util.HashMap[Integer,Integer]
    if (reward != null){
      res = getRewardStrToMap(reward)
    }
    res
  }

  def initWorldCloseReward(areaKey : String): Unit ={
    BaseSetDbPojo.getSetDbPojo(classOf[WorldCloseSaveReward], areaKey)
  }

}

/** 世界地图资源
  * Created by Administrator on 2015/11/10.
  */
class WorldService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {

  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      log.error(e.getCause, e.getMessage)
      e.printStackTrace()
      Resume
    }
    case _ => Resume
  }

  override def preStart() = {
    BaseSetDbPojo.getSetDbPojo(classOf[WorldTileSetDb], areaKey)
    initWorld()
    WorldService.initWorldCloseReward(areaKey)
  }

  //记录地图块上的玩家数，用来分配玩家具体情况 当为负数或大于上限时 则不能再进入 TODO
  val worldBlockMap: java.util.concurrent.ConcurrentHashMap[Int, WorldBlock] = new java.util.concurrent.ConcurrentHashMap[Int, WorldBlock]()

  var maxBlockNum = 0
  var curInitBlockNum = 0
  val titleNodeTimeMap: java.util.Map[String, Long] = new java.util.concurrent.ConcurrentHashMap[String, Long]() //节点的时间缓存，用于定时删除很久没用的点释放内存

  def onFightBuild(x: Int, y: Int, attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, attIcon: Int, attLevel: Int, attName: String, legionId: Long, powerMap: util.Map[Integer, java.lang.Long]): Unit = {
    val defendTile = getWorldTitleByPoint(x, y)
    val attackTile = getWorldTitleByPoint(attackX, attackY)
    if (defendTile == null || defendTile.tileType == TileType.Empty) {
      sendErrorFightMsgBak(ErrorCodeDefine.M80001_7, attackTeams)
      return
    }
    if (attackTile == null) {
      sendErrorFightMsgBak(ErrorCodeDefine.M80001_8, attackTeams)
      return
    }
    if (defendTile.defendPlayerId == attackTile.building.getPlayerId) {
      sendErrorFightMsgBak(ErrorCodeDefine.M80001_12, attackTeams)
      return
    }

    //获取防守玩家的属性
    var name = ""
    var level = 0
    var id = 0
    var defendPlayer: SimplePlayer = null
    if (defendTile.tileType == TileType.Building) {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(defendTile.building.getPlayerId, areaKey)
      name = simplePlayer.getName
      level = simplePlayer.getLevel
      defendPlayer = simplePlayer
      if (defendPlayer.getProtectOverDate > GameUtils.getServerDate().getTime) {
        sendErrorFightMsgBak(ErrorCodeDefine.M80001_11, attackTeams)
        return
      }
      if (defendPlayer.getArmygrouid == legionId && legionId > 0) {
        sendErrorFightMsgBak(ErrorCodeDefine.M80001_14, attackTeams)
        return
      }
    } else if (defendTile.tileType == TileType.Resource) {
      if (defendTile.defendPlayerId > 0) {
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(defendTile.defendPlayerId, areaKey)
        if (simplePlayer.getArmygrouid == legionId && legionId > 0) {
          sendErrorFightMsgBak(ErrorCodeDefine.M80001_14, attackTeams)
          return
        }
      }
    }

    val attackSort: Int = getPointSortId(x, y)
    val defendSort: Int = getPointSortId(attackX, attackY)


    val key = x + "_" + y
    if (context.child(key) == None) {
      createBuildNode(defendTile, defendSort, key)
    }

    var time: Double = Math.sqrt(Math.pow(x - attackX, 2) + Math.pow(y - attackY, 2)) * TaskDefine.WORLD_TASK_TIME_EACH + TaskDefine.WORLD_TASK_TIME_LIMIT
    val rduce = (1 + (powerMap.get(PlayerPowerDefine.NOR_POWER_speedRate) / UtilDefine.RUN_UNDER))
    time = time / rduce
    val fightTime: Long = GameUtils.getServerDate().getTime + (time.toInt) * 1000
    context.actorSelection(key) ! PushWorldFightToNode(attackTeams, attackX, attackY, fightTime, attackSort, powerMap)
    titleNodeTimeMap.put(key, GameUtils.getServerDate().getTime)

    //发送通知回模块产生任务列表


    if (defendTile.tileType == TileType.Resource) {
      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, defendTile.resPointId)
      name = pointConfig.getString("name")
     // level = pointConfig.getInt("level")
      level = -1
      if (defendTile.defendPlayerId > 0) {
        defendPlayer = PlayerService.getSimplePlayer(defendTile.defendPlayerId, areaKey)
      }
      id = defendTile.resPointId
    }
    sender() ! CreateAttackTask(x, y, attackTeams, fightTime, name, level, defendTile.tileType, attackX, attackY)
    //通知防守玩家有人进攻了
    if (defendPlayer != null) {
      val notice: TeamNotice = BaseDbPojo.create(classOf[TeamNotice], areaKey)
      notice.setArriveTime(fightTime)
      notice.setIconId(attIcon)
      notice.setLevel(attLevel)
      notice.setName(attName)
      notice.setPlayerId(defendPlayer.getId)
      notice.setTargetId(id)
      notice.setX(x)
      notice.setY(y)
      notice.setType(TaskDefine.NOTICE_TASK_ATTACK)
      notice.save()
      if (defendPlayer.online == true) {
        sendMsgPlayerModule(context, defendPlayer.getAccountName, ActorDefine.MAP_MODULE_NAME, BeAttackedNotify(notice))
      } else {
        val player: Player = BaseDbPojo.getOfflineDbPojo(defendPlayer.getId, classOf[Player], areaKey)
        player.addTeamNotice(notice.getId)
        player.save()
//        player.finalize()
      }
    }

  }


  def battleResultHandle(result: TileBattleResult) = {
    if (result.winner == 1) {
      //防守赢了

    } else {
      //进攻赢了
      if (result.defendTileType == TileType.Building) {
        //通知对应节点产生返回队列
        sendFightTeamBackToNode(result)
      }
    }

  }

  def sendFightTeamBackToNode(result: TileBattleResult): Unit = {
    //产生返回队列
    val key = result.attackX + "_" + result.attackY
    if (context.child(key) == None) {
      val attackTile = getWorldTitleByPoint(result.attackX, result.attackY)
      createBuildNode(attackTile, result.attackSortId, key)
    }
    var time: Double = Math.sqrt(Math.pow(result.defendX - result.attackX, 2) + Math.pow(result.defendY - result.attackY, 2)) * TaskDefine.WORLD_TASK_TIME_EACH + TaskDefine.WORLD_TASK_TIME_LIMIT
    val rduce = (1 + (result.powerMap.get(PlayerPowerDefine.NOR_POWER_speedRate) / UtilDefine.RUN_UNDER))
    time = time / rduce
    val backTime: Long = GameUtils.getServerDate().getTime + (time.toInt) * 1000
    context.actorSelection(key) ! FightTeamBack(result.attackTeam, backTime, result.defendX, result.defendY, result.defendSortId, result)
  }


  def sendErrorFightMsgBak(rs: Int, attackTeams: util.List[PlayerTeam]): Unit = {
    sender() ! FightBuildErrorBack(0, attackTeams, rs)
  }


  def createBuildNode(defendTile: WorldTile, defendSort: Int, key: String): Unit = {
    val x: Integer = defendTile.x
    val y: Integer = defendTile.y
    val sort: Integer = defendSort
    context.watch(context.actorOf(BattleNode.props(x, y, sort, defendTile.tileType, defendTile, areaKey), key))
  }


  def pillBuilNode(x: Int, y: Int): Unit = {
    val key = x + "_" + y
    context.actorSelection(key) ! PoisonPill.getInstance
  }

  /** *关服时调用，停掉自己所有的孩子节点 ***/
  def onStopNodeTeamBack(): Unit = {
    print(context.children.size)
    print("===================================")
    context.children.foreach(node => {
      node ! PoisonPill.getInstance
    })
  }

  override def receive: Receive = {
    //    case e: CompleteInitWorldBlock =>
    //      completeInitWorldBlock(e.worldBlock)
    case UpdateWorldBlock(worldBlock) =>
      updateWorldBlock(worldBlock)
    case AutoAddBuilding(playerId, accountName) =>
      onAutoAddBuilding(playerId, accountName)
    case msg: AddWorldBuildingSuccess =>
      getPlayerService(context) ! msg
    case Watchmagnifying(x: Int, y: Int, playerId: Long) =>
      onWatchmagnifying(x, y, playerId)
    case WatchBuildingTileInfo(x, y) =>
      onWatchBuildingTileInfo(x, y)
    case FightBuild(x: Int, y: Int, attackTeams: util.List[PlayerTeam], attackX: Int, attackY: Int, attIcon: Int, attLevel: Int, attName: String, legionId: Long, powerMap: util.Map[Integer, java.lang.Long]) =>
      onFightBuild(x, y, attackTeams, attackX, attackY, attIcon, attLevel, attName, legionId, powerMap)
    case AskBuildTitle(x: Int, y: Int, sortId: Int) =>
      onAskBuildTitle(x, y, sortId)
    case BuildBattleResult(result: TileBattleResult) =>
      battleResultHandle(result)
    case DigTeamBackNotice(result: TileBattleResult) =>
      sendFightTeamBackToNode(result)
    case DetectBuild(targetX: Int, targetY: Int, detectType: Int, playerId: Long) =>
      onDetectBuild(targetX, targetY, detectType, playerId)
    case StopAllNode() =>
      onStopNodeTeamBack()
    case AllNodeTeamBack() =>
      sender() ! context.children.size
    case MoveWorldBuild(targetX: Int, targetY: Int, myX: Int, myY: Int) =>
      onMoveWorldBuild(targetX, targetY, myX, myY)
    case CollectMsg(collinfo: M8.CollectInfo) =>
      onCollect(collinfo)
    case MoveRandomWorldBuild(playerId: Long, myX: Int, myY: Int) =>
      onRandomMove(playerId, myX, myY)
    case getOrePoint(simple: SimplePlayer, myaccontName: String) =>
      ongetOrePoint(simple, myaccontName)
    case GetHelpDefendTime(myX: Int, myY: Int, targetX: Int, targetY: Int, legionId: Long, powerMap: util.Map[Integer, java.lang.Long]) =>
      onGetHelpDefendTime(myX, myY, targetX, targetY, legionId, powerMap)
    case Tohelp(myX: Int, myY: Int, targetX: Int, targetY: Int, legionId: Long, fightteam: util.List[PlayerTeam], myId: Long, powerMap: util.Map[Integer, java.lang.Long]) =>
      onTohelp(myX, myY, targetX, targetY, legionId, fightteam, myId, powerMap)
    case GiveMeAPill(x: Int, y: Int) =>
      pillBuilNode(x, y)
    case SaveCloseNodeResource(playerId:Long,rewardMap : util.HashMap[Integer,Integer]) =>
      onSaveCloseNodeResource(playerId,rewardMap)
    case _ =>
      log.warning("未知消息")
  }



  def onSaveCloseNodeResource(playerId:Long,rewardMap : util.HashMap[Integer,Integer]): Unit ={
    if(rewardMap.size() > 0){
      WorldService.addWorldCloseReward(areaKey,playerId,rewardMap)
    }
  }

  def onGetHelpDefendTime(myX: Int, myY: Int, targetX: Int, targetY: Int, legionId: Long, powerMap: util.Map[Integer, java.lang.Long]): Unit = {
    val targetTile = getWorldTitleByPoint(targetX, targetY)
    if (targetTile.tileType != TileType.Building) {
      sender() ! HelpDefendError(ErrorCodeDefine.M80012_3)
      return
    }
    val targetSimple: SimplePlayer = PlayerService.getSimplePlayer(targetTile.building.getPlayerId, areaKey)
    if (targetSimple.getArmygrouid != legionId) {
      sender() ! HelpDefendError(ErrorCodeDefine.M80012_4)
      return
    }
    var time: Double = Math.sqrt(Math.pow(targetX - myX, 2) + Math.pow(targetY - myY, 2)) * TaskDefine.WORLD_TASK_TIME_EACH + TaskDefine.WORLD_TASK_TIME_LIMIT
    val rduce = (1 + (powerMap.get(PlayerPowerDefine.NOR_POWER_speedRate) / UtilDefine.RUN_UNDER))
    time = time / rduce
    val backTime: Long = GameUtils.getServerDate().getTime + (time.toInt) * 1000
    sender() ! HelpDefendback(myX, myY, targetX, targetY, backTime)
  }


  def onTohelp(myX: Int, myY: Int, targetX: Int, targetY: Int, legionId: Long, fightteam: util.List[PlayerTeam], myId: Long, powerMap: util.Map[Integer, java.lang.Long]): Unit = {
    val targetTile = getWorldTitleByPoint(targetX, targetY)
    if (targetTile.tileType != TileType.Building) {
      sender() ! tohelpError(ErrorCodeDefine.M80013_3)
      return
    }
    val targetSimple: SimplePlayer = PlayerService.getSimplePlayer(targetTile.building.getPlayerId, areaKey)
    if (targetSimple.getArmygrouid != legionId) {
      sender() ! tohelpError(ErrorCodeDefine.M80013_4)
      return
    }
    if (targetSimple.getGardNum >= 5) {
      sender() ! tohelpError(ErrorCodeDefine.M80013_6)
      return
    }
    val troop: PlayerTroop = new PlayerTroop()
   // val teamId: Long = BaseSetDbPojo.getSetDbPojo(classOf[HelpTeamDateSetDb], areaKey).addTeamDate(troop)
    var time: Double = Math.sqrt(Math.pow(targetX - myX, 2) + Math.pow(targetY - myY, 2)) * TaskDefine.WORLD_TASK_TIME_EACH + TaskDefine.WORLD_TASK_TIME_LIMIT
    val rduce = (1 + (powerMap.get(PlayerPowerDefine.NOR_POWER_speedRate) / UtilDefine.RUN_UNDER))
    time = time / rduce
    val backTime: Long = GameUtils.getServerDate().getTime + (time.toInt) * 1000
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(myId, areaKey)
    if (targetSimple.online) {
      val accountName = targetSimple.getAccountName
      sendMsgToPlayerMapModule(targetSimple.getAccountName, tellforhelp(myX, myY, fightteam, backTime, simplePlayer.getName, simplePlayer.getLevel, 0, simplePlayer.getIconId, targetX, targetY,powerMap))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(targetSimple.getId, classOf[Player], areaKey)
      val list: util.Set[java.lang.Long] = player.getHelpteams
      list.add(0l)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
      val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
      player.setGardNum(performTasksProxy.getguardNum())
      performTasksProxy.addPerformTaskForOffLine(TaskDefine.PERFORM_TASK_OTHERHELPBACK, simplePlayer.getName,
        simplePlayer.getLevel, myX, myY, backTime, fightteam, dungeoProxy, 0, simplePlayer.getId, playerProxy,
        simplePlayer.getIconId, targetX, targetY,powerMap)
      player.save()
    }
    val defendTile = getWorldTitleByPoint(targetX, targetY)
    val attackTile = getWorldTitleByPoint(myX, myY)
    //获取防守玩家的属性
    var name = ""
    var level = 0
    var id = 0
    var defendPlayer: SimplePlayer = null
    if (defendTile.tileType == TileType.Building) {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(defendTile.building.getPlayerId, areaKey)
      name = simplePlayer.getName
      level = simplePlayer.getLevel
      defendPlayer = simplePlayer
    }
    val defendSort: Int = getPointSortId(targetX, targetY)
    val attackSort: Int = getPointSortId(myX, myY)

    val key = targetX + "_" + targetY

    if (context.child(key) == None) {
      createBuildNode(defendTile, defendSort, key)
    }
    context.actorSelection(key) ! PushHelpToNode(fightteam, myX, myY, backTime, attackSort, powerMap)
    titleNodeTimeMap.put(key, GameUtils.getServerDate().getTime)
    sender() ! GameMsg.tohelpbackSucess(targetX, targetY, fightteam, backTime, defendPlayer.getName, defendPlayer.getLevel, 0, myX, myY)
  }

  def ongetOrePoint(simple: SimplePlayer, myaccontName: String): Unit = {
    if (simple.online == true) {
      sendMsgPlayerModule(context, simple.getAccountName, ActorDefine.TASK_MODULE_NAME, GameMsg.getOrePoint(simple, myaccontName))
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(simple.getId, classOf[Player], areaKey)
      val performTasksProxy: PerformTasksProxy = new PerformTasksProxy(player.getPerformTaskSet, player.getTeamNoticeSet, areaKey)
      val list: util.List[String] = performTasksProxy.getDigList
      sendMsgPlayerModule(context, myaccontName, ActorDefine.MAP_MODULE_NAME, GameMsg.getOrePointback(simple, list))
    }
  }


  def onCollect(collinfo: M8.CollectInfo): Unit = {
    var rs: Int = 0
    var ower: Int = 1
    var x: Int = collinfo.getX
    var y: Int = collinfo.getY
    val title: WorldTile = getWorldTitleByPoint(x, y)
    if (title == null) {
      rs = ErrorCodeDefine.M80008_1
    } else {
      if (title.tileType == TileType.Empty) {
        ower = 3
      }
      if (title.tileType == TileType.Resource) {
        ower = 2
      }
      if (title.tileType == TileType.Building) {
        ower = 1
      }
    }
    sender() ! CollectBack(collinfo, ower, rs)
  }

  def onMoveWorldBuild(targetX: Int, targetY: Int, myX: Int, myY: Int): Unit = {
    val targetTile = getWorldTitleByPoint(targetX, targetY)
    var rs = 0
    var id = 0l
    if (targetTile.tileType != TileType.Empty) {
      rs = ErrorCodeDefine.M80005_2
    } else {
      val myTile = getWorldTitleByPoint(myX, myY)
      val building: WorldBuilding = myTile.building
      building.setWorldTileX(targetX)
      building.setWorldTileY(targetY)
      building.save()
      targetTile.building_(building)
      myTile.building_(null)
      myTile.tileType_(TileType.Empty)
      targetTile.tileType_(TileType.Building)
      val worldTileSetDb = BaseSetDbPojo.getSetDbPojo(classOf[WorldTileSetDb], areaKey)
      worldTileSetDb.replaceKeyValue(getBuildingTileKey(myX, myY), getBuildingTileKey(targetX, targetY), building.getId)
    }
    sender() ! MoveWorldBuildBack(targetX, targetY, rs)
  }

  def getBuildingTileKey(x: Int, y: Int) = {
    "buildTile" + x + "_" + y
  }

  //创建侦查报告
  def createSpyReport(targetX: Int, targetY: Int, detectType: Int, playerId: Long): Unit = {
    val tile = getWorldTitleByPoint(targetX, targetY)
    var targetName = ""
    var defendId = 0l
    var defendName = ""
    var level = 0
    var defendTypeIdList = new util.ArrayList[Integer]
    var defendNumList = new util.ArrayList[Integer]
    val dungeoProxy: DungeoProxy = new DungeoProxy(new util.HashSet[lang.Long](), areaKey)
    val report: ReportTemplate = new ReportTemplate(playerId, "", defendId, defendName, ReportDefine.REPORT_TYPE_SPY, null)
    var sendToNode = false
    if (tile.tileType == TileType.Resource) {
      val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
      targetName = pointConfig.getString("name")
      defendId = tile.defendPlayerId
      if (defendId > 0) {
        //有人防守的资源点
        val sp: SimplePlayer = PlayerService.getSimplePlayer(defendId, areaKey)
        defendName = sp.getName
        defendTypeIdList = dungeoProxy.getSoldierTypeIdListFormPlayerTeam(tile.defendTeams)
        defendNumList = dungeoProxy.getSoldierNumListFormPlayerTeam(tile.defendTeams)
        report.setDefendLegion(sp.getLegionName)
        report.setAim(sp.getName)
        report.setDefendVip(sp.getVipLevel)
      } else {
        //没人防守那就是怪物列表了
        val teams = dungeoProxy.getMonsterList(tile.monsterGroupId)
        defendTypeIdList = dungeoProxy.getSoldierTypeIdListFormPlayerTeam(teams)
        defendNumList = dungeoProxy.getSoldierNumListFormPlayerTeam(teams)
        report.setAim(pointConfig.getString("name"))
      }
      report.defentIcon = pointConfig.getInt("icon")
      level = tile.resLv
      report.setResourceMapId(tile.resPointId)
      if (context.child(targetX + "_" + targetY) == None) {
        report.setResourceGet(0)
      } else {
        sendToNode = true
//        val resGet: Int = askNode(targetX + "_" + targetY, AskNodeResourceGet())
//        report.setResourceGet(resGet)
      }
    } else {
      //获取玩家的防守属性
      val sp: SimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
      report.setAim(sp.getName)
      report.setDefentCurrBoom(sp.getBoom.toInt)
      report.setDefentTotalBoom(sp.getBoomUpLimit.toInt)
      report.defentIcon = GameUtils.getCityIcon(sp.getBoomLevel)
      if (sp.getFacadeendTime > GameUtils.getServerDate().getTime) {
        report.defentIcon = sp.getFaceIcon
      }else if(sp.getBoomState == ActorDefine.DEFINE_BOOM_RUINS){
        report.defentIcon = ActorDefine.RUINS_ICON
      }
      defendId = sp.getId
      level = sp.getLevel
      defendTypeIdList = dungeoProxy.getSoldierTypeIdListFormPlayerTeam(sp.getDefendTroop.getPlayerTeams)
      defendNumList = dungeoProxy.getSoldierNumListFormPlayerTeam(sp.getDefendTroop.getPlayerTeams)
      targetName = sp.getName
      var temas: util.List[PlayerTeam] = null
      if (context.child(targetX + "_" + targetY) == None) {
        temas = new util.ArrayList[PlayerTeam]()
      } else {
        sendToNode = true
//        temas = askNode(targetX + "_" + targetY, AskNodeDefendGet())
      }
      /*if (temas.size() > 0) {
        val groupsp: SimplePlayer = PlayerService.getSimplePlayer(temas.get(0).playerId, areaKey)
        defendName = groupsp.getName
        defendTypeIdList = dungeoProxy.getSoldierTypeIdListFormPlayerTeam(temas)
        defendNumList = dungeoProxy.getSoldierNumListFormPlayerTeam(temas)
      }*/
      val list = getRewardList(sp)
      report.setResourceGet(list.get(0))
      list.remove(0)
      report.setPosResource(list)
      report.setDefendLegion(sp.getLegionName)
      report.setDefendVip(sp.getVipLevel)
    }
    report.setDefendName(defendName)
    report.setDefendId(defendId)
    //发送战报服务产生侦查战报
    report.setName(targetName)
    report.setLevel(level)
    report.setX(targetX)
    report.setY(targetY)
    report.setDefendSoldierTypeIds(defendTypeIdList)
    report.setDefendSoldierNums(defendNumList)
    if (sendToNode){
      context.actorSelection(targetX + "_" + targetY) ! GetReportInfo(report)
    }else{
      tellService(context, ActorDefine.MAIL_SERVICE_NAME, SendReport(report, 0))
    }
  }

  def getRewardList(simplePlayer: SimplePlayer): util.List[Integer] = {
    val resourceList: util.List[Integer] = new util.ArrayList[Integer]()
    var tael = simplePlayer.getTael - simplePlayer.getProtectNum
    if (tael < 0) {
      tael = 0
    }
    var iron = simplePlayer.getIron - simplePlayer.getProtectNum
    if (iron < 0) {
      iron = 0
    }
    var wood = simplePlayer.getWood - simplePlayer.getProtectNum
    if (wood < 0) {
      wood = 0
    }
    var stones = simplePlayer.getStones - simplePlayer.getProtectNum
    if (stones < 0) {
      stones = 0
    }
    var food = simplePlayer.getFood - simplePlayer.getProtectNum
    if (food < 0) {
      food = 0
    }
    resourceList.add(((tael + iron + stones + wood + food) * 0.1).toInt)
    resourceList.add((tael * 0.1).toInt)
    resourceList.add((iron * 0.1).toInt)
    resourceList.add((stones * 0.1).toInt)
    resourceList.add((wood * 0.1).toInt)
    resourceList.add((food * 0.1).toInt)
    resourceList
  }

//  def askNode[T](nodeName: String, msg: AnyRef) = {
//    val ref = context.actorSelection(nodeName)
//    val value: Option[T] = GameUtils.futureAsk(ref, msg, 10)
//    val result: T = value.getOrElse(null).asInstanceOf[T]
//    result
//  }

  //侦查事件
  def onDetectBuild(targetX: Int, targetY: Int, detectType: Int, playerId: Long): Unit = {

    if (detectType == ActorDefine.DETECT_TYPE_PRICE) {
      val tile = getWorldTitleByPoint(targetX, targetY)
      var price: Int = 0
      var id = 0l
      if (tile == null || tile.tileType == TileType.Empty) {
        price = ErrorCodeDefine.M80002_1
      } else if (tile.tileType == TileType.Resource) {
        val pointConfig = ConfigDataProxy.getConfigInfoFindById(DataDefine.RESOURCE_POINT, tile.resPointId)
        price = pointConfig.getInt("reqtael")
        id = -tile.resPointId
      } else {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(tile.building.getPlayerId, areaKey)
        if (sp.getProtectOverDate > GameUtils.getServerDate().getTime) {
          price = ErrorCodeDefine.M80002_1
        } else {
          val pointConfig = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.DETECT_PRICE, "level", sp.getLevel)
          price = pointConfig.getInt("price")
        }
        id = sp.getId
      }
      sender() ! DetectPriceBack(targetX, targetY, price, id)
    } else {
      createSpyReport(targetX, targetY, detectType, playerId)
    }
  }

  //  def completeInitWorldBlock(worldBlock: WorldBlock) = {
  //    worldBlockMap.put(worldBlock.sortId, worldBlock)
  //    curInitBlockNum = curInitBlockNum + 1
  //    if (maxBlockNum == curInitBlockNum) {
  //      log.info("!!!!!!!!!!世界地图全部初始化完毕!!!!!!!!!!!!!!!!!!!!!!!")
  //
  //      //      testTriggerEvent()
  //    }
  //  }

  def testTriggerEvent() = {
    val event = new TriggerEvent(self, "hello", TriggerType.COUNT_DOWN, 10)
    getTriggerService(context) ! AddTriggerEvent(event)
  }

  def updateWorldBlock(worldBlock: WorldBlock) = {
    worldBlockMap.replace(worldBlock.sortId, worldBlock)
  }

  def getPointSortId(x: Int, y: Int): Int = {
    val block = findBlockInfoByPos(x, y)
    var sortId: Int = 0
    if (block == None) {
      log.warning("该坐标上没有对应的世界块" + x + " " + y)
    } else {
      sortId = block.get._2.sortId
    }
    sortId
  }

  def onAskBuildTitle(x: Int, y: Int, sortId: Int): Unit = {
    val title: WorldTile = getWorldTitleByPoint(x, y)
    sender() ! Some(title)
  }

  def getWorldTitleByPoint(x: Int, y: Int) = {
    val block = findBlockInfoByPos(x, y)
    var worldTile: WorldTile = null
    if (block == None) {
      log.warning("该坐标上没有对应的世界块" + x + " " + y)
    } else {
      val sortId = block.get._2.sortId
      val key = getBuildingTileKey(x, y)
      val tile: WorldTile = block.get._2.worldTileMap.get(key)
      //      val tile: Option[WorldTile] = askWorldBlockActor(sortId, WatchBuildingTileInfo(x, y))
      if (tile != null) {
        worldTile = tile
      }
    }
    worldTile
  }

  def onWatchmagnifying(x: Int, y: Int, playerId: Long) = {
    var maxX: Int = x + ChatAndMailDefine.WORLD_MAGNIFY
    var minX: Int = x - ChatAndMailDefine.WORLD_MAGNIFY
    var maxY: Int = y + ChatAndMailDefine.WORLD_MAGNIFY
    var minY: Int = y - ChatAndMailDefine.WORLD_MAGNIFY
    if (maxX >= ChatAndMailDefine.WORLD_MAX) {
      maxX = ChatAndMailDefine.WORLD_MAX
    }
    if (minX <= 0) {
      minX = 0
    }
    if (maxY >= ChatAndMailDefine.WORLD_MAX) {
      maxY = ChatAndMailDefine.WORLD_MAX
    }
    if (minY <= 0) {
      minY = 0
    }
    val buildingPlayerList: util.List[java.lang.Long] = new util.ArrayList[java.lang.Long]()
    val worldtitlelist: util.List[WorldTile] = new util.ArrayList[WorldTile]()
    val worldtitlelistaddll: util.List[WorldTile] = new util.ArrayList[WorldTile]()

    ////////////////////////////////////////////////
    {
      var tx: Int = minX
      while (tx <= maxX) {
        {
          {
            var ty: Int = minY
            while (ty <= maxY) {
              {
                val title: WorldTile = getWorldTitleByPoint(tx, ty)
                if (title.building != null) {
                  if (title.building.getPlayerId != playerId) {
                    worldtitlelistaddll.add(title)
                    buildingPlayerList.add(title.building.getPlayerId)
                  }
                }
              }
              ({
                ty += 1;
                ty - 1
              })
            }
          }
        }
        ({
          tx += 1;
          tx - 1
        })
      }
    }
    //////////////////////////////////////
    val buildingPlayergetList = new util.HashSet[java.lang.Long]()
    var flag: Boolean = true
    //随机玩家
    while (flag) {
      if (buildingPlayergetList.size >= buildingPlayerList.size || buildingPlayergetList.size >= ChatAndMailDefine.PEOPLE_RANDOM) {
        flag = false //todo: break is not supported
      } else {
        val random: Int = GameUtils.getRandomValueByRange(buildingPlayerList.size)
        val randomid: java.lang.Long = buildingPlayerList.get(random)
        buildingPlayergetList.add(randomid)
      }
    }


    ////////////////////////
    val simplePlayerList: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(buildingPlayergetList, areaKey)
    worldtitlelistaddll.foreach(worldTile => {
      if (worldTile.building != null) {
        simplePlayerList.foreach(simplePlayer => {
          if (simplePlayer.getId == worldTile.building.getPlayerId) {
            worldTile.playerName_(simplePlayer.getName)
            worldTile.playerLevel_(simplePlayer.getLevel)
            worldTile.boomState_(simplePlayer.getBoomState)
            worldTile.degree_(simplePlayer.getBoom.toInt)
            worldTile.degreemax_(simplePlayer.getBoomUpLimit.toInt)
            worldTile.icon_(simplePlayer.getIconId)
            worldTile.pendant_(simplePlayer.getPendant)
            val boomConfig = GameUtils.getBoomConfig(simplePlayer.getBoom.toInt)
            worldTile.icon_(boomConfig.getInt("BaseLook"))
            val now = GameUtils.getServerDate().getTime
            if (simplePlayer.getProtectOverDate > now) {
              worldTile.protetc_(true)
            } else {
              worldTile.protetc_(false)
            }
            if (simplePlayer.getBoomState == ActorDefine.DEFINE_BOOM_RUINS) {
              worldTile.icon_(ActorDefine.RUINS_ICON)
            } else {
              if (simplePlayer.getFacadeendTime > now) {
                worldTile.icon_(simplePlayer.getFaceIcon)
              }
            }
            worldtitlelist.add(worldTile)
          }
        })
      }
    })
    //资源
    val mysimple: SimplePlayer = PlayerService.getSimplePlayer(playerId, areaKey)
    val ranm1: Int = GameUtils.getRandomValueByRange(ChatAndMailDefine.RANDOM_RESOUCE1.size)
    val ranValue1: Int = ChatAndMailDefine.RANDOM_RESOUCE1.get(ranm1)
    val random2: Int = GameUtils.getRandomValueByRange(ChatAndMailDefine.RANDOM_RESOUCE2.size)
    val ranValue2: Int = ChatAndMailDefine.RANDOM_RESOUCE2.get(random2)
    var level: Int = mysimple.getLevel - ranValue1 + 2 * ranValue2
    if (level <= 2) {
      level = 2
    }
    if (level >= 60) {
      level = 60
    }
    if (level % 2 != 0) {
      level = level + 1
    }
    var add: Int = 0
    var falg: Boolean = true
    val setlist: util.Set[java.lang.Integer] = new util.HashSet[java.lang.Integer]()
    while (falg) {
      var lineminX: Int = x - add
      var linemaxX: Int = x + add
      var lineminY: Int = y - add
      var linemaxY: Int = y + add
      if (lineminX <= 0) {
        lineminX = 0
      }
      if (lineminY < 0) {
        lineminY = 0
      }
      if (linemaxY > ChatAndMailDefine.WORLD_MAX) {
        linemaxY = ChatAndMailDefine.WORLD_MAX
      }
      if (linemaxX > ChatAndMailDefine.WORLD_MAX) {
        linemaxX = ChatAndMailDefine.WORLD_MAX
      }
      {
        var l1: Int = lineminX
        while (l1 <= linemaxX) {
          {
            if (lineminY <= y - add) {
              val title1: WorldTile = getWorldTitleByPoint(l1, lineminY)
              if (lineminY <= y - add && title1.building == null && title1.resLv == level) {
                worldtitlelist.add(title1)
                setlist.add(title1.resType)
              }
            }

            if (linemaxY >= y + add) {
              val title2: WorldTile = getWorldTitleByPoint(l1, linemaxY)
              if (title2.building == null && title2.resLv == level) {
                worldtitlelist.add(title2)
                setlist.add(title2.resType)
              }
            }
          }
          ({
            l1 += 1;
            l1 - 1
          })
        }
      }
      {
        var l2: Int = lineminY
        while (l2 <= linemaxY) {
          {
            if (lineminX <= x - add) {
              val title1: WorldTile = getWorldTitleByPoint(lineminX, l2)
              if (title1.building == null && title1.resLv == level) {
                worldtitlelist.add(title1)
                setlist.add(title1.resType)
              }
            }

            if (linemaxX >= x + add) {
              val title2: WorldTile = getWorldTitleByPoint(linemaxX, l2)
              if (title2.building == null && title2.resLv == level) {
                worldtitlelist.add(title2)
                setlist.add(title2.resType)
              }
            }
          }
          ({
            l2 += 1;
            l2 - 1
          })
        }
      }
      add = add + 1
      if (setlist.size() >= ChatAndMailDefine.PEOPLE_RESOUCE || (linemaxX >= ChatAndMailDefine.WORLD_MAX && linemaxY >= ChatAndMailDefine.WORLD_MAX && lineminX <= 0 && lineminY <= 0)) {
        falg = false
        if (linemaxX >= ChatAndMailDefine.WORLD_MAX && linemaxY >= ChatAndMailDefine.WORLD_MAX && lineminX <= 0 && lineminY <= 0) {
          println("********" + "没有找到等级为" + level + "资源!!")
        }
      }
    }
    //////////////////////////////////////
    sender() ! Watchmagnifyingback(worldtitlelist, x, y)
  }

  def getLatelyTitle(list: util.List[WorldTile], x: Int, y: Int): WorldTile = {
    var tile: WorldTile = null
    import scala.collection.JavaConversions._
    for (wt <- list) {
      if (tile == null) {
        tile = wt
      }
      else {
        if (getPoinLdistace(wt.x, wt.y, x, y) < getPoinLdistace(tile.x, tile.y, x, y)) {
          tile = wt
        }
      }
    }
    return tile
  }

  def getPoinLdistace(x1: Int, y1: Int, x2: Int, y2: Int): Int = {
    return (Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2)).toInt
  }

  def onWatchBuildingTileInfo(x: Int, y: Int) = {

    val senderer = sender()
    val worldTileList = new util.ArrayList[WorldTile]()

    val buildingPlayerList = new util.HashSet[java.lang.Long]()

    //查看x y -2 +2 区域的格子信息
    val dt: Int = 4
    for (i <- x - dt until x + dt + 1) {
      for (j <- y - dt until y + dt + 1) {
        val block = findBlockInfoByPos(i, j)
        if (block == None) {
          log.warning("该坐标上没有对应的世界块" + i + " " + j)
        } else {
          //          val sortId = block.get._2.sortId
          val key = getBuildingTileKey(i, j)
          val tile: WorldTile = block.get._2.worldTileMap.get(key)
          //          val tile: Option[WorldTile] = askWorldBlockActor(sortId, WatchBuildingTileInfo(i, j)) //可优化，按组请求相关数据
          if (tile != null) {
            val worldTile = tile
            worldTileList.add(worldTile)
            if (worldTile.building != null) {
              buildingPlayerList.add(worldTile.building.getPlayerId)
            } else {
              if (worldTile.defendPlayerId > 0) {
                buildingPlayerList.add(worldTile.defendPlayerId)
              }
            }
          }
        }
      }
    }

  //  if (buildingPlayerList.size() > 0) {
      //去查看数据
      val simplePlayerList: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(buildingPlayerList, areaKey)
      worldTileList.foreach(worldTile => {
        if (worldTile.building != null) {
          simplePlayerList.foreach(simplePlayer => {
            if (simplePlayer.getId == worldTile.building.getPlayerId) {
              worldTile.playerName_(simplePlayer.getName)
              worldTile.playerLevel_(simplePlayer.getLevel)
              worldTile.boomState_(simplePlayer.getBoomState)
              val boomConfig = GameUtils.getBoomConfig(simplePlayer.getBoom.toInt)
              worldTile.cityicon_(boomConfig.getInt("BaseLook"))
              worldTile.icon_(simplePlayer.getIconId)
              val now = GameUtils.getServerDate().getTime
              if (simplePlayer.getProtectOverDate > now) {
                worldTile.protetc_(true)
              } else {
                worldTile.protetc_(false)
              }
              worldTile.legionName_(simplePlayer.getLegionName)
              if (simplePlayer.getBoomState == ActorDefine.DEFINE_BOOM_RUINS) {
                worldTile.cityicon_(ActorDefine.RUINS_ICON)
              } else {
                if (simplePlayer.getFacadeendTime > now) {
                  worldTile.cityicon_(simplePlayer.getFaceIcon)
                }
              }
            }
          })
        } else {
          var falg: Boolean = true
          simplePlayerList.foreach(simplePlayer => {
            if (simplePlayer.getId == worldTile.defendPlayerId) {
              worldTile.legionName_(simplePlayer.getLegionName)
              falg = false
            }
          })
          if (falg) {
            worldTile.legionName_("")
          }
        }
      })
  //  }

    //把查询到的数据 推送给请求这条协议的发送者 一般是在模块里面
    senderer ! WatchBuildingTileInfoBack(worldTileList)

  }

  def findBlockInfoByPos(x: Int, y: Int) = {
    val value = worldBlockMap.find(v => {
      val block = v._2
      if (x >= block.xOrigin && x <= block.xEnd && y >= block.yOrigin && y <= block.yEnd) {
        true
      } else {
        false
      }
    })

    value
  }

  //使用随机迁城令
  def onRandomMove(playerId: Long, myX: Int, myY: Int): Unit = {
    val keyList = new util.ArrayList[Integer]()
    val keySet = worldBlockMap.keySet()
    keySet.foreach(key => {
      keyList.add(key)
    })
    util.Collections.sort(keyList)
    var key: Int = -1
    while (key == -1) {
      val ran = GameUtils.getRandomValueByRange(keyList.size())
      val worldBlock = worldBlockMap.get(ran)
      if (worldBlock != null && worldBlock.curEmptyTileNum > 0 && worldBlock.curPlayerNum < worldBlock.maxPlayerNum) {
        key = ran
      }
    }

    //    val key = keyList.find(key => {
    //      val worldBlock = worldBlockMap.get(key)
    //      if (worldBlock.curEmptyTileNum > 0 && worldBlock.curPlayerNum < worldBlock.maxPlayerNum) {
    //        true
    //      } else {
    //        false
    //      }
    //    })
    if (key == -1) {
      log.warning("!!!!!!!!竟然找不出可以入住的建筑了!!!!!!!!进行迁移逻辑!!!")
    } else {
      val worldBlock = worldBlockMap.get(key)
      //发送具体的块进行添加建筑，系统的不需要推送
      val sortId = worldBlock.sortId
      val actor = getWorldBlockActor(sortId)
      val targetTile: WorldTile = askWorldBlockActor(actor, GetRandomEmpty())
      if (targetTile != null) {
        //执行迁城逻辑
        val myTile = getWorldTitleByPoint(myX, myY)
        val building: WorldBuilding = myTile.building
        building.setWorldTileX(targetTile.x)
        building.setWorldTileY(targetTile.y)
        building.save()
        targetTile.building_(building)
        myTile.building_(null)
        myTile.tileType_(TileType.Empty)
        targetTile.tileType_(TileType.Building)
        val worldTileSetDb = BaseSetDbPojo.getSetDbPojo(classOf[WorldTileSetDb], areaKey)
        worldTileSetDb.replaceKeyValue(getBuildingTileKey(myX, myY), getBuildingTileKey(targetTile.x, targetTile.y), building.getId)
        sender() ! MoveRandomWorldBuildBack(targetTile.x, targetTile.y)
      }
    }
  }

  def askWorldBlockActor[T](actor: ActorSelection, msg: AnyRef) = {
    val value: Option[T] = GameUtils.futureAsk(actor, msg, 10)
    val result: T = value.getOrElse(null).asInstanceOf[T]
    result
  }

  //系统自动添加建筑
  def onAutoAddBuilding(playerId: Long, accountName: String) = {
    val keyList = new util.ArrayList[Integer]()
    val keySet = worldBlockMap.keySet()
    keySet.foreach(key => {
      keyList.add(key)
    })
    util.Collections.sort(keyList)

    val key = keyList.find(key => {
      val worldBlock = worldBlockMap.get(key)
      if (worldBlock.curEmptyTileNum > 0 && worldBlock.curPlayerNum < worldBlock.maxPlayerNum) {
        true
      } else {
        false
      }
    })
    if (key == None) {
      log.warning("!!!!!!!!竟然找不出可以入住的建筑了!!!!!!!!进行迁移逻辑!!!")
    } else {
      val worldBlock = worldBlockMap.get(key.get)
      //发送具体的块进行添加建筑，系统的不需要推送
      val sortId = worldBlock.sortId
      val actor = getWorldBlockActor(sortId)
      actor ! AddWorldBuildingToTile(playerId, accountName)
    }
  }


  //初始化世界 仅在开服时初始化
  def initWorld() = {

    val worldTileSetDb = BaseSetDbPojo.getSetDbPojo(classOf[WorldTileSetDb], areaKey)
    val keySet: java.util.Set[String] = worldTileSetDb.getAllKey

    val infos = ConfigDataProxy.getConfigAllInfo(DataDefine.MAP_GENERATE)

    infos.foreach(info => {
      val name = ActorDefine.WORLD_BLOCK_PRE_NAME
      val sortId = info.getInt("sort")
      maxBlockNum = maxBlockNum + 1
      val worldBlock = initWorldBlock(info, keySet)
      worldBlockMap.put(worldBlock.sortId, worldBlock)
      context.watch(context.actorOf(WorldBlockActor.props(worldBlock, areaKey), name + sortId))
    })
  }


  def initWorldBlock(config: JSONObject, keySet: java.util.Set[String]): WorldBlock = {
    val worldBlock = new WorldBlock()
    val playMax: Int = config.getInt("playmax")
    val sortId: Int = config.getInt("sort")
    val group: Int = config.getInt("group")
    val xOrigin: Int = config.getInt("xorigin")
    val xEnd: Int = config.getInt("xend")
    val yOrigin: Int = config.getInt("yorigin")
    val yEnd: Int = config.getInt("yend")
    val worldTileMap = new util.HashMap[String, WorldTile]()
    val list: util.ArrayList[(Integer, Integer)] = new util.ArrayList[(Integer, Integer)]
    val infos = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RES_GENERATE, "group", group)
    infos.foreach(info => {
      val id = info.getInt("ID")
      val rate = info.getInt("rate")

      list.add((id, rate))

    })
    val randomEmitter = new RandomEmitter(list) //生成资源生成器

    val buildingKeyList: util.List[String] = new util.ArrayList[String]()

    var emptyTileNum = 0 //空格数
    var curPlayerNum: Int = 0 //当前玩家人数

    for (x <- xOrigin until xEnd + 1) {
      for (y <- yOrigin until yEnd + 1) {
        val worldTile = new WorldTile()
        worldTile.x_(x)
        worldTile.y_(y)

        val key = getBuildingTileKey(x, y)
        if (keySet.contains(key) == true) {
          //这个坐标已经有人了
          worldTile.tileType_(TileType.Building) //需要去缓存DB拿到具体的building数据 拿ID，再拿数据
          curPlayerNum = curPlayerNum + 1
          buildingKeyList.add(key)
        } else {
          //没有人，随机规则
          resGenerate(randomEmitter, worldTile)

          if (worldTile.tileType == TileType.Empty) {
            emptyTileNum = emptyTileNum + 1
          }
        }
        worldTileMap += (key -> worldTile)
      }
    }

    if (buildingKeyList.size() > 0) {
      //有建筑的ID才去拿具体的缓存数据 不然会报错
      val worldTileSetDb = BaseSetDbPojo.getSetDbPojo(classOf[WorldTileSetDb], areaKey)

      buildingKeyList.foreach(key => {
        val id = worldTileSetDb.getValueByKey(key)
        if (id == null) {
          log.error("拿到的建筑ID竟然是空的，需要通知查看数据库！！key:" + key)
        } else {
          val tile = worldTileMap(key)
          val worldBuilding = BaseDbPojo.get(id.toLong, classOf[WorldBuilding], areaKey)
          tile.building_(worldBuilding)
        }

      }) //拿各个玩家建筑数据  玩家密集时可能会执行比较久 TODO
    }

    ////////////////////初始化完毕，将该块的总信息发送出去
    worldBlock.sortId_(sortId)
    worldBlock.curEmptyTileNum_(emptyTileNum)
    worldBlock.curPlayerNum_(curPlayerNum)
    worldBlock.maxPlayerNum_(playMax)
    worldBlock.xOrigin_(xOrigin)
    worldBlock.xEnd_(xEnd)
    worldBlock.yOrigin_(yOrigin)
    worldBlock.yEnd_(yEnd)
    worldBlock.worldTileMap_(worldTileMap)
    worldBlock
  }

  //资源生成规则
  def resGenerate(randomEmitter: RandomEmitter, worldTile: WorldTile) = {
    val resId: Int = randomEmitter.emitter() //随机生成资源ID
    //再随机出等级
    val info = ConfigDataProxy.getConfigInfoFindById(DataDefine.RES_GENERATE, resId)
    val levelMin = info.getInt("levelmin")
    val levelMax = info.getInt("levelmax")
    val resType = info.getInt("type")

    worldTile.resType_(resType)
    if (resType == 6) {
      //写死空地类型
      worldTile.tileType_(TileType.Empty)
    } else {
      worldTile.tileType_(TileType.Resource)
      var level = GameUtils.getRandomValueByInterval(levelMin, levelMax)
      if (level % 2 != 0) {
        level = level + 1
      }
      worldTile.resLv_(level)
      worldTile.resId_(resId)

      val pointInfo = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RESOURCE_POINT, "type", resType, "level", level)
      val pointId = pointInfo.getInt("ID")
      worldTile.resPointId_(pointId)
      val array = pointInfo.getJSONArray("monstergroup")
      val ranNum = GameUtils.getRandomValueByRange(array.length())
      worldTile.monsterGroupId_(array.getInt(ranNum))
    }

  }

  def getWorldBlockActor(sortId: Int) = {
    val name = ActorDefine.WORLD_BLOCK_PRE_NAME
    val key = name + sortId
    context.actorSelection(key)
  }

  def askWorldBlockActor(sortId: Int, msg: AnyRef) = {
    val actor = getWorldBlockActor(sortId)
    val value: Option[WorldTile] = GameUtils.futureAsk(actor, msg, 10)
    value
  }


  def sendMsgToPlayerMapModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.MAP_MODULE_NAME)
    actor ! msg
  }
}


