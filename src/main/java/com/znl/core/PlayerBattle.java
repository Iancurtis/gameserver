package com.znl.core;

import com.google.protobuf.GeneratedMessage;
import com.znl.define.PlayerPowerDefine;
import com.znl.proto.M5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/12.
 */
public class PlayerBattle {
    public PlayerBattle(){

    }
    public int totalCapacity = 0;
    public int totalSoldierNum = 0;
    public int id;
    public List<PlayerTeam> soldierList ;
    public List<PlayerTeam> monsterList ;
    public int type=0;
    public boolean battleResult = false;				//战斗结果：胜利还是失败
    public PlayerReward reward = new PlayerReward();
    public int roundCount = 0;				//总回合数
    public int waitSeconds = 0;				//等待的秒数
    public int infoType = 0;
    public int rs = 0;
    public List<M5.Round> _roundDataList = null;
    public int star = 0;
    public int cmd;
    public int saveTraffic;
    //世界攻打战斗相关
    public int x =0;
    public int y =0;
    public long attackId;
    public long defendId;
    public String attackName;
    public String defendName;
    public GeneratedMessage message;
    public int bgIcon = 0;
    public Map<Integer, Long> powerMap=new HashMap<Integer, java.lang.Long>(){{put(PlayerPowerDefine.NOR_POWER_speedRate,0l);put(PlayerPowerDefine.NOR_POWER_resexprate,0l);put(PlayerPowerDefine.NOR_POWER_rescollectrate,0l);put(PlayerPowerDefine.NOR_POWER_loadRate,0l);}};
}
