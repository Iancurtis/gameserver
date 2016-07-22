package com.znl.event

import org.apache.commons.lang.StringUtils
import org.quartz.{Job, JobExecutionContext}

/**
 * Created by Administrator on 2016/6/25.
 */
class QuartzJob extends Job{
  def execute(jobExecutionContext:JobExecutionContext ){
    /*JobDetail jobDetail=jobExecutionContext.getJobDetail();
    String jobName = ((JobDetailImpl)jobDetail).getName();   //任务名称
    System.err.println(jobName);*/
    // System.err.println(jobExecutionContext.getTrigger().getKey().getName());
    val cmdName:String =jobExecutionContext.getTrigger().getKey().getName();
    System.err.println(cmdName)
    if(StringUtils.isBlank(cmdName))return;
    FixedTimeEventHandler.executeFixedTimeEvent(Integer.parseInt(cmdName));
  }
}
