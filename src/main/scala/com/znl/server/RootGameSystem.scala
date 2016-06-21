package com.znl.server

import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Resume
import akka.actor.{ActorLogging, OneForOneStrategy, Actor, Props}
import com.znl.GameMainServer
import com.znl.define.ActorDefine
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg.StopGame
import com.znl.utils.GameUtils

import scala.collection.JavaConversions._

object RootGameSystem{
  def props(p: Properties) ={
    Props(classOf[RootGameSystem], p)
  }
}

/**
 * 游戏根系统
 * Created by Administrator on 2015/12/29.
 */
class RootGameSystem(p: Properties) extends Actor with ActorLogging{

  val port: Int = p.getProperty("port").toInt
  val http_port : Int = p.getProperty("http_port").toInt
  val redis_ip: String = p.getProperty("redis_ip")
  val redis_port: Int = p.getProperty("redis_port").toInt
  val mysql_ip : String = p.getProperty("mysql_ip")
  val mysql_db : String = p.getProperty("mysql_db")
  val mysql_user : String = p.getProperty("mysql_user")
  val mysql_password : String = p.getProperty("mysql_password")
  val protectTime = p.getProperty("protect_time").toInt //保护时间

  val areaKeyMap : ConcurrentHashMap[Int, String] = new ConcurrentHashMap[Int, String]()
  p.propertyNames().foreach(v => {
    val name = v.toString
    if(name.toString.indexOf("areaServer") >= 0){
      val areaLoginId = name.replace("areaServer", "").toInt
      val areaKey : String = p.getProperty(name)
      areaKeyMap.put( areaLoginId , areaKey)
    }
  })

  startDbServer(redis_ip , redis_port, mysql_ip, mysql_db, mysql_user, mysql_password)

  startGateServer()

  areaKeyMap.foreach( area => {
    startAreaServer(area._1, p)
  })

  startAdminServer()
  startTriggerServer()
  startPushServer()
  starLogServer()

  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      log.error(e.fillInStackTrace(), e.getMessage)
      CustomerLogger.error("RootGameSystem",e)
      Resume
    }
    case _ => Resume
  }

  override def preStart() ={

  }

  override def receive: Receive = {
    case StopGame() =>
      context.watch(context.actorOf(StopServer.props(null, null, null), "stopServer"+GameUtils.getRandomValueByRange(50000)))
    case _=>
  }


  def startDbServer(redisIp:String, redisPort:Int, mysql_ip : String, mysql_db : String, mysql_user : String, mysql_pwd : String) ={
    context.watch(context.actorOf(DbServer.props(redisIp, redisPort, mysql_ip, mysql_db, mysql_user,mysql_pwd,p), ActorDefine.DB_SERVER_NAME))
  }

  def startGateServer() ={
    context.watch(context.actorOf(Props[GateServer], ActorDefine.GATE_SERVER_NAME))
  }

  def startAreaServer(logicAreaId : Int, p: Properties) ={
    context.watch(context.actorOf(AreaServer.props(logicAreaId, GameMainServer.getAreaKeyByLogicAreaId(logicAreaId), p), ActorDefine.AREA_SERVER_PRE_NAME + logicAreaId))
  }

  def startAdminServer() ={
    context.watch(context.actorOf(Props(classOf[AdminServer]), ActorDefine.ADMIN_SERVER_NAME))
  }

  def startTriggerServer() ={
    context.watch(context.actorOf(Props[TriggerServer], ActorDefine.TRIGGER_SERVER_NAME))
  }

  def startPushServer() ={
    context.watch(context.actorOf(Props[PushServer], ActorDefine.PUSH_SERVER_NAME))
  }

  def starLogServer()={
    context.watch(context.actorOf(Props[LogServer], ActorDefine.LOG_SERVER_NAME))
  }


}
