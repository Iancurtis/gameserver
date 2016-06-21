package com.znl.log;

import com.znl.base.BaseLog;

/**
 * Created by Administrator on 2016/2/20.
 */
public class FunctionLog extends BaseLog {
    private int actionId;
    private String expand;//用json格式书写各种扩展属性
    private String areaKey;
    private int areaId;
    private long playerId;
    private int level;

    public FunctionLog(){

    }
}
