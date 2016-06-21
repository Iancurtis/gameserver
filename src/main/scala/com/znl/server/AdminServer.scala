package com.znl.server

import java.util

import akka.actor.{Props, Actor, ActorLogging}
import com.znl.GameMainServer
import com.znl.define.{AdminCodeDefine, ActorDefine}
import com.znl.framework.http.{HttpMessage, HttpRequestMessage}
import com.znl.msg.GameMsg.{AdminActionMessage, AdminMessageReceived, GMCommand}
import com.znl.server.actor.admin._
import com.znl.utils.GameUtils

/**
 * Created by Administrator on 2015/10/29.
 */
class AdminServer extends Actor with ActorLogging{

  context.watch(context.actorOf(Props(classOf[AdminChargeActor]), ActorDefine.ADMIN_CHARGE_NAME))
  context.watch(context.actorOf(Props(classOf[AdminBroadcastActor]), ActorDefine.ADMIN_NEWS_BROADCAST))
  context.watch(context.actorOf(Props(classOf[AdminMailActor]), ActorDefine.ADMIN_SEND_MAIL))
  context.watch(context.actorOf(Props(classOf[AdminSendGiftActor]), ActorDefine.ADMIN_SEND_GIFT))
  context.watch(context.actorOf(Props(classOf[AdminBanActor]), ActorDefine.ADMIN_BAN))
  context.watch(context.actorOf(Props(classOf[AdminKickActor]), ActorDefine.ADMIN_KICK))
  context.watch(context.actorOf(Props(classOf[AdminUserInfoListActor]), ActorDefine.ADMIN_USER_INFO_LIST))
  context.watch(context.actorOf(Props(classOf[AdminInstructorActor]), ActorDefine.ADMIN_INSTRUCTOR))

  override def preStart() ={
    log.info("-----AdminServer---start nm--------")
  }

  override def receive: Receive = {
    //从管理平台发送过来的信息
    case AdminMessageReceived(msg) =>
      onAdminMessageReceived(msg)
    //直接从别的进程发过来的消息
    case GMCommand(command) =>
      onGMCommand(command)
    case _ =>
  }

  //统一。所有的操作都需要server_id这个参数！！
  def onAdminMessageReceived(httpMessage: HttpMessage): Unit ={
    val msg = httpMessage.getHttpRequest
    val value =  msg.getParameter("server")
    val isNull = value == null

    log.info(httpMessage.getHttpRequest.toString)

    if(isNull || value.length == 0){
      val error = GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.ACTION_PARAMETER_FAIL, "参数不全：server_id")
      httpMessage.sendContent(error)
    }else{
      val cmd = msg.getContext
      if(context.child(cmd) == None){
        val error = GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.ACTION_UNKNOWN_FAIL, "未知行为")
        httpMessage.sendContent(error)
      }else{
        context.actorSelection(cmd) ! AdminActionMessage(httpMessage)
      }
    }
  }

  def onGMCommand(command : String) ={
    log.info("--------onGMCommand---------" + command)
    if(command.equals("stop")){
      GameMainServer.stopServer()
    }
  }
}
