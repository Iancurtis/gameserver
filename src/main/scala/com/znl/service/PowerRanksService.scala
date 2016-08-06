package com.znl.service

import java.util.{Date, Calendar}
import java.{util, lang}

import akka.actor._
import com.znl.GameMainServer
import com.znl.base.{BaseLog, BaseDbPojo, BaseSetDbPojo}
import com.znl.core.{ArenaRank, PowerRanks, SimplePlayer}
import com.znl.define._
import com.znl.log.CustomerLogger
import com.znl.log.admin.tbllog_activityrank
import com.znl.msg.GameMsg._
import com.znl.pojo.db.{Armygroup, Player, LimitDungeoReport}
import com.znl.pojo.db.set.{ArenaLastRankSetDb, LimitDungeonNearSetDb, LimitDungeonFastSetDb, PlayerRankSetDb}
import com.znl.proto.{M6, M21}
import com.znl.proxy.{ConfigDataProxy, PlayerProxy, ActivityProxy, DbProxy}
import com.znl.service.trigger.{TriggerType, TriggerEvent}
import com.znl.utils.{GameUtils, DateUtil, SortUtil}
import org.json.JSONObject

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
  * 排行榜
  * Created by Administrator on 2015/12/24.
  */
object PowerRanksService {
  def props(areaKey: String) = {
    Props(classOf[PowerRanksService], areaKey)
  }
  val rankPowerList: util.Map[Integer, util.List[PowerRanks]] = new util.concurrent.ConcurrentHashMap[Integer, util.List[PowerRanks]]
  val lastRankPowerList: util.Map[Integer, util.List[PowerRanks]] = new util.concurrent.ConcurrentHashMap[Integer, util.List[PowerRanks]]


