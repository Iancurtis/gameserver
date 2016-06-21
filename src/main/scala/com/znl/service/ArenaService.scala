package com.znl.service

import java.util
import java.util.Calendar

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{OneForOneStrategy, ActorLogging, Actor, Props}
import com.znl.base.{BaseDbPojo, BaseSetDbPojo}
import com.znl.core.{PlayerBattle, ArenaRank, SimplePlayer, PlayerTroop}
import com.znl.define._
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg._
import com.znl.node.battle.BattleNodeActor
import com.znl.pojo.db.Report
import com.znl.pojo.db.set.{ArenaReportSetDb, TeamDateSetDb, ArenaLastRankSetDb, ArenaRankSetDb}
import com.znl.proto.Common.FightElementInfo
import com.znl.proto.{M21, M7, M20}
import com.znl.proxy.{ConfigDataProxy, TimerdbProxy, DbProxy}
import com.znl.service.map.{WorldTile, WorldBlock}
import com.znl.service.trigger.{TriggerType, TriggerEvent}
import com.znl.template.ReportTemplate
import com.znl.utils.GameUtils
import org.json.JSONObject
import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.collection.JavaConversions._

object ArenaService {
  def props(areaKey: String) = {
    Props(classOf[ArenaService], areaKey)
  }

  val arenaMap: util.Map[java.lang.Long, Integer] = new util.concurrent.ConcurrentHashMap[java.lang.Long, Integer]
  val rankPlayerIdMap: util.Map[Integer, java.lang.Long] = new util.concurrent.ConcurrentHashMap[Integer, java.lang.Long]

  /**
    * 返回竞技场数据
    */
  def getArenaRankMap(areaKey :String): M21.RankListInfo.Builder = {
    val infos: M21.RankListInfo.Builder = M21.RankListInfo.newBuilder()
    val rankType = PowerRanksDefine.POWERRANK_TYPE_ARENA
    var i = 1
    var len = ArenaService.rankPlayerIdMap.size()
    while (len > 0) {
      try {
        if (infos.getPowerRankInfoList.size() < PowerRanksDefine.MAX_SHOW_RANK_NUM) {
          val info:M21.PowerRankInfo.Builder =M21.PowerRankInfo.newBuilder()
          val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(ArenaService.rankPlayerIdMap.get(i),areaKey)
          if (simplePlayer != null) {
            info.setTypeId(rankType)
            info.setRank(i)
            info.setPlayerId(simplePlayer.getId)
            if(simplePlayer.arenaTroop!=null) {
              info.setRankValue(simplePlayer.arenaTroop.getCapity)
            }else{
              info.setRankValue(0)
            }
            if(simplePlayer.getId<0){
              info.setRankValue(simplePlayer.getCapacity)
            }
            info.setLevel(simplePlayer.getLevel())
            info.setName(simplePlayer.getName())
            infos.addPowerRankInfo(info)
          }else{
            CustomerLogger.error("获得玩家的竞技场数据时得到的对方是个空！！！！！！！！！！！！！！")
          }
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          CustomerLogger.error("获得玩家的竞技场数据时出错！！！！！！！！！！！！！！",e)
      }
      if (infos.getPowerRankInfoList.size() >= PowerRanksDefine.MAX_SHOW_RANK_NUM){
        len =0//超过100人了就跳出来吧
      }
      i += 1
      len -= 1
    }
    infos.setTypeId(rankType)
  }

}

class ArenaService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {

  override val supervisorStrategy = OneForOneStrategy() {
    case _ => Resume
  }
  updateEachHourNotice()

  def updateEachHourNotice() = {
    val event = new TriggerEvent(self, EachHourNotice(), TriggerType.WHOLE_HOUR, 0)
    getTriggerService(context) ! AddTriggerEvent(event)
  }

