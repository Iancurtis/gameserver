package com.znl.service

import java.io.IOException
import java.util

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import com.znl.GameMainServer
import com.znl.base.{BaseDbPojo, BaseSetDbPojo}
import com.znl.core.{PlayerTroop, SimplePlayer}
import com.znl.define._
import com.znl.framework.socket.Request
import com.znl.log.CustomerLogger
import com.znl.log.admin.tbllog_online
import com.znl.msg.GameMsg._
import com.znl.msg.{GameMsg, PushShareMsg, ShareMsg}
import com.znl.pojo.db.Player
import com.znl.pojo.db.set._
import com.znl.proxy._
import com.znl.service.actor.PlayerActor
import com.znl.service.trigger.{TriggerEvent, TriggerType}
import com.znl.utils.GameUtils
import org.apache.mina.core.session.IoSession
import org.json.JSONObject

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/** 统筹所有的玩家服务
  * Created by Administrator on 2015/10/22.
  */

object PlayerService {
  def props(areaKey: String) = {
    onlineMap.put(areaKey, new util.concurrent.ConcurrentHashMap[Long, SimplePlayer])
    offlineMap.put(areaKey, new util.concurrent.ConcurrentHashMap[Long, SimplePlayer])
    initRobotMap()
    Props(classOf[PlayerService], areaKey)
  }

  val onlineMap: util.concurrent.ConcurrentHashMap[String, util.concurrent.ConcurrentHashMap[Long, SimplePlayer]] =
    new util.concurrent.ConcurrentHashMap[String, util.concurrent.ConcurrentHashMap[Long, SimplePlayer]]
  val offlineMap: util.concurrent.ConcurrentHashMap[String, util.concurrent.ConcurrentHashMap[Long, SimplePlayer]] =
    new util.concurrent.ConcurrentHashMap[String, util.concurrent.ConcurrentHashMap[Long, SimplePlayer]]
  val robotMap: util.concurrent.ConcurrentHashMap[Long, SimplePlayer] = new util.concurrent.ConcurrentHashMap[Long, SimplePlayer]

  val baniplist: util.List[java.lang.String] = new util.ArrayList[java.lang.String]()


  import scala.collection.JavaConversions._

  def getOnlineMap(areaKey: String): util.concurrent.ConcurrentHashMap[Long, SimplePlayer] = {
    onlineMap.get(areaKey)
  }

  //判断ip是否处于封禁状态
  def isBanIp(ip :java.lang.String):java.lang.Boolean ={
   for(ipdate<- baniplist){
     val iptemp:java.lang.String=ipdate.split("_")(0)
     val date:java.lang.Long=ipdate.split("_")(1).toLong
     if(iptemp.equals(ip)&& GameUtils.getServerTime()<date){
      return   true
     }
   }

    false
  }

  def getOfflineMap(areaKey: String): util.concurrent.ConcurrentHashMap[Long, SimplePlayer] = {
    offlineMap.get(areaKey)
  }

