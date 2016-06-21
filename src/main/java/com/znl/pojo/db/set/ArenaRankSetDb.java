package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 竞技场排行集合
 * Created by Administrator on 2016/1/14.
 */
public class ArenaRankSetDb extends BaseSetDbPojo{
    //通过类型去拿到排行的数据 playerId_rankValue
    public List<String> getAllArenaRank(){
        List<String> rankList = new ArrayList<>();

        setMap.keySet().forEach( key -> {
            rankList.add(key + "_" + setMap.get(key));
        });

        return rankList;
    }
}
