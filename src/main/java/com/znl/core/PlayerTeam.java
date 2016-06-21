package com.znl.core;

import com.znl.base.BasicBattleAttr;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/11/9.
 */
public class PlayerTeam extends BasicBattleAttr implements Serializable {

    public long playerId;
    public HashMap<Integer,Object> powerMap = new HashMap<>();
    public HashMap<Integer,Object> basePowerMap = new HashMap<>();
    public HashMap<Integer,Object> capacityMap = new HashMap<>();
    public boolean reset = false;
    public PlayerTeam(HashMap<Integer,Object> basePowerMap,long id){
        this.basePowerMap = basePowerMap;
        playerId = id;
        init();
    }


    public void init(){
        powerMap = new HashMap<Integer, Object>(basePowerMap);
    }


    @Override
    public int getSkillFixdam(int skillId){

        return 0;
    }

    @Override
    public Object getValue(Integer key){
        return powerMap.get(key);
    }

    @Override
    public void setValue(Integer key, Object value){
        powerMap.put(key, value);
    }

    @Override
    public void reset(){
        if(reset){
            init();
        }

    }
}
