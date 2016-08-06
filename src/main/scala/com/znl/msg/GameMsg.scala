package com.znl.msg

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorRef
import akka.util.ByteString
import com.google.protobuf.GeneratedMessage
import com.znl.base.{BaseSetDbPojo, BaseLog, BaseDbPojo}
import com.znl.core._
import com.znl.framework.http.{HttpMessage, HttpRequestMessage}
import com.znl.pojo.db._
import com.znl.proto.Common.FightElementInfo
import com.znl.proto.M19.M190000
import com.znl.proto.M2.M20400
import com.znl.proto.M22.LegionMemberInfo
import com.znl.proto.M3.{TimeInfo, M30000}
import com.znl.proto._
import com.znl.proxy.GameProxy
import com.znl.service.map.TileType.TileType
import com.znl.service.map.{TileBattleResult, WorldTile, WorldBlock}
import com.znl.service.trigger.TriggerEvent
import com.znl.template.{ChargeTemplate, ReportTemplate, MailTemplate}
import org.apache.mina.core.session.IoSession
import com.znl.framework.socket.{Response, Request}
/**
 * Created by woko on 2015/10/7.
 */
object GameMsg {

  ////////////////AdminServer相关消息类型///////////////////////
  final case class AdminMessageReceived(http : HttpMessage)  //接受到管理平台数据
  final case class AdminActionMessage(http : HttpMessage)  //管理接口的具体行为消息
  final case class AdminActionMessageToService(http : HttpMessage, msg : AnyRef) //将管理行为信息发送到具体的Service
  final case class AdminActionMessageToServiceBack(http : HttpMessage, result : String) //行为操作返回

  ////////////////////////////pushServer///////////////////////////////////////
  final case class PushMsgToPlayerDevice(channelId : String, title : String, msg : String, time : Int) //将对应的消息发送到玩家设备去

  ///////////////////////GateServer相关消息类型
  final case class SessionOpen(ioSession: IoSession)  //网络链接 连接
  final case class SessionMessageReceived(ioSession: IoSession, request: Request) //接受到网络消息
  final case class SessionClose(ioSession: IoSession) //网络链接关闭
  final case class AutoClearOffPlayerData()  //定时清楚下线数据玩家
  final case class AutoSaveOffPlayerData(accountName : String)  //自动保存下线的玩家数据

  /////////////////arenaService/////////


  /////////////////playerService/////////
  final case class SendPlayerNetMsg(accountName : String, request: Request) //发送网络数据给具体player actor
  final case class CreatePlayerActor(accountName : String, areaId : Int, ioSession: IoSession) //创建player actor
  final case class ReplacePlayerSession(accountName : String, ioSession: IoSession)  //重连，替换掉玩家的session
  final case class StopPlayerActor(accountName : String)  //停止player actor
  final case class CreatePlayerActorSuccess(simplePlayer : SimplePlayer)  //创建玩家成功
  final case class CreatePlayerActorFail(accountName : String) //创建玩家失败，被禁号等原因
  final case class StopPlayerActorSuccess(id : Long)  //成功停止玩家，则表示下线
  final case class GetPlayerIsOnline(id : Long)  //判断玩家是否在线
  final case class GetPlayerSimpleInfo(id : Long,cmd : String) //获取玩家数据
  final case class GetPlayerSimpleInfoByRoleName(roleName : String, cmd : String) //
  final case class GetPlayerSimpleInfoListByRoleNameList(roleName : java.util.ArrayList[String], cmd : String)
  final case class GetPlayerSimpleInfoListByIds(ids :  java.util.Set[java.lang.Long],cmd : String)
  final case class GetPlayerSimpleInfoSuccess(simplePlayer: SimplePlayer,cmd : String) //获取玩家数据成功
  final case class GetPlayerSimpleInfoListSuccess(simplePlayer: util.List[SimplePlayer],cmd : String)
  final case class GetPlayerIdByName(name: String,typeId : Int )  //通过名字获得玩家id
  final case class BanPlayerHandle(banType : String, dataList : java.util.List[String], status : Int, banDate : Int, reason : String) //玩家封禁操作
  final case class AdminKickPlayerOffline(sendType : String, dataList : java.util.List[String], kickAll : Int, reason: String)
  final case class KickPlayerOffline(rs : Int, reason : String)  //强制踢玩家下线
  final case class BanPlayerChat(time : Int,status : Int)
  final case class AdminInstructorGM(sendType : String, dataList : java.util.List[String], optType : Int, instructorType : Int, startTime : Int, endTime : Int)
  final case class InstructorGM(instructorType : Int, startTime : Int, endTime : Int)
  final case class UpdateOnlineSimplePlayer(simplePlayer: SimplePlayer)  //更新一些关键的在线玩家信息

  final case class UpdateSimplePlayer(simplePlayer: SimplePlayer)  //更新一些玩家的信息，在线跟不再线
  final case class UpdateSimplePlayerDefendTroop(id: Long,troop : PlayerTroop)  //更新一些玩家的信息，在线跟不再线
  final case class GetPlayerIdByNameSucess(playerId: Long,typeId : Int)  //通过名字获得玩家id返回


//  final case class AskPlayerSimpleInfoList(ids :  java.util.Set[java.lang.Long]) //获取自己好友的信息列表

//  final case class AskOnePlayerSimplePlayer(id :  Long) //获取某个玩家的信息

