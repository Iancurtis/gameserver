package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.core.PlayerTroop;
import com.znl.pojo.db.TeamDate;
import com.znl.utils.GameUtils;

/**
 * 队伍集合数据
 * Created by Administrator on 2016/1/14.
 */
public class TeamDateSetDb extends BaseSetDbPojo {

    public void addTeamDate( PlayerTroop team, Long playerId, Integer teamType ){
        String key = teamType + "_" + playerId;
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

    public PlayerTroop getTeamData(Long playerId, Integer teamType){
        String key = teamType + "_" + playerId;
        if(this.isKeyExist(key)){
            TeamDate teamDate =  this.getDbPojoByKey(key, TeamDate.class);
            Object obj  = GameUtils.ByteToObject(teamDate.getTeam());

            return (PlayerTroop)obj;
        }

        return null;
    }
}
