package com.znl.base

import java.util
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}

import akka.actor.{Actor, ActorLogging}
import com.google.protobuf.GeneratedMessage
import com.znl.core.{SimplePlayer, PlayerCache, PlayerReward}
import com.znl.define._
import com.znl.framework.socket.{Request, Response}
import com.znl.log.CustomerLogger
import com.znl.log.admin.{tbllog_function, tbllog_error}
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg._
import com.znl.proto.Common.AttrDifInfo
import com.znl.proto.M19.M190000
import com.znl.proto._
import com.znl.proxy._
import com.znl.utils.GameUtils
import org.json.JSONObject

import scala.collection.JavaConversions._

/**
  * Created by woko on 2015/10/7.
  */
abstract class BasicModule extends Actor with ActorLogging {
  //
  var moduleId = 0

  //协议访问缓存<cmd,time>
  var protocalRequestMap: ConcurrentHashMap[Integer, Integer] = new ConcurrentHashMap[Integer, Integer]

  //虚拟协议缓存
  var ptmap: util.Map[Integer, Object] = new util.HashMap[Integer, Object]()

  def setModuleId(moduleId: Int) = {
    this.moduleId = moduleId
  }

  def sendFuntctionLog(functionId: Int, expand1: Long, expand2: Long, expand3: Long, expandstr: String): Unit = {
    if (gameProxy != null) {
      val log: tbllog_function = createFunctionLog(functionId)
      log.setExpand1(expand1)
      log.setExpand2(expand2)
      log.setExpand3(expand3)
      log.setExpandstr(expandstr)
      sendLog(log)
    }
  }

  def createFunctionLog(functionId: Int): tbllog_function = {
    val log: tbllog_function = new tbllog_function
    val playerProxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
    log.setAccount_name(playerProxy.getAccountName)
    log.setRole_id(playerProxy.getPlayerId)
    log.setPlatform(playerProxy.getPlayerCache.getPlat_name)
    log.setDim_level(playerProxy.getPlayer.getLevel)
    log.setAction_id(functionId)
    log.setStatus(FunctionIdDefine.ACCOMPLISH)
    val now: Int = (GameUtils.getServerDate().getTime / FunctionIdDefine.tranform).toInt
    log.setLog_time(now)
    log.setHappend_time(now)
    log
  }

  def sendFuntctionLog(functionId: Int, expand1: Long, expand2: Long, expand3: Long): Unit = {
    if (gameProxy != null) {
      val log: tbllog_function = createFunctionLog(functionId)
      log.setExpand1(expand1)
      log.setExpand2(expand2)
      log.setExpand3(expand3)
      sendLog(log)
    }
  }

  def sendFuntctionLog(functionId: Int): Unit = {
    if (gameProxy != null) {
      val log: tbllog_function = createFunctionLog(functionId)
      sendLog(log)
    }
  }

  private[this] var gameProxy: GameProxy = null;

  def setGameProxy(value: GameProxy) = {
    this.gameProxy = value
  }

  def getGameProxy(): GameProxy = {
    return gameProxy
  }

  def getProxy[T](name: String) = {
    this.gameProxy.getProxy(name).asInstanceOf[T]
  }

  def saveAllProxy() = {
    this.gameProxy.save()
  }

  override def preStart() = {
    log.info("----模块启动--------:" + this.toString())
    //    registerAllNetEvent()
  }

  //模块异常重启
  override def postRestart(reason: scala.Throwable) = {
    sendPushNetMsgToClient() //尝试发送协议给客户端
  }

  //  def registerAllNetEvent();

  var netEventMap: Map[Int, Request => Unit] = Map()

  def registerNetEvent(cmd: Int, handle: Request => Unit): Unit = {
    netEventMap += (cmd -> handle)
  }

  def getTipMap() = {
    val powerMap: util.Map[java.lang.Integer, java.lang.Integer] = new util.HashMap[java.lang.Integer, java.lang.Integer]()
    if (this.gameProxy != null) {
      val playerPoxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
      powerMap.putAll(playerPoxy.getTipmap)
    }
    powerMap
  }