  /**
    * 获取某个类型的排行榜
    */
  def onGetRankByType(rankType: Int,playerId :Long,areaKey :String): M21.RankListInfo.Builder = {
    val rankList = PowerRanksService.lastRankPowerList.get(rankType)
    val infos: M21.RankListInfo.Builder = M21.RankListInfo.newBuilder()
    if(rankList==null){
      return  infos
    }
    SortUtil.anyProperSort(rankList, "getValue", false)
    var sizeNum = rankList.size()
    var myinfo: M21.PowerRankInfo.Builder = M21.PowerRankInfo.newBuilder()
    var i = 0
    while (sizeNum > 0) {
      try {
        val info: M21.PowerRankInfo.Builder = M21.PowerRankInfo.newBuilder()
        val powerRank: PowerRanks = rankList.get(i)
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(powerRank.getPlayerId,areaKey)
          if (simplePlayer != null) {
        info.setTypeId(rankType)
        info.setRank(i + 1)
        info.setPlayerId(rankList.get(i).getPlayerId())
        info.setRankValue(rankList.get(i).getValue())
        if (rankType == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) {
          info.setLevel(powerRank.getAtklv)
        } else if (rankType == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) {
          info.setLevel(powerRank.getCritlv)
        } else if (rankType == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) {
          info.setLevel(powerRank.getDogelv)
        } else {
          info.setLevel(powerRank.getSumLevel)
        }
        info.setName(powerRank.getName())
        infos.addPowerRankInfo(info)
         if(playerId==simplePlayer.getId){
           myinfo = info
           infos.setMyRank(myinfo)
         }
        }
        sizeNum -= 1
        i += 1
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
    infos.setTypeId(rankType)
    infos
  }




  def GetRanks(playerid :Long): util.Map[Integer,Integer] ={
    val rankmap:util.Map[Integer,Integer]=new util.HashMap[Integer,Integer]()
    var  retype:Int =1
    while (retype<=PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE) {
      val rankList =lastRankPowerList.get(retype)
      if(rankList!=null) {
        SortUtil.anyProperSort(rankList, "getValue", false)
        rankmap.put(retype, 0)
        var rank: Int = 0
        for (pr <- rankList) {
          rank = rank + 1
          if (playerid == pr.getPlayerId) {
            rankmap.put(retype, rank)
          }
        }
      }
      retype=retype+1
    }
    rankmap
  }

}

class PowerRanksService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {
  val reportlist: util.List[LimitDungeoReport] = new util.ArrayList[LimitDungeoReport]

  override def receive: Receive = {
    case GetAnRankByType(rankType: Int) =>
      onGetAllRankByType(rankType)
    case AddPlayerToRank(playerId: Long, value: Long, rankType: Int) =>
      onAddPlayerToPowerRank(playerId, value, rankType)
    case OnServerTrigger() =>
      onUpdateAllRankByType()
    //checkTimer
    case getLimitChangetInfo(builder: M6.M60100.S2C.Builder, playerId: Long, dungId: Int) =>
      ongetLimitChangetInfo(builder, playerId, dungId)
    case AddLimitchangeNearList(id: Long, dungeoOrder: Int, playerId: Long) =>
      addlimitNearList(dungeoOrder, id, playerId)
    case saveDateBeforeStop() =>
      save()
    case ActivityRankTrigger(ids:util.List[java.lang.Integer]) =>
      checkTimer(ids)
    case getMyRanks(playerId:Long) =>
      logionGetRanks(playerId)
    case _ =>
      log.warning("未知消息")
  }

  def checkTimer(ids:util.List[java.lang.Integer]) {
    val list: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_DESIGN, "uitype", ActivityDefine.POWER_RANK_UITYPE)
    val c: Calendar = Calendar.getInstance
    c.setTime(GameUtils.getServerDate)
    val serverYear: Int = c.get(Calendar.YEAR)
    val serverMonth: Int = c.get(Calendar.MONTH)
    val serverDay: Int = c.get(Calendar.DAY_OF_MONTH)
    val serverHour: Int = c.get(Calendar.HOUR_OF_DAY)
    val serverMinute: Int = c.get(Calendar.MINUTE)
    val serverSecond: Int = c.get(Calendar.SECOND)
    for (define <- list) {
    /*  val endTime: Long = getActivityEndTime(define)
      val endCalender: Calendar = Calendar.getInstance
      endCalender.setTimeInMillis(endTime)
      val endserverYear: Int = endCalender.get(Calendar.YEAR)
      val endserverMonth: Int =endCalender.get(Calendar.MONTH)
      val endserverDay: Int = endCalender.get(Calendar.DAY_OF_MONTH)
      val endserverHour: Int = endCalender.get(Calendar.HOUR_OF_DAY)
      val endserverMinute: Int = endCalender.get(Calendar.MINUTE)
      val endserverSecond: Int = endCalender.get(Calendar.SECOND)
      if (endserverYear == serverYear
        && endserverMonth == serverMonth
        && endserverDay == serverDay
        && endserverHour == serverHour
        && endserverMinute == serverMinute
        && endserverSecond == serverSecond) {*/
        //年月日时分秒都一致就发吧
        if(ids.contains(define.getInt("ID"))){
        val effectId = define.getInt("effectID")
        val effectDefineList: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT, "effectID", effectId)
        if (effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_CAPITY_RANK) {
          addActivity(ActivityDefine.ACTIVITY_CONDITION_TYPE_CAPITY_RANK, PowerRanksDefine.POWERRANK_TYPE_CAPACITY)
        }
        if (effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_GUANQIA_RANK) {
          addActivity(ActivityDefine.ACTIVITY_CONDITION_TYPE_GUANQIA_RANK, PowerRanksDefine.POWERRANK_TYPE_CUSTOMS)
        }
        if (effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_HONOR_RANK) {
          addActivity(ActivityDefine.ACTIVITY_CONDITION_TYPE_HONOR_RANK, PowerRanksDefine.POWERRANK_TYPE_HONOR)
        }
        /*if (effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK) {
          tellService(ActorDefine.ARMYGROUP_SERVICE_NAME, CheckAndSendActivity(ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK,areaKey))
        }*/
        onUpdateAllRankByType()
      }
    }
    getActivityTimer()
  }

