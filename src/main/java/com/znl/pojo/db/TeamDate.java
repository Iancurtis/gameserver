package com.znl.pojo.db;

import com.znl.base.BaseDbPojo;

/**
 * Created by Administrator on 2016/1/14.
 */
public class TeamDate extends BaseDbPojo {
    private byte[] team=null;

    public byte[] getTeam() {
        return team;
    }

    public void setTeam(byte[] team) {
        this.team = team;
    }
}
