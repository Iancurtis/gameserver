package com.znl.server.actor.admin

import java.util

import com.znl.define.ActorDefine
import com.znl.framework.http.HttpMessage
import com.znl.msg.GameMsg.{AdminKickPlayerOffline}
import com.znl.utils.GameUtils

/**
 * Created by Administrator on 2016/2/20.
 */
class AdminKickActor extends BasicAdminActor{
  override def onAdminActionMessage(httpMessage: HttpMessage): Unit = {
    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("sendType", "data", "kickAll")
    val flag = super.checkParameter(httpMessage, parameterList)
    if(flag){
      val sendType = java.net.URLDecoder.decode(msg.getParameter("sendType"), "utf-8")
      val data = java.net.URLDecoder.decode(msg.getParameter("data"), "utf-8")
      val kickAll = msg.getParameter("kickAll").toInt
      val reason = java.net.URLDecoder.decode(msg.getParameter("reason"), "utf-8")

      val server_id = msg.getParameter("server").toInt

      val dataList = new util.ArrayList[String]()
      data.split(",").foreach( value => dataList.add(value))

      sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, AdminKickPlayerOffline(sendType, dataList, kickAll, reason))
      val result = GameUtils.getAdminStatusJsonMsg(0, "发送成功")
      httpMessage.sendContent(result)
    }
  }
}
