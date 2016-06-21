package com.znl.server.actor.admin

import java.util

import akka.actor.{ActorLogging, Actor}
import com.znl.define.AdminCodeDefine
import com.znl.framework.http.{HttpRequestMessage, HttpMessage}
import com.znl.msg.GameMsg.{AdminActionMessageToServiceBack, AdminActionMessage}
import com.znl.utils.GameUtils

/**
 * 管理类积累
 * Created by Administrator on 2016/1/6.
 */
abstract class BasicAdminActor extends Actor with ActorLogging with AdminTrait{
  override def receive: Receive = {
    case AdminActionMessage(msg) =>
      onAdminActionMessage(msg)
    case AdminActionMessageToServiceBack(http, result) =>
      onAdminActionMessageToServiceBack(http, result)
  }

  def onAdminActionMessageToServiceBack(http : HttpMessage, result : String): Unit ={
    println("@@@@@@@@@@@接收到http请求返回，result = "+result)
    http.sendContent(result)
  }

  def checkParameter(httpMessage: HttpMessage, parameterList: util.List[java.lang.String]) ={
    val msg = httpMessage.getHttpRequest
    val hasNull = GameUtils.checkParameter(msg, parameterList)
    if(hasNull.size() > 0){
      val error = GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.ACTION_PARAMETER_FAIL, "参数不全：" + hasNull.toString)
      httpMessage.sendContent(error)
      false
    }else{
      true
    }
  }

  def onAdminActionMessage(httpMessage: HttpMessage)


}