  override def preStart() = {
    BaseSetDbPojo.getSetDbPojo(classOf[ArenaRankSetDb], areaKey)
    BaseSetDbPojo.getSetDbPojo(classOf[ArenaLastRankSetDb], areaKey)

    import context.dispatcher
    context.system.scheduler.schedule(0 milliseconds, 1 minutes, context.self, OnServerTrigger())

    val ranks: util.List[java.lang.String] = BaseSetDbPojo.getSetDbPojo(classOf[ArenaRankSetDb], areaKey).getAllArenaRank // DbProxy.ask(GetAllArenaRank(areaKey))

    val lastranks: util.List[java.lang.String] =  BaseSetDbPojo.getSetDbPojo(classOf[ArenaLastRankSetDb], areaKey).getAllArenaRank  // DbProxy.ask(GetAllArenaLastTimeRankList(areaKey))
    initArenaMap(ranks, lastranks)
    sortMap()
    loadReports()
    checkRankMap()
  }
  def checkRankMap(): Unit ={
    val jasonall : util.List[JSONObject]=ConfigDataProxy.getConfigAllInfo(DataDefine.ArenaRobot)
    var rank:Int=1
    while (rank<=jasonall.size()){
      if(ArenaService.rankPlayerIdMap.get(rank)==null){
        val json:JSONObject=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.ArenaRobot,"rank",rank)
        ArenaService.rankPlayerIdMap.put(rank,-json.getInt("ID").toLong)
        ArenaService.arenaMap.put(-json.getInt("ID").toLong,rank)
      }
      rank= rank +1
    }
  }

  def sortMap(): Unit = {
    var rank: Int = 1
      var i: Int = 1
      val rankPlayerIdnew: util.Map[Integer, java.lang.Long] = new util.concurrent.ConcurrentHashMap[Integer, java.lang.Long]
      while (i <=  ArenaService.rankPlayerIdMap.size) {
        {
          if ( ArenaService.rankPlayerIdMap.get(i) != null) {
            val oldran: Int= ArenaService.arenaMap.get(ArenaService.rankPlayerIdMap.get(i))
            ArenaService.arenaMap.put( ArenaService.rankPlayerIdMap.get(i), rank)
            println(ArenaService.rankPlayerIdMap.get(i),rank)
            rankPlayerIdnew.put(rank,ArenaService.rankPlayerIdMap.get(i))
            if(oldran != rank) {
              onAddArenaRank(ArenaService.rankPlayerIdMap.get(i), rank, areaKey)
            }
            rank += 1
          }
        }
        ({
          i += 1; i - 1
        })
      }
    ArenaService.rankPlayerIdMap.clear()
    ArenaService.rankPlayerIdMap.putAll(rankPlayerIdnew)
    if(ArenaService.arenaMap.size()!=ArenaService.rankPlayerIdMap.size()) {
      ArenaService.arenaMap.clear()
      for (count <- ArenaService.rankPlayerIdMap.keySet()) {
        ArenaService.arenaMap.put(ArenaService.rankPlayerIdMap.get(count), count)
      }
    }
  }

  def initLastRank(ranks: util.List[String]): Unit = {
    lastRank.clear()
    for (rank <- ranks) {
      val temp: Array[String] = rank.split("_")
      val playerId: Long = temp(0).toLong
      val pos: Int = temp(1).toInt
      lastRank.put(playerId, pos)
    }
  }

  def initArenaMap(ranks: util.List[String], lastranks: util.List[java.lang.String]): Unit = {
    ArenaService.arenaMap.clear()
    ArenaService.rankPlayerIdMap.clear()
    for (rank <- ranks) {
      val temp: Array[String] = rank.split("_")
      val playerId: Long = temp(0).toLong
      val pos: Int = temp(1).toInt
      ArenaService.arenaMap.put(playerId, pos)
      ArenaService.rankPlayerIdMap.put(pos, playerId)
     // DbProxy.tell(onRemoveArenaRank(playerId, pos, areaKey), self)
    }
    for (rank <- lastranks) {
      val temp: Array[String] = rank.split("_")
      val playerId: Long = temp(0).toLong
      val pos: Int = temp(1).toInt
      lastRank.put(playerId, pos)
    }
  }

