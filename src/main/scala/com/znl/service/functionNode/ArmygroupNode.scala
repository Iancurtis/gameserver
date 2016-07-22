package com.znl.service.functionNode

import java.util.concurrent.ConcurrentHashMap
import java.util.{Calendar, Date}
import java.{util, lang}

import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import com.znl.GameMainServer
import com.znl.base.{BaseLog, BaseSetDbPojo, BaseDbPojo}
import com.znl.core._
import com.znl.define._
import com.znl.log.CustomerLogger
import com.znl.log.admin.tbllog_activityrank
import com.znl.msg.GameMsg
import com.znl.msg.GameMsg._
import com.znl.pojo.db._
import com.znl.pojo.db.set.{LegionDungeoTeamSetDb, SituationDateSetDb, TeamDateSetDb}
import com.znl.proto.{Common, M22}
import com.znl.proto.M22.{LegionCustomJobShortInfo, LegionMemberInfo}
import com.znl.proxy._
import com.znl.service.PlayerService
import com.znl.service.trigger.{TriggerType, TriggerEvent}
import com.znl.utils.{DateUtil, GameUtils, RandomUtil, SortUtil}
import org.json.{JSONArray, JSONObject}

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.language.postfixOps

object ArmygroupNode {
  def props(armygroup: Armygroup, areakey: String) = Props(classOf[ArmygroupNode], armygroup, areakey)
  //val ArmygroupDungeoMap: util.Map[Integer,Armygroup] = new util.HashMap[Integer,Armygroup]()
}

class ArmygroupNode(armygroup: Armygroup, areakey: String) extends Actor with ActorLogging {
  val menbers: util.List[ArmygroupMenber] = new util.ArrayList[ArmygroupMenber]()
  val techsMap: util.Map[Integer, ArmyGroupTech] = new util.HashMap[Integer, ArmyGroupTech]()
  val situationmap: util.Map[Integer, util.List[Situation]] = new util.HashMap[Integer, util.List[Situation]]()
  val simples: util.List[SimplePlayer] = new util.ArrayList[SimplePlayer]()
  var ArmygroupDungeoMax:  Integer= 0
  var Dungeolist: util.List[Integer] = new util.ArrayList[Integer]()
  var armygroupdungeoinfo:util.Map[Integer, util.List[PlayerTeam]] = new util.HashMap[Integer, util.List[PlayerTeam]]()
  var attackDungeolist: util.List[Integer] = new util.ArrayList[Integer]()

/*
//以下为军团副本测试数据
  attackDungeolist.clear();

  Dungeolist.add(1)
  Dungeolist.add(2)
  Dungeolist.add(9)
  Dungeolist.add(4)
  Dungeolist.add(7)
  Dungeolist.add(15)
  Dungeolist.add(20)

  val dungeoDb:LegionDungeoTeamSetDb =new LegionDungeoTeamSetDb()
  val playertroop:PlayerTroop = new PlayerTroop()
  val  dungeoproxy:DungeoProxy = new DungeoProxy()
  val son: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LegionEvent)
  for(json <- son){
    armygroupdungeoinfo.put(json.getInt("ID"),dungeoproxy.createArmyGroupDungeoMonsterList(json.getInt("ID")))
  }

  armygroup.setLegionDungeoidbox(Dungeolist)

  ArmygroupDungeoMax = 20
  armygroup.setMaxLegionDungeoid(ArmygroupDungeoMax)*/



  import scala.collection.JavaConversions._

