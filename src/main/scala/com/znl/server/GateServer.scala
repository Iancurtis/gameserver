package com.znl.server

import java.util
import java.util.concurrent.{ConcurrentHashMap}

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{ActorPath, OneForOneStrategy, ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.znl.GameMainServer
import com.znl.define.{ErrorCodeDefine, ActorDefine, ProtocolModuleDefine}
import com.znl.framework.socket.{Response, Request}
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg._
import com.znl.proto.M1
import com.znl.utils.GameUtils
import org.apache.mina.core.session.IoSession

import scala.collection.JavaConversions._
import scala.concurrent.duration._

/**
 * Created by Administrator on 2015/10/22.
 */
class GateServer extends Actor with ActorLogging{

  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      CustomerLogger.error("GateServer出现异常",e)
//      GameUtils.stackTrackLog(log, e.getStackTrace)
      Resume
    }
    case _ => Resume
  }

  val triggerSeconds = GameMainServer.protectTime  //单位秒
  import context.dispatcher
  context.system.scheduler.schedule(triggerSeconds seconds,triggerSeconds seconds,context.self,AutoClearOffPlayerData())

  context.system.scheduler.schedule(50 milliseconds,50 milliseconds,context.self,"TriggerKillPlayer")  //1秒kill掉一次

  val offLineMap = new ConcurrentHashMap[String, IoSession]() // 下线的玩家map，一段时间后清除，实现重连
  val onLineMap = new ConcurrentHashMap[String, IoSession]()

  override def preStart() = {
    log.info("------------gate server start----")
  }

  override def receive: Receive = {
    case SessionOpen(ioSession) =>
      onSessionOpen(ioSession)
    case SessionMessageReceived(ioSession, request) =>
      onSessionMessageReceived(ioSession, request)
    case SessionClose(ioSession) =>
      onSessionClose(ioSession)
    case AutoClearOffPlayerData() =>
      onAutoClearOffPlayerData()
    case "TriggerKillPlayer" =>
      onTriggerKillPlayer()
    case _ =>
  }


  //清楚下线玩家数据
  def onAutoClearOffPlayerData() ={

//    val removeKeyList = new util.ArrayList[String]()
    offLineMap.foreach( f => {
      val session = f._2

      var isKill = true
      if(session.getAttribute(ActorDefine.PLAYER_LOGOUT_TIME_KEY) != null){
        val logoutTime = session.getAttribute(ActorDefine.PLAYER_LOGOUT_TIME_KEY).asInstanceOf[Int]
        if(GameUtils.getServerTime - logoutTime < triggerSeconds){ //退出时间比较短，先保存数据
          onAutoSaveOffPlayerData(session)
          isKill = false
        }
      }

      if(isKill){
//        removeKeyList.add(f._1)
//        onKillPlayer(session)
        killQueue.synchronized{
          killQueue.offer(session)
        }
      }
    })

//    removeKeyList.foreach(key =>{
//      offLineMap.remove(key)
//    })
  }


  def onSessionOpen(ioSession: IoSession) ={
    log.info("---------Session Open----")
  }

  def onSessionClose(ioSession: IoSession) ={
    log.info("------session---close-------")
    val areaId : Int = ioSession.getAttribute(ActorDefine.PLAYER_AREA_ID_KEY).asInstanceOf[Int]
    val accountName : String = ioSession.getAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY).asInstanceOf[String]
    val isLoginSuccess : Boolean = ioSession.getAttribute(ActorDefine.PLAYER_IS_LOGIN_SUCCESS_KEY).asInstanceOf[Boolean]
    log.info("onSessionClose========areaId:"+areaId)

    ioSession.setAttribute(ActorDefine.PLAYER_LOGOUT_TIME_KEY, GameUtils.getServerTime)
    if(accountName == null){
      println("！！！！！accountName == null")
    }else{
      onLineMap.remove(accountName)
      if(!isLoginSuccess){  //没有登录成功的，直接释放掉
        onKillPlayer(areaId, accountName)
      }else{
        offLineMap.put(accountName, ioSession)  //缓存起来，重连使用
      }
    }
  }

  def onAutoSaveOffPlayerData(ioSession: IoSession) : Unit ={
    val areaId : Int = ioSession.getAttribute(ActorDefine.PLAYER_AREA_ID_KEY).asInstanceOf[Int]
    val accountName : String = ioSession.getAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY).asInstanceOf[String]
    this.onAutoSaveOffPlayerData(areaId, accountName)

    log.info("下线自动保存玩家数据accountName:%s".format(accountName))
  }

  def onAutoSaveOffPlayerData(areaId : Int, accountName : String) ={
    val areaServer = getAreaServerActor(areaId)
    areaServer ! AutoSaveOffPlayerData(accountName)
  }

  val killQueue = new util.LinkedList[IoSession]()
  def onTriggerKillPlayer() ={
    if(killQueue.size() > 0){
      killQueue.synchronized{
        val ioSession = this.killQueue.poll()
        val accountName : String = ioSession.getAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY).asInstanceOf[String]
        if(onLineMap.contains(accountName)){ //又在线了

        }else{
          offLineMap.remove(accountName)
          onKillPlayer(ioSession)
        }
      }
    }
  }

  //将player清楚掉
  def onKillPlayer(ioSession: IoSession) : Unit ={
    val areaId : Int = ioSession.getAttribute(ActorDefine.PLAYER_AREA_ID_KEY).asInstanceOf[Int]
    val accountName : String = ioSession.getAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY).asInstanceOf[String]
    this.onKillPlayer(areaId, accountName)

    log.info("将玩家释放掉accountName:%s".format(accountName))
  }

  def onKillPlayer(areaId : Int, accountName : String) : Unit={
    val areaServer = getAreaServerActor(areaId)
    areaServer ! StopPlayerActor(accountName)
  }

  def onSessionMessageReceived(ioSession: IoSession, request: Request) ={
    log.info("received message" + request)
    val moduleId = request.getModule
    val cmdId = request.getCmd

//    System.err.println("++++++++++++++++++++++++接收到cmdId="+cmdId);
    if(moduleId == ProtocolModuleDefine.NET_M1 && cmdId == ProtocolModuleDefine.NET_M1_C9999){
      val proto : M1.M9999.C2S = request.getValue()
      val loginType = proto.getType()
      val areaId = proto.getAreId()
      val accountName = proto.getAccount + "_" + areaId

//      System.out.println(ActorPath.isValidPathElement(accountName))

      //loginType 1正常登陆  2重连登陆

      ioSession.setAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY, accountName)
      println("ioSession设置Att，"+accountName)
      ioSession.setAttribute(ActorDefine.PLAYER_AREA_ID_KEY, areaId)
      //TODO
      if(accountName == null || accountName.length == 0){
        // TODO 网关登录失败，名称有误 通知客户端
      }

      var isOnline = false
      if(onLineMap.containsKey(accountName)){
        isOnline = true
      }

      log.info("网关登录:" + loginType)

      var rc = 0
      // 该玩家的actor还保存着，如果是重连请求的话，直接替换，接入；如果不是，然后错误码，客户端强制等会登陆界面
      //这条socket还有心跳
      if(offLineMap.containsKey(accountName) || isOnline){
        val oldSession = offLineMap.get(accountName)
        if(oldSession != null){
          oldSession.setAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY, null)
          oldSession.close(true)
        }

        val onlineOldSession = onLineMap.get(accountName)
        if(onlineOldSession != null){ //顶号了
          //TODO !!!!发送消息给客户端，通知异地登陆了
          val response = Response.valueOf(ProtocolModuleDefine.NET_M1, ProtocolModuleDefine.NET_M1_C9998, M1.M9998.S2C.newBuilder().setRs(ErrorCodeDefine.M9998_1).build())
          onlineOldSession.write(response)
          onlineOldSession.setAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY, null)
          onlineOldSession.close(true)
        }

        if(loginType == 2){ //重连成功  需要替换信息session

        }else{

        }

        //TODO 重连成功 替换掉当前player的session
        val areaServer = getAreaServerActor(areaId)
        areaServer ! ReplacePlayerSession(accountName, ioSession)

        onLineMap.put(accountName, ioSession)
        offLineMap.remove(accountName)


      }else{
        onLineMap.put(accountName, ioSession)
        if(loginType == 2){  //重连请求时，服务器已经释放掉了，发送错误码，客户端强制跳回登陆界面 直接不创建actor了
          rc = 2
        } else{
          //TODO 发送到具体AreaServer 创建新得Player Actor
          //通过areaId映射到具体的 AreaServer
        }
        val areaServer = getAreaServerActor(areaId)
        areaServer ! CreatePlayerActor(accountName, areaId, ioSession)
      }

      val response = Response.valueOf(ProtocolModuleDefine.NET_M1, ProtocolModuleDefine.NET_M1_C9999, M1.M9999.S2C.newBuilder().
        setRs(rc).build())
      ioSession.write(response)

    }else {
      val areaId : Int = ioSession.getAttribute(ActorDefine.PLAYER_AREA_ID_KEY).asInstanceOf[Int]
      val accountName : String = ioSession.getAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY).asInstanceOf[String]
      if(areaId.equals(null)){
        log.error("角色没有区ID！！") //TODO
      }else
      {
        //TODO 将消息发送到具体的AreaServer 再广播到具体的玩家
        val areaServer = getAreaServerActor(areaId)
        areaServer ! SendPlayerNetMsg(accountName, request)
      }
    }
  }


  def getAreaServerActor(areaId : Int) ={
    //TODO 根据策略获取
//    println("GateServer获取areaId="+areaId)
    val logicAreaId = GameMainServer.getLogicAreaIdByAreaId(areaId)
    val areaServer = context.actorSelection("../" + ActorDefine.AREA_SERVER_PRE_NAME + logicAreaId)
    areaServer
  }
}
