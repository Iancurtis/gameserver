package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.pojo.db.LimitDungeoReport;

import java.util.Set;

/**
 * Created by Administrator on 2016/1/20.
 */
public class LimitDungeonFastSetDb extends BaseSetDbPojo {

    public void addLimitDungeonSet(int dungeoId,long battleId,long playerId,long time){
        String key = dungeoId + "";
        if(this.isKeyExist(key)){
            return;
        }else {
            LimitDungeoReport dungeoReport = this.createDbPojo(key,LimitDungeoReport.class);
            dungeoReport.setBattleId(battleId);
            dungeoReport.setDungeoId(dungeoId);
            dungeoReport.setPlayerId(playerId);
            dungeoReport.setTime(time);
            dungeoReport.save();
        }
    }

    public LimitDungeoReport getLimitDungeonFastReport(int dungeoId){
        String key = dungeoId + "";
        if(this.isKeyExist(key) == false){
            return null;
        }else {
            LimitDungeoReport dungeoReport = this.getDbPojoByKey(key, LimitDungeoReport.class);
            return dungeoReport;
        }
    }

}