  override def receive: Receive = {
    case Addmenber(menber: ArmygroupMenber) =>
      if (!menbers.contains(menber)) {
        menbers.add(menber)
      }
    case removeArmyGroup(menber: ArmygroupMenber) =>
      if (!menbers.contains(menber)) {
        menber.del()
        menbers.remove(menber)
      }
    case changeMenberCapity(playerId: Long, capity: Long) =>
      onchangeMenberCapity(playerId, capity)
    case changeMenberLevel(playerId: Long, level: Int) =>
      onchangeMenberLevel(playerId, level)
    case changeMenberJob(playerId: Long, job: Int) =>
      onchangeMenberJob(playerId, job)
    case changeMenberlogintime(playerId: Long) =>
      onchangeMenberlogintime(playerId)
    case changeMenberlogOuttime(playerId: Long) =>
      onchangeMenberlogOuttime(playerId)
    case applyArmyJoin(idlist: util.Set[java.lang.Long], applytype: Int, level: Int, capity: Long, playerId: Long) =>
      onapplyArmyJoin(idlist, applytype, level, capity, playerId)
    case removeApplyid(playerId: Long) =>
      onremoveApplyid(playerId)
    case opeRateArmy(otherId: Long, myId: Long, retype: Int) =>
      onopeRateArmy(otherId, myId, retype)
    case clearApplylist(myId: Long) =>
      onclearApplylist(myId)
    case editArmyGroup(myId: Long, joinType: Int, list: util.List[Integer], level: Int, capity: Long, content: String) =>
      oneditArmyGroup(myId, joinType, list, level, capity, content)
    case editJobName(myId: Long, list: util.List[M22.LegionCustomJobShortInfo]) =>
      oneditJobName(myId, list)
    case setorUpJob(myId: Long, retype: Int, otherId: Long, job: Int) =>
      onsetorUpJob(myId, retype, otherId, job)
    case agreeApply(myId: Long, otherId: Long, retype: Int) =>
      agreeApply(myId, otherId, retype)
    case getMyGroupInfos() =>
      sender() ! getMyGroupInfosback(armygroup,menbers)
    case lookAppList() =>
      dolookAppList()
    case TechExpandPowerMap() =>
      onGetTechExpandPowerMap()
    case EachHourNotice() =>
      checkTimer
    case GetWelfareRes(resMap: util.Map[java.lang.Integer, java.lang.Integer]) =>
      getFiveRes(resMap)
    case MtSTechUpReq(playerId: Long, opt: Int) =>
      onArmyGroupTechUp(playerId, opt)
    case MtSTechContributeReq(techId: Int, power: Int, playerId: Long, cmd: Int, isFirst: Int, alltime: Int) =>
      legionTechGoldDonate(techId, power, playerId, cmd, isFirst, alltime)
    case MtSHallContribute(power: Int) =>
      sender() ! MtSHallContributeReqSucess(armygroup, power)
    case MtSHallContributeReq(playerId: Long, power: Int, num: Int) =>
      legionHallGoldDonate(playerId, power, num)
    case MtSHallUpReq(playerId: Long, cmd: Int) =>
      legionHallUp(playerId, cmd)
    case StMHallUpSucesstonode() =>
      armyLeveUp()
    case saveDateBeforeStop() =>
      savaDate()
    case MtSwelfareUpReq(playerId: Long, cmd: Int, typeId: Int) =>
      welfareUpNeed(playerId, cmd, typeId)
    case MtSGetwelfareReq(playerId: Long, cmd: Int, canGetId: Int, typeId: Int) =>
      getWelfare(playerId, cmd, canGetId, typeId)
    case MtSwelfareReq(playerId: Long, typeId: Int) =>
      welfareReqNeed(playerId, typeId)
    case MtSwelfareGetRes(playerId: Long) =>
      welfareCanGetFiveRes(playerId)
    case requestSlMwelfareInfo(playerId: Long) =>
      sender() ! requestSlMwelfareInfoBack(walfareInfo(playerId), menbers)
    case ArmyGroupShop(playerId: Long, itemId: Int, opt: Int, typeId: Int) =>
      onArmyGroupShop(playerId, itemId, opt, typeId)
    case test(retype: Int) =>
      test(retype)
      refreshShop()
    case changeLegionName(name: String) =>
      armygroup.setName(name)
    case changeMenberName(name: String, playerId: Long) =>
      onchangeMenberName(name, playerId)
    case editAffeche(cont: String, playerId: Long) =>
      editAffeche(playerId, cont)
    case editLegionFinish(retype: Int, playerId: Long) =>
      addMessionActivity(retype, playerId)
    case getLegionLevelInfo() =>
      sender() ! getLegionLevelInfoback(armygroup)
    case addShareValue(playerId: Long, value: Int) =>
      addShareValue(playerId, value)
    case addSituation(situation: Situation) =>
      onaddSituation(situation)
    case getSituationInfo(build: M22.M220300.S2C.Builder) =>
      getM220300Info(build)
    case changetIconPend(playerId: Long, icon: Int, pendIcon: Int) =>
      onchangetIconPend(playerId, icon, pendIcon)
    case applistNum() =>
      onapplistNum()
    case ActivityRankTrigger() =>
      checkACTimer()
    case legionenlist(playerId: Long) =>
      onlegionenlist(playerId)
    case addLegionShareRecord(playerId :Long,chargeId:Int,createTime:Int,sharedPlayerName:String) =>
      doAddLegionShareRecord(playerId,chargeId,createTime,sharedPlayerName)
    /*case getArmyGroupDungeoInfo(dungeoid:Int) =>
      ongetArmyGroupDungeoInfo(dungeoid)
    case iscanattackArmyGroupDungeo(dungeoid:Int) =>
      oniscanattackArmyGroupDungeo(dungeoid)
    case addPuppetList(acname:String,battleType: Int, eventId: Int, cmd: Int, team: util.List[PlayerTeam], saveTraffic: Int) =>
      onaddungeolist(acname,battleType,eventId,cmd,team,saveTraffic)
    case changeArmyGroupDungeoInfo(sort:Integer) =>
      onchangeArmyGroupDungeoInfo(sort)
    case allarmygroupdungeoinfo()  =>
      onallarmygroupdungeoinfo()
    case changeGroupDungeomonsterInfo(dungeoId:Int,monsterlist:util.List[PlayerTeam]) =>
      onchangeGroupDungeoInfo(dungeoId,monsterlist)*/
    case _ =>

  }
  def checkACTimer() {
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
        if (effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK) {
          refreshACdevoteRank()
        }
        if(effectDefineList.get(0).getInt("conditiontype") == ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION){
          checkRefreshLegionRank()
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
        val openServerDate: Date = GameMainServer.getOpenServerDateByAreaKey(areakey)
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


  def onlegionenlist(playerId: Long): Unit = {
    val men: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    var rs: Int = 0
    if (men.getJob != ArmyGroupDefine.JOB_MANGER) {
      rs = ErrorCodeDefine.M220400_1
    }
    if (GameUtils.getServerDate().getTime < armygroup.getNextenlistiem) {
      rs = ErrorCodeDefine.M220400_2
    }
    if (rs == 0) {
      //设置下次招募的时间
      armygroup.setNextenlistiem(GameUtils.getServerDate().getTime + ArmyGroupDefine.ENDLIST_TIME)
    }
    sendMsgToPlayerModule(men.getAccoutName, GameMsg.legionenlistback(rs), ActorDefine.SHARE_MODULE_NAME)
  }

  def onapplistNum(): Unit = {
    sender() ! applistNumback(armygroup.getApplymenbers.size())
  }


  def sendapplistNum(): Unit = {
    for (men <- menbers) {
      if (men.getJob >= ArmyGroupDefine.JOB_MIN_MANGER) {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(men.getPlayerId, areakey)
        if (sp.online) {
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.applistNumback(armygroup.getApplymenbers.size()))
        }
      }
    }
  }


  def onchangetIconPend(playerId: Long, icon: Int, pendIcon: Int): Unit = {
    val menber: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    menber.setIcon(icon)
    menber.setPendantId(pendIcon)
    menber.save()
  }

  def getPlayerSetId: util.Set[java.lang.Long] = {
    val setlist: util.Set[java.lang.Long] = new util.HashSet[lang.Long]()
    for (list <- situationmap.values()) {
      for (st <- list) {
        if (st.getAttackPlayerid > 0) {
          setlist.add(st.getAttackPlayerid)
        }
        if (st.getDefendPlayerid > 0) {
          setlist.add(st.getDefendPlayerid)
        }
      }
    }
    return setlist
  }

  def getM220300Info(build: M22.M220300.S2C.Builder): Unit = {
    initSimples()
    for (list <- situationmap.values()) {
      for (st <- list) {
        build.addStinfos(getSituationInfo(st))
      }
    }
    sender() ! getSituationInfoback(build)
  }

  //获得情报信息
  def getSituationInfo(st: Situation): M22.SituationInfo.Builder = {
    val info: M22.SituationInfo.Builder = M22.SituationInfo.newBuilder()
    if (getRoleInfo(st.defendPlayerid) != null) {
      info.setInfo1(getRoleInfo(st.defendPlayerid))
    }
    if (getRoleInfo(st.attackPlayerid) != null) {
      info.setInfo2(getRoleInfo(st.attackPlayerid))
    }
    info.setBigtype(st.getType)
    info.setSmalltype(st.getSmalltype)
    //TODO 试炼场
    //TODO 团长发奖励
    info.setLoseNum(st.getLose)
    info.setResult(st.getResult)
    info.setTime((st.getEvenTime / 1000).toInt)
    info.setJob(st.getNewName)
    info.setBuildup(st.getBuildup)
    return info
  }

  //获得玩家基本信息
  def getRoleInfo(playerId: java.lang.Long): M22.RoleInfo = {
    val sim: SimplePlayer = getSimplerByPlayerId(playerId)
    if (sim == null) {
      return null
    }
    val info: M22.RoleInfo.Builder = M22.RoleInfo.newBuilder()
    info.setCapity(sim.getCapacity)
    info.setDegreemax(sim.getBoomUpLimit.toInt)
    info.setDegreenow(sim.getBoom.toInt)
    info.setFaceIcon(sim.getFaceIcon)
    info.setLegionName(sim.getLegionName)
    info.setMilitary(sim.getMilitaryRank)
    info.setName(sim.getName)
    info.setPlayerId(sim.getId)
    return info.build()
  }

  def getSimplerByPlayerId(playerId: java.lang.Long): SimplePlayer = {
    for (sim <- simples) {
      if (sim.getId() == playerId) {
        return sim
      }
    }
    return null
  }

  def initSimples(): Unit = {
    val setlist: util.Set[java.lang.Long] = getPlayerSetId
    simples.clear()
    simples.addAll(PlayerService.onGetPlayerSimpleInfoList(setlist, areakey))
  }

  //添加情报
  def onaddSituation(situation: Situation): Unit = {
    var list: util.List[Situation] = situationmap.get(situation.getType)
    if (list == null) {
      list = new util.ArrayList[Situation]()
    }
    list.add(situation)
    SortUtil.anyProperSort(list, "getEvenTime", false)
    if (list.size() >= ArmyGroupDefine.SITUATION_MAX) {
      val del: Situation = list.remove(ArmyGroupDefine.SITUATION_MAX - 1)
      BaseSetDbPojo.getSetDbPojo(classOf[SituationDateSetDb], areakey).deletNotice(del)
    }
    BaseSetDbPojo.getSetDbPojo(classOf[SituationDateSetDb], areakey).addTeamDate(situation)
    situationmap.put(situation.getType, list)
  }

  def addShareValue(playerId: Long, value: Int): Unit = {
    val menber: ArmygroupMenber = getMenber(playerId)
    if (menber != null) {
      menber.setContribute(menber.getContribute + value)
      menber.setContributeWeek(menber.getContributeWeek + value)
      menber.save()
    }
    refreshdevoteRank()
    refreshContributeRank()
  }

  //  updateEachHourNotice()
  //
  //  def updateEachHourNotice() = {
  //    val event = new TriggerEvent(self, EachHourNotice(), TriggerType.WHOLE_HOUR, 0)
  //    getTriggerService(context) ! AddTriggerEvent(event)
  //  }

  def getTriggerService(actorContext: ActorContext) = {
    actorContext.actorSelection("../../" + ActorDefine.TRIGGER_SERVICE_NAME)
  }

  def onchangeMenberName(name: String, playerId: Long): Unit = {
    val menber: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    if (menber != null) {
      menber.setName(name)
      if (menber.getJob == ArmyGroupDefine.JOB_MANGER) {
        armygroup.setCommander(name)
      }
    }
  }

  def test(retype: Int): Unit = {
    armygroup.setLevel(10)
    armygroup.setBuild(999999)
    if (retype == 1) {
      for (men <- menbers) {
        men.setContribute(999999)
      }
    }
    armygroup.setVitality(armygroup.getVitality + 20000)
    if (armygroup.getVitality > getMaxVitilyValue()) {
      armygroup.setVitality(getMaxVitilyValue())
    }
  }

  //返回军团科技属性加成信息
  def onGetTechExpandPowerMap(): Unit = {
    addTechEpandPower()
    sender() ! GetTechExpandPowerMap(techExpandPowerMap)
  }

  def onArmyGroupShop(playerId: Long, itemId: Int, opt: Int, typeId: Int): Unit = {
    val menberInfo = getArmyMenberByPlayerId(playerId)
    if (opt == 0 || opt == 1) {
      val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
      val playerName = sp.getAccountName
      sendMsgToPlayerArmyModule(playerName, GameMsg.getArmyShop(armygroup, itemId, opt, typeId, menberInfo))
    }
    if (opt == 2) {
      val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
      val playerName = sp.getAccountName
      sendMsgToPlayerArmyModule(playerName, GameMsg.getArmyShopSucess(armygroup, itemId, 1, typeId, menberInfo))
    }
  }

  /** *查看申请列表 ***/
  def dolookAppList(): Unit = {
    val setlist: util.Set[java.lang.Long] = new util.LinkedHashSet[java.lang.Long]
    for (id <- armygroup.getApplymenbers) {
      setlist.add(id)
    }
    val simplePlayers: util.List[SimplePlayer] = PlayerService.onGetPlayerSimpleInfoList(setlist, areakey)
    sender() ! lookAppListback(simplePlayers)
  }

  /** *同意申请*取消 **/
  def agreeApply(myId: Long, otherId: Long, retype: Int): Unit = {
    var rs = 0
    val myMenber = getMenber(myId)
    if (retype == 1) {
      val otherSimple: SimplePlayer = PlayerService.getSimplePlayer(otherId, areakey)
      if (myMenber.getJob < ArmyGroupDefine.JOB_MIN_MANGER) {
        rs = ErrorCodeDefine.M220203_1
      }
      val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel)
      if (armygroup.getMenbers.size >= jsonObject.getInt("personNum")) {
        rs = ErrorCodeDefine.M220203_2
      }
      if (!armygroup.getApplymenbers().contains(otherId)) {
        rs = ErrorCodeDefine.M220203_4
      }
      if (otherSimple.getArmygrouid > 0) {
        rs = ErrorCodeDefine.M220203_5
      }
      if (armygroup.getMenbers.contains(otherId)) {
        rs = ErrorCodeDefine.M220203_3
        val setlist: util.Set[java.lang.Long] = armygroup.getApplymenbers
        setlist.remove(otherId)
        armygroup.setApplymenbers(setlist)
      }
      sendapplistNum()
      if (rs == 0) {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(otherId, areakey)
        val player = BaseDbPojo.getOfflineDbPojo(otherId, classOf[Player], areakey)
        val list: util.Set[java.lang.Long] = player.getApplyArmylist
        for (id <- list) {
          try {
            val msg: GameMsg.removeApplyid = new GameMsg.removeApplyid(otherId)
            context.actorSelection("../" + ActorDefine.ARMYGROUPNODE + id).tell(msg, self)
          } catch {
            case e: Exception => {
              System.err.print(e)
            }
          }
        }
        val value:Int = 1
        if (sp.online == true) {
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.notiyaddArmgroup(armygroup))
          sendMsgToRoleModule(sp.getAccountName, addAtivity(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION,value, 0))
        } else {
          player.setApplyArmylist(new util.LinkedHashSet[lang.Long]())
          player.setPost(ArmyGroupDefine.JOB_NORMAL)
          player.setArmygroupId(armygroup.getId)
          player.setLegionName(armygroup.getName)
          player.save()
          val activityProxy: ActivityProxy = new ActivityProxy(player.getActivitySet, areakey)
          val playerProxy: PlayerProxy = new PlayerProxy(player, areakey)
          activityProxy.reloadDefineData(playerProxy)
          activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION,value, playerProxy, 0)
          activityProxy.saveActivity()

          val simplePlayer: SimplePlayer = new SimplePlayer()
          val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
          tellService(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(sp))
        }
        val menberlylist: util.Set[java.lang.Long] = armygroup.getMenbers
        menberlylist.add(otherId)
        armygroup.setMenbers(menberlylist)
        val addmenber: ArmygroupMenber = BaseDbPojo.create(classOf[ArmygroupMenber], areakey)
        addmenber.setAccoutName(sp.getAccountName)
        addmenber.setArmyId(armygroup.getId)
        addmenber.setCapity(sp.getCapacity)
        addmenber.setContribute(0)
        addmenber.setJob(ArmyGroupDefine.JOB_NORMAL)
        addmenber.setLevel(sp.getLevel)
        addmenber.setName(sp.getName)
        addmenber.setPlayerId(sp.getId)
        addmenber.setSex(sp.getSex)
        addmenber.setVitality(0)
        addmenber.setLogintime(sp.getLogintime * 1000)
        addmenber.setOutlinetime(sp.getLoginOut)
        addmenber.setLevel(sp.getLevel)
        addmenber.setPendantId(sp.getPendant)
        addmenber.setIcon(sp.getIconId)
        addmenber.save()
        addmenber.setActivitycontributerank(0)
        val armberlist: util.Set[java.lang.Long] = armygroup.getArmmenbers
        armberlist.add(addmenber.getId)
        armygroup.setArmmenbers(armberlist)
        menbers.synchronized {
          menbers.add(addmenber)
        }
        refreshVitilyRank()
        refreshdevoteRank()
        refreshcapityRank()
        refreshCapity()
        val situation: Situation = new Situation(armygroup.getId, sp.getId, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_JOIN)
        onaddSituation(situation)
      }
    } else {
      if (myMenber.getJob < ArmyGroupDefine.JOB_MIN_MANGER) {
        rs = ErrorCodeDefine.M220203_1
      }

      if (rs == 0) {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(otherId, areakey)
        if (sp.online == true) {
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.notiyCancelApply(armygroup.getId))
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(otherId, classOf[Player], areakey)
          val setlist: util.Set[java.lang.Long] = player.getApplyArmylist
          setlist.remove(armygroup.getId)
          player.setApplyArmylist(setlist)
          player.save()
        }
        for (id <- sp.getAppArmylist) {
          val msg: GameMsg.removeApplyid = new GameMsg.removeApplyid(otherId)
          context.actorSelection("../" + ActorDefine.ARMYGROUPNODE + id) ! msg
        }
      }
    }
    if (rs == 0) {
      val setlist: util.Set[java.lang.Long] = armygroup.getApplymenbers
      setlist.remove(otherId)
      armygroup.setApplymenbers(setlist)
      sendapplistNum()
    }
    val info: LegionMemberInfo = getLegionMemberInfo(otherId)
    sender() ! agreeApplyBack(rs, armygroup.getId, info, retype)
  }

  def getNameByPosi(posi: Int): String = {
    if (posi == 7) {
      return "团长"
    } else if (posi == 6) {
      return "副团长"
    } else if (posi == 6) {
      return "副团长"
    } else if (posi == 5) {
      return "普通成员"
    } else if (posi == 4) {
      return armygroup.getSelfname4
    } else if (posi == 3) {
      return armygroup.getSelfname3
    } else if (posi == 2) {
      return armygroup.getSelfname2
    } else {
      return armygroup.getSelfname1
    }

  }

  /**
    * 检查军团排行榜刷新就发送
    */
  def checkRefreshLegionRank():Unit= {
    refreshcapityRank()
    if (menbers.size() != 0) {
      for (newrank <- menbers) {
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(newrank.getPlayerId, areakey)
        if (simplePlayer != null) {
          sendRankLog(simplePlayer.getId, newrank.getCapityrank, ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION)
          if (simplePlayer.online == true) {
            sendMsgToRoleModule(simplePlayer.getAccountName, addAtivity(ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION, armygroup.getRank, newrank.getCapityrank))
          } else {
            val player = BaseDbPojo.getOfflineDbPojo(newrank.getPlayerId, classOf[Player], areakey)
            val activityProxy: ActivityProxy = new ActivityProxy(player.getActivitySet, areakey)
            val playerProxy: PlayerProxy = new PlayerProxy(player, areakey)
            activityProxy.reloadDefineData(playerProxy)
            activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_RANK_LEGION, armygroup.getRank, playerProxy, newrank.getCapityrank)
            activityProxy.saveActivity()
            player.save()
          }
        }
      }
    }
  }
  /** *设置值为或者升职 ***/
  def onsetorUpJob(myId: Long, retype: Int, otherId: Long, job: Int): Unit = {
    var rs: Int = 0
    var up: Int = 0
    val myMenber = getMenber(myId)
    val myoldjob: Int = myMenber.getJob
    if (retype == 1) {
      if (myMenber.getJob != ArmyGroupDefine.JOB_MANGER) {
        rs = ErrorCodeDefine.M220221_1
      }
      if (job <= 4) {
        val jSONObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONJOB, "ID", job)
        if (armygroup.getLevel < jSONObject.getInt("openlv")) {
          rs = ErrorCodeDefine.M220221_2
        }
        if (getIndexNum(job) >= jSONObject.getInt("num")) {
          rs = ErrorCodeDefine.M220221_3
        }
      }
      if (job == ArmyGroupDefine.JOB_MIN_MANGER) {
        if (getIndexNum(job) >= 2) {
          rs = ErrorCodeDefine.M220221_3
        }
      }
      if (job >= ArmyGroupDefine.JOB_MANGER) {
        rs = ErrorCodeDefine.M220221_4
      }
      if (rs == 0) {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(otherId, areakey)
        if (sp.online == true) {
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.notiychangeJob(job))
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(otherId, classOf[Player], areakey)
          player.setPost(job)
          player.save()
        }
        val otherMenber = getMenber(otherId)
        otherMenber.setJob(job)
        val situation: Situation = new Situation(armygroup.getId, sp.getId, myId, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_NAME)
        situation.setNewName(getNameByPosi(job))
        onaddSituation(situation)
      }
    } else {
      if (myMenber.getJob == ArmyGroupDefine.JOB_MANGER) {
        rs = ErrorCodeDefine.M220221_5
      }
      if (getOutLineDayByPost(myMenber.getJob) < 7) {
        rs = ErrorCodeDefine.M220221_6
        if (myMenber.getJob <= ArmyGroupDefine.JOB_NORMAL) {
          if (getIndexNum(ArmyGroupDefine.JOB_MIN_MANGER) < 2) {
            rs = 0;
          }
        }
      }
      if (myMenber.getContribute < getOutMax(myMenber.getJob)) {
        rs = ErrorCodeDefine.M220221_7
        if (myMenber.getJob <= ArmyGroupDefine.JOB_NORMAL) {
          if (getIndexNum(ArmyGroupDefine.JOB_MIN_MANGER) < 2) {
            rs = 0;
          }
        }
      }

      if (rs == 0) {
        val menberchange: ArmygroupMenber = getOutLineDayByPostMenber(myMenber.getJob)
        if (myMenber.getJob <= ArmyGroupDefine.JOB_NORMAL) {
          myMenber.setJob(ArmyGroupDefine.JOB_MIN_MANGER)
          up = ArmyGroupDefine.JOB_MIN_MANGER
        } else {
          myMenber.setJob(ArmyGroupDefine.JOB_MANGER)
          armygroup.setCommander(myMenber.getName)
          armygroup.setCommandid(myMenber.getPlayerId)
          up = ArmyGroupDefine.JOB_MANGER
        }
        val situation: Situation = new Situation(armygroup.getId, myMenber.getPlayerId, myId, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_NAME)
        situation.setNewName(getNameByPosi(up))
        onaddSituation(situation)
        var falg: Int = 1
        if (menberchange != null) {
          if (menberchange.getJob == ArmyGroupDefine.JOB_MIN_MANGER) {
            if (getIndexNum(ArmyGroupDefine.JOB_MIN_MANGER) <= 2) {
              falg = 2
            }
          }
          if (falg == 1) {
            if (menberchange.getOutlinetime > menberchange.getLogintime) {
              val player = BaseDbPojo.getOfflineDbPojo(menberchange.getPlayerId, classOf[Player], areakey)
              player.setPost(myoldjob)
              player.save()
            } else {
              try {
                sendMsgToPlayerArmyModule(menberchange.getAccoutName, GameMsg.notiychangeJob(myoldjob))
              }
              catch {
                case e: Exception => {
                  System.err.print(e)
                }
              }
            }
            menberchange.setJob(myoldjob)
            val situation: Situation = new Situation(armygroup.getId, menberchange.getPlayerId, myId, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_NAME)
            situation.setNewName(getNameByPosi(myoldjob))
            onaddSituation(situation)
          }
        }
      }
    }
    refreshcapityRank()
    refreshdevoteRank()
    val leglist: util.List[LegionMemberInfo] = new util.ArrayList[LegionMemberInfo]()
    if (rs == 0) {
      if (getLegionMemberInfo(otherId) != null) {
        leglist.add(getLegionMemberInfo(otherId))
      }
      if (getLegionMemberInfo(myId) != null) {
        leglist.add(getLegionMemberInfo(myId))
      }
    }
    sender() ! setorUpJobBack(rs, retype, otherId, job, up, leglist)
  }


  def getOutMax(post: Int): Int = {
    var max: Int = 0
    if (post == ArmyGroupDefine.JOB_MIN_MANGER) {
      for (men <- menbers) {
        if (men.getJob == post) {
          if (men.getContribute >= max) {
            max = men.getContribute
          }
        }
      }
    }
    if (post <= ArmyGroupDefine.JOB_NORMAL) {
      for (men <- menbers) {
        if (men.getJob <= ArmyGroupDefine.JOB_NORMAL) {
          if (men.getContribute >= max) {
            max = men.getContribute
          }
        }
      }
    }
    return max
  }


  def getOutLineDayByPostMenber(post: Int): ArmygroupMenber = {
    var day = 0
    var menbr1: ArmygroupMenber = null;
    if (post <= ArmyGroupDefine.JOB_NORMAL) {
      for (menber <- menbers) {
        if (menber.getJob == ArmyGroupDefine.JOB_MIN_MANGER) {
          day = DateUtil.getDaysBetweenTowTime(GameUtils.getServerDate, new Date(menber.getOutlinetime))
          if (menber.getLogintime > menber.getOutlinetime) {
            day = 0
          }
          if (day >= 7) {
            menbr1 = menber
          }
        }
      }
    } else {
      for (menber <- menbers) {
        if (menber.getJob == ArmyGroupDefine.JOB_MANGER) {
          day = DateUtil.getDaysBetweenTowTime(GameUtils.getServerDate, new Date(menber.getOutlinetime))
          if (menber.getLogintime > menber.getOutlinetime) {
            day = 0
          }
          if (day >= 7) {
            menbr1 = menber
          }
        }
      }
    }
    return menbr1
  }


  def getOutLineDayByPost(post: Int): Int = {
    var day = 0
    if (post <= ArmyGroupDefine.JOB_NORMAL) {
      for (menber <- menbers) {
        if (menber.getJob == ArmyGroupDefine.JOB_MIN_MANGER) {
          day = DateUtil.getDaysBetweenTowTime(GameUtils.getServerDate, new Date(menber.getOutlinetime))
          if (menber.getLogintime > menber.getOutlinetime) {
            day = 0
          }
          if (day >= 7) {
            return day
          }
        }
      }
    } else {
      for (menber <- menbers) {
        if (menber.getJob == ArmyGroupDefine.JOB_MANGER) {
          day = DateUtil.getDaysBetweenTowTime(GameUtils.getServerDate, new Date(menber.getOutlinetime))
          if (menber.getLogintime > menber.getOutlinetime) {
            day = 0
          }
          if (day >= 7) {
            return day
          }
        }
      }
    }
    return day
  }

  def getIndexNum(post: Int): Int = {
    var num: Int = 0
    for (menber <- menbers) {
      if (menber.getJob == post) {
        num = num + 1
      }
    }
    return num
  }

  def String_length(value: String): Int = {
    var valueLength: Int = 0
    val chinese: String = "[\u4e00-\u9fa5]"
    var i: Int = 0
    while (i < value.length) {
      {
        val temp: String = value.substring(i, i + 1)
        if (temp.matches(chinese)) {
          valueLength += 2
        }
        else {
          valueLength += 1
        }
      }
      ({
        i += 1;
        i - 1
      })
    }
    return valueLength
  }

  /** ***改职位名称 ******/
  def oneditJobName(myId: Long, list: util.List[M22.LegionCustomJobShortInfo]): Unit = {
    var rs = 0
    val myMenber = getMenber(myId)
    if (myMenber.getJob < ArmyGroupDefine.JOB_MANGER) {
      rs = ErrorCodeDefine.M220220_1
    }
    for (lcj <- list) {
      val name: String = lcj.getName
      val index: Int = lcj.getIndex
      if (String_length(name) > 8) {
        rs = ErrorCodeDefine.M220220_3
      }
      val jSONObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONJOB, "ID", index)
      if (armygroup.getLevel < jSONObject.getInt("openlv")) {
        rs = ErrorCodeDefine.M220220_2
      }
    }
    if (rs == 0) {
      for (lcj <- list) {
        val name: String = lcj.getName
        val index: Int = lcj.getIndex
        if (index == 1) {
          armygroup.setSelfname1(name)
        }
        if (index == 2) {
          armygroup.setSelfname2(name)
        }
        if (index == 3) {
          armygroup.setSelfname3(name)
        }
        if (index == 4) {
          armygroup.setSelfname4(name)
        }
      }
    }
    val newlist: util.List[M22.LegionCustomJobShortInfo] = new util.ArrayList[LegionCustomJobShortInfo]()
    val lecutnemw1: M22.LegionCustomJobShortInfo.Builder = M22.LegionCustomJobShortInfo.newBuilder
    lecutnemw1.setIndex(1)
    lecutnemw1.setName(armygroup.getSelfname1)
    newlist.add(lecutnemw1.build())
    val lecutnemw2: M22.LegionCustomJobShortInfo.Builder = M22.LegionCustomJobShortInfo.newBuilder
    lecutnemw2.setIndex(2)
    lecutnemw2.setName(armygroup.getSelfname2)
    newlist.add(lecutnemw2.build())
    val lecutnemw3: M22.LegionCustomJobShortInfo.Builder = M22.LegionCustomJobShortInfo.newBuilder
    lecutnemw3.setIndex(3)
    lecutnemw3.setName(armygroup.getSelfname3)
    newlist.add(lecutnemw3.build())
    val lecutnemw4: M22.LegionCustomJobShortInfo.Builder = M22.LegionCustomJobShortInfo.newBuilder
    lecutnemw4.setIndex(4)
    lecutnemw4.setName(armygroup.getSelfname4)
    newlist.add(lecutnemw4.build())
    sender() ! editJobNameback(rs, newlist)
  }


  def editAffeche(myId: Long, content: String): Unit = {
    var rs = 0
    val myMenber = getMenber(myId)
    if (myMenber.getJob < ArmyGroupDefine.JOB_MANGER) {
      rs = ErrorCodeDefine.M220211_1
    }
    if (content.getBytes.length > 120) {
      rs = ErrorCodeDefine.M220211_2
    }
    if (content.indexOf(":") >= 0 || content.indexOf(";") >= 0 || content.indexOf("\"") >= 0 || content.indexOf("'") >= 0 || content.indexOf(",") >= 0 || content.indexOf("/") >= 0 || content.indexOf("\\") >= 0 || content.indexOf("*") >= 0) {
      rs = ErrorCodeDefine.M220211_3
    }
    if (rs == 0) {
      armygroup.setFiche(content)
    }
    sender() ! editAffecheSucess(rs)
  }

  def oneditArmyGroup(myId: Long, joinType: Int, list: util.List[Integer], level: Int, capity: Long, content: String): Unit = {
    var rs = 0
    val myMenber = getMenber(myId)
    if (myMenber.getJob < ArmyGroupDefine.JOB_MANGER) {
      rs = ErrorCodeDefine.M220210_1
    }
    for (id <- list) {
      if (id == 1) {
        if (joinType != ArmyGroupDefine.JOIN_TYPE1 && joinType != ArmyGroupDefine.JOIN_TYPE2) {
          rs = ErrorCodeDefine.M220210_2
        }
      }
      if (id == 4) {
        if (content.getBytes.length > 40) {
          rs = ErrorCodeDefine.M220210_3
        }
      }
    }
    if (rs == 0) {
      for (id <- list) {
        if (id == 1) {
          armygroup.setJoinWay(joinType)
        }
        if (id == 2) {
          armygroup.setConditonlevel(level)
        }
        if (id == 3) {
          armygroup.setConditoncapity(capity)
        }
        if (id == 4) {
          armygroup.setNotice(content)
        }

      }
    }
    sender() ! editArmyGroupback(rs, joinType, list, level, capity, content)
  }

  def onclearApplylist(myId: Long): Unit = {
    var rs = 0
    val myMenber = getMenber(myId)
    if (myMenber.getJob < ArmyGroupDefine.JOB_MIN_MANGER) {
      rs = ErrorCodeDefine.M220204_1
    }
    if (rs == 0) {
      for (id <- armygroup.getApplymenbers) {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(id, areakey)
        if (sp.online == true) {
          /** 在线 ***/
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.notiyCancelApply(armygroup.getId))
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(id, classOf[Player], areakey)
          val mylist: util.Set[java.lang.Long] = player.getApplyArmylist
          mylist.remove(armygroup.getId)
          player.setApplyArmylist(mylist)
          player.save()
        }
      }
      armygroup.setApplymenbers(new util.LinkedHashSet[lang.Long]())
      sendapplistNum()
    }
    sender() ! clearApplylistBack(rs)
  }

  /** ****踢人退出装让 ********/
  def onopeRateArmy(otherId: Long, myId: Long, retype: Int): Unit = {
    var rs = 0
    var oldjob: Int = ArmyGroupDefine.JOB_NORMAL
    val myMenber = getMenber(myId)
    val othetMeb = getMenber(otherId)
    if (myMenber == null) {
      rs = ErrorCodeDefine.M220201_1
    }
    if (retype == ArmyGroupDefine.OPERATE_KICK) {
      if (myMenber.getJob >= ArmyGroupDefine.JOB_MIN_MANGER && myMenber.getJob > othetMeb.getJob) {
      } else {
        rs = ErrorCodeDefine.M220201_2
      }
      if (othetMeb == null) {
        rs = ErrorCodeDefine.M220201_3
      }
      if (rs == 0) {
        val memberlist: util.Set[java.lang.Long] = armygroup.getMenbers
        memberlist.remove(otherId)
        armygroup.setMenbers(memberlist)
        val armylist: util.Set[java.lang.Long] = armygroup.getArmmenbers
        armylist.remove(othetMeb.getId)
        menbers.remove(othetMeb)
        othetMeb.del()
        armygroup.setArmmenbers(armylist)
        armygroup.setMenbers(memberlist)
        val sp: SimplePlayer = PlayerService.getSimplePlayer(otherId, areakey)
        if (sp.online == true) {
          /** 在线 ***/
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.notiyKickArmy())
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(otherId, classOf[Player], areakey)
          player.setApplyArmylist(new util.LinkedHashSet[java.lang.Long]())
          player.setArmygroupId(0l)
          player.setLegionName("")
          player.setPost(0)
          player.save()
          val simplePlayer: SimplePlayer = new SimplePlayer()
          val sp: SimplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
          tellService(ActorDefine.PLAYER_SERVICE_NAME, UpdateSimplePlayer(sp))
          player.save()
        }
        val situation: Situation = new Situation(armygroup.getId, sp.getId, myId, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_KICK)
        onaddSituation(situation)
      }
    } else if (retype == ArmyGroupDefine.OPERATE_transfer) {
      if (myMenber.getJob != ArmyGroupDefine.JOB_MANGER) {
        rs = ErrorCodeDefine.M220201_4
      }
      if (othetMeb == null) {
        rs = ErrorCodeDefine.M220201_5
      }
      if (rs == 0) {
        val sp: SimplePlayer = PlayerService.getSimplePlayer(otherId, areakey)
        if (sp.online == true) {
          /** 在线 ***/
          sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.notiyTrueManger())
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(otherId, classOf[Player], areakey)
          player.setApplyArmylist(new util.LinkedHashSet[java.lang.Long]())
          player.setPost(ArmyGroupDefine.JOB_MANGER)
          player.save()
        }
        armygroup.setCommander(sp.getName)
        armygroup.setCommandid(sp.getId)
        oldjob = othetMeb.getJob
        myMenber.setJob(othetMeb.getJob)
        othetMeb.setJob(ArmyGroupDefine.JOB_MANGER)
      }
      val situation: Situation = new Situation(armygroup.getId, myId, otherId, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_CHANGE_MASTER)
      onaddSituation(situation)
    } else if (retype == ArmyGroupDefine.OPERATE_Level) {
      if (menbers.size() <= 1) {
        rs = ErrorCodeDefine.M220201_6
      }
      if (myMenber.getJob == ArmyGroupDefine.JOB_MANGER) {
        rs = ErrorCodeDefine.M220201_7
      }
      if (rs == 0) {
        val memberlist: util.Set[java.lang.Long] = armygroup.getMenbers
        memberlist.remove(myId)
        armygroup.setMenbers(memberlist)
        val armylist: util.Set[java.lang.Long] = armygroup.getArmmenbers
        armylist.remove(myMenber.getId)
        myMenber.del()
        armygroup.setArmmenbers(armylist)
        menbers.remove(myMenber)
        val situation: Situation = new Situation(armygroup.getId, myId, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_LEVEL)
        onaddSituation(situation)
      }
    }
    if (rs == 0) {
      refreshVitilyRank()
      refreshdevoteRank()
      refreshcapityRank()
        armygroup.save()

    }
    val leglist: util.List[LegionMemberInfo] = new util.ArrayList[LegionMemberInfo]()
    if (rs == 0) {
      if (getLegionMemberInfo(otherId) != null) {
        leglist.add(getLegionMemberInfo(otherId))
      }
      if (getLegionMemberInfo(myId) != null) {
        leglist.add(getLegionMemberInfo(myId))
      }
    }
    refreshCapity()
    sender() ! opeRateArmyBack(rs, retype, otherId, leglist, oldjob)
  }

  def getMenber(playerId: Long): ArmygroupMenber = {
    for (menber <- menbers) {
      if (menber.getPlayerId == playerId) {
        return menber
      }
    }
    return null
  }

  /** ***通知去除申请 ******/
  def onremoveApplyid(playerId: Long): Unit = {
    val applylist: util.Set[java.lang.Long] = armygroup.getApplymenbers
    applylist.remove(playerId)
    armygroup.setApplymenbers(applylist)
    sendapplistNum()
  }

  def onapplyArmyJoin(idlist: util.Set[java.lang.Long], applytype: Int, level: Int, capity: Long, playerId: Long): Unit = {
    var rs = 0
    if (applytype == ArmyGroupDefine.OPERATE_APPLY) {
      if (idlist.size() >= ArmyGroupDefine.MAX_APPLYNUM) {
        rs = ErrorCodeDefine.M220102_1
      }
      if (armygroup.getConditoncapity > capity) {
        rs = ErrorCodeDefine.M220102_2
      }
      if (armygroup.getConditonlevel > level) {
        rs = ErrorCodeDefine.M220102_3
      }
      val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armygroup.getLevel)
      if (armygroup.getMenbers.size >= jsonObject.getInt("personNum")) {
        rs = ErrorCodeDefine.M220102_4
      }
      if (rs == 0) {
        if (armygroup.getJoinWay == ArmyGroupDefine.JOIN_TYPE1) {
          //直接加入
          val armgroupmenber: ArmygroupMenber = BaseDbPojo.create(classOf[ArmygroupMenber], areakey)
          val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
          armgroupmenber.setAccoutName(sp.getAccountName)
          armgroupmenber.setArmyId(armygroup.getId)
          armgroupmenber.setCapity(capity)
          armgroupmenber.setContribute(0)
          armgroupmenber.setName(sp.getName)
          armgroupmenber.setPlayerId(playerId)
          armgroupmenber.setSex(sp.getSex)
          armgroupmenber.setJob(ArmyGroupDefine.JOB_NORMAL)
          armgroupmenber.setLogintime(sp.getLogintime * 1000)
          armgroupmenber.setOutlinetime(sp.getLoginOut)
          armgroupmenber.setLevel(sp.getLevel)
          armgroupmenber.setPendantId(sp.getPendant)
          armgroupmenber.setIcon(sp.getIconId)
          armgroupmenber.save()
          armgroupmenber.setActivitycontributerank(0)
          menbers.synchronized {
            menbers.add(armgroupmenber)
          }
          val value:Int = 1
          sendMsgToRoleModule(sp.getAccountName, addAtivity(ActivityDefine.ACTIVITY_CONDITION_HAVE_LEGION,value, 0))
          val memberlist: util.Set[java.lang.Long] = armygroup.getMenbers
          memberlist.add(playerId)
          armygroup.setMenbers(memberlist)
          val armylist: util.Set[java.lang.Long] = armygroup.getArmmenbers
          armylist.add(armgroupmenber.getId)
          armygroup.setArmmenbers(armylist)
          refreshVitilyRank()
          refreshdevoteRank()
          refreshcapityRank()
          refreshCapity()
          armygroup.save()
          val situation: Situation = new Situation(armygroup.getId, sp.getId, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_JOIN)
          onaddSituation(situation)
          for (id <- idlist) {
            val msg: GameMsg.removeApplyid = new GameMsg.removeApplyid(playerId)
            context.actorSelection("../" + ActorDefine.ARMYGROUPNODE + id) ! msg
          }
          rs = 2
        } else {
          val applylist: util.Set[java.lang.Long] = armygroup.getApplymenbers
          applylist.add(playerId)
          armygroup.setApplymenbers(applylist)
          sendapplistNum()
          armygroup.save()
          rs = 1
        }

      }
    } else {
      val applylist: util.Set[java.lang.Long] = armygroup.getApplymenbers
      applylist.remove(playerId)
      menbers.remove(getArmyMenberByPlayerId(playerId))
      armygroup.setApplymenbers(applylist)
      sendapplistNum()
      rs = 3
      val situation: Situation = new Situation(armygroup.getId, playerId, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.PEOPLE_LEVEL)
      onaddSituation(situation)
    }
    sender() ! applyArmyJoinBack(rs, armygroup, applytype, armygroup.getId)

  }

  def onchangeMenberlogOuttime(playerId: Long): Unit = {

    import scala.collection.JavaConversions._

    for (menber <- menbers) {
      if (menber.getPlayerId == playerId) {
        menber.setOutlinetime(GameUtils.getServerDate().getTime)
        System.err.println(menber.getName + "下线时间" + GameUtils.getServerDateStr())
      }
    }
  }

  def onchangeMenberlogintime(playerId: Long): Unit = {

    import scala.collection.JavaConversions._

    for (menber <- menbers) {
      if (menber.getPlayerId == playerId) {
        menber.setLogintime(GameUtils.getServerDate().getTime)
        System.err.println(menber.getName + "上线时间时间" + GameUtils.getServerDateStr())
      }
    }
  }

  def onchangeMenberJob(playerId: Long, job: Int): Unit = {

    import scala.collection.JavaConversions._

    for (menber <- menbers) {
      if (menber.getPlayerId == playerId) {
        menber.setJob(job)
      }
    }
  }

  def onchangeMenberLevel(playerId: Long, level: Int): Unit = {

    import scala.collection.JavaConversions._

    for (menber <- menbers) {
      if (menber.getPlayerId == playerId) {
        menber.setLevel(level)
      }
    }
  }

  def onchangeMenberCapity(playerId: Long, capity: Long): Unit = {

    import scala.collection.JavaConversions._

    var allcapity: Long = 0
    for (menber <- menbers) {
      if (menber.getPlayerId == playerId) {
        menber.setCapity(capity)
      }
      allcapity = allcapity + menber.getCapity
    }
    armygroup.setCapity(allcapity)
    refreshCapity()
  }


  def refreshCapity(): Unit = {

    import scala.collection.JavaConversions._
    var allcapity: Long = 0
    for (menber <- menbers) {
      allcapity = allcapity + menber.getCapity
    }
    armygroup.setCapity(allcapity)
    refreshcapityRank()
  }

  override def preStart() = {
    import context.dispatcher
    context.system.scheduler.schedule(0 milliseconds, 1 minutes, context.self, OnServerTrigger())
    //初始化成员信息
    getActivityTimer()
    import scala.collection.JavaConversions._
    val setmenbers: util.Set[java.lang.Long] = armygroup.getMenbers
    val setarmenber: util.Set[java.lang.Long] = armygroup.getArmmenbers
    val removemenbers: util.Set[java.lang.Long] = new util.LinkedHashSet[java.lang.Long]()
    val removearmenber: util.Set[java.lang.Long] = new util.LinkedHashSet[java.lang.Long]()
    try {
      for (id <- armygroup.getArmmenbers) {
        val armygroupMenber: ArmygroupMenber = BaseDbPojo.get(id, classOf[ArmygroupMenber], areakey)
        if (armygroupMenber != null && !menbers.contains(armygroupMenber)) {
          menbers.add(armygroupMenber)
        } else {
          removearmenber.add(id)
        }
      }
      for (id <- armygroup.getMenbers) {
        val armygroupMenber: ArmygroupMenber = getArmyMenberByPlayerId(id)
        if (armygroupMenber == null) {
          removemenbers.add(id)
        }
      }
      setarmenber.removeAll(removearmenber)
      setmenbers.removeAll(removemenbers)
      armygroup.setArmmenbers(setarmenber)
      armygroup.setMenbers(setmenbers)
      techsMap.clear()
      for (id <- armygroup.getTechs) {
        val armyGroupTech: ArmyGroupTech = BaseDbPojo.get(id, classOf[ArmyGroupTech], areakey)
        techsMap.put(armyGroupTech.getTypeId, armyGroupTech)
      }
      refreshVitilyRank()
      refreshdevoteRank()
      refreshcapityRank()
      initfiveMap()
    }
    catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    if (armygroup.getRandomShops.size() == 0) {
      refreshShop()
    }
    val actor = context.actorSelection("../../" + ActorDefine.CHAT_SERVICE_NAME)
    actor ! CreateLegionChatNode(armygroup.getId)
    initSituation()

    val son: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LegionEvent)
    val playertroop:PlayerTroop = new PlayerTroop()
    val  dungeoproxy:DungeoProxy = new DungeoProxy()
    for(json <- son){
    if(BaseSetDbPojo.getSetDbPojo(classOf[LegionDungeoTeamSetDb], areakey).getTeamData(armygroup.getId, json.getInt("ID")) == null){
      playertroop.setPlayerTeams(dungeoproxy.createArmyGroupDungeoMonsterList(json.getInt("ID")))
      BaseSetDbPojo.getSetDbPojo(classOf[LegionDungeoTeamSetDb], areakey).addTeamDate(playertroop, armygroup.getId, json.getInt("ID"))
      armygroupdungeoinfo.put(json.getInt("ID"),dungeoproxy.createArmyGroupDungeoMonsterList(json.getInt("ID")))
    }
   }
  }


  def initSituation(): Unit = {
    situationmap.clear()
    val list: util.List[Situation] = BaseSetDbPojo.getSetDbPojo(classOf[SituationDateSetDb], areakey).getAllSituationDatas(armygroup.getId)
    for (st <- list) {
      if (situationmap.get(st.getType) == null) {
        val typelist: util.List[Situation] = new util.ArrayList[Situation]()
        typelist.add(st)
        situationmap.put(st.getType, typelist)
      } else {
        val typelist: util.List[Situation] = situationmap.get(st.getType)
        typelist.add(st)
        situationmap.put(st.getType, typelist)
      }
    }
  }

  def initfiveMap(): Unit = {
    fiveResMap.put(PlayerPowerDefine.POWER_food, armygroup.getFood)
    fiveResMap.put(PlayerPowerDefine.POWER_iron, armygroup.getIron)
    fiveResMap.put(PlayerPowerDefine.POWER_stones, armygroup.getStones)
    fiveResMap.put(PlayerPowerDefine.POWER_tael, armygroup.getTael)
    fiveResMap.put(PlayerPowerDefine.POWER_wood, armygroup.getWood)

  }


  override def postStop() = {
    savaDate()
  }

  def savaDate(): Unit = {
    for (men <- menbers) {
      if (men != null) {
        men.save()
      }
      if (armygroup != null) {
        armygroup.save()
      }
    }
    val son: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LegionEvent)
    val playertroop:PlayerTroop = new PlayerTroop()
    for (info <- armygroupdungeoinfo.keySet()) {
      for (json <- son) {
        if (info == json.getInt(("ID")) && armygroupdungeoinfo.get(info) != BaseSetDbPojo.getSetDbPojo(classOf[LegionDungeoTeamSetDb], areakey).getTeamData(armygroup.getId, json.getInt("ID")).getPlayerTeams) {
          playertroop.setPlayerTeams(armygroupdungeoinfo.get(info))
          BaseSetDbPojo.getSetDbPojo(classOf[LegionDungeoTeamSetDb], areakey).addTeamDate(playertroop, armygroup.getId, json.getInt("ID"))
        }
      }
    }
  }


  //发送消息给具体的player模块
  def sendMsgToPlayerArmyModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.ARMYGROUP_MODULE_NAME)
    actor ! msg
  }

  //发送到指定某块
  def sendMsgToPlayerModule(accountName: String, msg: AnyRef, mouduleName: String) = {
    val actor = context.actorSelection("../../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + mouduleName)
    actor ! msg
  }


  //获取某个service的消息值
  def askService[T](actorContext: ActorContext, serviceName: String, msg: AnyRef) = {
    val ref = actorContext.actorSelection("../../" + serviceName)
    val value: Option[T] = GameUtils.futureAsk(ref, msg, 10)

    val result: T = value.getOrElse(null).asInstanceOf[T]
    result
  }


  def changeArmyMenberExpandPower(): Unit = {
    val spIdList = armygroup.getArmmenbers
    for (spId <- spIdList) {
      val sp: SimplePlayer = PlayerService.getSimplePlayer(spId, areakey)
      if (sp.online == true) {
        /** 在线 ***/
        sendMsgToPlayerArmyModule(sp.getAccountName, GameMsg.GetTechExpandPowerMap(techExpandPowerMap))
      }
    }
  }

  def refreshcapityRank(): Unit = {
    var rank = 1
    SortUtil.anyProperSort(menbers, "getCapity", false)
    for (armymenber <- menbers) {
      armymenber.setCapityrank(rank)
      rank = rank + 1
    }
  }

  //刷新贡献排名
  def refreshdevoteRank(): Unit = {
    var rank = 1
    SortUtil.anyProperSort(menbers, "getContributeWeek", false)
    for (armymenber <- menbers) {
      armymenber.setDevotrank(rank)
      rank = rank + 1
    }
  }

  def refreshContributeRank(): Unit = {
    var rank = 1
    SortUtil.anyProperSort(menbers, "getContribute", false)
    for (armymenber <- menbers) {
      armymenber.setActivitycontributerank(rank)
      rank = rank + 1
    }
    for (player <- menbers) {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(player.getPlayerId, areakey)
      if (simplePlayer != null) {
        if (simplePlayer.online == true) {
          simplePlayer.setActivitycontributerank(player.getActivitycontributerank)
          sendMsgToRoleModule(simplePlayer.getAccountName, PlayerSetContributeRank( player.getActivitycontributerank))
        } else {
          val newplayer = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areakey)
          val playerProxy: PlayerProxy = new PlayerProxy(newplayer, areakey)
          newplayer.setActivitycontributerank(player.getActivitycontributerank)
          newplayer.save()
        }
      }
    }
  }

  def refreshACdevoteRank(): Unit = {
    var rank = 1
    SortUtil.anyProperSort(menbers, "getContribute", false)
    for (armymenber <- menbers) {
      armymenber.setActivitycontributerank(rank)
      rank = rank + 1
    }
    for (player <- menbers) {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(player.getPlayerId, areakey)
      if (simplePlayer != null) {
        sendRankLog(simplePlayer.getId, player.getActivitycontributerank, ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK)
        if (simplePlayer.online == true) {
          sendMsgToRoleModule(simplePlayer.getAccountName, addAtivity(ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK, player.getActivitycontributerank, 0))
        } else {
          val newplayer = BaseDbPojo.getOfflineDbPojo(simplePlayer.getId, classOf[Player], areakey)
          val activityProxy: ActivityProxy = new ActivityProxy(newplayer.getActivitySet, areakey)
          val playerProxy: PlayerProxy = new PlayerProxy(newplayer, areakey)
          newplayer.save()
          activityProxy.reloadDefineData(playerProxy)
          activityProxy.addActivityConditionValue(ActivityDefine.ACTIVITY_CONDITION_DONVATE_RANK, player.getActivitycontributerank, playerProxy, 0)
          activityProxy.saveActivity()
        }
      }
    }

  }
  var techExpandPowerMap: ConcurrentHashMap[java.lang.Integer, java.lang.Long] = new ConcurrentHashMap[java.lang.Integer, java.lang.Long]
  var reload = true

  /**
   * 军团科技属性加成
   */
  def addTechEpandPower() {
    try {
      if (reload) {
        val jsonObject: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LEGIONSCIENCE)
        for (obj <- jsonObject) {
          val techId = obj.getInt("type")
          val techInfo = techsMap.get(techId)
          if (techInfo != null) {
            val techLv: Int = techInfo.getTechLv
            if (techLv > 0) {
              //              val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONSCIENCE, "type", techId)
              val json: JSONObject = obj
              val array: JSONArray = json.getJSONArray("effect")
              var i = 0
              var len = array.length()
              while (len > 0) {
                val ja: JSONArray = array.getJSONArray(i)
                i += 1
                val power: Integer = ja.getInt(0)
                val powerVlaue: Long = ja.getInt(1)
                val value = powerVlaue * techLv
                techExpandPowerMap.put(power, value)
                len -= 1
              }
            }
          }
        }
        reload = false
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
  }

  /**
   * 返回的军团大厅信息
   */
  def getArmyHallInfo(): M22.ArmyInfo = {
    val builder: M22.ArmyInfo.Builder = M22.ArmyInfo.newBuilder()
    val armyName = armygroup.getName
    val armyLv = armygroup.getLevel
    val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armyLv)
    val buildNeed = jsonObject.getInt("reqScore")
    val allBuild = armygroup.getBuild
    builder.setArmyName(armyName)
    builder.setBuildNeed(buildNeed)
    builder.setAllBuild(allBuild)
    builder.setArmyLv(armyLv)
    return builder.build()
  }

  /**
   * 军团大厅，提升等级
   */
  def legionHallUp(playerId: Long, cmd: Int): Unit = {
    val buildNum = armygroup.getBuild
    val armyLv = armygroup.getLevel
    val allBuild = armygroup.getBuild
    val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
    val playerName = sp.getAccountName
    val menber: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    if (cmd == 0) {
      val armyInfo: M22.ArmyInfo = getArmyHallInfo()
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMHallUpInfo(cmd, armyLv, allBuild, armyInfo, menber))
    } else {
      val armyInfo: M22.ArmyInfo = getArmyHallInfo()
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMHallUpInfo(1, armyLv, allBuild, armyInfo, menber))
    }
  }

  /** 通知军团等级改变 ***/
  def notityLegionLevelChange(): Unit = {
    for (menber <- menbers) {
      if (menber.getLogintime > menber.getOutlinetime) {
        sendMsgToPlayerArmyModule(menber.getAccoutName, GameMsg.notityLegionLevel(armygroup.getLevel))
      }
    }
  }

  def armyLeveUp(): Unit = {
    val buildNum = armygroup.getBuild
    val armyLv = armygroup.getLevel
    val allBuild = armygroup.getBuild
    var sucess = false
    armygroup.synchronized {
      val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGION, "level", armyLv)
      if (jsonObject != null) {
        val needBuilNum = jsonObject.getInt("reqScore")
        if (buildNum >= needBuilNum) {
          armygroup.setBuild(buildNum - needBuilNum)
          armygroup.setLevel(armyLv + 1)
          val situation: Situation = new Situation(armygroup.getId, 0l, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.BUILD_UP)
          situation.setBuildup("军团大厅等级到" + armygroup.getLevel + "级")
         
          onaddSituation(situation)
          sucess = true
          armygroup.save()

        }
      }
    }
    val armyInfo: M22.ArmyInfo = getArmyHallInfo()
    if (sucess) {
      for (menber <- menbers) {
        val memberSimplePlayer = PlayerService.getSimplePlayer(menber.getPlayerId, areakey)
        if (memberSimplePlayer.online) {
          sendMsgToPlayerArmyModule(menber.getAccoutName, GameMsg.StMHallUpInfoSucc(armyInfo))
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(menber.getPlayerId, classOf[Player], areakey)
          player.setLegionLevel(armygroup.getLevel)
          player.save()
        }
      }
    } else {
      sender() ! StMHallUpInfoFaild(armyInfo)
    }


  }

  /**
   * 军团大厅，金币,资源捐献
   */
  def legionHallGoldDonate(playerId: Long, power: Int, num: Int): Unit = {
    val menberInfo = getArmyMenberByPlayerId(playerId)
    if (power == 200) {
      val goldContrubute: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.GOLD_CONTIBUTE, "num", num)
      val getBuild = goldContrubute.getInt("score")
      val getGoldContribute = goldContrubute.getInt("contribute")
      menberInfo.setContribute(menberInfo.getContribute + getGoldContribute)
      menberInfo.setDonatecontributeWeek(menberInfo.getDonatecontributeWeek + getGoldContribute)
      menberInfo.setContributeWeek(menberInfo.getContributeWeek + getGoldContribute)
      refreshdevoteRank()
      refreshContributeRank()
      armygroup.setBuild(armygroup.getBuild + getBuild)
      armygroup.setMession1(armygroup.getMession1 - 1)
    } else {
      val resContrubute: JSONObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RES_CONTIBUTE, "restype", power, "num", num)
      val getBuild = resContrubute.getInt("score")
      armygroup.setBuild(armygroup.getBuild + getBuild)
      val getResContribute = resContrubute.getInt("contribute")
      menberInfo.setContribute(menberInfo.getContribute + getResContribute)
      menberInfo.setDonatecontributeWeek(menberInfo.getDonatecontributeWeek + getResContribute)
      menberInfo.setContributeWeek(menberInfo.getContributeWeek + getResContribute)
      refreshdevoteRank()
      refreshContributeRank()
      menberInfo.save()
    }
    val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
    val playerName = sp.getAccountName
    val armyInfo: M22.ArmyInfo = getArmyHallInfo()
    addMessionActivity(ArmyGroupDefine.MESSIONTYPE1, playerId)
    sendMsgToPlayerArmyModule(playerName, GameMsg.StMHallContributeSucc(armyInfo, power))
  }


  /**
   * 军团科技，金币,资源捐献
   */
  def legionTechGoldDonate(techId: Int, power: Int, playerId: Long, cmd: Int, isFirst: Int, alltime: Int): Unit = {
    val armyMen: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    if (cmd == ArmyGroupDefine.UP_REQ) {
      if (!techsMap.containsKey(techId)) {
        val tech: ArmyGroupTech = BaseDbPojo.create(classOf[ArmyGroupTech], areakey)
        tech.setArmyGroupId(armygroup.getId)
        tech.setTypeId(techId)
        tech.setTechExp(0)
        tech.setTechLv(0)
        tech.save()
        armygroup.getTechs.add(tech.getId)
        armygroup.save()
        techsMap.put(techId, tech)
      }
      val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
      val playerName = sp.getAccountName
      val techInfo = techsMap.get(techId)
      val techLv = techInfo.getTechLv
      val techExp = techInfo.getTechExp
      val armyLv = armygroup.getLevel
      val armyTechLv = armygroup.getSciencelevel
      val techsinfo: M22.TechInfo = getTechInfo(playerId)
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMTechContributeInfo(techsinfo, techId, power, techLv, techExp, armyLv, armyTechLv, armyMen, armygroup))
    } else {
      //捐献成功
      val acLv = activityLv()
      val techInfo = techsMap.get(techId)
      val techexp = techInfo.getTechExp
      if (power == 200) {
        val goldContrubute: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.GOLD_CONTIBUTE, "num", isFirst)
        if (alltime == 0) {
          val activityObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", acLv)
          val scoretimes = activityObject.getInt("scoretimes")
          val getTechExp = goldContrubute.getInt("LegSciexp")
          val rewardExp = techexp + ((scoretimes / 100) * getTechExp)
          techInfo.setTechExp(rewardExp)
          armygroup.setMession1(armygroup.getMession1 - 1)
        } else {
          val getTechExp = goldContrubute.getInt("LegSciexp")
          val rewardExp = techexp + getTechExp
          techInfo.setTechExp(rewardExp)
        }
        val addcomtrubute = goldContrubute.getInt("contribute")
        val menberInfo = getArmyMenberByPlayerId(playerId)
        menberInfo.setContribute(menberInfo.getContribute + addcomtrubute)
        menberInfo.setDonatecontributeWeek(menberInfo.getDonatecontributeWeek + addcomtrubute)
        menberInfo.setContributeWeek(menberInfo.getContributeWeek + addcomtrubute)
        refreshdevoteRank()
        refreshContributeRank()
        techInfo.save()
        techsMap.put(techId, techInfo)
      } else {
        val resContrubute: JSONObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.RES_CONTIBUTE, "restype", power, "num", isFirst)
        if (alltime == 0) {
          val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", acLv)
          val scoretimes = jsonObject.getInt("scoretimes")
          val getTechExp = resContrubute.getInt("LegSciexp")
          val rewardExp = techexp + ((scoretimes / 100) * getTechExp)
          techInfo.setTechExp(rewardExp)
        } else {
          val getTechExp = resContrubute.getInt("LegSciexp")
          val rewardExp = techexp + getTechExp
          techInfo.setTechExp(rewardExp)
        }
        val addcomtrubute = resContrubute.getInt("contribute")
        val menberInfo = getArmyMenberByPlayerId(playerId)
        menberInfo.setContribute(menberInfo.getContribute + addcomtrubute)
        menberInfo.setDonatecontributeWeek(menberInfo.getDonatecontributeWeek + addcomtrubute)
        menberInfo.setContributeWeek(menberInfo.getContributeWeek + addcomtrubute)
        refreshdevoteRank()
        refreshContributeRank()
        techsMap.put(techId, techInfo)
        techInfo.save()
      }

      val levelUp = techAutoUp(techId) //科技大厅经验满值自动升级
      if (levelUp) {
        sendTechnologyToMembers()
      }
      //addTechEpandPower() //科技属性加成
      val playerName = getMenber(playerId).getAccoutName
      val techsinfo: M22.TechInfo = getTechInfo(playerId)
      addMessionActivity(ArmyGroupDefine.MESSIONTYPE1, playerId)
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMContributeSucc(techsinfo, techId, power))
      sendTechPowerChange()
    }

  }


  //推送告诉玩家属性改变
  def sendTechPowerChange(): Unit = {
    for (menber <- menbers) {
      val simple: SimplePlayer = PlayerService.getSimplePlayer(menber.getPlayerId, areakey)
      if (simple != null && simple.online) {
        sendMsgToPlayerArmyModule(menber.getAccoutName, GameMsg.GetTechExpandPowerMap(techExpandPowerMap))
      }
    }
  }

  /**
   * 请求科技大厅升级
   * 返回techLv,techExp
   */
  def onArmyGroupTechUp(playerId: Long, opt: Int): Unit = {
    val armyLv = armygroup.getLevel
    val techLv = armygroup.getSciencelevel
    if (opt == 0 || opt == 1) {
      val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
      val playerName = sp.getAccountName
      val buildNum = armygroup.getBuild
      val techInfo: M22.TechInfo = getTechInfo(playerId)
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMTechUpInfo(armyLv, techLv, buildNum, techInfo, opt))
    } else {
      armygroup.synchronized {
        val buildBum = armygroup.getBuild
        val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONSCIENCELV, "level", techLv)
        val reqScore: Int = jsonObject.getInt("reqScore")
        if (buildBum >= reqScore) {
          armygroup.setBuild(buildBum - reqScore)
          armygroup.setSciencelevel(techLv + 1)
          val situation: Situation = new Situation(armygroup.getId, 0l, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.BUILD_UP)
          situation.setBuildup("军团科技大厅等级到" + armygroup.getSciencelevel + "级")
          onaddSituation(situation)
          armygroup.save()
          val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
          val playerName = sp.getAccountName
          sendMsgToPlayerArmyModule(playerName, GameMsg.StMTechUpInfoSucc(getTechInfo(playerId)))
        }
      }
    }
  }


  def getSubTechsInfo(): util.List[M22.SubTechsInfo] = {
    val builder: M22.SubTechsInfo.Builder = M22.SubTechsInfo.newBuilder()
    val builderList: util.List[M22.SubTechsInfo] = new util.ArrayList[M22.SubTechsInfo]()
    for (techId <- techsMap.keySet()) {
      val tech = techsMap.get(techId)
      builder.setSubTechId(techId)
      builder.setSubTechExp(tech.getTechExp)
      builder.setSubTechLv(tech.getTechLv)
      builderList.add(builder.build())
      System.err.println("军团Id：" + tech.getArmyGroupId + "........ID:" + techId + ",,,,exp:" + tech.getTechExp + ",,lv:" + tech.getTechLv);
    }
    return builderList
  }

  /**
   * 返回的科技大厅信息
   */
  def getTechInfo(playerId: Long): M22.TechInfo = {
    val builder: M22.TechInfo.Builder = M22.TechInfo.newBuilder()
    val techLv = armygroup.getSciencelevel
    val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONSCIENCELV, "level", techLv)
    val buildNeed = jsonObject.getInt("reqScore")
    val allBuild = armygroup.getBuild
    val menberInfo = getArmyMenberByPlayerId(playerId)
    val myContribute = menberInfo.getContribute
    builder.setTechLv(techLv)
    builder.setBuildNeed(buildNeed)
    builder.setAllBuild(allBuild)
    builder.setMyContribute(myContribute)
    val subTechInfo: util.List[M22.SubTechsInfo] = getSubTechsInfo()
    if (subTechInfo.size() > 0) {
      builder.addAllSubTech(subTechInfo)
    }
    return builder.build()
  }


  /**
   * 科技大厅的科技，自动升级
   */
  def techAutoUp(techid: Int): Boolean = {
    val techInfo = techsMap.get(techid)
    var flag: Boolean = false
    var levelUp = false
    while (!flag) {
      val techLv = techInfo.getTechLv
      val techExp = techInfo.getTechExp
      val jsonObject: JSONObject = ConfigDataProxy.getConfigInfoFindByTwoKey(DataDefine.LEGIONLEVEL, "type", techid, "level", techLv)
      if (jsonObject != null && jsonObject.getInt("reqexp") != 0) {
        val needTechExp = jsonObject.getInt("reqexp")
        if (techExp >= needTechExp && techLv <= armygroup.getSciencelevel) {
          techInfo.setTechLv(techLv + 1)
          val newExp = techExp - needTechExp
          techInfo.setTechExp(newExp)
          levelUp = true
          reload = true
        } else {
          flag = true
        }
      } else {
        flag = true
      }
    }
    levelUp
  }


  def getWalfarRewardId(): Int = {
    val jsonList: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.WELFAREREWARD)
    val welfareLv = armygroup.getWelfarelevel
    for (json <- jsonList) {
      if (json.getInt("welfarelvmin") <= welfareLv && json.getInt("welfarelvmax") >= welfareLv) {
        return json.getInt("ID")
      }
    }
    return 1
  }

  /**
   * 福利院信息
   */
  def walfareInfo(playerId: Long): M22.PanelInfo = {
    initfiveMap()
    val jsonList: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.WELFAREREWARD)
    val armyLv = armygroup.getLevel
    val arrayList = new util.ArrayList[Int]()
    val menberInfo = getArmyMenberByPlayerId(playerId)
    val myContribute = menberInfo.getContribute
    System.err.println("我的贡献值val：" + myContribute)
    val welfareLv = armygroup.getWelfarelevel
    val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WELFARELV, "lv", welfareLv)
    val buildNeed = json.getInt("scoreneed")
    val info: M22.PanelInfo.Builder = M22.PanelInfo.newBuilder()
    info.setWelfarelv(welfareLv)
    info.setMyContribute(myContribute)
    info.setBuildNeed(buildNeed)
    info.setAllBuild(armygroup.getBuild)
    import scala.collection.JavaConversions._
    /*  for (ojt <- jsonList) {
        if (armyLv >= ojt.getInt("welfarelvmin") && armyLv <= ojt.getInt("welfarelvmax")) {
          val alist: JSONArray = ojt.getJSONArray("reward")
          var len = alist.length()
          var i = 0
          while (len > 0) {
            i += 1
            len -= 1
          }
          info.setCanGetId(ojt.getInt("ID"))
        }
      }*/
    info.setCanGetId(getWalfarRewardId())
    //info.setIscangetWelf(menberInfo.getIsgetwelfare)
    info.setActivityLv(activityLv())
    info.setActivityValue(armygroup.getVitality)
    val hasGetFood = menberInfo.getFood
    val hasGetIron = menberInfo.getIron
    val hasGetStone = menberInfo.getStones
    val hasGetTael = menberInfo.getTael
    val hasGetWood = menberInfo.getWood
    info.setHasgetfood(hasGetFood)
    info.setHasgetiron(hasGetIron)
    info.setHasgetstone(hasGetStone)
    info.setHasgettael(hasGetTael)
    info.setHasgetwood(hasGetWood)
    val armyHasFood = fiveResMap.get(PlayerPowerDefine.POWER_food)
    val armyHasIron = fiveResMap.get(PlayerPowerDefine.POWER_iron)
    val armyHasStone = fiveResMap.get(PlayerPowerDefine.POWER_stones)
    val armyHasTael = fiveResMap.get(PlayerPowerDefine.POWER_tael)
    val armyHasWood = fiveResMap.get(PlayerPowerDefine.POWER_wood)
    info.setCangetfood(armyHasFood - hasGetFood)
    info.setCangetiron(armyHasIron - hasGetIron)
    info.setCangetstone(armyHasStone - hasGetStone)
    info.setCangettael(armyHasTael - hasGetTael)
    info.setCangetwood(armyHasWood - hasGetWood)
    info.setType1(armygroup.getMession1)
    info.setType2(armygroup.getMession2)
    info.setType3(armygroup.getMession3)
    info.setType4(armygroup.getMession4)
    info.setType5(armygroup.getMession5)
    try {
      return info.build()
    } catch {
      case e: Exception => {
        System.err.print(e)
      }
    }
    null
  }

  //福利领取
  def getWelfare(playerId: Long, cmd: Int, canGetId: Int, typeId: Int): Unit = {
    val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
    val playerName = sp.getAccountName
    val welfareInfo = walfareInfo(playerId)
    val menber: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    sendMsgToPlayerArmyModule(playerName, GameMsg.StMGetwelfare(welfareInfo, getWalfarRewardId(), typeId, menber))

  }

  //福利升级
  def welfareUpNeed(playerId: Long, cmd: Int, typeId: Int): Unit = {
    val welfareLv = armygroup.getWelfarelevel
    if (cmd == ArmyGroupDefine.UP_REQ) {
      val armyLv = armygroup.getLevel
      val buildNum = armygroup.getBuild
      val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
      val playerName = sp.getAccountName
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMwelfareUpInfo(welfareLv, armyLv, buildNum, typeId))
    } else {
      welfareUp()
      val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
      val playerName = sp.getAccountName
      val welfareInfo = walfareInfo(playerId)
      sendMsgToPlayerArmyModule(playerName, GameMsg.StMwelfareUpInfoSucc(welfareInfo, typeId))
    }

  }

  def welfareReqNeed(playerId: Long, typeId: Int): Unit = {
    val welfare = walfareInfo(playerId)
    val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
    val playerName = sp.getAccountName
    sendMsgToPlayerArmyModule(playerName, GameMsg.StMwelfarReqInfo(welfare, typeId))
  }

  /**
   * 军团福利，(日常福利)福利院升级
   */
  def welfareUp(): Unit = {
    armygroup.synchronized {
      val wefareLv: Int = armygroup.getWelfarelevel
      val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.WELFARELV, "lv", wefareLv)
      val armyLv: Int = armygroup.getLevel
      val buildNum: Int = armygroup.getBuild
      if (buildNum >= json.getInt("scoreneed")) {
        armygroup.setBuild(buildNum - json.getInt("scoreneed"))
        armygroup.setWelfarelevel(armygroup.getWelfarelevel + 1)
        val situation: Situation = new Situation(armygroup.getId, 0l, 0l, GameUtils.getServerDate().getTime, 0, 0, ArmyGroupDefine.SITUATION_PEOPLE, ArmyGroupDefine.BUILD_UP)
        situation.setBuildup("福利院等级到" + armygroup.getWelfarelevel + "级")
        onaddSituation(situation)
      }
    }
  }

  def welfareCanGetFiveRes(playerId: Long): Unit = {
    val sp: SimplePlayer = PlayerService.getSimplePlayer(playerId, areakey)
    val menberInfo = getArmyMenberByPlayerId(playerId)
    myCanGetiveResMap.clear()
    for (power <- fiveResMap.keySet()) {
      if (power == PlayerPowerDefine.POWER_tael) {
        val tael = fiveResMap.get(power) - menberInfo.getTael
        myCanGetiveResMap.put(power, tael)
      } else if (power == PlayerPowerDefine.POWER_food) {
        val food = fiveResMap.get(power) - menberInfo.getFood
        myCanGetiveResMap.put(power, food)
      } else if (power == PlayerPowerDefine.POWER_iron) {
        val iron = fiveResMap.get(power) - menberInfo.getIron
        myCanGetiveResMap.put(power, iron)
      } else if (power == PlayerPowerDefine.POWER_stones) {
        val stones = fiveResMap.get(power) - menberInfo.getStones
        myCanGetiveResMap.put(power, stones)
      } else if (power == PlayerPowerDefine.POWER_wood) {
        val wood = fiveResMap.get(power) - menberInfo.getWood
        myCanGetiveResMap.put(power, wood)
      }
    }
    val menber: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
    sender() ! GameMsg.MtSwelfareGetSucc(myCanGetiveResMap, menber)
  }

  val myCanGetiveResMap: util.Map[java.lang.Integer, java.lang.Integer] = new util.HashMap[java.lang.Integer, java.lang.Integer]()
  val fiveResMap: util.Map[java.lang.Integer, java.lang.Integer] = new util.HashMap[java.lang.Integer, java.lang.Integer]()

  /**
   * 军团福利：五种资源
   */
  def getFiveRes(resMap: util.Map[java.lang.Integer, java.lang.Integer]): Unit = {
    val jSONObject: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", activityLv())
    for (res <- resMap.keySet()) {
      if (res == PlayerPowerDefine.POWER_tael) {
        val value: java.lang.Long = resMap.get(res).toLong * (jSONObject.getInt("resrate")) / 1000
        armygroup.setTael(value.toInt + armygroup.getTael);
        fiveResMap.put(res, armygroup.getTael)
      } else if (res == PlayerPowerDefine.POWER_food) {
        val value: java.lang.Long = resMap.get(res).toLong * (jSONObject.getInt("resrate")) / 1000
        armygroup.setFood(value.toInt + armygroup.getFood);
        fiveResMap.put(res, armygroup.getFood)
      } else if (res == PlayerPowerDefine.POWER_iron) {
        val value: java.lang.Long = resMap.get(res).toLong * (jSONObject.getInt("resrate")) / 1000
        armygroup.setIron(value.toInt + armygroup.getIron);
        fiveResMap.put(res, armygroup.getIron)
      } else if (res == PlayerPowerDefine.POWER_stones) {
        val value: java.lang.Long = resMap.get(res).toLong * (jSONObject.getInt("resrate")) / 1000
        armygroup.setStones(value.toInt + armygroup.getStones);
        fiveResMap.put(res, armygroup.getStones)
      } else if (res == PlayerPowerDefine.POWER_wood) {
        val value: java.lang.Long = resMap.get(res).toLong * (jSONObject.getInt("resrate")) / 1000
        armygroup.setWood(value.toInt + armygroup.getWood);
        fiveResMap.put(res, armygroup.getWood)
      }
    }
  }


  /**
   * 领取军团福利资源
   */
  def getWelfareRes(playerId: Long): util.Map[java.lang.Integer, java.lang.Integer] = {
    val getWelfareResMap: util.Map[java.lang.Integer, java.lang.Integer] = new util.HashMap[java.lang.Integer, java.lang.Integer]()
    val activLv = activityLv
    val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", activLv)
    val rate = json.getInt("resrate")
    if (json.getInt("resrate") == 0) {
      return getWelfareResMap
    } else {
      val armygroupMenber = getArmyMenberByPlayerId(playerId)
      if (armygroup.getTael > 0) {
        val menberTael = armygroupMenber.getTael
        if (menberTael > 0) {
          armygroupMenber.setTael(armygroup.getTael * rate)
        } else {
          armygroupMenber.setTael((armygroup.getTael * rate) + menberTael)
        }
        getWelfareResMap.put(PlayerPowerDefine.POWER_tael, armygroup.getTael)
      }
      if (armygroup.getStones > 0) {
        val menberStones = armygroupMenber.getStones
        if (menberStones > 0) {
          armygroupMenber.setStones(armygroup.getStones * rate)
        } else {
          armygroupMenber.setStones((armygroup.getStones * rate) + menberStones)
        }
        getWelfareResMap.put(PlayerPowerDefine.POWER_stones, armygroup.getStones)
      }
      if (armygroup.getWood > 0) {
        val menberWood = armygroupMenber.getWood
        if (menberWood > 0) {
          armygroupMenber.setWood(armygroup.getWood * rate)
        } else {
          armygroupMenber.setWood((armygroup.getWood * rate) + menberWood)
        }
        getWelfareResMap.put(PlayerPowerDefine.POWER_wood, armygroup.getWood)
      }
      if (armygroup.getFood > 0) {
        val menberFood = armygroupMenber.getFood
        if (menberFood > 0) {
          armygroupMenber.setFood(armygroup.getFood * rate)
        } else {
          armygroupMenber.setFood((armygroup.getFood * rate) + menberFood)
        }
        getWelfareResMap.put(PlayerPowerDefine.POWER_food, armygroup.getFood)
      }
      if (armygroup.getIron > 0) {
        val menberIron = armygroupMenber.getIron
        if (menberIron > 0) {
          armygroupMenber.setIron(armygroup.getIron * rate)
        } else {
          armygroupMenber.setIron((armygroup.getIron * rate) + menberIron)
        }
        getWelfareResMap.put(PlayerPowerDefine.POWER_iron, armygroup.getIron)
      }
    }
    return getWelfareResMap
  }

  def checkTimer {
    val c: Calendar = Calendar.getInstance
    c.setTime(GameUtils.getServerDate)
    val hour: Int = c.get(Calendar.HOUR_OF_DAY)
    val minu: Int = c.get(Calendar.MINUTE)
    val week: Int = c.get(Calendar.DAY_OF_WEEK) - 1
    if (hour == 0) {
      refreshShop()
      if (week == 1) {
        refreshWeek()
      }
    }
    if (hour == 4) {
      refreshwelfare()
      reduceVitity()
      refreshZero()
      refreshActiVitily()
      initfiveMap()
      refreshArmygroupdungeo()
      refreshMenbersDungeoTimes()
      refreshArmygroupdungeoinfo()
    }
    if (hour == 12) {
      refreshShop()
    }
    if (hour == 18) {
      refreshShop()
    }
    armygroup.save()
  }

  //重置军团副本
  def refreshArmygroupdungeoinfo(): Unit ={
    val dungeoDb:LegionDungeoTeamSetDb =new LegionDungeoTeamSetDb()
    val playertroop:PlayerTroop = new PlayerTroop()
    val  dungeoproxy:DungeoProxy = new DungeoProxy()
    val son: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LegionEvent)
    for(json <- son){
      playertroop.setPlayerTeams(dungeoproxy.createArmyGroupDungeoMonsterList(json.getInt("ID")))
      BaseSetDbPojo.getSetDbPojo(classOf[LegionDungeoTeamSetDb], areakey).addTeamDate(playertroop, armygroup.getId, json.getInt("ID"))
      armygroupdungeoinfo.put(json.getInt("ID"),dungeoproxy.createArmyGroupDungeoMonsterList(json.getInt("ID")))
    }
  }

  //刷新每日通关军团副本
  def refreshArmygroupdungeo(): Unit ={
    Dungeolist.clear()
    attackDungeolist.clear()
    armygroup.setLegionDungeoidbox(Dungeolist)
  }
  //刷新玩家每日挑战军团副本次数 及 4点刷新（在线玩家）军团副本列表信息---即270000推送
  def refreshMenbersDungeoTimes(): Unit ={
    //TODO  该方法需优化
    var dungeoinfo:util.Map[Integer, util.List[PlayerTeam]] = new util.HashMap[Integer, util.List[PlayerTeam]]()
    for(infos <- armygroupdungeoinfo.keySet()){
      dungeoinfo.put(infos,armygroupdungeoinfo.get(infos))
    }
    var list: util.List[Integer] = new util.ArrayList[Integer]()
    for(men <- menbers ){
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(men.getPlayerId, areakey)
      if (simplePlayer != null ) {
        if (simplePlayer.online == true) {
          sendMsgToRoleModule(simplePlayer.getAccountName, refreshLegionDungeoTimes(men.getPlayerId,dungeoinfo))
        } else {
          val player = BaseDbPojo.getOfflineDbPojo(men.getPlayerId, classOf[Player], areakey)
          player.setArmygroupdungeotimes(ActorDefine.LEGION_DUNGEO_CHANGE_TIME)
          player.setGetbox(list)
          player.save()
        }
      }
    }
  }
  def refreshZero(): Unit = {
    for (men <- menbers) {
      men.setIsgetwelfare(0)
    }
  }

  def refreshWeek(): Unit = {
    for (men <- menbers) {
      men.setDonatecontributeWeek(0)
      men.setContributeWeek(0)
    }
  }

  def refreshShop(): Unit = {
    var falg:Boolean=true
    var list: util.List[Integer]=new util.ArrayList[Integer]()
    while (falg) {
     list = rand3ShopGemItem()
      if(list.size()>0){
        falg = false
      }
    }
    armygroup.setGetlist("")
    armygroup.setPlayerrandomBuytimes("")
    armygroup.setRandomShops(list)
  }

  /**
   * 军团商店：珍品随机三个
   */
  def rand3ShopGemItem(): util.List[Integer] = {
    val objList: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LEGION_RANDOM_SHOP)
    var rate: Int = 0
    val armyLv: Int = armygroup.getLevel
    import scala.collection.JavaConversions._
    val map : java.util.Map[Integer,JSONObject]=new util.HashMap[Integer,JSONObject]()
    for (obj <- objList) {
      if (armyLv >= obj.getInt("legionlv")) {
        rate += obj.getInt("rate")
        map.put(rate,obj)
      }
    }
    val listItem: util.List[Integer] = new util.ArrayList[Integer]
    var flag: Boolean = false
    while (!flag) {
      import scala.collection.JavaConversions._
      val randNum: Int = RandomUtil.random(0, rate)
      for (objmap <- map.keySet()) {
        val jsonValue=map.get(objmap)
        if (objmap >= randNum  && (objmap - jsonValue.getInt("rate") )< randNum ) {
          if (!listItem.contains(jsonValue.getInt("ID"))) {
            if (listItem.size < 3) {
              listItem.add(jsonValue.getInt("ID"))
            } else {
              flag = true
            }
          }
        }
      }
    }
    return listItem
  }

  def refreshwelfare(): Unit = {
    for (menber <- menbers) {
      menber.setTael(0)
      menber.setIron(0)
      menber.setWood(0)
      menber.setStones(0)
      menber.setFood(0)
    }
    armygroup.setTael(0)
    armygroup.setIron(0)
    armygroup.setWood(0)
    armygroup.setStones(0)
    armygroup.setFood(0)
  }

  def refreshActiVitily(): Unit = {
    armygroup.setMession1(0)
    armygroup.setMession2(0)
    armygroup.setMession3(0)
    armygroup.setMession4(0)
    armygroup.setMession5(0)
    for (men <- menbers) {
      men.setVitality(0)
    }
  }

  def refreshVitilyRank(): Unit = {
    var rank = 1
    SortUtil.anyProperSort(menbers, "getVitality", false)
    for (armymenber <- menbers) {
      armymenber.setActivityrank(rank)
      rank = rank + 1
    }

  }

  /** *增加活跃度 ****/
  def addMessionActivity(retype: Int, playerId: Long): Unit = {
    val jsonObjectmession: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", retype)
    if (retype == ArmyGroupDefine.MESSIONTYPE1) {
      if (armygroup.getMession1 >= jsonObjectmession.getInt("max")) {
        return
      }
      val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", retype)
      val men: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
      men.setVitality(men.getVitality + json.getInt("reward"))
      armygroup.setVitality(armygroup.getVitality + json.getInt("reward"))
      if (armygroup.getVitality > getMaxVitilyValue()) {
        armygroup.setVitality(getMaxVitilyValue())
      }
      armygroup.setMession1(armygroup.getMession1 + 1)
    }
    if (retype == ArmyGroupDefine.MESSIONTYPE2) {
      if (armygroup.getMession2 >= jsonObjectmession.getInt("max")) {
        return
      }
      val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", retype)
      val men: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
      men.setVitality(men.getVitality + json.getInt("reward"))
      armygroup.setVitality(armygroup.getVitality + json.getInt("reward"))
      if (armygroup.getVitality > getMaxVitilyValue()) {
        armygroup.setVitality(getMaxVitilyValue())
      }
      armygroup.setMession2(armygroup.getMession2 + 1)
    }
    if (retype == ArmyGroupDefine.MESSIONTYPE3) {
      if (armygroup.getMession3 >= jsonObjectmession.getInt("max")) {
        return
      }
      val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", retype)
      val men: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
      men.setVitality(men.getVitality + json.getInt("reward"))
      armygroup.setVitality(armygroup.getVitality + json.getInt("reward"))
      if (armygroup.getVitality > getMaxVitilyValue()) {
        armygroup.setVitality(getMaxVitilyValue())
      }
      armygroup.setMession3(armygroup.getMession3 + 1)
    }
    if (retype == ArmyGroupDefine.MESSIONTYPE4) {
      if (armygroup.getMession4 >= jsonObjectmession.getInt("max")) {
        return
      }
      val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", retype)
      val men: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
      men.setVitality(men.getVitality + json.getInt("reward"))
      armygroup.setVitality(armygroup.getVitality + json.getInt("reward"))
      if (armygroup.getVitality > getMaxVitilyValue()) {
        armygroup.setVitality(getMaxVitilyValue())
      }
      armygroup.setMession4(armygroup.getMession4 + 1)
    }
    if (retype == ArmyGroupDefine.MESSIONTYPE5) {
      if (armygroup.getMession5 >= jsonObjectmession.getInt("max")) {
        return
      }
      val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONMESSION, "type", retype)
      val men: ArmygroupMenber = getArmyMenberByPlayerId(playerId)
      men.setVitality(men.getVitality + json.getInt("reward"))
      armygroup.setVitality(armygroup.getVitality + json.getInt("reward"))
      if (armygroup.getVitality > getMaxVitilyValue()) {
        armygroup.setVitality(getMaxVitilyValue())
      }
      armygroup.setMession5(armygroup.getMession5 + 1)
    }
    refreshVitilyRank()
  }

  def getArmyMenberByPlayerId(playerId: Long): ArmygroupMenber = {
    for (men1 <- menbers) {
      if (men1.getPlayerId == playerId) {
        return men1
      }
    }
    null
  }

  def reduceVitity(): Unit = {
    val json: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", activityLv())
    val value: Int = armygroup.getVitality
    var num: Int = value - json.getInt("dailydeduct")
    if (num < 0) {
      num = 0
    }
    armygroup.setVitality(num)
    if (armygroup.getVitality > getMaxVitilyValue()) {
      armygroup.setVitality(getMaxVitilyValue())
    }
  }

  /**
   * 活跃等级
   */
  def activityLv(): Int = {
    val jsonlist: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LEGIONACTIVITY)
    val hadActivity = armygroup.getVitality
    for (json <- jsonlist) {
      //上一等级的值
      val lastjson: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", json.getInt("level") - 1)
      var lastvalue = -1
      if (lastjson != null) {
        lastvalue = lastjson.getInt("activeneed")
      }
      if (hadActivity > lastvalue && hadActivity <= json.getInt("activeneed")) {
        return json.getInt("level")
      }
      val nextjson: JSONObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.LEGIONACTIVITY, "level", json.getInt("level") + 1)
      if (nextjson == null) {
        return json.getInt("level")
      }
    }
    return 1
  }

  def sendTechnologyToMembers(): Unit = {
    addTechEpandPower()
    for (member: ArmygroupMenber <- menbers) {
      if (isMemberOnline(member)) {
        sendMsgToPlayerArmyModule(member.getAccoutName, GetTechExpandPowerMap(techExpandPowerMap))
      }
    }
  }

  def isMemberOnline(member: ArmygroupMenber): Boolean = {
    member.getLogintime > member.getOutlinetime
  }

  def getLegionMemberInfo(playerId: Long): LegionMemberInfo = {
    for (armygroupMenber <- menbers) {
      if (armygroupMenber.getPlayerId == playerId) {
        val legMen: M22.LegionMemberInfo.Builder = M22.LegionMemberInfo.newBuilder
        legMen.setId(armygroupMenber.getPlayerId)
        legMen.setDevoterank(armygroupMenber.getDevotrank)
        legMen.setCapacity(armygroupMenber.getCapity)
        legMen.setName(armygroupMenber.getName)
        legMen.setJob(armygroupMenber.getJob)
        legMen.setLevel(armygroupMenber.getLevel)
        legMen.setCapacity(armygroupMenber.getCapity)
        legMen.setDevote(armygroupMenber.getDonatecontributeWeek)
        legMen.setDevotoWeek(armygroupMenber.getContributeWeek)
        legMen.setSex(armygroupMenber.getSex)
        legMen.setCapityrank(armygroupMenber.getCapityrank)
        legMen.setActivityrank(armygroupMenber.getActivityrank)
        legMen.setActivityvalue(armygroupMenber.getVitality)
        legMen.setPendantId(armygroupMenber.getPendantId)
        legMen.setIconId(armygroupMenber.getIcon)
        if (armygroupMenber.getLogintime > armygroupMenber.getOutlinetime) {
          legMen.setIsOnline(1)
        }
        else {
          legMen.setIsOnline(0)
        }
        return legMen.build()
      }
    }
    null
  }

  def getMaxVitilyValue(): Int = {
    var value: Int = 0
    val jsonlist: util.List[JSONObject] = ConfigDataProxy.getConfigAllInfo(DataDefine.LEGIONACTIVITY)
    for (json <- jsonlist) {
      if (value < json.getInt("activeneed")) {
        value = json.getInt("activeneed")
      }
    }
    return value
  }

  /**
   * 军团增加有福同享活动 包箱记录
    *
    * @param playerId 分享者
   * @param chargeId 充值所在档位
   * @param createTime 记录生成时间
   */
  def doAddLegionShareRecord(playerId: Long, chargeId: Int, createTime: Int, sharedPlayer: String) = {
    if (menbers.size() != 0) {
      for (mem <- menbers) {
        val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(mem.getPlayerId, areakey)
        if (simplePlayer != null && simplePlayer.getId != playerId) {
          if (simplePlayer.online == true) {
            //playerId :Long,chargeId:Int,createTime:Long, sharePlayerName: String
            sendMsgToRoleModule(simplePlayer.getAccountName, addLegionShareRecord(mem.getPlayerId, chargeId, createTime, sharedPlayer));
          } else {
            val player = BaseDbPojo.getOfflineDbPojo(mem.getPlayerId, classOf[Player], areakey)
            val activityProxy: ActivityProxy = new ActivityProxy(player.getActivitySet, areakey)
            val playerProxy: PlayerProxy = new PlayerProxy(player, areakey)
            activityProxy.reloadDefineData(playerProxy)
            // addLegionShareRecord(playerProxy : PlayerProxy, chargeId : Int, sharePlayerName : String, recordTime : Int) {
            activityProxy.addLegionShareRecord(playerProxy, chargeId, sharedPlayer, createTime)
            activityProxy.saveActivity()
            player.save()
          }
        }
      }
    }
  }
