package com.znl.server.actor.admin

import java.util

import com.znl.define.ActorDefine
import com.znl.framework.http.HttpMessage
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg.{AdminInstructorGM, AdminKickPlayerOffline}
import com.znl.utils.GameUtils

/**
 * Created by Administrator on 2016/2/25.
 */
class AdminInstructorActor extends BasicAdminActor {
  override def onAdminActionMessage(httpMessage: HttpMessage): Unit = {
    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("sendType", "data", "type", "instructorType", "startTime", "endTime")
    val flag = super.checkParameter(httpMessage, parameterList)
    if(flag){
      val sendType = java.net.URLDecoder.decode(msg.getParameter("sendType"), "utf-8")
      val data = java.net.URLDecoder.decode(msg.getParameter("data"), "utf-8")
      val optType = msg.getParameter("type").toInt
      val instructorType = msg.getParameter("instructorType").toInt
      val startTime = msg.getParameter("startTime").toInt
      val endTime = msg.getParameter("endTime").toInt
      val server_id = msg.getParameter("server").toInt
      val dataList = new util.ArrayList[String]()
      data.split(",").foreach( value => dataList.add(value))
      sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, GameMsg.AdminInstructorGM(sendType, dataList, optType, instructorType,startTime,endTime))
      val result = GameUtils.getAdminStatusJsonMsg(0, "发送成功")
      httpMessage.sendContent(result)
    }

  }
}