  final case class ClearOfflineSimplePlayerTrigger()   //定时清除离线玩家数据
  final case class StopServerSaveSimplePlayer()  //关服时把玩家的在线和离线数据清除并保存
  final case class CheckSaveSimplePlayerDone() //检查保存是否完成

  final case class EachHourNotice()//每小时定时
  final case class EachMinuteNotice()//每分钟定时
  final case class EachSecondNotice()//每分钟定时
  //final case class FixedTimeNotice(timeFlag:Int)//定时器
  final case class ActivityRankTrigger(ids:util.List[java.lang.Integer])

  final case class Reload()
  //////////////////////MailService相关消息类型
  final case class SendMail(players : java.util.Set[java.lang.Long],mailTemplate : MailTemplate,senderName : String,senderId : Long)
  final case class ReceiveMailNotice(mailTemplate : MailTemplate)
  final case class ReceiveReportNotice(report : Report)
  final case class ReceiveArenaReportNotice(report : Report)
  final case class SendReport(reportTemplate : ReportTemplate,battleId : Long)
  final case class GetSpyReportNotify(id :Long)
  //////////////////////worldService世界地图相关消息/////////////////////////////
  final case class CompleteInitWorldBlock(worldBlock : WorldBlock)  //初始化完成世界块
  final case class UpdateWorldBlock(worldBlock : WorldBlock)  //更新世界块信息
  final case class AddWorldBuildingToTile(playerId : Long, accountName : String)  //添加世界建筑到格子

  final case class AutoAddBuilding(playerId : Long, accountName : String)  //系统达到一定层度，自动添加建筑
  final case class AddWorldBuildingSuccess(playerId : Long, accountName : String, buildingId : Long,x : Int, y : Int)  //添加建筑成功 通知到具体的玩家

  final case class WatchBuildingTileInfo(x : Int, y : Int)  //查看x y周围的各种信息
  final case class WatchBuildingTileInfoBack(list : java.util.List[WorldTile]) //返回查看的相关信息列表
  final case class AskBuildTitle(x:Int ,y :Int,sortId :Int)
  final case class CollectMsg(collinfo : M8.CollectInfo)
  final case class Watchmagnifying(x : Int, y : Int,playerid : Long)   //放大镜查看
  final case class Watchmagnifyingback(list : java.util.List[WorldTile],x : Int, y : Int)   //放大镜查看返回

  final case class CollectBack(collinfo : M8.CollectInfo , ower :Int,rs : Int)
  final case class FightBuild(x : Int, y : Int,attackTeams : util.List[PlayerTeam],attackX : Int,attackY : Int,attIcon : Int,attLevel : Int,attName : String,legionId : Long,powerMap: util.Map[Integer, java.lang.Long] )//请求一场世界战斗
  final case class FightBuildErrorBack(time : Int,attackTeams : util.List[PlayerTeam],rs:Int)//世界战斗请求返回
  final case class PushWorldFightToNode(attackTeams : util.List[PlayerTeam],attackX : Int,attackY : Int,fightTime : Long,attackSort : Int ,powerMap: util.Map[Integer, java.lang.Long])
  final case class FightBuildResult(attackTeams : util.List[PlayerTeam],boomAdd : Int,honner:Int,result:Boolean)//返回出战队列给玩家模块计算伤兵
  final case class DefendBuildResult(defendTeams : util.List[PlayerTeam],rewardMap : util.HashMap[Integer,Integer],boomReduce : Int,honner : Int)//返回防守队列
  final case class BuildPointNotify(id : Long,x :Int,y : Int)//推送坐标通知
  final case class BuildBattleResult(result : TileBattleResult)
  final case class FightTeamBack(attackTeams : util.List[PlayerTeam],getBackTime:Long,targetX : Int,targetY:Int,targetSortId:Int,result : TileBattleResult)//战斗结束队伍返回
  final case class BuildBattleEndBack(result : TileBattleResult)//世界请求一次战斗的所有逻辑结束返回
  final case class DigTeamBackNotice(result : TileBattleResult)
  final case class DigTeamCallBack(targetX : Int,targetY:Int,targetSortId:Int)
  final case class DetectBuild(targetX : Int,targetY:Int,detectType:Int,playerId:Long) //侦查坐标
  final case class DetectPriceBack(targetX : Int,targetY:Int,price : Int,id:Long)//返回坐标的侦查价格
  final case class DetectResultBack(rs : Int)
  final case class AskNodeResourceGet()
  final case class AskNodeDefendGet()
  final case class GetReportInfo(report: ReportTemplate)
  final case class DefendResourceFight(x : Int, y : Int,teams : util.List[PlayerTeam],iswin: Boolean,load :Long)
  final case class BeAttackedNotify(teamNotice : TeamNotice)  //被攻击提示
  final case class RemoveBeAttackedNotify(x : Int, y : Int,time :Long)
  final case class RemoveBeAttackedNotifyBytime(time :Long)
  final case class RemoveBeAttackedNotifyByXY(x : Int, y : Int)
  final case class PushHelpToNode(attackTeams : util.List[PlayerTeam],attackX : Int,attackY : Int,fightTime : Long,attackSort : Int,powerMap: util.Map[Integer, java.lang.Long])
  final case class Delhelper(helpId : Long)
  final case class Deltohelp(helpId : Long)
  final case class changeformTask(x :Int ,y :Int ,time :Long)
  final case class tellHasArried(time :Long)
  final case class DelformTask(targetX : Int,targetY: Int,time : Long)
  final case class changefightTeam(timeer: Long, teams: util.List[PlayerTeam])
  final case class finishResouceTask(lv: Int)
  final case class SendTeamTaskInfo()
  final case class DeleteDiggingTask(x :Int ,y :Int)
  final case class CheckDeleteDiggingTask(playerId : Long)

