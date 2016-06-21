package com.znl.framework.socket.base;

import com.znl.framework.socket.RequestDecoder;
import com.znl.framework.socket.ResponseEncoder;
import com.znl.framework.socket.ServerHandler;
import com.znl.framework.socket.factory.CommonCodecFactory;
import com.znl.framework.socket.websocket.WebSocketCodecFactory;
import com.znl.log.CustomerLogger;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2015/10/22.
 */
public class GameNetServer {
    private NioSocketAcceptor accept = null;

    public void startSocketServer(int port){
        // 服务端的实例
        accept = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
        accept.setReuseAddress(true);
        ResponseEncoder encoder = new ResponseEncoder();
        RequestDecoder decoder = new RequestDecoder();
        ProtocolCodecFactory protocolCodecFactory = new CommonCodecFactory(encoder, decoder);

//        ProtocolCodecFactory protocolCodecFactory = new WebSocketCodecFactory();

        // 添加filter，codec为序列化方式。这里为对象序列化方式，即表示传递的是对象。
        accept.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(protocolCodecFactory));

        // 设置服务端的handler
        accept.setHandler(new ServerHandler());
        accept.getSessionConfig().setReadBufferSize(2048);
        accept.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        // 绑定ip
        try {
            accept.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            CustomerLogger.error("绑定端口失败", e);
        }
        CustomerLogger.error("game net server started. bind port");
    }

    public void stop(){
        accept.unbind();
    }

    public void startHttpServer(int port){

    }
}
