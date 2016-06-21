package com.znl.service

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import com.znl.base.{BaseSetDbPojo, BaseDbPojo}
import com.znl.core.{PlayerReward, PlayerCache, SimplePlayer}
import com.znl.define._
import com.znl.framework.http.HttpMessage
import com.znl.msg.GameMsg._
import com.znl.pojo.db.Player
import com.znl.pojo.db.set.{VipActSetDb, PlayerRankSetDb, BillOrderSetDb}
import com.znl.proxy.{ActivityProxy, PlayerProxy, GameProxy, DbProxy}
import com.znl.template.ChargeTemplate
import com.znl.utils.GameUtils
import scala.collection.JavaConversions._

/**
 * Created by Administrator on 2015/12/23.
 */

object ChargeService {
  def props(areaKey : String) = Props(classOf[ChargeService], areaKey)
}

class ChargeService(areaKey : String) extends Actor with ActorLogging with ServiceTrait{

  def chargeHandle(message: HttpMessage, msg: AnyRef): Unit = {
    val chargeTemplate : ChargeTemplate = msg.asInstanceOf[ChargeTemplate]

    val billOrderSetDb: BillOrderSetDb = BaseSetDbPojo.getSetDbPojo(classOf[BillOrderSetDb], areaKey)

    val isRepeat: Boolean = billOrderSetDb.isKeyExist(chargeTemplate.getOrderId) // DbProxy.ask(IsRepeatBillOrder(chargeTemplate.getOrderId), 20)
    var res : String= null
    println("**********充值服务器接收到了充值请求")
    if (isRepeat) {
      println("**********结果，重复的订单号")
      res = GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.CHARGE_ORDER_ID_REPEAT, "重复的订单号")
      val senderA :akka.actor.ActorRef = sender()
      sender()! AdminActionMessageToServiceBack(message,res)
    }else {
      val simplePlayer: SimplePlayer = PlayerService.getSimplePlayer(chargeTemplate.getPlayerId,areaKey)
      if(simplePlayer == null){
        println("**********结果，不存在的玩家")
        res =  GameUtils.getAdminStatusJsonMsg(AdminCodeDefine.CHARGE_CAN_NOT_FIND_PLAYER, "不存在的玩家")
        sender()! AdminActionMessageToServiceBack(message,res)
      }else{
        val vipactset = BaseSetDbPojo.getSetDbPojo(classOf[VipActSetDb], areaKey)
        val lastvalue:Long=vipactset.getAllVipExpByplayerId(simplePlayer.getId)
        vipactset.addKeyValue(simplePlayer.getId+"", lastvalue+chargeTemplate.getChargeValue*10)
        if(simplePlayer.online){
          //通知到具体玩家模块处理逻辑
          println("**********结果，玩家在线，发送到模块")
          sendMsgToPlayerActor(simplePlayer.getAccountName,ChargeToPlayer(chargeTemplate,message,sender()))
        }else{
          //离线玩家处理
          println("**********结果，玩家不在线，进入离线玩家处理")
          chargeToOffLinePlayer(chargeTemplate,message)
        }
      }
    }

  }

  /*****给在线玩家推送更新充值活动**********/
  def notifPlayerChargeAct(): Unit ={
   for(simple <- PlayerService.onlineMap.get(areaKey).values()){
    if(simple!=null && simple.online){
      sendMsgToPlayerActor(simple.getAccountName,notitySomeOneCharge())
    }
   }
  }

  /****给离线玩家充值逻辑（包括充值活动触发相关逻辑都在这里）****/
  //http://127.0.0.1:8002/change_money?amount=10&order_id=test9992&51821&server_id=9992&player_id=5182&callback_info=0&server=9992
  def chargeToOffLinePlayer(template: ChargeTemplate, message: HttpMessage): Unit = {
    val player = BaseDbPojo.get(template.getPlayerId, classOf[Player],areaKey)
    val gameProxy: GameProxy = new GameProxy(player,new PlayerCache())
    val playerProxy :PlayerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME)
    playerProxy.setPlayerCache(new PlayerCache)
    val result : String = playerProxy.chargeToPlayer(template.getChargeValue,template.getChargeType,template.getOrderId,new PlayerReward())
    sender() ! AdminActionMessageToServiceBack(message,result)
    println("**********离线玩家充值完成，发送结果="+result)
    playerProxy.savePlayer()

    //保存玩家信息，各种任务数据也要保存
    val activityProxy : ActivityProxy = gameProxy.getProxy(ActorDefine.ACTIVITY_PROXY_NAME)
    activityProxy.saveActivity()
    //释放掉
    gameProxy.finalize()
  }

  override def receive: Receive = {
    case AdminActionMessageToService(http : HttpMessage, msg : AnyRef) =>
      chargeHandle(http,msg)
    case ChargeToPlayerDone(http : HttpMessage, result : String) =>
      val actor = context.actorSelection("../../"+ActorDefine.ADMIN_SERVER_NAME+"/"+ActorDefine.ADMIN_CHARGE_NAME)
      actor! AdminActionMessageToServiceBack(http,result)
      println("**********在线玩家充值完成，发送结果="+result)
    case _ =>
  }

  def sendMsgToService(serviceNme :String ,anyRef: AnyRef ): Unit ={
    val actor = context.actorSelection("../"+serviceNme)
    actor ! anyRef
  }

  //发送消息给具体的player系统模块
  def sendMsgToPlayerActor(accountName : String, msg : AnyRef) ={
    val actor = context.actorSelection("../"+ActorDefine.PLAYER_SERVICE_NAME+"/"+accountName+"/"+ActorDefine.SYSTEM_MODULE_NAME)
    actor ! msg
  }


  override val supervisorStrategy = OneForOneStrategy() {
    case e : Exception => {
      e.printStackTrace()
      log.error(e.fillInStackTrace(), e.getMessage)
      log.error("charge出现异常，正在重启",e)
      Resume
    }
    case _ => Resume
  }
}


