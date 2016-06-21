package com.znl.service

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}

/**
 * Created by Administrator on 2016/1/15.
 */
class ActivityService  extends Actor with ActorLogging{
  override def receive: Receive = {
    case _ =>
  }
}
