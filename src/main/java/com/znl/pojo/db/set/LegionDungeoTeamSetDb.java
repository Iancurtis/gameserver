package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.core.PlayerTroop;
import com.znl.define.SoldierDefine;
import com.znl.pojo.db.TeamDate;
import com.znl.utils.GameUtils;

/**
 * 队伍集合数据
 * Created by Administrator on 2016/1/14.
 */
public class LegionDungeoTeamSetDb extends BaseSetDbPojo {

    public void addTeamDate( PlayerTroop team, long id, int dungeoid ){
        String key = id + "_" + dungeoid+"_"+ SoldierDefine.FORMATION_LEGION_DUNGEO;
        TeamDate teamDate = null;
        if(this.isKeyExist(key)){
            teamDate = this.getDbPojoByKey(key, TeamDate.class);
        }else {
            teamDate = this.createDbPojo(key, TeamDate.class);
        }

        byte[] bytes = GameUtils.objectToBytes(team);
        teamDate.setTeam(bytes);
        teamDate.save();
    }

    public PlayerTroop getTeamData(Long legionId, int dongeoid){
        String key = legionId + "_" +dongeoid +"_"+ SoldierDefine.FORMATION_LEGION_DUNGEO;
        if(this.isKeyExist(key)){
            TeamDate teamDate =  this.getDbPojoByKey(key, TeamDate.class);
            if(teamDate==null){
                return null;
            }
            Object obj  = GameUtils.ByteToObject(teamDate.getTeam());

            return (PlayerTroop)obj;
        }

        return null;
    }
}
