/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.znl.framework.socket.websocket;

import com.znl.framework.socket.CodecContext;
import com.znl.framework.socket.DecoderState;
import com.znl.framework.socket.Request;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Method;

/**
 * Decodes incoming buffers in a manner that makes the sender transparent to the 
 * decoders further up in the filter chain. If the sender is a native client then
 * the buffer is simply passed through. If the sender is a websocket, it will extract
 * the content out from the dataframe and parse it before passing it along the filter
 * chain.
 * 
 * @author DHRUV CHOPRA
 */
public class WebSocketDecoder extends CumulativeProtocolDecoder{    
    
    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        System.out.println("开始解析数据");
        IoBuffer resultBuffer;
        if(!session.containsAttribute(WebSocketUtils.SessionAttribute)){
            // first message on a new connection. see if its from a websocket or a 
            // native socket.
            if(tryWebSockeHandShake(session, in, out)){
                // websocket handshake was successful. Don't write anything to output
                // as we want to abstract the handshake request message from the handler.
                in.position(in.limit());
                return true;
            }
            else{
                // message is from a native socket. Simply wrap and pass through.
                resultBuffer = IoBuffer.wrap(in.array(), 0, in.limit());
                in.position(in.limit());
                session.setAttribute(WebSocketUtils.SessionAttribute, false);

                System.out.println("开始解析数据1");
            }
        }
        else if(session.containsAttribute(WebSocketUtils.SessionAttribute) && true==(Boolean)session.getAttribute(WebSocketUtils.SessionAttribute)){            
            // there is incoming data from the websocket. Decode and send to handler or next filter.     
            int startPos = in.position();
            resultBuffer = WebSocketDecoder.buildWSDataBuffer(in, session);
            if(resultBuffer == null){
                // There was not enough data in the buffer to parse. Reset the in buffer
                // position and wait for more data before trying again.
                in.position(startPos);
                System.err.println("开始解析数据2");
                return false;
            }
            System.out.println("开始解析数据3");
        }
        else{
            // session is known to be from a native socket. So
            // simply wrap and pass through.
            resultBuffer = IoBuffer.wrap(in.array(), 0, in.limit());    
            in.position(in.limit());

            System.out.println("开始解析数据4");
        }

        String contextKey = "CONTEXT_KEY";

        CodecContext ctx = (CodecContext)session.getAttribute(contextKey);

        if(ctx != null && ctx.isSameState(DecoderState.WAITING_DATA)){
            int remaining = resultBuffer.remaining();

            byte[] buffer = new byte[remaining];
            resultBuffer.get(buffer);
            ctx.revData(buffer);

            if(ctx.getRemaining() > 0){ //还需要剩余的数据要接收
                return false;
            }


            Request request = decodeBuffer(ctx.getBuffer());
            if(request != null){
                out.write(request);
            }

            ctx.setState(DecoderState.READY);
            session.removeAttribute(contextKey);
            return true;
        }

        int len = resultBuffer.getInt();  //数据包的真实长度
        int remaining = resultBuffer.remaining();

