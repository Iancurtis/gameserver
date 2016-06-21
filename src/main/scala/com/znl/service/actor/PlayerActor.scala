package com.znl.service.actor

import java.util
import java.util.Calendar

import akka.actor.{Actor, ActorLogging, Props}
import com.znl.base.BaseLog
import com.znl.core.{Notice, SimplePlayer}
import com.znl.define._
import com.znl.framework.socket.Response
import com.znl.log.PlayerLogin
import com.znl.log.admin.tbllog_player
import com.znl.modules.activity.ActivityModule
import com.znl.modules.adviser.AdviserModule
import com.znl.modules.arena.ArenaModule
import com.znl.modules.armygroup.ArmyGroupModule
import com.znl.modules.battle.BattleModule
import com.znl.modules.build.BuildModule
import com.znl.modules.capacity.CapacityModule
import com.znl.modules.cdkey.CdkeyModule
import com.znl.modules.chat.ChatModule
import com.znl.modules.dungeo.DungeoModule
import com.znl.modules.equip.EquipModule
import com.znl.modules.friend.FriendModule
import com.znl.modules.item.ItemModule
import com.znl.modules.login.LoginModule
import com.znl.modules.lotter.LotterModule
import com.znl.modules.mail.MailModule
import com.znl.modules.map.MapModule
import com.znl.modules.newBuild.NewBuildModule
import com.znl.modules.ranks.PowerRanksModule
import com.znl.modules.role.RoleModule
import com.znl.modules.share.ShareModule
import com.znl.modules.skill.SkillModule
import com.znl.modules.soldier.SoldierModule
import com.znl.modules.system.SystemModule
import com.znl.modules.task.TaskModule
import com.znl.modules.technology.TechnologyModule
import com.znl.modules.troop.TroopModule
import com.znl.msg.GameMsg._
import com.znl.pojo.db.Player
import com.znl.proto.M1
import com.znl.proxy._
import com.znl.service.ServiceActorTrait
import com.znl.utils.GameUtils
import org.apache.mina.core.session.IoSession
import org.json.JSONObject

import scala.concurrent.duration._

/**
 * Created by Administrator on 2015/10/22.
 */

object PlayerActor{
  def props(accountName : String, areaId : Int, ioSession: IoSession) = Props(classOf[PlayerActor], accountName, areaId, ioSession)
}

class PlayerActor(accountName : String, areaId : Int, var ioSession: IoSession) extends Actor with ActorLogging with ServiceActorTrait{

  var player:Player = null
  var gameProxy : GameProxy = null
  var lastHeartbeat = System.currentTimeMillis

  val responseLists : java.util.List[Response]  = new util.ArrayList[Response]()

  import context.dispatcher
  context.system.scheduler.schedule(20 minutes,20 minutes,context.self,AutoSavePlayer())

  override def preStart() = {
    log.info("!start player actor!!" + accountName)
    registerModules()
    startLoginModule()
    startCheckHeartbeat()
  }

  override def postStop() ={
    if(this.gameProxy != null){
      savePlayer()
      val playerProxy: PlayerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME)
      /**退出日志**/
      playerProxy.quitLog()
      this.gameProxy.finalize()
    }

