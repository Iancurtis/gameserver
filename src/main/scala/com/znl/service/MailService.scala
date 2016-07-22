package com.znl.service

import java.util

import akka.actor.{Props, ActorLogging, Actor}
import com.znl.base.BaseDbPojo
import com.znl.core.SimplePlayer
import com.znl.define._
import com.znl.log.admin.tbllog_function
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg._
import com.znl.pojo.db.{Report, Mail, Player}
import com.znl.proto.M2
import com.znl.proto.M2.M20400
import com.znl.proxy.DbProxy
import com.znl.template.{ReportTemplate, MailTemplate}
import com.znl.utils.GameUtils
import scala.collection.JavaConversions._

/**
  * Created by Administrator on 2015/10/29.
  */

object MailService {
  def props(areaKey: String) = Props(classOf[MailService], areaKey)
}

class MailService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {

  def starSendMail(ids: java.util.Set[java.lang.Long], mailTemplate: MailTemplate, senderName: String, senderId: Long): Unit = {
    val simplePlayerList: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(ids,areaKey)

    import scala.collection.JavaConversions._
    val rewardStr: StringBuffer = new StringBuffer()
    for (str <- mailTemplate.getRewards) {
      rewardStr.append(str)
      rewardStr.append(",")
    }
    val buffer: StringBuffer = new StringBuffer
    for (attach <- mailTemplate.getAttachments) {
      buffer.append(attach(0))
      buffer.append(",")
      buffer.append(attach(1))
      buffer.append(",")
      buffer.append(attach(2))
      buffer.append("&")
    }
    for (simplePlayer <- simplePlayerList) {
      if (simplePlayer.online) {
        //在线玩家
        val recMess: ReceiveMailNotice = new ReceiveMailNotice(mailTemplate)
        sendMsgToPlayerActor(simplePlayer.getAccountName, recMess)
      } else {
        //离线处理
        val receiver: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
        if (receiver.getShieldMailSet.contains(senderId) == false) {
          //只发不屏蔽的邮件
          val mail: Mail = BaseDbPojo.create(classOf[Mail], areaKey)
          mail.setPlayerId(simplePlayer.getId)
          mail.setContent(mailTemplate.getContext)
          mail.setCreateMailTime(GameUtils.getServerDate().getTime)
          mail.setRewardIdStr(rewardStr.toString)
          mail.setAttachmentStr(buffer.toString)
          mail.setSenderId(senderId)
          mail.setSenderName(senderName)
          mail.setTitle(mailTemplate.getTitle)
          mail.setType(mailTemplate.getType)
          mail.setFriendId(mailTemplate.getFriendId)
          mail.save()
          receiver.addMail(mail.getId)
          receiver.save()
        }
        receiver.finalize()
      }
      //写入行为日志
      val log: tbllog_function = new tbllog_function
      log.setAccount_name(simplePlayer.getAccountName)
      log.setRole_id(simplePlayer.getId)
      log.setPlatform(simplePlayer.getPlatform)
      log.setDim_level(simplePlayer.getLevel)
      log.setAction_id(FunctionIdDefine.GET_NEW_MAIL_FUNCTION_ID)
      log.setStatus(FunctionIdDefine.ACCOMPLISH)
      val now: Int = GameUtils.getServerTime
      log.setLog_time(now)
      log.setHappend_time(now)
      log.setExpand1(senderId)
      var str = mailTemplate.getContext + "("
      if (rewardStr.length() > 0) {
        str += rewardStr.toString
      }
      if (buffer.length() > 0) {
        str += buffer.toString
      }
      str += ")"
      log.setExpandstr(str)
      sendAdminLogToService(context, SendAdminLog(log, ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0))
    }

  }