  final case class CreateAttackTask(x : Int, y : Int,attackTeams: util.List[PlayerTeam],time : Long,defendName : String,level:Int,tileType:TileType,starX:Int ,starY :Int)
  final case class CreateDiggingTask(x : Int, y : Int,attackTeams: util.List[PlayerTeam],time : Long,defendName : String,level:Int,product : Int,beginTime : Long ,starX:Int ,starY :Int)
  final case class CreateReturnTask(x : Int, y : Int,attackTeams: util.List[PlayerTeam],time : Long,defendName : String,level:Int ,starX:Int ,starY :Int)
  final case class RefTaskListNotify()

  final case class CallBackTask(x : Int, y : Int,playerId : Long ,time : Long,taskType : Int,product :Long)
  final case class CallBackTaskBack()
  final case class StopNodeTeamBack(teams : util.List[PlayerTeam],rewardMap : util.HashMap[Integer,Integer])
  final case class StopAllNode()
  final case class SaveCloseNodeResource(playerId:Long,rewardMap : util.HashMap[Integer,Integer])
  final case class AllNodeTeamBack()
  final case class MoveWorldBuild(targetX : Int,targetY :Int,myX :Int,myY :Int)
  final case class MoveWorldBuildBack(x : Int,y :Int,rs :Int)
  final case class MoveRandomWorldBuild(playerId: Long, myX: Int, myY: Int)//随机迁城令
  final case class MoveRandomWorldBuildBack(x: Int, y: Int)
  final case class GetRandomEmpty()
  final case class GetHelpDefendTime(myX : Int,myY : Int,targetX : Int,targetY :Int,legionId : Long,powerMap: util.Map[Integer, java.lang.Long])
  final case class Tohelp(myX : Int,myY : Int,targetX : Int,targetY :Int,legionId : Long,fightteam: util.List[PlayerTeam],myId:Long,powerMap: util.Map[Integer, java.lang.Long])
  final case class HelpDefendError(rs : Int)
  final case class tohelpError(rs : Int)
  final case class HelpDefend(myX : Int,myY : Int,targetX : Int,targetY :Int,attackTeams: util.List[PlayerTeam])
  final case class HelpDefendback(myX : Int,myY : Int,targetX : Int,targetY :Int,time:Long)
  final case class tohelpbackSucess(x : Int, y : Int,attackTeams: util.List[PlayerTeam],time : Long,defendName : String,level:Int ,teamId : Long ,starX:Int ,starY :Int)
  final case class tellforhelp(x : Int, y : Int,attackTeams: util.List[PlayerTeam],time : Long,defendName : String,level:Int,teamId : Long,icon  :Int ,starX:Int ,starY :Int,powerMap: util.Map[Integer, java.lang.Long])

  final case class GiveMeAPill(x : Int, y : Int)

  //////////////////////////friendService好友服务////////////////////////////////////////////////////
  final case class WatchFriendInfoList(friendSet: util.Set[java.lang.Long], beBlessIdSet : util.Set[java.lang.Long])  //查看自己的好友列表信息
  final case class WatchFriendInfoListBack(friendList : util.List[SimplePlayer], beBlessList : util.List[SimplePlayer])   //返回好友的简要信息列表

  final case class FriendBlessPlayers(blesser : Long, players : util.List[java.lang.Long])   //blesser祝福者 祝福玩家
  final case class AcceptFriendBless(playerId : Long)  //接受到某个好友的好友祝福

  //////////////////////////battleReportService战报服务////////////////////////////////////////////////////
  final case class AddMailBattleProto(reportTemplate : ReportTemplate,cmd : String)//添加一个战斗包到服务，
  final case class GetBattleProto(id : Long, cmd : String)
  final case class GetBattleProtoSuccess(msg : GeneratedMessage,cmd : String)
  final case class addNewReport(id : Long)
  ///////////////////logService/日志服务/////////////////////
  final case class SendLog(obj : BaseLog) //发送日志
  final case class ModuleSendLog(obj : BaseLog) //模块发送日志
  final case class ModuleSendAdminLog(obj : BaseLog, actionType : Int, key : String, value : Long) //模块发送管理日志
  final case class SendAdminLog(obj : BaseLog, actionType : Int, key : String, value : Long, logAreaKey : String = "") //模块发送管理日志

  /////////////////net/////////////////
  final case class ReceiveNetMsg(request: Request) //player actor接受到网络数据
  final case class SendNetMsgToClient(response: Response) //player actor发送网络消息直接到客户端
  final case class PushtNetMsgToClient()//推送协议到客户端
  final case class SendNetMsg(response: Response) //模块发送消息结构体到playerActor
  final case class MulticastNetToClient() //将缓存在playerActor上的协议消息体一起发送到客户端
  ////////////////////////////////////////////////

