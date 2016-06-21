package com.znl.service

import java.util

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorLogging, Actor}
import com.znl.core.SimplePlayer
import com.znl.define.ActorDefine
import com.znl.msg.GameMsg.{FriendBlessPlayers, WatchFriendInfoList, WatchFriendInfoListBack}

import scala.collection.JavaConversions._

/**
 * 好友服务，统筹好友交互
 * Created by Administrator on 2015/12/7.
 */
object FriendService {
  def props(areaKey: String) = Props(classOf[FriendService], areaKey)
}

class FriendService(areaKey: String) extends Actor with ActorLogging with ServiceTrait{
  override def receive: Receive = {
    case WatchFriendInfoList(friendSet, beBlessIdSet) =>
      onWatchFriendInfoList(friendSet, beBlessIdSet)
    case msg : FriendBlessPlayers =>
      onFriendBlessPlayers(msg)
    case _=>

  }

  def onWatchFriendInfoList(friendSet: java.util.Set[java.lang.Long], beBlessIdSet: java.util.Set[java.lang.Long]) ={
    val simplePlayerList : util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(friendSet,areaKey)
    val beBlessPlayerList : util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(beBlessIdSet,areaKey)

    sender() ! WatchFriendInfoListBack(simplePlayerList, beBlessPlayerList)
  }

  def onFriendBlessPlayers(msg : FriendBlessPlayers) ={
    tellService(context, ActorDefine.PLAYER_SERVICE_NAME, msg)
  }


}