  def getPlayerPowerValues() = {
    val powerMap: util.Map[java.lang.Integer, java.lang.Long] = new util.HashMap[java.lang.Integer, java.lang.Long]()
    if (this.gameProxy != null) {
      val playerProxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
      val sendPowers: java.util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RESOURCE, "isshow", 1)
      for (powerDefine <- sendPowers) {
        val power = powerDefine.getInt("ID")
        val value: java.lang.Long = playerProxy.getPowerValue(power)
        powerMap.put(power.intValue(), value)
      }
      //      for (power <- PlayerPowerDefine.NameMap.keySet) {
      //
      //      }
    }
    powerMap
  }


  //  def checkPlayerPowerValues(map: util.Map[java.lang.Integer, java.lang.Long]) = {
  //    var send = false
  //    var ref = false
  //    val builder: M2.M20002.S2C.Builder = M2.M20002.S2C.newBuilder()
  //    val refurceSoldier: util.List[Integer] = new util.ArrayList[Integer]
  //    if (map.size() > 0 && gameProxy != null) {
  //      val playerProxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
  //      val sendPowers: java.util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.RESOURCE, "isshow", 1)
  //      var falg: Boolean = false
  //      for (powerDefine <- sendPowers) {
  //        val power = powerDefine.getInt("ID")
  //        val value: Long = playerProxy.getPowerValue(power)
  //        val _value: Long = map.get(power)
  //        if (_value != value) {
  //          val diff: AttrDifInfo.Builder = AttrDifInfo.newBuilder()
  //          diff.setTypeid(power)
  //          diff.setValue(value)
  //          if (power == PlayerPowerDefine.POWER_exp){
  //          }
  //          builder.addDiffs(diff.build())
  //          if (power == PlayerPowerDefine.POWER_gold && value < _value) {
  //            val taskproxy: TaskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
  //            val reward = new PlayerReward()
  //            val build: M190000.S2C.Builder = taskproxy.getTaskUpdate(TaskDefine.TASK_TYPE_COSTGOLD_TIMES, 1)
  //            if (build != null) {
  //              sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(build, reward))
  //            }
  //          }
  //          if (power >= 57 && power <= 61) {
  //            falg = true
  //          }
  //          send = true
  //          if (power <= SoldierDefine.TOTAL_FIGHT_POWER) {
  //            refurceSoldier.add(power)
  //            ref = true
  //          }
  //          if(power == PlayerPowerDefine.POWER_command){
  //            ref = true
  //          }
  //        }
  //      }
  //      if (falg == true) {
  //        val taskproxy: TaskProxy = getProxy(ActorDefine.TASK_PROXY_NAME);
  //        val reward = new PlayerReward()
  //        val build: M190000.S2C.Builder = taskproxy.getTaskUpdate(TaskDefine.TASK_TYPE_RESOURCE_VALUE, 1)
  //        if(build!=null) {
  //          sendModuleMsg(ActorDefine.TASK_MODULE_NAME, new GameMsg.RefeshTaskUpdate(build, reward))
  //        }
  //      }
  //    }
  //    if (send == true) {
  //      if (refurceSoldier.size() > 0) {
  //        val soldierProxy: SoldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME)
  //        val infos = soldierProxy.refurceSoldierPowerValue(refurceSoldier)
  ////        val infoBuilder: M4.M40000.S2C.Builder = M4.M40000.S2C.newBuilder()
  ////        infoBuilder.addAllSoldiers(infos)
  ////        sendNetMsg(ActorDefine.SOLDIER_MODULE_ID, ProtocolModuleDefine.NET_M4_C40000, infoBuilder.build())
  //      }
  //      if(ref == true){
  //        //重新计算最高战力
  //        sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, CountCapacity())
  //      }
  //      sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, builder.build())
  //    }
  //    checkDifferentRefurceLogic(builder.build())
  //    map.clear()
  //  }


  /** *检查所有的different的power，完成对应需要刷新的操作 ***/
  def checkDifferentRefurceLogic(mess: M2.M20002.S2C): Unit = {
    //发送到战力排行榜刷新
    val soldierProxy: SoldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME)
    val playerProxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
    for (diff: Common.AttrDifInfo <- mess.getDiffsList) {
      if (diff.getTypeid == PlayerPowerDefine.NOR_POWER_highestCapacity) {
        val msg: GameMsg.AddPlayerToRank = new GameMsg.AddPlayerToRank(playerProxy.getPlayerId, soldierProxy.getHighestCapacity, PowerRanksDefine.POWERRANK_TYPE_CAPACITY)
        sendServiceMsg(ActorDefine.POWERRANKS_SERVICE_NAME, msg)
        if (playerProxy.getArmGrouId > 0) {
          val groupmsg: GameMsg.changeMenberCapity = new GameMsg.changeMenberCapity(playerProxy.getPlayerId, soldierProxy.getHighestCapacity)
          tellMsgToArmygroupNode(groupmsg, playerProxy.getArmGrouId)
        }
      } else if (diff.getTypeid == PlayerPowerDefine.POWER_level) {
        val groupmsg: GameMsg.changeMenberLevel = new GameMsg.changeMenberLevel(playerProxy.getPlayerId, playerProxy.getLevel)
        tellMsgToArmygroupNode(groupmsg, playerProxy.getArmGrouId)
      }
    }
    var needRefSimplePlayer = false
    //simplePlayer刷新
    for (diff: Common.AttrDifInfo <- mess.getDiffsList) {
      if (GameUtils.refSimplePlayerMap.contains(diff.getTypeid)) {
        updateMySimplePlayerData()
      }
    }

    /* //检查一下体力时间是否有更新，有的话推送协议给客户端
     val time = playerProxy.sendtimes()
     if (time > 0){
       val builder: M2.M20501.S2C.Builder = M2.M20501.S2C.newBuilder()
       builder.setEnergyRefTime((time /1000).toInt)
       builder.setRs(0)
       sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20501, builder.build())
     }*/
  }

  def sendDifferent(powerList: util.List[Integer]): M2.M20002.S2C = {
    val builder: M2.M20002.S2C.Builder = M2.M20002.S2C.newBuilder()
    val playerProxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
    for (power <- powerList) {
      val value: Long = playerProxy.getPowerValue(power)
      val diff: AttrDifInfo.Builder = AttrDifInfo.newBuilder()
      if (diff.getTypeid == PlayerPowerDefine.POWER_level) {
        val groupmsg: GameMsg.changeMenberLevel = new GameMsg.changeMenberLevel(playerProxy.getPlayerId, playerProxy.getLevel)
        tellMsgToArmygroupNode(groupmsg, playerProxy.getArmGrouId)
      }
      diff.setTypeid(power)
      diff.setValue(value)
      builder.addDiffs(diff.build())
    }
    //    sendNetMsg(ActorDefine.ROLE_MODULE_ID,ProtocolModuleDefine.NET_M2_C20002,builder.build())
    builder.build()
  }

  //  def sendPowerDiff (list: util.List[Integer]) {
  //    val different: M2.M20002.S2C = sendDifferent(list)
  //    pushNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20002, different)
  //    sendPushNetMsgToClient
  //  }


  def onTriggerNetEvent(cmd: Int, request: Request) = {
    try {
      val curTime = System.currentTimeMillis()
      val tipmap: util.Map[java.lang.Integer, java.lang.Integer] = getTipMap()
      val powerMap: util.Map[java.lang.Integer, java.lang.Long] = getPlayerPowerValues()
      request.setPowerMap(powerMap)
      val method = this.getClass.getDeclaredMethod("OnTriggerNet" + cmd + "Event", request.getClass)
      CustomerLogger.info("OnTriggerNet" + cmd + "Event")
      method.setAccessible(true)
      method.invoke(this, request) //反射，性能很大的消耗

      //      multicastNetToClient()
      val dtTime = System.currentTimeMillis() - curTime
      if (dtTime > 100) {
        log.error("==============操作延迟：cmd:%d==dt:%d================".format(cmd, dtTime))
        //        GameUtils.addSlowWarnMap(cmd)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        ///出错日志
        //        errorLog(e.getMessage);
      }
    }

  }

  override def receive: Receive = {
    case netMsg: ReceiveNetMsg =>
      onReceiveNetMsg(netMsg)
    case anyRef: AnyRef =>
      onReceiveOtherMsg(anyRef)
  }

  def onReceiveOtherMsg(anyRef: AnyRef)

  //更新自己的在线数据
  def updateMySimplePlayerData() = {
    val playerProxy: PlayerProxy = this.getProxy(ActorDefine.PLAYER_PROXY_NAME)
    val simplePlayer: SimplePlayer = playerProxy.getSimplePlayer
    if (simplePlayer != null) {
      sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, UpdateOnlineSimplePlayer(playerProxy.getSimplePlayer))
    }
  }


  //更新玩家的简要数据
  def updateSimplePlayerData(simplePlayer: SimplePlayer) = {
    sendServiceMsg(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(simplePlayer))
  }


  def onReceiveNetMsg(netMsg: ReceiveNetMsg): Any = {
    val request = netMsg.request
    val cmd = request.getCmd()
    //判断协议重复请求
    if (checkIsRepeatedProtocal(cmd, request.getReqTime)) {
      repeatedProtocalHandler(cmd)
      return null
    }
    if (cmd == 230001) {
      System.err.println("++++++++++++++++++++++++==444");
    }
    onTriggerNetEvent(cmd, request)
  }

  def sendNetMsg(moduleId: Int, cmd: Int, message: GeneratedMessage): Unit = {
    val response: Response = Response.valueOf(moduleId, cmd, message)
    context.parent ! SendNetMsgToClient(response)
    //    println("==========服务器返回协议"+cmd)
  }

  //  def multicastNetToClient() = {
  //    context.parent ! MulticastNetToClient()
  //  }

  /*只在推送的时候调用，慎用！*/
  def pushNetMsg(moduleId: Int, cmd: Int, message: GeneratedMessage): Unit = {
    val response: Response = Response.valueOf(moduleId, cmd, message)
    context.parent ! SendNetMsgToClient(response)
    //    println("==========服务器推送协议"+cmd)
  }

  def sendMsg(): Unit = {
    context.parent ! PushtNetMsgToClient()
  }

  //所有的协议接收后都要走这里面才能发送到客户端，顺便会执行所有的检验要发送的附带协议逻辑
  def sendPushNetMsgToClient(): Unit = {
    // 检查有没有任务要推送
    checkSendTask()
    // 检查有没有活动要推送
    checkSendActivity()
    //检查有没有属性要推送
    checkSendPowerValue()
    sendMsg()
  }

  // 检查有没有任务要推送
  def checkSendActivity(): Unit = {
    val activityProxy: ActivityProxy = getProxy(ActorDefine.ACTIVITY_PROXY_NAME)
    val sendSet = activityProxy.getNeedSendActivitys()
    if (sendSet.size() > 0) {
      val builder = M23.M230007.S2C.newBuilder
      val infos = activityProxy.getActivityInfoByIds(sendSet)
      builder.addAllActivityInfo(infos)
      sendNetMsg(ProtocolModuleDefine.NET_M23, ProtocolModuleDefine.NET_M23_C230007, builder.build)
    }
  }

  // 检查有没有活动要推送
  def checkSendTask(): Unit = {
    val taskProxy: TaskProxy = getProxy(ActorDefine.TASK_PROXY_NAME)
    val sendSet = taskProxy.getNeedPushTasks()
    if (sendSet.size() > 0) {
      //发送协议刷新任务
      val taskInfos = taskProxy.getTaskInfosByTaskId(sendSet)
      if (taskInfos.size() > 0) {
        val builder = M19.M190000.S2C.newBuilder()
        builder.setDayActivityId(taskProxy.getActivityId)
        val dayilNum: Int = taskProxy.taskTimer.getDaytaskNum // timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_MESSION, 0, 0);
        val num: Int = taskProxy.taskTimer.getActivyTastId //timerdbProxy.getTimerNum(TimerDefine.FRIEND_DAY_ACTIVITY, 0, 0);
        builder.setRs(0)
        builder.setHasGetMaxId(num)
        builder.setDayliynum(dayilNum)
        builder.addAllTaskInfos(taskInfos)
        builder.setDayActivityId(taskProxy.getActivityId)
        sendNetMsg(ProtocolModuleDefine.NET_M19, ProtocolModuleDefine.NET_M19_C190000, builder.build)
      }
      val reward = taskProxy.getDayActivityTaskReward(sendSet)
      if (reward.haveReward) {
        //发送奖励
        val rewardProxy: RewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME)
        rewardProxy.getRewardToPlayer(reward, LogDefine.GET_DAYLIY_TASK_DAYACTIVITY)
        val build20007 = rewardProxy.getRewardClientInfo(reward)
        sendNetMsg(ProtocolModuleDefine.NET_M2, ProtocolModuleDefine.NET_M2_C20007, build20007)
      }
      sendPushNetMsgToClient()
    }
  }

  //检查有没有属性要推送
  def checkSendPowerValue(): util.List[Common.AttrDifInfo] = {
    val diffs = new util.ArrayList[Common.AttrDifInfo]()
    val refurceSoldier = new util.ArrayList[Integer]()
    var refCapacity = false //是否需要通知到战力模块去执行重算战力
    val playerProxy: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
    val powerSet = playerProxy.getChangePower
    if (powerSet.size() > 0) {
      for (power: Integer <- powerSet) {
        val diffInfo = Common.AttrDifInfo.newBuilder();
        diffInfo.setTypeid(power)
        diffInfo.setValue(playerProxy.getPowerValue(power))
        diffs.add(diffInfo.build())
        if (power <= SoldierDefine.TOTAL_FIGHT_POWER) {
          refurceSoldier.add(power)
          refCapacity = true
        }
        if (power == PlayerPowerDefine.POWER_command) {
          refCapacity = true
        }
      }
    }
    if (refurceSoldier.size() > 0) {
      val soldierProxy: SoldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME)
      soldierProxy.refurceSoldierPowerValue(refurceSoldier)
    }

    if (refCapacity) {
      sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME, CountCapacity())
    }
    if (diffs.size() > 0) {
      val powerBuilder: M2.M20002.S2C.Builder = M2.M20002.S2C.newBuilder()
      powerBuilder.addAllDiffs(diffs)
      sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20002, powerBuilder.build())
      checkDifferentRefurceLogic(powerBuilder.build())
    }
    diffs
  }

  //module -> service 发送信息
  def sendServiceMsg(serviceName: String, msg: AnyRef) = {
    context.actorSelection("../../../" + serviceName) ! msg
  }

  //直接到服务去拿数据，慎用！！！
  def askServiceMsg[T](serviceName: String, msg: AnyRef) = {
    val ref = context.actorSelection("../../../" + serviceName)
    val value: Option[T] = GameUtils.futureAsk(ref, msg)

    val result: T = value.getOrElse(null).asInstanceOf[T]
    result
  }

  //module -> module 发送消息
  def sendModuleMsg(moduleName: String, msg: AnyRef): Unit = {
    context.actorSelection("../" + moduleName) ! msg
  }

  //发送消息到别的玩家的模块中
  def sendMsgToOtherPlayerModule(moduleName: String, accountName: String, msg: AnyRef): Unit = {
    context.actorSelection("../../" + accountName + "/" + moduleName) ! msg
  }

  //发送日志
  def sendLog(log: BaseLog) = {
    val path: String = log.getClass.getCanonicalName
    if (path.indexOf("admin") > -1) {
      context.parent ! ModuleSendAdminLog(log, ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0)
    } else {
      context.parent ! ModuleSendLog(log)
    }
  }

  //发送管理平台的日志
  //行为类型 插入，更新(只有player才会有更新操作，其他都是插入)
  def sendAdminLog(log: BaseLog, actionType: Int, key: String, value: Int) = {
    context.parent ! ModuleSendAdminLog(log, actionType, key, value)
  }

  //将消息推送到玩家设备去
  def pushMsgToPlayerDevice(pushChannelId: String, title: String, msg: String, time: Int) = {
    sendMsgToServer(ActorDefine.PUSH_SERVER_NAME, PushMsgToPlayerDevice(pushChannelId, title, msg, time))
  }


  //发送消息到Server去
  def sendMsgToServer(serverName: String, msg: AnyRef) = {
    context.actorSelection("../../../../" + serverName) ! msg
  }

  /**
    * 检测是否是重复请求的协议
    * @param cmd
    * @return
    */
  def checkIsRepeatedProtocal(cmd: Int, requestTime: Int): Boolean = {
    var result = false
    if (protocalRequestMap.contains(cmd)) {
      val preRequestTime = protocalRequestMap.get(cmd)
      if (preRequestTime == requestTime) {
        result = true
      } else {
        protocalRequestMap.put(cmd, requestTime)
      }
    } else {
      protocalRequestMap.put(cmd, requestTime)
    }
    return result
  }

  /**
    * 协议重复请求处理
    */
  def repeatedProtocalHandler(cmd: Int);

  /**
    * tbllog_error 出错日志
    */
  def errorLog(msg: String) = {
    val player: PlayerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME)
    val cache: PlayerCache = player.getPlayerCache
    val errorlog: tbllog_error = new tbllog_error();
    errorlog.setPlatform(cache.getPlat_name)
    errorlog.setRole_id(player.getPlayerId)
    errorlog.setAccount_name(player.getAccountName)
    errorlog.setError_msg(msg)
    errorlog.setDid(cache.getImei)
    errorlog.setGame_version(cache.getGame_version)
    errorlog.setOs(cache.getOsName)
    errorlog.setOs_version(cache.getModel)
    errorlog.setDevice(cache.getModel)
    errorlog.setDevice_type(cache.getModel)
    errorlog.setScreen(cache.getScreen)
    errorlog.setMno(cache.getOperators)
    errorlog.setNm(cache.getNet)
    errorlog.setHappend_time(GameUtils.getServerTime)
    sendLog(errorlog)
  }


  def tellMsgToArmygroupNode(mess: AnyRef, id: Long) {
    context.actorSelection("../../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE + id).tell(mess, self)
  }
}
