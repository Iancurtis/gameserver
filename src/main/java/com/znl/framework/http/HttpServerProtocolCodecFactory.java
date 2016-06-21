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

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a protocol codec for HTTP server.
 * 
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev: 555855 $, $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $
 */

public class HttpServerProtocolCodecFactory  implements ProtocolCodecFactory{
	private static Logger logger = LoggerFactory.getLogger(HttpServerProtocolCodecFactory.class);
	private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;
    
	public HttpServerProtocolCodecFactory(ProtocolEncoder encoder, ProtocolDecoder decoder) {
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