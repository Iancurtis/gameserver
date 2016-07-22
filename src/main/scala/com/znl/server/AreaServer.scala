package com.znl.server

import java.util.Properties

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import com.znl.define.ActorDefine
import com.znl.msg.GameMsg._
import com.znl.service._
import com.znl.utils.GameUtils

/**
 * 逻辑区服务器 ID对应各种对应的区服
 * 用来处理各区的玩家
 * Created by woko on 2015/10/7.
 */

object AreaServer {
  def props(logicAreaId : Int, areaKey : String, p: Properties) = Props(classOf[AreaServer], logicAreaId, areaKey, p)
}

class AreaServer(logicAreaId : Int, areaKey : String, p: Properties) extends Actor with ActorLogging{

  val playerService = context.actorOf(PlayerService.props(areaKey), ActorDefine.PLAYER_SERVICE_NAME)
  context.watch(playerService)

  val worldService = context.actorOf(WorldService.props(areaKey), ActorDefine.WORLD_SERVICE_NAME)
  context.watch(worldService)

  val triggerService = context.actorOf(Props[TriggerService], ActorDefine.TRIGGER_SERVICE_NAME)
  context.watch(triggerService)

  val chatService = context.actorOf(Props[ChatService], ActorDefine.CHAT_SERVICE_NAME)
  context.watch(chatService)

  val mailService = context.actorOf(MailService.props(areaKey), ActorDefine.MAIL_SERVICE_NAME)
  context.watch(mailService)

  val logService = context.actorOf(LogService.
    props(p, logicAreaId,areaKey), ActorDefine.ADMIN_LOG_SERVICE_NAME)
  context.watch(logService)

  val friendService = context.actorOf(FriendService.props(areaKey), ActorDefine.FRIEND_SERVICE_NAME)
  context.watch(friendService)

  val arenaService = context.actorOf(ArenaService.props(areaKey), ActorDefine.ARENA_SERVICE_NAME)
  context.watch(arenaService)


  val armyGroupService = context.actorOf(ArmyGroupService.props(areaKey), ActorDefine.ARMYGROUP_SERVICE_NAME)
  context.watch(armyGroupService)

  val battleReportService = context.actorOf(Props[BattleReportService], ActorDefine.BATTLE_REPORT_SERVICE_NAME)
  context.watch(battleReportService)

  val chargeService = context.actorOf(ChargeService.props(areaKey), ActorDefine.CHARGE_SERVICE_NAME)
  context.watch(chargeService)

  val powerRanksService = context.actorOf(PowerRanksService.props(areaKey),ActorDefine.POWERRANKS_SERVICE_NAME)
  context.watch(powerRanksService)

  val pushService = context.actorOf(PushService.props(areaKey),ActorDefine.PUSH_SERVICE_NAME)
  context.watch(pushService)

  //确保下面的service不会restart
  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      log.error(e.fillInStackTrace(), e.getMessage)
      Resume
    }
    case _ => Resume
  }

  override def preStart() = {
    log.info("area server start")
  }

  override def receive: Receive = {
    case createPlayerActor : CreatePlayerActor =>
      playerService ! createPlayerActor
    case sendPlayerNetMsg : SendPlayerNetMsg =>
      if (GameUtils.openServer == true){
        playerService ! sendPlayerNetMsg
      }
    case stopPlayerActor : StopPlayerActor =>
      playerService ! stopPlayerActor
    case replacePlayerSession : ReplacePlayerSession =>
      playerService ! replacePlayerSession
    case msg : AutoSaveOffPlayerData =>
      playerService ! msg
   /* case msg:FixedTimeNotice=>
       //定时器
      triggerService! msg*/
    case _ =>
  }

}
