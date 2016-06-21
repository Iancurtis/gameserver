package com.znl.framework.socket;

import com.znl.framework.socket.base.FireWall;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/10/22.
 */
public class RequestDecoder  extends CumulativeProtocolDecoder {

    private static final Logger log = LoggerFactory.getLogger(RequestDecoder.class);

    @Override
    protected boolean doDecode(IoSession session, IoBuffer input,
                               ProtocolDecoderOutput out) throws Exception {

//		CodecContext ctx = (CodecContext) session.getAttribute("CONTEXT_KEY");
//		if (ctx != null && ctx.isSameState(DecoderState.WAITING_DATA)) {
//			if (input.remaining() < ctx.getBytesNeeded()) {
//				return false;
//			}
//
//			byte[] buffer = new byte[ctx.getBytesNeeded()];
//			input.get(buffer);
//
//		}
//		else
//		{
//
//		}

        FireWall fireWall = FireWall.getInstance();
        if(fireWall.checkInBlackList(session)){
            session.close(true);
            return false;//被防火墙加入了黑名单
        }
        Boolean firstRequest = (Boolean) session.getAttribute("FIRST_REQUEST_KEY");
        if(firstRequest == null)
        {
            firstRequest = true;
            session.setAttribute("FIRST_REQUEST_KEY", false);
        }
        if(firstRequest != false)
        {
            input.mark();
            String str = input.getString((Charset.forName("UTF-8"))
                    .newDecoder());
            String key = "";
            Pattern p=Pattern.compile("Sec-WebSocket-Key:(.*?)\r\n");
            Matcher m=p.matcher(str);
            if(m.find() == true)
            {
                key = m.group().replace("Sec-WebSocket-Key:", "").replace("\r\n", "").trim() ; //
                key = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] encryptionString = md.digest(key.getBytes());
                String secKeyAccept = Base64.getEncoder().encodeToString(encryptionString);

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("HTTP/1.1 101 Switching Protocols" + "\r\n");
                responseBuilder.append("Upgrade: websocket" + "\r\n");
                responseBuilder.append("Connection: Upgrade" + "\r\n");
                responseBuilder.append("Sec-WebSocket-Accept: " + secKeyAccept + "\r\n\r\n");

                String sendStr = responseBuilder.toString();
                session.write(sendStr);

            }

            return true;
        }
        else
        {
            input.mark();

            if(input.remaining()<2){
                //消息头不完整
                return false;
            }

//			int clen = input.getInt();
//			log.info("客户端上传的数据长度为：" + clen);

            int moduleId = 0;
            int cmdId = 0;

            byte[]  data = analyticData(session, input);
            if(data == null){
                return false;
            }

            String contextKey = "CONTEXT_KEY";

            CodecContext ctx = (CodecContext)session.getAttribute(contextKey);

            if(ctx != null && ctx.isSameState(DecoderState.WAITING_DATA)){
                ctx.revData(data);

                if(ctx.getRemaining() > 0){ //还需要剩余的数据要接收
                    return false;
                }


                List<Request> requests = decodeBuffer(ctx.getBuffer());
                if(requests != null){
                    out.write(requests);
                }

                ctx.setState(DecoderState.READY);
                session.removeAttribute(contextKey);
                return true;
            }

            int len = data.length;

            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(stream);


            int alllen = dataInputStream.readInt();

            if(len - 4 < alllen){
                ctx = CodecContext.valueOf(alllen - 4, DecoderState.WAITING_DATA);
                session.setAttribute(contextKey, ctx);

                byte[] buffer = new byte[len - 4];
                dataInputStream.read(buffer);
                ctx.revData(buffer);

                return false;
            }


            byte[] buffer = new byte[len - 4];
            dataInputStream.read(buffer);

            List<Request> requests = this.decodeBuffer(buffer);
            if (requests != null) {
                out.write(requests);
            }

            return true;
        }
    }

    private List<Request> decodeBuffer(byte[]  data ){
        int moduleId = 0;
        int cmdId = 0;
        List<Request> requestList = new ArrayList<Request>();
        try
        {
//				log.info("===========接受到数据包==开始解析========" + System.currentTimeMillis());

            int len = data.length;

            ByteArrayInputStream stream = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(stream);

            int reqTime = dataInputStream.readInt(); //客户端发送协议的时间点，一个时间点会对应多条协议，具体得判断协议内容，不能单独判断协议号
            int sendCount = dataInputStream.readByte();

            Request request = null;
            int count = 0;
            while(count < sendCount){
                count++;

                int potoLen = dataInputStream.readInt();
                moduleId = dataInputStream.readByte();
                cmdId = dataInputStream.readInt();

                byte[] obj = new byte[potoLen];
                dataInputStream.read(obj, 0 , potoLen);

                Object[] args = {obj};
                String className = "com.znl.proto.M" + moduleId + "$M" + cmdId + "$C2S";  //TODO包名头配置
                Object result = invokeStaticMethod(className, "parseFrom", args);

//            System.out.println("===客户端发送的数据协议解析成功=====moduleId:"+moduleId+"==cmdId:="+ cmdId);

                request = Request.valueOf( moduleId, cmdId, result);
                request.setReqTime(reqTime);

                requestList.add(request);
            }

//				log.info("===========接受到数据包==结束解析========" + System.currentTimeMillis());
        }catch(Exception e)
        {
//            log.error(String.format("===客户端发送的数据协议解析失败=====moduleId%d==cmdId:%d=", moduleId, cmdId));
//            log.error(e.getMessage());
//				e.printStackTrace();
            System.err.println("===客户端发送的数据协议解析失败=====moduleId:"+moduleId+"==cmdId:="+ cmdId);
//            fireWall.pushToBlackList(session);
        }

        return requestList;


    }

    public Object invokeStaticMethod(String className, String methodName,
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


    private static final byte FIN = 0x1; // 1000 0000
    private static final byte OPCODE = 0x0F;// 0000 1111
    private static final byte MASK = 0x1;// 1000 0000
    private static final byte PAYLOADLEN = 0x7F;// 0111 1111
    private static final byte HAS_EXTEND_DATA = 126;
    private static final byte HAS_EXTEND_DATA_CONTINUE = 127;

    // 解析客户端数据包
    private byte[]  analyticData(IoSession session, IoBuffer buffer) throws Exception
    {

        if(buffer.remaining()<2){
            //消息头不完整
            return null;
        }

        buffer.mark();

        byte head1 = buffer.get();
        byte head2 = buffer.get();

        int opcode = head1 & OPCODE;

        //1为字符数据，8为关闭socket
        if(opcode == 8){
            //关闭socket
            session.close(true);
            return null;
        }

        int ismask = head2 >> 7 & MASK;
        int length = 0;
        int datalength = head2 & PAYLOADLEN;

        if(datalength < HAS_EXTEND_DATA){
            length = datalength;
        }else if(datalength == HAS_EXTEND_DATA){
            if(buffer.remaining() < 2){
                //消息头不完整
                buffer.reset();
                return null;
            }
            byte[] extended = new byte[2];
            buffer.get(extended);

            int shift = 0;
            length = 0;
            for (int i = extended.length - 1; i >= 0; i--) {
                length = length + ((extended[i] & 0xFF) << shift);
                shift += 8;
            }
        }

        byte[] data = new byte[length];
        if(ismask == 1) {
            if (buffer.remaining() < 4 + length) {
                buffer.reset();
                return null;
            }

            byte[] mask = new byte[4];
            buffer.get(mask);
            buffer.get(data);

            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (data[i] ^ mask[i % 4]);
            }
        }else{

            if(buffer.remaining() < length){
                buffer.reset();
                return null;
            }
            buffer.get(data);
        }

        return data;



//        if (payloadLength == 126) {
//
//            if(buffer.remaining()<2){
//                //消息头不完整
//                buffer.reset();
//                return null;
//            }
//
//            byte[] extended = new byte[2];
//            buffer.get(extended, 0, 2);
//
//            int shift = 0;
//            payloadLength = 0;
//            for (int i = extended.length - 1; i >= 0; i--) {
//                payloadLength = payloadLength + ((extended[i] & 0xFF) << shift);
//                shift += 8;
//            }
//
//        } else if (payloadLength == 127) {
//            if(buffer.remaining()<2){
//                //消息头不完整
//                buffer.reset();
//                return null;
//            }
//            byte[] extended = new byte[8];
//            buffer.get(extended, 0, 8);
//            int shift = 0;
//            payloadLength = 0;
//            for (int i = extended.length - 1; i >= 0; i--) {
//                payloadLength = payloadLength + ((extended[i] & 0xFF) << shift);
//                shift += 8;
//            }
//        }
//
//        ByteBuffer byteBuf = ByteBuffer.allocate(payloadLength);
//
//        if(ismask==1){
//            if(buffer.remaining()< 4 + payloadLength){
//                buffer.reset();
//                return null;
//            }
//
//            byte[] mask = new byte[4];
//            buffer.get(mask, 0, 4);
//            int readThisFragment = 1;
//
//            while(payloadLength > 0){
//                int masked = buffer.get();
//                masked = masked ^ (mask[(int) ((readThisFragment - 1) % 4)] & 0xFF);
//                byteBuf.put((byte) masked);
//                payloadLength--;
//                readThisFragment++;
//            }
//
//        }else{
//            if(buffer.remaining() < payloadLength){
//                buffer.reset();
//                return null;
//            }
//            buffer.get(byteBuf.array());
//        }

        //掩码

//        return byteBuf.array();
    }

}
