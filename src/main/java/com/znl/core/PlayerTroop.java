package com.znl.core;

import com.znl.proto.Common;
import com.znl.proto.M7;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2015/12/16.
 */
public class PlayerTroop implements Serializable {
    private Long playerId;
    private List<M7.FormationMember> fightElementInfos = new ArrayList<>();
    private int general;
    private int capity;
    private List<PlayerTeam> playerTeams = new ArrayList<>();
    private long protime;
    private int wintimes;
    public PlayerTroop() {

    }

    public int getGeneral() {
        return general;
    }

    public void setGeneral(int general) {
        this.general = general;
    }

    public int getCapity() {
        return capity;
    }

    public void setCapity(int capity) {
        this.capity = capity;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public List<PlayerTeam> getPlayerTeams() {
        return playerTeams;
    }

    public void setPlayerTeams(List<PlayerTeam> playerTeams) {
        this.playerTeams = playerTeams;
    }

    public long getProtime() {
        return protime;
    }

    public void setProtime(long protime) {
        this.protime = protime;
    }

    public int getWintimes() {
        return wintimes;
    }

    public void setWintimes(int wintimes) {
        this.wintimes = wintimes;
    }



    public List<M7.FormationMember> getFightElementInfos() {
        return fightElementInfos;
    }

    public void setFightElementInfos(List<M7.FormationMember> fightElementInfos) {
        this.fightElementInfos = fightElementInfos;
    }
}