  def checkTimer {
    val c: Calendar = Calendar.getInstance
    c.setTime(GameUtils.getServerDate)
    val hour: Int = c.get(Calendar.HOUR_OF_DAY)
    val minu: Int = c.get(Calendar.MINUTE)
    if (hour == TimerDefine.TIMER_REFRESH_ZERO) {
      lastRank.clear()
      lastRank.putAll(ArenaService.arenaMap)
      val list: util.List[ArenaRank] = new util.ArrayList[ArenaRank]
      import scala.collection.JavaConversions._
      for (id <- ArenaService.arenaMap.keySet) {
        val ar: ArenaRank = new ArenaRank(id, ArenaService.arenaMap.get(id), areaKey)
        list.add(ar)
      }

      //TODO 待优化 集合接口
      list.foreach( rank =>{
        BaseSetDbPojo.getSetDbPojo(classOf[ArenaLastRankSetDb], areaKey).addKeyValue(rank.playerId.toString, rank.rankVaalue.toLong)
      })
//      DbProxy.tell(RefArenaLastTimeRankList(list), self)
    }
  }


  val lastRank: util.Map[java.lang.Long, Integer] = new util.concurrent.ConcurrentHashMap[java.lang.Long, Integer]

  override def receive: Receive = {
    case GetAllArenaFromMoudle(cmd: String) =>
      onAllArenaFromMoudle(cmd)
    case AddArenaRank(playerId: Long, rankValue: Int, areaKey: String) =>
      onAddArenaRank(playerId, rankValue, areaKey)
    case GetAllArenaInfos(playerId: Long, cmd: String) =>
      onAllArenaInfos(playerId, cmd)
    case AddArenaRankList(ranks: java.util.List[ArenaRank]) =>
      onAddArenaRankList(ranks)
    case ChangeArenaRank(ranks: util.List[ArenaRank]) =>
      onChangeArenaRank(ranks)
    case GetSimplePlayerBysection(rivalId: java.lang.Long, playerId: java.lang.Long, cmd: String ,battle : PlayerBattle) =>
      onGetSimplePlayerBysection(rivalId, playerId, cmd,battle)
    case RestArena(feis: M7.FormationInfo, genel: Int, playerId: Long, arenaKey: String) =>
      println("RestArena")
      onRestArena(feis, genel, playerId, areaKey)
    case getLaskRank(playerId: Long) =>
      ongetLaskRank(playerId)
    case GetArenaRankInfos() =>
      getArenaMap()
 /*   case OnServerTrigger() =>
      checkTimer*/
    case askFight(rivalRank: Integer, playerId: java.lang.Long) =>
      onaskFight(rivalRank, playerId)
    case getArenaRank(playeridlist : util.List[java.lang.Long] , cmd : String) =>
      ongetArenaRank(playeridlist,cmd)
    case AddServerArenaReport(report : Report) =>
      onAddServerArenaReport(report)
    case GetAllServerArenaReport() =>
      onGetAllServerArenaReport()
    case GetOneServerArenaReport(id : Long) =>
      onGetOneServerArenaReport(id)
    case EachHourNotice() =>
      checkTimer
    case saveDateBeforeStop()=>
      stopService
    case _ =>
      log.warning("未知消息")

  }

  def ongetArenaRank(playeridlist : util.List[java.lang.Long] , cmd : String): Unit ={
    val map: util.Map[java.lang.Long, Integer] = new util.HashMap[java.lang.Long, Integer]
    for(playerId <- playeridlist){
     if(ArenaService.arenaMap.get(playerId)==null){
       map.put(playerId,-1)
     }else{
       map.put(playerId,ArenaService.arenaMap.get(playerId))
     }
    }
    sender() ! getArenaRankBack(map,cmd)
  }

