package com.znl.pojo.db.set;

import com.znl.base.BaseDbPojo;
import com.znl.base.BaseSetDbPojo;
import com.znl.core.PlayerTroop;
import com.znl.pojo.db.TeamDate;
import com.znl.utils.GameUtils;

/**
 * 队伍集合数据
 * Created by Administrator on 2016/1/14.
 */
public class HelpTeamDateSetDb extends BaseSetDbPojo {

    public long addTeamDate(PlayerTroop team){
        TeamDate teamDate = BaseDbPojo.create(TeamDate.class,getAreaKey());
        byte[] bytes = GameUtils.objectToBytes(team);
        teamDate.setTeam(bytes);
        teamDate.save();
        return teamDate.getId();
    }

    public void updateTeamDate(PlayerTroop team,long id){
        TeamDate teamDate = BaseDbPojo.get(id, TeamDate.class,getAreaKey());
        if(teamDate!=null) {
            byte[] bytes = GameUtils.objectToBytes(team);
            teamDate.setTeam(bytes);
            teamDate.save();
        }
    }

    public void deleteTeamDate(long id){
        TeamDate teamDate = BaseDbPojo.get(id, TeamDate.class,getAreaKey());
        if(teamDate!=null) {
            teamDate.del();
        }
    }

    public PlayerTroop getTeamData(long id){
            TeamDate teamDate = BaseDbPojo.get(id, TeamDate.class,getAreaKey());
            Object obj  = GameUtils.ByteToObject(teamDate.getTeam());
            if(obj!=null) {
                return (PlayerTroop) obj;
            }
        return null;
    }
}