    if(player != null){
      context.parent ! StopPlayerActorSuccess(player.getId)
    }else{
      log.warning("!!!!停止的playerActor竟然没有玩家数据!!!!!!")
    }
  }

  def savePlayer() ={
    val playerProxy: PlayerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME)
    playerProxy.savePlayer
    val soldierProxy: SoldierProxy = gameProxy.getProxy(ActorDefine.SOLDIER_PROXY_NAME)
    soldierProxy.saveSoldier
    val itemProxy: ItemProxy = gameProxy.getProxy(ActorDefine.ITEM_PROXY_NAME)
    itemProxy.saveItems
    val timerdbProxy: TimerdbProxy = gameProxy.getProxy(ActorDefine.TIMERDB_PROXY_NAME)
    timerdbProxy.saveTimers
    val dungeoProxy: DungeoProxy = gameProxy.getProxy(ActorDefine.DUNGEO_PROXY_NAME)
    dungeoProxy.saveDungeo
    val resFunBuildProxy: ResFunBuildProxy = gameProxy.getProxy(ActorDefine.RESFUNBUILD_PROXY_NAME)
    resFunBuildProxy.saveResFunBuildings
    val technologyProxy: TechnologyProxy = gameProxy.getProxy(ActorDefine.TECHNOLOGY_PROXY_NAME)
    technologyProxy.saveTechnology
    val skillProxy: SkillProxy = gameProxy.getProxy(ActorDefine.SKILL_PROXY_NAME)
    skillProxy.saveSkill
    val equipProxy: EquipProxy = gameProxy.getProxy(ActorDefine.EQUIP_PROXY_NAME)
    equipProxy.saveEquips
    val performTasksProxy: PerformTasksProxy = gameProxy.getProxy(ActorDefine.PERFORMTASKS_PROXY_NAME)
    performTasksProxy.savePerformTasks
    val itemBuffProxy: ItemBuffProxy = gameProxy.getProxy(ActorDefine.ITEMBUFF_PROXY_NAME)
    itemBuffProxy.saveItemBuff
    val ordancepieceProxy :OrdnancePieceProxy=gameProxy.getProxy(ActorDefine.ORDANCEPIECE_PROXY_NAME);
    ordancepieceProxy.saveOrdnancePieces()
    val ordanceProxy :OrdnanceProxy=gameProxy.getProxy(ActorDefine.ORDANCE_PROXY_NAME);
    ordanceProxy.saveOrdnances()
    val activityProxy : ActivityProxy = gameProxy.getProxy(ActorDefine.ACTIVITY_PROXY_NAME)
    activityProxy.saveActivity()
    val collproxy: CollectProxy=gameProxy.getProxy(ActorDefine.COLLECT_PROXY_NAME)
    collproxy.saveCollect()
    val advserproxy:AdviserProxy=gameProxy.getProxy(ActorDefine.ADVISER_PROXY_NAME)
    advserproxy.saveAdviser()
    val newBuildProxy:NewBuildProxy = gameProxy.getProxy(ActorDefine.NEW_BUILD_PROXY_NAME)
    newBuildProxy.saveResFunBuildings()
  }

  def startCheckHeartbeat() ={
    lastHeartbeat = System.currentTimeMillis
    import context.dispatcher
    context.system.scheduler.schedule(30 seconds,30 seconds,context.self, CheckHeartbeat())
  }

  override def receive: Receive = {
    case msg : ReplacePlayerSession =>
      onReplacePlayerSession(msg)
    case netMsg : ReceiveNetMsg =>
      onReceiveNetMsg(netMsg)
    case sendMsg : SendNetMsgToClient =>
      onSendNetMsgToClient(sendMsg.response)
    case pushMsg : PushtNetMsgToClient =>
      pushNetMsgToClient()
    case sendNetMsg : SendNetMsg =>
      onSendNetMsg(sendNetMsg.response)
    case m : MulticastNetToClient =>
      onMulticastNetToClient()
    case LoginSuccess(player,gameProxy) =>
      onLoginSuccess(player,gameProxy)
    case LoginFail(player) =>
      onLoginFail(player)
    case KickPlayerOffline(rs : Int, reason : String) =>
      onKickPlayerOffline(rs,reason)
    case AutoSavePlayer() =>
      autoSavePlayer(player)
    case AutoSaveOffPlayerData(accountName : String) =>
      logOutHandle(player)
      autoSavePlayer(player)
    case ModuleSendLog(log) =>
      onModuleSendLog(log)
    case ModuleSendAdminLog(log, actionType, key, value) =>
      onSendAdminLogToService(log, actionType, key, value)
    case CheckHeartbeat() =>
      checkHeartbeat()
    case msg : AddWorldBuildingSuccess =>
      onAddWorldBuildingSuccess(msg)
    case msg :EachHourNotice =>
      eachHourNotice()
    case msg : EachMinuteNotice =>
      eachMiuteNotice()
    case Reload() =>
      reloadNotice()
    case _ =>
  }


  def reloadNotice(): Unit ={
    context.actorSelection(ActorDefine.ACTIVITY_MODULE_NAME) ! Reload()
  }

  def logOutHandle(player: Player): Unit ={
    player.setLoginOutTime(GameUtils.getServerDate().getTime)
    if (player.getArmygroupId > 0) {
      val mess: changeMenberlogOuttime = new changeMenberlogOuttime(player.getId)
      context.actorSelection("../../" + ActorDefine.ARMYGROUP_SERVICE_NAME + "/" + ActorDefine.ARMYGROUPNODE + player.getArmygroupId).tell(mess, self)
    }
    val notice : Notice= new Notice(player.getId, GameUtils.getServerDate().getTime, "",0l, TipDefine.NOTICE_TYPE_ENERY, player.getPushId)
    tellService(ActorDefine.PUSH_SERVICE_NAME, addNotice(notice))
    val nextime:Long=getGuanyuqingkeTme
   if(nextime!=0l){
     val notice : Notice= new Notice(player.getId, GameUtils.getServerDate().getTime, "",nextime, TipDefine.NOTICE_TYPE_GUANYUQINGKE, player.getPushId)
     tellService(ActorDefine.PUSH_SERVICE_NAME, addNotice(notice))
   }
  }

  def getGuanyuqingkeTme: Long = {
    val jsonObjectlist: util.List[JSONObject] = ConfigDataProxy.getConfigInfoFilterByOneKey(DataDefine.ACTIVE_EFFECT, "conditiontype", ActivityDefine.ACTIVITY_CONDITION_ENERY_EVERYDAY)
    val c: Calendar = Calendar.getInstance
    c.setTime(GameUtils.getServerDate)
    if (jsonObjectlist.size == 0) {
      return 0l
    }
    val nowTime: Int = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE)
    var mintime: Int = 2329
    import scala.collection.JavaConversions._
    for (jsonObject <- jsonObjectlist) {
      if (jsonObject.getInt("condition2") > nowTime) {
        val hour: Int = jsonObject.getInt("condition1") / 100
        val min: Int = jsonObject.getInt("condition1") - (hour * 100)
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, min)
        val nexttime: Long = c.getTimeInMillis - GameUtils.getServerDate.getTime
        return nexttime
      }
      if (jsonObject.getInt("condition1") < mintime) {
        mintime = jsonObject.getInt("condition1")
      }
    }
    val hour: Int = mintime / 100
    val min: Int = mintime - (hour * 100)
    c.set(Calendar.HOUR_OF_DAY, hour)
    c.set(Calendar.MINUTE, min)
    c.add(Calendar.DATE, 1)
    val nexttime: Long = c.getTimeInMillis - GameUtils.getServerDate.getTime
    return nexttime
  }

  def onReplacePlayerSession(msg : ReplacePlayerSession) ={
    this.ioSession = msg.ioSession
    ioSession.setAttribute(ActorDefine.PLAYER_IS_LOGIN_SUCCESS_KEY, true) //重连也表示登录成功
    //刷新一下心跳时间
    lastHeartbeat = System.currentTimeMillis()
  }

  def eachHourNotice() = {
    val moduleName = moduleMap.get(ActorDefine.SYSTEM_MODULE_ID)
    val moduleActor = context.actorSelection(moduleName.get)
    moduleActor ! EachHourNotice()
  }

  def eachMiuteNotice() = {
    val moduleName = moduleMap.get(ActorDefine.SYSTEM_MODULE_ID)
    val moduleActor = context.actorSelection(moduleName.get)
    moduleActor ! EachMinuteNotice()
  }

  def autoSavePlayer(player: Player) = {
    val moduleName = moduleMap.get(ActorDefine.SYSTEM_MODULE_ID)
    val moduleActor = context.actorSelection(moduleName.get)
    moduleActor ! AutoSavePlayer()
  }


  var heartIndex = 0
  def checkHeartbeat() = {
//    heartIndex = heartIndex + 1
//    if (heartIndex % 30 == 0){
      //30秒才执行一次
      val curTime = System.currentTimeMillis()  //心跳包检测时间，采用系统时间，其他相关时间采用游戏时间
      val detaTime = curTime - lastHeartbeat
      if(detaTime > 180 * 1000){
        val accoutName = ioSession.getAttribute(ActorDefine.PLAYER_ACTOR_NAME_KEY)
        log.warning("！！！警告！！！已经超过2分钟没有 心跳了 " + accoutName)
        ioSession.close(true)
      }
//      heartIndex = 0
//    }
//    context.actorSelection(moduleMap.get(ActorDefine.SYSTEM_MODULE_ID).get) ! EachSecondNotice()

  }

  //接受到网络事件，进行具体的网络逻辑处理
  def onReceiveNetMsg( netMsg : ReceiveNetMsg) ={
    //通过网络消息注册，来回调具体的逻辑方法
    val module = netMsg.request.getModule()
    val cmd = netMsg.request.getCmd()

    if(module == ProtocolModuleDefine.NET_M1 && cmd == ProtocolModuleDefine.NET_M1_C8888){
      lastHeartbeat = System.currentTimeMillis()  //心跳时间
      val response = Response.valueOf(ProtocolModuleDefine.NET_M1, ProtocolModuleDefine.NET_M1_C8888, M1.M8888.S2C.newBuilder().setServerTime(GameUtils.getServerTime).build())
      onSendNetMsgToClient(response)
      pushNetMsgToClient()
    }else{
      val moduleName = moduleMap.get(module)
      if(!moduleName.equals(None)){
        val moduleActor = context.actorSelection(moduleName.get)
        moduleActor ! netMsg
      }else{
        log.warning("模块未注册：" + module)
      }
    }
  }

  def onLoginSuccess(player : Player,gameProxy : GameProxy) ={
    ioSession.setAttribute(ActorDefine.PLAYER_IS_LOGIN_SUCCESS_KEY, true) //标记该玩家已经登录成功了
    this.player = player
    this.gameProxy = gameProxy
    launchAllLogicModules(gameProxy)
    var simplePlayer = new SimplePlayer()
    simplePlayer = GameUtils.player2SimplePlayer(player, simplePlayer)
    val playerProxy : PlayerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME)
    simplePlayer.setPlatform(playerProxy.getPlayerCache.getPlat_name)
    simplePlayer.setDevice(playerProxy.getPlayerCache.getModel)
    playerProxy.setSimplePlayer(simplePlayer)
    context.parent ! CreatePlayerActorSuccess(simplePlayer)  //通知playerService 创建角色成功咯