        if(remaining < len){
            ctx = CodecContext.valueOf(len, DecoderState.WAITING_DATA);
            session.setAttribute(contextKey, ctx);

            byte[] buffer = new byte[remaining];
            resultBuffer.get(buffer);
            ctx.revData(buffer);

            return false;
        }else{
            byte[] buffer = new byte[len];
            resultBuffer.get(buffer);
            Request request = this.decodeBuffer(buffer);
            if (request != null) {
                out.write(request);
            }

            return true;
        }
    }

    //TODO 解析客户端发送过来的协议，这里是可以写日志，然后分析玩家的行为的
    private  Request decodeBuffer(byte[]  data ){
        int moduleId = 0;
        int cmdId = 0;
        Request request = null;
        try
        {
//				log.info("===========接受到数据包==开始解析========" + System.currentTimeMillis());

            int len = data.length;

            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(stream);

            moduleId = dataInputStream.readInt();
            cmdId = dataInputStream.readInt();

            byte[] obj = new byte[len - 8];
            dataInputStream.read(obj);

            Object[] args = {obj};
            String className = "com.znl.proto.M" + moduleId + "$M" + cmdId + "$C2S";  //TODO包名头配置
            Object result = invokeStaticMethod(className, "parseFrom", args);

            System.out.println("===客户端发送的数据协议解析成功=====moduleId:"+moduleId+"==cmdId:="+ cmdId);

            request = Request.valueOf( moduleId, cmdId, result);
//				log.info("===========接受到数据包==结束解析========" + System.currentTimeMillis());
        }catch(Exception e)
        {
//            log.error(String.format("===客户端发送的数据协议解析失败=====moduleId%d==cmdId:%d=", moduleId, cmdId));
//            log.error(e.getMessage());
//				e.printStackTrace();
            System.out.println("===客户端发送的数据协议解析失败=====moduleId:"+moduleId+"==cmdId:="+ cmdId);
//            fireWall.pushToBlackList(session);
        }

        return request;


    }

    private Object invokeStaticMethod(String className, String methodName,
                                     Object[] args) throws Exception {
        Class<?> ownerClass = Class.forName(className);

        @SuppressWarnings("rawtypes")
        Class[] argsClass = new Class[args.length];

        for (int i = 0, j = args.length; i < j; i++) {
            argsClass[i] = args[i].getClass();
        }

        Method method = ownerClass.getMethod(methodName, argsClass);

        return method.invoke(null, args);
    }

    /**
    *   Try parsing the message as a websocket handshake request. If it is such
    *   a request, then send the corresponding handshake response (as in Section 4.2.2 RFC 6455).
    */
    private boolean tryWebSockeHandShake(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
        
        try{
            String payLoadMsg = new String(in.array());
            String socketKey = WebSocketUtils.getClientWSRequestKey(payLoadMsg);
            if(socketKey.length() <= 0){
                return false;
            }
            String challengeAccept = WebSocketUtils.getWebSocketKeyChallengeResponse(socketKey);            
            WebSocketHandShakeResponse wsResponse = WebSocketUtils.buildWSHandshakeResponse(challengeAccept);
            session.setAttribute(WebSocketUtils.SessionAttribute, true);
            session.write(wsResponse);
            return true;
        }
        catch(Exception e){
            // input is not a websocket handshake request.
            return false;
        }        
    }
    
    // Decode the in buffer according to the Section 5.2. RFC 6455
    // If there are multiple websocket dataframes in the buffer, this will parse
    // all and return one complete decoded buffer.
    private static IoBuffer buildWSDataBuffer(IoBuffer in, IoSession session) {

        IoBuffer resultBuffer = null;
        do{
            byte frameInfo = in.get();            
            byte opCode = (byte) (frameInfo & 0x0f);
            if (opCode == 8) {
                // opCode 8 means close. See RFC 6455 Section 5.2
                // return what ever is parsed till now.
                session.close(true);
                return resultBuffer;
            }        
            int frameLen = (in.get() & (byte) 0x7F);
            if(frameLen == 126){
                frameLen = in.getShort();
            }
            
            // Validate if we have enough data in the buffer to completely
            // parse the WebSocket DataFrame. If not return null.
            if(frameLen+4 > in.remaining()){                
                return null;
            }
            byte mask[] = new byte[4];
            for (int i = 0; i < 4; i++) {
                mask[i] = in.get();
            }

            /*  now un-mask frameLen bytes as per Section 5.3 RFC 6455
                Octet i of the transformed data ("transformed-octet-i") is the XOR of
                octet i of the original data ("original-octet-i") with octet at index
                i modulo 4 of the masking key ("masking-key-octet-j"):

                j                   = i MOD 4
                transformed-octet-i = original-octet-i XOR masking-key-octet-j
            * 
            */
             
            byte[] unMaskedPayLoad = new byte[frameLen];
            for (int i = 0; i < frameLen; i++) {
                byte maskedByte = in.get();
                unMaskedPayLoad[i] = (byte) (maskedByte ^ mask[i % 4]);
            }
            
            if(resultBuffer == null){
                resultBuffer = IoBuffer.wrap(unMaskedPayLoad);
                resultBuffer.position(resultBuffer.limit());
                resultBuffer.setAutoExpand(true);
            }
            else{
                resultBuffer.put(unMaskedPayLoad);
            }
        }
        while(in.hasRemaining());
        
        resultBuffer.flip();
        return resultBuffer;

    }    
}