  def addActivity(acType: Int, ranktype: Int): Unit = {
    val powerlist: util.List[PowerRanks] = PowerRanksService.rankPowerList.get(ranktype)
    var index: Int = 1
    for (rankInfo <- SortUtil.anyProperSort(powerlist, "getValue", false)) {
      if (index < PowerRanksDefine.MAX_SHOW_RANK_NUM) {
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(rankInfo.getPlayerId,areaKey)
        if (simplePlayer != null) {
         sendRankLog(simplePlayer.getId,index,ranktype)
          if (simplePlayer.online == true) {
            sendMsgToRoleModule(simplePlayer.getAccountName, addAtivity(acType, index, 0))
          } else {
            val player = BaseDbPojo.getOfflineDbPojo(rankInfo.getPlayerId, classOf[Player], areaKey)
            val activityProxy: ActivityProxy = new ActivityProxy(player.getActivitySet, areaKey)
            val playerProxy: PlayerProxy = new PlayerProxy(player, areaKey)
            player.save()
            activityProxy.reloadDefineData(playerProxy)
            activityProxy.addActivityConditionValue(acType, index, playerProxy, 0)
            activityProxy.saveActivity()
          }
        }
      }
      index = index + 1
    }
  }


  def save() = {
    for (report <- reportlist) {
      BaseSetDbPojo.getSetDbPojo(classOf[LimitDungeonNearSetDb], areaKey).addLimitDungeonSet(report.getDungeoId, report.getBattleId, report.getPlayerId, report.getTime)
    }
  }
  val deleEven:util.List[TriggerEvent] =new util.ArrayList[TriggerEvent]()
  override def preStart() = {
    /*    BaseSetDbPojo.getSetDbPojo(classOf[LimitDungeonNearSetDb], areaKey).removeAllKey()
        BaseSetDbPojo.getSetDbPojo(classOf[LimitDungeonFastSetDb], areaKey).removeAllKey()*/
    reportlist.clear()
    reportlist.addAll(BaseSetDbPojo.getSetDbPojo(classOf[LimitDungeonNearSetDb], areaKey).getLimitDungeoReports)
    BaseSetDbPojo.getSetDbPojo(classOf[PlayerRankSetDb], areaKey)
    PowerRanksService.rankPowerList.clear()
    val playerRankSetDb = BaseSetDbPojo.getSetDbPojo(classOf[PlayerRankSetDb], areaKey)
    val capacity: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_CAPACITY) // DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_CAPACITY))
    initPowerMap(capacity, PowerRanksDefine.POWERRANK_TYPE_CAPACITY)
    val compile: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_COMPILE) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_COMPILE))
    initPowerMap(compile, PowerRanksDefine.POWERRANK_TYPE_COMPILE)
    val customs: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_CUSTOMS) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_CUSTOMS))
    initPowerMap(customs, PowerRanksDefine.POWERRANK_TYPE_CUSTOMS)
    val honor: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_HONOR) // DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_HONOR))
    initPowerMap(honor, PowerRanksDefine.POWERRANK_TYPE_HONOR)
    val atk: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN))
    initPowerMap(atk, PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN)
    val crit: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN))
    initPowerMap(crit, PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN)
    val dodge: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN))
    initPowerMap(dodge, PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN)
    val arena: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_ARENA) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_ARENA))
    initPowerMap(arena, PowerRanksDefine.POWERRANK_TYPE_ARENA)
    val achivement: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_ACHIEVEMENT) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_ACHIEVEMENT))
    initPowerMap(achivement, PowerRanksDefine.POWERRANK_TYPE_ACHIEVEMENT)
    val limitchange: util.List[String] = playerRankSetDb.getAllRankByType(PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE) //DbProxy.ask(GetAllRankByType(areaKey,PowerRanksDefine.POWERRANK_TYPE_ACHIEVEMENT))
    initPowerMap(limitchange, PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE)

    import context.dispatcher
    context.system.scheduler.schedule(5 minute, 5 minute, context.self, OnServerTrigger())
    getActivityTimer()
    onUpdateAllRankByType()
  }

  def getActivityTimer(): Unit = {
    val list: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_DESIGN, "uitype", ActivityDefine.POWER_RANK_UITYPE)
    val now = GameUtils.getServerDate().getTime
    var end = 0l
    val ids : util.List[java.lang.Integer] =new util.ArrayList[java.lang.Integer]()
    for (define <- list) {
      val timeType = define.getInt("timetype")
      if (timeType == 2) {
        val endTime = getActivityEndTime(define)
        if (endTime > 0 && endTime > now) {
          if (end == 0 || end > endTime) {
            end = endTime
          }
        }
      }
    }
    for (define <- list) {
      val timeType = define.getInt("timetype")
      if (timeType == 2) {
        val endTime = getActivityEndTime(define)
          if ( end ==endTime) {
            ids.add(define.getInt("ID"))
          }
      }
    }
    if (end > 0) {
      for(del <-  deleEven){
        getTriggerService(context) ! RemoveTriggerEvent(del)
      }
      deleEven.clear()
      val event = new TriggerEvent(self, ActivityRankTrigger(ids), TriggerType.COUNT_DOWN, (end / 1000 - now / 1000).toInt)
      getTriggerService(context) ! AddTriggerEvent(event)
      deleEven.add(event)
    }