//    context.actorSelection(moduleMap.get(ActorDefine.SYSTEM_MODULE_ID).get) ! RefTimerSet() //通知系统模块刷新各个定时任务
    //写入登录成功日志
    loginLog()
  }

  //登录失败
  def onLoginFail(player : Player) ={
    player.finalize()
    ioSession.setAttribute(ActorDefine.PLAYER_IS_LOGIN_SUCCESS_KEY, false) //标记该玩家登录失败
    ioSession.close(true)  //直接把socket断掉
    context.parent ! CreatePlayerActorFail(player.getAccountName)
  }

  //强制踢下线
  def onKickPlayerOffline(rs : Int,reason:String) ={
    val response = Response.valueOf(ProtocolModuleDefine.NET_M1, ProtocolModuleDefine.NET_M1_C9998, M1.M9998.S2C.newBuilder().setRs(rs).setReason(reason).build())
    onSendNetMsgToClient(response)
    pushNetMsgToClient()
    ioSession.close(true)
  }

  var moduleMap : Map[Int, String] = Map()
  def registerModules() ={
    moduleMap += (ActorDefine.LOGIN_MODULE_ID -> ActorDefine.LOGIN_MODULE_NAME)
    moduleMap += (ActorDefine.ROLE_MODULE_ID -> ActorDefine.ROLE_MODULE_NAME)
    moduleMap += (ActorDefine.SYSTEM_MODULE_ID -> ActorDefine.SYSTEM_MODULE_NAME)
    moduleMap += (ActorDefine.SOLDIER_MODULE_ID -> ActorDefine.SOLDIER_MODULE_NAME)
    moduleMap += (ActorDefine.BATTLE_MODULE_ID -> ActorDefine.BATTLE_MODULE_NAME)
    moduleMap += (ActorDefine.DUNGEO_MODULE_ID -> ActorDefine.DUNGEO_MODULE_NAME)
    moduleMap += (ActorDefine.MAP_MODULE_ID -> ActorDefine.MAP_MODULE_NAME)
    moduleMap += (ActorDefine.ITEM_MODULE_ID -> ActorDefine.ITEM_MODULE_NAME)
    moduleMap += (ActorDefine.BUILD_MODULE_ID -> ActorDefine.BUILD_MODULE_NAME)
    moduleMap += (ActorDefine.TECHNOLOGY_MODULE_ID -> ActorDefine.TECHNOLOGY_PROXY_NAME)
    moduleMap += (ActorDefine.TROOP_MODULE_ID -> ActorDefine.TROOP_MODULE_NAME)
    moduleMap += (ActorDefine.SKILL_MODULE_ID -> ActorDefine.SKILL_MODULE_NAME)
    moduleMap += (ActorDefine.EQUIP_MODULE_ID -> ActorDefine.EQUIP_MODULE_NAME)
    moduleMap += (ActorDefine.CHAT_MODULE_ID -> ActorDefine.CHAT_MODULE_NAME)
    moduleMap += (ActorDefine.LOTTER_MODULE_ID -> ActorDefine.LOTTER_MODULE_NAME)
    moduleMap += (ActorDefine.MAIL_MODULE_ID -> ActorDefine.MAIL_MODULE_NAME)
    moduleMap += (ActorDefine.FRIEND_MODULE_ID -> ActorDefine.FRIEND_MODULE_NAME)
    moduleMap += (ActorDefine.TASK_MODULE_ID -> ActorDefine.TASK_MODULE_NAME)
    moduleMap += (ActorDefine.ARENA_MODULE_ID -> ActorDefine.ARENA_MODULE_NAME)
    moduleMap += (ActorDefine.RANKS_MODULE_ID -> ActorDefine.RANKS_MODULE_NAME)
    moduleMap += (ActorDefine.ARMYGROUP_MODULE_ID -> ActorDefine.ARMYGROUP_MODULE_NAME)
    moduleMap += (ActorDefine.ACTIVITY_MODULE_ID -> ActorDefine.ACTIVITY_MODULE_NAME)
    moduleMap += (ActorDefine.CDKEY_MODULE_ID -> ActorDefine.CDKEY_MODULE_NAME)
    moduleMap += (ActorDefine.SHARE_MODULE_ID -> ActorDefine.SHARE_MODULE_NAME)
    moduleMap += (ActorDefine.CAPACITY_MODULE_ID -> ActorDefine.CAPACITY_MODULE_NAME)
    moduleMap += (ActorDefine.ADVISER_MODULE_ID -> ActorDefine.ADVISER_MODULE_NAME)
    moduleMap += (ActorDefine.NEW_BUILD_MODULE_ID -> ActorDefine.NEW_BUILD_MODULE_NAME)
  }

  //开始登陆模块，进行校验，校验完成后，才会启动所有的模块
  def startLoginModule() ={

    context.watch(context.actorOf(LoginModule.props(areaId), ActorDefine.LOGIN_MODULE_NAME))
  }

  //启动所有的模块
  def launchAllLogicModules(gameProxy : GameProxy) ={
//    gameProxy.registerProxy(ActorDefine.PLAYER_PROXY_NAME, new PlayerProxy(this.player));
    context.watch(context.actorOf(RoleModule.props(gameProxy), ActorDefine.ROLE_MODULE_NAME))
    context.watch(context.actorOf(SystemModule.props(gameProxy), ActorDefine.SYSTEM_MODULE_NAME))
    context.watch(context.actorOf(SoldierModule.props(gameProxy), ActorDefine.SOLDIER_MODULE_NAME))
    context.watch(context.actorOf(BattleModule.props(gameProxy), ActorDefine.BATTLE_MODULE_NAME))
    context.watch(context.actorOf(DungeoModule.props(gameProxy), ActorDefine.DUNGEO_MODULE_NAME))
    context.watch(context.actorOf(MapModule.props(gameProxy), ActorDefine.MAP_MODULE_NAME))
    context.watch(context.actorOf(ItemModule.props(gameProxy), ActorDefine.ITEM_MODULE_NAME))
    context.watch(context.actorOf(BuildModule.props(gameProxy), ActorDefine.BUILD_MODULE_NAME))
    context.watch(context.actorOf(TechnologyModule.props(gameProxy), ActorDefine.TECHNOLOGY_MODULE_NAME))
    context.watch(context.actorOf(TroopModule.props(gameProxy), ActorDefine.TROOP_MODULE_NAME))
    context.watch(context.actorOf(SkillModule.props(gameProxy),ActorDefine.SKILL_MODULE_NAME))
    context.watch(context.actorOf(EquipModule.props(gameProxy),ActorDefine.EQUIP_MODULE_NAME))
    context.watch(context.actorOf(ChatModule.props(gameProxy),ActorDefine.CHAT_MODULE_NAME))
    context.watch(context.actorOf(LotterModule.props(gameProxy),ActorDefine.LOTTER_MODULE_NAME))
    context.watch(context.actorOf(MailModule.props(gameProxy),ActorDefine.MAIL_MODULE_NAME))
    context.watch(context.actorOf(FriendModule.props(gameProxy),ActorDefine.FRIEND_MODULE_NAME))
    context.watch(context.actorOf(TaskModule.props(gameProxy),ActorDefine.TASK_MODULE_NAME))
    context.watch(context.actorOf(ArenaModule.props(gameProxy),ActorDefine.ARENA_MODULE_NAME))
    context.watch(context.actorOf(PowerRanksModule.props(gameProxy),ActorDefine.RANKS_MODULE_NAME))
    context.watch(context.actorOf(ArmyGroupModule.props(gameProxy),ActorDefine.ARMYGROUP_MODULE_NAME))
    context.watch(context.actorOf(ActivityModule.props(gameProxy),ActorDefine.ACTIVITY_MODULE_NAME))
    context.watch(context.actorOf(CdkeyModule.props(gameProxy),ActorDefine.CDKEY_MODULE_NAME))
    context.watch(context.actorOf(ShareModule.props(gameProxy),ActorDefine.SHARE_MODULE_NAME))
    context.watch(context.actorOf(CapacityModule.props(gameProxy),ActorDefine.CAPACITY_MODULE_NAME))
    context.watch(context.actorOf(AdviserModule.props(gameProxy),ActorDefine.ADVISER_MODULE_NAME))
    context.watch(context.actorOf(NewBuildModule.props(gameProxy),ActorDefine.NEW_BUILD_MODULE_NAME))
    getModuleRef(ActorDefine.ROLE_MODULE_NAME) ! InitSendRoleInfo()  //模块全部启动完毕，发送角色信息 通知客户端 进入游戏
//    getModuleRef(ActorDefine.CAPACITY_MODULE_NAME) ! LoginInitCapacity()//启动的时候重算一下战斗力
  }

  val pushList : java.util.List[Response]  = new util.ArrayList[Response]()
  def onSendNetMsgToClient( response: Response) ={
//    ioSession.write(response)
    pushList.synchronized{
      pushList.add(response)
    }
  }

  def pushNetMsgToClient()={
    var _pushList : java.util.List[Response]  = null
    pushList.synchronized{
      if(pushList.size() > 0){
        _pushList = new util.ArrayList[Response](pushList)
        pushList.clear()
      }
    }
    if (_pushList != null){
      ioSession.write(_pushList)
    }
  }

  def onSendNetMsg( response: Response) ={
    responseLists.synchronized{
      responseLists.add(response)
    }
  }

  def onMulticastNetToClient() ={
    responseLists.synchronized{
      if(responseLists.size() > 0){
        ioSession.write(new util.ArrayList[Response](responseLists))
        responseLists.clear()
      }
    }
  }

  def onAddWorldBuildingSuccess(msg : AddWorldBuildingSuccess) ={
    player.setBuildingId(msg.buildingId)
    getModuleRef(ActorDefine.MAP_MODULE_NAME) ! BuildPointNotify(msg.buildingId,msg.x,msg.y)
  }

  def getModuleRef(moduleName : String) ={
    context.actorSelection(moduleName)
  }

  def loginLog() ={
    if(this.player != null){
      val playerLogin = new PlayerLogin
      playerLogin.setAccountName(player.getAccountName)
      playerLogin.setAreaId(player.getAreaId)
      playerLogin.setName(player.getName)
      playerLogin.setId(player.getId)

      sendMsgLogServer(context, SendLog(playerLogin) )


    }
  }

  def onModuleSendLog(log : BaseLog) ={
    sendMsgLogServer(context, SendLog(log) )
  }

  def onSendAdminLogToService(log : BaseLog, actionType : Int, key : String, value : Long) ={
    sendAdminLogToService(context, SendAdminLog(log, actionType, key, value) )
  }

  //通知到service
  def tellService(serviceName: String, msg: AnyRef) = {
    context.actorSelection("../../" + serviceName) ! msg
  }

}
