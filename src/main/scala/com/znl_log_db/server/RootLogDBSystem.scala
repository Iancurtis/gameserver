package com.znl.server

import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Resume
import akka.actor.{ActorLogging, OneForOneStrategy, Actor, Props}
import com.znl.GameMainServer
import com.znl.define.ActorDefine
import com.znl.service.LogMysqlServer
import com.znl.service.actor.MySqlServer
import com.znl.utils.GameUtils

import scala.collection.JavaConversions._

object RootLogDBSystem{
  def props(p: Properties) ={
    Props(classOf[RootLogDBSystem], p)
  }
}

/**
 * 游戏根系统
 * Created by Administrator on 2015/12/29.
 */
class RootLogDBSystem(p: Properties) extends Actor with ActorLogging{

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
    context.watch(context.actorOf(LogMysqlServer.props(p), "logMysqlServer"))
  }

}