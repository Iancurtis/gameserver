package com.znl.service

import java.util
import java.util.{Date, Calendar}

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props}
import com.znl.GameMainServer
import com.znl.base.{BaseLog, BaseSetDbPojo, BaseDbPojo}
import com.znl.core.{PowerRanks, SimplePlayer}
import com.znl.define._
import com.znl.log.CustomerLogger
import com.znl.log.admin.tbllog_activityrank
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg._
import com.znl.pojo.db.{Player, ArmygroupMenber, Armygroup}
import com.znl.pojo.db.set.ArmGroupSetDb
import com.znl.proxy.{PlayerProxy, ActivityProxy, ConfigDataProxy, DbProxy}
import com.znl.service.functionNode.ArmygroupNode
import com.znl.service.trigger.{TriggerType, TriggerEvent}
import com.znl.template.ReportTemplate
import com.znl.utils.{GameUtils, SortUtil}
import org.json.JSONObject

import scala.collection.JavaConversions._
import scala.concurrent.duration._


object ArmyGroupService {
  def props(areaKey: String) = {
    Props(classOf[ArmyGroupService], areaKey)
  }
  val armymap: util.Map[java.lang.Long, Armygroup] = new util.concurrent.ConcurrentHashMap[java.lang.Long, Armygroup]
}


