package com.znl.framework.socket.factory;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2015/10/22.
 */
public class CommonCodecFactory  implements ProtocolCodecFactory {
    private static Logger logger = LoggerFactory.getLogger(CommonCodecFactory.class);
    private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;

    public CommonCodecFactory(ProtocolEncoder encoder, ProtocolDecoder decoder) {
        if (encoder == null) {
            logger.error("ProtocolEncoder is null!");
            throw new NullPointerException("ProtocolEncoder is null!");
        }

        if (decoder == null) {
            logger.error("ProtocolDecoder is null!");
            throw new NullPointerException("ProtocolDecoder is null!");
        }

        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
        return this.decoder;
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
        return this.encoder;
    }

}
