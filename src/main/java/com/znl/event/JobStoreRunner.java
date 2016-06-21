package com.znl.event;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时器启动类
 */
public class JobStoreRunner {
    private static  final Logger logger= LoggerFactory.getLogger(JobStoreRunner.class);
    //启动定时器
    public static void startJobEvent() {
        try {

            EventCache.registerEvent();

            SchedulerFactory schedulerFactory = new StdSchedulerFactory("quartz.properties");
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("JobStoreRunner startJobEvent error",e);
        }
    }

    public  static  void  main(String[]args){
        startJobEvent();
    }
}