class ArmyGroupService(areaKey: String) extends Actor with ActorLogging with ServiceTrait {
  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception => {
      e.printStackTrace()
      CustomerLogger.error("ArmyGroupService出现异常",e)
      //      GameUtils.stackTrackLog(log, e.getStackTrace)
      Resume
    }
    case _ => Resume
  }

  override def preStart() = {

//    BaseSetDbPojo.getSetDbPojo(classOf[ArmGroupSetDb], areaKey)

    import context.dispatcher
    context.system.scheduler.schedule(0 milliseconds, 10 seconds, context.self, OnServerTrigger())

    val armGroupSetDb = BaseSetDbPojo.getSetDbPojo(classOf[ArmGroupSetDb], areaKey)
      val ids: util.List[java.lang.Long] =  armGroupSetDb.getAllValue //DbProxy.ask(GetAllArmygroupids(areaKey))
     initArmyMap(ids)
    getActivityTimer()
    updateEachHourNotice()
  }


  def updateEachHourNotice() = {
    val event = new TriggerEvent(self, EachHourNotice(), TriggerType.WHOLE_HOUR, 0)
    getTriggerService(context) ! AddTriggerEvent(event)
  }

  override def receive: Receive = {
    case AddArmygroupid(id: Long, arenKey: String) =>
      onAddArmygroupid(id, arenKey)
    case RemoveArmygroupid(id: Long, arenKey: String) =>
      onRemoveArmygroupid(id, arenKey)
    case createArmyArmyGroup(name: String, joinType: Int, way: Int) =>
      oncreateArmyArmyGroup(name, joinType, way)
    case AddArmyGroup(armytemp: Armygroup) =>
      onAddArmyGroup(armytemp) //创建
    case getArmyGroupByid(appId: Long, cmd: Int) =>
      onArmyGroupByid(appId, cmd)
    case getAllArmyGroup(obj : Object,cmd : Int) =>
    ongetAllArmyGroup(obj,cmd)
    case OnServerTrigger() =>
      refreshRank()
    case saveDateBeforeStop() =>
      saveDate()
    case AddMailBattleProto(reportTemplate : ReportTemplate,cmd : String) =>
      onAddMailBattleProto(reportTemplate,cmd)
    case getAllLegion(name:String,typeId:Int) =>
      sender() ! getAllLegionSucess(ArmyGroupService.armymap,name,typeId)
    case ActivityRankTrigger() =>
      checkTimer()
    case checkArmy(id : Long) =>
      oncheckArmy(id)
   /* case CheckAndSendActivity(ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK,areakey:String) =>
      onCheckAndSendActivity(CheckAndSendActivity(ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK,areakey))*/
    case EachHourNotice() =>
      context.children.foreach(f=>{
        f!EachHourNotice()
      })
    case _ =>
      log.warning("未知消息")

  }

  def onAddMailBattleProto(reportTemplate : ReportTemplate,cmd : String): Unit ={
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(reportTemplate.defendId,areaKey)
    reportTemplate.defendVip=simplePlayer.getVipLevel
    if(ArmyGroupService.armymap.get(reportTemplate.getAttackLegion.toLong)==null){
      reportTemplate.setAttackLegion("")
    }else{
      val mylegon : Armygroup=ArmyGroupService.armymap.get(reportTemplate.getAttackLegion.toLong)
      reportTemplate.setAttackLegion(mylegon.getName)
    }
    if(ArmyGroupService.armymap.get(simplePlayer.getArmygrouid)==null){
      reportTemplate.setDefendLegion("")
    }else{
      val mylegon : Armygroup=ArmyGroupService.armymap.get(simplePlayer.getArmygrouid)
      reportTemplate.setDefendLegion(mylegon.getName)
    }
    tellService(ActorDefine.BATTLE_REPORT_SERVICE_NAME, AddMailBattleProto(reportTemplate, "200001"))
  }

  def saveDate(): Unit ={
    for(arm<- ArmyGroupService.armymap.values()){
      context.actorSelection(ActorDefine.ARMYGROUPNODE + arm.getId) ! GameMsg.saveDateBeforeStop()
    }
  }

  def ongetAllArmyGroup(obj : Object,cmd : Int): Unit ={
    sender() ! getAllArmyGroupSucess(obj,ArmyGroupService.armymap,cmd)
  }

  //发送消息给具体的player模块
  def sendMsgToPlayerArmyModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.ARMYGROUP_MODULE_NAME)
    actor ! msg
  }


  def onArmyGroupByid(appId: Long, cmd: Int): Unit = {
    val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(ArmyGroupService.armymap.get(appId).getCommandid,areaKey)
    sender() ! getArmyGroupByidSucess(ArmyGroupService.armymap.get(appId),simplePlayer.getIconId,simplePlayer.getPendant, cmd)
  }



  def onAddArmyGroup(armytemp: Armygroup): Unit = {
    ArmyGroupService.armymap.put(armytemp.getId, armytemp)
    DbProxy.tell(AddArmygroupid(armytemp.getId, areaKey), self)
    creatArmNode(armytemp)
    refreshRank()
    refreshLevelRank()
    onAddArmygroupid(armytemp.getId ,areaKey)
  }

  def creatArmNode(armygroup : Armygroup) = {
    val actor = context.actorOf(ArmygroupNode.props(armygroup, areaKey), ActorDefine.ARMYGROUPNODE+armygroup.getId + "")
    context.watch(actor)
  }


  def oncreateArmyArmyGroup(name: String, joinType: Int, way: Int): Unit = {
    sender() ! createArmyArmyGroupSucess(name: String, joinType: Int, way: Int, ArmyGroupService.armymap)
  }

  def onAddArmygroupid(id: Long, arenKey: String): Unit = {
    BaseSetDbPojo.getSetDbPojo(classOf[ArmGroupSetDb], areaKey).addKeyValue(id.toString, id)
//    DbProxy.tell(AddArmygroupid(id, arenKey), self)
  }


  def onRemoveArmygroupid(id: Long, arenKey: String): Unit = {
//    DbProxy.tell(RemoveArmygroupid(id, arenKey), self)
    BaseSetDbPojo.getSetDbPojo(classOf[ArmGroupSetDb], areaKey).removeKey(id.toString)
  }

  def initArmyMap(ids: util.List[java.lang.Long]) {
    import scala.collection.JavaConversions._
    for (id <- ids) {
      val armygroup : Armygroup = BaseDbPojo.getOfflineDbPojo(id, classOf[Armygroup],areaKey)
      if(armygroup != null&&armygroup.getArmmenbers.size()!=0) {
        ArmyGroupService.armymap.put(id, armygroup)
        creatArmNode(armygroup)
      }else{
        if(armygroup != null){
          armygroup.del()
        }
      }
    }

  }

  def oncheckArmy(id : Long): Unit ={
    if(!ArmyGroupService.armymap.containsKey(id)){
      sender() ! checkeNoneId()
    }
  }

  def refreshRank(): Unit ={
    val list: util.List[Armygroup] = new util.ArrayList[Armygroup]
    for (army <- ArmyGroupService.armymap.values) {
      list.add(army)
    }
    var rank=1
    SortUtil.anyProperSort(list, "getCapity", false)
    for(armygroup <- list){
      armygroup.setRank(rank)
      rank=rank+1
    }

  }

  def refreshLevelRank(): Unit ={
    val list: util.List[Armygroup] = new util.ArrayList[Armygroup]
    for (army <- ArmyGroupService.armymap.values) {
      list.add(army)
    }
    var rank=1
    SortUtil.anyProperSort(list, "getLevel", false)
    for(armygroup <- list){
      armygroup.setLevelrank(rank)
      rank=rank+1
    }

  }
  //通知到service
  def tellService(serviceName : String, msg : AnyRef) ={
    context.actorSelection("../" + serviceName) ! msg
  }



 def checkTimer() {
    val list: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_DESIGN, "uitype", ActivityDefine.POWER_RANK_UITYPE)
    val c: Calendar = Calendar.getInstance
    c.setTime(GameUtils.getServerDate)
    val serverYear: Int = c.get(Calendar.YEAR)
    val serverMonth: Int = c.get(Calendar.MONTH)
    val serverDay: Int = c.get(Calendar.DAY_OF_MONTH)
    val serverHour: Int = c.get(Calendar.HOUR_OF_DAY)
    val serverMinute: Int = c.get(Calendar.MINUTE)
    val serverSecond: Int = c.get(Calendar.SECOND)
    for (define <- list) {
      val endTime: Long = getActivityEndTime(define)
      val endCalender: Calendar = Calendar.getInstance
      endCalender.setTimeInMillis(endTime)
      if (endCalender.get(Calendar.YEAR) == serverYear
        && endCalender.get(Calendar.MONTH) == serverMonth
        && endCalender.get(Calendar.DAY_OF_MONTH) == serverDay
        && endCalender.get(Calendar.HOUR_OF_DAY) == serverHour
        && endCalender.get(Calendar.MINUTE) == serverMinute
        && endCalender.get(Calendar.SECOND) == serverSecond) {
        //年月日时分秒都一致就发吧
        val effectId = define.getInt("effectID")
        val effectDefineList: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT, "effectID", effectId)
        if (effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK) {
          onRefLevelRank()
        }
      }
    }
    getActivityTimer()
  }


  def getActivityTimer(): Unit = {
    val list: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_DESIGN, "uitype", ActivityDefine.POWER_RANK_UITYPE)
    val now = GameUtils.getServerDate().getTime
    var end = 0l
    for (define <- list) {
      val timeType = define.getInt("timetype")
      if (timeType == 2) {
        val endTime = getActivityEndTime(define)
        if (endTime > 0 && endTime > now) {
          if (end == 0 || end > endTime) {
            end = endTime
          }
        }
      }
    }
    if (end > 0) {
      val event = new TriggerEvent(self, ActivityRankTrigger(), TriggerType.COUNT_DOWN, (end / 1000 - now / 1000).toInt)
      getTriggerService(context) ! AddTriggerEvent(event)
    }
  }

  private def getActivityEndTime(define: JSONObject): Long = {
    val timeType: Int = define.getInt("timetype")
    var endTime: Long = -1
    timeType match {
      case 1 => {
        CustomerLogger.error("排行榜居然出现时间类型为1的定时器！！！")
      }
      case 2 => {
        val openServerDate: Date = GameMainServer.getOpenServerDateByAreaKey(areaKey)
        val calendar: Calendar = Calendar.getInstance
        calendar.setTime(openServerDate)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, define.getInt("timeA"))
        val timeB: Int = define.getInt("timeB")
        val hour: Int = timeB / 10000
        val minute: Int = timeB / 100 % 100
        val seconds: Int = timeB % 100
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, seconds)
        val endDate: Date = calendar.getTime
        endTime = endDate.getTime
      }
      case 3 => {
        CustomerLogger.error("排行榜居然出现时间类型为3的定时器！！！")
      }
      case 4 => {
        val endDate: Date = GameUtils.getDateFromStryyyyMMdd(define.getInt("timeB") + "")
        endTime = endDate.getTime
        CustomerLogger.error("排行榜居然出现时间类型为4的定时器！！！")
      }
    }
    return endTime
  }


  def onRefLevelRank(): Unit ={
    refreshLevelRank()
    val list: util.List[Armygroup] = new util.ArrayList[Armygroup]
    for (army <- ArmyGroupService.armymap.values) {
      list.add(army)
    }
    for(armygroup <- list){
      for (player <- armygroup.getMenbers) {
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(player, areaKey)
        if (simplePlayer != null) {
          sendRankLog(simplePlayer.getId, armygroup.getLevelrank, ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK)
          if (simplePlayer.online == true) {
            sendMsgToRoleModule(simplePlayer.getAccountName, addAtivity(ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK, armygroup.getLevelrank, 0))
          } else {
            val newplayer = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areaKey)
            val activityProxy: ActivityProxy = new ActivityProxy(newplayer.getActivitySet, areaKey)
            val playerProxy: PlayerProxy = new PlayerProxy(newplayer, areaKey)
            newplayer.save()
            activityProxy.reloadDefineData(playerProxy)
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_TYPE_LEVEL_RANK, armygroup.getLevelrank, playerProxy, 0)
            activityProxy.saveActivity()
          }
        }
      }


    }

  }
  def sendMsgToRoleModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.ROLE_MODULE_NAME)
    actor ! msg
  }

  def sendRankLog(playerId: Long, rank: Int, ranktype: Int): Unit = {
    val log: tbllog_activityrank = new tbllog_activityrank(playerId, rank, ranktype)
    log.setLog_time(GameUtils.getServerTime())
    sendLog(log)
  }

  def sendLog(log: BaseLog) = {
    tellService(ActorDefine.ADMIN_LOG_SERVICE_NAME, SendAdminLog(log, ActorDefine.ADMIN_LOG_ACTION_INSERT, "", 0))
  }


  /**
   * 增加有福同享 包箱记录
   */
  def addLegionShareActivityRecord(armyId:Long,message:String)={

  }
}