  def initRobotMap() = {
    println("机器人初始化")
    robotMap.clear()
    val jasonall: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.ArenaRobot)
    for (json <- jasonall) {
      val simpleInfo: SimplePlayer = getRobotSimple(json)
      robotMap.put(-json.getInt("ID").toLong, simpleInfo)
    }
  }

  def getRobotSimple(define: JSONObject): SimplePlayer = {
    val simpleInfo: SimplePlayer = new SimplePlayer
    simpleInfo.setId(-define.getInt("ID").toLong)
    simpleInfo.setName(define.getString("name"))
    simpleInfo.setLevel(define.getInt("level"))
    simpleInfo.setCapacity(define.getInt("force"))
    simpleInfo.setLegionName("")
    simpleInfo.setIconId(1)
    simpleInfo.setFaceIcon(1)
    simpleInfo.setCapacity(define.getInt("force"))
    simpleInfo
  }

  def getRobotSimplePlayer(playerId: Long): SimplePlayer = {
    var simplePlayer: SimplePlayer = null
    if (robotMap.get(playerId) != null) {
      simplePlayer = robotMap.get(playerId)
    }
    simplePlayer
  }

  //获取玩家数据快照 ,包括在线，下线
  def getSimplePlayer(playerId: Long, areaKey: java.lang.String): SimplePlayer = {
    try {
      var simplePlayer: SimplePlayer = null
      if (playerId < 0) {
        return getRobotSimplePlayer(playerId)
      }
      if (onlineMap.get(areaKey).containsKey(playerId)) {
        simplePlayer = onlineMap.get(areaKey).get(playerId) //先去在线玩家池 拿
        simplePlayer.online = true;
      } else {
        if (simplePlayer == null) {
          //再去离线玩家池 拿
          simplePlayer = offlineMap.get(areaKey).get(playerId)
        }
        var player: Player = null
        if (simplePlayer == null) {
          //最后去数据库拿
          player = BaseDbPojo.getOfflineDbPojo(playerId, classOf[Player], areaKey)
          if (player != null && GameMainServer.checkAreaIdInAreaKey(areaKey, player.getAreaId)) {
            //如果不在这个区里面的玩家不能拿到
            simplePlayer = new SimplePlayer()
            simplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
            offlineMap.get(areaKey).put(playerId, simplePlayer) //到数据库里面拿的话，证明该玩家已经离线的了，缓存起来
            //            player.finalize()
          }
        }
        if (simplePlayer != null) {
          simplePlayer.online = false
          //给离线玩家增加繁荣度
          addOfflinePlayerBoom(simplePlayer, player, areaKey)
        }
      }
      simplePlayer
    } catch {
      case e: IOException => {
        e.printStackTrace
        null
      }
    }

  }

  //TODO 这里还需要更新在线玩家的一些关键信息 比如名字
  def onCreatePlayerActorSuccess(simplePlayer: SimplePlayer, areaKey: String) = {
    onlineMap.get(areaKey).put(simplePlayer.getId, simplePlayer)

    offlineMap.get(areaKey).remove(simplePlayer.getId) //同时将离线的对应的数据清除掉


  }

  //玩家真正下线了，把在线数据移到离线缓存中去
  def onStopPlayerActorSuccess(id: Long, areaKey: String) = {
    val simplePlayer = onlineMap.get(areaKey).get(id)
    if (simplePlayer != null) {
      onlineMap.get(areaKey).remove(id)
      offlineMap.get(areaKey).put(id, simplePlayer)
    }


  }

  def addOfflinePlayerBoom(simplePlayer: SimplePlayer, player: Player, areaKey: String): Unit = {
    val now: Long = GameUtils.getServerDate().getTime
    if (simplePlayer.getBoomRefTime + 60 * 1000 < now) {
      var p: Player = player
      if (p == null) {
        p = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      }
      val playerProxy: PlayerProxy = new PlayerProxy(p, areaKey)
      playerProxy.setSimplePlayer(simplePlayer)
      val systemProxy: SystemProxy = new SystemProxy(areaKey)
      systemProxy.checkBoomTimer(playerProxy)
    }

  }

  //通过玩家ID，判断是否是否在线
  def isOnline(playerId: Long, areaKey: String) = {
    onlineMap.get(areaKey).containsKey(playerId)
  }

  //更新在线玩家简要数据
  def onUpdateOnlineSimplePlayer(simplePlayer: SimplePlayer, areaKey: String) = {
    onlineMap.get(areaKey).replace(simplePlayer.getId, simplePlayer)
  }

  //更新玩家简要数据
  def onUpdateSimplePlayer(simplePlayer: SimplePlayer, areaKey: String) = {
    if (onlineMap.get(areaKey).containsKey(simplePlayer.getId)) {
      onlineMap.get(areaKey).replace(simplePlayer.getId, simplePlayer)
    } else if (offlineMap.get(areaKey).containsKey(simplePlayer.getId)) {
      offlineMap.get(areaKey).replace(simplePlayer.getId, simplePlayer)
    }

  }

  //更新玩家的防守阵型
  def onUpdateSimplePlayerDefendTroop(id: Long, troop: PlayerTroop, areaKey: String): Unit = {
    if (onlineMap.get(areaKey).containsKey(id)) {
      val simplePlayer: SimplePlayer = onlineMap.get(areaKey).get(id)
      simplePlayer.setDefendTroop(troop)
      simplePlayer.setRefDefendTroop(true)
    } else if (offlineMap.get(areaKey).containsKey(id)) {
      val simplePlayer: SimplePlayer = offlineMap.get(areaKey).get(id)
      simplePlayer.setDefendTroop(troop)
      simplePlayer.setRefDefendTroop(true)
    } else {
      CustomerLogger.error("玩家更新防守阵型的时候居然没在缓存中，没办法，直接保存吧")
      //      saveDefendTroop(id,troop,areaKey)
    }

    //2016/04/05 修改成马上保存
    saveDefendTroop(id, troop, areaKey)
  }

  def saveDefendTroop(id: Long, troop: PlayerTroop, areaKey: String) = {
    BaseSetDbPojo.getSetDbPojo(classOf[TeamDateSetDb], areaKey).addTeamDate(troop, id, SoldierDefine.FORMATION_DEFEND)
  }

  //直接拿多个玩家的简要信息
  //离线的也需要拿
  def onGetPlayerSimpleInfoList(ids: java.util.Set[java.lang.Long], areaKey: String): util.List[SimplePlayer] = {
    val simpleInfoList: util.List[SimplePlayer] = new util.ArrayList[SimplePlayer]
    try {
      ids.foreach(id => {
        if (id == null) {
          CustomerLogger.error("获取SimplePlayer的时候出现空啦！！！！")
        } else {
          val simplePlayer: SimplePlayer = getSimplePlayer(id, areaKey)
          simpleInfoList.add(simplePlayer)
        }
      })
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    simpleInfoList
  }


  //离线的也需要拿
  def onGetPlayerSimpleInfos(ids: java.util.List[java.lang.Long], areaKey: String): util.List[SimplePlayer] = {
    val simpleInfoList: util.List[SimplePlayer] = new util.ArrayList[SimplePlayer]
    try {
      ids.foreach(id => {
        val simplePlayer: SimplePlayer = getSimplePlayer(id, areaKey)
        simpleInfoList.add(simplePlayer)
      })
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    simpleInfoList
  }
}


class PlayerService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {

  //TODO 需要提供GM命令，直接kill掉 playerActor

  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      log.error(e.fillInStackTrace(), e.getMessage)
      log.error("playerServer出现异常，正在重启", e)
      Resume
    }
    case _ => Resume
  }


  val clearTriggerTime = 6 * 60 * 60 //6小时清除掉

  //记录在线玩家的玩家ID 以及accountName的映射 这个数据需要有更新的机制
  //  val onlineMap : util.Map[Long, SimplePlayer] = new util.concurrent.ConcurrentHashMap[Long, SimplePlayer]
  //  val offlineMap : util.Map[Long, SimplePlayer] = new util.concurrent.ConcurrentHashMap[Long, SimplePlayer]  //游戏过程中，产生的下面玩家快照数据，一段时间后，需要释放掉

  import context.dispatcher

  context.system.scheduler.schedule(1 hours, 1 hours, context.self, ClearOfflineSimplePlayerTrigger())

  //1小时触发一次

  def distributeEachHourNoticeToPlayer(): Unit = {
    context.children.foreach(f => {
      f ! EachHourNotice()
    })
  }

  def noticeAllServiceSave(): Unit = {
    tellService(context, ActorDefine.POWERRANKS_SERVICE_NAME, saveDateBeforeStop())
    tellService(context, ActorDefine.ARENA_SERVICE_NAME, saveDateBeforeStop())
    tellService(context, ActorDefine.ARMYGROUP_SERVICE_NAME, saveDateBeforeStop())
  }

  def distributeReloadNoticeToPlayer(): Unit = {
    context.children.foreach(f => {
      f ! Reload()
    })
  }

  override def preStart(): Unit = {
    initPlayerSetDb()
    updateEachHourNotice()
    updateEachMinuteNotice()
    initBanIp()
  }

  def initBanIp(): Unit = {
    val lastranks: util.List[java.lang.String] = BaseSetDbPojo.getSetDbPojo(classOf[BanIPSetDb], areaKey).getAllArenaRank
    PlayerService.baniplist.clear();
    PlayerService.baniplist.addAll(lastranks)
  }

  def saveBanIp(): Unit = {
    for (ip <- PlayerService.baniplist) {
      val bandate:java.lang.String=ip.split("_")(1);
      val date:java.lang.Long=bandate.toLong
      if(date >  GameUtils.getServerTime()) {
        BaseSetDbPojo.getSetDbPojo(classOf[BanIPSetDb], areaKey).addKeyValue(ip + "_" + ActorDefine.BANIP, 0l)
      }
    }
  }

  override def postStop() = {
 //   saveBanIp()
  }


  //初始化玩家相关的setDb
  def initPlayerSetDb(): Unit = {
    BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
    BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
    BaseSetDbPojo.getSetDbPojo(classOf[BillOrderSetDb], areaKey)
    BaseSetDbPojo.getSetDbPojo(classOf[TeamDateSetDb], areaKey)
  }

  override def receive: Receive = {
    case CreatePlayerActor(accountName, areaId, ioSession) =>
      onCreatePlayerActor(accountName, areaId, ioSession)
    //      log.info("accountName:%s isOnline: %s".format(accountName, isOnline(accountName).toString))
    case msg: ReplacePlayerSession =>
      onReplacePlayerSession(msg)
    case SendPlayerNetMsg(accountName, request) =>
      onSendPlayerNetMsg(accountName, request)
    case StopPlayerActor(accountName: String) =>
      onStopPlayerActor(accountName)
    case CreatePlayerActorSuccess(simplePlayer: SimplePlayer) =>
      PlayerService.onCreatePlayerActorSuccess(simplePlayer, areaKey)
      log.error("当前在线人数：" + PlayerService.getOnlineMap(areaKey).size())
    case CreatePlayerActorFail(accountName: String) =>
      onCreatePlayerActorFail(accountName)
    case StopPlayerActorSuccess(id: Long) =>
      PlayerService.onStopPlayerActorSuccess(id, areaKey)
      log.error("离线：当前在线人数：" + PlayerService.getOnlineMap(areaKey).size())
    case BanPlayerHandle(banType: String, dataList: java.util.List[String], status: Int, banDate: Int, reason: String) =>
      onBanPlayerHandle(banType, dataList, status, banDate, reason)
    case AdminKickPlayerOffline(sendType: String, dataList: java.util.List[String], kickAll: Int, reason: String) =>
      onAdminKickPlayerOffline(sendType, dataList, kickAll, reason)
    case AdminInstructorGM(sendType: String, dataList: java.util.List[String], optType: Int, instructorType: Int, startTime: Int, endTime: Int) =>
      onAdminInstructorGM(sendType, dataList, optType, instructorType, startTime, endTime)
    case GetPlayerIsOnline(id: Long) =>
      val online = PlayerService.isOnline(id, areaKey)
      sender() ! Some(online)
    case GetPlayerSimpleInfo(id: Long, cmd: String) =>
      onGetPlayerSimpleInfo(id, cmd)
    case GetPlayerSimpleInfoByRoleName(roleName: String, cmd: String) =>
      onGetPlayerSimpleInfoByRoleName(roleName, cmd)
    case GetPlayerSimpleInfoListByRoleNameList(roleName: java.util.ArrayList[String], cmd: String) =>
      onGetPlayerSimpleInfoListByRoleNameList(roleName, cmd)
    case GetPlayerIdByName(name: String, typeId: Int) =>
      onGetPlayerIdByName(name: String, typeId: Int)
    case GetPlayerSimpleInfoListByIds(ids: java.util.Set[java.lang.Long], cmd: String) =>
      onGetPlayerSimpleInfoListByIds(ids, cmd)
    //    case msg : AskPlayerSimpleInfoList=>
    //      onAskPlayerSimpleInfoList(msg.ids)
    case msg: UpdateOnlineSimplePlayer =>
      PlayerService.onUpdateOnlineSimplePlayer(msg.simplePlayer, areaKey)
    case msg: UpdateSimplePlayer =>
      PlayerService.onUpdateSimplePlayer(msg.simplePlayer, areaKey)
    case msg: UpdateSimplePlayerDefendTroop =>
      PlayerService.onUpdateSimplePlayerDefendTroop(msg.id, msg.troop, areaKey)
    case msg: FriendBlessPlayers =>
      onFriendBlessPlayers(msg.blesser, msg.players)
    case msg: AddWorldBuildingSuccess =>
      sendMsgToPlayerActor(msg.accountName, msg)
    case msg: AutoSaveOffPlayerData =>
      sendMsgToPlayerActor(msg.accountName, AutoSaveOffPlayerData(msg.accountName))
    case msg: ClearOfflineSimplePlayerTrigger =>
      onClearOfflineSimplePlayerTrigger()
    case msg: StopServerSaveSimplePlayer =>
      onStopServerSaveSimplePlayer()
      saveBanIp()
    case msg: CheckSaveSimplePlayerDone =>
      onCheckSaveSimplePlayerDone()
    case msg: EachHourNotice =>
      distributeEachHourNoticeToPlayer()
      noticeAllServiceSave()
    //    case msg :  AskOnePlayerSimplePlayer=>
    //      onAskPlayerSimple(msg.id)
    case msg: EachMinuteNotice =>
      onEachMinuteNotice()
    case Reload() =>
      distributeReloadNoticeToPlayer()
    case trumpeNotity(playerId: Long, name: String, mess: String, retype: Int) =>
      ontrumpeNotity(playerId, name, mess, retype)
    case t: Terminated =>
      log.info("player actor关闭" + t.getActor().path.name) //表示彻底离线
    //      log.info("accountName:%s isOnline: %s".format(t.getActor().path.name, isOnline(t.getActor().path.name).toString))
    case m: ShareMsg =>
      tellService(context, ActorDefine.CHAT_SERVICE_NAME, m)
    case m: PushShareMsg =>
      distributeShareMsg(m)
    case _ =>
  }


  def distributeShareMsg(m: PushShareMsg): Unit = {
    for (simplePlayer <- PlayerService.getOnlineMap(areaKey).values) {
      sendMsgToPlayerModule(simplePlayer.getAccountName, m, ActorDefine.SHARE_MODULE_NAME)
    }
  }

  def ontrumpeNotity(playerId: Long, name: String, mess: String, retype: Int): Unit = {
    for (simplePlayer <- PlayerService.getOnlineMap(areaKey).values) {
      sendMsgToPlayerChatModule(simplePlayer.getAccountName, GameMsg.trumpeNotity(playerId, name, mess, retype))
    }
  }


  def updateEachHourNotice() = {
    val event = new TriggerEvent(self, EachHourNotice(), TriggerType.WHOLE_HOUR, 0)
    getTriggerService(context) ! AddTriggerEvent(event)
  }

  def updateEachMinuteNotice() = {
    println("！！！！设置每分钟定时器！！" + GameUtils.getServerDate())
    val event = new TriggerEvent(self, EachMinuteNotice(), TriggerType.WHOLE_MINUTE, 0)
    getTriggerService(context) ! AddTriggerEvent(event)
  }


  def onEachMinuteNotice(): Unit = {
    println("进入每分钟定时器！！" + GameUtils.getServerDate())
    CustomerLogger.info("进入每分钟定时器！！" + GameUtils.getServerDate())
    val minuteTimer = GameUtils.getNowMinute()
    if (minuteTimer % 5 == 0) {
      onlineLog()
    }
    context.children.foreach(f => {
      f ! EachMinuteNotice()
    })
  }


  //在线人数日志
  //todo 需要分渠道
  def onlineLog(): Unit = {
    println("！！！！开始记录在线日志")
    val time = GameUtils.getServerDate().getTime
    //先按渠道分 放设备集合
    val map: util.Map[String, util.HashSet[String]] = new util.HashMap[String, util.HashSet[String]]
    val mapPeople: util.Map[String, Int] = new util.HashMap[String, Int]
    var size = 0
    PlayerService.getOnlineMap(areaKey).foreach(v => {
      if (map.get(v._2.getPlatform) == null) {
        map.put(v._2.getPlatform, new util.HashSet[String]())
        mapPeople.put(v._2.getPlatform, 0)
      }
      map.get(v._2.getPlatform).add(v._2.getDevice)
      mapPeople.put(v._2.getPlatform, mapPeople.get(v._2.getPlatform) + 1)
      size = size + 1
    })

    map.foreach(v => {
      val onlinelog: tbllog_online = new tbllog_online()
      onlinelog.setPlatform(v._1)
      onlinelog.setDevice_cnt(v._2.size().toLong)
      onlinelog.setPeople(mapPeople.get(v._1))
      onlinelog.setHappend_time(GameUtils.getServerTime())
      sendAdminLogToService(context, SendAdminLog(onlinelog, ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0))
    })

    log.error("在线总人数统计：" + size + " :" + (time))
  }


  //登录失败
  def onCreatePlayerActorFail(accountName: String) = {

  }


  //设置GM接口
  def onAdminInstructorGM(sendType: String, dataList: java.util.List[String], optType: Int, instructorType: Int, startTime: Int, endTime: Int): Unit = {
    sendType match {
      case "1" =>
        //玩家id
        dataList.foreach(roleId => {
          val id: Long = java.lang.Long.valueOf(roleId)
          setPlayerTypeByPlayerId(id, optType, instructorType, startTime, endTime)
        })
      case "2" =>
        //玩家名称
        dataList.foreach(roleName => {
          val roleNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
          val playerId = roleNameSetDb.getValueByKey(roleName)
          if (playerId != null) {
            setPlayerTypeByPlayerId(playerId, optType, instructorType, startTime, endTime)
          }
        })
      case "3" =>
        //玩家账号
        dataList.foreach(accountName => {
          val accountNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
          val playerId = accountNameSetDb.getValueByKey(accountName)
          if (playerId != null) {
            setPlayerTypeByPlayerId(playerId, optType, instructorType, startTime, endTime)
          }
        })
    }
  }

  //踢下线接口
  def onAdminKickPlayerOffline(sendType: String, dataList: java.util.List[String], kickAll: Int, reason: String): Unit = {
    if (kickAll == 1) {
      //全部踢下线
      for (simplePlayer <- PlayerService.getOnlineMap(areaKey).values) {
        sendMsgToPlayerActor(simplePlayer.getAccountName, KickPlayerOffline(ErrorCodeDefine.M9998_2, reason))
      }
    } else {
      sendType match {
        case "1" =>
          //玩家id
          dataList.foreach(roleId => {
            val id: Long = java.lang.Long.valueOf(roleId)
            kickPlayerOffLineByPlayerId(id, reason)
          })
        case "2" =>
          //玩家名称
          dataList.foreach(roleName => {
            val roleNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
            val playerId = roleNameSetDb.getValueByKey(roleName)
            if (playerId != null) {
              kickPlayerOffLineByPlayerId(playerId, reason)
            }
          })
        case "3" =>
          //玩家账号
          dataList.foreach(accountName => {
            val accountNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
            val playerId = accountNameSetDb.getValueByKey(accountName)
            if (playerId != null) {
              kickPlayerOffLineByPlayerId(playerId, reason)
            }
          })
      }
    }
  }

  def setPlayerTypeByPlayerId(playerId: Long, optType: Int, instructorType: Int, startTime: Int, endTime: Int): Unit = {
    val simplePlayer = PlayerService.getOnlineMap(areaKey).get(playerId)
    if (simplePlayer != null) {
      if (optType == 1) {
        sendMsgToPlayerModule(simplePlayer.getAccountName, InstructorGM(0, 0, 0), ActorDefine.ROLE_MODULE_NAME)
      } else {
        sendMsgToPlayerModule(simplePlayer.getAccountName, InstructorGM(instructorType, startTime, endTime), ActorDefine.ROLE_MODULE_NAME)
      }
    } else {
      val player: Player = BaseDbPojo.getOfflineDbPojo(playerId, classOf[Player], areaKey)
      if (optType == 1) {
        player.setPlayerType(0)
        player.setTypeBeginTime(0)
        player.setTypeEndTime(0)
      } else {
        player.setPlayerType(instructorType)
        player.setTypeBeginTime(startTime)
        player.setTypeEndTime(endTime)
      }
      player.save()
    }

  }

  def kickPlayerOffLineByPlayerId(playerId: Long, reason: String): Unit = {
    val simplePlayer = PlayerService.getOnlineMap(areaKey).get(playerId)
    if (simplePlayer != null) {
      sendMsgToPlayerActor(simplePlayer.getAccountName, KickPlayerOffline(ErrorCodeDefine.M9998_2, reason))
    }
  }

  //封禁类接口
  def onBanPlayerHandle(banType: String, dataList: java.util.List[String], status: Int, banDate: Int, reson: String) = {
    banType match {
      case "BanRole" => //玩家昵称
        dataList.foreach(roleName => {
          banPlayerByRoleName(roleName, status, banDate)
        })
      case "BanAct" => //账号名
        dataList.foreach(accountName => banPlayerByAccountName(accountName, status, banDate))
      case "BanChat" =>
        dataList.foreach(roleName => {
          banChatByRoleName(roleName, status, banDate)
        })
      case "BanChatAct" =>
        dataList.foreach(accountName => banChatByAccountName(accountName, status, banDate))
      case "BanIP" =>
        if(status==1) {
          dataList.foreach(ip => PlayerService.baniplist.add(ip+"_"+banDate))
        }else{
          removeBanIp(dataList)
        }
    }
  }

  def removeBanIp(dataList: java.util.List[String]): Unit ={
    val removeips:util.List[String]=new util.ArrayList[String]()
    dataList.foreach(ip =>
      for(hasip<- PlayerService.baniplist){
        val tempip:String=hasip.split("_")(0)
        if(tempip.equals(ip)){
          removeips.add(hasip)
          BaseSetDbPojo.getSetDbPojo(classOf[BanIPSetDb], areaKey).removeKey(hasip+"_"+ActorDefine.BANIP)
        }
      }
    )

    PlayerService.baniplist.removeAll(removeips)
  }

  def banChatByRoleName(roleName: String, status: Int, banDate: Int, reason: String = ""): Unit = {
    val roleNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
    val playerId = roleNameSetDb.getValueByKey(roleName)
    if (playerId != null) {
      banChatByPlayerId(playerId, status, banDate, reason)
    }
  }

  def banPlayerByRoleName(roleName: String, status: Int, banDate: Int, reason: String = "") = {
    val roleNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey)
    val playerId = roleNameSetDb.getValueByKey(roleName)
    if (playerId != null) {
      banPlayerByPlayerId(playerId, status, banDate, reason)
    } else {

    }
  }

  def banChatByAccountName(accountName: String, status: Int, banDate: Int, reason: String = "") = {
    val accountNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
    val playerId = accountNameSetDb.getValueByKey(accountName)
    if (playerId != null) {
      banChatByPlayerId(playerId, status, banDate, reason)
    }
  }

  def banPlayerByAccountName(accountName: String, status: Int, banDate: Int, reason: String = "") = {
    val accountNameSetDb = BaseSetDbPojo.getSetDbPojo(classOf[AccountNameSetDb], areaKey)
    val playerId = accountNameSetDb.getValueByKey(accountName)
    if (playerId != null) {
      banPlayerByPlayerId(playerId, status, banDate, reason)
    }
  }

  def banChatByPlayerId(playerId: Long, status: Int, banDate: Int, reason: String = "") = {
    val isOnline = PlayerService.isOnline(playerId, areaKey)
    if (isOnline) {
      val simplePlayer = PlayerService.getSimplePlayer(playerId, areaKey)
      sendMsgToPlayerModule(simplePlayer.getAccountName, BanPlayerChat(banDate, status), ActorDefine.ROLE_MODULE_NAME)
    } else {
      val player = BaseDbPojo.getOfflineDbPojo(playerId, classOf[Player], areaKey)
      player.setBanChatAct(status)
      player.setBanChatActDate(banDate)
      player.save()
      player.finalize()
    }
  }


  def banPlayerByPlayerId(playerId: Long, status: Int, banDate: Int, reason: String = "") = {
    val isOnline = PlayerService.isOnline(playerId, areaKey)
    if (isOnline) {
      //TODO 改玩家在线，直接推送被禁号的提示框
      val simplePlayer = PlayerService.getSimplePlayer(playerId, areaKey)
      sendMsgToPlayerActor(simplePlayer.getAccountName, KickPlayerOffline(ErrorCodeDefine.M9998_2, reason))
    } else {
    }

    val player = BaseDbPojo.getOfflineDbPojo(playerId, classOf[Player], areaKey)
    player.setBanAct(status)
    player.setBanActDate(banDate)
    player.save()
    //    player.finalize()
  }


  //通过时间判断，自动清除离线数据
  def onClearOfflineSimplePlayerTrigger() = {
    val removeKeyList = new util.ArrayList[Long]
    val curTime = GameUtils.getServerTime
    PlayerService.getOfflineMap(areaKey).foreach(f => {
      if (curTime - f._2.getCreateTime >= clearTriggerTime) {
        removeKeyList.add(f._1)
        if (f._2.isRefDefendTroop) {
          PlayerService.saveDefendTroop(f._2.getId, f._2.getDefendTroop, areaKey)
        }
      }
    })

    removeKeyList.foreach(key => PlayerService.getOfflineMap(areaKey).remove(key))
  }

  //关服保存所有玩家的缓存数据
  def onStopServerSaveSimplePlayer(): Unit = {
    //将onlineMap的数据转移到offlineMap,然后执行保存
    val offlineMap = PlayerService.getOfflineMap(areaKey)
    val onlineMap = PlayerService.getOnlineMap(areaKey)
    offlineMap.putAll(onlineMap)
    onlineMap.clear()
    offlineMap.foreach(f => {
      if (f._2.isRefDefendTroop) {
        PlayerService.saveDefendTroop(f._2.getId, f._2.getDefendTroop, areaKey)
      }
    })
    offlineMap.clear()
  }

  //检查所有缓存数据是否已经保存完毕
  def onCheckSaveSimplePlayerDone(): Unit = {
    var done: Boolean = true
    if (PlayerService.getOnlineMap(areaKey).size() > 0) {
      done = false
    } else if (PlayerService.getOfflineMap(areaKey).size() > 0) {
      done = false
    }
    sender() ! done
  }


  //查看玩家简要信息  playerService直接去拿player数据，然后再包装起来
  //获取玩家简要数据
  def onGetPlayerSimpleInfo(id: java.lang.Long, cmd: String) = {
    try {
      if (id == null) {
        //玩家ID是空的
        sender() ! GetPlayerSimpleInfoSuccess(null, cmd)
      } else {
        val simplePlayer = PlayerService.getSimplePlayer(id, areaKey)
        if (simplePlayer != null) {
          if (PlayerService.isOnline(id, areaKey)) {
            simplePlayer.online = true
          } else {
            simplePlayer.online = false
          }
        }
        sender() ! GetPlayerSimpleInfoSuccess(simplePlayer, cmd)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }

  def onGetPlayerSimpleInfoByRoleName(roleName: String, cmd: String) = {
    val id: java.lang.Long = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey).getValueByKey(roleName); //DbProxy.ask(GetPlayerIdByRoleName(roleName, areaKey))

    onGetPlayerSimpleInfo(id, cmd)
  }


  def onGetPlayerIdByName(name: String, typeId: Int): Unit = {
    val id: java.lang.Long = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey).getValueByKey(name)
    sender() ! GetPlayerIdByNameSucess(id, typeId)
  }

  def onGetPlayerSimpleInfoListByRoleNameList(roleNames: java.util.ArrayList[String], cmd: String): Unit = {
    val simpleInfoList: util.List[SimplePlayer] = new util.ArrayList[SimplePlayer]
    try {
      if (roleNames != null && roleNames.size() > 0) {
        roleNames.foreach(name => {
          val id: java.lang.Long = BaseSetDbPojo.getSetDbPojo(classOf[RoleNameSetDb], areaKey).getValueByKey(name); // DbProxy.ask(GetPlayerIdByRoleName(name, areaKey))
          if (id != null) {
            val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(id, areaKey)
            if (simplePlayer != null) {
              if (PlayerService.isOnline(simplePlayer.getId, areaKey)) {
                simplePlayer.online = true
              }
              simpleInfoList.add(simplePlayer)
            }
          }
        })
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
    sender() ! GetPlayerSimpleInfoListSuccess(simpleInfoList, cmd)
  }

  def onGetPlayerSimpleInfoListByIds(ids: java.util.Set[java.lang.Long], cmd: String) = {

    val simpleInfoList: util.List[SimplePlayer] = new util.ArrayList[SimplePlayer]
    ids.foreach(id => {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(id, areaKey)
      simpleInfoList.add(simplePlayer)
    })

    sender() ! GetPlayerSimpleInfoListSuccess(simpleInfoList, cmd)

  }


  def addOfflinePlayerBoom(simplePlayer: SimplePlayer, player: Player): Unit = {
    val now: Long = GameUtils.getServerDate().getTime
    if (simplePlayer.getBoomRefTime + 60 * 1000 < now) {
      var p: Player = player
      if (p == null) {
        p = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
      }
      val playerProxy: PlayerProxy = new PlayerProxy(p, areaKey)
      playerProxy.setSimplePlayer(simplePlayer)
      val systemProxy: SystemProxy = new SystemProxy(areaKey)
      systemProxy.checkBoomTimer(playerProxy)
    }

  }

  def onFriendBlessPlayers(blesser: Long, playerIds: java.util.List[java.lang.Long]) = {
    playerIds.foreach(id => {
      val online = PlayerService.isOnline(id, areaKey)
      if (online) {
        //在线，直接通知该玩家的好友模块进行具体逻辑处理
        val simplePlayer = PlayerService.getOnlineMap(areaKey).get(id)
        val accountName = simplePlayer.getAccountName
        sendMsgPlayerModule(context, accountName, ActorDefine.FRIEND_MODULE_NAME, AcceptFriendBless(blesser))
      } else {
        //直接修改数据库，同时扔到离线队列里去
        val player = BaseDbPojo.getOfflineDbPojo(id, classOf[Player], areaKey)
        if (player != null) {
          //  val timerdb : Timerdb=BaseDbPojo.get(player.getFriendbleestimeId,classOf[Timerdb],areaKey)
          /*  val nowTime:Long=GameUtils.getServerDate().getTime
            if(timerdb!=null&& DateUtil.isCanGet(nowTime, timerdb.getLasttime, TimerDefine.TIMER_REFRESH_FOUR)){
              //执行刷新
              timerdb.setLasttime(nowTime)
              timerdb.setNum(0)
              player.setBeBlessSet(new util.HashSet[lang.Long]())
              player.setGetBlessSet(new util.HashSet[lang.Long]())
              timerdb.save()
              player.save()
            }*/
        }
        if (player != null && player.getBeBlessSet.size() < FriendDefine.MAX_DAY_GET_BLESS_NUM) {
          player.addBeBlesser(blesser)
          player.save()
        }
        var simplePlayer = new SimplePlayer()
        simplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
        PlayerService.getOfflineMap(areaKey).put(id, simplePlayer) //到数据库里面拿的话，证明该玩家已经离线的了，缓存起来
        player.finalize()
      }
    })
  }


  def onCreatePlayerActor(accountName: String, areaId: Int, ioSession: IoSession) = {
    val actor = context.actorOf(PlayerActor.props(accountName, areaId, ioSession), accountName)
    context.watch(actor)
  }

  def onReplacePlayerSession(msg: ReplacePlayerSession) = {
    sendMsgToPlayerActor(msg.accountName, msg)
  }


  def onSendPlayerNetMsg(accountName: String, request: Request) = {
    sendMsgToPlayerActor(accountName, ReceiveNetMsg(request))
  }

  def onStopPlayerActor(accountName: String) = {
    sendMsgToPlayerActor(accountName, PoisonPill.getInstance)
  }

  //发送消息给具体的player actor
  def sendMsgToPlayerActor(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection(accountName)
    actor ! msg
  }

  def sendMsgToPlayerChatModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.CHAT_MODULE_NAME)
    actor ! msg
  }

  def sendMsgToPlayerModule(accountName: String, msg: AnyRef, modlueName: String): Unit = {
    val actor = context.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + modlueName)
    actor ! msg
  }
}
