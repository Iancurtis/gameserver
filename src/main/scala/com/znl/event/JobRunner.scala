package com.znl.event

import org.quartz.{SchedulerException, Scheduler, SchedulerFactory}
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by Administrator on 2016/6/25.
 */
object JobRunner {

  def startJobEvent {
    try {
      val schedulerFactory: SchedulerFactory = new StdSchedulerFactory("quartz.properties")
      val scheduler: Scheduler = schedulerFactory.getScheduler
      scheduler.start
    }
    catch {
      case e: SchedulerException => {

      }
    }
  }

  def main(args: Array[String]) {
    startJobEvent
  }
}
