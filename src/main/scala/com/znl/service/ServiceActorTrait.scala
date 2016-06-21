package com.znl.service

import akka.actor.ActorContext
import com.znl.define.ActorDefine

/**
 * service服务下面的子Actor工具类，仅service下面的隶属actor才能with使用
 * Created by Administrator on 2015/12/5.
 */
trait ServiceActorTrait {

  //通知某个服务
  def tellServer(actorContext: ActorContext, serverName : String, msg : AnyRef) ={
    val ref = actorContext.actorSelection("../../../" + serverName)
    ref ! msg
  }

  //发送消息到日志服务
  def sendMsgLogServer(actorContext: ActorContext, msg : AnyRef) ={
    tellServer(actorContext, ActorDefine.LOG_SERVER_NAME, msg)
  }

  def tellService(actorContext: ActorContext, serviceName : String, msg : AnyRef) ={
    val ref = actorContext.actorSelection("../../" + serviceName)
    ref ! msg
  }

  def sendAdminLogToService(actorContext: ActorContext, msg : AnyRef) ={
    tellService(actorContext, ActorDefine.ADMIN_LOG_SERVICE_NAME, msg)
  }


}
