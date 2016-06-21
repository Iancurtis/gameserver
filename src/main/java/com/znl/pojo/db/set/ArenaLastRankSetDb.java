package com.znl.pojo.db.set;

import com.znl.base.BaseSetDbPojo;
import com.znl.pojo.db.LimitDungeoReport;

import java.util.ArrayList;
import java.util.List;

/**
 * 竞技场上一次排行集合
 * Created by Administrator on 2016/1/14.
 */
public class ArenaLastRankSetDb extends BaseSetDbPojo {

    public List<String> getAllArenaRank(){
        List<String> rankList = new ArrayList<>();

        setMap.keySet().forEach( key -> {
            rankList.add(key + "_" + setMap.get(key));
        });

        return rankList;
    }
}