  ////////////////charge/////////////////////////////
  final case class ChargeToPlayer(chargeTemplate : ChargeTemplate,http : HttpMessage,actor :ActorRef)
  final case class ChargeToPlayerDone(http : HttpMessage, result : String)
  final case class notitySomeOneCharge()
  ////////////////////db////////////////////////////////////////////////
  final case class SaveDBPojo(pojo : BaseDbPojo)
  final case class DelDBPojo(pojo : BaseDbPojo)  //将DB数据删除，这里需要做一级日志，确保出线问题可以还原
  final case class CreateDBPojo(pojoClass : Class[_])
  final case class GetDBPojo(id : Long, pojoClass : Class[_])
  final case class FinalizeDbPojo(pojo : BaseDbPojo)  //释放掉对应的DB缓存
  final case class TriggerDBAction()   //DB行为队列处理 保存 删除
  final case class TriggerClearOfflineData()
  final case class IsDbQueueEmpty()  //判断现在的BD入库队列是否为空
  final case class LoadMysql(data :util.List[DbOper])  //读取列表到mysql
  final case class LoadMysqlDic(dbOper : DbOper)
  final case class LoadDone(id : Int)
  final case class WriteServerOpen(areaKey : String)

//  final case class InitSetDbPojo(setDbPojo: BaseSetDbPojo)  //初始化 areaKey已经赋值了
  final case class UpdateSetDbPojoElement(setDbPojo: BaseSetDbPojo, key : String, value : Long)  //更新Set的元素数据
  final case class DelSetDbPojoElement(setDbPojo: BaseSetDbPojo, key : String)  //删除Set的元素

  final case class CreateProtoGeneratedMessage(cmd : Int, msg : GeneratedMessage, expire : Int)  //保存协议消息
  final case class GetProtoGeneratedMessage(cmd : Int, id : Long)   //获取消息协议体

  /////////////////////////////////////
  final case class GetAllArmygroupids(arenKey : String)            //获得军团ids  --处理 未验证， 验证getAllValue接口有没有问题
  final case class AddArmygroupid(id : Long , arenKey : String)     //增加军团  --处理 未验证
  final case class RemoveArmygroupid(id : Long , arenKey : String) //移除军团  --处理 未验证

  final case class GetPlayerByAccountName(name:String, areaKey : String)  //通过账号名称 获取角色ID  --处理
  final case class AddPlayerByAccountName(name : String, id : Long, areaKey : String) //添加角色ID  --处理

  final case class AddRoleName(roleName : String, playerId : Long, areaKey : String)  //添加角色名称到库 携带玩家id  --处理
  final case class IsRepeatRoleName(roleName : String, areaKey : String)  //角色名是否重复  --处理
  final case class GetPlayerIdByRoleName(roleName : String, areaKey : String)  //通过角色名称获取玩家ID  --处理

  final case class AddBillOrder(orderId : String, playerId : Long)  // --已处理，待验证
  final case class IsRepeatBillOrder(orderId : String)    // --已处理，待验证

  final case class GetAllWorldBuildingKey(areaKey : String)  //获取所有的一个人的建筑的KEY列表  --处理
  final case class AddWorldBuildingToDb(buildingTileKey : String, id: Long, areaKey : String) //添加建筑到全局DB，用来记录有多少个建筑了  --处理
  final case class GetWorldBuildingsIdByTileKey(buildingTileKeySet : java.util.List[String], areaKey : String) //获取一组数组的key值  --处理

  final case class GetAllArenaRank(areaKey : String)                            //server  --处理 未验证
  final case class AddArenaRank(playerId : Long,rankValue : Int,areaKey : String)  //保存单个竞技场数据  --处理 未验证
  final case class AddArenaRankList(ranks : util.List[ArenaRank])   //保存竞技场数据  --处理 未验证 需要做一层优化 列表更新
  final case class onRemoveArenaRank(playerId : Long,rankValue : Int,areaKey : String)  //删除单个竞技场数据 -- 消息被注释了，如果还要删除的话，用removeKey接口

  final case class addTeamDate(team : PlayerTroop, areaKey: String, playerId: Long, teamType : Int)//添加或刷新玩家的竞技场对象 --已处理 待验证
  final case class getTeamDate(areaKey: String,playerId: Long,teamType : Int)//获取玩家竞技场缓存  --已处理 待验证

  final case class RefArenaLastTimeRankList(ranks : util.List[ArenaRank])//刷新竞技场上次排行（定时刷新） --已处理 待验证
  final case class GetAllArenaLastTimeRankList(areaKey : String)//获取所有上次排行数值 --已处理 待验证

  //rank排行榜
  final case class GetAllRankByType(areaKey : String,rankType : Int)   // --已处理，待验证
  final case class AddPlayerIntoRank(playerId : Long,rankValue : Long,areaKey : String,rankType : Int)  // --已处理 待验证
  final case class UpdateAllRankByType()   //没有使用，待校验
  final case class getMyRanks(playerId:Long)
  final case class getMyRanksback(map: util.Map[java.lang.Integer,java.lang.Integer])
  final case class changeRankBytype(retype:Int , value:Int)

  //mysql
  final case class InsertToMysql(table : String, id : Long, logAreaId : Int)  //将新的创建的ID插入到MySQL
  final case class UpdateToMysql(table : String, id : Long, json : String, logAreaId : Int) //将数据结构转成json 更新到mysql
  final case class UpdateSetToMysql(table : String,key : String,value : Long, logAreaId : Int)//保存set结构
  final case class DelToMysql(table : String, id : Long, logAreaId : Int) //通过id 删除数据
  final case class QueryToMysql(sql : String) // 向mysql查询数据
  final case class TriggerExecuteToMysql() // 触发运行到MySQL数据库

