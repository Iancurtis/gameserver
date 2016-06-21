package com.znl.framework.http;

import org.apache.mina.core.session.IoSession;

/**
 * Created by Administrator on 2015/12/21.
 */
public class HttpMessage {
    private final IoSession httpsession;
    private final HttpRequestMessage httpRequest;

    public HttpMessage(IoSession httpsession, HttpRequestMessage httpRequest) {
        this.httpsession = httpsession;
        this.httpRequest = httpRequest;
    }

    //发送数据
    public void sendContent(String body){
        HttpResponseMessage response = new HttpResponseMessage();
        response.setContentType("text/plain");
        response.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
        response.appendBody(body);
        httpsession.write(response);
    }

    public HttpRequestMessage getHttpRequest() {
        return httpRequest;
    }

    public static HttpMessage valueOf(IoSession httpsession, HttpRequestMessage httpRequest)
    {
        return new HttpMessage(httpsession, httpRequest);
    }
}
