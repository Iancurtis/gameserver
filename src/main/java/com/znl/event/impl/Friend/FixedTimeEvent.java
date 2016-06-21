package com.znl.event.impl.Friend;

import com.znl.event.annotation.FixedTimeAnnotation;
import com.znl.event.base.BaseFixedTimeEvent;

/**
 * 定时执行事件
 * Created by pwy on 2016/6/16.
 */
@FixedTimeAnnotation(actionTimes = {1,2},description = "每天4点执行")
public class FixedTimeEvent implements BaseFixedTimeEvent {
    @Override
    public void exceute(int cmd) {
        System.err.println(" friend module action fixedTime ="+cmd);
    }
}
