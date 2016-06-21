package com.znl.server

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.znl.msg.GameMsg.{OnServerTrigger, AutoSavePlayer}
import com.znl.utils.{GameUtils, DateBuilder}
import scala.concurrent.duration._
/**
 * 只处理 全局的游戏时间
 * Created by Administrator on 2015/10/31.
 */
class TriggerServer extends Actor{

  import context.dispatcher
  context.system.scheduler.schedule(0 milliseconds,1 seconds,context.self, OnServerTrigger())
  GameUtils.setServerTime((System.currentTimeMillis() / 1000).toInt)

  override def receive: Receive = {
    case onServerTrigger : OnServerTrigger =>
      onTrigger()
    case _ =>

  }

  /**
   * 每天在指定的时间执行
   * @param hour
   * @param minute
   */
  def dailyAtHourAndMinute(hour : Int, minute : Int) = {
    DateBuilder.validateHour(hour)
    DateBuilder.validateMinute(minute)

//    val cronExpression = String.format("0 %d %d ? * *", minute, hour)

  }

  def onTrigger() ={
    val serverTime = GameUtils.getServerTime
    GameUtils.setServerTime(serverTime + 1)
  }
}
