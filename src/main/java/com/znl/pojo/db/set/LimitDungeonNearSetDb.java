package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.pojo.db.LimitDungeoReport;

import java.util.*;

/**
 * Created by Administrator on 2016/1/20.
 */
public class LimitDungeonNearSetDb extends BaseSetDbPojo {

    public void addLimitDungeonSet(int dungeoId,long battleId,long playerId,long time){
        Set<String> allKey = getAllKey();
        Set<String> keys =  new HashSet<>();
        for (String key:allKey){
            String[] strs = key.split("_");
            if (Integer.parseInt(strs[0]) == dungeoId){
                keys.add(key);
            }
        }
        if (keys.size() < 3){
            String key = dungeoId+"_"+(keys.size()+1);
            LimitDungeoReport dungeoReport = this.createDbPojo(key,LimitDungeoReport.class);
            dungeoReport.setBattleId(battleId);
            dungeoReport.setDungeoId(dungeoId);
            dungeoReport.setPlayerId(playerId);
            dungeoReport.setTime(time);
            dungeoReport.save();
        }else {
            List<Long> values = getAllValue();
            long minTime = 0l;
            LimitDungeoReport dungeoReport = null;
            for (String key : keys){
                LimitDungeoReport report = getDbPojoByKey(key,LimitDungeoReport.class);
                if (minTime == 0){
                    minTime = report.getTime();
                    dungeoReport = report;
                }else if(report.getTime() < minTime){
                    minTime = report.getTime();
                    dungeoReport = report;
                }
            }
            dungeoReport.setBattleId(battleId);
            dungeoReport.setDungeoId(dungeoId);
            dungeoReport.setPlayerId(playerId);
            dungeoReport.setTime(time);
            dungeoReport.save();
        }
    }

    public List<LimitDungeoReport> getLimitDungeoReports(){
        Set<String> keys = getAllKey();
        List<LimitDungeoReport> res = new ArrayList<>(3);
        for (String key : keys){
            LimitDungeoReport report = getDbPojoByKey(key,LimitDungeoReport.class);
            res.add(report);
        }
        return res;
    }
}