  ////////////////////////////////////////////////////////


  //////////////////////arena//////////////////////////////////
  final case class GetSimplePlayerBysection(rivalId :java.lang.Long ,playerId :java.lang.Long ,cmd :String,battle : PlayerBattle)
  final case class GetAllArenaFromMoudle(cmd : String)                    //模块获得竞技场数据
  final case class ChangeArenaRank(ranks : util.List[ArenaRank])  //改变竞技场排名
  final case class RestArena(feis : M7.FormationInfo,genel : Int, playerId:Long , arenaKey: String )  //重置竞技场数据
  final case class askChangeInfo()
  final case class getLaskRank(playerId :Long)  //获得上期排名
  final case class sendAreaInfo()  //模块推送
  final case class askFight(rivalRank : Integer ,playerId :java.lang.Long )//竞技场请求挑战
  final case class getArenaRank(playeridlist : util.List[java.lang.Long] , cmd : String)//请求竞技场排名

  final case class getArenaRankBack(rankmap : util.Map[java.lang.Long,Integer], cmd : String)//请求竞技场排名返回
  final case class RestArenaSuceess(feis : M7.FormationInfo,genel : Int ,arenMap: util.Map[java.lang.Long,Integer],team:PlayerTroop ,simplePlayer: SimplePlayer)  //重置竞技场数据成功返回
  final case class GetAllArenaRankSuceess(arenMap: util.Map[java.lang.Long,Integer], cmd :String )
  final case class GetAllArenaInfos(playerId: Long, cmd :String)
  final case class GetAllArenaInfosSucess(build: M20.M200000.S2C.Builder, cmd :String ,rivals : java.util.List[Integer],arenMap: util.Map[java.lang.Long,Integer],simplePlayers: util.List[SimplePlayer],rs : Int)
  final case class test(retype : Int)
  final case class getLaskRankSucess(rank :Int)  //获得上期排名成功
  final case class changeSnucess()  //改变排名成功
  final case class GetArenaRankMap(build: M21.M210000.S2C)  //获得竞技场排行数据
  final case class GetArenaRankInfos()  /////////////////////m -> playerActor//////////////////////////////////
  final case class LoginSuccess(player : Player, gameProxy: GameProxy)  //玩家登陆成功
  final case class LoginFail(player: Player) //玩家登陆失败
  final case class AutoSavePlayer()
  final case class CheckHeartbeat() //检测心跳包
//  final case class SystemTimer()  //定时器时间
  final case class NewArenaReportNotify()
  final case class GetWinTimesReward(simplePlayer: util.List[SimplePlayer],cmd : String,battle :PlayerBattle)
  final case class SendFormationToClient()//刷新阵型
  final case class CheckBaseDefendFormation()//检查阵型是否需要刷新
  final case class AddServerArenaReport(report : Report)
  final case class GetAllServerArenaReport(cmd :Int)
  final case class GetAllServerArenaReportBack(reports : util.List[Report],cmd :Int)
  final case class GetOneServerArenaReport(id : Long)
  final case class GetOneServerArenaReportBack(report : Report)
  final case class AutoPushToArena()
  final case class RefTimerSet(initType : Int)

  ////////////////////////task////////////////////////////////
  final case class RefeshTask()  // 任务刷新
  final case class RefeshTaskUpdate(build :M190000.S2C.Builder,reward :PlayerReward)  // 任务刷新
  final case class RefeshItemBuff()  // buff刷新
  final case class ReshEveryDay()  // 每天登陆刷新
  final case class RefreshActivity() // 活动每天刷新
  final case class RefreshLaba() // 刷新拉霸
  final case class RefreshBlessState() // 祝福刷新

  ////////////////////////build////////////////////////////////
  final case class BuildInfo(infos:util.List[util.List[Int]], firstInit: Boolean,m30000:M3.M30000.S2C )  //建筑信息
  final case class ReshBuildings()  //刷新建筑信息
  final case class BuildTimer(cn : Int , cmd : Int ,obj : Object, powerlist: util.List[Integer],buildType : Int,index :Int )  //建筑定时器
  final case class BuyBuildSite()  //升级建筑时，如队列已满，但可以购买时，提示购买
//  final case class CheckAllTimerAndSend30000()  //检查所有定时器并发送30000
  ////////////////////////admin////////////////////////////////
  final case class GMCommand(command : String)

  ////////////////////server///////////////////////////
  final case class OnServerTrigger()

  ////////////////chat/////////////////////
  final case class GetChatTimer()
  final case class GetChat(playerIndex : Int,accoutName : String)
  final case class GetLegionChat(playerIndex : Int,accoutName : String,legionId : Long)
  final case class AddChat(playerChat: PlayerChat)
  final case class SendChatToPlayer(accoutName : String,chats : util.ArrayList[PlayerChat],chattype : Int,index : Int)
  final case class GetSystemChatIndex(index : Int,chatType : Int)
  final case class PrivateChatHandle(playerChat: PlayerChat)
  final case class CreateLegionChatNode(legionId : Long)
  final case class JoinLegionNotify()
  final case class LeaveLegionNotify()
  final case class trumpeNotity(playerId:Long,name:String,mess:String,retype:Int)
  final case class sendAChat(chat :PlayerChat)
  //////////////////trigger//service///////////////////////////
  final case class AddTriggerEvent( triggerEvent: TriggerEvent ) //添加触发倒计时
  final case class RemoveTriggerEvent(triggerEvent: TriggerEvent)

