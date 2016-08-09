package com.znl.modules.skill;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BaseLog;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.core.PlayerTask;
import com.znl.define.ActorDefine;
import com.znl.define.DataDefine;
import com.znl.define.FunctionIdDefine;
import com.znl.define.ProtocolModuleDefine;
import com.znl.framework.socket.Request;
import com.znl.log.SkillLog;
import com.znl.msg.GameMsg;
import com.znl.pojo.db.Skill;
import com.znl.proto.M12;
import com.znl.proto.M2;
import com.znl.proto.M3;
import com.znl.proxy.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/26.
 */
public class SkillModule extends BasicModule {

    public static Props props(final GameProxy gameProxy) {
        return Props.create(new Creator<SkillModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public SkillModule create() throws Exception {
                return new SkillModule(gameProxy);
            }
        });
    }

    public SkillModule(GameProxy gameProxy) {
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M12);
    }

    @Override
    public void onReceiveOtherMsg(Object anyRef) {

    }

    /**
     * 技能信息 该协议已经屏蔽迁移到20000
     */
    private void OnTriggerNet120000Event(Request request) {
        SkillProxy skillProxy = this.getProxy(ActorDefine.SKILL_PROXY_NAME);
        M12.M120000.S2C.Builder s2c = M12.M120000.S2C.newBuilder();
        s2c.addAllSkillInfos(skillProxy.getAllSkillInfo());
        s2c.setRs(0);
        sendNetMsg(ProtocolModuleDefine.NET_M12, ProtocolModuleDefine.NET_M12_C120000, s2c.build());
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M12_C120000);
    }

    /**
     * 技能升级
     */
    private void OnTriggerNet120001Event(Request request) {
        SkillProxy skillProxy = this.getProxy(ActorDefine.SKILL_PROXY_NAME);
        M12.M120001.C2S c2s = request.getValue();
        int skillId = c2s.getSkillId();
        int type = c2s.getType();
        List<Integer> itemList = new ArrayList<Integer>();
        List<SkillLog> log = new ArrayList<SkillLog>();
        int rs = skillProxy.skillLevelUp(skillId, type, itemList, log);
        M12.M120001.S2C.Builder s2c = M12.M120001.S2C.newBuilder();
        s2c.setRs(rs);
        s2c.setSkillInfo(skillProxy.getSkillInfo(skillId));
        sendNetMsg(ProtocolModuleDefine.NET_M12, ProtocolModuleDefine.NET_M12_C120001, s2c.build());
        if (rs == 0) {// 向客户端推送道具刷新
            if (itemList.size() > 0) {
                RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                M2.M20007.S2C msg = rewardProxy.getItemListClientInfo(itemList);
                sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg);
            }
            //日志记录
           for(SkillLog lg : log){
               sendLog(lg);
           }

        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M12_C120001);
    }

    /**
     * 技能重置
     */
    private void OnTriggerNet120002Event(Request request) {
        SkillProxy skillProxy = this.getProxy(ActorDefine.SKILL_PROXY_NAME);
        List<Integer> itemList = new ArrayList<Integer>();
        int rs = skillProxy.resetSkill(itemList);
        M12.M120002.S2C.Builder s2c = M12.M120002.S2C.newBuilder();
        s2c.setRs(rs);
        s2c.addAllSkillInfo(skillProxy.getAllSkillInfo());
        sendNetMsg(ProtocolModuleDefine.NET_M12, ProtocolModuleDefine.NET_M12_C120002, s2c.build());
        if (rs == 0) {// 向客户端推送道具刷新
            if (itemList.size() > 0) {
                RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
                M2.M20007.S2C msg = rewardProxy.getItemListClientInfo(itemList);
                sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, msg);
            }
            //日志记录
            BaseLog log = new SkillLog(0, 0, 0, 0, ActorDefine.MIN_RESET_SKILL);
            sendLog(log);
            sendFuntctionLog(FunctionIdDefine.SKILL_INITIALIZE_FUNCTION_ID);
//            sendPushNetMsgToClient();
        }
        sendPushNetMsgToClient(ProtocolModuleDefine.NET_M12_C120002);
    }

    /**
     * 重复协议请求处理
     * @param request
     */
    @Override
    public void repeatedProtocalHandler(Request request) {

    }
}
