package com.znl.framework.socket;

import org.apache.mina.core.session.IoSession;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/10/22.
 */
public class Message implements Serializable {
    /** 模块ID */
    private int module;

    /** 命令ID */
    private int cmd;

    /** 消息请求/响应时的服务器时间 */
    private long time = System.currentTimeMillis();

    /** 请求/响应的消息 */
    private Object value;

    private IoSession session;

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public <T> T getValue() {
        return (T)value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }
}
