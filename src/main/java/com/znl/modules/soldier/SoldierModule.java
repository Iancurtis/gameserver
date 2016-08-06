package com.znl.modules.soldier;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.define.ActorDefine;
import com.znl.define.FunctionIdDefine;
import com.znl.define.ProtocolModuleDefine;
import com.znl.framework.socket.Request;
import com.znl.msg.GameMsg;
import com.znl.proto.Common;
import com.znl.proto.M2;
import com.znl.proto.M4;
import com.znl.proxy.GameProxy;
import com.znl.proxy.SoldierProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/6.
 */
public class SoldierModule  extends BasicModule {

    public static Props props(final GameProxy gameProxy){
        return Props.create(new Creator<SoldierModule>(){
            private static final long serialVersionUID = 1L;
            @Override
            public SoldierModule create() throws Exception {
                return new SoldierModule(gameProxy) ;
            }
        });
    }

    public SoldierModule(GameProxy gameProxy){
        super.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M4);
    }

    @Override
    public void onReceiveOtherMsg(Object object){
        if(object instanceof GameMsg.FixSoldierList){
            pushNetMsg(ActorDefine.SOLDIER_MODULE_ID,ProtocolModuleDefine.NET_M4_C40001,getFixList());
            sendPushNetMsgToClient();
        }
    }

    private M4.M40001.S2C getFixList(){
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        List<M4.FixSoldierInfo> infos = soldierProxy.getAllLostSoldierInfos();
        M4.M40001.S2C.Builder builder = M4.M40001.S2C.newBuilder();
        builder.setRs(0);
        builder.addAllSoldiers(infos);
        return builder.build();
    }

    private void OnTriggerNet40001Event(Request request){
        sendNetMsg(ActorDefine.SOLDIER_MODULE_ID,ProtocolModuleDefine.NET_M4_C40001,getFixList());
        sendFuntctionLog(FunctionIdDefine.LOST_SOLDIER_LIST_FUNCIION_ID);
    }

    private void OnTriggerNet40002Event(Request request){
        M4.M40002.C2S proto = request.getValue();
        int typeId = proto.getTypeid();
        int type = proto.getType();
        SoldierProxy soldierProxy = getProxy(ActorDefine.SOLDIER_PROXY_NAME);
        List<Common.SoldierInfo> soldierInfos = new ArrayList<>();
        int rs = soldierProxy.fixLostSoldier(typeId,type,soldierInfos);
        M4.M40002.S2C.Builder builder = M4.M40002.S2C.newBuilder();
        builder.setRs(rs);
        if(rs >= 0){
            builder.setRs(0);
            builder.addAllSoldiers(soldierProxy.getAllLostSoldierInfos());
            sendModuleMsg(ActorDefine.CAPACITY_MODULE_NAME,new GameMsg.CountCapacity());

        }
        sendNetMsg(ActorDefine.SOLDIER_MODULE_ID,ProtocolModuleDefine.NET_M4_C40002,builder.build());
        if(rs >=0 && soldierInfos.size() > 0){
            sendRefuceSoldierInfo(soldierInfos);
            //阵型
            sendModuleMsg(ActorDefine.TROOP_MODULE_NAME,new GameMsg.CheckBaseDefendFormation());
        }
        sendPushNetMsgToClient();
    }

    private void sendRefuceSoldierInfo(List<Common.SoldierInfo> soldierInfos){
        M2.M20007.S2C.Builder builder = M2.M20007.S2C.newBuilder();
        builder.addAllSoldierList(soldierInfos);
        sendNetMsg(ProtocolModuleDefine.NET_M2,ProtocolModuleDefine.NET_M2_C20007,builder.build());
        sendPushNetMsgToClient();
    }

    /**
     * 重复协议请求处理
     * @param cmd
     */
    @Override
    public void repeatedProtocalHandler(int cmd) {

    }
}
