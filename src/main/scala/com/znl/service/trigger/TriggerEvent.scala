package com.znl.service.trigger

import akka.actor.ActorRef
import com.znl.service.trigger.TriggerType.TriggerType
import com.znl.utils.GameUtils

/**
 * 倒计时事件类
 * countDown 当前剩余的时间
 * triggerType 触发类型
 * Created by Administrator on 2015/11/14.
 */
class TriggerEvent(actor : ActorRef, msg : AnyRef, triggerType: TriggerType, remainTime : Int = 0,timeDefine:Int ){

  val addTime : Int = GameUtils.getServerTime

  def getFixRemainTime() ={
    val curTime = GameUtils.getServerTime
    val updateCount = curTime - addTime
    val curRemainTime = remainTime - updateCount
    curRemainTime
  }

  def getTriggerType() ={
    triggerType
  }

  def getTimDefine()={
    timeDefine
  }
  def trigger = {
    actor ! msg
  }

  override def toString() ={
    actor.toString() + "_" + msg.toString
  }

}