/*
  //添加每日通关军团副本
  def onchangeArmyGroupDungeoInfo(sort:Integer) = {
    if (!Dungeolist.contains(sort)) {
      Dungeolist.add(sort)
      armygroup.setLegionDungeoidbox(Dungeolist)
    }
    if(sort > ArmygroupDungeoMax ){
      ArmygroupDungeoMax = sort
      armygroup.setMaxLegionDungeoid(ArmygroupDungeoMax)
    }

  }
  //更新军团副本信息
  def onchangeGroupDungeoInfo(dungeoId:java.lang.Integer,monsterlist:util.List[PlayerTeam]): Unit ={
    for(infos <- armygroupdungeoinfo.keySet()){
      if(infos == dungeoId && armygroupdungeoinfo.get(infos) != monsterlist ){
        armygroupdungeoinfo.put(infos,monsterlist)
      }
    }
    if(attackDungeolist.contains(dungeoId)){
      attackDungeolist.remove(dungeoId)
    }
  }

  //判断是否可打
 def oniscanattackArmyGroupDungeo(dungeoid:Int): Unit ={
    var flag:Boolean = false
    //attackDungeolist.synchronized{
      if(!attackDungeolist.contains(dungeoid)){
        flag = true
      }
      sender() ! iscanattackArmyGroupDungeore(flag)
    //}

  }

  //获取军团副本信息(monster)
   def onaddungeolist(acname:String,battleType: Int, eventId: Int, cmd: Int, team: util.List[PlayerTeam], saveTraffic: Int): Unit ={
    attackDungeolist.add(eventId)
    for(infos <- armygroupdungeoinfo.keySet()){
       if(infos == eventId){
         sendMsgDungeoModule(acname,returnarmygroupinfos(battleType,eventId,cmd,team,saveTraffic,armygroupdungeoinfo.get(infos)))
       }
     }

   }
  //获取章节副本信息
  def ongetArmyGroupDungeoInfo(dungeoid:Int): Unit ={
    var dungeoinfo:util.Map[Integer, util.List[PlayerTeam]] = new util.HashMap[Integer, util.List[PlayerTeam]]()
    for(infos <- armygroupdungeoinfo.keySet()){
      var json: JSONObject= ConfigDataProxy.getConfigInfoFindById(DataDefine.LegionEvent,infos.toLong)
      if(dungeoid ==  json.getInt("chapter")){
         dungeoinfo.put(infos,armygroupdungeoinfo.get(infos))
      }
    }
    sender() ! ArmyGroupDungeoInfo(dungeoid,dungeoinfo)
  }
  //270000协议 获取副本信息
  def onallarmygroupdungeoinfo(): Unit ={
    var dungeoinfo:util.Map[Integer, util.List[PlayerTeam]] = new util.HashMap[Integer, util.List[PlayerTeam]]()
    for(infos <- armygroupdungeoinfo.keySet()){
        dungeoinfo.put(infos,armygroupdungeoinfo.get(infos))
      }
    sender() !reallarmygroupdungeoinfo(dungeoinfo)
  }
*/
  //通知到service
  def tellService(serviceName: String, msg: AnyRef) = {
    context.actorSelection("../../" + serviceName) ! msg
  }
  def sendMsgToRoleModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.ROLE_MODULE_NAME)
    actor ! msg
  }

  def sendMsgDungeoModule(accountName: String, msg: AnyRef) = {
    val actor = context.actorSelection("../../" + ActorDefine.PLAYER_SERVICE_NAME + "/" + accountName + "/" + ActorDefine.DUNGEO_MODULE_NAME)
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

}