  ////////////////////battle///////////////////////////
  final case class ReqPuppetList(message : AnyRef)
  final case class ServerBattleEndHandle(battle : PlayerBattle)//SERVER_BATTLE_END_HANDLE
  final case class ClientEndHandle(battleId: Int)
  final case class EndBattle(battle : PlayerBattle)
  final case class PackPuppet(puppet : M5.PuppetAttr)
  final case class ErrorBattle(rs : Int)
  //final case class addPuppetList(acname:String,battleType: Int, eventId: Int, cmd: Int, team: util.List[PlayerTeam], saveTraffic: Int)//获取怪物信息(军团副本)
  ///////////////////soldier//////////////////////
  final case class FixSoldierList()

  //////////////role////////////////////
  final case class InitSendRoleInfo()  //playerActor初始化发送角色信息
  final case class CreateWorldBuild()
  final case class RefrshTip()
  final case class addAtivity(conditionType: Int, value: Int, expandCondition: Int)
  final case class refreshengry()
  final case class refreshLegionDungeoTimes(playerid:Long,dungeoinfo:util.Map[Integer, util.List[PlayerTeam]])//刷新玩家军团副本挑战次数


  /////////////dungeo//////////////////////
  final case class AutoFightDungeo(dungeoType : Int,eventId : Int,fightElementInfos : util.List[Common.FightElementInfo])
  final case class getLimitChangetInfo(builder : M6.M60100.S2C.Builder,playerId : Long,dungId :Int)
  final case class getLimitChangetInfoBack(builder : M6.M60100.S2C.Builder)
  final case class AddLimitchangeBattleProto(msg : GeneratedMessage,dungeoOrder:Int)
  final case class AddLimitchangeBattleProtoBack(id :Long,dungeoOrder:Int)
  final case class AddLimitchangeNearList(id:Long,dungeoOrder:Int,playerId : Long)
  final case class getLimitChangeInfo()

  ///////////////////RankList///////////////////////
  //rank排行榜
  final case class GetAnRankByType(rankType : Int)
  final case class AddPlayerToRank(playerId : Long,value : Long,rankType : Int)
  final case class UpdateAllRanksByType()
  final case class GetAnRankMessageByType(build: M21.M210000.S2C,rankType : Int)
  final case class ActivityTimer()
  final case class RefLevelRank()
  final case class PlayerSetContributeRank(Activitycontributerank:Int)
  ///////////////////RankList///////////////////////


  ///////////////////ArmyGroup///////////////////////
  final case class createArmyArmyGroup(name : String , joinType : Int ,way : Int)//创建
  final case class AddArmyGroup(armytemp : Armygroup)
  final case class getArmyGroupByid(appId : Long ,  cmd : Int)  // 获得某个军团
  final case class TechExpandPowerMap()  // 获得军团科技属性加成
  final case class ArmyGroupShop(playerId : Long , itemId : Int,opt : Int,typeId : Int)  // 军团商店贡献值兑换
  final case class getAllArmyGroup(obj : Object,cmd : Int)  // 获得全部军团信息
 /* final case class getArmyGroupDungeoInfo(dungeoid:Int)  // 获得军团副本信息(dungeoid 为章节)
  final case class ArmyGroupDungeoInfo(chapter:Int,dungeoinfo:util.Map[Integer, util.List[PlayerTeam]])
  final case class returnarmygroupinfos(battleType: Int, eventId: Int, cmd: Int, team:util.List[PlayerTeam],saveTraffic: Int,infoslist:util.List[PlayerTeam])//返回军团副本信息
  final case class iscanattackArmyGroupDungeo(dungeoid:Int)//判断是否可打
  final case class iscanattackArmyGroupDungeore(flag:Boolean) //返回判断结果
  final case class changeArmyGroupDungeoInfo(sort:Integer) //每日通关副本更新
  final case class allarmygroupdungeoinfo()//270000上线初始信息
  final case class reallarmygroupdungeoinfo(dungeoinfo:util.Map[Integer, util.List[PlayerTeam]])//270000上线初始信息返回
  final case class changeGroupDungeomonsterInfo(dungeoId:Int,monsterlist:util.List[PlayerTeam]) //更新副本怪物信息*/

  final case class createArmyArmyGroupSucess(name : String , joinType : Int ,way : Int , armymap: util.Map[java.lang.Long, Armygroup])
  final case class getArmyGroupByidSucess(apparm : Armygroup ,icon : Int ,peniCon :Int,  cmd : Int)  //获得某个军团成功
  final case class getArmyShop(armygroup: Armygroup ,itemId : Int,opt : Int,typeId : Int,armymenber : ArmygroupMenber) // 刷新军团商店成功
  final case class getArmyShopSucess(armygroup: Armygroup ,itemId : Int,opt : Int,typeId : Int,armymenber : ArmygroupMenber) // 军团商店兑换成功
  final case class getAllArmyGroupSucess(obj : Object,armymap: util.Map[java.lang.Long, Armygroup],  cmd : Int)  // 获得全部军团信息返回
  final case class GetTechExpandPowerMap(techExpandPowerMap: ConcurrentHashMap[java.lang.Integer, java.lang.Long])  // 获得我的军团科技效果加成信息返回
  /////////////////系统公告///////////////////////
  final case class addNotice(notice : Notice)
  final case class addNoticelist(notice : util.List[Notice],retype :Int)