//    import context.dispatcher
//    context.system.scheduler.schedule(1 seconds, 1 seconds, context.self, ActivityRankTrigger())
  }

  private def getActivityEndTime(define: JSONObject): Long = {
    val timeType: Int = define.getInt("timetype")
    var endTime: Long = -1
    timeType match {
      case 1 => {
        //        val openServerDate: Date = GameMainServer.getOpenServerDateByAreaKey(areaKey)
        //        val calendar: Calendar = Calendar.getInstance
        //        calendar.setTime(openServerDate)
        //        calendar.set(Calendar.HOUR_OF_DAY, 0)
        //        calendar.set(Calendar.MINUTE, 0)
        //        calendar.set(Calendar.SECOND, 0)
        //        calendar.set(Calendar.MILLISECOND, 0)
        //        calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"))
        //        val starTime: Long = calendar.getTimeInMillis
        //        endTime = starTime + (define.getInt("timeB") * 60 * 1000)
        CustomerLogger.error("排行榜居然出现时间类型为1的定时器！！！")
      }
      case 2 => {
        val openServerDate: Date = GameMainServer.getOpenServerDateByAreaKey(areaKey)
        val calendar: Calendar = Calendar.getInstance
        calendar.setTime(openServerDate)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"))
        val timeB: Int = define.getInt("timeB")
        val hour: Int = timeB / 10000
        val minute: Int = timeB / 100 % 100
        val seconds: Int = timeB % 100
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, seconds)
        val endDate: Date = calendar.getTime
        endTime = endDate.getTime
      }
      case 3 => {
        CustomerLogger.error("排行榜居然出现时间类型为3的定时器！！！")
      }
      case 4 => {
        val endDate: Date = GameUtils.getDateFromStryyyyMMdd(define.getInt("timeB") + "")
        endTime = endDate.getTime
        CustomerLogger.error("排行榜居然出现时间类型为4的定时器！！！")
      }
    }
    return endTime
  }

  def initPowerMap(ranks: util.List[String], rankType: java.lang.Integer): Unit = {
    val tempPlayerList = new util.ArrayList[PowerRanks]()
    val playerList = new util.ArrayList[PowerRanks]()
    for (rank <- ranks) {
      val powerValue = new PowerRanks()
      val temp: Array[String] = rank.split("_")
      val playerId: Long = temp(0).toLong
      val values: Long = temp(1).toLong
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId,areaKey)
      if (simplePlayer != null) {
        powerValue.setType(rankType)
        powerValue.setAreaKey(areaKey)
        powerValue.setPlayerId(playerId)
        powerValue.setValue(values)
        tempPlayerList.add(powerValue)
      }

    }
    for (rankInfo <- SortUtil.anyProperSort(tempPlayerList, "getValue", false)) {
      if (playerList.size() < PowerRanksDefine.MAX_SHOW_RANK_NUM) {
        playerList.add(rankInfo)
      }
    }
    PowerRanksService.rankPowerList.put(rankType, playerList)
    PowerRanksService.lastRankPowerList.put(rankType, playerList)
  }

  /**
    * 把玩家加到排行榜
    */
  def onAddPlayerToPowerRank(playerId: Long, rankValue: Long, rankType: Int): Unit = {
    val powerValue = new PowerRanks()
    powerValue.setType(rankType)
    powerValue.setAreaKey(areaKey)
    powerValue.setPlayerId(playerId)
    powerValue.setValue(rankValue)
    val rankNewPlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId,areaKey)
    if("".equals(rankNewPlayer.getName)){
      return
    }
    powerValue.setAtklv(rankNewPlayer.getAtklv)
    powerValue.setCritlv(rankNewPlayer.getCritlv)
    powerValue.setDogelv(rankNewPlayer.getDogelv)
    powerValue.setSumLevel(rankNewPlayer.getLevel)
    powerValue.setName(rankNewPlayer.getName)
    val playerList = PowerRanksService.rankPowerList.get(rankType)
    //排行榜玩家数大于等于100
    if (playerList.size() >= PowerRanksDefine.MAX_SHOW_RANK_NUM) {
      val lastPlayerInfo = playerList.get(playerList.size() - 1)
      if (lastPlayerInfo.getValue <= rankValue) {
        //排行榜已存在刚添加进来的，先删除
        var tempDel: PowerRanks = null
        for (info: PowerRanks <- playerList) {
          if (info.getPlayerId == playerId) {
            tempDel = info
          }
        }
        if (tempDel != null) {
          playerList.remove(tempDel)
        }
        //排行榜已存在刚添加进来的，先删除
        var i = 0
        var flag: Boolean = true
        val len = playerList.size()
        while (flag) {
          i += 1
          if (playerList.get(0).getValue < rankValue) {
            //大于第一名，排第一
            playerList.add(0, powerValue)
            flag = false
          } else if (playerList.get(len - i).getValue < rankValue && playerList.get(len - (i + 1)).getValue >= rankValue) {
            if (playerList.get(len - i).getValue == rankValue) {
              val rankOldPlayer: SimplePlayer = PlayerService.getSimplePlayer(playerList.get(len - i).getPlayerId,areaKey)
              if (rankOldPlayer.getLevel < rankNewPlayer.getLevel) {
                playerList.add(len - i, powerValue)
                flag = false
              } else {
                playerList.add(len - (i + 1), powerValue)
                flag = false
              }
            } else {
              playerList.add(len - i, powerValue)
              flag = false
            }
          }
          if(i >= len){
             flag=false
          }
        }
        //保留前100名排名
        while (playerList.size() > PowerRanksDefine.MAX_SHOW_RANK_NUM) {
          playerList.remove(playerList.size() - 1)
        }

        val rankSetKey = this.getRankSetKey(playerId, rankType)
        val playerRankSetDb = BaseSetDbPojo.getSetDbPojo(classOf[PlayerRankSetDb], areaKey)
        playerRankSetDb.addKeyValue(rankSetKey, rankValue)
        //            DbProxy.tell(AddPlayerIntoRank(playerId, rankValue, areaKey, rankType), self)
      }
    } else {
      //排行榜玩家数小于100
      if (playerList.size() == 0) {
        playerList.add(powerValue)
        val rankSetKey = this.getRankSetKey(playerId, rankType)
        val playerRankSetDb = BaseSetDbPojo.getSetDbPojo(classOf[PlayerRankSetDb], areaKey)
        playerRankSetDb.addKeyValue(rankSetKey, rankValue)
        //          DbProxy.tell(AddPlayerIntoRank(playerId, rankValue, areaKey, rankType), self)
      } else {
        //排行榜已存在刚添加进来的，先删除
        var tempDel: PowerRanks = null
        for (info: PowerRanks <- playerList) {
          if (info.getPlayerId == playerId) {
            tempDel = info
          }
        }
        if (tempDel != null) {
          playerList.remove(tempDel)
        }
        //排行榜已存在刚添加进来的，先删除
        val len = playerList.size()
        var i = 0
        var flag: Boolean = true
        //数量大于等于2条，开始判断改插入区间
        if (len >= 2) {
          if (playerList.get(len - 1).getValue <= rankValue) {
            while (flag) {
              i += 1
              try {
                if (playerList.get(0).getValue < rankValue) {
                  //大于第一名，排第一
                  playerList.add(0, powerValue)
                  flag = false
                } else if (playerList.get(len - i).getValue <= rankValue && playerList.get(len - (i + 1)).getValue >= rankValue) {
                  if (playerList.get(len - i).getValue == rankValue) {
                    val rankOldPlayer: SimplePlayer = PlayerService.getSimplePlayer(playerList.get(len - i).getPlayerId,areaKey)
                    val rankNewPlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId,areaKey)
                    if (rankOldPlayer.getLevel < rankNewPlayer.getLevel) {
                      playerList.add(len - i, powerValue)
                      flag = false
                    } else {
                      playerList.add(len - (i + 1), powerValue)
                      flag = false
                    }
                  } else {
                    playerList.add(len - i, powerValue)
                    flag = false
                  }
                }
              } catch {
                case e: Exception =>
                  e.printStackTrace()
              }
            }
          } else {
            playerList.add(powerValue)
          }
        } else {
          if (len == 1) {
            if (playerList.get(len - 1).getValue < rankValue) {
              playerList.add(len - 1, powerValue)
            } else {
              playerList.add(powerValue)
            }
          } else {
            playerList.add(powerValue)
          }
        }
        //            DbProxy.tell(AddPlayerIntoRank(playerId, rankValue, areaKey, rankType), self)
        val rankSetKey = this.getRankSetKey(playerId, rankType)
        val playerRankSetDb = BaseSetDbPojo.getSetDbPojo(classOf[PlayerRankSetDb], areaKey)
        playerRankSetDb.addKeyValue(rankSetKey, rankValue)
      }
    }
    PowerRanksService.rankPowerList.put(rankType, playerList)
  }

  /**
    * 更新排行榜
    */
  def onUpdateAllRankByType(): Unit = {
  //  initCapityRank()
    PowerRanksService.lastRankPowerList.clear()
    var num: Int = 1
    while (num <= PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE) {
      val rankList = PowerRanksService.rankPowerList.get(num)
      var sizeNum = rankList.size()
      /*  val message: M21.M210000.S2C.Builder = M21.M210000.S2C.newBuilder
        val info: M21.PowerRankInfo.Builder = M21.PowerRankInfo.newBuilder()
        val myinfo: M21.PowerRankInfo.Builder = M21.PowerRankInfo.newBuilder()*/
      var i = 0
      val list: util.List[PowerRanks] = new util.ArrayList[PowerRanks]()
      while (sizeNum > 0) {
        try {
          var simplePlayer: SimplePlayer = null
          if(rankList.get(i)!=null){
            simplePlayer =   PlayerService.getSimplePlayer(rankList.get(i).getPlayerId,areaKey)
          }
          val powerValue: PowerRanks = rankList.get(i)
          if (simplePlayer != null) {
            powerValue.setAtklv(simplePlayer.getAtklv)
            powerValue.setCritlv(simplePlayer.getCritlv)
            powerValue.setDogelv(simplePlayer.getDogelv)
            powerValue.setSumLevel(simplePlayer.getLevel)
            powerValue.setName(simplePlayer.getName)
            list.add(powerValue)
            /*   info.setTypeId(PowerRanksDefine.POWERRANK_TYPE_CAPACITY)
               info.setRank(i + 1)
               info.setPlayerId(rankList.get(i).getPlayerId())
               info.setRankValue(rankList.get(i).getValue())
               info.setLevel(simplePlayer.getLevel())
               info.setName(simplePlayer.getName())
               message.addPowerRankInfo(info)*/
            if(simplePlayer.online){
               sendMsgToRoleModule(simplePlayer.getAccountName,changeRankBytype(num,i+1))
            }
            sizeNum -= 1
            i += 1
          }
        }catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
      PowerRanksService.lastRankPowerList.put(num, list)
      num = num + 1
    }
    /*  rankPowerList.clear()
      rankPowerList.putAll(lastRankPowerList)*/
  }

  //登陆获得自己排行榜的排名
  def logionGetRanks(playerid :Long): Unit ={
   val rankmap:util.Map[Integer,Integer]=new util.HashMap[Integer,Integer]()
    var  retype:Int =1
    while (retype<=PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE) {
      val rankList = PowerRanksService.lastRankPowerList.get(retype)
      if(rankList!=null) {
        SortUtil.anyProperSort(rankList, "getValue", false)
        rankmap.put(retype, 0)
        var rank: Int = 0
        for (pr <- rankList) {
          rank = rank + 1
          if (playerid == pr.getPlayerId) {
            rankmap.put(retype, rank)
          }
        }
      }
      retype=retype+1
    }
    sender() ! getMyRanksback(rankmap)
  }


  def initCapityRank(): Unit = {
    val rankList = PowerRanksService.rankPowerList.get(PowerRanksDefine.POWERRANK_TYPE_CAPACITY)
    val checklist: util.List[PowerRanks] = new util.ArrayList[PowerRanks]()
    checklist.addAll(rankList)
    for (rank <- checklist) {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(rank.getPlayerId,areaKey)
      onAddPlayerToPowerRank(simplePlayer.getId, simplePlayer.getCapacity, PowerRanksDefine.POWERRANK_TYPE_CAPACITY)
    }
  }

  def ongetLimitChangetInfo(builder: M6.M60100.S2C.Builder, playerId: Long, duongId: Int): Unit = {
    val rankList = PowerRanksService.rankPowerList.get(PowerRanksDefine.POWERRANK_TYPE_LIMITCHANGE)
    SortUtil.anyProperSort(rankList, "getValue", false)
    var index = 1
    var myindexInfo: M6.IndexInfo.Builder = null
    for (rank <- rankList) {
      if (index <= PowerRanksDefine.MAX_SHOW_RANK_NUM) {
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(rank.getPlayerId,areaKey)
        if (simplePlayer != null) {
          val indexInfo: M6.IndexInfo.Builder = M6.IndexInfo.newBuilder()
          indexInfo.setIndex(index)
          indexInfo.setFight(simplePlayer.getCapacity.toInt)
          indexInfo.setName(simplePlayer.getName)
          indexInfo.setGrade(rank.getValue.toInt)
          builder.addAllIndexInfo(indexInfo)
          if (rank.getPlayerId == playerId) {
            myindexInfo = indexInfo
          }
          index = index + 1
        }
      }
    }
    val mysimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(playerId,areaKey)
    if (myindexInfo == null) {
      val indexInfo: M6.IndexInfo.Builder = M6.IndexInfo.newBuilder()
      indexInfo.setIndex(-1)
      indexInfo.setGrade(0)
      indexInfo.setFight(mysimplePlayer.getCapacity.toInt)
      indexInfo.setName(mysimplePlayer.getName)
      myindexInfo = indexInfo
    }
    builder.setMyIndexInfo(myindexInfo)
    //TODO 首次通关 最近通关
    val fastreport: LimitDungeoReport = BaseSetDbPojo.getSetDbPojo(classOf[LimitDungeonFastSetDb], areaKey).getLimitDungeonFastReport(duongId)
    if (fastreport != null) {
      val fastsimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(fastreport.getPlayerId,areaKey)
      val passinfo: M6.PassInfo.Builder = M6.PassInfo.newBuilder
      passinfo.setBattleId(fastreport.getBattleId)
      passinfo.setLv(fastsimplePlayer.getLevel)
      passinfo.setName(fastsimplePlayer.getName)
      passinfo.setTime(DateUtil.getDateFormt(fastreport.getTime))
      builder.setFirstPass(passinfo.build())
    }
    val list: util.List[LimitDungeoReport] = getlimitNearList(duongId)
    SortUtil.anyProperSort(list, "getTime", false)
    for (report <- list) {
      val nearsimplePlayer: SimplePlayer = PlayerService.getSimplePlayer(report.getPlayerId,areaKey)
      val passinfo: M6.PassInfo.Builder = M6.PassInfo.newBuilder
      passinfo.setBattleId(report.getBattleId)
      passinfo.setLv(nearsimplePlayer.getLevel)
      passinfo.setName(nearsimplePlayer.getName)
      passinfo.setTime(DateUtil.getDateFormt(report.getTime()))
      builder.addNearPass(passinfo.build())
    }
    sender() ! getLimitChangetInfoBack(builder)
  }

  /**
    * 获取某个类型的排行榜
    */
  def onGetAllRankByType(rankType: Int): Unit = {
    val rankList = PowerRanksService.lastRankPowerList.get(rankType)
    SortUtil.anyProperSort(rankList, "getValue", false)
    var sizeNum = rankList.size()
    val message: M21.M210000.S2C.Builder = M21.M210000.S2C.newBuilder
    val infos: M21.RankListInfo.Builder = M21.RankListInfo.newBuilder()
    val myinfo: M21.PowerRankInfo.Builder = M21.PowerRankInfo.newBuilder()
    var i = 0
    while (sizeNum > 0) {
      try {
        // val simplePlayer: SimplePlayer = askService(context, ActorDefine.PLAYER_SERVICE_NAME, AskOnePlayerSimplePlayer(rankList.get(i).getPlayerId))
        val info: M21.PowerRankInfo.Builder = M21.PowerRankInfo.newBuilder()
        val powerRank: PowerRanks = rankList.get(i)
        //  if (simplePlayer != null) {
        info.setTypeId(rankType)
        info.setRank(i + 1)
        info.setPlayerId(rankList.get(i).getPlayerId())
        info.setRankValue(rankList.get(i).getValue())
        if (rankType == PowerRanksDefine.POWERRANK_TYPE_ATK_STRENGTHEN) {
          info.setLevel(powerRank.getAtklv)
        } else if (rankType == PowerRanksDefine.POWERRANK_TYPE_CRIT_STRENGTHEN) {
          info.setLevel(powerRank.getCritlv)
        } else if (rankType == PowerRanksDefine.POWERRANK_TYPE_DODGE_STRENGTHEN) {
          info.setLevel(powerRank.getDogelv)
        } else {
          info.setLevel(powerRank.getSumLevel)
        }
        info.setName(powerRank.getName())
        infos.addPowerRankInfo(info)
        sizeNum -= 1
        i += 1
        // }
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
    if (infos.getPowerRankInfoBuilderList.size() > 0) {
      infos.setTypeId(rankType)
      sender() ! GetAnRankMessageByType(message.build(),rankType)
    } else {
      val messageno: M21.M210000.S2C.Builder = M21.M210000.S2C.newBuilder
      sender() ! GetAnRankMessageByType(messageno.build(),rankType)
    }

  }

  def getRankSetKey(playerId: Long, rankType: Int): String = {
    playerId + "_" + rankType
  }

  def getlimitNearList(dungId: Int): util.List[LimitDungeoReport] = {
    val list: util.List[LimitDungeoReport] = new util.ArrayList[LimitDungeoReport]
    for (limit <- reportlist) {
      if (limit.getDungeoId == dungId) {
        if(!isHasPlayerId(list,limit.getPlayerId)){
          list.add(limit)
        }
      }
    }
    list
  }

  def addlimitNearList(dungId: Int, battleId: Long, playerId: Long): Unit = {
    val list: util.List[LimitDungeoReport] = getlimitNearList(dungId)
    if (isHasPlayerId(list, playerId) == false) {
      val report: LimitDungeoReport = new LimitDungeoReport()
      report.setBattleId(battleId)
      report.setDungeoId(dungId)
      report.setPlayerId(playerId)
      report.setTime(GameUtils.getServerDate().getTime)
      if (list.size() < 3) {
        reportlist.add(report)
      } else {
        SortUtil.anyProperSort(list, "getTime", false)
        reportlist.remove(list.get(2))
        reportlist.add(report)
      }
    }
  }

  def isHasPlayerId(list: util.List[LimitDungeoReport], playerid: Long): java.lang.Boolean = {
    for (ldp <- list) {
      if (ldp.getPlayerId == playerid) {
        return true
      }
    }
    return false
  }


  //发送消息给具体的角色模块
  def sendMsgToRoleModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.ROLE_MODULE_NAME)
    actor ! msg
  }

  //发送日志
  def sendLog(log: BaseLog) = {
    tellService(ActorDefine.ADMIN_LOG_SERVICE_NAME, SendAdminLog(log, ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0))
  }
  //通知到service
  def tellService(serviceName: String, msg: AnyRef) = {
    context.actorSelection("../" + serviceName) ! msg
  }

  def sendRankLog(playerId :Long,rank:Int, ranktype:Int): Unit ={
    val log:tbllog_activityrank=new tbllog_activityrank(playerId,rank,ranktype)
    log.setLog_time(GameUtils.getServerTime())
    sendLog(log)
  }

}
