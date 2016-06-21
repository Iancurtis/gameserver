package com.znl.framework.socket;

import java.io.Serializable;

/**
 * 解析协议上下文
 * Created by Administrator on 2015/12/10.
 */


public class CodecContext implements Serializable {

    private int bytesNeeded = 0;
    private DecoderState state = DecoderState.WAITING_DATA;
    private int remaining = 0;

    private byte[] buffer;

    public int getBytesNeeded() {
        return bytesNeeded;
    }

    public void setBytesNeeded(int bytesNeeded) {
        this.bytesNeeded = bytesNeeded;
    }

    public DecoderState getState() {
        return state;
    }

    public void setState(DecoderState state) {
        this.state = state;
    }

    public int getRemaining(){
        return this.remaining;
    }

    //接受到数据，先缓存起来
    public void revData(byte[] buffer){
        if(this.buffer == null){
            this.buffer = buffer;
        }else{
            byte[] newBuffer = new byte[this.buffer.length + buffer.length];
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
            System.arraycopy(buffer, 0, newBuffer, this.buffer.length, buffer.length);
            this.buffer = newBuffer;
        }

        this.remaining = this.bytesNeeded - this.buffer.length;
    }

    public byte[] getBuffer(){
        return this.buffer;
    }

    /**
     * 是否相同状态
     *
     * @param 	state			需要验证的状态对象
     * @return {@link Boolean}	true-相同状态, false-不相同的状态
     */
    public boolean isSameState(DecoderState state) {
        return this.state != null && state != null && this.state == state;
    }

    public static CodecContext valueOf(int byteNeeded, DecoderState state){
        CodecContext codecContext = new CodecContext();
        codecContext.bytesNeeded = byteNeeded;
        codecContext.state = state;
        codecContext.remaining = byteNeeded;
        return codecContext;
    }
}
