package com.znl.modules.technology;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.define.ProtocolModuleDefine;
import com.znl.framework.socket.Request;
import com.znl.proxy.GameProxy;

/**
 * Created by Administrator on 2015/11/24.
 */
public class TechnologyModule extends BasicModule {

    public static Props props(final GameProxy gameProxy){
        return Props.create(new Creator<TechnologyModule>() {
            private static final long serialVersionUID = 1L;
            @Override
            public TechnologyModule create() throws Exception {
                return new TechnologyModule(gameProxy);
            }
        });
    }

    public TechnologyModule(GameProxy gameProxy) {
        super.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M11);
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {

    }

    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {

    }
}
