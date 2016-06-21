package com.znl.event;

import com.znl.event.base.BaseFixedTimeEvent;
import com.znl.event.base.BaseZeroEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 定时执行处理
 * Created by pwy on 2016/6/16.
 */
public class FixedTimeEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(FixedTimeEventHandler.class);

    /**
     * 执行定时处理事件
     * @param cmd 0 零点处理   >0 其他时间点执行
     */
    public static void executeFixedTimeEvent(int cmd){
        if(cmd==0){
            executeZeroEvent();
        }else{
            executeOtherTimeEvent(cmd);
        }
    }

    /**
     * 执行零点处理
     */
    private static void executeZeroEvent(){
        long time = System.currentTimeMillis();
        for(BaseZeroEvent event:EventCache.zeroHandlers){
            event.zeroHandler();
        }
        logger.info("zero time handler success  spend time="+(System.currentTimeMillis()-time));
    }

    /**
     * 执行除零点外的定时处理
     * @param cmd
     */
    private static void executeOtherTimeEvent(int cmd){
        logger.info("start fixed time handler cmd="+cmd);
        long time = System.currentTimeMillis();
        List<BaseFixedTimeEvent> list = EventCache.fixedTimeMap.get(cmd);
        if(list==null||list.isEmpty())return;
        for(BaseFixedTimeEvent bfte:list){
            try{
                bfte.exceute(cmd);
            }catch(Exception e){
                logger.error("BaseFixedTimeEvent exception:",e);
            }
        }
        logger.info("fixed time handler success cmd="+cmd+" spend time="+(System.currentTimeMillis()-time));
    }
}