  ///////////////////ArmyGroupNode///////////////////////
  final case class Addmenber(menber : ArmygroupMenber)   //增加成员
  final case class removeArmyGroup(menber : ArmygroupMenber)   //删除成员
  final case class changeMenberCapity(playerId :Long , capity : Long)   //改变成员的战力
  final case class changeMenberLevel(playerId :Long , level : Int)   //改变成员的等级
  final case class changeMenberJob(playerId :Long , job : Int)   //改变成员的职位
  final case class changeMenberlogintime(playerId :Long )   //改变成员登陆时间
  final case class changeMenberlogOuttime(playerId :Long)   //改变成员登出时间
  final case class removeApplyid(playerId :Long)   //移出申请列表
  final case class applyArmyJoin(idlist : util.Set[java.lang.Long] , applytype :Int , level : Int , capity : Long , playerId : Long)  // 申请加入军团 取消加入
  final case class opeRateArmy(otherId : Long , myId : Long , retype :Int)  //1 踢出军团 2转让团长 3退出军团
  final case class clearApplylist(myId: Long)  //清空军团列表
  final case class editArmyGroup(myId: Long ,joinType : Int ,list :util.List[Integer] , level : Int , capity : Long , content : String )  //编辑
  final case class editJobName(myId: Long ,list :util.List[M22.LegionCustomJobShortInfo]  )  //编辑职位名称
  final case class setorUpJob(myId: Long ,retype  : Int , otherId : Long , job : Int)  //任职和升职
  final case class agreeApply(myId: Long ,otherId : Long ,retype :Int)  //同意申请
  final case class getMyGroupInfos()  //查看军团信息
  final case class lookAppList()  //查看审批列表
  final case class GetWelfareRes(resMap : util.Map[java.lang.Integer,java.lang.Integer] )  //军团福利资源
  final case class requestSlMwelfareInfo(playerId : Long)  //福利院信息请求
  final case class getAllLegion(name:String,typeId:Int)  //获得所有的军团军团改名
  final case class changeLegionName(name : String )   //改变军团名称
  final case class changeMenberName(name : String , playerId : Long)   //改变军团成员名字
  final case class editAffeche(cont : String , playerId : Long)
  final case class editLegionFinish(retype : Int , playerId : Long)
  final case class getLegionLevelInfo()  //获得军团等级信息
  final case class notityLegionLevel(level:Int)  //通知玩家军团等级
  final case class checkeNoneId() //通知没有改军团
  final case class checkArmy(id : Long)
  final case class addShareValue(playerId : Long,value :Int)
  final case class addLegionShareRecord(playerId :Long,chargeId:Int,createTime:Int, sharePlayerName: String)//有福同享 活动 增加宝箱记录
  final case class removeLegionShareRecord(playerId :Long,chargeId:Int,sharePlayerName:String,createTime:Int)//有福同享活动 移除宝箱记录
  final case class removeAllLegionShareRecord(playerId :Long)//有福同享活动 移除宝箱记录

  final case class getLegionLevelInfoback(army : Armygroup)  //获得军团等级信息返回
  final case class applyArmyJoinBack( rs : Int , army : Armygroup, retype :Int , armyId : Long)  // 申请加入军团 取消加入 返回
  final case class notiyKickArmy() // 通知被踢出军团
  final case class notiyTrueManger() // 转让军团
  final case class notiyCancelApply(id : Long) // 通知别取消申请
  final case class notiychangeJob(job: Int) // 通知改变职位
  final case class notiyaddArmgroup(armygroup : Armygroup) // 加入军团
  final case class opeRateArmyBack( rs : Int , retype : Int , playerid: Long,legInfos: util.List[LegionMemberInfo],oldJob : Int)  // 1 踢出军团 2转让团长 3退出军团返回
  final case class clearApplylistBack( rs : Int)  // 清空军团列表返回
  final case class editArmyGroupback(rs : Int ,joinType :Int ,list :util.List[Integer] , level : Int , capity : Long , content : String )  //编辑返回
  final case class editJobNameback(rs : Int ,list :util.List[M22.LegionCustomJobShortInfo] )  //编辑职称返回
  final case class setorUpJobBack(rs : Int ,retype  : Int , otherId : Long , job : Int, upjob : Int,legInfos: util.List[LegionMemberInfo])  //任职和升职
  final case class agreeApplyBack(rs : Int ,armId : Long , info: LegionMemberInfo,retype:Int )  //同意申请取消申请
  final case class getMyGroupInfosback(armygroup : Armygroup , list : util.List[ArmygroupMenber])  //查看军团信息返回
  final case class lookAppListback(list : util.List[SimplePlayer])  //查看审批列表返回
  final case class requestSlMwelfareInfoBack(welfareInfo : M22.PanelInfo,menbers: util.List[ArmygroupMenber])  //福利院信息请求返回
  final case class getAllLegionSucess(armymap: util.Map[java.lang.Long, Armygroup],name : String,typeId:Int)  //获得所有的军团成功军团改名
  final case class getAllLegionToarmygroup(armymap: util.Map[java.lang.Long, Armygroup],areaky : String)  //获得所有的军团
  final case class editAffecheSucess(rs : Int)
  final case class changetIconPend(playerId : Long , icon :Int ,pendIcon :Int)
  final case class applistNum()
  final case class applistNumback(num:Int)

