package com.znl.server.actor.admin

import java.util

import akka.actor.{ActorContext, ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.znl.GameMainServer
import com.znl.define.{ActorDefine, AdminCodeDefine}
import com.znl.framework.http.{HttpMessage, HttpRequestMessage}
import com.znl.msg.GameMsg.{AdminActionMessageToService, AdminActionMessageToServiceBack, AdminActionMessage}
import com.znl.template.ChargeTemplate
import com.znl.utils.GameUtils

/**
 * 管理平台 充值
 * Created by Administrator on 2015/12/21.
 */
class AdminChargeActor extends BasicAdminActor{

  def onAdminActionMessage(httpMessage: HttpMessage) ={
    val msg = httpMessage.getHttpRequest
    val parameterList = util.Arrays.asList("amount", "order_id", "player_id", "callback_info")
    val flag = super.checkParameter(httpMessage, parameterList)

    if(flag){
      val server_id = msg.getParameter("server").toInt  //areaId
      val amount = msg.getParameter("amount").toInt
      val order_id = msg.getParameter("order_id")
      val player_id = msg.getParameter("player_id").toLong
      val callback_info = msg.getParameter("callback_info").toInt //充值的类型0一般充值 1月卡

      log.info("充值："  + amount + " " +  player_id + " " +  order_id)

      val chargeTemplate = new ChargeTemplate(order_id,amount,player_id,callback_info)
//      sendMsgToService(context,server_id,ActorDefine.CHARGE_SERVICE_NAME,AdminActionMessageToService(httpMessage, chargeTemplate))
      sendMsgToChargeService(server_id, AdminActionMessageToService(httpMessage, chargeTemplate))
    }


    def sendMsgToChargeService(areaId : Int,  msg : AnyRef) ={
      val logicId = GameMainServer.getLogicAreaIdByAreaId(areaId)
      val actor = context.actorSelection("../../" + ActorDefine.AREA_SERVER_PRE_NAME + logicId + "/" + ActorDefine.CHARGE_SERVICE_NAME)
      actor ! msg
    }
  }


}
