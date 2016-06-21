package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.define.DataDefine;
import com.znl.proxy.ConfigDataProxy;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/14.
 */
public class VipActSetDb extends BaseSetDbPojo {

    //根据玩家vip经验获取玩家vip等级
    public Map<Long,Long> getAllVipExp(){
        Map<Long,Long> vipexpmap = new HashedMap();

        setMap.keySet().forEach( key -> {
            List vipList = ConfigDataProxy.getConfigAllInfo(DataDefine.VIPDATA);
            for (long i = 0; i < vipList.size(); i++) {
                //判断是否为vip12
                if (i != vipList.size() - 1) {
                    JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", i);
                    JSONArray jsonArray = jsonObject.getJSONArray("value");
                    long x=Long.valueOf(String.valueOf(jsonArray.get(1)));
                    JSONObject jsonObject1 = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", i + 1);
                    JSONArray jsonArray1 = jsonObject1.getJSONArray("value");
                    long y=Long.valueOf(String.valueOf(jsonArray1.get(1)));
                    if (setMap.get(key) >= x && setMap.get(key) < y) {
                        vipexpmap.put(Long.parseLong(key), i);
                    }
                } else {
                    JSONObject jsonObject = ConfigDataProxy.getConfigInfoFindByOneKey(DataDefine.VIPDATA, "level", i);
                    JSONArray jsonArray = jsonObject.getJSONArray("value");
                    long z=Long.valueOf(String.valueOf(jsonArray.get(1)));
                    if (setMap.get(key) >= z) {
                        vipexpmap.put(Long.parseLong(key), i);
                    }
                }
            }
        });
        return vipexpmap;
    }

    public long getAllVipExpByplayerId(long playerId) {
        for (String temp : setMap.keySet()) {
            if (Long.parseLong(temp) == playerId) {
                return setMap.get(temp);
            }
        }
        return 0;
    }

}
