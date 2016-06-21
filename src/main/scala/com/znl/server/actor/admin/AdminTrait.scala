package com.znl.server.actor.admin

import akka.actor.{ActorContext}
import com.znl.GameMainServer
import com.znl.define.ActorDefine

/**
 * Created by Administrator on 2015/12/22.
 */
trait AdminTrait {

  //发送消息到具体的Service
  def sendMsgToService(context : ActorContext, areaId : Int, serviceName : String,  msg : AnyRef) ={
    val logicId = GameMainServer.getLogicAreaIdByAreaId(areaId)
    val actor = context.actorSelection("../../" + ActorDefine.AREA_SERVER_PRE_NAME + logicId + "/" + serviceName)
    actor ! msg
  }
}
