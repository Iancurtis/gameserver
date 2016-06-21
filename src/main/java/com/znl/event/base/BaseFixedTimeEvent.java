package com.znl.event.base;

/**
 * Created by pwy on 2016/6/16.
 */
public interface BaseFixedTimeEvent {
    /**
     * 执行定时事件方法
     * @param cmd
     */
    public void exceute(int cmd);
}