  def onGetOneServerArenaReport(id : Long): Unit ={
    import scala.collection.JavaConversions._
    var res : Report = null
    for (report : Report <- reportList){
      if (report.getId == id){
        res = report
      }
    }
    sender() ! GetOneServerArenaReportBack(res)
  }

  def onGetAllServerArenaReport(): Unit ={
    sender() ! GetAllServerArenaReportBack(reportList)
  }

  def onAddServerArenaReport(r : Report): Unit ={
    val attackId = r.getAttackerId
    val defendId = r.getDefendId
    if (ArenaService.arenaMap.containsKey(attackId) && ArenaService.arenaMap.get(attackId) < 10
    && ArenaService.arenaMap.containsKey(defendId) && ArenaService.arenaMap.get(defendId) < 10){
      //只有双方都是十名以内才会有
      val n : Report = BaseDbPojo.create(classOf[Report],areaKey)
      n.setPlayerId(0)
      n.setAttackerId(attackId)
      n.setAttackerName(r.getAttackerName)
      n.setDefendId(r.getDefendId)
      n.setDefendName(r.getDefendName)
      n.setMessageId(r.getMessageId)
      n.setResult(r.getResult)
      n.setReportType(r.getReportType)
      n.setDefendSoldierTypeIds(r.getDefendSoldierTypeIds)
      n.setDefendSoldierNums(r.getDefendSoldierNums)
      n.setAttackVip(r.getAttackVip)
      n.setDefendVip(r.getDefendVip)
      n.setDefendLegion(r.getDefendLegion)
      n.setAttackLegion(r.getAttackLegion)
      n.setAttackSoldierNums(r.getAttackSoldierNums)
      n.setAttackSoldierTypeIds(r.getAttackSoldierTypeIds)
      n.setFirstHand(r.getFirstHand)
      n.setRead(1)
      n.setCreateTime(r.getCreateTime)
      n.save()
      addReport(n)
    }
  }

  val reportList : util.ArrayList[Report] = new util.ArrayList[Report]

  def addReport(r : Report): Unit ={
    val arenaReportSetDb : ArenaReportSetDb = BaseSetDbPojo.getSetDbPojo(classOf[ArenaReportSetDb],areaKey)
    if (reportList.size() > ArenaDefine.ARENA_SERVER_REPORT_SIZE){
      var delReport : Report = null
      import scala.collection.JavaConversions._
      for (report : Report <- reportList){
        if (delReport == null){
          delReport = report
        }else{
          if(report.getCreateTime < delReport.getCreateTime){
            delReport = report
          }
        }
      }
      delReport.del()
      reportList.remove(delReport)
      arenaReportSetDb.removeKey(delReport.getId+"")
    }
    reportList.add(r)
    arenaReportSetDb.addKeyValue(r.getId+"",0l)
  }

  def loadReports(): Unit ={
    val arenaReportSetDb : ArenaReportSetDb = BaseSetDbPojo.getSetDbPojo(classOf[ArenaReportSetDb],areaKey)
    val keys : util.Set[java.lang.String] = arenaReportSetDb.getAllKey
    import scala.collection.JavaConversions._
    for (key : java.lang.String <- keys){
      val report : Report = BaseDbPojo.getOfflineDbPojo(java.lang.Long.parseLong(key),classOf[Report],areaKey)
      if(report != null){
        reportList.add(report)
      }
    }
  }


  def onaskFight(rirank: Integer, playerId: java.lang.Long): Unit = {
    val rivalId = ArenaService.rankPlayerIdMap.get(rirank);
    val list: java.util.Set[java.lang.Long] = new util.HashSet[java.lang.Long]
    list.add(playerId)
    if(ArenaService.rankPlayerIdMap.get(rirank) != null) {
      list.add(ArenaService.rankPlayerIdMap.get(rirank))
    }
    val simplePlayers: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(list,areaKey)
    sender() ! GetPlayerSimpleInfoListSuccess(simplePlayers, ArenaDefine.CMD_ASK_FIGHT)
  }


