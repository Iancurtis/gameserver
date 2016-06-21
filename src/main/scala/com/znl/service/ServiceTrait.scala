package com.znl.service

import akka.actor.ActorContext
import com.znl.define.ActorDefine
import com.znl.utils.GameUtils

/**
 * Service服务逻辑的工具类  只能service才能with使用
 * Created by Administrator on 2015/11/12.
 */
trait ServiceTrait {

  def getPlayerService(actorContext: ActorContext) ={
    actorContext.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME)
  }

  def getTriggerService(actorContext: ActorContext) ={
    actorContext.actorSelection("../" + ActorDefine.TRIGGER_SERVICE_NAME)
  }

  //获取某个service的消息值
  def askService[T](actorContext: ActorContext, serviceName : String, msg : AnyRef) ={
    val ref = actorContext.actorSelection("../" + serviceName)
    val value : Option[T]  = GameUtils.futureAsk(ref, msg, 10)

    val result: T = value.getOrElse(null).asInstanceOf[T]
    result
  }

  //通知某个服务
  def tellService(actorContext: ActorContext, serviceName : String, msg : AnyRef) ={
    val ref = actorContext.actorSelection("../" + serviceName)
    ref ! msg
  }

  //发送消息到日志服务
  def sendMsgLogServer(actorContext: ActorContext, msg : AnyRef) ={
    tellService(actorContext, ActorDefine.LOG_SERVER_NAME, msg)
  }

  //发送消息到某个玩家的某个模块
  def sendMsgPlayerModule(actorContext: ActorContext, accountName : String, moduleName : String, msg : AnyRef) ={
    val path = "../%s/%s/%s".format(ActorDefine.PLAYER_SERVICE_NAME, accountName, moduleName)
    val ref = actorContext.actorSelection(path)
    ref ! msg
  }

  def sendAdminLogToService(actorContext: ActorContext, msg : AnyRef) ={
    tellService(actorContext, ActorDefine.ADMIN_LOG_SERVICE_NAME, msg)
  }

}
