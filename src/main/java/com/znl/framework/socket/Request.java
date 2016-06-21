package com.znl.framework.socket;

import java.util.Map;

/**
 * Created by Administrator on 2015/10/22.
 */
public class Request extends Message{

    private Map<Integer, Long>  powerMap;

    public Map<Integer, Long> getPowerMap() {
        return powerMap;
    }

    public void setPowerMap(Map<Integer, Long> powerMap) {
        this.powerMap = powerMap;
    }

    public int getReqTime() {
        return reqTime;
    }

    public void setReqTime(int reqTime) {
        this.reqTime = reqTime;
    }

    private int reqTime;


    public static Request valueOf(int module, int cmd, Object value) {
        Request request = new Request();
        request.setCmd(cmd);
        request.setModule(module);
        request.setValue(value);
        return request;
    }
}
