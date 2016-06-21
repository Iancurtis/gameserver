package com.znl.service

import java.util
import java.util.{Date, Calendar}

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}
import com.znl.msg.GameMsg.{RemoveTriggerEvent, AddTriggerEvent, OnServerTrigger}
import com.znl.service.trigger.{TriggerType, TriggerEvent}
import com.znl.utils.GameUtils

import scala.concurrent.duration._
import scala.collection.JavaConversions._

/**
 * 每个区的 倒计时服务
 * 使用的环境只在一些service上
 * Created by Administrator on 2015/11/14.
 */
class TriggerService  extends Actor with ActorLogging{

  import context.dispatcher
  context.system.scheduler.schedule(0 milliseconds,1 seconds,context.self, OnServerTrigger())

  val triggerEventMap : java.util.Map[String, TriggerEvent] = new java.util.concurrent.ConcurrentHashMap[String, TriggerEvent]()

  override def receive: Receive = {
    case msg : OnServerTrigger =>
      onServerTrigger()
    case AddTriggerEvent(triggerEvent) =>
      onAddTriggerEvent(triggerEvent)
    case RemoveTriggerEvent(triggerEvent) =>
    case _  =>
  }

  def onAddTriggerEvent(triggerEvent: TriggerEvent): Unit ={
    val eventKey = triggerEvent.toString()
    println("服务器添加定时器"+eventKey)
    if(triggerEventMap.containsKey(eventKey)){
      triggerEventMap.replace(eventKey, triggerEvent)
    }else{
      triggerEventMap.put(eventKey, triggerEvent)
    }
  }

  def onRemoveTriggerEvent(triggerEvent: TriggerEvent) ={
    val eventKey = triggerEvent.toString()
    if(triggerEventMap.containsKey(eventKey)){
      triggerEventMap.remove(eventKey)
    }
  }

  def onServerTrigger() ={
    val removeList: java.util.List[String] = new util.ArrayList[String]()
    val triggerList : java.util.List[String] = new util.ArrayList[String]()
    val date = GameUtils.getServerDate()
    triggerEventMap.foreach( map => {
      val triggerEvent = map._2
      val triggerType = triggerEvent.getTriggerType()
      val curRemainTime = triggerEvent.getFixRemainTime()
      if(triggerType == TriggerType.COUNT_DOWN && curRemainTime <= 0){
        removeList.add(map._1)
      }else if(triggerType == TriggerType.WHOLE_MINUTE){
        if(date.getSeconds() == 0){
          triggerList.add(map._1)
        }
      }else if(triggerType == TriggerType.WHOLE_HOUR){
        if(date.getSeconds() == 0 && date.getMinutes() == 0){
          triggerList.add(map._1)
        }
      }
    })

    removeList.foreach( key => {
      val triggerEvent = triggerEventMap.get(key)
      triggerEvent.trigger

      triggerEventMap.remove(key)
    })

    triggerList.foreach( key => {
      val triggerEvent = triggerEventMap.get(key)
      triggerEvent.trigger
    })
  }

}
