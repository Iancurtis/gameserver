package com.znl.framework.socket;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.znl.GameMainServer;
import com.znl.define.ActorDefine;
import com.znl.msg.GameMsg;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.List;

/**
 * Created by Administrator on 2015/10/22.
 */
public class ServerHandler extends IoHandlerAdapter {

    @Override
    public void sessionOpened(IoSession session) throws Exception {
//        logger.error("server open，有新的连接连上来了");
        ActorSystem system = GameMainServer.system();
        ActorSelection gateServer = system.actorSelection(ActorDefine.GATE_SERVER_PATH);
        gateServer.tell(new GameMsg.SessionOpen(session), ActorRef.noSender());
    }
    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
//        logger.warn("*****session******exception*******");
        session.close(true);
        super.exceptionCaught(session, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        if(message instanceof List)
        {
            List<Request> requestList = (List<Request>)message;
            for(Request request : requestList){
                request.setSession(session);
                System.out.println("===============接受到数据包=== " + session.getRemoteAddress() + " 模块ID：" + request.getModule() + "命令id：" + request.getCmd());
                ActorSystem system = GameMainServer.system();
                ActorSelection gateServer = system.actorSelection(ActorDefine.GATE_SERVER_PATH);
                gateServer.tell(new GameMsg.SessionMessageReceived(session,  request), ActorRef.noSender()); //TODO 如果里面有相同协议的，对该协议进行批量处理，比如自动升级
            }
        }

//		session.write(message);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
//		System.out.println("server messageSent:" + message );
        if(message instanceof Response)
        {
            Response response = (Response) message;
//			logger.info("===============发送数据包=== " + session.getRemoteAddress()+"  模块ID：" + response.getModule() + "命令id："+response.getCmd());
        }
    }
    @Override
    public void sessionClosed(IoSession session) throws Exception {
//        logger.info("==============连接关闭================ " + session.getRemoteAddress());
        ActorSystem system = GameMainServer.system();
        ActorSelection gateServer = system.actorSelection(ActorDefine.GATE_SERVER_PATH);
        gateServer.tell(new GameMsg.SessionClose(session), ActorRef.noSender());
    }



}
