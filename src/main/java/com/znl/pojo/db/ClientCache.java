package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * 客户端保存到服务器的缓存
 * Created by Administrator on 2016/1/4.
 */
public class ClientCache extends BaseDbPojo{
    private Integer msgType = 0;

    private byte[] msg;

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
}
