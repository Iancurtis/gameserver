package com.znl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/11/13.
 */
public class PlayerReward {
    public HashMap<Integer,Integer> addItemMap = new HashMap<>();
    public HashMap<Integer,Integer> addPowerMap = new HashMap<>();
    public HashMap<Integer,Integer> soldierMap = new HashMap<>();
    public HashMap<Integer,Integer> counsellorMap = new HashMap<>();//军师
    public HashMap<Integer,Integer> generalMap = new HashMap<>();
    public HashMap<Integer,Integer> ordanceMap = new HashMap<>();
    public HashMap<Integer,Integer> ordanceFragmentMap = new HashMap<>();
    public List<Long> generalList = new ArrayList<Long>();
    public List<Long> ordanceList = new ArrayList<Long>();

    /**
     * 判断是否有奖励
     * @return
     */
    public boolean haveReward() {
        if (!addItemMap.isEmpty()
                || !addPowerMap.isEmpty()
                || !soldierMap.isEmpty()
                || !counsellorMap.isEmpty()
                || !generalMap.isEmpty()
                || !ordanceMap.isEmpty()
                || !ordanceFragmentMap.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
