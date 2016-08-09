package com.znl.server.actor.admin

import java.util

import com.znl.define.ActorDefine
import com.znl.framework.http.HttpMessage
import com.znl.msg.GameMsg.{BanPlayerHandle, SendMail}
import com.znl.utils.GameUtils

import scala.collection.JavaConversions._

/**
 * Created by Administrator on 2016/1/20.
 */
class AdminBanActor extends BasicAdminActor{
  override def onAdminActionMessage(httpMessage: HttpMessage): Unit = {
    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("banType", "data", "status")
    val flag = super.checkParameter(httpMessage, parameterList)
    if(flag){

      val banType = java.net.URLDecoder.decode(msg.getParameter("banType"), "utf-8")
      val data = java.net.URLDecoder.decode(msg.getParameter("data"), "utf-8")
      val status = msg.getParameter("status").toInt
      var banDate : Int = GameUtils.getServerTime
      val dateStr = java.net.URLDecoder.decode(msg.getParameter("banDate"), "utf-8")
      if (dateStr.length > 0){
        banDate = msg.getParameter("banDate").toInt
      }
      val reason = java.net.URLDecoder.decode(msg.getParameter("reason"), "utf-8")
      val server_id = msg.getParameter("server").toInt
      val dataList = new util.ArrayList[String]()
      data.split(",").foreach( value => dataList.add(value))

      banType match {
        case "BanRole" => //玩家昵称禁号
          sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, BanPlayerHandle(banType, dataList, status, banDate, reason))
        case "BanAct" => //账号禁言
          sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, BanPlayerHandle(banType, dataList, status, banDate, reason))
        case "BanChat" => //TODO 玩家禁言操作
          sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, BanPlayerHandle(banType, dataList, status, banDate, reason))
        case "BanChatAct" =>
          sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, BanPlayerHandle(banType, dataList, status, banDate, reason))
        case "BanIP" =>
          sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, BanPlayerHandle(banType, dataList, status, banDate, reason))
        case _ =>
      }


      val result = GameUtils.getAdminStatusJsonMsg(0, "发送成功")
      httpMessage.sendContent(result)
    }
  }
}
