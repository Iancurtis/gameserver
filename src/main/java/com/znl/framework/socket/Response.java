package com.znl.framework.socket;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Administrator on 2015/10/22.
 */
public class Response extends Message {
    public static Response valueOf(int module, int cmd, com.google.protobuf.GeneratedMessage value) {
        Response response = new Response();
        response.setCmd(cmd);
        response.setModule(module);
        response.setValue(value);

        return response;
    }



}
