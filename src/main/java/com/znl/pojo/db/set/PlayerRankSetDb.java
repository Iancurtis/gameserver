package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家各种排行集合
 * Created by Administrator on 2016/1/14.
 */
public class PlayerRankSetDb extends BaseSetDbPojo {

    //通过类型去拿到排行的数据 playerId_rankValue
    public List<String> getAllRankByType(Integer rankType){
        List<String> rankList = new ArrayList<>();

        setMap.keySet().forEach( key -> {
            String[] tmp = key.split("_");
            if(tmp[1].equals(rankType.toString())){
                rankList.add(tmp[0] + "_" + setMap.get(key));
            }
        });

        return rankList;
    }


}
