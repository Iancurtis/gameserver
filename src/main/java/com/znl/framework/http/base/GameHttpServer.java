package com.znl.framework.http.base;

import com.znl.framework.http.HttpRequestDecoder;
import com.znl.framework.http.HttpResponseEncoder;
import com.znl.framework.http.HttpServerHandler;
import com.znl.framework.http.HttpServerProtocolCodecFactory;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2015/12/15.
 */
public class GameHttpServer {
    NioSocketAcceptor acceptor = null;

    public void startHttpServer(int port){
        try {
            // Create an acceptor
            acceptor = new NioSocketAcceptor();


            HttpResponseEncoder encoder = new HttpResponseEncoder();
            HttpRequestDecoder decoder = new HttpRequestDecoder();
            ProtocolCodecFactory protocolCodecFactory = new HttpServerProtocolCodecFactory(encoder, decoder);

            // Create a service configuration
            acceptor.getFilterChain().addLast("codec",
                    new ProtocolCodecFilter(protocolCodecFactory));
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

            acceptor.setHandler(new HttpServerHandler());
            acceptor
                    .bind(new InetSocketAddress(port));

            System.out.println("http Server now listening on port " + port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stop(){
        acceptor.unbind();
    }
}
