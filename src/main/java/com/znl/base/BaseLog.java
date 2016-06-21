package com.znl.base;

import java.io.Serializable;

/**
 * 日志基类
 * Created by Administrator on 2015/12/5.
 */
public class BaseLog implements Serializable{
    private Integer logTime;  //写日志的时间

    private String logType; //日志类型

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public Integer getLogTime() {
        return logTime;
    }

    public void setLogTime(Integer logTime) {
        this.logTime = logTime;
    }
}
