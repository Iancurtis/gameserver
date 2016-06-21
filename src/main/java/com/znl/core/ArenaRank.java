package com.znl.core;

/**
 * Created by Administrator on 2015/12/22.
 */
public class ArenaRank {
    public Long playerId;
    public int rankVaalue;
    public String areakey;

    public ArenaRank(Long playerId, int rankVaalue, String areakey) {
        this.playerId = playerId;
        this.rankVaalue = rankVaalue;
        this.areakey = areakey;
    }
}
