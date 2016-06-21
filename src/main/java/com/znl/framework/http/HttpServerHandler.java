/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package com.znl.framework.http;

import java.net.SocketAddress;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.znl.GameMainServer;
import com.znl.define.ActorDefine;
import com.znl.msg.GameMsg;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;



/**
 * An {@link IoHandler} for HTTP.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $
 */
public class HttpServerHandler extends IoHandlerAdapter {
	
    public void sessionOpened(IoSession session) {
        // set idle time to 60 seconds
//        session.setIdleTime(IdleStatus.BOTH_IDLE, 60);
    	System.out.println("http session open");
    }

    private static String[] whiteIpList = {"119.29.63.155","115.159.35.155","119.29.11.150","127.0.0.1","192.168.31.119"};
    
    public void messageReceived(IoSession session, Object message) {
    	
    	HttpRequestMessage requestMsg = (HttpRequestMessage)message;
    	SocketAddress addr = session.getRemoteAddress();
		String[] addrTemp = addr.toString().split(":");
		String ip = addrTemp[0].replace("/", "");
		boolean checkInIp = false;
		for(String whiteIp : whiteIpList){
			if(whiteIp.equals(ip)){
				checkInIp = true;
			}
		}

		ActorSystem system = GameMainServer.system();
		ActorSelection gateServer = system.actorSelection(ActorDefine.ADMIN_SERVER_PATH);
		gateServer.tell(new GameMsg.AdminMessageReceived(HttpMessage.valueOf(session,requestMsg)), ActorRef.noSender());

//		if(checkInIp || gameWorld.testPattern){
//			System.out.println("%%%%%%%%%%%%%收到白名单内的http协议："+ip);
//			gameWorld.eventHandle.onHttpEvent(requestMsg, session);
//		}else{
//			System.out.println("%%%%%%%%%%%%%收到非白名单内的http协议："+ip);
//		}
    	
        // Check that we can service the request context test
//        HttpResponseMessage response = new HttpResponseMessage();
//        response.setContentType("text/plain");
//        response.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
//        response.appendBody("CONNECTED");
//
//        if (response != null)
//            session.write(response).isWritten();
    	
    }

    public void sessionIdle(IoSession session, IdleStatus status) {
        session.close(true);
        System.out.println("******sessionIdle*****" + status);
    }

    public void exceptionCaught(IoSession session, Throwable cause) {
        session.close(true);
        System.out.println("******exceptionCaught*****" + cause);
    }
    
    @Override
	public void sessionClosed(IoSession session) throws Exception {
    	System.out.println("==============连接关闭================ " + session.getRemoteAddress());
	}
}
