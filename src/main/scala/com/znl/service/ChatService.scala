package com.znl.service

import java.util

import akka.actor.{ActorLogging, Actor}
import com.znl.core.PlayerChat
import com.znl.define.{ActorDefine, ChatAndMailDefine}
import com.znl.msg.{PushShareMsg, ShareMsg, GameMsg}
import com.znl.msg.GameMsg._
import com.znl.service.functionNode.ChatNode
import scala.concurrent.duration._
/**
 * Created by Administrator on 2015/11/30.
 */
class ChatService  extends Actor with ActorLogging with ServiceTrait{

  def sendChatsToPlayer(accountName : String,message : SendChatToPlayer): Unit = {
    sendMsgToPlayerActor(accountName,message)
  }

  import context.dispatcher
  context.system.scheduler.schedule(5 seconds, 5 seconds,context.self, OnServerTrigger())

  override def receive: Receive = {
    case message : SendChatToPlayer =>
      sendChatsToPlayer(message.accoutName,message)
    case message : AddChat =>
      addChatToChatNode(message.playerChat)
    case message : GetChat =>
      getChatNotifyToWorldChatNode(message.playerIndex,message.accoutName)
    case message : GetLegionChat =>
      getChatNotifyToLegionChatNode(message.playerIndex,message.accoutName,message.legionId)
    case message : CreateLegionChatNode =>
      onCreateLegionChatNode(message.legionId)
    case m : ShareMsg =>
      addShareMsg(m)
    case OnServerTrigger()=>
      pushShareMsg()
    case _ =>
  }


  val shareList = new util.ArrayList[ShareMsg]()
  def addShareMsg(m :ShareMsg): Unit ={
    shareList.add(m)
  }

  def pushShareMsg(): Unit ={
    if(shareList.size() > 0){
      shareList.synchronized{
        val pushList = new util.ArrayList[ShareMsg](shareList)
        shareList.clear()
        tellService(context,ActorDefine.PLAYER_SERVICE_NAME,PushShareMsg(pushList))
      }
    }
  }

  def getChatNotifyToLegionChatNode(index : Int ,accountName : String ,legionId : Long): Unit ={
    sendMessageToChatNode(ChatAndMailDefine.CHAT_NODE_NAME_LEGION+"_"+legionId,GameMsg.GetChat(index,accountName))
  }
  def getChatNotifyToWorldChatNode(index : Int ,accountName : String): Unit ={
    sendMessageToChatNode(ChatAndMailDefine.CHAT_NODE_NAME_WORLD,GameMsg.GetChat(index,accountName))
  }

  def addChatToChatNode(chat: PlayerChat) ={
    if(chat.`type` == ChatAndMailDefine.CHAT_TYPE_WORLD){
      sendMessageToChatNode(ChatAndMailDefine.CHAT_NODE_NAME_WORLD,GameMsg.AddChat(chat))
    }else if(chat.`type` == ChatAndMailDefine.CHAT_TYPE_LEGION){
      sendMessageToChatNode(ChatAndMailDefine.CHAT_NODE_NAME_LEGION+"_"+chat.legionId,GameMsg.AddChat(chat))
    }
  }

  def sendMessageToChatNode(name :String,anyRef: AnyRef): Unit ={
    val node = context.actorSelection(name)
    if(node != null){
      node ! anyRef
    }
  }

  def onCreateLegionChatNode(legionId : Long): Unit ={
    val actor = context.actorOf(ChatNode.props(ChatAndMailDefine.MAX_CHAT_LEGION,ChatAndMailDefine.CHAT_TYPE_LEGION), ChatAndMailDefine.CHAT_NODE_NAME_LEGION+"_"+legionId)
    context.watch(actor)
  }

  override def preStart() = {
    val actor = context.actorOf(ChatNode.props(ChatAndMailDefine.MAX_CHAT_WORLD,ChatAndMailDefine.CHAT_TYPE_WORLD), ChatAndMailDefine.CHAT_NODE_NAME_WORLD)
    context.watch(actor)
  }

  //发送消息给具体的player聊天模块
  def sendMsgToPlayerActor(accountName : String, msg : AnyRef) ={
    val actor = context.actorSelection("../"+ActorDefine.PLAYER_SERVICE_NAME+"/"+accountName+"/"+ActorDefine.CHAT_MODULE_NAME)
    actor ! msg
  }
}