  def stopService(): Unit ={
    val list: util.List[ArenaRank] = new util.ArrayList[ArenaRank]
    import scala.collection.JavaConversions._
    for (id <- ArenaService.arenaMap.keySet) {
      val ar: ArenaRank = new ArenaRank(id, ArenaService.arenaMap.get(id), areaKey)
      list.add(ar)
    }
    list.foreach( rank =>{
      BaseSetDbPojo.getSetDbPojo(classOf[ArenaRankSetDb], areaKey).addKeyValue(rank.playerId.toString, rank.rankVaalue.toLong)
    })
  }

  override def postStop() = {


//    DbProxy.tell(RefArenaLastTimeRankList(list), self)
  }


  def ongetLaskRank(playerId: Long): Unit = {
    var rank = lastRank.get(playerId)
    if (rank == null) {
      rank = -1
    }
    sender() ! getLaskRankSucess(rank)
  }

  def onRestArena(feis: M7.FormationInfo, genel: Int, playerId: Long, arenaKey: String): Unit = {
    val obj: Object = BaseSetDbPojo.getSetDbPojo(classOf[TeamDateSetDb], areaKey).getTeamData(playerId, SoldierDefine.FORMATION_ARENA) // DbProxy.ask(getTeamDate(areaKey, playerId, SoldierDefine.FORMATION_ARENA))
    var tool: PlayerTroop = null
    if (obj != None) {
      tool = obj.asInstanceOf[PlayerTroop]
    }
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId,areaKey)
    sender() ! RestArenaSuceess(feis, genel, ArenaService.arenaMap, tool, simplePlayer)
  }


  def onGetSimplePlayerBysection(rivalId: java.lang.Long, playerId: java.lang.Long, cmd: String,battle : PlayerBattle): Unit = {

    if (cmd.equals(ArenaDefine.CMD_ADD_PROTIME)) {
      val rivalrank: Int = ArenaService.arenaMap.get(rivalId)
      val playRank: Int = ArenaService.arenaMap.get(playerId)
      val playerList: util.Set[java.lang.Long] = getSomePlayer(rivalrank, playRank)
      val simplePlayers: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(playerList,areaKey)
      sender() ! GetPlayerSimpleInfoListSuccess(simplePlayers, cmd)
    } else if (cmd.equals(ArenaDefine.CMD_CHANGE_WINTIMES)) {
      val list: java.util.Set[java.lang.Long] = new util.HashSet[java.lang.Long]
      list.add(playerId)
      list.add(rivalId)
      val simplePlayers: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(list,areaKey)
      sender() ! GetWinTimesReward(simplePlayers, cmd,battle)
    }
  }


  def getSomePlayer(begin: Int, end: Int): util.Set[java.lang.Long] = {
    if (begin > end) {
      val oldbegin: Int = begin
      begin == end
      end == oldbegin
    }
    val list: java.util.Set[java.lang.Long] = new util.HashSet[java.lang.Long]
    import scala.collection.JavaConversions._
    for (entry <- ArenaService.arenaMap.entrySet) {
      if (entry.getValue >= begin && entry.getValue <= end) {
        list.add(entry.getKey)
      }
    }
    return list
  }

  def onChangeArenaRank(ranks: util.List[ArenaRank]): Unit = {
    for (rank <- ranks) {
      ArenaService.arenaMap.put(rank.playerId, rank.rankVaalue)
      ArenaService.rankPlayerIdMap.put(rank.rankVaalue, rank.playerId)
    }
    sender() ! changeSnucess()
  }

  def onAllArenaFromMoudle(cmd: String): Unit = {
    sender() ! GetAllArenaRankSuceess(ArenaService.arenaMap, cmd)
  }


  def onAddArenaRank(playerId: Long, rankValue: Int, areaKey: String): Unit = {
    ArenaService.arenaMap.put(playerId, rankValue);
    ArenaService.rankPlayerIdMap.put(rankValue, playerId)
   // rankPlayerIdMap.put(rankValue, playerId)
//    DbProxy.tell(AddArenaRank(playerId, rankValue, areaKey), self)
    BaseSetDbPojo.getSetDbPojo(classOf[ArenaRankSetDb], areaKey).addKeyValue(playerId.toString, rankValue.toLong)
  }

  def onAddArenaRankList(ranks: java.util.List[ArenaRank]): Unit = {
//    DbProxy.tell(AddArenaRankList(ranks), self)
    ranks.foreach( rank => {  //TODO 需要优化 列表更新 而不是一个一个更新
      BaseSetDbPojo.getSetDbPojo(classOf[ArenaRankSetDb], areaKey).addKeyValue(rank.playerId.toString, rank.rankVaalue.toLong)
    })
  }


  def onAllArenaInfos(playerId: Long, cmd: String): Unit = {
    val message: M20.M200000.S2C.Builder = M20.M200000.S2C.newBuilder()
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId,areaKey)
    var rs: Int = 0
    try {
      if (null == simplePlayer.getArenaTroop.getFightElementInfos || simplePlayer.getArenaTroop == null) {
        rs = ErrorCodeDefine.M200000_1
      }
    } catch {
      case e: Exception => {
        rs = ErrorCodeDefine.M200000_1
      }
    }
    var lastrank: Int = -1
    if (lastRank.get(playerId) != null) {
      lastrank = lastRank.get(playerId)
    }
    message.setLasttimes(lastrank)
    val rivals: util.List[Integer] = new util.ArrayList[Integer]();
    message.setRs(rs)
    val myrank = ArenaService.arenaMap.get(playerId);
    if (myrank != null && simplePlayer.getArenaTroop != null) {
      message.setWintimes(simplePlayer.getArenaTroop.getWintimes)
      //TODO 获得对手的 SimplePlayer 包括自己
      try {
        val simplePlayers: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(getDivis(myrank),areaKey)
        val sendList: util.ArrayList[SimplePlayer] =new util.ArrayList[SimplePlayer]();
        for(sim <- simplePlayers ){
           if(sim!=null&&sim.getId>0&&sim.getArenaTroop !=null){
             sendList.add(sim)
           }
          if(sim!=null&&sim.getId<0){
            sendList.add(sim)
          }
        }
        /** *返回后处理的数据给他初始化 ***/
        message.setChallengetimes(0)
        sender() ! GetAllArenaInfosSucess(message, cmd, rivals, ArenaService.arenaMap,  sendList, rs)
      }
      catch {
        case e: Exception => {
          System.err.println(e)
        }
      }
    } else {
      message.setWintimes(-1)
      sender() ! GetAllArenaInfosSucess(message, cmd, rivals, ArenaService.arenaMap, null, rs)
    }
  }


  def getDivis(rank: Int): java.util.Set[java.lang.Long] = {
    val list: java.util.Set[java.lang.Long] = new util.HashSet[java.lang.Long]
    list.add(ArenaService.rankPlayerIdMap.get(rank))
    if (rank == 1) {
      {
        var i: Int = 1
        while (i <= 5) {
          {
            if (ArenaService.rankPlayerIdMap.get(rank + i) != null) {
              list.add(ArenaService.rankPlayerIdMap.get(rank + i));
            }
          }
          ({
            i += 1;
            i - 1
          })
        }
      }
    }
    else if (rank == 2) {
      list.add(ArenaService.rankPlayerIdMap.get(1))
      var i: Int = 1
      while (i <= 4) {
        {
          if (ArenaService.rankPlayerIdMap.get(rank + i) != null) {
            list.add(ArenaService.rankPlayerIdMap.get(rank + i));
          }
        }
        ({
          i += 1;
          i - 1
        })
      }
    }
    else if (rank == 3) {
      list.add(ArenaService.rankPlayerIdMap.get(1))
      list.add(ArenaService.rankPlayerIdMap.get(2))
      var i: Int = 1
      while (i <= 3) {
        {
          if (ArenaService.rankPlayerIdMap.get(rank + i) != null) {
            list.add(ArenaService.rankPlayerIdMap.get(rank + i));
          }
        }
        ({
          i += 1;
          i - 1
        })
      }
    }
    else if (rank == 4) {
      list.add(ArenaService.rankPlayerIdMap.get(1))
      list.add(ArenaService.rankPlayerIdMap.get(2))
      list.add(ArenaService.rankPlayerIdMap.get(3))

      var i: Int = 1
      while (i <= 2) {
        {
          if (ArenaService.rankPlayerIdMap.get(rank + i) != null) {
            list.add(ArenaService.rankPlayerIdMap.get(rank + i));
          }
        }
        ({
          i += 1;
          i - 1
        })
      }

    }
    else {
      val jsonObject: JSONObject = getFightJson(rank)
      var i: Int = 1
      while (i <= 4) {
        {
          if(ArenaService.rankPlayerIdMap.get(rank - jsonObject.getInt("upadd") * i)!=null) {
            list.add(ArenaService.rankPlayerIdMap.get(rank - jsonObject.getInt("upadd") * i))
          }
        }
        ({
          i += 1;
          i - 1
        })
      }
      if (ArenaService.rankPlayerIdMap.get(rank + jsonObject.getInt("downadd")) != null) {
        list.add((ArenaService.rankPlayerIdMap.get(rank + jsonObject.getInt("downadd"))))
      }
    }
    return list
  }

  def getFightJson(rank: Int): JSONObject = {
    import scala.collection.JavaConversions._
    for (jsonObject <- ConfigDataProxy.getConfigAllInfo(DataDefine.ARENA_RANK)) {
      if (jsonObject.getInt("rankmin") <= rank && jsonObject.getInt("ranmax") >= rank) {
        return jsonObject
      }
    }
    return null
  }

  /**
    * 返回竞技场数据
    */
  def getArenaMap(): Unit = {
    val message: M21.M210000.S2C.Builder = M21.M210000.S2C.newBuilder
    val infos: M21.RankListInfo.Builder = M21.RankListInfo.newBuilder()
    val rankType = PowerRanksDefine.POWERRANK_TYPE_ARENA
    var i = 1
    var len = ArenaService.rankPlayerIdMap.size()
    while (len > 0) {
      try {
        if (infos.getPowerRankInfoList.size() < PowerRanksDefine.MAX_SHOW_RANK_NUM) {
          val info:M21.PowerRankInfo.Builder =M21.PowerRankInfo.newBuilder()
          val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(ArenaService.rankPlayerIdMap.get(i),areaKey)
          if (simplePlayer != null) {
            info.setTypeId(rankType)
            info.setRank(i)
            info.setPlayerId(simplePlayer.getId)
            if(simplePlayer.arenaTroop!=null) {
              info.setRankValue(simplePlayer.arenaTroop.getCapity)
            }else{
              info.setRankValue(0)
            }
            if(simplePlayer.getId<0){
              info.setRankValue(simplePlayer.getCapacity)
            }
            info.setLevel(simplePlayer.getLevel())
            info.setName(simplePlayer.getName())
            infos.addPowerRankInfo(info)
          }else{
            CustomerLogger.error("获得玩家的竞技场数据时得到的对方是个空！！！！！！！！！！！！！！")
          }
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          CustomerLogger.error("获得玩家的竞技场数据时出错！！！！！！！！！！！！！！",e)
      }
      if (infos.getPowerRankInfoList.size() >= PowerRanksDefine.MAX_SHOW_RANK_NUM){
        len =0//超过100人了就跳出来吧
      }
      i += 1
      len -= 1
    }
    infos.setTypeId(rankType)
    sender() ! GetArenaRankMap(message.build())
  }

}


