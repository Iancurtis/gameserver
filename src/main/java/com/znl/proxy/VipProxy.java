package com.znl.proxy;

import com.znl.base.BasicProxy;
import com.znl.define.ActorDefine;
import com.znl.define.DataDefine;
import com.znl.define.PlayerPowerDefine;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/9.
 */
public class VipProxy extends BasicProxy {
    @Override
    public void shutDownProxy() {

    }

    @Override
    protected void init() {

    }

    public VipProxy(String areaKey){
        this.areaKey = areaKey;
    }

    /**
     * 初始化VIP特权数据
     */
    public void initVipData(){
        super.expandPowerMap.clear();
        JSONObject vipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA,"level",getVipLevel());
        if(vipInfo != null){
            JSONArray jsonArray1 = vipInfo.getJSONArray(ActorDefine.VIP_BOOMLOSS);
            if(jsonArray1.length()>0){//繁荣度扣除比例
                addVipDataToPlayerPower(jsonArray1.getInt(0),jsonArray1.getInt(1));
            }
            JSONArray jsonArray2 = vipInfo.getJSONArray(ActorDefine.VIP_REDBUILDTIME);
            if(jsonArray2.length()>0){//建筑升级提速
                addVipDataToPlayerPower(jsonArray2.getInt(0),jsonArray2.getInt(1));
            }
            JSONArray jsonArray3 = vipInfo.getJSONArray(ActorDefine.VIP_REDSCIENCETIME);
            if(jsonArray3.length()>0){//科技升级提速
                addVipDataToPlayerPower(jsonArray3.getInt(0),jsonArray3.getInt(1));
            }
            JSONArray jsonArray4 = vipInfo.getJSONArray(ActorDefine.VIP_SPEEDUPCOLLECTRES);
            if(jsonArray4.length()>0){//世界资源点采集提速
                addVipDataToPlayerPower(jsonArray4.getInt(0),jsonArray4.getInt(1));
            }
            JSONArray jsonArray5 = vipInfo.getJSONArray(ActorDefine.VIP_REDTANKPRO);
            if(jsonArray5.length()>0){//坦克生成提速
                addVipDataToPlayerPower(jsonArray5.getInt(0),jsonArray5.getInt(1));
            }
            JSONArray jsonArray6 = vipInfo.getJSONArray(ActorDefine.VIP_REDTANKREM);
            if(jsonArray6.length()>0){//坦克改造提速
                addVipDataToPlayerPower(jsonArray6.getInt(0),jsonArray6.getInt(1));
            }
            JSONArray jsonArray7 = vipInfo.getJSONArray(ActorDefine.VIP_SPEEDUPMARCH);
            if(jsonArray7.length()>0){//野外行军速度加速
                addVipDataToPlayerPower(jsonArray7.getInt(0),jsonArray7.getInt(1));
            }
        }
    }


    /**
     * 属性效果加成
     */
    private void addVipDataToPlayerPower(int id, long value) {
        if (super.expandPowerMap.get(id) == null) {
            super.expandPowerMap.put(id, value);
        } else {
            super.expandPowerMap.put(id, super.expandPowerMap.get(id) + value);
        }
    }
    /**
     * 获取当前VIP等级
     */
    public long getVipLevel(){
        GameProxy gameProxy = getGameProxy();
        PlayerProxy playerProxy = gameProxy.getProxy(ActorDefine.PLAYER_PROXY_NAME);
        return playerProxy.getPowerValue(PlayerPowerDefine.POWER_vipLevel);
    }


    /**
     * VIP特权次数
     * 次数
     */
    public int getVipNum(String str){
        JSONObject vipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA,"level",getVipLevel());
        if(vipInfo != null){
            return vipInfo.getInt(str);
        }else {
            return 0;
        }
    }

    /**
     *VIP特权加速
     */
    public List getVipSpeed(String str){
        List list = new ArrayList<>();
        JSONObject vipInfo = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA,"level",getVipLevel());
        if(vipInfo != null){
            JSONArray jsonArray = vipInfo.getJSONArray(str);
            if(jsonArray.length()>0){
                for(int i=0;i<jsonArray.length();i++){
                    list.add(jsonArray.getInt(i));
                }
            }
        }
        return list;
    }

    /**
     * 返回VIP最大等级
     */
    public int getMaxVIPLv(){
        List<JSONObject> vipAllInfo = ConfigDataProxy.getConfigAllInfo(DataDefine.VIPDATA);
        int level = 0;
        if(vipAllInfo != null){
            JSONObject info = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA,"ID",vipAllInfo.size());
             level = info.getInt("level");
        }
        return level;
    }

}
