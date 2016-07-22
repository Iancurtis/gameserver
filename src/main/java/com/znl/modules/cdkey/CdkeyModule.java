package com.znl.modules.cdkey;

import akka.actor.Props;
import akka.japi.Creator;
import com.znl.base.BasicModule;
import com.znl.core.PlayerReward;
import com.znl.define.*;
import com.znl.framework.socket.Request;
import com.znl.log.CustomerLogger;
import com.znl.proto.Common;
import com.znl.proto.M24;
import com.znl.proto.M8;
import com.znl.proxy.ConfigDataProxy;
import com.znl.proxy.GameProxy;
import com.znl.proxy.PlayerProxy;
import com.znl.proxy.RewardProxy;
import com.znl.utils.GameUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/15.
 */
public class CdkeyModule extends BasicModule{

    public static Props props(final GameProxy gameProxy){
        return Props.create(new Creator<CdkeyModule>(){
            private static final long serialVersionUID = 1L;
            @Override
            public CdkeyModule create() throws Exception {
                return new CdkeyModule(gameProxy) ;
            }
        });
    }

    public CdkeyModule(GameProxy gameProxy){
        this.setGameProxy(gameProxy);
        this.setModuleId(ProtocolModuleDefine.NET_M24);
    }


    @Override
    public void onReceiveOtherMsg(Object anyRef) {

    }

    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    private long coldDownTime = 0l;

    private void OnTriggerNet240000Event(Request request){
        M24.M240000.C2S c2S = request.getValue();
        String cdkey = c2S.getCdkey();

        String s = cdkey;
        //解密完解析出来对应的值
        //最后两位是服务器类型
        int id = 0;
        int cdkeyId = 0;
        int rs = 0;
        int rewardId = 0;
        int type = 0;
        long now = GameUtils.getServerDate().getTime();
        if (now < coldDownTime){
            rs = ErrorCodeDefine.M240000_5;
        }
        if (s.length() != 16){
            rs = ErrorCodeDefine.M240000_3;
        }
        //http://127.0.0.1:1980/gcol/?service=Cdkey.GetCdKey&plat_id=1&cd_key=jhMxAWmrnz0FRzoC
        //http://127.0.0.1:1980/gcol/?service=Cdkey.AddCdKey&type_id=1&game_id=102&server_type=1&num=1000
        //http://127.0.0.1:1980/gcol/?service=Cdkey.DelCdKey&cd_key_id=3
        //http://127.0.0.1:1980/gcol/?service=Cdkey.showCdKey&date_1=2016/2/16 00:00:00&date_2=2016/2/16 23:20:37
        //http://127.0.0.1:1980/gcol/?service=Cdkey.DelType&plat_id=2

        //http://203.195.140.103/gcol/?service=Cdkey.GetCdKey&plat_id=1&cd_key=1111222233334444
        //http://203.195.140.103/gcol/?service=Cdkey.AddCdKey&type_id=1&game_id=102&server_type=1&num=1000
        //http://203.195.140.103/gcol/?service=Cdkey.DelCdKey&cd_key_id=3
        //http://203.195.140.103/gcol/?service=Cdkey.showCdKey&date_1=2016/2/16 00:00:00&date_2=2016/2/16 23:20:37
        //http://203.195.140.103/gcol/?service=Cdkey.DelType&plat_id=2
        PlayerProxy playerProxy = getProxy(ActorDefine.PLAYER_PROXY_NAME);
        String result = sendGet("http://"+ GameUtils.getCenterServerIp()+"/gcol/","service=Cdkey.GetCdKey"+"&cd_key="+cdkey+"&plat_id="+playerProxy.getPlayerCache().getPlat_id());
        System.out.println(result);
        if (result.startsWith("false")){
            rs = ErrorCodeDefine.M240000_4;
        }else {
            try {
                JSONObject resObj = new JSONObject(result);
                JSONObject dataJson = resObj.getJSONObject("data");
                cdkeyId = dataJson.getInt("id");
                id = dataJson.getInt("cdkeyId");
                int serverType = dataJson.getInt("server_type");
                int gameId = dataJson.getInt("game_id");
                if (serverType != GameUtils.getServerType()){
                    rs = ErrorCodeDefine.M240000_4;
                }else if(gameId != GameUtils.getGameId()){
                    rs = ErrorCodeDefine.M240000_4;
                }
            }catch (Exception e){
                rs = ErrorCodeDefine.M240000_3;
                CustomerLogger.error("解析cdkey结果的时候出错啦",e);
            }
        }

        if (rs == 0){
            JSONObject define = ConfigDataProxy.getConfigInfoFindById(DataDefine.CDK,id);
            if (define == null){
                rs = ErrorCodeDefine.M240000_2;
            }else {
                type = define.getInt("type");
                int times = playerProxy.getCdKeyTimes(type);
                rewardId = define.getInt("rewardID");
                if (times >= define.getInt("time")){
                    rs = ErrorCodeDefine.M240000_1;
                }
            }
        }
        M24.M240000.S2C.Builder builder = M24.M240000.S2C.newBuilder().setRs(rs);
        if (rs == 0){
            RewardProxy rewardProxy = getProxy(ActorDefine.REWARD_PROXY_NAME);
            PlayerReward reward = new PlayerReward();
            rewardProxy.getPlayerReward(rewardId, reward);
            rewardProxy.getRewardToPlayer(reward, LogDefine.GET_CDKEY);
            sendNetMsg(ActorDefine.ROLE_MODULE_ID, ProtocolModuleDefine.NET_M2_C20007, rewardProxy.getRewardClientInfo(reward));
            List<Common.RewardInfo> infos = new ArrayList<>();
            rewardProxy.getRewardInfoByReward(reward,infos);
            builder.addAllRewards(infos);
            //记录cdkey的领取次数
            playerProxy.addCdKeyTimes(type);
            //请求中央服删除该cdkey
            result = sendGet("http://"+ GameUtils.getCenterServerIp()+"/gcol/","service=Cdkey.DelCdKey"+"&cd_key_id="+cdkeyId);
            System.out.println(result);
        }else {
            coldDownTime = GameUtils.getServerDate().getTime() + 60*1000;
        }
        sendNetMsg(ActorDefine.CDKEY_MODULE_ID,ProtocolModuleDefine.NET_M24_C240000,builder.build());
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