  ///////////////////军情///////////////////////
  final case class addSituation(situation: Situation) //添加军情
  final case class getSituationInfo(build : M22.M220300.S2C.Builder) //请求获得军情
  final case class getSituationInfoback(build : M22.M220300.S2C.Builder) //获得军情返回

  ///////////////////ArmyGroupNode:军团科技///////////////////////
  final case class MtSTechUpReq(playerId : Long,opt :Int)  //军团科技大厅请求升级
  final case class StMTechUpInfo(armyLv : Int,techLv : Int,buildNum :Int,techInfo : M22.TechInfo,opt:Int)  //返回军团科技大厅请求升级信息
  final case class StMTechUpInfoSucc(techInfo : M22.TechInfo)  //返回军团科技大厅升级成功
  final case class MtSTechContributeReq(techId : Int,power : Int ,playerId : Long,cmd :Int,isFirst : Int,alltime:Int)  //军团科技捐献请求
  final case class StMTechContributeInfo(techInfo : M22.TechInfo,techId : Int,power : Int ,techLv : Int,techExp : Int,armyLv :Int,armyTechLv:Int,armymen :ArmygroupMenber, armygroup: Armygroup)  //返回军团科技捐献请求信息
  final case class StMContributeSucc(techInfo : M22.TechInfo,techId : Int,power : Int)  //返回军团科技捐献成功
  /////////////////ArmyGroupNode:军团大厅///////////////////////
  final case class MtSHallContribute(power : Int)//军团大厅捐献请求
  final case class MtSHallContributeReqSucess(armygroup: Armygroup,power : Int)//军团大厅捐献请求成功
  final case class MtSHallContributeReq(playerId : Long,power : Int ,num : Int )
  final case class MtSHallUpReq(playerId : Long,opt :Int)  //军团大厅请求升级
  final case class StMHallUpInfo(opt : Int,armyLv : Int,buildNum :Int,hallInfo : M22.ArmyInfo,menber:ArmygroupMenber)  //返回军团大厅请求升级信息
  final case class StMHallUpSucesstonode()  //通知节点升级成功
  final case class StMHallUpInfoSucc(hallInfo : M22.ArmyInfo)  //返回军团大厅升级成功
  final case class StMHallUpInfoFaild(hallInfo : M22.ArmyInfo)  //返回军团大厅升级成功
  final case class StMHallContributeSucc(hallInfo : M22.ArmyInfo ,power : Int)  //返回军团大厅捐献成功


  ////////////////军团招募///////////////////////
  final case class legionenlist(playerId:Long)
  final case class legionenlistback(rs :Int)

  ///////////////////////////////////////////最近/////////////////////////////
  final case class AddlaterPlayer(playerid : Long)
  final case class getlaterInfo(players : util.List[java.lang.Long],build : M20400.S2C.Builder)
  final case class getlaterInfoback(build : M20400.S2C.Builder)

  ///////////////////关服保存///////////////////////
  final case class StopGame()
  final case class saveDateBeforeStop()
  /////////////////ArmyGroupNode:军团福利院///////////////////////
  final case class MtSwelfareUpReq(playerId : Long,cmd :Int,typeId:Int)  //军团福利院请求升级
  final case class MtSGetwelfareReq(playerId : Long,cmd :Int,canGetid : Int,typeId:Int)  //军团福利院请求领取
  final case class MtSwelfareReq(playerId : Long,typeId:Int)  //军团福利院请求信息
  final case class StMwelfarReqInfo(welfareInfo : M22.PanelInfo,typeId:Int)  //返回军团福利院请求升级信息
  final case class StMwelfareUpInfo(wefareLv: Int, armyLv: Int, buildNum: Int,typeId:Int)  //返回军团福利院请求升级信息
  final case class StMwelfareUpInfoSucc(welfareInfo : M22.PanelInfo,typeId:Int)  //返回军团福利院升级成功
  final case class StMGetwelfare(welfareInfo : M22.PanelInfo,canGetid : Int,typeId:Int,menber: ArmygroupMenber)  //返回军团福利院领取
  final case class MtSwelfareGetRes(playerId : Long)  //资源福利请求
  final case class MtSwelfareGetSucc(canGetfiveResMap : util.Map[java.lang.Integer,java.lang.Integer], menber:ArmygroupMenber)  //资源福利返回

  final case class CountCapacity()
  final case class LoginInitCapacity()
 /////////////////////////////////////////////////////////////item//////////////////////////////////////////////////////////////////////
  final case class getOrePoint(simple : SimplePlayer,myaccontName : String)
  final case class getOrePointback(simple : SimplePlayer,list: util.List[String]) }

  /////////////////share:分享///////////////////////
  final case class ShareMsg(msg : M25.M250000.S2C,legionId : Long)
  final case class SendSystemChatToPlayerService(msg : M25.M250000.S2C)
  final case class PushShareMsg(pushList : util.ArrayList[ShareMsg])