  def createReport(template: ReportTemplate, battleId: Long, playerId: Long): Report = {
    val report: Report = BaseDbPojo.create(classOf[Report], areaKey)
    report.setAttackerId(template.getAttackId)
    report.setAttackerName(template.getAttackName)
    report.setDefendId(template.getDefendId)
    report.setDefendName(template.getDefendName)
    report.setReportType(template.getReportType)
    report.setMessageId(battleId)
    report.setLevel(template.getLevel)
    report.setDefendLevel(template.getLevel)
    report.setAttackAdviserIcondId(template.getAttackAdviserIcondId)
    report.setAttackAdviserName(template.getAttackAdviserName)
    report.setAttackAdviserSkillId(template.getAttackAdviserSkillId)
    report.setAttackAdviserSkillName(template.getAttackAdviserSkillName)
    report.setAttackExp(template.getAttackExp)
    report.setAttackLegion(template.getAttackLegion)
    report.setAttackSoldierNums(template.getAttackSoldierNums)
    report.setAttackSoldierTypeIds(template.getAttackSoldierTypeIds)
    report.setAttackVip(template.getAttackVip)
    report.setDefendAdviserIcondId(template.getDefendAdviserIcondId)
    report.setDefendAdviserName(template.getDefendAdviserName)
    report.setDefendAdviserSkillId(template.getDefendAdviserSkillId)
    report.setDefendAdviserSkillName(template.getDefendAdviserSkillName)
    report.setDefendExp(template.getDefendExp)
    report.setDefendLegion(template.getDefendLegion)
    report.setDefendSoldierNums(template.getDefendSoldierNums)
    report.setDefendSoldierTypeIds(template.getDefendSoldierTypeIds)
    report.setDefendVip(template.getDefendVip)
    report.setGarrisonName(template.getGarrisonName)
    report.setHonner(template.getHonner)
    report.setName(template.getName)
    report.setAim(template.getAim)
    report.setPosResource(template.getPosResource)
    report.setResourceMapId(template.getResourceMapId)
    report.setTotalResourceNum(template.getTotalResourceNum)
    report.setX(template.getX)
    report.setY(template.getY)
    report.setDefendX(template.getDefendX)
    report.setDefendY(template.getDefendY)
    report.setResourceGet(template.getResourceGet)
    report.setAttackCityIcon(template.getAttackCityIcon)
    report.setAttackAddBoom(template.getAttackAddBoom)
    report.setAttackTotalBoom(template.getAttackTotalBoom)
    report.setAttackCurrBoom(template.getAttackCurrBoom)
    report.setDefentAddBoom(template.getDefentAddBoom)
    report.setDefentCurrBoom(template.getDefentCurrBoom)
    report.setDefentIcon(template.getDefentIcon)
    report.setDefentTotalBoom(template.getDefentTotalBoom)
    report.setReward(template.getReward)
    report.setFirstHand(template.getFirstHand)
    report.setCreateTime(GameUtils.getServerDate().getTime)
    //根据玩家选出结果
    report.setPlayerId(playerId)
    report.setGarrisonId(template.getGarrisonid)
    if (template.getReportType != ReportDefine.REPORT_TYPE_SPY && template.getReportType != ReportDefine.REPORT_TYPE_ARENA) {
      if (playerId == template.getAttackId) {
        report.setResult(template.getResult)
      } else {
        if (template.getResult == 0) {
          report.setResult(1)
        } else {
          report.setResult(0)
        }
      }
    }else if( template.getReportType == ReportDefine.REPORT_TYPE_ARENA){
      report.setResult(template.getResult)
    }
    report.save()
    report
  }

