package com.znl.server

import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Resume
import akka.actor.{ActorLogging, OneForOneStrategy, Actor, Props}
import com.znl.GameMainServer
import com.znl.define.ActorDefine
import com.znl.service.actor.MySqlServer
import com.znl.utils.GameUtils

import scala.collection.JavaConversions._

object RootGameDBSystem{
  def props(p: Properties) ={
    Props(classOf[RootGameDBSystem], p)
  }
}

/**
 * 游戏根系统
 * Created by Administrator on 2015/12/29.
 */
class RootGameDBSystem(p: Properties) extends Actor with ActorLogging{

  val port: Int = p.getProperty("port").toInt
  val http_port : Int = p.getProperty("http_port").toInt
  val redis_ip: String = p.getProperty("redis_ip")
  val redis_port: Int = p.getProperty("redis_port").toInt
  val mysql_ip : String = p.getProperty("mysql_ip")
  val mysql_db : String = p.getProperty("mysql_db")
  val mysql_user : String = p.getProperty("mysql_user")
  val mysql_password : String = p.getProperty("mysql_password")
  val protectTime = p.getProperty("protect_time").toInt //保护时间


  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      log.error(e.fillInStackTrace(), e.getMessage)
      Resume
    }
    case _ => Resume
  }

  override def preStart() ={
    startMysqlServer()
  }

  override def receive: Receive = {
    case _=>
  }


  def startMysqlServer() ={
    context.watch(context.actorOf(MySqlServer.props(mysql_ip, mysql_db,mysql_user,mysql_password), "mysqlServer"))
  }


}