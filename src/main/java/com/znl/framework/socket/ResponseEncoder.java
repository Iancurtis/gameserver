package com.znl.framework.socket;

import com.google.protobuf.GeneratedMessage;
import com.znl.utils.ZipUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.io.DataOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

/**
 * Created by Administrator on 2015/10/22.
 */
public class ResponseEncoder extends ProtocolEncoderAdapter {

    private final AttributeKey ENCODER = new AttributeKey(getClass(), "encoder");
    private int maxLineLength = Integer.MAX_VALUE;

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws Exception {

        Boolean firstResponse = (Boolean) session.getAttribute("FIRST_RESPONSE_KEY");
        if(firstResponse == null)
        {
            firstResponse = true;
            session.setAttribute("FIRST_RESPONSE_KEY", false);
        }

        if(firstResponse == true)
        {
            CharsetEncoder encoder = (CharsetEncoder) session.getAttribute(ENCODER);

            if (encoder == null) {
                encoder = (Charset.forName("UTF-8")).newEncoder();
                session.setAttribute(ENCODER, encoder);
            }

            String value = (message == null ? "" : message.toString());
            IoBuffer buf = IoBuffer.allocate(value.length())
                    .setAutoExpand(true);
            buf.putString(value, encoder);

            buf.flip();
            out.write(buf);
        }
        else
        {
            int compressSize = 1024; //大于1024才压缩
            if(message instanceof List){
                List<Response> responses = (List<Response>)message;

                java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
//                dataOutputStream.writeByte(responses.size());

                for(Response response : responses){
                    GeneratedMessage gmessage = (GeneratedMessage)response.getValue();
                    byte[] bytes = gmessage.toByteArray();
                    dataOutputStream.writeInt(bytes.length);
                    dataOutputStream.writeInt(response.getModule());
                    dataOutputStream.writeInt(response.getCmd());
                    dataOutputStream.write(bytes);

//                    System.out.println("===发送的数据协议=====moduleId:"+response.getModule()+"==cmdId:="+ response.getCmd());
                }

                byte[] zipBytes = byteArrayOutputStream.toByteArray();
                int isZip = 0;
                if(zipBytes.length >= compressSize){
                    isZip = 1;
                    zipBytes = ZipUtils.compress(byteArrayOutputStream.toByteArray());
                }else {
                    isZip = 0;
                }

                byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                dataOutputStream.writeByte(responses.size());
                dataOutputStream.writeByte(isZip);  //0不压缩，1压缩
                dataOutputStream.write(zipBytes);

                byte[] sendData = packData(byteArrayOutputStream.toByteArray());
                IoBuffer buffer = IoBuffer.allocate(sendData.length);
                buffer.setAutoExpand(true);
                buffer.put(sendData);
                buffer.flip();
                buffer.free();
                out.write(buffer);
                out.flush();
            }

            if(message instanceof Response)
            {
                Response response = (Response)message;
                GeneratedMessage gmessage = (GeneratedMessage)response.getValue();

                java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
//                dataOutputStream.writeByte(0);
                dataOutputStream.writeInt(response.getModule());
                dataOutputStream.writeInt(response.getCmd());
                dataOutputStream.write(gmessage.toByteArray());

                byte[] zipBytes = byteArrayOutputStream.toByteArray();
                int isZip = 0;
                if(zipBytes.length >= compressSize){
                    isZip = 1;
                    zipBytes = ZipUtils.compress(byteArrayOutputStream.toByteArray());
                }else {
                    isZip = 0;
                }

                byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                dataOutputStream.writeByte(0);
                dataOutputStream.writeByte(isZip);  //0不压缩，1压缩
                dataOutputStream.write(zipBytes);

                byte[] sendData = packData(byteArrayOutputStream.toByteArray());

                //byte[] sendData = webSocket.getPackData(byteArrayOutputStream.toByteArray());
//                System.out.println("===发送的数据协议=====moduleId:"+response.getModule()+"==cmdId:="+ response.getCmd());

                IoBuffer buffer = IoBuffer.allocate(sendData.length);
                buffer.setAutoExpand(true);
                buffer.put(sendData);
                buffer.flip();
                buffer.free();
                out.write(buffer);
                out.flush();
            }

        }
    }

    //打包服务器数据
    private  byte[] packData(String message){
        byte[] contentBytes = packData(message.getBytes());
        return contentBytes;
    }


    private  byte[] packData(byte[] temp){
        byte[] contentBytes = null;
        if (temp.length < 126){
            contentBytes = new byte[temp.length + 2];
            contentBytes[0] = (byte) 0x81;
            contentBytes[1] = (byte)temp.length;
            System.arraycopy(temp, 0, contentBytes, 2, temp.length);
        }else if (temp.length < 0xFFFF){
            contentBytes = new byte[temp.length + 4];
            contentBytes[0] = (byte) 0x81;
            contentBytes[1] = 126;
            contentBytes[2] = (byte)(temp.length >> 8 & 0xFF);
            contentBytes[3] = (byte)(temp.length & 0xFF);
            System.arraycopy(temp, 0, contentBytes, 4, temp.length);
        }else{
//        	contentBytes = new byte[temp.length + 10];
//        	contentBytes[0] = (byte) 0x81;
//        	contentBytes[1] = 127;
//        	contentBytes[2] = (byte)(temp.length >> 56 & 0xFF);
//        	contentBytes[3] = (byte)(temp.length >> 48 & 0xFF);
//        	contentBytes[4] = (byte)(temp.length >> 40 & 0xFF);
//        	contentBytes[5] = (byte)(temp.length >> 32 & 0xFF);
//        	contentBytes[6] = (byte)(temp.length >> 24 & 0xFF);
//        	contentBytes[7] = (byte)(temp.length >> 16 & 0xFF);
//        	contentBytes[8] = (byte)(temp.length >> 8 & 0xFF);
//        	contentBytes[9] = (byte)(temp.length & 0xFF);
//        	System.arraycopy(temp, 0, contentBytes, 10, temp.length);
            contentBytes = new byte[temp.length + 10];
            contentBytes[0] = (byte) 0x81;
            contentBytes[1] = 127;
            contentBytes[2] = 0;
            contentBytes[3] = 0;
            contentBytes[4] = 0;
            contentBytes[5] = 0;
            contentBytes[6] = (byte)(temp.length >>> 24);
            contentBytes[7] = (byte)(temp.length >>> 16);
            contentBytes[8] = (byte)(temp.length >>> 8);
            contentBytes[9] = (byte)(temp.length & 0xFF);
            System.arraycopy(temp, 0, contentBytes, 10, temp.length);

        }

        return contentBytes;
    }

}