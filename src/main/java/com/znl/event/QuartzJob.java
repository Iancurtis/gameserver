package com.znl.event;


import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 定时job
 */
public class QuartzJob implements Job {

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        /*JobDetail jobDetail=jobExecutionContext.getJobDetail();
        String jobName = ((JobDetailImpl)jobDetail).getName();   //任务名称
        System.err.println(jobName);*/
       // System.err.println(jobExecutionContext.getTrigger().getKey().getName());
        String cmdName=jobExecutionContext.getTrigger().getKey().getName();
        if(StringUtils.isBlank(cmdName))return;
        FixedTimeEventHandler.executeFixedTimeEvent(Integer.parseInt(cmdName));
    }

}
