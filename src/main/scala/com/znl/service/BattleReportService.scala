package com.znl.service

import java.util

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}
import com.google.protobuf.GeneratedMessage
import com.znl.base.BaseDbPojo
import com.znl.define.{ReportDefine, ActorDefine, ProtocolModuleDefine}
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg._
import com.znl.pojo.db.{Player, Report}
import com.znl.proto.{M5, M1}
import com.znl.proxy.DbProxy
import com.znl.template.ReportTemplate

//import com.znl.msg.GameMsg.AddBattleProto

/**
  * Created by Administrator on 2015/12/10.
  */
class BattleReportService extends Actor with ActorLogging {

  override def receive: Receive = {
    case AddMailBattleProto(reportTemplate: ReportTemplate, cmd: String) =>
      onAddBattleProto(reportTemplate, cmd)
    case GetBattleProto(id: Long, cmd: String) =>
      onGetBattleProto(id, cmd)
    case AddLimitchangeBattleProto(msg : GeneratedMessage,dungeoOrder:Int) =>
      onAddLimitchangeBattleProto(msg,dungeoOrder)
    case _ =>
  }

  def onAddLimitchangeBattleProto(msg : GeneratedMessage,dungeoOrder:Int): Unit ={
    val id: Long= onAddBattleProto(msg)
    sender() ! AddLimitchangeBattleProtoBack(id, dungeoOrder)
  }

  def sendReportMailToPlayer(id: Long, reportTemplate: ReportTemplate) = {
    sendMsgToService(ActorDefine.MAIL_SERVICE_NAME, GameMsg.SendReport(reportTemplate, id))
  }

  def onAddBattleProto(reportTemplate: ReportTemplate, cmd: String) = {
    //    val report : Report = createReport(reportTemplate)
    reportTemplate.getReportType match {
      case ReportDefine.REPORT_TYPE_ATTACK =>
        val id: Long = DbProxy.createProtoGeneratedMessage(ProtocolModuleDefine.NET_M5_C50000, reportTemplate.getMessage)
        sendReportMailToPlayer(id, reportTemplate)
      case ReportDefine.REPORT_TYPE_ARENA =>
        val id: Long = DbProxy.createProtoGeneratedMessage(ProtocolModuleDefine.NET_M5_C50000, reportTemplate.getMessage)
        sendReportMailToPlayer(id, reportTemplate)
    }
  }


  def onAddBattleProto(message: GeneratedMessage): Long = {
    val id: Long = DbProxy.createProtoGeneratedMessage(ProtocolModuleDefine.NET_M5_C50000, message)
    id
  }

  def onGetBattleProto(id: Long, cmd: String): Unit = {
    val res: Array[Byte] = DbProxy.getProtoGeneratedMessageBytes(ProtocolModuleDefine.NET_M5_C50000, id);

    var msg: GeneratedMessage = null
    if (res != null) {
      msg = M5.M50000.S2C.newBuilder.mergeFrom(res).build
    }
    sender() ! GetBattleProtoSuccess(msg, cmd)
  }


  def sendMsgToService(serviceNme: String, anyRef: AnyRef): Unit = {
    val actor = context.actorSelection("../" + serviceNme)
    actor ! anyRef
  }
}
