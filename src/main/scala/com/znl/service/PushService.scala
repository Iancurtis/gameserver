package com.znl.service

import java.{lang, util}
import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor, Props}
import com.znl.base.BaseSetDbPojo
import com.znl.core.{SimplePlayer, Notice}
import com.znl.define.{DataDefine, TipDefine, TimerDefine, ActorDefine}
import com.znl.msg.GameMsg._
import com.znl.pojo.db.set.{NoticeDateSetDb, LimitDungeonFastSetDb}
import com.znl.proxy.{ConfigDataProxy, TimerdbProxy}
import com.znl.utils.GameUtils
import org.json.JSONObject
import scala.concurrent.duration._
import scala.collection.JavaConversions._

/**
  * Created by Administrator on 2016/2/23.
  */
object PushService {
  def props(areaKey: String) = Props(classOf[PushService], areaKey)
}


class PushService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {
  val notices: util.List[Notice] = new util.ArrayList[Notice]()
  val simplelist: util.List[SimplePlayer] = new util.ArrayList[SimplePlayer]()

  override def receive: Receive = {
    case OnServerTrigger() =>
      checkPush()
    case addNotice(notice: Notice) =>
      onaddNotice(notice)
    case addNoticelist(notice : util.List[Notice],retype :Int) =>
      onaddNoticBytype(notice ,retype)
    case _ =>
      log.warning("未知消息")
  }
  def onaddNoticBytype(notice : util.List[Notice],retype :Int): Unit ={
    val typelist: util.List[Notice]=new util.ArrayList[Notice]()
    for(nt <- notices){
      if(nt.getType==retype){
        typelist.add(nt)
        BaseSetDbPojo.getSetDbPojo(classOf[NoticeDateSetDb], areaKey).deletNotice(nt)
      }
    }
    notices.removeAll(typelist)
    for(add <- notice){
      onaddNotice(add)
    }
  }

  def checkPush(): Unit = {
    updateSimple()
    val typelist: util.List[Notice]=new util.ArrayList[Notice]()
    for (nt <- notices) {
      val simplePlayer: SimplePlayer = getSimpleById(nt.getPlayerId)
      if (simplePlayer != null) {
        var falg:java.lang.Boolean =false
        if(nt.getType== TipDefine.NOTICE_TYPE_ENERY){
          //设置完成时间
          nt.setEndTime(getEneryEndTime(simplePlayer))
        }
        if (GameUtils.getServerDate().getTime >= nt.getEndTime) {
          val json :JSONObject=ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.REMIND,"type",nt.getType)
           if(nt.getType== TipDefine.NOTICE_TYPE_AUTUBUILD){
             if(isHasAutoLevel(simplePlayer)==false){
               falg=true
             }
           }else{
             falg=true
           }
       /*   if(simplePlayer.online==true){
            falg=false
          }*/
          if(!simplePlayer.getRemianset.contains(nt.getType.toLong)){
            falg=false
          }
          //执行推送并且删除
          if(falg){
            sendPushMsg(nt.getPushId ,"系统信息",json.getString("info"),0)
          }
            BaseSetDbPojo.getSetDbPojo(classOf[NoticeDateSetDb], areaKey).deletNotice(nt)
            typelist.add(nt)
        }
      }
    }
    notices.removeAll(typelist)
  }

  def getEneryEndTime(simplePlayer: SimplePlayer): Long ={
    val nownum=simplePlayer.getEnery
    var need=20-nownum
    if(need<0){
      need==0
    }
    val needtime :Long=TimerDefine.DEFAULT_TIME_RECOVER *need
    return needtime
  }


  def getPlayerId(list: util.List[Notice]): util.Set[java.lang.Long] = {
    val setlist: util.Set[java.lang.Long] = new util.HashSet[java.lang.Long]
    import scala.collection.JavaConversions._
    for (nt <- list) {
      setlist.add(nt.getPlayerId)
    }
    return setlist
  }

  def updateSimple(): Unit = {
    simplelist.clear()
    val newsimplelist: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(getPlayerId(notices),areaKey)
    simplelist.addAll(newsimplelist)
  }

  def getSimpleById(playerId: Long): SimplePlayer = {
    for (simple <- simplelist) {
      if (playerId == simple.getId) {
        return simple
      }
    }
    return null
  }

  def onaddNotice(notice: Notice): Unit = {
    if(notice.getType == TipDefine.NOTICE_TYPE_ENERY){
      //设置完成时间
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(notice.getPlayerId,areaKey)
      val end :Long =getEneryEndTime(simplePlayer)
      if(GameUtils.getServerDate().getTime>=end){
        return
      }
      notice.setEndTime(end)
    }
    BaseSetDbPojo.getSetDbPojo(classOf[NoticeDateSetDb], areaKey).addTeamDate(notice)
    addNewNotice(notice)
  }


  def addNewNotice(notice: Notice): Unit = {
    var delnoti: Notice = null
    for (nt <- notices) {
      if (nt.getPlayerId == notice.getPlayerId && nt.getBeginTime == notice.getBeginTime) {
        delnoti = nt
      }
      if(notice.getType==TipDefine.NOTICE_TYPE_ENERY){
        if(nt.getPlayerId == notice.getPlayerId ){
          delnoti = nt
        }
      }
    }
    notices.remove(delnoti)
    notices.add(notice)
  }

  def isHasAutoLevel(simple: SimplePlayer): java.lang.Boolean = {
    val lasttiem: Long = simple.getAutobuildendtime
    val endTime: Long = GameUtils.getServerDate.getTime - lasttiem
    if (endTime < 0) {
      return true
    }
    return false
  }

  override def preStart() = {
    import context.dispatcher
    context.system.scheduler.schedule(0 millisecond, 1 second, context.self, OnServerTrigger())
    init()
  }

  def init(): Unit = {
    val res: util.List[Notice] = BaseSetDbPojo.getSetDbPojo(classOf[NoticeDateSetDb], areaKey).getAllTeamDatas()
    notices.addAll(res)
  }

  def sendPushMsg(pushChannelId: String, title: String, msg: String, time: Int): Unit ={
    val actor = context.actorSelection("../../"+ActorDefine.PUSH_SERVER_NAME)
    actor!  PushMsgToPlayerDevice(pushChannelId, title, msg, time)
  }

}
