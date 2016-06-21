package com.znl.server.actor.admin

import java.util

import akka.actor.Actor.Receive
import com.znl.core.PlayerChat
import com.znl.define.{ActorDefine, ChatAndMailDefine}
import com.znl.framework.http.HttpMessage
import com.znl.log.CustomerLogger
import com.znl.msg.GameMsg
import com.znl.utils.GameUtils

/**
 * 消息广播接口 1弹窗聊天栏， 2表示顶部滚动, 3聊天栏。
 * Created by Administrator on 2015/12/15.
 */
class AdminBroadcastActor extends BasicAdminActor{

  override def onAdminActionMessage(httpMessage: HttpMessage): Unit = {
    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("type", "content")
    val flag = super.checkParameter(httpMessage, parameterList)
    CustomerLogger.info("！！！！！接收到网络消息，消息广播"+flag)
    if(flag){
      val server_id = msg.getParameter("server").toInt  //areaId
      val btype = msg.getParameter("type").toInt
      val content = java.net.URLDecoder.decode(msg.getParameter("content"), "utf-8")
      btype match {
        case 1 =>  //弹窗聊天栏
        case 2 => //表示顶部滚动
          CustomerLogger.info("表示顶部滚动"+content)
          sendMsgToService(context, server_id, ActorDefine.PLAYER_SERVICE_NAME, GameMsg.trumpeNotity(0l,"系统提示",content,0))
        case 3 =>  //聊天栏
          CustomerLogger.info("聊天栏消息"+content)
          val playerChat = new PlayerChat
          playerChat.`type` =  ChatAndMailDefine.CHAT_TYPE_WORLD
          playerChat.context = content
          playerChat.playerId = -1l
          playerChat.playerName = "系统信息"
          playerChat.iconId = ActorDefine.SYSTEM_NOTICE_HEAD_TYPE
          sendMsgToService(context, server_id, ActorDefine.CHAT_SERVICE_NAME, GameMsg.AddChat(playerChat))
      }

      val result = GameUtils.getAdminStatusJsonMsg(0, "发送成功")
      httpMessage.sendContent(result)
    }
  }
}
