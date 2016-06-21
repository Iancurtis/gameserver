package com.znl.proxy

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import com.google.protobuf.GeneratedMessage
import com.znl.GameMainServer
import com.znl.base.BaseDbPojo
import com.znl.define.ActorDefine
import com.znl.msg.GameMsg.{FinalizeDbPojo, CreateProtoGeneratedMessage, GetProtoGeneratedMessage}
import com.znl.server.DbServer
import com.znl.utils.GameUtils

/**
 * Created by Administrator on 2015/10/26.
 */
object DbProxy {

  def ask[T](msg : AnyRef, seconds : Int = 10) ={
    val system: ActorSystem = GameMainServer.system
    val dbService: ActorSelection = system.actorSelection(ActorDefine.DB_SERVER_PATH)
    val value : Option[T]  = GameUtils.futureAsk(dbService, msg, seconds)

    val result: T = value.getOrElse(null).asInstanceOf[T]

    result
  }

  def tell(msg : AnyRef, sendRef : ActorRef) ={
    val system: ActorSystem = GameMainServer.system
    val dbService: ActorSelection = system.actorSelection(ActorDefine.DB_SERVER_PATH)
    dbService.tell(msg, sendRef)
  }

  def getDbPojo(id: Long, pojoClass: Class[_],pushDataMap : Boolean) ={
    DbServer.getDbPojo(id, pojoClass,pushDataMap).getOrElse(null)
  }

  def createDbPojo(pojoClass: Class[_],areaId : Int) ={
    DbServer.createDbPojo(pojoClass,areaId).getOrElse(null)
  }

  def delDbPojo(pojo: BaseDbPojo) ={
    DbServer.delDbPojo(pojo)
  }

  def finalizeDbPojo(pojo: BaseDbPojo) ={
    DbServer.finalizeDbPojo(pojo)
    tell(FinalizeDbPojo(pojo))
  }

  def tell(msg : AnyRef) ={
    val system: ActorSystem = GameMainServer.system
    val dbService: ActorSelection = system.actorSelection(ActorDefine.DB_SERVER_PATH)
    dbService.tell(msg, ActorRef.noSender)
  }

  def createProtoGeneratedMessage(cmd : Int, msg : GeneratedMessage, expire : Int) : Long ={
    val id : Long =  DbServer.createProtoGeneratedMessage(cmd , msg, expire)
    id
  }

  //保存协议体
  def createProtoGeneratedMessage(cmd : Int,  msg : GeneratedMessage) : Long ={
    val id : Long = createProtoGeneratedMessage(cmd, msg, 30 * 24 * 60)
    id
  }


  //获取协议体二进制
  def getProtoGeneratedMessageBytes(cmd : Int, id : Long) ={
    val result : Array[Byte] = ask(GetProtoGeneratedMessage(cmd, id))
    result
  }



}