  def sendArenaReportToPlayer(report: Report): Unit = {
    try {
      val ids: java.util.Set[java.lang.Long] = new java.util.HashSet[java.lang.Long]()
      ids.add(report.getPlayerId)
      val simplePlayerList: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(ids,areaKey)
      import scala.collection.JavaConversions._
      for (simplePlayer <- simplePlayerList) {
        if(simplePlayer.getId<=0){
          return
        }
        if (simplePlayer.online) {
          //在线玩家
          sendMsgToPlayerActor(simplePlayer.getAccountName, GameMsg.ReceiveArenaReportNotice(report))
        } else {
          val receiver: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
          receiver.addReport(report.getId)
          receiver.save()
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }

  def sendReportToPlayer(report: Report): Unit = {
    try {
      val ids: java.util.Set[java.lang.Long] = new java.util.HashSet[java.lang.Long]()
      ids.add(report.getPlayerId)
      val simplePlayerList: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(ids,areaKey)
      import scala.collection.JavaConversions._
      for (simplePlayer <- simplePlayerList) {
        if (simplePlayer.online) {
          //在线玩家
          sendMsgToPlayerActor(simplePlayer.getAccountName, GameMsg.ReceiveReportNotice(report))
        } else {
          val receiver: Player = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
          receiver.addReport(report.getId)
          val mail: Mail = BaseDbPojo.create(classOf[Mail], areaKey)
          mail.setType(ChatAndMailDefine.MAIL_TYPE_REPORT)
          mail.setReportId(report.getId)
          mail.setCreateMailTime(GameUtils.getServerDate().getTime)
          mail.save()
          receiver.addMail(mail.getId)
          receiver.save()
        }
      }
    } catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }

  def starSendReport(template: ReportTemplate, battleId: Long): Unit = {
    if (template.getReportType == ReportDefine.REPORT_TYPE_SPY) {
      val report = createReport(template, battleId, template.getAttackId)
      sendReportToPlayer(report)
    } else if (template.getReportType == ReportDefine.REPORT_TYPE_ARENA) {
      val report1 = createReport(template, battleId, template.getAttackId)
      sendArenaReportToPlayer(report1)
      val report2 = createReport(template, battleId, template.getDefendId)
      sendArenaReportToPlayer(report2)
      tellService(context, ActorDefine.ARENA_SERVICE_NAME, AddServerArenaReport(report1))
      val attacksimple:SimplePlayer=PlayerService.getSimplePlayer(template.getAttackId,areaKey)
      val defendsimple:SimplePlayer=PlayerService.getSimplePlayer(template.getDefendId,areaKey)
      if(attacksimple!=null&&attacksimple.online){
        sendMsgPlayerModule(context,attacksimple.getAccountName,ActorDefine.ARENA_MODULE_NAME,addNewReport(report1.getId))
      }
      if(defendsimple!=null&&defendsimple.online){
        sendMsgPlayerModule(context,defendsimple.getAccountName,ActorDefine.ARENA_MODULE_NAME,addNewReport(report2.getId))
      }
    } else {
      //      if (template.getFirstHand == 0){
      //        template.setFirstHand(1)
      //      }else{
      //        template.setFirstHand(0)
      //      }
      val report1 = createReport(template, battleId, template.getAttackId)
      sendReportToPlayer(report1)
      if (template.getDefendId > 0) {
        template.setHonner(-template.getHonner)
        template.setReportType(ReportDefine.REPORT_TYPE_BE_ATTACK)
        val report2 = createReport(template, battleId, template.getDefendId)
        report2.setX(template.getAttackX)
        report2.setY(template.getAttackY)
        report2.setDefendX(template.getDefendX)
        report2.setDefendY(template.getDefendY)
        report2.setName(template.getName)
        report2.setAim(template.getAttackName)
        report2.setLevel(template.getAttackLevel)
        if (report2.getPosResource != null) {
          var index = 0
          val posResList = new util.ArrayList[Integer](report2.getPosResource)
          while (report2.getPosResource.size() > index) {
            val res: Integer = -posResList.get(index)
            posResList.set(index, res)
            index = index + 1
          }
          report2.setPosResource(posResList)
        }
        sendReportToPlayer(report2)
      }
      if(template.getGarrisonid>0){
        template.setReportType(ReportDefine.REPORT_TYPE_BE_ATTACK)
        val report2 = createReport(template, battleId, template.getGarrisonid)
        report2.setX(template.getAttackX)
        report2.setY(template.getAttackY)
        report2.setDefendX(template.getDefendX)
        report2.setDefendY(template.getDefendY)
        report2.setName(template.getName)
        report2.setAim(template.getAttackName)
        report2.setLevel(template.getAttackLevel)
        if (report2.getPosResource != null) {
          var index = 0
          val posResList = new util.ArrayList[Integer](report2.getPosResource)
          while (report2.getPosResource.size() > index) {
            val res: Integer = -posResList.get(index)
            posResList.set(index, res)
            index = index + 1
          }
          report2.setPosResource(posResList)
        }
        sendReportToPlayer(report2)
      }
    }
  }

  override def receive: Receive = {
    case GameMsg.SendMail(players: java.util.Set[java.lang.Long], mailTemplate: MailTemplate, senderName: String, senderId: Long) =>
      starSendMail(players, mailTemplate, senderName, senderId);
      sendLaterPlayer(players,mailTemplate)
    case SendReport(reportTemplate: ReportTemplate, battleId: Long) =>
      starSendReport(reportTemplate, battleId)
    case getlaterInfo(players : util.List[java.lang.Long],build : M20400.S2C.Builder) =>
      ongetlaterInfo(players,build)
    case _ =>
  }

  override def preStart() = {

  }


  def ongetlaterInfo(players : util.List[java.lang.Long],build : M20400.S2C.Builder): Unit ={
    val simplePlayerList: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfos(players,areaKey)
    for(simple <- simplePlayerList){
      val info : M2.laterinfo.Builder = M2.laterinfo.newBuilder()
      info.setIconId(simple.getIconId)
      info.setName(simple.getName)
      info.setPlayerId(simple.getId.toLong)
      build.addInfos(info)
    }
    sender() ! getlaterInfoback(build)
  }

  //发送消息给具体的player邮件模块
  def sendMsgToPlayerActor(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.MAIL_MODULE_NAME)
    actor ! msg
  }


  //产生最近
  def sendLaterPlayer(players: java.util.Set[java.lang.Long], mailTemplate: MailTemplate): Unit = {
    if (mailTemplate.getSenderId > 0) {
      val sendsimple: SimplePlayer = PlayerService.getSimplePlayer(mailTemplate.getSenderId,areaKey)
      for (playerid <- players) {
        val simple: SimplePlayer = PlayerService.getSimplePlayer(playerid,areaKey)
        if (simple.online) {
          sendMsgToPlayerActor(simple.getAccountName, AddlaterPlayer(mailTemplate.getSenderId))
        } else {
          val player: Player = BaseDbPojo.getOfflineDbPojo(playerid, classOf[Player],areaKey)
          var str:String=player.getLaterPeople
          var newstr:String=""
          var num:Int=0
          var strarr : Array[String]=str.split("_");
          for(temp <- strarr){
            if(!temp.equals("")){
              if(!temp.equals(mailTemplate.getSenderId+"")&& ! temp.equals(player.getId+"") && num<ChatAndMailDefine.LATERMAX-1){
                   newstr =newstr+"_"+temp
                   num=num+1
              }
            }
          }
          newstr=mailTemplate.getSenderId+"_"+newstr
          player.setLaterPeople(newstr)
          player.save()
        }
        if(sendsimple.online){
          sendMsgToPlayerActor(sendsimple.getAccountName, AddlaterPlayer(playerid))
        }else{
          val player: Player = BaseDbPojo.getOfflineDbPojo(playerid, classOf[Player],areaKey)
          var str:String=player.getLaterPeople
          var newstr:String=""
          var num:Int=0
          var strarr : Array[String]=str.split("_");
          for(temp <- strarr){
            if(!temp.equals("")){
              if(!temp.equals(playerid+"")&& ! temp.equals(player.getId+"") && num<ChatAndMailDefine.LATERMAX-1){
                newstr =newstr+"_"+temp
              }
            }
          }
          newstr=playerid+"_"+newstr
          player.setLaterPeople(newstr)
          player.save()
        }
      }
    }
  }


}
