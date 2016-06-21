package com.znl.service.functionNode

import java.util
import java.util.Date

import akka.actor.{Props, ActorLogging, Actor}
import com.znl.core.PlayerChat
import com.znl.define.{ChatAndMailDefine, ActorDefine}
import com.znl.msg.GameMsg
import scala.language.postfixOps

object ChatNode{
  def props(chatSize : Int,chatType :Int) = Props(classOf[ChatNode], chatSize,chatType)
}
class ChatNode(chatSize : Int,chatType :Int) extends Actor with ActorLogging {
  override def receive: Receive = {
    case getChat : GameMsg.GetChat =>
      onRefGetChat(getChat.playerIndex,getChat.accoutName)
    case add : GameMsg.AddChat =>
      addChat(add)
    case _ =>
  }

  private val chatText: Array[PlayerChat] = new Array[PlayerChat](chatSize)
  private var chatIndex: Int = 0

  /**
   * Description: 添加一条聊天信息
   */
  def addChat(add : GameMsg.AddChat) {
    val chat : PlayerChat = add.playerChat
    chatText synchronized {
      if (chatIndex >= chatText.length) {
        chatIndex = 0
      }
      chatText(chatIndex) = chat
      chatIndex += 1
    }
  }

  /**
   * Description:当前时间聊天的索引
   * @return
   */
  def getChatIndex: Int = {
    return chatIndex
  }

  def onRefGetChat(playerIndex : Int,accountName : String) :Unit = {
//    println("=======聊天服务接收到" + accountName + "发送消息GetChat" + playerIndex +"====="+ new Date)
    if(playerIndex < 0){
      var newIndex = 0
      if(chatIndex > 20){
        newIndex = chatIndex-20
      }
      sendMsgToPlayerChatModule(accountName,GameMsg.GetSystemChatIndex(newIndex,chatType))
    }else{
      val chats: util.ArrayList[PlayerChat] = getChatText(playerIndex)
      if(chats != null && chats.size() > 0){
        context.parent ! GameMsg.SendChatToPlayer(accountName,chats,chatType,chatIndex);
      }
    }
  }

  //发送消息给具体的player聊天模块
  def sendMsgToPlayerChatModule(accountName : String, msg : AnyRef) ={
    val actor = context.actorSelection("../../"+ActorDefine.PLAYER_SERVICE_NAME+"/"+accountName+"/"+ActorDefine.CHAT_MODULE_NAME)
    actor ! msg
  }

  /**
   * Function name:getChatTextWorld
   * Description: 根据个人的聊天索引提取没看的信息
   * @return
   */
  def getChatText(palyerIndex: Int): util.ArrayList[PlayerChat] = {
    val sysIndex: Int = chatIndex;
    if (palyerIndex == sysIndex) {
      return null
    } else {
      val tempList: util.ArrayList[PlayerChat] = new util.ArrayList[PlayerChat]
      if (palyerIndex < sysIndex) {
        {
          var i: Int = palyerIndex
          while (i < sysIndex && tempList.size < chatSize) {
            {
              if (chatText(i) != null) {
                tempList.add(chatText(i))
              }
            }
            ({
              i += 1;
              i - 1
            })
          }
        }
      }else {
        {
          var i: Int = palyerIndex
          while (i < chatText.length && tempList.size < chatSize) {
            {
              if (chatText(i) != null) {
                tempList.add(chatText(i))
              }
            }
            ({
              i += 1;
              i - 1
            })
          }
        }
        {
          var i: Int = 0
          while (i < sysIndex && tempList.size < chatSize) {
            {
              if (chatText(i) != null) {
                tempList.add(chatText(i))
              }
            }
            ({
              i += 1;
              i - 1
            })
          }
        }
      }
      val resList: util.ArrayList[PlayerChat] = new util.ArrayList[PlayerChat]
      var index: Int = 0
      //每个玩家只要发50条就够了
      if (tempList.size > ChatAndMailDefine.CLIENT_GET_CHAT_NUM) {
        index = tempList.size - ChatAndMailDefine.CLIENT_GET_CHAT_NUM
      }
      while (index < tempList.size) {
        {
          resList.add(tempList.get(index))
        }
        ({
          index += 1; index - 1
        })
      }
      return tempList
    }
  }
}
